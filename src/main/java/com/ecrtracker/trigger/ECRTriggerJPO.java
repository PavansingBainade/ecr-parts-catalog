package com.ecrtracker.trigger;

import com.ecrtracker.exception.InvalidStatusTransitionException;

import java.util.List;
import java.util.Map;

public class ECRTriggerJPO {

    public void validateTransition(
            String currentStatus,
            String newStatus,
            Map<String, List<String>> allowedTransitions) {

        List<String> allowedNextStates =
                allowedTransitions.get(currentStatus);

        if (allowedNextStates == null
                || !allowedNextStates.contains(newStatus)) {

            throw new InvalidStatusTransitionException(
                    currentStatus,
                    newStatus
            );
        }
    }
}