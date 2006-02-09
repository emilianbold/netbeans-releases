package org.netbeans.bluej;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 * factory of bluej projects, only applied when netbeans related files are not created..
 * @author  Milos Kleint (mkleint@netbeans.org)
 */
public class BluejProjectFactory implements ProjectFactory {
    /** Creates a new instance of BluejProjectFactory */
    public BluejProjectFactory() {
    }
    
    public boolean isProject(FileObject fileObject) {
        File projectDir = FileUtil.toFile(fileObject);
        if (projectDir == null) {
            return false;
        }
        if (fileObject.getFileObject("nbproject") != null && fileObject.getFileObject("build.xml") != null) {
            return false;
        }
        File project = new File(projectDir, "bluej.pkg"); // NOI18N
        File parentProject = new File(projectDir.getParentFile(), "bluej.pkg");
        return project.exists() && project.isFile() &&
                (!parentProject.exists()) && !"nbproject".equalsIgnoreCase(projectDir.getName()); //NOI18N
    }
    
    public Project loadProject(FileObject fileObject, ProjectState projectState) throws IOException {
        if (FileUtil.toFile(fileObject) == null) {
            return null;
        }
        if ("nbproject".equalsIgnoreCase(fileObject.getName())) {
            return null;
        }
        FileObject projectFile = fileObject.getFileObject("bluej.pkg"); //NOI18N
        if (projectFile == null || !projectFile.isData()) {
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        if (projectDiskFile == null)  {
            return null;
        }
        if (fileObject.getParent().getFileObject("bluej.pkg") != null) {
            return null;
        }
        
        if (fileObject.getFileObject("nbproject") == null) {
            FileObject nbfolder = fileObject.createFolder("nbproject");
            InputStream str = getClass().getResourceAsStream("resources/build.xml");
            FileObject buildxml = fileObject.createData("build.xml");
            FileLock lock = buildxml.lock();
            OutputStream out = buildxml.getOutputStream(lock);
            FileUtil.copy(str, out);
            out.close();
            lock.releaseLock();
            str = getClass().getResourceAsStream("resources/build-impl.xml");
            FileObject buildimplxml = nbfolder.createData("build-impl.xml");
            lock = buildimplxml.lock();
            out = buildimplxml.getOutputStream(lock);
            FileUtil.copy(str, out);
            out.close();
            lock.releaseLock();
            str = getClass().getResourceAsStream("resources/project.properties");
            FileObject props = nbfolder.createData("project.properties");
            lock = props.lock();
            out = props.getOutputStream(lock);
            FileUtil.copy(str, out);
            out.close();
            lock.releaseLock();
            str = getClass().getResourceAsStream("resources/project.xml");
            FileObject projxml = nbfolder.createData("project.xml");
            lock = projxml.lock();
            out = projxml.getOutputStream(lock);
            FileUtil.copy(str, out);
            out.close();
            lock.releaseLock();
            Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(ProjectFactory.class));
            Iterator it = res.allInstances().iterator();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            while (it.hasNext()) {
                ProjectFactory elem = (ProjectFactory) it.next();
                if (elem.getClass().getName().indexOf("AntBasedProjectFactorySingleton") != -1) {
                    return elem.loadProject(fileObject, projectState);
                }
            }
        }
        return null;
    }
    
    public void saveProject(Project project) throws IOException {
        // what to do here??
    }
    
}
