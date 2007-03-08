/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.MissingResourceException;

import org.openide.util.NbBundle;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;

/** BeanInfo for CC editor options */
public class CCOptionsBeanInfo extends BaseOptionsBeanInfo {

    private static final String[] EXPERT_PROP_NAMES = new String[] {
        CCOptions.COMPLETION_CASE_SENSITIVE_PROP,
        CCOptions.COMPLETION_INSTANT_SUBSTITUTION_PROP,
        CCOptions.JAVADOC_AUTO_POPUP_PROP,
        CCOptions.JAVADOC_AUTO_POPUP_DELAY_PROP,
        CCOptions.JAVADOC_PREFERRED_SIZE_PROP,
        CCOptions.JAVADOC_BGCOLOR,
        CCOptions.CODE_FOLDING_UPDATE_TIMEOUT_PROP
    };
    
    public CCOptionsBeanInfo() {
	super("/org/netbeans/modules/cnd/editor/cplusplus/CCIcon"); //NOI18N
    }
    
    protected String[] getPropNames() {
        // already merged on initialization
        return CCOptions.CC_PROP_NAMES;
    }

    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();
        setExpert(EXPERT_PROP_NAMES);
    }    
    
    protected Class getBeanClass() {
	return CCOptions.class;
    }

    protected String getString(String key) {
        try {
            return NbBundle.getBundle(CCOptionsBeanInfo.class).getString(key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }
}
