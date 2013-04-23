/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.netbeans.modules.websvc.rest.model.api.RestProviderDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

public class ApplicationSubclassGenerator {

    static final String GET_REST_RESOURCE_CLASSES = "getRestResourceClasses";//NOI18N
    static final String GET_REST_RESOURCE_CLASSES2 = "addRestResourceClasses";//NOI18N
    private static final String GET_CLASSES = "getClasses";                         //NOI18N

    private static final String JACKSON_JSON_PROVIDER =
            "org.codehaus.jackson.jaxrs.JacksonJsonProvider";               // NOI18N
    private static final String JACKSON_JERSEY2_JSON_PROVIDER =
            "org.glassfish.jersey.jackson.JacksonFeature";               // NOI18N

    private RequestProcessor.Task refreshTask = null;
    private static RequestProcessor RP = new RequestProcessor(ApplicationSubclassGenerator.class);

    private RestSupport restSupport;

    public ApplicationSubclassGenerator(RestSupport restSupport) {
        this.restSupport = restSupport;
    }

    public void refreshApplicationSubclass() {
        getRefreshTask().schedule(1000);
    }

    private synchronized RequestProcessor.Task getRefreshTask() {
        if (refreshTask == null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        doReconfigure();
                    }
                    catch(IOException e ){
                        Logger.getLogger(RestSupport.class.getName()).log(
                                Level.INFO, e.getLocalizedMessage(), e);
                    }
                }
            };
            refreshTask = RP.create(runnable);
        }
        return refreshTask;
    }

    private void doReconfigure() throws IOException {
        RestApplicationModel restAppModel = restSupport.getRestApplicationsModel();
        if (restAppModel != null) {
            try {
                restAppModel.runReadAction(
                        new MetadataModelAction<RestApplications, Void>() {

                            @Override
                            public Void run(final RestApplications metadata)
                                    throws IOException
                            {
                                List<RestApplication> applications =
                                        metadata.getRestApplications();
                                if ( applications!= null &&
                                        !applications.isEmpty())
                                {
                                    RestApplication application =
                                            applications.get(0);
                                    String clazz = application.
                                            getApplicationClass();
                                    reconfigApplicationClass(clazz);
                                }
                                return null;
                            }
                        });
            }
                catch (MetadataModelException ex) {
                Logger.getLogger(RestSupport.class.getName()).log(
                        Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }

    protected void reconfigApplicationClass( String appClassFqn ) throws IOException{
        JavaSource javaSource = MiscPrivateUtilities.getJavaSourceFromClassName(restSupport.getProject(), appClassFqn);
        if ( javaSource == null ){
            return;
        }
        javaSource.runModificationTask( new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree tree = workingCopy.getCompilationUnit();
                for (Tree typeDeclaration : tree.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                        MethodTree getClasses = null;
                        MethodTree restResources = null;
                        MethodTree restResources2 = null;
                        ClassTree classTree = (ClassTree) typeDeclaration;
                        List<? extends Tree> members = classTree.getMembers();
                        for (Tree member : members) {
                            if ( member.getKind().equals(Tree.Kind.METHOD)){
                                MethodTree method = (MethodTree)member;
                                String name = method.getName().toString();
                                if ( name.equals(GET_CLASSES)){
                                    getClasses = method;
                                }
                                else if ( name.equals(GET_REST_RESOURCE_CLASSES)){
                                    restResources = method;
                                } else if ( name.equals(GET_REST_RESOURCE_CLASSES2)){
                                    restResources2 = method;
                                }
                            }
                        }
                        TreeMaker maker = workingCopy.getTreeMaker();
                        ClassTree modified = classTree;

                        if (getClasses != null && restResources != null) {
                            // this is old code generator replaced in NB 7.3.1
                            // as part of EE7 upgrade:
                            modified = removeResourcesMethod( restResources,
                                    maker, modified);
                            modified = createMethodsOlderVersion(
                                    maker, modified, workingCopy);
                        } else {
                            if (restResources2 != null) {
                                modified = removeResourcesMethod( restResources2,
                                        maker, modified);
                            }
                            modified = createMethods(getClasses,
                                    maker, modified , restResources2 == null,
                                    workingCopy);
                        }

                        workingCopy.rewrite(classTree, modified);
                    }
                }
            }

        }).commit();
        Collection<FileObject> files = javaSource.getFileObjects();
        if ( files.isEmpty() ){
            return;
        }
        FileObject fileObject = files.iterator().next();
        DataObject dataObject = DataObject.find(fileObject);
        if ( dataObject!= null){
            SaveCookie cookie = dataObject.getLookup().lookup(SaveCookie.class);
            if ( cookie!= null ){
                cookie.save();
            }
        }
    }

    private ClassTree removeResourcesMethod( MethodTree restResources,
            TreeMaker maker, ClassTree modified )
    {
        return maker.removeClassMember(modified, restResources);
    }

    private ClassTree createMethods( MethodTree getClasses,
            TreeMaker maker,ClassTree modified, boolean addComment,
            CompilationController controller) throws IOException
    {
        WildcardTree wildCard = maker.Wildcard(Tree.Kind.UNBOUNDED_WILDCARD,
                null);
        ParameterizedTypeTree wildClass = maker.ParameterizedType(
                maker.QualIdent(Class.class.getCanonicalName()),
                Collections.singletonList(wildCard));
        ParameterizedTypeTree wildSet = maker.ParameterizedType(
                maker.QualIdent(Set.class.getCanonicalName()),
                Collections.singletonList(wildClass));
        if ( getClasses == null ){
            ModifiersTree modifiersTree = maker.Modifiers(
                    EnumSet.of(Modifier.PUBLIC), Collections.singletonList(
                            maker.Annotation( maker.QualIdent(
                                    Override.class.getCanonicalName()),
                                    Collections.<ExpressionTree>emptyList())));
            MethodTree methodTree = maker.Method(modifiersTree,
                    GET_CLASSES, wildSet,
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    createBodyForGetClassesMethod(), null);
            modified = maker.addClassMember(modified, methodTree);
        }
        StringBuilder builder = new StringBuilder();
        collectRestResources(builder, controller, false);
        ModifiersTree modifiersTree = maker.Modifiers(EnumSet
                .of(Modifier.PRIVATE));
        VariableTree newParam = maker.Variable(
                maker.Modifiers(Collections.<Modifier>emptySet()),
                "resources", wildSet, null);
        MethodTree methodTree = maker.Method(modifiersTree,
                GET_REST_RESOURCE_CLASSES2, maker.Type("void"),
                Collections.<TypeParameterTree> emptyList(),
                Arrays.asList(newParam),
                Collections.<ExpressionTree> emptyList(), builder.toString(),
                null);
        if (addComment) {
            Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2,
                    "Do not modify "+GET_REST_RESOURCE_CLASSES2+"() method.\nIt is "
                    + "automatically re-generated by "
                    + "NetBeans REST support to populate\ngiven list with "
                    + "all resources defined in the project."); // NOI18N
            maker.addComment(methodTree, comment, true);
        }
        modified = maker.addClassMember(modified, methodTree);
        return modified;
    }

    private ClassTree createMethodsOlderVersion(
            TreeMaker maker,ClassTree modified,
            CompilationController controller) throws IOException
    {
        WildcardTree wildCard = maker.Wildcard(Tree.Kind.UNBOUNDED_WILDCARD,
                null);
        ParameterizedTypeTree wildClass = maker.ParameterizedType(
                maker.QualIdent(Class.class.getCanonicalName()),
                Collections.singletonList(wildCard));
        ParameterizedTypeTree wildSet = maker.ParameterizedType(
                maker.QualIdent(Set.class.getCanonicalName()),
                Collections.singletonList(wildClass));
        StringBuilder builder = new StringBuilder();
        collectRestResources(builder, controller, true);
        ModifiersTree modifiersTree = maker.Modifiers(EnumSet
                .of(Modifier.PRIVATE));
        MethodTree methodTree = maker.Method(modifiersTree,
                GET_REST_RESOURCE_CLASSES, wildSet,
                Collections.<TypeParameterTree> emptyList(),
                Collections.<VariableTree> emptyList(),
                Collections.<ExpressionTree> emptyList(), builder.toString(),
                null);
        modified = maker.addClassMember(modified, methodTree);
        return modified;
    }

    private String createBodyForGetClassesMethod() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        builder.append("Set<Class<?>> resources = new java.util.HashSet<Class<?>>();");// NOI18N
        if (restSupport.isJersey2()) {
            builder.append(getJersey2JSONFeature());
        } else {
            builder.append(getJacksonProviderSnippet());
        }
        builder.append(GET_REST_RESOURCE_CLASSES2+"(resources);");
        builder.append("return resources;}");
        return builder.toString();
    }

    private void collectRestResources( final StringBuilder builder ,
            final CompilationController controller, final boolean oldVersion) throws IOException
    {
        RestServicesModel model = restSupport.getRestServicesModel();
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>()
            {

                @Override
                public Void run( RestServicesMetadata metadata )
                        throws Exception
                {
                    builder.append('{');
                    if (oldVersion) {
                        builder.append("Set<Class<?>> resources = new java.util.HashSet<Class<?>>();");// NOI18N
                    }
                    RestServices services = metadata.getRoot();
                    for (RestServiceDescription description : services.getRestServiceDescription()){
                        handleResource(controller, description.getClassName(), builder);
                    }
                    for (RestProviderDescription provider : services.getProviders()){
                        handleResource(controller, provider.getClassName(), builder);
                    }
                    if (oldVersion) {
                        if (restSupport.isJersey2()) {
                            builder.append(getJersey2JSONFeature());
                        } else {
                            builder.append(getJacksonProviderSnippet());
                        }
                    }
                    if (oldVersion) {
                        builder.append("return resources;");                // NOI18N
                    }
                    builder.append('}');
                    return null;
                }

            });
        }
        catch (MetadataModelException e) {
            Logger.getLogger(RestSupport.class.getName()).log(Level.INFO,
                    e.getLocalizedMessage(), e);
        }
    }

    private boolean handleResource(CompilationController controller, String className, StringBuilder builder) throws IllegalArgumentException {
        // Fix for BZ#216168
        TypeElement typeElement = controller.getElements().getTypeElement(className);
        if (typeElement != null) {
            FileObject file = SourceUtils.getFile(ElementHandle.
                    create(typeElement), controller.getClasspathInfo());
            if (file == null) {
                return false;
            }
        }
        builder.append("resources.add(");       // NOI18N
        builder.append( className );
        builder.append(".class);");             // NOI18N
        return true;
    }

    private String getJacksonProviderSnippet(){
        boolean addJacksonProvider = MiscPrivateUtilities.hasResource(restSupport.getProject(),
                "org/codehaus/jackson/jaxrs/JacksonJsonProvider.class");    // NOI18N
        if( !addJacksonProvider) {
            JaxRsStackSupport support = restSupport.getJaxRsStackSupport();
            if (support != null){
                addJacksonProvider = support.isBundled(JACKSON_JSON_PROVIDER);
            }
        }
        StringBuilder builder = new StringBuilder();
        if ( addJacksonProvider ){
            builder.append("\n// following code can be used to customize Jersey 1.x JSON provider: \n");
            builder.append("try {");
            builder.append("Class jacksonProvider = Class.forName(");
            builder.append('"');
            builder.append(JACKSON_JSON_PROVIDER);
            builder.append("\");");
            builder.append("resources.add(jacksonProvider);");
            builder.append("} catch (ClassNotFoundException ex) {");
            builder.append("java.util.logging.Logger.getLogger(getClass().getName())");
            builder.append(".log(java.util.logging.Level.SEVERE, null, ex);}\n");
            return builder.toString();
        }
        else {
            return builder.toString();
        }
    }

    private String getJersey2JSONFeature(){
        boolean addProvider = MiscPrivateUtilities.hasResource(restSupport.getProject(),
                "org/glassfish/jersey/jackson/JacksonFeature.class");    // NOI18N
        if( !addProvider) {
            JaxRsStackSupport support = restSupport.getJaxRsStackSupport();
            if (support != null){
                addProvider = support.isBundled(JACKSON_JERSEY2_JSON_PROVIDER);
            }
        }
        StringBuilder builder = new StringBuilder();
        if ( addProvider ){
            builder.append("\n// following code can be used to customize Jersey 2.0 JSON provider: \n");
            builder.append("try {");
            builder.append("Class jsonProvider = Class.forName(");
            builder.append('"');
            builder.append(JACKSON_JERSEY2_JSON_PROVIDER);
            builder.append("\");\n");
            builder.append("// Class jsonProvider = Class.forName(");
            builder.append('"');
            builder.append("org.glassfish.jersey.moxy.json.MoxyJsonFeature");
            builder.append("\");\n");
            builder.append("// Class jsonProvider = Class.forName(");
            builder.append('"');
            builder.append("org.glassfish.jersey.jettison.JettisonFeature");
            builder.append("\");\n");
            builder.append("resources.add(jsonProvider);");
            builder.append("} catch (ClassNotFoundException ex) {");
            builder.append("java.util.logging.Logger.getLogger(getClass().getName())");
            builder.append(".log(java.util.logging.Level.SEVERE, null, ex);}\n");
            return builder.toString();
        }
        else {
            return builder.toString();
        }
    }
}
