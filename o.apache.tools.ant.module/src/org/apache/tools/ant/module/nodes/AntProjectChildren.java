/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.nodes;

import javax.swing.event.*;

import org.w3c.dom.Element;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;

public class AntProjectChildren extends ElementChildren implements ChangeListener {
    
    private AntProjectCookie cookie;
    
    public AntProjectChildren (AntProjectCookie cookie) {
        super ();
        this.cookie = cookie;
    }
    
    protected Element getElement () {
        return cookie.getProjectElement ();
    }
    
    protected Node[] createNodes (Object key) {
        Element el = (Element) key;
        String type = el.getNodeName ();
        if (type.equals ("target")) { // NOI18N
            return new Node[] { new AntTargetNode (cookie, el) };
        } else if (type.equals ("property") || type.equals ("taskdef")) { // NOI18N
            return new Node[] { new AntTaskNode (el) };
        } else {
            // Data type, hopefully.
            String clazz = (String) IntrospectedInfo.getDefaults ().getTypedefs ().get (type);
            if (clazz == null) {
                clazz = (String) AntSettings.getDefault ().getCustomDefs ().getTypedefs ().get (type);
            }
            if (clazz != null) {
                AntModule.err.log ("AntProjectChildren.createNodes: type=" + type + " clazz=" + clazz);
                return new org.openide.nodes.Node[] { new DataTypeNode (el, clazz) };
            } else {
                // Unknown tidbit of XML.
                return new org.openide.nodes.Node[] { new ElementNode (el, NbBundle.getMessage (AntProjectChildren.class, "LBL_unknown_datatype", type)) };
            }
        }
    }
    
    protected void addNotify () {
        super.addNotify ();
        cookie.addChangeListener (this);
    }

    protected void removeNotify () {
        super.removeNotify ();
        cookie.removeChangeListener (this);
    }

    public void stateChanged (ChangeEvent ev) {
        refreshKeys ();
    }
    
}
