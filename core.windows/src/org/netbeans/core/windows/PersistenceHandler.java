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


import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.netbeans.core.windows.persistence.GroupConfig;
import org.netbeans.core.windows.persistence.ModeConfig;
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.netbeans.core.windows.persistence.PersistenceObserver;
import org.netbeans.core.windows.persistence.TCGroupConfig;
import org.netbeans.core.windows.persistence.TCRefConfig;
import org.netbeans.core.windows.persistence.WindowManagerConfig;

import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/** 
 * Class which handles loading, saving of window system and persistence events.
 *
 * @author  Peter Zavadsky, Marek Slama
 */
final class PersistenceHandler implements PersistenceObserver {

    // Persistence data
    /** Maps mode config name to mode instance. */
    private final Map name2mode = new WeakHashMap(10);
    /** Maps group config name to group instance. */
    private final Map name2group = new WeakHashMap(10);
//    /** Mpas TopComponent id to TopComponent instance. */
//    private final Map id2tc = new WeakHashMap(10);

    private static PersistenceHandler defaultInstance;
    
    
    /** Creates a new instance of PersistenceHanlder */
    private PersistenceHandler() {
    }

    
    public static synchronized PersistenceHandler getDefault() {
        if(defaultInstance == null) {
            defaultInstance = new PersistenceHandler();
        }
        
        return defaultInstance;
    }

    // XXX helper method
    public boolean isTopComponentPersistentWhenClosed(TopComponent tc) {
        Object prop = tc.getClientProperty(PersistenceManager.PERSISTENCE_TYPE);
        
        return prop == null
               || (!PersistenceManager.NEVER_PERSISTENT.equals(prop)
               && !PersistenceManager.ONLY_OPENED_PERSISTENT.equals(prop));
    }
    
    
    public synchronized void load() {
        debugLog("## PersistenceHandler.load"); // NOI18N
        
        WindowManagerConfig wmc = PersistenceManager.getDefault().loadWindowSystem();

        // In case of persistence problem fall back to predefined settings.
        if(wmc == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new NullPointerException("Cannot load window system persistent data." // NOI18N
                    + " Using internally predefined configuration")); // NOI18N
            wmc = ConfigFactory.createDefaultConfig();
        }

        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        if (wmc.tcIdViewList.length > 0) {
            List tcList = new ArrayList(wmc.tcIdViewList.length);
            for (int i = 0; i < wmc.tcIdViewList.length; i++) {
                TopComponent tc = getTopComponentForID(wmc.tcIdViewList[i]);
                if (tc != null) {
                    tcList.add(tc);
                }
            }
            TopComponent [] tcs = (TopComponent []) tcList.toArray(new TopComponent[tcList.size()]);
            wm.setRecentViewList(tcs);
        } else {
            //No recent view list is saved, fill it by opened TopComponents
            List tcList = new ArrayList();
            for (int i = 0; i < wmc.modes.length; i++) {
                ModeConfig mc = wmc.modes[i];
                for (int j = 0; j < mc.tcRefConfigs.length; j++) {
                    //Only opened
                    if (mc.tcRefConfigs[j].opened) {
                        TopComponent tc = getTopComponentForID(mc.tcRefConfigs[j].tc_id);
                        if (tc != null) {
                            tcList.add(tc);
                        }
                    }
                }
            }
            TopComponent [] tcs = (TopComponent []) tcList.toArray(new TopComponent[tcList.size()]);
            wm.setRecentViewList(tcs);
        }
        
        wm.setEditorAreaConstraints(wmc.editorAreaConstraints);
        wm.setEditorAreaState(wmc.editorAreaState);

        ModeImpl activeMode    = null;
        ModeImpl maximizedMode = null;
        
        // First create empty modes.
        Map mode2config = new HashMap();
        for (int i = 0; i < wmc.modes.length; i++) {
            ModeConfig mc = (ModeConfig) wmc.modes[i];
            ModeImpl mode = getModeFromConfig(mc);
            
            mode2config.put(mode, mc);
            
            if(mc.name.equals(wmc.activeModeName)) {
                activeMode = mode;
            }
            if(mc.name.equals(wmc.maximizedModeName)) {
                maximizedMode = mode;
            }
        }
        
        // Then fill them with TopComponents.
        for(Iterator it = mode2config.keySet().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            ModeConfig mc = (ModeConfig)mode2config.get(mode);
            initModeFromConfig(mode, mc);

            // Set selected TopComponent.
            if(mc.selectedTopComponentID != null) {
                mode.setUnloadedSelectedTopComponent(mc.selectedTopComponentID);
            }
        }
        
        //Initialize top component groups
        for (int i = 0; i < wmc.groups.length; i++) {
            GroupConfig groupCfg = wmc.groups[i];
            createTopComponentGroupFromConfig(groupCfg);
        }

        // XXX #37188 Hot fix for the problem with initing active
        // component (even it is not on screen yet).
        if(activeMode != null) {
            TopComponent active = activeMode.getSelectedTopComponent();
            if(active != null) {
                try {
                    WindowManagerImpl.getInstance().componentShowing(active);
                } catch(RuntimeException re) {
                    IllegalStateException ise = new IllegalStateException("[Winsys] TopComponent tc=" + active // NOI18N
                    + " throws runtime exception from its componentShowing method. Repair it!"); // NOI18N
                    ErrorManager.getDefault().annotate(ise, re);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ise);
                }
            }
        }
        
        wm.setActiveMode(activeMode);
        wm.setMaximizedMode(maximizedMode);

        Rectangle joinedBounds = computeBounds(
            wmc.centeredHorizontallyJoined,
            wmc.centeredVerticallyJoined,
            wmc.xJoined,
            wmc.yJoined,
            wmc.widthJoined,
            wmc.heightJoined,
            wmc.relativeXJoined,
            wmc.relativeYJoined,
            wmc.relativeWidthJoined,
            wmc.relativeHeightJoined);
        if(joinedBounds != null) {
            wm.setMainWindowBoundsJoined(joinedBounds);
        }
        // PENDING else { ...some default value?
        
        Rectangle separatedBounds = computeBounds(
            wmc.centeredHorizontallySeparated,
            wmc.centeredVerticallySeparated,
            wmc.xSeparated,
            wmc.ySeparated,
            wmc.widthSeparated,
            wmc.heightSeparated,
            wmc.relativeXSeparated,
            wmc.relativeYSeparated,
            wmc.relativeWidthSeparated,
            wmc.relativeHeightSeparated);
        if(separatedBounds != null) {
            wm.setMainWindowBoundsSeparated(separatedBounds);
        }
        // PENDING else { ...some default value?

        wm.setMainWindowFrameStateJoined(wmc.mainWindowFrameStateJoined);
        wm.setMainWindowFrameStateSeparated(wmc.mainWindowFrameStateSeparated);
        
        wm.setEditorAreaBounds(wmc.editorAreaBounds);
        wm.setEditorAreaFrameState(wmc.editorAreaFrameState);
        
    }
    
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. */
    public synchronized void save() {
        debugLog("## PersistenceHandler.save"); // NOI18N
        
        WindowManagerConfig wmc = getConfig();
        PersistenceManager.getDefault().saveWindowSystem(wmc);
    }

    private ModeImpl getModeFromConfig(ModeConfig mc) {
        debugLog("Getting mode name=" + mc.name);

        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(mc.name);
        if(mode == null) {
            mode = createModeFromConfig(mc);
        }
        
        return initModeFromConfig(mode, mc);
    }
    
    private ModeImpl createModeFromConfig(ModeConfig mc) {
        debugLog(""); // NOI18N
        debugLog("Creating mode name=\"" + mc.name + "\""); // NOI8N
        
        ModeImpl mode = WindowManagerImpl.getInstance().createMode(
            mc.name, mc.kind, mc.permanent, mc.constraints);


        name2mode.put(mc.name, mode);
        
        return mode;
    }
    
    private ModeImpl initModeFromConfig(ModeImpl mode, ModeConfig mc) {
        for (int j = 0; j < mc.tcRefConfigs.length; j++) {
            TCRefConfig tcRefConfig = (TCRefConfig) mc.tcRefConfigs[j];
            debugLog("\tTopComponent[" + j + "] id=\"" // NOI18N
                + tcRefConfig.tc_id + "\", \topened=" + tcRefConfig.opened); // NOI18N

            // PENDING
            if (tcRefConfig.opened) {
                TopComponent tc = getTopComponentForID(tcRefConfig.tc_id);
                if(tc != null) {
                    mode.addOpenedTopComponent(tc);
                }
            } else {
                mode.addUnloadedTopComponent(tcRefConfig.tc_id);
            }
        }

        mode.setBounds(mc.bounds);
        mode.setFrameState(mc.frameState);
        
        return mode;
    }
    
    TopComponent getTopComponentForID(String tc_id) {
        if(tc_id == null || "".equals(tc_id)) {
            return null;
        }
        
        long start = System.currentTimeMillis();
        TopComponent tc = PersistenceManager.getDefault().getTopComponentForID(tc_id);
        debugLog("***Getting TopComponent for ID=" + tc_id + " in " + (System.currentTimeMillis() - start) + " ms"); // NOI18N
        
        return tc;
    }
    
    private TopComponentGroupImpl createTopComponentGroupFromConfig(GroupConfig groupCfg) {
        debugLog(""); // NOI18N
        debugLog("Creating group name=\"" + groupCfg.name + "\" \t[opened=" + groupCfg.opened + "]"); // NOI18N
        
        TopComponentGroupImpl tcGroup = new TopComponentGroupImpl(groupCfg.name, groupCfg.opened);

        name2group.put(groupCfg.name, tcGroup);
        
        for (int j = 0; j < groupCfg.tcGroupConfigs.length; j++) {
            TCGroupConfig tcGroupCfg = groupCfg.tcGroupConfigs[j];
            debugLog("\tTopComponent[" + j + "] id=\"" // NOI18N
                + tcGroupCfg.tc_id + "\", \topen=" + tcGroupCfg.open + ", \tclose=" + tcGroupCfg.close
                + ", \twasOpened=" + tcGroupCfg.wasOpened); // NOI18N

            tcGroup.addUnloadedTopComponent(tcGroupCfg.tc_id);

            if (tcGroupCfg.open) {
                tcGroup.addUnloadedOpeningTopComponent(tcGroupCfg.tc_id);
            }
            if (tcGroupCfg.close) {
                tcGroup.addUnloadedClosingTopComponent(tcGroupCfg.tc_id);
            }

            // Handle also wasOpened flag.
            if(groupCfg.opened && tcGroupCfg.wasOpened) {
                tcGroup.addGroupUnloadedOpenedTopComponent(tcGroupCfg.tc_id);
            }
        }
        
        WindowManagerImpl.getInstance().addTopComponentGroup(tcGroup);

        return tcGroup;
    }


    
    private WindowManagerConfig getConfig() {
        
        WindowManagerConfig wmc = new WindowManagerConfig();
        
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        
        Rectangle joinedBounds = wm.getMainWindowBoundsJoined();
        debugLog("joinedBouds=" + joinedBounds); // NOI18N
        wmc.xJoined      = joinedBounds.x;
        wmc.yJoined      = joinedBounds.y;
        wmc.widthJoined  = joinedBounds.width;
        wmc.heightJoined = joinedBounds.height;
        Rectangle separatedBounds = wm.getMainWindowBoundsSeparated();
        debugLog("separatedBounds=" + separatedBounds); // NOI18N
        wmc.xSeparated      = separatedBounds.x;
        wmc.ySeparated      = separatedBounds.y;
        wmc.widthSeparated  = separatedBounds.width;
        wmc.heightSeparated = separatedBounds.height;
        
        wmc.mainWindowFrameStateJoined = wm.getMainWindowFrameStateJoined();
        debugLog("mainWindowFrameStateJoined=" + wmc.mainWindowFrameStateJoined); // NOI18N
        wmc.mainWindowFrameStateSeparated = wm.getMainWindowFrameStateSeparated();
        debugLog("mainWindowFrameStateSeparated=" + wmc.mainWindowFrameStateSeparated); // NOI18N

        wmc.editorAreaState = wm.getEditorAreaState();
        debugLog("editorAreaState=" + wmc.editorAreaState); // NOI18N
        wmc.editorAreaBounds = wm.getEditorAreaBounds();
        debugLog("editorAreaBounds=" + wmc.editorAreaBounds); // NOI18N
        wmc.editorAreaConstraints = wm.getEditorAreaConstraints();
        debugLog("editorAreaConstraints=" + wmc.editorAreaConstraints); // NOI18N
        wmc.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        ModeImpl mo = wm.getActiveMode();
        debugLog("active mode=" + mo); // NOI18N
        if (mo != null) {
            wmc.activeModeName = mo.getName();
        }
        
        mo = wm.getMaximizedMode();
        debugLog("maximized mode=" + mo); // NOI18N
        if (mo != null) {
            wmc.maximizedModeName = mo.getName();
        }
        
        wmc.toolbarConfiguration = wm.getToolbarConfigName();
        debugLog("toolbarConfiguration=" + wmc.toolbarConfiguration); // NOI18N
        
        PersistenceManager pm = PersistenceManager.getDefault();
        
        Set modeSet = wm.getModes();
        
        ModeConfig [] modeCfgArray = new ModeConfig[modeSet.size()];
        ModeConfig modeCfg;
        int i = 0;
        for (Iterator it = modeSet.iterator(); it.hasNext(); i++) {
            mo = (ModeImpl) it.next();
            modeCfg = new ModeConfig();
            modeCfg.name = mo.getName();
            debugLog(""); // NOI18N
            debugLog("mode name=" + modeCfg.name); // NOI18N
            modeCfg.state = mo.getState();
            debugLog("mode state=" + modeCfg.state); // NOI18N
            
            modeCfg.kind = mo.getKind();
            debugLog("mode kind=" + modeCfg.kind); // NOI18N
            modeCfg.constraints = mo.getConstraints();
            debugLog("mode constraints=" + modeCfg.constraints); // NOI18N
            // PENDING Whether to save relative or absolute bounds.
            // In case of relative, they would need to be computed.
            Rectangle relBounds = null;
            if (relBounds != null) {
                modeCfg.relativeBounds = relBounds;
            } else {
                modeCfg.bounds = mo.getBounds();
                debugLog("mode bounds=" + modeCfg.bounds); // NOI18N
            }
            modeCfg.frameState = mo.getFrameState();
            debugLog("mode frame state=" + modeCfg.frameState); // NOI18N
                
            TopComponent tc = mo.getSelectedTopComponent();
            if (tc != null) {
                if (pm.isTopComponentPersistent(tc)) {
                    try {
                        String tc_id = pm.getTopComponentPersistentIDAndSave(tc);
                        debugLog("selected tc=" + tc.getName()); // NOI18N 
                        if(tc_id != null) {
                            modeCfg.selectedTopComponentID = tc_id;
                        }
                    } catch (NotSerializableException nse) {
                        //Ignore: Some instances of some TopComponents like for example
                        //OutputTabTerm does not want to be serialized even if they declare
                        //they are serializable so isTopComponentPersistent() return true.
                    } catch (IOException ioe) {
                        //Not able to get top component id
                        ErrorManager em = ErrorManager.getDefault();
                        em.annotate(ioe, "No tc_id for TopComponent:[" + tc.getName() + "] " + tc // NOI18N
                        + " Class:[" + tc.getClass().getName() + "]"); // NOI18N
                        em.notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                }
            }
            modeCfg.permanent = mo.isPermanent();
            debugLog("mode permanent=" + modeCfg.permanent); // NOI18N
            
            //TopComponents
            TopComponent [] tcs = mo.getTopComponents();
            List tcRefCfgList = new ArrayList(tcs.length);
            TCRefConfig tcRefCfg;
            for (int j = 0; j < tcs.length; j++) {
                if (pm.isTopComponentPersistent(tcs[j])) {
                    String tc_id;
                    try {
                        tc_id = pm.getTopComponentPersistentIDAndSave(tcs[j]);
                        debugLog("tc="+tcs[j].getName()); // NOI18N
                    } catch (NotSerializableException nse) {
                        //Ignore: Some instances of some TopComponents like for example
                        //OutputTabTerm does not want to be serialized even if they declare
                        //they are serializable so isTopComponentPersistent() return true.
                        continue;
                    } catch (IOException ioe) {
                        //Not able to get top component id
                        ErrorManager em = ErrorManager.getDefault();
                        em.annotate(ioe, "No tc_id for TopComponent [" + tc.getName() + "] " + tc); // NOI18N
                        em.notify(ErrorManager.INFORMATIONAL, ioe);
                        continue;
                    }
                    tcRefCfg = new TCRefConfig();
                    tcRefCfg.tc_id = tc_id;
                    tcRefCfg.opened = tcs[j].isOpened();
                    //XXX previous mode where to get????
                    tcRefCfgList.add(tcRefCfg);
                }
            }
            modeCfg.tcRefConfigs = (TCRefConfig []) tcRefCfgList.toArray(new TCRefConfig[tcRefCfgList.size()]);
            modeCfgArray[i] = modeCfg;
        }
        wmc.modes = modeCfgArray;
        
        //Sets
        Set tcGroups = wm.getTopComponentGroups();
        GroupConfig [] groupCfgArray = new GroupConfig[tcGroups.size()];
        i = 0;
        for (Iterator it1 = tcGroups.iterator(); it1.hasNext(); i++) {
            GroupConfig groupCfg = new GroupConfig();
            TopComponentGroupImpl tcGroup = (TopComponentGroupImpl)it1.next();
            groupCfg.name = tcGroup.getName();
            groupCfg.opened = tcGroup.isOpened();
            debugLog(""); // NOI18N
            debugLog("group name=" + groupCfg.name); // NOI18N
            Set openSet = tcGroup.getOpeningSet();
            Set closeSet = tcGroup.getClosingSet();
            Set wasOpenedSet = tcGroup.getGroupOpenedTopComponents();
            
            Map tcGroupCfgMap = new HashMap();
            
//            for (Iterator it2 = openSet.iterator(); it2.hasNext(); ) {
            for (Iterator it2 = tcGroup.getTopComponents().iterator(); it2.hasNext(); ) {
                TopComponent tc = (TopComponent) it2.next();
                String tc_id;
                TCGroupConfig tcGroupCfg;
                if (pm.isTopComponentPersistent(tc)) {
                    try {
                        tc_id = pm.getTopComponentPersistentIDAndSave(tc);
                    } catch (NotSerializableException nse) {
                        //Ignore: Some instances of some TopComponents like for example
                        //OutputTabTerm does not want to be serialized even if they declare
                        //they are serializable so isTopComponentPersistent() return true.
                        continue;
                    } catch (IOException ioe) {
                        //Not able to get top component id
                        ErrorManager em = ErrorManager.getDefault();
                        em.annotate(ioe, "No tc_id for TopComponent [" + tc.getName() + "] " + tc); // NOI18N
                        em.notify(ErrorManager.INFORMATIONAL, ioe);
                        continue;
                    }
                    if (tcGroupCfgMap.containsKey(tc_id)) {
                        tcGroupCfg = (TCGroupConfig) tcGroupCfgMap.get(tc_id);
                    } else {
                        tcGroupCfg = new TCGroupConfig();
                        tcGroupCfg.tc_id = tc_id;
                        tcGroupCfgMap.put(tc_id, tcGroupCfg);
                    }
                    tcGroupCfg.open  = openSet.contains(tc);
                    tcGroupCfg.close = closeSet.contains(tc);
                    if(groupCfg.opened) {
                        tcGroupCfg.wasOpened = wasOpenedSet.contains(tc);
                    }
                    debugLog("tc id=" + tcGroupCfg.tc_id // NOI18N
                        + ", open=" + tcGroupCfg.open // NOI18N
                        + ", close=" + tcGroupCfg.close // NOI18N
                        + ", wasOpened=" + tcGroupCfg.wasOpened); // NOI18N
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("TopComponent of class=" + tc.getClass() + ", name=" + tc.getName() // NOI18N
                            + " is not persistent. Can't be saved as a member of group name=" + groupCfg.name)); // NOI18N
                }
            }
            
            int j = 0;
            TCGroupConfig [] tcGroupCfgArray = new TCGroupConfig[tcGroupCfgMap.size()];
            for (Iterator it4 = tcGroupCfgMap.keySet().iterator(); it4.hasNext(); j++) {
                tcGroupCfgArray[j] = (TCGroupConfig) tcGroupCfgMap.get(it4.next());
            }
            groupCfg.tcGroupConfigs = tcGroupCfgArray;
            groupCfgArray[i] = groupCfg;
        }
        wmc.groups = groupCfgArray;
        
        //RecentViewList
        TopComponent [] tcs = wm.getRecentViewList();
        List tcIdList = new ArrayList(tcs.length);
        for (i = 0; i < tcs.length; i++) {
            String tc_id = null;
            if (pm.isTopComponentPersistent(tcs[i])) {
                try {
                    tc_id = pm.getTopComponentPersistentIDAndSave(tcs[i]);
                } catch (NotSerializableException nse) {
                    //Ignore: Some instances of some TopComponents like for example
                    //OutputTabTerm does not want to be serialized even if they declare
                    //they are serializable so isTopComponentPersistent() return true.
                    continue;
                } catch (IOException ioe) {
                    //Not able to get top component id
                    ErrorManager em = ErrorManager.getDefault();
                    em.annotate(ioe, "No tc_id for TopComponent:[" + tcs[i].getName() + "] " + tcs[i] // NOI18N
                    + " Class:[" + tcs[i].getClass().getName() + "]"); // NOI18N
                    em.notify(ErrorManager.INFORMATIONAL, ioe);
                    continue;
                }
                if (tc_id != null) {
                    tcIdList.add(tc_id);
                }
            }
        }
        wmc.tcIdViewList = (String []) tcIdList.toArray(new String [tcIdList.size()]);
        
        return wmc;
    }

    
    
    /** Handles adding mode to model.
     * @param modeConfig configuration data of added mode
     */
    public synchronized void modeConfigAdded(ModeConfig modeConfig) {
        debugLog("WMI.modeConfigAdded mo:" + modeConfig.name); // NOI18N
        getModeFromConfig(modeConfig);
    }
    
    /** Handles removing mode from model.
     * @param modeName unique name of removed mode
     */
    public synchronized void modeConfigRemoved(String modeName) {
        debugLog("WMI.modeConfigRemoved mo:" + modeName); // NOI18N
        ModeImpl mode = (ModeImpl)name2mode.remove(modeName);
        if(mode != null) {
            WindowManagerImpl.getInstance().removeMode(mode);
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new NullPointerException("Mode for name="+modeName+" was not created")); // NOI18N
        }
    }
    
    /** Handles adding tcRef to model. 
     * @param modeName unique name of parent mode.
     * @param tcRefConfig configuration data of added tcRef
     * @param tcRefNames array of tcIds to pass ordering of new tcRef,
     * if there is no ordering defined tcRef is appended to end of array
     */
    public synchronized void topComponentRefConfigAdded
    (String modeName, TCRefConfig tcRefConfig, String [] tcRefNames) {
        debugLog("WMI.topComponentRefConfigAdded mo:" + modeName + " tcRef:" + tcRefConfig.tc_id); // NOI18N
        
        TopComponent tc = getTopComponentForID(tcRefConfig.tc_id);
        if(tc != null) {
            ModeImpl mode = (ModeImpl)name2mode.get(modeName);
            if(mode != null) {
                if(tcRefConfig.opened) {
                    mode.addOpenedTopComponent(tc);
                } else {
                    mode.addClosedTopComponent(tc);
                }
            }
        }
    }
    
    /** Handles removing tcRef from model. 
     * @param tc_id unique id of removed tcRef
     */
    public synchronized void topComponentRefConfigRemoved(String tc_id) {
        debugLog("WMI.topComponentRefConfigRemoved tcRef:" + tc_id); // NOI18N
        
        TopComponent tc = getTopComponentForID(tc_id);
        if(tc != null) {
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
            if(mode != null) {
                mode.removeTopComponent(tc);
            }
        }
    }
    
    /** Handles adding group to model.
     * @param groupConfig configuration data of added group
     */
    public synchronized void groupConfigAdded(GroupConfig groupConfig) {
        debugLog("WMI.groupConfigAdded group:" + groupConfig.name); // NOI18N
        createTopComponentGroupFromConfig(groupConfig);
    }
    
    /** Handles removing group from model.
     * @param groupName unique name of removed group
     */
    public synchronized void groupConfigRemoved(String groupName) {
        debugLog("WMI.groupConfigRemoved group:" + groupName); // NOI18N
        TopComponentGroupImpl group = (TopComponentGroupImpl)name2group.remove(groupName);
        if(group != null) {
            WindowManagerImpl.getInstance().removeTopComponentGroup(group);
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new NullPointerException("Null group for name="+groupName)); 
        }
    }
    
    /** Handles adding tcGroup to model. 
     * @param groupName unique name of parent group
     * @param tcGroupConfig configuration data of added tcGroup
     */
    public synchronized void topComponentGroupConfigAdded(String groupName, TCGroupConfig tcGroupConfig) {
        debugLog("WMI.topComponentGroupConfigAdded group:" + groupName + " tcGroup:" + tcGroupConfig.tc_id); // NOI18N
        
        TopComponentGroupImpl group = (TopComponentGroupImpl)name2group.get(groupName);
        if(group != null) {
            group.addUnloadedTopComponent(tcGroupConfig.tc_id);
            if(tcGroupConfig.open) {
                group.addUnloadedOpeningTopComponent(tcGroupConfig.tc_id);
            } 

            if(tcGroupConfig.close) {
                group.addUnloadedClosingTopComponent(tcGroupConfig.tc_id);
            }
        }
    }
    
    /** Handles removing tcGroup from model. 
     * @param groupName unique name of parent group
     * @param tc_id unique id of removed tcGroup
     */
    public synchronized void topComponentGroupConfigRemoved(String groupName, String tc_id) {
        debugLog("WMI.topComponentGroupConfigRemoved group:" + groupName + " tcGroup:" + tc_id); // NOI18N
        
        TopComponentGroupImpl group = (TopComponentGroupImpl)name2group.get(groupName);
        if(group != null) {
            group.removeUnloadedTopComponent(tc_id);
        }
    }
    
    /** Dump window manager configuration data to standard output. */
    private static String dumpConfig (WindowManagerConfig wmc) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n-- wmc: [" + Integer.toHexString(System.identityHashCode(wmc)) + "]");
        buffer.append("\n-- JOINED --");
        buffer.append("\n-- x: " + wmc.xJoined);
        buffer.append("\n-- y: " + wmc.yJoined);
        buffer.append("\n--  width: " + wmc.widthJoined);
        buffer.append("\n-- height: " + wmc.heightJoined);
        buffer.append("\n--  relativeX: " + wmc.relativeXJoined);
        buffer.append("\n--  relativeY: " + wmc.relativeYJoined);
        buffer.append("\n--  relativeWidth: " + wmc.relativeWidthJoined);
        buffer.append("\n-- relativeHeight: " + wmc.relativeHeightJoined);
        buffer.append("\n-- centeredHorizontally: " + wmc.centeredHorizontallyJoined);
        buffer.append("\n--   centeredVertically: " + wmc.centeredVerticallyJoined);
        buffer.append("\n--    maximizeIfWidthBelowJoined: " + wmc.maximizeIfWidthBelowJoined);
        buffer.append("\n--   maximizeIfHeightBelowJoined: " + wmc.maximizeIfHeightBelowJoined);
        
        buffer.append("\n-- SEPARATED --");
        buffer.append("\n-- x: " + wmc.xSeparated);
        buffer.append("\n-- y: " + wmc.ySeparated);
        buffer.append("\n--  width: " + wmc.widthSeparated);
        buffer.append("\n-- height: " + wmc.heightSeparated);
        buffer.append("\n--  relativeX: " + wmc.relativeXSeparated);
        buffer.append("\n--  relativeY: " + wmc.relativeYSeparated);
        buffer.append("\n--  relativeWidth: " + wmc.relativeWidthSeparated);
        buffer.append("\n-- relativeHeight: " + wmc.relativeHeightSeparated);
        buffer.append("\n-- centeredHorizontally: " + wmc.centeredHorizontallySeparated);
        buffer.append("\n--   centeredVertically: " + wmc.centeredVerticallySeparated);
        
        buffer.append("\n-- editorAreaState: " + wmc.editorAreaState);
        if(wmc.editorAreaConstraints != null) {
            for (int i = 0; i < wmc.editorAreaConstraints.length; i++) {
                buffer.append("\n-- co[" + i + "]: " + wmc.editorAreaConstraints[i]);
            }
        }
        buffer.append("\n--         editorAreaBounds: " + wmc.editorAreaBounds);
        buffer.append("\n-- editorAreaRelativeBounds: " + wmc.editorAreaRelativeBounds);
        
        buffer.append("\n--     screenSize: " + wmc.screenSize);
        buffer.append("\n--    activeModeName: " + wmc.activeModeName);
        buffer.append("\n-- maximizedModeName: " + wmc.maximizedModeName);
        buffer.append("\n--     toolbarconfig: " + wmc.toolbarConfiguration);
        buffer.append("\n-- modes: " + wmc.modes + " size " + (wmc.modes == null ? -1 : wmc.modes.length));
        for (int i = 0; i < wmc.modes.length; i++) {
            ModeConfig mc = wmc.modes[i];
            buffer.append("\n-- --");
            buffer.append("\n-- -- mode[" + i + "]: " + mc.name);
            buffer.append("\n-- -- state: " + mc.state + " "
            + ((mc.state == Constants.MODE_STATE_JOINED) ? "joined" : "separated"));
            if (mc.constraints != null) {
                for (int j = 0; j < mc.constraints.length; j++) {
                    buffer.append("\n-- -- co[" + j + "]: " + mc.constraints[j]);
                }
            }
            buffer.append("\n-- -- kind: " + mc.kind + " "
            + ((mc.kind == Constants.MODE_KIND_EDITOR) ? "editor" : "view"));
            buffer.append("\n-- --         bounds: " + mc.bounds);
            buffer.append("\n-- -- relativeBounds: " + mc.relativeBounds);
            buffer.append("\n-- --          state: " + mc.frameState);
            buffer.append("\n-- -- active-tc: " + mc.selectedTopComponentID);
            buffer.append("\n-- -- permanent: " + mc.permanent);
            if (mc.tcRefConfigs != null) {
                for (int k = 0; k < mc.tcRefConfigs.length; k++) {
                    TCRefConfig tcRefCfg = mc.tcRefConfigs[k];
                    buffer.append("\n++ ++ ++ tcRef[" + k + "]: " + tcRefCfg.tc_id);
                    buffer.append("\n++ ++ ++   opened: " + tcRefCfg.opened);
                }
            }
        }
        buffer.append("\n-- groups: " + wmc.groups + " size " + (wmc.groups == null ? -1 : wmc.groups.length));
        for (int i = 0; i < wmc.groups.length; i++) {
            GroupConfig sc = wmc.groups[i];
            buffer.append("\n-- --");
            buffer.append("\n-- -- group[" + i + "]: " + sc.name);
            if (sc.tcGroupConfigs != null) {
                for (int k = 0; k < sc.tcGroupConfigs.length; k++) {
                    TCGroupConfig tcGroupCfg = sc.tcGroupConfigs[k];
                    buffer.append("\n++ ++ ++ tcGroup[" + k + "]: " + tcGroupCfg.tc_id);
                    buffer.append("\n++ ++ ++   open: " + tcGroupCfg.open);
                    buffer.append("\n++ ++ ++  close: " + tcGroupCfg.close);
                }
            }
        }
        
        return buffer.toString();
    }

    private static void debugLog(String message) {
        Debug.log(PersistenceHandler.class, message);
    }
    
    private static Rectangle computeBounds(boolean centeredHorizontaly, boolean centeredVerticaly,
    int x, int y, int width, int height, float relativeX, float relativeY, float relativeWidth, float relativeHeight) {
        Rectangle bounds; 
        if(width > 0 && height > 0) {
            // From absoute values.
            bounds = new Rectangle(x, y, width, height);
        } else if(relativeWidth > 0F && relativeHeight > 0F) {
            // From relative values.
            Rectangle screen = Utilities.getUsableScreenBounds();
            bounds = new Rectangle((int)(screen.x * relativeX), (int)(screen.y * relativeY),
                        (int)(screen.width * relativeWidth), (int)(screen.height * relativeHeight));
        } else {
            return null;
        }
        // Center the bounds if necessary.
        if(centeredHorizontaly || centeredVerticaly) {
            Rectangle centered = Utilities.findCenterBounds(new Dimension(bounds.width, bounds.height));
            if(centeredHorizontaly) {
                bounds.x = centered.x;
            }
            if(centeredVerticaly) {
                bounds.y = centered.y;
            }
        }
        
        return bounds;
    }

}
