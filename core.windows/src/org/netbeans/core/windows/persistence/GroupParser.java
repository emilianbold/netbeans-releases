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

package org.netbeans.core.windows.persistence;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

import org.netbeans.core.windows.Debug;

/**
 * Handle loading/saving of Group configuration data.
 *
 * @author Marek Slama
 */

class GroupParser {
    
    private static final String INSTANCE_DTD_ID_2_0
        = "-//NetBeans//DTD Group Properties 2.0//EN"; // NOI18N
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private PropertyHandler propertyHandler;
    
    private InternalConfig internalConfig;
    
    private Map tcGroupParserMap = new HashMap(19);
    
    /** Unique group name from file name */
    private String groupName;
    
    /** true if wsmode file is present in module folder */
    private boolean inModuleFolder;
    /** true if wsmode file is present in local folder */
    private boolean inLocalFolder;
    
    public GroupParser(String name) {
        this.groupName = name;
    }
    
    /** Load group configuration including all tcgrp's. */
    GroupConfig load () throws IOException {
        //log("");
        //log("++ GroupParser.load ENTER" + " group:" + name);
        GroupConfig sc = new GroupConfig();
        readProperties(sc);
        readTCGroups(sc);
        //log("++ GroupParser.load LEAVE" + " group:" + name);
        //log("");
        return sc;
    }
    
    /** Save group configuration including all tcgrp's. */
    void save (GroupConfig sc) throws IOException {
        //log("-- GroupParser.save ENTER" + " group:" + name);
        writeProperties(sc);
        writeTCGroups(sc);
        //log("-- GroupParser.save LEAVE" + " group:" + name);
    }
    
    private void readProperties (GroupConfig sc) throws IOException {
        log("readProperties ENTER" + " group:" + getName());
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(sc, internalCfg);
        
        /*log("               specVersion: " + internalCfg.specVersion);
        log("        moduleCodeNameBase: " + internalCfg.moduleCodeNameBase);
        log("     moduleCodeNameRelease: " + internalCfg.moduleCodeNameRelease);
        log("moduleSpecificationVersion: " + internalCfg.moduleSpecificationVersion);*/
        log("readProperties LEAVE" + " group:" + getName());
    }
    
    private void readTCGroups (GroupConfig sc) throws IOException {
        log("readTCGroups ENTER" + " group:" + getName());
        
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            tcGroupParser.setInModuleFolder(false);
            tcGroupParser.setInLocalFolder(false);
        }
        
        /*log("moduleParentFolder: " + moduleParentFolder);
        log(" localParentFolder: " + localParentFolder);
        log("   moduleGroupFolder: " + moduleGroupFolder);
        log("    localGroupFolder: " + localGroupFolder);*/
        
        if (isInModuleFolder()) {
            FileObject moduleGroupFolder = moduleParentFolder.getFileObject(groupName);
            if (moduleGroupFolder != null) {
                FileObject [] files = moduleGroupFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //log("-- MODULE fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCGROUP_EXT.equals(files[i].getExt())) {
                        //wstcgrp file
                        TCGroupParser tcGroupParser;
                        if (tcGroupParserMap.containsKey(files[i].getName())) {
                            tcGroupParser = (TCGroupParser) tcGroupParserMap.get(files[i].getName());
                        } else {
                            tcGroupParser = new TCGroupParser(files[i].getName());
                            tcGroupParserMap.put(files[i].getName(), tcGroupParser);
                        }
                        tcGroupParser.setInModuleFolder(true);
                        tcGroupParser.setModuleParentFolder(moduleGroupFolder);
                    }
                }
            }
        }

        if (isInLocalFolder()) {
            FileObject localGroupFolder = localParentFolder.getFileObject(groupName);
            if (localGroupFolder != null) {
                FileObject [] files = localGroupFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //log("-- LOCAL fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCGROUP_EXT.equals(files[i].getExt())) {
                        //wstcgrp file
                        TCGroupParser tcGroupParser;
                        if (tcGroupParserMap.containsKey(files[i].getName())) {
                            tcGroupParser = (TCGroupParser) tcGroupParserMap.get(files[i].getName());
                        } else {
                            tcGroupParser = new TCGroupParser(files[i].getName());
                            tcGroupParserMap.put(files[i].getName(), tcGroupParser);
                        }
                        tcGroupParser.setInLocalFolder(true);
                        tcGroupParser.setLocalParentFolder(localGroupFolder);
                    }
                }
            }
        }
        
        /*for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            log("tcGroupParser: " + tcGroupParser.getName()
            + " isInModuleFolder:" + tcGroupParser.isInModuleFolder()
            + " isInLocalFolder:" + tcGroupParser.isInLocalFolder());
        }*/
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List tcGroupCfgList = new ArrayList(tcGroupParserMap.size());
        List toRemove = new ArrayList(tcGroupParserMap.size());
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            TCGroupConfig tcGroupCfg;
            try {
                tcGroupCfg = tcGroupParser.load();
            } catch (IOException exc) {
                //If reading of one tcGroup fails we want to log message
                //and continue.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                continue;
            }
            boolean tcGroupAccepted = acceptTCGroup(tcGroupParser);
            if (tcGroupAccepted) {
                tcGroupCfgList.add(tcGroupCfg);
            } else {
                toRemove.add(tcGroupParser);
                deleteLocalTCGroup(tcGroupParser.getName());
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            TCGroupParser tcGroupParser = (TCGroupParser) toRemove.get(i);
            tcGroupParserMap.remove(tcGroupParser.getName());
        }
        
        sc.tcGroupConfigs = (TCGroupConfig [])
            tcGroupCfgList.toArray(new TCGroupConfig[tcGroupParserMap.size()]);
        
        PersistenceManager pm = PersistenceManager.getDefault();
        for (int i = 0; i < sc.tcGroupConfigs.length; i++) {
            pm.addUsedTCId(sc.tcGroupConfigs[i].tc_id);
        }
        
        log("readTCGroups LEAVE" + " group:" + getName());
    }
    
    /** Checks if module for given tcGroup exists.
     * @return true if tcGroup is valid - its module exists
     */
    private boolean acceptTCGroup (TCGroupParser tcGroupParser) {
        InternalConfig cfg = tcGroupParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
            (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
             cfg.moduleSpecificationVersion);
            if ((curModuleInfo != null) && curModuleInfo.isEnabled()) {
                //Module is present and is enabled
                return true;
            } else {
                //Module is NOT present (it could be deleted offline)
                //or is NOT enabled
                return false;
            }
        } else {
            //No module info
            return true;
        }
    }
    
    private void writeProperties (GroupConfig sc) throws IOException {
        log("writeProperties ENTER");
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(sc, internalCfg);
        log("writeProperties LEAVE");
    }
    
    private void writeTCGroups (GroupConfig sc) throws IOException {
        log("writeTCGroups ENTER");
        //Step 1: Clean obsolete tcGroup parsers
        Map tcGroupConfigMap = new HashMap(19);
        for (int i = 0; i < sc.tcGroupConfigs.length; i++) {
            tcGroupConfigMap.put(sc.tcGroupConfigs[i].tc_id, sc.tcGroupConfigs[i]);
        }
        List toDelete = new ArrayList(10);
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            if (!tcGroupConfigMap.containsKey(tcGroupParser.getName())) {
                toDelete.add(tcGroupParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            /*log("-- GroupParser.writeTCGroups"
            + " ** REMOVE FROM MAP tcGroupParser: " + toDelete.get(i));*/
            tcGroupParserMap.remove(toDelete.get(i));
            /*log("-- GroupParser.writeTCGroups"
            + " ** DELETE tcGroupParser: " + toDelete.get(i));*/
            deleteLocalTCGroup((String) toDelete.get(i));
        }
        
        //Step 2: Create missing tcGoup parsers
        for (int i = 0; i < sc.tcGroupConfigs.length; i++) {
            if (!tcGroupParserMap.containsKey(sc.tcGroupConfigs[i].tc_id)) {
                TCGroupParser tcGroupParser = new TCGroupParser(sc.tcGroupConfigs[i].tc_id);
                tcGroupParserMap.put(sc.tcGroupConfigs[i].tc_id, tcGroupParser);
            }
        }
        
        //Step 3: Save all groups
        FileObject localFolder = localParentFolder.getFileObject(getName());
        if ((localFolder == null) && (tcGroupParserMap.size() > 0)) {
            //Create local group folder
            //log("-- GroupParser.writeTCGroups" + " CREATE LOCAL FOLDER");
            localFolder = FileUtil.createFolder(localParentFolder, getName());
        }
        //log("-- GroupParser.writeTCGroups" + " localFolder:" + localFolder);
        
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            tcGroupParser.setLocalParentFolder(localFolder);
            tcGroupParser.setInLocalFolder(true);
            tcGroupParser.save((TCGroupConfig) tcGroupConfigMap.get(tcGroupParser.getName()));
        }
        
        log("writeTCGroups LEAVE");
    }
    
    private void deleteLocalTCGroup (String tcGroupName) {
        log("deleteLocalTCGroup" + " tcGroupName:" + tcGroupName);
        if (localParentFolder == null) {
            return;
        }
        FileObject localGroupFolder = localParentFolder.getFileObject(groupName);
        if (localGroupFolder == null) {
            return;
        }
        FileObject tcGroupFO = localGroupFolder.getFileObject(tcGroupName, PersistenceManager.TCGROUP_EXT);
        if (tcGroupFO != null) {
            PersistenceManager.deleteOneFO(tcGroupFO);
        }
    }
    
    /** Removes TCGroupParser from GroupParser and cleans wstcgrp file from local folder.
     * @param tcGroupName unique name of tcgroup
     */
    void removeTCGroup (String tcGroupName) {
        //log("-- GroupParser.removeTCGroup" + " group:" + getName() + " tcGroup:" + tcGroupName);
        tcGroupParserMap.remove(tcGroupName);
        deleteLocalTCGroup(tcGroupName);
    }
    
    /** Adds TCGroupParser to GroupParser.
     * @param tcGroupName unique name of tcGroup
     */
    TCGroupConfig addTCGroup (String tcGroupName) {
        //log("-- GroupParser.addTCGroup" + " group:" + getName() + " tcGroup:" + tcGroupName);
        //Check consistency. TCGroupParser instance should not exist.
        TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(tcGroupName);
        if (tcGroupParser != null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
            "[WinSys.GroupParser.addTCGroup]" // NOI18N
            + " Warning: GroupParser " + getName() + ". TCGroupParser " // NOI18N
            + tcGroupName + " exists but it should not."); // NOI18N
            tcGroupParserMap.remove(tcGroupName);
        }
        tcGroupParser = new TCGroupParser(tcGroupName);
        FileObject moduleFolder = moduleParentFolder.getFileObject(groupName);
        tcGroupParser.setModuleParentFolder(moduleFolder);
        tcGroupParser.setInModuleFolder(true);
        FileObject localFolder = localParentFolder.getFileObject(groupName);
        tcGroupParser.setLocalParentFolder(localFolder);
        tcGroupParserMap.put(tcGroupName, tcGroupParser);
        TCGroupConfig tcGroupConfig = null;
        try {
            tcGroupConfig = tcGroupParser.load();
        } catch (IOException exc) {
            ErrorManager em = ErrorManager.getDefault();
            em.log(ErrorManager.WARNING,
            "[WinSys.GroupParser.addTCGroup]" // NOI18N
            + " Warning: GroupParser " + getName() + ". Cannot load tcGroup " +  tcGroupName); // NOI18N
            em.notify(ErrorManager.INFORMATIONAL, exc);
        }
        return tcGroupConfig;
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
        return groupName;
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
    
    void log (String s) {
        Debug.log(GroupParser.class, s);
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** Group configuration data */
        private GroupConfig groupConfig = null;
        
        /** Internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** xml parser */
        private XMLReader parser;
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject groupConfigFO;
            if (isInLocalFolder()) {
                //log("-- GroupParser.getConfigFOInput" + " looking for LOCAL");
                groupConfigFO = localParentFolder.getFileObject
                (GroupParser.this.getName(), PersistenceManager.GROUP_EXT);
            } else if (isInModuleFolder()) {
                //log("-- GroupParser.getConfigFOInput" + " looking for MODULE");
                groupConfigFO = moduleParentFolder.getFileObject
                (GroupParser.this.getName(), PersistenceManager.GROUP_EXT);
            } else {
                //XXX should not happen
                groupConfigFO = null;
            }
            //log("-- GroupParser.getConfigFOInput" + " groupConfigFO:" + groupConfigFO);
            return groupConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject groupConfigFO;
            groupConfigFO = localParentFolder.getFileObject
            (GroupParser.this.getName(), PersistenceManager.GROUP_EXT);
            if (groupConfigFO != null) {
                //log("-- GroupParser.getConfigFOOutput" + " groupConfigFO LOCAL:" + groupConfigFO);
                return groupConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(GroupParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.GROUP_EXT);
                //XXX should be improved localParentFolder can be null
                groupConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                //log("-- GroupParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return groupConfigFO;
            }
        }
        
        /**
         Reads group configuration data from XML file.
         Data are returned in output params.
         */
        void readData (GroupConfig groupCfg, InternalConfig internalCfg)
        throws IOException {
            groupConfig = groupCfg;
            internalConfig = internalCfg;
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing Group configuration file:" // NOI18N
                + GroupParser.this.getName());
            }
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    log("DUMP Group: " + GroupParser.this.getName());
                    String s = new String(arr);
                    log(s);*/
                    //DUMP END
                    
                    getXMLParser().parse(new InputSource(cfgFOInput.getInputStream()));
                }
            } catch (SAXException exc) {
                //Turn into annotated IOException
                String msg = NbBundle.getMessage(GroupParser.class,
                    "EXC_GroupParse", cfgFOInput);
                IOException ioe = new IOException(msg);
                ErrorManager.getDefault().annotate(ioe, exc);
                throw ioe;
            }
            
            groupCfg = groupConfig;
            internalCfg = internalConfig;
            
            groupConfig = null;
            internalConfig = null;
        }
        
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) throws SAXException {
            if ("group".equals(qname)) { // NOI18N
                handleGroup(attrs);
            } else if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) == 0) { // NOI18N
                //Parse version 2.0
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("name".equals(qname)) { // NOI18N
                    handleName(attrs);
                } else if ("state".equals(qname)) { // NOI18N
                    handleState(attrs);
                }
            } else {
                log("-- GroupParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }
        
        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }
        
        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }
        
        public void warning(SAXParseException ex) throws SAXException {
            // ignore
        }
        
        /** Reads element "group" */
        private void handleGroup (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.GroupParser.handleGroup]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"group\"."); // NOI18N
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
                ErrorManager.getDefault().notify(
                    ErrorManager.INFORMATIONAL,
                    new IllegalStateException(
                        "Module release code was saved as null string" // NOI18N
                        + " for module "  + internalConfig.moduleCodeNameBase // NOI18N
                        + "! Repairing.") // NOI18N
                );
                internalConfig.moduleCodeNameRelease = null;
            }
        }
        
        /** Reads element "name" */
        private void handleName (Attributes attrs) throws SAXException {
            String name = attrs.getValue("unique"); // NOI18N
            if (name != null) {
                groupConfig.name = name;
                if (!name.equals(GroupParser.this.getName())) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.GroupParser.handleName]" // NOI18N
                    + " Error: Value of attribute \"unique\" of element \"name\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.GroupParser.handleName]" // NOI18N
                + " Error: Missing required attribute \"unique\" of element \"name\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
        }
        
        /** Reads element "state" */
        private void handleState (Attributes attrs) throws SAXException {
            String opened = attrs.getValue("opened"); // NOI18N
            if (opened != null) {
                if ("true".equals(opened)) { // NOI18N
                    groupConfig.opened = true;
                } else if ("false".equals(opened)) { // NOI18N
                    groupConfig.opened = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.GroupParser.handleState]" // NOI18N
                    + " Warning: Invalid value of attribute \"opened\" of element \"state\"."); // NOI18N
                    groupConfig.opened = false;
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.GroupParser.handleState]" // NOI18N
                + " Error: Missing required attribute \"opened\" of element \"state\"."); // NOI18N
                groupConfig.opened = false;
            }
        }
        
        public void endDocument() throws SAXException {
        }
        
        public void ignorableWhitespace(char[] values, int param, int param2) 
        throws SAXException {
        }
        
        public void endElement(String str, String str1, String str2) 
        throws SAXException {
        }
        
        public void skippedEntity(String str) throws SAXException {
        }
        
        public void processingInstruction(String str, String str1) 
        throws SAXException {
        }
                
        public void endPrefixMapping(String str) throws SAXException {
        }
        
        public void startPrefixMapping(String str, String str1) 
        throws SAXException {
        }
        
        public void characters(char[] values, int param, int param2) 
        throws SAXException {
        }
        
        public void setDocumentLocator(org.xml.sax.Locator locator) {
        }
        
        public void startDocument() throws SAXException {
        }
        
        /** Writes data from asociated group to the xml representation */
        void writeData (GroupConfig sc, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(sc, ic);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = cfgFOOutput.lock();
                OutputStreamWriter osw = null;
                try {
                    OutputStream os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, "UTF-8"); // NOI18N
                    osw.write(buff.toString());
                    //log("-- DUMP Group: " + GroupParser.this.getName());
                    //log(buff.toString());
                } finally {
                    if (osw != null) {
                        osw.close();
                    }
                    lock.releaseLock();
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (GroupConfig gc, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            String curValue = null;
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"); // NOI18N
            /*buff.append("<!DOCTYPE group PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Group Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/group-properties2_0.dtd\">\n\n"); // NOI18N*/
            buff.append("<group version=\"2.0\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendName(gc, buff);
            appendState(gc, buff);
            
            buff.append("</group>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append("    <module"); // NOI18N
                buff.append(" name=\""); // NOI18N
                buff.append(ic.moduleCodeNameBase);
                if (ic.moduleCodeNameRelease != null) {
                    buff.append("/" + ic.moduleCodeNameRelease); // NOI18N
                }
                if (ic.moduleSpecificationVersion != null) { 
                    buff.append("\" spec=\""); // NOI18N
                    buff.append(ic.moduleSpecificationVersion);
                }
                buff.append("\" />\n"); // NOI18N
            }
        }

        private void appendName (GroupConfig gc, StringBuffer buff) {
            buff.append("    <name"); // NOI18N
            buff.append(" unique=\""); // NOI18N
            buff.append(gc.name);
            buff.append("\""); // NOI18N
            buff.append(" />\n"); // NOI18N
        }
        
        private void appendState (GroupConfig gc, StringBuffer buff) {
            buff.append("    <state"); // NOI18N
            buff.append(" opened=\""); // NOI18N
            if (gc.opened) {
                buff.append("true"); // NOI18N
            } else {
                buff.append("false"); // NOI18N
            }
            buff.append("\""); // NOI18N
            buff.append(" />\n"); // NOI18N
        }
        
        /** @return Newly created parser with group content handler, errror handler
         * and entity resolver
         */
        private XMLReader getXMLParser () throws SAXException {
            if (parser == null) {
                // get non validating, not namespace aware parser
                parser = XMLUtil.createXMLReader();
                parser.setContentHandler(this);
                parser.setErrorHandler(this);
                parser.setEntityResolver(this);
            }
            return parser;
        }

        /** Implementation of entity resolver. Points to the local DTD
         * for our public ID */
        public InputSource resolveEntity (String publicId, String systemId)
        throws SAXException {
            if (INSTANCE_DTD_ID_2_0.equals(publicId)) {
                InputStream is = new ByteArrayInputStream(new byte[0]);
                //getClass().getResourceAsStream(INSTANCE_DTD_LOCAL);
//                if (is == null) {
//                    throw new IllegalStateException ("Entity cannot be resolved."); // NOI18N
//                }
                return new InputSource(is);
            }
            return null; // i.e. follow advice of systemID
        }
    }
    
}

