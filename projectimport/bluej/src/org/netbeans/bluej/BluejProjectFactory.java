package org.netbeans.bluej;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

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
        
        String projectName = removeSpaces(fileObject.getName());
        
        if (fileObject.getFileObject("nbproject") == null) {
            FileObject nbfolder = fileObject.createFolder("nbproject");
            InputStream str = getClass().getResourceAsStream("resources/build.xml");
            FileObject buildxml = fileObject.createData("build.xml");
            FileLock lock = buildxml.lock();
            OutputStream out = buildxml.getOutputStream(lock);
            copyAndReplaceInStream(str, out, "@PROJECTNAME@", projectName);
            out.close();
            lock.releaseLock();
            str = getClass().getResourceAsStream("resources/build-impl.xml");
            FileObject buildimplxml = nbfolder.createData("build-impl.xml");
            lock = buildimplxml.lock();
            out = buildimplxml.getOutputStream(lock);
            copyAndReplaceInStream(str, out, "@PROJECTNAME@", projectName);
            out.close();
            lock.releaseLock();
            str = getClass().getResourceAsStream("resources/project.properties");
            FileObject props = nbfolder.createData("project.properties");
            lock = props.lock();
            out = props.getOutputStream(lock);
            copyAndReplaceInStream(str, out, "@PROJECTNAME@", projectName);
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
        // when creating a project through this factory, route the saving to the ant based factory.
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(ProjectFactory.class));
        Iterator it = res.allInstances().iterator();
        while (it.hasNext()) {
            ProjectFactory elem = (ProjectFactory) it.next();
            if (elem.getClass().getName().indexOf("AntBasedProjectFactorySingleton") != -1) {
                elem.saveProject(project);
            }
        }
    }
    
    private void copyAndReplaceInStream(InputStream is, OutputStream os, 
            String ptrn, String rpl) throws IOException {
        String sep = System.getProperty("line.separator");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Writer writer = new OutputStreamWriter(os);
        String line = br.readLine();
        while (line != null) {
            if (line.indexOf('@') != -1) {
                line = line.replaceAll(ptrn, rpl);
            }
            writer.write(line + sep);
            line = br.readLine();
        }
        writer.flush();
    }
    
    public static String removeSpaces(String s) {
        int si = s.indexOf(" ");
        if (si != -1) {
            return s.substring(0, si) + removeSpaces(s.substring(si + 1));
        } else {
            return s;
        }
    }
    
}
