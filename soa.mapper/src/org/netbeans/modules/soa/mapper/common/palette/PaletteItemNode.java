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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.soa.mapper.common.palette;

import javax.swing.Action;

import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;

/**
 * The PaletteItemNode is a Node representing the Item of Component Palette in
 * the tree under Environment.
 *
 * @author    Tientien Li
 * @created   September 5, 2003
 */
public class PaletteItemNode
     extends org.openide.nodes.FilterNode
     implements IPaletteItem {

    /**
     * generated Serialized Version UID
     */

    //  static final long serialVersionUID = -2098259549820241091L;

    /**
     * Name of the template property.
     */
    public static final String PROP_IS_CONTAINER = "isContainer";
    // NOI18N

    /**
     * Field staticActions
     */
    private static SystemAction[] staticActions;

    /**
     * Creates a new palette node
     *
     * @param original  the original node to copy from
     */
    public PaletteItemNode(org.openide.nodes.Node original) {
        super(original, org.openide.nodes.FilterNode.Children.LEAF);
        initializeTooltip();
    }

    /**
     * Return the name of the PaletteItemNode object
     *
     * @return   The name value
     */
    public String getName() {
        String name = this.getDisplayName();
        if (name == null) {
            return super.getName();
        }
        return name;
    }

    /**
     * get the palette item node Icon
     *
     * @return   the icon image
     */
    public java.awt.Image getIcon() {
        return getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
    }

    /**
     * get the palette item node ToolTip
     *
     * @return   the tooltip text
     */
    public String getToolTip() {
        return getShortDescription();
    }

    /**
     * get a palette item node Attribute
     *
     * @param attr  the requested palette item node attribute
     * @return      the attribute value
     */
    public Object getItemAttribute(String attr) {

        DataObject obj =
            (DataObject) getOriginal().getCookie(DataObject.class);

        if (obj != null) {
            return obj.getPrimaryFile().getAttribute(attr);
            // NOI18N
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // Other methods

    /**
     * get the palette item node Display Name
     *
     * @return   the display name
     */
    public String getDisplayName() {
        String name = getAttributeDisplayName();

        if (name == null) {
            name = getExplicitDisplayName();
        }

        if (name != null) {
            return name;
        }

        org.openide.cookies.InstanceCookie ic =
            (InstanceDataObject) getCookie(InstanceDataObject.class);

        return (ic != null)
            ? ic.instanceName()
            : super.getDisplayName();
    }

    /**
     * Gets the attributeDisplayName attribute of the PaletteItemNode object
     *
     * @return   The attributeDisplayName value
     */
    String getAttributeDisplayName() {
        String localName = (String) this.getItemAttribute("LocalName");
        if (localName == null) {
            return null;
        }

        // test if localname is a key in the default bundle.
        String bundleLocalName = getStringFromDefaultBundle(localName);
        return (bundleLocalName == null) ? localName : bundleLocalName;
    }

    /**
     * get the palette item node explicit, i.e., long, Display Name
     *
     * @return   the explicity display name
     */
    String getExplicitDisplayName() {

        String name = getOriginal().getName();
        String displayName = getOriginal().getDisplayName();

        if (org.openide.loaders.DataNode.getShowFileExtensions()) {
            DataObject obj =
                (DataObject) getOriginal().getCookie(DataObject.class);

            if (obj != null) {
                String ext = "." + obj.getPrimaryFile().getExt();

                if (displayName.endsWith(ext)) {
                    displayName =
                        displayName.substring(0, displayName.length()
                        - ext.length());
                }
            }
        }

        return name.equals(displayName)
            ? null
            : displayName;
    }

    /**
     * Creates properties for this node
     *
     * @return   the list of node properties
     */
    public org.openide.nodes.Node.PropertySet[] getPropertySets() {

        java.util.ResourceBundle bundle = PaletteManager.getBundle();

        // default sheet with "properties" property set // NOI18N
        org.openide.nodes.Sheet sheet = org.openide.nodes.Sheet.createDefault();

        return sheet.toArray();
    }

    /**
     * destroy the palette item node
     *
     * @throws java.io.IOException  if encounter IO errors
     */
    public void destroy()
        throws java.io.IOException {
        super.destroy();
    }

    /**
     * can this palette item node be renamed
     *
     * @return   true if the node can be renamed
     */
    public boolean canRename() {

        DataObject dobj = (DataObject) getCookie(DataObject.class);

        return (dobj != null) && !(dobj instanceof InstanceDataObject);
    }

    /**
     * Set up the associated node actions.
     *
     * @return   array of actions for this node
     */
    public Action[] getActions(boolean context) {

        if (staticActions == null) {
            staticActions = new SystemAction[]{
                SystemAction.get(org.openide.actions.CustomizeAction.class),
                null,
                SystemAction.get(org.openide.actions.MoveUpAction.class),
                SystemAction.get(org.openide.actions.MoveDownAction.class),
                null,
                SystemAction.get(org.openide.actions.CutAction.class),
                SystemAction.get(org.openide.actions.CopyAction.class),
                null,
                SystemAction.get(org.openide.actions.DeleteAction.class),
                null,
                SystemAction.get(org.openide.actions.ToolsAction.class),
                SystemAction.get(org.openide.actions.PropertiesAction.class),
                };
        }

        return staticActions;
    }

    /**
     * Description of the Method
     */
    private void initializeTooltip() {
        String tooltip = (String) this.getItemAttribute("Tooltip");
        if (tooltip == null) {
            this.setShortDescription("");
            return;
        }

        // test if tooltip is a key in the default bundle.
        String bundleTooltip = getStringFromDefaultBundle(tooltip);
        if (bundleTooltip == null) {
            this.setShortDescription(tooltip);
        } else {
            this.setShortDescription(bundleTooltip);
        }
    }

    /**
     * Gets the stringFromDefaultBundle attribute of the PaletteItemNode object
     *
     * @param key  Description of the Parameter
     * @return     The stringFromDefaultBundle value
     */
    private String getStringFromDefaultBundle(String key) {
        String defaultBundle = (String) this.getItemAttribute("SystemFileSystem.localizingBundle");
        if (defaultBundle != null) {
            java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(defaultBundle);
            try {
                if (bundle != null) {
                    return bundle.getString(key);
                }
            } catch (java.util.MissingResourceException m) {
                // it is a test of resource existence, save to igrone exception
            }
        }
        return null;
    }
}
