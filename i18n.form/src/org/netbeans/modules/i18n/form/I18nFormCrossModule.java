/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n.form;

import org.netbeans.modules.form.FormPropertyEditorManager;
import org.netbeans.modules.i18n.I18nSupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;

/** 
 * Installation class for i18n to form cross dependency module.
 * It registers <code>FormI18nStringEditor</code> to form property editors
 * and <code>FormI18nSupport.Factory</code> to i18n module.
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

    /**
     * Used for layer registration. (to keep correct classloader?)
     * @param fo .settings looks for this signature
     */
    public static I18nSupport.Factory createFactory(FileObject fo) {
        return new FormI18nSupport.Factory();
    }
}
