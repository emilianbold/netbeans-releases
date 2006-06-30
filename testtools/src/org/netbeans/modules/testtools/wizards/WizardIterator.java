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
 * WizardIterator.java
 *
 * Created on April 10, 2002, 1:51 PM
 */

import java.io.*;
import java.util.*;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ChangeListener;
import java.lang.reflect.Modifier;

import org.openide.src.*;
import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.cookies.EditorCookie;
import org.openide.util.RequestProcessor;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;

import org.netbeans.modules.java.JavaDataObject;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/** Abstract Wizard Iterator class for all Test Tools Wizard Iterators
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public abstract class WizardIterator implements TemplateWizard.Iterator {
    
    static final long serialVersionUID = -1496383337413300945L;
    
    static class CaseElement extends Object {
        String name;
        MethodElement template;
        /**
         * @param name
         * @param template  */        
        CaseElement(String name, MethodElement template) {
            this.name=name;
            this.template=template;
        }
        /**
         * @return  */        
        String getName() {
            return name;
        }
        /**
         * @return  */        
        MethodElement getTemplate() {
            return template;
        }
        /** returns String representation of CaseElement class
         * @return String representation of CaseElement class */        
        public String toString() {
            return name+" ["+template.getName().getName()+"]"; // NOI18N
        }
    }
    
    static class MyCellRenderer extends DefaultListCellRenderer {

        static final long serialVersionUID = 6764582346511445803L;
        
        MyCellRenderer() {
            super();
        }
        
        /** Cell Renderer implemention method
         * @param list Jlist
         * @param value Object value
         * @param index int value index
         * @param isSelected boolean
         * @param cellHasFocus boolean
         * @return Component of rendered cell (JLabel) */        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof MethodElement)
                value=((MethodElement)value).getName().getName();
            else if (value instanceof DataObject)
                value=((DataObject)value).getNodeDelegate().getDisplayName();
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    transient WizardDescriptor.Panel[] panels;
    transient String[] names;
    transient int current = 0;
    transient TemplateWizard wizard;
    
    /** adds Change Listener
     * @param changeListener ChangeListener */    
    public void addChangeListener(ChangeListener changeListener) {}
    
    /** returns current Wizard Panel
     * @return current WizardDescripto.Panel */    
    public WizardDescriptor.Panel current() {
        return panels[current];
    }
    
    /** test if current Panel is not last
     * @return boolean true if current Panel is not last */    
    public boolean hasNext() {
        return (current+1)<panels.length;
    }
    
    /** test if current Panel is not first
     * @return boolean true if current Panel is not first */    
    public boolean hasPrevious() {
        return current>0;
    }
    
    /** returns name of current Panel
     * @return name of current Panel */    
    public String name() {
        return names[current];
    }
    
    /** goes to the next Panel */    
    public void nextPanel() {
        current++;
    }
    
    /** goes to the previous Panel */    
    public void previousPanel() {
        current--;
    }
    
    /** removes Change Listener
     * @param changeListener ChangeListener */    
    public void removeChangeListener(ChangeListener changeListener) {}
    
    /** performs unitialization of Wizard Iterator
     * @param wizard TemplateWizard instance requested unitialization */    
    public void uninitialize(TemplateWizard wizard) {
        panels=null;
        names=null;
    }
    
    static MethodElement[] getTemplateMethods(JavaDataObject source) {
        ClassElement clel = source.getSource().getClass(Identifier.create(source.getName()));
        MethodElement[] methods = clel.getMethods();
        ArrayList templates = new ArrayList();
        for (int i=0; i<methods.length; i++)
            if ((methods[i].getName().getName().startsWith("test"))&& // NOI18N
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
        for (int i=0; methods!=null && i<methods.length; i++) {
            CaseElement cel=methods[i];
            clel.addMethod(cel.getTemplate());
            clel.getMethod(cel.getTemplate().getName(), null).setName(Identifier.create(cel.getName()));
            if (cel.getTemplate().getBody().indexOf("compareReferenceFiles")>=0) // NOI18N
                try {
                    createGoldenFile(source, cel.getName());
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
                }
        }

        // creating list of test cases
        String className=source.getName();
        StringBuffer suite = new StringBuffer();
        suite.append("\n        TestSuite suite = new NbTestSuite();\n"); // NOI18N
        for (int i=0; methods!=null && i<methods.length; i++) {
            suite.append("        suite.addTest(new "); // NOI18N
            suite.append(className);
            suite.append("(\""); // NOI18N
            suite.append(methods[i].getName());
            suite.append("\"));\n"); // NOI18N
        }
        suite.append("        return suite;\n"); // NOI18N
        clel.getMethod(Identifier.create("suite"), null).setBody(suite.toString()); // NOI18N
    }

    static void createGoldenFile(JavaDataObject source, String name) throws IOException {
        FileObject fo=source.getFolder().getPrimaryFile();
        FileObject fo2=fo.getFileObject("data"); // NOI18N
        if ((fo2==null)||(!fo2.isFolder()))
            fo2=fo.createFolder("data"); // NOI18N
        fo=fo2.getFileObject("goldenfiles"); // NOI18N
        if ((fo==null)||(!fo.isFolder()))
            fo=fo2.createFolder("goldenfiles"); // NOI18N
        fo2=fo.getFileObject(source.getName());
        if ((fo2==null)||(!fo2.isFolder()))
            fo2=fo.createFolder(source.getName());
        fo2.createData(name, "pass"); // NOI18N
    }
    
    static boolean detectBuildScript(DataFolder folder) {
        FileObject fo=folder.getPrimaryFile();
        return (fo!=null)&&
               ((fo=fo.getFileObject("test"))!=null)&& // NOI18N
               (fo.isFolder())&&
               (fo.getFileObject("build","xml")!=null); // NOI18N
    }
    
    static boolean detectTestType(DataFolder folder, String name) {
        FileObject fo=folder.getPrimaryFile();
        if (fo==null)  return false;
        return (fo.getFileObject(name)!=null)||
               (fo.getFileObject("cfg-"+name,"xml")!=null)|| // NOI18N
               (fo.getFileObject("build-"+name,"xml")!=null); // NOI18N
    }
    
    static int detectWorkspaceLevel(DataFolder folder) {
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(folder.getPrimaryFile().getFileObject("CVS").getFileObject("Repository").getInputStream())); // NOI18N
            StringTokenizer repository=new StringTokenizer(br.readLine(),"/"); // NOI18N
            br.close();
            int i=repository.countTokens();
            if ((i>0)&&(i<4))
                return i-1;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return 0;
    }
    
    static Set instantiateTestSuite(WizardSettings set) throws IOException {
        if (set.suiteName!=null && !Utilities.isJavaIdentifier(set.suiteName))
            throw new IOException(NbBundle.getMessage(WizardIterator.class, "ERR_TestSuiteName")); // NOI18N
        try {
            set.suite=(JavaDataObject)set.suiteTemplate.createFromTemplate(set.suiteTarget, set.suiteName);
        } catch (IOException ioe) {
            if (set.suiteName==null)
                set.suiteName=set.suiteTemplate.getPrimaryFile().getName();
            throw new IOException(NbBundle.getMessage(WizardIterator.class, "ERR_CreateSuite", new Object[] {set.suiteName, set.suiteTarget.getPrimaryFile().getPackageName('/'), ioe.getMessage()})); // NOI18N
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

    static Set instantiateTestType(WizardSettings set) throws IOException {
        if (set.typeName!=null && set.typeName.indexOf(' ')>=0)
            throw new IOException(NbBundle.getMessage(WizardIterator.class, "ERR_TestTypeName")); // NOI18N
        HashSet res=new HashSet();
        DataObject dob=null;
        try {
            dob=set.typeTemplate.createFromTemplate(set.typeTarget, set.typeName);
        } catch (IOException ioe) {
            if (set.typeName==null)
                set.typeName=set.typeTemplate.getPrimaryFile().getName();
            throw new IOException(NbBundle.getMessage(WizardIterator.class, "ERR_CreateTestType", new Object[] {set.typeName, set.typeTarget.getPrimaryFile().getPackageName('/'), ioe.getMessage()})); // NOI18N
        }
        Object o[] = ((DataObject.Container)dob).getChildren();
        for (int i=0; i<o.length; i++) 
            if (o[i] instanceof DataObject) {
                if (((DataObject)o[i]).getName().startsWith("build-")) // NOI18N
                    set.typeScript=(DataObject)o[i];
                if (((DataObject)o[i]).getName().startsWith("cfg-")) // NOI18N
                    set.typeConfig=(DataObject)o[i];
                res.add((DataObject)o[i]);
            }
        dob.delete();
        
        set.writeTypeSettings();
        
        if (set.typeName==null)
            set.typeName=set.typeTemplate.getPrimaryFile().getName();
        set.suiteTarget=DataFolder.create(set.typeTarget, set.typeName+"/src"); // NOI18N
        res.add(set.suiteTarget);
        File root=FileUtil.toFile(set.suiteTarget.getPrimaryFile());
        if (root!=null) try {
            LocalFileSystem lfs=new LocalFileSystem();
            lfs.setRootDirectory(root);
            Repository.getDefault().addFileSystem(lfs);
            set.suiteTarget=DataFolder.findFolder(lfs.getRoot());
            res.add(set.suiteTarget);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        
        if (set.createSuite) {
            set.suiteTarget=DataFolder.create(set.suiteTarget, set.suitePackage);
            res.addAll(instantiateTestSuite(set));
        }
        return res;
    }
    
    static Set instantiateTestWorkspace(WizardSettings set) throws IOException {
        if (set.workspaceName!=null && set.workspaceName.indexOf(' ')>=0)
            throw new IOException(NbBundle.getMessage(WizardIterator.class, "ERR_TestWorkspaceName")); // NOI18N
        HashSet res=new HashSet();
        
        set.typeTarget=DataFolder.create(set.workspaceTarget,"test"); // NOI18N
        res.add(set.typeTarget);
        
        try {
            set.workspaceScript=set.workspaceTemplate.createFromTemplate(set.typeTarget, "build"); // NOI18N
        } catch (IOException ioe) {
            throw new IOException(NbBundle.getMessage(WizardIterator.class, "ERR_CreateTestWorkspace", new Object[] {set.typeTarget.getPrimaryFile().getPackageName('/'), ioe.getMessage()})); // NOI18N
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
    
    static DataObject[] getSuiteTemplates() {
        Enumeration enum=Repository.getDefault().getDefaultFileSystem().findResource("Templates").getFileObject("TestTools").getData(false); // NOI18N
        ArrayList list=new ArrayList();
        DataObject o;
        while (enum.hasMoreElements()) try {
            o=DataObject.find((FileObject)enum.nextElement());
            if (o instanceof JavaDataObject)
                list.add(o);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return (DataObject[])list.toArray(new DataObject[list.size()]);
    }
    
    static DataObject[] getTestTypeTemplates() {
        Enumeration enum=Repository.getDefault().getDefaultFileSystem().findResource("Templates").getFileObject("TestTools").getData(false); // NOI18N
        ArrayList list=new ArrayList();
        DataObject o;
        while (enum.hasMoreElements()) try {
            o=DataObject.find((FileObject)enum.nextElement());
            if (GroupShadowTool.instanceOf(o))
                list.add(o);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return (DataObject[])list.toArray(new DataObject[list.size()]);
    }
    
    static void save(final DataObject dob) throws IOException {
            Runnable run=new Runnable() {
                public void run() {
                    try {
                        if (dob.isModified())
                            ((EditorCookie)dob.getCookie(EditorCookie.class)).saveDocument();
                    } catch (Exception e) {}
                }
            };
            //save after 1 second
            RequestProcessor.postRequest(run, 1000);
            //and after 5 seconds for sure
            RequestProcessor.postRequest(run, 5000);
    }
   
    private  static File target=new File("."); // NOI18N
    
    private static class JarAndZipFilter extends FileFilter {
        /** FileFilter implementation method
         * @param f file to be accepted
         * @return boolean true if file accepted (is Jar, Zip or directory) */        
        public boolean accept(File f) {
            if (f.isDirectory ()) return true;
            String s = f.getPath().toLowerCase();
            return s.endsWith(".jar") || s.endsWith(".zip"); // NOI18N
        }
        /** return description of FileFilter
         * @return String description of FileFilter */        
        public String getDescription() {
            return NbBundle.getMessage(WizardIterator.class, "LBL_JarZipFilter");  // NOI18N
        }
    }
   
    /** shows File Chooser Dialog and returns selected file or directory
     * @param parent parent Component (usually JFrame)
     * @param title String title of the Dialog
     * @param selectDirectories boolean switch if directory can be selected
     * @param selectJars boolean switch if filter for Jars and Zips should be applied
     * @return selected File of null when Cancel was user canceled operation */    
   public static File showFileChooser(Component parent, String title, boolean selectDirectories, boolean selectJars) {
        JFileChooser f=new JFileChooser(target);
        f.setDialogTitle(title);
        // show also hidden files (e.g. .* on unix)
        f.setFileHidingEnabled(false);
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
