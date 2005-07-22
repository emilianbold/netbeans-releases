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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 * Wizard MBean code generator controller class.
 * @author tl156378
 */
public class GeneratorControler {
    
    /**
     * Generate MBean code and creates the corresponding files.
     * @param wiz <CODE>TemplateWizard</CODE> MBean informations map
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>CreationResults</CODE> the generated files set
     */
    public static CreationResults generate(TemplateWizard wiz) 
    throws java.io.IOException, Exception {
        MBeanDO mbeanDO = Translator.createMBeanDO(wiz);
        MBeanFileGenerator generator = null;
        if (mbeanDO.getType().equals(
                WizardConstants.MBEAN_DYNAMICMBEAN)) {
            generator = new DynMBeanClassGen();
        } else 
            generator = new StdMBeanClassGen();
        CreationResults result = new CreationResults(1);
        FileObject createdFile = generator.generateMBean(mbeanDO);
        
        //add notifications
        JavaClass mbeanClass = generator.getMBeanClass();
        if (mbeanDO.isNotificationEmitter()) {
            AddNotifGenerator notifGenerator = new AddNotifGenerator();
            List notifList = mbeanDO.getNotifs();
            MBeanNotification[] notifs = (MBeanNotification[])
                notifList.toArray(new MBeanNotification[notifList.size()]);
            notifGenerator.update(mbeanClass, mbeanClass.getResource(), notifs);
        }
        
        //add MBeanRegistration interface implementation
        if (mbeanDO.implMBeanRegist()) {
            AddRegistIntfGenerator mbeanRegistGen = new AddRegistIntfGenerator();
            //TODO link to mbean wizard settings
            mbeanRegistGen.update(mbeanClass, mbeanClass.getResource(),true);
        }
        
        result.addCreated(createdFile);
        
        // JUnit tests generation
        MBeanGenInfo genInfo = Translator.createGenInfo(wiz);
        if (genInfo.isGenJUnit()) {
            MBeanTestGen junitTestGenerator = new MBeanTestGen();
            FileObject junitTestFile = 
                    junitTestGenerator.generateTest(mbeanDO,genInfo);
            result.addCreated(junitTestFile);
        }
        WizardHelpers.refreshProjectTree(wiz);
        return result;                         
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
         * Empty CreationResults.
         */
        public static final CreationResults EMPTY = new CreationResults();
        
        private Set created; // Set< createdTest : FileObject >
        private Set skipped; // Set< sourceClass : JavaClass >
        private boolean abborted = false;
        
        /**
         * Default constructor of CreationResults.
         */
        public CreationResults() { this(1);}
        
        /**
         * Constructor of CreationResults. the expectedSize parameter allow to
         * optimize the size of the file set structure.
         * @param expectedSize <CODE>int</CODE> set size
         */
        public CreationResults(int expectedSize) {
            created = new HashSet(expectedSize * 2 , 0.5f);
            skipped = new HashSet(expectedSize * 2 , 0.5f);
        }
        
        /**
         * Set abborted.
         */
        public void setAbborted() {
            abborted = true;
        }
        
        /**
         * Returns true if the process of creation was abborted. The
         * result contains the results gathered so far.
         * @return <CODE>boolean</CODE> true if the the creation is abborted
         */
        public boolean isAbborted() {
            return abborted;
        }
        
        
        /**
         * Adds a new entry to the set of created tests.
         * @return true if it was added, false if it was present before
         * @param test <CODE>Object</CODE> object to add
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
