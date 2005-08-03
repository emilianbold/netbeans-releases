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
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint - inspired by j2eerefactoring
 */
public class NbWhereUsedRefactoringPlugin implements RefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    private AbstractRefactoring refactoring;
    private static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.j2ee.refactoring.whereused");   // NOI18N
    
    /**
     * Creates a new instance of NbWhereUsedRefactoringPlugin
     */
    public NbWhereUsedRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
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
            WhereUsedQuery whereUsedRefactor = ((WhereUsedQuery)refactoring);
            
            if (!whereUsedRefactor.isFindUsages()) {
                return null;
            }
            
            Problem problem = null;
            RefObject refObject = (RefObject) whereUsedRefactor.getRefactoredObject();
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
            
            
            err.log("Gonna return problem: " + problem);
            return problem;
        } finally {
            semafor.set(null);
        }
    }
    
    private void checkMetaInfServices(Project project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
        FileObject services = findMetaInfServices(project);
        if (services == null) {
            return;
        }
        String name = clzz.getName();
        // Easiest to check them all; otherwise would need to find all interfaces and superclasses:
        FileObject[] files = services.getChildren();
        for (int i = 0; i < files.length; i++) {
            int line = checkContentOfFile(files[i], name);
            if (line != -1) {
                RefactoringElementImplementation elem =
                        new ServicesWhereUsedRefactoringElement(clzz.getSimpleName(), files[i]);
                refactoringElements.add(refactoring, elem);
            }
        }
    }
    
    
    private FileObject findMetaInfServices(Project project) {
        Sources srcs = (Sources)project.getLookup().lookup(Sources.class);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            FileObject fo = grps[i].getRootFolder().getFileObject("META-INF/services");
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }
    
    /**
     * returns the line number in the file if found, otherwise -1
     */
    private int checkContentOfFile(FileObject fo, String classToLookFor) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(fo.getInputStream(), "UTF-8")); // NOI18N
            String line = reader.readLine();
            int counter = 0;
            while (line != null) {
                if (line.indexOf(classToLookFor) != -1) {
                    return counter;
                }
                counter = counter + 1;
                line = reader.readLine();
            }
        } catch (IOException exc) {
            //TODO
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
        return -1;
    }
    
    private void checkManifest(NbModuleProject project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
        String name = clzz.getName();
        String pathName = name.replace('.', '/') + ".class"; //NOI18N
        Manifest mf = project.getManifest();
        Attributes attrs = mf.getMainAttributes();
        Iterator it = attrs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String val = (String)entry.getValue();
            if (val.indexOf(name) != -1 || val.indexOf(pathName) != -1) {
                RefactoringElementImplementation elem =
                   new ManifestWhereUsedRefactoringElement(val, project.getManifestFile(), 
                                           ((Attributes.Name)entry.getKey()).toString());
                refactoringElements.add(refactoring, elem);
            }
        }
        Map entries = mf.getEntries();
        if (entries != null) {
            it = entries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry secEnt = (Map.Entry)it.next();
                attrs = (Attributes)secEnt.getValue();
                String val = (String)secEnt.getKey();
                    if (val.indexOf(name) != -1 || val.indexOf(pathName) != -1) {
                        String section = attrs.getValue("OpenIDE-Module-Class"); //NOI18N
                        RefactoringElementImplementation elem =
                           new ManifestWhereUsedRefactoringElement(val, project.getManifestFile(), 
                                                   null, section);
                        refactoringElements.add(refactoring, elem);
                    }
            }
        }
    }
    
    public final class ManifestWhereUsedRefactoringElement extends AbstractWhereUsedRefactoringElement {
        
        private String attrName;
        private String sectionName = null;
        public ManifestWhereUsedRefactoringElement(String name, FileObject parentFile, String attributeName) {
            this.name = name;
            this.parentFile = parentFile;
            attrName = attributeName;
        }
        public ManifestWhereUsedRefactoringElement(String name, FileObject parentFile, String attributeName, String secName) {
            this(name, parentFile, attributeName);
            sectionName = secName;
        }
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(getClass(), "TXT_ManifestSectionWhereUsed", this.name, sectionName);
            }
            return NbBundle.getMessage(getClass(), "TXT_ManifestWhereUsed", this.name, attrName);
        }
    }
    
    public final class ServicesWhereUsedRefactoringElement extends AbstractWhereUsedRefactoringElement {
        
        
        /**
         * Creates a new instance of ServicesWhereUsedRefactoringElement
         */
        public ServicesWhereUsedRefactoringElement(String name, FileObject file) {
            this.name = name;
            parentFile = file;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(getClass(), "TXT_ServicesWhereUsed", this.name);
        }
    }
    
}
