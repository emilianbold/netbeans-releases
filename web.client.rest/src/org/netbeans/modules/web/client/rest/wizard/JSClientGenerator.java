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
package org.netbeans.modules.web.client.rest.wizard;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * @author ads
 *
 */
class JSClientGenerator {
    
    enum MethodType {
        GET,
        SET
    }
    
    enum HttpRequests {
        POST,
        PUT,
        DELETE
    }
    
    private static final String SLASH = "/";                        // NOI18N

    private static final Logger LOG = Logger.getLogger( JSClientGenerator.class.getName()); 
    
    private static final String PATH = "javax.ws.rs.Path";           // NOI18N
    private static final String PATH_PARAM = "javax.ws.rs.PathParam";// NOI18N
    private static final String GET = "javax.ws.rs.GET";             // NOI18N
    private static final String PUT = "javax.ws.rs.PUT";             // NOI18N
    private static final String POST = "javax.ws.rs.POST";           // NOI18N
    private static final String DELETE = "javax.ws.rs.DELETE";       // NOI18N
    private static final String PRODUCES = "javax.ws.rs.Produces";   // NOI18N
    private static final String CONSUMES = "javax.ws.rs.Consumes";   // NOI18N
    private static final String JSON = "application/json";           // NOI18N
    
    private static final String XML_ROOT_ELEMENT = 
        "javax.xml.bind.annotation.XmlRootElement";                  // NOI18N
    private static final String ID = "javax.persistence.Id";         // NOI18N
    
    private JSClientGenerator(RestServiceDescription description){
        myDescription = description;
    }

    static JSClientGenerator create( RestServiceDescription description )
    {
        return new JSClientGenerator(description);
    }

    public void generate( final FileObject jsFile) {
        FileObject restSource = myDescription.getFile();
        if ( restSource == null ){
            return;
        }
        myContent = new StringBuilder("$(function(){\n");               // NOI18N
        JavaSource javaSource = JavaSource.forFileObject( restSource);
        final String restClass = myDescription.getClassName();
        Task<CompilationController> task = new Task<CompilationController>(){

            @Override
            public void run( CompilationController controller ) throws Exception {
                controller.toPhase( Phase.ELEMENTS_RESOLVED );
                
                List<ExecutableElement> getMethods = new LinkedList<ExecutableElement>();
                List<ExecutableElement> postMethods = new LinkedList<ExecutableElement>();
                List<ExecutableElement> putMethods = new LinkedList<ExecutableElement>();
                List<ExecutableElement> deleteMethods = new LinkedList<ExecutableElement>();
                
                TypeElement restResource = controller.getElements().getTypeElement( 
                       restClass );
                List<ExecutableElement> methods = ElementFilter.methodsIn(
                        restResource.getEnclosedElements());
                for (ExecutableElement method : methods) {
                    List<? extends AnnotationMirror> annotations = 
                        method.getAnnotationMirrors();
                    if ( getAnnotation( annotations , DELETE)!= null ){
                        deleteMethods.add( method );
                        continue;
                    }
                    if ( !hasJsonMedia( annotations) ){
                        continue;
                    }
                    if ( getAnnotation( annotations, GET) != null ){
                        getMethods.add( method);
                    }
                    else if ( getAnnotation( annotations , POST)!= null ){
                        postMethods.add( method );
                    }
                    else if ( getAnnotation( annotations , PUT)!= null ){
                        putMethods.add( method );
                    }
                }
                
                try {
                    handleRestMethods(controller , getMethods, postMethods, 
                        putMethods, deleteMethods);
                }
                catch( IOException e ){
                    LOG.log(Level.WARNING , null ,e );
                }
            }

        };
        try {
            Future<Void> future = javaSource.runWhenScanFinished( task, true);
            future.get();
        }
        catch (IOException e) {
            LOG.log(Level.INFO , null ,e );
        }
        catch (InterruptedException e) {
            LOG.log(Level.INFO , null ,e );
        }
        catch (ExecutionException e) {
            LOG.log(Level.INFO , null ,e );
        }
        
        if ( !isModelGenerated ){
            myContent.append("// No JSON media type is detected in GET RESTful methods\n");
        }
        myContent.append("});");
        

        try {
            DataObject jsDo = DataObject.find(jsFile);
            EditorCookie cookie = jsDo.getCookie(EditorCookie.class);
            cookie.open();
            final BaseDocument document = (BaseDocument) cookie.openDocument();

            final Indent indent = Indent.get(document);
            indent.lock();
            try {
                document.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        int position = document.getLength();
                        try {
                            document.insertString(position,
                                    myContent.toString(), null);

                            indent.reindent(0, document.getLength());
                        }
                        catch (BadLocationException e) {
                            LOG.log(Level.WARNING, null, e);
                        }
                    }
                });
            }
            finally {
                indent.unlock();
            }
            cookie.saveDocument();

        }
        catch (DataObjectNotFoundException e) {
            LOG.log(Level.WARNING, null, e);
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, null, e);
        }
        
    }
    
    private void handleRestMethods( CompilationController controller,  
            List<ExecutableElement> getMethods,
            List<ExecutableElement> postMethods,
            List<ExecutableElement> putMethods,
            List<ExecutableElement> deleteMethods ) throws IOException
    {
        Map<String,ExecutableElement> noParamGetMethods = 
            new HashMap<String, ExecutableElement>();
        Map<String,ExecutableElement> oneParamGetMethods = 
            new HashMap<String, ExecutableElement>();
        for(ExecutableElement method : getMethods){
            List<? extends VariableElement> parameters = method.getParameters();
            if ( parameters.size() > 1){
                // TODO : handle methods with more than one  param
                continue;
            }
            
            AnnotationMirror annotation = getAnnotation(method, PATH);
            String path = getValue( annotation );
            if ( parameters.isEmpty() ){
                if ( path == null ){
                    path = "";
                }
                noParamGetMethods.put( path, method );
                continue;
            }
            
            VariableElement param = parameters.get(0);
            annotation = getAnnotation(param, PATH_PARAM);
            if ( annotation == null ){
                continue;
            }
            String pathNoParam = removeParamTemplate( path , getValue(annotation));
            oneParamGetMethods.put(pathNoParam, method );
        }
        Map<String,String> fqn2Path = new HashMap<String, String>();
        for(Entry<String,ExecutableElement> entry : noParamGetMethods.entrySet()){
            String path = entry.getKey();
            ExecutableElement method = entry.getValue();
            
            TypeMirror returnType = method.getReturnType();
            Element returnElement = controller.getTypes().asElement( returnType );
            TypeMirror entityCollectionType = getCollectionType(returnType, controller);
            if ( entityCollectionType == null && 
                    returnElement instanceof TypeElement)           // skip primitives ( consider just type element )
            {
                if ( getAnnotation( returnElement, XML_ROOT_ELEMENT) == null ){
                    /* TODO : here is only @XmlRootElement annotated elements
                     * are considered as JSON serializable ( that's true for
                     * NB generated entities ) but there could be probably
                     * other ways to serialize ( read/write REST providers )
                     * POJO classes     
                     */
                    continue;
                }
                EnumMap<HttpRequests, String> paths = 
                    new EnumMap<HttpRequests, String>(HttpRequests.class);
                paths.put(HttpRequests.POST, parseNoIdPath(postMethods, 
                        returnType, controller ));
                paths.put(HttpRequests.PUT, parseNoIdPath(putMethods , 
                        returnType , controller));
                paths.put(HttpRequests.DELETE, parseNoIdPath(deleteMethods,
                        returnType, controller ));
                generateBackendModel( (TypeElement)returnElement , path , 
                        null, paths , Collections.<HttpRequests, Boolean>emptyMap(), 
                        controller );
            }
            else {
                // collection of entities
                Element entityType = controller.getTypes().asElement(entityCollectionType);
                if ( entityType instanceof TypeElement ){
                    String fqn = ((TypeElement)entityType).getQualifiedName().toString();
                    fqn2Path.put(fqn, path);
                }
            }
        }
        for(Entry<String,ExecutableElement> entry : oneParamGetMethods.entrySet()){
            String path = entry.getKey();
            ExecutableElement method = entry.getValue();
            
            TypeMirror returnType = method.getReturnType();
            Element returnElement = controller.getTypes().asElement( returnType );
            if ( returnElement instanceof TypeElement ){
                // TODO: return type could be a primitive type. How it should be handled ?
                if ( getAnnotation( returnElement, XML_ROOT_ELEMENT) == null ){
                    /* TODO : here is only @XmlRootElement annotated elements
                     * are considered as JSON serializable ( that's true for
                     * NB generated entities ) but there could be probably
                     * other ways to serialize ( read/write REST providers )
                     * POJO classes     
                     */
                    continue;
                }
                String fqn = ((TypeElement)returnElement).getQualifiedName().toString();
                String collectionPath = fqn2Path.get(fqn);
                EnumMap<HttpRequests, String> paths = 
                    new EnumMap<HttpRequests, String>(HttpRequests.class);
                EnumMap<HttpRequests, Boolean> ids = 
                    new EnumMap<HttpRequests, Boolean>(HttpRequests.class);
                parsePath(postMethods, returnType, paths, ids, 
                        HttpRequests.POST, controller );
                parsePath(putMethods, returnType, paths, ids, 
                        HttpRequests.PUT, controller );
                parsePath(deleteMethods, returnType, paths, ids, 
                        HttpRequests.DELETE, controller );
                generateBackendModel( (TypeElement)returnElement , path , 
                        collectionPath, paths, ids, controller );
            }
        }
    }
    
    private String parseNoIdPath( List<ExecutableElement> methods , 
            TypeMirror type , CompilationController controller) 
    {
        for (ExecutableElement method : methods) {
            List<? extends VariableElement> parameters = method.getParameters();
            boolean matches = false;
            if ( parameters.size() == 0 ){
                matches = true;
            }
            else if ( parameters.size() == 1){
                VariableElement param = parameters.get(0);
                if ( controller.getTypes().isSameType(param.asType(),type)){
                    matches = true;
                }
            }
            else {
                continue;
            }
            if ( matches ){
                AnnotationMirror annotation = getAnnotation(method, PATH);
                if ( annotation == null ){
                    return "";
                }
                else {
                    return getValue(annotation);
                }
            }
        }
        return null;
    }
    
    private void parsePath( List<ExecutableElement> methods , TypeMirror type ,
            EnumMap<HttpRequests, String> paths, 
            EnumMap<HttpRequests, Boolean> ids, HttpRequests request,
            CompilationController controller) 
    {
        for (ExecutableElement method : methods) {
            List<? extends VariableElement> parameters = method.getParameters();
            boolean matches = false;
            String pathParam = null;
            if ( parameters.size() == 1){
                VariableElement param = parameters.get(0);
                if ( controller.getTypes().isSameType(param.asType(),type)){
                    matches = true;
                }
                else if ( getAnnotation(param, PATH_PARAM) != null ){
                    pathParam = getValue(getAnnotation(param, PATH_PARAM));
                    matches = true;
                    ids.put(request, Boolean.TRUE);
                }
            }
            else if (parameters.size() == 2) {
                VariableElement param1 = parameters.get(0);
                VariableElement param2 = parameters.get(1);
                if ( getAnnotation(param1, PATH_PARAM) != null ){
                    pathParam = getValue(getAnnotation(param1, PATH_PARAM));
                    if ( controller.getTypes().isSameType(param2.asType(),type)){
                        matches = true;
                    }
                }
                else if ( controller.getTypes().isSameType(param1.asType(),type)){
                    if ( getAnnotation(param2, PATH_PARAM) != null ){
                        pathParam = getValue(getAnnotation(param2, PATH_PARAM));
                        matches = true;
                    }
                }
                if ( matches ){
                    ids.put(request, Boolean.TRUE);
                }
            }
            else {
                continue;
            }
            if ( matches ){
                AnnotationMirror annotation = getAnnotation(method, PATH);
                if ( annotation == null ){
                    paths.put(request, "") ;
                }
                else {
                    String path = getValue(annotation);
                    if ( pathParam != null ){
                        path = removeParamTemplate(path, pathParam);
                    }
                    paths.put(request,path);
                }
                break;
            }
        }
    }

    private void generateBackendModel( TypeElement entity, String path,
            String collectionPath, Map<HttpRequests, String> httpPaths ,
            Map<HttpRequests, Boolean> useIds,
            CompilationController controller ) throws IOException
    {
        isModelGenerated = true;
        String fqn = entity.getQualifiedName().toString();
        String name = entity.getSimpleName().toString();
        String modelName = suggestModelName(name );
        
        myContent.append("\n// Model for ");                    // NOI18N
        if ( name.equals(modelName)){
            myContent.append( name );
        }
        else {
            myContent.append( fqn );
        }
        myContent.append(" entity\n");                          // NOI18N
        
        String url = getUrl( path );
        
        myContent.append("window.");                            // NOI18N
        myContent.append(modelName);
        myContent.append(" = Backbone.Model.extend({\n");       // NOI18N
        myContent.append("urlRoot : \"");                       // NOI18N
        myContent.append( url );
        myContent.append("\"");                                 // NOI18N
        String parsedData = parse(entity, controller);
        if ( parsedData != null ){
            myContent.append(',');                              
            myContent.append(parsedData);
        }
        String sync = overrideSync( url, httpPaths , useIds ); 
        if ( sync != null && sync.length()>0 ){
            myContent.append(",\n");                             // NOI18N
            myContent.append(sync);
            myContent.append("\n");                             // NOI18N
        }
        myContent.append("\n});\n\n");                          // NOI18N
        
        if ( collectionPath == null){
            return;
        }
        myContent.append("\n// Collection class for ");          // NOI18N
        if ( name.equals(modelName)){
            myContent.append( name );
        }
        else {
            myContent.append( entity.getQualifiedName().toString() );
        }
        myContent.append(" entities\n");                        // NOI18N
        myContent.append("window.");
        myContent.append(modelName);
        myContent.append("Collection");                         // NOI18N
        myContent.append(" = Backbone.Collection.extend({\n");  // NOI18N
        myContent.append("model: ");                            // NOI18N
        myContent.append(modelName);
        myContent.append(",\nurl : \"");                        // NOI18N
        myContent.append( getUrl( collectionPath ));
        myContent.append("\"\n");                               // NOI18N
        myContent.append("});\n\n");                            // NOI18N
    }

    private String overrideSync( String url,
            Map<HttpRequests, String> httpPaths,
            Map<HttpRequests, Boolean> useIds ) throws IOException 
    {
        StringBuilder builder = new StringBuilder();
        for( Entry<HttpRequests,String> entry : httpPaths.entrySet() ){
            overrideMethod(url, entry.getValue(), 
                    useIds.get(entry.getKey()), entry.getKey(), builder);
        }
        EnumSet<HttpRequests> set = EnumSet.allOf(HttpRequests.class);
        set.removeAll( httpPaths.keySet());
        for( HttpRequests request : set  ){
            overrideMethod(url, null, null, request, builder);
        }
        if ( builder.length()>0 ){
            builder.insert(0, "sync: function(method, model, options){\n");         // NOI18N
            builder.append("return Backbone.sync(method, model, options);\n}\n");   // NOI18N
        }
        return builder.toString();
    }
    
    private void overrideMethod(String url, String path, Boolean useId, 
            HttpRequests request, StringBuilder builder ) throws IOException
    {
        if ( path == null ){
            builder.append("if(method=='");                              // NOI18N
            builder.append(request.toString());
            builder.append("'){\n");                                     // NOI18N
            builder.append("return false;\n}\n");                        // NOI18N
        }
        else {
            path = getUrl(path);
            if ( !url.equals(path) || ( useId!= null && useId )){
                if ( !path.endsWith("/")){                              // NOI18N
                    path = path +'/';
                }
                builder.append("if(method=='");                         // NOI18N
                builder.append(request.toString());
                builder.append("'){\n");                                // NOI18N
                builder.append("options.url = '");                      // NOI18N
                builder.append(path);
                builder.append("'+id;\n");                              // NOI18N
                builder.append("}\n");                                  // NOI18N
            }
        }
    }

    private String parse( TypeElement entity, CompilationController controller ) {
        /*
         *  parse entity and generate attributes:
         *  1) idAttribute
         *  2) primitive attributes if any
         *  3) do not include attributes with complex type  
         */
        Set<String> attributes = parseBeanMethods( entity , controller );
        
        List<VariableElement> fields = ElementFilter.fieldsIn(
                controller.getElements().getAllMembers(entity));
        VariableElement id = null;
        for (VariableElement field : fields) {
            if ( getAnnotation(field, ID) != null ){
                boolean has = attributes.remove(field.getSimpleName().toString());
                if ( has ){
                    id = field;
                    break;
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        if ( id != null ){
            String idAttr = id.getSimpleName().toString();
            builder.append("\nidAttribute : '");                        // NOI18N
            builder.append(idAttr);
            builder.append("'");                                        // NOI18N
            if ( attributes.size() >0 ){
                builder.append(',');                                  
            }
        }
        
        if (attributes.size() > 0) {
            builder.append("\ndefaults: {");                            // NOI18N
            for (String attribute : attributes) {
                builder.append("\n");                                   // NOI18N
                builder.append(attribute);
                builder.append(": \"\",");                              // NOI18N
            }
            builder.deleteCharAt(builder.length()-1);
            builder.append("\n}");                                      // NOI18N
        }
        
        if ( builder.length() >0 ){
            return builder.toString();
        }
        else {
            return null;
        }
    }

    private Set<String> parseBeanMethods( TypeElement entity,
            CompilationController controller )
    {
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                controller.getElements().getAllMembers(entity));
        Set<String> result = new HashSet<String>();
        Map<String,TypeMirror> getAttrs = new HashMap<String, TypeMirror>();
        Map<String,TypeMirror> setAttrs = new HashMap<String, TypeMirror>();
        for (ExecutableElement method : methods) {
            if ( !method.getModifiers().contains( Modifier.PUBLIC)){
                continue;
            }
            
            Object[] attribute = getAttrName( method , controller);
            if ( attribute == null ){
                continue;
            }
            String name = (String)attribute[1];
            TypeMirror type = (TypeMirror)attribute[2];
            if ( attribute[0] == MethodType.GET ){
                if ( findAccessor(name, type, getAttrs, setAttrs, controller)){
                    result.add(name);
                }
            }
            else {
                if ( findAccessor(name, type, setAttrs, getAttrs, controller)){
                    result.add(name);
                }
            }
        }
        return result;
    }
    
    private boolean findAccessor(String name, TypeMirror type, 
            Map<String,TypeMirror> map1, Map<String,TypeMirror> map2, 
            CompilationController controller)
    {
        TypeMirror typeMirror = map2.remove(name);
        if ( typeMirror!= null && 
                controller.getTypes().isSameType(typeMirror, type))
        {
            return true;
        }
        else {
            map1.put(name, type);
        }
        return false;
    }

    private Object[] getAttrName( ExecutableElement method,
            CompilationController controller )
    {
        String name = method.getSimpleName().toString();
        if ( name.startsWith("set") ){                               // NOI18N
            TypeMirror returnType = method.getReturnType();
            if ( returnType.getKind()!= TypeKind.VOID){
                return null;
            }
            List<? extends VariableElement> parameters = method.getParameters();
            if ( parameters.size() !=1 ){
                return null;
            }
            VariableElement param = parameters.get(0);
            TypeMirror type = param.asType();
            if ( isSimple(type, controller)){
                return new Object[]{MethodType.SET, lowerFirstLetter(
                        name.substring(3)), type};
            }
            else {
                return null;
            }
        }
        int start =0;
        if ( name.startsWith("get")){                                   // NOI18N
            start =3;
        }
        else if ( name.startsWith( "is")){                              // NOI18N
            start =2;
        }
        if ( start > 0){
            List<? extends VariableElement> parameters = method.getParameters();
            if ( parameters.size() !=0 ){
                return null;
            }
            TypeMirror returnType = method.getReturnType();
            if ( isSimple(returnType, controller)){
                return new Object[]{ MethodType.GET, lowerFirstLetter(
                        name.substring(start)), returnType};
            }
            else {
                return null;
            }
        }
        return null;
    }
    
    private String lowerFirstLetter( String name ){
        if ( name.length() <=1){
            return name;
        }
        char firstLetter = name.charAt(0);
        if ( Character.isUpperCase(firstLetter)){
            return Character.toLowerCase(firstLetter) +name.substring(1);
        }
        return name;
    }

    /*
     * returns true if type is primitive or String
     */
    private boolean isSimple(TypeMirror typeMirror, CompilationController controller){
        if ( typeMirror.getKind().isPrimitive() ){
            return true;
        }
        Element fieldTypeElement = controller.getTypes().asElement(typeMirror);
        TypeElement stringElement = controller.getElements().
            getTypeElement(String.class.getName());
        if ( stringElement != null && stringElement.equals( fieldTypeElement)){
            return true;
        }
        
        PackageElement pack = controller.getElements().getPackageOf(
                fieldTypeElement);
        if ( pack.getQualifiedName().contentEquals("java.lang")){      // NOI18N
            try {
                if ( controller.getTypes().unboxedType(typeMirror) != null ){
                    return true;
                }
            }
            catch(IllegalArgumentException e){
                // just skip field
            }
        }
        return false;
    }
    
    private String getUrl( String relativePath ) throws IOException {
        Project project = FileOwnerQuery.getOwner(myDescription.getFile());
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        String applicationPath = restSupport.getApplicationPath();
        String uri = myDescription.getUriTemplate();
        
        if (applicationPath == null) {
            applicationPath = uri;
        }
        else {
            applicationPath = addUrlPath(applicationPath, uri);
        }
        applicationPath = addUrlPath(applicationPath, relativePath);
        
        return addUrlPath(restSupport.getContextRootURL(),applicationPath);            
    }

    private String addUrlPath( String path, String uri ) {
        if (uri.startsWith(SLASH)) {
            if (path.endsWith(SLASH)) {
                path = path + uri.substring(1);
            }
            else {
                path = path + uri;
            }
        }
        else {
            if (path.endsWith(SLASH)) {
                path = path + uri;
            }
            else {
                path = path + SLASH + uri;
            }
        }
        return path;
    }

    private String suggestModelName( String name ) {
        if ( myEntities.contains(name)){
            String newName ;
            int index =1;
            while( true ){
                newName = name+index;
                if ( !myEntities.contains(newName)){
                    myEntities.add(newName);
                    return newName;
                }
                index++;
            }
        }
        else {
            myEntities.add(name);
        }
        return name;
    }

    private String removeParamTemplate( String path, String param ) {
        int index = path.indexOf('{');
        String template = path;
        if ( index == -1 ){
            return path;
        }
        else {
            template = path.substring(index+1).trim();
            int lastIndex = template.lastIndexOf('}');
            if ( lastIndex == -1 ){
                return path;
            }
            template = template.substring( 0, lastIndex ).trim();
        }
        if ( !template.startsWith(param) ){
            return path;
        }
        template = template.substring(param.length()).trim();
        if ( template.length() == 0 || template.charAt(0)==':'){
            return path.substring( 0, index );
        }
        return path;
    }

    private TypeMirror getCollectionType (TypeMirror type , 
            CompilationController controller)
    {
        TypeElement collectionElement = controller.getElements().getTypeElement(
                Collection.class.getName());
        TypeMirror collectionType = controller.getTypes().erasure(
                collectionElement.asType());
        TypeMirror erasure = controller.getTypes().erasure(type);
        if (!controller.getTypes().isSubtype(erasure, collectionType)) {
            return null;
        }
        List<? extends TypeMirror> supers = controller.getTypes().directSupertypes( type );
        for (TypeMirror superType : supers) {
            erasure = controller.getTypes().erasure(superType);
            if ( controller.getTypes().isSameType(erasure, collectionType)){
                return getParameterType( superType);
            }
            TypeMirror found = getCollectionType(superType, controller);
            if ( found != null ){
                return found;
            }
        }
        return null;
    }
    
    private TypeMirror getParameterType( TypeMirror type ){
        if ( type instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = ((DeclaredType)type).getTypeArguments();
            if ( typeArguments.size() == 0){
                return null;
            }
            return typeArguments.get(0);
        }
        return null;
    }
    
    private String getValue( AnnotationMirror annotation ){
        if ( annotation == null ){
            return null;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = 
            annotation.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            elementValues.entrySet()) 
        {
            ExecutableElement annotationMethod = entry.getKey();
            AnnotationValue value = entry.getValue();
            if (annotationMethod.getSimpleName().contentEquals("value")) { // NOI18N
                Object val = value.getValue();
                if ( val != null ){
                    return val.toString();
                }
            }
        }
        return null;
    }
    
    private boolean hasJsonMedia( List<? extends AnnotationMirror> annotations) {
        AnnotationMirror consumes = getAnnotation(annotations, CONSUMES);
        AnnotationMirror produces = getAnnotation(annotations, PRODUCES);
        AnnotationMirror mimeTypeDecl = consumes==null ? produces: consumes;
        if ( mimeTypeDecl == null ){
            return false;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = 
            mimeTypeDecl.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            elementValues.entrySet()) 
        {
            ExecutableElement annotationMethod = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( annotationMethod.getSimpleName().contentEquals("value")){      // NOI18N
                Object mediaType = value.getValue();
                if ( mediaType instanceof List<?>){
                    List<?> types = (List<?>)mediaType;
                    for (Object type : types) {
                        if ( type instanceof AnnotationValue ){
                            mediaType = ((AnnotationValue)type).getValue();
                            if ( JSON.equals( mediaType )){
                                return true;
                            }
                        }
                    }
                }
                else if ( JSON.equals( mediaType )){
                    return true;
                }
            }
        }
        return false;
    }
    
    private AnnotationMirror getAnnotation( List<? extends AnnotationMirror> annotations, 
            String annotation )
    {
        for (AnnotationMirror annotationMirror : annotations) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement){
                TypeElement annotationDecl = (TypeElement) annotationElement;
                if ( annotationDecl.getQualifiedName().contentEquals( annotation)){
                    return annotationMirror;
                }
            }
        }
        return null;
    }
    
    private  AnnotationMirror getAnnotation( Element element, String annotation )
    {
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        return getAnnotation(annotations, annotation);
    }
    
    private  Map<String,AnnotationMirror> getAnnotions( Element element )
    {
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        Map<String,AnnotationMirror> map = new HashMap<String, AnnotationMirror>();
        for (AnnotationMirror annotationMirror : annotations) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement){
                TypeElement annotationDecl = (TypeElement) annotationElement;
                map.put(annotationDecl.getQualifiedName().toString(), annotationMirror);
            }
        }
        return map;
    }
    
    private RestServiceDescription myDescription;
    private StringBuilder myContent;
    private Set<String> myEntities  = new HashSet<String>();
    private boolean isModelGenerated;

}
