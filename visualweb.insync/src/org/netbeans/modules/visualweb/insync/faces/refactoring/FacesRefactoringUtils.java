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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

final class FacesRefactoringUtils {
    static final String FACES_SERVLET_URL_PATTERN_PREFIX = "/faces/"; // NOI18N
    
    private static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    private static final List<String> specialFolderNames = 
        Collections.unmodifiableList(Arrays.asList(
            new String[] {
                    "META-INF"   // NOI18N
                    ,"WEB-INF"   // NOI18N
            }
        ));
       
    static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
    }
    
    static boolean isJspFile(FileObject f) {
        return JSP_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
    }
    
    static boolean isSpecialFolderName(String folderName) {
        return specialFolderNames.contains(folderName);
    }
    
    static boolean isVisualWebJspFile(FileObject fileObject) {
        // Is it a Jsp file
        if (isFileUnderDocumentRoot(fileObject)) {
            if (isJspFile(fileObject)) {            
                FileObject javaFileObject = FacesModel.getJavaForJsp(fileObject);
                if (javaFileObject != null) {
                    return true;
                }                
            }
        }
        return false;
    }
    
    static boolean isFileUnderDocumentRoot(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) {
            return false;
        }
        // Is project a Jsf project
        if (JsfProjectUtils.isJsfProject(p)) {
            FileObject documentRoot = JsfProjectUtils.getDocumentRoot(p);
            if (documentRoot != null) {
                return FileUtil.isParentOf(documentRoot, fo);
            }
        }
        return false;
    }
    
    static boolean isFileUnderPageBeanRoot(FileObject fo) {
        if (isFileInJsfProject(fo)) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p==null) {
                return false;
            }
            FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(p);
            if (pageBeanRoot != null) {
                return FileUtil.isParentOf(pageBeanRoot, fo);
            }
        }
        return false;
    }

    static boolean isFolderParentOfPageBeanRoot(FileObject fo) {
    	if (fo.isFolder()) {
    		Project p = FileOwnerQuery.getOwner(fo);
            if (p==null) {
                return false;
            }
            FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(p);
            if (pageBeanRoot != null) {
                return FileUtil.isParentOf(fo, pageBeanRoot);
            }
    	}
        return false;
    }
        
    static boolean isFolderPageBeanRoot(FileObject fo) {
    	if (fo.isFolder()) {
    		Project p = FileOwnerQuery.getOwner(fo);
            if (p==null) {
                return false;
            }
            FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(p);
            if (pageBeanRoot != null) {
                return pageBeanRoot.equals(fo);
            }
    	}
        return false;
    }
    
    static boolean isFolderUnderPageBeanRoot(FileObject fo) {
    	if (fo.isFolder()) {
	        return isFileUnderPageBeanRoot(fo);
    	}
        return false;
    }
    
    static boolean isFileInJsfProject(FileObject f) {
        // Any project owner?
        Project p = FileOwnerQuery.getOwner(f);            
        if (p==null) {
            return false;
        }
        return JsfProjectUtils.isJsfProject(p);
    }
       
    static boolean isOpenProject(Project p) {
        // Any project owner?
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            // Is project open
            if (p==opened[i]) {                
                return true;
            }
        }
        return false;
    }
    
    static boolean isJavaFileObjectOfInterest(FileObject fo) {
        if (isFileUnderPageBeanRoot(fo)) {
            if (isOnSourceClasspath(fo)) {
                if (isJavaFile(fo)) {
                    FacesModel facesModel = FacesModel.getFacesModel(fo);
                    if (facesModel != null) {
                    	// ensure it is synced
                    	facesModel.sync();
                    	if (!facesModel.isBusted()) {
	                        JavaUnit javaUnit = facesModel.getJavaUnit();
	                        if (javaUnit != null) {
	                            JavaClass javaClass = javaUnit.getJavaClass();
	                            for (String className : FacesModel.managedBeanNames) {
	                                if (javaClass.isSubTypeOf(className)) {
	                                    return true;
	                                }
	                            }
	                        }
                    	}
                    }
                }
            }
        }
        return false;
    }
    
    static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) return false;
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p==opened[i]) {
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups("java"); // NOI18N
                for (int j = 0; j < gr.length; j++) {
                    if (fo==gr[j].getRootFolder()) return true;
                    if (FileUtil.isParentOf(gr[j].getRootFolder(), fo))
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    public static boolean isClasspathRoot(FileObject fo) {
        return fo.equals(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo));
    }
    
    public static String getPackageName(FileObject folder) {
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
        
    static String getName(Dictionary dict) {
        if (dict==null) 
            return null;
        return (String) dict.get("name"); //NOI18N
    }
    
    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
        assert files.length >0;
        Set<URL> dependentRoots = new HashSet();
        for (FileObject fo: files) {
            Project p = null;
            if (fo!=null)
                p=FileOwnerQuery.getOwner(fo);
            if (p!=null) {
                URL sourceRoot = URLMapper.findURL(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo), URLMapper.INTERNAL);
                dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
                for (SourceGroup root:ProjectUtils.getSources(p).getSourceGroups("java")) {
                    dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
                }
            } else {
                for(ClassPath cp: GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                    for (FileObject root:cp.getRoots()) {
                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
                    }
                }
            }
        }
        
        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
//        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath boot = ClassPath.getClassPath(files[0], ClassPath.BOOT);
        ClassPath compile = ClassPath.getClassPath(files[0], ClassPath.COMPILE);
        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
        return cpInfo;
    }
    
    public static FileObject getClassPathRoot(FileObject fileObject) {
        return ClassPath.getClassPath(fileObject, ClassPath.SOURCE).findOwnerRoot(fileObject);
    }
    
    /**
     * Method that allows to find its
     * CloneableEditorSupport from given DataObject
     * @return the support or null if the CloneableEditorSupport 
     * was not found
     * This method is hot fix for issue #53309
     * this methd was copy/pasted from OpenSupport.Env class
     * @param dob an instance of DataObject
     */
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Node.Cookie obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    /** This method returns a BaseDocument for the configuration file. If the configuration
     *  file is not opened, then the document is not created yet and this method push to load 
     *  the document to the memory. 
     */
    public static BaseDocument getBaseDocument(DataObject dataObject){
        BaseDocument document = null;
        
        if (dataObject != null){
            synchronized (dataObject){
                EditorCookie editor = dataObject.getLookup().lookup(EditorCookie.class);
                if (editor != null){
                    document = (BaseDocument)editor.getDocument();
                    if (document == null){
                        Task preparing = editor.prepareDocument();
                        preparing.waitFinished();
                        document = (BaseDocument)editor.getDocument();
                    }
                }
            }
        }
        return document;
    }
    
    private static final Logger LOGGER = Logger.getLogger(FacesRefactoringUtils.class.getName());
    
    static abstract class OccurrenceItem {
        // the faces configuration file
        protected FileObject config;
        protected String newValue;
        protected String oldValue;
        
        public OccurrenceItem(FileObject config, String newValue, String oldValue){
            this.config = config;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
        
        public FileObject getFacesConfig() {
            return config;
        }
        
        public String getElementText(){
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<font color=\"#0000FF\">"); // NOI18N
            stringBuffer.append("&lt;").append(getXMLElementName()).append("&gt;</font><b>");  // NOI18N
            stringBuffer.append(oldValue).append("</b><font color=\"#0000FF\">&lt;/").append(getXMLElementName()); // NOI18N
            stringBuffer.append("&gt;</font>"); // NOI18N
            return stringBuffer.toString();
        }
        
        protected abstract String getXMLElementName();
        
        public abstract void performRename();
        public abstract void undoRename();
        public abstract String getRenameMessage();     
    }
    
    static class ManagedBeanNameItem extends OccurrenceItem {
        private final ManagedBean bean;
        
        public ManagedBeanNameItem(FileObject config, ManagedBean bean, String newValue){
            super(config, newValue, bean.getManagedBeanName());
            this.bean = bean;
        }
        
        protected String getXMLElementName(){
            return "managed-bean-name"; //NOI18N
        }
        
        public void performRename(){
            changeBeanName(oldValue, newValue);
        }
        
        public void undoRename(){
            changeBeanName(newValue, oldValue);
        }        
        
        public String getRenameMessage(){
            return NbBundle.getMessage(FacesRefactoringUtils.class, "MSG_ManagedBeanName_Rename",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        private void changeBeanName(String oldBeanName, String newBeanName){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <ManagedBean> beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (oldBeanName.equals(managedBean.getManagedBeanName())){
                    facesConfig.getModel().startTransaction();
                    managedBean.setManagedBeanName(newBeanName);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
            }
        }
    }

    static class ManagedBeanClassItem extends OccurrenceItem {
        private final ManagedBean bean;
        
        public ManagedBeanClassItem(FileObject config, ManagedBean bean, String newValue){
            super(config, newValue, bean.getManagedBeanClass());
            this.bean = bean;
        }
        
        protected String getXMLElementName(){
            return "managed-bean-class"; //NOI18N
        }
        
        public void performRename(){
            changeBeanName(oldValue, newValue);
        }
        
        public void undoRename(){
            changeBeanName(newValue, oldValue);
        }        
        
        public String getRenameMessage(){
            return NbBundle.getMessage(FacesRefactoringUtils.class, "MSG_ManagedBeanClass_Rename",  //NOI18N
                    new Object[] { bean.getManagedBeanClass(), getElementText()});
        }
        
        private void changeBeanName(String oldBeanClass, String newBeanClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <ManagedBean> beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (oldBeanClass.equals(managedBean.getManagedBeanClass())){
                    facesConfig.getModel().startTransaction();
                    managedBean.setManagedBeanClass(newBeanClass);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
            }
        }
    }   
    
    static class NavigationFromViewIdItem extends OccurrenceItem {
        private final NavigationRule navigationRule;
        
        public NavigationFromViewIdItem(FileObject config, NavigationRule navigationRule, String newValue){
            super(config, newValue, navigationRule.getFromViewId());
            this.navigationRule = navigationRule;
        }
        
        protected String getXMLElementName(){
            return "from-view-id"; //NOI18N
        }
        
        public void performRename(){
            changeFromViewId(oldValue, newValue);
        }
        
        public void undoRename(){
            changeFromViewId(newValue, oldValue);
        }        
        
        public String getRenameMessage(){
            return NbBundle.getMessage(FacesRefactoringUtils.class, "MSG_NavigationFromViewId_Rename",  //NOI18N
                    new Object[] { navigationRule.getFromViewId(), getElementText()});
        }
        
        private void changeFromViewId(String oldFromViewId, String newFromViewId){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <NavigationRule> beans = facesConfig.getNavigationRules();
            for (Iterator<NavigationRule> it = beans.iterator(); it.hasNext();) {
                NavigationRule aNavigationRule = it.next();
                if (oldFromViewId.equals(aNavigationRule.getFromViewId())){
                    facesConfig.getModel().startTransaction();
                    aNavigationRule.setFromViewId(newFromViewId);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
            }
        }
    }
    
    static class NavigationToViewIdItem extends OccurrenceItem {
        private final NavigationCase navigationCase;
        
        public NavigationToViewIdItem(FileObject config, NavigationCase navigationCase, String newValue){
            super(config, newValue, navigationCase.getToViewId());
            this.navigationCase = navigationCase;
        }
        
        protected String getXMLElementName(){
            return "to-view-id"; //NOI18N
        }
        
        public void performRename(){
            changeToViewId(oldValue, newValue);
        }
        
        public void undoRename(){
            changeToViewId(newValue, oldValue);
        }        
        
        public String getRenameMessage(){
            return NbBundle.getMessage(FacesRefactoringUtils.class, "MSG_NavigationToViewId_Rename",  //NOI18N
                    new Object[] { navigationCase.getToViewId(), getElementText()});
        }
        
        private void changeToViewId(String oldFromViewId, String newFromViewId){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <NavigationRule> beans = facesConfig.getNavigationRules();
            for (Iterator<NavigationRule> it = beans.iterator(); it.hasNext();) {
                NavigationRule aNavigationRule = it.next();
                List <NavigationCase> navigationCases = aNavigationRule.getNavigationCases();
                for (Iterator<NavigationCase> ncit = navigationCases.iterator(); ncit.hasNext();) {
                    NavigationCase aNavigationCase = ncit.next();
                    if (oldFromViewId.equals(aNavigationCase.getToViewId())){
                        facesConfig.getModel().startTransaction();
                        aNavigationCase.setToViewId(newFromViewId);
                        facesConfig.getModel().endTransaction();
                        continue;
                    }
                }
            }
        }
    }
    
    static List <OccurrenceItem> getAllBeanNameOccurrences(WebModule webModule, String oldBeanName, String newBeanName){
        List result = new ArrayList();
        assert webModule != null;
        assert oldBeanName != null;
        assert newBeanName != null;
        
        LOGGER.fine("getAllOccurences("+ webModule.getDocumentBase().getPath() + ", " + oldBeanName + ", " + newBeanName + ")"); // NOI18N
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();                    
                    List<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                    for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                        ManagedBean managedBean = it.next();
                        if (oldBeanName.equals(managedBean.getManagedBeanName())) {
                            result.add(new ManagedBeanNameItem(configs[i], managedBean, newBeanName));
                        }
                    }
                }
            }
        }
        return result;
    }
    
    static List <OccurrenceItem> getAllBeanClassOccurrences(WebModule webModule, String oldBeanClass, String newBeanClass){
        List result = new ArrayList();
        assert webModule != null;
        assert oldBeanClass != null;
        assert newBeanClass != null;
        
        LOGGER.fine("getAllOccurences("+ webModule.getDocumentBase().getPath() + ", " + oldBeanClass + ", " + newBeanClass + ")"); // NOI18N
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();                    
                    List<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                    for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                        ManagedBean managedBean = it.next();
                        if (oldBeanClass.equals(managedBean.getManagedBeanClass())) {
                            result.add(new ManagedBeanClassItem(configs[i], managedBean, newBeanClass));
                        }
                    }
                }
            }
        }
        return result;
    }    

    static List <OccurrenceItem> getAllFromViewIdOccurrences(WebModule webModule, String oldFromViewId, String newFromViewId){
        List result = new ArrayList();
        assert webModule != null;
        assert oldFromViewId != null;
        assert newFromViewId != null;
        
        LOGGER.fine("getAllFromViewOccurrences("+ webModule.getDocumentBase().getPath() + ", " + oldFromViewId + ", " + newFromViewId + ")"); // NOI18N
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();                    
                    List<NavigationRule> navigationRules = facesConfig.getNavigationRules();
                    for (Iterator<NavigationRule> it = navigationRules.iterator(); it.hasNext();) {
                        NavigationRule navigationRule = it.next();
                        if (oldFromViewId.equals(navigationRule.getFromViewId())) {
                            result.add(new NavigationFromViewIdItem(configs[i], navigationRule, newFromViewId));
                        }
                    }
                }
            }
        }
        return result;
    }
    

    static List <OccurrenceItem> getAllToViewOccurrences(WebModule webModule, String oldToViewId, String newToViewId){
        List result = new ArrayList();
        assert webModule != null;
        assert oldToViewId != null;
        assert newToViewId != null;
        
        LOGGER.fine("getAllToViewOccurrences("+ webModule.getDocumentBase().getPath() + ", " + oldToViewId + ", " + newToViewId + ")"); // NOI18N
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();                    
                    List<NavigationRule> navigationRules = facesConfig.getNavigationRules();
                    for (Iterator<NavigationRule> it = navigationRules.iterator(); it.hasNext();) {
                        NavigationRule navigationRule = it.next();
                        List<NavigationCase> navigationCases = navigationRule.getNavigationCases();
                        for (Iterator<NavigationCase> ncit = navigationCases.iterator(); ncit.hasNext();) {
                            NavigationCase navigationCase = ncit.next();
                            if (oldToViewId.equals(navigationCase.getToViewId())) {
                                result.add(new NavigationToViewIdItem(configs[i], navigationCase, newToViewId));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * @return true if given str is null or empty.
     */
    static boolean isEmpty(String str){
        return str == null || "".equals(str.trim());
    }
    
    //
    // File Path utilities
    //
    
    // !EAT TODO: Cloned from com.sun.rave.xhtml.FragmentPanel
    static String computePathFromTo(FileObject fromFile, FileObject toFile) {
        ArrayList fromPathList = getPathList(fromFile);
        ArrayList toPathList = getPathList(toFile);
        StringBuffer stringBuffer = new StringBuffer(computePathFromTo(fromPathList, toPathList));
        if (toFile.isData()) {
            stringBuffer.append(toFile.getNameExt());
        }
        return stringBuffer.toString();
    }
    
    // !EAT TODO: Cloned from com.sun.rave.xhtml.FragmentPanel
    // this assume toPathList is for a directory
    static String computePathFromTo(ArrayList fromPathList, ArrayList toPathList) {
        int index = 0;
        // find the first non matching sub dir
        for (; index < fromPathList.size() && index < toPathList.size();
        index++) {
            if (!fromPathList.get(index).equals(toPathList.get(index))) {
                break;
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        // create a file that goes up to match found
        for (int i = index; i < fromPathList.size(); i++) {
            stringBuffer.append("../"); // NOI18N
        }
        // create a file that goes down from match found
        for (int i = index; i < toPathList.size(); i++) {
            stringBuffer.append(toPathList.get(i));
            stringBuffer.append("/"); // NOI18N
        }        
        return stringBuffer.toString();
    }

    // !EAT TODO: Cloned from com.sun.rave.xhtml.FragmentPanel
    static ArrayList getPathList(FileObject file) {
        if (!file.isFolder()) {
            file = file.getParent();
        }
        ArrayList result = new ArrayList(4);
        while (file != null) {
            result.add(file.getName());
            file = file.getParent();
        }
        Collections.reverse(result);
        return result;
    }
    
    // 
    static ArrayList getPathList(String fileName) {
        if (fileName.length() == 0) {
            return new ArrayList();
        }
        String[] fileNameParts = fileName.replace('\\','/').split("/");
        ArrayList result = new ArrayList(fileNameParts.length);
        for (int i = 0; i < fileNameParts.length; i++) {
            result.add(fileNameParts[i]);
        }        
        return result;
    }
    
    static String computeRelativePath(String from, String to) {
        if (!from.startsWith("/")) { from = "/" + from; }
        if (!to.startsWith("/")) { to = "/" + to; }
        List<String> fromList = new LinkedList<String>(Arrays.<String>asList(from.split("/")));
        List<String> toList = new LinkedList<String>(Arrays.<String>asList(to.split("/")));
        Collections.reverse(fromList);
        Collections.reverse(toList);
        String relativePath = computeRelativePath(fromList, toList);
        if (relativePath != null && relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);            
        }
        return relativePath;
    }
    
    /**
     * figure out a string representing the relative path of
     * 'f' with respect to 'r'
     * @param from home path. Must be a folder.
     * @param to path of file
     */
    static String computeRelativePath(List<String> from, List<String> to) {
        int i;
        int j;
        StringBuilder stringBuilder = new StringBuilder();
        // start at the beginning of the lists
        // iterate while both lists are equal
        i = from.size() - 1;
        j = to.size() - 1;

        // first eliminate common root
        while ((i >= 0) && (j >= 0) && (from.get(i).equals(to.get(j)))) {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for (; i >= 0; i--) {
            stringBuilder.append("../");
        }

        // for each level in the file path, add the path
        for (; j >= 1; j--) {
            stringBuilder.append(to.get(j) + "/");
        }

        // file name
        stringBuilder.append(to.get(j));
        return stringBuilder.toString();
    }

    private static Dialog waitDialog = null;
    private static RequestProcessor.Task waitTask = null;
    
    /**
     * This is a helper method to provide support for delaying invocations of actions
     * depending on java model. See <a href="http://java.netbeans.org/ui/waitscanfinished.html">UI Specification</a>.
     * <br>Behavior of this method is following:<br>
     * If classpath scanning is not in progress, runnable's run() is called. <br>
     * If classpath scanning is in progress, modal cancellable notification dialog with specified
     * tile is opened.
     * </ul>
     * As soon as classpath scanning finishes, this dialog is closed and runnable's run() is called.
     * This method must be called in AWT EventQueue. Runnable is performed in AWT thread.
     * 
     * @param runnable Runnable instance which will be called.
     * @param actionName Title of wait dialog.
     * @return true action was cancelled <br>
     *         false action was performed
     */
    public static boolean invokeAfterScanFinished(final Runnable runnable , final String actionName) {
        assert SwingUtilities.isEventDispatchThread();
        if (SourceUtils.isScanInProgress()) {
            final ActionPerformer ap = new ActionPerformer(runnable);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ap.cancel();
                    waitTask.cancel();
                }
            };
            JLabel label = new JLabel(getString("MSG_WaitScan"), javax.swing.UIManager.getIcon("OptionPane.informationIcon"), SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(12,12,11,11));
            DialogDescriptor dd = new DialogDescriptor(label, actionName, true, new Object[]{getString("LBL_CancelAction", new Object[]{actionName})}, null, 0, null, listener);
            waitDialog = DialogDisplayer.getDefault().createDialog(dd);
            waitDialog.pack();
            waitTask = RequestProcessor.getDefault().post(ap);
            waitDialog.setVisible(true);
            waitTask = null;
            waitDialog = null;
            return ap.hasBeenCancelled();
        } else {
            runnable.run();
            return false;
        }
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(FacesRefactoringUtils.class, key);
    }

    private static String getString(String key, Object values) {
        return new MessageFormat(getString(key)).format(values);
    }
    
    private static class ActionPerformer implements Runnable {
        private Runnable action;
        private boolean cancel = false;

        ActionPerformer(Runnable a) {
            this.action = a;
        }
        
        public boolean hasBeenCancelled() {
            return cancel;
        }
        
        public void run() {
            try {
                SourceUtils.waitScanFinished();
            } catch (InterruptedException ie) {
                Exceptions.printStackTrace(ie);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!cancel) {
                        if (waitDialog != null) {
                            waitDialog.setVisible(false);
                        }
                        action.run();
                    }
                }
            });
        }
        
        public void cancel() {
            assert SwingUtilities.isEventDispatchThread();
            // check if the scanning did not finish during cancel
            // invocation - in such case do not set cancel to true
            // and do not try to hide waitDialog window
            if (waitDialog != null) {
                cancel = true;
                waitDialog.setVisible(false);
            }
        }
    }
    
}
