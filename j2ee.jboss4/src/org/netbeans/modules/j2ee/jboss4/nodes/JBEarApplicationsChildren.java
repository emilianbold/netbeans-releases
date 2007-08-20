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
import org.netbeans.modules.j2ee.jboss4.nodes.actions.Refreshable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the EJB Modules node. Implements
 * Refreshable interface and due to it can be refreshed via ResreshModulesAction.
 *
 * @author Michal Mocnak
 */
public class JBEarApplicationsChildren extends Children.Keys implements Refreshable {

    private final JBAbilitiesSupport abilitiesSupport;

    private Lookup lookup;

    JBEarApplicationsChildren(Lookup lookup) {
        this.lookup = lookup;
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
    }

    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});

        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();

            public void run() {
                try {
                    // Query to the jboss4 server
                    ObjectName searchPattern;
                    String propertyName;
                    if (abilitiesSupport.isRemoteManagementSupported() && abilitiesSupport.isJB4x()) {
                        searchPattern = new ObjectName("jboss.management.local:j2eeType=J2EEApplication,*"); // NOI18N
                        propertyName = "name"; // NOI18N
                    } else {
                        searchPattern = new ObjectName("jboss.j2ee:service=EARDeployment,*"); // NOI18N
                        propertyName = "url"; // NOI18N
                    }
                    Object server = Util.getRMIServer(lookup);
                    Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});

                    // Query results processing
                    for (Iterator it = managedObj.iterator(); it.hasNext();) {
                        try {
                            ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                            String name = elem.getKeyProperty(propertyName);

                            if (abilitiesSupport.isRemoteManagementSupported() && abilitiesSupport.isJB4x()) {
                                if (name.endsWith(".sar") || name.endsWith(".deployer")) { // NOI18N
                                    continue;
                                }
                            } else {
                                name = name.substring(1, name.length() - 1); // NOI18N
                            }

                            keys.add(new JBEarApplicationNode(name, lookup));
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
        if (key instanceof JBEarApplicationNode){
            return new Node[]{(JBEarApplicationNode)key};
        }

        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }

        return null;
    }

}