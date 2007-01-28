/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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