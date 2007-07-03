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
 */

package org.netbeans.modules.editor.impl;

import org.netbeans.modules.editor.*;
import java.awt.BorderLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.editor.WeakEventListenerList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *  Editor Customizable Side Bar.
 *  Contains components for particular MIME type as defined in XML layer.
 *
 *  @author  Martin Roskanin
 */
public final class CustomizableSideBar {
    
    private static final Logger LOG = Logger.getLogger(CustomizableSideBar.class.getName());
    
    private static final Map<JTextComponent, Map<SideBarPosition, Reference<JPanel>>> CACHE = new WeakHashMap<JTextComponent, Map<SideBarPosition, Reference<JPanel>>>(5);
    private static final Map<String, WeakEventListenerList> LISTENERS = new HashMap<String, WeakEventListenerList>(5);

    private static final Map<MimePath, Lookup.Result<SideBarFactoriesProvider>> LR = new WeakHashMap<MimePath, Lookup.Result<SideBarFactoriesProvider>>(5);
    private static final Map<Lookup.Result<SideBarFactoriesProvider>, LookupListener> LL = new WeakHashMap<Lookup.Result<SideBarFactoriesProvider>, LookupListener>(5);
    
    private CustomizableSideBar() {
    }

    /** Add weak listener to listen to change of activity of documents or components.
     * The caller must
     * hold the listener object in some instance variable to prevent it
     * from being garbage collected.
     * @param l listener to add
     */
    public static void addChangeListener(String mimeType, ChangeListener l) {
        synchronized (LISTENERS){
            WeakEventListenerList listenerList = (WeakEventListenerList)LISTENERS.get(mimeType);
            if (listenerList == null) {
                listenerList = new WeakEventListenerList();
                LISTENERS.put(mimeType, listenerList);
            }
            listenerList.add(ChangeListener.class, l);
        }
    }

    /** Remove listener for changes in activity. It's optional
     * to remove the listener. It would be done automatically
     * if the object holding the listener would be garbage collected.
     * @param l listener to remove
     */
    public static void removeChangeListener(String mimeType, ChangeListener l) {
        synchronized (LISTENERS){
            WeakEventListenerList listenerList = LISTENERS.get(mimeType);
            if (listenerList != null) {
                listenerList.remove(ChangeListener.class, l);
            }
        }
    }

    private static void fireChange(String mimeType) {
        ChangeListener[] listeners = null;
        
        synchronized (LISTENERS){
            WeakEventListenerList listenerList = LISTENERS.get(mimeType);
            if (listenerList != null) {
                listeners = (ChangeListener[])listenerList.getListeners(ChangeListener.class);
            }
        }

        if (listeners != null && listeners.length > 0) {
            ChangeEvent evt = new ChangeEvent(CustomizableSideBar.class);
            for (ChangeListener l : listeners) {
                l.stateChanged(evt);
            }
        }
    }
    

    public static Map<SideBarPosition, JComponent> getSideBars(JTextComponent target) {
        assert SwingUtilities.isEventDispatchThread() : "Side bars can only be accessed from AWT"; //NOI18N
        return getSideBarsInternal(target);
    }

    private static Map<SideBarPosition, JComponent> getSideBarsInternal(JTextComponent target) {
        synchronized (CACHE) {
            Map<SideBarPosition, Reference<JPanel>> panelsMap = CACHE.get(target);
            
            if (panelsMap != null) {
                Map<SideBarPosition, JComponent> map = new HashMap<SideBarPosition, JComponent>();
                
                for(SideBarPosition pos : panelsMap.keySet()) {
                    Reference<JPanel> ref = panelsMap.get(pos);
                    if (ref != null) {
                        JPanel panel = ref.get();
                        if (panel != null) {
                            map.put(pos, panel);
                        } else {
                            break;
                        }
                    }
                }
                
                if (map.size() == panelsMap.size()) {
                    // All components from the cache
                    return map;
                }
            }
        }
        
        // Should not run under the lock, see #107056, #107656
        Map<SideBarPosition, List<JComponent>> sideBarsMap = createSideBarsMap(target);

        synchronized (CACHE) {
            Map<SideBarPosition, Reference<JPanel>> panelsMap = new HashMap<SideBarPosition, Reference<JPanel>>();
            Map<SideBarPosition, JComponent> map = new HashMap<SideBarPosition, JComponent>();
            
            for(SideBarPosition pos : sideBarsMap.keySet()) {
                List<JComponent> sideBars = sideBarsMap.get(pos);
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, pos.getAxis()));

                for(JComponent c : sideBars) {
                    panel.add(c);
                }
                
                panelsMap.put(pos, new WeakReference<JPanel>(panel));
                map.put(pos, panel);
            }

            CACHE.put(target, panelsMap);
            return map;
        }
    }
    
    private static Map<SideBarPosition, List<JComponent>> createSideBarsMap(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target);
        Map<SideBarPosition, List<SideBarFactory>> factoriesMap = getFactoriesMap(mimeType);
        Map<SideBarPosition, List<JComponent>> sideBarsMap = new HashMap<SideBarPosition, List<JComponent>>(factoriesMap.size());
        
        // XXX: We should better let clients to register a regexp filter
        boolean errorStripeOnly = Boolean.TRUE.equals(target.getClientProperty("errorStripeOnly")); //NOI18N

        for(SideBarPosition pos : factoriesMap.keySet()) {
            List<SideBarFactory> factoriesList = factoriesMap.get(pos);
            
            // Get sideBars list
            List<JComponent> sideBars = sideBarsMap.get(pos);
            if (sideBars == null) {
                sideBars = new ArrayList<JComponent>();
                sideBarsMap.put(pos, sideBars);
            }
            
            // Create side bars from the factories for this position
            for(SideBarFactory f : factoriesList) {
                JComponent sideBar = f.createSideBar(target);
                if (sideBar == null) {
                    LOG.fine("Ignoring null side bar created by the factory: " + f); //NOI18N
                    continue;
                }
                
                if (errorStripeOnly && !"errorStripe".equals(sideBar.getName())) { //NOI18N
                    LOG.fine("Error stripe sidebar only. Ignoring '" + sideBar.getName() + "' side bar created by the factory: " + f); //NOI18N
                    continue;
                }
                
                sideBars.add(sideBar);
            }
        }
        
        return sideBarsMap;
    }

    private static Map<SideBarPosition, List<SideBarFactory>> getFactoriesMap(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        
        Lookup.Result<SideBarFactoriesProvider> lR = LR.get(mimePath);
        if (lR == null) {
            lR = MimeLookup.getLookup(mimePath).lookupResult(SideBarFactoriesProvider.class);
            
            LookupListener listener = LL.get(lR);
            if (listener == null) {
                listener = new MyLookupListener(mimeType);
                LL.put(lR, listener);
            }
            
            lR.addLookupListener(listener);
            LR.put(mimePath, lR);
        }
        
        Collection<? extends SideBarFactoriesProvider> providers = lR.allInstances();
        assert providers.size() == 1 : "There should always be only one SideBarFactoriesProvider"; //NOI18N
        
        SideBarFactoriesProvider provider = providers.iterator().next();
        return provider.getFactories();
    }
    
    public static final class SideBarPosition {
        public static final int WEST  = 1;
        public static final int NORTH = 2;
        public static final int SOUTH = 3;
        public static final int EAST  = 4;
        
        public static final String WEST_NAME   = "West"; // NOI18N
        public static final String NORTH_NAME  = "North"; // NOI18N
        public static final String SOUTH_NAME  = "South"; // NOI18N
        public static final String EAST_NAME   = "East"; // NOI18N
        
        private int position;
        private boolean scrollable;
        
        SideBarPosition(FileObject fo) {
            Object position = fo.getAttribute("location"); // NOI18N
            if (position == null) {
                // Compatibility:
                position = fo.getAttribute("position"); // NOI18N
            }
            
            if (position != null && position instanceof String) {
                String positionName = (String) position;
                
                if (WEST_NAME.equals(positionName)) {
                    this.position = WEST;
                } else {
                    if (NORTH_NAME.equals(positionName)) {
                        this.position = NORTH;
                    } else {
                        if (SOUTH_NAME.equals(positionName)) {
                            this.position = SOUTH;
                        } else {
                            if (EAST_NAME.equals(positionName)) {
                                this.position = EAST;
                            } else {
                                if (ErrorManager.getDefault().isLoggable(ErrorManager.INFORMATIONAL))
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unsupported position: " + positionName);
                                
                                this.position = WEST;
                            }
                        }
                    }
                }
            } else {
                this.position = WEST;
            }
            
            Object scrollable = fo.getAttribute("scrollable"); // NOI18N
            
            if (scrollable != null && scrollable instanceof Boolean) {
                this.scrollable = ((Boolean) scrollable).booleanValue();
            } else {
                this.scrollable = true;
            }
            
            if (this.scrollable && (this.position == SOUTH || this.position == EAST)) {
                if (ErrorManager.getDefault().isLoggable(ErrorManager.INFORMATIONAL))
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unsupported combination: scrollable == true, position=" + getBorderLayoutPosition());
            }
        }
        
        public int hashCode() {
            return scrollable ? position : - position;
        }
        
        public boolean equals(Object o) {
            if (o instanceof SideBarPosition) {
                SideBarPosition p = (SideBarPosition) o;
                
                if (scrollable != p.scrollable)
                    return false;
                
                if (position != p.position)
                    return false;
                
                return true;
            }
            
            return false;
        }
        
        public int getPosition() {
            return position;
        }
        
        private static String[] borderLayoutConstants = new String[] {"", BorderLayout.WEST, BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST};
        
        public String getBorderLayoutPosition() {
            return borderLayoutConstants[getPosition()];
        }
        
        private static int[] axisConstants = new int[] {-1, BoxLayout.X_AXIS, BoxLayout.Y_AXIS, BoxLayout.Y_AXIS, BoxLayout.X_AXIS};
        
        private int getAxis() {
            return axisConstants[getPosition()];
        }
        
        public boolean isScrollable() {
            return scrollable;
        }
        
        public String toString() {
            return "[SideBarPosition: scrollable=" + scrollable + ", position=" + position + "]"; // NOI18N
        }
    } // End of SideBarPosition class

    private static class MyLookupListener implements LookupListener {
        private String mimeType;
        
        public MyLookupListener(String mimeType) {
            this.mimeType = mimeType;
        }
        
        public void resultChanged(LookupEvent ev) {
            synchronized (CACHE) {
                ArrayList<JTextComponent> toRemove = new ArrayList<JTextComponent>();
                
                for(JTextComponent jtc : CACHE.keySet()) {
                    String mimeType = NbEditorUtilities.getMimeType(jtc);
                    if (mimeType.equals(this.mimeType)) {
                        toRemove.add(jtc);
                    }
                }
                
                CACHE.keySet().removeAll(toRemove);
            }
            
            fireChange(mimeType);
        }
    } // End of MyLookupListener class

}
