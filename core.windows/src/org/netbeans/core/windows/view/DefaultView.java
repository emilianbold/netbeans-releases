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

package org.netbeans.core.windows.view;


import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.openide.awt.ToolbarPool; // Why is this in open API?
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.model.ModelElement;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.WindowSystemSnapshot;


/**
 * Class which handles view requests, i.e. updates GUI accordingly (ViewHierarchy)
 * and also handles changes to GUI made by user, informs controller handler.
 *
 * @author  Peter Zavadsky
 */
class DefaultView implements View, Controller, WindowDnDManager.ViewAccessor {
    
    
    private final ViewHierarchy hierarchy = new ViewHierarchy(this, new WindowDnDManager(this));
    
    private final ControllerHandler controllerHandler;
    
    private final Set showingTopComponents = new WeakSet(10);

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(DefaultView.class);

    
    public DefaultView(ControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
    }
    

    // XXX
    public boolean isDragInProgress() {
        return hierarchy.isDragInProgress();
    }
    
    // XXX
    public Frame getMainWindow() {
        return hierarchy.getMainWindow();
    }
    
                                                  
    public void changeGUI(ViewEvent[] viewEvents, WindowSystemSnapshot snapshot) {
        
        // Change to view understandable-convenient structure.
        WindowSystemAccessor wsa = ViewHelper.createWindowSystemAccessor(snapshot);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Structure=" + wsa); // NOI18N
            debugLog(""); // NOI18N
        }

        // Update accessors.
        if(wsa != null) { // wsa == null during hiding.
            hierarchy.updateViewHierarchy(wsa.getModeStructureAccessor(),
                wsa.getMaximizedModeAccessor() == null || wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED);
        }

        // Update showing TopComponents.
        Set oldShowing = new HashSet(showingTopComponents);
        Set newShowing = hierarchy.getShowingTopComponents();
        showingTopComponents.clear();
        showingTopComponents.addAll(newShowing);
        
        Set toShow = new HashSet(newShowing);
        toShow.removeAll(oldShowing);
        for(Iterator it = toShow.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            WindowManagerImpl.getInstance().componentShowing(tc);
        }
        
        // PENDING Find main event first.
        for(int i = 0; i < viewEvents.length; i++) {
            ViewEvent viewEvent = viewEvents[i];
            int changeType = viewEvent.getType();
            
            if(changeType == CHANGE_VISIBILITY_CHANGED) {
                if(DEBUG) {
                    debugLog("Winsys visibility changed, visible=" + viewEvent.getNewValue()) ; // NOI18N
                }
                
                windowSystemVisibilityChanged(((Boolean)viewEvent.getNewValue()).booleanValue(), wsa);
                // PENDING this should be processed separatelly, there is nothing to coallesce.
                return;
            }
        }
        
        // Process all event types.
        for(int i = 0; i < viewEvents.length; i++) {
            ViewEvent viewEvent = viewEvents[i];
            int changeType = viewEvent.getType();
            
            // The other types.
            if(changeType == CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window bounds joined changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                    Rectangle bounds = (Rectangle)viewEvent.getNewValue();
                    if(bounds != null) {
                        hierarchy.getMainWindow().setBounds(bounds);
                    }
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window bounds separated changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                    Rectangle bounds = (Rectangle)viewEvent.getNewValue();
                    if(bounds != null) {
                        hierarchy.getMainWindow().setBounds(bounds);
                    }
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window frame state joined changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                    hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateJoined());
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window frame state separated changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                    hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateSeparated());
                }
            } else if(changeType == CHANGE_EDITOR_AREA_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area state changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.updateMainWindowBounds(wsa);
                hierarchy.setSeparateModesVisible(true);
                hierarchy.updateSplits();
            } else if(changeType == CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area frame state changed"); // NOI18N
                }

                hierarchy.updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
            } else if(changeType == CHANGE_EDITOR_AREA_BOUNDS_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area bounds changed"); // NOI18N
                }

                hierarchy.updateEditorAreaBounds((Rectangle)viewEvent.getNewValue());
            } else if(changeType == CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area constraints changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_ACTIVE_MODE_CHANGED) {
                if(DEBUG) {
                    debugLog("Active mode changed, mode=" + viewEvent.getNewValue()); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOOLBAR_CONFIGURATION_CHANGED) {
                if(DEBUG) {
                    debugLog("Toolbar config name changed"); // NOI18N
                }

                ToolbarPool.getDefault().setConfiguration(wsa.getToolbarConfigurationName());
            } else if(changeType == CHANGE_MAXIMIZED_MODE_CHANGED) {
                if(DEBUG) {
                    debugLog("Maximized mode changed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                hierarchy.updateSplits();
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_ADDED) {
                if(DEBUG) {
                    debugLog("Mode added"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_MODE_REMOVED) {
                if(DEBUG) {
                    debugLog("Mode removed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_CONSTRAINTS_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode constraints changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_MODE_BOUNDS_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode bounds changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.getComponent().setBounds((Rectangle)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_MODE_FRAME_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode state changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.setFrameState(((Integer)viewEvent.getNewValue()).intValue());
                    modeView.updateFrameState();
                }
            } else if(changeType == CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED) {
                if(DEBUG) {
                    debugLog("Selected topcomponent changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                // XXX PENDING see TopComponent.requestFocus (it's wrongly overriden).
                hierarchy.updateDesktop(wsa);
                // XXX if the selection is changed in the active mode reactivate it.
                ModeAccessor ma = wsa.getActiveModeAccessor();
                if(ma == wsa.getActiveModeAccessor()) {
                    hierarchy.activateMode(ma);
                }
            } else if(changeType == CHANGE_MODE_TOPCOMPONENT_ADDED) {
                if(DEBUG) {
                    debugLog("TopComponent added"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.setSeparateModesVisible(true);
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                hierarchy.updateSplits();
            } else if(changeType == CHANGE_MODE_TOPCOMPONENT_REMOVED) {
                if(DEBUG) {
                    debugLog("TopComponent removed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.removeTopComponent((TopComponent)viewEvent.getNewValue());
                }
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent display name changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateName((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent display name annotation changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateName((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent tooltip changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateToolTip((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_ICON_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent icon changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateIcon((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_ATTACHED) {
                if(DEBUG) {
                    debugLog("TopComponent attached"); // NOI18N
                }                

                hierarchy.updateDesktop(wsa);
                // PENDING Updating splits - figure out how to init just newly added ones.
                hierarchy.updateSplits();
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ARRAY_ADDED) {
                if(DEBUG) {
                    debugLog("TopComponent array added:" // NOI18N
                        + Arrays.asList((TopComponent[])viewEvent.getNewValue()));
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.updateSplits();
            } else if(changeType == CHANGE_TOPCOMPONENT_ARRAY_REMOVED) {
                if(DEBUG) {
                    debugLog("TopComponent array removed:" // NOI18N
                        + Arrays.asList((TopComponent[])viewEvent.getNewValue()));
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.updateSplits();
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ACTIVATED) {
                if(DEBUG) {
                    debugLog("TopComponent activated, tc=" + viewEvent.getNewValue()); // NOI18N
                }
                
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_CLOSED) {
                if(DEBUG) {
                    debugLog("Mode closed, mode=" + viewEvent.getSource()); // NOI18N
                }
                
                hierarchy.updateDesktop();
                hierarchy.updateSplits();
            } else if(changeType == CHANGE_DND_PERFORMED) {
                if(DEBUG) {
                    debugLog("DnD performed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop();
                hierarchy.updateSplits();
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_UI_UPDATE) {
                if(DEBUG) {
                    debugLog("UI update"); // NOI18N
                }

                hierarchy.updateUI();
            } else if(changeType == CHANGE_PROJECT_NAME) {
                if(DEBUG) {
                    debugLog("Update project name"); // NOI18N
                }
                
                hierarchy.setProjectName(wsa.getProjectName());
            }
        }
        
        Set toHide = new HashSet(oldShowing);
        toHide.removeAll(newShowing);
        for(Iterator it = toHide.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            WindowManagerImpl.getInstance().componentHidden(tc);
        }
    }
    
    /** Whether the window system should show or hide its GUI. */
    private void windowSystemVisibilityChanged(boolean visible, WindowSystemAccessor wsa) {
        if(visible) {
            showWindowSystem(wsa);
        } else {
            hideWindowSystem();
        }
    }
    

    //////////////////////////////////////////////////////////
    private void showWindowSystem(WindowSystemAccessor wsa) {
        long start = System.currentTimeMillis();
        
        hierarchy.getMainWindow().initializeComponents();
        // Init toolbar.
        ToolbarPool.getDefault().setConfiguration(wsa.getToolbarConfigurationName());
        
        if(DEBUG) {
            debugLog(wsa.getModeStructureAccessor().toString());
        }

        hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));

        // Init desktop.
        hierarchy.updateDesktop(wsa);

        if(DEBUG) {
            debugLog("Init view 2="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
        
        hierarchy.setSplitModesVisible(true);

        if(DEBUG) {
            debugLog("Init view 3="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
        
        // Prepare main window (pack and set bounds).
        hierarchy.getMainWindow().prepareWindow();

        if(DEBUG) {
            debugLog("Init view 4="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
       
        // Shows main window
        hierarchy.getMainWindow().setVisible(true);
        
        // XXX Seems it needs to be called after setVisible(true);
        // When tried to be set (maximized) immediately after prepareWindow, it didn't work,
        // even thou its peer was already present (addNotify from pack was called on frame).
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateJoined());
        } else {
            hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateSeparated());
        }
        // XXX #37369 Again this needs to be called after setting possible 
        // Adjusts positions of splits.
        hierarchy.updateSplits();
        
        // Show separate modes.
        hierarchy.setSeparateModesVisible(true);

        hierarchy.updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
        
        // Updates frame states of separate modes.
        hierarchy.updateFrameStates();
        
        hierarchy.activateMode(wsa.getActiveModeAccessor());
        hierarchy.setProjectName(wsa.getProjectName());
        
        // XXX PENDING
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            updateMainWindowBoundsSeparatedHelp();
            updateEditorAreaBoundsHelp();
            updateSeparateBoundsForView(hierarchy.getSplitRootElement());
        }

        hierarchy.installMainWindowListeners();
        
        if(DEBUG) {
            debugLog("Init view 5="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
    }
    
    private void hideWindowSystem() {
        hierarchy.uninstallMainWindowListeners();
        
        hierarchy.setSeparateModesVisible(false);
        hierarchy.getMainWindow().setVisible(false);
        // Release all.
        hierarchy.releaseAll();
    }
    
    ////////////////////////////////////////////////////
    // Controller >>
    public void userActivatedModeView(ModeView modeView) {
        if(DEBUG) {
            debugLog("User activated mode view, mode=" + modeView); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedMode(mode);
    }
    
    public void userSelectedTab(ModeView modeView, TopComponent selected) {
        if(DEBUG) {
            debugLog("User selected tab, tc=" + WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedTopComponent(mode, selected);
    }
    
    public void userClosingMode(ModeView modeView) {
        if(DEBUG) {
            debugLog("User closing mode="+modeView); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userClosedMode(mode);
    }
    
    private void removeModeViewFromHierarchy(ModeView modeView) {
        hierarchy.removeModeView(modeView);
        hierarchy.updateDesktop();
    }
    
    public void userResizedMainWindow(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized main window"); // NOI18N
        }

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            controllerHandler.userResizedMainWindow(bounds);
        }

        // Update also the splits.
        updateChangedSplits();

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            // XXX PENDING
            updateMainWindowBoundsSeparatedHelp();
            updateEditorAreaBoundsHelp();
            updateSeparateBoundsForView(hierarchy.getSplitRootElement());
        }
    }
    
    
    private void updateChangedSplits() {
        if(hierarchy.getMaximizedModeView() != null) { // PENDING
            return;
        }
        splitChangedForView(hierarchy.getSplitRootElement());
    }
    
    private void splitChangedForView(ViewElement view) {
        if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            JSplitPane sp = (JSplitPane)sv.getComponent();
            int absoluteLocation = sp.getDividerLocation();
            double relativeLocation;
            if(sp.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                relativeLocation = (double)absoluteLocation/(sp.getHeight() - sp.getDividerSize());
            } else {
                relativeLocation = (double)absoluteLocation/(sp.getWidth() - sp.getDividerSize());
            }
            userMovedSplit(relativeLocation, sv, sv.getFirst(), sv.getSecond());
            
            splitChangedForView(sv.getFirst());
            splitChangedForView(sv.getSecond());
        }
    }
    
    public void userMovedMainWindow(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User moved main window"); // NOI18N
        }

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            controllerHandler.userResizedMainWindow(bounds);
        }
    }
    
    public void userResizedEditorArea(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized editor area"); // NOI18N
        }

        controllerHandler.userResizedEditorArea(bounds);
    }
    
    public void userResizedModeBounds(ModeView modeView, Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized mode"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userResizedModeBounds(mode, bounds);
    }
    
    public void userMovedSplit(double splitLocation, SplitView splitView,
    ViewElement first, ViewElement second) {
        if(DEBUG) {
            debugLog("User moved split"); // NOI18N
        }

        SplitAccessor splitAccessor = (SplitAccessor)hierarchy.getAccessorForView(splitView);
        ElementAccessor firstAccessor = hierarchy.getAccessorForView(first);
        ElementAccessor secondAccessor = hierarchy.getAccessorForView(second);
        
        ViewHelper.computeSplitWeights(splitLocation, splitAccessor, firstAccessor, secondAccessor, controllerHandler);
        
        // XXX PENDING
        updateSeparateBoundsForView(splitView);
    }
    
    public void userClosedTopComponent(ModeView modeView, TopComponent tc) {
        if(DEBUG) {
            debugLog("User closed topComponent=" + tc); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userClosedTopComponent(mode, tc);
    }
    
    public void userChangedFrameStateMainWindow(int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state main window"); // NOI18N
        }
        
        controllerHandler.userChangedFrameStateMainWindow(frameState);
    }
    
    public void userChangedFrameStateEditorArea(int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state editor area"); // NOI18N
        }

        controllerHandler.userChangedFrameStateEditorArea(frameState);
    }
    
    public void userChangedFrameStateMode(ModeView modeView, int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state mode"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userChangedFrameStateMode(mode, frameState);
    }
    
    // DnD
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDroppedTopComponents(mode, tcs);
    }
    
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs, int index) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's to index=" + index); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        
        // #37127 Refine the index if the TC is moving inside the mode.
        int position = Arrays.asList(modeAccessor.getOpenedTopComponents()).indexOf(tcs[0]);
        if(position > -1 && position <= index) {
            index--;
        }
                
        controllerHandler.userDroppedTopComponents(mode, tcs, index);
    }
    
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs, String side) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's to side=" + side); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDroppedTopComponents(mode, tcs, side);
    }

    public void userDroppedTopComponentsIntoEmptyEditor(TopComponent[] tcs) {
        if(tcs.length == 0) {
            return;
        }

        if(DEBUG) {
            debugLog("User dropped TopComponent's into empty editor"); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsIntoEmptyEditor(tcs);
    }
    
    public void userDroppedTopComponentsAround(TopComponent[] tcs, String side) {
        if(tcs.length == 0) {
            return;
        }

        if(DEBUG) {
            debugLog("User dropped TopComponent's around, side=" + side); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsAround(tcs, side);
    }
    
    public void userDroppedTopComponentsIntoSplit(SplitView splitView, TopComponent[] tcs) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's into split=" + splitView); // NOI18N
        }

        SplitAccessor splitAccessor = (SplitAccessor)hierarchy.getAccessorForView(splitView);
        ElementAccessor firstAccessor = hierarchy.getAccessorForView(splitView.getFirst());
        ElementAccessor secondAccessor = hierarchy.getAccessorForView(splitView.getSecond());
        
        ModelElement splitElement  = getModelElementForAccessor(splitAccessor);
        ModelElement firstElement  = getModelElementForAccessor(firstAccessor);
        ModelElement secondElement = getModelElementForAccessor(secondAccessor);
        
        controllerHandler.userDroppedTopComponentsIntoSplit(splitElement, firstElement, secondElement, tcs);
    }
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side) {
        if(tcs.length == 0) {
            return;
        }

        if(DEBUG) {
            debugLog("User dropped TopComponent's around editor, side=" + side); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsAroundEditor(tcs, side);
    }
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's into free area, bounds=" + bounds); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsIntoFreeArea(tcs, bounds);
    }
    
    
    private static ModeImpl getModeForModeAccessor(ModeAccessor accessor) {
        return accessor == null ? null : accessor.getMode();
    }
    
    private static ModelElement getModelElementForAccessor(ElementAccessor accessor) {
        return accessor == null ? null : accessor.getOriginator();
    }
    // Controller <<
    ////////////////////////////////////////////////////
    
    // XXX
    private void updateMainWindowBoundsSeparatedHelp() {
        controllerHandler.userResizedMainWindowBoundsSeparatedHelp(
                hierarchy.getMainWindow().getPureMainWindowBounds());
    }
    
    // XXX
    private void updateEditorAreaBoundsHelp() {
        Rectangle bounds = hierarchy.getPureEditorAreaBounds();
        controllerHandler.userResizedEditorAreaBoundsHelp(bounds);
    }
    
    // XXX PENDING This is just for the cases split modes doesn't have a separated
    // opposite ones, so they keep the bounds for them. Revise.
    private void updateSeparateBoundsForView(ViewElement view) {
        if(view instanceof ModeView) {
            ModeView mv = (ModeView)view;
            ModeAccessor ma = (ModeAccessor)hierarchy.getAccessorForView(mv);
            if(ma != null) {
                Component comp = mv.getComponent();
                Rectangle bounds = comp.getBounds();
                Point point = new Point(0, 0);
                SwingUtilities.convertPointToScreen(point, comp);
                bounds.setLocation(point);
                
                ModeImpl mode = getModeForModeAccessor(ma);
                // XXX ControllerHandler
                controllerHandler.userResizedModeBoundsSeparatedHelp(mode, bounds);
            }
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            updateSeparateBoundsForView(sv.getFirst());
            updateSeparateBoundsForView(sv.getSecond());
        } else if(view instanceof EditorView) {
            updateEditorAreaBoundsHelp();
            // Editor area content isn't needed to remember.
        }
    }

    ///////////////
    // ViewAccessor
    public Set getModeComponents() {
        return hierarchy.getModeComponents();
    }
    
    public Set getSeparateModeFrames() {
        return hierarchy.getSeparateModeFrames();
    }
    
    public Controller getController() {
        return this;
    }
    // ViewAccessor
    ///////////////

    
    private static void debugLog(String message) {
        Debug.log(DefaultView.class, message);
    }


}

