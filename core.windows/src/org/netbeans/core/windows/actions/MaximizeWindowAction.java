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
 */

package org.netbeans.core.windows.actions;


import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;


/**
 * @author   Peter Zavadsky
 */
public class MaximizeWindowAction extends AbstractAction {

    private final PropertyChangeListener propListener;
    
    public MaximizeWindowAction() {
        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if(TopComponent.Registry.PROP_ACTIVATED.equals(propName)
                || WindowManagerImpl.PROP_MAXIMIZED_MODE.equals(propName)
                || WindowManagerImpl.PROP_EDITOR_AREA_STATE.equals(evt.getPropertyName())) {
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
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        
        if(wm.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            ModeImpl mode = wm.getMaximizedMode();
            if(mode != null) {
                wm.setMaximizedMode(null);
            } else {
                ModeImpl activeMode = wm.getActiveMode();
                if(activeMode != null) {
                    wm.setMaximizedMode(activeMode);
                }
            }
        } else {
            ModeImpl activeMode = wm.getActiveMode();
            if(activeMode != null) {
                if(activeMode.getKind() == Constants.MODE_KIND_EDITOR) {
                    if(wm.getEditorAreaFrameState() == Frame.NORMAL) {
                        wm.setEditorAreaFrameState(Frame.MAXIMIZED_BOTH);
                    } else {
                        wm.setEditorAreaFrameState(Frame.NORMAL);
                    }
                } else {
                    if(activeMode.getFrameState() == Frame.NORMAL) {
                        activeMode.setFrameState(Frame.MAXIMIZED_BOTH);
                    } else {
                        activeMode.setFrameState(Frame.NORMAL);
                    }
                }
            }
        }
        
        updateState();
    }
    
    private void updateState() {
        TopComponent active = TopComponent.getRegistry().getActivated();
        Object param = active == null ? "" : active.getName(); // NOI18N

        boolean maximize;
        if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            maximize = WindowManagerImpl.getInstance().getMaximizedMode() == null;
        } else {
            ModeImpl activeMode = (ModeImpl)WindowManagerImpl.getInstance().findMode(active);
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

        if(maximize) {
            putValue(Action.NAME, NbBundle.getMessage(MaximizeWindowAction.class, "CTL_MaximizeWindowAction", param));
        } else {
            putValue(Action.NAME, NbBundle.getMessage(MaximizeWindowAction.class, "CTL_UnmaximizeWindowAction", param));
        }

        setEnabled(active != null);
    }
    
}

