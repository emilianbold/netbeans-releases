/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.bluej.export;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.netbeans.bluej.UpdateHelper;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
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
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));   // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(ConvertToJ2SEAction.class, "TITLE_Convert"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
                final ProgressHandle handle = ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(ConvertToJ2SEAction.class, "Progress_Display"));
                handle.start(10);
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            doExport((File)wizardDescriptor.getProperty("NewProjectLocation"), handle);   // NOI18N
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
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));  // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);  // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);  // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);  // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);  // NOI18N
                }
            }
        }
        return panels;
    }    
    
    public String getName() {
        return NbBundle.getMessage(ConvertToJ2SEAction.class, "CTL_ConvertToJ2SEAction");  // NOI18N
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
        Class j2seclazz = loader.loadClass("org.netbeans.modules.java.j2seproject.J2SEProjectGenerator");  // NOI18N
        Method createMethod = j2seclazz.getMethod("createProject", new Class[] {  // NOI18N
            File.class, String.class, String.class, String.class, String.class
        });
        createMethod.invoke(null, new Object[] {
            file, info.getName(), null, null, null
        });
        handle.progress(5);
        
        
        FileObject root = FileUtil.toFileObject(file);
        Project j2seproject = ProjectManager.getDefault().findProject(root);
//        ProjectManager.getDefault().saveProject(j2seproject);
        FileObject originRoot = project.getProjectDirectory();
        FileObject targetTestRoot = root.getFileObject("test");  // NOI18N
        FileObject targetSrcRoot = root.getFileObject("src");  // NOI18N
        splitSources(originRoot, targetSrcRoot, targetTestRoot);
        handle.progress(7);
        ClassPath path = ClassPath.getClassPath(project.getProjectDirectory(), ClassPath.COMPILE);
        ProjectClassPathExtender extender = (ProjectClassPathExtender)j2seproject.getLookup().lookup(ProjectClassPathExtender.class);
        Iterator it = path.entries().iterator();
        FileObject libsFolder = root.getFileObject("libs");  // NOI18N
        if (it.hasNext() && libsFolder == null) {
            libsFolder = root.createFolder("libs");  // NOI18N
        }
        while (it.hasNext()) {
            ClassPath.Entry entry = (ClassPath.Entry) it.next();
            URL url = FileUtil.getArchiveFile(entry.getURL());
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                if (fo.getName().indexOf("junit") == -1) {  // NOI18N
                    // we don't want to copy junit..
                    FileObject createdOne = FileUtil.copyFile(fo, libsFolder, fo.getName());
                    extender.addArchiveFile(createdOne);
                }
            }
        }
        // if main class selected, add the main.class property to prop file.
        UpdateHelper updateHelper = project.getUpdateHelper();
        EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String mainClass = (String)ep.get ("main.class"); // NOI18N
        
        if (mainClass != null) {
            FileObject fo = j2seproject.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties eds = new EditableProperties();
            InputStream instr = null;
            OutputStream outstr = null;
            FileLock lock = null;
            try {
                instr = fo.getInputStream();
                eds.load(instr);
                instr.close();
                instr = null;
                eds.setProperty("main.class", mainClass);
                if (ep.getProperty("application.args") != null) {
                    eds.setProperty("application.args", ep.getProperty("application.args"));
                }
                if (ep.getProperty("work.dir") != null) {
                    eds.setProperty("work.dir", ep.getProperty("work.dir"));
                }
                if (ep.getProperty("run.jvmargs") != null) {
                    eds.setProperty("run.jvmargs", ep.getProperty("run.jvmargs"));
                }
                lock = fo.lock();
                outstr = fo.getOutputStream(lock);
                eds.store(outstr);
            } catch (IOException ex) {
                
            } finally {
                if (instr != null) {
                    instr.close();
                }
                if (outstr != null) {
                    outstr.close();
                }
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
        
        handle.progress(9);
        OpenProjects.getDefault().open(new Project[] { j2seproject }, false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TopComponent tc = WindowManager.getDefault().findTopComponent("projectTabLogical_tc");  // NOI18N
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
                if ("java".equals(sourceFOs[i].getExt())) {  // NOI18N
                    boolean test = sourceFOs[i].getName().endsWith("Test");  // NOI18N
                    FileUtil.copyFile(sourceFOs[i], test ? targetTestRoot : targetSrcRoot, sourceFOs[i].getName());
                }
            } else if (sourceFOs[i].getFileObject("bluej.pkg") != null) {  // NOI18N
                //only the bluej package items get copied.
                FileObject childTargetSrc  = targetSrcRoot.createFolder(sourceFOs[i].getName());
                FileObject childTargetTest  = targetTestRoot.createFolder(sourceFOs[i].getName());
                splitSources(sourceFOs[i], childTargetSrc, childTargetTest);
            }
        }
    }
    
    
}
