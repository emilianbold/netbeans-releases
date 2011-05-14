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
public class EjbFlatProfileTest extends FlatProfileTestBase {

    public EjbFlatProfileTest( String name ) {
        super(name);
    }
    
    public void testLifeCycle(){
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
        
        status.updateInstrMethodsInfo("pack.NewMessageBean", 0,
                "onMessage", "(Ljavax/jms/Message;)V");

        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add(2);
        builder.methodExit( 2,0,3,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0,
                "method", "()V");

        builder.methodEntry( 3 , 0, 1, 0, 0);
        
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
        
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        Category ejbContainer = getCategory("Enterprise Beans");
        Category lifeCycle = findCategory(ejbContainer, "Lifecycle");

        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( ejbContainer.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Enterprise Beans category",
                markedIds.size() ,  flatProfile.getNRows());
        
        drillDown.drilldown( lifeCycle.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Lifecycle category",
                markedIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Lifecycle", markedIds);
    }
    
    public void testPersistence(){
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
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0,
                "method", "()V");

        builder.methodEntry( 2 , 0, 1, 0, 0);
        
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

        Category ejbContainer = getCategory("Enterprise Beans");
        Category persistence = findCategory(ejbContainer, "Persistence");

        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( ejbContainer.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Enterprise Beans category",
                markedIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Enterprise Beans", markedIds);
        
        drillDown.drilldown( persistence.getId());
        
        flatProfileBuilder.cctEstablished( root , false );

        assertEquals(markedIds.size()+" methods expected in Persistence category",
                markedIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Persistence", markedIds);
    }
    
    public void testEjbContainer(){
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
        
        status.updateInstrMethodsInfo("pack.NewMessageBean", 0,
                "wait", "()V");

        builder.methodEntry( 2 , 0, 3, 0, 0);
        markedIds.add(2);
        
        builder.methodExit( 2,0,1,0,0);
        
        status.updateInstrMethodsInfo("pack.CustomClass", 0,
                "method", "()V");

        builder.methodEntry( 3 , 0, 1, 0, 0);
        
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
        
        status.updateInstrMethodsInfo("pack.StatefulBean", 0,
                "method", "()V");

        builder.methodEntry( 7 , 1, 3, 0, 0);
        markedIds.add(7);
        
        
        status.updateInstrMethodsInfo("pack.CustomClass3", 0,
                "method", "()V");

        builder.methodEntry( 8 , 1, 1, 0, 0);
        markedIds.add(8);
        
        SimpleCPUCCTNode root = (SimpleCPUCCTNode)builder.getAppRootNode();

        Category ejbContainer = getCategory("Enterprise Beans");

        DrillDown drillDown = (DrillDown)factory.getEvaluators().iterator().next();

        drillDown.drilldown( ejbContainer.getId());
        
        flatProfileBuilder.cctEstablished( root , false );
        FlatProfileContainer flatProfile = flatProfileBuilder.createFlatProfile();

        assertEquals(markedIds.size()+" methods expected in Enterprise Beans category",
                markedIds.size() ,  flatProfile.getNRows());
        checkCategory(status, flatProfile, "Enterprise Beans", markedIds);
        
    }
    
    @Override
    protected String getProjectName() {
        return EjbMarkTest.APP_NAME;
    }

}
