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

package org.netbeans.modules.web.core.syntax.settings;

import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.settings.JSPOptions;
import java.beans.*;
import java.util.MissingResourceException;
import org.openide.util.NbBundle;

/** BeanInfo for JSP editor options
 *
 * @author Petr Jiricka, Libor Kramolis
 */
public class JSPOptionsBeanInfo extends org.netbeans.modules.editor.options.BaseOptionsBeanInfo {

     private static final String[] EXPERT_PROP_NAMES = new String[] {
        JSPOptions.JAVADOC_AUTO_POPUP_PROP,
        JSPOptions.JAVADOC_PREFERRED_SIZE_PROP,
        JSPOptions.JAVADOC_BGCOLOR,
        JSPOptions.CODE_FOLDING_UPDATE_TIMEOUT_PROP
            };
    
    public JSPOptionsBeanInfo() {
        super ("/org/netbeans/modules/web/core/syntax/resources/jspOptions"); // NOI18N
    }
    
    protected String[] getPropNames() {
        return JSPOptions.JSP_PROP_NAMES;
    }
    
    protected Class getBeanClass() {
        return JSPOptions.class;
    }
    
    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();
        setExpert(EXPERT_PROP_NAMES);
    }
    
    //Try to find the key value in editor module first, then look to JSP syntax module bundle
    protected String getString(String key) {
        try {
            return super.getString(key);
        }catch(MissingResourceException mre) {
            return NbBundle.getMessage(JSPKit.class, key);
        }
    }
    
}
