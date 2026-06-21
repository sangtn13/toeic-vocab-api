package com.toeic.vocab.request.study;

import jakarta.validation.constraints.Size;

public record ResolveStudyProgressRequest(
    @Size(max = 100, message = "Display name must be at most 100 characters")
    String displayName,

    @Size(max = 6000, message = "Progress token must be at most 6000 characters")
    String progressToken,

    @Size(max = 120, message = "Client key must be at most 120 characters")
    String clientKey
) {
}
