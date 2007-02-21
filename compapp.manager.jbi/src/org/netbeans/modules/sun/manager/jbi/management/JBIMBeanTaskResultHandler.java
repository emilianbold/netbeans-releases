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

package org.netbeans.modules.sun.manager.jbi.management;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author jqian
 */
public class JBIMBeanTaskResultHandler {
    
    public static final String LINE_SEPARATOR =
            System.getProperty("line.separator"); // NOI18N
    
    /**
     * @param actionName      remote action name
     * @param target          action target (JBI component name,
     *                        service assembly name, or the artifact)
     * @param result          remote invocation result
     */
    public static void showRemoteInvokationResult(String actionName,
            String target, String result) {
        
        if (result == null || result.trim().length() == 0) {
            return;
        }
        
        System.out.println(result);
        
        StringBuffer ret = new StringBuffer();
        ret = ret.append("<html>"); // NOI18N
        
        boolean failed = false;
        
        if (result.indexOf("<?xml") == -1) { // NOI18N
            // No XML, certain exception (IO) occurred during invoke()
            if (result.indexOf("Exception") == -1) {
                return;
            } else {
                failed = true;
                ret = ret.append(result);
            }
        } else {
            // Need to extract info from the XML result
            result = result.substring(result.indexOf("<?xml")); // NOI18N
            Document document = getDocument(result);
            
            failed = !JBIMBeanTaskResultHandler.isFrameworkTaskResultSuccessful(document);
            
            List<TaskResult> frameworkTaskResults =
                    JBIMBeanTaskResultHandler.getTaskResultProblems(document, true);
            
            if (failed) {
                ret = ret.append("Failed execution of ");  // NOI18N
            } else if (frameworkTaskResults != null && frameworkTaskResults.size() > 0) {
                ret = ret.append("Successful execution of ");  // NOI18N
            } else {    // complete success
                return;
            }
            
            ret = ret.append(actionName);
            ret = ret.append(": ");  // NOI18N
            ret = ret.append(target);
            
//            if (partialSuccess) {
//                ret = ret.append("  (partial success)");  // NOI18N
//            }
            
            List<TaskResult> componentTaskResults =
                    JBIMBeanTaskResultHandler.getTaskResultProblems(document, false);
            
            for (TaskResult frameworkTaskResult : frameworkTaskResults) {
                ret = ret.append("<br>"); // NOI18N
                ret = ret.append(frameworkTaskResult.toHtmlString());
            }
            
            ret = ret.append("<ul>"); // NOI18N
            for (TaskResult componentTaskResult : componentTaskResults) {
                ret = ret.append(componentTaskResult.toHtmlString());
            }
            ret = ret.append("</ul>"); // NOI18N
        }
        
        ret.append("</html>");
        
        int msgType = failed ? NotifyDescriptor.ERROR_MESSAGE : NotifyDescriptor.WARNING_MESSAGE;
        NotifyDescriptor d = new NotifyDescriptor.Message(ret.toString(), msgType);
        DialogDisplayer.getDefault().notify(d);
    }
    
    public static boolean isFrameworkTaskResultSuccessful(Document document) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        
        try {
            String frameworkTaskResult = xpath.evaluate(
                    "//frmwk-task-result/frmwk-task-result-details/task-result-details/task-result/text()", document); // NOI18N
            return frameworkTaskResult.equals("SUCCESS"); // NOI18N
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    public static List<TaskResult> getTaskResultProblems(Document document, boolean framework) {
        List<TaskResult> ret = new ArrayList<TaskResult>();
        
        ret.addAll(getTaskResultExceptions(document, framework));
        ret.addAll(getTaskResultErrors(document, framework));
        ret.addAll(getTaskResultWarnings(document, framework));
        ret.addAll(getTaskResultInfos(document, framework));
        
        return ret;
    }
    
    public static List<TaskResult> getTaskResultExceptions(Document document, boolean framework) {
        String expression = getMyXPathExpression("EXCEPTION", framework);
        return getMsgLocInfoOfType("ERROR", document, expression, framework);
    }
    
    public static List<TaskResult> getTaskResultErrors(Document document, boolean framework) {
        String expression = getMyXPathExpression("ERROR", framework);
        return getMsgLocInfoOfType("ERROR", document, expression, framework);
    }
    
    public static List<TaskResult> getTaskResultWarnings(Document document, boolean framework) {
        String expression = getMyXPathExpression("WARNING", framework);
        return getMsgLocInfoOfType("WARNING", document, expression, framework);
    }
    
    public static List<TaskResult> getTaskResultInfos(Document document, boolean framework) {
        String expression = getMyXPathExpression("INFO", framework);
        return getMsgLocInfoOfType("INFO", document, expression, framework);
    }
    
    
    private static String getMyXPathExpression(String messageType, boolean framework) {
        String ret = null;
        
        String taskResult = framework? "frmwk-task-result" : "component-task-result";
        if (messageType.equals("EXCEPTION")) {
            ret = "//" + taskResult + "/*/task-result-details/exception-info/msg-loc-info"; // NOI18N
        } else {
            ret = "//" + taskResult + "/*/task-result-details[message-type='" + messageType +"']/task-status-msg/msg-loc-info"; // NOI18N
        }
        
        return ret;
    }
    
    
    private static List<TaskResult> getMsgLocInfoOfType(String type,
            Document document, String expression, boolean framework) {
        
        List<TaskResult> ret = new ArrayList<TaskResult>();
        
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            
            NodeList msgLocInfoNodeList = (NodeList) xpath.evaluate(expression,
                    document, XPathConstants.NODESET);
            
            if (msgLocInfoNodeList != null) {
                int length = msgLocInfoNodeList.getLength();
                for (int i = 0; i < length; i++) {
                    Node msgLocInfoNode = msgLocInfoNodeList.item(i);
                    String locTokenValue = xpath.evaluate("loc-token/text()", msgLocInfoNode); // NOI18N
                    String locMessageValue = xpath.evaluate("loc-message/text()", msgLocInfoNode); // NOI18N
                    
                    if (locTokenValue != null || locMessageValue != null) {
                        
                        if (framework) {
                            ret.add(new TaskResult(type, locTokenValue, locMessageValue));
                        } else {                            
                            Node parent = msgLocInfoNode;
                            while (!parent.getNodeName().equals("component-task-result")) {
                                parent = parent.getParentNode();
                            }
                            String componentName = 
                                    xpath.evaluate("component-name/text()", parent); // NOI18N
                            
                            
                            ret.add(new ComponentTaskResult(type, locTokenValue, 
                                    locMessageValue, componentName));  
                        }
                    }
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    private static Document getDocument(String xmlString) {
        try {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(xmlString)));
            
        } catch (Exception e) {
            System.out.println("Error parsing XML string: " + e); // NOI18N
            return null;
        }
    }
    
    public static Document getDocument(File xmlFile) {
        try {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new FileReader(xmlFile)));
        } catch (Exception e) {
            System.out.println("Error parsing XML file: " + e); // NOI18N
            return null;
        }
    }
    
}

class TaskResult {
    private String messageType;
    private String locToken;
    private String locMessage;
    
    TaskResult(String messageType, String locToken, String locMessage) {
        this.messageType = messageType;
        this.locToken = locToken;
        this.locMessage = locMessage;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getLocToken() {
        return locToken;
    }
    
    public void setLocToken(String locToken) {
        this.locToken = locToken;
    }
    
    public String getLocMessage() {
        return locMessage;
    }
    
    public void setLocMessage(String locMessage) {
        this.locMessage = locMessage;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer();
        
        ret = ret.append(getMessageType());
        ret = ret.append(": ("); // NOI18N
        ret = ret.append(getLocToken());
        ret = ret.append(") ");  // NOI18N
        ret = ret.append(getLocMessage());
        
        return ret.toString();
    }
    
    public String toHtmlString() {
        StringBuffer ret = new StringBuffer();
        
        ret = ret.append("<b>"); // NOI18N
        ret = ret.append(getMessageType());
        ret = ret.append("</b>"); // NOI18N
        ret = ret.append(": ("); // NOI18N
        ret = ret.append(getLocToken());
        ret = ret.append(") ");  // NOI18N
        ret = ret.append(getLocMessage());
        
        return ret.toString();
    }
}

class ComponentTaskResult extends TaskResult {
    private String componentName;
    
    ComponentTaskResult(String messageType, String locToken, String locMessage, String componentName) {
        super(messageType, locToken, locMessage);
        this.setComponentName(componentName);
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer();
        
        ret = ret.append("    * Component: "); // NOI18N
        ret = ret.append(getComponentName());
        ret = ret.append(JBIMBeanTaskResultHandler.LINE_SEPARATOR);
        ret = ret.append("      "); // NOI18N
        ret = ret.append(super.toString());
        
        return ret.toString();
    }
    
    public String toHtmlString() {
        StringBuffer ret = new StringBuffer();
        
        ret = ret.append("<li>"); // NOI18N
        ret = ret.append("Component: "); // NOI18N
        ret = ret.append(getComponentName());
        ret = ret.append("<br>"); // NOI18N
        ret = ret.append("      "); // NOI18N
        ret = ret.append(super.toHtmlString());
        ret = ret.append("</li>"); // NOI18N
        
        return ret.toString();
    }
}