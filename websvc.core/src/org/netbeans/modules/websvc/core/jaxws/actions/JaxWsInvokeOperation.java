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
    public void invokeOperation() {
    }
    
}
