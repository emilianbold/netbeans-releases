/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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

import org.netbeans.modules.group.GroupShadow;

/**
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
class WizardSettings extends Object {
    
    private static final String PROPERTY_NAME = "WIZARD_SETTINGS_PROPERTY";
    
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

    String defaultType = "";
    String defaultAttributes = "";
    String netbeansHome = "";
    String xtestHome = "";
    
    WizardIterator.CaseElement methods[];
    MethodElement templateMethods[];

    void readWorkspaceSettings() {
        XMLDocument doc=new XMLDocument(workspaceTemplate);
        defaultType=doc.getProperty("xtest.testtype", "value");
        defaultAttributes=doc.getProperty("xtest.attribs", "value");
        netbeansHome=doc.getProperty("netbeans.home", "location");
        xtestHome=doc.getProperty("xtest.home", "location");
    }

    
    void writeWorkspaceSettings() throws IOException {
        XMLDocument doc=new XMLDocument(workspaceScript);
        if (workspaceName==null)
            workspaceName=workspaceTemplate.getPrimaryFile().getName();
        doc.setElement("project", "name", workspaceName+" XTest Workspace Script");
        doc.setProperty("netbeans.home", "location", netbeansHome);
        doc.setProperty("xtest.home", "location", xtestHome);
        doc.setProperty("xtest.module", "value", workspaceName);
        doc.setProperty("xtest.testtype", "value", defaultType);
        doc.setProperty("xtest.attribs", "value", defaultAttributes);
        WizardIterator.save(workspaceScript);
    }

    void readTypeSettings() {
        GroupShadow template=(GroupShadow)typeTemplate;
        Object o[]=template.getLinks();
        XMLDocument doc=null;
        for (int i=0; (i<o.length)&&(doc==null); i++) 
            if ((o[i] instanceof DataObject) && (((DataObject)o[i]).getName().indexOf("build-")>=0))
                doc=new XMLDocument((DataObject)o[i]);
        String value;
        value=doc.getProperty("xtest.extra.jars.path", "value");
        typeUseJemmy=(value!=null)&&(value.indexOf("jemmy")>=0);
        value=doc.getProperty("xtest.ide.winsys", "value");;
        typeSDI=(value!=null)&&!value.equals("mdi");
        typeJVMSuffix=doc.getProperty("xtest.jvmargs", "value");
        typeExcludes=doc.getProperty("compile.excludes", "value");
        typeCompPath=doc.getProperty("compiletest.classpath", "value");
        typeExecPath=doc.getProperty("xtest.extra.jars", "value");
        if (typeJemmyHome==null)
            typeJemmyHome=doc.getProperty("jemmy.home", "location");
        if (typeJellyHome==null)
            typeJellyHome=doc.getProperty("jelly.home", "location");
        doc=null;
        for (int i=0; (i<o.length)&&(doc==null); i++) 
            if ((o[i] instanceof DataObject) && (((DataObject)o[i]).getName().indexOf("cfg-")>=0))
                doc=new XMLDocument((DataObject)o[i]);
        bagAttrs=doc.getElement("testbag", "testattribs");
        bagIncludes=doc.getElement("include", "name");
        bagExcludes=doc.getElement("exclude", "name");
        bagIDEExecutor=!"code".equals(doc.getElement("testbag", "executor"));
    }
    
    void writeTypeSettings() throws IOException {
        XMLDocument doc=new XMLDocument(typeScript);
        if (typeName==null)
            typeName=typeTemplate.getPrimaryFile().getName();
        doc.setElement("project", "name", typeName+" Test Type Script");
        doc.setProperty("jemmy.home", "location", typeJemmyHome);
        doc.setProperty("jelly.home", "location", typeJellyHome);
        if (typeUseJemmy) {
            doc.setProperty("xtest.extra.jars.path", "value", "${jemmy.home};${jelly.home}");
            doc.setProperty("xtest.extra.jars.ide", "value", "jemmy.jar;jelly-nb.jar");
        } else {
            doc.setProperty("xtest.extra.jars.path", "value", "");
            doc.setProperty("xtest.extra.jars.ide", "value", "");
        }
        doc.setProperty("xtest.extra.jars", "value", typeExecPath);
        doc.setProperty("xtest.jvmargs", "value", typeJVMSuffix);
        doc.setProperty("compiletest.classpath", "value", typeCompPath);
        doc.setProperty("compile.excludes", "value", typeExcludes);
        if (typeSDI) {
            doc.setProperty("xtest.ide.winsys", "value", "sdi");
        } else {
            doc.setProperty("xtest.ide.winsys", "value", "mdi");
        }            
        WizardIterator.save(typeScript);
        
        doc=new XMLDocument(typeConfig);
        doc.setElement("mconfig", "name", typeName+" Test Type Config");
        doc.setElement("testset", "dir", typeName+"/src");
        String antfile=typeScript.getPrimaryFile().getNameExt();
        doc.setElement("compiler", "antfile", antfile);
        doc.setElement("executor", "antfile", antfile);
        doc.setElement("testbag", "name", bagName);
        doc.setElement("testbag", "testattribs", bagAttrs);
        doc.setElement("include", "name", bagIncludes);
        doc.setElement("exclude", "name", bagExcludes);
        if (bagIDEExecutor) {
            doc.setElement("testbag", "executor", "ide");
        } else {
            doc.setElement("testbag", "executor", "code");
        }
        WizardIterator.save(typeConfig);
        
        if (startFromType && defaultType!=null && defaultType.length()>0) {
            FileObject fo=typeTarget.getPrimaryFile().getFileObject("build","xml");
            if (fo!=null) {
                workspaceScript=DataObject.find(fo);
                doc=new XMLDocument(workspaceScript);
                doc.setProperty("xtest.testtype", "value", defaultType);
                WizardIterator.save(workspaceScript);
            }
        }
    }
}
