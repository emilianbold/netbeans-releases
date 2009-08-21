/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.jsf.editor.el;

import javax.swing.text.Document;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.editor.completion.JsfElCompletionItem;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Petr Pisl
 * @author Po-Ting Wu
 */
public class JsfElExpression extends ELExpression {
    
    public static final int EL_JSF_BEAN = 100;
    public static final int EL_JSF_RESOURCE_BUNDLE = 101;
    public static final int EL_JSF_BEAN_REFERENCE = 102;

    private static final String VARIABLE_NAME="var";  //NOI18N
    private static final String VALUE_NAME="value";  //NOI18N
    
    private static final Logger logger = Logger.getLogger(JsfElExpression.class.getName());
    
    private WebModule webModule;
    
    protected String bundleName;
    
    public JsfElExpression(WebModule wm, Document doc){
        super(doc);
        this.webModule = wm;
    }
    
    /* Managed properties of managed bean could be declared in 
     * faces-config.xml. But they still should have accessors in 
     * class file. So this code will duplicate class file accessor scaning. 
     * 
     * 
    public List<CompletionItem> getPropertyCompletionItems(String beanType, int anchor) {
        List<CompletionItem> result = new LinkedList<CompletionItem>();
        CompletionInfo completionInfo = getPropertyCompletionInfo(beanType, 
                anchor);
        runTask( completionInfo );
        result.addAll( completionInfo.getCompletionItems() );
        
        String qName = completionInfo.getTypeOnCaretQualifiedName();
        if ( qName == null ){
            return result;
        }
        List<FacesManagedBean> managedBeans = JSFBeanCache.getBeans(webModule);
        FacesManagedBean bean = null;
        for (FacesManagedBean managedBean : managedBeans) {
            String beanName = managedBean.getManagedBeanClass();
            if ( qName.equals( beanName )){
                bean = managedBean;
            }
        }
        if ( bean == null ){
            return result;
        }
        String prefix = getPropertyBeingTypedName();
        List<ManagedProperty> managedProperties = bean.getManagedProperties();
        for (ManagedProperty managedProperty : managedProperties) {
            String propName = managedProperty.getPropertyName();
            if ( propName != null && propName.startsWith(prefix)){
                CompletionItem item = ElCompletionItem.createELProperty(
                        propName, propName, 
                        anchor, managedProperty.getPropertyClass());
                result.add( item );
            }
        }
        
        return result;
    }*/
    
    @Override
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');
        int value = EL_UNKNOWN;
        
        if (dotIndex > -1 || bracketIndex > -1 ){
            String first = expr.substring(0, getPositiveMin(dotIndex, bracketIndex));
            
            // look through all registered managed beans
            List<FacesManagedBean> beans = JSFBeanCache.getBeans(webModule);
            for (FacesManagedBean bean : beans) {
                if (first.equals(bean.getManagedBeanName())) {
                    value = EL_JSF_BEAN;
                    break;
                }
            }
            
            // look trhough all registered resource bundles
            List <ResourceBundle> bundles = getJSFResourceBundles(webModule);
            for (ResourceBundle bundle : bundles) {
                if (first.equals(bundle.getVar())) {
                    value = EL_JSF_RESOURCE_BUNDLE;
                    bundleName = bundle.getBaseName();
                    break;
                }
            }
            //This part look for variables defined in JSP/JSF code
            if (!beans.isEmpty() && value == EL_UNKNOWN) {
                FileObject fileObject = getFileObject();
                JspContextInfo contextInfo = JspContextInfo.getContextInfo(fileObject);
                if (contextInfo !=null) {
                    JspParserAPI.ParseResult result = contextInfo.
                        getCachedParseResult(fileObject, false, true);
                    if (result !=null) {
                        Node.Nodes nodes = result.getNodes();
                        Node node = findValue(nodes, first);
                        if (node != null) {
                            String ref_val = node.getAttributeValue(VALUE_NAME);
                            bundleName = ref_val;
                            value = EL_JSF_BEAN_REFERENCE;
                        }
                    }
                }

            }
        } else if (dotIndex == -1 && bracketIndex == -1) {
            value = EL_START;
        }
        return value;
    }
    
    /**
     * Recursively search for given variable name
     * @param nodes result of the parsing of the Jsp page
     * @param variableName name of the variable to search in the nodes
     * @return found node or null if nothing found
     */
    private Node findValue(Node.Nodes nodes, String variableName) {
        if (nodes == null)
            return null;
        for (int i=0;i<nodes.size();i++) {
            Node node = nodes.getNode(i);
            if (variableName.equals(node.getAttributeValue(VARIABLE_NAME)))
                return node;
            else {
                node = findValue(nodes.getNode(i).getBody(), variableName);
                if (node != null)
                    return node;
            }
        }
        return null;
    }
    
    @Override 
    public String getObjectClass(){
        String beanName = extractBeanName();
        if (bundleName !=null && bundleName.startsWith("#{")) {//NOI18N
            beanName = bundleName.substring(2,bundleName.length()-1);
        }
  
        List<FacesManagedBean> beans = JSFBeanCache.getBeans(webModule);
        
        for (FacesManagedBean bean : beans){
            if (beanName.equals(bean.getManagedBeanName())){
                return bean.getManagedBeanClass();
            }
        }
        
        return null;
    }

    public String getBundleName() {
        return bundleName;
    }
    
    /**
     * Finds list of all ResourceBundles, which are registered in all
     * JSF configuration files in a web module.
     * @param webModule
     * @return
     */
    public List <ResourceBundle> getJSFResourceBundles(WebModule webModule){
        final ArrayList <ResourceBundle> bundles = new ArrayList<ResourceBundle>();
        
        /*
         *  Old usage of JSF XML model.
         *  Switched to merged JSF model. 
         * 
        FileObject[] files = ConfigurationUtils.getFacesConfigFiles(webModule);
        for (int i = 0; i < files.length; i++) {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(files[i], 
                    true).getRootComponent();
            for (int j = 0; j < facesConfig.getApplications().size(); j++) {
                Collection<ResourceBundle> resourceBundles = facesConfig.
                getApplications().get(j).getResourceBundles();
                for (Iterator<ResourceBundle> it = resourceBundles.iterator(); 
                    it.hasNext();) 
                {
                    bundles.add(it.next());   
                }
            }
        }*/
        /**
         * @author ads
         */
        MetadataModel<JsfModel> model = JsfModelFactory.getModel(webModule);
        if ( model != null ){
            try {
                model.runReadAction( new MetadataModelAction<JsfModel, Void>() {

                public Void run( JsfModel metaModel ) throws Exception {
                    List<Application> applications = metaModel.getElements(
                            Application.class);
                        for (Application application : applications) {
                            Collection<ResourceBundle> resourceBundles = 
                                application.getResourceBundles();
                            for (Iterator<ResourceBundle> it = resourceBundles
                                    .iterator(); it.hasNext();)
                            {
                                bundles.add(it.next());
                            }
                        }
                    return null;
                }
            });
            }
            catch(MetadataModelException e ){
                logger.log( Level.WARNING, e.getMessage(), e );
            }
            catch(IOException e ){
                logger.log( Level.WARNING, e.getMessage(), e );
            }
        }
        return bundles;
    }
    
    public  List<CompletionItem> getPropertyKeys(String propertyFile, 
            int anchorOffset, String prefix) 
    {
        ArrayList<CompletionItem> items = new ArrayList<CompletionItem>();
        java.util.ResourceBundle labels = null;
        ClassPath classPath;
        ClassLoader classLoader;
        
        try { // try to find on the source classpath
            classPath = ClassPath.getClassPath(getFileObject(), ClassPath.SOURCE);
            classLoader = classPath.getClassLoader(false);
            labels = java.util.ResourceBundle.getBundle(propertyFile, 
                    Locale.getDefault(), classLoader);
        }  
        catch (MissingResourceException exception) {
            // There is not the property on source classpath - try compile
            try {
                classLoader = ClassPath.getClassPath(getFileObject(), 
                        ClassPath.COMPILE).getClassLoader(false);
                labels = java.util.ResourceBundle.getBundle(propertyFile, 
                        Locale.getDefault(), classLoader);
            } catch (MissingResourceException exception2) {
                // the propertyr file wasn't find on the compile classpath as well
            }
        }
        
        if (labels != null) {  
            // the property file was found
            Enumeration<String> keys = labels.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                if (key.startsWith(prefix)) {
                    StringBuffer helpText = new StringBuffer();
                    helpText.append(key).append("=<font color='#ce7b00'>"); //NOI18N
                    helpText.append(labels.getString(key)).append("</font>"); //NOI18N
                    items.add(new JsfElCompletionItem.JsfResourceItem(key, 
                            anchorOffset, helpText.toString()));
                }
            }
        }
        
        return items;
    }
    
    public List<CompletionItem> /*getListenerMethodCompletionItems*/
                                getMethodCompletionItems(String beanType, 
            int anchor)
    {
        JSFCompletionItemsTask task = new JSFCompletionItemsTask(beanType, anchor);
        runTask(task);
        return task.getCompletionItems();
    }
    
    private boolean checkMethod( ExecutableElement method , 
            CompilationController controller )
    {
        System.out.println("&&&&&&&&&&&& " +method);
        TypeMirror returnType = method.getReturnType();
        if ( returnType.getKind() == TypeKind.VOID && 
                method.getSimpleName().toString().startsWith("set")
                && method.getParameters().size() == 1)    // NOI18N
        {
            System.out.println("@@@@ "+method.getSimpleName());
            VariableElement param = method.getParameters().get(0);
            // probably method is setter for some property...
            String propertyName = method.getSimpleName().toString().
                substring(3);
            String getterName = "get"+propertyName;
            for ( ExecutableElement exec : ElementFilter.methodsIn(
                    method.getEnclosingElement().getEnclosedElements()))
            {
                if ( exec.getSimpleName().contentEquals(getterName) &&
                        exec.getParameters().size() == 0 )
                {
                    TypeMirror execReturnType = exec.getReturnType();
                    System.out.println("%%% "+execReturnType);
                    System.out.println("^^^^ type of param :"+param.asType());
                    if ( controller.getTypes().
                            isSameType(param.asType(), execReturnType))
                    {
                        /*
                         *  Found getter which correspond 
                         *  <code>method</code> as setter. So this method 
                         *  should not be available in completion list .
                         *  Pair setter/getter is represented just property name. 
                         */
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public class JSFCompletionItemsTask extends ELExpression.BaseELTaskClass 
        implements CancellableTask<CompilationController> 
    {
        
        private List<CompletionItem> completionItems = new ArrayList<CompletionItem>();
        private int anchor;
        
        JSFCompletionItemsTask(String beanType, int anchor){
            super(beanType);
            this.anchor = anchor;
        }
        
        @Override
        public void cancel() {}
        
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            
            TypeElement bean = getTypePreceedingCaret(controller);
            
            if (bean != null){
                String prefix = getPropertyBeingTypedName();
                
                for (ExecutableElement method : ElementFilter.methodsIn(bean.
                        getEnclosedElements()))
                {
                    /* EL 2.1 for JSF allows to call any method , not just action listener 
                     * if (isActionListenerMethod(method)) {
                      */
                    // skip bean property accessors 
                    if ( method.getSimpleName().toString().equals( 
                            getExpressionSuffix(method, controller)) )
                    {
                        String methodName = method.getSimpleName().toString();
                            if (methodName != null && methodName.startsWith(prefix)){
                                CompletionItem item = new JsfElCompletionItem.JsfMethod(
                                    methodName, anchor, method.getReturnType().toString());

                            completionItems.add(item);
                        }
                    }
                }
            }
        }
        

        public List<CompletionItem> getCompletionItems(){
            return completionItems;
        }
        
        protected boolean isActionListenerMethod(ExecutableElement method){
            boolean isALMethod = false;
            
            if (method.getModifiers().contains(Modifier.PUBLIC)
                    && method.getParameters().size() == 1) {
                TypeMirror type = method.getParameters().get(0).asType();
                if ("javax.faces.event.ActionEvent".equals(type.toString()) //NOI18N
                        && TypeKind.VOID == method.getReturnType().getKind()) { 
                    isALMethod = true;
                }
            }
            
            return isALMethod;
        }
        
        @Override
        protected boolean checkMethodParameters( ExecutableElement method ,
                CompilationController controller)
        {
            return true;
        }
        
        @Override
        protected boolean checkMethod( ExecutableElement method , 
                CompilationController compilationController)
        {
            return JsfElExpression.this.checkMethod(method, compilationController);
        }
    }

    @Override
    public boolean gotoPropertyDeclaration(String beanType){
        GoToSourceTask task = new GoToSourceTask(beanType);
        runTask(task);
        return task.wasSuccessful();
    }

    /**
     * Go to the java source code of expression
     * - a getter in case of
     */
    private class GoToSourceTask extends BaseELTaskClass implements 
        CancellableTask<CompilationController>
    {
        private boolean success = false;

        GoToSourceTask(String beanType){
            super(beanType);
        }

        public void run(CompilationController controller ) throws Exception {
            controller .toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement bean = getTypePreceedingCaret(controller );

            if (bean != null){
                String suffix = removeQuotes(getPropertyBeingTypedName());

                for (ExecutableElement method : ElementFilter.methodsIn(bean.
                        getEnclosedElements()))
                {
                    String propertyName = getExpressionSuffix(method, controller);

                    if (propertyName != null && propertyName.equals(suffix)){
                        ElementHandle<ExecutableElement> el = 
                            ElementHandle.create(method);
                        FileObject fo = SourceUtils.getFile(el, 
                                controller .getClasspathInfo());

                        // Not a regular Java data object (may be a multi-view data object), open it first
                        DataObject od = DataObject.find(fo);
                        if (!"org.netbeans.modules.java.JavaDataObject".equals(
                                od.getClass().getName())) { // NOI18N
                            EditorCookie oc = od.getCookie(EditorCookie.class);
                            oc.open();
                        }
                      
                        success = ElementOpen.open(fo, el);
                        break;
                    }
                }
            }
        }

        public boolean wasSuccessful(){
            return success;
        }
        
        @Override
        protected boolean checkMethodParameters( ExecutableElement method ,
                CompilationController controller )
        {
            return true;
        }
        
        @Override
        protected boolean checkMethod( ExecutableElement method, 
                CompilationController controller)
        {
            return JsfElExpression.this.checkMethod(method, controller);
        }
    }
}
