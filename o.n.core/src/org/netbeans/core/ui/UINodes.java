/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.*;
import org.netbeans.core.LoaderPoolNode;
import org.netbeans.core.NbPlaces;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.options.*;
import org.openide.nodes.*;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;

/** Set of basic nodes for the visualization of IDE state (originally org.netbeans.core.DesktopNode)
*
* @author Petr Hamernik, Dafe Simonek
*/
public final class UINodes extends Object {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4457929339850358728L;

    private static java.util.ResourceBundle bundle = NbBundle.getBundle (UINodes.class);

    private final static String templatesIconURL = "org/netbeans/core/resources/templates.gif"; // NOI18N
    private final static String templatesIcon32URL = "org/netbeans/core/resources/templates32.gif"; // NOI18N
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
        environmentNode.setShortDescription (bundle.getString ("CTL_Environment_Hint"));
        return environmentNode;
    }

    /** Getter for session settings node.
    * @return session settings node
    */
    public static Node createSessionNode () {
        Node sessionNode = NbPlaces.getDefault().session ().cloneNode ();
        sessionNode.setShortDescription (bundle.getString ("CTL_Session_Settings_Hint"));
        return sessionNode;
    }

    /** Creates template node.
    */
    public static Node createTemplate () {
        return new TemplatesNode ();
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
        
        public String getHtmlDisplayName() {
            return null;
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

    /** Node representing templates folder */
    private static class TemplatesNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;
        private DataFolder root;

        public TemplatesNode () {
            this (NbPlaces.getDefault().templates ().getNodeDelegate ());
            root = NbPlaces.getDefault().templates ();
        }

        public TemplatesNode(Node ref) {
            super(ref, templatesIconURL, templatesIcon32URL);
            super.setDisplayName(bundle.getString("CTL_Templates_name"));
            super.setShortDescription(bundle.getString("CTL_Templates_hint"));
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (TemplatesNode.class);
        }
        
        public NewType[] getNewTypes () {
            NewType type = new NewType () {
                public String getName () {
                    return NbBundle.getMessage (UINodes.class, "MSG_ActionName"); // NOI18N
                }
                
                public void create () throws IOException {
                    NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                            NbBundle.getMessage (UINodes.class, "MSG_FolderName"), // NOI18N
                            NbBundle.getMessage (UINodes.class, "MSG_InputNameDialogTitle"), // NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE);
                    
                    if (!NotifyDescriptor.OK_OPTION.equals (DialogDisplayer.getDefault ().notify (input))) {
                        return;
                    }
                    String newFolderName = input.getInputText ();
                    if (newFolderName.length () == 0) {
                        // same as cancel
                        return ;
                    }
                    if (root.getPrimaryFile ().getFileObject (newFolderName) != null) {
                        DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message (
                                    NbBundle.getMessage (UINodes.class, "MSG_FolderExists", newFolderName))); // NOI18N
                        return ;
                    }
                    DataFolder.create (root, newFolderName);
                }
            };
            return new NewType[] {type};
        }
        
        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(NewAction.class),
                                    null,
                                    SystemAction.get(PasteAction.class),
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
            this (LoaderPoolNode.getLoaderPoolNode());
        }

        public ObjectTypesNode(Node ref) {
            super(ref, objectTypesIconURL, objectTypesIcon32URL);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
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
