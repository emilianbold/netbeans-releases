/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 *//*
 * DefaultTabSelectionModel.java
 *
 * Created on May 26, 2003, 5:37 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.event.ArrayDiff;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import java.util.Iterator;
import java.util.Set;

/**
 * Default implementation of tab selection model.  Listens to the supplied data
 * model and updates the selection appropriately on all add/remove events so that
 * the actual selection does not change if items are inserted into the model ahead
 * of the current selection, etc.
 *
 * @author Tim Boudreau
 */
final class DefaultTabSelectionModel implements SingleSelectionModel,
        ComplexListDataListener {
    TabDataModel dataModel;
    int sel = -1;
    /**
     * Utility field holding list of ChangeListeners.
     */
    private transient java.util.ArrayList changeListenerList;

    /**
     * Creates a new instance of DefaultTabSelectionModel
     */
    public DefaultTabSelectionModel(TabDataModel tdm) {
        dataModel = tdm;
        attach();
    }

    public void attach() {
        dataModel.addComplexListDataListener(this);
    }

    public void detach() {
        dataModel.removeComplexListDataListener(this);
    }

    public void clearSelection() {
        sel = -1;
        fireStateChanged();
    }

    public int getSelectedIndex() {
        return sel;
    }

    public boolean isSelected() {
        return sel != -1;
    }

    public void setSelectedIndex(int index) {
        if (index != sel) {
            int oldIndex = sel;
            if ((index < -1) || (index >= dataModel.size())) {
                throw new IllegalArgumentException("Selected index set to "
                   + index
                   + " but model size is only " + dataModel.size());
            }
            sel = index;
            fireStateChanged();
        }
    }

    private void adjustSelectionForEvent(ListDataEvent e) {
        if (e.getType() == e.CONTENTS_CHANGED) {
            return;
        }
        int start = e.getIndex0();
        int end = e.getIndex1() + 1;
        if (e.getType() == e.INTERVAL_REMOVED) {
            if (sel < start) {
                return;
            } else {
                if (sel >= start) {
                    if (sel > end) {
                        sel -= end - start;
                    } else {
                        sel = start;
                        if (sel >= dataModel.size()) {
                            sel = dataModel.size() - 1;
                        }
                    }
                    fireStateChanged();
                }
            }
        } else {
            if (sel < start) {
                //not affected, do nothing
                return;
            }
            if (sel >= start) {
                if (end - 1 == start) {
                    sel++;
                } else if (sel < end) {
                    sel = (end + (sel - start)) - 1;
                } else {
                    sel += (end - start) - 1;
                }
                fireStateChanged();
            }
        }
    }

    public void contentsChanged(ListDataEvent e) {
        adjustSelectionForEvent(e);
    }

    public void intervalAdded(ListDataEvent e) {
        adjustSelectionForEvent(e);
    }

    public void intervalRemoved(ListDataEvent e) {
        adjustSelectionForEvent(e);
    }

    public void indicesAdded(ComplexListDataEvent e) {
        int[] indices = e.getIndices();
        java.util.Arrays.sort(indices);
        int offset = 0;
        for (int i = 0; i < indices.length; i++) {
            if (sel >= indices[i]) {
                offset++;
            } else {
                break;
            }
        }
        if (offset > 0) {
            sel += offset;
            fireStateChanged();
        }
    }

    public void indicesRemoved(ComplexListDataEvent e) {
        int[] indices = e.getIndices();
        java.util.Arrays.sort(indices);
        int offset = -1;
        for (int i = 0; i < indices.length; i++) {
            if (sel > indices[i]) {
                offset--;
            } else {
                break;
            }
        }
        if (sel == dataModel.size()) {
            sel -= 1;
            fireStateChanged();
            return;
        }
        if (dataModel.size() == 0) {
            sel = -1;
            fireStateChanged();
        } else if (offset != 0) {
            sel += offset;
            fireStateChanged();
        }
    }

    public void indicesChanged(ComplexListDataEvent e) {
        if (e instanceof VeryComplexListDataEvent) { //it always will be

            ArrayDiff dif = ((VeryComplexListDataEvent) e).getDiff();

            boolean changed = false;
            
            if (dif == null) {
                //no differences
                return;
            }
            
            //Get the deleted and added indices
            Set deleted = dif.getDeletedIndices();
            Set added = dif.getAddedIndices();
            
            //create an Integer to compare
            Integer idx = new Integer(getSelectedIndex());
            
            //Don't iterate if everything was closed, we know what to do
            if (dataModel.size() == 0) {
                sel = -1;
                fireStateChanged();
                return;
            }
            
            //Iterate all of the deleted items, and count how many were
            //removed at indices lower than the selection, so we can subtract
            //that from the selected index to keep selection on the same tab
            Iterator i = deleted.iterator();
            int offset = 0;
            Integer curr;
            while (i.hasNext()) {
                curr = (Integer) i.next();
                if (curr.compareTo(idx) <= 0) {
                    offset++;
                }
            }
            
            //Iterate all of the added items, and count how many were added at
            //indices below the selected index, so we can add that to the selected
            //index
            i = added.iterator();
            while (i.hasNext()) {
                curr = (Integer) i.next();
                if (curr.compareTo(idx) >= 0) {
                    offset--;
                }
            }

            sel -= offset;
            if (sel < 0) {
                //The tab at index 0 was closed, but we always want to show
                //something if we can, so change it to 0 if possible
                sel = dataModel.size() > 0 ? 0 : -1;
            }
            
            //Make sure we're not off the end of the array - we could be if the
            //selection was the last and it and others were removed
            if (sel >= dataModel.size()) {
                sel = dataModel.size() - 1;
            }
            
            if (offset != 0) {
                fireStateChanged();
            }
        }
        //do nothing
    }

    public synchronized void addChangeListener(
            javax.swing.event.ChangeListener listener) {
        if (changeListenerList == null) {
            changeListenerList = new java.util.ArrayList();
        }
        changeListenerList.add(listener);
    }

    public synchronized void removeChangeListener(
            javax.swing.event.ChangeListener listener) {
        if (changeListenerList != null) {
            changeListenerList.remove(listener);
        }
    }

    ChangeEvent ce = new ChangeEvent(this);

    private void fireStateChanged() {
        java.util.ArrayList list;
        synchronized (this) {
            if (changeListenerList == null) {
                return;
            }
            list = (java.util.ArrayList) changeListenerList.clone();
        }
        for (int i = 0; i < list.size(); i++) {
            ((javax.swing.event.ChangeListener) list.get(i)).stateChanged(ce);
        }
    }

}
