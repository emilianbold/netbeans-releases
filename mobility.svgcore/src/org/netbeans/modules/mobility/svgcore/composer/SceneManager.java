/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
package org.netbeans.modules.mobility.svgcore.composer;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import javax.microedition.m2g.SVGImage;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.actions.DeleteActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.HighlightActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveBackwardActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveForwardActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveToBottomActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveToTopActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.RotateActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.ScaleActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.SelectAction;
import org.netbeans.modules.mobility.svgcore.composer.actions.SelectActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.TranslateActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Pavel Benes
 */
public class SceneManager {
    private final SVGDataObject               m_dObj;
    private final InstanceContent             m_lookupContent;
    private       PerseusController           m_perseusController;
    private       ScreenManager               m_screenMgr;
    private       InputControlManager         m_inputControlMgr;
    private final List<ComposerActionFactory> m_registeredActions = new ArrayList<ComposerActionFactory>();
    private final Stack<ComposerAction>       m_activeActions = new Stack<ComposerAction>();
    private       SelectActionFactory         m_selectActionFactory;
    private final List<SelectionListener>     m_selectionListeners = new ArrayList<SelectionListener>();
    private       SVGImage                    m_svgImage      = null;
    private       boolean                     m_isReadOnly      = true;

    public static interface SelectionListener {
        public void selectionChanged( SVGObject [] newSelection, SVGObject [] oldSelection, boolean isReadOnly);
    }
    
    public SceneManager(SVGDataObject dObj,InstanceContent lookupContent) {
        m_dObj              = dObj;
        m_lookupContent     = lookupContent;
    }

    public void initialize() {
        //TODO HACK - revisit
        PatchedGroup.s_sceneMgr = this;

        //TODO use some Netbeans mechanism for action registration
        m_selectActionFactory = new SelectActionFactory(this); 
        m_registeredActions.add( new HighlightActionFactory(this));
        m_registeredActions.add( m_selectActionFactory);
        m_registeredActions.add( new TranslateActionFactory(this));
        m_registeredActions.add( new ScaleActionFactory(this));
        m_registeredActions.add( new RotateActionFactory(this));
        m_registeredActions.add( new DeleteActionFactory(this));
        m_registeredActions.add( new MoveToTopActionFactory(this));
        m_registeredActions.add( new MoveToBottomActionFactory(this));
        m_registeredActions.add( new MoveForwardActionFactory(this));
        m_registeredActions.add( new MoveBackwardActionFactory(this));
    }
    
    public void setImage(SVGImage svgImage) {
        m_svgImage = svgImage;
        m_perseusController = new PerseusController(this);
        m_screenMgr         = new ScreenManager(this);
        m_inputControlMgr   = new InputControlManager(this);
        
        m_perseusController.initialize();
        m_screenMgr.initialize();
        m_inputControlMgr.initialize();
    }
    
    public void registerPopupActions( Action [] actions, Lookup lookup) {
        List<Action> factoryMenuActions = new ArrayList(Arrays.asList(actions));
        
        for (ComposerActionFactory factory : m_registeredActions) {
            Action a;
            if ( (a=factory.getMenuAction()) != null) {
                factoryMenuActions.add(a);
            }
        }
        
        actions = factoryMenuActions.toArray( new Action[factoryMenuActions.size()]);
        JPopupMenu popup = Utilities.actionsToPopup( actions, lookup);
        m_screenMgr.registerPopupMenu(popup);
    }
    
    public Action [] getMenuActions() {
        List<Action> factoryMenuActions = new ArrayList();
        
        for (ComposerActionFactory factory : m_registeredActions) {
            Action a;
            if ( (a=factory.getMenuAction()) != null) {
                factoryMenuActions.add(a);
            }
        } 
        return factoryMenuActions.toArray( new Action[factoryMenuActions.size()]);
    }

    public SVGDataObject getDataObject() {
        return m_dObj;
    }
    
    public InstanceContent getLoookupContent() {
        return m_lookupContent;
    }
    
    public PerseusController getPerseusController() {
        return m_perseusController;
    }

    public ScreenManager getScreenManager() {
        return m_screenMgr;
    }
    
    public SVGImage getSVGImage() {
        return m_svgImage;
    }

    public JComponent getComposerGUI() {
        return m_screenMgr.getComponent();
    }

    public void addSelectionListener( SelectionListener listener) {
        m_selectionListeners.add(listener);
    }
    
    public boolean isReadOnly() {
        return m_isReadOnly;
    }
    
    public void setReadOnly(boolean isReadOnly) {
        if ( m_isReadOnly != isReadOnly) {
            m_isReadOnly = isReadOnly;
            if ( !m_isReadOnly) {
                m_perseusController.stopAnimator();
            }
            SVGObject [] selected = getSelected();
            notifySelectionChanged(selected, selected);
            m_screenMgr.repaint();
        }
    }
    
    public void setSelection(String id) {
        SVGObject selectedObj = m_perseusController.getObjectById(id);
        
        if (selectedObj != null) {
            SVGObject [] oldSelection = getSelected();

            SelectAction action = m_selectActionFactory.getActiveAction();
            if (action != null) {
                action.actionCompleted();
            }
            
            m_activeActions.push( m_selectActionFactory.startAction(selectedObj));
            ActionMouseCursor cursor = m_selectActionFactory.getMouseCursor(null);
            m_screenMgr.setCursor(cursor != null ? cursor.getCursor() : null);

            //TODO implement better selection change handling
            SVGObject [] newSelection = getSelected();
            if (!areSame(newSelection, oldSelection)) {
                selectionChanged(newSelection, oldSelection);
            }        
        }
    }
    
     void processEvent(InputEvent event) {
         if (!isReadOnly()) {
           SVGObject [] oldSelection = getSelected();

            //first let ongoing actions to process the event         
            boolean consumed = false;
            for (int i = m_activeActions.size() - 1; i >= 0; i--) {
                ComposerAction action = m_activeActions.get(i);
                if ( action.consumeEvent(event)) {
                    consumed = true;
                    break;
                }
                if (action.isCompleted()) {
                    m_activeActions.remove(i);
                }
            }

            ComposerAction action = null;

            if ( !consumed) {
                //now check if the new action should be started
                for (int i = m_registeredActions.size() - 1; i >= 0; i--) {
                    if ( (action=m_registeredActions.get(i).startAction(event)) != null) {
                        m_activeActions.push(action);
                        break;
                    }
                }
            } 

            ActionMouseCursor cursor = null;
            for (int i = m_registeredActions.size() - 1; i >= 0; i--) {
                ActionMouseCursor c;
                if ( (c=m_registeredActions.get(i).getMouseCursor(event)) != null) {
                    if (cursor == null || cursor.getPriority() < c.getPriority()) {
                        cursor = c;
                    }
                }
            }  
            m_screenMgr.setCursor(cursor != null ? cursor.getCursor() : null);

            //TODO implement better selection change handling
            SVGObject [] newSelection = getSelected();
            if (!areSame(newSelection, oldSelection)) {
                selectionChanged(newSelection, oldSelection);
            }                    
        }
    }

    public SVGObject []  getSelected() {
        SVGObject    selected = null;
        SelectAction action   = m_selectActionFactory.getActiveAction();
        
        if (action != null) {
            selected = action.getSelected();
        }
        if (selected != null) {
            return new SVGObject[] { selected };
        } else {
            return null;
        }
    }
    
    public Stack<ComposerAction> getActiveActions() {
        return m_activeActions;
    }

    public boolean containsAction( Class clazz) {
        for (int i = m_activeActions.size() - 1; i >= 0; i--) {
            if ( clazz.isInstance( m_activeActions.get(i))) {
                return true;
            }
        }
        return false;    
    }
    
    public void deleteObject(SVGObject svgObj) {
        SVGObject [] oldSelection = getSelected();
        svgObj.delete();
        SVGObject [] newSelection = getSelected();
        if (!areSame(newSelection, oldSelection)) {
            selectionChanged(newSelection, oldSelection);
        }                    
    }
        
    protected void selectionChanged(SVGObject [] newSelection, SVGObject [] oldSelection) { 
        if (oldSelection != null) {
            for (int i = 0; i < oldSelection.length; i++) {
                m_lookupContent.remove(oldSelection[i]);
            }
        }

        if (newSelection != null && newSelection.length > 0) {
            for (int i = 0; i < newSelection.length; i++) {
                m_lookupContent.add(newSelection[i]);
            }
            //TODO use better mechanism for selection handling
            m_dObj.getModel().setSelected( newSelection[0].getElementId());
        }  
        notifySelectionChanged(newSelection, oldSelection);
    }
    
    protected void notifySelectionChanged(SVGObject [] newSelection, SVGObject [] oldSelection) {
        for (SelectionListener listener : m_selectionListeners) {
            listener.selectionChanged(newSelection, oldSelection, m_isReadOnly);
        }
    }    
    
    //TODO move to SVGObject class
    protected static boolean areSame(SVGObject [] arr1,SVGObject [] arr2) {
        if (arr1 == arr2) {
            return true;
        } else if (arr1 == null || arr2 == null) {
            return false;
        } else if (arr1.length != arr2.length) {
            return false;
        } else {
            for (int i = 0; i < arr1.length; i++) {
                if ( arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}   
