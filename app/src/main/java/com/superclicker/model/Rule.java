package com.superclicker.model;

public class Rule {
    public String name;
    public String description;
    public ErrorAction onStepError;
    public int jumpToStep;
    public int maxRetries;
    public long retryInterval;
    public boolean stopOnError;

    public enum ErrorAction {
        RETRY, SKIP, JUMP, STOP, PAUSE
    }

    public Rule() {
        onStepError = ErrorAction.RETRY;
        maxRetries = 3;
        retryInterval = 1000;
        stopOnError = false;
    }
}
