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


package org.netbeans.modules.websvc.manager.ui;

import com.sun.tools.ws.processor.model.java.JavaArrayType;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.model.java.JavaType;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.util.*;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import org.netbeans.modules.websvc.manager.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.util.Util;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Dialog that tests JAX-WS client methods
 * 
 * @author  David Botterill
 */
public class TestWebServiceMethodDlg extends JPanel implements ActionListener, MethodTaskListener {
    private Dialog dialog;
    private DialogDescriptor dlg = null;
    private String okString = NbBundle.getMessage(this.getClass(), "CLOSE");
    private final JavaMethod method;
    /**
     * The runtimeClassLoader should be used when running the web service client.  This classloader
     * only includes the necessary runtime jars for JAX-RPC to run.  The classloader does NOT have a
     * parent to delegate to.  I did this because of Xerces classloader clashes with other netbeans
     * modules.
     * -David Botterill 4/21/2004
     */
    private URLClassLoader runtimeClassLoader;
    
    private String packageName;
    private DefaultMutableTreeNode parameterRootNode = new DefaultMutableTreeNode();
    private DefaultMutableTreeNode resultRootNode = new DefaultMutableTreeNode();
    private WebServiceData wsData;
    public String portName;
    private WsdlPort port;
    private MethodTask methodTask;
    
    /** Creates new form TestWebServiceMethodDlg */
    public TestWebServiceMethodDlg(WebServiceData inWSData,  JavaMethod inMethod, WsdlPort inPort) {
        this.method = inMethod;
        wsData = inWSData;
        port = inPort;
        packageName = inWSData.getPackageName();
        portName = inPort.getName();
        
        assert wsData.getJaxWsDescriptor() != null;
        
        initComponents();
        myInitComponents();
        
        this.lblTitle.setText(NbBundle.getMessage(this.getClass(), "TEST_WEBSVC_LABEL") + " " + method.getName());
    }
    
    
    /**
     * This method returns the classloader of the Jar file containing the web service for which we are testing the methods.
     * This class loader should be used for the runtime environment when invoking a web service.
     * TODO: determine if the tree components should get the class loader here, store the classloader in the tree nodes, or pass
     * to the tree component constructors.
     *@returns URLClassLoader - the class loader of the Jar file for the web service with the methods to test.
     */
    private URLClassLoader getRuntimeClassLoader() {
        if(null == runtimeClassLoader) {
            
            /**
             * First add the URL to the jar file for this web service.
             */
            try {
                List<URL> urlList = null;
                
                // XXX Only include jax-ws jars if running on JDK 1.5 since JAX-WS 2.0
                // is included with JDK 6+
                if (System.getProperty("java.version").contains("1.5.0")) { // NOI18N
                    urlList = Util.buildClasspath(null, "libs.jaxws21.classpath"); // NOI18N
                }else {
                    urlList = Util.buildClasspath(null);
                }
                
                WebServiceDescriptor descriptor = wsData.getJaxWsDescriptor();
                for (WebServiceDescriptor.JarEntry entry : descriptor.getJars()) {
                    if (entry.getType().equals(WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE)) {
                        File jarFile = new File(descriptor.getXmlDescriptorFile().getParent(), entry.getName());
                        urlList.add(jarFile.toURI().toURL());
                    }
                }
                
                URL [] urls = (URL [])urlList.toArray(new URL[0]);
                /**
                 * Make sure we don't have a parent to delegate to.  I first experienced Xerces classloader
                 * clashes with the currentLoader as the parent.
                 */
                
                runtimeClassLoader = new URLClassLoader(urls, null);
            } catch(IOException mfu) {
                ErrorManager.getDefault().notify(mfu);
                ErrorManager.getDefault().log(this.getClass().getName() + ":IOException=" + mfu);
                return null;
            }            
        }
        
        return runtimeClassLoader;
    }
    /**
     * This method returns the package name of the  web service for which we are testing the methods.
     * TODO: determine if the tree components should get the class loader here, store the classloader in the tree nodes, or pass
     * to the tree component constructors.
     *@returns URLClassLoader - the class loader of the Jar file for the web service with the methods to test.
     */
    public String getPackageName() {
        return packageName;
    }
    
    public WebServiceData getWebServiceData() {
        return this.wsData;
    }
    
    public void displayDialog(){
        
        dlg = new DialogDescriptor(this, NbBundle.getMessage(this.getClass(), "TEST_WEB_SERVICE_METHOD"),
                false, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), this);
        dlg.setOptions(new Object[] { okButton });
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        /**
         * After the window is opened, set the focus to the Get information button.
         */
        
        final JPanel thisPanel = this;
        dialog.addWindowListener( new WindowAdapter(){
            public void windowOpened( WindowEvent e ){
                SwingUtilities.invokeLater(
                        new Runnable() {
                    public void run() {
                        btnSubmit.requestFocus();
                        thisPanel.getRootPane().setDefaultButton(btnSubmit);
                    }
                });
            }
        });
        
        /**
         * Fix for Bug: 6217545
         * Need to know what the normal cursor is so we can reset it when
         * the dialog is closed.
         * - David Botterill 1/14/2005
         *
         */
        normalCursor=dialog.getCursor();
        /**
         * Fix for Bug: 6217545
         * Set the MouseListener for the OK button to a special adapter that will
         * make the cursor look normal ALWAYS when over the OK button.
         * - David Botterill 1/14/2005
         */
        BusyMouseAdapter mouseAdapter = new BusyMouseAdapter(normalCursor);
        okButton.addMouseListener(mouseAdapter);
        
        
        dialog.show();
    }
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_test_websvcdb");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        pnlParameter = new javax.swing.JPanel();
        pnlLabel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblParameters = new javax.swing.JLabel();
        scrollPaneParameter = new javax.swing.JScrollPane();
        btnPanel = new javax.swing.JPanel();
        btnSubmit = new javax.swing.JButton();
        pnlResults = new javax.swing.JPanel();
        lblResults = new javax.swing.JLabel();
        scrollPaneResults = new javax.swing.JScrollPane();

        setPreferredSize(new java.awt.Dimension(600, 450));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        pnlParameter.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 5, 12));
        pnlParameter.setLayout(new java.awt.BorderLayout());

        pnlLabel.setLayout(new java.awt.GridLayout(2, 0));

        lblTitle.setFont(lblTitle.getFont().deriveFont(lblTitle.getFont().getStyle() | java.awt.Font.BOLD, lblTitle.getFont().getSize()-2));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlLabel.add(lblTitle);
        lblTitle.getAccessibleContext().setAccessibleName(null);
        lblTitle.getAccessibleContext().setAccessibleDescription(null);

        lblParameters.setFont(lblParameters.getFont().deriveFont(lblParameters.getFont().getSize()-4f));
        lblParameters.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblParameters.setText(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TEST_WEBSVC_INSTRUCTIONS")); // NOI18N
        pnlLabel.add(lblParameters);
        lblParameters.getAccessibleContext().setAccessibleName(null);
        lblParameters.getAccessibleContext().setAccessibleDescription(null);

        pnlParameter.add(pnlLabel, java.awt.BorderLayout.NORTH);
        pnlLabel.getAccessibleContext().setAccessibleName(null);
        pnlLabel.getAccessibleContext().setAccessibleDescription(null);

        pnlParameter.add(scrollPaneParameter, java.awt.BorderLayout.CENTER);
        scrollPaneParameter.getAccessibleContext().setAccessibleName(null);
        scrollPaneParameter.getAccessibleContext().setAccessibleDescription(null);

        btnSubmit.setMnemonic(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.btnSubmit.ACC_mnemonic").charAt(0));
        btnSubmit.setText(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "BUTTON_SUBMIT")); // NOI18N
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });
        btnPanel.add(btnSubmit);
        btnSubmit.getAccessibleContext().setAccessibleName(null);
        btnSubmit.getAccessibleContext().setAccessibleDescription(null);

        pnlParameter.add(btnPanel, java.awt.BorderLayout.SOUTH);
        btnPanel.getAccessibleContext().setAccessibleName(null);
        btnPanel.getAccessibleContext().setAccessibleDescription(null);

        jSplitPane1.setLeftComponent(pnlParameter);
        pnlParameter.getAccessibleContext().setAccessibleName(null);
        pnlParameter.getAccessibleContext().setAccessibleDescription(null);

        pnlResults.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 12, 5, 12));
        pnlResults.setLayout(new java.awt.BorderLayout(0, 5));

        lblResults.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        pnlResults.add(lblResults, java.awt.BorderLayout.NORTH);
        lblResults.getAccessibleContext().setAccessibleName(null);
        lblResults.getAccessibleContext().setAccessibleDescription(null);

        pnlResults.add(scrollPaneResults, java.awt.BorderLayout.CENTER);
        scrollPaneResults.getAccessibleContext().setAccessibleName(null);
        scrollPaneResults.getAccessibleContext().setAccessibleDescription(null);

        jSplitPane1.setRightComponent(pnlResults);
        pnlResults.getAccessibleContext().setAccessibleName(null);
        pnlResults.getAccessibleContext().setAccessibleDescription(null);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jSplitPane1.getAccessibleContext().setAccessibleName(null);
        jSplitPane1.getAccessibleContext().setAccessibleDescription(null);

        getAccessibleContext().setAccessibleName(null);
        getAccessibleContext().setAccessibleDescription(null);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        invokeMethod();
    }//GEN-LAST:event_btnSubmitActionPerformed
    private void invokeMethod() {
        /**
         *  Steps to call the method.
         *  1. Get the parameter values from the tree
         *  2. Get the client wrapper class
         *  3. Get the method.
         *  4. call the Method with the parameter values
         *  5. Display the return value.
         */
        
        /**
         * Get the parameter values from the tree.  The parameters will be the children of the root node only. Any children
         * of the parameter nodes are values used to derive the parameter values.  This means only the first children of the root
         * node will be used a parameters.  The logic to "roll-up" a parameter value is left to the TypeCellEditor class.
         */
        
        /**
         * Use a LinkedList because we care about the order of the parameters.
         */
        LinkedList paramList = new LinkedList();
        for(int ii=0; null != this.getParamterRootNode() && ii < this.getParamterRootNode().getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) this.getParamterRootNode().getChildAt(ii);
            TypeNodeData nodeData = (TypeNodeData)childNode.getUserObject();
            Object parameterValue = nodeData.getParameterValue();
            JavaType parameterType = nodeData.getNodeType();
            
            if(parameterValue instanceof ArrayList) {
                try {
                    parameterValue = ReflectionHelper.getTypedParameterArray((ArrayList)parameterValue, nodeData.getParameterType(),
                            this.getRuntimeClassLoader(),wsData.getPackageName());
                } catch(WebServiceReflectionException wsre) {
                    Throwable cause = wsre.getCause();
                    ErrorManager.getDefault().notify(cause);
                    ErrorManager.getDefault().log(this.getClass().getName() +
                            ": Error trying to create a typed parameter array for type:" + nodeData.getParameterType()  +
                            "WebServiceReflectionException=" + cause);
                    return;
                }
            }
            paramList.add(parameterValue);
        }
        
        /**
         * specify the wrapper client class name for this method.
         */
        String className = port.getName() + "Client";
        String packageName = wsData.getPackageName();
        String clientClassName = packageName + "." + className;
        
        /**
         * Fix for Bug: 6217545
         * We need to run the method in a separate thread so the user can cancel if the method call
         * locks up.
         * First we need to create the thread, then register for a listener so we can get notified when the method's
         * finished.
         * -David Botterill 1/14/2005
         */
        methodTask = new MethodTask(clientClassName,paramList,this.method,this.getRuntimeClassLoader());
        
        methodTask.registerListener(this);
        
        Thread methodThread = new Thread(methodTask);
        
        methodThread.start();
    }
    
    public void methodFinished(Object inReturnedObject,LinkedList inParamList) {
        dialog.setCursor(normalCursor);
        
        showResults(inReturnedObject);
        
        /**
         * Fix for Bug#: 5059732
         * Now we need to also set the parameter values in the tree nodes since they may have changed due
         * to the support for pass by reference ("Holders").
         * - David Botterill 8/12/2004
         */
        
        for(int ii=0; null != this.getParamterRootNode() && ii < this.getParamterRootNode().getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) this.getParamterRootNode().getChildAt(ii);
            TypeNodeData nodeData = (TypeNodeData)childNode.getUserObject();
            nodeData.setParameterValue(inParamList.get(ii));
            /**
             * We really only care about Holder types from here since they are the only type of parameter that
             * can have the value changed by the endpoint service.
             */
            JavaType topNodeType = nodeData.getNodeType();
            if(topNodeType.isHolder()) {
                ((ParameterTreeNode)childNode).updateChildren();
            }
        }
        /**
         * Update the table since we may have changed some tree node values.
         */
        parameterOutline.tableChanged(new TableModelEvent((TableModel)parameterOutline.getOutlineModel().getRowNodeModel()));
        
    }
    
    private void showResults(Object inResultObject) {
        /**
         * Create a tree of the result object types.
         */
        
        resultOutline = loadResultTreeTable(this.method, inResultObject);
        resultOutline.getTableHeader().setReorderingAllowed( false );
        resultOutline.getAccessibleContext().setAccessibleName(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.resultOutline.ACC_name"));
        resultOutline.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.resultOutline.ACC_desc"));
        addFocusListener(resultOutline);
        
        lblResults.setLabelFor( resultOutline );
        
        scrollPaneResults.setViewportView(resultOutline);
    }
    
    
    private void myInitComponents() {
        okButton.setText(okString);
        
        /**
         * Now set up the Nodes for the TreeTableView
         */
        if(null == this.method) {
            return;
        }
        
        parameterOutline = loadParameterTreeTable(this.method);
        
        // Turn off the reordering
        
        /**
         * Add it to the correct Panel.
         */
        
        scrollPaneParameter.setViewportView(parameterOutline);
        
        /**
         * Set up Accessibility stuff for not UI-Editor stuff.
         *
         */
        
        okButton.getAccessibleContext().setAccessibleName(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.okButton.ACC_name"));
        okButton.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.okButton.ACC_desc"));
        okButton.setMnemonic(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.okButton.ACC_mnemonic").charAt(0));
        
        parameterOutline.getAccessibleContext().setAccessibleName(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.parameterOutline.ACC_name"));
        parameterOutline.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.parameterOutline.ACC_desc"));
        lblParameters.setLabelFor( parameterOutline );
        addFocusListener(parameterOutline);
    }
    
    private void addFocusListener(final JTable table) {
        // fixes tab cycle when the table is empty
        table.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent evt) {
                Container cycleRoot = table.getFocusCycleRootAncestor();
                FocusTraversalPolicy policy = table.getFocusTraversalPolicy();
                if (policy == null && cycleRoot != null) {
                    policy = cycleRoot.getFocusTraversalPolicy();
                }

                if (table.getRowCount() == 0 && policy != null) {
                    Component target = policy.getComponentAfter(cycleRoot, table);
                    if (target != null && target == evt.getOppositeComponent()) {
                        target = policy.getComponentBefore(cycleRoot, table);
                    }

                    if (target != null) {
                        target.requestFocusInWindow();
                    }
                }
            }

            public void focusLost(FocusEvent evt) {
            }
        });
    }
    
    private DefaultMutableTreeNode getParamterRootNode() {
        return parameterRootNode;
    }
    
    private void setParameterRootNode(DefaultMutableTreeNode inNode) {
        parameterRootNode = inNode;
    }
    private DefaultMutableTreeNode getResultRootNode() {
        return resultRootNode;
    }
    
    private void setResultRootNode(DefaultMutableTreeNode inNode) {
        resultRootNode = inNode;
    }
    
    private Outline loadResultTreeTable(JavaMethod inMethod, Object inResultObject) {
        if(null == inMethod) {
            return null;
        }
        JavaType currentType = inMethod.getReturnType();
        
        
        ResultNodeData data = new ResultNodeData(currentType,inResultObject);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
        
        if(currentType instanceof JavaArrayType) {
            /**
             * Create the instances of the array.
             */
            
            addResultArrayInstances(node);
        } else if(currentType instanceof JavaStructureType) {
            
            /**
             * If this is a JavaStructureType, we need to traverse the types until we have either all JavaSimpleType or
             * JavaEnumerationType.
             */
            traverseResultType(node);
        }
        /**
         * Make sure to create a new result root each time since the user can change the parameters and submit many
         * times.
         */
        this.setResultRootNode(new DefaultMutableTreeNode());
        /**
         *  Add it to the root.
         */
        this.getResultRootNode().add(node);
        
        DefaultTreeModel treeModel = new DefaultTreeModel(this.getResultRootNode());
        ResultRowModel rowModel = new ResultRowModel();
        OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(treeModel,rowModel, false);
        outlineModel.setNodeColumnName(NbBundle.getMessage(this.getClass(), "TYPE_COLUMN_NAME"));
        Outline returnOutline = new Outline(outlineModel);
        ResultCellEditor cellEditor = new ResultCellEditor();
        returnOutline.setDefaultEditor(Object.class,cellEditor);
        returnOutline.setRootVisible(false);
        
        returnOutline.setRenderDataProvider(new TypeDataProvider());
        
        return returnOutline;
    }
    
    private Outline loadParameterTreeTable(JavaMethod inMethod) {
        if(null == inMethod) {
            return null;
        }
        
        List<JavaParameter> parameters = inMethod.getParametersList();
        for (JavaParameter currentParameter : parameters) {
            /**
             * Add all Parameter's to the root tree node.
             */
            JavaType currentType = currentParameter.getType();
            Object value = null;
            /**
             * First get create the Node to be added.
             */
            String holdingType = null;
            if(currentParameter.isHolder()) {
                /**
                 * First we need to know what type of Holding value we have.
                 */
                
                holdingType = Util.getParameterType(currentParameter);
                if(null == holdingType) {
                    continue;
                }
                
                value = getParameterDefaultValue(currentType);
            } else {
                value = getParameterDefaultValue(currentType);
            }
            
            TypeNodeData data = new TypeNodeData(currentType,currentParameter.getName(),value);
            DefaultMutableTreeNode node = null;
            
            /**
             * Now create the appropriate node to be added.
             */
            if(currentParameter.isHolder()) {
                node = new HolderTypeTreeNode(data,this.getRuntimeClassLoader(),this.getPackageName());
                /**
                 * Now add the specific type node to this node.
                 */
                DefaultMutableTreeNode childNode = null;
                /**
                 * We need to account for ByteArrayHolders and All other Holder types separately.
                 */
                if(currentParameter.getType() instanceof JavaArrayType) {
                    TypeNodeData heldData = new TypeNodeData(currentParameter.getType(),"",this.getParameterDefaultValue(currentParameter.getType()));
                    childNode = new ArrayTypeTreeNode(heldData,this.getRuntimeClassLoader(),this.getPackageName());
                    addParameterArrayInstances(currentParameter.getType(),childNode);
                } else if(currentParameter.getType() instanceof JavaStructureType)  {
                    JavaType type = currentParameter.getType();
                    TypeNodeData heldData = new TypeNodeData(currentParameter.getType(),"",this.getParameterDefaultValue(type));
                    childNode = new StructureTypeTreeNode(heldData,this.getRuntimeClassLoader(),this.getPackageName());
                    traverseType(childNode);
                } else {
                    TypeNodeData heldData = new TypeNodeData(currentParameter.getType(),"",this.getParameterDefaultValue(currentParameter.getType()));
                    childNode = new DefaultMutableTreeNode(heldData);
                }
                
                node.add(childNode);
                
            } else if(currentType instanceof JavaArrayType) {
                node = new ArrayTypeTreeNode(data,this.getRuntimeClassLoader(),this.getPackageName());
                /**
                 * Create some instances of the array.
                 */
                
                JavaType elementType = ((JavaArrayType)currentType).getElementType();
                addParameterArrayInstances(elementType,node);
            } else if(currentType instanceof JavaStructureType) {
                node = new StructureTypeTreeNode(data,this.getRuntimeClassLoader(),this.getPackageName());
                
                /**
                 * If this is a JavaStructureType, we need to traverse the types until we have either all JavaSimpleType or
                 * JavaEnumerationType.
                 */
                traverseType(node);
            } else {
                node = new DefaultMutableTreeNode(data);
            }
            /**
             *  Add it to the root.
             */
            this.getParamterRootNode().add(node);
            
        }
        
        DefaultTreeModel treeModel = new DefaultTreeModel(this.getParamterRootNode());
        rowModel = new TypeRowModel(this.getRuntimeClassLoader(),this.getPackageName());
        OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(treeModel,rowModel, false);
        outlineModel.setNodeColumnName(NbBundle.getMessage(this.getClass(), "TYPE_COLUMN_NAME"));
        Outline returnOutline = new Outline(outlineModel);
        TypeCellEditor cellEditor = new TypeCellEditor();
        returnOutline.setDefaultEditor(Object.class,cellEditor);
        returnOutline.setRootVisible(false);
        returnOutline.setRenderDataProvider(new TypeDataProvider());
        /**
         * Fix Bug 5052705.  This setting will cause the cells values to take affect when
         * the focus is lost.  This will remove the requirement of hitting "ENTER" after
         * entering a value in a cell to get the value to take affect.
         */
        returnOutline.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        
        return returnOutline;
    }
    
    private void traverseType(DefaultMutableTreeNode inNode) {
        
        if(null == inNode) {
            return;
        }
        
        JavaType inType = ((NodeData)inNode.getUserObject()).getNodeType();
        /**
         * We should only be traversing JavaStructureTypes but let's make sure.
         */
        if(!(inType instanceof JavaStructureType)) {
            return;
        }
        JavaStructureType type = (JavaStructureType)inType;
        Iterator typeIterator = type.getMembers();
        while(null != typeIterator && typeIterator.hasNext()) {
            JavaStructureMember entry = (JavaStructureMember)typeIterator.next();
            JavaType entryType = entry.getType();
            if(entryType instanceof JavaSimpleType) {
                /**
                 * add this node to the input node, the parameter name is null since this is somewhere
                 * in a JavaStructureType hierarchy.
                 */
                TypeNodeData data = new TypeNodeData(entryType,entry.getName(),getParameterDefaultValue(entryType));
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
                inNode.add(node);
            } else if(entryType instanceof JavaArrayType) {
                /**
                 * add this node to the input node, the parameter name is null since this is somewhere
                 * in a JavaStructureType hierarchy.
                 */
                TypeNodeData data = new TypeNodeData(entryType,entry.getName(),getParameterDefaultValue(entryType));
                ArrayTypeTreeNode node = new ArrayTypeTreeNode(data,this.getRuntimeClassLoader(),this.getPackageName());
                inNode.add(node);
                JavaType elementType = ((JavaArrayType)entryType).getElementType();
                addParameterArrayInstances(elementType,node);
            } else if(entryType instanceof JavaStructureType) {
                /**
                 * add this node to the input node, the parameter name is null since this is somewhere
                 * in a JavaStructureType hierarchy.
                 */
                TypeNodeData data = new TypeNodeData(entryType,entry.getName(),getParameterDefaultValue(entryType));
                StructureTypeTreeNode node = new StructureTypeTreeNode(data,this.getRuntimeClassLoader(),this.getPackageName());
                inNode.add(node);
                /**
                 * Now traverse this new JavaStructureType node.
                 */
                traverseType(node);
            }
        }
        
    }
    
    private void traverseResultType(DefaultMutableTreeNode inParentNode) {
        
        if(null == inParentNode) {
            return;
        }
        
        ResultNodeData parentData = (ResultNodeData)inParentNode.getUserObject();
        JavaType parentType = parentData.getResultType();
        Object parentValue = parentData.getResultValue();
        /**
         * We should only be traversing JavaStructureTypes but let's make sure.
         */
        if(!(parentType instanceof JavaStructureType)) {
            return;
        }
        JavaStructureType type = (JavaStructureType)parentType;
        Iterator typeIterator = type.getMembers();
        while(null != typeIterator && typeIterator.hasNext()) {
            JavaStructureMember member = (JavaStructureMember)typeIterator.next();
            JavaType memberType = member.getType();
            String memberName = member.getName();
            Object subTypeValue = null;
            /**
             * If the parentValue is null, we know the subtype value will also be null so don't
             * try to get the value.  However, we still want to show the structure subtypes so we'll
             * keep traversing.
             */
            if(null != parentValue) {
                
                try {
                    subTypeValue = ReflectionHelper.getStructureValue((NodeData)parentData,member,this.getRuntimeClassLoader(),
                            this.getPackageName());
                } catch(WebServiceReflectionException wsfe) {
                    
                }
            }
            ResultNodeData data = new ResultNodeData(memberType,subTypeValue);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
            inParentNode.add(node);
            if(memberType instanceof JavaArrayType) {
                addResultArrayInstances(node);
            } else if(memberType instanceof JavaStructureType) {
                /**
                 * Now traverse the JavaStructureType node.
                 */
                traverseResultType(node);
            }
        }
    }
    
    private void addParameterArrayInstances(JavaType inType, DefaultMutableTreeNode parentNode) {
        /**
         * Now add some instances of the array.
         * TODO: figure out some better way to pick the number of instances of the array to create
         */
        DefaultMutableTreeNode childNode = null;
        TypeNodeData data = null;
        for(int ii=0; ii < 9; ii++) {
            data = new TypeNodeData(inType,"[" + ii + "]",getParameterDefaultValue(inType));
            childNode = new DefaultMutableTreeNode(data);
            parentNode.add(childNode);
            
            /**
             * If these entries are of type JavaStructureType, traverse through them.
             */
            if(inType instanceof JavaStructureType) {
                this.traverseType(childNode);
            }
        }
    }
    
    private void addResultArrayInstances(DefaultMutableTreeNode parentNode) {
        /**
         * The result value is an array of a certain type that needs to be shown.
         *  1. first get the type of the array.
         *  2. create a node for each occurance of the array.
         *
         */
        DefaultMutableTreeNode childNode = null;
        
        ResultNodeData parentData = (ResultNodeData)parentNode.getUserObject();
        JavaType parentType = parentData.getResultType();
        if(!(parentType instanceof JavaArrayType)) {
            return;
        }
        /**
         * get the value type
         */
        JavaType valueType = ((JavaArrayType)parentType).getElementType();
        
        if(null == parentData.getResultValue()) return;
        List valueList = Arrays.asList((Object [])parentData.getResultValue());
        Iterator valueIterator = valueList.iterator();
        ResultNodeData childData = null;
        while(valueIterator.hasNext()) {
            childData = new ResultNodeData(valueType,valueIterator.next());
            childNode = new DefaultMutableTreeNode(childData);
            parentNode.add(childNode);
            /**
             * If these array elements are of type JavaStructureType, we must traverse them
             * as well.
             */
            if(valueType instanceof JavaStructureType) {
                this.traverseResultType(childNode);
            }
        }
    }
    
    /**
     * This method will create a default value for based on the JavaType of the parameter.  If the
     * Type is JavaStructureType, there will be no default value.
     */
    private Object getParameterDefaultValue(JavaType type) {
        
        Object value = null;
        
        if(null == type) return null;
        
        /**
         * If the type is JavaStructureType, instantiate one if it's types using reflection and
         * the default constructor.
         */
        if(type instanceof JavaStructureType) {
            Object returnValue = null;
            try {
                returnValue = ReflectionHelper.makeStructureType((JavaStructureType)type,
                        this.getRuntimeClassLoader(),this.getPackageName());
            } catch(WebServiceReflectionException wsre) {
                Throwable cause = wsre.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() +
                        ": Error trying to do Class.forName on: " + packageName + "." + type.getFormalName() + "WebServiceReflectionException=" + cause);
                return null;
            }
            
            return returnValue;
        }
        /**
         *  If we have an Array Type, create an ArrayList
         */
        else if(type instanceof JavaArrayType) {
            return new ArrayList();
        }
        /**
         * Must be a JavaSimpleType
         */
        else {
            
            String currentType = type.getRealName();
            
            
            if(currentType.equals(int.class.getName()) ||
                    currentType.equals(Integer.class.getName())) {
                value = new Integer(0);
            } else if(currentType.equals(byte.class.getName()) ||
                    currentType.equals(Byte.class.getName())) {
                value  = new Byte("0");
            } else if(currentType.equals(boolean.class.getName()) ||
                    currentType.equals(Boolean.class.getName())) {
                value = new Boolean(false);
            } else if(currentType.equals(float.class.getName()) ||
                    currentType.equals(Float.class.getName())) {
                value = new Float(0);
            } else if(currentType.equals(double.class.getName()) ||
                    currentType.equals(Double.class.getName())) {
                value = new Double(0);
            } else if(currentType.equals(long.class.getName()) ||
                    currentType.equals(Long.class.getName())) {
                value = new Long(0L);
            } else if(currentType.equals(short.class.getName()) ||
                    currentType.equals(Short.class.getName())) {
                value  = new Short("0");
            } else if(currentType.equals(String.class.getName())) {
                value = "";
            } else if(currentType.equals(BigDecimal.class.getName())) {
                value = new BigDecimal("0");
            } else if(currentType.equals(BigInteger.class.getName())) {
                value = new BigInteger("0");
            } else if(currentType.equals(URI.class.getName())) {
                try {
                    value = new URI("http://java.sun.com");
                } catch(URISyntaxException uri) {
                    
                }
            } else if(currentType.equals(Calendar.class.getName())) {
                value = Calendar.getInstance();
            } else if(currentType.equalsIgnoreCase(Date.class.getName())) {
                value = new Date();
            } 
            /* Revisit this
             else {
                // Otherwise instantiate if the class has a 0 parameter constructor
                try {
                    Class valueClass = Class.forName(currentType, true, getRuntimeClassLoader());
                    value = valueClass.newInstance();
                }catch (Exception ex) {
                }
            }*/
            
            return value;
        }
    }
    
    public void actionPerformed(ActionEvent evt) {
        String actionCommand = evt.getActionCommand();
        if(actionCommand.equalsIgnoreCase(okString)) {
            okButtonAction(evt);
        }
    }
    
    private void okButtonAction(ActionEvent evt) {
        /**
         * If the MethodTask is not null, the MethodTask
         * thread may still be running so we need to tell
         * it we've cancelled.
         */
        if(null != methodTask) {
            methodTask.cancel();
        }
        dialog.setCursor(normalCursor);
        dialog.dispose();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblParameters;
    private javax.swing.JLabel lblResults;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlLabel;
    private javax.swing.JPanel pnlParameter;
    private javax.swing.JPanel pnlResults;
    private javax.swing.JScrollPane scrollPaneParameter;
    private javax.swing.JScrollPane scrollPaneResults;
    // End of variables declaration//GEN-END:variables
    
    private JButton okButton = new JButton();
    private Outline parameterOutline;
    private Outline resultOutline;
    private TypeRowModel rowModel;
    private Cursor normalCursor;
    
    class MethodTask implements Runnable {
        
        private String clientClassName;
        private LinkedList paramList;
        private JavaMethod javaMethod;
        private URLClassLoader urlClassLoader;
        private ArrayList listeners = new ArrayList();
        private boolean cancelled=false;
        
        MethodTask(String inClientClassName, LinkedList inParamList, JavaMethod inJavaMethod,
                URLClassLoader inURLClassLoader) {
            clientClassName = inClientClassName;
            paramList = inParamList;
            javaMethod = inJavaMethod;
            urlClassLoader = inURLClassLoader;
        }
        
        public void registerListener(MethodTaskListener inListener) {
            if(!listeners.contains(inListener)) {
                listeners.add(inListener);
            }
        }
        
        private void notifyListeners(Object returnedObject) {
            Iterator listenerIterator = listeners.iterator();
            MethodTaskListener currentListener = null;
            while(listenerIterator.hasNext()) {
                currentListener = (MethodTaskListener)listenerIterator.next();
                currentListener.methodFinished(returnedObject, paramList);
            }
        }
        
        public void run() {
            /**
             * Now invoke the method using the ReflectionHelper.
             */
            Object returnObject=null;
            try {
                returnObject = ReflectionHelper.callMethodWithParams(clientClassName, paramList, javaMethod,urlClassLoader);
            } catch(WebServiceReflectionException wsre) {
                if(!cancelled) {
                    Throwable exception = wsre;
                    if (wsre.getCause() instanceof java.lang.reflect.InvocationTargetException) {
                        exception = wsre.getCause();
                    }
                    MethodExceptionDialog errorDialog = new MethodExceptionDialog(exception);
                    /**
                     * Notify the listeners so the cursor will be reset;
                     */
                    notifyListeners(null);
                    errorDialog.show();
                }
                return;
            }
            
            notifyListeners(returnObject);
        }
        
        public void cancel() {
            cancelled=true;
        }
    }
    
    private static class BusyMouseAdapter extends MouseAdapter {
        private Cursor normalCursor;
        
        public BusyMouseAdapter(Cursor inNormalCursor) {
            normalCursor = inNormalCursor;
        }
        
        public void mouseEntered(MouseEvent e) {
            e.getComponent().setCursor(normalCursor);
        }
        
        public void mouseExited(MouseEvent e) {
        }
    }
}
