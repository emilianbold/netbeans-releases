/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.editor.options;

import java.beans.*;
import java.util.MissingResourceException;
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

    protected @Override String[] getPropNames() {
        return JavaOptions.JAVA_PROP_NAMES;
    }

    protected @Override void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();

        setExpert(EXPERT_PROP_NAMES);
        String hidden[] = (usesNewOptions()) ?
            new String[] {
                JavaOptions.COMPLETION_AUTO_POPUP_DELAY_PROP,
                JavaOptions.COMPLETION_AUTO_POPUP_PROP,
                JavaOptions.COMPLETION_CASE_SENSITIVE_PROP,
                JavaOptions.COMPLETION_INSTANT_SUBSTITUTION_PROP,
                JavaOptions.COMPLETION_NATURAL_SORT_PROP,
                JavaOptions.FAST_IMPORT_PACKAGE_PROP,
                JavaOptions.FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
                JavaOptions.FORMAT_COMPOUND_BRACKET_ADD_NL_PROP,
                JavaOptions.GOTO_CLASS_CASE_SENSITIVE_PROP,
                JavaOptions.GOTO_CLASS_SHOW_INNER_CLASSES_PROP,
                JavaOptions.GOTO_CLASS_SHOW_LIBRARY_CLASSES_PROP,
                JavaOptions.INDENT_ENGINE_PROP,
                JavaOptions.SHOW_DEPRECATED_MEMBERS_PROP,
        } : new String [] {
                JavaOptions.FAST_IMPORT_PACKAGE_PROP,
                JavaOptions.FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
                JavaOptions.FORMAT_COMPOUND_BRACKET_ADD_NL_PROP,
                JavaOptions.GOTO_CLASS_CASE_SENSITIVE_PROP,
                JavaOptions.GOTO_CLASS_SHOW_INNER_CLASSES_PROP,
                JavaOptions.GOTO_CLASS_SHOW_LIBRARY_CLASSES_PROP,
                JavaOptions.INDENT_ENGINE_PROP,
        };
        
        setHidden(hidden);
        setPropertyEditor(BaseOptions.CODE_FOLDING_PROPS_PROP, CodeFoldingEditor.class, false);
    }

    protected @Override Class getBeanClass() {
        return JavaOptions.class;
    }

    /**
     * Get localized string
     */
    protected @Override String getString(String key) {
        try {
            return NbBundle.getMessage(JavaOptionsBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
