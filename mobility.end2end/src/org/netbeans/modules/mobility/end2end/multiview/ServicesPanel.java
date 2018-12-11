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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * ServicesPanel.java
 *
 * Created on July 25, 2005, 10:28 AM
 */
package org.netbeans.modules.mobility.end2end.multiview;
import java.text.MessageFormat;
import java.util.HashSet;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.*;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.ui.treeview.MethodCheckedTreeBeanView;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
import org.netbeans.modules.mobility.end2end.ui.treeview.MultiStateCheckBox;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;


/**
 *
 */
public class ServicesPanel extends SectionInnerPanel implements ExplorerManager.Provider, PropertyChangeListener {
    
    private static final RequestProcessor RP = new RequestProcessor(ServicesPanel.class);

    private final String INVALID_OPERATION  = "TXT_InvalidOperation";//NOI18N
    private final String INVALID_OPERATION_NULL_PARAM = "TXT_InvalidOperationNoParam";//NOI18N
    
    private final javax.swing.JLabel servicesLabel;
    private final MethodCheckedTreeBeanView checkedTreeView;
    private final Node waitNode;
    private final transient E2EDataObject dataObject;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final RequestProcessor.Task repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateTree();
        }
    });
    private final RequestProcessor.Task changeTask = RP.create(new Runnable() {
        public void run() {
            selectionChanged();
        }
    });

    private Configuration configuration;
    boolean wsdl = false;
    private FileObject serverProjectFolder;
    
    private transient ExplorerManager manager;
    private transient Node rootNode;
        
    /** Creates new form ServicesPanel */
    public ServicesPanel(SectionView sectionView, final E2EDataObject dataObject) {
        super(sectionView);
        this.dataObject = dataObject;
        initComponents();
        servicesLabel = new javax.swing.JLabel();
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 5, 0);
        add(servicesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        if (dataObject != null){
            gridBagConstraints.insets = new java.awt.Insets( 5, 10, 5, 0 );
        } else {
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        }
        
        checkedTreeView = new MethodCheckedTreeBeanView();
        waitNode = checkedTreeView.getWaitNode();
        checkedTreeView.setPopupAllowed( false );
        checkedTreeView.setDefaultActionAllowed( false );
        checkedTreeView.setBorder( new LineBorder( Color.BLACK, 1, true ));
        checkedTreeView.setRootVisible(true);
        getExplorerManager().setRootContext(waitNode);
        add( checkedTreeView, gridBagConstraints );
        
        servicesLabel.setLabelFor( checkedTreeView );

        generateButton.setEnabled( false );

        getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_ServicesPanel" ));
        checkedTreeView.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_Services" ));
        
        if(dataObject == null || dataObject.isGenerating()) generateButton.setEnabled( false ); //for case we are generating
        if( dataObject == null ) generateButton.setVisible( false );
        checkedTreeView.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
               if (dataObject != null) ServicesPanel.this.dataObject.setModified(true);
               changeTask.schedule(100);
            }
        });
        if (dataObject != null) {
            setConfiguration(dataObject.getConfiguration());
            dataObject.addPropertyChangeListener(this);
        }
    }
    
    public void setConfiguration( final Configuration configuration ){
        this.configuration = configuration;
        if (configuration != null){
            wsdl = Configuration.WSDLCLASS_TYPE.equals( configuration.getServiceType());
            Mnemonics.setLocalizedText(servicesLabel, NbBundle.getMessage(ServicesPanel.class, wsdl? "LBL_Operations" : "LBL_Methods")); //NOI18N
            waitNode.setDisplayName(NbBundle.getMessage( ServicesPanel.class, wsdl ? "MSG_WaitComputingWebServices" : "MSG_WaitComputingMethods")); //NOI18N
            getExplorerManager().setRootContext( waitNode );
            repaintingTask.schedule(100);
        }
    }
    
    public Configuration getConfiguration(){
        return configuration;
    }
    
    public void removeNotify(){
        if (repaintingTask != null) repaintingTask.cancel();
        if (changeTask != null) changeTask.cancel();
        getExplorerManager().removePropertyChangeListener(this);
        if (dataObject != null) dataObject.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    /** Get the explorer manager.
     * @return the manager
     */
    public ExplorerManager getExplorerManager() {
        if (manager == null) manager = new ExplorerManager();
        return manager;
    }

    private void updateTree() {
        if( !wsdl ) {
            rootNode = ServiceNodeManager.getRootNode(configuration, checkedTreeView);
        } else {
            final Project serverProject = Util.getServerProject( configuration );
            final List<AbstractService> services = configuration.getServices();
            final WSDLService service = (WSDLService)services.get( 0 );

            final JAXWSClientView a = JAXWSClientView.getJAXWSClientView();
            rootNode = a.createJAXWSClientView( serverProject );
            for( Node nn : rootNode.getChildren().getNodes()) {
                if( nn.getName().equals( service.getName())){
                    rootNode = nn;
                    break;
                }
            }
            if (rootNode.getChildren().getNodesCount() == 0){
                repaintingTask.schedule( 500 );
                return;
            } else {
                final List<ClassData> ports = service.getData();
                HashSet<String> selectedIDs = new HashSet();
                List<org.netbeans.modules.mobility.end2end.classdata.ClassData> data = configuration.getServices().get(0).getData();
                if (data != null) for (org.netbeans.modules.mobility.end2end.classdata.ClassData cd : data) {
                    for (OperationData od : cd.getOperations()) {
                        StringBuffer sb = new StringBuffer(cd.getType());
                        sb.append('.').append(od.getMethodName());
                        for (TypeData td : od.getParameterTypes()) {
                            sb.append(',').append(td.getType());
                        }
                        selectedIDs.add(sb.toString());
                    }
                }
                FileObject generatedClientFO = serverProject.getProjectDirectory().getFileObject( "build/generated/wsimport/client/" );
                // Add all paths to the ClasspathInfo structure
                List<ClasspathInfo> classpaths = Collections.singletonList( ClasspathInfo.create( generatedClientFO ));
                // Get the registry for all available classes
                ClassDataRegistry registry = ClassDataRegistry.getRegistry( ClassDataRegistry.DEFAULT_PROFILE, classpaths );
                final PortData port = ( ports != null && ports.size() > 0 ) ? (PortData)ports.get( 0 ) : null; // Only one port allowed
                rootNode = new FilterNode(rootNode, new FilterNode.Children(rootNode) {
                    protected Node[] createNodes(Node serviceNode) {
                        return new Node[] { new FilterNode(serviceNode, new FilterNode.Children(serviceNode) {
                            String firstWSPortName = null;
                            protected Node[] createNodes(Node portNode) {
                                WSPort wsdlPort = portNode.getLookup().lookup( WSPort.class );
                                if (wsdlPort == null) return null;
                                if (firstWSPortName == null) firstWSPortName = wsdlPort.getName();
                                return portNode.getName().equals(port == null ? firstWSPortName : port.getName()) ? super.createNodes(portNode) : null;
                            }
                        })};
                    }
                });
                for( Node serviceNode : rootNode.getChildren().getNodes()) {
                    boolean serviceValid = false;
                    for( Node portNode : serviceNode.getChildren().getNodes()) {
                        boolean portValid = false;
                        WSPort wsdlPort = portNode.getLookup().lookup( WSPort.class );
                        org.netbeans.modules.mobility.e2e.classdata.ClassData cd = registry.getClassData( wsdlPort.getJavaName());
                        HashSet<String> methodIDs = new HashSet();
                        if (cd != null) {
                            for( org.netbeans.modules.mobility.e2e.classdata.MethodData md : cd.getMethods()) {
                                StringBuffer sb = new StringBuffer(md.getName());
                                for (MethodParameter mp : md.getParameters()) {
                                    sb.append(',').append(mp.getType().getFullyQualifiedName());
                                }
                                methodIDs.add(sb.toString());
                            }
                        }
                        for( Node operationNode : portNode.getChildren().getNodes()) {
                            WSOperation wsdlOperation = operationNode.getLookup().lookup( WSOperation.class );
                            boolean operationValid = false;
                            StringBuilder operationId = new StringBuilder();
                            StringBuilder parameters = new StringBuilder();
                            if (wsdlOperation != null) {
                                operationId.append(wsdlOperation.getJavaName());
                                for (WSParameter par : wsdlOperation.getParameters()) {
                                    String pt = par.getTypeName();
                                    int i = pt.indexOf('<'); //cutting off any generics from the ID
                                    operationId.append(',').append(i > 0 ? pt.substring(0, i) : pt);
                                    parameters.append( i > 0 ? pt.substring(0, i) : pt );
                                    parameters.append(", ");
                                }
                                operationValid = methodIDs.contains(operationId.toString());
                            }
                            operationNode.setValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE, operationValid);
                            if ( !operationValid ){
                                String message;
                                if ( parameters.length() >0 ){
                                    message = NbBundle.getMessage( ServicesPanel.class , 
                                            INVALID_OPERATION);
                                    String params = message.substring( 0 , 
                                            message.length() - 2);
                                    operationNode.setShortDescription(
                                            MessageFormat.format( message , 
                                                    wsdlOperation.getName(), params));
                                }
                                else {
                                    message = NbBundle.getMessage( ServicesPanel.class , 
                                            INVALID_OPERATION_NULL_PARAM);
                                    operationNode.setShortDescription(
                                            MessageFormat.format( message , 
                                                    wsdlOperation.getName()));
                                }
                            }
                            if (operationValid && cd != null) operationNode.setValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE, selectedIDs.contains(cd.getFullyQualifiedName()+'.'+operationId.toString()) ? MultiStateCheckBox.State.SELECTED : MultiStateCheckBox.State.UNSELECTED);
                            portValid = portValid || operationValid;
                        }
                        portNode.setValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE, portValid);
                        serviceValid = serviceValid || portValid;
                    }
                    serviceNode.setValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE, serviceValid);
                }  
                checkedTreeView.updateTreeNodeStates(null);
            }
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if( !wsdl && rootNode.getChildren().getNodesCount() == 0 ) {
                        waitNode.setDisplayName( NbBundle.getMessage( ServicesPanel.class, "MSG_NoMethodAvailable" ));
                        return;
                    } else {
                        checkedTreeView.setRootVisible(false);
                        checkedTreeView.setRoot(rootNode);
                        getExplorerManager().setRootContext(rootNode);
                        expandNodes(rootNode);
                    }
                    checkValid();
                }
            });
        } catch (Exception ite) {
            ErrorManager.getDefault().notify(ite);
        }
    }
    
    private void expandNodes(Node n) {
        if (Boolean.FALSE != n.getValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE)) checkedTreeView.expandNode(n);
        for (Node ch : n.getChildren().getNodes()) expandNodes(ch);
    }
    
    private void selectionChanged() {
            getSelectedMethods();
            checkValid();
            fireChange();
    }

    private void checkValid() {
        final AbstractService service = configuration.getServices().get(0);
        final SectionView sectionView = getSectionView();
        if (sectionView != null){
            if( service == null || ( service != null && service.getData().size() == 0 )) {
                sectionView.getErrorPanel().setError(
                        new Error( Error.TYPE_FATAL, Error.MISSING_VALUE_MESSAGE,
                        NbBundle.getMessage( ServicesPanel.class, "ERR_MissingServiceSelection" ), checkedTreeView ));
                generateButton.setEnabled( false );
            } else {
                sectionView.getErrorPanel().clearError();
                generateButton.setEnabled( true );
            }
        }

    }
    
    public void getSelectedMethods() {
//        todo should distinguish between WS and classes
        if (!wsdl) {
            final List<ClassData> classData = new ArrayList<ClassData>();
            final Node packageNodes[] = rootNode.getChildren().getNodes();
               for ( Node pkNode : packageNodes ) {
                    final Node classNodes[] = pkNode.getChildren().getNodes();
                    for ( Node clNode : classNodes ) {
                        final Node methodNodes[] = clNode.getChildren().getNodes();
                        final List<OperationData> methodData = new ArrayList<OperationData>();
                        for ( Node node : methodNodes ) {
                            if (MultiStateCheckBox.State.SELECTED == node.getValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE)) {
                                final org.netbeans.modules.mobility.e2e.classdata.MethodData mthData = (
                                        org.netbeans.modules.mobility.e2e.classdata.MethodData) node.getLookup().lookup(org.netbeans.modules.mobility.e2e.classdata.MethodData.class);
                                final List<org.netbeans.modules.mobility.e2e.classdata.MethodParameter> params = mthData.getParameters();
                                final List<TypeData> newParams = new ArrayList<TypeData>(params.size());
                                for ( final org.netbeans.modules.mobility.e2e.classdata.MethodParameter param : params ) {
                                    //have a list of parameters for each method
                                    final TypeData td = new TypeData( param.getName(), param.getType().getFullyQualifiedName());
                                    newParams.add( td );
                                }
                                final OperationData od = new OperationData( mthData.getName());
                                od.setReturnType( mthData.getReturnType().getFullyQualifiedName());
                                od.setParameterTypes( newParams );
                                methodData.add( od );
                            }
                        }
                        if (methodData.size() != 0){
                            final String classFQN = ((org.netbeans.modules.mobility.e2e.classdata.ClassData)
                                clNode.getLookup().lookup(org.netbeans.modules.mobility.e2e.classdata.ClassData.class)).getFullyQualifiedName();

                            final ClassData cd = new ClassData( classFQN );
                            cd.setOperations( methodData );
                            classData.add( cd );
                        }
                    }
                }
                final ClassService classService = (ClassService)configuration.getServices().get(0);
                classService.setData( classData );
                final List<AbstractService> services = new ArrayList<AbstractService>();
                services.add(classService);
                configuration.setServices( services );

        } else { //we are wsdl
            final List<AbstractService> servicesData = new ArrayList<AbstractService>();
            final Client client = rootNode.getLookup().lookup(Client.class);
            if (client != null) for(Node serviceNode : rootNode.getChildren().getNodes()) { //there is only one service node now!
                final List<ClassData> classData = new ArrayList<ClassData>();
                for(Node portNode : serviceNode.getChildren().getNodes()) {
                    final WSPort port = portNode.getLookup().lookup(WSPort.class);
                    final List<OperationData> methodData = new ArrayList<OperationData>();
                    if (port != null) for(Node operationNode : portNode.getChildren().getNodes()) {
                        final String operationName = operationNode.getName(); //name of the operation (selection)
                        if(MultiStateCheckBox.State.SELECTED == operationNode.getValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE)) {
                            WSOperation wsdlOp = operationNode.getLookup().lookup( WSOperation.class );
                            final OperationData md = new OperationData( operationName );
                            md.setMethodName( wsdlOp.getJavaName());
                            md.setReturnType( wsdlOp.getReturnTypeName());
                            List<? extends WSParameter> wsdlParams = wsdlOp.getParameters();
                            List<TypeData> params = new ArrayList<TypeData>();
                            for( WSParameter param : wsdlParams ) {
                                params.add( new TypeData( param.getName(), param.getTypeName()));
                            }
                            md.setParameterTypes( params );
                            if( md != null ) {
                                methodData.add( md );
                            }
                        }
                    }
                    if(port != null && methodData.size() > 0) { //class was found
                        final PortData pd = new PortData(port.getJavaName());
                        pd.setName( portNode.getName());
                        pd.setOperations( methodData );
                        classData.add( pd );
                    }
                }
                final WSDLService wsdlService = (WSDLService)configuration.getServices().get(0);
                wsdlService.setData( classData );
                wsdlService.setName( rootNode.getName());
                wsdlService.setType( serviceNode.getName());
                servicesData.add( wsdlService );
            }
            configuration.setServices( servicesData );
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        generateButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(500, 350));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(generateButton, org.openide.util.NbBundle.getBundle(ServicesPanel.class).getString("LBL_Generate")); // NOI18N
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(generateButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        dataObject.generate();
    }//GEN-LAST:event_generateButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton generateButton;
    // End of variables declaration//GEN-END:variables
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (E2EDataObject.PROP_GENERATING.equals(evt.getPropertyName())){
            generateButton.setEnabled(!(Boolean)evt.getNewValue());
        }
    }
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    
    public void setServerProjectFolder(final FileObject serverProjectFolder) {
        this.serverProjectFolder = serverProjectFolder;
    }

    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }
}
