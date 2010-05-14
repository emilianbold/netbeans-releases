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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.xslt.tmap.ui.editors.PortTypePropertyCustomizer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author Vitaly Bychkov
 */
public class PortTypePropEditor extends PropertyEditorSupport
        implements ExPropertyEditor, Reusable {

    private static PortTypePropertyCustomizer customizer = null;
    protected PropertyEnv myPropertyEnv = null;

    /**
     * Allows to use single instance of editor for differen properties
     */
    /** Creates a new instance of PortTypePropEditor */
    public PortTypePropEditor() {
    }

    @Override
    public String getAsText() {
        Object value = super.getValue();
        return value == null ? "" : String.valueOf(value);
    }

    @Override
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        setValue(text);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
//        customizer = PropertyUtils.propertyCustomizerPool.getObjectByClass(PortTypePropertyCustomizer.class);
        customizer = new PortTypePropertyCustomizer(myPropertyEnv);
//        customizer.init(myPropertyEnv);
        return customizer;
    }

    public void attachEnv(PropertyEnv newPropertyEnv) {
        myPropertyEnv = newPropertyEnv;
    }
}
