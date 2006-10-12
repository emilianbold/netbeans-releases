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
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.Refreshable;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the Web Applications node. Implements
 * Refreshable interface and due to it can be refreshed via ResreshModulesAction.
 *
 * @author Michal Mocnak
 */
public class JBWebApplicationsChildren extends Children.Keys implements Refreshable {
    
    private Lookup lookup;
    private Boolean remoteManagementSupported = null;
    
    public JBWebApplicationsChildren(Lookup lookup) {
        this.lookup = lookup;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            JBDeploymentManager dm = (JBDeploymentManager)lookup.lookup(JBDeploymentManager.class);
            
            public void run() {
                try {
                    // Query to the jboss4 server
                    ObjectName searchPattern;
                    if (isRemoteManagementSupported()) {
                        searchPattern = new ObjectName("jboss.management.local:j2eeType=WebModule,J2EEApplication=null,*"); // NOI18N
                    }
                    else {
                        searchPattern = new ObjectName("jboss.web:j2eeType=WebModule,J2EEApplication=none,*"); // NOI18N
                    }

                    Object server = Util.getRMIServer(lookup);
                    Set managedObj = (Set) server.getClass().getMethod("queryMBeans", new Class[]  {ObjectName.class, QueryExp.class}).invoke(server, new Object[]  {searchPattern, null});
                    
                    Iterator it = managedObj.iterator();
                    
                    // Query results processing
                    while(it.hasNext()) {
                        try {
                            ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                            String name = elem.getKeyProperty("name");
                            String url = "http://" + dm.getHost() + ":" + dm.getPort();
                            String context = "";
                            if (isRemoteManagementSupported()) {
                                if("jmx-console.war".equals(name)) { // Excluding it. It's system package
                                    continue;
                                }
                                String descr = (String)Util.getMBeanParameter(dm, "jbossWebDeploymentDescriptor", elem.getCanonicalName(), String.class); // NOI18N
                                context = Util.getWebContextRoot(descr);
                            }
                            else {
                                if (name.startsWith("//localhost/")) { // NOI18N
                                    name = name.substring("//localhost/".length()); // NOI18N
                                }
                                if("".equals(name) || "jmx-console".equals(name) || "jbossws".equals(name)) { // Excluding it. It's system package
                                    continue;
                                }
                                name +=  ".war"; // NOI18N

                                context = (String)Util.getMBeanParameter(dm, "path", elem.getCanonicalName(), String.class); // NOI18N
                            }
                            keys.add(new JBWebModuleNode(name, lookup, url + context));                        
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
        if (key instanceof JBWebModuleNode){
            return new Node[]{(JBWebModuleNode)key};
        }
        
        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }
        
        return null;
    }
    
    private boolean isRemoteManagementSupported() {
        if (remoteManagementSupported == null) {
            remoteManagementSupported = Util.isRemoteManagementSupported(lookup);
        }
        return remoteManagementSupported;
    }
    
}
