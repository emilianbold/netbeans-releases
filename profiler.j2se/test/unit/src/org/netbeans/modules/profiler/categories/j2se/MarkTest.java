/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.categories.j2se;

import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

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


/**
 * @author ads
 *
 */
public class MarkTest extends TestBase {
    
    private static final String JAVA_APP_NAME = "JavaApp";

    public MarkTest( String name ) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return JAVA_APP_NAME;
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
    
    public void testFilesCategoryMarks(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        boolean childFound = false;
        
        Set<String> outputStreamMethods = getCategorizationMethods( FileOutputStream.class);
        Set<String> outputStreamMethods1 = new HashSet( outputStreamMethods );
        Set<String> outputStreamMethods2 = new HashSet( outputStreamMethods ); 
        
        Set<String> excludedOSMethods = getExcludedMethods( FileOutputStream.class );
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "files.TestFileOutputStream".equals(className)){
                testFound = true;
                outputStreamMethods1.remove(methodName);
                if ( !outputStreamMethods.contains( methodName)){
                    assertTrue( "Found unexpected mark for 'files.TestFileOutputStream." +
                            methodName+"'", false );
                }
                assertNotSame("There is a MarkMapping for files.TestFileOutputStream.reset() method " +
                		"which should not present in Files Category for" +
                		" subtypes of FileOutputStream", "reset", methodName);
                assertFalse( "There is a mark for 'files.TestFileOutputStream."
                        +methodName+"' method", excludedOSMethods.contains(methodName));
            } 
            else if ("files.ChildTestOutputStream".equals(className)){
                childFound = true;
                outputStreamMethods2.remove(methodName );
                if ( !outputStreamMethods.contains( methodName)){
                    assertTrue( "Found unexpected mark for 'files.ChildTestFileOutputStream." +
                            methodName+"'", false );
                }
                assertNotSame("There is a MarkMapping for files.ChildTestOutputStream.reset() method " +
                        "which should not present in Files Category for" +
                        " subtypes of FileOutputStream", "reset", methodName);
                assertFalse( "There is a mark for 'files.ChildTestFileOutputStream."
                        +methodName+"' method", excludedOSMethods.contains(methodName));
            }
        }
        
        
        assertTrue( "Mark for files.TestFileOutputStream class is not found ", testFound );
        assertTrue( "Mark for files.ChildTestOutputStream class is not found", childFound );
        
        if ( outputStreamMethods1.size() != 0 ){
            assertTrue( "Mark for 'files.TestFileOutputStream."+
                    outputStreamMethods1.iterator().next()+"' is not found",
                    false);
        }
        if ( outputStreamMethods2.size() != 0 ){
            assertTrue( "Mark for 'files.ChildTestOutputStream."+
                    outputStreamMethods2.iterator().next()+"' is not found",
                    false);
        }
    }
    
    public void testListeners(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean dispatcherFound = false;
        boolean subDispatcherFound = false;
        boolean mouseListenerFound = false;
        String mouseListenerMethod = null;
        Set<String> listenerMethods = getMethods(MouseListener.class);
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "listeners.TestAWTEventListener".equals( className)){
                assertNotSame("Found mark for 'TestAWTEventListener.method()'", 
                    "method", methodName);
                if ( "eventDispatched".equals( methodName)){
                    dispatcherFound = true;
                }
                if ( getMethods( Object.class).contains(methodName)){
                   assertTrue( "Method 'listeners.TestAWTEventListener."+
                           methodName+ "' should not be marked", false);
                }
            }
            else if ( "listeners.SubTestAWTEventListener".equals( className)){
                assertNotSame("Found mark for 'listeners.SubTestAWTEventListener.method()'", 
                        "method", methodName);
                    if ( "eventDispatched".equals( methodName)){
                        subDispatcherFound = true;
                    }
                    if ( getMethods( Object.class).contains(methodName)){
                        assertTrue( "Method 'listeners.SubTestAWTEventListener."+
                                methodName+ "' should not be marked", false);
                     }
            }
            else if ( "listeners.TestMouseListener".equals( className)){
                mouseListenerFound = true;
                if ( !listenerMethods.contains( methodName)){
                    mouseListenerMethod = methodName;
                }
            }
        }
        assertTrue( "Mark for " +
        		"'listeners.TestAWTEventListener.eventDispatched() method' is not found", 
        		dispatcherFound);
        assertTrue( "Mark for " +
                "'listeners.SubTestAWTEventListener.eventDispatched() method' is not found", 
                subDispatcherFound);
        /*
         * * TODO : Temporary commented. There is an issue which need to be fixed
         * assertTrue( 'Mark  for listeners.TestMouseListener is not found",
                mouseListenerFound);
         * along with enabling this test.
         * 
         * assertNull("Mark for 'listeners.TestMouseListener."+mouseListenerMethod
                +"' is not found",mouseListenerMethod);
                */
    }
    
    public void testPainters(){
        MarkMapping[] mappings = getCategorization().getMappings();
        String method = null;
        Set<String> jComponentMethods = getCategorizationMethods(JComponent.class);
        Set<String> copyJComponentMethods = new HashSet<String>( jComponentMethods );
        Set<String> jComponentExcludedMethods = getExcludedMethods(JComponent.class);
        boolean found = false;
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "painters.TestPanel".equals( className)){
                found = true;
                assertNotSame("Found mark for 'TestPanel.getUI()'", 
                    "getUI", methodName);
                copyJComponentMethods.remove( methodName);
                if ( !jComponentMethods.contains(methodName) ){
                    assertTrue( "Unexpected mark for " +
                            "'painters.TestPanel."+ methodName+"' method is found", 
                            false);
                }
                assertFalse("Found mark for 'painters.TestPanel."+methodName
                        +"' method",jComponentExcludedMethods.contains( methodName));
            }
        }
        
        
        /* TODO : Temporary commented. There is an issue which need to be fixed
         * along with enabling this test.
        assertTrue( "Mark for painters.TestPanel class is not found",found );
                
        if ( copyJComponentMethods.size() != 0 ){
            assertTrue( "Mark for 'painters.TestPanel."+
                    copyJComponentMethods.iterator().next()+"' is not found",
                    copyJComponentMethods.size(),  0);
                    
        }*/
    }
    
    public void testSocketChanelMarks(){
        MarkMapping[] mappings = getCategorization().getMappings();
        String method = null;
        Set<String> socketChanel = getCategorizationMethods(SocketChannel.class);
        Set<String> copySocketChanel = new HashSet<String>( socketChanel );
        Set<String> socketChanelExcluded = getExcludedMethods(SocketChannel.class);
        boolean found = false;
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "socket.TestSocketChanel".equals( className)){
                found = true;
                copySocketChanel.remove(methodName);
                if ( !socketChanel.contains(methodName) ){
                    assertTrue( "Found unexpected mark for " +
                            "'socket.TestSocketChanel."+ methodName+"' method'", 
                            false);
                }
                assertFalse("Found mark for 'socket.TestSocketChanel."+methodName
                        +"' method",socketChanelExcluded.contains( methodName));
            }
        }
        assertTrue( "Mark for socket.TestSocketChanel class is not found",found );
        if ( copySocketChanel.size()!= 0){
            assertTrue( "Mark for 'socket.TestSocketChanel."+
                    copySocketChanel.iterator().next()+"' is not found", false);
        }
    }
    
    public void testInputStreamMarks(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        Set<String> inputStreamMethods = getCategorizationMethods( InputStream.class);
        Set<String> copyInputStreamMethods = new HashSet<String>( inputStreamMethods);
        Set<String> excludedISMethods = getExcludedMethods( InputStream.class );
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "streams.TestInputStream".equals(className)){
                testFound = true;
                assertNotSame("There is a mark for 'streams.TestInputStream.write' " +
                		"method","write",methodName);
                copyInputStreamMethods.remove( methodName );
                if ( !inputStreamMethods.contains(methodName)){
                    assertTrue( "Found unexpected mark for 'streams.TestInputStream."+ 
                            methodName+"' method", false );
                }
                assertFalse( "There is a mark for method : 'streams.TestInputStream."+
                        methodName+"'", excludedISMethods.contains( methodName));
            }
        }
        
        assertTrue( "Mark for streams.TestInputStream class is not found", testFound );
        if ( copyInputStreamMethods.size()!= 0){
            assertTrue( "Mark for 'streams.TestInputStream."+
                    copyInputStreamMethods.iterator().next()+"' is not found", false);
        }
        
    }
    
    private Set<String> getCategorizationMethods( Class<?>  clazz){
        assertFalse("Method getCategorizationMethods() should not be called " +
        		"for interface class. Otherwise it returns empty set." +
        		"This lead to incorrect test result." +
        		"Interface methods are not marked. Only implementation" +
        		" classes methods are marked",clazz.isInterface());
        String fqn = clazz.getCanonicalName();
        Set<String> methods= new HashSet<String>();
        MarkMapping[] mappings = getCategorization().getMappings();
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            if ( fqn.equals( className )){
                methods.add( markMapping.markMask.getMethodName());
            }
        }
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> allMethods = new LinkedList<Method>( Arrays.asList(declaredMethods));
        allMethods.addAll( Arrays.asList( clazz.getMethods()));
        Set<String> visibleMethods = new HashSet<String>();
        for (Method method : allMethods) {
            int modifiers = method.getModifiers();
            if ( Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers)){
                visibleMethods.add( method.getName());
            }
        }
        //methods.retainAll(visibleMethods);
        return methods;
    }
    
    private Set<String> getExcludedMethods( Class<?> clazz){
        Method[] methods = clazz.getMethods();
        Set<String> allMethods = new HashSet<String>();
        for (Method method : methods) {
            allMethods.add( method.getName());
        }
        allMethods.removeAll(getCategorizationMethods(clazz));
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
            Class<?> clazz = null;
            try {
                clazz  = Class.forName(typeName);
                if ( clazz.isInterface() ){
                    return;
                }
            }
            catch (ClassNotFoundException e) {
            }
            if (excludes == null && includes == null) {
                Set<String> allMethods = getMethods(clazz);
                boolean found = false;
                for (MarkMapping markMapping : mappings) {
                    if ( !markMapping.mark.equals( definition.getAssignedMark())){
                        continue;
                    }
                    String className = markMapping.markMask.getClassName();
                    if (typeName.equals(className)) {
                        found = true;
                        allMethods.remove(markMapping.markMask.getMethodName());
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
