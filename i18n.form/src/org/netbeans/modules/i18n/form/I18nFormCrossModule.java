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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n.form;

import org.netbeans.modules.form.FormPropertyEditorManager;
import org.netbeans.modules.i18n.I18nSupport;
import org.openide.modules.ModuleInstall;

/**
 * Installation class for i18n to form cross dependency module.
 * It registers <code>FormI18nStringEditor</code> to form property editors.
 *
 * @author Peter Zavadsky
 */
public class I18nFormCrossModule extends ModuleInstall {

    /** Registers property editor in form module and factory in i18n module. */
    public void restored() {
        registerFormPropertyEditor();
    }
    
    /** Registers <code>FormI18nStringEditor</code> form property editor to form module. */
    private void registerFormPropertyEditor() {
        Class newEditorClass = FormI18nStringEditor.class;
        Class newEditorClassInteger = FormI18nIntegerEditor.class;
        Class newEditorClassMnemonic = FormI18nMnemonicEditor.class;
              
        // Register new property editor.
        FormPropertyEditorManager.registerEditor (String.class, newEditorClass);
        FormPropertyEditorManager.registerEditor (int.class, newEditorClassInteger);
        FormPropertyEditorManager.registerEditor (int.class, newEditorClassMnemonic);
    }

}
