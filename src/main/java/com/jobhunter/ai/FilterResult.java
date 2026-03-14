package com.jobhunter.ai;

public record FilterResult(boolean shouldApply,int matchScore,String reason){}
