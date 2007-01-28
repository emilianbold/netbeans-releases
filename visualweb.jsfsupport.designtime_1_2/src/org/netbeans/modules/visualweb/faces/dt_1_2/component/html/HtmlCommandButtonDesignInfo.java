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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.html.HtmlCommandButton;

public class HtmlCommandButtonDesignInfo extends HtmlDesignInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlCommandButtonDesignInfo.class);

    public Class getBeanClass() { return HtmlCommandButton.class; }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignProperty valueProp = bean.getProperty("value"); //NOI18N
        if (valueProp != null) {
            valueProp.setValue(bundle.getMessage("submit")); //NOI18N
        }
        return Result.SUCCESS;
    }
}
