/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import org.netbeans.core.ui.LookupNode;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/** This object represents environment settings in the Corona system.
* This class is final only for performance purposes.
* Can be unfinaled if desired.
*
* @author Petr Hamernik, Dafe Simonek
*/
final class EnvironmentNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4782447107972624693L;
    /** name of section to filter */
    private String filter;
    /** icon base for icons of this node */
    private static final String EN_ICON_BASE = "org/netbeans/core/resources/"; // NOI18N
    /** map between type of node and the parent node for this type (String, Node) */
    private static java.util.HashMap types = new java.util.HashMap (11);
    /** A lock for the find method. */
    private static final Object lock = new Object();

    /** Type to add an entry to the root nodes. */
    public static final String TYPE_ROOTS = "roots"; // NOI18N
    /** Type to add an entry to the Environment (in the Explorer). */
    public static final String TYPE_ENVIRONMENT = "environment"; // NOI18N
    /** Type to add an entry to the Session settings. */
    public static final String TYPE_SESSION = "session"; // NOI18N
    
    /** Constructor */
    private EnvironmentNode (String filter, Children children) {
        super (children);
        this.filter = filter;
        decorateNode(filter, this);
    }
    
    
    /** Finds the node for given name.
     */
    public static Node find (final String name) {
        // XXX this is probably obsolete? consider deleting
        Object retValue = 
            Children.MUTEX.readAccess(new Mutex.Action() {
                public Object run() {
                    synchronized (lock) {
                        Node n = (Node)types.get (name);
                        if (n == null) {
                            DataFolder folder = null;
                            if (TYPE_ENVIRONMENT.equals(name)) {
                                folder = NbPlaces.getDefault().findSessionFolder("UI/Runtime"); // NOI18N
                            } else if (TYPE_ROOTS.equals(name)) {
                                folder = NbPlaces.getDefault().findSessionFolder("UI/Roots");
                            } else {
                                assert TYPE_SESSION.equals(name) : name;
                                folder = NbPlaces.getDefault().findSessionFolder("UI/Services"); // NOI18N
                            }

                            n = new PersistentLookupNode(name, folder);
                            types.put (name, n);
                        }
                        return n;
                    }
                }
            });
        if (retValue != null) {
            return (Node)retValue;
        }
        throw new IllegalStateException();
    }
    
    private static void decorateNode (String name, AbstractNode node) {
        String resourceName = "CTL_" + name + "_name"; // NOI18N
        String iconBase = EN_ICON_BASE + name.toLowerCase ();
        
        node.setDisplayName(NbBundle.getMessage (EnvironmentNode.class, resourceName));
        node.setIconBase(iconBase);
    }
        

    public HelpCtx getHelpCtx () {
        return new HelpCtx (EnvironmentNode.class);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class)
               };
    }

    /** For deserialization */
    public Node.Handle getHandle () {
        return new EnvironmentHandle (filter);
    }

    /** Adds serialization support to LookupNode */
    private static final class PersistentLookupNode extends LookupNode 
    implements java.beans.PropertyChangeListener {
        
        private String filter;
        
        public PersistentLookupNode (String filter, DataFolder folder) {
            super(folder);
            this.filter = filter;
            
            if (TYPE_ROOTS.equals(filter)) {
                folder.addPropertyChangeListener(
                    org.openide.util.WeakListeners.propertyChange(this, folder));
            }
        }
        
        public Node.Handle getHandle () {
            return new EnvironmentHandle (filter);
        }

        /** Listens on changes on root nodes. */
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if(DataFolder.PROP_CHILDREN.equals(evt.getPropertyName())) {
                NbPlaces.getDefault().fireChange();
            }
        }
    } // end of PersistentLookupNode

    static final class EnvironmentHandle implements Node.Handle {
        static final long serialVersionUID =-850350968366553370L;
        
        /** field */
        private String filter;
        
        /** constructor */
        public EnvironmentHandle (String filter) {
            this.filter = filter;
        }
        public Node getNode () {
            String f = filter;
            if (f == null) {
                // use the original node
                f = TYPE_ENVIRONMENT;
            }
            
            return find (f);
        }
    }
}
