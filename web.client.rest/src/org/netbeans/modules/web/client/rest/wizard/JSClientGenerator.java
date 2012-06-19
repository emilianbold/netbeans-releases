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
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
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
        myContent = new StringBuilder("$(function(){\n");
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
                    else if ( getAnnotation( annotations , DELETE)!= null ){
                        deleteMethods.add( method );
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
            if ( parameters.size() == 0 ){
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
            TypeMirror entityCollectionType = getCollectionType(returnType, controller);
            if ( entityCollectionType == null ){
                // just plain entity
                
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
                // TODO: return type could be a promitive type. How it should be handled ?
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
                generateBackendModel( (TypeElement)returnElement , path , 
                        collectionPath, controller );
            }
        }
    }
    
    private void generateBackendModel( TypeElement entity, String path,
            String collectionPath, CompilationController controller ) throws IOException
    {
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
        
        myContent.append("window.");
        myContent.append(modelName);
        myContent.append(" = Backbone.Model.extend({\n");       // NOI18N
        myContent.append("urlRoot : \"");                       // NOI18N
        myContent.append( getUrl( path ));
        myContent.append("\"");                                 // NOI18N
        String parsedData = parse(entity, controller);
        if ( parsedData != null ){
            myContent.append(",\n");                              // NOI18N
            myContent.append(parsedData);
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
        myContent.append(" entities\n");                         // NOI18N
        myContent.append("window.");
        myContent.append(modelName);
        myContent.append("Collection");                         // NOI18N
        myContent.append(" = Backbone.Collection.extend({\n");  // NOI18N
        myContent.append("model: ");                            // NOI18N
        myContent.append(modelName);
        myContent.append(",\nurl : \"");                        // NOI18N
        myContent.append( getUrl( collectionPath ));
        myContent.append("\"\n"); 
        myContent.append("});\n\n");                             // NOI18N
    }

    private String parse( TypeElement entity, CompilationController controller ) {
        /*
         *  parse entity and generate attributes:
         *  1) idAttribute
         *  2) primitive attributes if any
         *  3) do not include attributes with complex type  
         */
        List<VariableElement> fields = ElementFilter.fieldsIn(entity.getEnclosedElements());
        VariableElement id = null;
        for (VariableElement field : fields) {
            if ( getAnnotation(field, ID) != null ){
                TypeMirror fieldType = field.asType();
                if ( fieldType.getKind().isPrimitive() ){
                    id = field;
                    break;
                }
                Element fieldTypeElement = controller.getTypes().asElement(fieldType);
                TypeElement stringElement = controller.getElements().
                    getTypeElement(String.class.getName());
                if ( stringElement != null && stringElement.equals( fieldTypeElement)){
                    id = field;
                    break;
                }
                
                PackageElement pack = controller.getElements().getPackageOf(
                        fieldTypeElement);
                if ( pack.getQualifiedName().contentEquals("java.lang")){      // NOI18N
                    try {
                        if ( controller.getTypes().unboxedType(fieldType) != null ){
                            id = field;
                            break;
                        }
                    }
                    catch(IllegalArgumentException e){
                        // just skip field
                    }
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        if ( id != null ){
            String idAttr = id.getSimpleName().toString();
            builder.append("idAttribute : '");                          // NO18N
            builder.append(idAttr);
            builder.append("'");                                        // NO18N
        }
        
        
        if ( builder.length() >0 ){
            return builder.toString();
        }
        else {
            return null;
        }
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

}
