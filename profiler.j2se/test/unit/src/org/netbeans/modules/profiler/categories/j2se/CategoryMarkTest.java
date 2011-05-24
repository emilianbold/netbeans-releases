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
import org.netbeans.modules.profiler.categorization.api.Category;


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
        resetMarkMappings();
        
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
        
        builder.methodExit( 3 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphics2D", 0, 
                "intersectShapes", "((Ljava/awt/Shape;Ljava/awt/Shape;ZZ)Ljava/awt/Shape;");
        builder.methodEntry( 4 , 1, 2, 0, 0);
        markedIds.add( 4 );
        
        builder.methodExit(4 , 1, 2, 0, 0);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root);
        Mark java2D =getJava2DMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, java2D, "Java 2D" );
    }
    
    public void testInheritedJava2D(){
        resetMarkMappings();
        
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
        
        builder.newThread( 1 , "Java2D Disposer", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphics2D", 0, 
                "setClip", "Ljava/awt/Shape;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add(2);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add(3);
        
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        builder.methodExit( 2 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.pipe.hw.ExtendedBufferCapabilities", 0, 
                "<init>", "(Ljava/awt/ImageCapabilities;Ljava/awt/ImageCapabilities;Ljava/awt/BufferCapabilities$FlipContents;)V");
        
        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add(4);
        
        builder.methodExit( 4 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.d3d.D3DRTTSurfaceToSurfaceScale", 0, 
                "<init>", "()V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        markedIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "<init>", "()V");
        
        builder.methodEntry( 6 , 1, 1, 0, 0);
        markedIds.add(6);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "operation", "(Ljava/lang/String;II)V");
        
        builder.methodEntry( 7 , 1, 1, 0, 0);
        markedIds.add(7);
        
        builder.methodExit( 7 , 1, 1, 0, 0);
        builder.methodExit( 6 , 1, 1, 0, 0);
        builder.methodExit( 5 , 1, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 8 , 1, 1, 0, 0);
        plainIds.add(8);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Mark java2D =getJava2DMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, java2D, "Java 2D");
    }
    
    public void testFilesCategory(){
        resetMarkMappings();
        
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
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "<init>", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("java.io.FileReader", 0, 
                "reset", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        markedIds.add( 3 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        markedIds.add(4);
        
        builder.methodExit( 4 , 0, 1, 0, 0);
        builder.methodExit( 3 , 0, 3, 0, 0);
        builder.methodExit( 2 , 0, 1, 0, 0);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("files.TestFileReader", 1, 
                "skip", "(J)J");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        markedIds.add(5);
        
        builder.methodExit( 5 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
        plainIds.add(6);
       
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Mark filesCategory =getCategoryMark( "Files");
        
        checkMarks(status, markedIds, plainIds, methodMarks, filesCategory, "Files");
    }
    
    public void testMixedJ2DCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> filesIds = new HashSet<Integer>();
        Set<Integer> java2DIds = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("sun.java2d.loops.DrawPolygons", 0, 
                "<init>", "()V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        java2DIds.add(2);
        
        status.updateInstrMethodsInfo("java.io.FileReader", 0, 
                "reset", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        filesIds.add( 3 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        filesIds.add(4);
        
        builder.methodExit( 4 , 0, 1, 0, 0);
        builder.methodExit( 3 , 0, 3, 0, 0);
        builder.methodExit( 2 , 0, 3, 0, 0);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("files.TestFileReader", 1, 
                "skip", "(J)J");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        filesIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
        filesIds.add(6);
        
        builder.methodExit( 6 , 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.d3d.D3DBlitLoops", 0, 
                "register", "()V");
        builder.methodEntry( 7, 1, 3, 0, 0);
        java2DIds.add(7);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        builder.methodEntry( 8, 1, 1, 0, 0);
        java2DIds.add(8);
        
        builder.methodExit( 8, 1, 1, 0, 0);
        builder.methodExit( 7, 1, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        builder.methodEntry( 9, 1, 1, 0, 0);
        filesIds.add(9);
       
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Mark filesCategory =getCategoryMark(  "Files");
        Mark java2DCategory = getJava2DMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if ( java2DIds.contains( id)){
                if (!mark.equals(java2DCategory)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Java 2D category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), false);
                }
                java2DIds.remove(id);
            }
            else  if ( filesIds.contains( id)){
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
                filesIds.remove( id );
            }
                
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
            }
        }
        if ( !filesIds.isEmpty()){
            int id = filesIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
        if ( !java2DIds.isEmpty()){
            int id = java2DIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
    public void testSocketCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "read", "([Ljava/nio/ByteBuffer;)J");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        ids.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        ids.add(3);
        
        builder.methodExit( 3 , 0, 1, 0, 0);
        builder.methodExit( 2 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        plainIds.add(4);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        plainIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
                "method", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
        plainIds.add(6);
        
        builder.methodExit( 6, 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "write", "([Ljava/nio/ByteBuffer;)J");
        builder.methodEntry( 7, 1, 3, 0, 0);
        ids.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root);
        Mark filesCategory =getCategoryMark(  "Socket");
        
        checkMarks(status, ids, plainIds, methodMarks, filesCategory, "Socket");
    }
    
    public void testIOCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("java.io.OutputStreamWriter", 0, 
                "write", "([CII)V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        ids.add(3);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        ids.add(4);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        plainIds.add(5);
        
        status.updateInstrMethodsInfo("streams.TestInputStream", 0, 
                "write", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
        plainIds.add(6);
        builder.methodExit( 6, 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("streams.TestInputStream", 0, 
                "skip", "(J)J");
        builder.methodEntry( 7, 1, 3, 0, 0);
        ids.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root);
        Mark filesCategory =getCategoryMark(  "IO");
        
        checkMarks(status, ids, plainIds, methodMarks, filesCategory, "IO");
    }

    public void testListenersCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "method", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        plainIds.add(3);
        
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "wait", "()V");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        plainIds.add(4);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 5 , 0, 3, 0, 0);
        ids.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6 , 0, 1, 0, 0);
        ids.add(6);
        
        builder.methodExit( 6 , 0, 1, 0, 0);
        builder.methodExit( 5 , 0, 3, 0, 0);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 7 , 1, 2, 0, 0);
        plainIds.add(7);
        
        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "method", "()V");
        builder.methodEntry( 8, 1, 1, 0, 0);
        plainIds.add(8);
        builder.methodExit( 8, 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "wait", "()V");
        builder.methodEntry( 9, 1, 1, 0, 0);
        plainIds.add(9);
        builder.methodExit( 9, 1, 1, 0, 0);

        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 10, 1, 3, 0, 0);
        ids.add(10);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Mark filesCategory =getCategoryMark( "Listeners");
        
        checkMarks(status, ids, plainIds, methodMarks, filesCategory, "Listeners");
    }
    
    public void testPaintersCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "revalidate", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        plainIds.add(3);
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "repaint", "(J)V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        ids.add(4);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 5 , 0, 1, 0, 0);
        ids.add(5);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 6 , 1, 2, 0, 0);
        plainIds.add(6);
        
        status.updateInstrMethodsInfo("painters.TestPanel", 0, 
                "updateUI", "()V");
        builder.methodEntry( 7, 1, 1, 0, 0);
        plainIds.add(7);
        builder.methodExit( 7, 1, 1, 0, 0);
        
/*        status.updateInstrMethodsInfo("painters.TestPanel", 0, 
                "paintImmediately", "(IIII)V");
        builder.methodEntry( 8, 1, 1, 0, 0);
        ids.add(8);*/

        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root);
        Mark filesCategory =getCategoryMark( "Painters");
        
        checkMarks(status, ids, plainIds, methodMarks, filesCategory, "Painters");
    }
    
    public void testUIManager(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("ui.TestUIManager", 0, 
                "method", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        plainIds.add(3);
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("ui.TestUIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        plainIds.add(4);
        builder.methodExit( 4 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("javax.swing.UIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 5 , 0, 3, 0, 0);
        ids.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6 , 0, 1, 0, 0);
        ids.add(6);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root);
        Mark filesCategory =getCategoryMark(  "UI");
        
        checkMarks(status, ids, plainIds, methodMarks, filesCategory, "UI");
    }
    
    public void testMixedCategories(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        builder.startup( client );
        
        Set<Integer> filesIds = new HashSet<Integer>();
        Set<Integer> java2DIds = new HashSet<Integer>();
        Set<Integer> plainIds = new HashSet<Integer>();
        Set<Integer> socketIds = new HashSet<Integer>();
        Set<Integer> ioIds = new HashSet<Integer>();
        Set<Integer> listenersIds = new HashSet<Integer>();
        Set<Integer> paintersIds = new HashSet<Integer>();
        Set<Integer> uiIds = new HashSet<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        plainIds.add(1);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("sun.java2d.loops.DrawPolygons", 0, 
                "<init>", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        java2DIds.add(3);
        
        status.updateInstrMethodsInfo("java.io.FileReader", 0, 
                "reset", "()V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        filesIds.add( 4 );
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        filesIds.add(5);
        
        builder.methodExit( 5 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "read", "([Ljava/nio/ByteBuffer;)J");
        builder.methodEntry( 6 , 0, 3, 0, 0);
        socketIds.add( 6 );
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 7 , 0, 1, 0, 0);
        socketIds.add(7);
        
        builder.methodExit( 7 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("java.io.OutputStreamWriter", 0, 
                "write", "([CII)V");
        builder.methodEntry( 8 , 0, 3, 0, 0);
        ioIds.add(8);
        
        builder.methodExit( 8 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 9 , 0, 3, 0, 0);
        listenersIds.add(9);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "repaint", "(J)V");
        builder.methodEntry( 10 , 0, 3, 0, 0);
        paintersIds.add(10);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
                "method", "()V");
        builder.methodEntry( 11 , 0, 1, 0, 0);
        paintersIds.add(11);
        
        builder.methodExit( 11 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("javax.swing.UIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 12 , 0, 3, 0, 0);
        uiIds.add(12);
        
        builder.methodExit( 12 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
                "method", "()V");
        builder.methodEntry( 13 , 0, 1, 0, 0);
        paintersIds.add(13);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Mark filesCategory =getCategoryMark(  "Files");
        Mark java2DCategory = getJava2DMark();
        Mark socketCategory = getCategoryMark(  "Socket");
        Mark ioCategory = getCategoryMark( "IO");
        Mark listenersCategory = getCategoryMark( "Listeners");
        Mark paintersCategory = getCategoryMark(  "Painters");
        Mark uiCategory = getCategoryMark(  "UI");
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if ( java2DIds.contains( id)){
                if (!mark.equals(java2DCategory)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Java 2D category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), false);
                }
                java2DIds.remove(id);
            }
            else  if ( filesIds.contains( id)){
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
                filesIds.remove( id );
            }
            else  if ( socketIds.contains( id)){
                if (!mark.equals(socketCategory)) {
                    assertTrue("Method '"
                            + status.getInstrMethodClasses()[id]
                            + "."
                            + status.getInstrMethodNames()[id]
                            + "' should be included in "
                            + "Socket category, but its category is "
                            + getCategorization().getCategoryForMark(mark)
                                    .getLabel(), false);
                    }
                socketIds.remove( id );
            }
            else  if ( ioIds.contains( id)){
                if (!mark.equals(ioCategory)) {
                    assertTrue("Method '"
                            + status.getInstrMethodClasses()[id]
                            + "."
                            + status.getInstrMethodNames()[id]
                            + "' should be included in "
                            + "IO category, but its category is "
                            + getCategorization().getCategoryForMark(mark)
                                    .getLabel(), false);
                    }
                ioIds.remove( id );
            } 
            else  if ( listenersIds.contains( id)){
                if (!mark.equals(listenersCategory)) {
                    assertTrue("Method '"
                            + status.getInstrMethodClasses()[id]
                            + "."
                            + status.getInstrMethodNames()[id]
                            + "' should be included in "
                            + "Listeners category, but its category is "
                            + getCategorization().getCategoryForMark(mark)
                                    .getLabel(), false);
                    }
                listenersIds.remove( id );
            }  
            else  if ( paintersIds.contains( id)){
                if (!mark.equals(paintersCategory)) {
                    assertTrue("Method '"
                            + status.getInstrMethodClasses()[id]
                            + "."
                            + status.getInstrMethodNames()[id]
                            + "' should be included in "
                            + "Painters category, but its category is "
                            + getCategorization().getCategoryForMark(mark)
                                    .getLabel(), false);
                    }
                paintersIds.remove( id );
            } 
            else  if ( uiIds.contains( id)){
                if (!mark.equals(uiCategory)) {
                    assertTrue("Method '"
                            + status.getInstrMethodClasses()[id]
                            + "."
                            + status.getInstrMethodNames()[id]
                            + "' should be included in "
                            + "UI category, but its category is "
                            + getCategorization().getCategoryForMark(mark)
                                    .getLabel(), false);
                    }
                uiIds.remove( id );
            } 
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
            }
        }
        if ( !filesIds.isEmpty()){
            int id = filesIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
        if ( !java2DIds.isEmpty()){
            int id = java2DIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
    @Override
    protected String getProjectName() {
        return JAVA_APP_NAME;
    }
    
    private void checkMarks( ProfilingSessionStatus status, Set<Integer> ids,
            Set<Integer> plainIds, Map<Integer, Mark> methodMarks,
            Mark requestedMark, String categoryName )
    {
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(requestedMark)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + categoryName +" category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), false);
            }
            ids.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
            }
        }
        if ( !ids.isEmpty()){
            int id = ids.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
    private Map<Integer, Mark> getMethodMarks( SimpleCPUCCTNode root ){
        Map<Integer, Mark> result = new HashMap<Integer, Mark>();
        Stack<Mark> stack = new Stack<Mark>();
        collectMethodMarks(root, result, stack);
        return result;
    }
    
    private void collectMethodMarks( RuntimeCPUCCTNode node , Map<Integer, Mark> map,
            Stack<Mark> stack )
    {
        if ( node instanceof MarkedCPUCCTNode ){
            Mark mark = ((MarkedCPUCCTNode)node).getMark();
            stack.push(mark);
        }
        else if ( node instanceof MethodCPUCCTNode ){
            int methodId = ((MethodCPUCCTNode)node).getMethodId();
            if ( !stack.isEmpty()) {
                Mark mark = stack.peek();
                map.put(methodId, mark);
            }
        }
        Children children = node.getChildren();
        for ( int i = 0 ; i <children.size(); i++){
            RuntimeCPUCCTNode child = children.getChildAt(i);
                collectMethodMarks(child, map, stack);
        }
        if ( node instanceof MarkedCPUCCTNode && !stack.isEmpty() ){
            stack.pop();
        }
    }
                                      
    
    private Mark getJava2DMark( ){
        return getCategoryMark("Java 2D");
    }
    
    private Mark getCategoryMark(String label){
        return findCategory(getCategorization().getRoot(), label);
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
