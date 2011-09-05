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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.results.cpu.FlatProfileBuilder;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainer;
import org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;
import org.netbeans.modules.profiler.categorization.api.Category;
import org.netbeans.modules.profiler.drilldown.DrillDown;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
public class FlatProfileTest extends TestBase {
    
    private static final String JAVA_APP_NAME = "JavaApp";

    public FlatProfileTest( String name ) {
        super(name);
    }
    
    public void testJava2D(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> java2DIds = new LinkedList<Integer>() ;
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        builder.newThread( 1 , "Java2D Disposer", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphics2D", 0, 
                "setClip", "Ljava/awt/Shape;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        java2DIds.add( 2);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        java2DIds.add(3);
        
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        builder.methodExit( 2 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.pipe.hw.ExtendedBufferCapabilities", 0, 
                "<init>", "(Ljava/awt/ImageCapabilities;Ljava/awt/ImageCapabilities;Ljava/awt/BufferCapabilities$FlipContents;)V");
        
        builder.methodEntry( 4 , 0, 3, 0, 0);
        java2DIds.add(4);
        
        builder.methodExit( 4 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.d3d.D3DRTTSurfaceToSurfaceScale", 0, 
                "<init>", "()V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        java2DIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "<init>", "()V");
        
        builder.methodEntry( 6 , 1, 1, 0, 0);
        java2DIds.add(6);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "operation", "(Ljava/lang/String;II)V");
        
        builder.methodEntry( 7 , 1, 1, 0, 0);
        java2DIds.add(7);
        
        builder.methodExit( 7 , 1, 1, 0, 0);
        builder.methodExit( 6 , 1, 1, 0, 0);
        builder.methodExit( 5 , 1, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 8 , 1, 1, 0, 0);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("UI"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(java2DIds.size()+" methods expected in UI category",
                java2DIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "UI", java2DIds);
        
        drillDown.drilldown( getCategoryId("Java 2D"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(java2DIds.size()+" methods expected in Java 2D category",
                java2DIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Java 2D", java2DIds);

    }

    
    public void testFilesCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> filesIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "<init>", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("java.io.FileInputStream", 0, 
                "read", "()I");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        filesIds.add( 3 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        filesIds.add(4);
        
        builder.methodExit( 4 , 0, 1, 0, 0);
        builder.methodExit( 3 , 0, 3, 0, 0);
        builder.methodExit( 2 , 0, 1, 0, 0);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("files.TestFileReader", 1, 
                "skip", "(J)J");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        filesIds.add(5);
        
        builder.methodExit( 5 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
       
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("IO"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(filesIds.size()+" method expected in IO category",filesIds.size() ,  
                flatProfile.getNRows());
        checkCategory(status, flatProfile, "IO", filesIds);
        
        drillDown.drilldown( getCategoryId("Files"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(filesIds.size()+" method expected in Files category",filesIds.size() ,  
                flatProfile.getNRows());
        checkCategory(status, flatProfile, "Files", filesIds);
    }
    
    public void testMixedJ2DCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> filesIds = new LinkedList<Integer>();
        List<Integer> java2DIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.loops.DrawPolygons", 0, 
                "<init>", "()V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        java2DIds.add(2);
        
        status.updateInstrMethodsInfo("java.io.FileInputStream", 0, 
                "read", "()I");
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
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("IO"));
        drillDown.drilldown( getCategoryId("Files"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(filesIds.size()+" method expected in Files category",
                filesIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Files", filesIds);
        
        drillDown.drillup();
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("UI"));
        drillDown.drilldown( getCategoryId("Java2D"));
        
        flatProfile = flatProfileBuilder.createFlatProfile();
        assertEquals(java2DIds.size()+" method expected in Java 2D category",
                java2DIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Java2D", java2DIds);
    }
    
    public void testSocketCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> socketIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "read", "(Ljava/nio/ByteBuffer;)I");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        socketIds.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        socketIds.add(3);
        
        builder.methodExit( 3 , 0, 1, 0, 0);
        builder.methodExit( 2 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
                "method", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
        
        builder.methodExit( 6, 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "write", "(Ljava/nio/ByteBuffer;)I");
        builder.methodEntry( 7, 1, 3, 0, 0);
        socketIds.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("IO"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(socketIds.size()+" method expected in IO category",
                socketIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "IO", socketIds);
        
        drillDown.drilldown( getCategoryId("Socket"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(socketIds.size()+" method expected in Socket category",
                socketIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Socket", socketIds);
    }
    
    public void testIOCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> ioIds = new LinkedList<Integer>();
       
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("java.io.OutputStreamWriter", 0, 
                "write", "([CII)V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        ioIds.add(3);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        ioIds.add(4);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("streams.TestInputStream", 0, 
                "write", "()V");
        builder.methodEntry( 6, 1, 1, 0, 0);
        builder.methodExit( 6, 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("streams.TestInputStream", 0, 
                "skip", "(J)J");
        builder.methodEntry( 7, 1, 1, 0, 0);
        // method skip() is in fact defined in BufferedInputStream
//        ioIds.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("IO"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(ioIds.size()+" method expected in IO category",
                ioIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "IO", ioIds);
        
        drillDown.drilldown( getCategoryId("Files"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals("No  methods expected in Files category",
                0 ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Files", Collections.<Integer>emptyList());
    }
    
    public void testListenersCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> listenerIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "method", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "wait", "()V");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 5 , 0, 3, 0, 0);
        listenerIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6 , 0, 1, 0, 0);
        listenerIds.add(6);
        
        builder.methodExit( 6 , 0, 1, 0, 0);
        builder.methodExit( 5 , 0, 3, 0, 0);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 7 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "method", "()V");
        builder.methodEntry( 8, 1, 1, 0, 0);
        builder.methodExit( 8, 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "wait", "()V");
        builder.methodEntry( 9, 1, 1, 0, 0);
        builder.methodExit( 9, 1, 1, 0, 0);

        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 10, 1, 3, 0, 0);
        listenerIds.add(10);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
     
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("UI"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(listenerIds.size()+" method expected in UI category",
                listenerIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "UI", listenerIds);
        
        drillDown.drilldown( getCategoryId("Listeners"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(listenerIds.size()+" method expected in Listeners category",
                listenerIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Listeners", listenerIds);
    }
    
    public void testPaintersCategory(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> paintersIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "revalidate", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "repaint", "(J)V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        paintersIds.add(4);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 5 , 0, 1, 0, 0);
        paintersIds.add(5);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass2", 1, 
                "method", "()V");
        builder.methodEntry( 6 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("painters.TestPanel", 0, 
                "updateUI", "()V");
        builder.methodEntry( 7, 1, 1, 0, 0);
        builder.methodExit( 7, 1, 1, 0, 0);
        
/*        status.updateInstrMethodsInfo("painters.TestPanel", 0, 
                "paintImmediately", "(IIII)V");
        builder.methodEntry( 8, 1, 1, 0, 0);
        paintersIds.add(8);*/

        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("UI"));
        drillDown.drilldown( getCategoryId("Painters"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(paintersIds.size()+" method expected in Painters category",
                paintersIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Painters", paintersIds);
    }
    
    public void testUIManager(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> uiIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("ui.TestUIManager", 0, 
                "method", "()V");
        builder.methodEntry( 3 , 0, 1, 0, 0);
        builder.methodExit( 3 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("ui.TestUIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 4 , 0, 1, 0, 0);
        builder.methodExit( 4 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("javax.swing.UIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 5 , 0, 3, 0, 0);
        uiIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        builder.methodEntry( 6 , 0, 1, 0, 0);
        uiIds.add(6);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("UI"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(uiIds.size()+" method expected in UI category",
                uiIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "UI", uiIds);
        
        drillDown.drilldown( getCategoryId("AWT/Swing"));
        drillDown.drilldown( getCategoryId("Listeners"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals("No methods expected in Listeners category",
                0 ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Listeners", Collections.<Integer>emptyList());
    }
    
    public void testMixedCategories(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> filesIds = new LinkedList<Integer>();
        List<Integer> java2DIds = new LinkedList<Integer>();
        List<Integer> socketIds = new LinkedList<Integer>();
        List<Integer> ioIds = new LinkedList<Integer>();
        List<Integer> listenersIds = new LinkedList<Integer>();
        List<Integer> paintersIds = new LinkedList<Integer>();
        List<Integer> uiIds = new LinkedList<Integer>();
        List<Integer> swingIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.loops.DrawPolygons", 0, 
                "<init>", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        java2DIds.add(3);
        uiIds.add(3);
        
        status.updateInstrMethodsInfo("java.io.FileInputStream", 0, 
                "skip", "(J)J");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        filesIds.add( 4 );
        ioIds.add(4);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        filesIds.add(5);
        ioIds.add(5);
        
        builder.methodExit( 5 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "read", "(Ljava/nio/ByteBuffer;)I");
        builder.methodEntry( 6 , 0, 3, 0, 0);
        socketIds.add( 6 );
        ioIds.add(6);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 7 , 0, 1, 0, 0);
        socketIds.add(7);
        ioIds.add(7);
        
        builder.methodExit( 7 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("java.io.FileOutputStream", 0, 
                "write", "([BII)V");
        builder.methodEntry( 8 , 0, 3, 0, 0);
        filesIds.add(8);
        ioIds.add(8);
        
        builder.methodExit( 8 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 9 , 0, 3, 0, 0);
        listenersIds.add(9);
        swingIds.add(9);
        uiIds.add(9);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "repaint", "(J)V");
        builder.methodEntry( 10 , 0, 3, 0, 0);
        paintersIds.add(10);
        swingIds.add(10);
        uiIds.add(10);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
                "method", "()V");
        builder.methodEntry( 11 , 0, 1, 0, 0);
        paintersIds.add(11);
        swingIds.add(11);
        uiIds.add(11);
        
        builder.methodExit( 11 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("javax.swing.UIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 12 , 0, 3, 0, 0);
        swingIds.add(12);
        uiIds.add(12);
        
        builder.methodExit( 12 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
                "method", "()V");
        builder.methodEntry( 13 , 0, 1, 0, 0);
        paintersIds.add(13);
        swingIds.add(13);
        uiIds.add(13);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("UI"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(uiIds.size()+" method expected in UI category",
                uiIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "UI", uiIds);

        drillDown.drilldown( getCategoryId("AWT/Swing"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(swingIds.size()+" method expected in AWT/Swing category",
                swingIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "AWT/Swing", swingIds);
        
        drillDown.drilldown( getCategoryId("Listeners"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(listenersIds.size()+" method expected in Listeners category",
                listenersIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Listeners", listenersIds);
        
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("Painters"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(paintersIds.size()+" method expected in Painters category",
                paintersIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Painters", paintersIds);
        
        drillDown.drillup();
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("Java 2D"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(java2DIds.size()+" method expected in Java 2D category",
                java2DIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Java 2D", java2DIds);
        
        drillDown.drillup();
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("IO"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(ioIds.size()+" method expected in IO category",
                ioIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "IO", ioIds);
        
        drillDown.drilldown( getCategoryId("Files"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(filesIds.size()+" method expected in Files category",
                filesIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Files", filesIds);
        
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("Socket"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(socketIds.size()+" method expected in Socket category",
                socketIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Socket", socketIds);
        
    }
    
    public void testThreadedMixedCategories(){
        resetMarkMappings();
        
        TestGraphBuilder builder = new TestGraphBuilder();
        ProfilerEngineSettings settings = new ProfilerEngineSettings();
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
        
        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);

        if (filter != null) {
            filter.reset(); 
        }

        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
                FlatProfileBuilder.class);
        flatProfileBuilder.setContext(client, null, filter);
        
        flatProfileBuilder.cctReset();
        
        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
        
        filter.setEvaluators(Collections.singleton( factory ));
        
        builder.startup( client );
        
        List<Integer> filesIds = new LinkedList<Integer>();
        List<Integer> java2DIds = new LinkedList<Integer>();
        List<Integer> socketIds = new LinkedList<Integer>();
        List<Integer> ioIds = new LinkedList<Integer>();
        List<Integer> listenersIds = new LinkedList<Integer>();
        List<Integer> paintersIds = new LinkedList<Integer>();
        List<Integer> swingIds = new LinkedList<Integer>();
        List<Integer> uiIds = new LinkedList<Integer>();
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 2 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("sun.java2d.loops.DrawPolygons", 0, 
                "<init>", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        java2DIds.add(3);
        uiIds.add(3);
        
        status.updateInstrMethodsInfo("java.io.FileInputStream", 0, 
                "skip", "(J)J");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        filesIds.add( 4 );
        ioIds.add(4);
        builder.methodExit(4, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        java2DIds.add(5);
        uiIds.add(5);
        
        builder.methodExit( 5 , 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("socket.TestSocketChanel", 0, 
                "read", "(Ljava/nio/ByteBuffer;)I");
        builder.methodEntry( 6 , 0, 3, 0, 0);
        socketIds.add( 6 );
        ioIds.add(6);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 7 , 0, 1, 0, 0);
        socketIds.add(7);
        ioIds.add(7);
        
        builder.methodExit( 7 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("java.io.FileOutputStream", 0, 
                "write", "([BII)V");
        builder.methodEntry( 8 , 0, 3, 0, 0);
        filesIds.add(8);
        ioIds.add(8);
        
        builder.methodExit( 8 , 0, 3, 0, 0);
        
        builder.newThread( 1 , "AWT-EventQueue-0", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("listeners.TestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 9 , 1, 2, 0, 0);
        listenersIds.add(9);
        swingIds.add(9);
        uiIds.add(9);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "repaint", "(J)V");
        builder.methodEntry( 10 , 1, 3, 0, 0);
        paintersIds.add(10);
        swingIds.add(10);
        uiIds.add(10);
        builder.methodExit(10, 1, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
                "method", "()V");
        builder.methodEntry( 11 , 1, 1, 0, 0);
        swingIds.add(11);
        uiIds.add(11);
        listenersIds.add(11);
        
        builder.methodExit( 11 , 1, 1, 0, 0);
        
        status.updateInstrMethodsInfo("javax.swing.UIManager", 0, 
                "getColor", "(Ljava/lang/Object;)Ljava/awt/Color;");
        builder.methodEntry( 12 , 1, 3, 0, 0);
        swingIds.add(12);
        uiIds.add(12);
        
        builder.methodExit( 12 , 1, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
                "method", "()V");
        builder.methodEntry( 13 , 1, 1, 0, 0);
        listenersIds.add(13);
        swingIds.add(13);
        uiIds.add(13);
        
        builder.newThread( 2 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("streams.TestInputStream", 0, 
                "read", "()I");
        builder.methodEntry( 14, 2, 2, 0, 0);
        ioIds.add(14);
        
        status.updateInstrMethodsInfo("pack.CustomClass5", 0, 
                "method", "()V");
        builder.methodEntry( 15 , 2, 1, 0, 0);
        ioIds.add(15 );
        builder.methodExit( 15 , 2, 1, 0, 0);
        
        status.updateInstrMethodsInfo("painters.TestComponent", 0, 
                "repaint", "(J)V");
        builder.methodEntry( 16 , 2, 3, 0, 0);
        paintersIds.add(16);
        swingIds.add(16);
        uiIds.add(16);
        builder.methodExit( 16 , 2, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
                "method", "()V");
        builder.methodEntry( 17 , 2, 1, 0, 0);
        ioIds.add(17 );
        
        status.updateInstrMethodsInfo("files.TestFileReader", 0, 
                "skip", "(J)J");
        builder.methodEntry( 18 , 2, 3, 0, 0);
        filesIds.add( 18 );
        ioIds.add(18);
        
        status.updateInstrMethodsInfo("listeners.SubTestAWTEventListener", 0, 
                "eventDispatched", "(Ljava/awt/AWTEvent;)V");
        builder.methodEntry( 19, 2, 3, 0, 0);
        swingIds.add(19);
        uiIds.add( 19);
        listenersIds.add(19);
        
        status.updateInstrMethodsInfo("sun.java2d.SunGraphics2D", 0, 
                "setClip", "Ljava/awt/Shape;)V");
        builder.methodEntry( 20 , 2, 3, 0, 0);
        java2DIds.add( 20);
        uiIds.add(20);
        
        builder.methodExit(20, 2, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass7", 0, 
                "method", "()V");
        builder.methodEntry( 21 , 2, 1, 0, 0);
        swingIds.add(21);
        uiIds.add(21 );
        listenersIds.add(21);
        
        builder.methodExit(21, 2, 1, 0, 0);
        builder.methodExit(19, 2, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass8", 0, 
                "method", "()V");
        builder.methodEntry( 22 , 2, 1, 0, 0);
        filesIds.add( 22 );
        ioIds.add(22);
        
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( getCategoryId("UI"));
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(uiIds.size()+" method expected in UI category",
                uiIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "UI", uiIds);
        
        drillDown.drilldown( getCategoryId("AWT/Swing"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(swingIds.size()+" method expected in AWT/Swing category",
                swingIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "AWT/Swing", swingIds);

        drillDown.drilldown( getCategoryId("Listeners"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(listenersIds.size()+" method expected in Listeners category",
                listenersIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Listeners", listenersIds);
        
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("Painters"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(paintersIds.size()+" method expected in Painters category",
                paintersIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Painters", paintersIds);
        
        drillDown.drillup();
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("Java 2D"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(java2DIds.size()+" method expected in Java 2D category",
                java2DIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Java 2D", java2DIds);
        
        drillDown.drillup();
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("IO"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(ioIds.size()+" method expected in IO category",
                ioIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "IO", ioIds);
        
        drillDown.drilldown( getCategoryId("Files"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(filesIds.size()+" method expected in Files category",
                filesIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Files", filesIds);
        
        drillDown.drillup();
        drillDown.drilldown( getCategoryId("Socket"));
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();
        
        assertEquals(socketIds.size()+" method expected in Socket category",
                socketIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Socket", socketIds);
        
    }
    
    
    private void checkCategory( ProfilingSessionStatus status,
            FlatProfileContainer flatProfile , String categoryName , 
            List<Integer> ids )
    {
        Set<Integer> methodIds = new HashSet<Integer>();
        for ( int i= 0; i<flatProfile.getNRows(); i++){
            int methodId = flatProfile.getMethodIdAtRow(i);
            methodIds.add( methodId );
        }
        List<Integer> copyIds = new ArrayList<Integer>(ids);
        for (Integer id : methodIds) {
            assertTrue( "Category "+categoryName+" should contain method '"+
                    status.getInstrMethodClasses()[id]+
                "."+status.getInstrMethodNames()[id]+"'",copyIds.contains(id));
            copyIds.remove(id);
        }
        if ( copyIds.size()>0){
            Integer id = copyIds.iterator().next();
            assertEquals( "Method "+status.getInstrMethodClasses()[id]+
                    "."+status.getInstrMethodNames()[id]+" is not found in category"
                    +categoryName,copyIds.size(), 0 );
        }
    }
    
    private String findCategoryId(Category parent, String categoryName ){
        Set<Category> subcategories = parent.getSubcategories();
        String label = parent.getLabel();
        if ( categoryName.equals( label)){
            return parent.getId();
        }
        for (Category category : subcategories) {
            String id = findCategoryId(category, categoryName );
            if ( id != null ){
                return id;
            }
        }
        return null;
    }
    
    @Override
    protected String getProjectName() {
        return JAVA_APP_NAME;
    }
    
    private String getCategoryId( String label ){
        return findCategoryId(getCategorization().getRoot(), label);
    }
    
    private class TestDrillDownFactory implements CCTResultsFilter.EvaluatorProvider {
        
        TestDrillDownFactory( ProfilerClient client ){
            myDrillDown = new DrillDown(getCategorization(), client);
        }

        @Override
        public Set<?> getEvaluators() {
            return Collections.singleton( myDrillDown);
        }
        
        private DrillDown myDrillDown;
    }
}
