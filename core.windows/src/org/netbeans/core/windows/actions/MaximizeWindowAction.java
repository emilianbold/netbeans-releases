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

package org.netbeans.core.windows.actions;


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/** An action that can toggle maximized window system mode for specific window.
 *
 * @author   Peter Zavadsky
 */
public class MaximizeWindowAction extends AbstractAction {

    private final PropertyChangeListener propListener;
    private TopComponent topComponent;
    private boolean isPopup;
    private static boolean isFirstActivated = true;
    
    public MaximizeWindowAction() {
        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if(WindowManagerImpl.PROP_MAXIMIZED_MODE.equals(propName)
                || WindowManagerImpl.PROP_EDITOR_AREA_STATE.equals(evt.getPropertyName())
                || WindowManagerImpl.PROP_ACTIVE_MODE.equals(evt.getPropertyName())) {
                    updateState();
                }
                // #64876: correctly initialize after startup 
                if (isFirstActivated && TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {
                    isFirstActivated = false;
                    updateState();
                }
            }
        };
        TopComponent.Registry registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(propListener, registry));
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        wm.addPropertyChangeListener(WeakListeners.propertyChange(propListener, wm));
        
        updateState();
    }
    /**
     * alternate constructor for use in the context menu, invoked from ActionUtils.java
     * see #38801 for details
     */
    MaximizeWindowAction(TopComponent tc) {
        topComponent = tc;
        propListener = null;
        isPopup = true;
        updateState();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        
        if(wm.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            if (topComponent != null) {
                ModeImpl mode = getModeToMaximize(topComponent);
                // maximize only non-sliding windows..
                if ( mode == null || mode.getKind() != Constants.MODE_KIND_SLIDING) {
                    wm.setMaximizedMode(mode);
                }
            } else {
                ModeImpl activeMode = wm.getActiveMode();
                ModeImpl mode = wm.getMaximizedMode();
                // maximize only non-sliding windows..
                if(activeMode != null && activeMode.getKind() != Constants.MODE_KIND_SLIDING) {
                    if(mode != null) {
                        wm.setMaximizedMode(null);
                    } else {
                        wm.setMaximizedMode(activeMode);
                    }
                }
            }
        } else {
            ModeImpl activeMode;
            if (topComponent != null) {
                activeMode = (ModeImpl)wm.findMode(topComponent);
            }
            else {
                activeMode = wm.getActiveMode();
            }
            if(activeMode != null) {
                if(activeMode.getKind() == Constants.MODE_KIND_EDITOR) {
                    if(wm.getEditorAreaFrameState() == Frame.NORMAL) {
                        wm.setEditorAreaFrameState(Frame.MAXIMIZED_BOTH);
                    } else {
                        wm.setEditorAreaFrameState(Frame.NORMAL);
                    }
                } else if (activeMode.getKind() == Constants.MODE_KIND_VIEW) {
                    if(activeMode.getFrameState() == Frame.NORMAL) {
                        activeMode.setFrameState(Frame.MAXIMIZED_BOTH);
                    } else {
                        activeMode.setFrameState(Frame.NORMAL);
                    }
                } else {
                    // do nothing for slidinbg windows..maximize only non-sliding windows..
                }
            }
        }
        
        updateState();
    }

    /** Updates state of this action, may be called from non-AWT thread.
     * #44825 - Shortcuts folder can call our constructor from non-AWT thread.
     */
    private void updateState() {
        if (SwingUtilities.isEventDispatchThread()) {
            doUpdateState();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doUpdateState();
                }
            });
        }
    }
    
    /** Updates state and text of this action.
     */
    private void doUpdateState() {
        TopComponent active = null;
        if (topComponent != null) {
            active = topComponent;
        } else {
            active = TopComponent.getRegistry().getActivated();
        }
        Object param = active == null ? "" : active.getName(); // NOI18N
        boolean maximize;
        ModeImpl activeMode = (ModeImpl)WindowManagerImpl.getInstance().findMode(active);
        if (WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            maximize = WindowManagerImpl.getInstance().getMaximizedMode() == null;
        } else {
            if(activeMode != null) {
                if(activeMode.getKind() == Constants.MODE_KIND_EDITOR) {
                    maximize = WindowManagerImpl.getInstance().getEditorAreaFrameState() == Frame.NORMAL;
                } else {
                    maximize = activeMode.getFrameState() == Frame.NORMAL;
                }
            } else {
                return;
            }
        }

        String label;
        if(maximize) {
            label = NbBundle.getMessage(MaximizeWindowAction.class, "CTL_MaximizeWindowAction", param);
        } else {
            label = NbBundle.getMessage(MaximizeWindowAction.class, "CTL_UnmaximizeWindowAction", param);
        }
        putValue(Action.NAME, (isPopup ? Actions.cutAmpersand(label) : label));
        if (activeMode != null && activeMode.getKind() == Constants.MODE_KIND_SLIDING) {
            maximize = false;
        }
        setEnabled(activeMode != null && activeMode.getKind() != Constants.MODE_KIND_SLIDING);
    }
    
    private static ModeImpl getModeToMaximize(TopComponent tc) {
         WindowManagerImpl wm = WindowManagerImpl.getInstance();
         ModeImpl mode = (ModeImpl)wm.findMode(tc);
         ModeImpl maximizedMode = wm.getMaximizedMode();
         if(mode == maximizedMode) {
             return null;
         } else {
             return mode;
         }
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("MaximizeWindow", newValue);
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("MaximizeWindow");
        } else {
            return super.getValue(key);
        }
    }
    
}

