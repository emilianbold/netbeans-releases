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
package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.rave.designtime.*;
import javax.faces.convert.NumberConverter;

public class NumberConverterDesignInfo implements DesignInfo {
    public Class getBeanClass() {
        return NumberConverter.class;
    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public Result beanCreatedSetup(DesignBean bean) {

        // Here are the defautls the NumberConverter has:
        // - min integer digits to 1
        // - max integer digits to 40
        // - min fractional digites to 0
        // - max fractional digits to 3
        bean.getProperty( "minIntegerDigits").setValue( new Integer(1) );
        bean.getProperty( "maxIntegerDigits").setValue( new Integer(40) );
        bean.getProperty( "minFractionDigits").setValue( new Integer(0) );
        bean.getProperty( "maxFractionDigits").setValue( new Integer(3) );

        return new CustomizerResult(bean, new NumberConverterCustomizer());
    }

    public Result beanPastedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[] { new NumberConverterCustomizerAction(bean) };
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        return null;
    }

    public void beanContextActivated(DesignBean bean) {}
    public void beanContextDeactivated(DesignBean bean) {}
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}
    public void beanChanged(DesignBean bean) {}
    public void propertyChanged(DesignProperty prop, Object oldValue) {}
    public void eventChanged(DesignEvent event) {}
}
