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
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.jmi.reflect.RefObject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
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
public class NbWhereUsedRefactoringPlugin extends AbstractRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    
    /**
     * Creates a new instance of NbWhereUsedRefactoringPlugin
     */
    public NbWhereUsedRefactoringPlugin(AbstractRefactoring refactoring) {
        super(refactoring);
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
                    checkLayer((NbModuleProject)project, clzz, refactoringElements);
                }
            }
            if (refObject instanceof Method) {
                Method method = (Method)refObject;
                problem = checkLayer(method, refactoringElements);
            }
            if (refObject instanceof Constructor) {
                Constructor constructor = (Constructor)refObject;
                problem = checkLayer(constructor, refactoringElements);
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
                                                                         String section) 
    {
        return new ManifestWhereUsedRefactoringElement(attributeValue, manifestFile, 
                                                       attributeKey, section);
    }

    protected RefactoringElementImplementation createMetaInfServicesRefactoring(JavaClass clazz, FileObject serviceFile) {
        return new ServicesWhereUsedRefactoringElement(clazz.getSimpleName(), serviceFile);
    }
    
    protected RefactoringElementImplementation createLayerRefactoring(JavaClass clazz,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerWhereUserRefElement(handle.getLayerFile(), layerFileObject, layerAttribute);
    }
    
    protected RefactoringElementImplementation createLayerRefactoring(Method method,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerWhereUserRefElement(handle.getLayerFile(), layerFileObject, layerAttribute);
    }
    
    protected RefactoringElementImplementation createLayerRefactoring(Constructor constructor,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerWhereUserRefElement(handle.getLayerFile(), layerFileObject, layerAttribute);
    }
    
    public final class LayerWhereUserRefElement extends AbstractRefactoringElement {
        private String attr;
        private String path;
        private String attrValue;
        public LayerWhereUserRefElement(FileObject fo, FileObject layerFo, String attribute) {
            parentFile = fo;
            attr = attribute;
            this.path = layerFo.getPath();
            if (attr != null) {
                Object vl = layerFo.getAttribute("literal:" + attr); //NOI18N
                if (vl instanceof String) {
                    attrValue = (String)vl;
                }
            }
        }
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (attr != null && attrValue != null) {
                return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_LayerAttrValueWhereUsed", path, attr, attrValue);
            }
            if (attr != null) {
                return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_LayerAttrWhereUsed", path, attr);
            }
            return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_LayerWhereUsed", path);
        }
    }
    
    
    public final class ManifestWhereUsedRefactoringElement extends AbstractRefactoringElement {
        
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
                return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_ManifestSectionWhereUsed", this.name, sectionName);
            }
            return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_ManifestWhereUsed", this.name, attrName);
        }
    }
    
    public final class ServicesWhereUsedRefactoringElement extends AbstractRefactoringElement {
        
        
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
            return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_ServicesWhereUsed", this.name);
        }
    }
    
}
