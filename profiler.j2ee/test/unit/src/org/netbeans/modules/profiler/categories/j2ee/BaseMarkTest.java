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
package org.netbeans.modules.profiler.categories.j2ee;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.categorization.api.Category;
import org.netbeans.modules.profiler.categorization.api.definitions.CustomCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.PackageCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SingleTypeCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SubtypeCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.TypeCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.impl.CategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.impl.CategoryDefinitionProcessor;
import org.netbeans.modules.profiler.utilities.Visitable;
import org.netbeans.modules.profiler.utilities.Visitor;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class BaseMarkTest extends TestBase {
    
    static final String APP_NAME = "WebApp";

    public BaseMarkTest( String name ) {
        super(name);
    }

    public void testPackageMarks(){
        PackageCategoryReader reader = new PackageCategoryReader();
        readCategories( reader );
        for ( PackageCategoryDefinition pakage : reader.getPackageNames()){
            MarkMapping[] mappings = getCategorization().getMappings();
            for (MarkMapping markMapping : mappings) {
                if ( markMapping.mark.equals( pakage.getAssignedMark())){
                    String className = markMapping.markMask.getClassName();
                    assertEquals("Package name "+pakage.getPackageName()+"" +
                            		" should be part of class name for mark mask " +
                            		"of MarkMapping class created for pakage category",
                            		true , className.startsWith( pakage.getPackageName()));
                    if ( pakage.isRecursive() ){
                        StringBuilder builder = new StringBuilder( pakage.getPackageName());
                        builder.append( ".**");
                        assertEquals( "Recursive package category should" +
                        		" have wildcard class name for mark mask " +
                        		"of MarkMapping created for package category :" +
                        		builder, 
                        		className, builder.toString());
                    }
                }
            }
        }
    }
    
    public void testSingleTypeCategoryMarks(){
        checkTypeMarks( new SingleTypeCategoryReader() );
    }
    
    public void testSubtypeCategoryMarks(){
        checkTypeMarks( new SubCategoryReader());
    }
    
    public void testConnection(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        Category connection = getCategory("Connection");
        Set<String> dataSourceMethods = getCategorizationMethods( connection,
                "javax.sql.DataSource");
        dataSourceMethods.retainAll(getAllMethods("connection.TestDataSource"));
        Set<String> excluded = getExcludedMethods(connection, "javax.sql.DataSource");
        Set<String> objectMethods = getMethods( Object.class );
        dataSourceMethods.removeAll( objectMethods );
        Set<String> copyDataSourceMethods = new HashSet<String>( dataSourceMethods );
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "connection.TestDataSource".equals(className)){
                testFound = true;
                assertNotSame("There is a mark for 'connection.TestDataSource.method' " +
                                "method","method",methodName);
                assertFalse( "There is a mark for 'connection.TestDataSource.' " +
                        methodName ,objectMethods.contains(methodName));
                assertFalse( "There is a mark for 'connection.TestDataSource.' " +
                        methodName ,excluded.contains(methodName));
                copyDataSourceMethods.remove(methodName);
                if ( !dataSourceMethods.contains(methodName)){
                    assertTrue( "Found unexpected mark for " +
                    		"'connection.TestDataSource."+ methodName+"' method", 
                    		false );
                }
            }
        }

        assertTrue( "Mark for 'connection.TestDataSource' class is not found", testFound );
        if ( copyDataSourceMethods.size()!= 0){
            assertFalse( "Method 'connection.TestDataSource."+
                    copyDataSourceMethods.iterator().next()+"' is not marked",true) ;
        }
    }
    
    public void testStatements(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        Category statements = getCategory("Statements");
        Set<String> statementMethods = getCategorizationMethods(statements,
                "java.sql.Statement");
        statementMethods.retainAll(getAllMethods("statements.TestStatement"));
        Set<String> excluded = getExcludedMethods(statements,
            "java.sql.Statement");
        Set<String> objectMethods = getMethods( Object.class );
        
        Set<String> copyStatementMethods = new HashSet<String>(statementMethods);
        copyStatementMethods.removeAll( objectMethods );
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "statements.TestStatement".equals(className)){
                testFound = true;
                assertNotSame("There is a mark for 'statements.TestStatement.method' " +
                                "method","method",methodName);
                assertFalse( "There is a mark for 'connection.TestDataSource.' " +
                        methodName ,objectMethods.contains(methodName));
                assertFalse( "There is a mark for 'connection.TestDataSource.' " +
                        methodName ,excluded.contains(methodName));
                copyStatementMethods.remove(methodName);
                if ( !statementMethods.contains(methodName)){
                    assertTrue( "Found unexpected mark for 'statements.TestStatement."+ 
                            methodName+"' method", false );
                }
            }
        }

        assertTrue( "Mark for 'statements.TestStatement' class is not found", testFound );
        if ( copyStatementMethods.size()!= 0){
            assertFalse( "Method 'statements.TestStatement."+
                    copyStatementMethods.iterator().next()+"' is not marked",true) ;
        }
    }
    
    public void testJpas(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        Category jpa = getCategory("JPA");
        Set<String> queryMethods = getCategorizationMethods( jpa, "javax.persistence.Query");
        queryMethods.retainAll(getAllMethods("jpa.TestQuery"));
        Set<String> excluded = getExcludedMethods(jpa, "javax.persistence.Query");
        Set<String> objectMethods = getMethods( Object.class );
        
        Set<String> copyQueryMethods = getAllMethods( "javax.persistence.Query");
        copyQueryMethods.removeAll( objectMethods);
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "jpa.TestQuery".equals(className)){
                testFound = true;
                assertNotSame("There is a mark for 'jpa.TestQuery.method' " +
                                "method","method",methodName);
                assertFalse( "There is a mark for 'jpa.TestQuery.' " +
                        methodName ,objectMethods.contains(methodName));
                assertFalse( "There is a mark for 'jpa.TestQuery.' " +
                        methodName ,excluded.contains(methodName));
                copyQueryMethods.remove( methodName );
                if ( !queryMethods.contains(methodName)){
                    assertTrue( "Found unexpected mark for 'jpa.TestQuery."+ methodName+
                            "' method", false );
                }
            }
        }

        assertTrue( "Mark for 'jpa.TestQuery' class is not found", testFound );
        if ( copyQueryMethods.size()!= 0){
            assertFalse( "Method 'jpa.TestQuery."+
                    copyQueryMethods.iterator().next()+"' is not marked",true) ;
        }
    }
    
    public void testEndpointInvocation(){
        MarkMapping[] mappings = getCategorization().getMappings();
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            assertNotSame( "type.TestAbstractTubeImpl", className);
        }
    }
    
    public void testFilters(){
        MarkMapping[] mappings = getCategorization().getMappings();
        
        boolean filterFound = false;
        boolean chainFound = false;
        boolean subChainFound = false;
        
        Category filters = getCategory("Filters");
        Set<String> chainMethods = getCategorizationMethods( filters, 
                "javax.servlet.FilterChain");
        chainMethods.retainAll( getAllMethods("filter.TestChain"));
        Set<String> excludedChainMethods = getExcludedMethods(filters, 
                "javax.servlet.FilterChain");
        Set<String> objectMethods = getMethods( Object.class );
        
        Category lifecycle = findCategory( filters, "Life Cycle");
        Set<String> lifecycleFilterMethods = getCategorizationMethods( lifecycle, 
            "javax.servlet.Filter");
        lifecycleFilterMethods.retainAll( getAllMethods("filter.TestFilter"));
        Set<String> copyLifecycleFilterMethods = getCategorizationMethods( lifecycle, 
            "javax.servlet.Filter");
        Set<String> lifecycleExcludedFilterMethods = getExcludedMethods(lifecycle, 
            "javax.servlet.Filter");
        
        Set<String> filterMethods = getCategorizationMethods( filters, 
            "javax.servlet.Filter");
        filterMethods.retainAll( getAllMethods("filter.TestFilter"));
        Set<String> copyFilterMethods = new HashSet<String>( filterMethods ); 
        Set<String> excludedFilterMethods = getExcludedMethods(filters, 
            "javax.servlet.Filter");
        
        Set<String> copyChainMethods= new HashSet<String>( chainMethods );
        copyChainMethods.removeAll(objectMethods);
        Set<String> copyChainMethods1= new HashSet<String>( chainMethods );
        copyChainMethods1.removeAll( objectMethods );
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "filter.TestFilter".equals(className)){
                filterFound = true;
                if ( markMapping.mark.equals( filters.getAssignedMark())){
                    assertTrue( "Found unexpected mark for method " +
                            "'filter.TestFilter."+methodName+"' in Filters category", 
                            filterMethods.contains(methodName));
                    copyFilterMethods.remove(methodName );
                    assertFalse( "Found unexpected mark for method " +
                            "'filter.TestFilter."+methodName+"' in Filters category" 
                            ,excludedFilterMethods.contains(methodName));
                }
                else if (markMapping.mark.equals( lifecycle.getAssignedMark())){
                    assertTrue( "Found unexpected mark for method " +
                            "'filter.TestFilter."+methodName+"' in Filters/Life Cycle category", 
                            lifecycleFilterMethods.contains(methodName));
                    copyLifecycleFilterMethods.remove(methodName );
                    assertFalse( "Found unexpected mark for method " +
                            "'filter.TestFilter."+methodName+"' in Filters/Life Cycle  category" 
                            ,lifecycleExcludedFilterMethods.contains(methodName));
                }
                assertFalse( "There is a mark for 'filter.TestFilter.' " +
                        methodName ,objectMethods.contains(methodName));
            }
            else if ( "filter.TestChain".equals(className)){
                    chainFound = true;
                    assertFalse( "There is a mark for 'filter.TestChain.' " +
                            methodName ,objectMethods.contains(methodName));
                    assertFalse( "There is a mark for 'filter.TestSubChain.' " +
                            methodName ,excludedChainMethods.contains(methodName));
                    copyChainMethods.remove( methodName );
                    if ( !chainMethods.contains(methodName)){
                        assertFalse( "Unexpected mark for method '"+
                                markMapping.markMask.toFlattened()+
                                "' is found", true );
                    }
            }
            else if ( "filter.TestSubChain".equals(className)){
                subChainFound = true;
                assertNotSame("There is a mark for 'filter.TestSubChain.method' " +
                                "method","method",methodName);
                assertFalse( "There is a mark for 'filter.TestSubChain.' " +
                        methodName ,objectMethods.contains(methodName));
                assertFalse( "There is a mark for 'filter.TestSubChain.' " +
                        methodName ,excludedChainMethods.contains(methodName));
                copyChainMethods1.remove( methodName );
                if ( !chainMethods.contains(methodName)){
                    assertFalse( "Unexpected mark for method '"+
                            markMapping.markMask.toFlattened()+
                            "' is found", true );
                }
            }
        }

        assertTrue("Mark for class 'filter.TestFilter' is not found", filterFound);
        
        assertTrue( "Mark for class 'filter.TestChain' is not found",chainFound );
        assertTrue( "Mark for 'filter.TestSubChain'  is not found", subChainFound );
        
        if ( copyChainMethods.size()!= 0){
            assertFalse( "Method 'filter.TestChain."+
                    copyChainMethods.iterator().next()+"' is not marked",true) ;
        }
        
        if ( copyFilterMethods.size()!= 0){
            assertFalse( "Method 'filter.TestFilter."+
                    copyFilterMethods.iterator().next()+"' is not marked",true) ;
        }
        
        if ( copyLifecycleFilterMethods.size()!= 0){
            assertFalse( "Method 'filter.TestFilter."+
                    copyLifecycleFilterMethods.iterator().next()+"' is not marked",true) ;
        }
        
        if ( copyChainMethods1.size()!= 0){
            assertFalse( "Method 'filter.TestSubChain."+
                    copyChainMethods1.iterator().next()+"' is not marked",true) ;
        }
    }
    
    public void testJSTL(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        Category jstl = getCategory("JSTL");
        Set<String> tagSupportMethods = getCategorizationMethods(jstl,
                "javax.servlet.jsp.tagext.SimpleTagSupport");
        tagSupportMethods.retainAll( getAllMethods("jstl.TestTagSupport"));
        Set<String> excluededTagSupportMethods = getExcludedMethods(jstl,
            "javax.servlet.jsp.tagext.SimpleTagSupport");
        
        Set<String> copytagSupportMethods = new HashSet<String>( tagSupportMethods );
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "jstl.TestTagSupport".equals(className)){
                testFound = true;
                assertNotSame("There is an unexpected mark for 'jstl.TestTagSupport.method' " +
                                "method","method",methodName);
                copytagSupportMethods.remove( methodName );
                if ( !tagSupportMethods.contains(methodName)){
                    assertTrue( "Found unexpected mark for 'jstl.TestTagSupport."+ 
                            methodName+"' method", false );
                }
                assertFalse( "There is an unexpected mark for 'jstl.TestTagSupport." +
                        methodName+"'",excluededTagSupportMethods.contains( methodName));
            }
        }

        assertTrue( "Mark for 'jstl.TestTagSupport' class is not found", testFound );
        if ( copytagSupportMethods.size()!= 0){
            assertFalse( "Method 'jstl.TestTagSupport."+
                    copytagSupportMethods.iterator().next()+"' is not marked",true) ;
        }
    }
    
    public void tesListeners(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        Category listeners = getCategory("Listeners");
        Set<String> listenerMethods = getCategorizationMethods(listeners,
                "javax.servlet.http.HttpSessionListener");
        listenerMethods.retainAll(getAllMethods("listeners.TestHttpSessionListener"));
        Set<String> excluded = new HashSet<String>( listenerMethods );
        Set<String> objectMethods = getMethods( Object.class );
        
        Set<String> copyListenerMethods = new HashSet<String>( listenerMethods);
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "listeners.TestHttpSessionListener".equals(className)){
                testFound = true;
                assertNotSame("There is a mark for 'listeners.TestHttpSessionListener.method' " +
                                "method","method",methodName);
                assertFalse( "There is a mark for 'listeners.TestHttpSessionListener.' " +
                        methodName ,objectMethods.contains(methodName));
                assertFalse( "There is a mark for 'listeners.TestHttpSessionListener.' " +
                        methodName ,excluded.contains(methodName));
                copyListenerMethods.remove(methodName );
                if ( !listenerMethods.contains(methodName)){
                    assertTrue( "Found unexpected mark for " +
                    		"'listeners.TestHttpSessionListener."+ methodName+"' method", 
                            false );
                }
            }
        }

        assertTrue( "Mark for 'listeners.TestHttpSessionListener' class is not found", testFound );
        if ( copyListenerMethods.size()!= 0){
            assertFalse( "Method 'listeners.TestHttpSessionListener."+
                    copyListenerMethods.iterator().next()+"' is not marked",true) ;
        }
    }
    
    public void testServlets(){
        MarkMapping[] mappings = getCategorization().getMappings();
        
        boolean destroyFound = false;
        boolean initFound = false;
        boolean filterFound = false;
        
        Category servlets = getCategory("Servlets");
        Category lifecycle = findCategory( servlets, "Life Cycle");
        
        Set<String> httpServletMethods = getCategorizationMethods( servlets, 
                "javax.servlet.http.HttpServlet");
        httpServletMethods.retainAll( getAllMethods("servlets.TestHttpServlet"));
        Set<String> copyServletMethods = new HashSet<String>( httpServletMethods);
        Set<String> excludedHttpServletMethods = getExcludedMethods(servlets, 
                "javax.servlet.http.HttpServlet");
        
        Set<String> lifeCycleHttpServletMethods = getCategorizationMethods( lifecycle, 
            "javax.servlet.http.HttpServlet");
        lifeCycleHttpServletMethods.retainAll( getAllMethods("servlets.TestHttpServlet"));
        Set<String> copylifeCycleServletMethods = new HashSet<String>( 
                lifeCycleHttpServletMethods);
        Set<String> excludedLifeCycleHttpServletMethods = getExcludedMethods(lifecycle, 
            "javax.servlet.http.HttpServlet");
        
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "servlets.TestHttpServlet".equals(className)){
                filterFound = true;
                if ( markMapping.mark.equals( servlets.getAssignedMark())){
                    assertTrue( "Unexpected mark found for method "+
                            markMapping.markMask.toFlattened() , 
                            httpServletMethods.contains(methodName));
                    copyServletMethods.remove(methodName );
                    assertFalse( "Unexpected mark found for method "+
                            markMapping.markMask.toFlattened() ,
                            excludedHttpServletMethods.contains(methodName));
                }
                else if (markMapping.mark.equals( lifecycle.getAssignedMark())){
                    assertTrue( "Unexpected mark found for method "+
                            markMapping.markMask.toFlattened() , 
                            lifeCycleHttpServletMethods.contains(methodName));
                    copylifeCycleServletMethods.remove(methodName );
                    assertFalse( "Unexpected mark found for method "+
                            markMapping.markMask.toFlattened() ,
                            excludedLifeCycleHttpServletMethods.contains(methodName));
                    if ( "init".equals(methodName)){
                        initFound = true;
                    }
                    else if ( "destroy".equals( methodName)){
                        destroyFound = true;
                    }
                    else {
                        assertFalse( "Found unexpected mark for method '" +
                                "servlets.TestHttpServlet."+methodName+"' in category " +
                                        "Filters/Life Cycle", true );
                    }
                }
            }
        }

        assertTrue("Mark for class 'servlets.TestHttpServlet' is not found", filterFound);
        assertTrue( "Mark for 'servlets.TestHttpServlet.init' method is not found", initFound );
        assertTrue( "Mark for 'servlets.TestHttpServlet.destroy' method is not found", destroyFound );
        
        if ( copyServletMethods.size()!= 0){
            assertFalse( "Mark for  'servlets.TestHttpServlet."+
                    copyServletMethods.iterator().next()+"' is not found", true );
        }
        if ( copylifeCycleServletMethods.size()!= 0){
            assertFalse( "Mark for  'servlets.TestHttpServlet."+
                    copylifeCycleServletMethods.iterator().next()+"' is not found", true );
        }
    }
    
    @Override
    protected String getProjectName() {
        return APP_NAME;
    }
    
    
    private Set<String> getCategorizationMethods( Category category, String fqn )
    {
        if ( isInterface( fqn )){
            Set<String> result = new HashSet<String>();
            Set<CategoryDefinition> definitions = category.getDefinitions();
            for (CategoryDefinition definition : definitions) {
                if ( definition instanceof SubtypeCategoryDefinition ){
                    String typeName = ((SubtypeCategoryDefinition)definition).getTypeName();
                    if ( !fqn.equals( typeName)){
                        continue;
                    }
                    String[] includes = ((SubtypeCategoryDefinition)definition).getIncludes();
                    String[] excludes = ((SubtypeCategoryDefinition)definition).getExcludes();
                    
                    if (includes != null) {
                        for (String string : includes) {
                            result.add(string);
                        }
                    }
                    else if ( excludes!= null){
                        result = getAllMethods(fqn);
                        for (String exclude : excludes) {
                            result.remove(exclude);
                        }
                    }
                    else {
                        result = getAllMethods(fqn);
                    }
                }
            }
            result.removeAll(getMethods(Object.class));
            return result;
        }
        Set<String> methods = new HashSet<String>();
        MarkMapping[] mappings = getCategorization().getMappings();
        for (MarkMapping markMapping : mappings) {
            if (category.getAssignedMark().equals(markMapping.mark)) {
                String className = markMapping.markMask.getClassName();
                if (fqn.equals(className)) {
                    methods.add(markMapping.markMask.getMethodName());
                }
            }
        }
        return methods;
    }
    
    private Set<String> getExcludedMethods( Category category, String fqn ){
        Set<String> allMethods = getAllMethods(fqn);
        allMethods.removeAll(getCategorizationMethods(category, fqn));
        return allMethods;
    }
    
    private Set<String> getMethods( Class<?> clazz ){
        Method[] methods = clazz.getMethods();
        Set<String> allMethods = new HashSet<String>();
        for (Method method : methods) {
            allMethods.add( method.getName());
        }
        return allMethods;
    }
    
    private Set<String> getAllMethods(final String fqn){
        ClasspathInfo info = ClasspathInfo.create( getClassPath(
                getProject(), ClassPath.BOOT), 
                getClassPath(getProject(), ClassPath.COMPILE), 
                getClassPath(getProject(), ClassPath.SOURCE));
        JavaSource javaSource = JavaSource.create(info, new FileObject[]{});
        final Set<String> result = new HashSet<String>();
        try {
        javaSource.runUserActionTask(  new Task<CompilationController>(){
            public void run(CompilationController controller) throws Exception {
                controller.toPhase( Phase.ELEMENTS_RESOLVED );
                TypeElement element = controller.getElements().getTypeElement(fqn);
                List<ExecutableElement> methods = ElementFilter.methodsIn(
                        controller.getElements().getAllMembers( element));
                for (ExecutableElement method : methods) {
                    if ( !method.getModifiers().contains(Modifier.PRIVATE))
                    {
                        result.add( method.getSimpleName().toString());
                    }
                }
            }
        }, true);
        }
        catch( IOException e ){
            throw new RuntimeException ( e);
        }
        return result;
    }
    
    private void checkTypeMarks( TypeCategoryReader reader ){
        readCategories(reader);
        for( TypeCategoryDefinition definition : reader.getTypeCategories()){
            String typeName = definition.getTypeName();
            MarkMapping[] mappings = getCategorization().getMappings();
            String absentMethod = null;
            String presentMethod = null;
            String[] includes = definition.getIncludes();
            if (includes != null) {
                for (String include : includes) {
                    boolean found = false;
                    for (MarkMapping markMapping : mappings) {
                        if ( !markMapping.mark.equals( definition.getAssignedMark())){
                            continue;
                        }
                        String className = markMapping.markMask.getClassName();
                        if (typeName.equals(className)) {
                            String methodName = markMapping.markMask.getMethodName();
                            if ( include.equals( methodName )){
                                found = true;
                            }
                        }
                    }
                    if ( !found ){
                        absentMethod = include;
                        break;
                    }
                }
                assertNull( "Method '"+ absentMethod+"' should be included for "+ typeName+
                        " but there is no MarkMapping with this method", absentMethod);
            }
            String[] excludes = definition.getExcludes();
            if (excludes != null) {
                excludes : for (String exclude : excludes) {
                    for (MarkMapping markMapping : mappings) {
                        if ( !markMapping.mark.equals( definition.getAssignedMark())){
                            continue;
                        }
                        String className = markMapping.markMask.getClassName();
                        if (typeName.equals(className)) {
                            String methodName = markMapping.markMask.getMethodName();
                            if ( exclude.equals( methodName )){
                                presentMethod = exclude;
                                break excludes;
                            }
                        }
                    }
                }
                assertNull( "Method '"+ presentMethod+"' should be excluded from "+ 
                        typeName+" but there is  MarkMapping with this method", 
                        presentMethod);
            }
            if ( isInterface(typeName)){
                return;
            }
            if (excludes == null && includes == null) {
                Set<String> allMethods = getAllMethods(typeName);
                boolean found = false;
                for (MarkMapping markMapping : mappings) {
                    if ( !markMapping.mark.equals( definition.getAssignedMark())){
                        continue;
                    }
                    String className = markMapping.markMask.getClassName();
                    if (typeName.equals(className)) {
                        found = true;
                        allMethods.remove( markMapping.markMask.getMethodName());
                    }
                }
                assertEquals( "There is no MarkMapping with class name '"+
                        typeName+"'", true , found);
                if ( !(definition instanceof SingleTypeCategoryDefinition) && allMethods.size()!= 0){
                    assertEquals( "Mark for '"+typeName+"."+
                            allMethods.iterator().next()+"' is not found",allMethods.size(),  0);
                }
            }
        }
    }
    
    private boolean isInterface( final String typeName ){
        ClasspathInfo info = ClasspathInfo.create( getClassPath(
                getProject(), ClassPath.BOOT), 
                getClassPath(getProject(), ClassPath.COMPILE), 
                getClassPath(getProject(), ClassPath.SOURCE));
        JavaSource javaSource = JavaSource.create(info, new FileObject[]{});
        final boolean[] result = new boolean[1];  
        try {
        javaSource.runUserActionTask(  new Task<CompilationController>(){
            public void run(CompilationController controller) throws Exception {
                controller.toPhase( Phase.ELEMENTS_RESOLVED );
                TypeElement element = controller.getElements().getTypeElement(typeName);
                result[0] = element.getKind() == ElementKind.INTERFACE; 
            }
        }, true);
        }
        catch( IOException e ){
            throw new RuntimeException ( e);
        }
        return result[0];
    }
    
    private ClassPath getClassPath( Project project, String type ) {
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
        SourceGroup[] webGroup = sources.getSourceGroups(
                WebProjectConstants.TYPE_WEB_INF);
        ClassPath[] paths = new ClassPath[ sourceGroups.length+webGroup.length];
        int i=0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
            i++;
        }
        for (SourceGroup sourceGroup : webGroup) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath( paths );
    }
    
    private void readCategories( CategoryProcessorAdaptor reader ){
        Category root = getCategorization().getRoot();
        root.accept(new Visitor<Visitable<Category>, Void, CategoryDefinitionProcessor>() {

            public Void visit(Visitable<Category> visitable, CategoryDefinitionProcessor parameter) {
                for(CategoryDefinition def : visitable.getValue().getDefinitions()) {
                    def.processWith(parameter);
                }
                return null;
            }
        }, reader);
    }
    
    static class CategoryProcessorAdaptor extends CategoryDefinitionProcessor {

        @Override
        public void process( SubtypeCategoryDefinition def ) {
        }

        @Override
        public void process( SingleTypeCategoryDefinition def ) {
        }

        @Override
        public void process( CustomCategoryDefinition def ) {
        }

        @Override
        public void process( PackageCategoryDefinition def ) {
        }
        
    }
    
    static class PackageCategoryReader extends CategoryProcessorAdaptor {
        @Override
        public void process( PackageCategoryDefinition def ) {
            myPackageCategoryDefinition.add( def );
        }
        
        List<PackageCategoryDefinition> getPackageNames(){
            return myPackageCategoryDefinition;
        }
        
        private List<PackageCategoryDefinition> myPackageCategoryDefinition 
            = new LinkedList<PackageCategoryDefinition>();
    }
    
    static class TypeCategoryReader extends CategoryProcessorAdaptor{
        
        protected void process(TypeCategoryDefinition definition){
            myTypeCategories.add( definition);
        }
        
        List<TypeCategoryDefinition> getTypeCategories(){
            return myTypeCategories;
        }
        
        private List<TypeCategoryDefinition> myTypeCategories = 
            new LinkedList<TypeCategoryDefinition>();
    }
    
    static class SingleTypeCategoryReader extends TypeCategoryReader {
        @Override
        public void process( SingleTypeCategoryDefinition def ) {
            process((TypeCategoryDefinition)def);
        }
    }
    
    static class SubCategoryReader extends TypeCategoryReader {

        @Override
        public void process( SubtypeCategoryDefinition def ) {
            process((TypeCategoryDefinition)def);
        }
    }

}
