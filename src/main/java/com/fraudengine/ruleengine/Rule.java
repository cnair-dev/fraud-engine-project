package com.fraudengine.ruleengine;


public interface Rule {
    String getName();
    RuleResult evaluate(RuleContext ctx);
}
