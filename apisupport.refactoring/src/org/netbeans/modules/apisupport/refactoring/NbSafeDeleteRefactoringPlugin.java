/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.ExternalChange;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
                        checkLayer((NbModuleProject)project, clzz, refactoringElements);
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
        return new ManifestSafeDeleteRefactoringElement(manifestFile, attributeValue,
                attributeKey, section);
    }
    
    protected RefactoringElementImplementation createMetaInfServicesRefactoring(JavaClass clazz, FileObject serviceFile, int line) {
        return new ServicesSafeDeleteRefactoringElement(clazz, serviceFile);
    }

    protected RefactoringElementImplementation createLayerRefactoring(
            Constructor constructor,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerSafeDeleteRefactoringElement(constructor.getName(), handle, layerFileObject);
            
    }

    protected RefactoringElementImplementation createLayerRefactoring(
            JavaClass clazz, 
            LayerUtils.LayerHandle handle, 
            FileObject layerFileObject, 
            String layerAttribute) {
        return new LayerSafeDeleteRefactoringElement(clazz.getSimpleName(), handle, layerFileObject, layerAttribute);
    
    }

    protected RefactoringElementImplementation createLayerRefactoring(
            Method method, 
            LayerUtils.LayerHandle handle, 
            FileObject layerFileObject, 
            String layerAttribute) {
        return new LayerSafeDeleteRefactoringElement(method.getName(), handle, layerFileObject);
    }
    
    
    public final class ManifestSafeDeleteRefactoringElement extends AbstractRefactoringElement implements ExternalChange {
        
        private String attrName;
        private String sectionName = null;
        private String oldContent;
        
        public ManifestSafeDeleteRefactoringElement(FileObject parentFile, String attributeValue, String attributeName) {
            this.name = attributeValue;
            this.parentFile = parentFile;
            attrName = attributeName;
            // read old content here. in the unprobable case when 2 classes are to be removed
            // and both are placed in same services file, we need the true original content
            oldContent = Utility.readFileIntoString(parentFile);
        }
        public ManifestSafeDeleteRefactoringElement(FileObject parentFile, String attributeValue, String attributeName, String secName) {
            this(parentFile, attributeValue, attributeName);
            sectionName = secName;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_ManifestSectionDelete", this.name, sectionName);
            }
            return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_ManifestDelete", this.name, attrName);
        }
        
        public void performChange() {
            JavaMetamodel.getManager().registerExtChange(this);
        }
        
        public void performExternalChange() {
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
        
        public void undoExternalChange() {
            if (oldContent != null) {
                Utility.writeFileFromString(parentFile, oldContent);
            }
        }
        
    }
    
    public final class ServicesSafeDeleteRefactoringElement extends AbstractRefactoringElement implements ExternalChange {
        
        private String oldName;
        private String oldContent;
        private File parent;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesSafeDeleteRefactoringElement(JavaClass clazz, FileObject file) {
            this.name = clazz.getSimpleName();
            parentFile = file;
            oldName = clazz.getName();
            // read old content here. in the unprobable case when 2 classes are to be removed
            // and both are placed in same services file, we need the true original content
            oldContent = Utility.readFileIntoString(parentFile);
            parent = FileUtil.toFile(parentFile);
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_ServicesDelete", this.name);
        }
        
        public void performChange() {
            JavaMetamodel.getManager().registerExtChange(this);
        }
        
        public void performExternalChange() {
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
        
        public void undoExternalChange() {
            try {
                if (oldContent != null) {
                    if (!parent.exists()) {
                        FileObject fo = FileUtil.toFileObject(parent.getParentFile());
                        if (fo != null) {
                            parentFile = fo.createData(parent.getName());
                        }
                    }
                    Utility.writeFileFromString(parentFile, oldContent);
                }
            } catch (IOException exc) {
                err.notify(exc);
            }
        }
        
    }
    
    public final class LayerSafeDeleteRefactoringElement extends AbstractRefactoringElement  implements ExternalChange {
        
        private FileObject layerFO;
        private LayerUtils.LayerHandle handle;

        private String attribute;
        /**
         * Creates a new instance of LayerRenameRefactoringElement
         */
        public LayerSafeDeleteRefactoringElement(String name, LayerUtils.LayerHandle handle, FileObject layerFO, String attr) {
            this(name, handle, layerFO);
            attribute = attr;
        }
        
        public LayerSafeDeleteRefactoringElement(String name, LayerUtils.LayerHandle handle, FileObject layerFO) {
            this.name = name;
            this.handle = handle;
            parentFile = handle.getLayerFile();
            this.layerFO = layerFO;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_LayerDelete", layerFO.getNameExt());
        }
        
        public void performChange() {
            JavaMetamodel.getManager().registerExtChange(this);
        }
        
        public void performExternalChange() {
            boolean on = handle.isAutosave();
            if (!on) {
                //TODO is this a hack or not?
                handle.setAutosave(true);
            }
            try {
                if (attribute != null) {
                    layerFO.setAttribute(attribute, null);
                    if ("originalFile".equals(attribute)) {
                        layerFO.delete();
                    }
                } else {
                    layerFO.delete();
                }
                deleteEmptyParent(layerFO.getParent());
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            } 
            if (!on) {
                handle.setAutosave(false);
            }
            
        }

        private void deleteEmptyParent(FileObject parent) throws IOException {
            if (parent != null) {
                if (!parent.getChildren(true).hasMoreElements() && 
                        !parent.getAttributes().hasMoreElements()) {
                    FileObject parentToDel = parent.getParent();
                    parent.delete();
                    deleteEmptyParent(parentToDel);
                } 
            }
        }
        
        public void undoExternalChange() {
        }
        
    }

}
