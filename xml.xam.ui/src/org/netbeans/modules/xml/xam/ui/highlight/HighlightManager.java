/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
