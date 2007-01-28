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
/*
 * Created on Jun 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import javax.faces.application.Application;
import com.sun.jsfcl.std.property.LocalizedMessageRuntimeException;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.faces.FacesDesignContext;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ValueBindingOnlyPropertyEditor extends ValueBindingPropertyEditor {

    protected static final ComponentBundle bundle = ComponentBundle.getBundle(ValueBindingPanel.class);

    public Component getCustomEditor() {

        ValueBindingOnlyPanel panel = new ValueBindingOnlyPanel(this, liveProperty);
        return panel;
    }

    /*
     * The only diff with super's version is that if we receive anything but blank and
     * value binding expression, we throw an IllegalArgumentException.
     *  (non-Javadoc)
     * @see java.beans.PropertyEditor#setAsText(java.lang.String)
     */

    public void setAsText(String text) throws IllegalArgumentException {

        verifyString(text);
        if (text.startsWith("#{")) { //NOI18N
            FacesDesignContext fctx = (FacesDesignContext)liveProperty.getDesignBean().getDesignContext();
            Application app = fctx.getFacesContext().getApplication();
            super.setValue(app.createValueBinding(text));
        } else if (text.trim().length() == 0) {
            superSetValue(null);
        }
    }

    public void verifyString(String string) {

        if (string.startsWith("#{")) { //NOI18N
            return;
        } else if (string.trim().length() == 0) {
            return;
        }
        throw new LocalizedMessageRuntimeException(bundle.getMessage("vbExpected", string)); // NOI18N
    }
}
