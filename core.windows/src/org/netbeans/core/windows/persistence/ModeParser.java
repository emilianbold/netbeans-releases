/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.persistence;

import java.util.logging.Level;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;


/**
 * Handle loading/saving of Mode configuration data.
 *
 * @author Marek Slama
 */

class ModeParser {
    
    public static final String INSTANCE_DTD_ID_1_0
        = "-//NetBeans//DTD Mode Properties 1.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_1_1
        = "-//NetBeans//DTD Mode Properties 1.1//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_1_2
        = "-//NetBeans//DTD Mode Properties 1.2//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_0
        = "-//NetBeans//DTD Mode Properties 2.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_1
        = "-//NetBeans//DTD Mode Properties 2.1//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_2
        = "-//NetBeans//DTD Mode Properties 2.2//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_3
        = "-//NetBeans//DTD Mode Properties 2.3//EN"; // NOI18N
    
    /** Name of extended attribute for order of children */
    private static final String EA_ORDER = "WinSys-TCRef-Order"; // NOI18N
    
    /** Separator of names of two files. The first file should be before
     * the second one in partial ordering. */
    private static final char SEP = '/';
    
    private static final boolean DEBUG = Debug.isLoggable(ModeParser.class);
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private InternalConfig internalConfig;
    
    /** Map of TCRefParser instances. Used for fast access. */
    private Map<String, TCRefParser> tcRefParserMap = new HashMap<String, TCRefParser>(19);
    
    /** map of names of tcRefs to their index or null */
    private Map<String,Integer> tcRefOrder;
    
    /** Unique mode name from file name */
    private String modeName;
    
    /** true if wsmode file is present in module folder */
    private boolean inModuleFolder;
    /** true if wsmode file is present in local folder */
    private boolean inLocalFolder;
    
    /** Contains names of all tcRefs placed in local folder <String> */
    private Set maskSet;
    
    public ModeParser (String name, Set maskSet) {
        this.modeName = name;
        this.maskSet = maskSet;
    }
    
    /** Load mode configuration including all tcrefs. */
    ModeConfig load () throws IOException {
        //if (DEBUG) Debug.log(ModeParser.class, "load ENTER" + " mo:" + name);
        ModeConfig mc = new ModeConfig();
        readProperties(mc);
        if (mc.kind == Constants.MODE_KIND_SLIDING && mc.side != null && !mc.permanent) {
            // now we have the 4.0 anonymous mode for the slide bar. replace with the 
            // predefined ones..
            mc.permanent = true;
            // well, the names are defined in core/ui.
            // shall we at all care about the name? or is making it permanent just fine?
//            if (mc.side.equals(Constants.BOTTOM)) {
//                mc.name = "bottomSlidingSide"; //NOI18N
//            } else if (mc.side.equals(Constants.LEFT)) {
//                mc.name = "leftSlidingSide"; //NOI18N
//            } else if (mc.side.equals(Constants.RIGHT)) {
//                mc.name = "rightSlidingSide"; //NOI18N
//            }
        }
        readTCRefs(mc);
        //if (DEBUG) Debug.log(ModeParser.class, "load LEAVE" + " mo:" + name);
        return mc;
    }
    
    /** Save mode configuration including all tcrefs. */
    void save (ModeConfig mc) throws IOException {
        //if (DEBUG) Debug.log(ModeParser.class, "save ENTER" + " mo:" + name);
        writeProperties(mc);
        writeTCRefs(mc);
        //if (DEBUG) Debug.log(ModeParser.class, "save LEAVE" + " mo:" + name);
    }
    
    private void readProperties (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "readProperties ENTER" + " mo:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(mc, internalCfg);
        
        /*if (DEBUG) Debug.log(ModeParser.class, "               specVersion: " + internalCfg.specVersion);
        if (DEBUG) Debug.log(ModeParser.class, "        moduleCodeNameBase: " + internalCfg.moduleCodeNameBase);
        if (DEBUG) Debug.log(ModeParser.class, "     moduleCodeNameRelease: " + internalCfg.moduleCodeNameRelease);
        if (DEBUG) Debug.log(ModeParser.class, "moduleSpecificationVersion: " + internalCfg.moduleSpecificationVersion);*/
        
        if (DEBUG) Debug.log(ModeParser.class, "readProperties LEAVE" + " mo:" + getName());
    }
    
    private void readTCRefs (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "readTCRefs ENTER" + " mo:" + getName());
        
        for (Iterator it = tcRefParserMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = tcRefParserMap.get(it.next());
            tcRefParser.setInModuleFolder(false);
            tcRefParser.setInLocalFolder(false);
        }
        
        //if (DEBUG) Debug.log(ModeParser.class, "moduleParentFolder: " + moduleParentFolder);
        //if (DEBUG) Debug.log(ModeParser.class, " localParentFolder: " + localParentFolder);
        //if (DEBUG) Debug.log(ModeParser.class, "  moduleModeFolder: " + moduleModeFolder);
        //if (DEBUG) Debug.log(ModeParser.class, "   localModeFolder: " + localModeFolder);
        
        if (isInModuleFolder()) {
            FileObject moduleModeFolder = moduleParentFolder.getFileObject(modeName);
            if (moduleModeFolder != null) {
                FileObject [] files = moduleModeFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //if (DEBUG) Debug.log(ModeParser.class, "-- -- MODULE fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCREF_EXT.equals(files[i].getExt())) {
                        //wstcref file
                        TCRefParser tcRefParser;
                        if (tcRefParserMap.containsKey(files[i].getName())) {
                            tcRefParser = tcRefParserMap.get(files[i].getName());
                        } else {
                            tcRefParser = new TCRefParser(files[i].getName());
                            tcRefParserMap.put(files[i].getName(), tcRefParser);
                        }
                        tcRefParser.setInModuleFolder(true);
                        tcRefParser.setModuleParentFolder(moduleModeFolder);
                    }
                }
            }
        }

        if (isInLocalFolder()) {
            FileObject localModeFolder = localParentFolder.getFileObject(modeName);
            if (localModeFolder != null) {
                FileObject [] files = localModeFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //if (DEBUG) Debug.log(ModeParser.class, "-- -- LOCAL fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCREF_EXT.equals(files[i].getExt())) {
                        //wstcref file
                        TCRefParser tcRefParser = tcRefParserMap.get(files[i].getName());
                        if (tcRefParser== null) {
                            tcRefParser = new TCRefParser(files[i].getName());
                            tcRefParserMap.put(files[i].getName(), tcRefParser);
                        }
                        tcRefParser.setInLocalFolder(true);
                        tcRefParser.setLocalParentFolder(localModeFolder);
                    }
                }
            }
        }
        
        /*for (Iterator it = tcRefParserMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = (TCRefParser) tcRefParserMap.get(it.next());
            if (DEBUG) Debug.log(ModeParser.class, "tcRefParser: " + tcRefParser.getName()
            + " isInModuleFolder:" + tcRefParser.isInModuleFolder()
            + " isInLocalFolder:" + tcRefParser.isInLocalFolder());
        }*/

        //Read order
        readOrder();
        
        List<TCRefParser> localList = new ArrayList<TCRefParser>(10);
        Map<String, TCRefParser> localMap = new HashMap<String, TCRefParser>( tcRefParserMap );
        
        if (tcRefOrder != null) {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- ORDER IS DEFINED");
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- map.size:" + localMap.size());
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- order.size:" + tcRefOrder.size());
            TCRefParser [] tcRefParserArray = new TCRefParser[tcRefOrder.size()];
            for (Iterator it = tcRefOrder.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry en = (Map.Entry) it.next();
                String tcRefName = (String) en.getKey();
                int index = ((Integer) en.getValue()).intValue();
                TCRefParser tcRefParser = (TCRefParser) localMap.remove(tcRefName);
                //Put instances to array according to defined order
                //Order should be defined from 0 to N-1
                //if (DEBUG) Debug.log(ModeParser.class, "-- -- ADD [" + index + "]: " + tcRefParser.getName());
                tcRefParserArray[index] = tcRefParser;
            }
            for (int i = 0; i < tcRefParserArray.length; i++) {
                if (tcRefParserArray[i] != null) {
                    localList.add(tcRefParserArray[i]);
                }
            }
            //Append remaining instances if any
            for (String s: localMap.keySet()) {
                TCRefParser tcRefParser = localMap.get(s);
                localList.add(tcRefParser);
            }
        } else {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- NO ORDER, USING PARTIAL ORDERING");
            for (String s: localMap.keySet()) {
                TCRefParser tcRefParser = localMap.get(s);
                localList.add(tcRefParser);
            }
            
            /*if (DEBUG) Debug.log(ModeParser.class, "LIST BEFORE SORT");
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = (TCRefParser) localList.get(i);
                if (DEBUG) Debug.log(ModeParser.class, " p[" + i + "]: " + tcRefParser.getName());
            }*/
            
            //Sort using partial ordering
            localList = carefullySort(localList);
            
            /*if (DEBUG) Debug.log(ModeParser.class, "LIST AFTER SORT");
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = (TCRefParser) localList.get(i);
                if (DEBUG) Debug.log(ModeParser.class, " p[" + i + "]: " + tcRefParser.getName());
            }*/
            
            if (tcRefOrder == null) {
                tcRefOrder = new HashMap<String,Integer>(19);
            }
            tcRefOrder.clear();
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = localList.get(i);
                tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
            }
            writeOrder();
        }
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List<TCRefConfig> tcRefCfgList = new ArrayList<TCRefConfig>(localList.size());
        List<TCRefParser> toRemove = new ArrayList<TCRefParser>(localList.size());
        for (int i = 0; i < localList.size(); i++) {
            TCRefParser tcRefParser = localList.get(i);
            //Special masking: Ignore tcRef which is present in module folder and
            //is present in local folder in DIFFERENT mode. (ie. when TopComponent defined
            //by module was moved to another module) It is to avoid creating _hidden file =>
            //trouble when disable/enable module.
            if (maskSet.contains(tcRefParser.getName())) {
                if (tcRefParser.isInModuleFolder() && !tcRefParser.isInLocalFolder()) {
                    toRemove.add(tcRefParser);
                    continue;
                }
            }
            TCRefConfig tcRefCfg;
            try {
                tcRefCfg = tcRefParser.load();
            } catch (IOException exc) {
                //If reading of one tcRef fails we want to log message
                //and continue.
                Logger.getLogger(ModeParser.class.getName()).log(Level.WARNING, null, exc);
                continue;
            }
            boolean tcRefAccepted = acceptTCRef(tcRefParser, tcRefCfg);
            if (tcRefAccepted) {
                tcRefCfgList.add(tcRefCfg);
            } else {
                toRemove.add(tcRefParser);
                deleteLocalTCRef(tcRefParser.getName());
            }
        }
        
        for (int i = 0; i < toRemove.size(); i++) {
            TCRefParser tcRefParser = toRemove.get(i);
            localList.remove(tcRefParser);
            tcRefParserMap.remove(tcRefParser.getName());
        }
        
        //Update order if any tcRef was removed
        if (toRemove.size() > 0) {
            if (tcRefOrder == null) {
                tcRefOrder = new HashMap<String,Integer>(19);
            }
            tcRefOrder.clear();
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = (TCRefParser) localList.get(i);
                tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
            }
            writeOrder();
        }
        
        mc.tcRefConfigs = 
            tcRefCfgList.toArray(new TCRefConfig[tcRefCfgList.size()]);
        
        PersistenceManager pm = PersistenceManager.getDefault();
        for (int i = 0; i < mc.tcRefConfigs.length; i++) {
            pm.addUsedTCId(mc.tcRefConfigs[i].tc_id);
        }
        
        if (DEBUG) Debug.log(ModeParser.class, "readTCRefs LEAVE" + " mo:" + getName());
    }
    
    /** Checks if module for given tcRef exists.
     * @return true if tcRef is valid - its module exists
     */
    private boolean acceptTCRef (TCRefParser tcRefParser, TCRefConfig config) {
        InternalConfig cfg = tcRefParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
                                            (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
                                             cfg.moduleSpecificationVersion);
            if (curModuleInfo == null) {
                PersistenceManager.LOG.fine("Cannot find module \'" +
                          cfg.moduleCodeNameBase + " " + cfg.moduleCodeNameRelease + " " + 
                          cfg.moduleSpecificationVersion + "\' for tcref with id \'" + config.tc_id + "\'"); // NOI18N
                
            }
            return (curModuleInfo != null) && curModuleInfo.isEnabled();
        } else {
            //No module info
            return true;
        }
    }
    
    private void writeProperties (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "writeProperties ENTER" + " mo:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(mc, internalCfg);
        if (DEBUG) Debug.log(ModeParser.class, "writeProperties LEAVE" + " mo:" + getName());
    }
    
    private void writeTCRefs (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "writeTCRefs ENTER" + " mo:" + getName());
        //Step 0: Create order
        if (mc.tcRefConfigs.length > 0) {
            if (tcRefOrder == null) {
                tcRefOrder = new HashMap<String,Integer>(19);
            }
            tcRefOrder.clear();
            for (int i = 0; i < mc.tcRefConfigs.length; i++) {
                tcRefOrder.put(mc.tcRefConfigs[i].tc_id, Integer.valueOf(i));
            }
        } else {
            tcRefOrder = null;
        }
        writeOrder();
        //Step 1: Clean obsolete tcRef parsers
        Map<String, TCRefConfig> tcRefConfigMap = new HashMap<String, TCRefConfig>(19);
        for (int i = 0; i < mc.tcRefConfigs.length; i++) {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- tcRefCfg[" + i + "]: " + mc.tcRefConfigs[i].tc_id);
            tcRefConfigMap.put(mc.tcRefConfigs[i].tc_id, mc.tcRefConfigs[i]);
        }
        TCRefParser tcRefParser;
        List<String> toDelete = new ArrayList<String>(10);
        for (String s: tcRefParserMap.keySet()) {
            tcRefParser = tcRefParserMap.get(s);
            if (!tcRefConfigMap.containsKey(tcRefParser.getName())) {
                toDelete.add(tcRefParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            //if (DEBUG) Debug.log(ModeParser.class, " ** REMOVE FROM MAP tcRefParser: " + toDelete.get(i));
            tcRefParserMap.remove(toDelete.get(i));
            //if (DEBUG) Debug.log(ModeParser.class, " ** DELETE tcRefParser: " + toDelete.get(i));
            deleteLocalTCRef(toDelete.get(i));
        }
        
        //Step 2: Create missing tcRefs parsers
        //if (DEBUG) Debug.log(ModeParser.class, "-- -- mc.tcRefConfigs.length:" + mc.tcRefConfigs.length);
        for (int i = 0; i < mc.tcRefConfigs.length; i++) {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- tcRefCfg[" + i + "]: " + mc.tcRefConfigs[i].tc_id);
            if (!tcRefParserMap.containsKey(mc.tcRefConfigs[i].tc_id)) {
                tcRefParser = new TCRefParser(mc.tcRefConfigs[i].tc_id);
                //if (DEBUG) Debug.log(ModeParser.class, " ** CREATE tcRefParser:" + tcRefParser.getName());
                tcRefParserMap.put(mc.tcRefConfigs[i].tc_id, tcRefParser);
            }
        }
        
        //Step 3: Save all tcRefs
        FileObject localFolder = localParentFolder.getFileObject(getName());
        if ((localFolder == null) && (tcRefParserMap.size() > 0)) {
            //Create local mode folder
            //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.writeTCRefs" + " CREATE LOCAL FOLDER");
            localFolder = FileUtil.createFolder(localParentFolder, getName());
        }
        //if (DEBUG) Debug.log(ModeParser.class, "writeTCRefs" + " localFolder:" + localFolder);
        
        for (Iterator it = tcRefParserMap.keySet().iterator(); it.hasNext(); ) {
            tcRefParser = tcRefParserMap.get(it.next());
            tcRefParser.setLocalParentFolder(localFolder);
            tcRefParser.setInLocalFolder(true);
            tcRefParser.save((TCRefConfig) tcRefConfigMap.get(tcRefParser.getName()));
        }
        
        if (DEBUG) Debug.log(ModeParser.class, "writeTCRefs LEAVE" + " mo:" + getName());
    }
    
    private void deleteLocalTCRef (String tcRefName) {
        if (DEBUG) Debug.log(ModeParser.class, "deleteLocalTCRef" + " tcRefName:" + tcRefName);
        if (localParentFolder == null) {
            return;
        }
        FileObject localModeFolder = localParentFolder.getFileObject(modeName);
        if (localModeFolder == null) {
            return;
        }
        FileObject tcRefFO = localModeFolder.getFileObject(tcRefName, PersistenceManager.TCREF_EXT);
        if (tcRefFO != null) {
            PersistenceManager.deleteOneFO(tcRefFO);
        }
    }
    
    //////////////////////////////////////////////////////////////////
    // BEGIN Code to keep order of TopComponents in Mode.
    //
    // It is taken from FolderOrder and FolderList where it is used
    // to keep order of DataObjects.
    //////////////////////////////////////////////////////////////////
    
    /** Reads the order of tcRefs from disk.
     */
    private void readOrder () {
        if (localParentFolder == null) {
            try {
                localParentFolder = PersistenceManager.getDefault().getModesLocalFolder();
            }
            catch (IOException ex) {
                Logger.getLogger(ModeParser.class.getName()).log(
                        Level.WARNING, "Cannot get access to lcoal modes folder", ex); // NOI18N
                return;
            }
        }
        FileObject localModeFolder = localParentFolder.getFileObject(modeName);
        if (localModeFolder == null) {
            tcRefOrder = null;
            return;
        }
        Object o = localModeFolder.getAttribute(EA_ORDER);
        
        if (o == null) {
            tcRefOrder = null;
            return;
        } else if (o instanceof String) {
            String sepNames = (String) o;
            Map<String,Integer> map = new HashMap<String,Integer>(19);
            StringTokenizer tok = new StringTokenizer(sepNames, "/"); // NOI18N
            int i = 0;
            while (tok.hasMoreTokens()) {
                String tcRefName = tok.nextToken();
                map.put(tcRefName, Integer.valueOf(i));
                i++;
            }
            tcRefOrder = map;
            return;
        } else {
            // Unknown format:
            tcRefOrder = null;
            return;
        }
    }
    
    /** Stores the order of tcRefs to disk.
    */
    private void writeOrder () throws IOException {
        //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.writeOrder ENTER" + " mo:" + getName());
        if (localParentFolder == null) {
            localParentFolder = PersistenceManager.getDefault().getModesLocalFolder();
        }
        
        FileObject localModeFolder = localParentFolder.getFileObject(modeName);
        if (localModeFolder == null) {
            //Create local mode folder
            localModeFolder = FileUtil.createFolder(localParentFolder, modeName);
        }
        if (tcRefOrder == null) {
            //Clear the order
            localModeFolder.setAttribute(EA_ORDER, null);
        } else {
            // Stores list of file names separated by /
            String[] tcRefNames = new String[tcRefOrder.size()];
            for (Map.Entry<String, Integer> en: tcRefOrder.entrySet()) {
                String tcRefName = en.getKey();
                int index = en.getValue().intValue();
                tcRefNames[index] = tcRefName;
            }
            StringBuilder buf = new StringBuilder(255);
            for (int i = 0; i < tcRefNames.length; i++) {
                if (i > 0) {
                    buf.append(SEP);
                }
                buf.append(tcRefNames[i]);
            }
            //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.writeOrder buf:" + buf);
            localModeFolder.setAttribute(EA_ORDER, buf.toString ());
        }
    }
    
    /** Read the list of intended partial orders from disk.
     * Each element is a string of the form "a<b" for a, b filenames
     * with extension, where a should come before b.
     */
    private Set<String> readPartials () {
        //if (DEBUG) Debug.log(ModeParser.class, "++ ++");
        //if (DEBUG) Debug.log(ModeParser.class, "++ ModeParser.readPartials ENTER");
        Set<String> s = new HashSet<String>(19);
        
        //Partials are defined only in module folder
        if (moduleParentFolder == null) {
            return s;
        }
        FileObject moduleModeFolder = moduleParentFolder.getFileObject(modeName);
        if (moduleModeFolder == null) {
            //if (DEBUG) Debug.log(ModeParser.class, "++ ModeParser.readPartials LEAVE 1");
            return s;
        }
        
        Enumeration e = moduleModeFolder.getAttributes();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            if (name.indexOf(SEP) != -1) {
                Object value = moduleModeFolder.getAttribute(name);
                if ((value instanceof Boolean) && ((Boolean) value).booleanValue()) {
                    int ind = name.indexOf(SEP);
                    //Remove file extension 'wstcref'.
                    String name1 = name.substring(0, ind);
                    String name2 = name.substring(ind + 1);
                    //if (DEBUG) Debug.log(ModeParser.class, "name1:" + name1 + " name2:" + name2);
                    int indExt = name1.indexOf('.');
                    if (indExt != -1) {
                        name1 = name1.substring(0, indExt);
                    }
                    indExt = name2.indexOf('.');
                    if (indExt != -1) {
                        name2 = name2.substring(0, indExt);
                    }
                    //if (DEBUG) Debug.log(ModeParser.class, "++ ModeParser.readPartials name BEFORE:" + name);
                    name = name1 + SEP + name2;
                    s.add(name);
                    //if (DEBUG) Debug.log(ModeParser.class, "++ ModeParser.readPartials name AFTER:" + name);
                }
            }
        }
        //if (DEBUG) Debug.log(ModeParser.class, "++ ModeParser.readPartials LEAVE 2");
        //if (DEBUG) Debug.log(ModeParser.class, "++");
        return s;
    }
    
    /**
     * Get ordering constraints for this folder.
     * Returns a map from data objects to lists of data objects they should precede.
     * @param objects a collection of data objects known to be in the folder
     * @return a constraint map, or null if there are no constraints
     */
    private Map<TCRefParser,List<TCRefParser>> getOrderingConstraints (List<TCRefParser> tcRefParsers) {
        //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints ENTER");
        final Set<String> partials = readPartials();
        if (partials.isEmpty()) {
            //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints LEAVE 1");
            return null;
        } else {
            //Remove items from partials which are in ordering
            if (tcRefOrder != null) {
                //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints CLEAN partials");
                Iterator it = partials.iterator();
                while (it.hasNext()) {
                    String constraint = (String) it.next();
                    //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints CLEAN constraint:" + constraint);
                    int idx = constraint.indexOf(SEP);
                    String a = constraint.substring(0, idx);
                    String b = constraint.substring(idx + 1);
                    //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints CLEAN a:" + a + " b:" + b);
                    if (tcRefOrder.containsKey(a) && tcRefOrder.containsKey(b)) {
                        //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints REMOVE:" + constraint);
                        it.remove();
                    } /*else {
                        if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints KEEP:" + constraint);
                    }*/
                }
            }
            Map<String, TCRefParser> objectsByName = new HashMap<String, TCRefParser>(19);
            for (int i = 0; i < tcRefParsers.size(); i++) {
                TCRefParser tcRefParser = tcRefParsers.get(i);
                objectsByName.put(tcRefParser.getName(), tcRefParser);
            }
            Map<TCRefParser,List<TCRefParser>> m = new HashMap<TCRefParser,List<TCRefParser>>(19);
            for (String constraint: partials) {
                int idx = constraint.indexOf(SEP);
                String a = constraint.substring(0, idx);
                String b = constraint.substring(idx + 1);
                if ((tcRefOrder != null) && (tcRefOrder.containsKey(a) && tcRefOrder.containsKey(b))) {
                    continue;
                }
                TCRefParser ad = objectsByName.get(a);
                if (ad == null) {
                    continue;
                }
                TCRefParser bd = objectsByName.get(b);
                if (bd == null) {
                    continue;
                }
                List<TCRefParser> l = m.get(ad);
                if (l == null) {
                    m.put(ad, l = new LinkedList<TCRefParser>());
                }
                l.add(bd);
            }
            //if (DEBUG) Debug.log(ModeParser.class, "getOrderingConstraints LEAVE 2");
            return m;
        }
    }
    
    /** Sort a list of TCRefParsers carefully.
     * If the partial ordering is self-contradictory,
     * it will be ignored and a warning issued.
     * @param l the list to sort
     * @return the sorted list (may or may not be the same)
     */
    private List<TCRefParser> carefullySort (List<TCRefParser> l) {
        Map<TCRefParser, List<TCRefParser>> constraints = getOrderingConstraints(l);
        if (constraints == null) {
            return l;
        } else {
            try {
                return Utilities.topologicalSort(l, constraints);
            } catch (TopologicalSortException ex) {
                @SuppressWarnings("unchecked")
                List<TCRefParser> corrected = ex.partialSort();
                PersistenceManager.LOG.log(Level.WARNING,"Note: Mode " + getName() // NOI18N
                + " cannot be consistently sorted due to ordering conflicts.", ex); // NOI18N
                PersistenceManager.LOG.log(Level.WARNING,"Using partial sort: " + corrected); // NOI18N
                return corrected;
            }
        }
    }
    //////////////////////////////////////////////////////////////////
    // END Code to keep order of TopComponents in Mode.
    //////////////////////////////////////////////////////////////////
    
    /** Removes TCRefParser from ModeParser and cleans wstcref file from local folder.
     * @param tcRefName unique name of tcRef
     */
    void removeTCRef (String tcRefName) {
        if (DEBUG) Debug.log(ModeParser.class, "removeTCRef ENTER" + " tcRef:" + tcRefName);
        //Update order
        List<TCRefParser> localList = new ArrayList<TCRefParser>(10);
        Map localMap = (Map) ((HashMap) tcRefParserMap).clone();
        
        tcRefParserMap.remove(tcRefName);
        
        TCRefParser [] tcRefParserArray = new TCRefParser[tcRefOrder.size()];
        for (Iterator it = tcRefOrder.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry en = (Map.Entry) it.next();
            String name = (String) en.getKey();
            int index = ((Integer) en.getValue()).intValue();
            TCRefParser tcRefParser = (TCRefParser) localMap.remove(name);
            //Put instances to array according to defined order
            //Order should be defined from 0 to N-1
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- ADD [" + index + "]: " + tcRefParser.getName());
            tcRefParserArray[index] = tcRefParser;
        }
        for (int i = 0; i < tcRefParserArray.length; i++) {
            localList.add(tcRefParserArray[i]);
        }
        //Append remaining instances if any
        for (Iterator it = localMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = (TCRefParser) localMap.get(it.next());
            localList.add(tcRefParser);
        }

        //Remove tcRef
        for (int i = 0; i < localList.size(); i++) {
            TCRefParser tcRefParser = localList.get(i);
            if (tcRefName.equals(tcRefParser.getName())) {
                localList.remove(i);
                break;
            }
        }

        //Create updated order
        tcRefOrder.clear();
        for (int i = 0; i < localList.size(); i++) {
            TCRefParser tcRefParser = localList.get(i);
            tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
        }
        try {
            writeOrder();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.WARNING,
            "[WinSys.ModeParser.removeTCRef]" // NOI18N
            + " Warning: Cannot write order of mode: " + getName(), exc); // NOI18N
        }
        
        deleteLocalTCRef(tcRefName);
        if (DEBUG) Debug.log(ModeParser.class, "removeTCRef LEAVE" + " tcRef:" + tcRefName);
    }
    
    /** Adds TCRefParser to ModeParser.
     * @param tcRefName unique name of tcRef
     */
    TCRefConfig addTCRef (String tcRefName, List<String> tcRefNameList) {
        if (DEBUG) Debug.log(ModeParser.class, "addTCRef ENTER" + " mo:" + getName()
        + " tcRef:" + tcRefName);
        //Check consistency. TCRefParser instance should not exist.
        TCRefParser tcRefParser = tcRefParserMap.get(tcRefName);
        if (tcRefParser != null) {
            PersistenceManager.LOG.log(Level.WARNING,
            "[WinSys.ModeParser.addTCRef]" // NOI18N
            + " Warning: ModeParser " + getName() + ". TCRefParser " // NOI18N
            + tcRefName + " exists but it should not."); // NOI18N
            tcRefParserMap.remove(tcRefName);
        }
        tcRefParser = new TCRefParser(tcRefName);
        FileObject moduleFolder = moduleParentFolder.getFileObject(modeName);
        tcRefParser.setModuleParentFolder(moduleFolder);
        tcRefParser.setInModuleFolder(true);
        tcRefParserMap.put(tcRefName, tcRefParser);
        TCRefConfig tcRefConfig = null;
        try {
            tcRefConfig = tcRefParser.load();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.WARNING,
            "[WinSys.ModeParser.addTCRef]" // NOI18N
            + " Warning: ModeParser " + getName() + ". Cannot load tcRef " +  tcRefName, exc); // NOI18N
        }
        
        // Update order
        List<TCRefParser> localList = new ArrayList<TCRefParser>(10);
        Map localMap = (Map) ((HashMap) tcRefParserMap).clone();
        
        TCRefParser [] tcRefParserArray = new TCRefParser[tcRefOrder.size()];
        for (Iterator it = tcRefOrder.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry en = (Map.Entry) it.next();
            String name = (String) en.getKey();
            int index = ((Integer) en.getValue()).intValue();
            tcRefParser = (TCRefParser) localMap.remove(name);
            //Put instances to array according to defined order
            //Order should be defined from 0 to N-1
            //log("-- -- ADD [" + index + "]: " + tcRefParser.getName());
            tcRefParserArray[index] = tcRefParser;
        }
        for (int i = 0; i < tcRefParserArray.length; i++) {
            localList.add(tcRefParserArray[i]);
        }
        //Append remaining instances if any
        for (Iterator it = localMap.keySet().iterator(); it.hasNext(); ) {
            tcRefParser = (TCRefParser) localMap.get(it.next());
            localList.add(tcRefParser);
        }
        
        /*if (DEBUG) Debug.log(ModeParser.class, "LIST BEFORE SORT");
        for (int i = 0; i < localList.size(); i++) {
            tcRefParser = (TCRefParser) localList.get(i);
            if (DEBUG) Debug.log(ModeParser.class, "p[" + i + "]: " + tcRefParser.getName());
        }*/
        
        localList = carefullySort(localList);
        
        /*if (DEBUG) Debug.log(ModeParser.class, "LIST AFTER SORT");
        for (int i = 0; i < localList.size(); i++) {
            tcRefParser = (TCRefParser) localList.get(i);
            if (DEBUG) Debug.log(ModeParser.class, "p[" + i + "]: " + tcRefParser.getName());
        }*/
        
        //Create updated order
        tcRefOrder.clear();
        for (int i = 0; i < localList.size(); i++) {
            tcRefParser = (TCRefParser) localList.get(i);
            tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
        }
        try {
            writeOrder();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.WARNING,
            "[WinSys.ModeParser.addTCRef]" // NOI18N
            + " Warning: Cannot write order of mode: " + getName(), exc); // NOI18N
        }
        
        //Fill output order
        tcRefNameList.clear();
        for (int i = 0; i < localList.size(); i++) {
            tcRefParser = (TCRefParser) localList.get(i);
            tcRefNameList.add(tcRefParser.getName());
        }
        
        if (DEBUG) Debug.log(ModeParser.class, "addTCRef LEAVE" + " mo:" + getName()
        + " tcRef:" + tcRefName);
        
        return tcRefConfig;
    }
    
    /** Adds TCRefParser to ModeParser. Called from import to pass module info
     * to new parser.
     * @param tcRefName unique name of tcRef
     */
    void addTCRefImport (String tcRefName, InternalConfig internalCfg) {
        if (DEBUG) Debug.log(ModeParser.class, "addTCRefImport ENTER" + " mo:" + getName()
        + " tcRef:" + tcRefName);
        //Check consistency. TCRefParser instance should not exist.
        TCRefParser tcRefParser = tcRefParserMap.get(tcRefName);
        if (tcRefParser != null) {
            PersistenceManager.LOG.log(Level.WARNING,
            "[WinSys.ModeParser.addTCRef]" // NOI18N
            + " Warning: ModeParser " + getName() + ". TCRefParser " // NOI18N
            + tcRefName + " exists but it should not."); // NOI18N
            tcRefParserMap.remove(tcRefName);
        }
        tcRefParser = new TCRefParser(tcRefName);
        //FileObject moduleFolder = moduleParentFolder.getFileObject(modeName);
        //tcRefParser.setModuleParentFolder(moduleFolder);
        //tcRefParser.setInModuleFolder(false);
        FileObject localFolder = localParentFolder.getFileObject(modeName);
        tcRefParser.setLocalParentFolder(localFolder);
        tcRefParser.setInternalConfig(internalCfg);
        
        //if (DEBUG) Debug.log(ModeParser.class, "CodeNameBase:" + internalCfg.moduleCodeNameBase);
        //if (DEBUG) Debug.log(ModeParser.class, "CodeNameRelease:" + internalCfg.moduleCodeNameRelease);
        //if (DEBUG) Debug.log(ModeParser.class, "SpecificationVersion:" + internalCfg.moduleSpecificationVersion);
        //if (DEBUG) Debug.log(ModeParser.class, "specVersion:" + internalCfg.specVersion);
        
        tcRefParserMap.put(tcRefName, tcRefParser);
        
        if (DEBUG) Debug.log(ModeParser.class, "addTCRefImport LEAVE" + " mo:" + getName()
        + " tcRef:" + tcRefName);
    }
    
    /** Finds TCRefParser with given ID. Returns null if such TCRefParser
     * is not found.
     * @param tcRefName unique name of tcRef
     */
    TCRefParser findTCRefParser (String tcRefName) {
        //if (DEBUG) Debug.log(ModeParser.class, "findTCRefParser ENTER" + " tcRef:" + tcRefName);
        return tcRefParserMap.get(tcRefName);
    }
    
    /** Getter for internal configuration data.
     * @return instance of internal configuration data
     */
    InternalConfig getInternalConfig () {
        if (internalConfig == null) {
            internalConfig = new InternalConfig();
        }
        return internalConfig;
    }
    
    void setModuleParentFolder (FileObject moduleParentFolder) {
        this.moduleParentFolder = moduleParentFolder;
    }
    
    void setLocalParentFolder (FileObject localParentFolder) {
        this.localParentFolder = localParentFolder;
    }
    
    String getName () {
        return modeName;
    }
    
    boolean isInModuleFolder () {
        return inModuleFolder;
    }
    
    void setInModuleFolder (boolean inModuleFolder) {
        this.inModuleFolder = inModuleFolder;
    }
    
    boolean isInLocalFolder () {
        return inLocalFolder;
    }
    
    void setInLocalFolder (boolean inLocalFolder) {
        this.inLocalFolder = inLocalFolder;
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** Mode configuration data */
        private ModeConfig modeConfig = null;
        
        /** Internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** List to store parsed path items */
        private List<SplitConstraint> itemList = new ArrayList<SplitConstraint>(10);
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject modeConfigFO;
            if (isInLocalFolder()) {
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOInput" + " looking for LOCAL");
                modeConfigFO = localParentFolder.getFileObject
                (ModeParser.this.getName(), PersistenceManager.MODE_EXT);
            } else if (isInModuleFolder()) {
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOInput" + " looking for MODULE");
                modeConfigFO = moduleParentFolder.getFileObject
                (ModeParser.this.getName(), PersistenceManager.MODE_EXT);
            } else {
                //XXX should not happen
                modeConfigFO = null;
            }
            //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOInput" + " modeConfigFO:" + modeConfigFO);
            return modeConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject modeConfigFO;
            modeConfigFO = localParentFolder.getFileObject
            (ModeParser.this.getName(), PersistenceManager.MODE_EXT);
            if (modeConfigFO != null) {
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOOutput" + " modeConfigFO LOCAL:" + modeConfigFO);
                return modeConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(ModeParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.MODE_EXT);
                //XXX should be improved localParentFolder can be null
                modeConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return modeConfigFO;
            }
        }
        /** 
         Reads mode configuration data from XML file. 
         Data are returned in output params.
         */
        void readData (ModeConfig modeCfg, InternalConfig internalCfg)
        throws IOException {
            modeConfig = modeCfg;
            internalConfig = internalCfg;
            itemList.clear();
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing Mode configuration file:" // NOI18N
                + ModeParser.this.getName());
            }
            InputStream is = null;
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*if ("explorer".equals(ModeParser.this.getName())) {
                        InputStream is = cfgFOInput.getInputStream();
                        byte [] arr = new byte [is.available()];
                        is.read(arr);
                        if (DEBUG) Debug.log(ModeParser.class, "DUMP Mode:");
                        String s = new String(arr);
                        if (DEBUG) Debug.log(ModeParser.class, s);
                    }*/
                    //DUMP END
                    is = cfgFOInput.getInputStream();
                    PersistenceManager.getDefault().getXMLParser(this).parse(new InputSource(is));
                }
            } catch (SAXException exc) {
                // Turn into annotated IOException
                String msg = NbBundle.getMessage(ModeParser.class,
                                                 "EXC_ModeParse", cfgFOInput);

                throw (IOException) new IOException(msg).initCause(exc);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException exc) {
                    Logger.getLogger(ModeParser.class.getName()).log(Level.WARNING, null, exc);
                }
            }
            
            modeConfig.constraints =
                itemList.toArray(new SplitConstraint[itemList.size()]);
            
            modeCfg = modeConfig;
            internalCfg = internalConfig;
            
            modeConfig = null;
            internalConfig = null;
        }
        
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) throws SAXException {
            if ("mode".equals(qname)) { // NOI18N
                handleMode(attrs);
            } else if (internalConfig.specVersion != null && 
            // check for null because of #45599 - no idea how it can happen other than having a broken file which doesn't
            // declade 'mode' element. (see handleMode() - after this is called NPE is impossible
                       internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) >= 0) { // NOI18N
                //Parse version 2.0 and beyond
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("name".equals(qname)) { // NOI18N
                    handleName(attrs);
                } else if ("kind".equals(qname)) { // NOI18N
                    handleKind(attrs);
                } else if ("slidingSide".equals(qname)) { // NOI18N
                    handleSlidingSide(attrs);
                } else if ("slide-in-size".equals(qname)) { // NOI18N
                    handleSlideInSize(attrs);
                } else if ("state".equals(qname)) { // NOI18N
                    handleState(attrs);
                } else if ("constraints".equals(qname)) { // NOI18N
                    handleConstraints(attrs);
                } else if ("path".equals(qname)) { // NOI18N
                    handlePath(attrs);
                } else if ("bounds".equals(qname)) { // NOI18N
                    handleBounds(attrs);
                } else if ("relative-bounds".equals(qname)) { // NOI18N
                    handleRelativeBounds(attrs);
                } else if ("frame".equals(qname)) { // NOI18N
                    handleFrame(attrs);
                } else if ("active-tc".equals(qname)) { // NOI18N
                    handleActiveTC(attrs);
                } else if ("empty-behavior".equals(qname)) { // NOI18N
                    handleEmptyBehavior(attrs);
                }
            } else {
                if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }
        
        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }
        
        /** Reads element "mode" */
        private void handleMode (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleMode]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"mode\"."); // NOI18N
                internalConfig.specVersion = new SpecificationVersion("2.0"); // NOI18N
            }
        }
        
        /** Reads element "module" and updates mode config content */
        private void handleModule (Attributes attrs) {
            String moduleCodeName = attrs.getValue("name"); // NOI18N
            //Parse code name
            internalConfig.moduleCodeNameBase = null;
            internalConfig.moduleCodeNameRelease = null;
            internalConfig.moduleSpecificationVersion = null;
            if (moduleCodeName != null) {
                int i = moduleCodeName.indexOf('/');
                if (i != -1) {
                    internalConfig.moduleCodeNameBase = moduleCodeName.substring(0, i);
                    internalConfig.moduleCodeNameRelease = moduleCodeName.substring(i + 1);
                    checkReleaseCode(internalConfig);
                } else {
                    internalConfig.moduleCodeNameBase = moduleCodeName;
                }
                internalConfig.moduleSpecificationVersion = attrs.getValue("spec"); // NOI18N
            }
        }

        /** Checks validity of <code>moduleCodeNameRelease</code> field. 
         * Helper method. */
        private void checkReleaseCode (InternalConfig internalConfig) {
            // #24844. Repair the wrongly saved "null" string
            // as release number.
            if("null".equals(internalConfig.moduleCodeNameRelease)) { // NOI18N
                Logger.getLogger(ModeParser.class.getName()).log(Level.WARNING, null,
                                  new IllegalStateException("Module release code was saved as null string" +
                                                            " for module " +
                                                            internalConfig.moduleCodeNameBase +
                                                            "! Repairing."));
                internalConfig.moduleCodeNameRelease = null;
            }
        }
        
        /** Reads element "name" */
        private void handleName (Attributes attrs) throws SAXException {
            String name = attrs.getValue("unique"); // NOI18N
            if (name != null) {
                modeConfig.name = name;
                if (!name.equals(ModeParser.this.getName())) {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleName]" // NOI18N
                    + " Error: Value of attribute \"unique\" of element \"name\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleName]" // NOI18N
                + " Error: Missing required attribute \"unique\" of element \"name\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
        }

        /** Reads element "kind" */
        private void handleKind (Attributes attrs) {
            String type = attrs.getValue("type"); // NOI18N
            if (type != null) {
                if ("editor".equals(type)) {
                    modeConfig.kind = Constants.MODE_KIND_EDITOR;
                } else if ("view".equals(type)) {
                    modeConfig.kind = Constants.MODE_KIND_VIEW;
                } else if ("sliding".equals(type)) {
                    modeConfig.kind = Constants.MODE_KIND_SLIDING;
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleKind]" // NOI18N
                    + " Warning: Invalid value of attribute \"type\"."); // NOI18N
                    modeConfig.kind = Constants.MODE_KIND_VIEW;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleKind]" // NOI18N
                + " Error: Missing required attribute \"type\" of element \"kind\"."); // NOI18N
                modeConfig.kind = Constants.MODE_KIND_VIEW;
            }
        }
        
        /** Reads element "kind" */
        private void handleSlidingSide(Attributes attrs) {
            String side = attrs.getValue("side");
            if (side != null) {
                if (Constants.LEFT.equals(side) ||
                    Constants.RIGHT.equals(side) ||
                    Constants.BOTTOM.equals(side)) 
                {
                    modeConfig.side = side;
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleSlidingSide]" // NOI18N
                    + " Warning: Wrong value \"" + side + "\" of attribute \"side\" for sliding mode"); // NOI18N
                    modeConfig.side = Constants.LEFT;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleSlidingSide]" // NOI18N
                + " Warning: Missing value of attribute \"side\" for sliding mode."); // NOI18N
                modeConfig.side = Constants.LEFT;
            }
        }      
        
        /** Reads element "slideInSize" */
        private void handleSlideInSize(Attributes attrs) {
            String tcId = attrs.getValue("tc-id");
            String size = attrs.getValue("size");
            if (tcId != null && size != null) {
                try {
                    Integer intSize = Integer.valueOf( size );
                    if( null == modeConfig.slideInSizes )
                        modeConfig.slideInSizes = new HashMap<String, Integer>(5);
                    modeConfig.slideInSizes.put( tcId, intSize );
                    return;
                } catch( NumberFormatException nfE ) {
                    //fall through
                }
            } 
            PersistenceManager.LOG.log(Level.WARNING,
            "[WinSys.ModeParser.handleSlideInSize]" // NOI18N
            + " Warning: Invalid attributes for preferred slide-in size."); // NOI18N
        }      
        
        private void handleState(Attributes attrs) throws SAXException {
            String type = attrs.getValue("type"); // NOI18N
            if (type != null) {
                if ("joined".equals(type)) {
                    modeConfig.state = Constants.MODE_STATE_JOINED;
                } else if ("separated".equals(type)) {
                    modeConfig.state = Constants.MODE_STATE_SEPARATED;
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleState]" // NOI18N
                    + " Warning: Invalid value of attribute \"type\"" // NOI18N
                    + " of element \"state\"."); // NOI18N
                    modeConfig.state = Constants.MODE_STATE_JOINED;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleState]" // NOI18N
                + " Error: Missing required attribute \"type\""
                + " of element \"state\"."); // NOI18N
                modeConfig.state = Constants.MODE_STATE_JOINED;
            }
        }
        
        /** Reads element "constraints" */
        private void handleConstraints (Attributes attrs) {
        }
        
        /** Reads element "path" */
        private void handlePath (Attributes attrs) {
            String s = attrs.getValue("orientation"); // NOI18N
            int orientation;
            if ("horizontal".equals(s)) { // NOI18N
                orientation = Constants.HORIZONTAL;
            } else if ("vertical".equals(s)) { // NOI18N
                orientation = Constants.VERTICAL;
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handlePath]" // NOI18N
                + " Warning: Invalid or missing value of attribute \"orientation\"."); // NOI18N
                orientation = Constants.VERTICAL;
            }
            
            int number;
            try {
                s = attrs.getValue("number"); // NOI18N
                if (s != null) {
                    number = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handlePath]" // NOI18N
                    + " Warning: Missing value of attribute \"number\"."); // NOI18N
                    number = 0;
                }
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handlePath]" // NOI18N
                + " Warning: Cannot read element \"path\", attribute \"number\"", exc); // NOI18N
                number = 0;
            }
            
            double weight;
            try {
                s = attrs.getValue("weight"); // NOI18N
                if (s != null) {
                    weight = Double.parseDouble(s);
                } else {
                    //Not required attribute, provide default value
                    weight = 0.5;
                }
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handlePath]" // NOI18N
                + " Warning: Cannot read element \"path\", attribute \"weight\".", exc); // NOI18N
                weight = 0.5;
            }
            SplitConstraint item = new SplitConstraint(orientation, number, weight);
            itemList.add(item);
        }
        
        /** Reads element "bounds" */
        private void handleBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                modeConfig.bounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                        "[WinSys.ModeParser.handleBounds]" // NOI18N
                        + " Warning: Missing attribute \"x\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"bounds\"."); // NOI18N
                    return;
                }
                modeConfig.bounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleBounds]" // NOI18N
                + " Warning: Cannot read element \"bounds\".", exc); // NOI18N
            }
        }
        
        /** Reads element "relative-bounds" */
        private void handleRelativeBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                modeConfig.relativeBounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"x\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                modeConfig.relativeBounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                + " Warning: Cannot read element \"relative-bounds\".", exc); // NOI18N
            }
        }
        
        /** Reads element "frame" */
        private void handleFrame (Attributes attrs) {
            String frameState = attrs.getValue("state"); // NOI18N
            if (frameState != null) {
                try {
                    modeConfig.frameState = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.ModeParser.handleFrame]" // NOI18N
                    + " Warning: Cannot read attribute \"state\"" // NOI18N
                    + " of element \"frame\".", exc); // NOI18N
                    modeConfig.frameState = Frame.NORMAL;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleFrame]" // NOI18N
                + " Warning: Missing value of attribute \"state\"" // NOI18N
                + " of element \"frame\"."); // NOI18N
                modeConfig.frameState = Frame.NORMAL;
            }
        }
        
        /** Reads element "active-tc" */
        private void handleActiveTC (Attributes attrs) {
            String id = attrs.getValue("id"); // NOI18N
            if (id != null) {
                modeConfig.selectedTopComponentID = id;
            } else {
                modeConfig.selectedTopComponentID = ""; // NOI18N
            }
            String prevId = attrs.getValue("prev-id"); // NOI18N
            if (prevId != null) {
                modeConfig.previousSelectedTopComponentID = prevId;
            } else {
                modeConfig.previousSelectedTopComponentID = ""; // NOI18N
            }
        }
        
        /** Reads element "empty-behavior" */
        private void handleEmptyBehavior (Attributes attrs) {
            String value = attrs.getValue("permanent"); // NOI18N
            if ("true".equals(value)) { // NOI18N
                modeConfig.permanent = true;
            } else if ("false".equals(value)) { // NOI18N
                modeConfig.permanent = false;
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.ModeParser.handleEmptyBehavior]" // NOI18N
                + " Warning: Invalid value of attribute \"permanent\"."); // NOI18N
                modeConfig.permanent = false;
            }
        }
        
        /** Writes data from asociated mode to the xml representation */
        void writeData (ModeConfig mc, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(mc, ic);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    lock = cfgFOOutput.lock();
                    os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, "UTF-8"); // NOI18N
                    osw.write(buff.toString());
                    /*log("-- DUMP Mode:");
                    log(buff.toString());*/
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException exc) {
                        Logger.getLogger(ModeParser.class.getName()).log(Level.WARNING, null, exc);
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (ModeConfig mc, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"). // NOI18N
            /*buff.append("<!DOCTYPE mode PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Mode Properties 2.3//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/mode-properties2_3.dtd\">\n\n"); // NOI18N*/
                append("<mode version=\"2.3\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendName(mc, buff);
            appendKind(mc, buff);
            if (mc.kind == Constants.MODE_KIND_SLIDING) {
                appendSlidingSide(mc, buff);
                if( null != mc.slideInSizes )
                    appendSlideInSize(mc, buff);
            }
            appendState(mc, buff);
            appendConstraints(mc, buff);
            if (mc.bounds != null) {
                appendBounds(mc, buff);
            } else if (mc.relativeBounds != null) {
                appendRelativeBounds(mc, buff);
            }
            appendFrame(mc, buff);
            appendActiveTC(mc, buff);
            appendEmptyBehavior(mc, buff);
            
            buff.append("</mode>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append("    <module name=\""); // NOI18N
                buff.append(ic.moduleCodeNameBase);
                if (ic.moduleCodeNameRelease != null) {
                    buff.append("/").append(ic.moduleCodeNameRelease); // NOI18N
                }
                if (ic.moduleSpecificationVersion != null) { 
                    buff.append("\" spec=\""); // NOI18N
                    buff.append(ic.moduleSpecificationVersion);
                }
                buff.append("\" />\n"); // NOI18N
            }
        }

        private void appendName (ModeConfig mc, StringBuffer buff) {
            buff.append("    <name unique=\""); // NOI18N
            buff.append(mc.name);
            buff.append("\" />\n"); // NOI18N
        }

        private void appendKind (ModeConfig mc, StringBuffer buff) {
            buff.append("  <kind type=\""); // NOI18N
            if (mc.kind == Constants.MODE_KIND_EDITOR) {
                buff.append("editor"); // NOI18N
            } else if (mc.kind == Constants.MODE_KIND_VIEW) {
                buff.append("view"); // NOI18N
            } else if (mc.kind == Constants.MODE_KIND_SLIDING) {
                buff.append("sliding"); // NOI18N
            }
            buff.append("\" />\n"); // NOI18N
        }
        
        private void appendSlidingSide(ModeConfig mc, StringBuffer buff) {
            buff.append("  <slidingSide side=\"");
            buff.append(mc.side);
            buff.append("\" ");
            buff.append("/>\n"); // NOI18N
        }
        
        private void appendSlideInSize(ModeConfig mc, StringBuffer buff) {
            if( null != mc.slideInSizes ) {
                for( Iterator<String> i=mc.slideInSizes.keySet().iterator(); i.hasNext(); ) {
                    String tcId = i.next();
                    Integer size = mc.slideInSizes.get(tcId);
                    
                    buff.append("  <slide-in-size tc-id=\"");
                    buff.append(tcId);
                    buff.append("\" size=\"");
                    buff.append(size.intValue());
                    buff.append("\" />\n"); // NOI18N
                }
            }
        }

        private void appendState (ModeConfig mc, StringBuffer buff) {
            buff.append("  <state type=\""); // NOI18N
            if (mc.state == Constants.MODE_STATE_JOINED) {
                buff.append("joined"); // NOI18N
            } else if (mc.state == Constants.MODE_STATE_SEPARATED) {
                buff.append("separated"); // NOI18N
            }
            buff.append("\" />\n"); // NOI18N
        }
        
        private void appendConstraints (ModeConfig mc, StringBuffer buff) {
            if (mc.constraints.length == 0) {
                return;
            }
            buff.append("  <constraints>\n"); // NOI18N
            for (int i = 0; i < mc.constraints.length; i++) {
                SplitConstraint item = mc.constraints[i];
                buff.append("    <path orientation=\""); // NOI18N
                if (item.orientation == Constants.HORIZONTAL) {
                    buff.append("horizontal"); // NOI18N
                } else {
                    buff.append("vertical"); // NOI18N
                }
                buff.append("\" number=\"").append(item.index).append("\" weight=\"").append(item.splitWeight).append("\"/>\n"); // NOI18N
            }
            buff.append("  </constraints>\n"); // NOI18N
        }
        
        private void appendBounds (ModeConfig mc, StringBuffer buff) {
            if (mc.bounds == null) {
                return;
            }
            buff.append("  <bounds x=\"").append(mc.bounds.x).
                append("\" y=\"").append(mc.bounds.y).
                append("\" width=\"").append(mc.bounds.width).
                append("\" height=\"").append(mc.bounds.height).append("\" />\n"); // NOI18N
        }
        
        private void appendRelativeBounds (ModeConfig mc, StringBuffer buff) {
            if (mc.relativeBounds == null) {
                return;
            }
            buff.append("  <relative-bounds x=\"").append(mc.relativeBounds.x).
                append("\" y=\"").append(mc.relativeBounds.y).
                append("\" width=\"").append(mc.relativeBounds.width).
                append("\" height=\"").append(mc.relativeBounds.height).append("\" />\n"); // NOI18N
        }
        
        private void appendFrame (ModeConfig mc, StringBuffer buff) {
            buff.append("  <frame state=\"").append(mc.frameState).append("\"/>\n"); // NOI18N
        }
        
        private void appendActiveTC (ModeConfig mc, StringBuffer buff) {
            if ((mc.selectedTopComponentID != null && !"".equals(mc.selectedTopComponentID)) 
                || (mc.previousSelectedTopComponentID != null && !"".equals(mc.previousSelectedTopComponentID)) ) {
                buff.append("    <active-tc ");
                
                if (mc.selectedTopComponentID != null && !"".equals(mc.selectedTopComponentID)) {
                    String tcName = PersistenceManager.escapeTcId4XmlContent(mc.selectedTopComponentID);
                    buff.append( " id=\"").append(tcName).append("\" "); // NOI18N
                }
                
                if (mc.previousSelectedTopComponentID != null && !"".equals(mc.previousSelectedTopComponentID)) {
                    String tcName = PersistenceManager.escapeTcId4XmlContent(mc.previousSelectedTopComponentID);
                    buff.append( " prev-id=\"").append(tcName).append("\" "); // NOI18N
                }
                buff.append("/>\n"); // NOI18N
            }
        }
        
        private void appendEmptyBehavior (ModeConfig mc, StringBuffer buff) {
            buff.append("    <empty-behavior permanent=\"").append(mc.permanent).append("\"/>\n"); // NOI18N
        }
    }
    
}
