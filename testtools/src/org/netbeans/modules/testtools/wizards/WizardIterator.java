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
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import java.util.Iterator;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public abstract class WizardIterator implements TemplateWizard.Iterator {
    
    
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
    
    protected static boolean detectBuildScript(DataFolder folder) {
        FileObject fo=folder.getPrimaryFile();
        return (fo!=null)&&
               ((fo=fo.getFileObject("test"))!=null)&&
               (fo.isFolder())&&
               (fo.getFileObject("build","xml")!=null);
   }
    
    protected static boolean detectTestType(DataFolder folder, String name) {
        FileObject fo=folder.getPrimaryFile();
        if (fo==null)  return false;
        return (fo.getFileObject(name)!=null)||
               (fo.getFileObject("cfg-"+name,"xml")!=null)||
               (fo.getFileObject("build-"+name,"xml")!=null);
    }
    
    protected static int detectWorkspaceLevel(DataFolder folder) {
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(folder.getPrimaryFile().getFileObject("CVS").getFileObject("Repository").getInputStream()));
            StringTokenizer repository=new StringTokenizer(br.readLine(),"/");
            br.close();
            int i=repository.countTokens();
            if ((i>0)&&(i<4))
                return i-1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    protected static Set instantiateTestSuite(WizardSettings set) throws IOException {
        try {
            set.suite=(JavaDataObject)set.suiteTemplate.createFromTemplate(set.suiteTarget, set.suiteName);
        } catch (IOException ioe) {
            if (set.suiteName==null)
                set.suiteName=set.suiteTemplate.getPrimaryFile().getName();
            throw new IOException("Could not create new Test Suite \""+set.suiteName+"\" in package \""+set.suiteTarget.getPrimaryFile().getPackageName('/')+"\". Reason is: "+ioe.getMessage());
        }
            
        try {
            transformTemplateMethods((JavaDataObject)set.suite, set.methods, set.templateMethods);
        } catch (SourceException se) {
            ErrorManager.getDefault().notify(se);
        }
        HashSet res=new HashSet();
        res.add(set.suite);
        save(set.suite);
        ((EditorCookie)set.suite.getCookie(EditorCookie.class)).open();
        return res;
    }

    protected static Set instantiateTestType(WizardSettings set) throws IOException {
        HashSet res=new HashSet();
        DataObject dob=null;
        try {
            dob=set.typeTemplate.createFromTemplate(set.typeTarget, set.typeName);
        } catch (IOException ioe) {
            if (set.typeName==null)
                set.typeName=set.typeTemplate.getPrimaryFile().getName();
            throw new IOException("Could not create new Test Type \""+set.typeName+"\" in package \""+set.typeTarget.getPrimaryFile().getPackageName('/')+"\". Reason is: "+ioe.getMessage());
        }
        Object o[]=((GroupShadow)dob).getLinks();
        for (int i=0; i<o.length; i++) 
            if (o[i] instanceof DataObject) {
                if (((DataObject)o[i]).getName().startsWith("build-"))
                    set.typeScript=(DataObject)o[i];
                if (((DataObject)o[i]).getName().startsWith("cfg-"))
                    set.typeConfig=(DataObject)o[i];
                res.add((DataObject)o[i]);
            }
        dob.delete();
        
        set.writeTypeSettings();
        
        if (set.typeName==null)
            set.typeName=set.typeTemplate.getPrimaryFile().getName();
        set.suiteTarget=DataFolder.create(set.typeTarget, set.typeName+"/src");
        res.add(set.suiteTarget);
        File root=FileUtil.toFile(set.suiteTarget.getPrimaryFile());
        if (root!=null) try {
            LocalFileSystem lfs=new LocalFileSystem();
            lfs.setRootDirectory(root);
            Repository.getDefault().addFileSystem(lfs);
            set.suiteTarget=DataFolder.findFolder(lfs.getRoot());
            res.add(set.suiteTarget);
        } catch (Exception e) {}
        
        if (set.createSuite) {
            set.suiteTarget=DataFolder.create(set.suiteTarget, set.suitePackage);
            res.addAll(instantiateTestSuite(set));
        }
        return res;
    }
    
    protected static Set instantiateTestWorkspace(WizardSettings set) throws IOException {
        HashSet res=new HashSet();
        
        set.typeTarget=DataFolder.create(set.workspaceTarget,"test");
        res.add(set.typeTarget);
        
        try {
            set.workspaceScript=set.workspaceTemplate.createFromTemplate(set.typeTarget, "build");
        } catch (IOException ioe) {
            throw new IOException("Could not create new Test Workspace in package \""+set.typeTarget.getPrimaryFile().getPackageName('/')+"\". Reason is: "+ioe.getMessage());
        }
        res.add(set.workspaceScript);
        
        if (set.workspaceName==null) 
            set.workspaceName=set.workspaceTemplate.getPrimaryFile().getName();
        
        set.writeWorkspaceSettings();
        
        if (set.createType) {
            res.addAll(instantiateTestType(set));
        }
        
        return res;
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
    
    public static void save(final DataObject dob) throws IOException {
            RequestProcessor.postRequest(new Runnable() {
                public void run() {
                    try {
                        ((EditorCookie)dob.getCookie(EditorCookie.class)).saveDocument();
                    } catch (Exception e) {}
                }
            }, 5000);
   }
   
   private  static File target=new File(".");
    
    private static class JarAndZipFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory ()) return true;
            String s = f.getPath().toLowerCase();
            return s.endsWith(".jar") || s.endsWith(".zip");
        }
        public String getDescription() {
            return "Jar and Zip File Filter";
        }
    }
   
   public static File showFileChooser(Component parent, String title, boolean selectDirectories, boolean selectJars) {
        JFileChooser f=new JFileChooser(target);
        f.setDialogTitle(title);
        if (selectJars) {
            f.setFileFilter(new JarAndZipFilter());
            if (selectDirectories)
                f.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            else
                f.setFileSelectionMode(JFileChooser.FILES_ONLY);
        } else {
            if (selectDirectories)
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        if (Utilities.showJFileChooser(f, parent, f.getApproveButtonText())==JFileChooser.APPROVE_OPTION) {
            target=f.getCurrentDirectory();
            return f.getSelectedFile();
        }
        return null;
   }

}
