package com.superclicker.model;
public class Rule{public String name;public ErrorAction onStepError;public int maxRetries;public long retryInterval;
public enum ErrorAction{RETRY,SKIP,JUMP,STOP,PAUSE}
public Rule(){onStepError=ErrorAction.RETRY;maxRetries=3;retryInterval=1000;}}
