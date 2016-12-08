package com.gamevenez.a21barth.a21barth.model;

public enum Value {
    ACE(1, "A"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J", 10),
    QUEEN(12, "Q", 10),
    KING(13, "K", 10);

    private int value;
    private String name;
    private int points;

    Value(int value, String name) {
        this.value = value;
        this.name = name;
        this.points = value;
    }

    Value(int value, String name, int points) {
        this.value = value;
        this.name = name;
        this.points = points;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }
}
