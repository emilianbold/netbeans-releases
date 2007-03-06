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

package org.netbeans.modules.visualweb.designer.jsf;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.w3c.dom.Element;

/**
 * Utilities class for the JSF support module.
 *
 * @author Peter Zavadsky
 */
public final class JsfSupportUtilities {
    
    /** Creates a new instance of JsfSupportUtilities */
    private JsfSupportUtilities() {
    }

    
    public static String[] getEditableProperties(DesignBean designBean) {
        return HtmlDomProviderServiceImpl.getEditablePropertyNames(designBean);
    }

    public static Designer getDesignerForDesignContext(DesignContext designContext) {
        Designer[] designers = JsfForm.findDesignersForDesignContext(designContext);
        return designers.length > 0 ? designers[0] : null;
    }

    public static Element getComponentRootElementForDesignBean(DesignBean bean) {
        return HtmlDomProviderServiceImpl.getComponentRootElementForDesignBean(bean);
    }
    
}
