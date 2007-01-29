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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.web.ui.component.Calendar;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;

/**
 * DesignInfo for {@link org.netbeans.modules.visualweb.web.ui.dt.component.Calendar}.
 *
 * @author gjmurphy
 */
public class CalendarDesignInfo extends EditableValueHolderDesignInfo {

    public CalendarDesignInfo() {
        super(Calendar.class);
    }

    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        // Specify the default binding property
        return targetBean.getProperty("selectedDate"); //NOI18N;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return false;
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        if (Converter.class.isAssignableFrom(sourceClass) || Validator.class.isAssignableFrom(sourceClass))
            return false;
        return super.acceptLink(targetBean, sourceBean, sourceClass);
    }
}
