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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
public class WebFlatProfileTest extends FlatProfileTestBase {

    public WebFlatProfileTest( String name ) {
        super(name);
    }
    
    public void testJsps(){
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
        
        status.updateInstrMethodsInfo("org.apache.jsp.index_jsp", 0,
                "_jspService", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );

        status.updateInstrMethodsInfo("pack.CustomClass", 0,
                "method", "()V");

        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add(3);
        
        builder.methodExit( 3,0,1,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0,
                "method", "()V");

        builder.methodEntry( 4 , 0, 1, 0, 0);
        markedIds.add(4);
        
        builder.methodExit( 4,0,1,0,0);
        
        builder.methodExit( 2,0,1,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0,
                "method", "()V");

        builder.methodEntry( 5 , 0, 1, 0, 0);

        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass1", 0,
                "<init>", "()V");

        builder.methodEntry( 6 , 1, 2, 0, 0);

        status.updateInstrMethodsInfo("org.apache.jsp.newjsp_jsp", 0,
                "_jspService", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add( 7 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        Category webContainer = getCategory("Web Container");
        drillDown.drilldown( webContainer.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Web Container category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "Web Container", markedIds);
        Category jsps = getCategory("JSPs");
        drillDown.drilldown( jsps.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in JSPs category",
                markedIds.size() ,  flatProfile.getNRows());
        
        checkCategory(status, flatProfile, "JSPs", markedIds);
    }
    
    public void testWebContainerWsCodec(){
        Category jaxWs = getCategory( "JAX-WS");
        Category codec = findCategory( jaxWs,  "Codec");
        
        doTestCodec( jaxWs, codec);
    }

    public void testWebContainerWsEndpointInvocation(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        Category jaxWs = getCategory( "JAX-WS");
//        Category endpointInvocation = findCategory( jaxWs, "Endpoint Invocation");
//        
//        doTestEndpointInvocation( jaxWs, endpointInvocation );
    }
    
    public void testWebContainerWsHttpTransport(){
        Category jaxWs = getCategory( "JAX-WS");
        Category httpTransport = findCategory( jaxWs, "HTTP Transport");
        
        doTestHttpTransport( jaxWs, httpTransport);
    }
    
    public void testWebContainerWsMessageProcessing(){
        Category jaxWs = getCategory( "JAX-WS");
        Category messageprocessing = findCategory( jaxWs, "Message Processing");
        
        doTestMessageProcessing( jaxWs, messageprocessing);
    }
    
    public void testWebContainerWsStreaming(){
        Category jaxWs = getCategory("JAX-WS");
        Category streaming = findCategory( jaxWs, "Streaming");
        
        doTestStreaming( jaxWs, streaming);
    }
    
    public void testMixedCategories(){
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
//        List<Integer> hibernateIds = new LinkedList<Integer>();
//        List<Integer> connectionIds = new LinkedList<Integer>();
//        List<Integer> statementsIds = new LinkedList<Integer>();
//        List<Integer> jpaIds = new LinkedList<Integer>();
//        List<Integer> filtersLifecycleIds = new LinkedList<Integer>();
//        List<Integer> filtersIds = new LinkedList<Integer>();
//        List<Integer> jstlIds = new LinkedList<Integer>();
//        List<Integer> listenersIds = new LinkedList<Integer>();
//        List<Integer> servletsLifecycleIds = new LinkedList<Integer>();
//        List<Integer> servletsIds = new LinkedList<Integer>();
//        List<Integer> jspsIds = new LinkedList<Integer>();
//        List<Integer> codecIds = new LinkedList<Integer>();
//        List<Integer> endpointInvocationIds = new LinkedList<Integer>();
//        List<Integer> httpTransportIds = new LinkedList<Integer>();
//        List<Integer> messageProcessingIds = new LinkedList<Integer>();
//        List<Integer> streamingIds = new LinkedList<Integer>();
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("connection.TestDataSource", 0, 
//                "setLogWriter", "(Ljava/io/PrintWriter;)V");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        connectionIds.add( 2 );
//        builder.methodExit( 2 , 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("org.hibernate.impl.SessionImpl", 0, 
//                "afterScrollOperation", "()V");
//        builder.methodEntry( 4 , 0, 3, 0, 0);
//        hibernateIds.add( 4 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 5 , 0, 1, 0, 0);
//        hibernateIds.add(5);
//        
//        status.updateInstrMethodsInfo("statements.TestStatement", 0, 
//                "getMoreResults", "()Z");
//        builder.methodEntry( 6 , 0, 3, 0, 0);
//        statementsIds.add( 6 );
//        
//        builder.methodExit( 6 , 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 7 , 0, 1, 0, 0);
//        hibernateIds.add(7);
//        
//        builder.methodExit( 7 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 8 , 0, 1, 0, 0);
//        hibernateIds.add(8);
//        
//        builder.methodExit( 8 , 0, 1, 0, 0);
//        builder.methodExit( 5 , 0, 1, 0, 0);
//        builder.methodExit( 4 , 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
//                "setParameter", "(ILjava/lang/Object;)Ljavax/persistence/Query;");
//        builder.methodEntry( 9 , 0, 3, 0, 0);
//        jpaIds.add( 9 );
//        builder.methodExit(9, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "init", "(Ljavax/servlet/FilterConfig;)V");
//        builder.methodEntry( 10 , 0, 3, 0, 0);
//        filtersLifecycleIds.add( 10 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 11 , 0, 1, 0, 0);
//        filtersLifecycleIds.add(11);
//        
//        builder.methodExit(11, 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V");
//        builder.methodEntry( 12 , 0, 3, 0, 0);
//        filtersIds.add( 12 );
//        
//        builder.methodExit(12, 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 13 , 0, 1, 0, 0);
//        filtersLifecycleIds.add(13);
//        
//        builder.methodExit(13, 0, 1, 0, 0);
//        builder.methodExit(10, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("jstl.TestTagSupport", 0, 
//                "setParent", "(Ljavax/servlet/jsp/tagext/JspTag;)V");
//        builder.methodEntry( 14 , 0, 3, 0, 0);
//        jstlIds.add( 14 );
//        builder.methodExit(14, 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass5", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 15 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("listeners.TestHttpSessionListener", 0, 
//                "sessionCreated", "(Ljavax/servlet/http/HttpSessionEvent;)V");
//        builder.methodEntry( 16 , 0, 3, 0, 0);
//        listenersIds.add( 16 );
//        
//        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "(Ljavax/servlet/FilterConfig;)V");
//        
//        builder.methodEntry( 17 , 1, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "init", "()V");
//        builder.methodEntry( 18 , 1, 3, 0, 0);
//        servletsLifecycleIds.add( 18 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "(Ljava/io/PrintWriter;)V");
//        builder.methodEntry( 19 , 1, 1, 0, 0);
//        servletsLifecycleIds.add( 19 );
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "doHead", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//        builder.methodEntry( 20 , 1, 3, 0, 0);
//        servletsIds.add( 20 );
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.encoding.MtomCodec.MtomStreamWriter", 0,
//                "flush", "()V");
//        builder.methodEntry( 21 , 1, 3, 0, 0);
//        codecIds.add( 21 );
//        
//        status.updateInstrMethodsInfo("org.apache.jsp.index_jsp", 0,
//                "_jspService", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//        builder.methodEntry( 22 , 1, 3, 0, 0);
//        jspsIds.add( 22 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 23 , 1, 1, 0, 0);
//        jspsIds.add(23);
//        
//        builder.methodExit(23, 1, 1, 0, 0);
//        builder.methodExit(22, 1, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl", 0,
//                "process", "(Lcom/sun/xml/ws/api/message/Packet;)Lcom/sun/xml/ws/api/message/Packet;");
//
//        builder.methodEntry( 24 , 1, 3, 0, 0);
//        endpointInvocationIds.add(24);
//        
//        builder.methodExit(24, 1, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 25 , 1, 1, 0, 0);
//        codecIds.add(25);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.transport.http.servlet.WSServlet", 0,
//                "doGet", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//
//        builder.methodEntry( 26 , 1, 3, 0, 0);
//        httpTransportIds.add(26);
//        
//        builder.methodExit( 26 , 1, 1, 0, 0);
//        builder.methodExit( 25 , 1, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.message.AttachmentSetImpl", 0,
//                "add", "(Lcom/sun/xml/ws/api/message/Attachment;)V");
//
//        builder.methodEntry( 27 , 1, 3, 0, 0);
//        messageProcessingIds.add(27);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.streaming.XMLStreamReaderUtil", 0,
//                "close", "(Ljavax/xml/stream/XMLStreamReader;)V");
//
//        builder.methodEntry( 28 , 1, 3, 0, 0);
//        streamingIds.add(28);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 29 , 1, 1, 0, 0);
//        streamingIds.add(29);
//        builder.methodExit( 29 , 1, 1, 0, 0);
//        builder.methodExit( 28 , 1, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 30 , 1, 1, 0, 0);
//        messageProcessingIds.add(30);
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        Category persistence = getCategory("Persistence");
//        Category hibernate = getCategory("Hibernate");
//        Category jdbc = getCategory("JDBC");
//        Category connection = getCategory("Connection");
//        Category statements = getCategory("Statements");
//        Category jpa = getCategory("JPA");
//        Category filters = getCategory("Filters");
//        Category filtersLifecycle = findCategory(filters, "Life Cycle");
//        Category jstl = getCategory("JSTL");
//        Category listeners = getCategory("Listeners");
//        Category servlets = getCategory("Servlets");
//        Category servletLifecycle = findCategory(servlets, "Life Cycle");
//        Category jsps = getCategory("JSPs");
//        
//        Category web = getCategory("Web Container");
//        Category jaxWs = getCategory("JAX-WS");
//        Category codec = findCategory( jaxWs, "Codec");
//        Category endpointInvocation = findCategory( jaxWs, "Endpoint Invocation");
//        Category httpTransport = findCategory( jaxWs, "HTTP Transport");
//        Category messageProcessing = findCategory( jaxWs, "Message Processing");
//        Category streaming = findCategory( jaxWs, "Streaming");
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        drillDown.drilldown( persistence.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        List<Integer> persistenceIds = new ArrayList<Integer>( hibernateIds.size() +
//                connectionIds.size() +statementsIds.size()+jpaIds.size());
//        persistenceIds.addAll( hibernateIds);
//        persistenceIds.addAll( connectionIds);
//        persistenceIds.addAll( statementsIds );
//        persistenceIds.addAll( jpaIds  );
//        assertEquals(persistenceIds.size()+" methods expected in Persistence category",
//                persistenceIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Persistence", persistenceIds);
//        
//        drillDown.drilldown( hibernate.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(hibernateIds.size()+" methods expected in Hibernate category",
//                hibernateIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Hibernate", hibernateIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( jdbc.getId());
//        
//        List<Integer> jdbcIds = new ArrayList<Integer>(connectionIds.size() +
//                statementsIds.size());
//        jdbcIds.addAll( connectionIds);
//        jdbcIds.addAll( statementsIds );
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jdbcIds.size()+" methods expected in JDBC category",
//                jdbcIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JDBC", jdbcIds);
//        
//        drillDown.drilldown( connection.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(connectionIds.size()+" methods expected in Connection category",
//                connectionIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Connection", connectionIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( statements.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(statementsIds.size()+" methods expected in Statements category",
//                statementsIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Statements", statementsIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown( jpa.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jpaIds.size()+" methods expected in JPA category",
//                jpaIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JPA", jpaIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown( web.getId());
//        
//        List<Integer> jaxIds = new ArrayList<Integer>(codecIds.size() +endpointInvocationIds.size() +
//                httpTransportIds.size()+messageProcessingIds.size()+
//                streamingIds.size());
//        
//        List<Integer> webIds = new ArrayList<Integer>( filtersIds.size() +
//                filtersLifecycleIds.size() +jspsIds.size() +jstlIds.size() +
//                listenersIds.size() +servletsIds.size() +servletsLifecycleIds.size()
//                +codecIds.size() +endpointInvocationIds.size() +
//                httpTransportIds.size()+messageProcessingIds.size()+
//                streamingIds.size());
//        
//        webIds.addAll( filtersIds);
//        webIds.addAll( filtersLifecycleIds);
//        webIds.addAll( jspsIds);
//        webIds.addAll( jstlIds);
//        webIds.addAll( listenersIds);
//        webIds.addAll(servletsIds) ;
//        webIds.addAll( servletsLifecycleIds);
//        jaxIds.addAll( codecIds);
//        jaxIds.addAll( endpointInvocationIds);
//        jaxIds.addAll( httpTransportIds);
//        jaxIds.addAll( messageProcessingIds );
//        jaxIds.addAll( streamingIds );
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(webIds.size()+" methods expected in Web Container category",
//                webIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Web Container", webIds);
//        
//        drillDown.drilldown( filters.getId());
//        
//        List<Integer> filtersAllIds = new ArrayList<Integer>( filtersIds.size()+
//                filtersLifecycleIds.size() );
//        filtersAllIds.addAll( filtersIds );
//        filtersAllIds.addAll( filtersLifecycleIds );
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(filtersAllIds.size()+" methods expected in Filters category",
//                filtersAllIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Filters", filtersAllIds);
//        
//        drillDown.drilldown( filtersLifecycle.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(filtersLifecycleIds.size()+" methods expected in Filters/Life Cycle category",
//                filtersLifecycleIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Filters/Life Cycle", filtersLifecycleIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown( jsps.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jspsIds.size()+" methods expected in JSPs category",
//                jspsIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JSPs", jspsIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( jstl.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jstlIds.size()+" methods expected in JSTL category",
//                jstlIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JSTL", jstlIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( listeners.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(listenersIds.size()+" methods expected in Listeners category",
//                listenersIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Listeners", listenersIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( servlets.getId());
//        
//        List<Integer> servletsAllIds = new ArrayList<Integer>( servletsIds.size()+
//                servletsLifecycleIds.size() );
//        servletsAllIds.addAll( servletsIds );
//        servletsAllIds.addAll( servletsLifecycleIds );
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(servletsAllIds.size()+" methods expected in Servlets category",
//                servletsAllIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Servlets", servletsAllIds);
//        
//        drillDown.drilldown( servletLifecycle.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(servletsLifecycleIds.size()+" methods expected in Servlets/Life Cycle category",
//                servletsLifecycleIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Servlets/Life Cycle", servletsLifecycleIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drillup();
//        
//        drillDown.drilldown( jaxWs.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jaxIds.size()+" methods expected in JAX-WS category",
//                jaxIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JAX-WS", jaxIds);
//        
//        drillDown.drilldown( codec.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(codecIds.size()+" methods expected in Codec category",
//                codecIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Codec", codecIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( endpointInvocation.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(endpointInvocationIds.size()+" methods expected in Endpoint Invocation category",
//                endpointInvocationIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Endpoint Invocation", endpointInvocationIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( httpTransport.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(httpTransportIds.size()+" methods expected in HTTP Transport category",
//                httpTransportIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "HTTP Transport", httpTransportIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( messageProcessing.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(messageProcessingIds.size()+" methods expected in Message Processing category",
//                messageProcessingIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Message Processing", messageProcessingIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( streaming.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(streamingIds.size()+" methods expected in Streaming category",
//                streamingIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Streaming", streamingIds);
    }
    
    public void testThreadedMixedCategories(){
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
//        List<Integer> hibernateIds = new LinkedList<Integer>();
//        List<Integer> connectionIds = new LinkedList<Integer>();
//        List<Integer> statementsIds = new LinkedList<Integer>();
//        List<Integer> jpaIds = new LinkedList<Integer>();
//        List<Integer> filtersLifecycleIds = new LinkedList<Integer>();
//        List<Integer> filtersIds = new LinkedList<Integer>();
//        List<Integer> jstlIds = new LinkedList<Integer>();
//        List<Integer> listenersIds = new LinkedList<Integer>();
//        List<Integer> servletsLifecycleIds = new LinkedList<Integer>();
//        List<Integer> servletsIds = new LinkedList<Integer>();
//        List<Integer> jspsIds = new LinkedList<Integer>();
//        List<Integer> codecIds = new LinkedList<Integer>();
//        List<Integer> endpointInvocationIds = new LinkedList<Integer>();
//        List<Integer> httpTransportIds = new LinkedList<Integer>();
//        List<Integer> messageProcessingIds = new LinkedList<Integer>();
//        List<Integer> streamingIds = new LinkedList<Integer>();
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("connection.TestDataSource", 0, 
//                "setLogWriter", "(Ljava/io/PrintWriter;)V");
//        builder.methodEntry( 2 , 0, 3, 0, 0);
//        connectionIds.add( 2 );
//        builder.methodExit( 2 , 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 3 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("org.hibernate.impl.SessionImpl", 0, 
//                "afterScrollOperation", "()V");
//        builder.methodEntry( 4 , 0, 3, 0, 0);
//        hibernateIds.add( 4 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 5 , 0, 1, 0, 0);
//        hibernateIds.add(5);
//        
//        status.updateInstrMethodsInfo("statements.TestStatement", 0, 
//                "getMoreResults", "()Z");
//        builder.methodEntry( 6 , 0, 3, 0, 0);
//        statementsIds.add( 6 );
//        
//        builder.methodExit( 6 , 0, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 7 , 0, 1, 0, 0);
//        hibernateIds.add(7);
//        
//        builder.methodExit( 7 , 0, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 8 , 0, 1, 0, 0);
//        hibernateIds.add(8);
//        
//        builder.methodExit( 8 , 0, 1, 0, 0);
//        builder.methodExit( 5 , 0, 1, 0, 0);
//        builder.methodExit( 4 , 0, 3, 0, 0);
//        
//        builder.newThread(1, "Thread-0", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
//                "setParameter", "(ILjava/lang/Object;)Ljavax/persistence/Query;");
//        builder.methodEntry( 9 , 1, 2, 0, 0);
//        jpaIds.add( 9 );
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "init", "(Ljavax/servlet/FilterConfig;)V");
//        builder.methodEntry( 10 , 1, 3, 0, 0);
//        filtersLifecycleIds.add( 10 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 11 , 1, 1, 0, 0);
//        filtersLifecycleIds.add(11);
//        
//        builder.methodExit(11, 1, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
//                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V");
//        builder.methodEntry( 12 , 1, 3, 0, 0);
//        filtersIds.add( 12 );
//        
//        builder.methodExit(12, 1, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass4", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 13 , 1, 1, 0, 0);
//        filtersLifecycleIds.add(13);
//        
//        builder.methodExit(13, 1, 1, 0, 0);
//        builder.methodExit(10, 1, 3, 0, 0);
//        
//        builder.newThread(2, "Thread-1", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("jstl.TestTagSupport", 0, 
//                "setParent", "(Ljavax/servlet/jsp/tagext/JspTag;)V");
//        builder.methodEntry( 14 , 2, 2, 0, 0);
//        jstlIds.add( 14 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass5", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 15 , 2, 1, 0, 0);
//        jstlIds.add(15);
//        
//        status.updateInstrMethodsInfo("listeners.TestHttpSessionListener", 0, 
//                "sessionCreated", "(Ljavax/servlet/http/HttpSessionEvent;)V");
//        builder.methodEntry( 16 , 2, 3, 0, 0);
//        listenersIds.add( 16 );
//        
//        builder.newThread( 3 , "Thread-2", "java.lang.Thread");
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "(Ljavax/servlet/FilterConfig;)V");
//        
//        builder.methodEntry( 17 , 3, 2, 0, 0);
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "init", "()V");
//        builder.methodEntry( 18 , 3, 3, 0, 0);
//        servletsLifecycleIds.add( 18 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
//                "method", "(Ljava/io/PrintWriter;)V");
//        builder.methodEntry( 19 , 3, 1, 0, 0);
//        servletsLifecycleIds.add( 19 );
//        
//        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
//                "doHead", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//        builder.methodEntry( 20 , 3, 3, 0, 0);
//        servletsIds.add( 20 );
//        
//        builder.newThread( 4 , "Thread-3", "java.lang.Thread");
//        status.updateInstrMethodsInfo("com.sun.xml.ws.encoding.MtomCodec.MtomStreamWriter", 0,
//                "flush", "()V");
//        builder.methodEntry( 21 , 4, 2, 0, 0);
//        codecIds.add( 21 );
//        
//        status.updateInstrMethodsInfo("org.apache.jsp.index_jsp", 0,
//                "_jspService", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//        builder.methodEntry( 22 , 4, 3, 0, 0);
//        jspsIds.add( 22 );
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 23 , 4, 1, 0, 0);
//        jspsIds.add(23);
//        
//        builder.methodExit(23, 4, 1, 0, 0);
//        builder.methodExit(22, 4, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl", 0,
//                "process", "(Lcom/sun/xml/ws/api/message/Packet;)Lcom/sun/xml/ws/api/message/Packet;");
//
//        builder.methodEntry( 24 , 4, 3, 0, 0);
//        endpointInvocationIds.add(24);
//        
//        builder.methodExit(24, 4, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 25 , 4, 1, 0, 0);
//        codecIds.add(25);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.transport.http.servlet.WSServlet", 0,
//                "doGet", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
//
//        builder.methodEntry( 26 , 4, 3, 0, 0);
//        httpTransportIds.add(26);
//        
//        builder.methodExit( 26 , 4, 1, 0, 0);
//        builder.methodExit( 25 , 4, 1, 0, 0);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.message.AttachmentSetImpl", 0,
//                "add", "(Lcom/sun/xml/ws/api/message/Attachment;)V");
//
//        builder.methodEntry( 27 , 4, 3, 0, 0);
//        messageProcessingIds.add(27);
//        
//        status.updateInstrMethodsInfo("com.sun.xml.ws.streaming.XMLStreamReaderUtil", 0,
//                "close", "(Ljavax/xml/stream/XMLStreamReader;)V");
//
//        builder.methodEntry( 28 , 4, 3, 0, 0);
//        streamingIds.add(28);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 29 , 4, 1, 0, 0);
//        streamingIds.add(29);
//        builder.methodExit( 29 , 4, 1, 0, 0);
//        builder.methodExit( 28 , 4, 3, 0, 0);
//        
//        status.updateInstrMethodsInfo("pack.CustomClass6", 0, 
//                "method", "()V");
//        
//        builder.methodEntry( 30 , 4, 1, 0, 0);
//        messageProcessingIds.add(30);
//        
//        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
//        Category persistence = getCategory("Persistence");
//        Category hibernate = getCategory("Hibernate");
//        Category jdbc = getCategory("JDBC");
//        Category connection = getCategory("Connection");
//        Category statements = getCategory("Statements");
//        Category jpa = getCategory("JPA");
//        Category filters = getCategory("Filters");
//        Category filtersLifecycle = findCategory(filters, "Life Cycle");
//        Category jstl = getCategory("JSTL");
//        Category listeners = getCategory("Listeners");
//        Category servlets = getCategory("Servlets");
//        Category servletLifecycle = findCategory(servlets, "Life Cycle");
//        Category jsps = getCategory("JSPs");
//        
//        Category web = getCategory("Web Container");
//        Category jaxWs = getCategory("JAX-WS");
//        Category codec = findCategory( jaxWs, "Codec");
//        Category endpointInvocation = findCategory( jaxWs, "Endpoint Invocation");
//        Category httpTransport = findCategory( jaxWs, "HTTP Transport");
//        Category messageProcessing = findCategory( jaxWs, "Message Processing");
//        Category streaming = findCategory( jaxWs, "Streaming");
//        
//        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();
//
//        drillDown.drilldown( persistence.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();
//
//        List<Integer> persistenceIds = new ArrayList<Integer>( hibernateIds.size() +
//                connectionIds.size() +statementsIds.size()+jpaIds.size());
//        persistenceIds.addAll( hibernateIds);
//        persistenceIds.addAll( connectionIds);
//        persistenceIds.addAll( statementsIds );
//        persistenceIds.addAll( jpaIds  );
//        assertEquals(persistenceIds.size()+" methods expected in Persistence category",
//                persistenceIds.size() ,  flatProfile.getNRows());
//        
//        checkCategory(status, flatProfile, "Persistence", persistenceIds);
//        
//        drillDown.drilldown( hibernate.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(hibernateIds.size()+" methods expected in Hibernate category",
//                hibernateIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Hibernate", hibernateIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( jdbc.getId());
//        
//        List<Integer> jdbcIds = new ArrayList<Integer>(connectionIds.size() +
//                statementsIds.size());
//        jdbcIds.addAll( connectionIds);
//        jdbcIds.addAll( statementsIds );
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jdbcIds.size()+" methods expected in JDBC category",
//                jdbcIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JDBC", jdbcIds);
//        
//        drillDown.drilldown( connection.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(connectionIds.size()+" methods expected in Connection category",
//                connectionIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Connection", connectionIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( statements.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(statementsIds.size()+" methods expected in Statements category",
//                statementsIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Statements", statementsIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown( jpa.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jpaIds.size()+" methods expected in JPA category",
//                jpaIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JPA", jpaIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown( web.getId());
//        
//        List<Integer> webIds = new ArrayList<Integer>( filtersIds.size() +
//                filtersLifecycleIds.size() +jspsIds.size() +jstlIds.size() +
//                listenersIds.size() +servletsIds.size() +servletsLifecycleIds.size()
//                +codecIds.size() +endpointInvocationIds.size() +
//                httpTransportIds.size()+messageProcessingIds.size()+
//                streamingIds.size());
//        
//        webIds.addAll( filtersIds);
//        webIds.addAll( filtersLifecycleIds);
//        webIds.addAll( jspsIds);
//        webIds.addAll( jstlIds);
//        webIds.addAll( listenersIds);
//        webIds.addAll(servletsIds) ;
//        webIds.addAll( servletsLifecycleIds);
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(webIds.size()+" methods expected in Web Container category",
//                webIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Web Container", webIds);
//        
//        drillDown.drilldown( filters.getId());
//        
//        List<Integer> filtersAllIds = new ArrayList<Integer>( filtersIds.size()+
//                filtersLifecycleIds.size() );
//        filtersAllIds.addAll( filtersIds );
//        filtersAllIds.addAll( filtersLifecycleIds );
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(filtersAllIds.size()+" methods expected in Filters category",
//                filtersAllIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Filters", filtersAllIds);
//        
//        drillDown.drilldown( filtersLifecycle.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(filtersLifecycleIds.size()+" methods expected in Filters/Life Cycle category",
//                filtersLifecycleIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Filters/Life Cycle", filtersLifecycleIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown( jsps.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jspsIds.size()+" methods expected in JSPs category",
//                jspsIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JSPs", jspsIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( jstl.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(jstlIds.size()+" methods expected in JSTL category",
//                jstlIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JSTL", jstlIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( listeners.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(listenersIds.size()+" methods expected in Listeners category",
//                listenersIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Listeners", listenersIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( servlets.getId());
//        
//        List<Integer> servletsAllIds = new ArrayList<Integer>( servletsIds.size()+
//                servletsLifecycleIds.size() );
//        servletsAllIds.addAll( servletsIds );
//        servletsAllIds.addAll( servletsLifecycleIds );
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(servletsAllIds.size()+" methods expected in Servlets category",
//                servletsAllIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Servlets", servletsAllIds);
//        
//        drillDown.drilldown( servletLifecycle.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(servletsLifecycleIds.size()+" methods expected in Servlets/Life Cycle category",
//                servletsLifecycleIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Servlets/Life Cycle", servletsLifecycleIds);
//        
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drillup();
//        drillDown.drilldown(jaxWs.getId());
//        
//        List<Integer> jaxIds = new ArrayList<Integer>(codecIds.size() +endpointInvocationIds.size() +
//                httpTransportIds.size()+messageProcessingIds.size()+ streamingIds.size());
//        
//        jaxIds.addAll(codecIds);
//        jaxIds.addAll(endpointInvocationIds);
//        jaxIds.addAll(httpTransportIds);
//        jaxIds.addAll(messageProcessingIds);
//        jaxIds.addAll(streamingIds);
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        
//        assertEquals(jaxIds.size()+" methods expected in JAX-WS category",
//                jaxIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "JAX-WS", jaxIds);
//        
//        drillDown.drilldown(codec.getId());
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        
//        assertEquals(codecIds.size()+" methods expected in JAX-WS Codecs category", 
//                     codecIds.size(), flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Codec", codecIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( endpointInvocation.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(endpointInvocationIds.size()+" methods expected in Endpoint Invocation category",
//                endpointInvocationIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Endpoint Invocation", endpointInvocationIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( httpTransport.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(httpTransportIds.size()+" methods expected in HTTP Transport category",
//                httpTransportIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "HTTP Transport", httpTransportIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( messageProcessing.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(messageProcessingIds.size()+" methods expected in Message Processing category",
//                messageProcessingIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Message Processing", messageProcessingIds);
//        
//        drillDown.drillup();
//        drillDown.drilldown( streaming.getId());
//        
//        flatProfileBuilder.cctEstablished( root , false );
//        flatProfile = flatProfileBuilder.createFlatProfile();
//        assertEquals(streamingIds.size()+" methods expected in Streaming category",
//                streamingIds.size() ,  flatProfile.getNRows());
//        checkCategory(status, flatProfile, "Streaming", streamingIds);
    }
    
    @Override
    protected String getProjectName() {
        return TestBase.APP_NAME;
    }
}
