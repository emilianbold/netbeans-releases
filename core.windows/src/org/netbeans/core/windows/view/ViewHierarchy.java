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
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.EditorAreaFrame;
import org.netbeans.core.windows.view.ui.MainWindow;

import org.openide.windows.TopComponent;

/**
 * Class which manages GUI components.
 *
 * @author  Peter Zavadsky
 */
final class ViewHierarchy {

    /** Observes user changes to view hierarchy. */
    private final Controller controller;
    
    private final WindowDnDManager windowDnDManager;

    /** Split root element. */
    private ViewElement splitRoot;
    /** Map of separate mode views (view <-> accessor). */
    private final Map separateModeViews = new HashMap(10);
    
    /** Component in which is editor area, when the editor state is separated. */
    private EditorAreaFrame editorAreaFrame;

    /** Active mode view. */
    private ModeView activeModeView;
    /** Maximized mode view. */
    private ModeView maximizedModeView;
    
    /** */
    private final Map accessor2view = new HashMap(10);
    /** */
    private final Map view2accessor = new HashMap(10);
    
    private final MainWindow mainWindow = new MainWindow();

    private final MainWindowListener mainWindowListener;
    
    
    /** Creates a new instance of ViewHierarchy. */
    public ViewHierarchy(Controller controller, WindowDnDManager windowDnDManager) {
        this.controller = controller;
        this.windowDnDManager = windowDnDManager;
        
        this.mainWindowListener = new MainWindowListener(controller, this);
    }
    

    public boolean isDragInProgress() {
        return windowDnDManager.isDragging();
    }
    
    public MainWindow getMainWindow() {
        return mainWindow;
    }
    
    public void installMainWindowListeners() {
        mainWindow.addComponentListener(mainWindowListener);
        mainWindow.addWindowStateListener(mainWindowListener);
    }
    
    public void uninstallMainWindowListeners() {
        mainWindow.removeComponentListener(mainWindowListener);
        mainWindow.removeWindowStateListener(mainWindowListener);
    }
    
    /** Updates the view hierarchy according to new structure. */
    public void updateViewHierarchy(ModeStructureAccessor modeStructureAccessor, 
    boolean addingAllowed) {
        updateAccessors(modeStructureAccessor);
        splitRoot = updateViewForAccessor(modeStructureAccessor.getSplitRootAccessor(), addingAllowed);
        updateSeparateViews(modeStructureAccessor.getSeparateModeAccessors());
    }
    
    /** Puts new instances of accessors in and reuses the old relevant views. */
    public void updateAccessors(ModeStructureAccessor modeStructureAccessor) {
        Map a2v = new HashMap(accessor2view);
        
        accessor2view.clear();
        view2accessor.clear();

        Set accessors  = getAllAccessorsForTree(modeStructureAccessor.getSplitRootAccessor());
        accessors.addAll(Arrays.asList(modeStructureAccessor.getSeparateModeAccessors()));
        
        for(Iterator it = accessors.iterator(); it.hasNext(); ) {
            ElementAccessor accessor = (ElementAccessor)it.next();
            ElementAccessor similar = findSimilarAccessor(accessor, a2v);
            if(similar != null) {
                Object view = a2v.get(similar);
                accessor2view.put(accessor, view);
                view2accessor.put(view, accessor);
            }
        }
    }
    
    private Set getAllAccessorsForTree(ElementAccessor accessor) {
        Set s = new HashSet();
        if(accessor instanceof ModeAccessor) {
            s.add(accessor);
        } else if(accessor instanceof SplitAccessor) {
            SplitAccessor sa = (SplitAccessor)accessor;
            s.add(sa);
            s.addAll(getAllAccessorsForTree(sa.getFirst()));
            s.addAll(getAllAccessorsForTree(sa.getSecond()));
        } else if(accessor instanceof EditorAccessor) {
            EditorAccessor ea = (EditorAccessor)accessor;
            s.add(ea);
            s.addAll(getAllAccessorsForTree(ea.getEditorAreaAccessor()));
        }
        
        return s;
    }
    
    private ElementAccessor findSimilarAccessor(ElementAccessor accessor, Map a2v) {
        for(Iterator it = a2v.keySet().iterator(); it.hasNext(); ) {
            ElementAccessor next = (ElementAccessor)it.next();
            if(accessor.originatorEquals(next)) {
                return next;
            }
        }
        
        return null;
    }

    
    private ViewElement updateViewForAccessor(ElementAccessor patternAccessor, boolean addingAllowed) {
        if(patternAccessor == null) {
            return null;
        }
        
        ViewElement view = (ViewElement)accessor2view.get(patternAccessor);
        
        if(view != null) {
            if(patternAccessor instanceof SplitAccessor) {
                SplitAccessor sa = (SplitAccessor)patternAccessor;
                SplitView sv = (SplitView)view;
                sv.setOrientation(sa.getOrientation());
                sv.setLocation(sa.getSplitPosition());
                sv.setFirst(updateViewForAccessor(sa.getFirst(), addingAllowed));
                sv.setSecond(updateViewForAccessor(sa.getSecond(), addingAllowed));
                return sv;
            } else if(patternAccessor instanceof EditorAccessor) {
                EditorAccessor ea = (EditorAccessor)patternAccessor;
                EditorView ev = (EditorView)view;
                ev.setEditorArea(updateViewForAccessor(ea.getEditorAreaAccessor(), addingAllowed), addingAllowed);
                return ev;
            } else if(patternAccessor instanceof ModeAccessor) {
                // It is a ModeView.
                ModeAccessor ma = (ModeAccessor)patternAccessor;
                ModeView mv = (ModeView)view;
                mv.setTopComponents(ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                if(ma.getState() == Constants.MODE_STATE_SEPARATED) {
                    mv.setFrameState(ma.getFrameState());
                }
                return mv;
            }
        } else {
            if(patternAccessor instanceof SplitAccessor) {
                SplitAccessor sa = (SplitAccessor)patternAccessor;
                ViewElement first = updateViewForAccessor(sa.getFirst(), addingAllowed);
                ViewElement second = updateViewForAccessor(sa.getSecond(), addingAllowed);
                SplitView sv = new SplitView(controller, sa.getResizeWeight(),
                    sa.getOrientation(), sa.getSplitPosition(), first, second);
                accessor2view.put(patternAccessor, sv);
                view2accessor.put(sv, patternAccessor);
                return sv;
            } else if(patternAccessor instanceof ModeAccessor) {
                ModeAccessor ma = (ModeAccessor)patternAccessor;
                ModeView mv;
                if(ma.getState() == Constants.MODE_STATE_JOINED) {
                    mv = new ModeView(controller, windowDnDManager, ma.getResizeWeight(), ma.getKind(), 
                            ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                } else {
                    mv = new ModeView(controller, windowDnDManager, ma.getBounds(), ma.getFrameState(),
                            ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                }
                accessor2view.put(patternAccessor, mv);
                view2accessor.put(mv, patternAccessor);
                return mv;
            } else if(patternAccessor instanceof EditorAccessor) {
                // Editor accesssor indicates there is a editor area (possible split subtree of editor modes).
                EditorAccessor editorAccessor = (EditorAccessor)patternAccessor;
                EditorView ev = new EditorView(controller, windowDnDManager, 
                                editorAccessor.getResizeWeight(), updateViewForAccessor(editorAccessor.getEditorAreaAccessor(), addingAllowed));
                accessor2view.put(patternAccessor, ev);
                view2accessor.put(ev, patternAccessor);
                return ev;
            }
        }
        
        throw new IllegalStateException("Unknown accessor type, accessor=" + patternAccessor); // NOI18N
    }
    
    private void updateSeparateViews(ModeAccessor[] separateModeAccessors) {
        Map newViews = new HashMap();
        for(int i = 0; i < separateModeAccessors.length; i++) {
            ModeAccessor ma = separateModeAccessors[i];
            ModeView mv = (ModeView)updateViewForAccessor(ma, true);
            newViews.put(mv, ma);
        }
        
        Set oldViews = new HashSet(separateModeViews.keySet());
        oldViews.removeAll(newViews.keySet());
        
        separateModeViews.clear();
        separateModeViews.putAll(newViews);
        
        // PENDING Close all old views.
        for(Iterator it = oldViews.iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            if(comp.isVisible()) {
                comp.setVisible(false);
            }
//            // PENDING
//            ((java.awt.Window)mv.getComponent()).dispose();
        }
        
        // Open all new views.
        for(Iterator it = newViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            // #37463, it is needed to provide a check, otherwise the window would 
            // get fronted each time.
            if(!comp.isVisible()) {
                mv.getComponent().setVisible(true);
            }
        }
    }
    
    
    public ModeView getModeViewForAccessor(ModeAccessor modeAccessor) {
        return (ModeView)accessor2view.get(modeAccessor);
    }
    
    public ElementAccessor getAccessorForView(ViewElement view) {
        return (ElementAccessor)view2accessor.get(view);
    }

    public void activateMode(ModeAccessor activeModeAccessor) {
        ModeView activeModeView = getModeViewForAccessor(activeModeAccessor);
        activateModeView(activeModeView);
    }

    private void activateModeView(ModeView modeView) {
        setActiveModeView(modeView);
        if(modeView != null) {
            modeView.focusSelectedTopComponent();
        }
    }
    
    /** Set active mode view. */
    private void setActiveModeView(ModeView modeView) {
        if(modeView == activeModeView) {
            return;
        }
        
        if(activeModeView != null) {
            activeModeView.setActive(false);
        }
        
        activeModeView = modeView;
        
        if(activeModeView != null) {
            activeModeView.setActive(true);
        }
    }

    /** Gets active mode view. */
    public ModeView getActiveModeView() {
        return activeModeView;
    }
    
    public void setMaximizedModeView(ModeView modeView) {
        if(modeView == maximizedModeView) {
            return;
        }

        maximizedModeView = modeView;
    }
    
    public ModeView getMaximizedModeView() {
        return maximizedModeView;
    }
    
    public void removeModeView(ModeView modeView) {
        if(!view2accessor.containsKey(modeView)) {
            return;
        }
        
        Object accessor = view2accessor.remove(modeView);
        accessor2view.remove(accessor);

        if(separateModeViews.keySet().contains(modeView)) {
            separateModeViews.keySet().remove(modeView);
            modeView.getComponent().setVisible(false);
            return;
        }
        
        splitRoot = removeModeViewFromElement(splitRoot, modeView);
    }
    
    /** Gets set of all mode view components. */
    public Set getModeComponents() {
        Set set = new HashSet();
        for(Iterator it = view2accessor.keySet().iterator(); it.hasNext(); ) {
            Object next = it.next();
            if(next instanceof ModeView) {
                ModeView modeView = (ModeView)next;
                set.add(modeView.getComponent());
            }
        }
        
        return set;
    }
    
    /** Gets set of separate mode view frames and editor frame (if separated). */
    public Set getSeparateModeFrames() {
        Set s = new HashSet();
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView modeView = (ModeView)it.next();
            s.add(modeView.getComponent());
        }
        
        if(editorAreaFrame != null) {
            s.add(editorAreaFrame);
        }
        
        return s;
    }
    
    
    private ViewElement removeModeViewFromElement(ViewElement view, ModeView modeView) {
        if(view == modeView) {
            return null;
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            if(sv.getFirst() == modeView) {
                return sv.getSecond();
            }
            
            if(sv.getSecond() == modeView) {
                return sv.getFirst();
            }
            
            sv.setFirst(removeModeViewFromElement(sv.getFirst(), modeView));
            sv.setSecond(removeModeViewFromElement(sv.getSecond(), modeView));
            return sv;
        } else if(view instanceof EditorView) {
            EditorView ev = (EditorView)view;
            ev.setEditorArea(removeModeViewFromElement(ev.getEditorArea(), modeView), true);
            return ev;
        }
        
        return view;
    }
    
    private Component getDesktopComponent() {
        return splitRoot == null ? null : splitRoot.getComponent();
    }    

    public ViewElement getSplitRootElement() {
        return splitRoot;
    }
    
    public void releaseAll() {
        splitRoot = null;
        separateModeViews.clear();
        activeModeView = null;
        accessor2view.clear();
    }
    
    public void setSplitModesVisible(boolean visible) {
        setVisibleModeElement(splitRoot, visible);
    }
    
    private static void setVisibleModeElement(ViewElement view, boolean visible) {
        if(view instanceof ModeView) {
            view.getComponent().setVisible(visible);
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            setVisibleModeElement(sv.getFirst(), visible);
            setVisibleModeElement(sv.getSecond(), visible);
        } else if(view instanceof EditorView) {
            setVisibleModeElement(((EditorView)view).getEditorArea(), visible);
        }
    }
    
    public void setSeparateModesVisible(boolean visible) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setVisible(visible);
        }
        
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            mv.getComponent().setVisible(visible);
        }
    }

    public void updateEditorAreaFrameState(int frameState) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setExtendedState(frameState);
        }
    }
    
    public void updateFrameStates() {
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            mv.updateFrameState();
        }
    }
    
    public void updateSplits() {
        if(maximizedModeView != null) { // PENDING
            return;
        }
        
        // #38014 The destkop can be null if special switch used.
        Component desktop = getDesktopComponent();
        if(desktop != null) {
            updateSplitElement(splitRoot, desktop.getSize());
        }
    }

    private static void updateSplitElement(ViewElement view, Dimension realSize) {
        if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            
            sv.updateSplit(realSize);
            
            Dimension firstRealSize;
            Dimension secondRealSize;
            double location = sv.getLocation();
            int dividerSize = sv.getDividerSize();
            if(sv.getOrientation() == javax.swing.JSplitPane.VERTICAL_SPLIT) {
                firstRealSize = new Dimension(realSize.width, (int)(realSize.height * location) - dividerSize);
                secondRealSize = new Dimension(realSize.width, (int)(realSize.height * (1D - location)) - dividerSize);
            } else {
                firstRealSize = new Dimension((int)(realSize.width * location) - dividerSize, realSize.height);
                secondRealSize = new Dimension((int)(realSize.width * (1D - location)) - dividerSize, realSize.height);
            }

            updateSplitElement(sv.getFirst(), firstRealSize);
            updateSplitElement(sv.getSecond(), secondRealSize);
        } else if(view instanceof EditorView) {
            EditorView ev = (EditorView)view;
            updateSplitElement(ev.getEditorArea(), realSize);
        }
    }

    
    public void updateMainWindowBounds(WindowSystemAccessor wsa) {
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            mainWindow.setBounds(wsa.getMainWindowBoundsJoined());
        } else {
            mainWindow.setBounds(wsa.getMainWindowBoundsSeparated());
        }
        // #38146 So the updateSplit works with correct size.
        mainWindow.validate();
        // PENDING consider better handling this event so there is not doubled
        // validation (one in MainWindow - needs to be provided here) and this as second one.
    }
    
    public void setProjectName(String projectName) {
        mainWindow.setProjectName(projectName);
    }

    // PENDING Revise, updating desktop and editor area, bounds... separate this method.
    public void updateDesktop(WindowSystemAccessor wsa) {
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            if(maximizedModeView != null) {
                setMainWindowDesktop(maximizedModeView.getComponent());
                return;
            }
        }

        int editorAreaState = wsa.getEditorAreaState();
        if(editorAreaState == Constants.EDITOR_AREA_JOINED) {
            if(editorAreaFrame != null) {
                editorAreaFrame.setVisible(false);
                editorAreaFrame = null;
            }
            setMainWindowDesktop(getDesktopComponent());
        } else {
            boolean showEditorFrame = hasEditorAreaVisibleView();
            
            if(editorAreaFrame == null && showEditorFrame) {
                editorAreaFrame = createEditorAreaFrame();
                Rectangle editorAreaBounds = wsa.getEditorAreaBounds();
                if(editorAreaBounds != null) {
                    editorAreaFrame.setBounds(editorAreaBounds);
                }
            } else if(editorAreaFrame != null && !showEditorFrame) { // XXX
                editorAreaFrame.setVisible(false);
                editorAreaFrame = null;
            }
            
            setMainWindowDesktop(null);
            if(showEditorFrame) {
                setEditorAreaDesktop(getDesktopComponent());
            }
        }
    }
    
    public void updateDesktop() {
        if(mainWindow.hasDesktop()) {
            if(maximizedModeView != null) {
                setMainWindowDesktop(maximizedModeView.getComponent());
            } else {
                setMainWindowDesktop(getDesktopComponent());
            }
        } else {
            boolean showEditorFrame = hasEditorAreaVisibleView();
            
            if(editorAreaFrame != null) {
                if(showEditorFrame) {
                    editorAreaFrame.setDesktop(getDesktopComponent());
                } else { // XXX
                    editorAreaFrame.setVisible(false);
                    editorAreaFrame = null;
                }
            }
        }
    }
    
    private void setMainWindowDesktop(Component component) {
        setDesktop(component, true);
    }
    
    private void setEditorAreaDesktop(Component component) {
        setDesktop(component, false);
    }
    
    private void setDesktop(Component component, boolean toMainWindow) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        List focusOwnerAWTHierarchyChain; // To find out whether there was a change in AWT hierarchy according to focusOwner.
        if(focusOwner != null) {
            focusOwnerAWTHierarchyChain = getComponentAWTHierarchyChain(focusOwner);
        } else {
            focusOwnerAWTHierarchyChain = Collections.EMPTY_LIST;
        }
        
        if(toMainWindow) {
            mainWindow.setDesktop(component);
        } else {
            editorAreaFrame.setDesktop(component);
        }

        // XXX #37239, #37632 Preserve focus in case the focusOwner component
        // was 'shifted' in AWT hierarchy. I.e. when removed/added it loses focus,
        // but we need to keep it, e.g. for case when its parent split is removing.
        if(focusOwner != null
        && !focusOwnerAWTHierarchyChain.equals(getComponentAWTHierarchyChain(focusOwner))) {
            focusOwner.requestFocus();
        }
    }
    
    
    private List getComponentAWTHierarchyChain(Component comp) {
        List l = new ArrayList();
        Component c = comp;
        while(c != null) {
            l.add(c);
            c = c.getParent();
        }
        
        Collections.reverse(l);
        return l;
    }
    
    private boolean hasEditorAreaVisibleView() {
        return findEditorAreaElement().getEditorArea() != null;
    }
    
    
    private EditorAreaFrame createEditorAreaFrame() {
        final EditorAreaFrame frame = new EditorAreaFrame();
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if(frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                controller.userResizedEditorArea(frame.getBounds());
            }
            
            public void componentMoved(ComponentEvent evt) {
                if(frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                controller.userResizedEditorArea(frame.getBounds());
            }
        });
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeEditorModes();
            }
        });
        
        frame.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent evt) {
                controller.userChangedFrameStateEditorArea(evt.getNewState());
            }
        });
        
        return frame;
    }
    
    private void closeEditorModes() {
        closeModeForView(findEditorAreaElement().getEditorArea());
    }
    
    private void closeModeForView(ViewElement view) {
        if(view instanceof ModeView) {
            controller.userClosingMode((ModeView)view);
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            closeModeForView(sv.getFirst());
            closeModeForView(sv.getSecond());
        }
    }
    
    
    public void updateEditorAreaBounds(Rectangle bounds) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setBounds(bounds);
        }
    }

    // XXX
    public Rectangle getPureEditorAreaBounds() {
        EditorView editorView = findEditorAreaElement();
        if(editorView == null) {
            return new Rectangle();
        } else {
            return editorView.getPureBounds();
        }
    }
    
    private EditorView findEditorAreaElement() {
        return findEditorViewForElement(getSplitRootElement());
    }
    
    private EditorView findEditorViewForElement(ViewElement view) {
        if(view instanceof EditorView) {
            return (EditorView)view;
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            EditorView ev = findEditorViewForElement(sv.getFirst());
            if(ev != null) {
                return ev;
            }
            ev = findEditorViewForElement(sv.getSecond());
            if(ev != null) {
                return ev;
            }
        }
        
        return null;
    }
    
    public void updateUI() {
        SwingUtilities.updateComponentTreeUI(mainWindow);
        if(editorAreaFrame != null) {
            SwingUtilities.updateComponentTreeUI(editorAreaFrame);
        }
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            SwingUtilities.updateComponentTreeUI(mv.getComponent());
        }
    }
    
    public Set getShowingTopComponents() {
        Set s = new HashSet();
        for(Iterator it = accessor2view.keySet().iterator(); it.hasNext(); ) {
            Object accessor = it.next();
            if(accessor instanceof ModeAccessor) {
                s.add(((ModeAccessor)accessor).getSelectedTopComponent());
            }
        }
        for(Iterator it = separateModeViews.values().iterator(); it.hasNext(); ) {
            Object accessor = it.next();
            if(accessor instanceof ModeAccessor) {
                s.add(((ModeAccessor)accessor).getSelectedTopComponent());
            }
        }
        
        return s;
    }
    
    public String toString() {
        return dumpElement(splitRoot, 0) + "\nseparateViews=" + separateModeViews.keySet(); // NOI18N
    }
    
    private String dumpElement(ViewElement view, int indent) {
        String indentString = createIndentString(indent);
        StringBuffer sb = new StringBuffer();
        
        if(view instanceof ModeView) {
            sb.append(indentString + view + "->" + view.getComponent().getClass() + "@"  + view.getComponent().hashCode());
        } else if(view instanceof EditorView) {
            sb.append(indentString + view);
            sb.append("\n" + dumpElement(((EditorView)view).getEditorArea(), ++indent));
        } else if(view instanceof SplitView) {
            sb.append(indentString + view + "->" + view.getComponent().getClass() + "@"  + view.getComponent().hashCode());
            indent++;
            sb.append("\n" + dumpElement(((SplitView)view).getFirst(), indent));
            sb.append("\n" + dumpElement(((SplitView)view).getSecond(), indent));
        }
         
        return sb.toString();
    }
    
    private static String createIndentString(int indent) {
        StringBuffer sb = new StringBuffer(indent);
        for(int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        
        return sb.toString();
    }

    private String dumpAccessors() {
        StringBuffer sb = new StringBuffer();
        for(Iterator it = accessor2view.keySet().iterator(); it.hasNext(); ) {
            Object accessor = it.next();
            sb.append("accessor="+accessor + "\tview="+accessor2view.get(accessor) + "\n"); // NOI18N
        }
        
        return sb.toString();
    }

    private void setStateOfSeparateViews(int state) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setExtendedState(state);
        }

        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            if(comp instanceof Frame) {
                ((Frame)comp).setExtendedState(state);
            }
        }
    }

    
    /** Main window listener. */
    private static class MainWindowListener extends ComponentAdapter
    implements WindowStateListener {
        
        private final Controller controller;
        private final ViewHierarchy hierarchy;
        
        public MainWindowListener(Controller controller, ViewHierarchy hierarchy) {
            this.controller = controller;
            this.hierarchy  = hierarchy;
        }
        
        public void componentResized(ComponentEvent evt) {
            controller.userResizedMainWindow(evt.getComponent().getBounds());
        }
        
        public void componentMoved(ComponentEvent evt) {
            controller.userMovedMainWindow(evt.getComponent().getBounds());
        }
        
        public void windowStateChanged(WindowEvent evt) {
            int oldState = evt.getOldState();
            int newState = evt.getNewState();
            controller.userChangedFrameStateMainWindow(newState);
            
            if(oldState == Frame.NORMAL && newState == Frame.ICONIFIED) {
                hierarchy.setStateOfSeparateViews(Frame.ICONIFIED);
            } else if(oldState == Frame.ICONIFIED && newState == Frame.NORMAL) {
                hierarchy.setStateOfSeparateViews(Frame.NORMAL);
            }
        }
    } // End of main window listener.
    
}

