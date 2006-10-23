/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *//*
 * DefaultTabDataModel.java
 *
 * Created on May 26, 2003, 3:41 PM
 */

package org.netbeans.swing.tabcontrol;

import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import java.util.*;

/**
 * Default implementation of TabDataModel.
 *
 * @author Tim Boudreau
 */
public class DefaultTabDataModel implements TabDataModel {
    /**
     * Utility field holding list of ChangeListeners.
     */
    private transient ArrayList<ComplexListDataListener> listenerList;

    private class L extends ArrayList<TabData> {
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }

    private L list = new L();

    /**
     * Utility field holding list of ChangeListeners.
     */
    private transient ArrayList<ChangeListener> changeListenerList;

    /**
     * Creates a new instance of DefaultTabDataModel
     */
    public DefaultTabDataModel() {
    }

    /**
     * Testing constructor
     */
    public DefaultTabDataModel(TabData[] data) {
        list.addAll(Arrays.asList(data));
    }

    public java.util.List<TabData> getTabs() {
        return Collections.unmodifiableList(list);
    }

    public TabData getTab(int index) {
        return (TabData) list.get(index);
    }

    public void setTabs(TabData[] data) {

        TabData[] oldContents = new TabData[list.size()];
        oldContents = (TabData[]) list.toArray(oldContents);
        
        //No change, Peter says this will be the typical case
        if (Arrays.equals(data, oldContents)) {
            return;
        }

        List newContents = Arrays.asList(data);

        list.clear();
        list.addAll(Arrays.asList(data));

        VeryComplexListDataEvent vclde = new VeryComplexListDataEvent(this,
                                                                      oldContents,
                                                                      data);
        fireIndicesChanged(vclde);
    }

    public void setIcon(int index, Icon i) {
        boolean[] widthChanged = new boolean[]{false};

        boolean fireChange = _setIcon(index, i, widthChanged);
        if (fireChange) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 index, index,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    private boolean _setIcon(int index, Icon i, final boolean[] widthChanged) {
        if (i == null) {
            i = TabData.NO_ICON;
        }
        TabData data = getTab(index);
        if (i != data.getIcon()) {
            widthChanged[0] = data.getIcon().getIconWidth()
                    != i.getIconWidth();
            data.icon = i;
            return true;
        } else {
            return false;
        }
    }

    public void setText(int index, String txt) {
        boolean[] widthChanged = new boolean[]{false};
        boolean fireChange = _setText(index, txt, widthChanged);
        if (fireChange) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 index, index,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    private int[] _setText(int[] indices, String[] txt,
                           final boolean[] widthChanged) {
        widthChanged[0] = false;
        boolean fireChange = false;
        boolean[] changed = new boolean[indices.length];
        int changedCount = 0;
        Arrays.fill(changed, false);
        for (int i = 0; i < indices.length; i++) {
            boolean[] currWidthChanged = new boolean[]{false};
            fireChange |=
                    _setText(indices[i], txt[i], currWidthChanged);
            widthChanged[0] |= currWidthChanged[0];
            if (currWidthChanged[0])
                changedCount++;
            changed[i] = currWidthChanged[0];
        }
        int[] toFire;
        if (widthChanged[0] || fireChange) {
            if (changedCount == indices.length) {
                toFire = indices;
            } else {
                toFire = new int[changedCount];
                int idx = 0;
                for (int i = 0; i < indices.length; i++) {
                    if (changed[i]) {
                        toFire[idx] = indices[i];
                        idx++;
                    }
                }
            }
            return toFire;
        }
        return null;
    }

    private int[] _setIcon(int[] indices, Icon[] icons,
                           final boolean[] widthChanged) {
        widthChanged[0] = false;
        boolean fireChange = false;
        boolean[] changed = new boolean[indices.length];
        int changedCount = 0;
        Arrays.fill(changed, false);
        boolean[] currWidthChanged = new boolean[]{false};
        boolean currChanged = false;
        for (int i = 0; i < indices.length; i++) {
            currChanged =
                    _setIcon(indices[i], icons[i], currWidthChanged);
            fireChange |= currChanged;
            widthChanged[0] |= currWidthChanged[0];
            if (currChanged)
                changedCount++;
            changed[i] = currChanged;
        }
        int[] toFire;
        if (widthChanged[0] || fireChange) {
            if (changedCount == indices.length) {
                toFire = indices;
            } else {
                toFire = new int[changedCount];
                int idx = 0;
                for (int i = 0; i < indices.length; i++) {
                    if (changed[i]) {
                        toFire[idx] = indices[i];
                        idx++;
                    }
                }
            }
            return toFire;
        }
        return null;
    }

    public void setIconsAndText(int[] indices, String[] txt, Icon[] icons) {
        boolean[] iconWidthsChanged = new boolean[]{false};
        boolean[] txtWidthsChanged = new boolean[]{false};
        int[] iconsToFire = _setIcon(indices, icons, iconWidthsChanged);
        int[] txtToFire = _setText(indices, txt, txtWidthsChanged);
        boolean widthChanged = iconWidthsChanged[0] || txtWidthsChanged[0];
        boolean fire = widthChanged || iconsToFire != null
                || txtToFire != null;
        if (fire) {
            if ((indices == iconsToFire) && (indices == txtToFire)) {
                //if all icons/txt changed, optimize and don't calculate a merge
                ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                     ComplexListDataEvent.CONTENTS_CHANGED,
                                                                     indices,
                                                                     widthChanged);
                fireContentsChanged(clde);
            } else {
                //okay, there are differences in what was set to what.  Build a 
                //merge of the change data and fire that
                int size = (iconsToFire != null ? iconsToFire.length : 0)
                        + (txtToFire != null ? txtToFire.length : 0);
                Set<Integer> allIndicesToFire = new HashSet<Integer>(size);
                Integer[] o;
                if (iconsToFire != null) {
                    o = toObjectArray(iconsToFire);
                    allIndicesToFire.addAll(Arrays.asList(o));
                }
                if (txtToFire != null) {
                    o = toObjectArray(txtToFire);
                    allIndicesToFire.addAll(Arrays.asList(o));
                }
                Integer[] all = new Integer[allIndicesToFire.size()];
                all = (Integer[]) allIndicesToFire.toArray(all);
                int[] allPrimitive = toPrimitiveArray(all);
                ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                     ComplexListDataEvent.CONTENTS_CHANGED,
                                                                     allPrimitive,
                                                                     widthChanged);
                fireContentsChanged(clde);
            }
        }
    }

    public void setIcon(int[] indices, Icon[] icons) {
        boolean[] widthChanged = new boolean[]{false};
        int[] toFire = _setIcon(indices, icons, widthChanged);
        if (toFire != null) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 toFire,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    public void setText(int[] indices, String[] txt) {
        boolean[] widthChanged = new boolean[]{false};
        int[] toFire = _setText(indices, txt, widthChanged);
        if (toFire != null) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 toFire,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    private boolean _setText(int index, String txt,
                             final boolean[] widthChanged) {
        TabData data = getTab(index);
        if (txt != data.txt) {
            widthChanged[0] = data.getText() != txt;
            data.txt = txt;
            return true;
        } else {
            return false;
        }
    }

    public void setTab(int index, TabData data) {
        if (!data.equals(getTab(index))) {
            TabData olddata = getTab(index);
            boolean txtChg = data.getText().equals(olddata.getText());
            boolean compChg = data.getUserObject() != olddata.getUserObject();
            list.set(index, data);
            ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                                ListDataEvent.CONTENTS_CHANGED,
                                                                index, index,
                                                                txtChg,
                                                                compChg);
            lde.setAffectedItems(new TabData[]{data});
            fireContentsChanged(lde);
        }
    }

    public void addTab(int index, TabData data) {
        list.add(index, data);
        ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                            ComplexListDataEvent.INTERVAL_ADDED,
                                                            index, index, true);
        lde.setAffectedItems(new TabData[]{data});
        fireIntervalAdded(lde);
    }

    public void addTabs(int start, TabData[] data) {
        list.addAll(start, Arrays.asList(data));
        ComplexListDataEvent lde = new ComplexListDataEvent(this, ListDataEvent.INTERVAL_ADDED, start, start
                                                                                                       + data.length
                                                                                                       - 1, true);
        lde.setAffectedItems(data);
        fireIntervalAdded(lde);
    }

    public void removeTab(int index) {
        TabData[] td = new TabData[]{(TabData) list.get(index)};
        list.remove(index);
        ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                            ListDataEvent.INTERVAL_REMOVED,
                                                            index, index);
        lde.setAffectedItems(td);
        fireIntervalRemoved(lde);
    }

    /**
     * Remove a range of tabs from <code>start</code> up to <i>and including</i> 
     * <code>finish</code>.
     */
    public void removeTabs(int start, int end) {
        java.util.List affected = list.subList(start, end);
        if (start == end) {
            list.remove(start);
        } else {
            list.removeRange(start, end + 1);
        }
        ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                            ListDataEvent.INTERVAL_REMOVED,
                                                            start, end);
        lde.setAffectedItems((TabData[]) list.toArray(new TabData[0]));
        fireIntervalRemoved(lde);
    }

    public void addTabs(int[] indices, TabData[] data) {
        Map<Integer,TabData> m = new HashMap<Integer,TabData>(data.length);
        for (int i = 0; i < data.length; i++) {
            m.put(new Integer(indices[i]), data[i]);
        }
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            Integer key = new Integer(indices[i]);
            TabData currData = m.get(key);
            list.add(indices[i], currData);
        }
        ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                             ComplexListDataEvent.ITEMS_ADDED,
                                                             indices, true);
        clde.setAffectedItems(data);
        fireIndicesAdded(clde);
    }

    public void removeTabs(int[] indices) {
        Arrays.sort(indices);
        TabData[] affected = new TabData[indices.length];
        for (int i = indices.length - 1; i >= 0; i--) {
            affected[i] = (TabData) list.remove(indices[i]);
        }
        ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                             ComplexListDataEvent.ITEMS_REMOVED,
                                                             indices, true);
        clde.setAffectedItems(affected);
        fireIndicesRemoved(clde);
    }

    public int size() {
        return list.size();
    }

    public synchronized void addComplexListDataListener(
            ComplexListDataListener listener) {
        if (listenerList == null) {
            listenerList = new ArrayList<ComplexListDataListener>();
        }
        listenerList.add(listener);
    }

    public synchronized void removeComplexListDataListener(
            ComplexListDataListener listener) {
        listenerList.remove(listener);
    }

    private void fireIntervalAdded(ListDataEvent event) {
        if (listenerList == null) {
            return;
        }
        int max = listenerList.size();
        for (int i = 0; i < max; i++) {
            ComplexListDataListener l = (ComplexListDataListener) listenerList.get(
                    i);
            l.intervalAdded(event);
        }
        fireChange();
    }

    private void fireIntervalRemoved(ListDataEvent event) {
        if (listenerList == null)
            return;
        int max = listenerList.size();
        for (int i = 0; i < max; i++) {
            ComplexListDataListener l = (ComplexListDataListener) listenerList.get(
                    i);
            l.intervalRemoved(event);
        }
        fireChange();
    }

    private void fireContentsChanged(ListDataEvent event) {
        if (listenerList == null)
            return;
        int max = listenerList.size();
        for (int i = 0; i < max; i++) {
            ComplexListDataListener l = (ComplexListDataListener) listenerList.get(
                    i);
            l.contentsChanged(event);
        }
        fireChange();
    }

    private void fireIndicesAdded(ComplexListDataEvent event) {
        if (listenerList == null)
            return;
        int max = listenerList.size();
        for (int i = 0; i < max; i++) {
            ComplexListDataListener l = (ComplexListDataListener) listenerList.get(
                    i);
            l.indicesAdded(event);
        }
        fireChange();
    }

    private void fireIndicesRemoved(ComplexListDataEvent event) {
        if (listenerList == null)
            return;
        int max = listenerList.size();
        for (int i = 0; i < max; i++) {
            ComplexListDataListener l = (ComplexListDataListener) listenerList.get(
                    i);
            l.indicesRemoved(event);
        }
        fireChange();
    }

    private void fireIndicesChanged(ComplexListDataEvent event) {
        if (listenerList == null)
            return;
        int max = listenerList.size();
        for (int i = 0; i < max; i++) {
            ComplexListDataListener l = (ComplexListDataListener) listenerList.get(
                    i);
            l.indicesChanged(event);
        }
        fireChange();
    }

    public String toString() {
        StringBuffer out = new StringBuffer(getClass().getName());
        out.append(" size =");
        int max = size();
        out.append(max);
        out.append(" - ");
        for (int i = 0; i < max; i++) {
            TabData td = getTab(i);
            out.append(td.toString());
            if (i != max - 1) {
                out.append(',');
            }
        }
        return out.toString();
    }
    
    //===========================
    //XXX remove ChangeListener support and handle the ComplexNNN events so nothing is repainted
    //if not displayed on screen!
    
    
    /**
     * Registers ChangeListener to receive events.
     *
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<ChangeListener>();
        }
        changeListenerList.add(listener);
    }

    /**
     * Removes ChangeListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(
            javax.swing.event.ChangeListener listener) {
        if (changeListenerList != null) {
            changeListenerList.remove(listener);
        }
    }

    ChangeEvent event = null;

    private void fireChange() {
        java.util.ArrayList list;
        synchronized (this) {
            if (changeListenerList == null)
                return;
            list = (java.util.ArrayList) changeListenerList.clone();
        }
        if (event == null) {
            event = new ChangeEvent(this);
        }
        for (int i = 0; i < list.size(); i++) {
            ((javax.swing.event.ChangeListener) list.get(i)).stateChanged(
                    event);
        }
    }

    public int indexOf(TabData td) {
        return list.indexOf(td);
    }

    private Integer[] toObjectArray(int[] o) {
        Integer[] result = new Integer[o.length];
        for (int i = 0; i < o.length; i++) {
            result[i] = new Integer(o[i]);
        }
        return result;
    }

    private int[] toPrimitiveArray(Integer[] o) {
        int[] result = new int[o.length];
        for (int i = 0; i < o.length; i++) {
            result[i] = o[i].intValue();
        }
        return result;
    }

}
