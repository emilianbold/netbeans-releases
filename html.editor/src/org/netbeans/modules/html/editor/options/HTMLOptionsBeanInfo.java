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

package org.netbeans.modules.html.editor.options;

import java.beans.*;
import java.util.MissingResourceException;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class HTMLOptionsBeanInfo extends BaseOptionsBeanInfo {

    private static final String[] EXPERT_PROP_NAMES = new String[] {
        HTMLOptions.COMPLETION_INSTANT_SUBSTITUTION_PROP,
        HTMLOptions.COMPLETION_LOWER_CASE_PROP,
        HTMLOptions.JAVADOC_AUTO_POPUP_PROP,
        HTMLOptions.JAVADOC_PREFERRED_SIZE_PROP,
        HTMLOptions.JAVADOC_BGCOLOR,
        HTMLOptions.CODE_FOLDING_UPDATE_TIMEOUT_PROP
    };

    public HTMLOptionsBeanInfo() {
        super("/org/netbeans/modules/html/editor/resources/htmlOptions"); // NOI18N
    }

    protected String[] getPropNames() {
        return HTMLOptions.HTML_PROP_NAMES;
    }

    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();
        setExpert(EXPERT_PROP_NAMES);
    }    

    protected Class getBeanClass() {
        return HTMLOptions.class;
    }

    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(HTMLOptionsBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
