/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
