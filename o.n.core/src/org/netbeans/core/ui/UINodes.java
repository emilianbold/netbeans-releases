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

package org.netbeans.core.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.*;

import org.openide.*;
import org.openide.actions.*;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.options.*;
import org.openide.nodes.*;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/** Set of basic nodes for the visualization of IDE state (originally org.netbeans.core.DesktopNode)
*
* @author Petr Hamernik, Dafe Simonek
*/
public final class UINodes extends Object {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4457929339850358728L;

    private static java.util.ResourceBundle bundle = NbBundle.getBundle (org.netbeans.core.NbTopManager.class);

    /** Default icons for nodes */
    private static final String DESKTOP_ICON_BASE="/org/netbeans/core/resources/desktop"; // NOI18N
    private static final String PROJECT_SETTINGS_ICON_BASE="/org/netbeans/core/resources/controlPanel"; // NOI18N


    private final static String templatesIconURL=  "/org/netbeans/core/resources/templates.gif"; // NOI18N
    private final static String templatesIcon32URL="/org/netbeans/core/resources/templates32.gif"; // NOI18N
    private final static String startupIconURL=    "/org/netbeans/core/resources/startup.gif"; // NOI18N
    private final static String startupIcon32URL=  "/org/netbeans/core/resources/startup32.gif"; // NOI18N
    private final static String objectTypesIconURL=    "/org/netbeans/core/resources/objectTypes.gif"; // NOI18N
    private final static String objectTypesIcon32URL=  "/org/netbeans/core/resources/objectTypes32.gif"; // NOI18N

    /** empty array of property sets */
    private static final PropertySet[] NO_PROPERTY_SETS = {};

    /** Constructor */
    private UINodes() {
    }

    /** Getter for environment node.
    * @return environment node
    */
    public static Node createEnvironmentNode () {
        Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
        Node environmentNode = ns.environment ().cloneNode ();
        environmentNode.setShortDescription (bundle.getString ("CTL_Environment_Hint"));
        return environmentNode;
    }

    /** Getter for session settings node.
    * @return session settings node
    */
    public static Node createSessionNode () {
        Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
        Node sessionNode = ns.session ().cloneNode ();
        sessionNode.setShortDescription (bundle.getString ("CTL_Session_Settings_Hint"));
        return sessionNode;
    }

   /** Getter for filesystem pool node.
    * @return a node
    */
    public static Node createFileSystems () {
        return new MountNode (
            org.netbeans.core.NbPlaces.findSessionFolder ("Mount") // NOI18N
        );
    }

/*
    static Node getProjectSettingsNode () {
        return ControlPianelNode.getProjectSettings ();
    }
*/

    /** Getter for environment node.
    * @return environment node
    *
    static Node createProjectSettingsNode () {
        return getProjectSettingsNode ().cloneNode ();
    }
    */


    /** Creates template node.
    */
    public static Node createTemplate () {
        return new TemplatesNode ();
    }

    /** Creates startup node.
    */
    public static Node createStartup () {
        return new StartupNode ();
    }

    /** Creates object types node.
    */
    public static Node createObjectTypes () {
        return new ObjectTypesNode ();
    }


    private static class IconSubstituteNode extends FilterNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -2098259549820241091L;

        private static SystemAction[] staticActions;

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

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
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

    /** Getter for folders.
    */
    private static Places.Folders fs () {
        return TopManager.getDefault ().getPlaces ().folders ();
    }

    /** Node representing templates folder */
    private static class TemplatesNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;

        public TemplatesNode () {
            this (fs ().templates ().getNodeDelegate ());
        }

        public TemplatesNode(Node ref) {
            super(ref, templatesIconURL, templatesIcon32URL);
            super.setDisplayName(bundle.getString("CTL_Templates_name"));
            super.setShortDescription(bundle.getString("CTL_Templates_hint"));
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (TemplatesNode.class);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(NewAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }
    }

    /** Node representing startup folder */
    private static class StartupNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;

        public StartupNode() {
            super (fs ().startup ().getNodeDelegate (), startupIconURL, startupIcon32URL);
            super.setDisplayName(bundle.getString("CTL_Startup_name"));
            super.setShortDescription(bundle.getString("CTL_Startup_hint"));
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (StartupNode.class);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(PasteAction.class),
                                    null,
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }
    }


    /** Node representing object types folder */
    private static class ObjectTypesNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;

        public ObjectTypesNode() {
            this (TopManager.getDefault ().getPlaces ().nodes ().loaderPool ());
        }

        public ObjectTypesNode(Node ref) {
            super(ref, objectTypesIconURL, objectTypesIcon32URL);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(CustomizeBeanAction.class),
                                    null,
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }
    }
}
