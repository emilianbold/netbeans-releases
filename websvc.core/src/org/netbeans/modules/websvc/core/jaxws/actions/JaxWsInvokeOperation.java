/*
 * JaxWsAddOperation.java
 *
 * Created on December 12, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.InvokeOperationCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsInvokeOperation implements InvokeOperationCookie {
    
    private Project project;
    /** Creates a new instance of JaxWsAddOperation */
    public JaxWsInvokeOperation(Project project) {
        this.project=project;
    }
    
    /*
     * Adds a WS invocation to the class
     */
    public void invokeOperation(int targetSourceType, Node targetNode, Node serviceOperationNode) {
        JaxWsCodeGenerator.insertMethodCall(getCurrentDataObject(targetNode), targetNode, serviceOperationNode);
    }
    
    private DataObject getCurrentDataObject(Node n) {
        return (DataObject) n.getCookie(DataObject.class);
    }
}
