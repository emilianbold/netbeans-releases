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
 * WizardIterator.java
 *
 * Created on April 10, 2002, 1:51 PM
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.reflect.Modifier;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.src.Type;
import org.openide.src.Identifier;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.SourceException;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.java.JavaDataObject;
import java.util.Vector;
import org.openide.loaders.DataFolder;
import java.util.HashSet;
import org.openide.ErrorManager;
import java.util.Set;
import java.util.Enumeration;
import org.openide.filesystems.Repository;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import java.awt.Component;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import java.io.File;
import org.netbeans.modules.group.GroupShadow;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public abstract class WizardIterator implements TemplateWizard.Iterator {
    
    public static final String METHODS_PROPERTY = "METHODS";
    public static final String TEMPLATE_METHODS_PROPERTY = "TEMPLATE_METHODS";
    public static final String SUITE_NAME_PROPERTY = "SUITE_NAME";
    public static final String SUITE_PACKAGE_PROPERTY = "SUITE_PACKAGE";
    public static final String SUITE_TEMPLATE_PROPERTY = "SUITE_TEMPLATE";
    public static final String SUITE_TARGET_PROPERTY = "SUITE_TARGET";
    public static final String TESTTYPE_TARGET_PROPERTY = "TESTTYPE_TARGET";
    public static final String TESTTYPE_NAME_PROPERTY = "TESTTYPE_NAME";
    public static final String TESTTYPE_TEMPLATE_PROPERTY = "TESTTYPE_TEMPLATE";
    public static final String CREATE_SUITE_PROPERTY = "CREATE_SUITE";
    public static final String TESTWORKSPACE_TARGET_PROPERTY = "TESTWORKSPACE_TARGET";
    public static final String TESTWORKSPACE_NAME_PROPERTY = "TESTWORKSPACE_NAME";
    public static final String TESTWORKSPACE_TEMPLATE_PROPERTY = "TESTWORKSPACE_TEMPLATE";
    public static final String CREATE_TESTTYPE_PROPERTY = "CREATE_TESTTYPE";
    public static final String CREATE_TESTBAG_PROPERTY = "CREATE_TESTBAG";
    
    public static class CaseElement extends Object {
        String name;
        MethodElement template;
        public CaseElement(String name, MethodElement template) {
            this.name=name;
            this.template=template;
        }
        public String getName() {
            return name;
        }
        public MethodElement getTemplate() {
            return template;
        }
        public String toString() {
            return name+" ["+template.getName().getName()+"]";
        }
    }
    
    public static class MyCellRenderer extends DefaultListCellRenderer {
        public MyCellRenderer() {
            super();
        }
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof MethodElement)
                value=((MethodElement)value).getName().getName();
            else if (value instanceof DataObject)
                value=((DataObject)value).getNodeDelegate().getDisplayName();
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    protected transient WizardDescriptor.Panel[] panels;
    protected transient String[] names;
    protected transient int current = 0;
    protected transient TemplateWizard wizard;
    
    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current() {
        return panels[current];
    }
    
    public boolean hasNext() {
        return (current+1)<panels.length;
    }
    
    public boolean hasPrevious() {
        return current>0;
    }
    
    public String name() {
        return names[current];
    }
    
    public void nextPanel() {
        current++;
    }
    
    public void previousPanel() {
        current--;
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
    public void uninitialize(TemplateWizard wizard) {
        panels=null;
        names=null;
    }
    
    protected static MethodElement[] getTemplateMethods(JavaDataObject source) {
        ClassElement clel = source.getSource().getClass(Identifier.create(source.getName()));
        MethodElement[] methods = clel.getMethods();
        ArrayList templates = new ArrayList();
        for (int i=0; i<methods.length; i++)
            if ((methods[i].getName().getName().startsWith("test"))&&
                (methods[i].getModifiers()==Modifier.PUBLIC)&&
                (methods[i].getParameters().length==0)&&
                (methods[i].getReturn().equals(Type.VOID)))
                templates.add(methods[i]);
        return (MethodElement[])templates.toArray(new MethodElement[templates.size()]);
    }
            
    protected static void transformTemplateMethods(JavaDataObject source, CaseElement[] methods, MethodElement[] templates) throws SourceException, IOException {
        ClassElement clel = source.getSource().getClass(Identifier.create(source.getName()));

        // removing old template methods
        clel.removeMethods(getTemplateMethods(source));

        // adding and renaming new methods, creating golden files if needed
        for (int i=0; i<methods.length; i++) {
            CaseElement cel=methods[i];
            clel.addMethod(cel.getTemplate());
            clel.getMethod(cel.getTemplate().getName(), null).setName(Identifier.create(cel.getName()));
            if (cel.getTemplate().getBody().indexOf("compareReferenceFiles")>=0)
                try {
                    createGoldenFile(source, cel.getName());
                } catch (IOException ioe) {}
        }

        // creating list of test cases
        String className=source.getName();
        StringBuffer suite = new StringBuffer();
        suite.append("\n        TestSuite suite = new NbTestSuite();\n");
        for (int i=0; i<methods.length; i++) {
            suite.append("        suite.addTest(new ");
            suite.append(className);
            suite.append("(\"");
            suite.append(methods[i].getName());
            suite.append("\"));\n");
        }
        suite.append("        return suite;\n");
        clel.getMethod(Identifier.create("suite"), null).setBody(suite.toString());
        ((SaveCookie)source.getCookie(SaveCookie.class)).save();
    }

    protected static void createGoldenFile(JavaDataObject source, String name) throws IOException {
        FileObject fo=source.getFolder().getPrimaryFile();
        FileObject fo2=fo.getFileObject("data");
        if ((fo2==null)||(!fo2.isFolder()))
            fo2=fo.createFolder("data");
        fo=fo2.getFileObject(source.getName());
        if ((fo==null)||(!fo.isFolder()))
            fo=fo2.createFolder(source.getName());
        fo.createData(name,"pass");
    }

    protected static boolean detectTestWorkspace(DataObject folder) throws IOException {
        FileObject test=folder.getPrimaryFile().getFileObject("test");
        return ((test!=null)&&(test.getFileObject("build","xml")!=null));
    }
    
    protected static boolean detectBuildScript(DataObject folder) throws IOException {
        return folder.getPrimaryFile().getFileObject("build","xml")!=null;
    }
    
    protected static int detectWorkspaceLevel(DataObject folder) {
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(folder.getPrimaryFile().getFileObject("CVS").getFileObject("Repository").getInputStream()));
            StringTokenizer repository=new StringTokenizer(br.readLine(),"/");
            br.close();
            return repository.countTokens();
        } catch (Exception e) {}
        return -1;
    }
    
    protected static Set instantiateTestSuite(TemplateWizard wizard) throws IOException {
        JavaDataObject template=(JavaDataObject)wizard.getProperty(SUITE_TEMPLATE_PROPERTY);
        String targetName=(String)wizard.getProperty(SUITE_NAME_PROPERTY);
        if (targetName==null)
            targetName=template.getPrimaryFile().getName();
        Vector methods=(Vector)wizard.getProperty(METHODS_PROPERTY);
        MethodElement[] templates=(MethodElement[])wizard.getProperty(TEMPLATE_METHODS_PROPERTY);
        DataFolder targetFolder=(DataFolder)wizard.getProperty(SUITE_TARGET_PROPERTY);

        template=(JavaDataObject)template.createFromTemplate(targetFolder, targetName);
        try {
            transformTemplateMethods(template, (CaseElement[])methods.toArray(new CaseElement[methods.size()]), templates);
        } catch (SourceException se) {
            ErrorManager.getDefault().notify(se);
        }
        HashSet set=new HashSet();
        set.add(template);
        return set;
    }

    protected static Set instantiateTestType(TemplateWizard wizard) throws IOException {
        String name=(String)wizard.getProperty(TESTTYPE_NAME_PROPERTY);
        DataFolder targetFolder=(DataFolder)wizard.getProperty(TESTTYPE_TARGET_PROPERTY);
        DataObject template=(DataObject)wizard.getProperty(TESTTYPE_TEMPLATE_PROPERTY);
        if (name==null)
            name=template.getPrimaryFile().getName();

        HashSet set=new HashSet();
        set.add(template.createFromTemplate(targetFolder, name));

        DataFolder suiteTarget=DataFolder.create(targetFolder, name+"/src");
        File root=FileUtil.toFile(suiteTarget.getPrimaryFile());
        if (root!=null) try {
            LocalFileSystem lfs=new LocalFileSystem();
            lfs.setRootDirectory(root);
            Repository.getDefault().addFileSystem(lfs);
            suiteTarget=DataFolder.findFolder(lfs.getRoot());
        } catch (Exception e) {}
        
        if (((Boolean)wizard.getProperty(CREATE_SUITE_PROPERTY)).booleanValue()) {
            suiteTarget=DataFolder.create(suiteTarget, (String)wizard.getProperty(SUITE_PACKAGE_PROPERTY));
            wizard.putProperty(SUITE_TARGET_PROPERTY, suiteTarget);
            set.addAll(instantiateTestSuite(wizard));
        }
        return set;
    }
    
    protected static Set instantiateTestWorkspace(TemplateWizard wizard) throws IOException {
        HashSet set=new HashSet();
        DataFolder df=DataFolder.create(wizard.getTargetFolder(),"test");
        set.add(wizard.getTemplate().createFromTemplate(df, "build"));
        wizard.putProperty(TESTTYPE_TARGET_PROPERTY, df);
        
        if (((Boolean)wizard.getProperty(CREATE_TESTTYPE_PROPERTY)).booleanValue()) {
            set.addAll(instantiateTestType(wizard));
        }
        
        return set;
    }
    
    public static DataObject[] getSuiteTemplates() {
        Enumeration enum=Repository.getDefault().getDefaultFileSystem().findResource("Templates").getFileObject("TestTools").getData(false);
        ArrayList list=new ArrayList();
        DataObject o;
        while (enum.hasMoreElements()) try {
            o=DataObject.find((FileObject)enum.nextElement());
            if (o instanceof JavaDataObject)
                list.add(o);
        } catch (Exception e) {}
        return (DataObject[])list.toArray(new DataObject[list.size()]);
    }
    
    public static DataObject[] getTestTypeTemplates() {
        Enumeration enum=Repository.getDefault().getDefaultFileSystem().findResource("Templates").getFileObject("TestTools").getData(false);
        ArrayList list=new ArrayList();
        DataObject o;
        while (enum.hasMoreElements()) try {
            o=DataObject.find((FileObject)enum.nextElement());
            if (o instanceof GroupShadow)
                list.add(o);
        } catch (Exception e) {}
        return (DataObject[])list.toArray(new DataObject[list.size()]);
    }
}
