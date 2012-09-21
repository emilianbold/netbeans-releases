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
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * factory of bluej projects, only applied when netbeans related files are not created..
 * @author  Milos Kleint (mkleint@netbeans.org)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.ProjectFactory.class)
public class BluejProjectFactory implements ProjectFactory {
    /** Creates a new instance of BluejProjectFactory */
    public BluejProjectFactory() {
    }
    
    public boolean isProject(FileObject fileObject) {
        File projectDir = FileUtil.toFile(fileObject);
        if (projectDir == null) {
            return false;
        }
        if (fileObject.getFileObject("nbproject") != null && fileObject.getFileObject("build.xml") != null) {  // NOI18N
            return false;
        }
        File project = new File(projectDir, "bluej.pkg"); // NOI18N
        File parentProject = new File(projectDir.getParentFile(), "bluej.pkg");  // NOI18N
        return project.exists() && project.isFile() &&
                (!parentProject.exists()) && !"nbproject".equalsIgnoreCase(projectDir.getName()); // NOI18N
    }
    
    public Project loadProject(FileObject fileObject, ProjectState projectState) throws IOException {
        if (FileUtil.toFile(fileObject) == null) {
            return null;
        }
        if ("nbproject".equalsIgnoreCase(fileObject.getName())) {  //NOI18N
            return null;
        }
        FileObject projectFile = fileObject.getFileObject("bluej.pkg"); // NOI18N
        if (projectFile == null || !projectFile.isData()) {
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        if (projectDiskFile == null)  {
            return null;
        }
        if (fileObject.getParent().getFileObject("bluej.pkg") != null) {  // NOI18N
            return null;
        }
        
        String projectName = removeSpaces(fileObject.getName());
        
        if (fileObject.getFileObject("nbproject") == null) {  // NOI18N
            String specVersion = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
            FileObject nbfolder = fileObject.createFolder("nbproject");  // NOI18N
            InputStream str = BluejProjectFactory.class.getResourceAsStream("resources/build.xml");  // NOI18N
            FileObject buildxml = fileObject.createData("build.xml");  // NOI18N
            FileLock lock = buildxml.lock();
            OutputStream out = buildxml.getOutputStream(lock);
            copyAndReplaceInStream(str, out, "@PROJECTNAME@", projectName);  // NOI18N
            out.close();
            str.close();
            lock.releaseLock();
            str = BluejProjectFactory.class.getResourceAsStream("resources/build-impl.xml");  // NOI18N
            FileObject buildimplxml = nbfolder.createData("build-impl.xml");  // NOI18N
            lock = buildimplxml.lock();
            out = buildimplxml.getOutputStream(lock);
            copyAndReplaceInStream(str, out, "@PROJECTNAME@", projectName);  // NOI18N
            out.close();
            str.close();
            lock.releaseLock();
            str = BluejProjectFactory.class.getResourceAsStream("resources/project.properties");  // NOI18N
            FileObject props = nbfolder.createData("project.properties");  // NOI18N
            lock = props.lock();
            out = props.getOutputStream(lock);
            copyAndReplaceInStream(str, out, 
                new String[] { "@PROJECTNAME@", "@JAVAVERSION@" },
                new String[] { PropertyUtils.getUsablePropertyName(projectName), specVersion} );  // NOI18N
            out.close();
            str.close();
            lock.releaseLock();
            str = BluejProjectFactory.class.getResourceAsStream("resources/project.xml");  // NOI18N
            FileObject projxml = nbfolder.createData("project.xml");  // NOI18N
            lock = projxml.lock();
            out = projxml.getOutputStream(lock);
            FileUtil.copy(str, out);
            out.close();
            str.close();
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
                if (elem.getClass().getName().indexOf("AntBasedProjectFactorySingleton") != -1) {  // NOI18N
                    return elem.loadProject(fileObject, projectState);
                }
            }
        } else {
            //handle upgrading
            FileObject xml = fileObject.getFileObject(org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_XML_PATH);
            if (xml == null || !xml.isData()) {
                return null;
            }
            File f = FileUtil.toFile(xml);
            try {
                 Document doc = XMLUtil.parse(new InputSource(f.toURI().toString()), false, true, null, null);
                 NodeList nl = doc.getDocumentElement().getElementsByTagNameNS("http://www.netbeans.org/ns/bluej-project/1", "data");
                 if (nl != null && nl.getLength() > 0) {
                     //upgrade to /2
                     InputStream str = BluejProjectFactory.class.getResourceAsStream("resources/build-impl.xml");  // NOI18N
                     FileObject buildimplxml = fileObject.getFileObject("nbproject/build-impl.xml");  // NOI18N
                     FileLock lock = buildimplxml.lock();
                     OutputStream out = buildimplxml.getOutputStream(lock);
                     copyAndReplaceInStream(str, out, "@PROJECTNAME@", projectName);  // NOI18N
                     out.close();
                     str.close();
                     lock.releaseLock();
                     str = BluejProjectFactory.class.getResourceAsStream("resources/project.xml");  // NOI18N
                     FileObject projxml = xml;  // NOI18N
                     lock = projxml.lock();
                     out = projxml.getOutputStream(lock);
                     FileUtil.copy(str, out);
                     out.close();
                     str.close();
                     lock.releaseLock();
                 }
            } catch (IOException e) {
                //                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (SAXException e) {
                //                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
            if (elem.getClass().getName().indexOf("AntBasedProjectFactorySingleton") != -1) {  // NOI18N
                elem.saveProject(project);
            }
        }
    }
    private void copyAndReplaceInStream(InputStream is, OutputStream os, 
            String ptrn, String rpl) throws IOException {
        copyAndReplaceInStream(is, os, new String[] {ptrn}, new String[] {rpl});
    }
    
    private void copyAndReplaceInStream(InputStream is, OutputStream os, 
            String[] ptrn, String[] rpl) throws IOException {
        String sep = System.getProperty("line.separator");  // NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        String line = br.readLine();
        while (line != null) {
            if (line.indexOf('@') != -1) {
                for (int i = 0; i < ptrn.length; i++) {
                    line = line.replaceAll(ptrn[i], rpl[i]);
                }
            }
            writer.write(line + sep);
            line = br.readLine();
        }
        writer.flush();
    }
    
    public static String removeSpaces(String s) {
        int si = s.indexOf(" ");  // NOI18N
        if (si != -1) {
            return s.substring(0, si) + removeSpaces(s.substring(si + 1));
        } else {
            return s;
        }
    }
    
}
