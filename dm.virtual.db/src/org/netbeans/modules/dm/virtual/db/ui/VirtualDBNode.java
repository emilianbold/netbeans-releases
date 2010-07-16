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
package org.netbeans.modules.dm.virtual.db.ui;

import java.beans.IntrospectionException;

import org.netbeans.modules.dm.virtual.db.ui.model.VirtualColumn;
import org.netbeans.modules.dm.virtual.db.ui.model.VirtualDatabase;
import org.netbeans.modules.dm.virtual.db.ui.model.VirtualTable;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * NetBeans node extension that represents a virtual db column for purposes of configuring
 * its properties in the Virtual Database wizard.
 * 
 * @author Ahimanikya Satapathy
 */
public class VirtualDBNode extends BeanNode implements Comparable {

    private static Logger mLogger = Logger.getLogger(VirtualDBNode.class.getName());

    public static final class Leaf extends VirtualDBNode {

        public Leaf() throws IntrospectionException {
            super(Children.LEAF);
        }

        public Leaf(Object model) throws IntrospectionException {
            super(model, Children.LEAF);
        }
    }
    private static final int COLUMN = 102;
    private static final int DATABASE = 100;
    private static final int TABLE = 101;
    private boolean enabled = true;
    private SystemAction[] mActions;
    private boolean selected = true;
    private int type;
    private Object userObject;

    public VirtualDBNode(Object model) throws IntrospectionException {
        this(model, new VirtualDBChildren());
    }

    public VirtualDBNode(Object model, Children children) throws IntrospectionException {
        super(model, children);

        if (model instanceof VirtualDatabase) {
            type = DATABASE;
            super.setName(((VirtualDatabase) model).getName());
            setIconBaseWithExtension("org/netbeans/modules/dm/virtual/db/ui/resource/images/root.png");
        } else if (model instanceof VirtualTable) {
            type = TABLE;
            super.setName(((VirtualTable) model).getTableName());
            setIconBaseWithExtension("org/netbeans/modules/dm/virtual/db/ui/resource/images/Table.gif");
        } else if (model instanceof VirtualColumn) {
            type = COLUMN;
            VirtualColumn column = (VirtualColumn) model;
            super.setName(column.getName());
            if (column.isNullable()) {
                setIconBaseWithExtension("org/netbeans/modules/dm/virtual/db/ui/resource/images/Column.gif");
            } else {
                setIconBaseWithExtension("org/netbeans/modules/dm/virtual/db/ui/resource/images/ColumnNotNull.gif");
            }
        } else {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBNode.class, "MSG_Unrecognized_Modeltype"));
        }

        super.setDisplayName(getName());

        userObject = model;
        initializeActionsAndCookies();
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    public int compareTo(Object o) {
        if (o == this) {
            return 0;
        } else if (o == null) {
            return -1;
        }

        VirtualDBNode aNode = (VirtualDBNode) o;
        switch (type) {
            case DATABASE:
                if (aNode.type == DATABASE) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == TABLE) {
                    return -1;
                } else if (aNode.type == COLUMN) {
                    return -1;
                } else {
                    throw new ClassCastException(NbBundle.getMessage(VirtualDBNode.class, "MSG_Unrecognized_FFTypes"));
                }

            case TABLE:
                if (aNode.type == TABLE) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == COLUMN) {
                    return -1;
                } else {
                    throw new ClassCastException(NbBundle.getMessage(VirtualDBNode.class, "MSG_Unrecognized_FFTypes"));
                }

            case COLUMN:
                if (aNode.type == COLUMN) {
                    return compareDisplayNames(this, aNode);
                } else if (aNode.type == TABLE) {
                    return 1;
                } else {
                    throw new ClassCastException(NbBundle.getMessage(VirtualDBNode.class, "MSG_Unrecognized_FFTypes"));
                }

            default:
                throw new ClassCastException(NbBundle.getMessage(VirtualDBNode.class, "MSG_Unrecognized_FFTypes"));
        }
    }

    @Override
    public SystemAction[] getActions() {
        return mActions;
    }

    @Override
    public SystemAction getDefaultAction() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    public Object getUserObject() {
        return userObject;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setDisplayName(String newName) {
        setName(newName);
    }

    public void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

    @Override
    public void setName(String newName) {
        super.setName(newName);
    }

    public void setSelected(boolean isSelected) {
        selected = isSelected;
    }

    public void setUserObject(Object obj) {
        userObject = obj;
    }

    public void updateUserObject() {
        // XXX Update user object.
        mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDBNode.class, "LOG_ObjectState", userObject));
    }

    protected VirtualDBChildren getVirtualDBChildren() {
        return (VirtualDBChildren) getChildren();
    }

    private int compareDisplayNames(Node first, Node second) {
        String firstDisplayName = (first.getDisplayName() != null) ? first.getDisplayName() : "";

        String secondDisplayName = (second.getDisplayName() != null) ? second.getDisplayName() : "";

        return firstDisplayName.compareTo(secondDisplayName);
    }

    private void initializeActionsAndCookies() {
        mActions = new SystemAction[]{};
    }
}

