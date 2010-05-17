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


package org.netbeans.modules.visualweb.dataprovider.designtime;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;

/**
 * <p>Convenience base class for <code>DesignInfo</code> implementations
 * that provide design time behavior for JSF components inside Creator.
 * Any <code>DesignInfo</code> implementation that extends this class
 * will receive the default behavior described for each method, unless that
 * method is overridden.</p>
 */
public abstract class AbstractDesignInfo implements DesignInfo {


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a <code>DesignInfo</code> instance for the specified
     * JavaBean class.</p>
     *
     * @param clazz Class of the JavaBean for which this instance is created
     */
    public AbstractDesignInfo(Class clazz) {
        this.beanClass = clazz;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The JavaBean class this <code>DesignInfo</code> instance is
     * designed to wrap.</p>
     */
    private Class beanClass = null;


    // ------------------------------------------------------ DesignInfo Methods


    /**
     * <p>Returns the class type of the JavaBean that was
     * passed to our constructor.</p>
     */
    public Class getBeanClass() {

        return this.beanClass;

    }


    /**
     * <p>Take no action by default.  Return <code>true</code>.</p>
     *
     * {@inheritDoc}
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {

        return true;

    }

    /**
     * <p>Take no action by default.  Return <code>true</code>.</p>
     *
     * {@inheritDoc}
     */
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {

        return true;

    }

    /**
     * <p>Take no action by default.  Return <code>Result.SUCCESS</code>.</p>
     *
     * @param bean The bean that was just created
     */
    public Result beanCreatedSetup(DesignBean bean) {

        return Result.SUCCESS;

    }


    /**
     * <p>Take no action by default.  Return <code>Result.SUCCESS</code>.</p>
     *
     * @param bean The bean that is about to be deleted
     */
    public Result beanDeletedCleanup(DesignBean bean) {

        return Result.SUCCESS;

    }


    /**
     * <p>Take no action by default.  Return <code>Result.SUCCESS</code>.</p>
     *
     * @param bean The bean that has been pasted
     */
    public Result beanPastedSetup(DesignBean bean) {

        return Result.SUCCESS;

    }

    /**
     * <p>Return <code>null</code>, indicating that no context menu items
     * will be provided.</p>
     *
     * @param bean The DesignBean that a user has right-clicked on
     */
    public DisplayAction[] getContextItems(DesignBean bean) {

        return null;

    }


    /**
     * <p>Return <code>false</code> by default.</p>
     *
     * @see #linkBeans
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean,
                                Class sourceClass) {

        return false;

    }


    /**
     * <p>Take no action by default.</p>
     *
     * @param targetBean The target <code>DesignBean</code> instance that the
     *  user has 'dropped' an object onto to establish a link
     * @param sourceBean The <code>DesignBean</code> instance that has
     *  been 'dropped'
     *
     * @see #acceptLink
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {

        return Result.SUCCESS;

    }


    // ---------------------------------------------- DesignBeanListener Methods


    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> whose context has been activated
     */
    public void beanContextActivated(DesignBean bean) {

        ;

    }


    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> whose context has been deactivated
     */
    public void beanContextDeactivated(DesignBean bean) {

        ;

    }


    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> that has been renamed.
     * @param oldInstanceName The prior instance name
     */
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {

        ;

    }

    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> that has changed.
     */
    public void beanChanged(DesignBean bean) {

        ;

    }



    /**
     * <p>Take no action by default.</p>
     *
     * @param event The <code>DesignEvent</code> that has changed.
     */
    public void eventChanged(DesignEvent event) {

        ;

    }



    /**
     * <p>Take no action by default.</p>
     *
     * @param property The <code>DesignProperty</code> that has changed.
     * @param oldValue Optional oldValue, or <code>null</code> if the
     *  previous value is not known
     */
    public void propertyChanged(DesignProperty property, Object oldValue) {

        ;

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Return the <code>BeanDescriptor</code> for the class this
     * <code>DesignInfo</code> is designed to wrap, if possible;
     * otherwise, return <code>null</code>.</p>
     */
    protected BeanDescriptor getBeanDescriptor() {

        try {
            return getBeanInfo().getBeanDescriptor();
        } catch (IntrospectionException e) {
            return null;
        }

    }


    /**
     * <p>Return the <code>BeanInfo</code> for the class this
     * <code>DesignInfo</code> is designed to wrap.</p>
     *
     * @exception IntrospectionException if an error occurs during introspection
     */
    protected BeanInfo getBeanInfo() throws IntrospectionException {

        return Introspector.getBeanInfo(getBeanClass());

    }


    /**
     * <p>Return the <code>PropertyDescriptor</code> for the specified
     * property of the class this <code>DesignInfo</code> is designed
     * to wrap, if possible and if it exists; otherwise, return
     * <code>null</code>.</p>
     */
    protected PropertyDescriptor getPropertyDescriptor(String name) {

        Map map = getPropertyDescriptorMap();
        if (map != null) {
            return (PropertyDescriptor) map.get(name);
        } else {
            return null;
        }

    }


    /**
     * <p>Return an array of <code>PropertyDescriptor</code>s for the class
     * this <code>DesignInfo</code> is designed to wrap, if possible;
     * otherwise, return <code>null</code>.</p>
     */
    protected PropertyDescriptor[] getPropertyDescriptors() {

        try {
            return getBeanInfo().getPropertyDescriptors();
        } catch (IntrospectionException e) {
            return null;
        }

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Cache key for the property descriptor map, cached in the
     * <code>BeanDescriptor</code> on first access.</p>
     */
    private static final String PROPERTY_DESCRIPTOR_MAP =
      "com.sun.data.provider.PROPERTY_DESCRIPTOR_MAP"; //NOI18N


    /**
     * <p>Return the <code>Map</code> of <code>PropertyDescriptor</code>s for
     * the class this <code>DesignInfo</code> is designed to wrap, if
     * possible; otherwise, return <code>null</code>.</p>
     */
    private Map getPropertyDescriptorMap() {

        BeanDescriptor bd = getBeanDescriptor();
        if (bd == null) {
            return null;
        }
        Map map = (Map) bd.getValue(PROPERTY_DESCRIPTOR_MAP);
        if (map == null) {
            PropertyDescriptor pd[] = getPropertyDescriptors();
            if (pd == null) {
                return null;
            }
            map = new HashMap(pd.length);
            for (int i = 0; i < pd.length; i++) {
                map.put(pd[i].getName(), pd[i]);
            }
            bd.setValue(PROPERTY_DESCRIPTOR_MAP, map);
        }
        return map;

    }


}
