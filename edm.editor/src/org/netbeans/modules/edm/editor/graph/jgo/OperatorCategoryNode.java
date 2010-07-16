/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.editor.graph.jgo;

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorCategoryNode extends CommonNode implements IOperatorXmlInfoCategory {

    private static final String KEY_TOOLBARCATEGORY = "ToolbarCategory";
    private static final String KEY_TOOLTIP = "ToolTip";
    private static final String KEY_DISPLAYNAME = "DisplayName";
    private static final String LOG_CATEGORY = OperatorCategoryNode.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(OperatorCategoryNode.class.getName());
    private DataFolder folder;
    private Map operatorNameToNodeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    /** Creates a new instance of OperatorCategoryNode */
    public OperatorCategoryNode(DataObject categoryObj) {
        super(categoryObj, new Children.Array());
        folder = (DataFolder) categoryObj.getCookie(DataFolder.class);
        createOperators();
    }

    private void createOperators() {
        DataObject[] children = folder.getChildren();
        for (int i = 0; i < children.length; i++) {
            DataObject obj = children[i];
            try {
                OperatorNode node = new OperatorNode(obj);
                this.getChildren().add(new Node[]{node});
                // this map for quick search of node based on node name
                operatorNameToNodeMap.put(node.getName(), node);
            } catch (EDMException ignore) {
                mLogger.log(Level.INFO,NbBundle.getMessage(OperatorCategoryNode.class, "LOG.INFO_Could_not_load_item",new Object[] {obj.getName()}),ignore);

            }
        }
    }

    /**
     * Gets the name of the operator (not I18n)
     * 
     * @return name
     */
    public String getName() {
        if (folder != null) {
            return folder.getName();
        }
        return "cat";

    }

    /**
     * Gets display name of the operator I18N
     * 
     * @return display name
     */
    public String getDisplayName() {
        if (folder != null) {
            String displayName = (String) folder.getPrimaryFile().getAttribute(KEY_DISPLAYNAME);
            return getLocalizedValue(displayName);
        }
        return "discat";
    }

    /**
     * Gets tool tip for the operator
     * 
     * @return tool tip
     */
    public String getToolTip() {
        String toolTip = (String) folder.getPrimaryFile().getAttribute(KEY_TOOLTIP);

        return getLocalizedValue(toolTip);
    }

    public int getToolbarType() {
        Integer toolbarType = (Integer) folder.getPrimaryFile().getAttribute(KEY_TOOLBARCATEGORY);
        int ret = IOperatorXmlInfoModel.CATEGORY_ALL;
        if (toolbarType != null) {
            ret = toolbarType.intValue();
        }

        return ret;
    }

    /**
     * Gets the icon for this operator
     * 
     * @return Icon for this category
     */
    public Icon getIcon() {
        return new ImageIcon(folder.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
    }

    /**
     * Gets the operator list for this category
     * 
     * @return operator list
     */
    public ArrayList getOperatorList() {
        ArrayList list = new ArrayList();

        Node[] nodes = this.getChildren().getNodes();
        for (int i = 0; i < nodes.length; i++) {
            list.add(nodes[i]);
        }
        return list;
    }

    /**
     * Gets the IOperatorXmlInfo instance in this category, if any, corresponding to the
     * given operator name.
     * 
     * @param operatorName name of operator to locate
     * @return IOperatorXmlInfo instance for <code>operatorName</code>, or null if it
     *         does not exist in this category
     */
    public IOperatorXmlInfo findOperatorXmlInfo(String operatorName) {
        return (IOperatorXmlInfo) operatorNameToNodeMap.get(operatorName);
    }
}

