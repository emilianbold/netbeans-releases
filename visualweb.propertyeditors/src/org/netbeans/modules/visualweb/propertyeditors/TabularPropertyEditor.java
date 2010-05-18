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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.DesignProperty;
import java.awt.Component;
import java.beans.PropertyDescriptor;

/**
 * An editor for list-like or table-like properties. Any property that takes a
 * list or array of objects that have a regular number of fields may be edited
 * using this editor. Implementing classes must provide an implementation of
 * the <code>getRows()</code> and <code>setRows(List)</code> methods, which are
 * called to get and set the property's value as a list of lists (using
 * {@link java.util.List}). In addition, implementing classes must provide
 * the column names and types to be used when representing the tabular data in
 * a table widget.
 *
 * Editing is done through a custom pop-up component, but an in-line, read-only
 * view of the values in the table's default column is also provided. The default
 * column is the first, or whatever column index is returned by the method
 * <code>getDefaultDisplayColumnIndex()</code>.
 *
 * @see TabularPropertyPanel
 * @see com.sun.rave.propertyeditors.TabularPropertyModel
 * @author gjmurphy
 */
public class TabularPropertyEditor extends PropertyEditorBase {
    
    /**
     * Key used to specify a table model within a property descriptor.
     */
    public final static String TABLE_MODEL_CLASS =
            "com.sun.rave.propertyeditors.TABLE_MODEL_CLASS"; //NOI18N
    /**
     * Creates a new instance of TabularPropertyEditor.
     */
    public TabularPropertyEditor() {
    }


    public boolean supportsCustomEditor() {
        return true;
}

    /**
     * Returns the index of the column to use as a default display when the
     * editor is not displaying the full table of values. By default, the
     * first (0th) column is used.
     */
    protected int getTextDisplayColumnIndex() {
        return 0;
    }

    /**
     * Return a table model suitable for displaying the value of this property
     * as a table of columns and rows. By default, looks for a table model class
     * specified as the value of the property descriptor key TABLE_MODEL_CLASS.
     */
    protected TabularPropertyModel getTabularPropertyModel() {
        TabularPropertyModel model = null;
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty != null) {
            PropertyDescriptor propertyDescriptor = designProperty.getPropertyDescriptor();
            model = (TabularPropertyModel) propertyDescriptor.getValue(TABLE_MODEL_CLASS);
        }
        return model;
    }

    /**
     * Returns this editor's custom editor panel, an instance of TabularPropertyPanel.
     * To notify the panel of changes in the tabular property, call
     * <code>((TabularPropertyPanel) this.getCustomEditor()).updateTableData()</code>.
     */
    public Component getCustomEditor() {
        TabularPropertyModel model = this.getTabularPropertyModel();
        model.setValue(this.getValue());
        TabularPropertyPanel panel = new TabularPropertyPanel(model, this);
        return panel;
    }

    /**
     * Returns a string containing the comma-delimited values of the column,
     * the index of which is returned by <code>getTextDisplayColumnIndex()</code>.
     */
    public String getAsText() {
        TabularPropertyModel model = this.getTabularPropertyModel();
        model.setValue(this.getValue());
        int c = this.getTextDisplayColumnIndex();
        int r = model.getRowCount();
        if (r <= 0)
            return "";
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < r; i++) {
            buffer.append(model.getValueAt(i, c).toString());
            buffer.append(", ");
        }
        buffer.setLength(buffer.length() - 2);
        return buffer.toString();
    }

    public void setAsText(String text) throws IllegalArgumentException {
    }

    public String[] getTags() {
        return null;
    }

    public boolean isEditableAsText() {
        return false;
    }

    public void setValue(Object value) {
        super.setValue(value);
    }

    public Object getValue() {
        Object retValue;
        retValue = super.getValue();
        return retValue;
    }
    
}
