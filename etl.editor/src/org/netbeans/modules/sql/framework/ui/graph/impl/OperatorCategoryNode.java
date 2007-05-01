/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoCategory;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorCategoryNode extends CommonNode implements IOperatorXmlInfoCategory {

    private static final String KEY_TOOLBARCATEGORY = "ToolbarCategory";
    private static final String KEY_TOOLTIP = "ToolTip";
    private static final String KEY_DISPLAYNAME = "DisplayName";
    private static final String LOG_CATEGORY = OperatorCategoryNode.class.getName();

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
                this.getChildren().add(new Node[] { node});
                // this map for quick search of node based on node name
                operatorNameToNodeMap.put(node.getName(), node);
            } catch (BaseException ignore) {
                Logger.printThrowable(Logger.DEBUG, LOG_CATEGORY, null, "Could not load item(s) in operator folder " + obj.getName(), ignore);
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

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoCategory#getToolbarType()
     */
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

