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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Milos Kleint - inspired by j2eerefactoring
 */
public class NbSafeDeleteRefactoringPlugin extends AbstractRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    /**
     * Creates a new instance of NbRenameRefactoringPlugin
     */
    public NbSafeDeleteRefactoringPlugin(AbstractRefactoring refactoring) {
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
            SafeDeleteRefactoring delete = (SafeDeleteRefactoring)refactoring;
            Problem problem = null;
            Element[] elements = delete.getElementsToDelete();
            for (int i = 0 ; i < elements.length; i++) {
                if (elements[i] instanceof JavaClass) {
                    JavaClass clzz = (JavaClass)elements[i];
                    Resource res = clzz.getResource();
                    FileObject fo = JavaModel.getFileObject(res);
                    Project project = FileOwnerQuery.getOwner(fo);
                    if (project != null && project instanceof NbModuleProject) {
                        checkMetaInfServices(project, clzz, refactoringElements);
                        checkManifest((NbModuleProject)project, clzz, refactoringElements);
                    }
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
        return new ManifestSafeDeleteRefactoringElement(clazz, manifestFile, attributeValue,
                attributeKey, section);
    }
    
    protected RefactoringElementImplementation createMetaInfServicesRefactoring(JavaClass clazz, FileObject serviceFile) {
        return new ServicesSafeDeleteRefactoringElement(clazz, serviceFile);
    }
    
    
    public final class ManifestSafeDeleteRefactoringElement extends AbstractRefactoringElement {
        
        private JavaClass clazz;
        private String attrName;
        private String sectionName = null;
        private String oldName;
        public ManifestSafeDeleteRefactoringElement(JavaClass clazz, FileObject parentFile, String attributeValue, String attributeName) {
            this.name = attributeValue;
            this.clazz = clazz;
            this.parentFile = parentFile;
            attrName = attributeName;
            oldName = clazz.getName();
        }
        public ManifestSafeDeleteRefactoringElement(JavaClass clazz, FileObject parentFile, String attributeValue, String attributeName, String secName) {
            this(clazz, parentFile, attributeValue, attributeName);
            sectionName = secName;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(getClass(), "TXT_ManifestSectionDelete", this.name, sectionName);
            }
            return NbBundle.getMessage(getClass(), "TXT_ManifestDelete", this.name, attrName);
        }
        
        public void performChange() {
            FileLock lock = null;
            OutputStream stream = null;
            InputStream instream = null;
            
            try {
                instream = parentFile.getInputStream();
                EditableManifest manifest = new EditableManifest(instream);
                instream.close();
                instream = null;
                if (sectionName != null) {
                    manifest.removeSection(name);
                } else {
                    manifest.removeAttribute(attrName, null);
                }
                lock = parentFile.lock();
                stream = parentFile.getOutputStream(lock);
                manifest.write(stream);
            } catch (FileNotFoundException ex) {
                //TODO
                err.notify(ex);
            } catch (IOException exc) {
                //TODO
                err.notify(exc);
            } finally {
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (IOException ex) {
                        err.notify(ex);
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        err.notify(ex);
                    }
                }
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
    }
    
    public final class ServicesSafeDeleteRefactoringElement extends AbstractRefactoringElement {
        
        private JavaClass clazz;
        private String oldName;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesSafeDeleteRefactoringElement(JavaClass clazz, FileObject file) {
            this.name = clazz.getSimpleName();
            parentFile = file;
            this.clazz = clazz;
            oldName = clazz.getName();
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(getClass(), "TXT_ServicesDelete", this.name);
        }
        
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            if (content != null) {
                String longName = oldName;
                longName = longName.replaceAll("[.]", "\\."); //NOI18N
                content = content.replaceAll("^" + longName + "[ \\\n]?", ""); //NOI18N
                // now check if there's more entries in the file..
                boolean hasMoreThanComments = false;
                StringTokenizer tok = new StringTokenizer(content, "\n"); //NOI18N
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken().trim();
                    if (token.length() > 0 && (! Pattern.matches("^[#].*", token))) { //NOI18N
                        hasMoreThanComments = true;
                        break;
                    }
                }
                if (hasMoreThanComments) {
                    Utility.writeFileFromString(parentFile, content);
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
