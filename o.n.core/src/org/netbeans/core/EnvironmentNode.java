/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.options.*;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.core.modules.ManifestSection;

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
    private static final String EN_ICON_BASE = "/org/netbeans/core/resources/"; // NOI18N
    /** map between type of node and the parent node for this type (String, Node) */
    private static java.util.HashMap types = new java.util.HashMap (11);
    

    /** Constructor */
    private EnvironmentNode (String filter) {
        super (new NbPlaces.Ch (filter));

        this.filter = filter;
        
        String resourceName = "CTL_" + filter + "_name"; // NOI18N
        String iconBase = EN_ICON_BASE + filter.toLowerCase ();
        
        setName(NbBundle.getMessage (EnvironmentNode.class, resourceName));
        setIconBase(iconBase);
    }
    
    /** Finds the node for given name.
     */
    public static EnvironmentNode find (String name) {
         EnvironmentNode n = (EnvironmentNode)types.get (name);
         if (n == null) {
             n = new EnvironmentNode (name);
             types.put (name, n);
         }
         return n;
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
                f = ManifestSection.NodeSection.TYPE_ENVIRONMENT;
            }
            
            return find (f);
        }
    }
}
