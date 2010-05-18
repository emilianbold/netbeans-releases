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

package com.sun.rave.designtime;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import com.sun.rave.designtime.event.DesignBeanListener;

/**
 * <P>A DesignBean represents an instance of a JavaBean class at design-time.  There is one
 * DesignBean instance 'wrapping' each instance of a component class in a bean design tool. All
 * access to properties and events should be done via the DesignBean interface at design-time, so
 * that the tool is able to track changes and persist them.  Think of the "DesignBean" as the
 * design-time proxy for an instance of a JavaBean.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignBean {

    /**
     * Returns the BeanInfo descriptor for this bean instance's type.
     *
     * @return The BeanInfo descriptor for this bean instance's type.
     */
    public BeanInfo getBeanInfo();

    /**
     * Returns the DesignInfo instance for this bean instance.
     *
     * @return The DesignInfo instance for this bean instance.
     */
    public DesignInfo getDesignInfo();

    /**
     * Returns the instance that this DesignBean is marshalling.
     *
     * @return The instance of the wrapped bean instance.
     */
    public Object getInstance();

    /**
     * Returns the instance name of this bean - as declared in source code.
     *
     * @return The source code instance name of this bean.
     */
    public String getInstanceName();

    /**
     * Returns <code>true</code> if this instance can be renamed via this interface.
     *
     * @return <code>true</code> if this instance can be renamed via this interface, or
     *         <code>false</code> if not.
     */
    public boolean canSetInstanceName();

    /**
     * Renames the instance variable for this bean instance in the source code.  If successful,
     * this method returns <code>true</code>, if there is a problem, including the existance of a
     * duplicate instance variable name, this method returns <code>false</code>.
     *
     * @param name The desired source code instance name for this bean.
     * @return <code>true</code> if the rename was successful, or <code>false</code> if not.
     */
    public boolean setInstanceName(String name);

    /**
     * Renames the instance variable for this bean instance in the source code, and appends an
     * auto-incremented number.  For example:  setInstanceName("button", true) --> button1 -->
     * button2 --> button3, etc.  If successful, this method returns <code>true</code>, if there is
     * a problem, this method returns <code>false</code>.
     *
     * @param name The desired source code instance name (base) for this bean.
     * @param autoNumber <code>true</code> to auto-number the instance name, <code>false</code> to
     *        strictly attempt the specified name.
     * @return <code>true</code> if the rename was successful, or <code>false</code> if not.
     */
    public boolean setInstanceName(String name, boolean autoNumber);

    /**
     * Returns the DesignContext that 'owns' this bean instance.
     *
     * @return The DesignContext 'owner' of this bean instance.
     */
    public DesignContext getDesignContext();

    /**
     * Returns the DesignBean parent of this bean instance, or null if this is a top-level bean.
     *
     * @return The DesignBean parent of this bean instance, or null if this is a top-
     *         level bean.
     */
    public DesignBean getBeanParent();

    /**
     * Returns an array of DesignProperty objects representing the properties of this DesignBean.
     *
     * @return An array of DesignProperty objects representing the properties of this DesignBean.
     */
    public DesignProperty[] getProperties();

    /**
     * Returns a single DesignProperty object representing the specified property (by name).
     *
     * @param propertyName The name of the desired DesignProperty to retrieve.
     * @return The DesignProperty representing the desired property, or null if the specified
     *         property does not exist in this DesignBean.
     */
    public DesignProperty getProperty(String propertyName);

    /**
     * Returns a single DesignProperty object representing the specified property (by descriptor).
     *
     * @param property The PropertyDescriptor of the desired DesignProperty to retrieve.
     * @return The DesignProperty representing the desired property, or null if the specified
     *         property does not exist in this DesignBean.
     */
    public DesignProperty getProperty(PropertyDescriptor property);

    /**
     * Returns an array of DesignEvent objects representing the events of this DesignBean.
     *
     * @return An array of DesignEvent objecst representing the events of this DesignBean.
     */
    public DesignEvent[] getEvents();

    /**
     * Returns the DesignEvent objects for a particular event set.
     *
     * @param eventSet The EventSetDescriptor containing the desired events.
     * @return An array of DesignEvent objects representing the events contained in the specified
     *         event set.
     */
    public DesignEvent[] getEvents(EventSetDescriptor eventSet);

    /**
     * Returns the DesignEvent from within the specified event set and having the specified
     * MethodDescriptor.
     *
     * @param eventSet The desired EventSetDescriptor
     * @param event The desired MethodDescriptor
     * @return The DesignEvent representing the event desired, or null if none matched criteria
     */
    public DesignEvent getEvent(EventSetDescriptor eventSet, MethodDescriptor event);

    /**
     * Returns a DesignEvent with the specified EventDescriptor.
     *
     * @param event The desired event's EventDescriptor
     * @return The DesignEvent representing the event desired, or null if none matched criteria
     */
    public DesignEvent getEvent(EventDescriptor event);

    /**
     * Returns <code>true</code> if this DesignBean can be a logical container for other
     * DesignBeans, or <code>false</code> if not.  For example, if a DesignBean is representing a
     * HtmlCommandButton instance, it will return <code>false</code> from this method, whereas a
     * DesignBean representing an HtmlDataTable will return <code>true</code>.  You can only add
     * children to a DesignBean that returns <code>true</code> from this method.
     *
     * @return <code>true</code> if this DesignBean is a container, and <code>false</code> if it is
     *         not
     */
    public boolean isContainer();

    /**
     * Returns the count of child DesignBeans contained in this DesignBean.  Children are "logical"
     * children in that they represent the sub-components contained inside of another component in
     * the markup (JSP) or containership hiearchy.
     *
     * @return The count of DesignBean children contained by this DesignBean
     */
    public int getChildBeanCount();

    /**
     * Returns the child DesignBean at the specified cardinal index (zero-based).
     *
     * @param index The zero-based cardinal index for the desired DesignBean child
     * @return the DesignBean at the specified index
     */
    public DesignBean getChildBean(int index);

    /**
     * Returns an array of DesignBean children of this DesignBean
     *
     * @return An array of DesignBean children of this DesignBean
     */
    public DesignBean[] getChildBeans();

    /**
     * Adds a DesignBeanListener event listener to this DesignBean
     *
     * @param beanListener the event listener to add
     */
    public void addDesignBeanListener(DesignBeanListener beanListener);

    /**
     * Removes a DesignBeanListener event listener from this DesignBean
     *
     * @param beanListener the event listener to remove
     */
    public void removeDesignBeanListener(DesignBeanListener beanListener);

    /**
     * Returns an array of DesignBeanListener currently listening to this DesignBean
     * @return An array of DesignBeanListener currently listening to this DesignBean
     */
    public DesignBeanListener[] getDesignBeanListeners();
}
