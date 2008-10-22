/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */package org.netbeans.modules.mobility.svgcore.composer;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.m2g.SVGImage;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.actions.CursorPositionActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.DeleteActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.HighlightActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveBackwardActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveFocusActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveForwardActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveToBottomActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveToTopActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.RotateActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.ScaleActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.ScaleXActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.ScaleYActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.SelectAction;
import org.netbeans.modules.mobility.svgcore.composer.actions.SelectActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.SkewActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.TranslateActionFactory;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGStatusBar;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public final class SceneManager {   
    public static final int    EVENT_ANIM_STARTED    = AWTEvent.RESERVED_ID_MAX + 534;
    public static final int    EVENT_ANIM_STOPPED    = EVENT_ANIM_STARTED + 1;
    public static final int    EVENT_IMAGE_DISPLAYED = EVENT_ANIM_STOPPED + 1;    
    public static final String OPERATION_TOKEN       = "operation"; //NOI18N
        
    private static final Logger  LOGGER;
    private static final String  LOGGER_NAME = "mobility.svg"; //NOI18N
    private static final boolean LOG_TIME;
    private static final boolean LOG_THREAD;
    
    private static int           s_instanceCounter = 0;
    
    static {
        LOGGER = Logger.getLogger( LOGGER_NAME); 
        if ( LOGGER.getLevel() == null) {
            LOGGER.setLevel( Level.WARNING);
        }
        //Logger.getLogger("global").log( Level.INFO, "mobility.svg.level=" + LOGGER.getLevel()); //NOI18N
        String str;

        str = LOGGER_NAME + ".logTime"; //NOI18N
        LOG_TIME = Boolean.valueOf(System.getProperty(str)); 
        LOGGER.info(str + '=' + LOG_TIME);
        
        str = LOGGER_NAME + ".logThread"; //NOI18N
        LOG_THREAD = Boolean.valueOf(System.getProperty(str)); 
        LOGGER.info(str + '=' + LOG_THREAD);
    }

    public static boolean isEnabled( Level level) {
        return LOGGER.isLoggable(level);
    }
    
    public static void log( Level level, String msg, Throwable thrown) {
        LOGGER.log(level, decorate(level, msg), thrown);
    }

    public static void log( Level level, String msg) {
        LOGGER.log(level, decorate(level, msg));
    }

    public static void error( String msg, Throwable thrown) {
        log(Level.SEVERE, msg, thrown);
        NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(thrown);
        DialogDisplayer.getDefault().notifyLater(e);        
    }
    
    private static String decorate(Level level, String msg) {
        if ( LOGGER.isLoggable(level) && (LOG_THREAD || LOG_TIME)) {
            StringBuilder sb = new StringBuilder();
            if ( LOG_TIME) {
                sb.append( System.currentTimeMillis());
                sb.append(' ');
            }
            if ( LOG_THREAD) {
                sb.append('[');
                sb.append( Thread.currentThread().toString());
                sb.append("] "); //NOI18N
            }
            sb.append(msg);
            msg = sb.toString();
        }
        return msg;
    }
    
    private transient SVGDataObject               m_dObj = null;
    private transient int                         m_instanceID;
    private transient InstanceContent             m_lookupContent;
    private transient Lookup                      m_lookup;   
    private transient PerseusController           m_perseusController;
    private transient ScreenManager               m_screenMgr;
    private transient InputControlManager         m_inputControlMgr;
    private transient List<ComposerActionFactory> m_actionFactories;
    private transient Stack<ComposerAction>       m_activeActions;
    private transient List<Action>                m_registeredActions;
    public transient  SelectActionFactory         m_selectActionFactory;
    private transient List<SelectionListener>     m_selectionListeners;
    private transient SVGImage                    m_svgImage;
    private transient SVGLocatableElement         m_popupElement = null;
    // use to remember element selection during changes in the editor
    private transient String                      m_selectedId = null;
    private transient String                      m_selectedTag = null;
    private           float                       m_animationDuration = PerseusController.ANIMATION_DEFAULT_DURATION; 
    private final     List<String>                m_busyStates   = new ArrayList<String>(4);
    private boolean                               m_busyCursorOn = false;
        
    /** persistent properties */
    private boolean  m_isReadOnly   = true;
            float    m_zoomRatio    = (float)1.0;
    
    public static interface SelectionListener {
        public void selectionChanged( SVGObject [] newSelection, SVGObject [] oldSelection, boolean isReadOnly);
    }
    
    public static AWTEvent createEvent(Object source, int id) {
        return new AWTEvent( source, id) {};
    }

    public SceneManager() {
        m_activeActions = new Stack<ComposerAction>();
    }

    public void initialize(SVGDataObject dObj) {
        assert m_dObj == null : "Scene manager cannot be initialized twice"; //NOI18N
        m_dObj              = dObj;
        m_instanceID        = s_instanceCounter++;
        m_lookupContent     = new InstanceContent();
        m_lookup            = new AbstractLookup(m_lookupContent);        

        m_actionFactories       = new ArrayList<ComposerActionFactory>();
        m_selectionListeners    = new ArrayList<SelectionListener>();
        m_activeActions.clear();
        
        m_selectActionFactory = new SelectActionFactory(this); 
        addSelectionListener(m_selectActionFactory);
        
        m_actionFactories.add( new HighlightActionFactory(this));
        m_actionFactories.add( m_selectActionFactory);
        m_actionFactories.add( new TranslateActionFactory(this));
        m_actionFactories.add( new ScaleXActionFactory(this));
        m_actionFactories.add( new ScaleYActionFactory(this));
        m_actionFactories.add( new ScaleActionFactory(this));
        m_actionFactories.add( new SkewActionFactory(this));
        m_actionFactories.add( new RotateActionFactory(this));
        m_actionFactories.add( new DeleteActionFactory(this));
        m_actionFactories.add( new MoveToTopActionFactory(this));
        m_actionFactories.add( new MoveToBottomActionFactory(this));
        m_actionFactories.add( new MoveForwardActionFactory(this));
        m_actionFactories.add( new MoveBackwardActionFactory(this));
        m_actionFactories.add( new CursorPositionActionFactory(this));
        m_actionFactories.add( new MoveFocusActionFactory(this));

        m_screenMgr = new ScreenManager(this);
        updateStatusBar();

        m_registeredActions = new ArrayList<Action>();
        for (ComposerActionFactory factory : m_actionFactories) {
            Action [] factoryActions;
            if ( (factoryActions=factory.getMenuActions()) != null) {
                for (Action action : factoryActions) {
                    m_registeredActions.add(action);
                }
            }
        }        
    }

    public String toString() {
        return "SceneManager-" + m_instanceID + "-" + (m_dObj != null ? m_dObj.getPrimaryFile().toString() : "null"); //NOI18N
    }
    
    public synchronized void setImage(SVGImage svgImage) {
        if (m_svgImage != null) {
            resetImage();
        }
        
        m_lookupContent.add(svgImage);
        m_svgImage = svgImage;
        
        m_perseusController = new PerseusController(this);
        m_inputControlMgr   = new InputControlManager(this);
        
        m_perseusController.initialize();
        m_screenMgr.initialize();
        m_inputControlMgr.initialize();
                
        m_screenMgr.refresh();
    }
    
    public synchronized void resetImage() {
        if (m_svgImage != null) {
            m_lookupContent.remove(m_svgImage);
            m_svgImage          = null;
        }
        m_popupElement      = null;
        m_perseusController = null;
        m_screenMgr.reset();
        m_inputControlMgr   = null;
        // remove all running actions
        synchronized( m_activeActions) {
            m_activeActions.clear();        
        }
    }
      
    public synchronized boolean isImageLoaded() {
        return m_svgImage != null;
    }
    
    public void saveSelection() {
        if (m_selectedId == null) {
            SVGObject [] selected = getSelected();
            if ( selected != null && selected.length > 0 && selected[0] != null) {
                SVGElement elem = selected[0].getSVGElement();
                m_selectedId  = elem.getId();
                m_selectedTag = elem.getLocalName();
            } else {
                m_selectedId = null;
                m_selectedTag = null;
            }
        }
    }
    
    public void restoreSelection() {
        if ( m_selectedId != null) {
            SVGObject selectedObj = m_perseusController.getObjectById(m_selectedId);
            
            if (selectedObj != null && (m_selectedTag == null ||
                m_selectedTag.equals( selectedObj.getSVGElement().getLocalName()))) {
                setSelection(m_selectedId, false);
            }
            m_selectedId = null;
        }
    }
    
    public void setBusyState( String key, boolean isBusy) {
        synchronized( m_busyStates) {
            if ( m_busyStates.contains(key)) {
                if ( !isBusy) {
                    m_busyStates.remove(key);
                }
            } else {
                if ( isBusy) {
                    m_busyStates.add(key);
                }
            }

            final boolean busyCursorOn = !m_busyStates.isEmpty();
            if (m_busyCursorOn != busyCursorOn) {
                if ( m_screenMgr != null) {
                    final Component comp = m_screenMgr.getComponent();

                    if ( comp != null) {
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                Cursor cursor = busyCursorOn ? Utilities.createProgressCursor(comp) : null;
                                comp.setCursor(cursor);
                            }
                        });
                    }
                }
                m_busyCursorOn = busyCursorOn;
            }
        }
    }
    
    public boolean isBusy() {
        synchronized( m_busyStates) {
            return !m_busyStates.isEmpty();
        }
    }
    
    public void serialize(ObjectOutputStream out) throws IOException {
        out.writeBoolean(m_isReadOnly);
        out.writeFloat(m_zoomRatio);
    }

    public void deserialize(ObjectInputStream in) throws IOException {
        m_isReadOnly   = in.readBoolean();
        m_zoomRatio    = in.readFloat();
    }
        
    Action getAction(String actionID) {
        for (Action action : m_registeredActions) {
            String id;
            if ( action instanceof AbstractSVGAction) {
                id = ((AbstractSVGAction) action).getActionID();
            } else {
                id = action.getClass().getName();
            }
            if ( id != null && id.equals(actionID)) {
                return action;
            }
        }
        SceneManager.log(Level.SEVERE, "Unknown action id " + actionID); //NOI18N
        return null;
    }
    
    public void registerPopupActions( Action [] guiActions, TopComponent tc, Lookup lookup) {
        for (Action action : guiActions) {
            m_registeredActions.add(action);
        }
        
        Lookup lkp = Lookups.forPath("Editors/text/svg+xml/Popup"); //NOI18N
        Collection<? extends ActionWrapperFactory> wrapperCol = lkp.lookupAll(ActionWrapperFactory.class);        

        Action [] popupActions = new Action[wrapperCol.size()];
        int i = 0;
        for ( Iterator<? extends ActionWrapperFactory> iter = wrapperCol.iterator(); iter.hasNext(); ) {
            ActionWrapperFactory factory = iter.next();
            ActionWrapper         wrapper = factory.createWrapper(this);
            
            if (wrapper != null) {
                Action a = wrapper.getAction();
                popupActions[i++] = a;
                if ( a instanceof AbstractSVGAction) {
                    ((AbstractSVGAction) a).registerAction(tc);
                }
            }
        }
        m_screenMgr.registerPopupMenu(popupActions, lookup);
    }
    
    public Action [] getToolbarActions(String ... actionIds) {
        Action [] actions = new Action[actionIds.length];
        
        for (int i = 0; i < actionIds.length; i++) {
            if (actionIds[i] != null) {
                actions[i] = getAction( actionIds[i]);
            }
        }
        return actions;
    }

    public void updateActionState() {
        for ( ComposerActionFactory factory : m_actionFactories) {
            factory.updateActionState();
        }
    }
    public SVGDataObject getDataObject() {
        return m_dObj;
    }
    
    public Lookup getLoookup() {
        return m_lookup;
    }
    
    public synchronized PerseusController getPerseusController() {
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

    public void removeSelectionListener( SelectionListener listener) {
        m_selectionListeners.remove(listener);
    }

    public float getAnimationDuration() {
        return m_animationDuration;
    }
    
    public void updateAnimationDuration(float time) {
        if ( time > m_animationDuration) {
            m_animationDuration = time;
        }
    }
    
    public boolean isReadOnly() {
        return m_isReadOnly;
    }
        
    public void setReadOnly(boolean isReadOnly) {
        if ( m_isReadOnly != isReadOnly) {
            m_isReadOnly = isReadOnly;
            updateStatusBar();
            /*
             * Fix for IZ#145739 - [65cat] NullPointerException at 
             * org.netbeans.modules.mobility.svgcore.composer.SceneManager.setReadOnly
             * 
             * m_perseusController could be null when image was broken 
             * from very beginning. In this case it was not initialized .
             */
            if ( !m_isReadOnly && m_perseusController!= null) {
                m_perseusController.stopAnimator();
            }
            SVGObject [] selected = getSelected();
            notifySelectionChanged(selected, selected);
            if ( m_perseusController!= null ){
                m_screenMgr.repaint();
            }
        }
    }

    public void setSelection(String id, boolean isDelayed) {
        if ( isDelayed) {
            m_selectedId = id;
        } else {
            SVGObject [] oldSelection = getSelected();
            
            SelectAction action = m_selectActionFactory.getActiveAction();
            if (action != null) {
                action.actionCompleted();
            }

            SVGObject [] newSelection = null;
            if ( id != null) {
                SVGObject selectedObj = m_perseusController.getObjectById(id);

                if (selectedObj != null) {
                    synchronized( m_activeActions) {
                        m_activeActions.push( m_selectActionFactory.startAction(selectedObj));
                    }
                    ActionMouseCursor cursor = m_selectActionFactory.getMouseCursor(null, false);
                    m_screenMgr.setCursor(cursor != null ? cursor.getCursor() : null);
                    newSelection = getSelected();
                } else {
                    // TODO Revisit: Hack! Do not send the notification about selection change
                    // if the selected object if not SVGLocatableElement, to allow
                    // SVG tree traversal in the navigator. Correct way is to allow
                    // selection of other SVG elements as well.
                    //return;
                    // fix for #145987 - send null in selection changed notification
                    // Persejus has selected object (getSelected() returns not null),
                    // but we do not want to display this selection.
                    newSelection = null;
                }
                
            }
            //TODO implement better selection change handling
            if (!SVGObject.areSame(newSelection, oldSelection)) {
                selectionChanged(newSelection, oldSelection);
            }        
        }
    }
    
    public void startAction( ComposerAction action) {
        synchronized(m_activeActions) {
            m_activeActions.add(action);
        }
    }
    
     public void processEvent(AWTEvent event) {
         if (isEnabled(Level.FINEST)) {
             SceneManager.log(Level.FINEST, "Processing event: " + event); //NOI18N
         }
         
         if ( !isBusy()) {
            boolean isOutsideEvent = event.getSource() != m_screenMgr.getAnimatorView(); 
            SVGObject [] oldSelection = getSelected();

            //first let ongoing actions to process the event         
            boolean consumed = false;
            ActionMouseCursor cursor = null;

            synchronized( m_activeActions) {
                for (int i = m_activeActions.size() - 1; i >= 0; i--) {
                    ComposerAction action = m_activeActions.get(i);
                    ActionMouseCursor c = action.getMouseCursor(isOutsideEvent);
                    if (cursor == null && c != null) {
                        cursor = c;
                    }
                    if ( action.consumeEvent(event, isOutsideEvent)) {
                        consumed = true;
                        break;
                    }
                    if (action.isCompleted()) {
                        m_activeActions.remove(i);
                    }
                }
            }

            ComposerAction action = null;

            if ( !consumed) {
                //now check if the new action should be started
                for (int i = m_actionFactories.size() - 1; i >= 0; i--) {
                    if ( (action=m_actionFactories.get(i).startAction(event, isOutsideEvent)) != null) {
                        synchronized( m_activeActions) {
                            m_activeActions.push(action);
                        }
                        break;
                    }
                }
            } 

            if ( event instanceof MouseEvent && cursor == null) {
                MouseEvent me = (MouseEvent) event;
                for (int i = m_actionFactories.size() - 1; i >= 0; i--) {
                    ActionMouseCursor c;
                    if ( (c=m_actionFactories.get(i).getMouseCursor(me, isOutsideEvent)) != null) {
                        if (cursor == null || cursor.getPriority() < c.getPriority()) {
                            cursor = c;
                        }
                    }
                }  
            }
            m_screenMgr.setCursor(cursor != null ? cursor.getCursor() : null);

            //TODO implement better selection change handling
            SVGObject [] newSelection = getSelected();
            if (!SVGObject.areSame(newSelection, oldSelection)) {
                selectionChanged(newSelection, oldSelection);
            }                    
         }
    }

    public SVGObject [] getSelected() {
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
        return findAction(clazz) != null;
    }
    
    public ComposerAction findAction( Class clazz) {
        synchronized( m_activeActions) {
            for (int i = m_activeActions.size() - 1; i >= 0; i--) {
                ComposerAction action = m_activeActions.get(i);
                
                if ( clazz.isInstance( action)) {
                    return action;
                }
            }
            return null;    
        }
    }
    
    public void deleteObject(SVGObject svgObj) {
        SVGObject [] oldSelection = getSelected();
        svgObj.delete();
        // fix for #150324. 
        // clear selection manually by setting new value to null.
        // removing transaction is started in separate thread and 
        //  can be in process at this moment. 
        // So have two options - wait for it to finish (to use getSelected()) 
        //  or clear selection manually.
        //SVGObject [] newSelection = getSelected();
        SVGObject [] newSelection = null;
        if (!SVGObject.areSame(newSelection, oldSelection)) {
            selectionChanged(newSelection, oldSelection);
        }                    
    }
        
    /**
     * Sets the position of the cursor. The position is in the component's coordinate
     * system.
     *
     * @param x the new position of the cursor along the x-axis.
     * @param y the new position of the cursor along the y-axis.
     */
    void popupAt(final int x, final int y) {
        SVGLocatableElement elem = m_perseusController.findElementAt(x, y);
        if (m_popupElement != null) {
            m_lookupContent.remove(m_popupElement);
        }
        
        m_popupElement = elem;
        if (m_popupElement != null){
            m_lookupContent.add(m_popupElement);
        }
        m_screenMgr.repaint();
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
        }  
        notifySelectionChanged(newSelection, oldSelection);
    }
    
    protected void notifySelectionChanged(SVGObject [] newSelection, SVGObject [] oldSelection) {
        for (SelectionListener listener : m_selectionListeners) {
            listener.selectionChanged(newSelection, oldSelection, m_isReadOnly);
        }
    }    
    
    private void updateStatusBar() {
        m_screenMgr.getStatusBar().setText( SVGStatusBar.CELL_MODE, m_isReadOnly ? SVGStatusBar.LOCKED : SVGStatusBar.UNLOCKED);
    }
}   
