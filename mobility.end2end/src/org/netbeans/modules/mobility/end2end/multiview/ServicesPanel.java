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

/*
 * ServicesPanel.java
 *
 * Created on July 25, 2005, 10:28 AM
 */
package org.netbeans.modules.mobility.end2end.multiview;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.*;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.ui.treeview.MethodCheckedTreeBeanView;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
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


/**
 *
 * @author  Michal Skvor, Bohemius
 */
public class ServicesPanel extends SectionInnerPanel implements ExplorerManager.Provider, PropertyChangeListener {
    
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

    private Configuration configuration;
    boolean wsdl = false;
    private FileObject serverProjectFolder;
    
    private transient ExplorerManager manager;
    private transient Node rootNode;
        
    /** Creates new form ServicesPanel */
    public ServicesPanel(SectionView sectionView, E2EDataObject dataObject) {
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

        getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_ServicesPanel" ));
        checkedTreeView.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ServicesPanel.class, "ACSD_Services" ));
        
        if( dataObject == null ){
            generateButton.setVisible(false); //not visible for wizard
        } else {
            if( dataObject.isGenerating()) generateButton.setEnabled( false ); //for case we are generating
            checkedTreeView.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                   ServicesPanel.this.dataObject.setModified(true);
                }
            });
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
        final Project serverProject = Util.getServerProject( configuration );
        if( !wsdl ) {
            rootNode = ServiceNodeManager.getRootNode( serverProject );
        } else {
            final List<AbstractService> services = configuration.getServices();
            final WSDLService service = (WSDLService)services.get( 0 );

            final JAXWSClientView a = JAXWSClientView.getJAXWSClientView();
            rootNode = a.createJAXWSClientView( serverProject );
            for( Node nn : rootNode.getChildren().getNodes()) {
                if( nn.getName().equals( service.getName()))
                    rootNode = nn;
                    break;
            }
            if (rootNode.getChildren().getNodesCount() == 0){
                repaintingTask.schedule( 500 );
                return;
            } else {
                final List<ClassData> ports = service.getData();
                FileObject generatedClientFO = serverProject.getProjectDirectory().getFileObject( "build/generated/wsimport/client/" );
                // Add all paths to the ClasspathInfo structure
                List<ClasspathInfo> classpaths = Collections.singletonList( ClasspathInfo.create( generatedClientFO ));
                // Get the registry for all available classes
                ClassDataRegistry registry = ClassDataRegistry.getRegistry( ClassDataRegistry.DEFAULT_PROFILE, classpaths );
                PortData port = null;
                if( ports != null && ports.size() > 0 ) port = (PortData)ports.get( 0 ); // Only one port allowed
                for( Node serviceNode : rootNode.getChildren().getNodes()) {
                    for( Node portNode : serviceNode.getChildren().getNodes()) {
                        WsdlPort wsdlPort = portNode.getLookup().lookup( WsdlPort.class );
                        if( port != null && !portNode.getName().equals( port.getName())) continue;
                        org.netbeans.modules.mobility.e2e.classdata.ClassData cd = registry.getClassData( wsdlPort.getJavaName());
                        for( Node operationNode : portNode.getChildren().getNodes()) {
                            WsdlOperation wsdlOperation = operationNode.getLookup().lookup( WsdlOperation.class );
                            org.netbeans.modules.mobility.e2e.classdata.MethodData methodData = null;
                            if( cd != null )
                            for( org.netbeans.modules.mobility.e2e.classdata.MethodData md : cd.getMethods()) {
                                if( md.getName().equals( wsdlOperation.getJavaName())) {
                                    methodData = md;
                                }
                            }
                            operationNode.setValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE, Boolean.valueOf(methodData != null));
                        }
                    }
                }                
            }
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    //remove listeners while selecting
                    getExplorerManager().removePropertyChangeListener( ServicesPanel.this );
                    if( !wsdl && rootNode.getChildren().getNodesCount() == 0 ) {
                        waitNode.setDisplayName( NbBundle.getMessage( ServicesPanel.class, "MSG_NoMethodAvailable" ));
                        return;
                    } else {
                        checkedTreeView.setRootVisible(false);
                        checkedTreeView.setRoot(rootNode);
                        getExplorerManager().setRootContext(rootNode);
                        setSelectedMethods();
                    }
                    //add back after selection
                    getExplorerManager().addPropertyChangeListener( ServicesPanel.this );
                }
            });
        } catch (Exception ite) {
            ErrorManager.getDefault().notify(ite);
        }
    }
    
    private void setSelectedMethods() {
        if( !wsdl ) {
            final AbstractService classService = configuration.getServices().get(0);
            final List<ClassData> classes = classService.getData();
            if (classes == null) return;
            final Error error = new Error( Error.TYPE_WARNING, Error.MISSING_VALUE_MESSAGE, NbBundle.getMessage( ServicesPanel.class, "ERR_ServiceMethodNotFound" ), this );
            for ( final ClassData classData : classes ) {
                final String className = classData.getClassName();
                final String pkgName = classData.getPackageName();
                final List<OperationData> methods = classData.getOperations();
                for ( final OperationData methodData : methods ) {
                    final String methodName = methodData.getName();
                    try {
                        checkedTreeView.setState(NodeOp.findPath(rootNode, new String[]{pkgName, className, methodName}), true);
                        checkedTreeView.expandNode(NodeOp.findPath(rootNode, new String[]{pkgName, className}));
                    } catch ( NodeNotFoundException ex) {
                        //System.err.println(" SETTING ERROR OUTPUT");
                        final SectionView sectionView = getSectionView();
                        if (sectionView != null){
                            sectionView.getErrorPanel().setError( error );
                        }
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        } else {            
            final WSDLService wsdlService = (WSDLService)configuration.getServices().get(0); //TODO we are assuming only one service
            final String serviceName = wsdlService.getName();
            final List<ClassData> ports = wsdlService.getData();
            final Error error = new Error( Error.TYPE_WARNING, Error.MISSING_VALUE_MESSAGE,
                                    NbBundle.getMessage( ServicesPanel.class, "ERR_ServiceMethodNotFound" ), this );
            //search for selections
            for( int i = 0; ports != null && i < ports.size(); i++ ) {
                final PortData portData = (PortData)ports.get( i );
                final String port = portData.getName();
                final List<OperationData> operations = portData.getOperations();
                for ( final OperationData operationData : operations ) {
                    final String operation = operationData.getName();
                    try {
                        final Node pkgNode = NodeOp.findPath( rootNode, new String[] { wsdlService.getType(), port, operation } );
                        checkedTreeView.setState( pkgNode, true );
                    } catch (Exception e){
                        final SectionView sectionView = getSectionView();
                        if( sectionView != null ) {
                            sectionView.getErrorPanel().setError( error );
                        }
                        ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
                    }
                }
            }
            checkedTreeView.expandAll();
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
                            if( checkedTreeView.getState( node ).equals( MethodCheckedTreeBeanView.SELECTED )) {
                                final org.netbeans.modules.mobility.e2e.classdata.MethodData mthData = (
                                        org.netbeans.modules.mobility.e2e.classdata.MethodData) node.getLookup().lookup(org.netbeans.modules.mobility.e2e.classdata.MethodData.class);
                                final List<org.netbeans.modules.mobility.e2e.classdata.MethodParameter> params = mthData.getParameters();
                                final List<TypeData> newParams = new ArrayList<TypeData>(params.size());
                                for ( final org.netbeans.modules.mobility.e2e.classdata.MethodParameter param : params ) {
                                    //have a list of parameters for each method
                                    final TypeData td = new TypeData( param.getName(), param.getType().getName());
                                    newParams.add( td );
                                }
                                final OperationData od = new OperationData( mthData.getName());
                                od.setReturnType( mthData.getReturnType().getName());
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
            final Node serviceNodes[] = rootNode.getChildren().getNodes();
            WSI serviceClassInfo = null;
                                
            for( int i = 0; i < serviceNodes.length; i++ ) { //there is only one service node now!
                final Node portNodes[] = serviceNodes[i].getChildren().getNodes();
                final List<ClassData> classData = new ArrayList<ClassData>();
                                
                for( int j = 0; j < portNodes.length; j++ ) {
                    final Node operationNodes[] = portNodes[j].getChildren().getNodes();
                    final List<OperationData> methodData = new ArrayList<OperationData>();
                    
                    for( int k = 0; k < operationNodes.length; k++ ) {
                        final String operationName = operationNodes[k].getName(); //name of the operation (selection)
                        
                        if( checkedTreeView.getState( operationNodes[k] ).equals( MethodCheckedTreeBeanView.SELECTED )) {
                            serviceClassInfo = computeMethodCall( operationNodes[ k ] );
                            if( serviceClassInfo == null ) {
                                continue;
                            }
                            WsdlOperation wsdlOp = operationNodes[k].getLookup().lookup( WsdlOperation.class );
                            final OperationData md = new OperationData( operationName );
                            md.setMethodName( wsdlOp.getJavaName());
                            md.setReturnType( wsdlOp.getReturnTypeName());
                            List<WsdlParameter> wsdlParams = wsdlOp.getParameters();
                            List<TypeData> params = new ArrayList<TypeData>();
                            for( WsdlParameter param : wsdlParams ) {
                                params.add( new TypeData( param.getName(), param.getTypeName()));
                            }
                            md.setParameterTypes( params );
                            if( md != null ) {
                                methodData.add( md );
                            }
                        }
                    }
                    if( serviceClassInfo != null && methodData.size() != 0 ) { //class was found
                        final PortData pd = new PortData( serviceClassInfo.getFqPortTypeName());
                        pd.setName( portNodes[j].getName());
                        pd.setOperations( methodData );
                        classData.add( pd );
                    }
                }
                final WSDLService wsdlService = (WSDLService)configuration.getServices().get(0);
                wsdlService.setData( classData );
                wsdlService.setName( rootNode.getName());
                wsdlService.setType( serviceNodes[i].getName());
                servicesData.add( wsdlService );
            }
            configuration.setServices( servicesData );
        }
    }
            
    private static WSI computeMethodCall( final Node serviceOperationNode ) {        
        try {
            final Node servicePortNode = serviceOperationNode.getParentNode();
            final Node serviceNode = servicePortNode.getParentNode();
            final Client client = serviceNode.getParentNode().getLookup().lookup( Client.class );
            final String servicePortName = client.getName();
            final String serviceName = client.getName();
            final String serviceClassName = client.getName(); //here is the class name //??
            String servicePackageName = client.getPackageName();
            String servicePortTypeName = servicePortName;
            if( servicePackageName == null ) {
                return null;
            }
            final String fqServiceClassName = servicePackageName + "." + serviceClassName; //NOI18N //MUST BE STORED FOR LATTER USAGE
            final String fqPortTypeName = servicePackageName + "." + servicePortTypeName; //NOI18N //MUST BE STORED FOR LATTER USAGE
            return new WSI( serviceName, fqServiceClassName, fqPortTypeName );
        } catch (NullPointerException npe) {
            ErrorManager.getDefault().notify(npe);
        }
        return null;
    }
        
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void setValue(JComponent source, Object value) {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
//        generateButton.setEnabled(false);
        dataObject.generate();
    }//GEN-LAST:event_generateButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton generateButton;
    // End of variables declaration//GEN-END:variables
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())){
            getSelectedMethods();
            System.err.println(" - selection changed");
            final AbstractService service = configuration.getServices().get(0);
            final SectionView sectionView = getSectionView();
            if (sectionView != null){
                if( service == null || ( service != null && service.getData().size() == 0 )) {
                    sectionView.getErrorPanel().setError(
                            new Error( Error.TYPE_FATAL, Error.MISSING_VALUE_MESSAGE,
                            NbBundle.getMessage( ServicesPanel.class, "ERR_MissingServiceSelection" ), checkedTreeView ));
//                    //dataObject.setSaveEnable( false );
                    generateButton.setEnabled( false );
                } else {
                    sectionView.getErrorPanel().clearError();
//                    //dataObject.setSaveEnable( true );
                    generateButton.setEnabled( true );
                }
            }
            fireChange();
        } else if (E2EDataObject.PROP_GENERATING.equals(evt.getPropertyName())){
            generateButton.setEnabled(!Boolean.TRUE.equals(evt.getNewValue()));
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
    
    private static class WSI {
        final private String serviceName;
        final private String fqServiceClassName;
        final private String fqPortTypeName;
        
        WSI(String serviceName, String fqServiceClassName, String fqPortTypeName) {
            this.serviceName = serviceName;
            this.fqServiceClassName = fqServiceClassName;
            this.fqPortTypeName = fqPortTypeName;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getFqServiceClassName() {
            return fqServiceClassName;
        }
        
        public String getFqPortTypeName() {
            return fqPortTypeName;
        }
    }
    
    public void setServerProjectFolder(final FileObject serverProjectFolder) {
        this.serverProjectFolder = serverProjectFolder;
    }
}
