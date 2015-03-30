package com.github.chaoticweg.DropAll.gui;

import java.awt.*;

public class Gui {

    private static int lineHeight = 15;
    private static int upperBound = 40, lowerBound = 355, leftBound = 10;

    public static void drawTitle(Graphics2D g, String title) {
        g.drawString(title, leftBound, upperBound);
    }

    public static void drawInfo(Graphics2D g, String... messages) {
        // calculate start point
        int calcHeight = lineHeight * messages.length + 10; // add 10 for padding
        int yStart = lowerBound - calcHeight;

        // start from start point and move down by lineHeight for each message
        int yCur = yStart;
        for (String msg : messages) {
            g.drawString(msg, leftBound, yCur);
            yCur += lineHeight;
        }
    }

}
