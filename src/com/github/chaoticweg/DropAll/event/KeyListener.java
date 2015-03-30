package com.github.chaoticweg.DropAll.event;

import com.github.chaoticweg.DropAll.DropAll;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class KeyListener implements java.awt.event.KeyListener {

    public static enum Hotkey {
        SELECT(KeyEvent.VK_F),
        TOGGLE(KeyEvent.VK_D),
        CLEAR(KeyEvent.VK_X);

        // static
        private static HashMap<Integer, Hotkey> by_event = new HashMap<Integer, Hotkey>();

        // instance
        private int e;

        Hotkey(int e) {
            this.e = e;
        }

        public int getEvent() {
            return e;
        }

        public boolean equals(Hotkey other) {
            return this.getEvent() == other.getEvent();
        }

        static {
            for (Hotkey h : values()) {
                by_event.put(h.getEvent(), h);
            }
        }

        public static Hotkey getByEvent(int e) {
            return by_event.get(e);
        }
    }

    private boolean armed = false;
    private int preventRetrigger;
    public boolean isArmed() {
        return armed;
    }

    private DropAll script;

    public KeyListener(DropAll script) {
        this.script = script;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == preventRetrigger)
            return;

        if (armed) {
            Hotkey hk = Hotkey.getByEvent(e.getKeyCode());

            if (hk != null) {
                preventRetrigger = e.getKeyCode();
                script.onHotkey(hk);
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            armed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == preventRetrigger)
            preventRetrigger = -1;

        if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            armed = false;
    }

}
