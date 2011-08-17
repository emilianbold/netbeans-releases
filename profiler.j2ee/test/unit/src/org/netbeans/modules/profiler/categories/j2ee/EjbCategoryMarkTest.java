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
import java.util.Set;
import java.util.Stack;

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
public class EjbCategoryMarkTest extends CategoryMarkTestBase {

    public EjbCategoryMarkTest( String name ) {
        super(name);
    }
    
    public void testLifeCycle(){
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
        
        status.updateInstrMethodsInfo("pack.NewMessageBean", 0,
                "onMessage", "(Ljavax/jms/Message;)V");

        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add(2);
        builder.methodExit( 2,0,3,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0,
                "method", "()V");

        builder.methodEntry( 3 , 0, 1, 0, 0);
        plainIds.add(3);
        
        status.updateInstrMethodsInfo("pack.NewSessionBean", 0,
                "businessMethod", "()V");

        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add(4);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0,
                "method", "()V");

        builder.methodEntry( 5 , 0, 1, 0, 0);
        markedIds.add(5);

        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.NewSessionBean", 0,
                "method", "()V");

        builder.methodEntry( 6 , 1, 2, 0, 0);
        markedIds.add(6);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0,
                "method", "()V");

        builder.methodEntry( 7 , 1, 1, 0, 0);
        markedIds.add(7);
        
        builder.methodExit( 7,1,1,0,0);
        builder.methodExit( 6,1,2,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0,
                "method", "()V");

        builder.methodEntry( 8 , 1, 1, 0, 0);
        plainIds.add(8);
        
        status.updateInstrMethodsInfo("pack.NewMessageBean", 0,
                "ejbRemove", "()V");
        
        builder.methodEntry( 9 , 1, 3, 0, 0);
        markedIds.add(9);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category ejbContainer = getCategory("Enterprise Beans");
        Category lifeCycle = findCategory(ejbContainer, "Lifecycle");
        Mark lifecycleMark = lifeCycle.getAssignedMark();

        checkMarks(status, markedIds, plainIds, methodMarks, lifecycleMark, lifeCycle);
    }
    
    public void testPersistence(){
        
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
        plainIds.add(2);
        
        status.updateInstrMethodsInfo("pack.StatefulBean", 0,
                "ejbStore", "()V");

        builder.methodEntry( 3 , 0, 3, 0, 0);
        markedIds.add(3);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0,
                "method", "()V");

        builder.methodEntry( 4 , 0, 1, 0, 0);
        markedIds.add(4);

        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.StatefulBean", 0,
                "ejbLoad", "()V");

        builder.methodEntry( 5 , 1, 2, 0, 0);
        markedIds.add(5);
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0,
                "method", "()V");

        builder.methodEntry( 6 , 1, 1, 0, 0);
        markedIds.add(6);
        builder.methodExit( 6,1,1,0,0);
        
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0,
                "method", "()V");

        builder.methodEntry( 7 , 1, 1, 0, 0);
        markedIds.add(7);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category ejbContainer = getCategory("Enterprise Beans");
        Category persistence = findCategory(ejbContainer, "Persistence");
        Mark persistenceMark = persistence.getAssignedMark();

        checkMarks(status, markedIds, plainIds, methodMarks, persistenceMark, persistence);
    }
    
    public void testEjbContainer(){
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
        
        status.updateInstrMethodsInfo("pack.NewMessageBean", 0,
                "wait", "()V");

        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add(2);
        
        builder.methodExit( 2,0,1,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0,
                "method", "()V");

        builder.methodEntry( 3 , 0, 1, 0, 0);
        plainIds.add(3);
        
        status.updateInstrMethodsInfo("pack.NewMessageBean", 0,
                "notify", "()V");

        builder.methodEntry( 4 , 0, 3, 0, 0);
        markedIds.add(4);
        
        status.updateInstrMethodsInfo("pack.CustomClass1", 0,
                "method", "()V");

        builder.methodEntry( 5 , 0, 1, 0, 0);
        markedIds.add(5);

        builder.newThread( 1 , "Thread-1", "java.lang.Thread");
        
        status.updateInstrMethodsInfo("pack.CustomClass2", 0,
                "method", "()V");

        builder.methodEntry( 6 , 1, 2, 0, 0);
        plainIds.add(6);
        
        status.updateInstrMethodsInfo("pack.StatefulBean", 0,
                "method", "()V");

        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add(7);
        
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0,
                "method", "()V");

        builder.methodEntry( 8 , 1, 1, 0, 0);
        markedIds.add(8);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        Map<Integer, Mark> methodMarks = getMethodMarks(root );
        Category ejbContainer = getCategory("Enterprise Beans");

        checkMarks(status, markedIds, plainIds, methodMarks, 
                ejbContainer.getAssignedMark(), ejbContainer);
    }
    
    /*
     * This test performs checks of presence Persistence category with all 
     * its subcategories for EjbProject type.
     * Correctness of marks is not checked because Web Project has Persistence
     * category also and it was completely checked for it.
     * No need to duplicate these checks for Ejb project.
     * One just need to be sure that all required categories present.  
     */
    public void testCategoriesPresence(){
        Category root = getCategorization().getRoot();
        boolean persistence = false;
        boolean hibernate = false;
        boolean jdbc = false;
        boolean statements = false;
        boolean jpa = false;
        boolean connection = false;
        
        Stack<Category> stack = new Stack<Category>();
        stack.push( root);
        while ( !stack.isEmpty() ){
            Category current = stack.pop();
            String label = current.getLabel();
            if ( label.equals("Hibernate")){
                hibernate = true;
            }
            else if ( label.equals("JDBC")){
                jdbc = true;
            }
            else if (label.equals("Persistence")){
                persistence = true;
            }
            else if (label.equals("Connection")){
                connection = true;
            }
            else if (label.equals("JPA")){ 
                jpa = true;
            }
            else if (label.equals("Statements")){
                statements = true;
            }
            for (Category category : current.getSubcategories()) {
                stack.push(category);
            }
        }
        assertTrue(persistence);
        assertTrue(hibernate);
        assertTrue(jdbc);
        assertTrue(connection);
        assertTrue(jpa);
        assertTrue(statements);
    }

    @Override
    protected String getProjectName() {
        return EjbMarkTest.APP_NAME;
    }
}
