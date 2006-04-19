package org.netbeans.bluej.export;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import javax.swing.AbstractAction;
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
import org.openide.NotifyDescriptor;
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
    
    public ConvertToJ2SEAction(BluejProject project) {
        putValue(NAME, getName());
        this.project = project;
    }
    
    public void actionPerformed(ActionEvent e) {
        final ExportPanel panel = new ExportPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, "Convert to J2SE Project type");
        Object ret = DialogDisplayer.getDefault().notify(dd);
        //TODO make sure the user selects en empty directory
        if (NotifyDescriptor.OK_OPTION == ret) {
                final ProgressHandle handle = ProgressHandleFactory.createHandle("Converting to J2SE Project");
                handle.start(10);
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            doExport(panel.getNewProjectLocation(), handle);
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
        Object helper = createMethod.invoke(null, new Object[] {
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
