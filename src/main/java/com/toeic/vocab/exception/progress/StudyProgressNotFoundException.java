package com.toeic.vocab.exception.progress;

import com.toeic.vocab.exception.common.ResourceNotFoundException;

public class StudyProgressNotFoundException extends ResourceNotFoundException {

    public StudyProgressNotFoundException(String token) {
        super("Study progress token not found: " + token);
    }
}
