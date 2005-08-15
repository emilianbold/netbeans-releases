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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.jmi.reflect.RefObject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaPackage;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveClassRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Milos Kleint - inspired by j2eerefactoring
 */
public class NbRenameRefactoringPlugin extends AbstractRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    /**
     * Creates a new instance of NbRenameRefactoringPlugin
     */
    public NbRenameRefactoringPlugin(AbstractRefactoring refactoring) {
        super(refactoring);
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
            RenameRefactoring rename = (RenameRefactoring)refactoring;
            Problem problem = null;
            RefObject refObject = (RefObject) rename.getRefactoredObject();
            if (refObject instanceof JavaClass) {
                JavaClass clzz = (JavaClass)refObject;
                Resource res = clzz.getResource();
                FileObject fo = JavaModel.getFileObject(res);
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null && project instanceof NbModuleProject) {
                    checkMetaInfServices(project, clzz, refactoringElements);
                    checkManifest((NbModuleProject)project, clzz, refactoringElements);
                }
            }
            if (refObject instanceof JavaPackage) {
                JavaPackage pack = (JavaPackage)refObject;
                Resource res = pack.getResource();
                FileObject fo = JavaModel.getFileObject(res);
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null && project instanceof NbModuleProject) {
                    checkMetaInfServices(project, pack, refactoringElements);
                }
            }
            
            err.log("Gonna return problem: " + problem);
            return problem;
        } finally {
            semafor.set(null);
        }
    }
   
    protected RefactoringElementImplementation createManifestRefactoring(JavaClass clazz,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section) {
        return new ManifestRenameRefactoringElement(clazz, manifestFile, attributeValue,
                attributeKey, section);
    }
    
    protected RefactoringElementImplementation createMetaInfServicesRefactoring(JavaClass clazz, FileObject serviceFile) {
        return new ServicesRenameRefactoringElement(clazz, serviceFile);
    }

    protected final void checkMetaInfServices(Project project, JavaPackage pack, RefactoringElementsBag refactoringElements) {
        FileObject services = Utility.findMetaInfServices(project);
        if (services == null) {
            return;
        }
        
        String name = pack.getName();
        // Easiest to check them all; otherwise would need to find all interfaces and superclasses:
        FileObject[] files = services.getChildren();
        for (int i = 0; i < files.length; i++) {
            int line = checkContentOfFile(files[i], name);
            if (line != -1) {
                RefactoringElementImplementation elem = 
                        new ServicesPackageRenameRefactoringElement(pack, files[i]);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
            }
        }
    }    
    
    
    
    public final class ManifestRenameRefactoringElement extends AbstractRefactoringElement {
        
        private JavaClass clazz;
        private String attrName;
        private String sectionName = null;
        private String oldName;
        public ManifestRenameRefactoringElement(JavaClass clazz, FileObject parentFile, String attributeValue, String attributeName) {
            this.name = attributeValue;
            this.clazz = clazz;
            this.parentFile = parentFile;
            attrName = attributeName;
            oldName = clazz.getName();
        }
        public ManifestRenameRefactoringElement(JavaClass clazz, FileObject parentFile, String attributeValue, String attributeName, String secName) {
            this(clazz, parentFile, attributeValue, attributeName);
            sectionName = secName;
        }
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(getClass(), "TXT_ManifestSectionRename", this.name, sectionName);
            }
            return NbBundle.getMessage(getClass(), "TXT_ManifestRename", this.name, attrName);
        }
        
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            if (content != null) {
                String longName = oldName;
                String newName = clazz.getName();
                longName = longName.replace('.', '/') + ".class"; //NOI18N
                newName = newName.replace('.', '/') + ".class"; //NOI18N
                content = content.replaceAll(longName, newName);
                Utility.writeFileFromString(parentFile, content);
            }
            
        }
    }
    
    public final class ServicesRenameRefactoringElement extends AbstractRefactoringElement {
        
        private JavaClass clazz;
        private String oldName;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesRenameRefactoringElement(JavaClass clazz, FileObject file) {
            this.name = clazz.getSimpleName();
            parentFile = file;
            this.clazz = clazz;
            oldName = clazz.getName();
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(getClass(), "TXT_ServicesRename", this.name);
        }
        
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            if (content != null) {
                String longName = oldName;
                String newName = clazz.getName();
                longName = longName.replaceAll("[.]", "\\.");
                content = content.replaceAll("^" + longName + "[ \\\n]?", newName + "\n");
                Utility.writeFileFromString(parentFile, content);
            }
        }
    }
    
    
    public final class ServicesPackageRenameRefactoringElement extends AbstractRefactoringElement {
        
        private JavaPackage pack;
        private String oldName;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesPackageRenameRefactoringElement(JavaPackage pack, FileObject file) {
            this.name = pack.getName();
            parentFile = file;
            this.pack = pack;
            oldName = pack.getName();
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(getClass(), "TXT_ServicesPackageRename", this.name);
        }
        
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            if (content != null) {
                String longName = oldName;
                String newName = pack.getName();
                longName = longName.replaceAll("[.]", "\\.");
                content = content.replaceAll("^" + longName, newName);
                Utility.writeFileFromString(parentFile, content);
            }
        }
    }

}
