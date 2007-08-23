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

package org.netbeans.modules.websvc.registry.ui;

import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.util.Util;
import com.sun.xml.rpc.processor.model.java.*;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;


/**
 *
 * @author  David Botterill
 */
public class TestWebServiceMethodDlg extends JPanel /* implements ActionListener*/ {
    private Dialog dialog;
    private DialogDescriptor dlg = null;
    private JavaMethod method;
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
    public String modifiedMethodName;

    /** Creates new form TestWebServiceMethodDlg */
    public TestWebServiceMethodDlg(WebServiceData inWSData,  JavaMethod inMethod, String inPortName) {
        this.setJavaMethod(inMethod);
        wsData = inWSData;
        packageName = inWSData.getPackageName();
        portName = inPortName;
        modifiedMethodName =  Util.getProperPortName(inPortName).toLowerCase() + Util.upperCaseFirstChar(inMethod.getName());

        initComponents();
        myInitComponents();

        this.lblTitle.setText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "TEST_WEBSVC_LABEL") + " " + modifiedMethodName);
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
             * Read in the Runtime Jar file Names
             */

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;

            try {
                builder = factory.newDocumentBuilder();

            } catch(ParserConfigurationException pe) {
                ErrorManager.getDefault().notify(pe);
                ErrorManager.getDefault().log("ParserConfigurationException=" + pe);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"));
                return null;
            }

            Document document = null;
            try {
                File runtimeJarsFile = InstalledFileLocator.getDefault().locate(
                        "config" + File.separator + "WebServices" + File.separator +
                        "websvc_runtimejars.xml", null, false);
                document = builder.parse(runtimeJarsFile);
            } catch(SAXException se) {
                ErrorManager.getDefault().notify(se);
                ErrorManager.getDefault().log("SAXException=" + se);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"));
                return null;
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                ErrorManager.getDefault().log("IOException=" + ioe);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"));
                return null;
            }

            NodeList list = document.getElementsByTagName("Jar");

            ArrayList urlList = new ArrayList();
            /**
             * First add the URL to the jar file for this web service.
             */
            try {
                /**
                 * If they are testing this from an existing web service in server navigator, or if they
                 * renamed their user directory, the path may have changed.  So if the file doesn't exist,
                 * try getting it from the current user directory.
                 */
                urlList.add(new URL("file:" + this.getWebServiceData().getProxyJarFileName()));
            } catch(MalformedURLException mfu) {
                ErrorManager.getDefault().notify(mfu);
                ErrorManager.getDefault().log(TestWebServiceMethodDlg.class.getName() + ":IOException=" + mfu);
//                StatusDisplayer.getDefault().displayError(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"),2);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"));
                return null;
            }

            // Now build remainder of class path from websvc_runtimejars.xml
            String serverInstanceIDs[] = Deployment.getDefault().getServerInstanceIDs ();
            J2eePlatform platform = null;
            for (int i = 0; i < serverInstanceIDs.length; i++) {
                J2eePlatform p = Deployment.getDefault().getJ2eePlatform (serverInstanceIDs [i]);
                if (p != null && p.isToolSupported ("wscompile")) {
                    platform = p;
                    break;
                }
            }
            File appserverRoot = platform == null ? null : platform.getPlatformRoots () [0];
            String asRootPath = (appserverRoot != null) ? appserverRoot.getAbsolutePath() : "";
            asRootPath = asRootPath.replace('\\', '/');
                        
            Node currentNode = null;
            for (int ii=0; ii < list.getLength(); ii++) {
                currentNode = list.item(ii);
                String name =currentNode.getNodeName();
                String localName =currentNode.getLocalName();
                String value = currentNode.getNodeValue();
                NamedNodeMap nodeMap = currentNode.getAttributes();
                Node fileNode = nodeMap.getNamedItem("file");
                String jarString = "";
                try {
                    jarString = fileNode.getNodeValue();
                } catch(DOMException de) {
                    ErrorManager.getDefault().notify(de);
                    ErrorManager.getDefault().log(TestWebServiceMethodDlg.class.getName() + ":IOException=" + de);
//                    StatusDisplayer.getDefault().displayError(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"),2);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"));
                    return null;
                }

                if (jarString.indexOf("\\{appserv\\.home\\}") > -1) {
                    jarString = jarString.replaceAll("\\{appserv\\.home\\}", asRootPath);
                } else {
                    File f = InstalledFileLocator.getDefault().locate(jarString, null, false);
                    if (f != null) {
                        jarString = f.getPath();
                    } 
                }
                
                /**
                 * Make sure we are starting with "file:".  If not, ifthe first character is
                 * not a "/", It's probably a drive letter and colon like
                 * "c:" on windows so we need to add an extra "/".
                 */
                if(!jarString.startsWith("file:")) {
                    if(jarString.substring(1,1).equals("/")) {
                        jarString = "file:/" + jarString;
                    } else {
                        jarString = "file:///" + jarString;
                    }
                }

                URL newURL = null;
                try {
                    newURL = new URL(jarString);
                } catch(MalformedURLException mfu) {
                    ErrorManager.getDefault().notify(mfu);
                    ErrorManager.getDefault().log(TestWebServiceMethodDlg.class.getName() + ":IOException=" + mfu);
//                    StatusDisplayer.getDefault().displayError(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"),2);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ERROR_READING_RUNTIMEJARS"));
                    return null;

                }
                urlList.add(newURL);

            }

            URL [] urls = (URL [])urlList.toArray(new URL[0]);
            /**
             * Make sure we don't have a parent to delegate to.  I first experienced Xerces classloader
             * clashes with the currentLoader as the parent.
             */

//			for(int i = 0; i < urls.length; i++) {
//				System.out.println("JAR: " + urls[i]);
//			}

            runtimeClassLoader = new URLClassLoader(urls, null);

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

        dlg = new DialogDescriptor(this, NbBundle.getMessage(TestWebServiceMethodDlg.class, "TEST_WEB_SERVICE_METHOD"),
            false, new Object[]{NotifyDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
            DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), null);
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        /**
         * After the window is opened, set the focus to the Get information button.
         */

        final JPanel thisPanel = this;
        final Dialog thisDialog = dialog;
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

            public void windowClosing(WindowEvent e) {
                thisDialog.dispose();
            }
        });

        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSD_TEST_WEB_SERVICE_DLG"));
        dialog.show();
    }
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TestWebServiceMethodDlg.class);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        pnlParameter = new javax.swing.JPanel();
        btnPanel = new javax.swing.JPanel();
        btnSubmit = new javax.swing.JButton();
        scrollPaneParameter = new javax.swing.JScrollPane();
        pnlLabel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pnlResults = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        scrollPaneResults = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        pnlParameter.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(btnSubmit, NbBundle.getMessage(TestWebServiceMethodDlg.class, "BUTTON_SUBMIT"));
        btnSubmit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSD_BUTTON_SUBMIT"));
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        btnPanel.add(btnSubmit);

        pnlParameter.add(btnPanel, java.awt.BorderLayout.SOUTH);

        pnlParameter.add(scrollPaneParameter, java.awt.BorderLayout.CENTER);

        pnlLabel.setLayout(new java.awt.GridLayout(2, 0, 10, 10));

        pnlLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        lblTitle.setFont(new java.awt.Font(getFont().getName(), getFont().getStyle(), getFont().getSize()+2));
        lblTitle.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/registry/ui/Bundle").getString("TEST_WEBSVC_LABEL"));
        lblTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pnlLabel.add(lblTitle);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(TestWebServiceMethodDlg.class, "PARAMETERS"));
        jLabel1.setToolTipText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "TEST_WEBSVC_INSTRUCTIONS"));
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pnlLabel.add(jLabel1);

        pnlParameter.add(pnlLabel, java.awt.BorderLayout.NORTH);

        jSplitPane1.setTopComponent(pnlParameter);

        pnlResults.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2,NbBundle.getMessage(TestWebServiceMethodDlg.class,"RESULTS"));
        jLabel2.setToolTipText(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSD_RESULT_TABLE"));
        jPanel4.add(jLabel2, java.awt.BorderLayout.NORTH);

        jPanel4.add(scrollPaneResults, java.awt.BorderLayout.CENTER);

        pnlResults.add(jPanel4, java.awt.BorderLayout.CENTER);

        jSplitPane1.setBottomComponent(pnlResults);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

    }

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run(){
                Cursor normalCursor = dialog.getCursor();
                dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                invokeMethod();
                dialog.setCursor(normalCursor);
            }
        });

    }


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
         * of the parameter nodes are values used to derive the parameter values.
         */

        /**
         * Use a LinkedList because we care about the order of the parameters.
         */
        LinkedList paramList = new LinkedList();
        for(int ii=0; null != this.getParamterRootNode() && ii < this.getParamterRootNode().getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) this.getParamterRootNode().getChildAt(ii);
            TypeNodeData nodeData = (TypeNodeData)childNode.getUserObject();
            Object parameterValue = nodeData.getParameterValue();
            if(parameterValue instanceof ArrayList) {
                try {
                    parameterValue = ReflectionHelper.getTypedParameterArray((ArrayList)parameterValue, nodeData.getParameterType(),
                    this.getRuntimeClassLoader(), getPackageName());
                } catch(WebServiceReflectionException wsre) {
                    Throwable cause = wsre.getCause();
                    ErrorManager.getDefault().notify(cause);
                    ErrorManager.getDefault().log(TestWebServiceMethodDlg.class.getName() +
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
        String clientClassName = wsData.getPackageName() + "." + wsData.getDisplayName() + "Client";

        /**
         * Now invoke the method using the ReflectionHelper.
         */
        Object returnObject=null;
        try {
            returnObject = ReflectionHelper.callMethodWithParams(clientClassName, paramList, this.getJavaMethod(),
            this.getRuntimeClassLoader(),modifiedMethodName);
        } catch(WebServiceReflectionException wsre) {
            MethodExceptionDialog errorDialog = new MethodExceptionDialog(wsre);
            errorDialog.show();
            return;
        }

        showResults(returnObject);

    }

    private void showResults(Object inResultObject) {
        /**
         * Create a tree of the result object types.
         */

        outline = loadResultTreeTable(this.getJavaMethod(), inResultObject);
        scrollPaneResults.setViewportView(outline);
        jLabel2.setLabelFor(outline);
    }


    private void myInitComponents() {

        /**
         * Now set up the Nodes for the TreeTableView
         */
        if(null == this.getJavaMethod()) {
            return;
        }

        outline = loadParameterTreeTable(this.getJavaMethod());



        /**
         * Add it to the correct Panel.
         */

        scrollPaneParameter.setViewportView(outline);
        jLabel1.setLabelFor(outline);

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

    private JavaMethod getJavaMethod() {
        return method;
    }

    private void setJavaMethod(JavaMethod inMethod) {
        method = inMethod;
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
        outlineModel.setNodeColumnName(NbBundle.getMessage(TestWebServiceMethodDlg.class, "TYPE_COLUMN_NAME"));
        Outline returnOutline = new Outline(outlineModel);
        ResultCellEditor cellEditor = new ResultCellEditor();
        returnOutline.setDefaultEditor(Object.class,cellEditor);
        returnOutline.setRootVisible(false);

        returnOutline.setRenderDataProvider(new TypeDataProvider());

        returnOutline.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSN_RESULT_TABLE"));
        //returnOutline.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSD_RESULT_TABLE"));
        
        return returnOutline;
    }

    private Outline loadParameterTreeTable(JavaMethod inMethod) {
        if(null == inMethod) {
            return null;
        }
        Iterator paramIterator = inMethod.getParameters();
        JavaParameter currentParameter = null;

        while(paramIterator.hasNext()) {
            /**
             * Add all Parameter's to the root tree node.
             */
            currentParameter = (JavaParameter)paramIterator.next();
            JavaType currentType = currentParameter.getType();
            Object value = null;
            value = getParameterDefaultValue(currentType);

            TypeNodeData data = new TypeNodeData(currentType,currentParameter.getName(),value);
            DefaultMutableTreeNode node = null;

            if(currentType instanceof JavaArrayType) {
                node = new ArrayTypeTreeNode(data);
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
        TypeRowModel rowModel = new TypeRowModel(this.getRuntimeClassLoader(),this.getPackageName());
        OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(treeModel,rowModel, false);
        outlineModel.setNodeColumnName(NbBundle.getMessage(TestWebServiceMethodDlg.class, "TYPE_COLUMN_NAME"));
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

        returnOutline.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSN_PARAMETER_TABLE"));
        returnOutline.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TestWebServiceMethodDlg.class, "ACSD_PARAMETER_TABLE"));
        
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
            if(entryType instanceof JavaEnumerationType || entryType instanceof JavaSimpleType) {
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
                ArrayTypeTreeNode node = new ArrayTypeTreeNode(data);
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
                    subTypeValue = ReflectionHelper.getStructureValue(parentData,member,this.getRuntimeClassLoader(),
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
        Object resultValue = parentData.getResultValue();
        if(null == resultValue) return;
        // !PW The code I wrote for native types is shorter and simpler than what
        // was here (that only worked on Objects and appears to work with Objects
        // properly so why not use it for both...
        ResultNodeData childData = null;
        int length = Array.getLength(resultValue);
        for(int i = 0; i < length; i++) {
            childData = new ResultNodeData(valueType, Array.get(resultValue, i));
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
                ErrorManager.getDefault().log(TestWebServiceMethodDlg.class.getName() +
                ": Error trying to do Class.forName on: " + packageName + "." + type.getFormalName() + "WebServiceReflectionException=" + cause);
                return null;
            }

            return returnValue;
        }
        /**
         *  If we have an Enumeration Type, instantiate the default as the first of the entries.
         */
        else if(type instanceof JavaEnumerationType) {
            Object returnValue = null;
            try {
                returnValue = ReflectionHelper.makeEnumerationType((JavaEnumerationType)type,
                this.getRuntimeClassLoader(),this.getPackageName());
            } catch(WebServiceReflectionException wsre) {
                Throwable cause = wsre.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(TestWebServiceMethodDlg.class.getName() +
                ": Error trying to create an Enumeration Type: " + packageName + "." + type.getFormalName() + "ClassNWebServiceReflectionExceptionotFoundException=" + cause);
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
            } else if("byte[]".equals(currentType)) {
                value  = new Byte[]{};               
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

            return value;
        }
    }

    // Variables declaration - do not modify
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlLabel;
    private javax.swing.JPanel pnlParameter;
    private javax.swing.JPanel pnlResults;
    private javax.swing.JScrollPane scrollPaneParameter;
    private javax.swing.JScrollPane scrollPaneResults;
    // End of variables declaration
    //    private TreeTableView treeTableView;
    private Outline outline;
}
