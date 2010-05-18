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
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.Image;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;


/**
 * Extension of DefaultMutableTreeNode which represents a Database, table or column for
 * purposes of configuring its properties in the Flatfile Database wizard.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class TableColumnNode extends DefaultMutableTreeNode implements Comparable {

    /**
     * Extends TableColumnNode to represent a TableColumnNode with no children.
     */
    public static final class Leaf extends TableColumnNode {
        /**
         * Constructs a default instance of TableColumnNode.
         */
        public Leaf(Object model) {
            super(model);
            this.setAllowsChildren(false);
        }
    }

    /* Constant: indicates field node type */
    private static final int COLUMN = 1;

    /* Constant: indicates folder node type */
    private static final int DB = -1;

    /* Constant: indicates flatfile node type */
    private static final int TABLE = 0;

    public static boolean isColumnVisible(SQLDBColumn column, List tableNodes) {
        Iterator it = tableNodes.iterator();
        while (it.hasNext()) {
            TableColumnNode tNode = (TableColumnNode) it.next();
            Enumeration enu = tNode.children();
            while (enu.hasMoreElements()) {
                TableColumnNode cNode = (TableColumnNode) enu.nextElement();
                SQLDBColumn tColumn = (SQLDBColumn) cNode.getUserObject();
                if (column.equals(tColumn)) {
                    return cNode.isSelected();
                }
            }
        }
        return true;
    }

    /* node is enabled and eligible to be selected */
    private boolean enabled = true;

    /* whether user has selected this node */
    private boolean selected = true;

    private String toolTip;

    /* node type */
    private int type;

    /* associated user object */
    private Object userObject;

    /**
     * Creates a new instance of FlatfileNode with the associated Object.
     * 
     * @param model Object associated with this instance.
     * @throws Introspection Exception if error occurs during instantiation
     */
    public TableColumnNode(Object model) {
        super(model);

        if (model instanceof SQLDBModel) {
            type = DB;
            toolTip = ((SQLDBModel) model).getDisplayName();
        } else if (model instanceof SQLDBTable) {
            type = TABLE;
            toolTip = UIUtil.getTableToolTip((SQLDBTable) model);
        } else if (model instanceof SQLDBColumn) {
            SQLDBColumn field = (SQLDBColumn) model;
            type = COLUMN;
            selected = field.isVisible();
            toolTip = UIUtil.getColumnToolTip(field);
        } else {
            throw new IllegalArgumentException("Unrecognized model type:  must be SQLDBModel, SourceTable, or SourceColumn");
        }

        userObject = model;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     * <p>
     * Note: this class has a natural ordering that is inconsistent with equals.
     * 
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less
     *         than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it from being
     *         compared to this Object.
     */
    public int compareTo(Object o) {
        if (o == this) {
            return 0;
        } else if (o == null) {
            return -1;
        }

        TableColumnNode aNode = (TableColumnNode) o;
        switch (type) {
            case DB:
                if (aNode.type == DB) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == TABLE) {
                    return -1;
                } else if (aNode.type == COLUMN) {
                    return -1;
                }

            case TABLE:
                if (aNode.type == TABLE) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == DB) {
                    return 1;
                } else if (aNode.type == COLUMN) {
                    return -1;
                }

            case COLUMN:
                if (aNode.type == COLUMN) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == DB) {
                    return 1;
                } else if (aNode.type == TABLE) {
                    return 1;
                }

            default:
                throw new ClassCastException("Cannot compare between unrecognized TableColumnNode types.");
        }
    }

    /**
     * Overrides NetBeans implementation to directly return resolved image icons. We
     * generally use PNG images, while the NetBeans implementation (which sets base icon
     * names using setIconBase) can only handle GIFs.
     */
    public Image getIcon(int imgType) {
        String imgPath = null;
        Image myImage = null;

        switch (type) {
            case DB:
                imgPath = "/org/netbeans/modules/sql/framework/ui/resources/images/root.png";
                break;

            case TABLE:
                imgPath = "/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png";
                break;

            case COLUMN:
                imgPath = "/org/netbeans/modules/sql/framework/ui/resources/images/column.png";
                break;

            default:
                imgPath = null;
        }

        if (imgPath != null) {
            URL url = getClass().getResource(imgPath);
            if (url != null) {
                myImage = new ImageIcon(url).getImage();
            }
        }

        return myImage;
    }

    public String getName() {
        return (userObject != null) ? ((SQLObject) userObject).getDisplayName() : "<< Unknown >>";
    }

    public int getNodeType() {
        return this.type;
    }

    /**
     * @return Returns the toolTip.
     */
    public String getToolTip() {
        return toolTip;
    }

    /**
     * Indicates whether this node is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Indicates whether this node is selected.
     * 
     * @return true if node is selected, false otherwise
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets whether this node is enabled.
     * 
     * @param isEnabled sets to true if node is enabled, false otherwise
     */
    public void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

    /**
     * Sets whether this node is selected.
     * 
     * @param isSelected sets to true if node is selected, false otherwise
     */
    public void setSelected(boolean isSelected) {
        selected = isSelected;
    }

    public void setSelectedBasedOnChildren() {
        if (type == TABLE && this.getChildCount() != 0) {
            TableColumnNode child = (TableColumnNode) this.getFirstChild();
            boolean newValue = true;

            while (child != null) {
                if (!child.isSelected()) {
                    newValue = false;
                    break;
                }
                child = (TableColumnNode) this.getChildAfter(child);
            }

            setSelected(newValue);
        }
    }

    /**
     * @param toolTip The toolTip to set.
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    private int compareDisplayNames(DefaultMutableTreeNode first, DefaultMutableTreeNode second) {
        String firstDisplayName = (first.getUserObject() != null) ? ((SQLObject) first.getUserObject()).getDisplayName() : "";

        String secondDisplayName = (second.getUserObject() != null) ? ((SQLObject) second.getUserObject()).getDisplayName() : "";

        return firstDisplayName.compareTo(secondDisplayName);
    }
}

