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
package org.netbeans.modules.visualweb.faces.dt.component;


import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.faces.component.EncodingComponent;


/**
 * <p>Design time information for this component.</p>
 */

public class EncodingComponentDesignInfo implements DesignInfo {


    // ---------------------------------------------------- DesignInfo Methods


    /**
     * <p>Return the bean class for which this <code>DesignInfo</code>
     * instance provides design time information.</p>
     */
    public Class getBeanClass() {

        return EncodingComponent.class;

    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    /**
     * <p>Set up the initial state of a newly created bean.</p>
     *
     * @param bean <code>DesignBean</code> wrapping the newly created
     *  bean instance
     */
    public Result beanCreatedSetup(DesignBean bean) {

    // Set the default value binding expression for the "value" property
    bean.getProperty("value").setValueSource
        ("#{ApplicationBean1.localeCharacterEncoding}");          //NOI18N
    return Result.SUCCESS;

    }


    public Result beanPastedSetup(DesignBean bean) { return Result.SUCCESS; }

    /**
     * <p>Clean up before a bean is deleted.</p>
     *
     * @param bean <code>DesignBean</code> wrapping the bean instance
     *  about to be deleted
     */
    public Result beanDeletedCleanup(DesignBean bean) {

    return Result.SUCCESS;

    }


    /**
     * <p>Return a list or hierarchy of items to be included in a context menu
     * for this bean at design time.</p>
     *
     * @param bean <code>DesignBean</code> wrapping the bean instance for which
     *  to return context menu items
     */
    public DisplayAction[] getContextItems(DesignBean bean) {

    return new DisplayAction[0];

    }


    /**
     * <p>Return <code>true</code> if instances of the specified type may be
     * dropped on the specified target bean.</p>
     *
     * @param targetBean <code>DesignBean</code> wrapping the target bean instance
     * @param sourceBean <code>DesignBean</code> wrapping the source bean instance (may be null)
     * @param sourceClass <code>Class</code> of the potential drop instance
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {

    return false;

    }


    /**
     * <p>Process the drop of the specified source bean on the specified
     * target bean.</p>
     *
     * @param target <code>DesignBean</code> wrapping the target bean instance
     * @param source <code>DesignBean</code> wrapping the source bean instance
     */
    public Result linkBeans(DesignBean target, DesignBean source) {

    return Result.SUCCESS;

    }

    // ------------------------------------------------ DesignBeanListener Methods

    public void beanContextActivated(DesignBean bean) {}

    public void beanContextDeactivated(DesignBean bean) {}

    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}

    /**
     * <p>Process a bean changed event.</p>
     *
     * @param bean <code>DesignBean</code> wrapping the bean that changed
     */
    public void beanChanged(DesignBean bean) { }


    /**
     * <p>Process an event changed event.</p>
     *
     * @param event <code>DesignEvent</code> wrapping the event that changed
     */
    public void eventChanged(DesignEvent event) { }


    /**
     * <p>Process a property changed event.</p>
     *
     * @param prop <code>DesignProperty</code> wrapping the property that changed
     * @param oldValue the old value of the property (may be null)
     */
    public void propertyChanged(DesignProperty prop, Object oldValue) { }


}
