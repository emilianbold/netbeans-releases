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

package org.netbeans.modules.websvc.wsitconf.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class SecurityCheckerRegistry {
    
    private static SecurityCheckerRegistry instance;
    
    private List<SecurityChecker> checkers = (List<SecurityChecker>) Collections.synchronizedList(new LinkedList<SecurityChecker>());
    
    private static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.websvc.wsitconf.spi");   // NOI18N
    
    /**
     * Creates a new instance of SecurityCheckerRegistry
     */
    private SecurityCheckerRegistry() {}

    /**
     * Returns default singleton instance of registry
     */
    public static SecurityCheckerRegistry getDefault(){
        if (instance == null) {
            instance = new SecurityCheckerRegistry();
        }
        return instance;
    }
      
    /**
     * Registers checker to the list
     */
    public void register(SecurityChecker checker) {
        if (checker != null) {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log(ErrorManager.INFORMATIONAL, "registerChecker: " + checker + ", dName: " + checker.getDisplayName());    //NOI18N
            }
            checkers.add(checker);
        }
    }
    
    /**
     * Unregisters checker from the list
     */
    public void unregister(SecurityChecker checker) {
        if (checker != null) {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log(ErrorManager.INFORMATIONAL, "unregisterChecker: " + checker + ", dName: " + checker.getDisplayName());    //NOI18N
            }
            checkers.remove(checker);
        }
    }
    
    public Collection<SecurityChecker> getSecurityCheckers() {
        return Collections.unmodifiableList(checkers);
    }
    
    public boolean isNonWsitSecurityEnabled(Node node, JaxWsModel jaxWsModel) {
        if ((node != null) && (jaxWsModel != null)) {
            Collection<SecurityChecker> secCheckers = getSecurityCheckers();
            for (SecurityChecker sc : secCheckers) {
                boolean secEnabled = sc.isSecurityEnabled(node, jaxWsModel);
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(ErrorManager.INFORMATIONAL, "securityEnabled: " + secEnabled + ", " + sc +                 //NOI18N
                            ", dName: " + sc.getDisplayName() + ", node: " + node + ", jaxwsmodel: " + jaxWsModel);    //NOI18N
                }
                if (secEnabled) {
                    return true;
                }
            }
        }
        return false;
    }

}
