/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.ProcedureNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableNodeInfo;
import org.netbeans.modules.db.explorer.infos.ViewNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

//import org.openide.util.Mutex;

// XXX This entire class is junk. Should have a sensible data model independent of
// nodes and display it using Children.Keys (or Looks) and everything would be
// much easier. -jglick

// I totally agree. It was planed to redesign the module and base it on a data model
// unfortunately this project was cancelled. Radko

public class DatabaseNodeChildren extends Children.Array {

    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
    
    NodeComparator comparator;

    // synchronized by this
    private TreeSet<Node> children;
    private transient PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    // synchronized by additionalNodes
    private boolean initialized = false; // true if the node is displaying its children (not the "Please wait..." node)
    // synchronized by additionalNodes
    private List<Node> additionalNodes = new ArrayList<Node>(); // nodes added by createSubnode() during the "Please wait..." phase
    
    private static Logger LOGGER = Logger.getLogger(
            DatabaseNodeChildren.class.getName());

    private PropertyChangeListener listener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals("finished")) { //NOI18N
                MUTEX.writeAccess(new Runnable() {
                    public void run() {
                        // remove wait node and replace it with a sorted set
                        nodes = new TreeSet<Node>(comparator);
                        add(getCh().toArray(new Node[0])); // change children ...
                        // add additional nodes created during the "Please wait..." phase
                        synchronized (additionalNodes) {
                            add(additionalNodes.toArray(new Node[0]));
                            initialized = true;
                        }
                        refresh(); // ... and refresh them
                    }
                });
                removeListener();
            }
        }
    };
    
    @Override
    protected Collection initCollection() {
        propertySupport.addPropertyChangeListener(listener);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {             
                DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();  
                java.util.Map nodeord = (java.util.Map)
                    nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);

                boolean sort = (! nodeinfo.getName().equals("Drivers") &&
                    (! (nodeinfo instanceof TableNodeInfo)) && 
                    (! (nodeinfo instanceof ViewNodeInfo)) && 
                    (! (nodeinfo instanceof ProcedureNodeInfo)) ); //NOI18N

                comparator = new NodeComparator(nodeord, sort);

                TreeSet<Node> tempChildren = new TreeSet<Node>(comparator);

                try {                    
                    Vector chlist;
                    chlist = nodeinfo.getChildren();

                    for (int i=0;i<chlist.size();i++) {
                        Node snode = null;
                        Object sinfo = chlist.elementAt(i);
                        
                        if (sinfo instanceof DatabaseNodeInfo) {
                            DatabaseNodeInfo dni = (DatabaseNodeInfo) sinfo;
                            
                            // aware! in this method is clone of instance dni created
                            snode = createNode(dni);
                        } else if (sinfo instanceof Node) {
                            snode = (Node)sinfo;
                        } else {
                            throw new ClassCastException(sinfo.getClass().getName());
                        }
                        
                        if (snode != null) {
                            LOGGER.log(Level.FINE, "Adding node " + 
                                    snode.getDisplayName());
                            logThreadDump();
                            tempChildren.add(snode);                            
                        }
                    }
                    
//commented out for 3.6 release, need to solve for next Studio release
//                    if (getNode() instanceof RootNode) {
//                        // open connection (after initCollection done)
//                        SwingUtilities.invokeLater(new Runnable() {
//                            public void run() {
//                                try {
//                                    // add connection (if needed) and make the connection to SAMPLE database connected
//                                    PointbasePlus.addOrConnectAccordingToOption();
//                                    } catch(Exception ex) {
//                                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.FINERMATIONAL, ex);
//                                    }
//                                }
//                            });
//                    }
                } catch (Exception e) {
                    Logger.getLogger("global").log(Level.FINE, null, e);
                    showException(e);
                    tempChildren.clear();
                }

                setCh(tempChildren);
                
                propertySupport.firePropertyChange("finished", null, null); //NOI18N
            }
        }, 0);

        TreeSet ts = new TreeSet();
        ts.add(createWaitNode());
        return ts;
    }
    
    private static void logThreadDump() {
        if ( LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Thread " + Thread.currentThread().getName());
            LOGGER.log(Level.FINE, "Stack dump: ", new Exception());  
        }
    }
    
    public boolean getChildrenInitialized() {
        return isInitialized();
    }

    /* Creates and returns the instance of the node
    * representing the status 'WAIT' of the node.
    * It is used when it spent more time to create elements hierarchy.
    * @return the wait node.
    */
    private Node createWaitNode () {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(bundle.getString("WaitNode")); //NOI18N
        n.setIconBase("org/netbeans/modules/db/resources/wait"); //NOI18N
        return n;
    }

    private synchronized TreeSet<Node> getCh() {
        return children;
    }

    private synchronized void setCh(TreeSet<Node> children) {
        this.children = children;
    }

    private void removeListener() {
        propertySupport.removePropertyChangeListener(listener);
    }

    class NodeComparator implements Comparator, Serializable {
        private java.util.Map map = null;
        private boolean sort;

        public NodeComparator(java.util.Map map, boolean sort) {
            this.map = map;
            this.sort = sort;
        }
        
        public int compare(Object o1, Object o2) {
            if (! sort)
                return 1;

            int o1val, o2val, diff;
            Integer o1i = (Integer)map.get(o1.getClass().getName());
            if (o1i != null)
                o1val = o1i.intValue();
            else
                o1val = Integer.MAX_VALUE;
            Integer o2i = (Integer)map.get(o2.getClass().getName());
            if (o2i != null)
                o2val = o2i.intValue();
            else
                o2val = Integer.MAX_VALUE;

            diff = o1val-o2val;
            
            if (diff != 0) {
                return diff;
            }
            
            String dname1 = ((Node)o1).getDisplayName();
            String dname2 = ((Node)o2).getDisplayName();

            if ( dname1 == null && dname2 == null) {
                // unlikely, but you never know
                return 0;
            }

            if ( dname1 == null ) {
                return -1;
            } else if ( dname2 == null ) {
                return 1;
            } else {
                return dname1.compareTo(dname2);
            }
        }
    }

    public DatabaseNode createNode(DatabaseNodeInfo info) {
        String nclass = (String)info.get(DatabaseNodeInfo.CLASS);
        DatabaseNode node = null;

        try {
            node = (DatabaseNode)Class.forName(nclass).newInstance();
            node.setInfo(info); /* makes a copy of info, use node.getInfo() to access it */
            node.getInfo().setNode(node); /* this is a weak, be cool, baby ;) */
        } catch (Exception e) {
            showException(e);
        }

        return node;
    }
    
    public void addSubNode(final Node subnode) {
        //workaround for issue #31617, children should be initialized if they are not
        //            getNodes();
        if (isInitialized()) {
            synchronized (additionalNodes) {
                if (initialized) {
                    MUTEX.postWriteRequest(new Runnable() {
                        public void run() {
                            add(new Node[] {subnode});
                        }
                    });
                } else {
                    LOGGER.log(Level.FINE, "ADDING TO ADDITIONAL: " + subnode.getDisplayName());
                    logThreadDump();
                    additionalNodes.add(subnode);
                }
            }
        }                

    }
    
    public void replaceNodes(final Node[] nodes) {
        if (isInitialized()) {
            synchronized (additionalNodes) {
                if (initialized) {
                    MUTEX.postWriteRequest(new Runnable() {
                        public void run() {
                            remove(getNodes());
                            add(nodes);
                        }
                    });
                } else {
                    if ( LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "REPLACING ON ADDITIONAL: "); 
                        for ( Node node : nodes ) {
                            LOGGER.log(Level.FINE, "==> " + node.getDisplayName());
                        }
                        logThreadDump();
                    }
                    additionalNodes.clear();
                    additionalNodes.addAll(Arrays.asList(nodes));
                }
            }
        }
    }
    
    public DatabaseNode createSubnode(DatabaseNodeInfo info, boolean addToChildrenFlag) throws DatabaseException {
        DatabaseNode subnode = createNode(info);
        if (subnode != null && addToChildrenFlag) {
            DatabaseNodeInfo ninfo = ((DatabaseNode)getNode()).getInfo();
            ninfo.getChildren().add(info);

            addSubNode(subnode);
        }

        return subnode;
    }
        
    private void showException(final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
                String format = bundle.getString("EXC_ConnectionError"); //NOI18N
                String message = bundle.getString("ReadStructureErrorPrefix") + " " + MessageFormat.format(format, new String[] {e.getMessage()}); //NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
        });
    }
}
