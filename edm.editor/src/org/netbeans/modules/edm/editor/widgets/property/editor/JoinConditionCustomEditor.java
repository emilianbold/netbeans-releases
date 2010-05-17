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
package org.netbeans.modules.edm.editor.widgets.property.editor;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.windows.WindowManager;
import org.openide.nodes.Node;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.widgets.property.JoinNode;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderView;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author Nithya
 */
public class JoinConditionCustomEditor implements ExPropertyEditor {

    private PropertyEnv env;
    private MashupDataObject mObj;
    private SQLJoinOperator joinOp;
    private ConditionBuilderView conditionView;
    private PropertyChangeSupport support;
    private Dialog dialog;
    private DialogDescriptor dd;
    private static final Logger mLogger = Logger.getLogger(JoinConditionCustomEditor.class.getName());

    public JoinConditionCustomEditor() {
        super();
        support = new PropertyChangeSupport(this);
    }

    /**
     * Describe <code>supportsCustomEditor</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

    public Object getValue() {
        return getAsText();
    }

    public void setValue(Object value) {
        if (mObj == null || joinOp == null) {
            initializeDataObject();
        }
        support.firePropertyChange("", null, null);
    }

    public String getAsText() {
        if (mObj == null || joinOp == null) {
            initializeDataObject();
        }
        return this.joinOp.getJoinCondition().getConditionText(true);
    }

    public void setAsText(String text) {
        setValue(text);
    }

    public Component getCustomEditor() {
        if (mObj == null || joinOp == null) {
            initializeDataObject();
        }
        dd = new DialogDescriptor(conditionView,
                NbBundle.getMessage(JoinConditionCustomEditor.class, "MSG_Edit_Join_Condition"), true,
                NotifyDescriptor.OK_CANCEL_OPTION, null, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (dd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                    SQLCondition cond = (SQLCondition) conditionView.getPropertyValue();
                    if (cond != null) {
                        SQLCondition oldCondition = joinOp.getJoinCondition();
                        if (joinOp != null && !cond.equals(oldCondition)) {
                            joinOp.setJoinCondition(cond);
                            joinOp.setJoinConditionType(SQLJoinOperator.USER_DEFINED_CONDITION);
                            setAsText(joinOp.getJoinCondition().getConditionText(true));
                            //mObj.getMashupDataEditorSupport().synchDocument();
                            mObj.getModel().setDirty(true);
                            mObj.setModified(true);
                        }
                    }
                    // This is a hack to close the window. Find a better way to do.
                    dialog.dispose();
                }
            }
        });
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        return dialog;
    }

    /** Gets java initialization string. Implements <code>PropertyEditor</code>
     * interface.
     * @return <code>null</code> */
    public String getJavaInitializationString() {
        return null; // no code generation
    }

    /** Gets tags. Implements <code>PropertyEditor</code> interface.
     * @return <code>null</code> */
    public String[] getTags() {
        return null;
    }

    /** Indicates wheter this editor paints itself the value. Implements
     * <code>PropertyEditor</code> interface.
     * @return <code>null</code> */
    public boolean isPaintable() {
        return false;
    }

    /** Dummy implementation of <code>PropertyEditor</code> interface method.
     * @see #isPaintable */
    public void paintValue(Graphics g, Rectangle rectangle) {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void initializeDataObject() {
        Node[] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        for (Node node : nodes) {
            if (node instanceof JoinNode) {
                this.joinOp = ((JoinNode) node).getJoinOperator();
                this.mObj = ((JoinNode) node).getMashupDataObject();
                break;
            }
        }
        conditionView = new ConditionBuilderView(mObj.getEditorView().getCollabSQLUIModel(),
                joinOp.getAllSourceTables(), joinOp.getJoinCondition(),
                IOperatorXmlInfoModel.CATEGORY_FILTER);

    }
}