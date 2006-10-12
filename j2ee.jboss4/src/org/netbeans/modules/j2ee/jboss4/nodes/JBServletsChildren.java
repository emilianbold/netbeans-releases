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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the Web Module node.
 *
 * @author Michal Mocnak
 */
public class JBServletsChildren extends Children.Keys {
    
    private static final String WAIT_NODE = "wait_node"; //NOI18N
    
    private String name;
    private Lookup lookup;
    
    JBServletsChildren(String name, Lookup lookup) {
        this.lookup = lookup;
        this.name = name;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            
            public void run() {
                try {
                    // Query to the jboss4 server
                    Object server = Util.getRMIServer(lookup);
                    ObjectName searchPattern = new ObjectName("jboss.management.local:WebModule="+name+",j2eeType=Servlet,*");
                    Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});
                    
                    Iterator it = managedObj.iterator();
                    
                    // Query results processing
                    while(it.hasNext()) {
                        ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                        String s = elem.getKeyProperty("name");
                        keys.add(new JBServletNode(s));
                    }                    
                } catch (MalformedObjectNameException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
                } catch (NullPointerException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
                } catch (SecurityException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
                } catch (InvocationTargetException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
                } catch (IllegalAccessException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
                } catch (NoSuchMethodException ex) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
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
        if (key instanceof JBServletNode){
            return new Node[]{(JBServletNode)key};
        }
        
        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node[]{createWaitNode()};
        }
        
        return null;
    }
    
    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    private Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBApplicationsChildren.class, "LBL_WaitNode_DisplayName")); //NOI18N
        n.setIconBaseWithExtension("org/openide/src/resources/wait.gif"); // NOI18N
        return n;
    }
}