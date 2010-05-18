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
