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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.core.syntax.completion.api.ElCompletionItem;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelFactory;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.beans.api.model.support.WebBeansModelSupport;
import org.netbeans.modules.web.beans.api.model.support.WebBeansModelSupport.WebBean;
import org.netbeans.modules.web.jsf.editor.completion.JsfElCompletionItem;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfPageModelFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 * @author Po-Ting Wu
 * @author ads
 */
public class JsfElExpression extends ELExpression {
    
    public static final int EL_JSF_BEAN = 100;
    public static final int EL_JSF_RESOURCE_BUNDLE = 101;
    public static final int EL_JSF_BEAN_REFERENCE = 102;
    public static final int EL_COMPOSITE_COMPONENT = 103;

    private static final String VARIABLE_NAME="var";  //NOI18N
    private static final String VALUE_NAME="value";  //NOI18N
    
    private static final String COMPOSITE_COMPONENT_ATTR_NAME = "cc"; //NOI18N

    private static final Logger logger = Logger.getLogger(JsfElExpression.class.getName());
    
    /** defines if the properties are limited to their context (true),
     * or are available everywhere after the property context end offset (false)
     */
    private static final boolean NESTING_AWARE = false;

    private WebModule webModule;
    MetadataModel<WebBeansModel> webBeansModel;
    protected String bundleName;

    public JsfElExpression(WebModule wm, Document doc, int offset) throws BadLocationException{
        super(doc, offset);
        this.webModule = wm;
	this.webBeansModel = JsfSupport.findFor(doc).getWebBeansModel();
    }

    @Override
    protected int findContext(final String expr) {
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');
        
        if (dotIndex > -1 || bracketIndex > -1 ){
            String first = expr.substring(0, getPositiveMin(dotIndex, bracketIndex));

            //check composition component object
            if(first.equals(COMPOSITE_COMPONENT_ATTR_NAME)) { //NOI18N
                return EL_COMPOSITE_COMPONENT;
            }

            // look through all registered managed beans
            List<FacesManagedBean> beans = JSFBeanCache.getBeans(webModule);
            for (FacesManagedBean bean : beans) {
                if (first.equals(bean.getManagedBeanName())) {
                    return EL_JSF_BEAN;
                }
            }

	    //go through web beans
	    List<WebBean> namedElements = WebBeansModelSupport.getNamedBeans(webBeansModel);
	    for (WebBean bean : namedElements) {
		String beanName = bean.getName();
		if (first.equals(beanName)) {
                    return EL_JSF_BEAN;
                }
	    }

            // look trhough all registered resource bundles
            List <ResourceBundle> bundles = getJSFResourceBundles(webModule);
            for (ResourceBundle bundle : bundles) {
                if (first.equals(bundle.getVar())) {
                    bundleName = bundle.getBaseName();
                    return EL_JSF_RESOURCE_BUNDLE;
                }
            }

            // look through declared jsf variables
            Source source = Source.create(getDocument());
            final int offset = getContextOffset();
            final String[] _value = new String[1];
            try {
                //XXX the parsing should run on the ELExpression's snapshot, not the live document
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Result result = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                        if (result instanceof HtmlParserResult) {
                            JsfVariablesModel model = JsfVariablesModel.getModel((HtmlParserResult)result);
                            _value[0] = model.resolveExpression(expr, findNearestMapableOffsetForward(result.getSnapshot(), offset), NESTING_AWARE);
                        }
                    }
                });
            } catch (ParseException e) {
                Exceptions.printStackTrace(e);
            }

            if(_value[0] != null) {
                resolvedExpression = _value[0]; //store the resolved variable expression
                return EL_JSF_BEAN;
            }

            //try if in a JSP...
            FileObject fileObject = getFileObject();
            JspContextInfo contextInfo = JspContextInfo.getContextInfo(fileObject);
            if (contextInfo != null) {
                JspParserAPI.ParseResult result = contextInfo.getCachedParseResult(fileObject, false, true);
                if (result != null) {
                    Node.Nodes nodes = result.getNodes();
                    Node node = findValue(nodes, first);
                    if (node != null) {
                        String ref_val = node.getAttributeValue(VALUE_NAME);
                        resolvedExpression = ref_val;
                        return EL_JSF_BEAN_REFERENCE;
                    }
                }
            }
            
            
        } else if (dotIndex == -1 && bracketIndex == -1) {
            return  EL_START;
        }

        //unknown context
        return EL_UNKNOWN;
    }

    //workaround for parsing api - we need to find a document offset for the given astoffset
    private int findNearestMapableOffsetForward(Snapshot s, int documentOffset) {
        Document doc = s.getSource().getDocument(false);
        assert doc != null;

        for(int i = documentOffset; i < doc.getLength(); i ++ ) {
            int embeddedOffset = s.getEmbeddedOffset(i);
            if(embeddedOffset != -1) {
                return embeddedOffset;
            }
        }
        return -1;
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

    /** returns a type name of the resolved expression.
     * So for example Company.product.name will cause
     * name property class type being returned.
     */
    @Override 
    public String getObjectClass(){
        return getTypeName( getResolvedExpression() );
    }

    /** returns a type name of the expression base bean.
     * So for example Company.product.name will cause
     * Company class type being returned.
     */
    public String getBaseObjectClass() {
        return getTypeName(extractBeanName(getResolvedExpression()));
    }

    @Override
    public String getBeanName(){
        String beanName = extractBeanName();
        if (bundleName !=null && bundleName.startsWith("#{")) {//NOI18N
            beanName = bundleName.substring(2,bundleName.length()-1);
        }
        return beanName;
    }

    public String getBundleName() {
        return bundleName;
    }
    
    /*
     * Appears as fix for IZ#172824 - JSF code completion problems
     */
    private String getTypeName( final String varType ) {
        Part[] parts = getParts( varType );
        String name = null;
        if ( parts!= null && parts .length >0 ){
            name = parts[0].getPart();
        }
        else {
            return null;
        }
        
        if ( name == null ){
            return null;
        }
        
        List<FacesManagedBean> beans = JSFBeanCache.getBeans(webModule);

	//managed beans
        for (FacesManagedBean bean : beans){
            if (name.equals(bean.getManagedBeanName())){
                name = bean.getManagedBeanClass();
                break;
            }
        }
	//web beans
	for (WebBean bean : WebBeansModelSupport.getNamedBeans(webBeansModel)){
	    String beanName = bean.getName();
            if (name.equals(beanName)){
                name = bean.getBeanClassName(); //bean class
                break;
            }
        }


        final String[] result= new String[1];
        InspectPropertiesTask inspectPropertiesTask = new InspectPropertiesTask(name){

            public void run( CompilationController controller ) throws Exception {
                TypeMirror type = getTypePreceedingCaret(controller, varType);
                if(type == null) {
                    //unresolvable type
                    return ;
                }
                if(type.getKind() == TypeKind.DECLARED) {
                    Element el = ((DeclaredType)type).asElement();
                    if(el instanceof TypeElement) {
                        result[0] = ((TypeElement)el).getQualifiedName().toString();
                    }
                }

                if ( type.getKind() == TypeKind.ARRAY){
                    TypeMirror typeMirror = ((ArrayType)type).
                        getComponentType();
                    if ( typeMirror.getKind() == TypeKind.DECLARED){
                        result[0] = ((TypeElement)controller.getTypes().asElement(
                                typeMirror)).getQualifiedName().toString();
                    }
                }
            }
        };
        inspectPropertiesTask.execute();
        return result[0] != null ? result[0] : name;
    }

    private static final String ATTRS_ATTR_NAME = "attrs"; //NOI18N

    private static final String CC_INTERFACE_ATTRIBUTE_NAME = "name"; //NOI18N

    private static final Map CC_PROPERTIES_BASE = new HashMap();
    private static final Map CC_PROPERTIES = new HashMap();
    static {
        Map attrsMap = new HashMap();
        attrsMap.put(ATTRS_ATTR_NAME, CC_PROPERTIES); //NOI18N
        CC_PROPERTIES_BASE.put(COMPOSITE_COMPONENT_ATTR_NAME, attrsMap); //NOI18N
    }

    //TODO - not recursive attributes - bug #2
    //TODO - no need to initialize the composite component model when completing just cc or attrs items
    //XXX - consider using index only - much faster, but may be inaccurate when editing w/o saving the file
    public List<CompletionItem> getCompositeComponentItems(final int anchor) {
        final List<CompletionItem> items = new ArrayList<CompletionItem>();

        /*
         * Fix for IZ#171833 - Array syntax doesn't work for composite component completion
         */
        Part[] parts = getParts();
        final List<String> path = new LinkedList<String>();
        for( Part part : parts ){
            path.add(part.getPart());
        }

        //put interface attributes to the CC_PROPERTIES map
        //get composite component model
        CC_PROPERTIES.clear();
        Source source = Source.create(getDocument());
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    HtmlParserResult result = (HtmlParserResult)JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if(result == null) {
                        return ;
                    }
                    CompositeComponentModel.Factory ccmodelFactory = 
                        (CompositeComponentModel.Factory)JsfPageModelFactory.
                        getFactory(CompositeComponentModel.Factory.class);
                    CompositeComponentModel ccmodel = 
                        (CompositeComponentModel)ccmodelFactory.getModel(result);
                    if(ccmodel == null) {
                        //no library file
                        return ;
                    }

                    for(Map<String, String> attr : ccmodel.getExistingInterfaceAttributes()) {
                        String attributeName = attr.get(CC_INTERFACE_ATTRIBUTE_NAME); //NOI18N
                        if(attributeName != null) {
                            CC_PROPERTIES.put(attributeName, null);
                        }
                    }

                    resolvePropertiesCompletion(path, CC_PROPERTIES_BASE, items, 
                            anchor);
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return items;
    }

    public List<CompletionItem> getAvailableDeclaredVariables(final String prefix) {
        final List<CompletionItem> items = new ArrayList<CompletionItem>();
        // look through declared jsf variables
        Source source = Source.create(getDocument());
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    //one level - works only if xhtml is top level
                    Result result = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if (result instanceof HtmlParserResult) {
                        JsfVariablesModel model = JsfVariablesModel.getModel((HtmlParserResult) result);
                        List<JsfVariableContext> contexts = model.getAllAvailableVariables(getContextOffset(), false);
                        for (JsfVariableContext var : contexts) {
                            if (var.getVariableName().startsWith(prefix)) {
                                int anchor = getContextOffset() - getReplace().length();
                                String type = getTypeName(var.getResolvedType());
                                items.add(new ElCompletionItem.ELProperty(var.getVariableName(), anchor, type));
                            }
                        }
                    }
                }
            });
        } catch (ParseException e) {
            Exceptions.printStackTrace(e);
        }

        return items;
    }

    //generic properties completion
    private void resolvePropertiesCompletion(List<String> path, Map properties, 
            List<CompletionItem> items,  int anchor) 
    {
        String expr = getExpression();
        boolean endsWithDot = expr.length() >0 &&
            expr.charAt(expr.length() - 1) == '.' ;
        boolean startsWithBracket = expr.length() >0 &&
            expr.charAt(expr.length() - 1) == '[' ;
        
        Map m = properties;
        Iterator<String> pathItr = path.iterator();
        while (pathItr.hasNext()) {
            String pathItem = pathItr.next();
            Map inner = (Map) m.get(pathItem);
            if (inner != null) {
                //match, take next item
                m = inner;
            } else if (pathItr.hasNext() || endsWithDot ||startsWithBracket) {
                break; //the path doesn't match
            }

            if (!pathItr.hasNext()) {
                //last item => try to complete
                if (endsWithDot || startsWithBracket ) {
                    //all items
                    for (Object key : m.keySet()) {
                        String text = (String) key;
                        items.add(new ElCompletionItem.ELProperty(text, 
                                 getInsert( text, 
                                        text.length() >0 ? text.charAt(0): 0 ), anchor, ""));
                    }
                } else {
                    //filter items
                    for (Object key : m.keySet()) {
                        String text = (String) key;
                        String prefix = pathItem;
                        char firstChar =0;
                        if ( prefix.length()> 0){
                            firstChar = prefix.charAt(0);
                        }
                        if ( firstChar == '"' || firstChar == '\''){
                            prefix = prefix.substring( 1 );
                        }
                        if (text.startsWith(prefix)) {
                            items.add(new ElCompletionItem.ELProperty(text, 
                                    getInsert( text, firstChar), anchor, 
                                    ""));
                        }
                    }
                }

            }

        }
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
    
    public  List<CompletionItem> getPropertyKeysCompletionItems(String propertyFile, 
            int anchorOffset, String prefix) 
    {
        java.util.ResourceBundle[] bundle = new java.util.ResourceBundle[1];
        char firstChar =0;
        if ( prefix.length()> 0){
            firstChar = prefix.charAt(0);
        }
        if ( firstChar == '"' || firstChar == '\''){
            prefix = prefix.substring( 1 );
        }
        
        List<String> keys = getPropertyKeys(propertyFile, prefix ,bundle);
        List<CompletionItem> result = new ArrayList<CompletionItem>( keys.size() );
        
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                StringBuffer helpText = new StringBuffer();
                helpText.append(key).append("=<font color='#ce7b00'>"); // NOI18N
                helpText.append(bundle[0].getString(key)).append("</font>"); // NOI18N
                result.add(new JsfElCompletionItem.JsfResourceItem(key,
                        getInsert( key , firstChar), anchorOffset, 
                        helpText.toString()));
            }
        }
        
        return result;
    }
    
    public  List<String> getPropertyKeys(String propertyFile, String prefix,
            java.util.ResourceBundle[]  bundle) 
    {
        java.util.ResourceBundle labels = null;
        
        Project project = FileOwnerQuery.getOwner(getFileObject());
        labels = getResourceBundle(project, propertyFile, ClassPath.SOURCE);
        if ( labels == null ){
            labels = getResourceBundle(project, propertyFile, ClassPath.COMPILE);
        }
        
        List<String> result = new LinkedList<String>();
        if (labels != null) {  
            if ( bundle!= null && bundle.length >0 ){
                bundle[0] = labels;
            }
            // the property file was found
            Enumeration<String> keys = labels.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.startsWith(prefix)) {
                    result.add( key );
                }
            }
        }
        
        return result;
    }
    
    public List<CompletionItem> /*getListenerMethodCompletionItems*/
                                getMethodCompletionItems(String beanType, 
            int anchor)
    {
        JSFCompletionItemsTask task = new JSFCompletionItemsTask(beanType, anchor);
        runTask(task);
        return task.getCompletionItems();
    }
    
    public boolean checkMethod( ExecutableElement method , 
            CompilationController controller )
    {
        TypeMirror returnType = method.getReturnType();
        if ( returnType.getKind() == TypeKind.VOID && 
                method.getSimpleName().toString().startsWith("set")
                && method.getParameters().size() == 1)    // NOI18N
        {
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
    
    private java.util.ResourceBundle getResourceBundle( Project project, 
            String file, String type ) 
    {
        ClassPathProvider provider = project.getLookup().lookup( 
                ClassPathProvider.class);
        if ( provider == null ){
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if ( sources == null ){
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups( 
                JavaProjectConstants.SOURCES_TYPE_JAVA );
        int i=0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            ClassPath classPath = ClassPath.getClassPath(rootFolder, type);
            if ( classPath == null){
                continue;
            }
            ClassLoader classLoader = classPath.getClassLoader(false);
            try {
                return java.util.ResourceBundle.getBundle(file, 
                    Locale.getDefault(), classLoader);
            }
            catch (MissingResourceException exception) {
                continue;
            }
        }
        return null;
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
                // Fix for IZ#173117 - multiplied EL completion items
                Set<String> addedItems = new HashSet<String>(); 
                
                for (ExecutableElement method : ElementFilter.methodsIn(
                        controller.getElements().getAllMembers(bean)))
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
                                // Fix for IZ#173117 - multiplied EL completion items
                                if ( addedItems.contains( methodName)){
                                    continue;
                                }
                                addedItems.add(methodName);
                                TypeMirror methodType = controller.getTypes().asMemberOf(
                                        (DeclaredType)bean.asType(), method);
                                String retType = ((ExecutableType)methodType).
                                    getReturnType().toString();
                                CompletionItem item = new JsfElCompletionItem.JsfMethod(
                                    methodName, anchor, retType);

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
            // Fix for IZ#173117 -  multiplied EL completion items
            if ( super.checkMethodParameters(method, compilationController)&& 
                    super.checkMethod(method, compilationController))
            {
                return false;
            }
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
            TypeElement bean = getTypePreceedingCaret(controller, getResolvedExpression() );

            if (bean != null){
                String suffix = removeQuotes(getPropertyBeingTypedName());

                for (ExecutableElement method : ElementFilter.methodsIn(
                        controller.getElements().getAllMembers(bean)))
                {
                    String propertyName = getExpressionSuffix(method, controller);

                    if (propertyName != null && propertyName.equals(suffix)){
                        ElementHandle<ExecutableElement> el = 
                            ElementHandle.create(method);
                        FileObject fo = SourceUtils.getFile(el, 
                                controller .getClasspathInfo());

                        // Not a regular Java data object (may be a multi-view data object), open it first
                        // Fix for IZ#172418 - Invoking Go To Source for property inside EL could throw IllegalArgumentException
                        if (fo != null) {
                            DataObject od = DataObject.find(fo);
                            if (!"org.netbeans.modules.java.JavaDataObject"
                                    .equals(od.getClass().getName()))
                            { // NOI18N
                                EditorCookie oc = od
                                        .getCookie(EditorCookie.class);
                                oc.open();
                            }
                            success = ElementOpen.open(fo, el);
                        }
                        else {
                            success = ElementOpen.open(controller.getClasspathInfo(), 
                                    el);
                        }
                      
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
