/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.insync;

import java.io.IOException;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.insync.models.FacesConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;

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
                return managedBean.getManagedBeanName();
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
        FacesConfigModel model = getFacesModelSet().getFacesConfigModel();
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
        FacesConfigModel model = getFacesModelSet().getFacesConfigModel();
        ManagedBean[] managedBeans = model.getManagedBeans();
        ManagedBean result = null;
        for (int i=0; i < managedBeans.length; i++) {
            ManagedBean managedBean = managedBeans[i];
            if (name.equals(managedBean.getManagedBeanClass())) {
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
                FileObject templatesFolder = FileUtil.getConfigFile("Templates"); // NOI18N
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
