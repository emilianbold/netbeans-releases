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
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicCustomizer2;

import com.sun.rave.propertyeditors.PropertyPanelBase;
import com.sun.rave.propertyeditors.UrlPropertyPanel;

//!CQ TODO: rename this to UrlCustomizer
public class URLCustomizer extends BasicCustomizer2 {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(URLCustomizer.class);

    public URLCustomizer() {
        this(bundle.getMessage("urlCustTitle")); //NOI18N
    }

    private URLCustomizer(String title) {
        super(null, title, null, null);
    }

    protected DesignProperty designProperty;
    protected UrlPropertyPanel panel;

    public boolean isApplyCapable() {
        return true;
    }

    public Result applyChanges() {
        if (designProperty == null)
            return Result.FAILURE;
        try {
            String url = (String) panel.getPropertyValue();
            designProperty.setValue(url);
            return Result.SUCCESS;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return Result.FAILURE;
    }

    public Component getCustomizerPanel(DesignBean bean) {
        this.designProperty = bean.getProperty("value"); //NOI18N
        this.panel = (UrlPropertyPanel) PropertyPanelBase.createPropertyPanel(designProperty);
        return panel;
    }
}
