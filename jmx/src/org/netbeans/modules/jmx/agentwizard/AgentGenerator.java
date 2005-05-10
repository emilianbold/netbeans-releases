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
package org.netbeans.modules.jmx.agentwizard;

import java.text.MessageFormat;
import java.io.IOException;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.cookies.SaveCookie;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;

/**
 *
 *  Wizard Agent code generator class
 */
public class AgentGenerator
{

       
    //======================================================
    // Entry point to generate agent code
    //======================================================
    public CreationResults generateAgent(WizardDescriptor wiz)
           throws java.io.IOException, Exception
    {
        FileObject createdFile = null;
        String agentName = Templates.getTargetName(wiz);
        FileObject agentFolder = Templates.getTargetFolder(wiz);
        DataFolder agentFolderDataObj = DataFolder.findFolder(agentFolder);
        
        //==============================================
        // agent generation
        //==============================================
        
        CreationResults result = new CreationResults(1);
        DataObject agentDObj = null;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            FileObject template = Templates.getTemplate( wiz );
            DataObject dTemplate = DataObject.find( template );                
            agentDObj = dTemplate.createFromTemplate( 
                    agentFolderDataObj, agentName );
            FileObject agentFile = agentDObj.getPrimaryFile();
            Resource agentRc = JavaModel.getResource(agentFile);
            JavaClass agentClass = WizardHelpers.getAgentJavaClass(agentRc,
                                                                   agentName);
            Boolean mainMethodSelected = (Boolean) wiz.getProperty(
                WizardConstants.PROP_AGENT_MAIN_METHOD_SELECTED);
            if ((mainMethodSelected == null) || (!mainMethodSelected)) {
                removeMainMethod(agentClass);
            }
            Boolean sampleCodeSelected = (Boolean) wiz.getProperty(
                WizardConstants.PROP_AGENT_SAMPLE_CODE_SELECTED);
            if ((sampleCodeSelected !=null) && (!sampleCodeSelected)) {
               removeSampleCode(agentClass);
            } 
            save(agentDObj);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
        result.addCreated(agentDObj.getPrimaryFile());
        return result;
    }

    /*
     * remove the init method body of the JavaClass
     */
    private void removeSampleCode(JavaClass clazz) {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
             Method initMethod = WizardHelpers.getMethod(clazz,
                     WizardConstants.PROP_AGENT_INIT_METHOD_NAME);
             // keep the first line of the body text
             String[] bodyParts = initMethod.getBodyText().split("\n");
             String textWithoutSampleCode = "\n" + bodyParts[2] + "\n\n";
             initMethod.setBodyText(textWithoutSampleCode);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    /*
     * remove the main method of the JavaClass
     */
    private void removeMainMethod(JavaClass clazz) {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
             Method mainMethod = WizardHelpers.getMainMethod(clazz); 
             mainMethod.refDelete();
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
            public static final CreationResults EMPTY = new CreationResults();
            
            Set created; // Set< createdTest : FileObject >
            Set skipped; // Set< sourceClass : JavaClass >
            boolean abborted = false;
            
            public CreationResults() { this(1);}
            
            public CreationResults(int expectedSize) {
                created = new HashSet(expectedSize * 2 , 0.5f);
                skipped = new HashSet(expectedSize * 2 , 0.5f);
            }
            
            public void setAbborted() {
                abborted = true;
            }
            
            /**
             * Returns true if the process of creation was abborted. The
             * result contains the results gathered so far.
             */
            public boolean isAbborted() {
                return abborted;
            }
            
            
            /**
             * Adds a new entry to the set of created tests.
             * @return true if it was added, false if it was present before
             */
            public boolean addCreated(FileObject test) {
                return created.add(test);
            }
            
            /**
             * Adds a new <code>JavaClass</code> to the collection of
             * skipped classes.
             * @return true if it was added, false if it was present before
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
