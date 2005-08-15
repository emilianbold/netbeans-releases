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
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
public abstract class AbstractRefactoringPlugin implements RefactoringPlugin {
    protected static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.refactoring");   // NOI18N
    
    protected AbstractRefactoring refactoring;
    /** Creates a new instance of AbstractRefactoringPlugin */
    public AbstractRefactoringPlugin(AbstractRefactoring refactoring) {
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
    
    /**
     * returns the line number in the file if found, otherwise -1
     */
    protected final int checkContentOfFile(FileObject fo, String classToLookFor) {
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
    
    protected final void checkManifest(NbModuleProject project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
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
                        createManifestRefactoring(clzz, project.getManifestFile(), ((Attributes.Name)entry.getKey()).toString(), val, null);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
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
                            createManifestRefactoring(clzz, project.getManifestFile(), null, val, section);
                    if (elem != null) {
                        refactoringElements.add(refactoring, elem);
                    }
                }
            }
        }
    }
    
    protected final void checkMetaInfServices(Project project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
        FileObject services = Utility.findMetaInfServices(project);
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
                        createMetaInfServicesRefactoring(clzz, files[i]);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
            }
        }
    }
    
    protected abstract RefactoringElementImplementation createMetaInfServicesRefactoring(JavaClass clazz,
            FileObject serviceFile);
    
    protected abstract RefactoringElementImplementation createManifestRefactoring(JavaClass clazz,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section);
    
    
    

}
