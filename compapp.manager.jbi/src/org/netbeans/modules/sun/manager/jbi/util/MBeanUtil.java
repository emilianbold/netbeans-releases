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
package org.netbeans.modules.sun.manager.jbi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
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
