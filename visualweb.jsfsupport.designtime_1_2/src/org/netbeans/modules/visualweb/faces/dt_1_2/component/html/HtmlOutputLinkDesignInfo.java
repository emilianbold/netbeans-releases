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
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;

public class HtmlOutputLinkDesignInfo extends HtmlDesignInfoBase implements DesignInfo {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlOutputLinkDesignInfo.class);

    public Class getBeanClass() {
        return HtmlOutputLink.class;
    }

    private static String defaultLink = System.getProperty("rave.defaultLinkUrl");

    public Result beanCreatedSetup(DesignBean bean) {
        try {
            DesignBean output = bean.getDesignContext().createBean(HtmlOutputText.class.getName(), bean, null);
            output.setInstanceName(bean.getInstanceName() + "Text", true);  //NOI18N
            output.getProperty("value").setValue(bundle.getMessage("hlink"));  //NOI18N
            bean.getProperty("value").setValue(defaultLink != null ? defaultLink : "http://www.sun.com/jscreator");  //NOI18N
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }
}
