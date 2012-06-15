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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class JSClientGenerator {
    
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
    
    private JSClientGenerator(RestServiceDescription description){
        myDescription = description;
    }

    static JSClientGenerator create( RestServiceDescription description )
    {
        return new JSClientGenerator(description);
    }

    public void generate( FileObject jsFile) {
        FileObject restSource = myDescription.getFile();
        if ( restSource == null ){
            return;
        }
        StringBuilder builder = new StringBuilder("$(function(){\n");
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
                
                handleRestMethods(controller , getMethods, postMethods, 
                        putMethods, deleteMethods);
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
        builder.append("});");
    }
    
    private void handleRestMethods( CompilationController controller,  
            List<ExecutableElement> getMethods,
            List<ExecutableElement> postMethods,
            List<ExecutableElement> putMethods,
            List<ExecutableElement> deleteMethods )
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
        for(Entry<String,ExecutableElement> entry : noParamGetMethods.entrySet()){
            String path = entry.getKey();
            ExecutableElement method = entry.getValue();
            
            TypeMirror returnType = method.getReturnType();
            TypeMirror entityCollectionType = getCollectionType(returnType, controller);
            if ( entityCollectionType == null ){
                // collection of entities
            }
            else {
                // just plain entity
            }
        }
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

}
