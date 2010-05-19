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
package org.netbeans.modules.sun.manager.jbi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class MBeanUtil {

    public ObjectName[] getAllObjectNames(MBeanServerConnection connection) throws Exception {
        Set<ObjectName> set = (Set<ObjectName>) connection.queryNames(null, null);
        return (ObjectName[]) set.toArray(new ObjectName[0]);
    }

    public String[] getAttributeNames(MBeanServerConnection connection, ObjectName objectName) throws Exception {
        Collection<String> list = new ArrayList<String>();
        MBeanInfo info = connection.getMBeanInfo(objectName);
        MBeanAttributeInfo[] attributes = info.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            MBeanAttributeInfo attributeInfo = (MBeanAttributeInfo) attributes[i];
            String attributeName = attributeInfo.getName();
            list.add(attributeName);
        }
        return list.toArray(new String[0]);
    }

    public Object getAttributeValue(MBeanServerConnection connection, ObjectName objectName, String attributeName) throws Exception {
        return connection.getAttribute(objectName, attributeName);
    }

    public Object[] getAttributeValues(MBeanServerConnection connection, ObjectName name, String[] attributeNames) throws Exception {
        Collection<Object> list = new ArrayList<Object>();
        Attribute[] attributes = getAttributes(connection, name, attributeNames);
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            list.add(attribute.getValue());
        }
        return list.toArray(new Object[0]);
    }

    public Attribute[] getAttributes(MBeanServerConnection connection, ObjectName objectName, String[] attributeNames) throws Exception {
        List<Object> list = (List<Object>) connection.getAttributes(objectName, attributeNames);
        return (Attribute[]) list.toArray(new Attribute[0]);
    }

    public void print(MBeanServerConnection connection) throws Exception {
        print(connection, null, null, null);
    }

   public void print(MBeanServerConnection connection, String objectNameFilter, String attributeNameFilter, String attributeValueFilter) throws Exception {
        ObjectName[] objectNames = getAllObjectNames(connection);
        for (int i = 0; i < objectNames.length; i++) {
            ObjectName objectName =  objectNames[i];
            if (objectNameFilter == null || String.valueOf(objectName).indexOf(objectNameFilter)!=-1) {
                String[] attributeNames = getAttributeNames(connection, objectName);
                for (int j = 0; j < attributeNames.length; j++) {
                    String attributeName = attributeNames[j];
                    if (attributeNameFilter == null || attributeName.indexOf(attributeNameFilter)!=-1) {
                        Object attributeValue;
                        try {
                            attributeValue = getAttributeValue(connection, objectName, attributeName);
                        } catch (Exception e) {
                            attributeValue = e.getMessage();
                        }
                        if (attributeValueFilter == null || String.valueOf(attributeValue).indexOf(attributeValueFilter)!=-1) {
                            System.out.println("objectName: " + objectName + " attributeName: " + attributeName + " attributeValue: " + attributeValue);  // NOI18N
                        }
                    }
                }
            }
        }
    }
}
