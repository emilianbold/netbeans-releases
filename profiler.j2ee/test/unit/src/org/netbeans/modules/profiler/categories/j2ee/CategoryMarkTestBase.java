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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode.Children;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;
import org.netbeans.modules.profiler.categories.Category;


/**
 * @author ads
 *
 */
public class CategoryMarkTestBase extends TestBase {

    public CategoryMarkTestBase( String name ) {
        super(name);
    }
    
    protected void doTestCodec( Category category ){
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
    }
    
    protected void doTestEndpointInvocation( Category category ){
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
    }
    
    protected void doTestHttpTransport( Category category ){
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
    }
    
    protected void doTestMessageProcessing( Category category ){
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
    }
    
    protected void doTestStreaming(Category category ){
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
    }
    
    protected void checkMarks( ProfilingSessionStatus status, Set<Integer> ids,
            Set<Integer> plainIds, Map<Integer, Mark> methodMarks,
            Mark requestedMark, Category category )
    {
        checkMarks(status, ids, plainIds, methodMarks, requestedMark, 
                category.getLabel());
    }
    
    protected void checkMarks( ProfilingSessionStatus status, Set<Integer> ids,
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
    
    protected Map<Integer, Mark> getMethodMarks( SimpleCPUCCTNode root ){
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
    
}
