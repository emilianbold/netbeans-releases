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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the enterprise application node.
 *
 * @author Michal Mocnak
 */
public class JBEarModulesChildren extends Children.Keys {
    
    private Lookup lookup;
    private String j2eeAppName;
    
    public JBEarModulesChildren(Lookup lookup, String j2eeAppName) {
        this.lookup = lookup;
        this.j2eeAppName = j2eeAppName;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            JBDeploymentManager dm = (JBDeploymentManager)lookup.lookup(JBDeploymentManager.class);
            
            public void run() {
                try {
                    // Query to the jboss4 server
                    Object server = Util.getRMIServer(lookup);
                    ObjectName searchPattern = new ObjectName("jboss.management.local:J2EEApplication="+j2eeAppName+",*");
                    Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});
                    
                    Iterator it = managedObj.iterator();
                    
                    // Query results processing
                    while(it.hasNext()) {
                        try {
                            ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                            String name = elem.getKeyProperty("name");                            
                            
                            if(elem.getKeyProperty("j2eeType").equals("EJBModule"))
                                keys.add(new JBEjbModuleNode(name, lookup));
                            else if(elem.getKeyProperty("j2eeType").equals("WebModule")) {
                                String url = "http://"+dm.getHost()+":"+dm.getPort();
                                String descr = (String)Util.getMBeanParameter(dm, "jbossWebDeploymentDescriptor", elem.getCanonicalName());
                                String context = Util.getWebContextRoot(descr, name);
                                keys.add(new JBWebModuleNode(name, lookup, (context == null) ? null : url+context));
                            }
                        } catch (Exception ex) {
                            Logger.getLogger("global").log(Level.INFO, null, ex);
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
                
                setKeys(keys);
            }
        }, 0);
        
    }
    
    protected void addNotify() {
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBEjbModuleNode){
            return new Node[]{(JBEjbModuleNode)key};
        }
        
        if (key instanceof JBWebModuleNode){
            return new Node[]{(JBWebModuleNode)key};
        }
        
        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }
        
        return null;
    }
}
