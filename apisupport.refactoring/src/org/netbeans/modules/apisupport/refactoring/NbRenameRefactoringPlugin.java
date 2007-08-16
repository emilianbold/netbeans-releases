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

import java.io.IOException;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint - inspired by j2eerefactoring
 */
public class NbRenameRefactoringPlugin extends AbstractRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    private RenameRefactoring rename;
    
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
            rename = (RenameRefactoring)refactoring;
            
            Problem problem = null;
            Lookup lkp = rename.getRefactoringSource();
            
            TreePathHandle handle = lkp.lookup(TreePathHandle.class);
            if (handle != null) {
                InfoHolder infoholder = examineLookup(lkp);
                Project project = FileOwnerQuery.getOwner(handle.getFileObject());
                if (project == null || project.getLookup().lookup(NbModuleProvider.class) == null) {
                    // take just netbeans module development into account..
                    return null;
                }
                
                if (infoholder.isClass) {
                    checkManifest(project, infoholder.fullName, refactoringElements);
                    checkMetaInfServices(project, infoholder.fullName, refactoringElements);
                    checkLayer(project, infoholder.fullName, refactoringElements);
                }
                if (infoholder.isMethod) {
                    checkMethodLayer(infoholder, handle.getFileObject(), refactoringElements);
                }
            }
            NonRecursiveFolder nrf = lkp.lookup(NonRecursiveFolder.class);
            if (nrf != null) {
                Project project = FileOwnerQuery.getOwner(nrf.getFolder());
                if (project.getLookup().lookup(NbModuleProvider.class) == null) {
                    // take just netbeans module development into account..
                    return null;
                }
                Sources srcs = org.netbeans.api.project.ProjectUtils.getSources(project);
                SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (SourceGroup gr : grps) {
                    if (FileUtil.isParentOf(gr.getRootFolder(), nrf.getFolder())) {
                        String relPath = FileUtil.getRelativePath(gr.getRootFolder(), nrf.getFolder());
                        relPath.replace('/', '.');
                        checkPackageMetaInfServices(project, relPath, refactoringElements);
                        //TODO probably aslo manifest or layers.
                        //TODO how to distinguish single package from recursive folder?
                    }
                }
            }
            
            err.log("Gonna return problem: " + problem);
            return problem;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            semafor.set(null);
        }
    }
   
    protected RefactoringElementImplementation createManifestRefactoring(
            String fqname,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section) {
        return new ManifestRenameRefactoringElement(fqname, manifestFile, attributeValue,
                attributeKey, section);
    }
    
    protected RefactoringElementImplementation createMetaInfServicesRefactoring(String clazz, FileObject serviceFile, int line) {
        return new ServicesRenameRefactoringElement(clazz, serviceFile);
    }

    protected final void checkPackageMetaInfServices(Project project, String pack, RefactoringElementsBag refactoringElements) {
        FileObject services = Utility.findMetaInfServices(project);
        if (services == null) {
            return;
        }
        
        String name = pack;
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
    
    protected RefactoringElementImplementation createConstructorLayerRefactoring(String constructor, String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        // cannot rename a constructor.. is always a class rename
        return null;
    }
    
    protected RefactoringElementImplementation createLayerRefactoring(String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerClassRefactoringElement(fqname, handle, layerFileObject,layerAttribute);
    }
    
    protected RefactoringElementImplementation createMethodLayerRefactoring(String method, String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerMethodRefactoringElement(method, handle, layerFileObject, layerAttribute);
    }
    
    public abstract class LayerAbstractRefactoringElement extends AbstractRefactoringElement {
        
        protected FileObject layerFile;
        protected LayerUtils.LayerHandle handle;
        protected String oldFileName;
        protected String oldAttrName;
        protected String oldAttrValue;
        protected String valueType;
        
        public LayerAbstractRefactoringElement(
                LayerUtils.LayerHandle handle,
                FileObject layerFile,
                String attributeName) {
            this.layerFile = layerFile;
            this.parentFile = handle.getLayerFile();
            this.handle = handle;
            
            oldFileName = layerFile.getName();
            oldAttrName = attributeName;
            if (attributeName != null) {
                Object val = layerFile.getAttribute("literal:" + attributeName);
                if (val == null) {
                    throw new IllegalStateException();
                }
                if (val instanceof String) {
                    oldAttrValue = (String)val;
                    if (oldAttrValue.startsWith("new:")) {
                        oldAttrValue = ((String)val).substring("new:".length());
                        valueType = "newvalue:";
                    } else if (oldAttrValue.startsWith("method:")) {
                        oldAttrValue = ((String)val).substring("method:".length());
                        valueType = "methodvalue:";
                    }
                }
            }
        }
        
        protected void doAttributeValueChange(String newOne, String type) {
            boolean on = handle.isAutosave();
            if (!on) {
                //TODO is this a hack or not?
                handle.setAutosave(true);
            }
            try {
                layerFile.setAttribute(oldAttrName, (type != null ? type : "") + newOne);
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            if (!on) {
                handle.setAutosave(false);
            }
        }
        
        protected void doAttributeMove(String oldKey, String newKey) {
            boolean on = handle.isAutosave();
            if (!on) {
                //TODO is this a hack or not?
                handle.setAutosave(true);
            }
            try {
                Object obj = layerFile.getAttribute(oldKey);
                // now assume we're just moving ordering attributes..
                layerFile.setAttribute(oldKey, null);
                layerFile.setAttribute(newKey, obj);
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            if (!on) {
                handle.setAutosave(false);
            }
        }
        
        protected void doFileMove(String newName) {
            boolean on = handle.isAutosave();
            if (!on) {
                //TODO is this a hack or not?
                handle.setAutosave(true);
            }
            FileLock lock = null;
            try {
                lock = layerFile.lock();
                layerFile.rename(lock, newName, layerFile.getExt());
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
            if (!on) {
                handle.setAutosave(false);
            }
        }
    }
    
    
    public final class LayerMethodRefactoringElement extends LayerAbstractRefactoringElement {
        
        private String newAttrValue;
        private String method;
        
        public LayerMethodRefactoringElement(String method,
                LayerUtils.LayerHandle handle,
                FileObject layerFile,
                String attributeName) {
            super(handle, layerFile, attributeName);
            this.method = method;
        }
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(getClass(), "TXT_LayerMethodRename", oldAttrValue, rename.getNewName());
        }
        
        
        public void performChange() {
            // for methods the change can only be in the attribute value;
            newAttrValue = oldAttrValue.replaceAll("\\." + method + "$", "." + rename.getNewName());
            doAttributeValueChange(newAttrValue, valueType);
        }
        
        public void undoChange() {
            doAttributeValueChange(oldAttrValue, valueType);
        }
    
    }
    
    public final class LayerClassRefactoringElement extends LayerAbstractRefactoringElement {
        
        private String fqname;
        private String newAttrName;
        private String newAttrValue;
        private String newFileName;
        
        public LayerClassRefactoringElement(String fqname,
                LayerUtils.LayerHandle handle,
                FileObject layerFile,
                String attributeName) {
            super(handle, layerFile, attributeName);
            this.fqname = fqname;
        }
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (newFileName != null) {
                return NbBundle.getMessage(getClass(), "TXT_LayerFileRename", oldFileName, rename.getNewName());
            }
            return NbBundle.getMessage(getClass(), "TXT_LayerMethodRename", oldAttrValue, rename.getNewName());
        }
        
        
        public void performChange() {
            // for classes the change can be anywhere;
            String nm = fqname.substring(fqname.lastIndexOf('.'));

            if (oldAttrName == null) {
                // no attribute -> it's a filename change. eg. org-milos-kleint-MyInstance.instance
                newFileName = oldFileName.replaceAll("\\-" + nm + "$", "-" + rename.getNewName());
            } else {
                if (oldAttrName.indexOf(fqname.replace('.','-') + ".instance") > 0) {
                    //replacing the ordering attribute..
                    newAttrName = oldAttrName.replaceAll("-" + nm + "\\.", "-" + rename.getNewName() + ".");
                } else {
                    //replacing attr value probably in instanceCreate and similar
                    if (oldAttrValue != null) {
                        String toReplacePattern = nm;
                        newAttrValue = oldAttrValue.replaceAll(toReplacePattern, rename.getNewName());
                    }
                }
            }
            
            if (newAttrValue != null) {
                doAttributeValueChange(newAttrValue, valueType);
            }
            if (newAttrName != null) {
                doAttributeMove(oldAttrName, newAttrName);
            }
            if (newFileName != null) {
                doFileMove(newFileName);
            }
        }
        
        public void undoChange() {
            if (newAttrValue != null) {
                doAttributeValueChange(oldAttrValue, valueType);
            }
            if (newAttrName != null) {
                doAttributeMove(newAttrName, oldAttrName);
            }
            if (newFileName != null) {
                doFileMove(oldFileName);
            }
        }
        
    }
    
    
    public final class ManifestRenameRefactoringElement extends AbstractRefactoringElement  {
        
        private String attrName;
        private String sectionName = null;
        private String oldName;
        private String oldContent;
        private String newName;
        public ManifestRenameRefactoringElement(String fqname, FileObject parentFile, String attributeValue, String attributeName) {
            this.name = attributeValue;
            this.parentFile = parentFile;
            attrName = attributeName;
            oldName = fqname;
        }
        public ManifestRenameRefactoringElement(String fqname, FileObject parentFile, String attributeValue, String attributeName, String secName) {
            this(fqname, parentFile, attributeValue, attributeName);
            sectionName = secName;
        }
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(NbRenameRefactoringPlugin.class, "TXT_ManifestSectionRename", this.name, sectionName);
            }
            return NbBundle.getMessage(NbRenameRefactoringPlugin.class, "TXT_ManifestRename", this.name, attrName);
        }
        
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            oldContent = content;
            if (content != null) {
                String longName = oldName;
                if (newName == null) {
//                    System.out.println("new name=" + rename.getNewName());
                    newName = rename.getNewName();
                    newName = newName.replace('.', '/') + ".class"; //NOI18N
                }
                longName = longName.replace('.', '/') + ".class"; //NOI18N
                content = content.replaceAll(longName, newName);
                Utility.writeFileFromString(parentFile, content);
            }
        }
        
        public void undoChange() {
            if (oldContent != null) {
                Utility.writeFileFromString(parentFile, oldContent);
            }
        }
    }
    
    public final class ServicesRenameRefactoringElement extends AbstractRefactoringElement {
        
        private String oldName;
        private String oldContent;
        private String newName;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesRenameRefactoringElement(String fqname , FileObject file) {
            this.name = fqname.substring(fqname.lastIndexOf('.') + 1);
            parentFile = file;
            oldName = fqname;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(NbRenameRefactoringPlugin.class, "TXT_ServicesRename", this.name);
        }
        
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            oldContent = content;
            if (content != null) {
                if (newName == null) {
//                    System.out.println("new name=" + rename.getNewName());
                    newName = rename.getNewName();
                }
                content = content.replaceAll(name + "[ \\\r\\\n]*", newName + System.getProperty("line.separator")); // NOI18N
                Utility.writeFileFromString(parentFile, content);
            }
        }
        
        @Override
        public void undoChange() {
            if (oldContent != null) {
                Utility.writeFileFromString(parentFile, oldContent);
            }
        }
    }
    
    
    public final class ServicesPackageRenameRefactoringElement extends AbstractRefactoringElement {
        
        private String pack;
        private String oldName;
        /**
         * Creates a new instance of ServicesRenameRefactoringElement
         */
        public ServicesPackageRenameRefactoringElement(String pack, FileObject file) {
            this.name = pack;
            parentFile = file;
            this.pack = pack;
            oldName = pack;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(NbRenameRefactoringPlugin.class, "TXT_ServicesPackageRename", this.name);
        }
        
        @Override
        public void performChange() {
            String content = Utility.readFileIntoString(parentFile);
            if (content != null) {
                String longName = oldName;
                String newName = rename.getNewName();
                longName = longName.replaceAll("[.]", "\\."); // NOI18N
                content = content.replaceAll("^" + longName, newName); // NOI18N
                Utility.writeFileFromString(parentFile, content);
            }
        }
        
        public void undoChange() {
            //TODO
        }
        
    }

}
