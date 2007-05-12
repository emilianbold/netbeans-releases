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

package com.sun.rave.faces.data;

import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;

public class DataValueBinder implements RowSetColumnBindable, RowSetListener, ValueChangeListener {

    protected RowSet rowSet;
    public void setBoundRowSet(RowSet rowSet) {
        dataUnbind();
        this.rowSet = rowSet;
        dataBind();
    }

    public RowSet getBoundRowSet() {
        return rowSet;
    }

    protected ColumnBinding boundColumn;
    public void setBoundColumn(ColumnBinding column) {
        dataUnbind();
        this.boundColumn = column;
        dataBind();
    }

    public ColumnBinding getBoundColumn() {
        return boundColumn;
    }

    protected ValueHolder valueHolder;
    public void setValueHolder(ValueHolder valueHolder) {
        dataUnbind();
        this.valueHolder = valueHolder;
        dataBind();
    }

    public ValueHolder getValueHolder() {
        return valueHolder;
    }

    protected void dataBind() {
        if (rowSet != null && boundColumn != null && valueHolder != null) {
            rowSet.addRowSetListener(this);
        }
        if (valueHolder instanceof UIInput) {
            ((UIInput)valueHolder).addValueChangeListener(this);
        }
    }

    protected void dataUnbind() {
        if (rowSet != null && boundColumn != null) {
            rowSet.removeRowSetListener(this);
        }
        if (valueHolder instanceof UIInput) {
            ((UIInput)valueHolder).removeValueChangeListener(this);
        }
    }

    // RowSetListener methods
    public void rowSetChanged(RowSetEvent event) {
        cursorMoved(event);
    }

    public void rowChanged(RowSetEvent event) {
        cursorMoved(event);
    }

    public void cursorMoved(RowSetEvent event) {
        if (rowSet != null && boundColumn != null && valueHolder != null) {
            try {
                valueHolder.setValue(rowSet.getObject(boundColumn.getColumnName()));
            } catch (Exception x) {
                // do nothing for now...
            }
        }
    }

    public PhaseId getPhaseId() {
        return PhaseId.INVOKE_APPLICATION;
    }

    public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
        if (rowSet != null && boundColumn != null && valueHolder != null) {
            try {
                rowSet.updateObject(boundColumn.getColumnName(), valueHolder.getValue());
            } catch (Exception x) {
                // do nothing for now...
            }
        }
    }
}
