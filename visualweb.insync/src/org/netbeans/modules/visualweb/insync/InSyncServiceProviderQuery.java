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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync;

import java.io.IOException;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.faces.config.ManagedBean;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.insync.models.ManagedBeansModel;

/**
 * I represent an object that optimizes the queries that can be performed by the InSyncServiceProvider.
 * Since InSyncServiceProvider is stateless and that all information is project and model set related, it
 * was decided that having an object that can cache this information on a per query request from
 * InSyncServiceProvider would be more efficient than always computing all interdependent values.
 *
 * @author eric
 *
 */
public class InSyncServiceProviderQuery {

    protected boolean hasInitializedFileObject;
    protected FileObject fileObject;
    protected boolean hasInitializedDataObject;
    protected DataObject dataObject;

    protected boolean hasInitializedProject;
    protected Project project;
    protected boolean hasInitializedFacesModelSet;
    protected FacesModelSet facesModelSet;
    protected Boolean isInProjectJspRoot;
    protected Boolean isInProjectJavaRoot;
    protected Boolean isTemplateFileObject;

    public InSyncServiceProviderQuery(FileObject queriedFileObject, DataObject queriedDataObject) {
        super();
        if (queriedFileObject == null && queriedDataObject == null)
            throw new RuntimeException("Cannot pass in null for both");
        this.fileObject = queriedFileObject;
        hasInitializedFileObject = queriedFileObject != null;
        this.dataObject = queriedDataObject;
        hasInitializedDataObject = queriedDataObject != null;
    }

    public String getBeanName() {
        if (isInProjectJspRoot()) {
            // Compute the bean name by
            String relativePath = FileUtil.getRelativePath(getProjectJspRoot(), getQueriedFileObject());
            String result = relativePath.replace('/', '$');
            result = FacesUnit.fixPossiblyImplicitBeanName(result);
            return result;
        } else if (isInProjectJavaRoot()) {
            String relativePath = FileUtil.getRelativePath(getProjectJavaRoot(), getQueriedFileObject());
            String javaClassName = relativePath.replace('/', '.');
            ManagedBean managedBean = getManagedBeanWithBeanClass(javaClassName);
            if (managedBean != null)
                return managedBean.getName();
            return null;
        } else {
            // If file is not in either root, then assume we cannot compute bean name
            return null;
        }
    }

    public String getBeanNameViaJsp() {
        String result;
        if (isTemplateFileObject()) {
            result = "";
        } else {
            if (!isInProjectJspRoot())
                return null;
            // Compute the bean name by
            String relativePath = FileUtil.getRelativePath(getProjectJspRoot(), getQueriedFileObject().getParent());
            result = relativePath.replace('/', '$');
            if (result.length() > 0)
                result += "$";
        }
        result += getQueriedFileObject().getName(); // NOI18N
        result = FacesUnit.fixPossiblyImplicitBeanName(result);
        return result;
    }

    public DataObject getDataObject() {
        if (hasInitializedDataObject)
            return dataObject;
        hasInitializedDataObject = true;
        try {
            dataObject = DataObject.find(getFileObject());
        } catch (DataObjectNotFoundException e) {
        }
        return dataObject;
    }
    
    public FacesModelSet getFacesModelSet() {
        if (hasInitializedFacesModelSet)
            return facesModelSet;
        hasInitializedFacesModelSet = true;
        facesModelSet = FacesModelSet.getInstance(getProject());
        return facesModelSet;
    }
    
    public FileObject getFileObject() {
        if (hasInitializedFileObject)
            return fileObject;
        hasInitializedFileObject = true;
        DataObject object = getDataObject();
        if (object != null)
            fileObject = object.getPrimaryFile();
        return fileObject;
    }
    
    /**
     * This version is model safe, does not depend on models being ok.
     * 
     * @return
     */
    public DataObject getJavaDataObjectEquivalent(String originalName, boolean forceCreate) {
        FileObject fileEquivalent = getJavaFileObjectEquivalent(originalName, forceCreate);
        if (fileEquivalent == null)
            return null;
        try {
            DataObject result = DataObject.find(fileEquivalent);
            return result;
        } catch (DataObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * This version is model safe, does not depend on models being ok.
     * @param forceCreate TODO
     * 
     * @return
     */
    public FileObject getJavaFileObjectEquivalent(String originalName, boolean forceCreate) {
        if (getFileObject() == null)
            return null;
        if (isInProjectJavaRoot())
            return getFileObject();
        // If the file is anywhere outside of java and web, we can't compute this ?
        if (!isInProjectJspRoot())
            return null;
        if (getFileObject().isFolder()) {
            if (getFileObject() == getProjectJspRoot())
                return getProjectPageBeanRoot();
            FileObject file;
            if (originalName == null) {
                file = getFileObject();
            } else {
                file = getFileObject().getParent();
            }
            String relative = FileUtil.getRelativePath(getProjectJspRoot(), file);
            relative = getProjectPageBeanPackageNameWithSlash() + relative;
            file = getProjectJavaRoot().getFileObject(relative);
            if (file == null) {
                if (!forceCreate)
                    return null;
                try {
                    file = FileUtil.createFolder(getProjectJavaRoot(), relative);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            if (originalName != null) {
                file = file.getFileObject(originalName);
                if (file == null) {
                    if (!forceCreate)
                        return null;
                    try {
                        file = FileUtil.createFolder(file, originalName);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            return file;
        }
        String name;
        if (originalName == null)
            name = getFileObject().getName();
        else
            name = originalName;
        String relative = FileUtil.getRelativePath(getProjectJspRoot(), getFileObject().getParent());
        relative = getProjectPageBeanPackageNameWithSlash() + relative;
        FileObject file = getProjectJavaRoot().getFileObject(relative);
        if (file == null)
            return null;
        file = file.getFileObject(name, "java");
        return file;
    }
    
    public FileObject getJavaFolderForJsp() {
        return null;
    }

    public FileObject getJavaForJsp() {
        if (!isInProjectJspRoot())
            return null;
//        String beanName = getBeanNameViaJsp();
        throw new RuntimeException("No implemented yet !!!");
    }
    
    public ManagedBean getManagedBeanNamed(String name) {
        ManagedBeansModel model = getFacesModelSet().getManagedBeansModel();
        ManagedBean result = model.getManagedBean(name);
        return result;
    }
    /**
     * Return the single managed bean that has name for its managed-bean-class element.
     * If there are 0, OR more than 1, return null.
     * 
     * @param name
     * @return
     */
    public ManagedBean getManagedBeanWithBeanClass(String name) {
        ManagedBeansModel model = getFacesModelSet().getManagedBeansModel();
        ManagedBean[] managedBeans = model.getManagedBeans();
        ManagedBean result = null;
        for (int i=0; i < managedBeans.length; i++) {
            ManagedBean managedBean = managedBeans[i];
            if (name.equals(managedBean.getClazz())) {
                if (result == null)
                    result = managedBean;
                else
                    // More than 1
                    return null;
            }
        }
        // 0 or 1
        return result;
    }
    
    public Project getProject() {
        if (hasInitializedProject)
            return project;
        hasInitializedProject = true;
        project = FileOwnerQuery.getOwner(getQueriedFileObject());
        return project;
    }
    
    public String getProjectPageBeanPackageNameWithDot() {
        String result = JsfProjectUtils.getProjectProperty(getProject(), JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE);
        if (result.length() > 0)
            result += ".";
        return result;
    }
    
    public String getProjectPageBeanPackageNameWithSlash() {
        String result = getProjectPageBeanPackageNameWithDot();
        result = result.replace('.', '/');
        return result;
    }
    
    public FileObject getProjectJavaRoot() {
        Project project = getProject();
        if (project == null)
            return null;
        FileObject result = JsfProjectUtils.getSourceRoot(project);
        return result;
    }
    
    public FileObject getProjectJspRoot() {
        Project project = getProject();
        if (project == null)
            return null;
        FileObject result = JsfProjectUtils.getDocumentRoot(project);
        return result;
    }
    
    public FileObject getProjectPageBeanRoot() {
        Project project = getProject();
        if (project == null)
            return null;
        FileObject result = JsfProjectUtils.getPageBeanRoot(project);
        return result;
    }
    
    public FileObject getQueriedFileObject() {
        return fileObject;
    }

    public boolean isInProjectJavaRoot() {
        if (isInProjectJavaRoot == null) {
            FileObject root = getProjectJavaRoot();
            boolean result;
            if (root == null) {
                result = false;
            } else {
                result = root == getQueriedFileObject() || FileUtil.isParentOf(root, getQueriedFileObject());
            }
            isInProjectJavaRoot = Boolean.valueOf(result);
        }
        return isInProjectJavaRoot.booleanValue();
    }
    
    public boolean isInProjectJspRoot() {
        if (isInProjectJspRoot == null) {
            FileObject root = getProjectJspRoot();
            boolean result;
            if (root == null) {
                result = false;
            } else {
                result = (root == getQueriedFileObject()) || (FileUtil.isParentOf(root, getQueriedFileObject()));
            }
            isInProjectJspRoot = Boolean.valueOf(result);
        }
        return isInProjectJspRoot.booleanValue();
    }
    
    public boolean isTemplateFileObject() {
        if (isTemplateFileObject == null) {
            FileObject fileObject = getQueriedFileObject();
            Object attribute = fileObject.getAttribute(DataObject.PROP_TEMPLATE);
            boolean hasTemplateAttribute;
            boolean isTemplate;
            if(attribute instanceof Boolean) {
                hasTemplateAttribute = ((Boolean) attribute).booleanValue();
            } else {
                hasTemplateAttribute = false;
            }
            if(hasTemplateAttribute) {
                isTemplate = true;
            } else {
                FileObject templatesFolder = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject("Templates"); // NOI18N
                if(templatesFolder != null) {
                    isTemplate = FileUtil.isParentOf(templatesFolder, fileObject);
                } else {
                    isTemplate = false;
                }
            }
            isTemplateFileObject = Boolean.valueOf(isTemplate);
        }
        return isTemplateFileObject.booleanValue();
    }
    
}
