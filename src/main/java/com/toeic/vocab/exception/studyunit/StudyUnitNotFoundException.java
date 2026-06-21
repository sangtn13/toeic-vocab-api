package com.toeic.vocab.exception.studyunit;

import com.toeic.vocab.exception.common.ResourceNotFoundException;
import java.util.UUID;

public class StudyUnitNotFoundException extends ResourceNotFoundException {

    public StudyUnitNotFoundException(UUID unitId) {
        super("Study unit not found with id: " + unitId);
    }
}
