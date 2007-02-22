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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.highlight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.WeakListeners;

/**
 * Manages the active set of HighlightGroup instances, as well as the
 * Highlighted implementations that interpret the active highlights.
 *
 * @author Nathan Fiedler
 */
public abstract class HighlightManager {
    /** Default global highlight manager instance. */
    private static final HighlightManager defaultInstance;
    /** Registered (weak) highlight listeners. */
    private List<Highlighted> listeners;
    /** Registered highlight groups. */
    private List<HighlightGroup> groups;
    /** Map of components to the listeners interested in them. */
    private Map<Component, List<Highlighted>> componentListenerMap;

    static {
        defaultInstance = new DefaultHighlightManager();
    }

    /**
     * Creates a new instance of HighlightManager.
     */
    public HighlightManager() {
        listeners = new ArrayList<Highlighted>();
        groups = new ArrayList<HighlightGroup>();
        componentListenerMap = new WeakHashMap<Component, List<Highlighted>>();
    }

    /**
     * Adds the given HighlightGroup to this manager.
     *
     * @param  group  HighlightGroup to be added.
     */
    public void addHighlightGroup(HighlightGroup group) {
        synchronized (groups) {
            groups.add(group);
        }
        showHighlights(group);
    }

    /**
     * Adds the given Highlighted to this manager. The listener will
     * be weakly referenced to allow it to be garbage collected.
     *
     * @param  l  Highlighted to be added.
     */
    public void addHighlighted(Highlighted l) {
        Highlighted wl = (Highlighted)
                WeakListeners.create(Highlighted.class, l, this);
        synchronized (listeners) {
            listeners.add(wl);
        }
        synchronized (componentListenerMap) {
            Set<Component> comps = wl.getComponents();
            if (comps != null) {
                for (Component comp : comps) {
                    List<Highlighted> list = componentListenerMap.get(comp);
                    if (list == null) {
                        list = new LinkedList<Highlighted>();
                        componentListenerMap.put(comp, list);
                    }
                    list.add(wl);
                }
            }
        }
        highlight(l);
    }

    /**
     * Locate the highlight listeners that are affected by the given group
     * of highlights.
     *
     * @param  group  highlight group.
     * @return  highlight listeners and the highlights they care about.
     */
    protected Map<Highlighted, List<Highlight>> findListeners(
            HighlightGroup group) {
        Map<Highlighted, List<Highlight>> map =
                new HashMap<Highlighted, List<Highlight>>();
        synchronized (componentListenerMap) {
            Iterator<Highlight> iter = group.highlights().iterator();
            while (iter.hasNext()) {
                Highlight h = iter.next();
                Component comp = h.getComponent();
                List<Highlighted> list = componentListenerMap.get(comp);
                if (list != null) {
                    for (Highlighted l : list) {
                        List<Highlight> lights = map.get(l);
                        if (lights == null) {
                            lights = new ArrayList<Highlight>();
                            map.put(l, lights);
                        }
                        lights.add(h);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Returns the global instance of a HighlightManager.
     *
     * @return  global highlight manager instance.
     */
    public static HighlightManager getDefault(){
        return defaultInstance;
    }

    /**
     * Retrieves the highlight groups of the given type, if any.
     *
     * @return  highlight groups matching type (empty if none).
     */
    public List<HighlightGroup> getHighlightGroups(String type) {
        List<HighlightGroup> list = new ArrayList<HighlightGroup>();
        synchronized (groups) {
            if (groups.size() > 0) {
                for (HighlightGroup group : groups) {
                    if (group.getType().equals(type)) {
                        list.add(group);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Deactivate all of the Highlight instances in the group.
     *
     * @param  group  HighlightGroup to be hidden.
     */
    protected abstract void hideHighlights(HighlightGroup group);

    /**
     * Determines if any of the existing highlight groups affect the given
     * listener or not, and if so, show the highlight for that listener.
     *
     * @param  listener  listener which needs highlighting.
     */
    protected void highlight(Highlighted listener) {
        Set<Component> comps = listener.getComponents();
        if (comps != null && !comps.isEmpty()) {
            synchronized (groups) {
                for (HighlightGroup group : groups) {
                    if (group.isShowing()) {
                        Iterator<Highlight> iter = group.highlights().iterator();
                        while (iter.hasNext()) {
                            Highlight h = iter.next();
                            Component comp = h.getComponent();
                            if (comps.contains(comp)) {
                                listener.highlightAdded(h);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes the given HighlightGroup from this manager.
     *
     * @param  group  HighlightGroup to be removed.
     */
    public void removeHighlightGroup(HighlightGroup group) {
        hideHighlights(group);
        synchronized (groups) {
            groups.remove(group);
        }
    }

    /**
     * Removes the given Highlighted from this manager. This should only
     * be called by the NetBeans WeakListeners class, which is used to
     * wrap the original listener in <code>addHighlighted()</code>.
     * That is, the listener will be automatically removed when it is no
     * longer needed.
     *
     * @param  l  Highlighted to be removed.
     */
    public void removeHighlighted(Highlighted l) {
        // Counting on weak listener to delegate equals() to actual listener.
        synchronized (listeners) {
            listeners.remove(l);
        }
        synchronized (componentListenerMap) {
            Set<Component> comps = l.getComponents();
            if (comps != null) {
                for (Component comp : comps) {
                    List<Highlighted> list = componentListenerMap.get(comp);
                    if (list != null) {
                        list.remove(l);
                    }
                }
            }
        }
    }

    /**
     * Activate all of the Highlight instances in the group.
     *
     * @param  group  HighlightGroup to be shown.
     */
    protected abstract void showHighlights(HighlightGroup group);
}
