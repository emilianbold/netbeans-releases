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
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.faces.application.Application;
import javax.faces.el.ValueBinding;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import com.sun.rave.designtime.faces.FacesBindingPropertyEditor;

public class ValueBindingPropertyEditor extends PropertyEditorSupport implements
    FacesBindingPropertyEditor {

    //------------------------------------------------------------------------------- PropertyEditor

    public Object getValue() {
        Object value = super.getValue();
        //System.err.println("VBPE.getValue value:" + value + "        " + System.currentTimeMillis());
        return value;
    }

    public void setValue(Object value) {
        Object v = getValue();
//        if ((value == v) ||
//            (value != null && value.equals(v)) ||
//            (value instanceof ValueBinding && ((ValueBinding)value).getExpressionString().equals(v)) ||
//            (v instanceof ValueBinding && ((ValueBinding)v).getExpressionString().equals(value)) ||
//            (value instanceof ValueBinding && v instanceof ValueBinding &&
//                ((ValueBinding)value).getExpressionString().equals(((ValueBinding)v).getExpressionString()))) {
//            return;
//        }
        //System.err.println("VBPE.setValue value:" + value + "        " + System.currentTimeMillis());
        this.quiet = true;
        if (facesDesignProperty != null && facesDesignProperty.isBound()) {
            super.setValue(facesDesignProperty.getValueBinding());
        } else {
            super.setValue(value);
        }
        this.quiet = false;
    }

    protected void superSetValue(Object value) {

        super.setValue(value);
    }

    protected boolean quiet = false;
    public void firePropertyChange() {
        if (quiet) {
            return;
        }
        super.firePropertyChange();
    }

    public String getAsText() {
        Object value = getValue();
        if (value instanceof ValueBinding) {
            return ((ValueBinding)value).getExpressionString();
        } else if (facesDesignProperty != null && facesDesignProperty.isBound()) {
            return facesDesignProperty.getValueBinding().getExpressionString();
        }
        return value != null ? value.toString() : ""; //NOI18N
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text.startsWith("#{")) { //NOI18N
            FacesDesignContext fctx = (FacesDesignContext)liveProperty.getDesignBean().getDesignContext();
            Application app = fctx.getFacesContext().getApplication();
            super.setValue(app.createValueBinding(text));
        } else if (text.length() > 0) {
            super.setValue(text);
        } else {
            super.setValue(null);
        }
    }

    public String getJavaInitializationString() {
        return "\"" + getAsText() + "\""; //NOI18N
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public Component getCustomEditor() {
        ValueBindingPanel vbp = new ValueBindingPanel(this, liveProperty);
        return vbp;
    }

    //--------------------------------------------------------------------------- PropertyEditor2

    // use only for reference and lookup

    protected FacesDesignProperty facesDesignProperty;
    protected DesignProperty liveProperty;

    public void setDesignProperty(DesignProperty lp) {
        this.liveProperty = lp;
        this.facesDesignProperty = lp instanceof FacesDesignProperty ? (FacesDesignProperty)lp : null;
    }
}
