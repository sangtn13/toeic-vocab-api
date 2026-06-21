package com.toeic.vocab.exception.vocabulary;

import com.toeic.vocab.exception.common.ResourceNotFoundException;
import java.util.UUID;

public class VocabularyNotFoundException extends ResourceNotFoundException {

    public VocabularyNotFoundException(UUID vocabularyId) {
        super("Vocabulary not found with id: " + vocabularyId);
    }
}
