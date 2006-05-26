/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.bluej.export;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.bluej.BluejProject;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class ConvertToJ2SEAction extends AbstractAction {
    
    private BluejProject project;
    private WizardDescriptor.Panel[] panels;
    
    public ConvertToJ2SEAction(BluejProject project) {
        putValue(NAME, getName());
        this.project = project;
    }
    
    public void actionPerformed(ActionEvent e) {
        
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Convert to NetBeans J2SE Project");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
                final ProgressHandle handle = ProgressHandleFactory.createHandle("Converting to J2SE Project");
                handle.start(10);
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            doExport((File)wizardDescriptor.getProperty("NewProjectLocation"), handle);
                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (NoSuchMethodException ex) {
                            ex.printStackTrace();
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        }
    }
    
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new ExportWizardPanel1(project.getProjectDirectory())
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }    
    
    public String getName() {
        return NbBundle.getMessage(ConvertToJ2SEAction.class, "CTL_ConvertToJ2SEAction");
    }
    
    
    /**
     * lets assume the file is directory and it's empty or not existing..
     */
    private void doExport(File file, ProgressHandle handle) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!file.exists()) {
            file.mkdirs();
        }
        handle.progress(1);
        ProjectInformation info = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
        ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        Class j2seclazz = loader.loadClass("org.netbeans.modules.java.j2seproject.J2SEProjectGenerator");
        Method createMethod = j2seclazz.getMethod("createProject", new Class[] {
            File.class, String.class, String.class, String.class
        });
        createMethod.invoke(null, new Object[] {
            file, info.getName(), null, null
        });
        handle.progress(5);
        
        
        FileObject root = FileUtil.toFileObject(file);
        Project j2seproject = ProjectManager.getDefault().findProject(root);
//        ProjectManager.getDefault().saveProject(j2seproject);
        FileObject originRoot = project.getProjectDirectory();
        FileObject targetTestRoot = root.getFileObject("test");
        FileObject targetSrcRoot = root.getFileObject("src");
        splitSources(originRoot, targetSrcRoot, targetTestRoot);
        handle.progress(7);
        ClassPath path = ClassPath.getClassPath(project.getProjectDirectory(), ClassPath.COMPILE);
        ProjectClassPathExtender extender = (ProjectClassPathExtender)j2seproject.getLookup().lookup(ProjectClassPathExtender.class);
        Iterator it = path.entries().iterator();
        FileObject libsFolder = root.getFileObject("libs");
        if (it.hasNext() && libsFolder == null) {
            libsFolder = root.createFolder("libs");
        }
        while (it.hasNext()) {
            ClassPath.Entry entry = (ClassPath.Entry) it.next();
            URL url = FileUtil.getArchiveFile(entry.getURL());
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                if (fo.getName().indexOf("junit") == -1) {
                    // we don't want to copy junit..
                    FileObject createdOne = FileUtil.copyFile(fo, libsFolder, fo.getName());
                    extender.addArchiveFile(createdOne);
                }
            }
        }
        handle.progress(9);
        OpenProjects.getDefault().open(new Project[] { j2seproject }, false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TopComponent tc = WindowManager.getDefault().findTopComponent("projectTabLogical_tc");
                if (tc != null) {
                    tc.open();
                    tc.requestActive();
                }
            }
        });
        handle.finish();
        
    }
    
    private void splitSources(FileObject originRoot, FileObject targetSrcRoot, FileObject targetTestRoot) throws IOException {
        FileObject[] sourceFOs = originRoot.getChildren();
        for (int i = 0; i < sourceFOs.length; i++) {
            if (sourceFOs[i].isData()) {
                if ("java".equals(sourceFOs[i].getExt())) {
                    boolean test = sourceFOs[i].getName().endsWith("Test");
                    FileUtil.copyFile(sourceFOs[i], test ? targetTestRoot : targetSrcRoot, sourceFOs[i].getName());
                }
            } else if (sourceFOs[i].getFileObject("bluej.pkg") != null) {
                //only the bluej package items get copied.
                FileObject childTargetSrc  = targetSrcRoot.createFolder(sourceFOs[i].getName());
                FileObject childTargetTest  = targetTestRoot.createFolder(sourceFOs[i].getName());
                splitSources(sourceFOs[i], childTargetSrc, childTargetTest);
            }
        }
    }
    
    
}
