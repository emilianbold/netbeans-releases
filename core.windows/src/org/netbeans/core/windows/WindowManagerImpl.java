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

package org.netbeans.core.windows;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.util.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.ObjectStreamField;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openide.windows.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NbMarshalledObject;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInfo;
import org.openide.ErrorManager;
import org.openide.util.actions.SystemAction;

import org.netbeans.core.NbTopManager;

import org.netbeans.core.windows.WeakHash.Value;
import org.netbeans.core.windows.util.*;
import org.netbeans.core.windows.frames.FrameType;
import org.netbeans.core.windows.frames.TopComponentContainer;
import org.netbeans.core.windows.layers.ResetCookie;
import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.windows.actions.CloseMaximizedFrameAction;
import org.netbeans.core.windows.actions.MinimizeMaximizedFrameAction;
import org.netbeans.core.windows.actions.RestoreMaximizedFrameAction;
import java.beans.PropertyChangeEvent;

/** Implementation of manager of windows in the IDE.
 * Handles work with workspaces, serialization of all window system,
 * allows to listen to workspace events.
 * This class is final only for performance reasons. Can be freely
 * unfinaled if desired.
 *
 * @author Jaroslav Tulach, Dafe Simonek
 */
public final class WindowManagerImpl extends WindowManager {
    /** Lookup prefix for top components */
    public static final String TC_PREFIX = "wstopcomp"; // NOI18N
    
    /** Constant that identifies sections of wm data */
    public static final int PROPERTIES = 1;
    public static final int WORKSPACES = 2;
    
    /** The only instance of the window manager implementation
     * in the system */
    private static WindowManagerImpl defaultInstance;
    
    /** array of workspaces */
    private Workspace[] workspaces;
    /** Current active workspace */
    private Workspace current;
    /** Saved UI mode*/
    int savedUIMode = 0;
    /** properties support */
    private PropertyChangeSupport changeSupport;
    /** the set of listeners which listen to the changes of
     * top component set in system */
    private HashSet tcListeners;
    
    /** initialized already? */
    private boolean isCreated;
    /** map of workspace::Value (activated nodes) */
    private WeakHash workspace2Nodes;
    /** true if main window was already positioned
     * during deserialization */
    private boolean mainPositioned = false;
    /** Helper temporary variable, holds TC manager currently
     * being validated, used in createTopComponentManager() method */
    private TopComponentManager validatedManager;
    /** map of exceptions to names of badly persistenced top components,
     * serves as additional annotation of main exception */
    private Map failedCompsMap;
    /** Screen size in at the time of window system last serialization */
    private Dimension oldScreenSize;
    /** Main window bounds in at the time of window system last serialization */
    private Rectangle oldMainWindowBounds;
    /** lazy updater of window manager, holds data on persistent storage*/
    private LazyUpdater updater;
    
    static final long serialVersionUID =680725949680433701L;
    
    /** default base name for noname modes */
    private static final String DEFAULT_NAME = "untitled_mode"; // NOI18N
    /** max length */
    private static final int MAX_MODE_NAME_LENGTH = 20; // NOI18N
    
    //Buttons for maximized internal frame in MDI mode placed to main window menu bar.
    private static java.awt.Component horizontalGlue;
    private static JButton iconButton;
    private static JButton minButton;
    private static JButton closeButton;
    private static boolean buttonsAdded;
    
    /** Close frame action */
    private static CloseMaximizedFrameAction closeFrame;
    
    /** Minimize frame action */
    private static MinimizeMaximizedFrameAction minimizeFrame;
    
    /** Restore frame action */
    private static RestoreMaximizedFrameAction restoreFrame;
    
    /** Deferred saving of winsys */
    private RequestProcessor.Task deferSaving;
    /** Delay in ms for saving of winsys */
    private static final int WINSYS_SAVING_DELAY = System.getProperty ("netbeans.debug.heap") != null ? 0 : 30000;
    /** lock to synchronize top component listeners access */
    private final Object TC_LISTENERS_LOCK = new Object();
    
    private static final RequestProcessor PROCESSOR = new RequestProcessor ("Winsys Save Processor");

    /** Used to control workspace switching */
    private boolean wasAnyWorkspaceVisible = false;
    
    /** Used to control workspace switching */
    private boolean switchingWorkspace = false;
    
    /** Used to control processing focus event during workspace switching */
    private boolean switchingWorkspaceIgnoreFocusEvent = false;
    
    /** It is set to true when IDE is in shutdown -> we do not want to
     * change state of winsys before it is saved. Ignore changes of state variables
     * eg. in listeners like frameDeactivated. */
    private boolean exitingIDE = false;
    
    /** Used to detect start of IDE when opening project. When first project is opened
     * during IDE start do not reset winsys DataObjects. */
    private boolean wasIDEStarted = false;
    
    /** Default constructor. Don't use directly, use getDefault()
     * instead.
     */
    public WindowManagerImpl() {
        synchronized (TC_PREFIX) {
            // a static object to synchronize on
            if (defaultInstance != null) {
                throw new IllegalStateException ("Instance already exists"); // NOI18N
            }
            defaultInstance = this;
            
            initialize();
            if (NbTopManager.getUninitialized().isInteractive(NbTopManager.IL_WORKSPACES)) {
                SessionManager.getDefault().addPropertyChangeListener(projectSwitchL);
                //Check if project module is eanbled
                ModuleInfo curModuleInfo = WindowUtils.findModule
                ("org.netbeans.modules.projects", null, null); // NOI18N
                if (curModuleInfo != null) {
                    curModuleInfo.addPropertyChangeListener(projectSwitchL);
                    setProjectModuleEnabled(curModuleInfo.isEnabled());
                } else {
                    setProjectModuleEnabled(false);
                    //Add listener to whole result to detect when project module
                    //is installed for the first time.
                    Lookup.Result modulesResult =
                        Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class));
                    modulesResult.addLookupListener(new LookupResultListener());
                }
            }
        }
    }
    
    /** Needed initialization, called from getDefault and
     * during deserialization */
    private void initialize() {
        initToolbars();
        changeSupport = new PropertyChangeSupport(this);
        workspace2Nodes = new WeakHash();
        workspaces = new Workspace[0];
        createButtons();
    }
    
    
    private void initToolbars() {
        
        /** toolbar dtd location */
        final String TOOLBAR_DTD               =
        "org/netbeans/core/windows/resources/toolbar.dtd"; // NOI18N
        /** toolbar dtd public id */
        final String TOOLBAR_DTD_PUBLIC_ID     =
        "-//Forte for Java//DTD toolbar//EN"; // NOI18N
        /** toolbar prcessor class */
        final Class  TOOLBAR_PROCESSOR_CLASS   =
        org.netbeans.core.windows.toolbars.ToolbarProcessor.class;
        /** toolbar icon base */
        final String TOOLBAR_ICON_BASE         =
        "/org/netbeans/core/windows/toolbars/xmlToolbars"; // NOI18N
        
        org.openide.loaders.XMLDataObject.Info xmlinfo = new org.openide.loaders.XMLDataObject.Info();
        xmlinfo.addProcessorClass(TOOLBAR_PROCESSOR_CLASS);
        xmlinfo.setIconBase(TOOLBAR_ICON_BASE);
        
        /* Register public toolbar dtd id with toolbar configuration xml info */
        org.openide.loaders.XMLDataObject.registerInfo(TOOLBAR_DTD_PUBLIC_ID, xmlinfo);
        
    }
    
    /** Singleton accessor, returns instance of window manager implementation */
    public static WindowManagerImpl getInstance() {
        return (WindowManagerImpl)org.openide.util.Lookup.getDefault().lookup (WindowManager.class);
    }
    
    /** Saves winsys.
     */
    public void save() throws IOException {
        try {
            PersistenceManager.getDefault().setSaveInProgress(true);
            /*if ((updater != null) && isChanged()) {
                updater.save();
            }*/
            //Save workspaces
            for (int i = 0; i < workspaces.length; i++) {
                try {
                    ((WorkspaceImpl) workspaces[i]).save();
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                }
            }
        } finally {
            PersistenceManager.getDefault().setSaveInProgress(false);
        }
    }
    
    /** Posts or reschedule winsys saving task to default request processor
     */
    public void postSaving() {
        RequestProcessor.Task task = deferSaving;
        if (task == null) {
            deferSaving = PROCESSOR.post(
                new Runnable() {
                    public void run() {
                        try {
                            WindowManagerImpl.getInstance().save();
                        } catch (IOException exc) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                        }
                    }
                }, WINSYS_SAVING_DELAY);
        } else {
            task.schedule(WINSYS_SAVING_DELAY);
        }
    }

    /** Create and initialize buttons for maximized frame in MDI mode */
    private synchronized void createButtons() {
        if (closeFrame == null) {
            closeFrame = (CloseMaximizedFrameAction) SystemAction.get(CloseMaximizedFrameAction.class);
        }
        if (minimizeFrame == null) {
            minimizeFrame = (MinimizeMaximizedFrameAction) SystemAction.get(MinimizeMaximizedFrameAction.class);
        }
        if (restoreFrame == null) {
            restoreFrame = (RestoreMaximizedFrameAction) SystemAction.get(RestoreMaximizedFrameAction.class);
        }
        if (horizontalGlue == null) {
            horizontalGlue = Box.createHorizontalGlue();
        }
        if (iconButton == null) {
            iconButton = new JButton();
            Icon iconIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
            iconButton.setIcon(iconIcon);
            iconButton.setBorder(BorderFactory.createEmptyBorder());
            iconButton.addActionListener(minimizeFrame);
        }
        if (minButton == null) {
            minButton = new JButton();
            Icon minIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
            minButton.setIcon(minIcon);
            minButton.setBorder(BorderFactory.createEmptyBorder());
            minButton.addActionListener(restoreFrame);
        }
        if (closeButton == null) {
            closeButton = new JButton();
            Icon closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
            closeButton.setIcon(closeIcon);
            closeButton.setBorder(BorderFactory.createEmptyBorder());
            closeButton.addActionListener(closeFrame);
        }
        buttonsAdded = false;
    }
    
    /** Add buttons for maximized frame in MDI mode to menu bar.
     * It must be called from AWT thread.
     */
    void addButtons() {
        if (buttonsAdded) {
            return;
        }
        buttonsAdded = true;
        JMenuBar menuBar = MainWindow.getDefault().getJMenuBar();
        //Add buttons to menu bar
        menuBar.add(horizontalGlue);
        menuBar.add(iconButton);
        menuBar.add(minButton);
        menuBar.add(closeButton);
        menuBar.revalidate();
        menuBar.repaint();
    }
    
    /** Remove buttons for maximized frame in MDI mode to menu bar.
     * It must be called from AWT thread.
     */
    void removeButtons() {
        if (!buttonsAdded) {
            return;
        }
        buttonsAdded = false;
        JMenuBar menuBar = MainWindow.getDefault().getJMenuBar();
        //Remove buttons from menu bar
        menuBar.remove(horizontalGlue);
        menuBar.remove(iconButton);
        menuBar.remove(minButton);
        menuBar.remove(closeButton);
        menuBar.revalidate();
        menuBar.repaint();
    }
    
    /** Set flag setWasAnyWorkspaceVisible. Used to control way how workspaces
     * are switched in WorkspaceVisibilityManager. */
    void setWasAnyWorkspaceVisible (boolean b) {
        wasAnyWorkspaceVisible = b;
    }
    
    /** Used to control way how workspaces are switched in 
     * WorkspaceVisibilityManager. */
    boolean isWasAnyWorkspaceVisible () {
        return wasAnyWorkspaceVisible;
    }
    
    /** Set flag switchingWorkspace. Used to control way how workspaces are
     * switched in WorkspaceVisibilityManager. */
    void setSwitchingWorkspace (boolean b) {
        switchingWorkspace = b;
    }
    
    /** Used to control way how workspaces are switched in
     * WorkspaceVisibilityManager. Called from InternalFrameTypeImpl.setCursor
     * to avoid cursor reset during internal frame initialization during
     * workspace switch. */
    public boolean isSwitchingWorkspace () {
        return switchingWorkspace;
    }
    
    /** Set flag switchingWorkspaceIgnoreFocusEvent. Used to control way how workspaces are
     * switched in WorkspaceVisibilityManager. */
    void setSwitchingWorkspaceIgnoreFocusEvent (boolean b) {
        switchingWorkspaceIgnoreFocusEvent = b;
    }
    
    /** Used to ignore FOCUS event fired by AWT during workspace switching. */
    public boolean isSwitchingWorkspaceIgnoreFocusEvent () {
        return switchingWorkspaceIgnoreFocusEvent;
    }
    
    /** Set flag exitingIDE. Called from NbTopManager. Used to freeze status
     * of winsys during IDE exit to save correct status. */
    public void setExitingIDE (boolean b) {
        exitingIDE = b;
    }
    
    /** If it returns true we should ignore changes in status of winsys. */
    boolean isExitingIDE () {
        return exitingIDE;
    }
    
    /** Set flag wasIDEStarted. Used when opening project during IDE startup.
     * Winsys DataObjects must not be reset during IDE startup. */
    public void setWasIDEStarted (boolean b) {
        wasIDEStarted = b;
    }
    
    /** Used to detect IDE startup during opening project. */
    boolean isWasIDEStarted () {
        return wasIDEStarted;
    }
    
    /** For testing of window system serialization - loading
     * Removes all workspaces so that new one can be created from XML configuration */
    public static void cleanWindowManager() {
        if (defaultInstance == null) {
            getDefault();
        } else {
            // remove and close old workspaces and all their content
            Workspace[] ws = getDefault().getWorkspaces();
            for(int i = 0; i < ws.length; i++) {
                ws[i].remove();
            }
        }
    }
    
    /** XXX - needs to be reimplemented, in layers system reset means following:
     * - setVisible(false) of current workspace, close all windows
     * - delete whole Windows dir in userdir
     * - setVisible(true) of current workspace - will trigger loading
     */
    public static void createFromScratch() {
        if ("full".equals(System.getProperty("netbeans.full.hack"))) {
            return;
        }
        if (defaultInstance == null) {
            getDefault();
        }
    }
    
    /** Indicates whether the window drag and drop is enabled. */
    public static boolean isWindowDnDEnabled() {
        return Boolean.getBoolean("netbeans.winsys.dnd") // NOI18N
        && (Dependency.JAVA_SPEC.compareTo(new SpecificationVersion("1.4")) >= 0); // NOI18N
    }
    
    /** Provides access to the MainWindow of the IDE.
     * This should ONLY be used for:
     * <UL>
     *   <LI>Using the MainWindow as the parent for dialogs</LI>
     *   <LI>Using the MainWindow's positions for positioning of windows
     *       that need to be prepositioned </LI>
     * </UL>
     * @return the MainWindow of the IDE.
     */
    public java.awt.Frame getMainWindow() {
        return MainWindow.getDefault();
    }
    
    /** Called after a current LookAndFeel change to update the IDE's UI
     * Should call updateUI on all opened windows */
    public void updateUI() {
        MainWindow mainWindow = MainWindow.getDefault();
        // update main window first
        SwingUtilities.updateComponentTreeUI(mainWindow);
        mainWindow.pack();
        // update all other opened windows on workspaces
        Workspace[] wArray = getWorkspaces();
        for (int i = 0; i < wArray.length; i++) {
            for (Iterator iter = wArray[i].getModes().iterator(); iter.hasNext(); ) {
                ((ModeImpl)iter.next()).updateUI();
            }
        }
    }
    
    /** Creates new workspace with given name.
     * @param name the name of the workspace
     * @return new workspace
     */
    public Workspace createWorkspace(String name, String displayName) {
        return new WorkspaceImpl(name, displayName);
    }
    
    /** Finds workspace given its name.
     * @param name the name of workspace to find
     * @return workspace or null if not found
     */
    public Workspace findWorkspace(String name) {
        return doFindWorkspace(name, true);
    }
    
    public Workspace findLoadedWorkspace(String name) {
        return doFindWorkspace(name, false);
    }
    
    private Workspace doFindWorkspace(String name, boolean load) {
        if (load) {
            ensureSectionLoaded(PROPERTIES | WORKSPACES);
        }
        for (int i = 0; i < workspaces.length; i++) {
            if (name.equals(workspaces[i].getName()))
                return workspaces[i];
        }
        return null;
    }
    
    /** List of all currenty available workspaces.
     */
    public Workspace[] getWorkspaces() {
        ensureSectionLoaded(PROPERTIES | WORKSPACES);
        return workspaces;
    }
    
    /** List of all currenty available workspaces.
     */
    public Workspace[] getWorkspacesNoLoad() {
        // this method is used in layers.WindowManagerData during creation
        // of workspaces. The getWorkspaces should not be called in that place
        // because it would reenter WindowManagerData again.
        return workspaces;
    }
    
    /** Sets new workspaces.
     * @param workspaces array of new workspaces
     */
    public void setWorkspaces(Workspace[] workspaces) {
        if (Arrays.equals(this.workspaces, workspaces))
            return;
        
        for (int i = 0; i < workspaces.length; i++) {
            if (workspaces[i] == null) {
                throw new IllegalArgumentException("Null worspace at " // NOI18N
                + i + " array index!"); // NOI18N
            }
        }
        
        //Workaround for #20536: Set current to any valid workspace
        //if current was not set yet.
        if ((current == null) && (workspaces.length > 0)) {
            current = workspaces[0];
        }
        
        Workspace[] old = this.workspaces;
        this.workspaces = workspaces;
        changeSupport.firePropertyChange(
        PROP_WORKSPACES, old, this.workspaces
        );
    }
    
    /** @return Current workspace.
     * Can be changed by calling Workspace.activate ()
     */
    public Workspace getCurrentWorkspace() {
        ensureSectionLoaded(PROPERTIES | WORKSPACES);
        if (current == null) {
            // create default empty workspace if setCurrentWorkspace wasn't
            // called yet
            current = new WorkspaceImpl();
        }
        return current;
    }
    
    public Workspace getCurrentWorkspaceNoLoad() {
        return current;
    }
    
    /** Sets current workspace.
     * @return true if succeded, false otherwise (when workspace is not
     * known to this window manager...)
     */
    public void setCurrentWorkspace(Workspace workspace) {
        ensureSectionLoaded(PROPERTIES | WORKSPACES);
        // ensure that code is called from AWT thread
        if (SwingUtilities.isEventDispatchThread()) {
            getWorkspaceSetter(workspace).run();
        } else {
            SwingUtilities.invokeLater(getWorkspaceSetter(workspace));
        }
    }
    
    /** @return runnable which switches workspaces. Should be run in AWT thread
     */
    private Runnable getWorkspaceSetter(final Workspace workspace) {
        return new Runnable() {
            public void run() {
                if ((current != null) && (current.equals(workspace)) &&
                ((WorkspaceImpl) current).isVisible()) {
                    return;
                }
                // check if present
                boolean found = false;
                for (int i = 0; i < workspaces.length; i++) {
                    if (workspaces[i].equals(workspace)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return;
                }
                // perform the change
                StateManager stateMan = StateManager.getDefault();
                if ((stateMan.getState() & StateManager.READY) != 0) {
                    stateMan.setMainState(StateManager.SWITCHING);
                }
                // close all menus before workspace switch
                // (opened popup menus caused some serious problems)
                javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
                WorkspaceImpl old = (WorkspaceImpl)current;
                current = workspace;
                if (old != null) {
                    workspace2Nodes.put(old, getSelectedNodes());
                    old.setVisible(false);
                    
                    ((WorkspaceImpl)current).setPendingShownTcs(
                    old.getShownTcs());
                }
                
                // Divide the setVisible(true) call to load part and visible one
                // to be able manage the shown components the proper way.
                //((WorkspaceImpl)current).setVisible(true);
                ((WorkspaceImpl)current).ensureSectionLoaded(
                WorkspaceImpl.PROPERTIES | WorkspaceImpl.MODES
                );
                
                // Notify also already shown tcs in new workspace, but not
                // shown in the old one.
                Set newShown = ((WorkspaceImpl)current).getShownTcs();
                if(old != null) {
                    Set oldShown = old.getShownTcs();
                    Set pending = old.getPendingShownTcs();
                    
                    oldShown.removeAll(pending);
                    newShown.removeAll(oldShown);
                }
                for(Iterator it = newShown.iterator(); it.hasNext(); ) {
                    ((WindowManagerImpl)WindowManager.getDefault())
                    .componentShowing((TopComponent)it.next());
                }
                
                ((WorkspaceImpl)current).setVisible(true, false);
                ((WorkspaceImpl)current).setPendingShownTcs(Collections.EMPTY_SET);
                
                // Notify shown components from old ws they were hidden,
                // if not shown in the new one.
                if(old != null) {
                    Set oldShown = old.getShownTcs();
                    oldShown.removeAll(((WorkspaceImpl)current).getShownTcs());
                    for(Iterator it = oldShown.iterator(); it.hasNext(); ) {
                        ((WindowManagerImpl)WindowManager.getDefault())
                        .componentHidden((TopComponent)it.next());
                    }
                }
                
                // notify others
                changeSupport.firePropertyChange(PROP_CURRENT_WORKSPACE, old, current);
                setSelectedNodes(workspace2Nodes.get(current));
                //Add buttons for maximized internal frame if there is maximized
                //internal frame at new workspace.
                //Remove buttons for maximized internal frame if there is no maximized
                //internal frame at new workspace.
                if (((WorkspaceImpl) current).isTopMaximizedMode()) {
                    addButtons();
                } else {
                    removeButtons();
                }
                MainWindow.getDefault().updateTitle();
                if ((stateMan.getState() & StateManager.SWITCHING) != 0) {
                    stateMan.setMainState(StateManager.READY);
                }
                return; // true;
            }
        };
    }
    
    /** @return UI mode saved in the definition(wswmrg) file. */
    public int getSavedUIMode() {
        ensureSectionLoaded(PROPERTIES);
        return savedUIMode;
    }
    
    /** Sets ui mode it is obtain from the definition(wswmrg) file. */
    public void setSavedUIMode(int uiMode) {
        savedUIMode = uiMode;
    }
    
    
    /** Attaches given lazy updater. Lazy updater keeps data on persistent
     * storage and can load/dispose data on our demand */
    public void attachUpdater(LazyUpdater updater) {
        this.updater = updater;
    }
    
    /** Loads specified data section if updater exists and if section
     * wasn't loaded already */
    private boolean ensureSectionLoaded(int section) {
        try {
            if (updater == null) {
                // updater will be attached during createInstance in readXML
                PersistenceManager.getDefault().readXML();
                if (updater == null)
                    return false;
            }
            if (!updater.isValid()) {
                return false;
            }
            if ((section & updater.getLoadedSections()) != section) {
                updater.loadDataSection(section);
            }
        } catch (IOException exc) {
            exc.printStackTrace();
            // XXX - notify user (checkPersistenceErrors)
            return false;
        } catch (ClassNotFoundException exc) {
            exc.printStackTrace();
            // XXX - notify user (checkPersistenceErrors)
            return false;
        }
        return true;
    }
    
    /** switches to next workspace as current */
    public synchronized void nextWorkspace() {
        Workspace current = getCurrentWorkspace();
        int len = workspaces.length - 1;
        for (int i = len; i >= 0; i--) {
            if (workspaces[i] == current) {
                if (i == len) i = 0; // cycle it
                else ++i;
                // i will be current
                current = (Workspace) workspaces[i];
                current.activate();
                return;
            }
        }
    }
    
    /** switche to previous workspace */
    public synchronized void previousWorkspace() {
        Workspace current = getCurrentWorkspace();
        int len = workspaces.length - 1;
        for (int i = len; i >= 0; i--) {
            if (workspaces[i] == current) {
                if (i == 0) i = len; // cycle it
                else --i;
                // i will be current
                current = (Workspace) workspaces[i];
                current.activate();
                return;
            }
        }
    }
    
    /** @return selected nodes at 0 and activated at 1 */
    static Value getSelectedNodes() {
        Node[] current = getDefault().getRegistry().getCurrentNodes();
        Node[] activated = getDefault().getRegistry().getActivatedNodes();
        TopComponent tc = getDefault().getRegistry().getActivated();
        return new Value(current, activated, tc);
    }
    
    void setSelectedNodes(Value active) {
        Node[] activated = (active == null ? new Node[0] : active.getActivatedNodes());
        Node[] current = (active == null ? new Node[0] : active.getCurrentNodes());
        Node[][] nodes = new Node[][] { current, activated };
        RegistryImpl rimpl = (RegistryImpl) getRegistry();
        SelectedNodesChangedEvent ev = new SelectedNodesChangedEvent(nodes, null, activated);
        rimpl.selectedNodesChanged(ev);
        ev = new SelectedNodesChangedEvent(nodes, null, current);
        rimpl.selectedNodesChanged(ev);
        
        if (active != null) {
            //      activateComponent(active.activatedTC);
      /*
      TopComponentChangedEvent tcev = new TopComponentChangedEvent(nodes,
                                                                 active.activatedTC,
                                                                 null,
                                                                 1
                                                                );
                                                                rimpl.topComponentActivated(tcev);
       */
        }
    }
    
    //
    // You can add implementation to this class (+firePropertyChange), or implement it in subclass
    // Do as you want.
    //
    
    /** Attaches listener for changes in workspaces
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }
    
    /** Adds top component listener for listening to the changes of
     * the set of top components in the system */
    public void addTopComponentListener(TopComponentListener tcl) {
        synchronized (TC_LISTENERS_LOCK) {
            if (tcListeners == null) {
                tcListeners = new HashSet(5);
            }
            tcListeners.add(tcl);
        }
    }
    
    /** Removes top component listener */
    public void removeTopComponentListener(TopComponentListener tcl) {
        synchronized (TC_LISTENERS_LOCK) {
            if (tcListeners == null) {
                return;
            }
            tcListeners.remove(tcl);
        }
    }
    
    /** @return True if workspace pool was created from scratch or
     * deserialized already, false if not initialized yet.
     */
    public boolean isCreated() {
        return isCreated;
    }
    
    /** Sets created flag. Accessible only for class in this
     * package */
    void setCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }
    
    /** @return true if main window was already positioned */
    public boolean isMainPositioned() {
        return mainPositioned;
    }
    
    /** Sets flag if main window was positioned or not. Called from XML
     * configuration loader. */
    public void setMainPositioned(boolean mainPositioned) {
        this.mainPositioned = mainPositioned;
    }
    
    /** Overrides superclass method, to enhance access modifier. */
    public void componentShowing(TopComponent tc) {
        super.componentShowing(tc);
    }
    
    /** Overrides superclass method, to enhance access modifier. */
    public void componentHidden(TopComponent tc) {
        super.componentHidden(tc);
    }
    
    /** Helper method.
     * @return set of listeners, which is prepared for firing.
     * Can return null if no listeners are attached */
    Set getTcListenersForFiring() {
        if (tcListeners == null)
            return null;
        Set cloned = null;
        synchronized (TC_LISTENERS_LOCK) {
            cloned = (Set)tcListeners.clone();
        }
        return cloned;
    }
    
    public static TopComponentContainer findContainer(String mode) {
        WindowManagerImpl wm = (WindowManagerImpl)getDefault();
        Workspace aWsp;
        TopComponentContainer c = null;
        
        // find selected component of Source Editor window in selected workspace
        aWsp = wm.getCurrentWorkspace();
        if (null != (c = findContainer(mode, aWsp)))
            return c;
        
        // find selected component of Source Editor window in other workspaces
        Workspace allWsp[] = wm.getWorkspaces();
        for(int i = 0; i < allWsp.length; i++) {
            if (allWsp[i] == aWsp)
                continue;
            
            if (null != (c = findContainer(mode, allWsp[i])))
                break;
        }
        
        return c;
    }
    
    /** Utility method, finds container associated with given mode in workspace.
     * This method is supposed to be used for searching components docked in
     * specified mode, thus empty containers are treated as non-existing and are not returned.
     */
    public static TopComponentContainer findContainer(String mode, Workspace wsp) {
        ModeImpl m;
        TopComponentContainer c = null;
        
        m = (ModeImpl)wsp.findMode(mode);
        if (null != m) {
            c = m.getContainerInstance();
            if (null != c && 0 == c.getTopComponents().length)
                c = null;
        }
        
        return c;
    }
    
    /** Utility method, finds unused name of the mode on given workspace
     * based on given string.
     * @param base Base name of the mode. Can be null - in this cas
     * some default string constant will be used
     * @return string representing mode name which is not used yet
     * on current workspace */
    public static String findUnusedModeName(String base, Workspace workspace) {
        return findUnusedModeName(base, workspace, false);
    }
    
    public static String findUnusedModeName(String base, Workspace workspace, boolean forceNumber) {
        // be prepared when base is null
        if (base == null) {
            base = DEFAULT_NAME;
        }
        // don't allow base to be too long, because will act as file name too
        if (base.length() > MAX_MODE_NAME_LENGTH) {
            base = base.substring(0, MAX_MODE_NAME_LENGTH);
        }
        if (!forceNumber && (workspace.findMode(base) == null)) {
            return base;
        }
        // add numbers to the name
        String result = null;
        int modeNumber = 1;
        while (workspace.findMode(result = base + "_" + modeNumber) != null) { // NOI18N
            modeNumber++;
        }
        return result;
    }
    
    /** Activate given top component and register component into history of
     * activated top components.
     */
    public void activateComponent(TopComponent tc) {
        if (tc != null) {
            // notify workspace
            ((WorkspaceImpl)getCurrentWorkspace()).componentActivated(tc);
        }
        // fire info that activated component changed
        Set listeners = getTcListenersForFiring();
        if (listeners != null) {
            for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                ((TopComponentListener)iter.next()).topComponentActivated(
                new TopComponentChangedEvent(this, tc, getCurrentWorkspace(),
                TopComponentChangedEvent.ACTIVATED));
            }
        }
        super.activateComponent(tc);
    }
    
    /** Annotate persistence exception. Exception is added to the exception
     * list, which is displayed at once when whole persistence process
     * (either serialization or deserialization) is about to finish.
     */
    public void annotatePersistenceError(Exception exc, String tcName) {
        if (failedCompsMap == null) {
            failedCompsMap = new HashMap();
        }
        failedCompsMap.put(exc, tcName);
    }
    
    /** Checks for some persistence errors and notifies the user if some
     * persistence errors occured. Shouild be called after serialization
     * and deserialization of window manager.
     */
    public void checkPersistenceErrors(boolean reading) {
        if(failedCompsMap == null || failedCompsMap.isEmpty()) {
            return;
        }

        for(Iterator it = failedCompsMap.keySet().iterator(); it.hasNext(); ) {
            Exception e = (Exception)it.next();
            Object name = failedCompsMap.get(e);
            // create message
            String message = NbBundle.getMessage(WindowManagerImpl.class, 
                    (reading ? "FMT_TCReadError" : "FMT_TCWriteError"),
                    new Object[] {name});
            ErrorManager.getDefault().annotate(e, message);
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
        
        // clear for futher use
        failedCompsMap = null;
    }
    
    
    /** Creates a component manager for given top component.
     * @param c the component
     * @return the manager that handles opening, closing and
     * selecting a component
     */
    protected synchronized org.openide.windows.WindowManager.Component createTopComponentManager(TopComponent c) {
        // create new manager or returng manager being validated
        org.openide.windows.WindowManager.Component result =
        (validatedManager == null) ? new TopComponentManager(c) : validatedManager;
        // clear after each usage
        validatedManager = null;
        return result;
    }
    
    /** Helper; used from TopComponentManager to identify itself
     * during deserialization to achieve asociation with its top component */
    private void setValidatedManager(TopComponentManager tcm) {
        validatedManager = tcm;
    }
    
    private TopComponentManager getValidatedManager() {
        return validatedManager;
    }
    
    /** Finds top component manager for given top component.
     * It's here just to rovide access for classes in this package.
     */
    public static TopComponentManager findManager(TopComponent tc) {
        return (TopComponentManager) /*WindowManager. not compilable by JIKES */findComponentManager(tc);
    }
    
    /** @return Screen size of last deserialized window system or null if
     * information is not available (no window system info was deserialized
     * in this IDE session */
    public Dimension getOldScreenSize() {
        return oldScreenSize;
    }
    
    /** @return Bounds of last deserialized main window or null if
     * information is not available (no window system info was deserialized
     * in this IDE session */
    public Rectangle getOldMainWindowBounds() {
        return oldMainWindowBounds;
    }
    
    /** Delegates serialization manager instance to be serialized
     * instead of window manager impl */
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationReplacer();
    }
    
    /** Instance of this class is serialized instead of WindowManagerImpl.
     * It saves all needed information and deserializesback  to the signleton
     * instance of WindowManagerImpl */
    private static final class SerializationReplacer implements Serializable {
        SerializationReplacer() {}
        static final long serialVersionUID =-8212722893309295268L;
        /** Description of persistent fields */
        private static final String WORKSPACES = "workspaces"; // NOI18N
        private static final String CURRENT_WORKSPACE = "currentWorkspace"; // NOI18N
        private static final String MAIN_WINDOW_BOUNDS = "mainWindowBounds"; // NOI18N
        private static final String UI_MODE = "uiMode"; // NOI18N
        private static final String SCREEN_SIZE = "screenSize"; // NOI18N
        private static final String TABBED_CONTAINER_UI = "tabbedContainerUI"; // NOI18N
        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField(WORKSPACES, java.util.List.class),
            new ObjectStreamField(CURRENT_WORKSPACE, WorkspaceImpl.class),
            new ObjectStreamField(MAIN_WINDOW_BOUNDS, Rectangle.class),
            new ObjectStreamField(UI_MODE, Integer.class),
            new ObjectStreamField(SCREEN_SIZE, Dimension.class),
            new ObjectStreamField(TABBED_CONTAINER_UI, Integer.class),
        };
        
        // deserialized workspaces
        private Workspace[] workspaces;
        // deserialized current workspaces
        private Workspace current;
        // deserialized size of screen
        private Dimension oldScreenSize;
        // deserialized ui mode of the IDE
        private int uiMode;
        // deserialized ContainerUI mode of the IDE
        private int tabbedContainerUI;
        // deserialized ui mode of the IDE
        private Rectangle mainWindowBounds;
        // flag for recognizing first startup
        private boolean isStartup = true;
        
        /** Deserialization of the workspace */
        private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            // obtain WindowManagerImpl whose serialization we manage
            WindowManagerImpl wm = (WindowManagerImpl)WindowManager.getDefault();
            
            //Clean XML files to be compatible with NB 3.2.1 deserialization
            PersistenceManager.getDefault().cleanXML();
            
            StateManager stateMan = StateManager.getDefault();
            stateMan.setMainState(StateManager.DESERIALIZING);
            try {
                ObjectInputStream.GetField gf = ois.readFields();
                //Bugfix #10993, workaround for JDK 1.2.2
                //if (gf.getObjectStreamClass().getField(MAIN_WINDOW_BOUNDS) != null) {
                if (WindowUtils.hasObjectStreamField(gf, MAIN_WINDOW_BOUNDS)) {
                    // current data format
                    java.util.List nwsList = (java.util.List)gf.get(WORKSPACES, null);
                    Workspace[] nworkspaces = (Workspace[])nwsList.toArray(new Workspace[nwsList.size()]);
                    java.util.List wsList = new java.util.ArrayList();
                    //Do not add Browsing workspace
                    Workspace browsing = null;
                    for (int i = 0; i < nworkspaces.length; i++) {
                        if ("Browsing".equals(nworkspaces[i].getName())) { // NOI18N
                            browsing = nworkspaces[i];
                        } else {
                            wsList.add(nworkspaces[i]);
                        }
                    }
                    workspaces = (Workspace[])wsList.toArray(new Workspace[wsList.size()]);
                    current = (Workspace)gf.get(CURRENT_WORKSPACE, null);
                    if (current == browsing) {
                        current = (wsList.size() > 0 ? workspaces[0] : null);
                    }
                    oldScreenSize = (Dimension)gf.get(SCREEN_SIZE, null);
                    uiMode = ((Integer)gf.get(UI_MODE, null)).intValue();
                    mainWindowBounds = (Rectangle)gf.get(MAIN_WINDOW_BOUNDS, null);
                } else {
                    // old data format
                    // read workspaces - first phase
                    int count = ois.readInt();
                    workspaces = new Workspace[count];
                    Workspace browsing = null;
                    for (int i = 0; i < count; i++) {
                        workspaces[i] = (Workspace)ois.readObject();
                        if ("CTL_Workspace_Browsing".equals(workspaces[i].getDisplayName())) { // NOI18N
                            browsing = workspaces[i];
                            Workspace[] nworkspaces = new Workspace[count - 1];
                            System.arraycopy(workspaces, 0, nworkspaces, 0, i);
                            if ( i + 1 < count) {
                                System.arraycopy(workspaces, i + 1, nworkspaces, i, count - i - 1);
                            }
                            
                            count -= 1;
                            i--;
                            workspaces = nworkspaces;
                        }
                        
                    }
                    
                    // read current workspace
                    current = (Workspace)ois.readObject();
                    
                    if (current == browsing) {
                        current = (count > 0 ? workspaces[0] : null);
                    }
                    
                    // bounds of main window
                    mainWindowBounds = (Rectangle)ois.readObject();
                    uiMode = UIModeManager.SDI_MODE;
                }
                if (gf.getObjectStreamClass().getField(TABBED_CONTAINER_UI) != null) {
                    tabbedContainerUI = ((Integer)gf.get(TABBED_CONTAINER_UI, null)).intValue();
                } else {
                    tabbedContainerUI = TabbedContainerUIManager.MODE_DEFAULT;
                }
            } finally {
                isStartup = false;
                stateMan.setMainState(StateManager.READY);
            }
        }
        
        /** Resolves deserialized SerializationReplacer to the singleton
         * instance of WindowManagerImpl */
        private synchronized Object readResolve()
        throws ObjectStreamException {
            // obtain WindowManagerImpl whose serialization we manage
            final WindowManagerImpl wm = WindowManagerImpl.getInstance();
            final StateManager stateMan = StateManager.getDefault();
            stateMan.setMainState(StateManager.DESERIALIZING);
            
            // #24863: Loading of window system (accessing GUI components),
            // needs to be run in AWT Thread.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        initGlobalProperties();
                        // validating workspaces and modes
                        for (int i = 0; i < workspaces.length; i++) {
                            ((WorkspaceImpl)workspaces[i]).validateSelf();
                        }
                        // set new workspaces
                        wm.setWorkspaces(workspaces);
                        // validate top components
                        UIModeManager uiModeManager = UIModeManager.getDefault();
                        if(uiModeManager.getUIMode() != uiMode) {
                            uiModeManager.changeModeFrameTypes();
                        }
                        for (int i = 0; i < workspaces.length; i++) {
                            ((WorkspaceImpl)workspaces[i]).validateData();
                        }
                        wm.setCurrentWorkspace(current);
                    } finally {
                        stateMan.setMainState(StateManager.READY);
                        wm.checkPersistenceErrors(true);
                    }
                    //Save whole window system after default window system from XML and
                    //old window system from NB 3.2.1 user dir was merged.
                    DeferredPerformer.getDefault().putRequest(
                    new DeferredPerformer.DeferredCommand() {
                        public void performCommand(DeferredPerformer.DeferredContext dc) {
                            try {
                                WindowManagerImpl wm2 = (WindowManagerImpl)WindowManager.getDefault();
                                PersistenceManager.getDefault().writeXML();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    },
                    new DeferredPerformer.DeferredContext(null, true)
                    );
                    // reactivate
                }
            });
            
            return wm;
        }
        
        /** Init global properties of window system like main window bounds,
         * UI mode */
        private void initGlobalProperties() {
            WindowManagerImpl wm = (WindowManagerImpl)WindowManager.getDefault();
            wm.mainPositioned = true;
            wm.oldScreenSize = oldScreenSize;
            wm.oldMainWindowBounds = new Rectangle(mainWindowBounds);
            TabbedContainerUIManager.getDefault()
                .setTabbedContainerUI(tabbedContainerUI);
            if (UIModeManager.getDefault().getUIMode() == uiMode) {
                if (oldScreenSize != null) {
                    WindowUtils.convertRectangle(
                    mainWindowBounds, oldScreenSize,
                    Toolkit.getDefaultToolkit().getScreenSize()
                    );
                }
                MainWindow.getDefault().setBounds(mainWindowBounds);
            }
        }
        
        /** Serialization of all workspaces */
        private void writeObject(ObjectOutputStream oos)
        throws IOException {
            // obtain WindowManagerImpl whose serialization we manage
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            StateManager stateMan = StateManager.getDefault();
            stateMan.setMainState(StateManager.SERIALIZING);
            try {
                ObjectOutputStream.PutField pf = oos.putFields();
                pf.put(WORKSPACES, Arrays.asList(wm.workspaces));
                pf.put(CURRENT_WORKSPACE, wm.current);
                pf.put(MAIN_WINDOW_BOUNDS, MainWindow.getDefault().getBounds());
                pf.put(SCREEN_SIZE, Toolkit.getDefaultToolkit().getScreenSize());
                pf.put(UI_MODE, new Integer(UIModeManager.getDefault().getUIMode()));
                pf.put(TABBED_CONTAINER_UI, new Integer(
                    TabbedContainerUIManager.getDefault().getTabbedContainerUI()));
                oos.writeFields();
            } finally {
                stateMan.setMainState(StateManager.READY);
                wm.checkPersistenceErrors(false);
            }
        }
        
    } // end of inner class SerializationReplacer
    
    
    private static boolean isProjectOpen = false;
    private static Object LOCK = new Object();
    
    private static ProjectSwitchListener projectSwitchL = new ProjectSwitchListener();
    
    /** Used from winsys layers to ignore data systems events during project
     * switching. */
    public static boolean isProjectOpen() {
        synchronized (LOCK) {
            return isProjectOpen;
        }
    }
    
    private static void setProjectOpen(boolean b) {
        synchronized (LOCK) {
            isProjectOpen = b;
        }
    }
    
    private static boolean isProjectModuleEnabled = false;
    
    /** Used from winsys layers to ignore data systems events during project
     * switching. */
    public static boolean isProjectModuleEnabled () {
        return isProjectModuleEnabled;
    }
    
    private static void setProjectModuleEnabled (boolean b) {
        isProjectModuleEnabled = b;
    }
    
    private static boolean wasAnyProjectOpen = false;
    
    /** Used from winsys layers to ignore data systems events during project
     * switching. 
     * Set to false when project module is enabled. Set to true when first project
     * is opened during time when project module is enabled. */
    public static boolean wasAnyProjectOpen () {
        return wasAnyProjectOpen;
    }
    
    private static void setAnyProjectOpen (boolean b) {
        wasAnyProjectOpen = b;
    }
    
    /** Saves state of window system when projects are about to switch */
    private static final class LookupResultListener implements LookupListener {
        LookupResultListener() {}
        public void resultChanged (LookupEvent ev) {
            //Check if status of project module was changed
            ModuleInfo curModuleInfo = WindowUtils.findModule
            ("org.netbeans.modules.projects", null, null); // NOI18N
            if (curModuleInfo != null) {
                curModuleInfo.addPropertyChangeListener(projectSwitchL);
                boolean oldValue = isProjectModuleEnabled();
                if (curModuleInfo.isEnabled() && !oldValue) {
                    setAnyProjectOpen(false);
                    setProjectModuleEnabled(true);
                }
                if (!curModuleInfo.isEnabled() && oldValue) {
                    setProjectModuleEnabled(false);
                }
            }
        }
    }
    
    /** Saves state of window system when projects are about to switch */
    private static final class ProjectSwitchListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
                boolean b = ((Boolean) evt.getNewValue()).booleanValue();
                if (b) {
                    setAnyProjectOpen(false);
                }
                setProjectModuleEnabled(b);
            }
            try {
                if (SessionManager.PROP_CLOSE.equals(propName)) {
                    // prevent winsys from processing changes
                    setProjectOpen(false);
                    
                    // XXX: bugfix #18560, PersistanceManager.writeXML is called by projects
                    // whenever the project needs to be saved, this ugly dependency will be
                    // removed after the winsys will persist its state automaticaly after each change
                    //                    PersistenceManager pmanager = ((WindowManagerImpl)WindowManager.getDefault()).persistenceManager();
                    //                    pmanager.writeXML();
                    
                    AWT performer = new AWT(false);
                    if (SwingUtilities.isEventDispatchThread()) {
                        // project switch is called on AWT during the IDE exit only
                        performer.run();
                    } else {
                        // wait for all deferred tasks
                        while (DeferredPerformer.getDefault().isProcessing()) {
                            Thread.currentThread().sleep(200);
                        }
                        SwingUtilities.invokeAndWait(performer);
                    }
                }
                if (SessionManager.PROP_OPEN.equals(propName)) {
                    // tricky, posting to RP ensures that winsys will be opened
                    // after the RP finishes task perfoming projects module uninstallation,
                    // if project switch was caused by projects uninst.
                    RequestProcessor.postRequest(new Runnable() {
                        public void run() {
                            final AWT performer = new AWT(true);
                            //                            if (java.awt.EventQueue.isDispatchThread()) {
                            //                                performer.run();
                            //                            } else {
                            java.awt.EventQueue.invokeLater(performer);
                            //                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                ((WindowManagerImpl)WindowManager.getDefault()).annotatePersistenceError(e, null);
                ((WindowManagerImpl)WindowManager.getDefault()).checkPersistenceErrors(false);
            } catch (java.lang.reflect.InvocationTargetException e) {
                ((WindowManagerImpl)WindowManager.getDefault()).annotatePersistenceError(e, null);
                ((WindowManagerImpl)WindowManager.getDefault()).checkPersistenceErrors(false);
            }
        }
        
        private static class AWT implements Runnable {
            private final boolean open;
            public AWT(boolean open) {
                this.open = open;
            }
            public void run() {
                if (open) {
                    run_open();
                } else {
                    run_close();
                }
            }
            private void run_close() {
                PersistenceManager pm = PersistenceManager.getDefault();
                pm.resetTCPairs();
                
                // hide workspace before the closeModes() is called on it,
                // this call resets dominance
                WindowManagerImpl wmi = WindowManagerImpl.getInstance();
                WorkspaceImpl wi = (WorkspaceImpl) wmi.getCurrentWorkspaceNoLoad();
                if (wi != null) {
                    wi.setVisible(false);
                    //Bugfix #22380: Reset top maximized mode to null to remove
                    //buttons from menu bar.
                    wi.setTopMaximizedMode(null);
                    //Bugfix #22794: Set maximized status of desktop to false
                    //to avoid keeping maximized state when project is switched.
                    wi.desktopPane().setMaxMode(false);
                }
                
                //Bugfix #16511 Close all modes in all workspaces
                Workspace[] ws = wmi.getWorkspacesNoLoad();
                if (ws != null) {
                    for(int i = 0; i < ws.length; i++) {
                        ((WorkspaceImpl) ws[i]).closeModes();
                        ((WorkspaceImpl) ws[i]).setWasVisible(false);
                    }
                }
            }
            /** After the project is opened workspace layout is validated.
             * This affects definition of dominance of modes living in the workspace.
             * Part of bugfix #12152.
             */
            private void run_open() {
                WindowManagerImpl wmi = (WindowManagerImpl)WindowManager.getDefault();
                //Do not reset during IDE startup.
                if (!wmi.isWasIDEStarted()) {
                    setProjectOpen(true);
                    setAnyProjectOpen(true);
                    wmi.setWasIDEStarted(true);
                    return;
                }
                PersistenceManager pm = PersistenceManager.getDefault();
                
                FileObject f = pm.getWindowManagerFolder();
                DataFolder d = DataFolder.findFolder(f);
                invalidate(d);
                
                // tricky, this piece of code forces WindowManagerData to fire
                // PROP_CHILDREN property change, which in turn refreshes
                // whole hierarchy of loaded winsys objects
                DataObject ch [] = d.getChildren();
                setProjectOpen(true);
                setAnyProjectOpen(true);
                try {
                    d.setOrder(ch);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
                
                WorkspaceImpl wi = (WorkspaceImpl) wmi.getCurrentWorkspace();
                if (wi != null) {
                    wi.setVisible(true);
                }
            }

            /** Resets folder and subfolders. */
            private static void invalidate(DataFolder d) {
                resetDataObject(d);

                DataObject[] ch = d.getChildren();
                for (int i = 0; i < ch.length; i ++) {
                    if (ch[i] instanceof DataFolder) {
                        invalidate((DataFolder)ch[i]);
                    } else {
                        resetDataObject(ch[i]);
                    }
                }
            }
            
            private static void resetDataObject(DataObject dobj) {
                ResetCookie rc = (ResetCookie)dobj.getCookie(ResetCookie.class);
                if(rc != null) {
                    rc.reset();
                }
            }
        } // end of AWT class
    } // end of ProjectSwitchListener
    
    /** The manager that handles operations on a top component.
     * It is always attached to a TopComponent via one-to-one
     * relationship.
     */
    public static final class TopComponentManager extends ComponentAdapter
    implements WindowManager.Component,
    DeferredPerformer.DeferredCommand,
    Runnable {
        /** The constants for properties of managed top component */
        public static final String PROP_ACTIVATED_NODES = "activatedNodes"; // NOI18N
        public static final String PROP_NAME = "name"; // NOI18N
        public static final String PROP_ICON = "icon"; // NOI18N
        
        /** client properties for tab policy types of top component
         * see JComponent.put/getClientProperty() for usage */
        public static final String TAB_POLICY = "TabPolicy";
        /** don't show tab for component when alone (only one visible docked)
         * in the mode */
        public static final String HIDE_WHEN_ALONE = "HideWhenAlone";
        
        /** Set of workspaces where top component is opened. */
        Set whereOpened;
        /** Agregation of the top component which we're trying to manage */
        TopComponent component;
        /** top component we manage in hlaf-way deserialized form,
         * used only during deserialization */
        private NbMarshalledObject marshalledComponent;
        /** helper variable, holds top component's name, used when
         * reporting failure of deserialization of the top copmponent */
        String componentName;
        
        /** Icon of the component we manage */
        Image icon;
        /** Activated nodes of the component we manage */
        Node[] nodes;
        /** Nodes which will be set as activated when the component becomes non-null */
        Node[] tempNodes;
        /** asociation with the window manager */
        WindowManagerImpl wm;
        /** Support for property changes */
        PropertyChangeSupport changeSupport;
        /** helper flag, holds current state of validation during
         * deserialization */
        private int innerState = READY;
        // Bugfix #15006 12 Sep 2001 by Jiri Rechtacek
        /** defered change of a name */
        private RequestProcessor.Task deferNameChange;
        /** constant for all-right state */
        private static final int READY = 1;
        /** constant for state when validation was not performed yet */
        private static final int INVALID = 2;
        /** constant for state when deserialization of tc completely failed */
        private static final int FAILED = 4;
        
        /** manager of versioned serialization */
        private static VersionSerializator serializationManager;
        
        static final long serialVersionUID =5669852754182098300L;
        /** Constructs new top component manager. Allow only classes in package
         * to construct us
         * @param component Managed top component.
         */
        TopComponentManager(TopComponent component) {
            this.component = component;
            whereOpened = new HashSet(5);
            initialize();
        }
        
        /** Initialization of this manager, called also when deserializaing */
        private void initialize() {
            this.wm = (WindowManagerImpl)WindowManager.getDefault();
            changeSupport = new PropertyChangeSupport(this);
        }
        
        /** Opens a component on current workspace. If main window is
         * not visible, open action is delayed until it's open.
         */
        public void open() {
            open(null);
        }
        
        /** Opens a component on given workspace. If window system is
         * in inconsistent state (main window is not visible, serializing etc..)
         * open action is delayed.
         * @workspace the workspace where to open managed top component
         */
        public void open(Workspace workspace) {
            if (DeferredPerformer.getDefault().canImmediatelly()) {
                // immediate open (replan to event thread if needed)
                final Workspace realWorkspace =
                (workspace == null) ? wm.getCurrentWorkspace() : workspace;
                if (java.awt.EventQueue.isDispatchThread()) {
                    doOpen(realWorkspace);
                } else {
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            doOpen(realWorkspace);
                        }
                    });
                }
            } else {
                // deferred open
                DeferredPerformer.getDefault().putRequest(this, workspace);
            }
        }
        
        /** Implementation of DeferredPerformer.DeferredCommand interface.
         * Actually opens managed top component. */
        public void performCommand(DeferredPerformer.DeferredContext context) {
            component.open((Workspace)(context.getData()));
        }
        
        /** Opens a component on given workspace.
         * If given workspace differs from current, component will
         * be visible only after switching to given workspace.
         * @workspace the workspace where to open managed top component
         */
        private void doOpen(Workspace workspace) {
            Mode mode = workspace.findMode(component);
            
            if (!whereOpened.add(workspace)) {
                // if the top component is alredy opened and ICONIFIED, the NORMAL state of the frame will be set
                if (mode != null && ((ModeImpl) mode).getFrameState() == FrameType.ICONIFIED)
                    ((ModeImpl) mode).setFrameState(FrameType.NORMAL);
                return;
            };
            if (mode == null) {
                // create new mode for given tc
                // important bugfix - create properly named mode even
                // for top components with null name
                String modeName = wm.findUnusedModeName(component.getName(), workspace);
                mode = ((WorkspaceImpl)workspace).createMode(
                modeName, modeName, null, true
                );
            }
            mode.dockInto(component);
            // inform top component's subclasses
            if (whereOpened.size() == 1) {
                wm.componentOpenNotify(component);
            }
            // then let others know that top component was opened...
            Set listeners = wm.getTcListenersForFiring();
            if (listeners != null) {
                for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                    ((TopComponentListener)iter.next()).topComponentOpened(
                    new TopComponentChangedEvent(this, component, workspace,
                    TopComponentChangedEvent.OPENED)
                    );
                }
            }
        }
        
        /** @return true if managed top component is currently opened
         * on some workspace, false otherwise */
        public boolean isOpened() {
            return whereOpened.size() > 0;
        }
        
        /** @return true if managed top component is currently opened
         * on given workspace, false otherwise */
        public boolean isOpened(Workspace workspace) {
            return whereOpened.contains(workspace);
        }

        /** Takes over opened status on all workspaces from given manager */
        public void takeOpenedStatus (TopComponentManager otherTcm) {
            whereOpened = new HashSet(otherTcm.whereOpened());
        }
        
        /** Closes the component on given workspace.
         * @param workspace the workspace where managed top component
         * should be closed.
         */
        public void close(Workspace workspace) {
            switch (component.getCloseOperation()) {
                case TopComponent.CLOSE_LAST:
                    if (isOpened(workspace))
                        doClose(workspace);
                    break;
                case TopComponent.CLOSE_EACH:
                    // special mode for editor etc...
                    // close on all workspaces
                    Workspace[] workspaces = wm.getWorkspaces();
                    for (int i = 0; i < workspaces.length; i++) {
                        if (isOpened(workspaces[i]))
                            doClose(workspaces[i]);
                    }
                    break;
            }
        }
        
        /** The component requests focus. Works only on opened component.
         * When the request comes too early, implemetation waits
         * till component is shown.
         */
        public void requestFocus() {
            //09 Feb 2001 by Marek Slama
            //Bug fix #9445
            //Call of requestFocus replanned to AWT to make sure that
            //it is performed AFTER open.
            DoRequest doRequestFocus = new DoRequest(DoRequest.REQUEST_FOCUS);
            if (java.awt.EventQueue.isDispatchThread()) {
                doRequestFocus.run();
            } else {
                javax.swing.SwingUtilities.invokeLater(doRequestFocus);
            }
        }
        
        /** Set this component visible but not selected or focused if possible.
         * If focus is in other container (multitab) or other pane (split) in
         * the same container it makes this component only visible eg. it selects
         * tab with this component.
         * If focus is in the same container (multitab) or in the same pane (split)
         * it has the same effect as requestFocus().
         */
        public void requestVisible() {
            DoRequest doRequestVisible = new DoRequest(DoRequest.REQUEST_VISIBLE);
            if (java.awt.EventQueue.isDispatchThread()) {
                doRequestVisible.run();
            } else {
                javax.swing.SwingUtilities.invokeLater(doRequestVisible);
            }
        }
        
        /** @return the mode which belongs to managed top component on
         * CURRENT workspace.
         */
        Mode getMode() {
            return wm.getCurrentWorkspace().findMode(component);
        }
        
        /** Actually perform the work of closing ther managed component on
         * given workspace, without checking. */
        void doClose(Workspace workspace) {
            ModeImpl mode = (ModeImpl)workspace.findMode(component);
            whereOpened.remove(workspace);
            if (mode != null) {
                mode.close(component);
            }
            // inform subclasses
            if (!isOpened()) {
                wm.componentCloseNotify(component);
            }
            // let others know that top component was closed...
            Set listeners = wm.getTcListenersForFiring();
            if (listeners != null) {
                for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                    ((TopComponentListener)iter.next()).topComponentClosed(
                    new TopComponentChangedEvent(this, component, workspace,
                    TopComponentChangedEvent.CLOSED)
                    );
                }
            }
        }
        
        /** Getter for set of activated nodes
         * @return activated nodes for the component
         */
        public Node[] getActivatedNodes() {
            return nodes;
        }
        
        /** Setter for set of activated nodes for the component
         * @param nodes activated nodes for this component
         */
        public void setActivatedNodes(Node[] nodes) {
            if (component == null) {
                // store the nodes to a temporary variable and set them properly
                // when a non-null value of the component is set
                tempNodes = nodes;
                return;
            }
            if (Arrays.equals(this.nodes, nodes))
                return;
            Node[] old = this.nodes;
            this.nodes = nodes;
            // notify all that are interested...
            changeSupport.firePropertyChange(PROP_ACTIVATED_NODES, old, nodes);
            Set listeners = wm.getTcListenersForFiring();
            if (listeners != null) {
                for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                    ((TopComponentListener)iter.next()).selectedNodesChanged(
                    new SelectedNodesChangedEvent(this, component, nodes));
                }
            }
        }
        
        /** Notify about name change.
         */
        public void nameChanged() {
            // component can be null for a while during deserialization
            if (component != null) {
                // Bugfix #15006 12 Sep 2001 by Jiri Rechtacek
                RequestProcessor.Task task = deferNameChange;
                if (task == null) {
                    // but create a processor for some change will arrive soon
                    deferNameChange = RequestProcessor.getDefault().post(this, 200, Thread.MIN_PRIORITY);
                } else {
                    // if change arrives while a processor is there, wait with update
                    task.schedule(200);
                }
                // end of bugfix #15006
            }
        }
        
        /** Fires change of a name.
         */
        public void run() {
            changeSupport.firePropertyChange(PROP_NAME, null, component.getName());
            // Bugfix #15006 12 Sep 2001 by Jiri Rechtacek
            // clear the task to signal that next change should be done imediatelly
            deferNameChange = null;
        }
        
        /** Sets the icon of the top component which will be used for
         * component representaion on the screen.
         * @param icon New components' icon.
         */
        public void setIcon(final Image icon) {
            if (((this.icon != null) && this.icon.equals(icon)) ||
            ((this.icon == null) && (icon == null)))
                return;
            Image old = this.icon;
            this.icon = icon;
            changeSupport.firePropertyChange(PROP_ICON, old, this.icon);
        }
        
        public Image getIcon() {
            return icon;
        }
        
        /** Getter for managed component
         * @return managed component
         */
        public TopComponent getComponent() {
            return component;
        }
        
        /** @return the set of workspaces where managed component is open */
        public Set whereOpened() {
            return whereOpened;
        }
        
        /** Adds listener for listening to top component property changes.
         */
        public void addPropertyChangeListener(PropertyChangeListener pchl) {
            changeSupport.addPropertyChangeListener(pchl);
        }
        
        /** Removes property change listener.
         */
        public void removePropertyChangeListener(PropertyChangeListener pchl) {
            changeSupport.removePropertyChangeListener(pchl);
        }
        
        /** Serialization */
        private Object writeReplace()
        throws ObjectStreamException {
            NbMarshalledObject marshalledTc = null;
            try {
                marshalledTc = new NbMarshalledObject(component);
                // testing
                // throw new Exception();
            } catch (Exception exc) {
                wm.annotatePersistenceError(exc, component.getName());
                // write null as a marker that there was serialization problem
                return null;
            }
            return new SerializationReplacer(componentName, marshalledTc);
        }
        
        /** Called when first phase of WS deserialization is done.
         * Deserialization of managed TC is finished here.
         */
        public boolean validateData() {
            // deserialize only once, then only return succes or failure
            // status
            if (innerState != INVALID) {
                return innerState == READY;
            }
            // component written badly, consider it a failure
            if (marshalledComponent == null) {
                innerState = FAILED;
                return false;
            }
            // Actually deserializes top component.
            // Uses ugly hack - redundant TopComponent<init>.getManager() call
            // to enforce wm.createTopComponentManager() to be called,
            // in which we can re-asociate deserialized top component
            // with this manager.
            // This is UGLY implementation, based on the fact that there's
            // no way to set new manager for top component and we want to
            // keep the Open API unchanged if possible
            try {
                wm.setValidatedManager(this);
                component = (TopComponent)marshalledComponent.get();
                // component can resolve itself to null,
                // be prepared to such situation
        /*if (component != null) {
          component.getActivatedNodes();
        }*/
                innerState = (component == null) ? FAILED : READY;
                if ((component != null) && (tempNodes != null)) {
                    setActivatedNodes(tempNodes);
                    tempNodes = null;
                }
            } catch(java.io.InvalidObjectException ioe) {
                // #25966: Component doesn't want to be deserialized.
                innerState = FAILED;
                // TODO Find out why there are null messages.
                String msg = ioe.getMessage();
                if(msg != null) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "[WinSys] Component is not deserializable. Reason: " + msg); // NOI18N
                }
                return false;
            } catch (Exception exc) {
                // do catch all exceptions, because exceptions
                // during deserialization of one top component
                // shoudn't break whole window system
                innerState = FAILED;
                wm.annotatePersistenceError(exc, componentName);
                return false;
            } finally {
                // ensure that manager is not set anymore
                wm.setValidatedManager(null);
            }
            return innerState == READY;
        }
        
        /** Utility method, creates, fills and returns instance of top component
         * manager */
        private static TopComponentManager createTCM(String componentName,
        NbMarshalledObject marshalledComponent) {
            TopComponentManager result = new TopComponentManager(null);
            result.componentName = componentName;
            result.marshalledComponent = marshalledComponent;
            result.innerState = INVALID;
            return result;
        }
        
        /** Used to do requestFocus or requestVisible in AWT thread.
         * Necessary when open is done in AWT thread too to make sure correct order
         * first open, then requestFocus.
         * Called from TopComponentManager.requestFocus and TopComponentManager.requestVisible
         */
        private class DoRequest extends Object implements Runnable {
            private static final int REQUEST_FOCUS = 0;
            private static final int REQUEST_VISIBLE = 1;
            /** Actione to be performed */
            private int action;
            
            DoRequest(int action) {
                this.action = action;
            }
            
            private void requestFocus() {
                if (component.isDisplayable()) {
                    // fulfill only if the request is still valid,
                    // be aware not to steal focus from modal dialog
                    ModeImpl modeImpl = (ModeImpl) getMode();
                    if ((modeImpl != null) && (component != null) &&
                    (NbPresenter.currentModalDialog == null)) {
                        modeImpl.requestFocus(component);
                    }
                }
            }
            
            private void requestVisible() {
                if (component.isDisplayable()) {
                    // fulfill only if the request is still valid,
                    // be aware not to steal focus from modal dialog
                    ModeImpl modeImpl = (ModeImpl) getMode();
                    if ((modeImpl != null) && (component != null) &&
                    (NbPresenter.currentModalDialog == null)) {
                        modeImpl.requestVisible(component);
                    }
                }
            }
            
            public void run() {
                if (!isOpened()) {
                    return;
                }
                if (action == REQUEST_FOCUS) {
                    requestFocus();
                } else if (action == REQUEST_VISIBLE) {
                    requestVisible();
                }
            }
        }
        
        /** Class that acts as serialization manager for component manager implementation */
        private static final class SerializationReplacer implements Serializable {
            /** Unique ID */
            static final long serialVersionUID =-9009124438910567345L;
            /** Description of serializable fields */
            private static final String NAME = "name"; // NOI18N
            private static final String MARSHALLED_COMPONENT = "marshalledComponent"; // NOI18N
            private static final ObjectStreamField[] serialPersistentFields = {
                new ObjectStreamField(NAME, String.class),
                new ObjectStreamField(MARSHALLED_COMPONENT, NbMarshalledObject.class),
            };
            
            private String componentName;
            private NbMarshalledObject marshalledComponent;
            
            SerializationReplacer(String componentName, NbMarshalledObject marshalledComponent){
                this.componentName = componentName;
                this.marshalledComponent = marshalledComponent;
            }
            
            /** Serialization of one top component, driven by its manager */
            private void writeObject(ObjectOutputStream oos)
            throws IOException {
                // write fields
                ObjectOutputStream.PutField pf = oos.putFields();
                pf.put(NAME, componentName);
                pf.put(MARSHALLED_COMPONENT, marshalledComponent);
                oos.writeFields();
            }
            
            /** Deserialization of the workspace */
            private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
                ObjectInputStream.GetField gf = ois.readFields();
                componentName = (String)gf.get(NAME, null);
                marshalledComponent = (NbMarshalledObject)gf.get(MARSHALLED_COMPONENT, null);
            }
            
            /** Resolves deserialized SerializationReplacer to the singleton
             * instance of WindowManagerImpl */
            private synchronized Object readResolve()
            throws ObjectStreamException {
                return createTCM(componentName, marshalledComponent);
            }
            
        } // end of SerializationReplacer
        
        
        /******** Old serialization, must be here for compatibility with 3.1 and older versions */
        
        /** Accessor to the versioned serialization manager */
        private VersionSerializator serializationManager() {
            if (serializationManager == null) {
                serializationManager = createSerializationManager();
            }
            return serializationManager;
        }
        
        /** Creates new serialization manager filled with our versions */
        private static VersionSerializator createSerializationManager() {
            VersionSerializator result = new VersionSerializator();
            result.putVersion(new Version1());
            return result;
        }
        
        /** Basic version of persistence for mode implementation.
         * Method assignData(modeImpl) must be called prior to serialization */
        private static final class Version1
        implements DefaultReplacer.ResVersionable {
            Version1() {}
            
            /* identification string */
            public static final String NAME = "Version_1.0"; // NOI18N
            
            /** variables of persistent state of the tc manager */
            String componentName;
            NbMarshalledObject marshalledComponent;
            /** asociation with outerclass, used when writing */
            TopComponentManager tcm;
            /** set of workspaces where managed tc is opened */
            transient HashSet whereOpened;
            
            /** Identification of the version */
            public String getName() {
                return "Version_1.0"; // NOI18N
            }
            
            /** read the data of the version from given input */
            public void readData(ObjectInput in)
            throws IOException, ClassNotFoundException {
                // read the fields
                componentName = (String)in.readObject();
                marshalledComponent = (NbMarshalledObject)in.readObject();
            }
            
            /** write the data of the version to given output */
            public void writeData(ObjectOutput out)
            throws IOException {
                // should be never called
                throw new InternalError();
            }
            
            /** Resolves this object to top component manager instance again */
            public Object resolveData()
            throws ObjectStreamException {
                return createTCM(componentName, marshalledComponent);
            }
            
        } // end of Version1 inner class
        
        /** Implementation of persistent access to our version serializator */
        private static final class VSAccess implements DefaultReplacer.Access {
            
            /** serialVersionUID */
            private static final long serialVersionUID = -6484558550904999459L;
            
            /** version serializator, used only during writing */
            transient VersionSerializator vs;
            
            public VSAccess(VersionSerializator vs) {
                this.vs = vs;
            }
            
            public VersionSerializator getVersionSerializator() {
                return (vs == null) ? createSerializationManager() : vs;
            }
            
        } // end of VSAccess inner class
        
    } // end of TopComponentManager inner class
    
}
