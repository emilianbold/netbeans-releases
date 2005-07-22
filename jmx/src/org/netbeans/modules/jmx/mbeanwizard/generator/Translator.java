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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.MBeanNotificationType;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.openide.WizardDescriptor;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 * create a Data Object for an MBean with the informations of the MBean 
 * informations map. 
 * @author thomas
 */
public class Translator {
    
    /**
     * Creates a new instance of MBeanDO with the informations of the MBean
     * informations map. 
     * example of members values :
     * mBeanName = CacheManager
     * mBeanPackageName = com.foo.bar
     * mBeanPackagePath = com/foo/bar
     * mBeanPackDir = 'package com.foo.bar;'
     * srcFilePath = /home/thomas/app/src
     * mBeanFilePath = /home/thomas/app/src/com/foo/bar
     * @param wiz <CODE>TemplateWizard</CODE> a wizard
     */
    public static MBeanDO createMBeanDO(TemplateWizard wiz) {
        MBeanDO mbean = new MBeanDO();
        
        String mBeanName = (String)wiz.getProperty(WizardConstants.PROP_MBEAN_NAME);
        if (mBeanName == null)  
            throw new IllegalArgumentException("MBean Name is null");// NOI18N
        mbean.setName(mBeanName);
        
        String mBeanPackageName = (String)
                wiz.getProperty(WizardConstants.PROP_MBEAN_PACKAGE_NAME);
        if (mBeanPackageName == null)  
            throw new IllegalArgumentException("MBean package name is null");// NOI18N
        mbean.setPackageName(mBeanPackageName);
        
        String projectLocation  = (String)
                wiz.getProperty(WizardConstants.PROP_PROJECT_LOCATION);                
        if (projectLocation == null)
            throw new IllegalArgumentException("project location is null");// NOI18N
        
        String mBeanDesc = (String)
                wiz.getProperty(WizardConstants.PROP_MBEAN_DESCRIPTION);
        if (mBeanDesc == null)
            throw new IllegalArgumentException("description is null");// NOI18N
        mbean.setDescription(mBeanDesc);
        
        String mbeanType = (String)
            wiz.getProperty(WizardConstants.PROP_MBEAN_TYPE);
        if ((mbeanType == null) ||
           ((!WizardConstants.MBEAN_STANDARDMBEAN.equals(mbeanType)) &&
            (!WizardConstants.MBEAN_EXTENDED.equals(mbeanType)) &&
            (!WizardConstants.MBEAN_DYNAMICMBEAN.equals(mbeanType))))
            throw new IllegalArgumentException("Bad MBean Type");// NOI18N
        mbean.setType(mbeanType);
        
        FileObject mbeanFolder = Templates.getTargetFolder(wiz);
        if (mbeanFolder == null)
            throw new IllegalArgumentException("MBeanFolder is null");// NOI18N
        DataFolder mbeanFolderDataObj = DataFolder.findFolder(mbeanFolder);
        mbean.setDataFolder(mbeanFolderDataObj);
        
        FileObject template = Templates.getTemplate( wiz );
        if (template == null)
            throw new IllegalArgumentException("template is null");// NOI18N
        try {
            mbean.setTemplate(DataObject.find( template ));
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        addAttributes(wiz,mbean);
        addOperations(wiz,mbean);
        addNotifications(wiz,mbean);
        
        return mbean;
    }
    
    /**
     * Collects all informations needed for JUnit test generation and returns the 
     * MBeanGenInfo object which represents these informations.
     *
     * example of members values :
     * testClassName = CacheManagerTest
     * testPackageName = com.foo.bar.test
     * mBeanPackagePath = com/foo/bar/test
     * mBeanPackDir = 'package com.foo.bar.test;'
     * testFilePath = /home/thomas/app/src/com/foo/bar/test
     * testMBeanImport = 'import com.foo.bar.CacheManager;'
     */
    public static MBeanGenInfo createGenInfo(WizardDescriptor wiz) {
        MBeanGenInfo mbeanGenInfo = new MBeanGenInfo();
        
        Object genJUnitTestCheck = wiz.getProperty(WizardConstants.PROP_JUNIT_SELECTED);
        boolean genJUnitTest;
        if (genJUnitTestCheck == null)  
            genJUnitTest = false;
        else 
            genJUnitTest = (Boolean) genJUnitTestCheck;
        mbeanGenInfo.setGenJUnit(genJUnitTest);
                
        String testClassName = (String) 
            wiz.getProperty(WizardConstants.PROP_JUNIT_CLASSNAME);
        if (((testClassName == null) || (testClassName.equals(""))) && // NOI18N
                mbeanGenInfo.isGenJUnit())
            throw new IllegalArgumentException("test class name invalid");// NOI18N
        mbeanGenInfo.setTestClassName(testClassName);
        
        String testPackageName = (String) 
            wiz.getProperty(WizardConstants.PROP_JUNIT_PACKAGE);
        if ((testPackageName == null) && mbeanGenInfo.isGenJUnit())
            throw new IllegalArgumentException("test class package or path invalid");// NOI18N
        mbeanGenInfo.setTestPackageName(testPackageName);
        
        String testFilePath = (String)
            wiz.getProperty(WizardConstants.PROP_JUNIT_LOCATION);
        if (((testFilePath == null) || (testFilePath.equals(""))) && genJUnitTest) // NOI18N
            throw new IllegalArgumentException("test file path invalid"); // NOI18N
        mbeanGenInfo.setTestFolderPath(testFilePath);
        
        // tag for generation
        Boolean javadocSelected = (Boolean)
            wiz.getProperty(WizardConstants.PROP_JUNIT_JAVADOC_SELECTED);
        if (javadocSelected == null) 
            mbeanGenInfo.setGenJUnitDoc(false);
        else 
            mbeanGenInfo.setGenJUnitDoc(javadocSelected.booleanValue());
        
        Boolean srcCodeHintsSelected = (Boolean)
            wiz.getProperty(WizardConstants.PROP_JUNIT_HINT_SELECTED);
        if (srcCodeHintsSelected == null) 
            mbeanGenInfo.setGenJUnitSrcCodeHints(false);
        else 
            mbeanGenInfo.setGenJUnitSrcCodeHints(srcCodeHintsSelected.booleanValue());
        
        Boolean defMethSelected = (Boolean)
            wiz.getProperty(WizardConstants.PROP_JUNIT_DEFMETHBODIES_SELECTED);
        if (defMethSelected == null) 
            mbeanGenInfo.setGenJUnitDefMethBod(false);
        else 
            mbeanGenInfo.setGenJUnitDefMethBod(defMethSelected.booleanValue());
        
        Boolean setUpSelected = (Boolean)
            wiz.getProperty(WizardConstants.PROP_JUNIT_SETUP_SELECTED);
        if (setUpSelected == null) 
            mbeanGenInfo.setGenJUnitSetUp(false);
        else 
            mbeanGenInfo.setGenJUnitSetUp(setUpSelected.booleanValue());
        
        Boolean tearDownSelected = (Boolean)
            wiz.getProperty(WizardConstants.PROP_JUNIT_TEARDOWN_SELECTED);
        if (tearDownSelected == null) 
            mbeanGenInfo.setGenJUnitTearDown(false);
        else 
            mbeanGenInfo.setGenJUnitTearDown(tearDownSelected.booleanValue());
        
        return mbeanGenInfo;
    }
    
    private static void addNotifications(WizardDescriptor wiz, MBeanDO mbean) {
        String strNbNotif = (String)wiz.getProperty(
                WizardConstants.PROP_NOTIF_NB);
        int nbNotif = 0;
        if (strNbNotif != null) {
            nbNotif = new Integer(strNbNotif).intValue();
        } 
        
        if (nbNotif > 0)
            mbean.setNotificationEmitter(true);
        
        List<MBeanNotification> notifs = new ArrayList();
        for (int i = 0 ; i < nbNotif ; i++) {
            String notifClass = (String)wiz.getProperty(
                    WizardConstants.PROP_NOTIF_CLASS + i);
            if ((notifClass == null) || (notifClass.equals(""))) // NOI18N
                throw new IllegalArgumentException("notification name invalid"); // NOI18N
            
            String notifType = (String)wiz.getProperty(
                    WizardConstants.PROP_NOTIF_TYPE + i);
            if (notifType == null)
                throw new IllegalArgumentException("notification type invalid"); // NOI18N
            List types = new ArrayList();
            if (notifClass.equals(WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                types.add(new MBeanNotificationType(
                        WizardConstants.ATTRIBUTECHANGE_TYPE));
            } else {
                if (!notifType.trim().equals("")) { // NOI18N
                   String[] notifTypes =
                           notifType.split(WizardConstants.PARAMETER_SEPARATOR);
                   for (int j = 0; j < notifTypes.length; j++)
                        types.add(new MBeanNotificationType(notifTypes[j]));
                }
            }
            
            String notifDescr = (String)wiz.getProperty(
                    WizardConstants.PROP_NOTIF_DESCR + i);
            if (notifDescr == null)
                throw new IllegalArgumentException("notification description invalid"); // NOI18N
            
            notifs.add(new MBeanNotification(notifClass, notifDescr, types));
        }
        mbean.setNotifs(notifs);
    }
    
    private static void addAttributes(WizardDescriptor wiz, MBeanDO mbean) {
        String strNbAttr = (String)wiz.getProperty(
                WizardConstants.PROP_ATTR_NB);
        int nbAttr = 0;
        if (strNbAttr != null) {
            nbAttr = new Integer(strNbAttr).intValue();
        }
        
        List attributes = new ArrayList();
        for (int i = 0 ; i < nbAttr ; i++) {
             String attrName   = (String)wiz.getProperty(
                    WizardConstants.PROP_ATTR_NAME + i);
            if ((attrName == null) || (attrName.equals(""))) // NOI18N
                throw new IllegalArgumentException("attribute name invalid"); // NOI18N
             
            String attrType   = (String)wiz.getProperty(
                    WizardConstants.PROP_ATTR_TYPE + i);
            if ((attrType == null) || (attrType.equals(""))) // NOI18N
                throw new IllegalArgumentException("attribute type invalid"); // NOI18N
            
            String attrAccess = (String)wiz.getProperty(
                    WizardConstants.PROP_ATTR_RW + i);
            if ((attrAccess == null) || 
                ((!attrAccess.equals(WizardConstants.ATTR_ACCESS_READ_WRITE)) &&
                (!attrAccess.equals(WizardConstants.ATTR_ACCESS_READ_ONLY))))
                throw new IllegalArgumentException("attribute access invalid"); // NOI18N
            
            String attrDescr = (String)wiz.getProperty(
                    WizardConstants.PROP_ATTR_DESCR + i);
            if (attrDescr == null)
                throw new IllegalArgumentException("attribute decription invalid"); // NOI18N
            
            attributes.add(new MBeanAttribute(
                    WizardHelpers.capitalizeFirstLetter(attrName), attrType,
                    attrAccess, attrDescr));
        }
        mbean.setAttributes(attributes);
    }
    
    private static void addOperations(WizardDescriptor wiz, MBeanDO mbean) {
        String strNbOp = (String) wiz.getProperty(
                WizardConstants.PROP_METHOD_NB);
        int nbOp = 0;
        if (strNbOp != null) {
            nbOp = new Integer(strNbOp).intValue();
        }
        
        List operations = new ArrayList();
        for (int i = 0; i < nbOp; i++) {
            String name  = (String)wiz.getProperty(
                    WizardConstants.PROP_METHOD_NAME + i);
            if ((name == null) || (name.equals(""))) // NOI18N
                throw new IllegalArgumentException("operation name invalid"); // NOI18N
            
            String type  = (String)wiz.getProperty(
                    WizardConstants.PROP_METHOD_TYPE + i);
            if ((type == null) || (type.equals(""))) // NOI18N
                throw new IllegalArgumentException("operation type invalid"); // NOI18N
            
            String desc = (String)wiz.getProperty(
                    WizardConstants.PROP_METHOD_DESCR + i);
            if (desc==null) 
                throw new IllegalArgumentException("operation description invalid"); // NOI18N
            if (desc.trim().equals("")) { // NOI18N
                desc = "Operation "+ name + "\n"; // NOI18N
            }
            
            // discovery of operation exceptions
            String excepts = (String)
                    wiz.getProperty(WizardConstants.PROP_METHOD_EXCEP + i);
            if (excepts == null) {
                throw new IllegalArgumentException("method exception invalid"); // NOI18N
            }
            List<MBeanOperationException> exceptions = new ArrayList();
            if ( (excepts != null) && (!excepts.trim().equals("")) ) { // NOI18N
                String[] splitExcepts = excepts.split(
                        WizardConstants.EXCEPTIONS_SEPARATOR);
                for (int j = 0 ; j < splitExcepts.length ; j++) {
                    String exceptClass = splitExcepts[j].trim();
                    String exceptDesc = (String)wiz.getProperty(
                        WizardConstants.PROP_METHOD_EXCEP + i + 
                        WizardConstants.DESC + j);
                    if (exceptDesc == null) {
                        throw new IllegalArgumentException(
                                "operation exception description invalid"); // NOI18N
                    }
                    exceptions.add(
                            new MBeanOperationException(exceptClass, exceptDesc));
                }
            }
            
            // discovery of operation parameters
            String param = (String)wiz.getProperty(
                WizardConstants.PROP_METHOD_PARAM + i);
            List<MBeanOperationParameter> parameters = new ArrayList();
            if ( (param != null) && (!param.trim().equals("")) ) { // NOI18N
                // one or more parameters
                String[] params = param.split(
                        WizardConstants.PARAMETER_SEPARATOR);
                for (int j = 0 ; j < params.length ; j++) {
                    String[] parts = params[j].trim().split(" "); // NOI18N
                    String paramDesc = (String)wiz.getProperty(
                        WizardConstants.PROP_METHOD_PARAM + i + 
                        WizardConstants.DESC + j);
                    if (paramDesc == null) {
                        paramDesc ="a parameter"; // NOI18N
                    }
                    parameters.add(new MBeanOperationParameter(parts[1], 
                            parts[0], paramDesc));
                }
            }
            
            operations.add(new MBeanOperation(name,type,parameters,exceptions,desc));   
        }
        mbean.setOperations(operations);
    }
    
}
