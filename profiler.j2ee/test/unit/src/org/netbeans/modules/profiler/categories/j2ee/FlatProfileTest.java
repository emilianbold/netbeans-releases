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
package org.netbeans.modules.profiler.categories.j2ee;

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
    
    public FlatProfileTest( String name ) {
        super(name);
    }
    
    public void testHibernate(){
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
        
        List<Integer> markedIds = new LinkedList<Integer>() ;
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("org.hibernate.impl.SessionImpl", 0, 
                "afterScrollOperation", "()V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("org.hibernate.impl.FilterImpl", 0, 
                "validate", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        markedIds.add( 3 );
        builder.methodExit( 3 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        markedIds.add(4);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "<init>", "()V");
        
        builder.methodEntry( 5 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("org.hibernate.impl.CriteriaImpl", 0, 
                "after", "()V");
        builder.methodEntry( 6 , 1, 3, 0, 0);
        markedIds.add( 6 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        Category persistence = getCategory("Persistence");
        drillDown.drilldown( persistence.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Persistence category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Persistence", markedIds);
        
        drillDown.drilldown( getCategory("Hibernate").getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Hibernate category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Hibernate", markedIds);
    }
    
    public void testConnection(){
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
        
        List<Integer> markedIds = new LinkedList<Integer>() ;
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("connection.TestDataSource", 0, 
                "setLogWriter", "(Ljava/io/PrintWriter;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        builder.methodExit(2, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("connection.TestDataSource", 0, 
                "setLoginTimeout", "(I)V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        markedIds.add( 3 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        markedIds.add(4);
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "<init>", "()V");
        
        builder.methodEntry( 5 , 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("connection.TestDataSource", 0, 
                "getConnection", "(Ljava/lang/String;Ljava/lang/String;)Ljava/sql/Connection;");
        builder.methodEntry( 6 , 1, 3, 0, 0);
        markedIds.add( 6 );
        
        builder.methodExit(6,1 , 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 7 , 1, 1, 0, 0);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        Category persistence = getCategory("Persistence");
        drillDown.drilldown( persistence.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Persistence category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Persistence", markedIds);
        
        Category jdbc = getCategory("JDBC");
        drillDown.drilldown( jdbc.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in JDBC category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "JDBC ", markedIds);
        
        Category connection = getCategory("Connection");
        drillDown.drilldown( connection.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Connection category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Connection ", markedIds);
    }
    
    public void testStatements(){
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
        
        List<Integer> markedIds = new LinkedList<Integer>() ;
        
        builder.newThread( 0 , "main", "java.lang.Thread");
        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
        builder.methodEntry( 1 , 0, 2, 0, 0);
        
        status.updateInstrMethodsInfo("statements.TestStatement", 0, 
                "getMoreResults", "()Z");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add( 3 );
        builder.methodExit(3, 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("statements.TestStatement", 0, 
                "isWrapperFor", "(Ljava/lang/Class;)Z");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add( 4 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("statements.TestStatement", 0, 
                "setCursorName", "(Ljava/lang/String;)V");
        
        builder.methodEntry( 5 , 1, 2, 0, 0);
        markedIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 6 , 0, 1, 0, 0);
        markedIds.add( 6 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        Category persistence = getCategory("Persistence");
        drillDown.drilldown( persistence.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Persistence category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Persistence", markedIds);
        
        Category jdbc = getCategory("JDBC");
        drillDown.drilldown( jdbc.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in JDBC category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "JDBC ", markedIds);
        Category statements = getCategory("Statements");
        drillDown.drilldown( statements.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Statements category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Statements ", markedIds);
    }
    
    public void testJpa(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks        
//        resetMarkMappings();
//        
//        TestGraphBuilder builder = new TestGraphBuilder();
//        ProfilerEngineSettings settings = new ProfilerEngineSettings();
//        ProfilingSessionStatus status = new ProfilingSessionStatus();
//        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
//        
//        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);
//
//        if (filter != null) {
//            filter.reset(); 
//        }
//
//        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
//                FlatProfileBuilder.class);
//        flatProfileBuilder.setContext(client, null, filter);
//        
//        flatProfileBuilder.cctReset();
//        
//        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
//        
//        filter.setEvaluators(Collections.singleton( factory ));
//        
//        builder.startup( client );
//        
//        List<Integer> markedIds = new LinkedList<Integer>() ;
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
//                "setParameter", "(ILjava/lang/Object;)Ljavax/persistence/Query;");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        markedIds.add( 2 );
//        builder.methodExit(2, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        builder.methodExit(3, 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 4 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
//                "executeUpdate", "()I");
//        builder.methodEntry( 5 , 0, 3, 0, 0);
//        markedIds.add( 5 );
//        
//        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 6 , 1, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
//                "setParameter", "(ILjava/util/Date;Ljavax/persistence/TemporalType;)Ljavax/persistence/Query;");
//       
//        builder.methodEntry( 7 , 1, 3, 0, 0);
//        markedIds.add(7);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 8 , 1, 1, 0, 0);
//        markedIds.add( 8 );
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        Category persistence = getCategory("Persistence");
//        drillDown.drilldown( persistence.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(markedIds.size()+" methods expected in Persistence category",
//                markedIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Persistence", markedIds);
//        
//        Category jpa = getCategory("JPA");
//        drillDown.drilldown( jpa.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(markedIds.size()+" methods expected in Hibernate category",
//                markedIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "JPA", markedIds);        
    }
    
    public void testFilters(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        resetMarkMappings();
//        
//        TestGraphBuilder builder = new TestGraphBuilder();
//        ProfilerEngineSettings settings = new ProfilerEngineSettings();
//        ProfilingSessionStatus status = new ProfilingSessionStatus();
//        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
//        
//        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);
//
//        if (filter != null) {
//            filter.reset(); 
//        }
//
//        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
//                FlatProfileBuilder.class);
//        flatProfileBuilder.setContext(client, null, filter);
//        
//        flatProfileBuilder.cctReset();
//        
//        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
//        
//        filter.setEvaluators(Collections.singleton( factory ));
//        
//        builder.startup( client );
//        
//        List<Integer> lifecycleIds = new LinkedList<Integer>() ;
//        List<Integer> filtersIds = new LinkedList<Integer>() ;
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "init", "(Ljavax/servlet/FilterConfig;)V");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        lifecycleIds.add( 2 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        lifecycleIds.add( 3 );
//        builder.methodExit(3, 0, 1, 0, 0);
//        builder.methodExit(2, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "destroy", "()V");
//        builder.methodEntry( 4 , 0, 3, 0, 0);
//        lifecycleIds.add( 4 );
//        
//        builder.methodExit(4, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 5 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestChain", 0, 
//                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V");
//        builder.methodEntry( 6 , 0, 3, 0, 0);
//        filtersIds.add(6);
//        
//        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 7 , 1, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V");
//        builder.methodEntry( 8 , 1, 3, 0, 0);
//        filtersIds.add(8);
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        Category webContainer = getCategory("Web Container");
//        drillDown.drilldown( webContainer.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        int containerSize = lifecycleIds.size() + filtersIds.size();
//        assertEquals(containerSize+" methods expected in Web Container category",
//                containerSize ,  flatProfile.getNRows());
//        
//        List<Integer> all = new ArrayList<Integer>( containerSize );
//        all.addAll( lifecycleIds);
//        all.addAll( filtersIds );
//        checkCategory(status, flatProfile, "Web Container", all);
//        
//        Category filters = getCategory("Filters");
//        
//        drillDown.drilldown( filters.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(containerSize+" methods expected in Filters category",
//                containerSize ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Filters", all); 
//        
//        Category lifecycle = findCategory(filters, "Life Cycle");
//        drillDown.drilldown( lifecycle.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(lifecycleIds.size()+" methods expected in Filters/Life Cycle category",
//                lifecycleIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Filters/Life Cycle", lifecycleIds); 
    }
    
    public void testJstl(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        resetMarkMappings();
//        
//        TestGraphBuilder builder = new TestGraphBuilder();
//        ProfilerEngineSettings settings = new ProfilerEngineSettings();
//        ProfilingSessionStatus status = new ProfilingSessionStatus();
//        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
//        
//        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);
//
//        if (filter != null) {
//            filter.reset(); 
//        }
//
//        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
//                FlatProfileBuilder.class);
//        flatProfileBuilder.setContext(client, null, filter);
//        
//        flatProfileBuilder.cctReset();
//        
//        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
//        
//        filter.setEvaluators(Collections.singleton( factory ));
//        
//        builder.startup( client );
//        
//        List<Integer> markedIds = new LinkedList<Integer>() ;
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("jstl.TestTagSupport", 0, 
//                "setParent", "(Ljavax/servlet/jsp/tagext/JspTag;)V");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        markedIds.add( 2 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        markedIds.add( 3 );
//        
//        builder.methodExit(3, 0, 1, 0, 0);
//        builder.methodExit(2, 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 4 , 0, 1, 0, 0);
//        
//        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 5 , 1, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("jstl.TestTagSupport", 0, 
//                "getJspBody", "()Ljavax/servlet/jsp/tagext/JspFragment;");
//        builder.methodEntry( 6 , 1, 3, 0, 0);
//        markedIds.add( 6 );
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        Category webContainer = getCategory("Web Container");
//        drillDown.drilldown( webContainer.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(markedIds.size()+" methods expected in Web Container category",
//                markedIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Web Container", markedIds);
//        Category jstl = getCategory("JSTL");
//        drillDown.drilldown( jstl.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(markedIds.size()+" methods expected in JSTL category",
//                markedIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "JSTL", markedIds);
    }
    
    public void testListeners(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        resetMarkMappings();
//        
//        TestGraphBuilder builder = new TestGraphBuilder();
//        ProfilerEngineSettings settings = new ProfilerEngineSettings();
//        ProfilingSessionStatus status = new ProfilingSessionStatus();
//        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
//        
//        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);
//
//        if (filter != null) {
//            filter.reset(); 
//        }
//
//        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
//                FlatProfileBuilder.class);
//        flatProfileBuilder.setContext(client, null, filter);
//        
//        flatProfileBuilder.cctReset();
//        
//        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
//        
//        filter.setEvaluators(Collections.singleton( factory ));
//        
//        builder.startup( client );
//        
//        List<Integer> markedIds = new LinkedList<Integer>() ;
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("listeners.TestHttpSessionListener", 0, 
//                "sessionCreated", "(Ljavax/servlet/http/HttpSessionEvent;)V");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        markedIds.add( 2 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        markedIds.add( 3 );
//        
//        builder.methodExit(3, 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 4 , 0, 1, 0, 0);
//        markedIds.add( 4 );
//        
//        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
//        
//        
//        status.updateInstrMethodsInfo("listeners.TestHttpSessionListener", 0, 
//                "sessionDestroyed", "(Ljavax/servlet/http/HttpSessionEvent;)V");
//        builder.methodEntry( 5 , 1, 2, 0, 0);
//        markedIds.add( 5 );
//        
//        builder.methodExit(5, 1, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 6 , 1, 1, 0, 0);
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        Category webContainer = getCategory("Web Container");
//        drillDown.drilldown( webContainer.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(markedIds.size()+" methods expected in Web Container category",
//                markedIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Web Container", markedIds);
//        Category listeners = getCategory("Listeners");
//        drillDown.drilldown( listeners.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(markedIds.size()+" methods expected in Listeners category",
//                markedIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Listeners", markedIds);        
    }
    
    public void testServlets(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        resetMarkMappings();
//        
//        TestGraphBuilder builder = new TestGraphBuilder();
//        ProfilerEngineSettings settings = new ProfilerEngineSettings();
//        ProfilingSessionStatus status = new ProfilingSessionStatus();
//        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
//        
//        CCTResultsFilter filter = Lookup.getDefault().lookup(CCTResultsFilter.class);
//
//        if (filter != null) {
//            filter.reset(); 
//        }
//
//        FlatProfileBuilder flatProfileBuilder = Lookup.getDefault().lookup(
//                FlatProfileBuilder.class);
//        flatProfileBuilder.setContext(client, null, filter);
//        
//        flatProfileBuilder.cctReset();
//        
//        CCTResultsFilter.EvaluatorProvider factory = new TestDrillDownFactory(client);
//        
//        filter.setEvaluators(Collections.singleton( factory ));
//        
//        builder.startup( client );
//        
//        List<Integer> servletsIds = new LinkedList<Integer>() ;
//        List<Integer> lifecycleIds = new LinkedList<Integer>() ;
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "init", "()V");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        lifecycleIds.add( 2 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        lifecycleIds.add( 3 );
//        builder.methodExit(3, 0, 1, 0, 0);
//        builder.methodExit(2, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "destroy", "()V");
//        builder.methodEntry( 4 , 0, 3, 0, 0);
//        lifecycleIds.add( 4 );
//        
//        builder.methodExit(4, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 5 , 0, 1, 0, 0);
//        
//        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 6 , 1, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "doHead", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//        builder.methodEntry( 7 , 0, 3, 0, 0);
//        servletsIds.add(7);
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "service", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V");
//        builder.methodEntry( 8 , 1, 3, 0, 0);
//        servletsIds.add(8);
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        Category webContainer = getCategory("Web Container");
//        drillDown.drilldown( webContainer.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        int size = lifecycleIds.size() +servletsIds.size();
//        List<Integer> all = new ArrayList<Integer>( size );
//        all.addAll( lifecycleIds);
//        all.addAll( servletsIds );
//        assertEquals(size+" methods expected in Web Container category",
//                size ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Web Container", all);
//        
//        Category servlets = getCategory("Servlets");
//        drillDown.drilldown( servlets.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(size+" methods expected in Servlets category",
//                size ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Servlets", all);
//        Category lifecycle = findCategory(servlets, "Life Cycle");
//        drillDown.drilldown( lifecycle.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//
//        assertEquals(lifecycleIds.size()+" methods expected in Servlets/Life Cycle category",
//                lifecycleIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Servlets/Life Cycle", lifecycleIds);
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
    
    
    @Override
    protected String getProjectName() {
        return TestBase.APP_NAME;
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
