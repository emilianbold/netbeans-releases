/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.generator;

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.Import;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;

/**
 *
 *  Add notifications to an MBean code generator class
 */
public class AddNotifGenerator
{   
    // MBeanNotificationInfo instantiation pattern
    // {0} = notification type
    // {1} = notification class
    // {2} = notification description
    private static final String MBEAN_NOTIF_INFO_PATTERN = 
      "      new MBeanNotificationInfo(new String[] '{'\n" + // NOI18N
      "             {0}'}',\n" + // NOI18N
      "             {1}.class.getName(),\n" + // NOI18N
      "             \"{2}\")"; // NOI18N
    
    private static final String BODY_COMMENT = 
            "//TODO add your code here\n"; // NOI18N
    
    /**
     * Entry point to generate NotificationEmitter implementation for MBean.
     * @param mbeanClass <CODE>JavaClass</CODE> the MBean class to update
     * @param mbeanRes <CODE>Resource</CODE> represents MBean class
     * @param notifs <CODE>MBeanNotification[]</CODE> notifications of this MBean
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     */
    public void update(JavaClass mbeanClass, Resource mbeanRes, MBeanNotification[] notifs,
            boolean genBroadcastDeleg, boolean genSeqNumber)
           throws java.io.IOException, Exception
    {
        boolean rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if (!WizardHelpers.getPackageName(mbeanClass.getName()).equals("")) // NOI18N
                MBeanFileGenerator.addManagementImport(mbeanRes);
            addNotificationEmitter(mbeanClass);
            addAddNotifListMethod(mbeanClass,genBroadcastDeleg);
            addGetNotifInfoMethod(mbeanClass,notifs);
            addRemoveNotifListMethod1Param(mbeanClass,genBroadcastDeleg);
            addRemoveNotifListMethod(mbeanClass,genBroadcastDeleg);
            if (genSeqNumber)
                addGetSeqNumberMethod(mbeanClass);
            addFields(mbeanClass,genBroadcastDeleg,genSeqNumber);
            addNotifTypes(mbeanClass,notifs);
        }  catch (Exception e) {
            rollback = true;
            e.printStackTrace();
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        
        rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if (WizardHelpers.getPackageName(mbeanClass.getName()).equals("")) // NOI18N
                MBeanFileGenerator.addManagementImport(mbeanRes);
        } catch (Exception e) {
            rollback = true;
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
    }
    
    private void addAddNotifListMethod(JavaClass tgtClass, 
            boolean genBroadcastDeleg) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "broadcaster.addNotificationListener(listener, filter, handback);\n"; // NOI18N
        if (!genBroadcastDeleg)
            methodBody = BODY_COMMENT;
        
        ArrayList params = new ArrayList();
        Parameter listener = pkg.getParameter().createParameter("listener", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "NotificationListener"), // NOI18N
                0, // dimCount
                false);
        params.add(listener);
        //param.setType(paramType);
        Parameter filter = pkg.getParameter().createParameter("filter", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "NotificationFilter"), // NOI18N
                0, // dimCount
                false);
        params.add(filter);
        Parameter handback = pkg.getParameter().createParameter("handback", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "Object"), // NOI18N
                0, // dimCount
                false);
        params.add(handback);
        
        ArrayList exceptions = new ArrayList();
        exceptions.add(MBeanFileGenerator.getTypeRef(pkg, "IllegalArgumentException")); // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "addNotificationListener", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                "MBeanNotification support\nYou shouldn't update these methods", // NOI18N
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                exceptions, // exceptions
                pkg.getMultipartId().createMultipartId("void", null, Collections.EMPTY_LIST), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }   
    
    private void addGetNotifInfoMethod(JavaClass tgtClass, MBeanNotification[] notifs) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        // remove getNotificationInfo() if it already exists
        List params = new ArrayList();
        Method getNotifInfo = tgtClass.getMethod("getNotificationInfo", params,false); // NOI18N 
        if (getNotifInfo != null)
            getNotifInfo.refDelete();
        
        StringBuffer methodBody = new StringBuffer();
        methodBody.append("return new MBeanNotificationInfo[] {\n"); // NOI18N
        MessageFormat notifInfo = new MessageFormat(MBEAN_NOTIF_INFO_PATTERN);
        
        int notifTypeIndex = 0;
        for (int i = 0; i < notifs.length; i ++) {
            StringBuffer notifType = new StringBuffer();
            for (int j = 0 ; j < notifs[i].getNotificationTypeCount() ; j++) {
                if (!notifs[i].getNotificationClass().equals(
                        WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                    notifType.append("NOTIF_TYPE_" + notifTypeIndex); // NOI18N
                    notifTypeIndex++;
                } else {
                    notifType.append(WizardConstants.ATTRIBUTECHANGE_TYPE);
                }
                if (j < notifs[i].getNotificationTypeCount() - 1) {
                    notifType.append(",\n             "); // NOI18N
                }
            }
            Object[] notifArguments = { notifType.toString(),
                        notifs[i].getNotificationClass(),
                        notifs[i].getNotificationDescription() };
            methodBody.append(notifInfo.format(notifArguments));
            if ((notifs.length > 1) && (i < (notifs.length - 1))) {
                methodBody.append(","); // NOI18N
            }
            methodBody.append("\n"); // NOI18N
        }
        methodBody.append("};\n"); // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "getNotificationInfo", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                null, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody.toString(), // string body
                Collections.EMPTY_LIST, // type params
                Collections.EMPTY_LIST, // parameters
                Collections.EMPTY_LIST, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "MBeanNotificationInfo[]"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }
    
    private void addRemoveNotifListMethod1Param(JavaClass tgtClass, 
            boolean genBroadcastDeleg) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "broadcaster.removeNotificationListener(listener);\n"; // NOI18N
        if (!genBroadcastDeleg)
            methodBody = BODY_COMMENT;
        
        ArrayList params = new ArrayList();
        Parameter listener = pkg.getParameter().createParameter("listener", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "NotificationListener"), // NOI18N
                0, // dimCount
                false);
        params.add(listener);
        
        ArrayList exceptions = new ArrayList();
        exceptions.add(MBeanFileGenerator.getTypeRef(pkg, "ListenerNotFoundException")); // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "removeNotificationListener", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                null, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                exceptions, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "void"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }
    
    private void addRemoveNotifListMethod(JavaClass tgtClass, 
            boolean genBroadcastDeleg) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "broadcaster.removeNotificationListener(listener, filter, handback);\n"; // NOI18N
        if (!genBroadcastDeleg)
            methodBody = BODY_COMMENT;
        
        ArrayList params = new ArrayList();
        Parameter listener = pkg.getParameter().createParameter("listener", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "NotificationListener"), // NOI18N
                0, // dimCount
                false);
        params.add(listener);
        Parameter filter = pkg.getParameter().createParameter("filter", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "NotificationFilter"), // NOI18N
                0, // dimCount
                false);
        params.add(filter);
        Parameter handback = pkg.getParameter().createParameter("handback", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "Object"), // NOI18N
                0, // dimCount
                false);
        params.add(handback);
        
        ArrayList exceptions = new ArrayList();
        exceptions.add(MBeanFileGenerator.getTypeRef(pkg, "ListenerNotFoundException")); // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "removeNotificationListener", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                null, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                exceptions, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "void"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }
    
    private void addGetSeqNumberMethod(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody = "return seqNumber++;\n"; // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "getNextSeqNumber", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PRIVATE | Modifier.SYNCHRONIZED,
                null, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                Collections.EMPTY_LIST, // parameters
                Collections.EMPTY_LIST, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "long"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }
    
    private static void addFields(JavaClass tgtClass, boolean genBroadcastDeleg,
            boolean genSeqNumber) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        if (genSeqNumber) {
            Field seqNumber = pkg.getField().createField("seqNumber", Collections.EMPTY_LIST, // NOI18N
                    Modifier.PRIVATE, null, null, false,
                    pkg.getMultipartId().createMultipartId("long", null, Collections.EMPTY_LIST), // NOI18N
                    0, null, null);
            tgtClass.getFeatures().add(seqNumber);
        }
        
        if (genBroadcastDeleg) {
            Field broadcaster = pkg.getField().createField("broadcaster", Collections.EMPTY_LIST, // NOI18N
                    Modifier.PRIVATE | Modifier.FINAL, null, null, true,
                    pkg.getMultipartId().createMultipartId(
                    "NotificationBroadcasterSupport", null, Collections.EMPTY_LIST), // NOI18N
                    0, null, "new NotificationBroadcasterSupport()"); // NOI18N
            tgtClass.getFeatures().add(broadcaster);
        }
    }
    
    private void addNotifTypes(JavaClass tgtClass, MBeanNotification[] notifs) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        String comments = 
                "Notification types definitions. To use when creating JMX Notifications."; // NOI18N
        int notifTypeIndex = 0;
        for (int i = 0; i < notifs.length; i ++) {
            if (!notifs[i].getNotificationClass().equals(
                    WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                for (int j = 0; j < notifs[i].getNotificationTypeCount(); j++) {
                    Field notifType = pkg.getField().createField(
                            "NOTIF_TYPE_" + notifTypeIndex, // NOI18N
                            Collections.EMPTY_LIST,
                            Modifier.STATIC | Modifier.PRIVATE,
                            notifTypeIndex == 0 ? comments : null,
                            null,
                            true,
                            MBeanFileGenerator.getTypeRef(pkg, "String"), // NOI18N
                            0,
                            null,
                            "\"" + notifs[i].getNotificationType(j).getNotificationType() + "\""); // NOI18N
                    tgtClass.getFeatures().add(notifType);
                    notifTypeIndex++;
                }
            }
        }
    }
    
    private static void addNotificationEmitter(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        tgtClass.getInterfaceNames().add(pkg.getMultipartId().createMultipartId(
                    "NotificationEmitter", // NOI18N
                    null,
                    Collections.EMPTY_LIST));
    }
    
}
