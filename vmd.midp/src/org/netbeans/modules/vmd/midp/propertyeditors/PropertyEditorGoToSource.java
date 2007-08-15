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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.Component;
import java.lang.ref.WeakReference;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorGoToSource extends DesignPropertyEditor {
    
    private String GO_TO_SOURCE_TEXT = NbBundle.getMessage(GoToSourceCPE.class, "LBL_GoToSourcePropertyText"); //NOI18N
    
    private WeakReference<DesignComponent> component;
    private GoToSourceCPE customPropertyEditor;

    private PropertyEditorGoToSource() {
    }

    public static PropertyEditorGoToSource createInstance() {
        return new PropertyEditorGoToSource();
    }

    @Override
    public Boolean canEditAsText() {
        return false;
    }

    @Override
    public Component getCustomEditor() {
        if (customPropertyEditor == null) {
            customPropertyEditor = new GoToSourceCPE(component);
        }
        return customPropertyEditor;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public void init(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public boolean supportsDefaultValue() {
        return false;
    }

    @Override
    public String getAsText() {
        return GO_TO_SOURCE_TEXT; // NOI18N
    }

}
