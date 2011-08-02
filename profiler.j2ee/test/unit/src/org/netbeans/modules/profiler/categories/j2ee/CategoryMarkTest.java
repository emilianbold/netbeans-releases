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
public class CategoryMarkTest extends CategoryMarkTestBase {

    public CategoryMarkTest( String name ) {
        super(name);
    }
    
    public void testHibernate(){
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
        plainIds.add(5);
        
        status.updateInstrMethodsInfo("org.hibernate.impl.CriteriaImpl", 0, 
                "after", "()V");
        builder.methodEntry( 6 , 1, 3, 0, 0);
        markedIds.add( 6 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category hibernate = getCategory("Hibernate");
        Mark hibernateMark = hibernate.getAssignedMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, hibernateMark, hibernate);
    }
    
    public void testConnection(){
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
        plainIds.add(5);
        
        status.updateInstrMethodsInfo("connection.TestDataSource", 0, 
                "getConnection", "(Ljava/lang/String;Ljava/lang/String;)Ljava/sql/Connection;");
        builder.methodEntry( 6 , 1, 3, 0, 0);
        markedIds.add( 6 );
        
        builder.methodExit(6,1 , 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 7 , 1, 1, 0, 0);
        plainIds.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category connection = getCategory("Connection");
        Mark connectionMark = connection.getAssignedMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, connectionMark, 
                connection);
    }
    
    public void testStatements(){
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
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category statements = getCategory("Statements");
        Mark statementsMark = statements.getAssignedMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, statementsMark, 
                statements);
    }
    
    public void testJpa(){
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
        
        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
                "setParameter", "(ILjava/lang/Object;)Ljavax/persistence/Query;");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        builder.methodExit(2, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        plainIds.add( 3 );
        builder.methodExit(3, 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        plainIds.add( 4 );
        
        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
                "executeUpdate", "()I");
        builder.methodEntry( 5 , 0, 3, 0, 0);
        markedIds.add( 5 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 6 , 1, 2, 0, 0);
        plainIds.add( 6 );
        
        status.updateInstrMethodsInfo("jpa.TestQuery", 0, 
                "setParameter", "(ILjava/util/Date;Ljavax/persistence/TemporalType;)Ljavax/persistence/Query;");
       
        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add(7);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0, 
                "method", "()V");
        
        builder.methodEntry( 8 , 1, 1, 0, 0);
        markedIds.add( 8 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category jpa = getCategory("JPA");
        Mark jpaMark = jpa.getAssignedMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, jpaMark, 
                jpa);
    }
    
    public void testFiltersLifecycle(){
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
        
        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
                "init", "(Ljavax/servlet/FilterConfig;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add( 3 );
        builder.methodExit(3, 0, 1, 0, 0);
        builder.methodExit(2, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
                "destroy", "()V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add( 4 );
        
        builder.methodExit(4, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        plainIds.add( 5 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 6 , 1, 2, 0, 0);
        plainIds.add( 6 );
        
        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V");
        builder.methodEntry( 7 , 1, 3, 0, 0);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category filters = getCategory("Filters");
        Category lifecycle = findCategory(filters, "Life Cycle");
        Mark lifecycleMark = lifecycle.getAssignedMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(lifecycleMark)) {
                assertEquals("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Filters/Life Cycle" +" category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), id, 7 );
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
            }
        }
        if ( !markedIds.isEmpty()){
            int id = markedIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
    public void testFilters(){
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
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add( 2 );
        builder.methodExit(2, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
                "destroy", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add( 4 );
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        markedIds.add( 5 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("filter.TestFilter", 0, 
                "init", "(Ljavax/servlet/FilterConfig;)V");
        builder.methodEntry( 6 , 1, 2, 0, 0);
        
        
        status.updateInstrMethodsInfo("filter.TestChain", 0, 
                "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V");
        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category filters = getCategory("Filters");
        Mark filtersMark = filters.getAssignedMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(filtersMark)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Filters category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), id==6 ||id==3);
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
            }
        }
        if ( !markedIds.isEmpty()){
            int id = markedIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
    }
    
   public void testJstl(){
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
        
        status.updateInstrMethodsInfo("jstl.TestTagSupport", 0, 
                "setParent", "(Ljavax/servlet/jsp/tagext/JspTag;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add( 3 );
        
        builder.methodExit(3, 0, 1, 0, 0);
        builder.methodExit(2, 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        plainIds.add( 4 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 1, 2, 0, 0);
        plainIds.add( 5 );
        
        status.updateInstrMethodsInfo("jstl.TestTagSupport", 0, 
                "getJspBody", "()Ljavax/servlet/jsp/tagext/JspFragment;");
        builder.methodEntry( 6 , 1, 3, 0, 0);
        markedIds.add( 6 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category jstl = getCategory("JSTL");
        Mark jstlMark = jstl.getAssignedMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, jstlMark, 
                jstl);
    }
    
    public void testListeners(){
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
        
        status.updateInstrMethodsInfo("listeners.TestHttpSessionListener", 0, 
                "sessionCreated", "(Ljavax/servlet/http/HttpSessionEvent;)V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add( 3 );
        
        builder.methodExit(3, 0, 1, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 4 , 0, 1, 0, 0);
        markedIds.add( 4 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        
        status.updateInstrMethodsInfo("listeners.TestHttpSessionListener", 0, 
                "sessionDestroyed", "(Ljavax/servlet/http/HttpSessionEvent;)V");
        builder.methodEntry( 5 , 1, 2, 0, 0);
        markedIds.add( 5 );
        
        builder.methodExit(5, 1, 2, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 6 , 1, 1, 0, 0);
        plainIds.add( 6 );
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category listeners = getCategory("Listeners");
        Mark listenersMark = listeners.getAssignedMark();
        
        checkMarks(status, markedIds, plainIds, methodMarks, listenersMark, 
                listeners);
    }
    
    public void testServletsLifecycle(){
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
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "init", "()V");
        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add( 2 );
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 3 , 0, 1, 0, 0);
        markedIds.add( 3 );
        builder.methodExit(3, 0, 1, 0, 0);
        builder.methodExit(2, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "destroy", "()V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add( 4 );
        
        builder.methodExit(4, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        plainIds.add( 5 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0, 
                "method", "()V");
        
        builder.methodEntry( 6 , 1, 2, 0, 0);
        plainIds.add( 6 );
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "service", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V");
        builder.methodEntry( 7 , 1, 3, 0, 0);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category servlets = getCategory("Servlets");
        Category lifecycle = findCategory(servlets, "Life Cycle");
        Mark lifecycleMark = lifecycle.getAssignedMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(lifecycleMark)) {
                assertEquals("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Servlets/Life Cycle" +" category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), id, 7 );
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
            }
        }
        if ( !markedIds.isEmpty()){
            int id = markedIds.iterator().next();
            assertTrue( "There is a method  '"+status.getInstrMethodClasses()[id]+"."+
                    status.getInstrMethodNames()[id]+
                    "' which is not marked", false);
        }
        
    }
    
    public void testServlets(){
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
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0, 
                "method", "()V");
        
        builder.methodEntry( 2 , 0, 1, 0, 0);
        plainIds.add( 2 );
        builder.methodExit(2, 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "destroy", "()V");
        builder.methodEntry( 3 , 0, 3, 0, 0);
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "doHead", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V");
        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add( 4 );
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0, 
                "method", "()V");
        
        builder.methodEntry( 5 , 0, 1, 0, 0);
        markedIds.add( 5 );
        
        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "init", "(Ljavax/servlet/FilterConfig;)V");
        builder.methodEntry( 6 , 1, 2, 0, 0);
        
        
        status.updateInstrMethodsInfo("servlets.TestHttpServlet", 0, 
                "getServletConfig", "()Ljavax/servlet/ServletConfig;");
        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();
        
        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category servlets = getCategory("Servlets");
        Mark servletsMark = servlets.getAssignedMark();
        
        for (Entry<Integer, Mark> entry : methodMarks.entrySet()) {
            int id = entry.getKey();
            Mark mark = entry.getValue();
            if (!mark.equals(servletsMark)) {
                assertTrue("Method '"
                        + status.getInstrMethodClasses()[id]
                        + "."
                        + status.getInstrMethodNames()[id]
                        + "' should be included in "
                        + "Servlets category, but its category is "
                        + getCategorization().getCategoryForMark(mark)
                                .getLabel(), id==6 ||id==3);
            }
            markedIds.remove( id );
            if (plainIds.contains(id)) {
                assertTrue("There is a method '"
                        + status.getInstrMethodClasses()[id] + "."
                        + status.getInstrMethodNames()[id]
                        + "' which should not be categorized , but is category is :" +
                                getCategorization().getCategoryForMark(mark).getLabel(), false);
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
        return TestBase.APP_NAME;
    }

}
