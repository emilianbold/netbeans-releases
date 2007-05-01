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
package org.netbeans.modules.mashup.db.ui;

import java.beans.IntrospectionException;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.ui.model.FlatfileColumn;
import org.netbeans.modules.mashup.db.ui.model.FlatfileDatabase;
import org.netbeans.modules.mashup.db.ui.model.FlatfileTable;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import com.sun.sql.framework.utils.Logger;

/**
 * NetBeans node extension that represents a flatfile or field for purposes of configuring
 * its properties in the Flatfile Database wizard.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileNode extends BeanNode implements Comparable {

    /**
     * Extends FlatfileNode to represent a FlatfileNode with no children.
     */
    public static final class Leaf extends FlatfileNode {
        /**
         * Constructs a default instance of FlatfileNode.
         * 
         * @throws FlatfileDBException if error occurs during instantiation
         */
        public Leaf() throws IntrospectionException {
            super(Children.LEAF);
        }

        /**
         * Creates an instance of Leaf associated with the given Object.
         * 
         * @param model Object associated with this new instance.
         * @throws FlatfileDBException if error occurs during instantiation
         */
        public Leaf(Object model) throws IntrospectionException {
            super(model, Children.LEAF);
        }
    }

    /* Constant: indicates table node type */
    private static final int COLUMN = 102;

    /* Constant: indicates database model type */
    private static final int DATABASE = 100;

    /* Log4J category string */
    private static final String LOG_CATEGORY = FlatfileNode.class.getName();

    /* Constant: indicates table node type */
    private static final int TABLE = 101;

    /* node is enabled and eligible to be selected */
    private boolean enabled = true;

    /* Array of available Actions for this node */
    private SystemAction[] mActions;

    /* whether user has selected this node */
    private boolean selected = true;

    /* node type */
    private int type;

    /* associated user object */
    private Object userObject;

    /**
     * Creates a new instance of FlatfileNode with the associated Object.
     * 
     * @param model Object associated with this instance.
     * @throws IntrospectionException if error occurs during instantiation
     */
    public FlatfileNode(Object model) throws IntrospectionException {
        this(model, new FlatfileChildren());
    }

    /**
     * Creates a new instance of FlatfileNode with the given child nodes and associated
     * Object.
     * 
     * @param children Children instance representing this instances's child nodes
     * @param model Object associated with this instance.
     * @throws IntrospectionException if error occurs during instantiation
     */
    public FlatfileNode(Object model, Children children) throws IntrospectionException {
        super(model, children);

        if (model instanceof FlatfileDatabase) {
            type = DATABASE;
            super.setName(((FlatfileDatabase) model).getName());
            setIconBase("org/netbeans/modules/mashup/db/ui/resource/images/OTD");
        } else if (model instanceof FlatfileTable) {
            type = TABLE;
            super.setName(((FlatfileTable) model).getTableName());
            setIconBase("org/netbeans/modules/mashup/db/ui/resource/images/Table");
        } else if (model instanceof FlatfileColumn) {
            type = COLUMN;
            FlatfileColumn column = (FlatfileColumn) model;
            super.setName(column.getName());
            if (column.isNullable()) {
                setIconBase("org/netbeans/modules/mashup/db/ui/resource/images/Column");
            } else {
                setIconBase("org/netbeans/modules/mashup/db/ui/resource/images/ColumnNotNull");
            }
        } else {
            throw new IllegalArgumentException("Unrecognized model type:  must be FlatfileDatabase, FlatfileTable, or FlatfileColumn");
        }

        super.setDisplayName(getName());

        userObject = model;
        initializeActionsAndCookies();
    }

    /**
     * Indicates whether this node can be copied hence can be dragged.
     * 
     * @return true if this node can be copied or dragged
     */
    public boolean canCopy() {
        return false;
    }

    /**
     * Indicates whether this node can be copied hence can be dragged.
     * 
     * @return true if node can be copied or dragged
     */
    public boolean canCut() {
        return false;
    }

    /**
     * Indicates whether this node can be renamed.
     * 
     * @return true if renameable via UI; false otherwise
     */
    public boolean canRename() {
        return false;
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

        FlatfileNode aNode = (FlatfileNode) o;
        switch (type) {
            case DATABASE:
                if (aNode.type == DATABASE) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == TABLE) {
                    return -1;
                } else if (aNode.type == COLUMN) {
                    return -1;
                } else {
                    throw new ClassCastException("Cannot compare between unrecognized FlatfileNode types.");
                }

            case TABLE:
                if (aNode.type == TABLE) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == COLUMN) {
                    return -1;
                } else {
                    throw new ClassCastException("Cannot compare between unrecognized FlatfileNode types.");
                }

            case COLUMN:
                if (aNode.type == COLUMN) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == TABLE) {
                    return 1;
                } else {
                    throw new ClassCastException("Cannot compare between unrecognized FlatfileNode types.");
                }

            default:
                throw new ClassCastException("Cannot compare between unrecognized FlatfileNode types.");
        }
    }

    /**
     * Gets all available system actions for this node.
     * 
     * @return array of system actions
     */
    public SystemAction[] getActions() {
        return mActions;
    }

    /**
     * Overrides default implementation to return null for this node. This prevents the
     * default NetBeans property panel from appearing.
     * 
     * @return null; no actions are associated with flatfile nodes.
     */
    public SystemAction getDefaultAction() {
        return null;
    }

    /**
     * @see org.openide.nodes.AbstractNode#getHelpCtx
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * Gets user object associated with this node.
     * 
     * @return user object
     */
    public Object getUserObject() {
        return userObject;
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
     * Overrides parent implementation.
     * 
     * @param newName new display name.
     */
    public void setDisplayName(String newName) {
        setName(newName);
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
     * Overrides parent implementation.
     * 
     * @param newName new table name.
     */
    public void setName(String newName) {
        // switch (type) {
        // case DATABASE:
        // ((FlatfileDatabase) userObject).setName(newName);
        // break;
        //
        // case COLUMN:
        // ((FlatfileColumn) userObject).setName(newName);
        // break;
        //
        // case TABLE:
        // ((FlatfileTable) userObject).setName(newName);
        // break;
        //
        // default:
        // break;
        // }

        super.setName(newName);
    }

    /**
     * Sets whether this node is selected.
     * 
     * @param isSelected sets to true if node is selected, false otherwise
     */
    public void setSelected(boolean isSelected) {
        if (type == COLUMN) {
            // ((FlatfileColumn) userObject).setSelected(isSelected);
        }
        selected = isSelected;
    }

    /**
     * Sets user object associated with this node.
     * 
     * @param obj -
     */
    public void setUserObject(Object obj) {
        userObject = obj;
    }

    /**
     * Updates underlying user object with properties of this node.
     */
    public void updateUserObject() {
        // XXX Update user object.
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "Current state of user object: " + userObject);
    }

    /**
     * Returns FlatfileChildren child nodes for this node.
     * 
     * @return FlatfileChildren
     */
    protected FlatfileChildren getFlatfileChildren() {
        return (FlatfileChildren) getChildren();
    }

    private int compareDisplayNames(Node first, Node second) {
        String firstDisplayName = (first.getDisplayName() != null) ? first.getDisplayName() : "";

        String secondDisplayName = (second.getDisplayName() != null) ? second.getDisplayName() : "";

        return firstDisplayName.compareTo(secondDisplayName);
    }

    // // RECOMMENDED - handle cloning specially
    // //(so as not to invoke the overhead of FilterNode):
    //    
    // public Node cloneNode() {
    // // Try to pass in similar constructor params to what you originally got:
    // return new OTDNode();
    // }
    //     
    //
    // // Permit user to customize whole node at once (instead of per-property):
    //    
    // public boolean hasCustomizer() {
    // return true;
    // }
    // public Component getCustomizer() {
    // return new MyCustomizingPanel(this);
    // }
    //     
    //
    // // Permit node to be reordered (you may also want to put
    // // MoveUpAction and MoveDownAction on the subnodes, if you can,
    // // but ReorderAction on the parent is enough):
    //    
    // private class ReorderMe extends Index.Support {
    //     
    // public Node[] getNodes() {
    // return OTDNode.this.getChildren().getNodes();
    // }
    //     
    // public int getNodesCount() {
    // return getNodes().length;
    // }
    //     
    // // This assumes that there is exactly one child node per key.
    // // If you are using e.g. Children.Array, you can use shortcut
    // implementations
    // // of the Index cookie.
    // public void reorder(int[] perm) {
    // // Remember: {2, 0, 1} cycles three items forwards.
    // List old = OTDNode.this.getFlatfileChildren().myKeys;
    // if (list.size() != perm.length) throw new IllegalArgumentException();
    // List nue = new ArrayList(perm.length);
    // for (int i = 0; i < perm.length; i++)
    // nue.set(i, old.get(perm[i]));
    // OTDNode.this.getFlatfileChildren().setKeys(nue);
    // }
    //     
    // }

    /*
     * Initializes Action and Cookies @param nodeType nodeType
     */
    private void initializeActionsAndCookies() {
        mActions = new SystemAction[] {};
    }
}

