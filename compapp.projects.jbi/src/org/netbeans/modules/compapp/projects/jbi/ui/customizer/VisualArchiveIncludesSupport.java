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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.jbi.ui.common.JBIComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentObject;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.ComponentTableModel;
import org.netbeans.modules.compapp.projects.jbi.ui.deployInfo.TableSorterUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
import org.netbeans.modules.compapp.projects.jbi.AdministrationServiceHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiActionProvider;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.NoSelectedServerWarning;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentInformationParser;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Handles adding and removing of additional war content.
 *
 * @author jqian
 */
final class VisualArchiveIncludesSupport {
        
    private static final int COMPONENT_TYPE_COLUMN = 0;
    private static final int COMPONENT_NAME_COLUMN = 1;
        
    private JbiProjectProperties projProperties;
    
    private Project project;    
    
    private JTable componentTable;    
    private ComponentTableModel componentTableModel;    
    
    private JTable classpathTable;        
    private ClasspathTableModel classpathTableModel;
    private Object[][] data;
    
    private JButton addProjectButton;    
    private JButton removeProjectButton;  
    private JButton updateComponentsButton;       
      
    private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
    
    private String compInfoFileLoc;
    private String assemblyInfoFileLoc;
    
    private List<String> componentNames = new ArrayList<String>();
    
    private List<VisualClassPathItem> bindingVisualClassPathItems = null; 
    private AntArtifact bcJar = null;
    
    
    /**
     * Creates a new VisualArchiveIncludesSupport object.
     *
     * @param projProperties 
     * @param componentTable 
     * @param classpathTable 
     * @param updateComponentsButton 
     * @param addProjectButton 
     * @param removeProjectButton 
     */
    public VisualArchiveIncludesSupport(
            JbiProjectProperties projProperties, 
            JTable componentTable, 
            JTable classpathTable,
            JButton updateComponentsButton, 
            JButton addProjectButton,
            JButton removeProjectButton) {
        
        this.projProperties = projProperties;
        this.componentTable = componentTable;
        this.classpathTable = classpathTable;
        this.updateComponentsButton = updateComponentsButton;
        this.addProjectButton = addProjectButton;
        this.removeProjectButton = removeProjectButton;        
        this.project = projProperties.getProject();       
                
        this.bindingVisualClassPathItems = projProperties.getBindingList();
                
        initClassPathTable();          
        
        initComponentTable();
                
        // Register the listeners
        ClasspathSupportListener csl = new ClasspathSupportListener();        
        this.updateComponentsButton.addActionListener(csl);
        this.addProjectButton.addActionListener(csl);
        this.removeProjectButton.addActionListener(csl);
        this.classpathTable.getSelectionModel().addListSelectionListener(csl);
        classpathTableModel.addTableModelListener(csl);
        
        // Set the initial state of the buttons
        csl.valueChanged(null);
        
        // init locals        
        Project p = projProperties.getProject();
        File pf = FileUtil.toFile(p.getProjectDirectory());
        
        List os = (List) projProperties.get(JbiProjectProperties.META_INF);        
        if ((os != null) && (os.size() > 0)) {            
            String path = pf.getPath() + File.separator + os.get(0).toString(); 
            compInfoFileLoc = path + File.separator + JbiProject.COMPONENT_INFO_FILE_NAME; 
            assemblyInfoFileLoc = path + File.separator + JbiProject.ASSEMBLY_INFO_FILE_NAME; 
        }
        
        AntProjectHelper helper = p.getLookup().lookup(AntProjectHelper.class);
        bcJar = helper.createSimpleAntArtifact(
                "CAPS.jbiserver:bpelse", "build/BCDeployment.jar", // NOI18N
                helper.getStandardPropertyEvaluator(), "dist_bc", "clean" // NOI18N
                );
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @param items DOCUMENT ME!
     */
    public void setVisualWarItems(List<VisualClassPathItem> items) {
        this.data = new Object[items.size()][2];
        
        for (int i = 0; i < items.size(); i++) {
            VisualClassPathItem vi = items.get(i);
            classpathTableModel.setValueAt(vi, i, 0);            
            classpathTableModel.setValueAt(vi.getAsaType(), i, 1);
        }
        
        classpathTableModel.fireTableDataChanged();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<VisualClassPathItem> getVisualWarItems() {
        List<VisualClassPathItem> items = new ArrayList<VisualClassPathItem>();
        
        for (int i = 0; i < data.length; i++) {
            items.add((VisualClassPathItem) classpathTableModel.getValueAt(i, 0));
        }
        
        return items;
    }
    
//    /**
//     * DOCUMENT ME!
//     *
//     * @param tml DOCUMENT ME!
//     */
//    public void addTableModelListener(TableModelListener tml) {
//        classpathTableModel.addTableModelListener(tml);
//    }
//    
//    /**
//     * DOCUMENT ME!
//     *
//     * @param tml DOCUMENT ME!
//     */
//    public void removeTableModelListener(TableModelListener tml) {
//        classpathTableModel.removeTableModelListener(tml);
//    }
    
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
        List<ActionListener> listeners;
        
        synchronized (this) {
            listeners = new ArrayList(actionListeners);
        }
        
        ActionEvent ae = new ActionEvent(this, 0, null);
        
        for (ActionListener al : listeners) {
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
            newData[data.length + i][1] = vi.getAsaType();
            if (VisualClassPathItem.isJavaEEProjectAntArtifact(artifact)){
                projProperties.addSunResourceProject(artifact);
            }
        }
        
        data = newData;
        classpathTableModel.fireTableRowsInserted(data.length,
                (data.length + uniqueArtifacts.size()) - 1);
        
        fireActionPerformed();
    }
    
    private void removeElements() {
        ListSelectionModel sm = classpathTable.getSelectionModel();
        Object vcpi = null;
        
        int index = sm.getMinSelectionIndex();
        
        if (sm.isSelectionEmpty()) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        
        Collection elements = new ArrayList();
        final int n0 = data.length;
        
        for (int i = 0; i < n0; i++) {
            if (!sm.isSelectedIndex(i)) {
                elements.add(data[i]);
            } else {
                if (data[i][0] instanceof VisualClassPathItem){
                    vcpi = ((VisualClassPathItem)data[i][0]).getObject();
                    if ((vcpi instanceof AntArtifact) &&
                            (VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact)vcpi))){
                        projProperties.removeSunResourceProject((AntArtifact)vcpi);
                    }
                }
            }
        }
        
        int n = elements.size();
        data = (Object[][]) elements.toArray(new Object[n][2]);
        classpathTableModel.fireTableRowsDeleted(elements.size(), n0 - 1);
        
        if (index >= n) {
            index = n - 1;
        }
        
        sm.setSelectionInterval(index, index);
        
        fireActionPerformed();
    }
    
    private void updateClassPathTableModel(String jar, String suName, 
            String suDesc, String compName) {
        for (int i = 0, size = classpathTableModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vcpi = 
                    (VisualClassPathItem) classpathTableModel.getValueAt(i, 0);
            
            String shortName = vcpi.getShortName();
            if (shortName.compareTo(jar) == 0 ||
                    // backward compatibility
                    shortName.endsWith(".jar") &&   // NOI18N
                    jar.endsWith("@SEDeployment.jar") &&    // NOI18N
                    shortName.substring(0, shortName.length() - 4).equals(
                    jar.substring(0, jar.length() - 17))) {
                vcpi.setAsaDescription(suDesc);
                vcpi.setAsaTarget(compName);
                
                classpathTableModel.setValueAt(compName, i, 1);
            }
        }
        
        // OK this is not a SE jar..
        for (VisualClassPathItem vi : bindingVisualClassPathItems) {
            if (vi.getAsaTarget().compareTo(compName) == 0) {
                vi.setAsaDescription(suDesc);                
                return;
            }
        }
    }
        
    // Load AsseemblyInfo.xml and update classpath table
    private void updateClassPathTable() {
        try {            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            
            Document doc = factory.newDocumentBuilder().parse(new File(assemblyInfoFileLoc));
            
            NodeList serviceUnitNodeList = doc.getElementsByTagName("service-unit"); // NOI18N
            String suName = null;
            String suDescription = null;
            String compName = null;
            String jar = null;
                        
            for (int i = 0, isize = serviceUnitNodeList.getLength(); i < isize; i++) {
                NodeList kids = serviceUnitNodeList.item(i).getChildNodes();
                
                for (int k = 0, ksize = kids.getLength(); k < ksize; k++) {
                    Node n = kids.item(k);
                    
                    if (n.getNodeName().equals("identification")) { // NOI18N
                        NodeList ids = n.getChildNodes();
                        
                        for (int j = 0, jsize = ids.getLength(); j < jsize; j++) {
                            Node m = ids.item(j);
                            
                            if (m.getNodeName().equals("name")) { // NOI18N
                                suName = m.getFirstChild().getNodeValue();
                            } else if (m.getNodeName().compareTo("description") == 0) { // NOI18N
                                suDescription = m.getFirstChild() == null ? "" : m.getFirstChild().getNodeValue();
                            }
                        }
                    } else if (n.getNodeName().equals("target")) { // NOI18N
                        NodeList ids = n.getChildNodes();
                        
                        for (int j = 0, jsize = ids.getLength(); j < jsize; j++) {
                            Node m = ids.item(j);
                            
                            if (m.getNodeName().equals("component-name")) { // NOI18N
                                compName = m.getFirstChild().getNodeValue();
                            } else if (m.getNodeName().equals("artifacts-zip")) { // NOI18N
                                jar = m.getFirstChild().getNodeValue();
                            }
                        }
                    }
                }
                
                if (jar != null) {
                    updateClassPathTableModel(jar, suName, suDescription, compName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  
    
    private void initClassPathTable() {
        
        this.classpathTableModel = new ClasspathTableModel();
        this.classpathTable.setModel(classpathTableModel);
        this.classpathTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        TableColumnModel columnModel = this.classpathTable.getColumnModel();
        TableColumn firstColumn = columnModel.getColumn(0);
        TableColumn secondColumn = columnModel.getColumn(1);
        firstColumn.setHeaderValue(NbBundle.getMessage(getClass(), "TXT_Archive_Item")); // NOI18N
        secondColumn.setHeaderValue(NbBundle.getMessage(getClass(), "TXT_Archive_PathInArchive")); // NOI18N
        firstColumn.setCellRenderer(new ClassPathCellRenderer());
    }    
    
    private void initComponentTable() {
        List<ComponentObject> componentObjects = new ArrayList<ComponentObject>();
        
        // setup the table model to use
        List<String> columnNames = new ArrayList<String>();
        columnNames.add(NbBundle.getMessage(getClass(), "Type"));  // NOI18N
        columnNames.add(NbBundle.getMessage(getClass(), "Component_ID"));  // NOI18N
        componentTableModel = new ComponentTableModel(componentObjects, columnNames);
        TableModel sortedComponentTableModel = new TableSorterUtil(componentTableModel);
        componentTable.setModel(sortedComponentTableModel);
                
        componentTable.setShowHorizontalLines(true);
        componentTable.setShowVerticalLines(false);
        componentTable.setShowGrid(false);
        componentTable.setAutoCreateColumnsFromModel(false);
        componentTable.setRowSelectionAllowed(true);
        componentTable.setColumnSelectionAllowed(false);
        componentTable.getTableHeader().setReorderingAllowed(false);
        componentTable.getTableHeader().setAlignmentY(JTable.LEFT_ALIGNMENT);
        componentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        TableColumnModel columnModel = componentTable.getColumnModel();
        columnModel.getColumn(COMPONENT_TYPE_COLUMN).setPreferredWidth(70);
        columnModel.getColumn(COMPONENT_NAME_COLUMN).setPreferredWidth(300);
    }
    
    /**
     * DOCUMENT ME!
     */
    public void initTableValues() {
        updateComponentTable();    
        updateClassPathTable();
    }
        
    /**
     * Update component table with components from ComponentInformation.xml.
     * Also rebuild componentNames and bindingVisualClassPathItems.
     */
    private void updateComponentTable() {
        
        File dst = new File(compInfoFileLoc);
        
        try {
            if (dst.exists()) {
                List compList = ComponentInformationParser.parse(dst);
                updateComponentTable(compList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
                
    /**
     * Update component table with components from the given list.
     * Also rebuild componentNames and bindingVisualClassPathItems.
     */            
    private void updateComponentTable(List<? extends JBIComponentInfo> compList) {
        
        List<ComponentObject> rowData = new ArrayList<ComponentObject>();
        
        componentNames.clear();
        bindingVisualClassPathItems.clear();
        
        for (JBIComponentInfo component : compList) {
            String type = component.getType();
            
            if (type.equals("sharedLibrary")) { // NOI18N 
                continue;
            }
            
            ComponentObject comp = new ComponentObject(
                    type, 
                    component.getState(),
                    component.getName(),
                    component.getDescription()); // update this when loading assembly info
            rowData.add(comp);
            
            componentNames.add(component.getName());
           
            if (type.equals("bindingComponent")) { // NOI18N ???
                VisualClassPathItem vi = new VisualClassPathItem(
                        bcJar, VisualClassPathItem.TYPE_ARTIFACT,
                        "BCDeployment.jar", null, // NOI18N
                        true);
                vi.setAsaTarget(component.getName());
                bindingVisualClassPathItems.add(vi);
            }
        }
        
        componentTableModel.setData(rowData);
    }
        
    private boolean isSelectedServer() {
        String instance = (String) projProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected = true;
        
        if ((instance == null) || !JbiManager.isAppServer(instance)) {
            String[] serverIDs = JbiManager.getAppServers();
            
            if (serverIDs.length < 1) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(
                        NbBundle.getMessage(JbiActionProvider.class,
                        "MSG_NoInstalledServerError"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }
            
            NoSelectedServerWarning panel = new NoSelectedServerWarning(serverIDs);
            
            Object[] options = new Object[] {
                DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(NoSelectedServerWarning.class, 
                    "CTL_NoSelectedServerWarning_Title"), // NOI18N
                    true, options, options[0], 
                    DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance();
                selected = instance != null;
                
                if (selected) {
                    projProperties.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    projProperties.store();
                }
            }
            
            dlg.dispose();
        }
        
        if ((instance == null) || (!selected)) {
            String msg = NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoSelectedServerError"); // NOI18N
            NotifyDescriptor d = 
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        } else if (!JbiManager.isRunningAppServer(instance)) {
            String msg = NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoRunningServerError"); // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        }
        
        return true;
    }
    
    private void fetchInfo() {
        try {
            RuntimeManagementServiceWrapper adminService = getAdministrationService();
            if (adminService != null) {
                adminService.clearJBIComponentStatusCache(JBIComponentType.SERVICE_ENGINE);
                adminService.clearJBIComponentStatusCache(JBIComponentType.BINDING_COMPONENT);
                List<JBIComponentInfo> compList = new ArrayList<JBIComponentInfo>();
                compList.addAll(adminService.listServiceEngines("server")); // NOI18N
                compList.addAll(adminService.listBindingComponents("server")); // NOI18N
                updateComponentTable(compList);
            }
        } catch (Exception e) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    private RuntimeManagementServiceWrapper getAdministrationService() 
        throws MalformedURLException, IOException, 
        MalformedObjectNameException, ManagementRemoteException {
        
        String serverInstance = (String) projProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
        
        if (serverInstance == null) {
            if (!isSelectedServer()) {
                return null;
            }
            serverInstance = (String) projProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
            
        } else if (!JbiManager.isRunningAppServer(serverInstance)) {
            String msg = NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoRunningServerError"); // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return null;
        }
        
        return AdministrationServiceHelper.getRuntimeManagementServiceWrapper(serverInstance);
    }
        
    private void updateProperties(JbiProjectProperties prop, 
            ClasspathTableModel classpathModel) {
        List<String> targetIDs = new ArrayList<String>();
        VisualClassPathItem vcpi = null;
        List<VisualClassPathItem> javaEEProjs = new ArrayList<VisualClassPathItem>();
        Object aa = null;
        
        for (int i = 0; i < classpathModel.getRowCount(); i++) {
            targetIDs.add((String) classpathModel.getValueAt(i, 1));
            
            vcpi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
            if (vcpi != null) {
                aa = vcpi.getObject();
                if ( (aa instanceof AntArtifact) && 
                        VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact) aa)){
                    javaEEProjs.add(vcpi);
                }
            }
        }
        
        prop.put(JbiProjectProperties.JBI_CONTENT_COMPONENT, targetIDs);
        prop.put(JbiProjectProperties.JBI_JAVAEE_JARS, javaEEProjs);
    }
    
    
    // -------------------- private inner classes ------------------------------
    
    private class ClasspathSupportListener
            implements ActionListener, ListSelectionListener, TableModelListener {
        
        //--------------------------- ActionListener  --------------------------
        /**
         * Handles button events
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            
            if (source == addProjectButton) {
                List<String> javaeeAntArtifactTypes = new ArrayList<String>();
                javaeeAntArtifactTypes.addAll(JbiProjectConstants.JAVA_EE_AA_TYPES);
                javaeeAntArtifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);                
                AntArtifact[] artifacts = AntArtifactChooser.showDialog(
                        javaeeAntArtifactTypes, project, null, null);
                
                if (artifacts != null) {
                    addArtifacts(artifacts);
                }
            } else if (source == removeProjectButton) {
                removeElements();
            } else if (source == updateComponentsButton) {
                RequestProcessor.getDefault().post(
                        new Runnable() {
                    public void run() {
                        fetchInfo();
                    }
                }
                );
            }
        }
        
        //--------------------------- ListSelectionListener --------------------
        /**
         * Handles changes in the selection
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel selectionModel = 
                    (DefaultListSelectionModel) classpathTable.getSelectionModel();
            int index = selectionModel.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean remove = index != -1;
            
            // and when the selection does not contain unremovable item
            if (remove) {
                VisualClassPathItem vcpi = 
                        (VisualClassPathItem) classpathTableModel.getValueAt(index, 0);
                
                if (!vcpi.canDelete()) {
                    remove = false;
                }
            }
            
            removeProjectButton.setEnabled(remove);
        }
        
        //--------------------------- TableModelListener -----------------------
        public void tableChanged(TableModelEvent e) {
            updateProperties(projProperties, classpathTableModel);
            
            if (e.getColumn() == 1) {
                fireActionPerformed();
            }
        }
    }
    
    private static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        
        public Component getTableCellRendererComponent(
                JTable table, Object value, 
                boolean isSelected, boolean hasFocus, 
                int row, int column) {
            
            assert value == null || value instanceof VisualClassPathItem;                 
            return super.getTableCellRendererComponent(
                    table,
                    (value == null) ? null : value.toString(), 
                    isSelected, false, row, column);
        }
    }
    
    private class ClasspathTableModel extends AbstractTableModel {
        
        public int getColumnCount() {
            return 2; 
        }
        
        public int getRowCount() {            
            return data == null ? 0 : data.length;
        }
        
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }
}
