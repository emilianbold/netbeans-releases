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
import javax.faces.component.html.HtmlSelectOneListbox;

/**
 * DesignInfo for the "drop down"
 * XXX - copy paste some relevant javadoc comments for the main
 * methods which are being over written here, until I learn the
 * stuff well
 */
public class HtmlSelectOneListboxDesignInfo extends HtmlSelectDesignInfoBase {

    public Class getBeanClass() { return HtmlSelectOneListbox.class; }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignProperty sizeProp = bean.getProperty("size"); //NOI18N
        if (sizeProp != null) {
            sizeProp.setValue(new Integer(5));
        }
        return selectOneBeanCreated(bean);
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        modifyVirtualFormsOnBeanDeletedCleanup(bean);
        return selectOneBeanDeleted(bean);
    }

    public Result beanPastedSetup(DesignBean bean) {
        return selectOneBeanPasted(bean);
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return selectOneGetContextItems(bean);
    }

    /**
     * This method is called when an object from a design surface or palette has been dropped 'on' a
     * JavaBean type handled by this DesignInfo (to establish a link). This method will not be
     * called unless the corresponding 'acceptLink' method call returned true. Typically, this
     * results in property settings on potentially both of the DesignBean objects.
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {

        try {
            if (canLinkConverterOrValidatorBeans(targetBean, sourceBean)) {
                linkConverterOrValidatorBeans(targetBean, sourceBean);
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.FAILURE;
        }
        return selectOneLinkBeans(targetBean, sourceBean);
    }
}
