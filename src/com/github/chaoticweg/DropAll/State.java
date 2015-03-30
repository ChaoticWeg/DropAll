package com.github.chaoticweg.DropAll;

public enum State {
    WAITING("waiting for input"),
    DROPPING("dropping items");

    private String desc;

    State(String desc) {
        this.desc = desc;
    }
    public String getDesc() {
        return desc;
    }
}
