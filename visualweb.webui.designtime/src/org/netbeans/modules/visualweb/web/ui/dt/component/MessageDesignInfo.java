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
import com.sun.rave.designtime.Result;
import com.sun.rave.web.ui.component.Message;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import javax.faces.component.EditableValueHolder;

/**
 * DesignInfo for {@link org.netbeans.modules.visualweb.web.ui.dt.component.Message} component.
 *
 * @author gjmurphy
 */
public class MessageDesignInfo extends AbstractDesignInfo {

    /** Creates a new instance of MessageDesignInfo. */
    public MessageDesignInfo() {
        super(Message.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        bean.getProperty("showSummary").setValue(Boolean.TRUE);
        bean.getProperty("showDetail").setValue(Boolean.FALSE);
        return Result.SUCCESS;
    }

    /**
     * Returns true if source bean implements EditaleValueHolder.
     *
     * @param targetBean Target <code>Label</code> bean
     * @param sourceBean Source bean (or <code>null</code>)
     * @param sourceClass Class of source object being dropped
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean,
                                Class sourceClass) {
        return (super.acceptLink(targetBean, sourceBean, sourceClass) ||
                EditableValueHolder.class.isAssignableFrom(sourceClass));
    }


    /** If a component that implements <code>EditableValueHolder</code> is
     * linked to us, update our <code>for</code> property such that it contains
     * the component's id. Links from all other types of components are treated
     * as no-ops.
     *
     * @param targetBean Target <code>Label</code> bean
     * @param sourceBean Source bean (or <code>null</code>)
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {

        if (!EditableValueHolder.class.isAssignableFrom(sourceBean.getInstance().getClass()))
            return super.linkBeans(targetBean, sourceBean);

        DesignProperty forProperty = targetBean.getProperty("for"); //NOI18N
        if (forProperty == null)
            return Result.FAILURE;
        forProperty.setValue(sourceBean.getProperty("id").getValue()); //NOI18N
        return Result.SUCCESS;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return false;
    }

    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        return null;
    }

}
