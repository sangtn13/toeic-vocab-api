package com.toeic.vocab.exception.studyset;

import com.toeic.vocab.exception.common.ResourceNotFoundException;

public class StudySetNotFoundException extends ResourceNotFoundException {

    public StudySetNotFoundException(String identifier) {
        super("Study set not found: " + identifier);
    }
}
