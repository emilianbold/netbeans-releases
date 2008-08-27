/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.buildplan;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleLoaderException;
import org.apache.maven.lifecycle.LifecycleSpecificationException;
import org.apache.maven.lifecycle.plan.BuildPlan;
import org.apache.maven.lifecycle.plan.BuildPlanner;
import org.apache.maven.lifecycle.plan.LifecyclePlannerException;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.buildplan.ui.BuildPlanTopComponent;
import org.netbeans.modules.maven.buildplan.ui.BuildPlanViewUI;
import org.netbeans.modules.maven.embedder.exec.NBBuildPlanner;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Anuradha G
 */
public class BuildPlanView {
    private MavenEmbedder embedder;
    private MavenProject project;
    private String[] tasks;
    private BuildPlanViewUI bpvui;
    
    private final PriorityQueue<MavenProject> queue;
    private PriorityComparator comparator;
    private final HashMap<MavenProject, BuildPlanGroup> results;
    
    public BuildPlanView(MavenEmbedder embedder,MavenProject project, String... tasks) {
        this.embedder = embedder;
        this.project = project;
        this.tasks = tasks;
        comparator = new PriorityComparator();
        queue = new PriorityQueue<MavenProject>(10, comparator);
        results = new HashMap<MavenProject, BuildPlanGroup>();
        bpvui=new BuildPlanViewUI(this);
    }

    public void open() {
        BuildPlanTopComponent bptc = BuildPlanTopComponent.findInstance();
        
        bptc.addView( bpvui);
        bpvui.buildNodeView();
        bptc.open();
        bptc.requestActive();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                processProjects();
            }
        });
        
    }

//    public void refesh() {
//        bpvui.buildNodeView();
//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                processProjects();
//            }
//        });
//    }

//    public void close() {
//        //todo
//    }

    /**
     * embedder to load the lifecycle. currently the execute embedder is used.
     * @return
     */
    public MavenEmbedder getEmbedder() {
        return embedder;
    }
 
    /**
     * mavenproject instance as loaded by the project embedder.
     * @return
     */
    public MavenProject getProject() {
        return project;
    }
    
    /**
     * mavenproject instances as loaded by the project embedder.
     * TODO this list is wrong in case the executed list of projects differs.
     * eg. when a profile with <modules> section is activated.
     * @return
     */
    public List getCollectedProjects() {
        return project.getCollectedProjects();
    }

    public String[] getTasks() {
        return tasks;
    }
    

    public BuildPlanGroup retrieveBuildPlanGroup(MavenProject project) {
        boolean wait = false;
        synchronized (queue) {
            if (queue.contains(project)) {
                comparator.increasePriority(project);
                wait = true;
            }
        }
        if (wait) {
            assert !SwingUtilities.isEventDispatchThread();
            try {
                synchronized (project) {
                    project.wait();
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        BuildPlanGroup grp;
        synchronized (results) {
            grp = results.get(project);
        }
        assert grp != null : " need result for " + project.getId();
        return grp;
        
    }
    
    private void processProjects() {
        synchronized (queue) {
            queue.add(getProject());
            queue.addAll(getCollectedProjects());
        }
        synchronized (results) {
            results.clear();
        }
        AggregateProgressHandle handle = AggregateProgressFactory.createSystemHandle("Constructing Build Plan", new ProgressContributor[0], null, null);
        handle.setInitialDelay(2000);
        handle.start();
        
        ProgressTransferListener.setAggregateHandle(handle);
        try {
            MavenProject prj;
            synchronized(queue) {
                prj = queue.peek();
            }
            while (prj != null) {
                //TODO add progress contributor for each project..
                
                NBBuildPlanner buildPlanner = (NBBuildPlanner) getEmbedder().getPlexusContainer().lookup(BuildPlanner.class);
                MavenSession session = buildPlanner.getMavenSession();
                if (session == null) {
                    continue;
                }
                List<String> list = Arrays.asList(getTasks());

                BuildPlan buildPlan = buildPlanner.constructBuildPlan(list, prj, session, false); //mkleint: what does the boolean param actually do?

                BuildPlanGroup bpg = BuildPlanUtil.getMojoBindingsGroupByPhase(buildPlan);
                
                synchronized (results) {
                    results.put(prj, bpg);
                }
                synchronized (prj) {
                    prj.notifyAll();
                }
                
                synchronized (queue) {
                    comparator.remove(prj);
                    //TODO, how to ensure re-sorting of the queue?
                    queue.poll();
                    //need to use peek+poll so that the retrieve... method correctly waits until the 
                    // result is put into the map
                    prj = queue.peek();
                }
            }
        } catch (LifecycleLoaderException ex) {
            Exceptions.printStackTrace(ex);
        } catch (LifecyclePlannerException ex) {
            Exceptions.printStackTrace(ex);
        } catch (LifecycleSpecificationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            ProgressTransferListener.clearAggregateHandle();
            handle.finish();
        }

    }



    private class PriorityComparator implements Comparator<MavenProject>  {
        
        Set<MavenProject> prioritized = new HashSet<MavenProject>();

        public synchronized int compare(MavenProject o1, MavenProject o2) {
            boolean p1 = prioritized.contains(o1);
            boolean p2 = prioritized.contains(o2);
            if (p1 && !p2) {
                return -1;
            }
            if (!p1 && p2) {
                return 1;
            }
            return 0;
        }

        private synchronized void increasePriority(MavenProject project) {
            prioritized.add(project);
        }
        
        private synchronized void remove(MavenProject project) {
            prioritized.remove(project);
        }
    }
        
}
