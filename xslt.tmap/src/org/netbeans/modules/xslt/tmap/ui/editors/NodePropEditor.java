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
package org.netbeans.modules.xslt.tmap.ui.editors;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * It works as a part of the CustomEditorProperty.
 * @author Vitaly Bychkov
 * @author nk160297
 */
public class NodePropEditor extends PropertyEditorSupport
        implements ExPropertyEditor, Reusable {
    
    private PropertyEnv myPropertyEnv = null;
    private NodePropertyCustomizer npCustomizer = null;
    
    public NodePropEditor() {
    }
    
    public String getAsText() {
        return "";
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        // do nothing
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        if (npCustomizer == null) {
            npCustomizer = new NodePropertyCustomizer(myPropertyEnv);
        }
        //
        return npCustomizer;
    }
    
    public void attachEnv(PropertyEnv newPropertyEnv) {
        myPropertyEnv = newPropertyEnv;
    }
    
}
