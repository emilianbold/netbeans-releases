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

import java.util.*;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Container;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.URL;

import javax.swing.SwingUtilities;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;

import org.openide.windows.*;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import org.netbeans.core.windows.util.*;
import org.netbeans.core.windows.frames.DesktopFrameTypeImpl;
import org.netbeans.core.windows.frames.TopComponentContainer;
import org.netbeans.core.windows.frames.MultiTabbedContainerImpl;
import org.netbeans.core.windows.frames.FrameType;
import org.netbeans.core.windows.frames.InternalFrameTypeImpl;
import org.netbeans.core.windows.frames.FrameTypeEvent;
import org.netbeans.core.windows.frames.WindowTypesManager;
import org.netbeans.core.windows.frames.FrameTypeListener;
import org.netbeans.core.windows.frames.DesktopPane;
import org.netbeans.core.windows.frames.PerimeterLayout;
import org.netbeans.core.windows.frames.FrameTypeSupport;
import org.openide.awt.Actions;
import org.openide.util.WeakSet;
import java.awt.Dimension;

/** This class is an implementation of Mode interface.
* It's responsible for managing top component container.
* This implementation supports switching of various types of
* top component containers.
*
* @author Dafe Simonek
*/
public final class ModeImpl implements Comparable, Mode, FrameTypeListener, ComponentListener,
                                        DeferredPerformer.DeferredCommand, StateManager.StateListener {

    /** serial version UID */
    static final long serialVersionUID = 2721445375634234372L;

    /** Single constant mode */
    public static final ModeImpl SINGLE = new ModeImpl("SingleNewMode", null, null, false, null);
    /** Multi tabbed constant mode */
    public static final ModeImpl MULTI = new ModeImpl("MultiNewMode", null, null, false, null);
    /** Split constant mode */
    public static final ModeImpl SPLIT = new ModeImpl("SplitNewMode", null, null, false, null);
    
    /** Top components changes property name */
    public static final String PROP_TOP_COMPONENTS = "topComponents"; // NOI18N
    public static final String PROP_FRAME_INSTANCE = "frameInstance"; // NOI18N
    public static final String PROP_CONTAINER_INSTANCE = "containerInstance"; // NOI18N
    public static final String PROP_CONSTRAINTS = "containerInstance"; // NOI18N
    
    /** id of properties section of data */
    public static final int PROPERTIES = 1;
    /** id of modes section of data */
    public static final int COMPONENTS = 2;
    /** id of component ids section of data */
    public static final int COMPONENT_IDS = 4;

    /** constants for naming types of mode */
    public static final String NAMING_TYPE = "NamingType";
    /** Declares the component to be in single mode in both SDI and MDI. */
    public static final String BOTH_ONLY_COMP_NAME = "BothOnlyCompName";
    /** Declares the component to be in single mode in SDI only. */
    public static final String SDI_ONLY_COMP_NAME = "SDIOnlyCompName";
    /** Declared the component to be in single mode in MDI only. */
    public static final String MDI_ONLY_COMP_NAME = "MDIOnlyCompName";

    /** Icon of the mode */
    private URL icon;
    /** icon as an image, created from URL */
    private Image iconImage;
    /** Programmatic unique name of the mode */
    private String name;
    /** Human presentable name of the mode */
    private String displayName;
    /** Bundle key human presentable description of the mode is taken from. */
    private String description;
    /** The bounds of the mode - should be kept synchronized with
    * top component container when container exists and visible */ 
    private Rectangle bounds;
    /** true if this mode was created by user, not system */
    private boolean userDefined;
    /** The workspace which this mode belongs to */
    private Workspace workspace;
    /** Name of workspace which this mode belongs to.
    * Used only during deserialization when delayed validation is needed */
    private String workspaceName;
    /** Composited container for top components. */
    private TopComponentContainer tcc;
    /** Composited frame type which represents this mode as frame of some type.
     * Can be null if frame was not used yet */
    private FrameType frame;
    /** Flag if frame is selected or not. */
    private boolean active = false;
    /** String representation of asociated container type */
    private String containerType;
    /** String representation of asociated container type */
    private String frameType;
    /** State of associated frame type as it was deserialized or the last state of
     * associated frame, which  was destroyed. It is used in setVisible, when mode
     * is shown first time. Value of frameTypeState is also serialized, when mode
     * doesn't have frame associated.
     */
    private int frameTypeState = -1;
    
    /** Layout constraints, null by default */
    private Object constraints;
    /** Current constraints, helper variable for residing frame out and back. */
    private Object currnetConstraints;
    
    /** List of top component classes which names should be ignored in certain
    * circumstances */
    private List ignoredTcList;
    /** Weak map which holds weak references to the top components which
    * are docked in this mode but closed, together with their contexts,
     * which are specified by ClosedTCContext objects 
     * <TopComponent, ClosedTCContext>
     */
    private WeakHashMap closedComponents;
    /** asociated property change support for firing property changes */
    private PropertyChangeSupport changeSupport;
    /** helper variable, true when asociated top component container
    * is showing on the screen, false otherwise */
    private boolean showing;
    /** helper variable, true when there is some pending focus request */
    private boolean deferredFocusRequest;
    /** helper variable, holds top component as context of pending
    * focus request */
    private TopComponent compToReceiveFocus;
    /** Listener to the changes of the name of top component */
    private NameListener nameListener;
    /** Relative bounds stored till main window is displayed */
    private Rectangle relativeBounds;
    /** Map of areas, constraint is used as key */
    private HashMap areas;
    /** manager of versioned serialization */
    private static VersionSerializator serializationManager;
    /** Asociated lazy updater, we ask it to load/store sections of
     * our data */
    private LazyUpdater updater;
    /** true when mode is "hidden". Mode can be hidden only if it's empty
     * or all components are closed */
    private boolean hidden = true;
    /** bundle from which localized display name is read */
    private String nameBundle;
    /** bundle from which localized description is read */
    private String descriptionBundle;
    /** Helper flag variable, true if display name is taken from the bundle */
    private boolean fromBundle;
    /** flag that affects closing policy */
    private boolean closeOnlyInMemory;
    /** true when bounds was already converted from relative to absolute bounds */
    private boolean boundsConverted;
    /** Array of top component Ids found in mode folder */
    private String[] topComponentIds;
    /** Set of shown <code>TopComponent</code>'s which are kept if frame
     * is in iconified state, otherwise is <code>null</code>. */
    private Set iconifiedShownTcs;
    
    /** Flag is set to true when mode is changed and should be saved */
    private boolean isChanged = false;
    /** Flag is set to false when module owning this mode is dsiabled 
     * and this mode should not be saved anymore. */
    private boolean isValidForSaving = true;
    
    private Container origContentPane;
    
    /** Counter of pending dockInto requests. */
    private int pendingDockInto = 0;
    
    /** Construct new mode with given properties
     * @deprecated Use constructor without containerType parameter
     */
    public ModeImpl (String name, String displayName, URL icon,
                     int containerType, boolean userDefined,
                     Workspace workspace) {
        this(name, displayName, icon, userDefined, workspace);
    }
    
    /** Construct new mode with given properties
     */
    public ModeImpl (String name, String displayName, URL icon,
                     boolean userDefined, Workspace workspace) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.workspace = workspace;
        this.userDefined = userDefined;
        
        initialize();
    }
    

    /** Creates new mode as a shallow copy of original mode on
    * given workspace. New mode will have the same characterists,
    * (displayname, icon, container type, bounds).
    * Contained components are taken from original.
    * The name can differ (name uniquennes is ensured automatically).
    */
    public ModeImpl (Workspace workspace, ModeImpl original) {
        this(original.getName(), original.getDisplayNameXML(), original.getIconURL(),
             original.isUserDefined(), workspace);
        // set unigue name
        if (workspace.findMode(name) != null) {
            int i = 1;
            while (workspace.findMode(name = original.name + "_" + i++) != null); // NOI18N
        }
        // other properties
        fromBundle = original.isFromBundle();
        nameBundle = original.getNameBundle();
        setBounds(new Rectangle(original.bounds));
        setContainerType(original.getContainerType());
        setFrameType(original.getFrameType());
        setHidden(original.isHidden());
        // beware, constraints may conflict
        WindowUtils.changeModeConstraints(this, original.constraints, true);

        //Add mode to workspace before top components are opened.
        ((WorkspaceImpl)workspace).addMode((Mode)this);
        
        TopComponent[] tcs = original.getTopComponents();
        TopComponentContainer tcContainer = original.tcc;
        for (int i = 0; i < tcs.length; i++) {
            Object constr = tcContainer == null ? null : tcContainer.getConstraints(tcs[i]);
            if (canDock(tcs[i])) {
                if (this.dockInto(tcs[i], constr)) {
                    //Open TCs only if Mode is NOT hidden
                    if (!original.isHidden()) {
                        tcs[i].open(workspace);
                    }
                }
            }
        }        
    }
    
    /** Initialization, called from constructors and deserialization */
    private void initialize () {
        bounds = new Rectangle();
        changeSupport = new PropertyChangeSupport(this);
        
        StateManager stateManager =StateManager.getDefault();
        //Track when main window gets visible it it is not visible already
        if ((stateManager.getState() & StateManager.VISIBLE) == 0) {
            stateManager.addStateListener(this);
        }
    }
    
    /** Returns true if mode was changed and should be saved.
     * @return value of flag isChanged
     */
    public boolean isChanged() {
        return isChanged;
    }
    
    /** Sets value of flag isChanged. It also posts task for saving of winsys.
     * @param isChanged new value of flag
     */
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
        if (isChanged) {
            WindowManagerImpl.getInstance().postSaving();
        }
    }
    
    /** Returns true if mode can be saved.
     * @return value of flag isValidForSaving
     */
    public boolean isValidForSaving() {
        return isValidForSaving;
    }
    
    /** Sets value of flag isValidForSaving. It is set to false when module
     * owning this mode is disabled. It is called only from ModeData.
     * @param isValidForSaving new value of flag
     */
    public void setValidForSaving(boolean isValidForSaving) {
        this.isValidForSaving = isValidForSaving;
    }
    
    /** Saves this mode and its components.
     */
    public void save() throws IOException {
        try {
            if ((updater != null) && isChanged() && isValidForSaving()) {
                updater.save();
            }
        } finally {
            if (isChanged()) {
                setChanged(false);
            }
        }
    }
    
    /** Converts relative bounds to absolute bounds */
    public void updateBounds () {
        if (relativeBounds != null) {
            //We can set relative bounds when we know desktop size
            setRelativeBounds(relativeBounds);
        }
        //Check area bounds and convert relative bounds to absolute bounds
        Rectangle modeBounds = getBounds();
        Rectangle absBounds, relBounds;
        if ((areas != null) && (modeBounds != null)) {
            TopComponentContainer.Area curArea;
            for (Iterator it = areas.keySet().iterator(); it.hasNext(); ) {
                curArea = (TopComponentContainer.Area) areas.get(it.next());
                relBounds = curArea.getRelativeBounds();
                if (relBounds == null) {
                    continue;
                }                    
                absBounds = new Rectangle();
                absBounds.x = (relBounds.x * modeBounds.width) / 100;
                absBounds.y = (relBounds.y * modeBounds.height) / 100;
                absBounds.width = (relBounds.width * modeBounds.width) / 100;
                absBounds.height = (relBounds.height * modeBounds.height) / 100;
                curArea.setBounds(absBounds);
                curArea.setRelativeBounds(null);
            }
        }
    }
    
    /** @param state current state of window manager.
     * It is bitwise OR of main state and visibility state.
     */
    public void stateChanged(int state) {
        if ((state & StateManager.VISIBLE) != 0) {
            //Main window becomes visible
            StateManager.getDefault().removeStateListener(this);
            updateBounds();
        }
    }

    /** Get the programmatic name of the mode.
     * This name should be unique, as it is used to find modes etc.
     * @return programmatic name of the mode
     */
    public String getName () {
        return name;
    }

    /* @return true if given top component can be docked to this mode,
    * false otherwise */
    public boolean canDock (TopComponent tc) {
        // removed !isSingleType() - bugfix #19614
        return (tcc == null) ? true : tcc.canAdd( new TopComponent[] {tc} );
    }

    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this desktop, it is 
    * removed from the original and moved to this one.
    *
    * @param tc top component to dock into this mode
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockInto (final TopComponent tc) {
        return dockInto(tc, null);
    }
    
    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this desktop, it is 
    * removed from the original and moved to this one.
    *
    * @param tc top component to dock into this mode
    * @param constr constraints used in TopComponentContainer
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockInto (final TopComponent tc, final Object constraints) {
        return dockInto(tc, constraints, Integer.MAX_VALUE);
    }
    
    /** Attaches a component to a mode for this workspace, to specified 
     * constraint and with specified order weight which will drive ordering.
    *
    * @param tc top component to dock into this mode
    * @param constraints constraints used in TopComponentContainer
    * @param orderWeight weight for ordering. Smaller weight number means
     * smaller position index, which means closer to the top or start in
     * visual representations 
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockInto(TopComponent tc, Object constraints, int orderWeight) {
        return doDockInto(tc, constraints, orderWeight, true);
    }
    
    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this desktop, it is 
    * removed from the original and moved to this one.
    * It does not select docked top component if it already exists in container.
    *
    * @param tc top component to dock into this mode
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockIntoNoSelect (final TopComponent tc) {
        return dockIntoNoSelect(tc, null);
    }
    
    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this desktop, it is 
    * removed from the original and moved to this one.
    * It does not select docked top component if it already exists in container.
    *
    * @param tc top component to dock into this mode
    * @param constr constraints used in TopComponentContainer
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockIntoNoSelect (final TopComponent tc, final Object constraints) {
        return dockIntoNoSelect(tc, constraints, Integer.MAX_VALUE);
    }
    
    /** Attaches a component to a mode for this workspace, to specified 
    * constraint and with specified order weight which will drive ordering.
    * It does not select docked top component if it already exists in container.
    *
    * @param tc top component to dock into this mode
    * @param constraints constraints used in TopComponentContainer
    * @param orderWeight weight for ordering. Smaller weight number means
     * smaller position index, which means closer to the top or start in
     * visual representations 
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockIntoNoSelect(TopComponent tc, Object constraints, int orderWeight) {
        return doDockInto(tc, constraints, orderWeight, false);
    }
    
    /** Actually performs the docking operation.
     * @param tc top component to dock into this mode
     * @param constraints constraints used in TopComponentContainer
     * @param orderWeight weight for ordering. Smaller weight number means
     * smaller position index, which means closer to the top or start in
     * visual representations 
     * @param select <code>true</code> if the docked <code>TopComponent</code>
     * will be selected afterwards
     * @return true if top component was succesfully docked to this */
    public boolean doDockInto(final TopComponent tc, final Object constraints,
    final int orderWeight, final boolean select) {
        if (!canDock(tc))
            return false;
        
        ensureSectionLoaded(PROPERTIES | COMPONENTS);
        // synchronize with window manager state and also run in AWT queue
        // because of 'dangerous' swing operations with AWT Event queue
        pendingDockInto++;
        DeferredPerformer.getDefault().putRequest(
            new DeferredPerformer.DeferredCommand () {
                public void performCommand (DeferredPerformer.DeferredContext dc) {
                    ModeImpl mi = (ModeImpl)workspace.findMode(tc);
                    // find out if component is opened
                    boolean opened = WindowManagerImpl.findManager(tc).isOpened(workspace);
                    if ((mi != null) && (!ModeImpl.this.equals(mi))) {
                        mi.release(tc);
                    }
                    if (opened) {
                        if(select) {
                            // The same selecting policy like in container (follow
                            // addToContainer, select parameter to get more.
                            if(tcc == null || tcc.containsTopComponent(tc)) {
                                // #23558, Notify just going to be opened&selected TopComponent.
                                ((WorkspaceImpl)getWorkspace()).addToShownTcs(tc);
                            }
                        }
                        
                        updateBounds();
                        // compute mode bounds if not known yet
                        if (!isBoundsSet()) {
                            ((WorkspaceImpl)workspace).placeMode(ModeImpl.this, tc);
                        }
                        // remove top component in a case it was previously
                        // docked in this mode in closed state
                        ClosedTCContext savedProps = removeClosedComponent(tc);

                        // Check mode constraints for conflicts.
                        WindowUtils.changeModeConstraints(ModeImpl.this,
                            ModeImpl.this.constraints, true);
                        
                        // adds component to the container
                        addToContainer(tc, select,
                            ((constraints == null) && (savedProps != null))
                                ? savedProps.constraints : constraints,
                            ((orderWeight == Integer.MAX_VALUE) && (savedProps != null))
                                ? savedProps.orderWeight : orderWeight
                        );
                        // show container if we are on current workspace only
                        if (workspace.equals(WindowManager.getDefault().getCurrentWorkspace())) {
                            setVisible(true);
                        }
                    } else {
                        // component not opened
                        addClosedComponent(tc, constraints, orderWeight);
                    }
                    // notify
                    changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
                    pendingDockInto--;
                }
            },
            new DeferredPerformer.DeferredContext(null, true)
        );
        return true;
    }
    
    /** Returns number of pending dockInto requests. It is used internally
     * from ModeData to detect if all loaded TopComponents are already docked
     * into Mode.
     */
    public int getPendingDockInto () {
        return pendingDockInto;
    }
    
    /** Releases given top component from current association
     * with this mode.
     * 
     * Must be called from EventDispatchThread
     */
    public void release (TopComponent tc) {
        // find out if component is opened
        if (WindowManagerImpl.getInstance().findManager(tc).isOpened(workspace)) {
            int left = 0;

            //#20516: tcc can be null when for example you dock
            //all tcs from one mode to another, original mode is closed =>
            //tcc is null, wstcref are deleted and TCRefImpl calls ModeImpl.release
            //See similar check in close method.
            if (tcc != null) {
                left = tcc.removeTopComponent(tc);
            }
            
            if (left <= 0) {
                destroyFrame();
                destroyContainer();
            } else {
                updateNameListener();
            }
        } else {
            removeClosedComponent(tc);
        }
        // notify
        changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
    }

    /** Closes given top component.
    * Closing here means removing from top component container and
    * adding to the set of closed components docked in this mode.
     * This method is thread safe and window manager safe.
    */
    public void close (final TopComponent tc) {
        // synchronize with window manager state and also run in AWT queue
        // because of 'dangerous' swing operations
        DeferredPerformer.getDefault().putRequest(
            new DeferredPerformer.DeferredCommand () {
                public void performCommand (DeferredPerformer.DeferredContext dc) {
                    // check validity of request, request may become
                    // invalid (container already destroyed) due to delayed processing
                    if (tcc == null) {
                        return;
                    }
                    if (tcc.containsTopComponent(tc)) {
                        Object constraints = tcc.getConstraints(tc);
                        int orderWeight = tcc.getOrderWeight(tc);
                        int left = tcc.removeTopComponent(tc);
                        if (left <= 0) {
                            destroyFrame();
                            destroyContainer();
                        } else {
                            updateNameListener();
                        }
                        addClosedComponent(tc, constraints, orderWeight);
                        setChanged(true);
                    }
                    // notify
                    changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
                }
            },
            new DeferredPerformer.DeferredContext(null, true)
        );
    }

    /** Closes this mode - it means, that opened top components
    * are closed and closed components docked to this mode are
    * removed 
    * @return true if mode was succesfully cleared and closed,
    * false if some top component refused to close
    */
    public boolean close () {
        // clear closed components
        if (closedComponents != null)
            closedComponents.clear();
        if (tcc == null) {
            return true;
        }
        // try to close all opened top components
        // selected top component will be closed at last
        // to prevent from focus transfering between components during closing
        TopComponent[] tcs = tcc.getTopComponents();
        TopComponent selected = tcc.getSelectedTopComponent();
        boolean result = true;
        for (int i = 0; i < tcs.length; i++) {
            if ((!tcs[i].equals(selected)) && (!tcs[i].close(workspace))) {
                result = false;
            }
        }
        // close selected top component, if possible
        if ((selected != null) && (!selected.close(workspace))) {
            result = false;
        }
        return result;
    }
    
    /** Resets components in ModeData. Necessary to force component update
     * after project switch. */
    public void resetComponents() {
        if (updater != null) {
            updater.resetComponents();
        }
    }

    /** Check if bounds was set.
     * @return true if any part of bounds is not zero 
     */
    public boolean isBoundsSet() {
        Rectangle b = bounds;
        if(b == null) {
            return false;
        }
        
        return !(b.x == 0 && b.y == 0 && b.width == 0 && b.height == 0);
    }
    
    /** Sets the bounds of the mode.
     * @param rect bounds for the mode 
     */
    public void setBounds (Rectangle rect) {
        if(rect == null) {
            throw new NullPointerException("Mode bounds cannot be null!"); // NOI18N
        }
        
        if (bounds.equals(rect)) {
            return;
        }
        final Rectangle old = bounds;
        bounds = rect;
        
        // notify frame if possible
        if (frame != null) {
            //Bugfix #17008 Make call of setBounds() in AWT thread
            //and call revalidate() if frame is JInternalFrame.
            if (SwingUtilities.isEventDispatchThread()) {
                doSetBounds();
                // notify others interested
                changeSupport.firePropertyChange(PROP_BOUNDS, old, bounds);
            } else {
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        doSetBounds();
                        // notify others interested
                        changeSupport.firePropertyChange(PROP_BOUNDS, old, bounds);
                    }
                });
            }
        } else {
            // notify others interested
            changeSupport.firePropertyChange(PROP_BOUNDS, old, bounds);
        }
    }
    
    /** Sets normal bounds of the mode. Should be called only from ModeData.
     * It calls JInternalFrame.setNormalBounds for JInternal frame when desktop 
     * is maximized. Otherwise it calls setBounds.
     * @param rect normal bounds for the mode 
     */
    public void setNormalBounds (Rectangle rect) {
        if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
            if (!isMaxMode()) {
                setBounds(rect);
            } else {
                //Try to set normal bounds for JInternalFrame
                //Bugfix #35322: We need to set mode bounds to some valid value
                //to avoid set bounds from dockInto from not current workspace.
                //As mode will be maximized correct bounds will be set to mode.
                setBounds(rect);
                final Rectangle rLocal = rect;
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        if (frame != null) {
                            Component comp = FrameTypeSupport.obtainFrameComponent(frame);
                            if ((comp != null) && (comp instanceof JInternalFrame)) {
                                ((JInternalFrame) comp).setNormalBounds(rLocal);
                            }
                        }
                    }
                });
            }
        } else {
            setBounds(rect);
        }
    }

    /** Actually changes the bounds of frame component according
     * the <code>bounds</code> field. */
    private void doSetBounds() {
        Component comp = FrameTypeSupport.obtainFrameComponent(frame);
        if (comp != null) {
            comp.setBounds(bounds);
            if (comp instanceof JInternalFrame) {
                ((JInternalFrame) comp).revalidate();
            }
        }
    }
    
    /** Sets the relative bounds of the mode.
     * @param s the bounds for the mode 
     */
    public void setRelativeBounds (Rectangle rect) {
        //Convert relative bound to absolute bounds and call
        //setBounds
        //Check if main window is already visible
        int state = StateManager.getDefault().getState();
        if ((state & StateManager.VISIBLE) != 0) {
            //Set bounds directly
            relativeBounds = rect;
        } else {
            //Store bounds to be set later
            relativeBounds = rect;
            return;
        }
        // do only once and only if frame is already ready
        if (relativeBounds == null) {
            return;
        }
        //Recompute relative to absolute bounds
        if (!boundsConverted) {
            Rectangle wsBounds = null;
            // internal frames are relative to inner desktop area
            if (WindowTypesManager.INTERNAL_FRAME == getFrameType()) {
                Dimension centerSize = ((WorkspaceImpl)getWorkspace()).desktopPane().getInnerDesktopSize();
                if (centerSize.width == 0 && centerSize.height == 0) {
                    // dimensions of inner desktop are not known, compute bounds later
                    return;
                }
                wsBounds = new Rectangle(0, 0, centerSize.width, centerSize.height);
            } else {
                wsBounds = ((WorkspaceImpl) workspace).getWorkingSpaceBounds();
            }
            Rectangle absoluteBounds = new Rectangle();
            absoluteBounds.x = (wsBounds.width * relativeBounds.x) / 100 + wsBounds.x;
            absoluteBounds.y = (wsBounds.height * relativeBounds.y) / 100 + wsBounds.y;
            absoluteBounds.width = (wsBounds.width * relativeBounds.width) / 100;
            absoluteBounds.height = (wsBounds.height * relativeBounds.height) / 100;
            setBounds(absoluteBounds);
            boundsConverted = true;
        }
        // try to propagate relative bounds to the frame component
        if (frame != null) {
            Component c = FrameTypeSupport.obtainFrameComponent(frame);
            if (handleLayoutResource(c, relativeBounds, workspace)) {
                relativeBounds = null;
            }
        }
    }
    
    private static boolean handleLayoutResource(Component c, Rectangle relativeBounds, Workspace ws) {
        if (c instanceof org.netbeans.core.windows.frames.LayoutResource) {
            org.netbeans.core.windows.frames.LayoutResource lr = (org.netbeans.core.windows.frames.LayoutResource) c;
            lr.setRelativeBounds(relativeBounds);
            lr.setWorkspace(ws);
            return true;
        }
        
        return false;
    }

    /** Getter for current bounds of the mode.
     * @return the bounds of the mode
     */
    public Rectangle getBounds () {
        ensureSectionLoaded(PROPERTIES);
        return (Rectangle) bounds.clone();
    }
    
    /** Getter for current relative bounds of the mode.
     * @return the relative bounds of the mode when size of main window is known,
     * otherwise return null.
     */
    public Rectangle getRelativeBounds () {
        //Check if main window is visible
        int state = StateManager.getDefault().getState();
        if ((state & StateManager.VISIBLE) == 0) {
            //Not visible we cannot compute relative bounds
            return null;
        }
        ensureSectionLoaded(PROPERTIES);
        Rectangle rBounds = new Rectangle();
        Rectangle absoluteBounds = new Rectangle(bounds);
        Rectangle wsBounds = ((WorkspaceImpl) workspace).getWorkingSpaceBounds();
        
        rBounds.x = (100 * (absoluteBounds.x - wsBounds.x)) / wsBounds.width;
        rBounds.y = (100 * (absoluteBounds.y - wsBounds.y)) / wsBounds.height;
        rBounds.width = (100 * absoluteBounds.width) / wsBounds.width;
        rBounds.height = (100 * absoluteBounds.height) / wsBounds.height;

        return rBounds;
    }
    
    /** Getter for relative bounds of the mode from XML.
     * @return relative bounds if mode was not visible yet ie. relative bounds from XML
     * was not used yet. If relative bounds from XML was already used it returns
     * <code>null</code>. Used from ModeData to store mode bounds.
     */
    public Rectangle getRelativeBoundsXML () {
        ensureSectionLoaded(PROPERTIES);
        return relativeBounds;
    }
    
    /** Returns bounds for serialization - same as regular bounds in most
     * cases, differs only in MDI max mode for internal frame types */
    public Rectangle getNormalBounds () {
        ensureSectionLoaded(PROPERTIES);
        Rectangle result = null;
        if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
            DesktopPane desktop = ((WorkspaceImpl)workspace).desktopPane();
            if (desktop.isMaxMode() && (frame != null)) {
                result = desktop.getFrameBounds(frame);
            }
        }
        return result != null ? result : getBounds();
    }

    /** Getter for asociated workspace.
     * @return The workspace instance to which is this mode asociated.
     */
    public Workspace getWorkspace () {
        return workspace;
    }

    /** @return array of top components which are currently
     * docked in this mode. May return empty array if no top component
     * is docked in this mode.
     */
    public TopComponent[] getTopComponents () {
        return doGetTopComponents(true);
    }
    
    /** @return array of top components which are currently
     * docked in this mode, ignoring components that are in this mode,
     * but not loaded, only stored on persistent storage.
     */
    public TopComponent[] getLoadedTopComponents () {
        return doGetTopComponents(false);
    }
    
    private TopComponent[] doGetTopComponents (boolean load) {
        if (load) {
            ensureSectionLoaded(COMPONENTS);
        }

        // First get TopComponents from container.
        List result = new ArrayList(Arrays.asList(
            (tcc == null) ? new TopComponent[0] : tcc.getTopComponents()
        ));
        
        // Then add closed TopComponents.
        if(closedComponents != null) {
            synchronized (closedComponents) {
                // #25285, seems there is a problem to use next line,
                // see more in the issue.
                // result.addAll(closedComponents.keySet());
                for(Iterator it = closedComponents.keySet().iterator();
                it.hasNext(); ) {
                    result.add(it.next());
                }
            }
        }
        
        // XXX [PENDING] #25133, there were null's inside in rare cases.
        while(result.remove(null)) ;
        
        return (TopComponent[])result.toArray(new TopComponent[0]);
    }
    
    /** @return an array of opened top components currently
    * docked in this mode.
    */
    public TopComponent[] getOpenedTopComponents () {
        ensureSectionLoaded(COMPONENTS);
        return getLoadedOpenedTopComponents();
    }

    /** @return an array of opened top components currently
    * docked in this mode. No effort to load components from disk first
    * is performed.
    */
    public TopComponent[] getLoadedOpenedTopComponents () {
        return (tcc == null) ? new TopComponent[0] : tcc.getTopComponents();
    }
    
    /** @return an array of top component ids found in mode folder.
    * It does not create instances of top components. It is used by 
    * Workspace.findMode(TopComponent) to avoid creating top component instances.
    * When mode folder is empty or mode is newly created (it does not have mode 
    * folder yet) empty array is returned.
    */
    public String[] getTopComponentIds () {
        ensureSectionLoaded(COMPONENT_IDS);
        if (topComponentIds == null) {
            topComponentIds = new String[0];
        }
        return topComponentIds;
    }

    /** Sets an array of top component ids found in mode folder.
    * It is called from ModeData only.
    */
    public void setTopComponentIds (String[] topComponentIds) {
        this.topComponentIds = topComponentIds;
    }
    
    /** The component requests focus. Request can be delayed
    * if top component container is not in consistent state.
    */
    public void requestFocus (final TopComponent comp) {
        if (SwingUtilities.isEventDispatchThread()) {
            doRequestFocus(comp);
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    doRequestFocus(comp);
                }
            });
        }
    }

    /** Requests focus for whole mode. Request can be delayed
    * if top component container is not in consistent state.
    */
    public void requestFocus () {
        if (SwingUtilities.isEventDispatchThread()) {
            doRequestFocus();
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    doRequestFocus();
                }
            });
        }
    }

    /** requests focus for given top component */
    private void doRequestFocus (TopComponent comp) {
        if (!showing) {
            StateManager stateManager = StateManager.getDefault();
            //Bugfix #17487: Ignore focus request when switching workspaces
            //but not when starting IDE.
            if (((stateManager.getState() & StateManager.SWITCHING) == 0)
             || ((stateManager.getState() & StateManager.INVISIBLE) != 0)) {
                deferredFocusRequest = true;
                compToReceiveFocus = comp;
            }
        } else {
            // due to possibility of 'too much' delayed requests
            // we must perform additional check 
            if ((tcc != null) && tcc.containsTopComponent(comp)) {
                requestFocus();
                tcc.requestFocus(comp);
            }
        }
    }
    
    /** requests focus for this mode */
    private void doRequestFocus () {
        if (!showing) {
            StateManager stateManager = StateManager.getDefault();
            //Bugfix #17487: Ignore focus request when switching workspaces
            //but not when starting IDE.
            if (((stateManager.getState() & StateManager.SWITCHING) == 0)
             || ((stateManager.getState() & StateManager.INVISIBLE) != 0)) {
                deferredFocusRequest = true;
            }
        } else {
            // due to possibility of 'too much' delayed requests
            // we must perform additional check 
            if (frame != null) {
                //Bugfix #24877: Make sure main window is focused and fronted. Transfer
                //focus from another native window to main window.
                if ((WindowTypesManager.DESKTOP_FRAME.equals(frameType)) || 
                    (WindowTypesManager.INTERNAL_FRAME.equals(frameType))) {
                    Window w = SwingUtilities.getWindowAncestor((Component) frame);
                    if (!w.isFocused()) {
                        w.toFront();
                    }
                }
                try {
                    frame.setSelected(true);
                } catch (PropertyVetoException exc) {
                    // not sure what I'm supposed to do ?
                }
            }
        }
    }
    
    /** Requests visibility for given TopComponent. */
    public void requestVisible (final TopComponent comp) {
        if (SwingUtilities.isEventDispatchThread()) {
            doRequestVisible(comp);
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    doRequestVisible(comp);
                }
            });
        }
    }
    
    /** Requests visibility for given TopComponent. */
    private void doRequestVisible (TopComponent comp) {
        // due to possibility of 'too much' delayed requests
        // we must perform additional check 
        if ((tcc != null) && tcc.containsTopComponent(comp)) {
            tcc.requestVisible(comp);
        }
    }

    /** @return asociated implementation of TopComponentContainer.
    * Can return null if no container is asociated at present.
    * (it means that no opened top component is docked in this mode) */
    public TopComponentContainer getContainerInstance () {
        return tcc;
    }

    /** @return asociated implementation of FrameType.
    * Can return null if no container is asociated at present.
    * (it means that no opened top component is docked in this mode) */
    public FrameType getFrameInstance () {
        return frame;
    }
    
    /** Get value of display name to be stored.
    * Used only to store XML configuration.
    * @return the diplay name of the workspace
    */
    public String getDisplayNameXML () {
        ensureSectionLoaded(PROPERTIES);
        return displayName;
    }
    
    /** @return Human presentable name of this mode implementation */
    public String getDisplayName () {
        ensureSectionLoaded(PROPERTIES);
        if (fromBundle) {
            String bName = nameBundle;
            if (bName == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("Mode " + name // NOI18N
                    + " does not specify nameBundle in xml data!") // NOI18N
                );
            } else {
                try {
                    return NbBundle.getBundle(bName).getString(displayName);
                } catch(MissingResourceException mre) {
                    ErrorManager.getDefault().annotate(mre, ErrorManager.UNKNOWN, "Getting display name for mode " + name + " in " + getWorkspace().getName(), null, null, null); // NOI18N
                    ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL, mre);
                }
            }
        }
        return displayName;
    }

    /** Sets new display name of this mode */
    public void setDisplayName (String s) {
        //Remove shortcut due to detection of change
        String localDisplay = getDisplayName();
        if (localDisplay != null) {
            localDisplay = Actions.cutAmpersand(localDisplay);
        }
        String inNoShortcut = Actions.cutAmpersand(s);
        if (((localDisplay != null) && localDisplay.equals(inNoShortcut)) ||
                (localDisplay == null) && (inNoShortcut == null)) {
            // no real change
            return;
        }
        // disable fromBundle feature, it user's choice now
        fromBundle = false;
        String old = displayName;
        displayName = s;
        changeSupport.firePropertyChange(PROP_DISPLAY_NAME, old, displayName);
    }
    
    /** @return Bundle from which localized display name is read */
    public String getNameBundle () {
        ensureSectionLoaded(PROPERTIES);
        return nameBundle;
    }

    /** Sets bundle display name will be read from */
    public void setNameBundle (String nameBundle) {
        this.nameBundle = nameBundle;
    }
    
    /** Get value of description to be stored.
    * Used only to store XML configuration.
    * @return the description name of the mode
    */
    public String getDescriptionXML() {
        ensureSectionLoaded(PROPERTIES);
        return description;
    }

    /** Sets new bundle key description of this mode is taken from */
    public void setDescriptionXML(String s) {
        description = s;
    }
    
    /** @return Human presentable description of this mode */
    public String getDescription() {
        ensureSectionLoaded(PROPERTIES);
        if(description == null || descriptionBundle == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
                "[WinSys] Mode " + this // NOI18N
                + " does not provide description in xml data."); // NOI18N
        } else {
            try {
                return NbBundle.getBundle(descriptionBundle).getString(description);
            } catch(MissingResourceException mre) {
                ErrorManager.getDefault().notify(
                    ErrorManager.INFORMATIONAL, mre);
            }
        }
        return description;
    }
    
    /** @return Bundle from which localized description is read */
    public String getDescriptionBundle() {
        ensureSectionLoaded(PROPERTIES);
        return descriptionBundle;
    }
    
    /** Sets bundle description will be read from */
    public void setDescriptionBundle(String descriptionBundle) {
        this.descriptionBundle = descriptionBundle;
    }

    /** @return icon of this mode */
    public Image getIcon () {
        ensureSectionLoaded(PROPERTIES);
        if ((iconImage == null) && (icon != null)) {
            try {
                iconImage = Toolkit.getDefaultToolkit().getImage(icon);
            } catch (Exception exc) {
                // sometimes null pointer exceptions are thrown for no good reason
                // we return null and log exception for further processing
                ErrorManager.getDefault().annotate(
                    exc,
                    NbBundle.getMessage(ModeImpl.class, "FMT_NoIconWarning",
                            new Object[] {icon})
                );
                ErrorManager.getDefault().notify(
                    ErrorManager.INFORMATIONAL,
                    exc
                );
                    
            }
        }
        return iconImage;
    }

    public URL getIconURL () {
        ensureSectionLoaded(PROPERTIES);
        return icon;
    }
    
    /** Sets icon URL. Should be set only from ModeData in xml layers impl */ 
    public void setIconURL (URL icon) {
        this.icon = icon;
        // PENDING - property change, redraw
    }
    
    /** Updates UI of asociated frame if possible */
    public void updateUI () {
        if (frame != null) {
            frame.updateUI();
        }
    }

    /** Sets and updates the state of associated frame, if frame exists.
     * Otherwise remembers state for futher use
     */
    public void setFrameState (final int state) {
        if (frame != null) {
            try {
                if (java.awt.EventQueue.isDispatchThread()) {
                    frame.setState(state);
                } else {
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                // be sure no one destroyed the frame
                                FrameType localFrame = frame;
                                if (localFrame != null) {
                                    localFrame.setState(state);
                                }
                            } catch (PropertyVetoException exc) {
                                // frame refuses to set desired state, log warning
                                ErrorManager em = ErrorManager.getDefault();
                                em.annotate(exc, NbBundle.getMessage(
                                        ModeImpl.class,
                                        "EXC_SetState",
                                        new Object[] {getDisplayName()})
                                );
                                em.notify(ErrorManager.WARNING, exc);
                            }
                        }
                    });
                }
            }
            catch (PropertyVetoException exc) {
                // frame refuses to set desired state, log warning
                ErrorManager em = ErrorManager.getDefault();
                em.annotate(exc, NbBundle.getMessage(ModeImpl.class,
                        "EXC_SetState", new Object[] {getDisplayName()})
                );
                em.notify(ErrorManager.WARNING, exc);
            }
        } else {
            frameTypeState = state;
        }
    }
    
    /** @return status of desktop.
     * It is valid only in MDI mode. 
     */
    public boolean isMaxMode () {
        if (WindowUtils.isMdi()) {
            DesktopPane desktop = ((WorkspaceImpl)workspace).desktopPane();
            return desktop.isMaxMode();
        } else {
            return false;
        }
    }
    
    /** @return state of the frame which is meaningful for serialization.
     * If frame exists, its real state is returned, except for internal frames 
     * in max MDI mode. Last remembered frame state is returned if frame currently
     * doesn't exist. FrameType.NORMAL is returned as default if state cannot be
     * obtained by mentioned procedures.
     */
    public int getRestoredFrameState () {
        ensureSectionLoaded(PROPERTIES);
        if (frame != null) {
            // Frame exists, return real state or "fake" state if in max mode
            if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
                DesktopPane desktop = ((WorkspaceImpl)workspace).desktopPane();
                if (desktop.isMaxMode()) {
                    return desktop.getFrameState(frame);
                }
            }
            return frame.getState();
        } else if (-1 != frameTypeState) {
            // return last remembered state of already destroyed frame
            return frameTypeState;
        }
        return FrameType.NORMAL;
    }
    
    /** @return state of the frame
     * If frame exists, its real state is returned. 
     * Last remembered frame state is returned if frame currently
     * doesn't exist. FrameType.NORMAL is returned as default if state cannot be
     * obtained by mentioned procedures.
     */
    public int getFrameState () {
        ensureSectionLoaded(PROPERTIES);
        if (frame != null) {
            return frame.getState();
        } else if (-1 != frameTypeState) {
            // return last remembered state of already destroyed frame
            return frameTypeState;
        }
        return FrameType.NORMAL;
    }
    
    /** Used internaly from UIModeManager when switching to SDI #33691 */
    int getFrameStateVar () {
        return frameTypeState;
    }
    
    /** Used internaly from UIModeManager when switching to SDI #33691 */
    void setFrameStateVar (int state) {
        frameTypeState = state;
    }
    
    /** Shows or hides asociated top component container,
    * if possible (if container exists)
    */
    public void setVisible (final boolean state) {
        // cancel setVisible processing if we're sure it's useless
        // (to force help lazy loading support)
        if (isHidden() && (frame == null)) {
            // get rid of user defined modes which are hidden, because they are
            // useless
            if (isUserDefined()) {
                destroyMode();
            }
            return;
        }
        ensureSectionLoaded(COMPONENTS);
        if (isOrphan() || (frame == null)) {
            setHidden(true);
            return;
        }
        // check and convert bounds, if needed
        BoundsConvertor.getInstance().updateBounds(this);
        Component frameComp = FrameTypeSupport.obtainFrameComponent(frame);
        if ((state != frameComp.isVisible()) && (!state || shouldShow())) {
            // handle MDI frame types
            if ((WindowTypesManager.DESKTOP_FRAME.equals(frameType)) || 
                (WindowTypesManager.INTERNAL_FRAME.equals(frameType))) {
                WorkspaceImpl w = (WorkspaceImpl)workspace;
                if (state) {
                    // ensure that desktop pane is attached to comp hierarchy
                    if (w.desktopPane().getParent() == null) {
                        MainWindow.getDefault().getContentPane().add(w.desktopPane());
                    }
                    // add or remove frame to/from desktop
                    if (WindowTypesManager.DESKTOP_FRAME.equals(frameType)) {
                        if(constraints instanceof String) {
                            Component comp = w.desktopPane()
                                .getComponentAt((String)constraints);
                            if(comp != null) { 
                                // #28872 Should not happen, but it does.
                                handleIssue28872(ModeImpl.this, comp, constraints);
                                return;
                            }
                        }
                        
                        w.desktopPane().addFrameType(frame, constraints);
                    } else {
                        w.desktopPane().addFrameType(frame);
                    }
                } else {
                    w.desktopPane().removeFrameType(frame);
                }
                // XXX #26359 Sometimes the desktop part was not redrawn right way.
                MainWindow.getDefault().invalidate();
                MainWindow.getDefault().validate();
            }
            // when mode is displayed first time, 
            // set deserialized frame state - bugfix #10455, #10533
            if (state) {
                restoreState();
            }

            // #30280 Attach to container before the frame is activated,
            // so the activated nodes are fired properly.
            //Bugfix #11153 before setAttached was called from DefaultContainerImpl
            //componentShown, componentHidden
            tcc.setAttached(state);
            
            frameComp.setVisible(state);

            // XXX(-ttran) bug #18154: frames are minimized after switching b/w
            // workspaces.  This happens at least on Linux +
            // sawfish/Enlightenment/KDE.  Windows platforms should not be
            // affected by the bug
            
            if (state && Utilities.isUnix()
                && frameComp instanceof javax.swing.JFrame ) {
                ((javax.swing.JFrame) frameComp).requestFocus();
            }
        }

        // #28273. For selected components in other areas is necessary 
        // to call componentShown.
        if(state && tcc != null) {
            tcc.checkShownComponents();
        }
    }
    
    /** When mode was attemted to put at the position in desktop, while
     * there was already another component(mode) added. */
    private static void handleIssue28872(
        final ModeImpl newMode, Component oldMode, Object constraints
    ) {
        // #28872 Should not happen, but it does.
        // There is already another component(mode) at the same constraints!
        ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
            "[WinSys] Mode " + newMode + " attempted to" // NOI18N
            + " be opened at the position of " + oldMode + ", constraints=" // NOi18N
            + constraints + ". The mode putting into separate window."); // NOi18N
        org.openide.util.RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                newMode.setFrameType(WindowTypesManager.TOP_FRAME);
                newMode.setVisible(true);
            }                                
        });
    }
    
    /** @return True if frame that represents this mode is visible, false
     * otherwise
     */
    public boolean isVisible () {
        return (frame == null) ? false : FrameTypeSupport.obtainFrameComponent(frame).isVisible();
    }
    
    /** @return true if frame that represents this mode is selected, false
     * otherwise
     */
    public boolean isActive () {
        return active;
    }
    
    /** Called from frame when maximized frame is moved to top. Propagated to workspace.
     */
    public void setTopMaximized () {
        ((WorkspaceImpl) workspace).setTopMaximizedMode(this);
    }
    
    /** Helper, restores deserialized state of the frame */
    private void restoreState () {
        if (-1 == frameTypeState) {
            return;
        }
        // do not try to maximize or iconify attached frames
        if (!WindowTypesManager.DESKTOP_FRAME.equals(frameType)) {
            //Bugfix #23689: Explicitly set desktop to maximized.
            if (frameTypeState == FrameType.MAXIMIZED) {
                WorkspaceImpl w = (WorkspaceImpl) workspace;
                w.desktopPane().setMaxMode(true);
            }
            setFrameState(frameTypeState);
        }
        frameTypeState = -1;
    }

    /** @return true if it is ok to show frame
    * false otherwise (all top components are closed)
    * Called from setVisible.
    */
    private boolean shouldShow () {
        TopComponent[] tcs = tcc.getTopComponents();
        for (int i = 0; i < tcs.length; i++) {
            if (WindowManagerImpl.findManager(tcs[i]).isOpened(getWorkspace())) {
                return true;
            }
        }
        return false;
    }

    /** @return true if no component is docked to this mode
    * or all components docked to this mode are closed */
    public boolean isOrphan () {
        ensureSectionLoaded(PROPERTIES | COMPONENTS);
        return (frame == null) || (tcc == null) || (tcc.getTopComponents().length <= 0);
    }

    /** @return true if this mode is currently in the state where
    * it contains exactly one OPENED top component.
    * (it can contains any number of closed components but this method
    * will still return true if there is exatly one opened)
    */
    public boolean isSingle () {
        ensureSectionLoaded(PROPERTIES | COMPONENTS);
        return (frame != null) && (tcc != null) && (tcc.getTopComponents().length == 1);
    }

    /** @return true if mode isSingle() returns true and if the only components
     * don't want display name of the mode to be visible in caption */
    public boolean isSingleType () {
        if(!isSingle()) {
            return false;
        }
        
        String nameType = (String)(tcc.getTopComponents())[0].getClientProperty(NAMING_TYPE);

        if(nameType == null) {
            return false;
        } else if(BOTH_ONLY_COMP_NAME.equals(nameType)) {
            // Is single in both SDI and MDI.
            return true;
        } else {
            if(WindowUtils.isMdi()) {
                // Is in single mode in MDI?
                return MDI_ONLY_COMP_NAME.equals(nameType);
            } else {
                // Is in single mode in SDI?
                return SDI_ONLY_COMP_NAME.equals(nameType);
            }
        }
    }
    
    /** @return true if mode is user defined, false otherwise
    * (defined programmatically) */
    public boolean isUserDefined () {
        ensureSectionLoaded(PROPERTIES);
        return userDefined;
    }
    
    /** @return true if mode has display name from bundle, false otherwise
    */
    public boolean isFromBundle () {
        ensureSectionLoaded(PROPERTIES);
        return fromBundle;
    }

    /** Sets user-defined flag. Should be set only from xml deserialization
     * layers ModeData class. */
    public void setUserDefined (boolean userDefined) {
        this.userDefined = userDefined;
        // PENDING - property change firing, sync
    }
    
    /** @return true if display name is taken from bundle, false otherwise */
    public boolean isNameFromBundle () {
        ensureSectionLoaded(PROPERTIES);
        return fromBundle;
    }

    /** Sets "from bundle" flag. If true, display name represents key to
     * bundle, otherwise display name contains real value of display name 
     */
    public void setNameFromBundle (boolean fromBundle) {
        this.fromBundle = fromBundle;
        // PENDING - property change firing, sync and redraw (in AWT)
    }
    
    private Map getClosedComponents () {
        ensureSectionLoaded(COMPONENTS);
        return closedComponents;
    }

    /** Fills this mode with top components contained in given source
    * mode.
    */
    void fillTopComponents (ModeImpl source) {
        // copy references to closed top components via dockInto calls
        Map closedComps = source.getClosedComponents();
        if (closedComps != null) {
            Map.Entry[] closedEntries = null;
            synchronized (closedComps) {
                closedEntries = (Map.Entry[])closedComps.entrySet().
                                toArray(new Map.Entry[closedComps.size()]);
            }
            Map.Entry curEntry = null;
            for (int i = 0; i < closedEntries.length; i++) {
                ClosedTCContext cc = (ClosedTCContext)closedEntries[i].getValue();
                dockInto((TopComponent)closedEntries[i].getKey(),
                         cc.constraints, cc.orderWeight);
            }
        }
        // copy references to opened top components (and reopen them)
        TopComponent[] tcs = source.getOpenedTopComponents();
        for (int i = 0; i < tcs.length; i++) {
            dockInto(tcs[i], source.tcc.getConstraints(tcs[i]));
            tcs[i].open(workspace);
        }
    }

    /**** Implementation of frame type and component listeners
     * we listen to the events in frame and react properly
     */

    /** When frame is being deactivated  */
    public void frameDeactivated(FrameTypeEvent fe) {
        active = false;
        ((WorkspaceImpl) workspace).setActiveMode(null);
    }
    
    /** When frame was closed  */
    public void frameClosed(FrameTypeEvent fe) {
        //If active mode is closed we should reset active mode
        if (active) {
            active = false;
            ((WorkspaceImpl) workspace).setActiveMode(null);
        }
    }
    
    /** When frame was brought back from icon to normal state  */
    public void frameDeiconified(FrameTypeEvent fe) {
        notifyComponentsShowing();
    }
    
    /** When frame was brought back from maximize to normal state  */
    public void frameNormalized(FrameTypeEvent fe) {
        if (frame != null) {
            if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
                if (this.equals(((WorkspaceImpl) workspace).getTopMaximizedMode())) {
                    ((WorkspaceImpl) workspace).setTopMaximizedMode(null);
                }
            }
        }
    }
    
    /** When frame was opened - showed  */
    public void frameOpened(FrameTypeEvent fe) {
    }
    
    /** When frame was iconified, deactivate currently active top component
     * if belongs to this mode */
    public void frameIconified(FrameTypeEvent fe) {
        if (frame != null) {
            if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
                if (this.equals(((WorkspaceImpl) workspace).getTopMaximizedMode())) {
                    ((WorkspaceImpl) workspace).setTopMaximizedMode(null);
                }
            }
        }
        
        TopComponent activeTc = TopComponent.getRegistry().getActivated();
        if ((activeTc != null) && this.equals(getWorkspace().findMode(activeTc))) {
            WindowManagerImpl.getInstance().activateComponent(null);
        }
        
        notifyComponentsHidden();
    }
    
    /** When user tried to invoke frame closing
     * So try to close all contained top components, if they agree.
     */
    public void frameClosing(FrameTypeEvent fe) {
        // check if we are in consistent state
        if (!isInClosableState()) {
            return;
        }
        TopComponent[] tcs = tcc.getTopComponents();
        TopComponent selected = tcc.getSelectedTopComponent();
        boolean shouldClose = true;
        for (int i = 0; i < tcs.length; i++) {
            if ((!tcs[i].equals(selected)) && (!tcs[i].close())) {
                shouldClose = false;
                break;
            }
        }
        // close selected top component, if possible
        if ((shouldClose) && (selected != null) && (!selected.close())) {
            shouldClose = false;
        }
        //If active mode is closed we should reset active mode
        if (active) {
            active = false;
            ((WorkspaceImpl) workspace).setActiveMode(null);
        }
        // should not be needed, destroyFrame already called from above close
        // calls
        /*if (shouldClose) {
            destroyFrame();
        }*/  
    }
    
    /** When frame was made active - receives focus  */
    public void frameActivated(FrameTypeEvent fe) {
        active = true;
        ((WorkspaceImpl) workspace).setActiveMode(this);
    }
    
    /** When frame was maximized  */
    public void frameMaximized(FrameTypeEvent fe) {
        if (frame != null) {
            if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
                setTopMaximized();
            }
        }
    }
    
    /** Notifies lastly shown <code>TopComponent</code>'s are showing again.
     * Helper method. Called from {@link #frameDeiconified}. */
    private void notifyComponentsShowing() {
        if(iconifiedShownTcs == null) {
            return;
        }
        
        WorkspaceImpl ws = (WorkspaceImpl)getWorkspace();
        for(Iterator it = iconifiedShownTcs.iterator(); it.hasNext();) {
            ws.addToShownTcs((TopComponent)it.next());
        }
        
        iconifiedShownTcs = null;
    }
    
    /** Notifies currently shown <code>TopComponent</code>'s are hidden.
     * Helper method. Called from {@link #frameIconified}. */
    private void notifyComponentsHidden() {
        WorkspaceImpl ws = (WorkspaceImpl)getWorkspace();
        Set shownTcs = ws.getShownTcs();
        iconifiedShownTcs = new WeakSet(shownTcs.size());
        TopComponent[] tcs = getTopComponents();
        for(int i = 0; i < tcs.length; i++) {
            if(shownTcs.contains(tcs[i])) {
                ws.removeFromShownTcs(tcs[i]);
                iconifiedShownTcs.add(tcs[i]);
            }
        }
    }
    
    /** When container was resized, its size was changed */
    public void componentResized (ComponentEvent ce) {
        setBounds(ce.getComponent().getBounds());
    }

    /** When container was moved, its position was changed */
    public void componentMoved (ComponentEvent ce) {
        setBounds(ce.getComponent().getBounds());
    }

    /** Called when container was shown. */
    public void componentShown (ComponentEvent ce) {
        showing = true;
        if (deferredFocusRequest) {
            deferredFocusRequest = false;
            if (compToReceiveFocus != null) {
                // XXX #21384, request focus for the compoment only
                // in the case the focus is supposed to remain in this mode.
                if(!compToReceiveFocus.hasFocus()
                && SwingUtilities.findFocusOwner((Component)frame) != null) {
                    requestFocus(compToReceiveFocus);
                }
                compToReceiveFocus = null;
            } else {
                requestFocus();
            }
        }
    }

    /** Called when container was hidden. */
    public void componentHidden (ComponentEvent ce) {
        showing = false;
    }

    /** Sets new container type and transfers content
    * of current container to the new one
    * @return true if new container was switched succesfully
    */
    public void setContainerType (String containerType) {
        if (containerType.equals(this.containerType)) {
            return;
        }
        this.containerType = containerType;
        if (tcc != null) {
            TopComponentContainer old = tcc;
            tcc = createContainer(this.containerType);
            changeContainer(old, tcc);
        }
    }


    /** @return unique string representation of current container type */
    public String getContainerType () {
        ensureSectionLoaded(PROPERTIES);
        if (containerType == null) {
            containerType = WindowTypesManager.getDefaultContainer();
        }
        return containerType;
    }
    
    /** Sets new frame type and associates new frame with existing container.
     * Also updates visual state in case that mode is showing on the screen
     * when executing this method.
     */
    public void setFrameType (String frameType) {
        restoreFrameType(this.frameType, frameType);
    }
    
    /** Restores frame type from provided infor about previous and current
     * frame types. Previous frame type is needed for correct computation of
     * bounds conversions.
     */ 
    public void restoreFrameType (String previousFrameType, String newFrameType) {
        if (newFrameType == null) {
            throw new IllegalArgumentException("Cannot set frame type to null."); // NOI18N
        }
        if (newFrameType.equals(previousFrameType)) {
            return;
        }
        this.frameType = newFrameType;
        DeferredPerformer.getDefault().putRequest(
            this, new DeferredPerformer.DeferredContext(previousFrameType, true)
        );
    }

    private void doSetFrameType (String oldFrameType) {
        // check and convert bounds
        BoundsConvertor.getInstance().updateBounds(this, oldFrameType, frameType);
        // update visual state if needed
        if (frame != null) {
            // hide first if now showing 
            boolean wasVisible = FrameTypeSupport.obtainFrameComponent(frame).isVisible();
            if (wasVisible) {
                setVisible(false);
            }
            // create new frame, swap with existing
            FrameType old = frame;
            frame = createFrame(frameType);
            changeFrame(old, frame);
            // throws away old frame
            doDisposeFrame(old);
            // restore visibility
            if (wasVisible) {
                setVisible(true);
            }
            changeSupport.firePropertyChange(PROP_FRAME_INSTANCE, old, frame);
        }
    }
    
    /** @return unique string representation of current frame type */
    public String getFrameType () {
        ensureSectionLoaded(PROPERTIES);
        if (frameType == null) {
            frameType = WindowTypesManager.getDefaultFrame();
        }
        return frameType;
    }

    
    /** Sets constraints for mode placement in its parent. Null constraints
     * means no constraints. Null constraints are default.
     * Also directly update visual state, if mode is visible when entering
     * this method */
    public void setConstraints (Object constraints) {
        if (this.constraints == null) {
            if (this.constraints == constraints) {
                return;
            }
        } else {
            if (this.constraints.equals(constraints)) {
                return;
            }
        }
        
        // check validity of request - multiple modes with the same side
        // constraints on one workspace are not allowed
        if ((constraints != null) && !PerimeterLayout.CENTER.equals(constraints)) {
            ModeImpl foundMode = WindowUtils.findConstrainedMode(workspace, constraints);
            if(foundMode != null && foundMode.isVisible()) {
                throw new IllegalArgumentException(
                    "Cannot attach " + getDisplayName() + "@" + System.identityHashCode(this) + " to " + constraints + // NOI18N
                    " side. " + foundMode.getDisplayName() + "@" + System.identityHashCode(foundMode) + // NOI18N
                    " is already attached there."); // NOI18N
            }
        }
        Object oldValue = this.constraints;
        if ((frame != null) && FrameTypeSupport.obtainFrameComponent(frame).isVisible()) {
            setVisible(false);
            this.constraints = constraints;
            setVisible(true);
        } else {
            this.constraints = constraints;
        }
        changeSupport.firePropertyChange(PROP_CONSTRAINTS, oldValue, this.constraints);
    }

    /** @return Current constraints of this mode, null by default */
    public Object getConstraints () {
        ensureSectionLoaded(PROPERTIES);
        return getLoadedConstraints();
    }
    
    /** Sets current mode's constraints. Use if mode is resided outside MDI.
     * Will be used if a mode is resided in IDE Desktop back. 
     * The current constraints are transient. */
    public void setCurrentConstraints (Object constraints) {
        currnetConstraints = constraints;
    }

    /** @return Current constraints of this mode, null by default */
    public Object getCurrentConstraints () {
        return currnetConstraints;
    }
    
    /** @return Current constraints of this mode, null by default. No effort
     * to load data from disk is done. */
    public Object getLoadedConstraints () {
        return constraints;
    }
    
    public Object getConstraint (TopComponent tc) {
        Object result;
        if (tcc != null) {
            result = tcc.getConstraints(tc);
            if (result != null) {
                return result;
            }
        }
        if (closedComponents != null) {
            ClosedTCContext cc = (ClosedTCContext)closedComponents.get(tc);
            return cc == null ? null : cc.constraints;
        } else {
            return null;
        }
    }

    /** @return true if mode is empty or all contained components are closed */
    public boolean isHidden () {
        ensureSectionLoaded(PROPERTIES);
        return hidden;
    }
    
    /** Sets hidden state of this mode. Method is public only as implementation
     * side effect, should be called externally only from ModeData */
    public void setHidden (boolean hidden) {
        this.hidden = hidden;
    }
    
    /** Add listener to the property changes */
    public void addPropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }

    /** Remove listener to the property changes */
    public void removePropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }
    
    /** Adds given class to the list of classes which name is ignored
    * when top component container enters some special state 
    * (usually when there is only one top component docked in it)
    * @param tcClass Class of top component.
    * @return true if list of classes was changed as a result of this call
    */
    public boolean addIgnoredNameClass (Class tcClass) {
        if (ignoredTcList == null) {
            ignoredTcList = new ArrayList(5);
        }
        if (!TopComponent.class.isAssignableFrom(tcClass)) {
            throw new IllegalArgumentException("tcClass parameter is not a subclass of TopComponent class"); // NOI18N
        }
        return ignoredTcList.add(tcClass);
    }

    /** Removes given class from the list. */
    public boolean removeIgnoredNameClass (Class tcClass) {
        if (ignoredTcList == null) {
            return false;
        }
        return ignoredTcList.remove(tcClass);
    }

    /** Adds top component to the set of closed tcs docked
    * in this mode */ 
    void addClosedComponent (TopComponent tc, Object constraints, int orderWeight) {
        if (closedComponents == null) {
            closedComponents = new WeakHashMap(10);
        }
        //Bugfix 34316: Set default value if constraint is not set.
        if (constraints == null) {
            constraints = PerimeterLayout.CENTER;
        }
        synchronized (closedComponents) {
            ClosedTCContext existing = (ClosedTCContext)closedComponents.get(tc);
            // update existing entry if found, don't duplicate
            if (existing != null) {
                if (constraints != null) {
                    existing.constraints = constraints;
                }
                if (orderWeight != Integer.MAX_VALUE) {
                    existing.orderWeight = orderWeight;
                }
            } else {
                closedComponents.put(
                    tc, new ClosedTCContext(constraints, orderWeight)
                );
            }
        }
    }

    /** Removes top component from the set of closed tcs docked
    * in this mode */ 
    ClosedTCContext removeClosedComponent (TopComponent tc) {
        if (closedComponents == null) {
            return null;
        }
        synchronized (closedComponents) {
            ClosedTCContext result = (ClosedTCContext)closedComponents.get(tc);
            closedComponents.remove(tc);
            return result;
        }
    }

    /** Removes the all top component from the set of closed tcs docked
    * in this mode. Used when switching projects. */ 
    public void removeAllClosedComponents () {
        if (closedComponents != null) {
            closedComponents.clear();
        }
    }
    
    /* Helper method, adds specified top component to the container.
     * Creates frame and container, if needed. */
    private void addToContainer (TopComponent tc, boolean selectExisting, Object constr, int orderWeight) {
        // assure frame is created and in normal state
        if (frame == null) {
            frame = createFrame(getFrameType());
            changeFrame(null, frame);
            Image frameIcon = obtainFrameIcon(tc);
            if (frameIcon != null) {
                frame.setIconImage(frameIcon);
            }
            changeSupport.firePropertyChange(PROP_FRAME_INSTANCE, null, frame);
        }
        // assure container is created and perform adding  
        if (tcc == null) {
            tcc = createContainer(getContainerType());
            changeContainer(null, tcc);
        }
        //Get area for given constraint if possible
        //Bugfix #17437: We must set constraint to default value here
        //to use and remove correct area.
        if (constr == null) {
            constr = PerimeterLayout.CENTER;
        }
        TopComponentContainer.Area area = null;
        if (areas != null) {
            //Remove area from mode and pass it to container
            area = (TopComponentContainer.Area) areas.remove(constr);
        }
        // add only if not re-opened multiple times
        if (constr == null ? tcc.addTopComponent(tc, selectExisting) 
                           : tcc.addTopComponent(tc, selectExisting, constr, orderWeight, area)) {
            updateNameListener();
        }
        setHidden(false);
    }

    /** Returns proper icon for this mode's frame or null if everything failed */ 
    private Image obtainFrameIcon (TopComponent firstTc) {
        Image modeIcon = getIcon();
        if (modeIcon != null) {
            return modeIcon;
        } else {
            Image tcIcon = firstTc.getIcon();
            if (tcIcon != null) {
                return tcIcon;
            }
        }
        return defaultIcon();
    }
    
    /** Returns default icon for frames */
    private static Image defaultIcon () {
        return Utilities.loadImage("org/netbeans/core/resources/frames/default.gif"); //NOI18N
    }
    
    /** Sets areas to mode.
     */
    public void setAreas (Map areas) {
        this.areas = new HashMap(areas);
    }
    
    /** @return Areas from container and from mode.
     */
    public Map getAreas () {
        return areas;
    }

    /** @return Newly created top component container of specified
     * type (class name). 
     */
    private TopComponentContainer createContainer (String className) {
        return WindowTypesManager.createContainer(className);
    }
    
    /** @return Newly created frame type of specified type (class name) */
    private FrameType createFrame (String className) {
        FrameType result = WindowTypesManager.createFrame(className);
        // set properties that we expect
        result.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        Component frame = FrameTypeSupport.obtainFrameComponent(result);
        frame.setVisible(false);
        frame.setBounds(bounds);
        
        if (relativeBounds != null) {
            handleLayoutResource(frame, relativeBounds, workspace);
            // bugfix #16611 the bounds was used and shouldn't be store henceforth
            // because a resized or moved bounds could be overwritten with relativeBounds
            relativeBounds = null;

        }
        result.attachToMode(this);
        return result;
    }
    
    /** Sets closing policy. If set to true, closing of user defined modes 
     * will not trigger complete destroy of data source (updater) and its files
     * on the disk.
     */
    void setCloseOnlyInMemory (boolean flag) {
        this.closeOnlyInMemory = flag;
    }
    
    /** Removes this mode from workspace and calls destroy for data source
     * element too (but only if flag closeOnlyInMemory is disabled)
     */
    private void destroyMode () {
        ((WorkspaceImpl)workspace).removeMode(this);
        if (!closeOnlyInMemory && (updater != null)) {
            try {
                updater.destroy();
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
    }
  
    /** Destroys asociated frame */
    private void destroyFrame () {
        if (isUserDefined()) {
            destroyMode();
        }
        if (frame == null) {
            return;
        }
        
        updateNameListener ();
        changeFrame(frame, null);
        
        // remember state of frame being destroyed, it will be used 
        // when frame will be displayed again, but change minimized
        // frames to normal (bugfix #11202)
        frameTypeState = frame.getState();
        if (FrameType.ICONIFIED == frameTypeState) {
            frameTypeState = FrameType.NORMAL;
        }
        // keep normal bounds, not maximized (if we are in max mode)
        bounds = getNormalBounds();
        // update hidden flag
        setHidden(true);
        
        //Fixed bug #10580 Call of frame.dispose is replanned to AWT thread
        //It is necessary because mode deserialization is done in RequestProcessor
        //when new Project is opened.
        if (SwingUtilities.isEventDispatchThread()) {
            doDisposeFrame(frame);
        } else {
            final FrameType localFrame = frame;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doDisposeFrame(localFrame);
                }
            });
        }
        //End of bugfix #10580

        FrameType old = frame;
        frame = null;
        changeSupport.firePropertyChange(PROP_FRAME_INSTANCE, old, null);
    }
    
    private void doDisposeFrame (FrameType frame) {
        // detach MDI frames from desktop pane if needed
        if(frame instanceof InternalFrameTypeImpl) {
             doRemoveInternalFrame((InternalFrameTypeImpl)frame);
        }
        //Release reference from frame to workspace
        if (frame instanceof org.netbeans.core.windows.frames.LayoutResource) {
            org.netbeans.core.windows.frames.LayoutResource lr = (org.netbeans.core.windows.frames.LayoutResource) frame;
            lr.setWorkspace(null);
        }
        //Release reference to mode.
        frame.attachToMode(null);
        frame.dispose();
    }

    /** Removes internal(i.e. desktop too). In case of <code>INTERNAL_FRAME</code>
     * also adjusts the maximized mode for the <code>workspace</code>. */
    private void doRemoveInternalFrame(InternalFrameTypeImpl internalFrame) {
        //Check for maximized internal frame
        JDesktopPane desktop = null;
        if(!(internalFrame instanceof DesktopFrameTypeImpl)) {
            if (internalFrame.isMaximum()) {
                desktop = (JDesktopPane)SwingUtilities.getAncestorOfClass(
                    JDesktopPane.class, internalFrame);
            }
        }
        
        ((WorkspaceImpl)workspace).desktopPane().removeFrameType(internalFrame);
        //Look for current top maximized frame if any
        if(desktop != null) {
            JInternalFrame[] allFrames = desktop.getAllFrames();
            if(allFrames.length > 0) {
                //Set top mode
                ModeImpl m = ((InternalFrameTypeImpl)allFrames[0]).getMode();
                m.setTopMaximized();
            } else {
                //No maximized frame left
                ((WorkspaceImpl) workspace).setTopMaximizedMode(null);
            }
        }
    }
    
    /** Destroys asociated container */
    private void destroyContainer () {
        if (tcc == null) {
            return;
        }
        changeContainer(tcc, null);
        TopComponentContainer old = tcc;
        tcc.destroy();
        tcc = null;
        changeSupport.firePropertyChange(PROP_CONTAINER_INSTANCE, old, null);
    }
    
    /** Gets title used for <code>frame</code>. Utility method. */
    String frameTitle () {
        TopComponent selectedTC = retrieveSelectedTopComponent();
        String selectedName = (selectedTC == null)
            ? "?" : selectedTC.getName(); // NOI18N
        if(selectedName == null) {
            selectedName = NbBundle.getMessage(
                ModeImpl.class, "CTL_UntitledComponent");
        }
        // solve really-single state (use name of the only TC)
        if (isSingleType()) {
            return selectedName;
        }
        // solve special states in which name of selected component should be hidden
        if((selectedTC != null) && shouldIgnoreName(selectedTC)) {
            return getDisplayName();
        }
        // other cases (normally formatted title)
        return NbBundle.getMessage(ModeImpl.class, "CTL_MultiTabTitle",
            getDisplayName(), selectedName);
    }

    /** Gets accessible desription used for the <code>frame</code> container
     * instance. Utility method. */
    private String frameDescription() {
        TopComponent selectedTC = retrieveSelectedTopComponent();
        String selectedDescription = (selectedTC == null)
            ? "?" // NOI18N
            : selectedTC.getAccessibleContext().getAccessibleDescription();
        
        if(selectedDescription == null)  {
            return null;
        }

        String modeDescription = getDescription();
        if(modeDescription == null) {
            modeDescription = NbBundle.getMessage(
                ModeImpl.class, "ACSD_DefaultModeDescription");
        }
        
        return NbBundle.getMessage(ModeImpl.class, "ACSD_FrameDescriptionFormat",
            modeDescription, selectedDescription);
    }

    /** Retrieves currently selected <code>TopComponent</code>. Helper method.
     * @return selected <code>TopCompnent</coce> or <code>null</code> */
    private TopComponent retrieveSelectedTopComponent() {
        TopComponentContainer container = getContainerInstance();
        if(container != null) {
            return container.getSelectedTopComponent();
        }
        return null;
    }
    
    // XXX used in core/output.
    public TopComponent getSelectedTopComponent() {
        return retrieveSelectedTopComponent();
    }

    /** Refreshes mode's title in title bar */
    public void updateTitle () {
        if (frame != null) {
            if (WindowTypesManager.INTERNAL_FRAME.equals(frameType)) {
                if (this.equals(((WorkspaceImpl) workspace).getTopMaximizedMode())) {
                    MainWindow.getDefault().updateTitle();
                }
            }
        }
        if (frame != null) {
            frame.setTitle(frameTitle());
            frame.getRootPaneContainer().getAccessibleContext().setAccessibleDescription(frameDescription());
        }
    }
    
    /** Attaches given lazy updater. Lazy updater keeps data on persistent
     * storage and can load/dispose data on our demand */
    public void attachUpdater (LazyUpdater updater) {
        this.updater = updater;
    }
        
    /** attaches new frame to this mode, detaches old frame. Also transfers
     * properties like icon image and state between frames */
    private void changeFrame (FrameType oldFrame, FrameType newFrame) {
        Image oldIcon = null;
        if (oldFrame != null) {
            if (origContentPane != null) {
                oldFrame.setContentPane(origContentPane);
                origContentPane = null;
            }
            oldFrame.removeFrameTypeListener(this);
            FrameTypeSupport.obtainFrameComponent(oldFrame).removeComponentListener(this);
            oldIcon = oldFrame.getIconImage();
        }
        if (newFrame != null) {
            newFrame.addFrameTypeListener(this);
            FrameTypeSupport.obtainFrameComponent(newFrame).addComponentListener(this);
            TopComponentContainer container = getContainerInstance();
            if (container != null) {
                origContentPane = newFrame.getContentPane();
                newFrame.setContentPane(container.getContentPane());
                newFrame.setTitle(frameTitle());
                newFrame.getRootPaneContainer().getAccessibleContext().setAccessibleDescription(frameDescription());
            }
            // keep icon
            if (oldIcon != null) {
                newFrame.setIconImage(oldIcon);
            }
            // keep state, will be applied during show
            if (oldFrame != null) {
                frameTypeState = oldFrame.getState();
            }
        }
    }

    /** attaches new container to this mode, detaches old container. */
    private void changeContainer (TopComponentContainer oldContainer, TopComponentContainer newContainer) {
        if (oldContainer != null) {
            oldContainer.attachToMode(null);
        }
        if (newContainer != null) {
            newContainer.attachToMode(this);
            FrameType frame = getFrameInstance();
            if (frame != null) {
                origContentPane = frame.getContentPane();
                frame.setContentPane(newContainer.getContentPane());
                frame.setTitle(frameTitle());
                frame.getRootPaneContainer().getAccessibleContext().setAccessibleDescription(frameDescription());
            }
        }
    }
    
    /** Decides whether to ignore the name of given top component or not.
    * @param tc top component to decide on
    * @return true TC containers should now ignore the name of currently
    * selected top component, false otherwise.
    * Should be called only if tcc is not null.
    */
    public boolean shouldIgnoreName (TopComponent tc) {
        if (!isSingle()) {
            return false;
        }
        
        String dName = getDisplayName();
        return (dName != null && dName.equalsIgnoreCase(tc.getName())) ||
               ((ignoredTcList != null) && ignoredTcList.contains(tc.getClass()));
    }

    /** Asigns or removes listener to the name of the top component.
    * If mode is single, its display name should be the same as
    * the name of contained component. */
    private void updateNameListener () {
        if (isSingle()) {
            if (nameListener == null) {
                nameListener = new NameListener();
            }
            nameListener.activate();
        } else {
            if (nameListener != null) {
                nameListener.passivate();
            }
        }
        // the style of mode display name is changing,
        // so notify about the change
        changeSupport.firePropertyChange(PROP_DISPLAY_NAME, null, getDisplayName());
    }

    /** Implementation of DeferredPerformer.DeferredCommand interface.
     * performs action depending on context */
    public void performCommand (DeferredPerformer.DeferredContext context) {
        Object data = context.getData();
        if (data instanceof DeferredOpenContext) {
            // opens managed top component
            DeferredOpenContext openContext = (DeferredOpenContext)(data);
            openContext.tc.open(openContext.workspace);
        } else if (data instanceof String) {
            // sets frame type of the mode
            doSetFrameType((String)data);
        }
    }

    /** @return true if it is in 'closable' state, it means that no modal
    * windows are opened at that time
    * (it's an ugly HACK, needed on unixes to prevent top component container
    * from closing during the time when modal dialog is shown)
    */
    private boolean isInClosableState () {
        return NbPresenter.currentModalDialog == null;
    }
    
    /** Loads specified data section if updater exists and if section
     * wasn't loaded already */
    private boolean ensureSectionLoaded (int section) {
        if (updater == null) {
            return false;
        }
        try {
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
        }
        return true;
    }

    /** @return string description of this mode */
    public String toString () {
        return super.toString () + "[" + getName () 
        + ", workspace " + ((workspace != null) ? workspace.getName() : "null") + "]";
    }
    
    /** Holds data context for delayed opening */
    private static final class DeferredOpenContext {
        DeferredOpenContext() {}
        TopComponent tc;
        Workspace workspace;
    }
    
    /** Handles mode's display name refresh based upon top components' name
     * changes */
    private final class NameListener implements PropertyChangeListener {
        NameListener() {}

        WindowManagerImpl.TopComponentManager tcm;

        void activate () {
            if (tcm == null) {
                tcm = WindowManagerImpl.findManager(tcc.getTopComponents()[0]);
                tcm.addPropertyChangeListener(this);
            }
        }

        void passivate () {
            if (tcm != null) {
                tcm.removePropertyChangeListener(this);
                tcm = null;
            }
        }

        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (WindowManagerImpl.TopComponentManager.PROP_NAME.equals(propName)) {
                // name of top component changed, update title 
                // and fire display name change
                updateFrameTitle();
                
                changeSupport.firePropertyChange(
                    PROP_DISPLAY_NAME,
                    evt.getOldValue(),
                    evt.getNewValue()
                );
            }
        }
        
        private void updateFrameTitle() {
            final FrameType f = frame;
            if (f == null) {
                return;
            }
            
            final String title = frameTitle();
            if(SwingUtilities.isEventDispatchThread()) {
                f.setTitle(title);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        f.setTitle(title);
                    }
                });
            }
        }

    } // end of NameListener inner class

    public static final class ModeDefinition {
        String  name;
        String  displayName;
        URL     iconURL;
        String  containerType;
        Object  constraints;
    }
    
    private static final class ClosedTCContext {
        Object  constraints;
        int orderWeight;
        
        ClosedTCContext(Object constraints, int orderWeight) {
            this.constraints = constraints;
            this.orderWeight = orderWeight;
        }
    }
    
/************** Serialization ********/    
    
    /** Let instance of SerializationReplacer to deal with persistence of the
     * Mode */
    private Object writeReplace () throws ObjectStreamException {
        return new SerializationReplacer(this);
    }

    /** Called when first phase of WS deserialization is done.
    * Validates asociation with its workspace.
    */
    void validateSelf (Workspace workspace) {
        this.workspace = workspace;
    }

    /** Called when first phase of WS deserialization is done.
    * Validates its top component container, if possible
    */
    void validateData () {
        // validate container, if possible
        if (tcc != null) {
            tcc.validateData();

            // swap tcc if needed: multitabbed -> split after deserialization
            if (tcc instanceof MultiTabbedContainerImpl &&
                null != containerType &&
                containerType.equals(WindowTypesManager.SPLIT_CONTAINER)) {
                TopComponentContainer newTcc = createContainer(containerType);
                TopComponent tcs[] = tcc.getTopComponents();
                
                newTcc.attachToMode(this);
                for(int i = 0; i < tcs.length; i++) {
                    newTcc.addTopComponent(tcs[i], true, PerimeterLayout.CENTER);
                }
                tcc = newTcc;
            }
            
            if (tcc.getTopComponents().length > 0) {
                //Store current top selected component
                final TopComponent tc = tcc.getSelectedTopComponent();
                // put a request for reopening all top components in
                // the container (actual opening is delayed and performed
                // after deserializaton)
                TopComponent[] tcs = tcc.getTopComponents();
                DeferredPerformer deferredPerformer = DeferredPerformer.getDefault();
                DeferredOpenContext openContext = null;
                for (int i = 0; i < tcs.length; i++) {
                    openContext = new DeferredOpenContext();
                    openContext.tc = tcs[i];
                    openContext.workspace = workspace;
                    deferredPerformer.putRequest(
                        this, new DeferredPerformer.DeferredContext(openContext, true)
                    );
                }
                //Bugfix 9579, 21 Feb 2001 by Marek Slama
                //Method requestFocus is called when ALL top components are opened
                //and added to container.
                //Every addComponent calls setSelectedTopComponent so in the end we must
                //make sure that correct top component is selected in container.
                if (tc != null) {
                    deferredPerformer.putRequest(
                        new DeferredPerformer.DeferredCommand () {
                            public void performCommand (DeferredPerformer.DeferredContext dc) {
                                synchronized (ModeImpl.this) {
                                    // check validity of request, request may become
                                    // invalid (container already destroyed) due to delayed processing
                                    if (tcc == null) {
                                        return;
                                    }
                                    requestFocus(tc);
                                }
                            }
                        },
                        new DeferredPerformer.DeferredContext(null, true)
                    );
                }
                //End
            } else {
                tcc = null;
            }
        }
        // validate frame
        if (frame != null) {
            frame.addFrameTypeListener(this);
            FrameTypeSupport.obtainFrameComponent(frame).addComponentListener(this);
        }
        // fire changes
        changeSupport.firePropertyChange(PROP_CONTAINER_INSTANCE, null, tcc);
        changeSupport.firePropertyChange(PROP_FRAME_INSTANCE, null, frame);
        changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
    }

    /** Class that acts as serialization manager for mode implementation */
    private static final class SerializationReplacer implements Serializable {
        /** Unique ID */
        static final long serialVersionUID =1230255666898346575L;
        /** Description of serializable fields for mode */
        private static final String NAME = "name"; // NOI18N
        private static final String DISPLAY_NAME = "displayName"; // NOI18N
        private static final String BOUNDS = "bounds"; // NOI18N
        private static final String ICON_URL = "iconURL"; // NOI18N
        private static final String USER_DEFINED = "userDefined"; // NOI18N
        private static final String WORKSPACE_NAME = "workspaceName"; // NOI18N
        private static final String TC_CONTAINER = "tcContainer"; // NOI18N
        private static final String FRAME_TYPE = "frameType"; // NOI18N
        private static final String FRAME_TYPE_STATE = "frameTypeState"; // NOI18N
        private static final String CONTAINER_TYPE = "containerType"; // NOI18N
        private static final String CONSTRAINTS = "constraints"; // NOI18N
        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField(NAME, String.class),
            new ObjectStreamField(DISPLAY_NAME, String.class),
            new ObjectStreamField(BOUNDS, Rectangle.class),
            new ObjectStreamField(ICON_URL, URL.class),
            new ObjectStreamField(USER_DEFINED, Boolean.class),
            new ObjectStreamField(WORKSPACE_NAME, String.class),
            new ObjectStreamField(TC_CONTAINER, TopComponentContainer.class),
            new ObjectStreamField(FRAME_TYPE, String.class),
            new ObjectStreamField(FRAME_TYPE_STATE, Integer.class),
            new ObjectStreamField(CONTAINER_TYPE, String.class),
            new ObjectStreamField(CONSTRAINTS, Object.class)
        };

        /** backward compatibility, we must recognize old container's name */
        private static final String OLD_SPLIT_CONTAINER = "org.netbeans.core.windows.frames.SplittedContainerImpl"; // NOI18N
        
        /** Asociation with container which data needs to be serialized */
        transient ModeImpl source;
        
        SerializationReplacer (ModeImpl source) {
            this.source = source;
        }

        /** Serialization of all workspaces */
        private void writeObject (ObjectOutputStream oos)
                     throws IOException {
            // write fields
            ObjectOutputStream.PutField pf = oos.putFields();
            pf.put(NAME, source.getName());
            pf.put(DISPLAY_NAME, source.getDisplayName());
            pf.put(BOUNDS, source.getNormalBounds());
            pf.put(ICON_URL, source.getIconURL());
            pf.put(USER_DEFINED, source.isUserDefined() ? Boolean.TRUE : Boolean.FALSE);
            pf.put(WORKSPACE_NAME, source.workspace.getName());
            pf.put(TC_CONTAINER, source.tcc);
            pf.put(FRAME_TYPE, source.getFrameType());
            pf.put(FRAME_TYPE_STATE, new Integer(source.getFrameState()));
            pf.put(CONTAINER_TYPE, source.getContainerType());
            pf.put(CONSTRAINTS, source.getConstraints());
            oos.writeFields();
        }

        /** Deserialization of the workspace */
        private void readObject (ObjectInputStream ois)
                     throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = ois.readFields();
            String name = (String)gf.get(NAME, null);
            String workspaceName = (String)gf.get(WORKSPACE_NAME, null);
            Workspace workspace = WindowManager.getDefault().findWorkspace(workspaceName);
            source = (workspace == null) ? null : (ModeImpl)workspace.findMode(name);
            
            if (source == null) {
                // mode don't exist, create new one
                source = new ModeImpl(
                    name, (String)gf.get(DISPLAY_NAME, null),
                    (URL)gf.get(ICON_URL, null),
                    ((Boolean)gf.get(USER_DEFINED, null)).booleanValue(),
                    workspace
                );
            } else {
                // old mode content cleanup
                source.close();
                source.destroyFrame();
                source.destroyContainer();
                source.setDisplayName((String)gf.get(DISPLAY_NAME, null));
            }
            
            Rectangle bounds = (Rectangle)gf.get(BOUNDS, null);
            source.setBounds(bounds == null ? new Rectangle() : bounds);
            source.frameType = (String)gf.get(FRAME_TYPE, null);
            
            source.containerType = (String)gf.get(CONTAINER_TYPE, null);
            if (OLD_SPLIT_CONTAINER.equals(source.containerType)) {
                source.containerType = WindowTypesManager.SPLIT_CONTAINER;
            }
            source.constraints = (Object)gf.get(CONSTRAINTS, null);
            source.tcc = (TopComponentContainer)gf.get(TC_CONTAINER, null);
            // assign workspace if needed for later validation
            if (workspace == null) {
                source.workspaceName = workspaceName;
            }

            if (null != source.frameType &&
                WindowUtils.hasObjectStreamField(gf, FRAME_TYPE_STATE)) {
                // current format - read the state of frame: iconified, normal, maximized
                source.frameTypeState = 
                    ((Integer)gf.get(FRAME_TYPE_STATE, new Integer(FrameType.NORMAL))).intValue();
            }
        }
    
        /** Resolves deserialized SerializationReplacer to the singleton
        * instance of WindowManagerImpl */
        private Object readResolve () throws ObjectStreamException {
            return source;                            
        }

    } // end of SerializationReplacer
    
/******** Old serialization, must be here for compatibility with 3.1 and older versions */    
    
    /** Accessor to the versioned serialization manager */
    private VersionSerializator serializationManager () {
        if (serializationManager == null) {
            serializationManager = createSerializationManager();
        }
        return serializationManager;
    }

    /** Creates new serialization manager filled with our versions */
    private static VersionSerializator createSerializationManager () {
        VersionSerializator result = new VersionSerializator();
        result.putVersion(new Version1());
        result.putVersion(new Version2());
        return result;
    }
        
    // bugfix #14396 by Jiri Rechtacek
    /** Compares modes by the display's names */
    // order: mode with null-display name after mode with any name
    // then by display name
    public int compareTo(Object obj) {
        String arg1, arg2;
        arg1 = getDisplayName();
        if(obj instanceof Mode)
            arg2 = ((Mode)obj).getDisplayName();
        else
            arg2 = null;
        if(arg2==null) {
            if(arg1==null)
                // both with null-name, equal
                return 0;
            else
                // any name before null-name
                return 1;
        } else {
            if(arg1==null)
                // null-name after any name
                return -1;
            else
                // compare by names
                return arg1.compareTo(arg2);
        }
    }

    /** Basic version of persistence for mode implementation.
    * Method assignData(modeImpl) must be called prior to serialization */
    private static class Version1
        implements DefaultReplacer.ResVersionable {
        Version1() {}

        /* identification string */
        public static final String NAME = "Version_1.0"; // NOI18N

        /** variables of persistent state of the mode implementation */
        String name;
        String displayName;
        Rectangle bounds;
        URL icon;
        int containerType;
        boolean userDefined;
        String workspaceName;
        TopComponentContainer tcc;

        /** asociation with mode implementation, used when writing */
        ModeImpl mode;

        /** Identification of the version */
        public String getName () {
            return NAME;
        }

        /** Assigns data to be written. Must be called before writing */
        public void assignData (ModeImpl mode) {
            this.mode = mode;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            // read mode fields
            name = (String)in.readObject();
            displayName = (String)in.readObject();
            bounds = (Rectangle)in.readObject();
            icon = (URL)in.readObject();
            containerType = ((Integer)in.readObject()).intValue();
            userDefined = ((Boolean)in.readObject()).booleanValue();
            workspaceName = (String)in.readObject();
            tcc = (TopComponentContainer)in.readObject();
        }

        /** Should be never called */
        public void writeData (ObjectOutput out) throws IOException {
            throw new InternalError();
        }

        public Object resolveData ()
        throws ObjectStreamException {
            Workspace workspace = WindowManager.getDefault().findWorkspace(workspaceName);
            ModeImpl result =
                (workspace == null) ? null : (ModeImpl)workspace.findMode(name);

            if (result == null) {
                // mode don't exist, create new one and fill it
                result = new ModeImpl(name, displayName, icon, 
                                        userDefined, workspace);
                // assign workspace if needed for later validation
                if (workspace == null) {
                    result.workspaceName = workspaceName;
                }
            }

            if (null != result) {
                result.bounds =
                    (bounds == null ? new Rectangle() : bounds);
                result.tcc = tcc;
            }
            return result;
        }

    } // end of Version1 inner class

    /** Added list of ignored top component classes */
    private static class Version2 extends Version1 {
        Version2() {}

        /* identification string */
        public static final String NAME = "Version_2.0"; // NOI18N

        /** variables of persistent state of the mode implementation */
       List ignoredTcList;

        /** Identification of the version */
        public String getName () {
            return NAME;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            // read version 1
            super.readData(in);
            // read list
            // things are messy here, because we must handle different
            // protocols. either there will be list of Class objects or
            // the list of Strings
            ArrayList readList = (ArrayList)in.readObject();
            if ((readList == null) || (readList.size() <= 0) ||
                (readList.get(0) instanceof Class)) {
                // old protocol
                ignoredTcList = readList;
            } else {
                // new protocol, convert strings back to Classes
                ignoredTcList = new ArrayList(readList.size());
                String curName = null;
                for (Iterator iter = readList.iterator(); iter.hasNext(); ) {
                    curName = (String)iter.next();
                    ignoredTcList.add(
                        Class.forName(curName, false,
                            (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class))
                    );
                }
            }
        }

        /** write the data of the version to given output */
        public void writeData (ObjectOutput out)
        throws IOException {
            // write version 1
            super.writeData(out);
            // write list of names of ignored classes. do not save classes
            // because it can cause versioning problems
            ArrayList ignoredNames = null;
            if (mode.ignoredTcList != null) {
                ignoredNames = new ArrayList(mode.ignoredTcList.size());
                String curName = null;
                for (Iterator iter = mode.ignoredTcList.iterator(); iter.hasNext(); ) {
                    curName = ((Class)iter.next()).getName();
                    ignoredNames.add(curName);
                }
            }
            out.writeObject(ignoredNames);
        }

        public Object resolveData ()
        throws ObjectStreamException {
            ModeImpl result = (ModeImpl)super.resolveData();
            // add ignored list to the deserialization result, if possible
            if (result.ignoredTcList == null) {
                result.ignoredTcList = ignoredTcList;
            } else {
                if (ignoredTcList != null) {
                    result.ignoredTcList.addAll(ignoredTcList);
                }
            }
            return result;
        }

    } // end of Version2 inner class


    /** Implementation of persistent access to our version serializator */
    private static final class VSAccess implements DefaultReplacer.Access {
        /** version serializator, used only during writing */
        transient VersionSerializator vs;

        /** serialVersionUID */
        private static final long serialVersionUID = -7577235918945664917L;

        public VSAccess (VersionSerializator vs) {
            this.vs = vs;
        }

        public VersionSerializator getVersionSerializator () {
            return (vs == null) ? createSerializationManager() : vs;
        }

    } // end of VSAccess inner class
}
