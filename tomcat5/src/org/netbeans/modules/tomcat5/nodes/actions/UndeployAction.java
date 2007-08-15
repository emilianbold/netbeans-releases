/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.tomcat5.nodes.TomcatWebModule;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public class UndeployAction extends NodeAction {

    /** Creates a new instance of Undeploy */
    public UndeployAction() {
    }


    public String getName() {
        return NbBundle.getMessage(UndeployAction.class, "LBL_UndeployAction"); //NOI18N
    }

    protected void performAction(Node[] nodes) {
        NodeRefreshTask refresh = new NodeRefreshTask(RequestProcessor.getDefault());
        for (int i=0; i<nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie) nodes[i].getCookie(TomcatWebModuleCookie.class);
            if (cookie != null) {
                Task task = cookie.undeploy();

                refresh.addPrerequisity(nodes[i].getParentNode(), task);
            }
        }

        RequestProcessor.getDefault().post(refresh);
    }

    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatWebModule module = (TomcatWebModule) nodes[i].getLookup().lookup(TomcatWebModule.class);
            if (module != null) {
                // it should not be allowed to undeploy the /manager application
                if ("/manager".equals(module.getTomcatModule().getPath())) { // NOI18N
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Helper class supporting the node(s) refresh after the set of prerequisity
     * tasks is finished.
     * <p>
     * Class itself is <i>thread safe</i> (uses intrinsic lock). Refresh
     * itself is performed from dedicated thread so, the refresh must be
     * implemented in thread safe way.
     *
     * @author Petr Hejl
     */
    private static class NodeRefreshTask implements Runnable {

        private final RequestProcessor requestProcessor;

        private Map<Node, Set<Task>> taskMap = new HashMap<Node, Set<Task>>();

        /**
         * Constructs the NodeRefreshTask using the given RequestProcessor.
         *
         * @param requestProcessor will be used for scheduling the refresh tasks
         */
        public NodeRefreshTask(RequestProcessor requestProcessor) {
            Parameters.notNull("requestProcessor", taskMap);

            this.requestProcessor = requestProcessor;
        }

        /**
         * Adds prerequisity task. Defines that the node should be refreshed
         * after the task (and all already added tasks) is finished.
         *
         * @param node node to refresh when the task is finished
         * @param task task to wait for (multiple task can be assigned) by calling this method
         */
        public synchronized void addPrerequisity(Node node, Task task) {
            Parameters.notNull("node", node);
            Parameters.notNull("task", task);

            Set<Task> tasks = taskMap.get(node);
            if (tasks == null) {
                tasks = new HashSet<Task>();
                taskMap.put(node, tasks);
            }

            tasks.add(task);
        }

        /**
         * Executes this task. For each node added with {@link #addPrerequisity(Node, Task)}
         * it post a new task that waits until all tasks asscociated with the node
         * are finished and after that refreshes the node.
         */
        public synchronized void run() {
            for (Map.Entry<Node, Set<Task>> entry : taskMap.entrySet()) {

                final Node node = entry.getKey();
                final Set<Task> tasks = entry.getValue();

                requestProcessor.post(new Runnable() {
                    public void run() {
                        for (Task task : tasks) {
                            task.waitFinished();
                        }
                        NodeRefreshTask.this.refresh(node);
                    }
                });
            }
        }

        private void refresh(Node node) {
            if (node == null) {
                return;
            }

            RefreshWebModulesCookie cookie = node.getLookup().lookup(RefreshWebModulesCookie.class);
            if (cookie != null) {
                cookie.refresh();
            }
        }

    }


}
