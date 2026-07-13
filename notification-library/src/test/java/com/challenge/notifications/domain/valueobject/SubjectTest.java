package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubjectTest {

    @Test
    void shouldAcceptAndTrimSubject() {
        Subject subject = new Subject("  Welcome  ");

        assertThat(subject.value()).isEqualTo("Welcome");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectMissingSubject(String value) {
        assertThatThrownBy(() -> new Subject(value))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SUBJECT);
    }

    @Test
    void shouldRejectSubjectLongerThanMaximum() {
        assertThatThrownBy(() -> new Subject("a".repeat(201)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("200");
    }
}
