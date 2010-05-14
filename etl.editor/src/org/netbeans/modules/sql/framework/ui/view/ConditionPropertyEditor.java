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

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditorSupport;
import java.util.List;

import java.util.logging.Level;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.editor.property.IProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.openide.awt.StatusDisplayer;

/**
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ConditionPropertyEditor extends PropertyEditorSupport implements IPropertyEditor {

    //private static transient final Logger mLogger = Logger.getLogger(ConditionPropertyEditor.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConditionPropertyEditor.class.getName());

    public static class Validation extends ConditionPropertyEditor {

        public Validation(IGraphViewContainer editor, SQLDBTable table) {
            super(editor, table);
        }

        protected Component getCustomEditorForSource() {
            return ConditionBuilderUtil.getValidationConditionBuilderView((SourceTable) table, editor);
        }

        protected Component getCustomEditorForTarget() {
            throw new UnsupportedOperationException("Validation Condition is not supported for target table");
        }
    }

    public static class OuterFilter extends ConditionPropertyEditor {

        public OuterFilter(IGraphViewContainer editor, SQLDBTable table) {
            super(editor, table);
        }

        protected Component getCustomEditorForSource() {
            throw new UnsupportedOperationException("Validation Condition is not supported for target table");
        }

        protected Component getCustomEditorForTarget() {
            return ConditionBuilderUtil.getFilterConditionBuilderView((TargetTable) table, editor);
        }
    }

    public static class JoinConditionEditor extends ConditionPropertyEditor {

        private SQLJoinOperator joinOp;

        public JoinConditionEditor(IGraphViewContainer editor, SQLJoinOperator joinOp) {
            super(editor, null);
            this.joinOp = joinOp;
        }

        public Component getCustomEditor() {
            return ConditionBuilderUtil.getConditionBuilderView(joinOp, editor);
        }

        protected Component getCustomEditorForSource() {
            throw new UnsupportedOperationException("not supported ");
        }

        protected Component getCustomEditorForTarget() {
            throw new UnsupportedOperationException("not supported ");
        }
    }

    /* log4j logger category */
    private static final String LOG_CATEGORY = ConditionPropertyEditor.class.getName();
    protected ConditionBuilderView cView;
    protected IGraphViewContainer editor;
    protected SQLDBTable table;
    private SQLCondition conditionContainer;
    private PropertyChangeSupport iPropertyChange = new PropertyChangeSupport(this);
    private IProperty property;

    /** Creates a new instance of ConditionPropertyEditor */
    public ConditionPropertyEditor(IGraphViewContainer editor, SQLDBTable table) {
        this.editor = editor;
        this.table = table;
    }

    /**
     * Register a listener for the PropertyChange event. The class will fire a
     * PropertyChange value whenever the value is updated.
     * 
     * @param listener An object to be invoked when a PropertyChange event is fired.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        iPropertyChange.addPropertyChangeListener(listener);
    }

    public String getAsText() {
        String sql = "";
        String text = conditionContainer != null ? conditionContainer.getConditionText() : null;

        return text != null ? text : sql;
    }

    public Component getCustomEditor() {
        if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
            return getCustomEditorForTarget();
        }
        return getCustomEditorForSource();
    }

    public String getJavaInitializationString() {
        return super.getJavaInitializationString();
    }

    public IProperty getProperty() {
        return property;
    }

    public Object getValue() {
        return this.conditionContainer;
    }

    /**
     * Remove a listener for the PropertyChange event.
     * 
     * @param listener The PropertyChange listener to be removed.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        iPropertyChange.removePropertyChangeListener(listener);
    }

    /**
     * Sets the property value by parsing a given String. May raise
     * java.lang.IllegalArgumentException if either the String is badly formatted or if
     * this kind of property can't be expressed as text.
     * 
     * @param text The string to be parsed.
     */
    public void setAsText(String text) {
        String oldText = this.conditionContainer.getConditionText();
        if (this.conditionContainer != null && text != null && !text.equals(oldText)) {
            try {
                this.conditionContainer = (SQLCondition) conditionContainer.cloneSQLObject();
            } catch (CloneNotSupportedException ex) {
                String msg = mLoc.t("EDIT196: error cloning the condition {0}", LOG_CATEGORY);
                StatusDisplayer.getDefault().setStatusText(msg.substring(15) + ex.getMessage());
                logger.log(Level.SEVERE, mLoc.t("EDIT196: error cloning the condition {0}", LOG_CATEGORY) + ex);
                return;
            }

            this.conditionContainer.setConditionText(text);
            SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition((SQLObject) conditionContainer.getParent());
            try {
                SQLObject obj = ConditionUtil.parseCondition(text, def);
                conditionContainer.removeAllObjects();
                ConditionUtil.populateCondition(conditionContainer, obj);
                // if we do not get a predicate then the condition is invalid
                // and if text is not empty string
                if (!(obj instanceof SQLPredicate) && !text.trim().equals("")) {
                    warnForInvalidCondition();
                }
            } catch (Exception ex) {
                String msg = mLoc.t("EDIT197: Error finding root predicate from text condition{0}", text);
                StatusDisplayer.getDefault().setStatusText(msg.substring(15) + ex.getMessage());
                logger.log(Level.SEVERE, mLoc.t("EDIT197: Error finding root predicate from text condition{0}", text) + ex);
                warnForInvalidCondition();
            }

            // if user modified text then change the gui mode
            this.conditionContainer.setGuiMode(SQLCondition.GUIMODE_SQLCODE);
            try {
                if (this.property != null) {
                    this.property.setValue(this.conditionContainer);
                }
            } catch (Exception ex) {
                String msg = mLoc.t("EDIT198: Error occurred in setting the property value for condition{0}from joinview table.", text);
                StatusDisplayer.getDefault().setStatusText(msg.substring(15) + ex.getMessage());
                logger.log(Level.SEVERE, mLoc.t("EDIT198: Error occurred in setting the property value for condition{0}from joinview table.", text) + ex);
            }
        }
    }

    public void setProperty(IProperty property) {
        this.property = property;
    }

    public void setValue(Object value) {
        this.conditionContainer = (SQLCondition) value;
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    protected void addRuntimeInput(List tables) {
        // add the runtime arguments also.
        RuntimeInput rInput = getRuntimeInput();
        if (rInput != null) {
            tables.add(rInput);
        }
    }

    protected Component getCustomEditorForSource() {
        return ConditionBuilderUtil.getConditionBuilderView((SourceTable) table, editor);
    }

    protected Component getCustomEditorForTarget() {
        return ConditionBuilderUtil.getJoinConditionBuilderView((TargetTable) table, editor);
    }

    protected RuntimeInput getRuntimeInput() {
        SQLDefinition sqlDefinition = SQLObjectUtil.getAncestralSQLDefinition(table);
        if (sqlDefinition != null) {
            RuntimeDatabaseModel runModel = sqlDefinition.getRuntimeDbModel();
            if (runModel != null) {
                RuntimeInput rInput = runModel.getRuntimeInput();
                return rInput;
            }
        }

        return null;
    }

    private void warnForInvalidCondition() {
        String nbBundle1 = mLoc.t("BUND484: The condition is not valid.Make sure you correct it.");
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(nbBundle1.substring(15),
                NotifyDescriptor.WARNING_MESSAGE));
    }
}
