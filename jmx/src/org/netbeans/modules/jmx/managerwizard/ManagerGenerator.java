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
package org.netbeans.modules.jmx.managerwizard;

import java.text.MessageFormat;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.cookies.SaveCookie;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.openide.loaders.TemplateWizard;

/**
 *
 *  Wizard Agent code generator class
 */
public class ManagerGenerator
{
    private String[] connectionTemplate;
    private Boolean isSecurityChecked;
    
    /**
     * Entry point to generate manager code.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>CreationResults</CODE> results of manager creation
     */
    public CreationResults generateManager(WizardDescriptor wiz)
           throws java.io.IOException, Exception
    {
        FileObject createdFile = null;
        String managerName = Templates.getTargetName(wiz);
        FileObject managerFolder = Templates.getTargetFolder(wiz);
        DataFolder managerFolderDataObj = DataFolder.findFolder(managerFolder);
        connectionTemplate = new String[3];
        
        //==============================================
        // manager generation
        //==============================================
        
        CreationResults result = new CreationResults(1);
        DataObject managerDObj = null;
        
        //for memory optimisation, manipulating dataobjects and fileobjects
        //has to be surrounded by a transaction
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            FileObject template = Templates.getTemplate( wiz );
            DataObject dTemplate = DataObject.find( template );                
            managerDObj = dTemplate.createFromTemplate( 
                    managerFolderDataObj, managerName );
            FileObject managerFile = managerDObj.getPrimaryFile();
            Resource managerRc = JavaModel.getResource(managerFile);
            JavaClass managerClass = WizardHelpers.getJavaClass(managerRc,
                                                                   managerName);       
            
            //get information of the wizard descriptor
            Boolean mainMethodSelected = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_MAIN_METHOD_SELECTED);
            Boolean sampleSelected = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_SAMPLE_CODE_SELECTED);
            isSecurityChecked = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_SECURITY_SELECTED);
            
            //get a reference on the main method
            Method mainMethod = WizardHelpers.getMainMethod(managerClass);
            if (!mainMethodSelected) {
                //delete main method from the template
                mainMethod.refDelete();
            } else { //mainMethodSelected
                //keep main method in the template
                String[] sampleTemplate;
                if (sampleSelected) {
                    //fill a string array with the sample code
                    sampleTemplate = fillSampleCode();
                } else { //!sampleSelected
                    sampleTemplate = new String[1];
                    sampleTemplate[0] = WizardConstants.EMPTYSTRING;
                }
                //get the main method body text, format it and replace the 
                //tag {0} with sampleTemplate[0]
                String bodyText = mainMethod.getBodyText();
                MessageFormat form = new MessageFormat(bodyText);
                String newMethodBody = form.format(sampleTemplate);
                mainMethod.setBodyText(newMethodBody);
            }
            //replace the tags in the connect method in the same manner
            replaceTags(managerClass, wiz);
            
            save(managerDObj);
        } finally {
            //end the transaction
            JavaModel.getJavaRepository().endTrans();
        }
        result.addCreated(managerDObj.getPrimaryFile());
        return result;
    }
    
    private String[] fillSampleCode() {
        String[] temp = new String[1];
        
        temp[0] = "\n" +
                  "/* *** SAMPLE MBEAN NAME DISCOVERY *** */ \n" +
                  "/* \n" +
                  " Set resultSet = \n" +
                  "    manager.getMBeanServerConnection().queryNames(null, null);\n" +
                  " for(Iterator i = resultSet.iterator(); i.hasNext();) {\n" +
                  "     System.out.println(\"MBean name: \" + i.next());\n" +
                  " }\n" +
                  "*/\n";// NOI18N
        
        return temp;
    }
    
    private void fillUserCredentials(WizardDescriptor wiz) {
        //Boolean isSecurityChecked = (Boolean)wiz.getProperty(
        //            WizardConstants.PROP_MANAGER_SECURITY_SELECTED);
        Boolean isSampleCredential = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_CREDENTIAL_SAMPLE_SELECTED);
        
        if (isSecurityChecked) {
            if (isSampleCredential) {
                connectionTemplate[0] =
                        "/* *** SAMPLE CREDENTIALS *** */ \n" +
                        "/* Replace userName and userPassword with your parameters.  \n" +
                        " * Provide env parameter when calling JMXConnectorFactory.connect(url, env) \n" +
                        "//RMI Authentication \n" +
                        "Map env = new HashMap(); \n" +
                        "env.put(JMXConnector.CREDENTIALS, new String[]{\"" +
                        "userName\", \"" +
                        "userPassword" +
                        "\"});\n" +
                        "*/ \n";// NOI18N
            } else { //user credential selected
                String userName = (String)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_USER_NAME);
                String userPassword = (String)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_USER_PASSWORD);
                connectionTemplate[0] = 
                    "//RMI Authentication \n" +
                    "Map env = new HashMap(); \n" +
                    "env.put(JMXConnector.CREDENTIALS, new String[]{\"" +
                                userName + "\", \"" +
                                userPassword + 
                                "\"});\n";// NOI18N
            }
        } else // security box unchecked
            connectionTemplate[0] = WizardConstants.EMPTYSTRING;
    }
    
    private void fillURL(WizardDescriptor wiz) {
        
        Boolean isRmiUrl = (Boolean)wiz.getProperty(
                            WizardConstants.PROP_MANAGER_RMI_URL_SELECTED);
        Boolean isCustomUrl = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_FREEFORM_URL_SELECTED);
        
        if (isRmiUrl) {
            //a rmi url is to generate
            //connectionTemplate[1] = "//Create JMX Agent URL \n" +
            //        "JMXServiceURL url = new JMXServiceURL(\"rmi\"," + "\"" +
            //        (String)wiz.getProperty(WizardConstants.PROP_MANAGER_HOST) +
            //        "\"" + "," +
            //       (String)wiz.getProperty(WizardConstants.PROP_MANAGER_PORT) +
            //        ",\"/jndi/rmi://jmxrmi\");"; // NOI18N
            connectionTemplate[1] = "//Create JMX Agent URL \n" +
                    "JMXServiceURL url = new JMXServiceURL(\"service:jmx:rmi:///jndi/rmi://" +
                    (String)wiz.getProperty(WizardConstants.PROP_MANAGER_HOST)+":"+
                    (String)wiz.getProperty(WizardConstants.PROP_MANAGER_PORT)+"/jmxrmi" +
                    "\");";// NOI18N
        } else {
            if (isCustomUrl) {
                //generation of a free form URL
                connectionTemplate[1] = "//Create JMX Agent URL \n" +
                         "JMXServiceURL url = new JMXServiceURL(\""+
                         (String)wiz.getProperty(
                             WizardConstants.PROP_MANAGER_FREEFORM_URL) + "\");";// NOI18N
            } else
                connectionTemplate[1] = WizardConstants.EMPTYSTRING;
        }
    }
    
    private void fillConnector(WizardDescriptor wiz) {
        
        // custom initialisation of code to be generated
        connectionTemplate[2] = "\n //Connect the JMXConnector \n" +
                      "connector = " +
                       "JMXConnectorFactory.connect(url, null);";// NOI18N
        
        if (isSecurityChecked) {
            Boolean isUserCredential = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_USER_CREDENTIAL_SELECTED);
            if (isUserCredential) {
                // only if SecurityChecked and UserCredential, the code changes
            connectionTemplate[2] = "\n //Connect the JMXConnector \n" +
                      "connector = " +
                      "JMXConnectorFactory.connect(url, env);";// NOI18N
            }
        }
    }
    
    private void replaceTags(JavaClass clazz, WizardDescriptor wiz) {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            Method connectMethod = WizardHelpers.getMethod(clazz, "connect");
            String bodyText = connectMethod.getBodyText();
            
            //Boolean isSecurityChecked = (Boolean)wiz.getProperty(
              //      WizardConstants.PROP_MANAGER_SECURITY_SELECTED);
            Boolean isSampleCredential = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_CREDENTIAL_SAMPLE_SELECTED);
            Boolean isUserCredential = (Boolean)wiz.getProperty(
                    WizardConstants.PROP_MANAGER_USER_CREDENTIAL_SELECTED);
            
            fillUserCredentials(wiz);
            fillURL(wiz);
            fillConnector(wiz);
            
            MessageFormat form = new MessageFormat(bodyText);
            //formating the array with the information of the code to
            //generate
            String newMethodBody = form.format(connectionTemplate);
            connectMethod.setBodyText(newMethodBody);
            
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    private static void save(DataObject dO) throws IOException {
            SaveCookie sc = (SaveCookie) dO.getCookie(SaveCookie.class);
            if (null != sc)
                sc.save();
        }

     /**
      * Utility class representing the results of a file creation
      * process. It gatheres all files (as FileObject) created and all
      * classes (as JavaClasses) .
      */
    public static class CreationResults {
        /**
         * empty results
         */
        public static final CreationResults EMPTY = new CreationResults();
        
        private Set created; // Set< createdTest : FileObject >
        private Set skipped; // Set< sourceClass : JavaClass >
        private boolean abborted = false;
        
        /**
         * Construct a result group.
         */
        public CreationResults() { this(1);}
        
        /**
         * Construct a result group.
         * @param expectedSize <CODE>int</CODE> initial size
         */
        public CreationResults(int expectedSize) {
            created = new HashSet(expectedSize * 2 , 0.5f);
            skipped = new HashSet(expectedSize * 2 , 0.5f);
        }
        
        /**
         * Aborts the process of creation.
         */
        public void setAbborted() {
            abborted = true;
        }
        
        /**
         * Returns true if the process of creation was abborted. The
         * result contains the results gathered so far.
         * @return <CODE>boolean</CODE> true if creation process was abborted
         */
        public boolean isAbborted() {
            return abborted;
        }
        
        
        /**
         * Adds a new entry to the set of created tests.
         * @return true if it was added, false if it was present before
         * @param test <CODE>FileObject</CODE> file to add
         */
        public boolean addCreated(FileObject test) {
            return created.add(test);
        }
        
        /**
         * Adds a new <code>JavaClass</code> to the collection of
         * skipped classes.
         * @return true if it was added, false if it was present before
         * @param c <CODE>JavaClass</CODE> class to add
         */
        public boolean addSkipped(JavaClass c) {
            return skipped.add(c);
        }
        
        /**
         * Returns a set of classes that were skipped in the process.
         * @return Set<JavaClass>
         */
        public Set getSkipped() {
            return skipped;
        }
        
        /**
         * Returns a set of test data objects created.
         * @return Set<FileObject>
         */
        public Set getCreated() {
            return created;
        }
        
        /**
         * Combines two results into one. If any of the results is an
         * abborted result, the combination is also abborted. The
         * collections of created and skipped classes are unified.
         * @param rhs the other CreationResult to combine into this
         */
        public void combine(CreationResults rhs) {
            if (rhs.abborted) {
                this.abborted = true;
            }
            
            this.created.addAll(rhs.created);
            this.skipped.addAll(rhs.skipped);
        }
        
    }
}