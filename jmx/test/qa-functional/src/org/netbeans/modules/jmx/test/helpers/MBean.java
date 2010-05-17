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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.test.helpers;

import java.util.ArrayList;

/**
 * Used to check MBean wizard values.
 */
public class MBean {

    private String mBeanName = null;
    private String mBeanType = null;
    private String mBeanPackage = null;
    private String mBeanDescription = null;
    private ArrayList<Attribute> mBeanAttributes = null;
    private ArrayList<Operation> mBeanOperations = null;
    private ArrayList<Notification> mBeanNotifications = null;
    private String classToWrap = null;
    private boolean objectWrappedAsMXBean = false;
    
    /** MBean constructor which is wrapper **/
    public MBean(
            String mBeanName,
            String mBeanType,
            String mBeanPackage,
            String mBeanDescription,
            String classToWrap,
            boolean objectWrappedAsMXBean,
            ArrayList<Attribute> mBeanAttributes,
            ArrayList<Operation> mBeanOperations,
            ArrayList<Notification> mBeanNotifications) {
        this.mBeanName = mBeanName;
        this.mBeanType = mBeanType;
        this.mBeanPackage = mBeanPackage;
        this.mBeanDescription = mBeanDescription;
        this.objectWrappedAsMXBean = objectWrappedAsMXBean;
        this.mBeanAttributes = mBeanAttributes;
        this.mBeanOperations = mBeanOperations;
        this.mBeanNotifications = mBeanNotifications;
        this.classToWrap = classToWrap;
    }
    
    /**
     * Returns the name of the MBean
     * @return mBeanName the name of the MBean
     */
    public String getName() {
        return mBeanName;
    }
    
    /**
     * Returns the type of the MBean
     * @return mBeanType the type of the MBean
     */
    public String getType() {
        return mBeanType;
    }
    
    /**
     * Returns the package of the MBean
     * @return mBeanPackage the package of the MBean
     */
    public String getPackage() {
        return mBeanPackage;
    }
    
    /**
     * Returns the description for the MBean
     * @return mBeanDescription the description of the MBean
     */
    public String getDescription() {
        return mBeanDescription;
    }
    
    /**
     * Returns the class to wrap for the MBean
     * @return classToWrap of the MBean
     */
    public String getClassToWrap() {
        return classToWrap;
    }
    
    /**
     * Returns true if the MBean class to wrap is not null
     * @return true if the MBean class to wrap is not null
     */
    public boolean isWrapped() {
        return (classToWrap == null ? false : true);
    }
    
    /**
     * Returns true if the MBean is wrapped as an MXBean
     * @return true if the MBean is wrapped as an MXBean
     */
    public boolean isObjectWrappedAsMXBean() {
        return objectWrappedAsMXBean;
    }
    
    /**
     * Returns the attribute list for the MBean
     * @return ArrayList<Attribute> the attribute list of the MBean
     */
    public ArrayList<Attribute> getAttributes() {
        return mBeanAttributes;
    }
    
    /**
     * Returns an attribute for the MBean
     * @param index index of the attribute to return
     * @return mBeanAttributes the attribute at index of the MBean
     */
    public Attribute getAttribute(int index) {
        return mBeanAttributes.get(index);
    }
    
    /**
     * Returns the operation list for the MBean
     * @return ArrayList<Attribute> the operation list of the MBean
     */
    public ArrayList<Operation> getOperations() {
        return mBeanOperations;
    }
    
    /**
     * Returns an operation for the MBean
     * @param index index of the operation to return
     * @return mBeanOperations the operation at index of the MBean
     */
    public Operation getOperation(int index) {
        return mBeanOperations.get(index);
    }
    
    /**
     * Returns the notification list for the MBean
     * @return ArrayList<Notification> the notification list of the MBean
     */
    public ArrayList<Notification> getNotifications() {
        return mBeanNotifications;
    }
    
    /**
     * Returns a notification for the MBean
     * @param index index of the notification to return
     * @return mBeanNotifications the notification at index of the MBean
     */
    public Notification getNotification(int index) {
        return mBeanNotifications.get(index);
    }
    
    /**
     * Returns the number of attributes of the MBean
     * @return mBeanAttributes the number of attributes of the MBean
     */
    public int getNumberOfAttributes() {
        if (mBeanAttributes != null)
            return mBeanAttributes.size();
        else
            return -1;
    }
    
    /**
     * Returns the number of operations of the MBean
     * @return mBeanOperations the number of operations of the MBean
     */
    public int getNumberOfOperations() {
        if (mBeanOperations != null)
            return mBeanOperations.size();
        else
            return -1;
    }
    
    /**
     * Returns the number of notifications of the MBean
     * @return mBeanNotifications the number of notifications of the MBean
     */
    public int getNumberOfNotifications() {
        if (mBeanNotifications != null)
            return mBeanNotifications.size();
        else
            return -1;
    }
}
