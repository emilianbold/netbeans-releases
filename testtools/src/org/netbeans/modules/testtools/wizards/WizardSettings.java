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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * WizardSettings.java
 *
 * Created on April 24, 2002, 3:45 PM
 */

import java.io.IOException;

import org.openide.src.MethodElement;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileObject;

import org.openide.util.NbBundle;

/**
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
class WizardSettings extends Object {

    private static final String PROPERTY_NAME = "WIZARD_SETTINGS_PROPERTY"; // NOI18N
    
    static WizardSettings get(Object o) {
        return (WizardSettings)((TemplateWizard)o).getProperty(PROPERTY_NAME);
    }
    
    void store(TemplateWizard wiz) {
        wiz.putProperty(PROPERTY_NAME, this);
    }
    
    boolean startFromWorkspace = false;
    boolean startFromType = false;
    boolean startFromSuite = false;
    boolean createType = false;
    boolean createSuite = false;
    
    DataFolder workspaceTarget = null;
    String workspaceName = null;
    DataObject workspaceTemplate = null;
    DataObject workspaceScript = null;
    int workspaceLevel = -1;
    
    DataFolder typeTarget = null;
    String typeName = null;
    DataObject typeTemplate = null;
    DataObject typeScript = null;
    DataObject typeConfig = null;
    boolean typeUseJemmy = true;
    boolean typeSDI = true;
    String typeJVMSuffix = null;
    String typeExcludes = null;
    String typeCompPath = null;
    String typeExecPath = null;
    String typeJemmyHome = null;
    String typeJellyHome = null;
    
    String bagName = null;
    String bagAttrs = null;
    String bagIncludes = null;
    String bagExcludes = null;
    boolean bagIDEExecutor = true;
    
    DataFolder suiteTarget = null;
    String suiteName = null;
    DataObject suiteTemplate = null;
    DataObject suite = null;
    String suitePackage = null;

    String defaultType = ""; // NOI18N
    String defaultAttributes = ""; // NOI18N
    String netbeansHome = ""; // NOI18N
    String xtestHome = ""; // NOI18N
    
    WizardIterator.CaseElement methods[];
    MethodElement templateMethods[];

    void readWorkspaceSettings() {
        XMLDocument doc=new XMLDocument(workspaceTemplate);
        defaultType=doc.getProperty("xtest.testtype", "value"); // NOI18N
        defaultAttributes=doc.getProperty("xtest.attribs", "value"); // NOI18N
        netbeansHome=doc.getProperty("netbeans.home", "location"); // NOI18N
        xtestHome=doc.getProperty("xtest.home", "location"); // NOI18N
    }

    
    void writeWorkspaceSettings() throws IOException {
        XMLDocument doc=new XMLDocument(workspaceScript);
        if (workspaceName==null)
            workspaceName=workspaceTemplate.getPrimaryFile().getName();
        doc.setElement("project", "name", NbBundle.getMessage(WizardSettings.class, "Title_WorkspaceScript", new Object[] {workspaceName}));
        doc.setProperty("netbeans.home", "location", netbeansHome); // NOI18N
        doc.setProperty("xtest.home", "location", xtestHome); // NOI18N
        doc.setProperty("xtest.module", "value", workspaceName); // NOI18N
        doc.setProperty("xtest.testtype", "value", defaultType); // NOI18N
        doc.setProperty("xtest.attribs", "value", defaultAttributes); // NOI18N
        WizardIterator.save(workspaceScript);
    }

    void readTypeSettings() {
        Object o[] = ((DataObject.Container)typeTemplate).getChildren();
        XMLDocument doc=null;
        for (int i=0; (i<o.length)&&(doc==null); i++) 
            if ((o[i] instanceof DataObject) && (((DataObject)o[i]).getName().indexOf("build-")>=0)) // NOI18N
                doc=new XMLDocument((DataObject)o[i]);
        String value;
        value=doc.getProperty("xtest.extra.jars.path", "value"); // NOI18N
        typeUseJemmy=(value!=null)&&(value.indexOf("jemmy")>=0); // NOI18N
        value=doc.getProperty("xtest.ide.winsys", "value");; // NOI18N
        typeSDI=(value!=null)&&!value.equals("mdi"); // NOI18N
        typeJVMSuffix=doc.getProperty("xtest.jvmargs", "value"); // NOI18N
        typeExcludes=doc.getProperty("compile.excludes", "value"); // NOI18N
        typeCompPath=doc.getProperty("compiletest.classpath", "value"); // NOI18N
        typeExecPath=doc.getProperty("xtest.extra.jars", "value"); // NOI18N
        if (typeJemmyHome==null)
            typeJemmyHome=doc.getProperty("jemmy.home", "location"); // NOI18N
        if (typeJellyHome==null)
            typeJellyHome=doc.getProperty("jelly.home", "location"); // NOI18N
        doc=null;
        for (int i=0; (i<o.length)&&(doc==null); i++) 
            if ((o[i] instanceof DataObject) && (((DataObject)o[i]).getName().indexOf("cfg-")>=0)) // NOI18N
                doc=new XMLDocument((DataObject)o[i]);
        bagAttrs=doc.getElement("testbag", "testattribs"); // NOI18N
        bagIncludes=doc.getElement("include", "name"); // NOI18N
        bagExcludes=doc.getElement("exclude", "name"); // NOI18N
        bagIDEExecutor=!"code".equals(doc.getElement("testbag", "executor")); // NOI18N
    }
    
    void writeTypeSettings() throws IOException {
        XMLDocument doc=new XMLDocument(typeScript);
        if (typeName==null)
            typeName=typeTemplate.getPrimaryFile().getName();
        doc.setElement("project", "name", NbBundle.getMessage(WizardSettings.class, "Title_TestTypeScript", new Object[] {typeName})); // NOI18N
        doc.setProperty("jemmy.home", "location", typeJemmyHome); // NOI18N
        doc.setProperty("jelly.home", "location", typeJellyHome); // NOI18N
        if (typeUseJemmy) {
            doc.setProperty("xtest.extra.jars.path", "value", "${jemmy.home};${jelly.home}"); // NOI18N
            doc.setProperty("xtest.extra.jars.ide", "value", "jemmy.jar;jelly2-nb.jar"); // NOI18N
        } else {
            doc.setProperty("xtest.extra.jars.path", "value", ""); // NOI18N
            doc.setProperty("xtest.extra.jars.ide", "value", ""); // NOI18N
        }
        doc.setProperty("xtest.extra.jars", "value", typeExecPath); // NOI18N
        doc.setProperty("xtest.jvmargs", "value", typeJVMSuffix); // NOI18N
        doc.setProperty("compiletest.classpath", "value", typeCompPath); // NOI18N
        doc.setProperty("compile.excludes", "value", typeExcludes); // NOI18N
        if (typeSDI) {
            doc.setProperty("xtest.ide.winsys", "value", "sdi"); // NOI18N
        } else {
            doc.setProperty("xtest.ide.winsys", "value", "mdi"); // NOI18N
        }            
        WizardIterator.save(typeScript);
        
        doc=new XMLDocument(typeConfig);
        doc.setElement("mconfig", "name", NbBundle.getMessage(WizardSettings.class, "Title_TestTypeConfig", new Object[] {typeName})); // NOI18N
        doc.setElement("testset", "dir", typeName+"/src"); // NOI18N
        String antfile=typeScript.getPrimaryFile().getNameExt();
        doc.setElement("compiler", "antfile", antfile); // NOI18N
        doc.setElement("executor", "antfile", antfile); // NOI18N
        doc.setElement("testbag", "name", bagName); // NOI18N
        doc.setElement("testbag", "testattribs", bagAttrs); // NOI18N
        doc.setElement("include", "name", bagIncludes); // NOI18N
        doc.setElement("exclude", "name", bagExcludes); // NOI18N
        if (bagIDEExecutor) {
            doc.setElement("testbag", "executor", "ide"); // NOI18N
        } else {
            doc.setElement("testbag", "executor", "code"); // NOI18N
        }
        WizardIterator.save(typeConfig);
        
        if (startFromType && defaultType!=null && defaultType.length()>0) {
            FileObject fo=typeTarget.getPrimaryFile().getFileObject("build","xml"); // NOI18N
            if (fo!=null) {
                workspaceScript=DataObject.find(fo);
                doc=new XMLDocument(workspaceScript);
                doc.setProperty("xtest.testtype", "value", defaultType); // NOI18N
                WizardIterator.save(workspaceScript);
            }
        }
    }
}
