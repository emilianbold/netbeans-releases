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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import org.netbeans.modules.compapp.jbiserver.connectors.HTTPServerConnector;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.ComponentInformationParser;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentDocument;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentStatus;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentObject;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentTableModel;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentTableRenderer;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.TableSorterUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
//import org.netbeans.modules.compapp.jbiserver.JbiClassLoader;
import org.netbeans.modules.compapp.jbiserver.management.AdministrationService;
import org.netbeans.modules.compapp.projects.jbi.JbiActionProvider;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.CreateComponentInformation;
import org.netbeans.modules.compapp.projects.jbi.ui.NoSelectedServerWarning;
import org.openide.DialogDescriptor;
//import org.netbeans.modules.compapp.jbiserver.management.FetchServerInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Handles adding and removing of additional war content.
 *
 * @author 
 * @version 
 */

final class VisualArchiveIncludesSupport {
    /**
     * DOCUMENT ME!
     */
    static final String JBIPROJ_JAR_LOC = "/org-netbeans-modules-compapp-projects-jbi.jar"; // NOI18N
    private JbiProjectProperties webProperties;
    
    /**
     * DOCUMENT ME!
     */
    final Project master;
    
    /**
     * DOCUMENT ME!
     */
    final JTable classpathTable;
    
    /**
     * DOCUMENT ME!
     */
    final JButton addArtifactButton;
    
    /**
     * DOCUMENT ME!
     */
    final JButton removeButton;
    
    /**
     * DOCUMENT ME!
     */
    final JTable jTableComp;
    
    /**
     * DOCUMENT ME!
     */
    final JButton jButtonUpdate;
    
    /**
     * DOCUMENT ME!
     */
    final JButton jButtonConfig;
    private final VisualArchiveIncludesSupport.ClasspathTableModel classpathModel;
    private Object[][] data;
    private final ArrayList actionListeners = new ArrayList();
    private ComponentTableModel mTableModel;
    private ComponentTableRenderer mTableRenderer;
    private Vector mColumnNames;
    private String nbuser;
    private String compFilename;
    private String compFileSrc;
    private String compFileDst;
    private String jbiFilename;
    private String jbiFileLoc;
    private JComboBox comboTarget = null;
    private DefaultComboBoxModel comboModel = null;
    private List<String> comboValues = new ArrayList<String>();
    private List<VisualClassPathItem> bindingList = null; 
    private AntArtifact bcjar = null;
    private String mModuleDir = null;
    
    
    
    /**
     * Creates a new VisualArchiveIncludesSupport object.
     *
     * @param webProperties DOCUMENT ME!
     * @param jTableComp DOCUMENT ME!
     * @param classpathTable DOCUMENT ME!
     * @param jButtonUpdate DOCUMENT ME!
     * @param jButtonConfig DOCUMENT ME!
     * @param addArtifactButton DOCUMENT ME!
     * @param removeButton DOCUMENT ME!
     */
    public VisualArchiveIncludesSupport(
            JbiProjectProperties webProperties, JTable jTableComp, JTable classpathTable,
            JButton jButtonUpdate, JButton jButtonConfig, JButton addArtifactButton,
            JButton removeButton
            ) {
        // Remember all buttons
        this.webProperties = webProperties;
        this.jTableComp = jTableComp;
        this.jButtonUpdate = jButtonUpdate;
        this.jButtonConfig = jButtonConfig;
        this.jButtonConfig.setEnabled(false);
        
        this.bindingList = webProperties.getBindingList();
        
        // combobox cell editor for target selection
        comboModel = new DefaultComboBoxModel(new String[] {" "}); // NOI18N
        comboTarget = new JComboBox(comboModel);
        
        this.classpathTable = classpathTable;
        this.classpathModel = new VisualArchiveIncludesSupport.ClasspathTableModel();
        this.classpathTable.setModel(classpathModel);
        this.classpathTable.getColumnModel().getColumn(0).setHeaderValue(
                NbBundle.getMessage(VisualArchiveIncludesSupport.class, "TXT_Archive_Item") // NOI18N
                );
        this.classpathTable.getColumnModel().getColumn(1).setHeaderValue(
                NbBundle.getMessage(VisualArchiveIncludesSupport.class, "TXT_Archive_PathInArchive") // NOI18N
                );
        this.classpathTable.getColumnModel().getColumn(0).setCellRenderer(
                new VisualArchiveIncludesSupport.ClassPathCellRenderer()
                );
        this.classpathTable.getColumnModel().getColumn(1).setCellEditor(
                new TargetComboBoxEditor(comboTarget)
                );
        
        this.classpathTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        this.addArtifactButton = addArtifactButton;
        this.removeButton = removeButton;
        this.master = webProperties.getProject();
        
        // Register the listeners
        VisualArchiveIncludesSupport.ClasspathSupportListener csl = new VisualArchiveIncludesSupport.ClasspathSupportListener();
        
        // On all buttons
        this.jButtonUpdate.addActionListener(csl);
        this.jButtonConfig.addActionListener(csl);
        this.addArtifactButton.addActionListener(csl);
        this.removeButton.addActionListener(csl);
        
        // On list selection
        classpathTable.getSelectionModel().addListSelectionListener(csl);
        classpathModel.addTableModelListener(csl);
        
        // Set the initial state of the buttons
        csl.valueChanged(null);
        
        // init locals
//        nbuser = "C:/Documents and Settings/jqian/.netbeans/5.5dev";    // TMP
        nbuser = System.getProperty("netbeans.user"); // NOI18N
        compFilename = "ComponentInformation.xml"; // NOI18N
        jbiFilename = "AssemblyInformation.xml"; // NOI18N
        compFileSrc = nbuser + "/" + compFilename; // NOI18N
        
        List os = (List) webProperties.get(JbiProjectProperties.META_INF);
        
        Project p = webProperties.getProject();
        File pf = FileUtil.toFile(p.getProjectDirectory());
        
        if ((os != null) && (os.size() > 0)) {
            
            String path = pf.getPath() + "/" + os.get(0).toString(); // NOI18N
            compFileDst = path + "/" + compFilename; // NOI18N
            jbiFileLoc = path + "/" + jbiFilename; // NOI18N
        }
        
        AntProjectHelper helper = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        bcjar = helper.createSimpleAntArtifact(
                "CAPS.jbiserver:bpelse", "build/BCDeployment.jar", // NOI18N
                helper.getStandardPropertyEvaluator(), "dist_bc", "clean" // NOI18N
                );
        
        EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        mModuleDir = ep.getProperty(JbiProject.MODULE_INSTALL_DIR);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param items DOCUMENT ME!
     */
    public void setVisualWarItems(List items) {
        Object[][] data = new Object[items.size()][2];
        this.data = data;
        
        for (int i = 0; i < items.size(); i++) {
            VisualClassPathItem vi = (VisualClassPathItem) items.get(i);
            classpathModel.setValueAt(vi, i, 0);
            classpathModel.setValueAt("", i, 1); // NOI18N
        }
        
        classpathModel.fireTableDataChanged();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getVisualWarItems() {
        ArrayList items = new ArrayList();
        
        for (int i = 0; i < data.length; i++)
            items.add((VisualClassPathItem) classpathModel.getValueAt(i, 0));
        
        return items;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param tml DOCUMENT ME!
     */
    public void addTableModelListener(TableModelListener tml) {
        classpathModel.addTableModelListener(tml);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param tml DOCUMENT ME!
     */
    public void removeTableModelListener(TableModelListener tml) {
        classpathModel.removeTableModelListener(tml);
    }
    
    /**
     * Action listeners will be informed when the value of the list changes.
     *
     * @param listener DOCUMENT ME!
     */
    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }
    
    private void fireActionPerformed() {
        ArrayList listeners;
        
        synchronized (this) {
            listeners = new ArrayList(actionListeners);
        }
        
        ActionEvent ae = new ActionEvent(this, 0, null);
        
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ActionListener al = (ActionListener) it.next();
            al.actionPerformed(ae);
        }
    }
    
    // Private methods ---------------------------------------------------------
    private void addArtifacts(AntArtifact[] artifacts) {
        // Detect duplicate first
        Map<AntArtifact, VisualClassPathItem> viMap = 
                new HashMap<AntArtifact, VisualClassPathItem>();
        
        List<AntArtifact> uniqueArtifacts = new ArrayList<AntArtifact>();
        for (int i = 0; i < artifacts.length; i++) {
            VisualClassPathItem vi = new VisualClassPathItem(
                        artifacts[i], VisualClassPathItem.TYPE_ARTIFACT, null,
                        artifacts[i].getArtifactLocations()[0].toString(), true
                        );
            viMap.put(artifacts[i], vi);
            boolean duplicate = false;
            for (int j = 0; j < data.length; j++) {
                if (data[j][0].toString().equals(vi.toString())) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                String msg = NbBundle.getMessage(AddProjectAction.class, 
                        "MSG_DuplicateJBIModule", vi.toString()); // NOI18N
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                uniqueArtifacts.add(artifacts[i]);
            }
        }
        
        if (uniqueArtifacts.size() == 0) {
            return;
        }
        
        Object[][] newData = new Object[data.length + uniqueArtifacts.size()][2];
        
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        
        for (int i = 0; i < uniqueArtifacts.size(); i++) {
            AntArtifact artifact = uniqueArtifacts.get(i);
            VisualClassPathItem vi = viMap.get(artifact);
            newData[data.length + i][0] = vi;
            newData[data.length + i][1] = getDefaultTarget(vi.getAsaType());
            if (VisualClassPathItem.isJavaEEProjectAntArtifact(artifact)){
                webProperties.addSunResourceProject(artifact);
            }
        }
        
        data = newData;
        classpathModel.fireTableRowsInserted(data.length,
                (data.length + uniqueArtifacts.size()) - 1);
        
        fireActionPerformed();
    }
    
    private void removeElements() {
        ListSelectionModel sm = classpathTable.getSelectionModel();
        Object aa = null;
        
        int index = sm.getMinSelectionIndex();
        
        if (sm.isSelectionEmpty()) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        
        Collection elements = new ArrayList();
        final int n0 = data.length;
        
        for (int i = 0; i < n0; i++) {
            if (!sm.isSelectedIndex(i)) {
                elements.add(data[i]);
            }else{
                if (data[i][0] instanceof VisualClassPathItem){
                    aa = ((VisualClassPathItem)data[i][0]).getObject();
                    if ((aa instanceof AntArtifact) &&
                            (VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact)aa))){
                        webProperties.removeSunResourceProject((AntArtifact)aa);
                    }
                }
            }
        }
        
        final int n = elements.size();
        data = (Object[][]) elements.toArray(new Object[n][2]);
        classpathModel.fireTableRowsDeleted(elements.size(), n0 - 1);
        
        if (index >= n) {
            index = n - 1;
        }
        
        sm.setSelectionInterval(index, index);
        
        fireActionPerformed();
    }
    
    private String parseTargetID(String str) {
        if (str != null) {
            int i = str.indexOf(" ["); // NOI18N
            int j = str.lastIndexOf(']');
            
            if ((i > 0) && (j > 0)) {
                return str.substring(i + 2, j);
            }
        }
        
        return null;
    }
    
    private String getDefaultTarget(String type) {
        int tsize = comboValues.size();
        
        for (int i = 0; i < tsize; i++) {
            String val = comboValues.get(i);
            
            if (val.startsWith(type)) {
                return val;
            }
        }
        
        return ""; // NOI18N
    }
    
    private void updateModels(String jar, String uuid, String desc, String cid) {
        for (int i = 0, size = classpathModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
            
            String shortName = vi.getShortName();
            if (shortName.compareTo(jar) == 0 ||
                    // backward compatibility
                    shortName.endsWith(".jar") &&   // NOI18N
                    jar.endsWith("@SEDeployment.jar") &&    // NOI18N
                    shortName.substring(0, shortName.length() - 4).equals(
                    jar.substring(0, jar.length() - 17))) {
                vi.setAsaDescription(desc);
                vi.setAsaTarget(cid);
                
                // lookup the targe list
                for (int j = 0, tsize = comboValues.size(); j < tsize; j++) {
                    String target = comboValues.get(j);
                    
                    if (target.indexOf(cid) > 0) {
                        classpathModel.setValueAt(target, i, 1);
                        
                        return;
                    }
                }
                
                // not set yet.. default to the first non-blank traget on the list
                classpathModel.setValueAt(getDefaultTarget(vi.getAsaType()), i, 1);
                
                return;
            }
        }
        
        // OK this is not a SE jar..
        for (VisualClassPathItem vi : bindingList) {
            if (vi.getAsaTarget().compareTo(cid) == 0) {
                vi.setAsaDescription(desc);                
                return;
            }
        }
    }
    
    private void updateTargetList(String cid) {
        if (mTableModel != null) {
            for (int i = 0, size = mTableModel.getRowCount(); i < size; i++) {
                // String cmp = (String) mTableModel.getValueAt(i, 2) + "-" + (String) mTableModel.getValueAt(i, 3);
                String cmp = (String) mTableModel.getValueAt(i, 2);
                if (cid.compareToIgnoreCase(cmp) == 0) {
                    mTableModel.setValueAt(new Boolean(true), i, 0);
                    
                    return;
                }
            }
        }
    }
    
    private void loadAssemblyInfo() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            
            Document doc = factory.newDocumentBuilder().parse(new File(jbiFileLoc));
            
            NodeList serviceUnitNodeList = doc.getElementsByTagName("service-unit"); // NOI18N
            String name = null;
            String desc = null;
            String cid = null;
            String jar = null;
            
            for (int i = 0, isize = serviceUnitNodeList.getLength(); i < isize; i++) {
                NodeList kids = serviceUnitNodeList.item(i).getChildNodes();
                
                for (int k = 0, ksize = kids.getLength(); k < ksize; k++) {
                    Node n = kids.item(k);
                    
                    if (n.getNodeName().compareTo("identification") == 0) { // NOI18N
                        NodeList ids = n.getChildNodes();
                        
                        for (int j = 0, jsize = ids.getLength(); j < jsize; j++) {
                            Node m = ids.item(j);
                            
                            if (m.getNodeName().compareTo("name") == 0) { // NOI18N
                                name = m.getFirstChild().getNodeValue();
                            } else if (m.getNodeName().compareTo("description") == 0) { // NOI18N
                                desc = m.getFirstChild().getNodeValue();
                            }
                        }
                    } else if (n.getNodeName().compareTo("target") == 0) { // NOI18N
                        NodeList ids = n.getChildNodes();
                        
                        for (int j = 0, jsize = ids.getLength(); j < jsize; j++) {
                            Node m = ids.item(j);
                            
                            if (m.getNodeName().compareTo("component-name") == 0) { // NOI18N
                                cid = m.getFirstChild().getNodeValue();
                            } else if (m.getNodeName().compareTo("artifacts-zip") == 0) { // NOI18N
                                jar = m.getFirstChild().getNodeValue();
                                
                                if (jar.startsWith(name)) {
                                    jar = jar.substring(name.length());
                                }
                            }
                        }
                    }
                }
                
                if (jar != null) {
                    updateModels(jar, name, desc, cid);
                    updateTargetList(cid);
                }
            }
        } catch (Exception e) {
            // A parsing error occurred; the xml input is not valid
        }
    }
    
    // Target Component support...
    //--------------------------------------------------------------------------
    public void initTable() {
        java.util.Vector datas = new java.util.Vector(1);
        
        // setup the table model to  use
        mColumnNames = new Vector();
        mColumnNames.addElement(" "); // NOI18N
        mColumnNames.addElement(org.openide.util.NbBundle.getMessage(VisualArchiveIncludesSupport.class, "Type"));  // NOI18N
        mColumnNames.addElement(org.openide.util.NbBundle.getMessage(VisualArchiveIncludesSupport.class, "Component_ID"));  // NOI18N
        mTableModel = new ComponentTableModel(datas, mColumnNames);
        
        // setup table sorter to use
        TableSorterUtil mTableSorter = new TableSorterUtil(mTableModel);
        
        // create the table
        jTableComp.setModel(mTableSorter);
        
        // setup the mouse listener to header
        // mTableSorter.addMouseListenerToHeaderInTable(jTableComp);
        // setup table attributes
        jTableComp.setShowHorizontalLines(true);
        jTableComp.setShowVerticalLines(false);
        jTableComp.setShowGrid(false);
        jTableComp.setAutoCreateColumnsFromModel(false);
        
        //jTableComp.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jTableComp.setRowSelectionAllowed(true);
        jTableComp.setColumnSelectionAllowed(false);
        jTableComp.getTableHeader().setReorderingAllowed(false);
        jTableComp.getTableHeader().setAlignmentY(JTable.LEFT_ALIGNMENT);
        jTableComp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // setup renderer
        mTableRenderer = new ComponentTableRenderer(mTableModel);
        jTableComp.getColumnModel().getColumn(0).setCellRenderer(mTableRenderer);
        jTableComp.getColumnModel().getColumn(1).setCellRenderer(mTableRenderer);
        jTableComp.getColumnModel().getColumn(2).setCellRenderer(mTableRenderer);
        jTableComp.getColumnModel().getColumn(0).setMaxWidth(30);
        jTableComp.getColumnModel().getColumn(1).setPreferredWidth(70);
        jTableComp.getColumnModel().getColumn(2).setPreferredWidth(300);
        
        mTableModel.addTableModelListener(new TargetSupportListener());
    }
    
    /**
     * DOCUMENT ME!
     */
    public void initTableValues() {
        java.util.Vector rowData = loadComponentInfo(false);
        mTableModel.setDataVector(rowData, mColumnNames);
        mTableRenderer.setModel(mTableModel);
        
        // load the existing assembly info from config
        loadAssemblyInfo();
        
        mTableModel.fireTableDataChanged();
    }
    
    private void updateComboTarget() {
        comboModel.removeAllElements();
        comboModel.addElement(" "); // NOI18N
        
        for (int i = 0; i < comboValues.size(); i++) {
            comboModel.addElement(comboValues.get(i));
        }
    }
    
    private void updateComboTargetWithType(String type) {
        comboModel.removeAllElements();
        comboModel.addElement(" "); // NOI18N
        
        if ((type == null) || (type.length() < 1)) {
            return;
        }
        
        for (int i = 0; i < comboValues.size(); i++) {
            String val = comboValues.get(i);
            
            if (val.startsWith(type)) {
                comboModel.addElement(val);
            }
        }
    }
    
    private Vector myLoadComponentInfo(List compList, boolean inDeployment) {
        java.util.Vector rowData = new java.util.Vector(1);
        
        // todo: reading the cache config data if any...
//        File dst = new File(compFileDst);
        
        try {
            if (compList != null) {
//            if (dst.exists()) {
//                JBIComponentDocument compDoc = ComponentInformationParser.parse(dst);
//                List compList = compDoc.getJbiComponentList();
                Iterator iterator = compList.iterator();
                JBIComponentStatus component = null;
                
                comboValues.clear();
                bindingList.clear();
                
                while ((iterator != null) && (iterator.hasNext() == true)) {
                    component = (JBIComponentStatus) iterator.next();
                    
                    if (component.getType().compareToIgnoreCase("shared-library") == 0) { // NOI18N
                        continue;
                    }
                    
                    ComponentObject comp = new ComponentObject(
                            // component.getComponentId(),
                            component.getType(), component.getState(), component.getName(),
                            component.getDescription(), inDeployment
                            ); // update this when loading assembly info
                    rowData.add(comp);
                    
                    // update the target combo model..
                    //comboValues.addElement(comp.getName() + " [" +comp.getId() + "]");
                    comboValues.add(component.getName());
                    
                    if (component.getType().compareToIgnoreCase("Binding") == 0) { // NOI18N
                        VisualClassPathItem vi = new VisualClassPathItem(
                                bcjar, VisualClassPathItem.TYPE_ARTIFACT, "BCDeployment.jar", null, // NOI18N
                                inDeployment
                                ); // true);
                        vi.setAsaTarget(component.getName());
                        bindingList.add(vi);
                    }
                }
                
                updateComboTarget();
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        
        return rowData;
    }
    
    private Vector loadComponentInfo(boolean inDeployment) {
        java.util.Vector rowData = new java.util.Vector(1);
        
        // todo: reading the cache config data if any...
        File dst = new File(compFileDst);
        
        try {
            if (dst.exists()) {
                JBIComponentDocument compDoc = ComponentInformationParser.parse(dst);
                List<JBIComponentStatus> compList = compDoc.getJbiComponentList();
                                
                comboValues.clear();
                bindingList.clear();
                
                for (JBIComponentStatus component : compList) {
                    
                    ComponentObject comp = new ComponentObject(
                            // component.getComponentId(),
                            component.getType(), component.getState(), component.getName(),
                            component.getDescription(), inDeployment
                            ); // update this when loading assembly info
                    rowData.add(comp);
                    
                    // update the target combo model..
                    //comboValues.addElement(comp.getName() + " [" +comp.getId() + "]");
                    comboValues.add(component.getName());
                    
                    if (component.getType().compareToIgnoreCase("Binding") == 0) { // NOI18N
                        VisualClassPathItem vi = new VisualClassPathItem(
                                bcjar, VisualClassPathItem.TYPE_ARTIFACT, "BCDeployment.jar", null, // NOI18N
                                inDeployment
                                ); // true);
                        vi.setAsaTarget(component.getName());
                        bindingList.add(vi);
                    }
                }
                
                updateComboTarget();
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        
        return rowData;
    }
    
    private void updateAsaTarget() {
        for (int i = 0, size = classpathModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
            String tid = (String) classpathModel.getValueAt(i, 1);
            
            if ((tid == null) || (tid.trim().length() < 1)) {
                // not set yet.. default to the first non-blank traget on the list
                classpathModel.setValueAt(getDefaultTarget(vi.getAsaType()), i, 1);
            }
        }
    }
    
    
    private boolean isSelectedServer() {
        String instance = (String) webProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected = true;
        
        if ((instance == null) || !JbiManager.isAppServer(instance)) {
            String[] serverIDs = JbiManager.getAppServers();
            
            if (serverIDs.length < 1) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(
                        NbBundle.getMessage(
                        JbiActionProvider.class, "MSG_NoInstalledServerError" // NOI18N
                        ),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }
            
            NoSelectedServerWarning panel = new NoSelectedServerWarning( serverIDs );
            
            Object[] options = new Object[] {
                DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(
                    NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title" // NOI18N
                    ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null
                    );
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance();
                selected = instance != null;
                
                if (selected) {
//                    JbiProjectProperties wpp = new JbiProjectProperties(
//                            project, antProjectHelper, refHelper
//                        );
                    webProperties.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    webProperties.store();
//                    System.out.println("setting server instance to be " + instance);
//                    wpp.store();
                }
            }
            
            dlg.dispose();
        }
        
        if ((instance == null) || (!selected)) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoSelectedServerError" // NOI18N
                    ),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        } else if (!JbiManager.isRunningAppServer(instance)) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoRunningServerError" // NOI18N
                    ),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        }
        
        return true;
    }
    
    private void myFetchInfo() {
        
        String serverInstance = (String) webProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
//        System.out.println("VisualArchiveIncludesSupport.myFetchInfo(): serverInstance=" + serverInstance);
        
        if (serverInstance == null) {
            if (!isSelectedServer()) {
                return;
            }
            serverInstance = (String) webProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
            
        } else if (!JbiManager.isRunningAppServer(serverInstance)) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoRunningServerError" // NOI18N
                    ),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        
        String hostName = (String) webProperties.get(JbiProjectProperties.HOST_NAME_PROPERTY_KEY);
        String port = (String) webProperties.get(JbiProjectProperties.ADMINISTRATION_PORT_PROPERTY_KEY);
        String userName = (String) webProperties.get(JbiProjectProperties.USER_NAME_PROPERTY_KEY);
        String password = (String) webProperties.get(JbiProjectProperties.PASSWORD_PROPERTY_KEY);
        String location = (String) webProperties.get(JbiProjectProperties.LOCATION_PROPERTY_KEY);
        
        if (hostName == null || port == null || userName == null || password == null
                || location == null) {
            
            Properties properties = JbiManager.getServerInstanceProperties(serverInstance);
            
            hostName = (String) properties.getProperty(JbiManager.HOSTNAME_ATTR);
            port = (String) properties.getProperty(JbiManager.PORT_ATTR);
            userName = (String) properties.getProperty(JbiManager.USERNAME_ATTR);
            password = (String) properties.getProperty(JbiManager.PASSWORD_ATTR);
            //location = (String) properties.getProperty(JbiManager.
            
            webProperties.put(JbiProjectProperties.HOST_NAME_PROPERTY_KEY, hostName);
            webProperties.put(JbiProjectProperties.ADMINISTRATION_PORT_PROPERTY_KEY, port);
            webProperties.put(JbiProjectProperties.USER_NAME_PROPERTY_KEY, userName);
            webProperties.put(JbiProjectProperties.PASSWORD_PROPERTY_KEY, password);
        }
        
        ClassLoader jbiClassLoader = JbiManager.getJbiClassLoader(serverInstance);
        
//        System.out.println("VisualArchiveIncludesSupport.fetchInfo():");
//        System.out.println("hostName=" + hostName);
//        System.out.println("port=" + port);
//        System.out.println("userName=" + userName);
//        System.out.println("password=" + password);
        
        if (hostName == null || port == null || userName == null || password == null || jbiClassLoader == null) {
            String msg = "The application server is not set up correctly or it is not running.";   // FIXME // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else {
            HTTPServerConnector httpServerConnector =
                    new HTTPServerConnector(hostName, port, userName, password, jbiClassLoader);
            
            AdministrationService adminService = null;
            try {
                adminService = new AdministrationService(httpServerConnector);
            } catch (Exception e) {
                String msg = e.getMessage();
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
            if (adminService != null) {
                adminService.constructDocumentObject();
                JBIComponentDocument componentDocument = adminService.getJBIComponentDocument();
                componentDocument.dump();    // XXX
                List compList = componentDocument.getJbiComponentList();
                
                java.util.Vector rowData = myLoadComponentInfo(compList, true);
                
                mTableModel.setDataVector(rowData, mColumnNames);
                mTableRenderer.setModel(mTableModel);
                mTableModel.fireTableDataChanged();
                
                // TODO: save on OK instead of Update; merge instead of overwrite
//                try {
//                    updateComponentInformationFiles(componentDocument);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
            }
        }
    }
    
    private void updateComponentInformationFiles(JBIComponentDocument componentDocument) throws IOException {
        FileObject rootFileObject = webProperties.getProject().getProjectDirectory();
        FileObject confFileObject = rootFileObject.getFileObject("src").getFileObject("conf"); // NOI18N
        File confFile = FileUtil.toFile(confFileObject);
        try {
            CreateComponentInformation infoDoc = new CreateComponentInformation();
            infoDoc.buildComponentDOMTree(componentDocument);
            infoDoc.writeToComponentFile(confFile.getPath()); // confRoot.getPath());
            // TODO: namespace missing for BC, resulting empty BCComponentList file
//            infoDoc.buildBCDOMTree(componentDocument);
//            infoDoc.writeToBCFile(confFile.getPath()); // confRoot.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateProperties(JbiProjectProperties prop, 
            VisualArchiveIncludesSupport.ClasspathTableModel classpathModel) {
        List<String> targetIDs = new ArrayList<String>();
        VisualClassPathItem vcpi = null;
        List<VisualClassPathItem> javaEEProjs = new ArrayList<VisualClassPathItem>();
        Object aa = null;
        
        for (int i = 0; i < classpathModel.getRowCount(); i++) {
            targetIDs.add((String) classpathModel.getValueAt(i, 1));
            
            vcpi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
            if (vcpi != null) {
                aa = vcpi.getObject();
                if ( (aa instanceof AntArtifact) && VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact) aa)){
                    javaEEProjs.add(vcpi);
                }
            }
        }
        
        prop.put(JbiProjectProperties.JBI_CONTENT_COMPONENT, targetIDs);
        prop.put(JbiProjectProperties.JBI_JAVAEE_JARS, javaEEProjs);
    }
    
    
    // Private innerclasses ----------------------------------------------------
    private class TargetSupportListener implements TableModelListener {
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            if ((e.getType() == TableModelEvent.UPDATE) && (e.getColumn() == 0)) {
                int rn = e.getFirstRow();
                // String tid = (String) mTableModel.getValueAt(rn, 2) + "-" + (String) mTableModel.getValueAt(rn, 3);
                String tid = (String) mTableModel.getValueAt(rn, 2);
                for (int i = 0, size = bindingList.size(); i < size; i++) {
                    VisualClassPathItem vi = (VisualClassPathItem) bindingList.get(i);
                    
                    if (vi != null) {
                        String sid = vi.getAsaTarget();
                        
                        if ((sid != null) && (sid.compareToIgnoreCase(tid) == 0)) {
                            boolean b = ((Boolean) mTableModel.getValueAt(rn, 0)).booleanValue();
                            vi.setInDeployment(b);
                        }
                    }
                }
            }
        }
    }
    
    private class ClasspathSupportListener implements ActionListener, ListSelectionListener,
            TableModelListener {
        // Implementation of ActionListener ------------------------------------
        
        /**
         * Handles button events
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            
            if (source == addArtifactButton) {
                List<String> javaeeAntArtifactTypes = new ArrayList<String>();
                javaeeAntArtifactTypes.addAll(JbiProjectConstants.JAVA_EE_AA_TYPES);
                javaeeAntArtifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);
                AntArtifact[] artifacts = AntArtifactChooser.showDialog(
                        javaeeAntArtifactTypes, master, null, null
                        );
                
                if (artifacts != null) {
                    addArtifacts(artifacts);
                }
            } else if (source == removeButton) {
                removeElements();
            } else if (source == jButtonUpdate) {
                // checkRoots();
                RequestProcessor.getDefault().post(
                        new Runnable() {
                    public void run() {
                        myFetchInfo();
                    }
                }
                );
            } else if (source == jButtonConfig) {
                // removeElements();
            }
        }
        
        // ListSelectionModel --------------------------------------------------
        
        /**
         * Handles changes in the selection
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel sm = (DefaultListSelectionModel) classpathTable.getSelectionModel();
            int index = sm.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean remove = index != -1;
            
            // and when the selection does not contain unremovable item
            if (remove) {
                VisualClassPathItem vcpi = (VisualClassPathItem) classpathModel.getValueAt(
                        index, 0
                        );
                
                if (!vcpi.canDelete()) {
                    remove = false;
                }
            }
            
            removeButton.setEnabled(remove);
        }
        
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            updateProperties(webProperties, classpathModel);
            
            if (e.getColumn() == 1) {
                //VisualClassPathItem cpItem = (VisualClassPathItem) classpathModel.getValueAt(e.getFirstRow(), 0);
                // cpItem.setPathInWAR((String) classpathModel.getValueAt(e.getFirstRow(), 1));
                fireActionPerformed();
            }
        }
    }
    
    private static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        /**
         * DOCUMENT ME!
         *
         * @param table DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param hasFocus DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
                ) {
            if (value instanceof VisualClassPathItem) {
                final VisualClassPathItem item = (VisualClassPathItem) value;
                setIcon(item.getIcon());
            }
            
            final String s = (value == null) ? null : value.toString();
            
            return super.getTableCellRendererComponent(table, s, isSelected, false, row, column);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @author 
     * @version 
     */
    class ClasspathTableModel extends AbstractTableModel {
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getColumnCount() {
            return 2; //classpath item name, item location within WAR
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getRowCount() {
            if (data == null) {
                return 0;
            }
            
            return data.length;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean isCellEditable(int row, int col) {
            if (col == 1) {
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         */
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }
    
    private class TargetComboBoxEditor extends DefaultCellEditor {
        /**
         * DOCUMENT ME!
         */
        JComboBox combo = null;
        
        /**
         * Creates a new TargetComboBoxEditor object.
         *
         * @param combo DOCUMENT ME!
         */
        public TargetComboBoxEditor(JComboBox combo) {
            super(combo);
            this.combo = combo;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param table DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column
                ) {
            String type = null;
            VisualClassPathItem vi = (VisualClassPathItem) classpathModel.getValueAt(row, 0);
            
            if (vi != null) {
                type = vi.getAsaType();
            }
            
            updateComboTargetWithType(type);
            
            return combo;
        }
    }
}
