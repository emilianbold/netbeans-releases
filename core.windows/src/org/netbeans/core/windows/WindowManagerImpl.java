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
 */

package org.netbeans.core.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.*;

/**
 * This class extends WindowManager to provide all window system functionality.
 *
 * This class is final only for performance reasons. Can be freely
 * unfinaled if desired.
 *
 * @author Peter Zavadsky
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.windows.WindowManager.class)
public final class WindowManagerImpl extends WindowManager implements Workspace {
// XXX Implements Workspace for backward compatibility of old API only,
// there are no workspaces any more.
    
    // XXX PENDING additional, not-yet officialy supported properties.
    /** Name of property change fired when active mode changed. */
    public static final String PROP_ACTIVE_MODE = "activeMode"; // NOI18N
    /** Name of property change fired when maximized mode changed. */
    public static final String PROP_MAXIMIZED_MODE = "maximizedMode"; // NOI18N
    /** Name of property change fired when editor area state changed. */
    public static final String PROP_EDITOR_AREA_STATE = "editorAreaState"; // NOI18N    
    
    /** Init lock. */
    private static final Object LOCK_INIT = new Object();
    
    /** The only instance of the window manager implementation in the system */
    private static WindowManagerImpl defaultInstance;

    /** Flag to tell is assertions are on. Note it's package private and non-final for tests */
    static boolean assertsEnabled;

    /** Central unit of window system. */
    private final Central central = new Central();
    
    /** properties support */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    // PENDING
    /** Manages list of recently activated <code>TopCompoennt</code>s. */
    private final RecentViewList recentViewList = new RecentViewList(this);

    /** Only for hack 40237, to not call componentShowing twice */ 
    private TopComponent persistenceShowingTC;
    
    /** exclusive invocation of runnables */
    private Exclusive exclusive = new Exclusive();

    /** timer to ensure exclusive runnables are really invoked in all circumstances */
    private javax.swing.Timer paintedTimer = new javax.swing.Timer(5000, exclusive);

    /** flag that prevents calling Exclusive.run on each main window repaint */
    private boolean exclusivesCompleted = false;

    /** Default constructor. Don't use directly, use getDefault()
     * instead.
     */
    public WindowManagerImpl() {
        synchronized(LOCK_INIT) {
            // a static object to synchronize on
            if(defaultInstance != null) {
                throw new IllegalStateException("Instance already exists"); // NOI18N
            }
            defaultInstance = this;
        }
        paintedTimer.setRepeats(false);
    }
    
    /** Singleton accessor, returns instance of window manager implementation */
    public static WindowManagerImpl getInstance() {
        if (defaultInstance != null) {
            // Save a bunch of time accessing global lookup, acc. to profiler.
            return defaultInstance;
        }
        return (WindowManagerImpl)Lookup.getDefault().lookup(WindowManager.class);
    }
    
    @Override
    public void topComponentRequestAttention(TopComponent tc) {
        ModeImpl mode = (ModeImpl) findMode(tc);
        
        central.topComponentRequestAttention(mode, tc);
    }

    @Override
    public void topComponentCancelRequestAttention(TopComponent tc) {
        ModeImpl mode = (ModeImpl) findMode(tc);
        
        central.topComponentCancelRequestAttention(mode, tc);
    }

    /////////////////////////
    // API impelementation >>
    /////////////////////////
    
    // PENDING revise this method, it is dangerous to expose the GUI.
    /** Provides access to the MainWindow of the IDE.
     * Implements <code>WindowManager</code> abstract method.
     * @return the MainWindow */
    public Frame getMainWindow() {
        warnIfNotInEDT();
        
        return central.getMainWindow();
    }
    
    /** Called after a current LookAndFeel change to update the IDE's UI
     * Implements <code>WindowManager</code> abstract method. */
    public void updateUI() {
        warnIfNotInEDT();
        
        central.updateUI();
    }
    
    /** Creates a component manager for given top component.
     * Implements <code>WindowManager</code> abstract method.
     * @param c the component
     * @return the manager that handles opening, closing and selecting a component
     * @deprecated Don't use this. */
    protected synchronized WindowManager.Component createTopComponentManager(TopComponent c) {
        warnIfNotInEDT();
        
        return null;
    }
    
    /** Creates new workspace with given name and display name.
     * Implements <code>WindowManager</code> abstract method.
     * @return fake implementation of only workspace
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public Workspace createWorkspace(String name, String displayName) {
        warnIfNotInEDT();
        
        // get back fake workspace.
        return this;
    }

    /** Finds workspace given its name.
     * @return fake implementation of only workspace
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public Workspace findWorkspace(String name) {
        warnIfNotInEDT();
        
        // PENDING what to return?
        return this;
    }
    
    /** List of all currenty available workspaces.
     * Implements <code>WindowManager</code> abstract method. 
     * @return array with only one (fake) workspace impl
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public Workspace[] getWorkspaces() {
        warnIfNotInEDT();
        
        return new Workspace[] {this};
    }

    /** Sets new workspaces.
     * Implements <code>WindowManager</code> abstract method.
     * @param workspaces array of new workspaces
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public void setWorkspaces(Workspace[] workspaces) {
        warnIfNotInEDT();
    }

    /** Gets current workspace. Can be changed by calling Workspace.activate ()
     * Implements <code>WindowManager</code> abstract method.
     * @return fake implementation of only workspace
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public Workspace getCurrentWorkspace() {
        warnIfNotInEDT();
        
        // Gets back this as a fake workspace.
        return this;
    }

    /** Finds TopComponentGroup of given name. */
    public TopComponentGroup findTopComponentGroup(String name) {
        assertEventDispatchThread();
        
        for(Iterator it = getTopComponentGroups().iterator(); it.hasNext(); ) {
            TopComponentGroupImpl group = (TopComponentGroupImpl)it.next();
            if(group.getName().equals(name)) {
                return group;
            }
        }
        
        return null;
    }
    
    /** Returns <code>TopComponent</code> for given unique ID.
     * @param tcID unique <code>TopComponent</code> ID
     * @return <code>TopComponent</code> instance corresponding to unique ID
     */
    public TopComponent findTopComponent(String tcID) {
        warnIfNotInEDT();
        
        return getTopComponentForID(tcID);
    }
    
    /** Adds listener.
    * Implements <code>WindowManager</code> abstract method. */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes listener.
     * Implements <code>WindowManager</code> abstract method. */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    ////////////////////////
    // API implementation <<
    ////////////////////////

//    /** Activates <code>TopComponent</code>, if it is opened. */
//    private boolean activateTopComponent(TopComponent tc) {
//        if(tc != null) {
//            // Find whether the component is in mode.
//            ModeImpl mode = (ModeImpl)findMode(tc);
//            if(mode != null) {
//                // Actually activates the TopComponent.
//                central.activateModeTopComponent(mode, tc);
//            } else {
//                // TopComponent not in mode yet.
//                return false;
//            }
//        }
//        
//        return true;
//    }
    
//    /** Selects <code>TopComponent</code>, if it is opened. */
//    protected void selectTopComponentImpl(TopComponent tc) {
//        if(tc != null) {
//            // Find whether the component is in mode.
//            ModeImpl mode = (ModeImpl)findMode(tc);
//            if(mode != null) {
//                // Actually select the TopComponent.
//                central.setModeSelectedTopComponent(mode, tc);
//            }
//        }
//    }
    

    // XXX For backward compatibility (Workspace class), this is the only (fake) workspace.
    // There are not supported workspaces any more.
    ///////////////////////////////////////
    // Start of  Workspace implementation>>
    ///////////////////////////////////////
    
    /** Gets the programmatic unique name of this workspace.
     * Implements <code>Workspace</code> interface method.
     * @return the programmatic name of only workspace impl
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public String getName () {
        return "FakeWorkspace"; // NOI18N
    }
    
    /** Gets human-presentable name of the workspace.
     * Implements <code>Workspace</code> interface method.
     * @return the diplay name of the workspace
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public String getDisplayName () {
        return NbBundle.getMessage(WindowManagerImpl.class, "LBL_FakeWorkspace");
    }

    /** Gets <code>Set</code> of all <code>Mode</code>'s.
     * Implements <code>Workspace</code> interface method. */
    public Set<? extends ModeImpl> getModes () {
        return central.getModes();
    }
    
    /** Get bounds.
     * Implements <code>Workspace</code> interface method. */
    public Rectangle getBounds () {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            return getMainWindowBoundsJoined();
        } else {
            return getMainWindowBoundsSeparated();
        }
    }

    /** Activates this workspace to be current one.
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public void activate () {
    }
    
    /** Creates new <code>Mode</code>.
     * Implements <code>Workspace</code> interface method.
     * @param name a unique programmatic name of the mode 
     * @param displayName <em>ignored</em> doesn't have a sense now
     * @param icon <em>ignored</em> doesn't have a sense now
     * @return the new mode */
    public Mode createMode(String name, String displayName, URL icon) {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            return new WrapMode (createMode(name, Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, null));
        } else {
            // #36945 In 'separate' ui mode create new mode.
            return createMode(name, Constants.MODE_KIND_VIEW, Constants.MODE_STATE_SEPARATED, false,
                new SplitConstraint[] { new SplitConstraint(Constants.HORIZONTAL, 1, 0.2)});
        }
    }
    private static class WrapMode implements Mode {
        private Mode wrap;
        
        public WrapMode (Mode wrap) {
            this.wrap = wrap;
        }
        
        public void addPropertyChangeListener (PropertyChangeListener list) {
            wrap.addPropertyChangeListener (list);
        }
        
        public boolean canDock (TopComponent tc) {
            return wrap.canDock (tc);
        }
        
        public boolean dockInto (TopComponent c) {
            if (c.getClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE) == null) {
                c.putClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE, Boolean.TRUE);
            }
            return wrap.dockInto (c);
        }
        
        public Rectangle getBounds () {
            return wrap.getBounds ();
        }
        
        public String getDisplayName () {
            return wrap.getDisplayName ();
        }
        
        public Image getIcon () {
            return wrap.getIcon ();
        }
        
        public String getName () {
            return wrap.getName ();
        }
        
        public TopComponent getSelectedTopComponent () {
            return wrap.getSelectedTopComponent ();
        }
        
        public TopComponent[] getTopComponents () {
            return wrap.getTopComponents ();
        }
        
        public Workspace getWorkspace () {
            return wrap.getWorkspace ();
        }
        
        public void removePropertyChangeListener (PropertyChangeListener list) {
            wrap.removePropertyChangeListener (list);
        }
        
        public void setBounds (Rectangle s) {
            wrap.setBounds (s);
        }
    } // end of WrapMode

    /** Finds mode by specified name.
     * Implements <code>Workspace</code> interface method.
     * @param name the name of the mode to search for
     * @return the mode with that name, or <code>null</code> */
    public Mode findMode(String name) {
        return findModeImpl(name);
    }
    
    /** Finds mode the component is in.
     * Implements <code>Workspace</code> interface method.
     * @param c component to find mode for
     * @return the mode or <code>null</code> if the component is not in any mode */
    public Mode findMode(TopComponent tc) {
        if(tc == null) {
            // Log something?
            return null;
        }
        
        for(Iterator it = getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            
            if(mode.containsTopComponent(tc)) {
                return mode;
            }
        }

        return null;
    }
    
    /** Clears this workspace and removes this workspace from window manager.
     * Implements <code>Workspace</code> interface method.
     * @deprecated Doesn't have a sense now. Workspaces aren't supported anymore. */
    public void remove () {
    }
    ////////////////////////////////////
    // End of Workspace implementation<<
    ////////////////////////////////////

    //////////////////////////////
    // TopComponentGroup>>
    public void addTopComponentGroup(TopComponentGroupImpl tcGroup) {
        central.addTopComponentGroup(tcGroup);
    }
    
    public void removeTopComponentGroup(TopComponentGroupImpl tcGroup) {
        central.removeTopComponentGroup(tcGroup);
    }
    
    public Set<TopComponentGroupImpl> getTopComponentGroups() {
        return central.getTopComponentGroups();
    }
    // TopComponentGroup<<
    //////////////////////////////

    
    /// Copy from older WorkspaceImpl>>

    ////////////////////////////////////////////////////////
    // PENDING some of the next methods could make inner API
    /** Creates new mode.
     * @param name a unique programmatic name of the mode 
     * @param permanent true if mode has to remain in model even it is emptied */
    public ModeImpl createMode(String name, int kind, int state, boolean permanent, SplitConstraint[] constraints) {
        // It gets existing mode with the same name.
        ModeImpl mode = (ModeImpl)findMode(name);
        if(mode != null) {
            return mode;
        }
        
        // XXX PENDING When no constraints are specified, default (editor or view) mode is returned.
        if(constraints == null && kind != Constants.MODE_KIND_SLIDING) {
            if(kind == Constants.MODE_KIND_EDITOR) {
                return getDefaultEditorMode();
            } else {
                return getDefaultViewMode();
            }
        }

        mode = createModeImpl(name, kind, state, permanent);
        addMode(mode, constraints);
        return mode;
    }
    
    public ModeImpl createSlidingMode(String name, boolean permanent, String side, Map<String,Integer> slideInSizes) {
        // It gets existing mode with the same name.
        ModeImpl mode = (ModeImpl)findMode(name);
        if(mode != null) {
            return mode;
        }
        
        mode = createModeImpl(name, Constants.MODE_KIND_SLIDING, permanent);
        central.addSlidingMode(mode, null, side, slideInSizes);
        return mode;
    }
    
    
    /*private*/ ModeImpl createModeImpl(String name, int kind, boolean permanent) {
        int state = getEditorAreaState() == Constants.EDITOR_AREA_JOINED
                                                ? Constants.MODE_STATE_JOINED
                                                : Constants.MODE_STATE_SEPARATED;
        return createModeImpl(name, kind, state, permanent);
    }
    
    /** */
    /*private*/ ModeImpl createModeImpl(String name, int kind, int state, boolean permanent) {
        if(name == null) {
            name = ModeImpl.getUnusedModeName();
        }
        ModeImpl toReturn =  ModeImpl.createModeImpl(name, state, kind, permanent);
        return toReturn;
    }

    // XXX
    /** Gets default mode. */
    /*private*/ ModeImpl getDefaultEditorMode() {
        ModeImpl mode = findModeImpl("editor"); // NOI18N
        if(mode == null) {
            Logger.getLogger(WindowManagerImpl.class.getName()).log(Level.FINE, null,
                              new java.lang.IllegalStateException("Creating default editor mode. It shouldn\'t happen this way")); // NOI18N
            // PENDING should be defined in winsys layer?
            ModeImpl newMode = createModeImpl("editor", Constants.MODE_KIND_EDITOR, true); // NOI18N
            addMode(newMode, new SplitConstraint[0]);
            return newMode;
        } else {
            return mode;
        }
    }
    
    /** Gets default mode for opening new component. */
    /*private*/ ModeImpl getDefaultEditorModeForOpen() {
        ModeImpl mode = central.getLastActiveEditorMode();
        if (mode == null) {
            return getDefaultEditorMode();
        } else {
            return mode;
        }
    }
    
    // XXX
    /** Gets default view mode. */
    ModeImpl getDefaultViewMode() {
        ModeImpl mode = findModeImpl("explorer"); // NOI18N
        if(mode == null) {
            Logger.getLogger(WindowManagerImpl.class.getName()).log(Level.INFO, null,
                              new java.lang.IllegalStateException("Creating default view mode. It shouldn\'t happen this way")); // NOI18N
            // PENDING should be defined in winsys layer?
            ModeImpl newMode = createModeImpl("explorer", Constants.MODE_KIND_VIEW, true); // NOI18N
            addMode(newMode, new SplitConstraint[] {
                new SplitConstraint(Constants.VERTICAL, 0, 0.7D),
                new SplitConstraint(Constants.HORIZONTAL, 0, 0.25D)
            });
            return newMode;
        } else {
            return mode;
        }
    }
    
    /** Gets default sliding view mode. */
    ModeImpl getDefaultSlidingMode() {
        ModeImpl mode = findModeImpl("sliding"); // NOI18N
        if(mode == null) {
            Logger.getLogger(WindowManagerImpl.class.getName()).log(Level.INFO, null,
                              new java.lang.IllegalStateException("Creating default sliding mode. It shouldn\'t happen this way")); // NOI18N
            // PENDING should be defined in winsys layer?
            ModeImpl newMode = createModeImpl("sliding", Constants.MODE_KIND_SLIDING, true); // NOI18N
            addMode(newMode, new SplitConstraint[] {
                new SplitConstraint(Constants.VERTICAL, 0, 0.7D),
                new SplitConstraint(Constants.HORIZONTAL, 0, 0.25D)
            });
            return newMode;
        } else {
            return mode;
        }
    }
    
    private ModeImpl findModeImpl(String name) {
        if(name == null) {
            // PENDING log something?
            return null;
        }
        
        for(Iterator it = getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if (name.equals(mode.getName())) {
                return mode;
            }
        }
        
        return null;
    }
    
    // XXX PENDING see WindowManager
    public TopComponent getSelectedTopComponent(Mode mode) {
        return central.getModeSelectedTopComponent((ModeImpl)mode);
    }
    
    public Rectangle getMainWindowBoundsJoined() {
        return central.getMainWindowBoundsJoined();
    }
    
    public void setMainWindowBoundsJoined(Rectangle bounds) {
        central.setMainWindowBoundsJoined(bounds);
    }
    
    public Rectangle getMainWindowBoundsSeparated() {
        return central.getMainWindowBoundsSeparated();
    }
    
    public void setMainWindowBoundsSeparated(Rectangle bounds) {
        central.setMainWindowBoundsSeparated(bounds);
    }
    
    public int getMainWindowFrameStateJoined() {
        return central.getMainWindowFrameStateJoined();
    }
    
    public void setMainWindowFrameStateJoined(int frameState) {
        central.setMainWindowFrameStateJoined(frameState);
    }
    
    public int getMainWindowFrameStateSeparated() {
        return central.getMainWindowFrameStateSeparated();
    }
    
    public void setMainWindowFrameStateSeparated(int frameState) {
        central.setMainWindowFrameStateSeparated(frameState);
    }
    
    
    /** Gets active mode.
     * @return active mode */
    public ModeImpl getActiveMode () {
        return central.getActiveMode();
    }
    
    /** Sets active mode.
     * @param current active mode */
    public void setActiveMode(ModeImpl activeMode) {
        central.setActiveMode(activeMode);
    }
    
    public void setEditorAreaBounds(Rectangle editorAreaBounds) {
        central.setEditorAreaBounds(editorAreaBounds);
    }
    
    public Rectangle getEditorAreaBounds() {
        return central.getEditorAreaBounds();
    }

    /** Sets editor area constraints. */
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints) {
        central.setEditorAreaConstraints(editorAreaConstraints);
    }
    
    public java.awt.Component getEditorAreaComponent() {
        return central.getEditorAreaComponent();
    }
    
    /** Gets editor area constraints. */
    public SplitConstraint[] getEditorAreaConstraints() {
        return central.getEditorAreaConstraints();
    }
    
    /** Sets editor area state. */
    public void setEditorAreaState(int editorAreaState) {
        setEditorAreaStateImpl(editorAreaState);
    }
    
    // XXX
    void setEditorAreaStateImpl(int editorAreaState) {
        central.setEditorAreaState(editorAreaState);
    }
    
    public int getEditorAreaState() {
        return central.getEditorAreaState();
    }
    
    public void setEditorAreaFrameState(int editorAreaFrameState) {
        central.setEditorAreaFrameState(editorAreaFrameState);
    }
    
    public int getEditorAreaFrameState() {
        return central.getEditorAreaFrameState();
    }
    
    /** 
     * Sets new maximized mode or cancels the current one. 
     * @param newMaximizedMode Mode to set as the maximized one or null to cancel the current one.
     */
    public void switchMaximizedMode(ModeImpl newMaximizedMode) {
        central.switchMaximizedMode(newMaximizedMode);
    }
    
    /** Sets editor mode that is currenlty maximized (used when the window system loads) */
    public void setEditorMaximizedMode(ModeImpl editorMaximizedMode) {
        central.setEditorMaximizedMode(editorMaximizedMode);
    }
    
    /** Sets view mode that is currenlty maximized (used when the window system loads) */
    public void setViewMaximizedMode(ModeImpl viewMaximizedMode) {
        central.setViewMaximizedMode(viewMaximizedMode);
    }
    
    /** Gets mode that is currently maximized. */
    public ModeImpl getCurrentMaximizedMode() {
        return central.getCurrentMaximizedMode();
    }
    
    /** Gets editor maximized mode. */
    public ModeImpl getEditorMaximizedMode() {
        return central.getEditorMaximizedMode();
    }
    
    /** Gets view maximized mode. */
    public ModeImpl getViewMaximizedMode() {
        return central.getViewMaximizedMode();
    }
    
    /** Sets constraints, delegates from ModeImpl. */
    public void setModeConstraints(ModeImpl mode, SplitConstraint[] modeConstraints) {
        central.setModeConstraints(mode, modeConstraints);
    }
    
    /** Gets constraints, delegates from ModeImpl. */
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        return central.getModeConstraints(mode);
    }

    /** Adds mode. */
    private void addMode(ModeImpl mode, SplitConstraint[] modeConstraints) {
        if (mode.getKind() == Constants.MODE_KIND_SLIDING) {
            // TODO.. where to get the side..
            central.addSlidingMode(mode, null, Constants.LEFT, null);
        } else {
            central.addMode(mode, modeConstraints);
        }
    }
    
    /** Removes mode. */
    public void removeMode(ModeImpl mode) {
        if (mode.getKind() == Constants.MODE_KIND_SLIDING) {
            
        } else {
            central.removeMode(mode);
        }
    }

    /** Sets toolbar configuration name. */
    public void setToolbarConfigName(String toolbarConfigName) {
        central.setToolbarConfigName(toolbarConfigName);
    }

    /** Gets toolbar configuration name.
     * @return toolbar configuration name */
    public String getToolbarConfigName () {
        return central.getToolbarConfigName();
    }

    // Copy from older WorkspaceImpl<< 
    
   

    /** Sets visible or invisible window system GUI. */
    public void setVisible(boolean visible) {
        if( !visible ) {
            FloatingWindowTransparencyManager.getDefault().stop();
        }
        central.setVisible(visible);

        // handle timer that assures runnign of exclusives when somehow
        // mainWindow.paint is not called during startup
        if (visible) {
            if (!exclusivesCompleted) {
                paintedTimer.restart();
            } else {
                FloatingWindowTransparencyManager.getDefault().start();
            }
        } else {
            paintedTimer.stop();
            exclusivesCompleted = false;
        }
    }
    
    /** Indicates whether windows system shows GUI. */
    public boolean isVisible() {
        return central.isVisible();
    }
    
    /** Attaches TopComponent to one side of mode, it removes it from original one. */
    public void attachTopComponentToSide(TopComponent tc, ModeImpl attachMode, String side) {
        central.attachTopComponentsToSide(new TopComponent[] {tc}, attachMode, side);
    }
    
    // XXX
    public TopComponent getTopComponentForID(String tcID) {
        return PersistenceHandler.getDefault().getTopComponentForID(tcID,true);
    }
    
    public boolean isTopComponentAllowedToMoveAnywhere(TopComponent tc) {
        if(Boolean.TRUE.equals(tc.getClientProperty(Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE))) {
            return true;
        }
        
        return false;
    }
    
    // XXX
    public ModeImpl findModeForOpenedID(String tcID) {
        if(tcID == null) {
            return null;
        }
        
        for(ModeImpl mode: getModes()) {
            if(mode.getOpenedTopComponentsIDs().contains(tcID)) {
                return mode;
            }
        }
        
        return null;
    }
    
    // XXX
    public ModeImpl findModeForClosedID(String tcID) {
        if(tcID == null) {
            return null;
        }
        
        for(ModeImpl mode: getModes()) {
            if(mode.getClosedTopComponentsIDs().contains(tcID)) {
                return mode;
            }
        }
        
        return null;
    }
    
    /** Helper method to retrieve some form of display name of TopComponent.
     * First tries TopComponent's getHtmlDisplayName, if is it null then continues
     * with getDisplayName and getName in this order.
     *
     * @param tc TopComponent to retrieve display name from. May be null.
     * @return TopComponent's display name or null if no display name available
     * or null TopComponent is given
     */
    public String getTopComponentDisplayName(TopComponent tc) {
        if(tc == null) {
            return null;
        }
        String displayName = tc.getHtmlDisplayName();
        if (displayName == null) {
            displayName = tc.getDisplayName();
        }
        if (displayName == null) {
            displayName = tc.getName();
        }
        return displayName;
    }
    
    // PENDING for ModeImpl only.
    Central getCentral() {
        return central;
    }

    // XXX
    public boolean isDragInProgress() {
        return central.isDragInProgress();
    }
    
    /** Analyzes bounds of given top component and finds appropriate side
     * of desktop for sliding for given top component.
     *
     * @param tc top component to find side for sliding for
     * @return side where top component should live in sliding state
     * @see Constants.LEFT 
     */ 
    public String guessSlideSide(TopComponent tc) {
        return central.guessSlideSide(tc);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDocked (TopComponent comp) {
        return central.isDocked(comp);
    }

    /** Takes given top component out of the main window and puts it in
     * new separate floating window.
     *
     * @param tc TopComponent to make floating
     * @param mode mode where TopComponent currently lives (before undock)
     *
     * @throws IllegalStateException when given top component is already floating
     */
    public void userUndockedTopComponent(TopComponent tc, ModeImpl mode) {
        if (!isDocked(tc)) {
            throw new IllegalStateException("TopComponent is already in floating state: " + tc);
        }

        central.userUndockedTopComponent(tc, mode);
    }

    /** Puts given top component back into main window.
     *
     * @param tc TopComponent to put back into main window
     * @param mode mode where TopComponent currently lives (before dock)
     *
     * @throws IllegalStateException when given top component is already inside main window
     */
    public void userDockedTopComponent(TopComponent tc, ModeImpl mode) {
        if (isDocked(tc)) {
            throw new IllegalStateException("TopComponent is already inside main window: " + tc);
        }

        central.userDockedTopComponent(tc, mode);
    }

    // PENDING>>
    public void setRecentViewList(TopComponent[] tcs) {
        recentViewList.setTopComponents(tcs);
    }
    
    public TopComponent[] getRecentViewList() {
        return recentViewList.getTopComponents();
    }
    // PENDING<<
    
    void doFirePropertyChange(final String propName,
    final Object oldValue, final Object newValue) {
        // PENDING When #37529 finished, then uncomment the next row and move the
        // checks of AWT thread away.
        //  WindowManagerImpl.assertEventDispatchThread();
        if(SwingUtilities.isEventDispatchThread()) {
            changeSupport.firePropertyChange(propName, oldValue, newValue);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.firePropertyChange(propName, oldValue, newValue);
                }
            });
        }
    }

    // PENDING used in persistence only, revise how to restrict its usage only there.
    /** Gets persistence observer. */
    public org.netbeans.core.windows.persistence.PersistenceObserver getPersistenceObserver() {
        return PersistenceHandler.getDefault();
    }

    
    /////////////////////////
    // Notifications>>
    public void notifyTopComponentOpened(TopComponent tc) {
        // Inform component instance.
        componentOpenNotify(tc);
        // then let others know that top component was opened...
        notifyRegistryTopComponentOpened(tc);
    }
    
    public void notifyTopComponentClosed(TopComponent tc) {
        // Inform component instance.
        componentCloseNotify(tc);
        // let others know that top component was closed...
        notifyRegistryTopComponentClosed(tc);
    }
    // Notifications<<
    /////////////////////////

    /////////////////////////////
    // Registry notifications
    static void notifyRegistryTopComponentActivated(final TopComponent tc) {
        ((RegistryImpl)getDefault().getRegistry()).topComponentActivated(tc);

        
        // #37457 It is needed to ensure the activation calls are in AWT thread.
        if(SwingUtilities.isEventDispatchThread()) {
            WindowManagerImpl.getInstance().activateComponent(tc);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    WindowManagerImpl.getInstance().activateComponent(tc);
                }
            });
        }
    }
    
    private static void notifyRegistryTopComponentOpened(TopComponent tc) {
        ((RegistryImpl)getDefault().getRegistry()).topComponentOpened(tc);
    }
    
    private static void notifyRegistryTopComponentClosed(TopComponent tc) {
        ((RegistryImpl)getDefault().getRegistry()).topComponentClosed(tc);
    }
    
    private static void notifyRegistrySelectedNodesChanged(TopComponent tc, Node[] nodes) {
        ((RegistryImpl)getDefault().getRegistry()).selectedNodesChanged(tc, nodes);
    }
    // Registry notifications
    /////////////////////////////

    /** Overrides superclass method, to enhance access modifier. */
    @Override
    public void componentShowing(TopComponent tc) {
        if((tc != null) && (tc != persistenceShowingTC)) {
            super.componentShowing(tc);
        }
    }
    
    /** XXX - Hack for 40237, should be changed to fix real reason of 37188
     * timing of activate events */
    void specialPersistenceCompShow(TopComponent tc) {
        componentShowing(tc);
        persistenceShowingTC = tc;
    }
    
    /** Overrides superclass method, to enhance access modifier. */
    @Override
    public void componentHidden(TopComponent tc) {
        if(tc != null) {
            super.componentHidden(tc);
            if (tc == persistenceShowingTC) {
                persistenceShowingTC = null;
            }
        }
    }

    
    // Manipulating methods (overriding the superclass dummy ones) >>
    protected void topComponentOpen (TopComponent tc) {
        topComponentOpenAtTabPosition(tc, -1);
    }

    @Override
    protected void topComponentOpenAtTabPosition (TopComponent tc, int position) {
        warnIfNotInEDT();
        
        if (tc == null) {
            throw new IllegalArgumentException ("Cannot open a null " +
                "TopComponent"); //NOI18N
        }
        
        ModeImpl mode = getMode(tc);
        
        if (mode == null) {
            mode = getDefaultEditorModeForOpen();
            assert getModes().contains(mode) : "Mode " + mode.getName() + " is not in model."; //NOI18N
            if (tc.getClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE) == null) {
                tc.putClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE, Boolean.TRUE);
            }
        }
        boolean alreadyOpened = mode.getOpenedTopComponents().contains( tc );

        // XXX PENDING If necessary, unmaximize the state, but exclude sliding modes
        // Consider to put it in addOpenedTopComponent, to do it in one step.
        ModeImpl maximizedMode = getCurrentMaximizedMode();
        if(maximizedMode != null && mode != maximizedMode
           && mode.getKind() != Constants.MODE_KIND_SLIDING
           && (central.isViewMaximized() || mode.getKind() == Constants.MODE_KIND_EDITOR)) {
            switchMaximizedMode(null);
        }
        
        if (position == -1) {
            mode.addOpenedTopComponent(tc);
        } else {
            mode.addOpenedTopComponent(tc, position);
        }
        
        if( central.isEditorMaximized() 
                && !alreadyOpened 
                && mode.getState() != Constants.MODE_STATE_SEPARATED ) {
            //the editor is maximized so the newly opened TopComponent should slide out
            String tcID = findTopComponentID( tc );
            if( !isTopComponentDockedInMaximizedMode( tcID ) && mode.getKind() == Constants.MODE_KIND_VIEW ) {
                //slide the TopComponent to edgebar and slide it out
                central.slide( tc, mode, central.getSlideSideForMode( mode ) );

                topComponentRequestActive( tc );
            }
        }
    }
    
    @Override
    protected int topComponentGetTabPosition (TopComponent tc) {
        warnIfNotInEDT();
        
        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            return mode.getTopComponentTabPosition(tc);
        } else {
            return -1;
        }
    }
    
    protected void topComponentClose(TopComponent tc) {
        warnIfNotInEDT();
        
        boolean opened = topComponentIsOpened(tc);
        boolean inCloseAll = tc.getClientProperty("inCloseAll") != null;
        tc.putClientProperty("inCloseAll", null);

        if(!opened) {
            return;
        }

        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            if( mode == central.getCurrentMaximizedMode() && central.isViewMaximized() ) {
                central.switchMaximizedMode( null );
                topComponentClose( tc );
            } else {
                TopComponent recentTc = null;
                if( mode.getKind() == Constants.MODE_KIND_EDITOR && !inCloseAll ) {
                    //an editor document is being closed so let's find the most recent editor to select
                    recentTc = central.getRecentTopComponent( mode, tc );
                }
                mode.close(tc);
                if( !tc.isOpened() && null != recentTc )
                    mode.setSelectedTopComponent(recentTc);
            }
        }
    }
    
    protected void topComponentRequestActive(TopComponent tc) {
        warnIfNotInEDT();
        
        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            central.activateModeTopComponent(mode, tc);
        }
    }
    
    protected void topComponentRequestVisible(TopComponent tc) {
        warnIfNotInEDT();
        
        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            central.setModeSelectedTopComponent(mode, tc);
            if( mode.getState() == Constants.MODE_STATE_SEPARATED ) {
                tc.toFront();
            }
        }
    }

    protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
        warnIfNotInEDT();
        
        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            central.topComponentDisplayNameChanged(mode, tc);
        }
    }
    
    protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
        // do the same thing as for display name, we can because string param is ignored
        topComponentDisplayNameChanged(tc, null);
    }
    
    protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
        warnIfNotInEDT();
        
        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            central.topComponentToolTipChanged(mode, tc);
        }
    }
    
    protected void topComponentIconChanged(TopComponent tc, Image icon) {
        warnIfNotInEDT();
        
        ModeImpl mode = getModeForOpenedTopComponent(tc);
        if(mode != null) {
            central.topComponentIconChanged(mode, tc);
        }
    }

    protected void topComponentActivatedNodesChanged(TopComponent tc, Node[] activatedNodes) {
        warnIfNotInEDT();
        
        notifyRegistrySelectedNodesChanged(tc, activatedNodes);
    }
    
    protected boolean topComponentIsOpened(TopComponent tc) {
        warnIfNotInEDT();
        
        return getModeForOpenedTopComponent(tc) != null;
    }
    
    protected Action[] topComponentDefaultActions(TopComponent tc) {
        warnIfNotInEDT();
        
        return ActionUtils.createDefaultPopupActions(tc);
    }
    
    protected String topComponentID (TopComponent tc, String preferredID) {
        warnIfNotInEDT();
        
        if (preferredID == null) {
            Logger.getLogger(WindowManagerImpl.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalStateException("Assertion failed. " +
                                                                  tc.getClass().getName() +
                                                                  ".preferredID method shouldn\'t be overriden to return null. " +
                                                                  "Please change your impl to return non-null string.")); // NOI18N
        }
        
        return PersistenceManager.getDefault().getGlobalTopComponentID(tc, preferredID);
    }

    @Override
    public void invokeWhenUIReady(Runnable run) {
        exclusive.register(run);
    }
    
    @Override
    public boolean isEditorTopComponent( TopComponent tc ) {
        if( null == tc )
            return false;
        //check opened TopComponents first to avoid AWT assertion if possible
        for(ModeImpl mode: getModes()) {
            if( mode.getKind() != Constants.MODE_KIND_EDITOR )
                continue;
            if( mode.containsTopComponent( tc ) ) {
                return true;
            }
        }

        //unknown TopComponent
        return false;
    }
    
    @Override
    public boolean isOpenedEditorTopComponent( TopComponent tc ) {
        if( null == tc )
            return false;
        for(ModeImpl mode: getModes()) {
            if( mode.getKind() != Constants.MODE_KIND_EDITOR )
                continue;
            if( mode.getOpenedTopComponents().contains( tc ) ) {
                return true;
            }
        }

        //unknown TopComponent
        return false;
    }
    
    @Override
    public boolean isEditorMode( Mode mode ) {
        if( null == mode )
            return false;
        ModeImpl modeImpl = findModeImpl( mode.getName() );
        return null != modeImpl && modeImpl.getKind() == Constants.MODE_KIND_EDITOR;
    }

    public final void mainWindowPainted () {
        if (!exclusivesCompleted) {
            exclusivesCompleted = true;
            paintedTimer.stop();

            exclusive.register(new Runnable() {
                public void run() {
                    FloatingWindowTransparencyManager.getDefault().start();
                }
            });

            SwingUtilities.invokeLater(exclusive);
        }
    }


    /** Handles exclusive invocation of Runnables.
     */
    private static final class Exclusive implements Runnable, ActionListener {
        /** lists of runnables to run */
        private ArrayList<Runnable> arr = new ArrayList<Runnable>();

        /** Registers given runnable and ensures that it is run when UI 
         * of the system is ready.
         */
        public synchronized void register(Runnable r) {
            arr.add(r);
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            if (!WindowManagerImpl.getInstance().isVisible()) {
                return;
            }
            
            ArrayList<Runnable> arrCopy = null;
            synchronized (this) {
                if (arr.isEmpty()) {
                    return;
                }
                
                arrCopy = arr;
                arr = new ArrayList<Runnable>();
            }

            Logger perf = Logger.getLogger("org.netbeans.log.startup"); // NOI18N
            for (Runnable r : arrCopy) {
                try {
                    perf.log(Level.FINE, "start", "invokeWhenUIReady: " + r.getClass().getName()); // NOI18N
                    r.run();
                    perf.log(Level.FINE, "end", "invokeWhenUIReady: " + r.getClass().getName()); // NOI18N
                } catch (RuntimeException ex) {
                    Logger.getLogger(WindowManagerImpl.class.getName()).log(
                            Level.WARNING, null, ex);
                }
            }
        }

        /** ActionListener implementation - reacts to Timer which ensures
         * invocation of registered exclusive runnables
         */
        public void actionPerformed(ActionEvent e) {
            Logger.getLogger(WindowManagerImpl.class.getName()).log(Level.FINE, 
                    "Painted timer action invoked, which probably means that MainWindow.paint was not called!"); //NOI18N
            WindowManagerImpl.getInstance().mainWindowPainted();
        }

    } // end of Exclusive class

    public void resetModel() {
        central.resetModel();
        RegistryImpl rimpl = (RegistryImpl)componentRegistry();
        rimpl.clear();
    }
    
    // Manipulating methods (overriding the superclass dummy ones) <<

    /** Helper only. */
    private ModeImpl getMode(TopComponent tc) {
        return (ModeImpl)findMode(tc);
    }

    // #37561
    /** Helper only */
    private ModeImpl getModeForOpenedTopComponent(TopComponent tc) {
        if(tc == null) {
            // Log something?
            return null;
        }
        
        for(ModeImpl mode: getModes()) {
            if(mode.getOpenedTopComponents().contains(tc)) {
                return mode;
            }
        }
        
        return null;
    }
    
    /**
     * @return The mode where the given TopComponent had been before it was moved to sliding or separate mode.
     */
    public ModeImpl getPreviousModeForTopComponent(String tcID, ModeImpl slidingMode) {
        return getCentral().getModeTopComponentPreviousMode(tcID, slidingMode);
    }
    
    /**
     * @return The position (tab index) of the given TopComponent before it was moved to sliding or separate mode.
     */
    public int getPreviousIndexForTopComponent(String tcID, ModeImpl slidingMode) {
        return getCentral().getModeTopComponentPreviousIndex(tcID, slidingMode);
    
    }
    
    /**
     * Remember the mode and position where the given TopComponent was before moving into sliding or separate mode.
     * 
     * @param tcID TopComponent's id
     * @param currentSlidingMode The mode where the TopComponent is at the moment.
     * @param prevMode The mode where the TopComponent had been before it was moved to the sliding mode.
     * @param prevIndex Tab index of the TopComponent before it was moved to the new mode.
     */
    public void setPreviousModeForTopComponent(String tcID, ModeImpl slidingMode, ModeImpl prevMode, int prevIndex) {
        getCentral().setModeTopComponentPreviousMode(tcID, slidingMode, prevMode, prevIndex);
    }
    
    /**
     * Set the state of the TopComponent when the editor is maximized.
     * 
     * @param tcID TopComponent id
     * @param docked True if the TopComponent should stay docked in maximized editor mode,
     * false if it should slide out when the editor is maximized.
     */
    public void setTopComponentDockedInMaximizedMode( String tcID, boolean docked ) {
        getCentral().setTopComponentDockedInMaximizedMode( tcID, docked );
    }
    
    /**
     * Get the state of the TopComponent when the editor is maximized.
     * 
     * @param tcID TopComponent id.
     * @return True if the TopComponent should stay docked in maximized editor mode,
     * false if it should slide out when the editor is maximized.
     */
    public boolean isTopComponentDockedInMaximizedMode( String tcID ) {
        return getCentral().isTopComponentDockedInMaximizedMode( tcID );
    }
    
    /**
     * Set the state of the TopComponent when no mode is maximized.
     * 
     * @param tcID TopComponent id
     * @param slided True if the TopComponent is slided in the default mode,
     * false if it is docked.
     */
    public void setTopComponentSlidedInDefaultMode( String tcID, boolean slided ) {
        getCentral().setTopComponentSlidedInDefaultMode( tcID, slided );
    }
    
    /**
     * Get the state of the TopComponent when no mode is maximized.
     * 
     * @param tcID TopComponent id.
     * @return True if the TopComponent is slided in the default mode,
     * false if it is docked.
     */
    public boolean isTopComponentSlidedInDefaultMode( String tcID ) {
        return getCentral().isTopComponentSlidedInDefaultMode( tcID );
    }
    
    /**
     * Get the state of the TopComponent when it is slided-in.
     * 
     * @param tcID TopComponent id. 
     * @return true if the TopComponent is maximized when slided-in.
     */
    public boolean isTopComponentMaximizedWhenSlidedIn( String tcID ) {
        return getCentral().isTopComponentMaximizedWhenSlidedIn( tcID );
    }
    
    /**
     * Set the state of the TopComponent when it is slided-in.
     * 
     * @param tcID TopComponent id. 
     * @param maximized true if the TopComponent is maximized when slided-in.
     */
    public void setTopComponentMaximizedWhenSlidedIn( String tcID, boolean maximized ) {
        getCentral().setTopComponentMaximizedWhenSlidedIn( tcID, maximized );
    }
    
    public void userToggledTopComponentSlideInMaximize( String tcID ) {
        getCentral().userToggledTopComponentSlideInMaximize( tcID );
    }

    /** Finds out if given Window is used as separate floating window or not.
     *
     * @return true if Window is separate floating window, false if window
     * is used for other purposes such as independent dialog/window, main window etc. 
     */
    public static boolean isSeparateWindow (Window w) {
        // work only in Swing environment
        if (!(w instanceof RootPaneContainer)) {
            return false;
        }
        // #85089 - getRootPane may return null in some edge situations
        JRootPane rp = ((RootPaneContainer) w).getRootPane();
        if (rp == null) {
            return false;
        }
        return rp.getClientProperty(Constants.SEPARATE_WINDOW_PROPERTY) != null;
    }

    private static final String ASSERTION_ERROR_MESSAGE = "Window System API is required to be called from AWT thread only, see " // NOI18N
        + "http://core.netbeans.org/proposals/threading/"; // NOI18N
    
    static void assertEventDispatchThread() {
        assert SwingUtilities.isEventDispatchThread() : ASSERTION_ERROR_MESSAGE;
    }

    static {
        assertsEnabled = false;
        assert assertsEnabled = true;  // Intentional side-effect!!!
    }

    /** Weaker form of assertion to control if client's code is calling
     * winsys from ED thread. Level of logged exception is lower if
     * assertions are disabled (for releases etc).
     */
    static void warnIfNotInEDT () {
        Level level = assertsEnabled ? Level.WARNING : Level.FINE;
        if(!SwingUtilities.isEventDispatchThread()) {
            // tries to catch known JDK problem, SwingUtilities.isEventDispatchThread()
            // returns false even if it *is* in ED thread.
            // if we find "java.awt.EventDispatchThread" stack line, it's probable
            // that we hit this JDK problem (see links below)
            boolean isJDKProblem = false;
            StackTraceElement[] elems = Thread.currentThread().getStackTrace();
            for (StackTraceElement elem : elems) {
                if ("java.awt.EventDispatchThread".equals(elem.getClassName())) {
                    isJDKProblem = true;
                    break;
                }
            }

            if (!isJDKProblem) {
                // problem somewhere in NetBeans modules' code
                Logger.getLogger(WindowManagerImpl.class.getName()).log(level, null,
                        new java.lang.IllegalStateException(
                        "Problem in some module which uses Window System: "
                        + ASSERTION_ERROR_MESSAGE));
            } else {
                // probably known problem in JDK
                Logger.getLogger(WindowManagerImpl.class.getName()).log(level, null,
                        new java.lang.IllegalStateException(
                        "Known problem in JDK occurred. If you are interested, vote and report at:\n" +
                        "http://bugs.sun.com/view_bug.do?bug_id=6424157, http://bugs.sun.com/view_bug.do?bug_id=6553239 \n" +
                        "Also see related discussion at http://www.netbeans.org/issues/show_bug.cgi?id=90590"));
            }
        }
    }

    /**
     * @return An array of TopComponents that are opened in editor modes (i.e. editor windows).
     */
    public TopComponent[] getEditorTopComponents() {
        ArrayList<TopComponent> editors = new ArrayList<TopComponent>();
        Set<? extends Mode> modes = getModes();
        for(Mode mode: modes) {
            ModeImpl modeImpl = findModeImpl( mode.getName() ); // XXX probably useless
            if( modeImpl.getKind() == Constants.MODE_KIND_EDITOR ) {
                editors.addAll( modeImpl.getOpenedTopComponents() );
            }
        }
        return editors.toArray( new TopComponent[editors.size()] );
    }

    /**
     * @return TopComponent that is selected in an arbitrary editor-type mode
     * or null if editor mode(s) is empty.
     */
    public TopComponent getArbitrarySelectedEditorTopComponent() {
        Set modes = getModes();
        for( Iterator i=modes.iterator(); i.hasNext(); ) {
            Mode mode = (Mode)i.next();
            ModeImpl modeImpl = findModeImpl( mode.getName() );
            if( modeImpl.getKind() == Constants.MODE_KIND_EDITOR ) {
                return mode.getSelectedTopComponent();
            }
        }
        return null;
    }

    /**
     * Send componentHidden() event to all selected TopComponents in editor modes.
     */
    public void deselectEditorTopComponents() {
        for(ModeImpl modeImpl: getModes()) {
            if( modeImpl.getKind() == Constants.MODE_KIND_EDITOR ) {
                //not a pretty hack - add an empty TopComponent into the mode
                //and make it the selected one so that componentHidden() gets called
                //on the previously selected TopComponent
                TopComponent dummy = new DummyTopComponent();
                modeImpl.addOpenedTopComponent( dummy );
                modeImpl.setSelectedTopComponent( dummy );
            }
        }
    }

    /**
     * Calls close() on all TopComponents that are opened in noneditor-type modes.
     *
     */
    public void closeNonEditorViews() {
        for(ModeImpl modeImpl: getModes()) {
            if( null != modeImpl && modeImpl.getKind() != Constants.MODE_KIND_EDITOR ) {
                java.util.List tcs = modeImpl.getOpenedTopComponents();
                for( Iterator j=tcs.iterator(); j.hasNext(); ) {
                    TopComponent tc = (TopComponent)j.next();
                    tc.close();
                }
            }
        }
    }

    @Override
    public TopComponent[] getOpenedTopComponents(Mode mode) {
        if( mode instanceof ModeImpl ) {
            java.util.List<TopComponent> openedTcs = ((ModeImpl)mode).getOpenedTopComponents();
            return openedTcs.toArray(new TopComponent[openedTcs.size()]);
        }
        return super.getOpenedTopComponents(mode);
    }

    public boolean isHeavyWeightShowing() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set<TopComponent> opened = new HashSet<TopComponent>(registry.getOpened());
        for( TopComponent tc : opened ) {
            if( !tc.isShowing() )
                continue;
            if( isHeavyWeight( tc ) )
                return true;
        }
        return false;
    }

    private boolean isHeavyWeight( java.awt.Component c ) {
        if( null != c && !c.isLightweight() )
            return true;
        if( c instanceof Container ) {
            for( java.awt.Component child : ((Container)c).getComponents() ) {
                if( isHeavyWeight(child) )
                    return true;
            }
        }
        return false;
    }
    
    /**
     * An empty TopComponent needed for deselectEditorTopComponents()
     */
    private static class DummyTopComponent extends TopComponent {
        protected String preferredID() {
            return "temp";
        }

        public int getPersistenceType() {
            return PERSISTENCE_NEVER;
        }
    }
}

