package org.netbeans.modules.websvc.wsitconf.util;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;


public class UndoCounter implements UndoableEditListener {
    int i = 0;
    public void undoableEditHappened(UndoableEditEvent e) {
        i++;
    }
    public int getCounter() {
        return i;
    }
}