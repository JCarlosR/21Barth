package com.gamevenez.a21barth.a21barth.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.gamevenez.a21barth.a21barth.model.Card;
import com.gamevenez.a21barth.a21barth.model.Value;

public class Scorer {
    private final static Scorer instance = new Scorer();

    private Scorer() {}

    public static Scorer getInstance() { return instance; }

    public int calculate(List<Card> cards) {
        int sum = 0;
        List<Value> values = new ArrayList<>(cards.size());
        for (Card card : cards) {
            values.add(card.getValue());
        }
        Collections.sort(values, new Comparator<Value>() {
            @Override
            public int compare(Value lhs, Value rhs) {
                return rhs.getPoints() - lhs.getPoints();
            }
        });
        for (Value value : values) {
            if (value.getValue() == 1) {
                if (sum > 10) {
                    sum += 1;
                } else {
                    sum += 11;
                }
            } else {
                sum += value.getPoints();
            }
        }
        return sum;
    }
}
