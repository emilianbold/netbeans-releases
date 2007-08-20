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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
public class JBEjbModulesChildren extends Children.Keys implements Refreshable {
    
    
    private final JBAbilitiesSupport abilitiesSupport;
    
    private Lookup lookup;
    
    public JBEjbModulesChildren(Lookup lookup) {
        this.lookup = lookup;
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
    }
    
    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                List keys = new LinkedList();
                Object server = Util.getRMIServer(lookup);
                addEjbModules(server, keys);
                addEJB3Modules(server, keys);
                
                setKeys(keys);
            }
        }, 0);
        
    }
    
    private void addEjbModules(Object server, List keys) {

        try {
            String propertyName;
            Object searchPattern;
            if (abilitiesSupport.isRemoteManagementSupported() && abilitiesSupport.isJB4x()) {
                propertyName = "name"; // NOI18N
                searchPattern = new ObjectName("jboss.management.local:j2eeType=EJBModule,J2EEApplication=null,*"); // NOI18N
            }
            else {
                propertyName = "module"; // NOI18N
                searchPattern = new ObjectName("jboss.j2ee:service=EjbModule,*"); // NOI18N
            }
            Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null}); // NOI18N

            Iterator it = managedObj.iterator();

            // Query results processing
            while(it.hasNext()) {
                ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                String name = elem.getKeyProperty(propertyName);
                keys.add(new JBEjbModuleNode(name, lookup));
            }

        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
    }
    
    private void addEJB3Modules(Object server, List keys) {
        
        try {
            ObjectName searchPattern = new ObjectName("jboss.j2ee:service=EJB3,*"); // NOI18N
            Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null}); // NOI18N

            Iterator it = managedObj.iterator();

            // Query results processing
            while(it.hasNext()) {
                try {
                    ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                    String name = elem.getKeyProperty("module"); // NOI18N
                    keys.add(new JBEjbModuleNode(name, lookup, true));
                } catch (Exception ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
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
        
        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }
        
        return null;
    }
    
}