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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;
import org.netbeans.modules.profiler.categorization.api.Category;


/**
 * @author ads
 *
 */
public class WebCategoryMarkTest extends CategoryMarkTestBase {

    public WebCategoryMarkTest( String name ) {
        super(name);
    }
    
    
    public void testJsps(){
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
        plainIds.add(5);

        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        status.updateInstrMethodsInfo("pack.CustomClass1", 0,
                "<init>", "()V");

        builder.methodEntry( 6 , 1, 2, 0, 0);
        plainIds.add(5);

        status.updateInstrMethodsInfo("org.apache.jsp.newjsp_jsp", 0,
                "_jspService", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add( 7 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category jsps = getCategory("JSPs");
        Mark jspsMark = jsps.getAssignedMark();

        checkMarks(status, markedIds, plainIds, methodMarks, jspsMark, jsps);
    }
    
    public void testWebContainerWsCodec(){
        Category jaxWs = getCategory("JAX-WS");
        Category codec = findCategory( jaxWs, "Codec");
        
        doTestCodec( codec);
    }
    
    public void testWebContainerWsEndpointInvocation(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        Category jaxWs = getCategory("JAX-WS");
//        Category endpointInvocation = findCategory( jaxWs, "Endpoint Invocation");
//        
//        doTestEndpointInvocation( endpointInvocation);
    }
    
    public void testWebContainerWsHttpTransport(){
        Category jaxWs = getCategory("JAX-WS");
        Category httpTransport = findCategory( jaxWs, "HTTP Transport");
        
        doTestHttpTransport( httpTransport);
    }
    
    public void testWebContainerWsMessageProcessing(){
        Category jaxWs = getCategory("JAX-WS");
        Category messageprocessing = findCategory( jaxWs, "Message Processing");
        
        doTestMessageProcessing( messageprocessing);
    }
    
    public void testWebContainerWsStreaming(){
        Category jaxWs = getCategory("JAX-WS");
        Category streaming = findCategory( jaxWs, "Streaming");
        
        doTestStreaming( streaming);
    }
    
    public void testMixedCategories(){
        // temporarily ignoring; need evaluation from T.Zezula about javasource, synthetic sources and user tasks
//        resetMarkMappings();
//        
//        TestGraphBuilder builder = new TestGraphBuilder();
//        ProfilerEngineSettings settings = new ProfilerEngineSettings();
//        ProfilingSessionStatus status = new ProfilingSessionStatus();
//        ProfilerClient client = new ProfilerClient(settings, status , null, null); 
//        builder.startup( client );
//        
//        Set<Integer> hibernateIds = new HashSet<Integer>();
//        Set<Integer> connectionIds = new HashSet<Integer>();
//        Set<Integer> statementsIds = new HashSet<Integer>();
//        Set<Integer> jpaIds = new HashSet<Integer>();
//        Set<Integer> filtersLifecycleIds = new HashSet<Integer>();
//        Set<Integer> filtersIds = new HashSet<Integer>();
//        Set<Integer> jstlIds = new HashSet<Integer>();
//        Set<Integer> listenersIds = new HashSet<Integer>();
//        Set<Integer> servletsLifecycleIds = new HashSet<Integer>();
//        Set<Integer> servletsIds = new HashSet<Integer>();
//        Set<Integer> jspsIds = new HashSet<Integer>();
//        Set<Integer> codecIds = new HashSet<Integer>();
//        Set<Integer> endpointInvocationIds = new HashSet<Integer>();
//        Set<Integer> httpTransportIds = new HashSet<Integer>();
//        Set<Integer> messageProcessingIds = new HashSet<Integer>();
//        Set<Integer> streamingIds = new HashSet<Integer>();
//        Set<Integer> plainIds = new HashSet<Integer>();
//        
//        builder.newThread( 0 , "main", "java.lang.Thread");
//        plainIds.add(0);
//        status.updateInstrMethodsInfo("Main", 0, "main", "([Ljava/lang/String;)V");
//        builder.methodEntry( 1 , 0, 2, 0, 0);
//        plainIds.add(1);
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
//        plainIds.add(3);
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
//        plainIds.add(15);
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
//        plainIds.add( 17 );
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
//        Map<Integer, Mark> methodMarks = getMethodMarks(root );
//        Category hibernate = getCategory("Hibernate");
//        Mark hibernateMark = hibernate.getAssignedMark();
//        Category connection = getCategory("Connection");
//        Mark connectionMark = connection.getAssignedMark();
//        Category statements = getCategory("Statements");
//        Mark statementsMark = statements.getAssignedMark();
//        Category jpa = getCategory("JPA");
//        Mark jpaMark = jpa.getAssignedMark();
//        Category filters = getCategory("Filters");
//        Mark filtersMark = filters.getAssignedMark();
//        Category filtersLifecycle = findCategory(filters, "Life Cycle");
//        Mark filtersLifecycleMark = filtersLifecycle.getAssignedMark();
//        Category jstl = getCategory("JSTL");
//        Mark jstlMark = jstl.getAssignedMark();
//        Category listeners = getCategory("Listeners");
//        Mark listenersMark = listeners.getAssignedMark();
//        Category servlets = getCategory("Servlets");
//        Mark servletsMark = servlets.getAssignedMark();
//        Category servletLifecycle = findCategory(servlets, "Life Cycle");
//        Mark servletLifecycleMark = servletLifecycle.getAssignedMark();
//        Category jsps = getCategory("JSPs");
//        Mark jspsMark = jsps.getAssignedMark();
//        
//        Category jaxWs = getCategory("JAX-WS");
//        Category codec = findCategory( jaxWs, "Codec");
//        Mark codecMark = codec.getAssignedMark();
//        Category endpointInvocation = findCategory( jaxWs, "Endpoint Invocation");
//        Mark endpointInvocationMark = endpointInvocation.getAssignedMark();
//        Category httpTransport = findCategory( jaxWs, "HTTP Transport");
//        Mark httpTranspMark = httpTransport.getAssignedMark();
//        Category messageProcessing = findCategory( jaxWs, "Message Processing");
//        Mark messageProcessingMark = messageProcessing.getAssignedMark();
//        Category streaming = findCategory( jaxWs, "Streaming");
//        Mark streamingMark = streaming.getAssignedMark();
//        
//        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
//            int id = entry.getKey();
//            Mark mark = entry.getValue();
//            if ( hibernateIds.contains( id)){
//                check(status, id, hibernate.getLabel(),mark,  hibernateMark);
//                hibernateIds.remove(id);
//            }
//            else  if ( connectionIds.contains( id)){
//                check(status, id, connection.getLabel(), mark , connectionMark);
//                connectionIds.remove(id );
//            }
//            else  if ( statementsIds.contains( id)){
//                check(status, id, statements.getLabel(), mark , statementsMark);
//                statementsIds.remove(id );
//            }
//            else  if ( jpaIds.contains( id)){
//                check(status, id, jpa.getLabel(), mark , jpaMark);
//                jpaIds.remove(id );
//            }
//            else  if ( filtersLifecycleIds.contains( id)){
//                check(status, id, "Filters/Life Cycle", mark , filtersLifecycleMark);
//                filtersLifecycleIds.remove(id );
//            }
//            else  if ( filtersIds.contains( id)){
//                check(status, id, filters.getLabel(), mark , filtersMark);
//                filtersIds.remove(id );
//            }
//            else  if ( jstlIds.contains( id)){
//                check(status, id, jstl.getLabel(), mark , jstlMark);
//                jstlIds.remove(id );
//            }
//            else  if ( listenersIds.contains( id)){
//                check(status, id, listeners.getLabel(), mark , listenersMark);
//                listenersIds.remove(id );
//            }
//            else  if ( servletsLifecycleIds.contains( id)){
//                check(status, id, "Servlets/Life Cycle", mark , servletLifecycleMark);
//                servletsLifecycleIds.remove(id );
//            }
//            else  if ( servletsIds.contains( id)){
//                check(status, id, servlets.getLabel(), mark , servletsMark);
//                servletsIds.remove(id );
//            }
//            else  if ( jspsIds.contains( id)){
//                check(status, id, jsps.getLabel(), mark , jspsMark);
//                jspsIds.remove(id );
//            }
//            else  if ( codecIds.contains( id)){
//                check(status, id, codec.getLabel(), mark , codecMark);
//                codecIds.remove(id );
//            }
//            else  if ( endpointInvocationIds.contains( id)){
//                check(status, id, endpointInvocation.getLabel(), mark , endpointInvocationMark);
//                endpointInvocationIds.remove(id );
//            }
//            else  if ( httpTransportIds.contains( id)){
//                check(status, id, httpTransport.getLabel(), mark , httpTranspMark);
//                httpTransportIds.remove(id );
//            }
//            else  if ( messageProcessingIds.contains( id)){
//                check(status, id, messageProcessing.getLabel(), mark , messageProcessingMark);
//                messageProcessingIds.remove(id );
//            }
//            else  if ( streamingIds.contains( id)){
//                check(status, id, streaming.getLabel(), mark , streamingMark);
//                streamingIds.remove(id );
//            }
//            else if (plainIds.contains(id)) {
//                assertTrue("There is a method '"
//                        + status.getInstrMethodClasses()[id] + "."
//                        + status.getInstrMethodNames()[id]
//                        + "' which should not be categorized , but is category is :" +
//                                getCategorization().getCategoryForMark(mark).getLabel(), false);
//            }
//        }
//        if ( !hibernateIds.isEmpty()){
//            fail( hibernateIds, status, hibernate.getLabel());
//        }
//        if ( !connectionIds.isEmpty()){
//            fail( connectionIds, status, connection.getLabel());
//        }
//        if ( !statementsIds.isEmpty()){
//            fail( statementsIds, status, statements.getLabel());
//        }
//        if ( !jpaIds.isEmpty()){
//            fail( jpaIds, status, jpa.getLabel());
//        }
//        if ( !filtersLifecycleIds.isEmpty()){
//            fail( filtersLifecycleIds, status, "Filters/Life Cycle");
//        }
//        if ( !filtersIds.isEmpty()){
//            fail( filtersIds, status, filters.getLabel());
//        }
//        if ( !jstlIds.isEmpty()){
//            fail( jstlIds, status, jstl.getLabel());
//        }
//        if ( !listenersIds.isEmpty()){
//            fail( listenersIds, status, listeners.getLabel());
//        }
//        if ( !servletsLifecycleIds.isEmpty()){
//            fail( servletsLifecycleIds, status, "Servlets/Life Cycle");
//        }
//        if ( !servletsIds.isEmpty()){
//            fail( servletsIds, status, servlets.getLabel());
//        }
//        if ( !jspsIds.isEmpty()){
//            fail( jspsIds, status, jsps.getLabel());
//        }
//        if ( !codecIds.isEmpty()){
//            fail( codecIds, status, codec.getLabel());
//        }
//        if ( !endpointInvocationIds.isEmpty()){
//            fail( endpointInvocationIds, status, endpointInvocation.getLabel());
//        }
//        if ( !httpTransportIds.isEmpty()){
//            fail( httpTransportIds, status, httpTransport.getLabel());
//        }
//        if ( !messageProcessingIds.isEmpty()){
//            fail( messageProcessingIds, status, messageProcessing.getLabel());
//        }
//        if ( !streamingIds.isEmpty()){
//            fail( streamingIds, status, streaming.getLabel());
//        }
    }
    
    private void fail(Set<Integer> set, ProfilingSessionStatus status,
            String categoryName )
    {
        int id = set.iterator().next();
        assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                status.getInstrMethodNames()[id]+
                "' which is not marked. It should be in "+categoryName, false);
    }
            
    private void check(ProfilingSessionStatus status,  int id, 
            String categoryName , Mark mark, Mark requiredMark)
    {
        if (!mark.equals(requiredMark)) {
            assertTrue("Method '"
                + status.getInstrMethodClasses()[id]
                + "."
                + status.getInstrMethodNames()[id]
                + "' should be included in "
                + categoryName +", but its category is "
                + getCategorization().getCategoryForMark(mark)
                        .getLabel(), false);
        }
    }
    
    @Override
    protected String getProjectName() {
        return TestBase.APP_NAME;
    }

}
