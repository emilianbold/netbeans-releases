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

package org.netbeans.core.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import org.netbeans.core.LoaderPoolNode;
import org.netbeans.core.NbPlaces;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Set of basic nodes for the visualization of IDE state
 * @author Petr Hamernik, Dafe Simonek
 */
public final class UINodes {
    
    private final static String objectTypesIconURL = "org/netbeans/core/resources/objectTypes.gif"; // NOI18N
    private final static String objectTypesIcon32URL = "org/netbeans/core/resources/objectTypes32.gif"; // NOI18N

    /** empty array of property sets */
    private static final PropertySet[] NO_PROPERTY_SETS = {};

    /** Constructor */
    private UINodes() {
    }

    /** Getter for environment node.
    * @return environment node
    */
    public static Node createEnvironmentNode () {
        Node environmentNode = NbPlaces.getDefault().environment ().cloneNode ();
        environmentNode.setShortDescription(NbBundle.getMessage(UINodes.class, "CTL_Environment_Hint"));
        return environmentNode;
    }

    /** Getter for session settings node.
    * @return session settings node
    */
    public static Node createSessionNode () {
        Node sessionNode = NbPlaces.getDefault().session ().cloneNode ();
        sessionNode.setShortDescription(NbBundle.getMessage(UINodes.class, "CTL_Session_Settings_Hint"));
        return sessionNode;
    }

    /** Creates object types node.
     * @see "core/ui/src/org/netbeans/core/ui/resources/layer.xml"
    */
    public static Node createObjectTypes () {
        return new ObjectTypesNode ();
    }


    private static class IconSubstituteNode extends FilterNode {

        /** icons for the IconSubstituteNode */
        private String iconURL, icon32URL;

        IconSubstituteNode (Node ref, String iconURL, String icon32URL) {
            super (ref);
            this.iconURL = iconURL;
            this.icon32URL = icon32URL;
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage (iconURL);
            }
            else {
                return Utilities.loadImage (icon32URL);
            }
        }

        public Image getOpenedIcon (int type) {
            return getIcon(type);
        }
        
        public String getHtmlDisplayName() {
            return null;
        }

        /** @return empty property sets. */
        public PropertySet[] getPropertySets () {
            return NO_PROPERTY_SETS;
        }

        public boolean canDestroy () {
            return false;
        }

        public boolean canCut () {
            return false;
        }

        public boolean canRename () {
            return false;
        }
    }

    /** Node representing object types folder */
    private static class ObjectTypesNode extends IconSubstituteNode {

        public ObjectTypesNode() {
            this (LoaderPoolNode.getLoaderPoolNode());
        }

        public ObjectTypesNode(Node ref) {
            super(ref, objectTypesIconURL, objectTypesIcon32URL);
        }

        public Action[] getActions(boolean context) {
            return new Action[] {
                SystemAction.get(ReorderAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        }
    }
    
}
