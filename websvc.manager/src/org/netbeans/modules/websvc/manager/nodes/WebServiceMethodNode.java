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

package org.netbeans.modules.websvc.manager.nodes;

import org.netbeans.modules.websvc.manager.util.WebServiceLibReferenceHelper;
import org.netbeans.modules.websvc.manager.spi.WebServiceTransferManager;

import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.ErrorManager;

import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.netbeans.modules.websvc.manager.model.WebServiceData;

import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer.MethodTransferable;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A simple node with no children.
 * Often used in conjunction with some kind of underlying data model, where
 * each node represents an element in that model. In this case, you should see
 * the Container Node template which will permit you to create a whole tree of
 * such nodes with the proper behavior.
 * @author octav
 */
public class WebServiceMethodNode extends AbstractNode {
    
    private WebServiceData wsData;
    private JavaMethod javaMethod;
    private WsdlPort port;
    private WsdlOperation operation;
    private Transferable transferable;
    
    public JavaMethod getJavaMethod() {
        return javaMethod;
    }
    
    public WebServiceData getWebServiceData() {
        return wsData;
    }
    
    public WsdlPort getPort() {
        return port;
    }
    
    // will frequently accept an element from some data model in the constructor:
    public WebServiceMethodNode(WebServiceData inWsData, WsdlPort inPort, WsdlOperation inOperation) {
        this(inWsData, inPort, inOperation, new InstanceContent());
    }
    
    private WebServiceMethodNode(WebServiceData inWsData, WsdlPort inPort, WsdlOperation inOperation, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        
        wsData = inWsData;
        
        /**
         * Bug fix: 5059732
         * We need to get the port that this method is on to correctly get the type for the parameter.
         *
         */
        
        port = inPort;
        if(null == inOperation) {
            return;
        }
        operation = inOperation;
        javaMethod = ((Operation)inOperation.getInternalJAXWSOperation()).getJavaMethod() ;
        setName(inOperation.getJavaName());
        content.add(inOperation);
        content.add(wsData);
        content.add(javaMethod);
        content.add(port);
        
        transferable = ExTransferable.create(new MethodTransferable(
                new WebServiceMetaDataTransfer.Method(wsData, javaMethod, port.getName(), operation)));
        
        /**
         * Set the shortDescription (tooltip) to the method signature
         */
        String signature = javaMethod.getReturnType().getFormalName() + " " + javaMethod.getName() + "(";
        Iterator parameterIterator = javaMethod.getParametersList().iterator();
        JavaParameter currentParam = null;
        while(parameterIterator.hasNext()) {
            currentParam = (JavaParameter)parameterIterator.next();
            String parameterType = ManagerUtil.getParameterType(currentParam);
            signature += parameterType + " " + currentParam.getName();
            if(parameterIterator.hasNext()) {
                signature += ", ";
            }
        }
        signature += ")";
        setShortDescription(signature);    
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (WebServiceManagerExt ext : ManagerUtil.getExtensions()) {
            for (Action a : ext.getMethodActions()) {
                actions.add(a);
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    public Action getPreferredAction() {
        Action[] actions = getActions(true);
        return actions.length > 0 ? actions[0] : null;
    }
    
    public Image getIcon(int type){
        return getMethodIcon();
    }
    
    public Image getOpenedIcon(int type){
        return getMethodIcon();
    }
    
    private Image getMethodIcon() {
        if(!"void".equals(javaMethod.getReturnType().getRealName())) {
            Image image1 = Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/methodicon.png");
            Image image2 = Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/table_dp_badge.png");
            int x = image1.getWidth(null) - image2.getWidth(null);
            int y = image1.getHeight(null) - image2.getHeight(null);
            return Utilities.mergeImages( image1, image2, x, y);
        } else
            return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/methodicon.png");
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(WebServiceMethodNode.class);
    }
    
    /**
     * Create a property sheet for the individual method node
     * @return property sheet for the data source nodes
     */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_INFO"));
            sheet.put(ss);
        }
        
        try {
            Reflection p;
            
            p = new Reflection(javaMethod, String.class, "getName", null); // NOI18N
            p.setName("name"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_NAME"));
            p.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_NAME"));
            ss.put(p);
            String signature = javaMethod.getReturnType().getRealName() + " " +
                    javaMethod.getName() + "(";
            
            Iterator tempIterator = javaMethod.getParametersList().iterator();
            JavaParameter currentparam = null;
            while(tempIterator.hasNext()) {
                currentparam = (JavaParameter)tempIterator.next();
                signature += currentparam.getType().getRealName() + " " + currentparam.getName();
                if(tempIterator.hasNext()) {
                    signature += ", ";
                }
            }
            
            signature += ")";
            
            Iterator excpIterator = javaMethod.getExceptions();
            if(excpIterator.hasNext()) {
                signature += " throws";
                while(excpIterator.hasNext()) {
                    String currentExcp = (String)excpIterator.next();
                    signature +=  " " + currentExcp;
                    if(excpIterator.hasNext()) {
                        signature +=  ",";
                    }
                }
                
                
            }
            
            p = new Reflection(signature, String.class, "toString", null); // NOI18N
            p.setName("signature"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_SIGNATURE"));
            p.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_SIGNATURE"));
            ss.put(p);
            
            p = new Reflection(javaMethod.getReturnType(), String.class, "getRealName", null); // NOI18N
            p.setName("returntype"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_RETURNTYPE"));
            p.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_RETURNTYPE"));
            ss.put(p);
            
            Set paramSet = sheet.get("parameters"); // NOI18N
            if (paramSet == null) {
                paramSet = new Sheet.Set();
                paramSet.setName("parameters"); // NOI18N
                paramSet.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_PARAMDIVIDER")); // NOI18N
                paramSet.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_PARAMDIVIDER")); // NOI18N
                sheet.put(paramSet);
            }
            Iterator paramIterator = javaMethod.getParametersList().iterator();
            if(paramIterator.hasNext()) {
                p = new Reflection(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_PARAMTYPE"),
                        String.class,
                        "toString",
                        null); // NOI18N
                p.setName("paramdivider2"); // NOI18N
                
                JavaParameter currentParameter = null;
                for(int ii=0;paramIterator.hasNext();ii++) {
                    currentParameter = (JavaParameter)paramIterator.next();
                    if(currentParameter.getType().isHolder()) {
                        p = new Reflection(ManagerUtil.getParameterType(currentParameter), String.class, "toString", null); // NOI18N
                    } else {
                        p = new Reflection(currentParameter.getType(), String.class, "getRealName", null); // NOI18N
                    }
                    p.setName("paramname" + ii); // NOI18N
                    p.setDisplayName(currentParameter.getName());
                    p.setShortDescription(currentParameter.getName() + "-" +
                            currentParameter.getType().getRealName());
                    paramSet.put(p);
                }
            }
            Set exceptionSet = sheet.get("exceptions"); // NOI18N
            if (exceptionSet == null) {
                exceptionSet = new Sheet.Set();
                exceptionSet.setName("exceptions"); // NOI18N
                exceptionSet.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_EXCEPTIONDIVIDER")); // NOI18N
                exceptionSet.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_EXCEPTIONDIVIDER")); // NOI18N
                sheet.put(exceptionSet);
            }
            
            Iterator exceptionIterator = javaMethod.getExceptions();
            String currentException = null;
            for(int ii=0;exceptionIterator.hasNext();ii++) {
                currentException = (String)exceptionIterator.next();
                p = new Reflection(currentException, String.class, "toString", null); // NOI18N
                p.setName("exception" + ii); // NOI18N
                p.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_PARAMTYPE"));
                p.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_PARAMTYPE"));
                exceptionSet.put(p);
            }
        } catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(nsme);
        }
        
        return sheet;
    }
    
    // Handle copying and cutting specially:
    
    public boolean canCopy() {
        return true;
    }
    public boolean canCut() {
        return true;
    }
    
    public Transferable clipboardCopy() throws IOException {
        return addFlavors(transferable);
    }
    
    static Transferable addFlavors(Transferable transfer) {
        Collection<? extends WebServiceTransferManager> managers = Lookup.getDefault().lookupAll(WebServiceTransferManager.class);
        Transferable result = transfer;
        
        for (WebServiceTransferManager m : managers) {
            result = m.addDataFlavors(result);
        }
        return result;
    }
    
}
