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

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public abstract class WizardIterator implements TemplateWizard.Iterator {
    
    public static final String METHODS_PROPERTY = "METHODS_PROPERTY";
    
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
    
    static MethodElement[] getTemplateMethods(JavaDataObject source) {
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
            
    static void transformTemplateMethods(JavaDataObject source, CaseElement[] methods, MethodElement[] templates) throws SourceException, IOException {
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

    public static void createGoldenFile(JavaDataObject source, String name) throws IOException {
        FileObject fo=source.getFolder().getPrimaryFile();
        FileObject fo2=fo.getFileObject("data");
        if ((fo2==null)||(!fo2.isFolder()))
            fo2=fo.createFolder("data");
        fo=fo2.getFileObject(source.getName());
        if ((fo==null)||(!fo.isFolder()))
            fo=fo2.createFolder(source.getName());
        fo.createData(name,"pass");
    }

    private static boolean detectTestWorkspace(DataObject folder) throws IOException {
        FileObject test=folder.getPrimaryFile().getFileObject("test");
        return ((test!=null)&&(test.getFileObject("build","xml")!=null));
    }
    
    private static boolean detectBuildScript(DataObject folder) throws IOException {
        return folder.getPrimaryFile().getFileObject("build","xml")!=null;
    }
    
    private static int detectWorkspaceLevel(DataObject folder) {
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(folder.getPrimaryFile().getFileObject("CVS").getFileObject("Repository").getInputStream()));
            StringTokenizer repository=new StringTokenizer(br.readLine(),"/");
            br.close();
            return repository.countTokens();
        } catch (Exception e) {}
        return -1;
    }
    
}
