/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.editor.options;

import java.beans.*;
import java.awt.Image;
import java.util.MissingResourceException;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import org.openide.util.NbBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class JavaOptionsBeanInfo extends BaseOptionsBeanInfo {

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;
    /** Additional beaninfo */
    private static BeanInfo[] additional;

    private static final String[] EXPERT_PROP_NAMES = new String[] {
        JavaOptions.JAVADOC_BGCOLOR,
        JavaOptions.JAVADOC_AUTO_POPUP_DELAY_PROP,
        JavaOptions.JAVADOC_PREFERRED_SIZE_PROP,
        JavaOptions.JAVADOC_AUTO_POPUP_PROP,
        JavaOptions.COMPLETION_CASE_SENSITIVE_PROP,
        JavaOptions.SHOW_DEPRECATED_MEMBERS_PROP,
        JavaOptions.COMPLETION_INSTANT_SUBSTITUTION_PROP,
        JavaOptions.COMPLETION_NATURAL_SORT_PROP,
        JavaOptions.FAST_IMPORT_PACKAGE_PROP,
	JavaOptions.PAIR_CHARACTERS_COMPLETION
            };

    
    public JavaOptionsBeanInfo() {
        super("/org/netbeans/modules/java/editor/resources/javaOptions"); // NOI18N

    }

    protected String[] getPropNames() {
        return JavaOptions.JAVA_PROP_NAMES;
    }

    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();

        setExpert(EXPERT_PROP_NAMES);
        setHidden(new String[] {
            JavaOptions.FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
            JavaOptions.FORMAT_COMPOUND_BRACKET_ADD_NL_PROP,
            JavaOptions.FAST_IMPORT_PACKAGE_PROP,
            JavaOptions.GOTO_CLASS_CASE_SENSITIVE_PROP,
            JavaOptions.GOTO_CLASS_SHOW_INNER_CLASSES_PROP,
            JavaOptions.GOTO_CLASS_SHOW_LIBRARY_CLASSES_PROP
        });
        setPropertyEditor(BaseOptions.CODE_FOLDING_PROPS_PROP, CodeFoldingEditor.class, false);
    }

    protected Class getBeanClass() {
        return JavaOptions.class;
    }

    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(JavaOptionsBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
