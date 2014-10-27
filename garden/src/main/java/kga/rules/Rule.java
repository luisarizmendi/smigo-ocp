package kga.rules;

import kga.Garden;
import kga.Hint;
import kga.Plant;
import kga.Species;

public interface Rule {
    public static final int ONE_YEAR_BACK = 1;
    public static final int CLOSEST_NEIGHBOURS = 1;

    int getId();

    int getRuleType();

    String getMessageKey();

    Species getHost();

    Hint getHint(Plant plant, Garden garden);
}
