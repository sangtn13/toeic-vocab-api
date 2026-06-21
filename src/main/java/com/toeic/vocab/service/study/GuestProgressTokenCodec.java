package com.toeic.vocab.service.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.vocab.exception.progress.StudyProgressNotFoundException;
import io.jsonwebtoken.io.Decoders;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.toeic.vocab.util.AppTime;

@Component
public class GuestProgressTokenCodec {

    private static final String TOKEN_PREFIX = "gpt";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final ObjectMapper objectMapper;

    @Value("${toeic.app.jwtSecret}")
    private String jwtSecret;

    @Value("${toeic.app.guestProgressExpirationMs:2592000000}")
    private long guestProgressExpirationMs;

    public GuestProgressTokenCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public StudyProgressState create(String displayName, String clientKey) {
        LocalDateTime now = LocalDateTime.now(AppTime.ZONE_ID);
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = Map.of();
        String token = encodePayload(new GuestProgressTokenPayload(
                displayName,
                clientKey,
                toEpochMillis(now),
                null,
                Map.of()));
        return new StudyProgressState(token, false, null, displayName, clientKey, now, null,
                progressByVocabularyId);
    }

    public StudyProgressState decode(String progressToken) {
        if (!StringUtils.hasText(progressToken)) {
            throw new StudyProgressNotFoundException(progressToken);
        }

        try {
            String[] parts = progressToken.split("\\.");
            if (parts.length != 3 || !TOKEN_PREFIX.equals(parts[0])) {
                throw new StudyProgressNotFoundException(progressToken);
            }

            byte[] compressedPayload = Base64.getUrlDecoder().decode(parts[1]);
            byte[] providedSignature = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expectedSignature = sign(compressedPayload);
            if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
                throw new StudyProgressNotFoundException(progressToken);
            }

            byte[] payloadBytes = gunzip(compressedPayload);
            GuestProgressTokenPayload payload = objectMapper.readValue(payloadBytes,
                    GuestProgressTokenPayload.class);
            LocalDateTime createdAt = fromEpochMillis(payload.ca());
            if (createdAt == null) {
                throw new StudyProgressNotFoundException(progressToken);
            }

            Instant expiration = createdAt.atZone(AppTime.ZONE_ID).toInstant().plusMillis(guestProgressExpirationMs);
            if (expiration.isBefore(Instant.now())) {
                throw new StudyProgressNotFoundException(progressToken);
            }

            Map<UUID, StudyProgressSnapshot> progressByVocabularyId = new HashMap<>();
            if (payload.pg() != null) {
                for (Map.Entry<String, StatelessProgressPayload> entry : payload.pg().entrySet()) {
                    UUID vocabularyId = UUID.fromString(entry.getKey());
                    StatelessProgressPayload progress = entry.getValue();
                    progressByVocabularyId.put(
                            vocabularyId,
                            new StudyProgressSnapshot(
                                    progress.a() == null ? 0 : progress.a(),
                                    progress.c() == null ? 0 : progress.c(),
                                    Boolean.TRUE.equals(progress.m())));
                }
            }

            return new StudyProgressState(
                    progressToken,
                    false,
                    null,
                    payload.dn(),
                    payload.ck(),
                    createdAt,
                    fromEpochMillis(payload.ls()),
                    Map.copyOf(progressByVocabularyId));
        } catch (IOException | IllegalArgumentException | GeneralSecurityException exception) {
            throw new StudyProgressNotFoundException(progressToken);
        }
    }

    public StudyProgressState reissue(
            StudyProgressState progressState,
            String displayName,
            String clientKey,
            LocalDateTime lastStudiedAt,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        Map<String, StatelessProgressPayload> payloadProgress = new HashMap<>();
        for (Map.Entry<UUID, StudyProgressSnapshot> entry : progressByVocabularyId.entrySet()) {
            StudyProgressSnapshot progress = entry.getValue();
            payloadProgress.put(
                    String.valueOf(entry.getKey()),
                    new StatelessProgressPayload(progress.attemptCount(), progress.correctCount(),
                            progress.mastered()));
        }

        String token = encodePayload(new GuestProgressTokenPayload(
                displayName,
                clientKey,
                toEpochMillis(progressState.createdAt()),
                toEpochMillis(lastStudiedAt),
                payloadProgress));

        return new StudyProgressState(
                token,
                false,
                null,
                displayName,
                clientKey,
                progressState.createdAt(),
                lastStudiedAt,
                Map.copyOf(progressByVocabularyId));
    }

    public boolean isGuestToken(String progressToken) {
        return StringUtils.hasText(progressToken) && progressToken.startsWith(TOKEN_PREFIX + ".");
    }

    private String encodePayload(GuestProgressTokenPayload payload) {
        try {
            byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);
            byte[] compressedPayload = gzip(payloadBytes);
            byte[] signature = sign(compressedPayload);
            return TOKEN_PREFIX
                    + "."
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(compressedPayload)
                    + "."
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (IOException | GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encode guest progress token.", exception);
        }
    }

    private byte[] sign(byte[] payloadBytes) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(Decoders.BASE64.decode(jwtSecret), HMAC_ALGORITHM));
        return mac.doFinal(payloadBytes);
    }

    private byte[] gzip(byte[] payloadBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(payloadBytes);
        }
        return outputStream.toByteArray();
    }

    private byte[] gunzip(byte[] compressedPayload) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedPayload))) {
            return gzipInputStream.readAllBytes();
        }
    }

    private Long toEpochMillis(LocalDateTime value) {
        return value == null ? null : value.atZone(AppTime.ZONE_ID).toInstant().toEpochMilli();
    }

    private LocalDateTime fromEpochMillis(Long value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), AppTime.ZONE_ID);
    }

    private record GuestProgressTokenPayload(
            String dn,
            String ck,
            Long ca,
            Long ls,
            Map<String, StatelessProgressPayload> pg) {
    }

    private record StatelessProgressPayload(
            Integer a,
            Integer c,
            Boolean m) {
    }
}
