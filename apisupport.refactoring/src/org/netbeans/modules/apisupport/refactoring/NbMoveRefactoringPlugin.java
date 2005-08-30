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

package org.netbeans.modules.apisupport.refactoring;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveClassRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Milos Kleint
 */
public class NbMoveRefactoringPlugin implements RefactoringPlugin {
    protected static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.refactoring");   // NOI18N
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    
    private MoveClassRefactoring refactoring;
    private Collection manifestRefactorings;
    private boolean firstManifestRefactoring = true;
    
    private HashMap oldManifests; /** <NBModuleProject, EditableManifest> */
    private EditableManifest targetManifest;
    
    /**
     * Creates a new instance of NbRenameRefactoringPlugin
     */
    public NbMoveRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = (MoveClassRefactoring)refactoring;
        
        manifestRefactorings = new ArrayList();
        oldManifests = new HashMap();
    }
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
        return null;
    }
    
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() != null) {
            return null;
        }
        semafor.set(new Object());
        try {
            Problem problem = null;
            Collection col = refactoring.getResources();
            Project targetProject = FileOwnerQuery.getOwner(refactoring.getTargetClassPathRoot());
            if (targetProject == null || ! (targetProject instanceof NbModuleProject)) {
                return problem;
            }
            NbModuleProject cachedProject = null;
            String[] cachedServices = null;
            FileObject[] cachedServicesFiles = null;
            Manifest cachedManifest = null;
            Iterator it = col.iterator();
            while (it.hasNext()) {
                Resource res = (Resource)it.next();
                FileObject fo = JavaModel.getFileObject(res);
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null && project instanceof NbModuleProject) {
                    if (cachedProject == null || cachedProject != project) {
                        cachedProject = (NbModuleProject)project;
                        cachedServices = loadMetaInfServices(cachedProject);
                        cachedManifest = cachedProject.getManifest();
                        FileObject services = Utility.findMetaInfServices(cachedProject);
                        if (services == null) {
                            cachedServicesFiles = new FileObject[0];
                        } else {
                            cachedServicesFiles = services.getChildren();
                        }
                    }
                    String name = res.getName();
                    String clazzName = name.replaceAll("\\.java$", ".class"); //NOI18N
                    String serviceName = name.replaceAll("\\.java$", "").replace('/', '.'); //NOI18N
                    JavaClass clazz = findClazz(res, serviceName);
                    //check services for this one
                    for (int i = 0; i < cachedServices.length; i++) {
                        serviceName = serviceName.replaceAll("[.]", "\\."); //NOI18N
                        if (cachedServices[i].matches("^" + serviceName + "[ \\\n]?")) { //NOI18N
                            RefactoringElementImplementation elem =
                                    new ServicesMoveRefactoringElement(clazz, cachedServicesFiles[i], cachedProject);
                            refactoringElements.add(refactoring, elem);
                        }
                    }
                    // check main attributes..
                    Attributes attrs = cachedManifest.getMainAttributes();
                    Iterator itx = attrs.entrySet().iterator();
                    while (itx.hasNext()) {
                        Map.Entry entry = (Map.Entry)itx.next();
                        String val = (String)entry.getValue();
                        if (val.indexOf(clazzName) != -1 || val.indexOf(clazzName) != -1) {
                            RefactoringElementImplementation elem =
                                    createManifestRefactoring(clazz, cachedProject.getManifestFile(),
                                    ((Attributes.Name)entry.getKey()).toString(), val, null, cachedProject);
                            refactoringElements.add(refactoring, elem);
                            manifestRefactorings.add(elem);
                        }
                    }
                    // check section attributes
                    Map entries = cachedManifest.getEntries();
                    if (entries != null) {
                        Iterator itf = entries.entrySet().iterator();
                        while (itf.hasNext()) {
                            Map.Entry secEnt = (Map.Entry)itf.next();
                            attrs = (Attributes)secEnt.getValue();
                            String val = (String)secEnt.getKey();
                            if (val.indexOf(clazzName) != -1) {
                                String section = attrs.getValue("OpenIDE-Module-Class"); //NOI18N
                                RefactoringElementImplementation elem =
                                        createManifestRefactoring(clazz, cachedProject.getManifestFile(), null, val, section, cachedProject);
                                refactoringElements.add(refactoring, elem);
                                manifestRefactorings.add(elem);
                            }
                        }
                    }
                }
            }
            // now check layer.xml and bundle file in manifest
            
            Iterator itd = refactoring.getOtherDataObjects().iterator();
            while (itd.hasNext()) {
                DataObject dobj = (DataObject)itd.next();
                Project project = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                if (project != null && project instanceof NbModuleProject) {
                    if (cachedProject == null || cachedProject != project) {
                        cachedProject = (NbModuleProject)project;
                        cachedServices = loadMetaInfServices(cachedProject);
                        cachedManifest = cachedProject.getManifest();
                    }
                }
                String packageName = findPackageName(cachedProject, dobj.getPrimaryFile());
                if (packageName != null) {
                    Iterator itf = cachedManifest.getMainAttributes().entrySet().iterator();
                    while (itf.hasNext()) {
                        Map.Entry ent = (Map.Entry)itf.next();
                        String val = (String)ent.getValue();
                        if (packageName.equals(val)) {
                            RefactoringElementImplementation elem = new ManifestMoveRefactoringElement(packageName, cachedProject.getManifestFile(), val,
                                    ((Attributes.Name)ent.getKey()).toString(), cachedProject, dobj.getPrimaryFile());
                            refactoringElements.add(refactoring, elem);
                            manifestRefactorings.add(elem);
                        }
                    }
                }
            }
            
            return problem;
        } finally {
            semafor.set(null);
        }
    }
    
    protected RefactoringElementImplementation createManifestRefactoring(JavaClass clazz,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section, NbModuleProject proj) {
        return new ManifestMoveRefactoringElement(clazz, manifestFile, attributeValue,
                attributeKey, section, proj);
    }
    
    private JavaClass findClazz(Resource res, String name) {
        Iterator itx = res.getClassifiers().iterator();
        while (itx.hasNext()) {
            JavaClass clzz = (JavaClass)itx.next();
            if (clzz.getName().equals(name)) {
                return clzz;
            }
        }
        //what to do now.. we should match always something, better to return wrong, than nothing?
        return (JavaClass)res.getClassifiers().iterator().next();
    }
    
    protected final String[] loadMetaInfServices(Project project) {
        FileObject services = Utility.findMetaInfServices(project);
        if (services == null) {
            return new String[0];
        }
        
        FileObject[] files = services.getChildren();
        String[] ret = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            ret[i] = Utility.readFileIntoString(files[i]);
        }
        return ret;
    }
    
    private static String findPackageName(NbModuleProject project, FileObject fo) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            if (grps[i].contains(fo)) {
                return FileUtil.getRelativePath(grps[i].getRootFolder(), fo);
            }
        }
        return null;
    }
    
    
    public final class ManifestMoveRefactoringElement extends AbstractRefactoringElement {
        
        private JavaClass clazz;
        private String attrName;
        private String sectionName = null;
        private String oldName;
        private NbModuleProject project;
        private FileObject movedFile = null;
        public ManifestMoveRefactoringElement(JavaClass clazz, FileObject parentFile,
                String attributeValue, String attributeName,
                NbModuleProject project) {
            this.name = attributeValue;
            this.clazz = clazz;
            this.parentFile = parentFile;
            this.project = project;
            attrName = attributeName;
            oldName = clazz.getName();
        }
        public ManifestMoveRefactoringElement(JavaClass clazz, FileObject parentFile,
                String attributeValue, String attributeName, String secName,
                NbModuleProject project) {
            this(clazz, parentFile, attributeValue, attributeName, project);
            sectionName = secName;
        }
        
        //for data objects that are not classes
        public ManifestMoveRefactoringElement(String pathName, FileObject parentFile,
                String attributeValue, String attributeName, NbModuleProject project, FileObject movedFile) {
            this.name = attributeValue;
            this.parentFile = parentFile;
            attrName = attributeName;
            oldName = pathName;
            this.project = project;
            this.movedFile = movedFile;
        }
        
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(NbMoveRefactoringPlugin.class, "TXT_ManifestSectionRename", this.name, sectionName);
            }
            return NbBundle.getMessage(NbMoveRefactoringPlugin.class, "TXT_ManifestRename", this.name, attrName);
        }
        
        public void performChange() {
            NbModuleProject targetProject = (NbModuleProject)FileOwnerQuery.getOwner(refactoring.getTargetClassPathRoot());
            if (firstManifestRefactoring) {
                // if this is the first manifest refactoring, check the list for non-enable ones and remove them
                Iterator it = manifestRefactorings.iterator();
                while (it.hasNext()) {
                    ManifestMoveRefactoringElement el = (ManifestMoveRefactoringElement)it.next();
                    if (!el.isEnabled()) {
                        it.remove();
                    }
                }
                FileObject fo = targetProject.getManifestFile();
                targetManifest = readManifest(fo);
                firstManifestRefactoring = false;
            }
            
            NbModuleProject sourceProject = project;
            EditableManifest sourceManifest = null;
            if (sourceProject == targetProject) {
                sourceManifest = targetManifest;
            } else {
                sourceManifest = (EditableManifest)oldManifests.get(sourceProject);
                if (sourceManifest == null) {
                    sourceManifest = readManifest(sourceProject.getManifestFile());
                    oldManifests.put(sourceProject, sourceManifest);
                }
            }
            // update section info
            if (sectionName != null) {
                String newSectionName = clazz.getName().replace('.', '/') + ".class"; //NOI18N
                targetManifest.addSection(newSectionName);
                Iterator it = sourceManifest.getAttributeNames(name).iterator();
                while (it.hasNext()) {
                    String secattrname = (String)it.next();
                    targetManifest.setAttribute(secattrname, sourceManifest.getAttribute(secattrname, name), newSectionName);
                }
                sourceManifest.removeSection(name);
            } else {
                // update regular attributes
                if (sourceManifest != targetManifest) {
                    sourceManifest.removeAttribute(attrName, null);
                }
                if (clazz != null) {
                    String newClassname = clazz.getName().replace('.','/') + ".class"; //NOI18N
                    targetManifest.setAttribute(attrName, newClassname, null);
                } else {
                    // mkleint - afaik this will get called only on folder rename.
                    String newPath = refactoring.getTargetPackageName(movedFile).replace('.','/') + "/" + movedFile.getNameExt(); //NOI18N
                    targetManifest.setAttribute(attrName, newPath, null);
                }
            }
            manifestRefactorings.remove(this);
            if (manifestRefactorings.isEmpty()) {
                // now write all the manifests that were edited.
                writeManifest(targetProject.getManifestFile(), targetManifest);
                Iterator it = oldManifests.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    EditableManifest man = (EditableManifest)entry.getValue();
                    NbModuleProject proj = (NbModuleProject)entry.getKey();
                    if (man == targetManifest) {
                        continue;
                    }
                    writeManifest(proj.getManifestFile(), man);
                }
            }
        }
    }
    
    public final class ServicesMoveRefactoringElement extends AbstractRefactoringElement {
        
        private JavaClass clazz;
        private String oldName;
        private NbModuleProject project;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesMoveRefactoringElement(JavaClass clazz, FileObject file, NbModuleProject proj) {
            this.name = clazz.getSimpleName();
            parentFile = file;
            this.clazz = clazz;
            oldName = clazz.getName();
            project = proj;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(NbMoveRefactoringPlugin.class, "TXT_ServicesRename", this.name);
        }
        
        public void performChange() {
            MoveClassRefactoring move = (MoveClassRefactoring)refactoring;
            NbModuleProject newproject = (NbModuleProject)FileOwnerQuery.getOwner(move.getTargetClassPathRoot());
            FileObject newFile = parentFile;
            if (newproject != project) {
                FileObject services = Utility.findMetaInfServices(newproject);
                try {
                    if (services == null) {
                        services = createMetaInf(newproject);
                    }
                    newFile = services.getFileObject(parentFile.getNameExt());
                    if (newFile == null) {
                        newFile = services.createData(parentFile.getNameExt());
                    }
                } catch (IOException ex) {
                    err.notify(ex);
                }
            }
            String oldcontent = Utility.readFileIntoString(parentFile);
            String longName = oldName;
            String newName = clazz.getName();
            if (oldcontent != null) {
                longName = longName.replaceAll("[.]", "\\."); //NOI18N
                if (newFile == parentFile) {
                    // same file, just replace
                    oldcontent = oldcontent.replaceAll("^" + longName, newName); //NOI18N
                    Utility.writeFileFromString(parentFile, oldcontent);
                } else {
                    // moving to a different file.
                    oldcontent = oldcontent.replaceAll("^" + longName + "[ \\\n]?", ""); //NOI18N
                    String newcontent = Utility.readFileIntoString(newFile);
                    newcontent = newName + "\n" + newcontent; // NOI18N
                    Utility.writeFileFromString(newFile, newcontent);
                    //check if we want to delete the old file or just update it.
                    StringTokenizer tok = new StringTokenizer(oldcontent, "\n"); //NOI18N
                    boolean hasMoreThanComments = false;
                    while (tok.hasMoreTokens()) {
                        String token = tok.nextToken().trim();
                        if (token.length() > 0 && (! Pattern.matches("^[#].*", token))) { //NOI18N
                            hasMoreThanComments = true;
                            break;
                        }
                    }
                    if (hasMoreThanComments) {
                        Utility.writeFileFromString(parentFile, oldcontent);
                    } else {
                        try {
                            parentFile.delete();
                        } catch (IOException exc) {
                            err.notify(exc);
                        }
                    }
                    
                }
            }
        }
    }
    
    private static FileObject createMetaInf(NbModuleProject project) throws IOException {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            FileObject fo = grps[i].getRootFolder().getFileObject("META-INF"); //NOI18N
            if (fo != null) {
                return fo.createFolder("services"); //NOI18N
            }
        }
        return grps[0].getRootFolder().createFolder("META-INF").createFolder("services"); //NOI18N
    }
    
    private static EditableManifest readManifest(FileObject fo) {
        InputStream str = null;
        try {
            str = fo.getInputStream();
            return  new EditableManifest(str);
        } catch (IOException exc) {
            err.notify(exc);
        } finally {
            if (str != null) {
                try {
                    str.close();
                } catch (IOException exc) {
                    err.notify(exc);
                }
            }
        }
        return new EditableManifest();
    }
    
    private static void writeManifest(FileObject fo, EditableManifest manifest) {
        OutputStream str = null;
        FileLock lock = null;
        try {
            lock = fo.lock();
            str = fo.getOutputStream(lock);
            manifest.write(str);
            
        } catch (IOException exc) {
            err.notify(exc);
        } finally {
            if (str != null) {
                try {
                    str.close();
                } catch (IOException exc) {
                    err.notify(exc);
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
    
}
