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

import org.openide.filesystems.FileObject;
import java.text.MessageFormat;
import org.netbeans.modules.jmx.WizardConstants;

/**
 * Generic MBean File generator.
 * @author thomas
 */
public abstract class MBeanFileGenerator {
  
    /**
     * Returns ObjectName import code.
     * @return <CODE>String</CODE> code to generate
     */
    protected static final String getObjectNameImport() {
        return "import javax.management.ObjectName;\n";
    }
    
    /**
     * Returns Date import code.
     * @return <CODE>String</CODE> code to generate
     */
    protected static final String getDateImport() {
        return "import java.util.Date;\n";
    }
    
    /**
     * Returns Properties import code.
     * @return <CODE>String</CODE> code to generate
     */
    protected static final String getPropertiesImport() {
        return "import java.util.Properties;\n";
    }
    
    /**
     * Returns begin code related to MBean notification.
     * @return <CODE>String</CODE> code to generate
     */
    protected static final String getNotifBegin() { 
        return new String(
      "   /**\n" +
      "    * MBean Notification support\n" +
      "    * You shouldn't update these methods\n" +
      "    */\n"+
      "    // <editor-fold defaultstate=\"collapsed\" desc=\" Generated Code \">\n" +
      "    public void addNotificationListener(NotificationListener listener,\n" +
      "       NotificationFilter filter, Object handback)\n" + 
      "       throws IllegalArgumentException {\n" +
      "         broadcaster.addNotificationListener(listener, filter, handback);\n" +
      "    }\n\n" +
      "    public MBeanNotificationInfo[] getNotificationInfo() {\n" +
      "         return new MBeanNotificationInfo[] {\n");
    }
    
    /**
     * Returns end code related to MBean notification.
     * @param mBeanModel <CODE>MBeanModel</CODE> the MBean to generate
     * @return <CODE>String</CODE> code to generate
     */
    protected static String getNotifEnd(MBeanModel mBeanModel) {
        StringBuffer notifEnd = new StringBuffer();
        notifEnd.append(
      "                };\n" +
      "    }\n\n" +
      "    public void removeNotificationListener(NotificationListener listener)\n" + 
      "       throws ListenerNotFoundException {\n" +
      "         broadcaster.removeNotificationListener(listener);\n" +
      "    }\n\n" +
      "    public void removeNotificationListener(NotificationListener listener,\n" +
      "       NotificationFilter filter, Object handback)\n" + 
      "       throws ListenerNotFoundException {\n" +
      "         broadcaster.removeNotificationListener(listener, filter, handback);\n" +
      "    }\n" +
      "    // </editor-fold> \n\n" +
      "    private synchronized long getNextSeqNumber() {\n" +
      "         return seqNumber++;\n" +
      "    }\n\n" +
      "    private long seqNumber;\n" +
      "    private final NotificationBroadcasterSupport broadcaster =\n" +
      "               new NotificationBroadcasterSupport();\n" +
      "   \n");
        if (mBeanModel.haveNotification()) {
            MessageFormat formNotifTypeField = 
                    new MessageFormat(getNotiftypeFieldPattern());
            int index = 0;
            for (int i = 0; i < mBeanModel.getNbNotifType(); i++) {
                String notifType = mBeanModel.getNotifType(i);
                if (!notifType.equals(WizardConstants.ATTRIBUTECHANGE_TYPE)) {
                    if (index == 0) {
                        notifEnd.append(
      "    /**\n" +
      "     * Notification types definitions. To use when creating JMX Notifications.\n" +
      "     */\n");
                    }
                    Object[] notifTypeArgs = { String.valueOf(index),
                                               notifType };
                    notifEnd.append(
                        formNotifTypeField.format(notifTypeArgs));
                    index++;
                }
            }
        }
        return notifEnd.toString();
    }
    
    /**
     * Returns Code pattern for notification type field.
     * {0} = notification type index
     * {1} = notification type
     * @return <CODE>String</CODE> code pattern to generate
     */
    protected static final String getNotiftypeFieldPattern() {
        return new String(
                "    private static final String NOTIF_TYPE_{0} = \"{1}\";\n");
    }
    
    // MBeanNotificationInfo instantiation pattern
    // {0} = notification type
    // {1} = notification class
    // {2} = notification description
    private static final String MBEAN_NOTIF_INFO_PATTERN = 
      "               new MBeanNotificationInfo(new String[] '{'\n" +
      "                      {0}'}',\n" +
      "                      {1}.class.getName(),\n" +
      "                      \"{2}\")";
    
    /**
     * Generates all the files for the new MBean.
     * @param mBeanModel <CODE>MBeanModel</CODE> the MBean to generate
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return * @return <CODE>FileObject</CODE> the generated file
     */
    public abstract FileObject generateMBean(MBeanModel mBeanModel)
            throws java.io.IOException, Exception;
    
    /**
     * Generates code for one standard mbean notification in Mbean class.
     * @param notifIndex <CODE>int</CODE> MBean notification index
     * @param mBeanModel <CODE>MBeanModel</CODE> the MBean to generate
     */
    protected void genMBeanNotifInfo(int notifIndex,
                        MBeanModel mBeanModel) {
        StringBuffer mBeanClassContent = mBeanModel.getMBeanClassContent();
        MessageFormat notifInfo = 
                new MessageFormat(MBEAN_NOTIF_INFO_PATTERN);
        StringBuffer notifType = new StringBuffer();
        for (int j = 0 ; j < mBeanModel.getNbNotifType(notifIndex) ; j++) {
            if (!mBeanModel.getNotifClass(notifIndex).equals(
                        WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                notifType.append("NOTIF_TYPE_" + 
                        mBeanModel.getNotifTypeIndex(notifIndex,j));
            } else {
                notifType.append(mBeanModel.getNotifType(notifIndex,j));
            }
            if (j < mBeanModel.getNbNotifType(notifIndex) - 1) {
                notifType.append(",\n                      ");
            }
        }
        Object[] notifArguments = { notifType.toString(), 
                mBeanModel.getNotifClass(notifIndex), 
                mBeanModel.getNotifDesc(notifIndex) };
        mBeanClassContent.append(notifInfo.format(notifArguments));
        if ((mBeanModel.getNbNotif() > 1) 
                && (notifIndex < (mBeanModel.getNbNotif() - 1))) {
            mBeanClassContent.append(",");
        }
        newLine(mBeanClassContent);
    }
    
    /**
     * Add the block close code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    protected void closeBloc(StringBuffer sb) {
        sb.append("}\n");
    }
    
    /**
     * Add a new line code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    protected void newLine(StringBuffer sb) {
        sb.append("\n");
    }
    
}
