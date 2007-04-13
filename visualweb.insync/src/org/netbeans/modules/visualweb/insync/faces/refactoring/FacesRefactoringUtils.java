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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Task;

final class FacesRefactoringUtils {
    
    private static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    private static final List<String> specialFolderNames = 
        Collections.unmodifiableList(Arrays.asList(
            new String[] {
                    "META-INF"   // NOI18N
                    ,"WEB-INF"   // NOI18N
                    ,"resources" // NOI18N
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
                    if (facesModel != null && !facesModel.isBusted()) {
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
    
    /** The constant from XML editor
     */
    // The constant are taken from class org.netbeans.modules.xml.text.syntax.XMLTokenIDs
    protected final static int XML_ELEMENT = 4;
    protected final static int XML_TEXT = 1;
    
    /* Returns offset, where starts the definition of the manage bean
     **/
    public static int[] getManagedBeanDefinition(BaseDocument doc, String beanName){
        try{
            String text = doc.getText(0, doc.getLength());
            int offset = text.indexOf(beanName);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == XML_TEXT){
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    if (token != null && token.getImage().equals("<managed-bean-name")){    //NOI18N
                        while (token != null
                                && !(token.getTokenID().getNumericID() ==  XML_ELEMENT
                                && token.getImage().equals("<managed-bean")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<managed-bean")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            while (token != null
                                    && !(token.getTokenID().getNumericID() ==  XML_ELEMENT
                                    && token.getImage().equals("</managed-bean")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</managed-bean")){
                                while (token != null
                                        && !(token.getTokenID().getNumericID() ==  XML_ELEMENT
                                        && token.getImage().equals(">")))
                                    token = token.getNext();
                                if (token!=null && token.getImage().equals(">")){
                                    end = token.getOffset()+1;
                                    return new int[]{start, end};
                                }
                            }
                            return new int[]{start, text.length()};
                        }
                    }
                }
                offset = text.indexOf(beanName, offset+beanName.length());
            }
        }
        catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } 
        return new int []{-1,-1};
    }
    
    private static final Logger LOGGER = Logger.getLogger(FacesRefactoringUtils.class.getName());
    
    public static abstract class OccurrenceItem {
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
            stringBuffer.append("<font color=\"#0000FF\">");
            stringBuffer.append("&lt;").append(getXMLElementName()).append("&gt;</font><b>");
            stringBuffer.append(oldValue).append("</b><font color=\"#0000FF\">&lt;/").append(getXMLElementName());
            stringBuffer.append("&gt;</font>");
            return stringBuffer.toString();
        }
        
        protected abstract String getXMLElementName();
        
        public abstract void performRename();
        public abstract void undoRename();
        public abstract String getRenameMessage();
        
        protected PositionBounds createPosition(int startOffset, int endOffset) {
            try{
                DataObject dataObject = DataObject.find(config);
                    CloneableEditorSupport editor
                            = FacesRefactoringUtils.findCloneableEditorSupport(dataObject);
                    if (editor != null){
                        PositionRef start=editor.createPositionRef(startOffset, Bias.Forward);
                        PositionRef end=editor.createPositionRef(endOffset, Bias.Backward);
                        return new PositionBounds(start,end);
                    }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex); // TODO
            }
            return null;
        }
        
        public PositionBounds getClassDefinitionPosition() {
            return createPosition(0, 0);
        };
        
        public PositionBounds getElementDefinitionPosition() {
            return createPosition(0, 0);
        };
    }
    
    public static class ManagedBeanNameItem extends OccurrenceItem {
        private final ManagedBean bean;
        
        public ManagedBeanNameItem(FileObject config, ManagedBean bean, String newValue){
            super(config, newValue, bean.getManagedBeanName());
            this.bean = bean;
        }
        
        protected String getXMLElementName(){
            return "managed-bean-name"; //NOI18N
        }
        
        public void performRename(){
            changeBeanName(newValue);
        }
        
        public void undoRename(){
            changeBeanName(oldValue);
        }        
        
        public String getRenameMessage(){
            return NbBundle.getMessage(FacesRefactoringUtils.class, "MSG_ManagedBeanName_Rename",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        private void changeBeanName(String beanName){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <ManagedBean> beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (bean.getManagedBeanName().equals(managedBean.getManagedBeanName())){
                    facesConfig.getModel().startTransaction();
                    managedBean.setManagedBeanName(beanName);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
                
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            try{
                DataObject dataObject = DataObject.find(config);
                BaseDocument document = FacesRefactoringUtils.getBaseDocument(dataObject);
                int [] offsets = FacesRefactoringUtils.getManagedBeanDefinition(document, bean.getManagedBeanName());
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            try {
                DataObject dataObject = DataObject.find(config);
                BaseDocument document = FacesRefactoringUtils.getBaseDocument(dataObject);
                int [] offsets = FacesRefactoringUtils.getManagedBeanDefinition(document, bean.getManagedBeanName());
                position =  createPosition(offsets[0], offsets[1]);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
    }

    public static List <OccurrenceItem> getAllOccurrences(WebModule webModule, String oldName, String newName){
        List result = new ArrayList();
        assert webModule != null;
        assert oldName != null;
        assert newName != null;
        
        LOGGER.fine("getAllOccurences("+ webModule.getDocumentBase().getPath() + ", " + oldName + ", " + newName + ")");
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();                    
                    List<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                    for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                        ManagedBean managedBean = it.next();
                        if (oldName.equals(managedBean.getManagedBeanName())) {
                            result.add(new ManagedBeanNameItem(configs[i], managedBean, newName));
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
    public static String computePathFromTo(FileObject fromFile, FileObject toFile) {
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
    public static String computePathFromTo(ArrayList fromPathList, ArrayList toPathList) {
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
    public static ArrayList getPathList(FileObject file) {
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
    public static ArrayList getPathList(String fileName) {
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
}
