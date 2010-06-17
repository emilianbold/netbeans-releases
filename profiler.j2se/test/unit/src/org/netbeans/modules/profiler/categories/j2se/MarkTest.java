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
package org.netbeans.modules.profiler.categories.j2se;

import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.categories.Category;
import org.netbeans.modules.profiler.categories.CategoryDefinition;
import org.netbeans.modules.profiler.categories.CategoryDefinitionProcessor;
import org.netbeans.modules.profiler.categories.definitions.CustomCategoryDefinition;
import org.netbeans.modules.profiler.categories.definitions.PackageCategoryDefinition;
import org.netbeans.modules.profiler.categories.definitions.SingleTypeCategoryDefinition;
import org.netbeans.modules.profiler.categories.definitions.SubtypeCategoryDefinition;
import org.netbeans.modules.profiler.categories.definitions.TypeCategoryDefinition;
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
        String method = null;
        String childMethod = null;
        Set<String> outputStreamMethods = getCategorizationMethods( FileOutputStream.class);
        Set<String> excludedOSMethods = getExcludedMethods( FileOutputStream.class );
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "files.TestFileOutputStream".equals(className)){
                testFound = true;
                if ( !outputStreamMethods.contains( methodName)){
                    method = methodName;
                }
                assertNotSame("There is a MarkMapping for files.TestFileOutputStream.reset() method " +
                		"which should not present in Files Category for" +
                		" subtypes of FileOutputStream", "reset", methodName);
                assertFalse( "There is a mark for 'files.TestFileOutputStream."
                        +methodName+"' method", excludedOSMethods.contains(methodName));
            } 
            else if ("files.ChildTestOutputStream".equals(className)){
                childFound = true;
                if ( !outputStreamMethods.contains( methodName)){
                    childMethod = methodName;
                }
                assertNotSame("There is a MarkMapping for files.ChildTestOutputStream.reset() method " +
                        "which should not present in Files Category for" +
                        " subtypes of FileOutputStream", "reset", methodName);
                assertFalse( "There is a mark for 'files.ChildTestFileOutputStream."
                        +methodName+"' method", excludedOSMethods.contains(methodName));
            }
        }
        
        
        assertTrue( "No found mark for files.TestFileOutputStream class", testFound );
        assertTrue( "No found mark for files.ChildTestOutputStream class", childFound );
        assertNull( "No found mark for 'files.TestFileOutputStream." +method+"'", method );
        assertNull( "No found mark for 'files.ChildTestFileOutputStream." +childMethod+"'", childMethod );
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
            }
            else if ( "listeners.SubTestAWTEventListener".equals( className)){
                assertNotSame("Found mark for 'listeners.SubTestAWTEventListener.method()'", 
                        "method", methodName);
                    if ( "eventDispatched".equals( methodName)){
                        subDispatcherFound = true;
                    }
            }
            else if ( "listeners.TestMouseListener".equals( className)){
                mouseListenerFound = true;
                if ( !listenerMethods.contains( methodName)){
                    mouseListenerMethod = methodName;
                }
            }
        }
        assertTrue( "No mark for " +
        		"'listeners.TestAWTEventListener.eventDispatched() method'", 
        		dispatcherFound);
        assertTrue( "No mark for " +
                "'listeners.SubTestAWTEventListener.eventDispatched() method'", 
                subDispatcherFound);
        /*
         * * TODO : Temporary commented. There is an issue which need to be fixed
         * assertTrue( "No marks found for listeners.TestMouseListener",
                mouseListenerFound);
         * along with enabling this test.
         * 
         * assertNull("No marks found for 'listeners.TestMouseListener."+mouseListenerMethod
                +"'",mouseListenerMethod);
                */
    }
    
    public void testPainters(){
        MarkMapping[] mappings = getCategorization().getMappings();
        String method = null;
        Set<String> jComponentMethods = getCategorizationMethods(JComponent.class);
        Set<String> jComponentExcludedMethods = getExcludedMethods(JComponent.class);
        boolean found = false;
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "painters.TestPanel".equals( className)){
                found = true;
                assertNotSame("Found mark for 'TestPanel.getUI()'", 
                    "getUI", methodName);
                if ( !jComponentMethods.contains(methodName) ){
                    method = methodName;
                    break;
                }
                assertFalse("Found mark for 'painters.TestPanel."+methodName
                        +"' method",jComponentExcludedMethods.contains( methodName));
            }
        }
        
        
        /* TODO : Temporary commented. There is an issue which need to be fixed
         * along with enabling this test.
        assertTrue( "No marks found for painters.TestPanel class",found );
        assertNull( "No mark for " +
                "'painters.TestPanel."+ method+"' method'", 
                method);
                */
    }
    
    public void testSocketChanelMarks(){
        MarkMapping[] mappings = getCategorization().getMappings();
        String method = null;
        Set<String> socketChanel = getMethods(SocketChannel.class);
        Set<String> socketChanelExcluded = getExcludedMethods(SocketChannel.class);
        boolean found = false;
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "socket.TestSocketChanel".equals( className)){
                found = true;
                if ( !socketChanel.contains(methodName) ){
                    method = methodName;
                    break;
                }
                assertFalse("Found mark for 'socket.TestSocketChanel."+methodName
                        +"' method",socketChanelExcluded.contains( methodName));
            }
        }
        assertTrue( "No marks found for socket.TestSocketChanel class",found );
        assertNull( "No mark for " +
                "'socket.TestSocketChanel."+ method+"' method'", 
                method);
    }
    
    public void testInputStreamMarks(){
        MarkMapping[] mappings = getCategorization().getMappings();
        boolean testFound = false;
        String method = null;
        Set<String> inputStreamMethods = getCategorizationMethods( InputStream.class);
        Set<String> excludedISMethods = getExcludedMethods( InputStream.class );
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            String methodName = markMapping.markMask.getMethodName();
            if ( "streams.TestInputStream".equals(className)){
                testFound = true;
                assertNotSame("There is a mark for 'streams.TestInputStream.write' " +
                		"method","write",methodName);
                if ( !inputStreamMethods.contains(methodName)){
                    method = methodName;
                }
                assertFalse( "There is a mark for method : 'streams.TestInputStream."+
                        methodName+"'", excludedISMethods.contains( methodName));
            }
        }
        
        assertTrue( "No found mark for files.TestFileOutputStream class", testFound );
        
        assertNull( "No found mark for 'streams.TestInputStream."+ method+"' method", method );
    }
    
    private Set<String> getCategorizationMethods( Class<?>  clazz){
        String fqn = clazz.getCanonicalName();
        Set<String> methods= new HashSet<String>();
        MarkMapping[] mappings = getCategorization().getMappings();
        for (MarkMapping markMapping : mappings) {
            String className = markMapping.markMask.getClassName();
            if ( fqn.equals( className )){
                methods.add( markMapping.markMask.getMethodName());
            }
        }
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
                boolean found = false;
                for (MarkMapping markMapping : mappings) {
                    String className = markMapping.markMask.getClassName();
                    if (typeName.equals(className)) {
                        found = true;
                    }
                }
                assertEquals( "There is no MarkMapping with class name '"+
                        typeName+"'", true , found);
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
