/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.nodes;

import org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Kirill Sorokin <Kirill.Sorokin@Sun.COM>
 */
public class JBRegistryNodeFactory implements RegistryNodeFactory {
    
//    public JBRegistryNodeFactory() {
//    }
    
    public Node getTargetNode(Lookup lookup) {
        return new JBTargetNode(lookup);
    }
    
    public Node getManagerNode(Lookup lookup) {
        return new JBManagerNode(new Children.Map(), lookup);
    }
    
//    public String getDisplayName() {
//        return "Registry Node Factory"; 
//    }
    
}
