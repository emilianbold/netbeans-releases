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
package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.swing.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;

public class HtmlOutputTextDesignInfo extends HtmlDesignInfoBase {
    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlOutputTextDesignInfo.class);

    public Class getBeanClass() { return HtmlOutputText.class; }

    public Result beanCreatedSetup(DesignBean bean) {
        /*
        DesignProperty valueProp = bean.getProperty("value"); //NOI18N
        if (valueProp != null) {
            valueProp.setValue(bundle.getMessage("text")); //NOI18N
        }
        */
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        DesignBean p = bean.getBeanParent();
        Object pi = p.getInstance();

// There's an unfortunate dependency on this code in the designer. Please tell Tor if you change this;
// SelectionManager.isFillerOutputText should be updated
        if (pi instanceof HtmlCommandLink ||
            pi instanceof HtmlOutputLabel ||
            pi instanceof HtmlOutputLink) {

            final DesignContext context = bean.getDesignContext();
            final String pin = p.getInstanceName();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DesignBean pb = context.getBeanByName(pin);
                    if (pb != null) {
                        Object pbi = pb.getInstance();
                        if ((pbi instanceof HtmlCommandLink ||
                            pbi instanceof HtmlOutputLabel ||
                            pbi instanceof HtmlOutputLink) &&
                            pb.getChildBeanCount() == 0) {

                            context.deleteBean(pb);
                        }
                    }
                }
            });
        }
        return Result.SUCCESS;
    }
}
