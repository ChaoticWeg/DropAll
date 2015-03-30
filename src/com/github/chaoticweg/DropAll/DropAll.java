package com.github.chaoticweg.DropAll;

import com.github.chaoticweg.DropAll.event.KeyListener;
import com.github.chaoticweg.DropAll.gui.Gui;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.ArrayList;

// TODO AGGREGATE LIST OF SLOTS CONTAINING ITEM (ints)
// ANTIBAN TODO DROP ITEM IN EACH SLOT (PREVENT ATTEMPTING TO DROP ITEMS SEVERAL TIMES)
// FIXME WHY THE FUCK DOESN'T IT DROP ITEMS

@ScriptManifest(author = "Chaoticw3g",
        info = "Select an item and drop all of it",
        name = "DropAll", version = 0.986, logo = "http://i.imgur.com/be2QZEu.png")

public class DropAll extends Script {

    // state machine
    private State state = State.WAITING;    // wait by default
    public State getCurrentState() {
        return this.state;
    }

    // static constants
    private static final int DROP_TICK_MIN = 600;
    private static final int DROP_TICK_MAX = 900;
    private static final int WAIT_TICK_MIN = 200;
    private static final int WAIT_TICK_MAX = 400;

    // drop processing
    private ArrayList<Integer> SlotsToDrop = new ArrayList<Integer>();
    private String itemToDrop = "";         // the name of the item to drop (NEVER hard code)

    // state-machine booleans
    private boolean hasItems = false;       // do we have any of the items to drop?
    private boolean inputLocked = false;    // have we cleanly transitioned from state to state?


    /* Methods below */

    @Override
    public void onStart() {
        this.getMouse().setSpeed(5);

        this.getBot().getCanvas().addKeyListener(new KeyListener(this));
        this.getBot().setHumanInputEnabled(true);
    }

    @Override
    public void onPaint(Graphics2D g) {
        // draw title, containing current state
        Gui.drawTitle(g, "DropAll v" + this.getVersion() + " - " + state.getDesc());

        // build a string of info lines, separated by \n
        StringBuilder info = new StringBuilder();

        switch (state) {

            case WAITING:
                if (!itemToDrop.equals("")) {
                    // Selected item: Item name [xCount]
                    info.append("Selected item: ").append(itemToDrop)
                            .append(" [x").append(this.getInventory().getAmount(itemToDrop)).append("]\n");
                } else {
                    info.append("No item selected\n");
                }

                // show hotkeys
                if (!itemToDrop.equals("")) {
                    // if we have an item selected
                    info.append("Ctrl-D to drop all\n");
                    info.append("Ctrl-X to clear selection\n");
                } else {
                    // if no item is selected
                    info.append("Hover over item to target\n");
                    info.append("Ctrl-F to select target item\n");
                }
                break;

            case DROPPING:
                info.append("Dropping item: ").append(itemToDrop).append("\n");

                // show 'stop' hotkey and progress
                info.append("Ctrl-D to stop dropping\n");
                info.append(this.getInventory().getAmount(itemToDrop)).append(" left to drop\n");
                break;

        }

        // draw info lines
        Gui.drawInfo(g, info.toString().trim().split("\n"));
    }

    @Override
    public int onLoop() throws InterruptedException {
        // update items status, defaults to false if no item is selected
        if (itemToDrop == null)
            itemToDrop = "";

        hasItems = !itemToDrop.equals("") && this.getInventory().contains(itemToDrop);

        switch (state) {

            case WAITING:
                // if we're locked out (haven't transitioned cleanly)
                if (inputLocked) {
                    // unlock
                    this.getBot().setHumanInputEnabled(true);
                    inputLocked = false;
                }

                // otherwise we're just waiting for input
                return random(WAIT_TICK_MIN, WAIT_TICK_MAX);

            case DROPPING:
                // goes before all other
                if (itemToDrop.equals("")) {
                    // no item selected
                    log("[DropAll] Please select an item to drop.");
                    state = State.WAITING;
                    return random(WAIT_TICK_MIN, WAIT_TICK_MAX);
                }

                if (!inputLocked) {
                    // ANTIBAN disable input while dropping
                    this.getBot().setHumanInputEnabled(false);
                    inputLocked = true;
                }

                if (!hasItems) {
                    // either DONE dropping or INVALID item, reset to WAITING
                    state = State.WAITING;
                    return random(WAIT_TICK_MIN, WAIT_TICK_MAX);
                }

                // if we have items, but the slots tracker has old info
                if (SlotsToDrop.size() == 0) {
                    // update and start dropping again on the next tick
                    findItemInSlots();
                }


                /* DROP THE ITEM */

                Integer slot = SlotsToDrop.get(0);
                dropFromSlot(slot);
                SlotsToDrop.remove(0);
                return random(DROP_TICK_MIN, DROP_TICK_MAX);

            default:
                // should never fall to here, but if it does...
                return random(WAIT_TICK_MIN, WAIT_TICK_MAX);

        }
    }


    /**
     * Drops an item from the slot in the inventory
     *
     * @param slot the slot to drop from
     * @return true if the item was dropped successfully
     */
    private boolean dropFromSlot(int slot) {
        Item target = this.getInventory().getItemInSlot(slot);

        // no item in slot
        if (target == null) {
            log("[DropAll] WARN attempted to drop item from slot " + slot + " but there was no item");
            return false;
        }

        // item is different
        if (!target.getName().equalsIgnoreCase(itemToDrop)) {
            log("[DropAll] WARN attempted to drop a different item from slot " + slot);
            return false;
        }

        return target.interact("Drop");
    }


    /**
     * Select a new item to drop by getting the name of the selected inventory item
     */
    public void selectNewItem() {
        if (this.getTabs().getOpen() == Tab.INVENTORY) {

            String uptext = this.getClient().getTooltip();
            if (uptext != null) {
                String itemName = Utils.parseItemFromUpText(uptext);

                if (!itemName.equalsIgnoreCase(itemToDrop)
                        && this.getInventory().contains(itemName)) {
                    itemToDrop = itemName;
                }
            }
        }
    }


    /**
     * Find which slots contain the item to drop
     */
    public void findItemInSlots() {
        if (SlotsToDrop == null)
            SlotsToDrop = new ArrayList<Integer>();

        SlotsToDrop.clear();

        for (int i = 0; i < 28; i++) {
            if (this.getInventory().getItemInSlot(i) != null) {
                String item = this.getInventory().getItemInSlot(i).getName();

                if (item.equalsIgnoreCase(itemToDrop)) {
                    SlotsToDrop.add(i);
                }
            }
        }
    }


    /**
     * Handle a hotkey
     *
     * @param e the hotkey that was pressed
     */
    public void onHotkey(KeyListener.Hotkey e) {
        switch (e) {
            case SELECT:
                selectNewItem();
                break;

            case TOGGLE:
                // if we're dropping, stop doing that
                if (state == State.DROPPING) {
                    state = State.WAITING;
                }

                // if we're not dropping, let's start
                else {
                    findItemInSlots();
                    state = State.DROPPING;
                }
                break;

            case CLEAR:
                if (!itemToDrop.equals(""))
                    itemToDrop = "";
                break;
        }
    }

}
