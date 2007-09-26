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

package org.netbeans.core.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import org.netbeans.core.LoaderPoolNode;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
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
