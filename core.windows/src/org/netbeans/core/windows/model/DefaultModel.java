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


package org.netbeans.core.windows.model;



import java.awt.Frame;
import java.awt.Rectangle;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.TopComponentGroupImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.WindowSystemSnapshot;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultModel implements Model {

    /** ModeImpl to ModeModel. */
    private final Map<ModeImpl, ModeModel> mode2model = 
            new WeakHashMap<ModeImpl, ModeModel>(10);
    /** TopComponentGroup to TopComponentGroupModel. */
    private final Map<TopComponentGroupImpl, TopComponentGroupModel> group2model = 
            new WeakHashMap<TopComponentGroupImpl, TopComponentGroupModel>(10);
    
    /** Whether the current winsys is visible on the screen.
     * 'The most important' property of all winsys. */
    private boolean visible = false;

    /** Bounds of main window when the editor area state is joined (tiled). */
    private final Rectangle mainWindowBoundsJoined = new Rectangle();
    /** Bounds of main window when the editor area state is separated. */
    private final Rectangle mainWindowBoundsSeparated = new Rectangle();
    /** Keeps initial value of separated bounds (helper non-serializable variable). */
    private final Rectangle mainWindowBoundsSeparatedHelp = new Rectangle();

    /** */
    private int mainWindowFrameStateJoined = Frame.NORMAL;
    /** */
    private int mainWindowFrameStateSeparated = Frame.NORMAL;
    
    /** State of editor area. 1 = joined, 2 = separated */
    private int editorAreaState = Constants.EDITOR_AREA_JOINED;
    /** Bounds of editor area. */
    private final Rectangle editorAreaBounds = new Rectangle();
    /** Keeps initial value of editor area (helper non-serializable variable). */
    private final Rectangle editorAreaBoundsHelp = new Rectangle();
    /** */
    private int editorAreaFrameState = Frame.NORMAL;
    /** Name of toolbars configuration. */
    private String toolbarConfigName = "Standard"; // NOI18N
    /** The docking status (slided-out/docked) for TopComponents in maximized editor mode */
    private DockingStatus maximizedDockingStatus = new DockingStatus( this );
    /** The docking status (slided-out/docked) for TopComponents in the default mode (nothing is maximized)*/
    private DockingStatus defaultDockingStatus = new DefaultDockingStatus( this );
    /** TopComponents that are maximized when slided-in. */
    private Set<String> slideInMaximizedTopComponents = new HashSet<String>( 3 );
    
    /** Modes structure. */
    private ModesSubModel modesSubModel = new ModesSubModel(this);

    /** Set of TopComponentGroup's. */
    private final Set<TopComponentGroupImpl> topComponentGroups = 
            new HashSet<TopComponentGroupImpl>(5);
    
    // Locks.
    /** Lock for visible property. */
    private final Object LOCK_VISIBLE = new Object();
    /** Lock for mainWindowBounds property -> joined(tiled) state. */
    private final Object LOCK_MAIN_WINDOW_BOUNDS_JOINED = new Object();
    /** Lock for mainWindowBounds property -> separated state. */
    private final Object LOCK_MAIN_WINDOW_BOUNDS_SEPARATED = new Object();
    /** */
    private final Object LOCK_MAIN_WINDOW_BOUNDS_SEPARATED_HELP = new Object();
    /** */
    private final Object LOCK_MAIN_WINDOW_FRAME_STATE_JOINED = new Object();
    /** */
    private final Object LOCK_MAIN_WINDOW_FRAME_STATE_SEPARATED = new Object();
    /** Lock for editor area state. */
    private final Object LOCK_EDITOR_AREA_STATE = new Object();
    /** */
    private final Object LOCK_EDITOR_AREA_FRAME_STATE = new Object();
    /** Lock for editor area bounds. */
    private final Object LOCK_EDITOR_AREA_BOUNDS = new Object();
    /** */
    private final Object LOCK_EDITOR_AREA_BOUNDS_HELP = new Object();
    /** Lock for toolbarConfigName property. */
    private final Object LOCK_TOOLBAR_CONFIG = new Object();
    /** Locks for modes sub model. */
    private final Object LOCK_MODES = new Object();
    /** Lock for topComponentGroups property. */
    private final Object LOCK_TOPCOMPONENT_GROUPS = new Object();
    
    private final Object LOCK_PROJECT_NAME = new Object();

    
    public DefaultModel() {
    }
    

    /////////////////////////////////////
    // Mutator methods >>
    /////////////////////////////////////
    /** Sets visibility status. */
    public void setVisible(boolean visible) {
        synchronized(LOCK_VISIBLE) {
            this.visible = visible;
        }
    }
    
    /** Setter for mainWindowBoundsJoined property. */
    public void setMainWindowBoundsJoined(Rectangle mainWindowBoundsJoined) {
        if(mainWindowBoundsJoined == null) {
            return;
        }
        
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_JOINED) {
            this.mainWindowBoundsJoined.setBounds(mainWindowBoundsJoined);
        }
    }
    
    /** Setter for mainWindowBoundsSeparated property. */
    public void setMainWindowBoundsSeparated(Rectangle mainWindowBoundsSeparated) {
        if(mainWindowBoundsSeparated == null) {
            return;
        }
        
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED) {
            this.mainWindowBoundsSeparated.setBounds(mainWindowBoundsSeparated);
        }
    }
    
    /** Sets frame state of main window when editor area is in tiled(joined) state. */
    public void setMainWindowFrameStateJoined(int frameState) {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_JOINED) {
            this.mainWindowFrameStateJoined = frameState;
        }
    }
    
    /** Sets frame state of main window when editor area is in separated state. */
    public void setMainWindowFrameStateSeparated(int frameState) {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_SEPARATED) {
            this.mainWindowFrameStateSeparated = frameState;
        }
    }

    /** Setter of editorAreaBounds property. */
    public void setEditorAreaBounds(Rectangle editorAreaBounds) {
        if(editorAreaBounds == null) {
            return;
        }
        
        synchronized(LOCK_EDITOR_AREA_BOUNDS) {
            this.editorAreaBounds.setBounds(editorAreaBounds);
        }
    }
 
    /** Setter of editorAreaState property. */
    public void setEditorAreaState(int editorAreaState) {
        synchronized(LOCK_EDITOR_AREA_STATE) {
            this.editorAreaState = editorAreaState;
        }
    }

    /** */
    public void setEditorAreaFrameState(int frameState) {
        synchronized(LOCK_EDITOR_AREA_FRAME_STATE) {
            this.editorAreaFrameState = frameState;
        }
    }
    
    /** Sets editor area constraints. */
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints) {
        synchronized(LOCK_MODES) {
            modesSubModel.setEditorAreaConstraints(editorAreaConstraints);
        }
    }

    public void setModeConstraints(ModeImpl mode, SplitConstraint[] constraints) {
        synchronized(LOCK_MODES) {
            // PENDING create changeMode method?
            modesSubModel.removeMode(mode);
            modesSubModel.addMode(mode, constraints);
        }
    }
    
    
    /** Adds mode. */ 
    public void addMode(ModeImpl mode, SplitConstraint[] constraints) {
        synchronized(LOCK_MODES) {
            modesSubModel.addMode(mode, constraints);
        }
    }



    // XXX
    public void addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeToSide(mode, attachMode, side);
        }
    }
    
    // XXX
    public void addModeAround(ModeImpl mode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeAround(mode, side);
        }
    }
    
    // XXX
    public void addModeAroundEditor(ModeImpl mode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeAroundEditor(mode, side);
        }
    }

    public void addSlidingMode(ModeImpl mode, String side, Map<String,Integer> slideInSizes) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeSliding(mode, side, slideInSizes);
        }
    }
    
    
    /** Removes mode. */
    public void removeMode(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            modesSubModel.removeMode(mode);
        }
    }

    /** Sets active mode. */
    private Reference<ModeImpl> lastActiveMode = null;
    public void setActiveMode(ModeImpl activeMode) {
        if (lastActiveMode != null && lastActiveMode.get() == activeMode) {
            return;
        } else {
            lastActiveMode = new WeakReference<ModeImpl>(activeMode);
        }
        synchronized(LOCK_MODES) {
            boolean success = modesSubModel.setActiveMode(activeMode);
            if (success) {
                updateSlidingSelections(activeMode);
            }
        }
    }

    /** Sets editor mode that is currenlty maximized */
    public void setEditorMaximizedMode(ModeImpl maximizedMode) {
        assert null == maximizedMode || maximizedMode.getKind() == Constants.MODE_KIND_EDITOR;
        synchronized(LOCK_MODES) {
            modesSubModel.setEditorMaximizedMode(maximizedMode);
        }
    }
    
    /** Sets view mode that is currenlty maximized */
    public void setViewMaximizedMode(ModeImpl maximizedMode) {
        assert null == maximizedMode || maximizedMode.getKind() == Constants.MODE_KIND_VIEW;
        synchronized(LOCK_MODES) {
            modesSubModel.setViewMaximizedMode(maximizedMode);
        }
    }

    /** Setter for toolbarConfigName property. */
    public void setToolbarConfigName(String toolbarConfigName) {
        synchronized(LOCK_TOOLBAR_CONFIG) {
            this.toolbarConfigName = toolbarConfigName;
        }
    }

    
    public void addTopComponentGroup(TopComponentGroupImpl tcGroup) {
        synchronized(LOCK_TOPCOMPONENT_GROUPS) {
            topComponentGroups.add(tcGroup);
        }
    }
    
    public void removeTopComponentGroup(TopComponentGroupImpl tcGroup) {
        synchronized(LOCK_TOPCOMPONENT_GROUPS) {
            topComponentGroups.remove(tcGroup);
        }
    }
    
    public void reset() {
        mode2model.clear();
        group2model.clear();
        mainWindowFrameStateJoined = Frame.NORMAL;
        mainWindowFrameStateSeparated = Frame.NORMAL;
        editorAreaState = Constants.EDITOR_AREA_JOINED;
        editorAreaFrameState = Frame.NORMAL;
        toolbarConfigName = "Standard"; // NOI18N
        modesSubModel = new ModesSubModel(this);
        topComponentGroups.clear();
        maximizedDockingStatus.clear();
        defaultDockingStatus.clear();
        slideInMaximizedTopComponents.clear();
    }
    
    /////////////////////////////////////
    // Mutator methods <<
    /////////////////////////////////////

    /////////////////////////////////////
    // Accessor methods >>
    /////////////////////////////////////
    /** Gets visibility status. */
    public boolean isVisible() {
        synchronized(LOCK_VISIBLE) {
            return this.visible;
        }
    }

    /** Getter for mainWindowBoundsJoined property. */
    public Rectangle getMainWindowBoundsJoined() {
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_JOINED) {
            return (Rectangle)mainWindowBoundsJoined.clone();
        }
    }
    
    /** Getter for mainWindowBoundsSeparated property. */
    public Rectangle getMainWindowBoundsSeparated() {
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED) {
            return (Rectangle)mainWindowBoundsSeparated.clone();
        }
    }
    
    public Rectangle getMainWindowBoundsSeparatedHelp() {
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED_HELP) {
            return (Rectangle)mainWindowBoundsSeparatedHelp.clone();
        }
    }
    
    /** Gets frame state of main window when editor area is in tiled(joined) state. */
    public int getMainWindowFrameStateJoined() {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_JOINED) {
            return mainWindowFrameStateJoined;
        }
    }
    
    /** Gets frame state of main window when editor area is in separated state. */
    public int getMainWindowFrameStateSeparated() {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_SEPARATED) {
            return mainWindowFrameStateSeparated;
        }
    }
    
    /** Getter of editorAreaState property. */
    public int getEditorAreaState() {
        synchronized(LOCK_EDITOR_AREA_STATE) {
            return this.editorAreaState;
        }
    }
    
    /** */
    public int getEditorAreaFrameState() {
        synchronized(LOCK_EDITOR_AREA_FRAME_STATE) {
            return this.editorAreaFrameState;
        }
    }
    
    /** Getter of editorAreaBounds property. */
    public Rectangle getEditorAreaBounds() {
        synchronized(LOCK_EDITOR_AREA_BOUNDS) {
            return (Rectangle)this.editorAreaBounds.clone();
        }
    }
    
    public Rectangle getEditorAreaBoundsHelp() {
        synchronized(LOCK_EDITOR_AREA_BOUNDS_HELP) {
            return (Rectangle)this.editorAreaBoundsHelp.clone();
        }
    }

    /** Gets editor area constraints. */
    public SplitConstraint[] getEditorAreaConstraints() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getEditorAreaConstraints();
        }
    }

    /** Gets set of modes. */
    public Set<ModeImpl> getModes() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getModes();
        }
    }
    
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getModeConstraints(mode);
        }
    }
    
    public SplitConstraint[] getModelElementConstraints(ModelElement element) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getModelElementConstraints(element);
        }
    }
    
    public String getSlidingModeConstraints(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getSlidingModeConstraints(mode);
        }
    }
    
    public ModeImpl getSlidingMode(String side) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getSlidingMode(side);
        }
    }
    
    /** Gets active mode. */
    public ModeImpl getActiveMode() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getActiveMode();
        }
    }

    /** Gets last active editor mode. */
    public ModeImpl getLastActiveEditorMode() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getLastActiveEditorMode();
        }
    }

    /**
     * @return The docking status (docked/slided) of TopComponents before the window system
     * switched to maximized mode.
     */
    public DockingStatus getDefaultDockingStatus() {
        return defaultDockingStatus;
    }

    /**
     * @return The docking status (docked/slided) of TopComponents in maximized editor mode.
     */
    public DockingStatus getMaximizedDockingStatus() {
        return maximizedDockingStatus;
    }
    
    /** Gets editor maximized mode. */
    public ModeImpl getEditorMaximizedMode() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getEditorMaximizedMode();
        }
    }
    
    /** Gets view maximized mode. */
    public ModeImpl getViewMaximizedMode() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getViewMaximizedMode();
        }
    }
    
    /**
     * Find the side (LEFT/RIGHT/BOTTOM) where the TopComponent from the given
     * mode should slide to.
     * 
     * @param mode Mode
     * @return The slide side for TopComponents from the given mode.
     */
    public String getSlideSideForMode( ModeImpl mode ) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getSlideSideForMode( mode );
        }
    }
    
    /** Getter for toolbarConfigName property. */
    public String getToolbarConfigName() {
        synchronized(LOCK_TOOLBAR_CONFIG) {
            return this.toolbarConfigName;
        }
    }
    
    /** 
     * Gets the sizes (width or height) of TopComponents in the given sliding 
     * side, the key in the Map is TopComponent's ID 
     */
    public Map<String,Integer> getSlideInSizes(String side) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getSlideInSizes( side );
        }
    }
    
    /** Set the size (width or height of the given TopComponent when it is slided in */
    public void setSlideInSize(String side, TopComponent tc, int size) {
        synchronized(LOCK_MODES) {
            modesSubModel.setSlideInSize(side, tc, size);
        }
    }
    
    /**
     * @return True if the given TopComponent is maximized when it is slided-in.
     */
    public boolean isTopComponentMaximizedWhenSlidedIn( String tcid ) {
        return null != tcid && slideInMaximizedTopComponents.contains( tcid );
    }
    
    /**
     * Set whether the given TopComponent is maximized when it is slided-in.
     */
    public void setTopComponentMaximizedWhenSlidedIn( String tcid, boolean maximized ) {
        if( null != tcid ) {
            if( maximized )
                slideInMaximizedTopComponents.add( tcid );
            else
                slideInMaximizedTopComponents.remove( tcid );
        }
    }

    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    
    ///////////////////
    // Mode specific >>
    public void createModeModel(ModeImpl mode, String name, int state, int kind, boolean permanent) {
        synchronized(mode2model) {
            ModeModel mm;
            mm = new DefaultModeModel(name, state, kind, permanent);
            mode2model.put(mode, mm);
        }
    }

    private ModeModel getModelForMode(ModeImpl mode) {
        synchronized(mode2model) {
            return (ModeModel)mode2model.get(mode);
        }
    }
    
    // Mutators
    /** Sets state. */
    public void setModeState(ModeImpl mode, int state) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setState(state);
        }
    }
    
    /** Sets bounds. */
    public void setModeBounds(ModeImpl mode, Rectangle bounds) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setBounds(bounds);
        }
    }
    
    /** Sets frame state. */
    public void setModeFrameState(ModeImpl mode, int frameState) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setFrameState(frameState);
        }
    } 
    
    /** Sets seleted TopComponent. */
    public void setModeSelectedTopComponent(ModeImpl mode, TopComponent selected) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setSelectedTopComponent(selected);
        }
    }
    
    /** Remember which top component was the selected one before switching to/from maximized mode */
    public void setModePreviousSelectedTopComponentID(ModeImpl mode, String prevSelectedId) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setPreviousSelectedTopComponentID(prevSelectedId);
        }
    }
    
    /** Adds opened TopComponent. */
    public void addModeOpenedTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.addOpenedTopComponent(tc);
        }
    }

    /** Inserts opened TopComponent. */
    public void insertModeOpenedTopComponent(ModeImpl mode, TopComponent tc, int index) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.insertOpenedTopComponent(tc, index);
        }
    }
    
    /** Adds closed TopComponent. */
    public void addModeClosedTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.addClosedTopComponent(tc);
        }
    }
    
    // XXX
    public void addModeUnloadedTopComponent(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.addUnloadedTopComponent(tcID);
        }
    }
    
    // XXX
    public void setModeUnloadedSelectedTopComponent(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setUnloadedSelectedTopComponent(tcID);
        }
    }
    
    public void setModeUnloadedPreviousSelectedTopComponent(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setUnloadedPreviousSelectedTopComponent(tcID);
        }
    }
    
    /** */
    public void removeModeTopComponent(ModeImpl mode, TopComponent tc, TopComponent recentTc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.removeTopComponent(tc, recentTc);
        }
    }
    
    // XXX
    public void removeModeClosedTopComponentID(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.removeClosedTopComponentID(tcID);
        }
    }

    /**
     * @param mode - sliding mode
     */
    
    public void setModeTopComponentPreviousConstraints(ModeImpl mode, String tcID, SplitConstraint[] constraints) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setTopComponentPreviousConstraints(tcID, constraints);
        }
    }
    
    /**
     * @param mode - sliding mode
     * @param previousMode - the original mode.
     * @param prevIndex - the tab index in the original mode
     */
    public void setModeTopComponentPreviousMode(ModeImpl mode, String tcID, ModeImpl previousMode, int prevIndex) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setTopComponentPreviousMode(tcID, previousMode, prevIndex);
        }
    }
    
    // Accessors
    /** Gets programatic name of mode. */
    public String getModeName(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getName();
        } else {
            return null;
        }
    }
    
    /** Gets bounds. */
    public Rectangle getModeBounds(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getBounds();
        } else {
            return null;
        }
    }
    
    public Rectangle getModeBoundsSeparatedHelp(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getBoundsSeparatedHelp();
        } else {
            return null;
        }
    }
    
    /** Gets state. */
    public int getModeState(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getState();
        } else {
            return -1;
        }
    }
    
    /** Gets kind. */
    public int getModeKind(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getKind();
        } else {
            return -1;
        }
    }

    /** Gets side. */
    public String getModeSide(ModeImpl mode) {
        String side = modesSubModel.getSlidingModeConstraints(mode);
        return side;
    }
    
    /** Gets frame state. */
    public int getModeFrameState(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getFrameState();
        } else {
            return -1;
        }
    }
    
    /** Gets used defined. */
    public boolean isModePermanent(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            boolean result = modeModel.isPermanent();
            if (!result) {
                for (TopComponent tc : mode.getTopComponents()) {
                    result |= tc.getClass().getAnnotation(RetainLocation.class)
                            != null;
                    if (result) {
                        break;
                    }
                }
            }
            return result;
        } else {
            return false;
        }
    }
    
    /** Indicates whether the mode is empty. */
    public boolean isModeEmpty(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.isEmpty();
        } else {
            return false;
        }
    }
    
    /** Indicates whether the mode contains the TopComponent. */
    public boolean containsModeTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.containsTopComponent(tc);
        } else {
            return false;
        }
    }
    
    /** Gets selected TopComponent. */
    public TopComponent getModeSelectedTopComponent(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getSelectedTopComponent();
        } else {
            return null;
        }
    }
    
    /** Get the ID of top component that had been the selected one before switching to/from maximzied mode */
    public String getModePreviousSelectedTopComponentID(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getPreviousSelectedTopComponentID();
        } else {
            return null;
        }
    }
    
    /** Gets list of top components. */
    public List<TopComponent> getModeTopComponents(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getTopComponents();
        } else {
            return Collections.emptyList();
        }
    }
    
    /** Gets list of top components. */
    public List<TopComponent> getModeOpenedTopComponents(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getOpenedTopComponents();
        } else {
            return Collections.emptyList();
        }
    }
    
    // XXX
    public List<String> getModeOpenedTopComponentsIDs(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getOpenedTopComponentsIDs();
        } else {
            return Collections.emptyList();
        }
    }
    
    public int getModeOpenedTopComponentTabPosition(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getOpenedTopComponentTabPosition(tc);
        } else {
            return -1;
        }
    }
    
    public List<String> getModeClosedTopComponentsIDs(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getClosedTopComponentsIDs();
        } else {
            return Collections.emptyList();
        }
    }
    
    public List<String> getModeTopComponentsIDs(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getTopComponentsIDs();
        } else {
            return Collections.emptyList();
        }
    }
    
    public SplitConstraint[] getModeTopComponentPreviousConstraints(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        return modeModel == null ? null : modeModel.getTopComponentPreviousConstraints(tcID);
    }
    
    public ModeImpl getModeTopComponentPreviousMode(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        return modeModel == null ? null : modeModel.getTopComponentPreviousMode(tcID);
    }
    
    /** Gets the tab index of the given top component before it was moved to sliding/separate mode */
    public int getModeTopComponentPreviousIndex(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        return modeModel == null ? null : modeModel.getTopComponentPreviousIndex(tcID);
    }
    
    // End of mode specific.

    
    ////////////////////////////////////
    // TopComponentGroup specific >>
    public void createGroupModel(TopComponentGroupImpl tcGroup, String name, boolean opened) {
        synchronized(group2model) {
            TopComponentGroupModel tcgm = new DefaultTopComponentGroupModel(name, opened);
            group2model.put(tcGroup, tcgm);
        }
    }

    private TopComponentGroupModel getModelForGroup(TopComponentGroupImpl tcGroup) {
        synchronized(group2model) {
            return (TopComponentGroupModel)group2model.get(tcGroup);
        }
    }

    
    public Set<TopComponentGroupImpl> getTopComponentGroups() {
        synchronized(LOCK_TOPCOMPONENT_GROUPS) {
            return new HashSet<TopComponentGroupImpl>(topComponentGroups);
        }
    }

    /** Gets programatic name of mode. */
    public String getGroupName(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getName();
        } else {
            return null;
        }
    }
    
    public void openGroup(TopComponentGroupImpl tcGroup, 
            Collection<TopComponent> openedTopComponents, 
            Collection<TopComponent> openedBeforeTopComponents) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            groupModel.open(openedTopComponents, openedBeforeTopComponents);
        }
    }
    
    public void closeGroup(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            groupModel.close();
        } 
    }
    
    public boolean isGroupOpened(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.isOpened();
        } else {
            return false;
        }
    }
    
    public Set<TopComponent> getGroupTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getTopComponents();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<TopComponent> getGroupOpenedTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpenedTopComponents();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<TopComponent> getGroupOpenedBeforeTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpenedBeforeTopComponents();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<TopComponent> getGroupOpeningTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpeningTopComponents();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<TopComponent> getGroupClosingTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getClosingTopComponents();
        } else {
            return Collections.emptySet();
        }
    }

    public boolean addGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean removeGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeUnloadedTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean addGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addOpeningTopComponent(tc);
        } else {
            return false;
        }
    }
    
    public boolean removeGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeOpeningTopComponent(tc);
        } else {
            return false;
        }
    }
    
    public boolean addGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedOpeningTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean removeGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeUnloadedOpeningTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean addGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedClosingTopComponent(tcID);
        } else {
            return false;
        }
    }
    public boolean removeGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeUnloadedClosingTopComponent(tcID);
        } else {
            return false;
        }
    }
    // XXX
    public boolean addGroupUnloadedOpenedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedOpenedTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    // XXX>>
    public Set<String> getGroupTopComponentsIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getTopComponentsIDs();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<String> getGroupOpeningSetIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpeningSetIDs();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<String> getGroupClosingSetIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getClosingSetIDs();
        } else {
            return Collections.emptySet();
        }
    }
    
    public Set<String> getGroupOpenedTopComponentsIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpenedTopComponentsIDs();
        } else {
            return Collections.emptySet();
        }
    }
    // XXX<<
    // TopComponentGroup specific <<
    ////////////////////////////////////
    
    
    /////////////////////////
    // Controller updates >>
    public void setActiveModeForOriginator(ModelElement originator) {
        synchronized(LOCK_MODES) {
            ModeImpl mode = modesSubModel.getModeForOriginator(originator);
            setActiveMode(mode);
        }
    }
    
    public void setModeSelectedTopComponentForOriginator(ModelElement originator, TopComponent tc) {
        ModeImpl mode;
        synchronized(LOCK_MODES) {
            mode = modesSubModel.getModeForOriginator(originator);
        }
        
        setModeSelectedTopComponent(mode, tc);


    }
    
    public void setMainWindowBoundsUserSeparatedHelp(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED_HELP) {
            this.mainWindowBoundsSeparatedHelp.setBounds(bounds);
        }
    }
    
    public void setEditorAreaBoundsUserHelp(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_EDITOR_AREA_BOUNDS_HELP) {
            this.editorAreaBoundsHelp.setBounds(bounds);
        }
    }
    
    public void setModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setBoundsSeparatedHelp(bounds);
        }
    }
    
    public void setSplitWeights( ModelElement[] snapshots, double[] splitWeights ) {
        synchronized(LOCK_MODES) {
            modesSubModel.setSplitWeights(snapshots, splitWeights);
        }
    }
    
    // Controller updates <<
    /////////////////////////


    public WindowSystemSnapshot createWindowSystemSnapshot() {
        WindowSystemSnapshot wsms = new WindowSystemSnapshot();

        // PENDING
        ModeStructureSnapshot mss = createModeStructureSnapshot();
        wsms.setModeStructureSnapshot(mss);
        
        ModeImpl activeMode = getActiveMode();
        wsms.setActiveModeSnapshot(activeMode == null ? null : mss.findModeSnapshot(activeMode.getName()));
        
        ModeImpl maximizedMode = null != getViewMaximizedMode() ? getViewMaximizedMode() : null;
        wsms.setMaximizedModeSnapshot(maximizedMode == null ? null : mss.findModeSnapshot(maximizedMode.getName()));

        wsms.setMainWindowBoundsJoined(getMainWindowBoundsJoined());
        wsms.setMainWindowBoundsSeparated(getMainWindowBoundsSeparated());
        wsms.setEditorAreaBounds(getEditorAreaBounds());
        wsms.setEditorAreaState(getEditorAreaState());
        wsms.setEditorAreaFrameState(getEditorAreaFrameState());
        wsms.setMainWindowFrameStateJoined(getMainWindowFrameStateJoined());
        wsms.setMainWindowFrameStateSeparated(getMainWindowFrameStateSeparated());
        wsms.setToolbarConfigurationName(getToolbarConfigName());
        return wsms;
    }

    /** Creates modes snapshot.. */
    private ModeStructureSnapshot createModeStructureSnapshot() {
        ModeStructureSnapshot.ElementSnapshot splitRoot;
        Set<ModeStructureSnapshot.ModeSnapshot> separateModes;
        Set<ModeStructureSnapshot.SlidingModeSnapshot> slidingModes;
        synchronized(LOCK_MODES) {
            splitRoot = modesSubModel.createSplitSnapshot();
            separateModes = modesSubModel.createSeparateModeSnapshots();
            slidingModes = modesSubModel.createSlidingModeSnapshots();
        }
        
        ModeStructureSnapshot ms =  new ModeStructureSnapshot(splitRoot, separateModes, slidingModes);
        return ms;
    }
    ///////////////////////////////////////////////////
    
    /** Checks whether the mode isn't null.  */
    private static boolean validateAddingMode(ModeImpl mode) {
        if(mode == null) {
            Logger.getLogger(DefaultModel.class.getName()).log(Level.WARNING, null,
                              new java.lang.NullPointerException("Not allowed null mode")); // NOI18N
            return false;
        }
        
        return true;
    }
    
    /** Keeps selected components of sliding modes in sync with given current
     * active mode. Sliding mode can have non-null selection (=slide) only if
     * it is active mode as well
     */   
    private void updateSlidingSelections (ModeImpl curActive) {
        Set slidingModes = modesSubModel.getSlidingModes();
        ModeImpl curSliding = null;
        for (Iterator iter = slidingModes.iterator(); iter.hasNext(); ) {
            curSliding = (ModeImpl)iter.next();
            if (!curSliding.equals(curActive)) {
                setModeSelectedTopComponent(curSliding, null);
            }
        }
    }

    /**
     * A special subclass of DockingStatus for default mode when no TopComponent is maximized.
     */
    private static class DefaultDockingStatus extends DockingStatus {
        public DefaultDockingStatus( Model model ) {
            super( model );
        }
        
        /**
         * When switching back to default mode, only slide those TopComponents 
         * there were slided-out before.
         */
        public boolean shouldSlide( String tcID ) {
            return null != tcID && slided.contains( tcID );
        }

        /**
         * In default mode all TopComponents are docked by default.
         */
        public boolean shouldDock( String tcID ) {
            return null != tcID && (docked.contains( tcID ) || (!docked.contains( tcID ) && !slided.contains( tcID )));
        }
        
        public void mark() {
            super.mark();
            Set<ModeImpl> modes = model.getModes();
            for( Iterator<ModeImpl> i=modes.iterator(); i.hasNext(); ) {
                ModeImpl modeImpl = i.next();
                if( modeImpl.getState() != Constants.MODE_KIND_VIEW )
                    continue;

                //also remember which top component is the selected one
                String selTcId = null;
                TopComponent selTc = modeImpl.getSelectedTopComponent();
                if( null != selTc ) 
                    selTcId = WindowManagerImpl.getInstance().findTopComponentID(selTc);
                modeImpl.setPreviousSelectedTopComponentID( selTcId );
            }
        }
    }
}

