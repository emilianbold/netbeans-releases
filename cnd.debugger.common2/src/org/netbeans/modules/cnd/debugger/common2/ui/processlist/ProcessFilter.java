/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;

import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author ak119685
 */
public final class ProcessFilter {

    private String filter = ""; // NOI18N
    private final ChangeSupport cs = new ChangeSupport(this);

    public ProcessFilter() {
    }

    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    public synchronized void set(String filter) {
        if (filter == null) {
            filter = ""; // NOI18N
        }

        if (filter.equals(this.filter)) {
            return;
        }

        this.filter = filter;
        cs.fireChange();
    }

    /**
     * 
     * @return NOT NULL filter
     */
    public synchronized String get() {
        return filter;
    }
}
