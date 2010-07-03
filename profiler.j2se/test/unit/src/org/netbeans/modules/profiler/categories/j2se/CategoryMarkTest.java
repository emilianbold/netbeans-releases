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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode.Children;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.categories.Category;


/**
 * @author ads
 *
 */
public class CategoryMarkTest extends TestBase {
    
    private static final String JAVA_APP_NAME = "JavaApp";

    public CategoryMarkTest( String name ) {
        super(name);
    }
    
    public void testJava2D(){
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> markedIds = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        plainIds.add(0);
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        builder.newThread( 1 , "Java2D Disposer", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphicsEnvironment", 0, 
                "<clinit>", "()V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("sun.java2d.windows.WindowsFlags$1", 0, 
                "run", "()Ljava/lang/Object;");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        markedIds.add( 3 );
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphics2D", 0, 
                "intersectShapes", "((Ljava/awt/Shape;Ljava/awt/Shape;ZZ)Ljava/awt/Shape;");
        builder.methodEntry( 4 , 1, 3, 0, 0);
        markedIds.add( 4 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root, 0 );
        Mark java2D =getJava2DMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(java2D)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Java 2D category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), false);
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                        		getCategorization().getCategoryForMark(mark), false);
            }
        }
        if ( !markedIds.isEmpty()){
            int id = markedIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked "+id, false);
        }
    }
    
    public void testInheritedJava2D(){
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> markedIds = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        fillMethodsInterval(2, 10, status, builder);
        
        builder.newThread( 1 , "Java2D Disposer", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphics2D", 0, 
                "setClip", "Ljava/awt/Shape;)V");
        builder.methodEntry( 10 , 0, 3, 0, 0);
        markedIds.add(10);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 11 , 0, 1, 0, 0);
        markedIds.add(11);
        
        status.updateInstrMethodsInfo("sun.java2d.pipe.hw.ExtendedBufferCapabilities", 0, 
                "<init>", "(Ljava/awt/ImageCapabilities;Ljava/awt/ImageCapabilities;Ljava/awt/BufferCapabilities$FlipContents;)V");
        
        builder.methodEntry( 12 , 0, 3, 0, 0);
        markedIds.add(12);
        
        status.updateInstrMethodsInfo("sun.java2d.d3d.D3DRTTSurfaceToSurfaceScale", 0, 
                "<init>", "()V");
        builder.methodEntry( 13 , 1, 3, 0, 0);
        markedIds.add(13);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "<init>", "()V");
        
        builder.methodEntry( 14 , 1, 1, 0, 0);
        markedIds.add(14);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "operation", "(Ljava/lang/String;II)V");
        
        builder.methodEntry( 15 , 1, 1, 0, 0);
        markedIds.add(15);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root, 10 );
        Mark java2D =getJava2DMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(java2D)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Java 2D category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), false);
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark), false);
            }
        }
        if ( !markedIds.isEmpty()){
            int id = markedIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
    public void testFilesCategory(){

        MarkMapping[] mappings = getCategorization().getMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> markedIds = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        fillMethodsInterval(2, 20, status, builder);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "<init>", "()V");
        builder.methodEntry( 20 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("java.io.FileReader", 0, 
                "reset", "()V");
        builder.methodEntry( 21 , 0, 3, 0, 0);
        markedIds.add( 21 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 22 , 0, 1, 0, 0);
        markedIds.add(22);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("files.TestFileReader", 1, 
                "skip", "(J)J");
        builder.methodEntry( 23 , 1, 3, 0, 0);
        markedIds.add(23);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 24, 1, 1, 0, 0);
        markedIds.add(24);
       
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root, 20 );
        Mark filesCategory =findCategory( getCategorization().getRoot(), "Files");
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(filesCategory)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Files category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), false);
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark), false);
            }
        }
        if ( !markedIds.isEmpty()){
            int id = markedIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
    @Override
    protected String getProjectName() {
        return JAVA_APP_NAME;
    }
    
    private void fillMethodsInterval( int start , int end , ProfilingSessionStatus
            status, TestGraphBuilder builder )
    {
        for ( int i=start ; i<end ; i++ ){
            status.updateInstrMethodsInfo("foo.FakeClass", 0, 
                "method"+i, "()V");
            builder.methodEntry( i , 0, 1, 0, 0);
        }
    }
    
    private Map<Integer, Mark> getMethodMarks( SimpleCPUCCTNode root , int id ){
        Map<Integer, Mark> result = new HashMap<Integer, Mark>();
        Stack<Mark> stack = new Stack<Mark>();
        collectMethodMarks(root, result, stack, id);
        return result;
    }
    
    private void collectMethodMarks( RuntimeCPUCCTNode node , Map<Integer, Mark> map,
            Stack<Mark> stack , int id )
    {
        if ( node instanceof MarkedCPUCCTNode ){
            Mark mark = ((MarkedCPUCCTNode)node).getMark();
            stack.push(mark);
        }
        else if ( node instanceof MethodCPUCCTNode ){
            int methodId = ((MethodCPUCCTNode)node).getMethodId();
            if ( methodId <id && !stack.isEmpty()){
                stack.pop();
            }
            if ( !stack.isEmpty()) {
                Mark mark = stack.peek();
                map.put(methodId, mark);
            }
        }
        Children children = node.getChildren();
        for ( int i = 0 ; i <children.size(); i++){
            RuntimeCPUCCTNode child = children.getChildAt(i);
                collectMethodMarks(child, map, stack, id );
        }
        if ( node instanceof MarkedCPUCCTNode && !stack.isEmpty() ){
            stack.pop();
        }
    }
                                      
    
    private Mark getJava2DMark( ){
        return findCategory(getCategorization().getRoot(), "Java 2D");
    }
    
    private Mark findCategory( Category parent , String categoryName ){
        Set<Category> subcategories = parent.getSubcategories();
        String label = parent.getLabel();
        if ( categoryName.equals( label)){
            return parent.getAssignedMark();
        }
        for (Category category : subcategories) {
            Mark mark = findCategory(category, categoryName );
            if ( mark != null ){
                return mark;
            }
        }
        return null;
    }
    
}
