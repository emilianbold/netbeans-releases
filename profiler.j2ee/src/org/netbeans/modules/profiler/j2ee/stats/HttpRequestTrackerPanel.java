/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.profiler.j2ee.stats;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.results.cpu.cct.CPUCCTVisitorAdapter;
import org.netbeans.lib.profiler.results.cpu.cct.CompositeCPUCCTWalker;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode;
import org.netbeans.modules.profiler.j2ee.WebProjectUtils;
import org.netbeans.modules.profiler.ui.stats.ProjectAwareStatisticalModule;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 *
 * @author Jaroslav Bachorik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.ui.stats.ProjectAwareStatisticalModule.class)
public class HttpRequestTrackerPanel extends ProjectAwareStatisticalModule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class HttpRequestTrackerModel extends CPUCCTVisitorAdapter {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final Set<String> EMPTY_SET = new HashSet<String>();
        private ReadWriteLock lock = new ReentrantReadWriteLock();
        private ServletRequestCPUCCTNode usedRequest = null;
        private Set<String> paths = new HashSet<String>();
        private Stack<ServletRequestCPUCCTNode> servletStack = new Stack();
        private int inCalls;
        private int lastCalls;
        private int outCalls;
        private long time0;
        private long time1;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public HttpRequestTrackerModel() {
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Set<String> getPaths(int methodId) {
            lock.readLock().lock();

            try {
                return (paths != null) ? new HashSet<String>(paths) : EMPTY_SET;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void afterWalk() {
            servletStack.clear();
            lock.writeLock().unlock();
            refreshData();
        }

        public void beforeWalk() {
            lock.writeLock().lock();
            servletStack.clear();
            paths.clear();
        }

        public void visit(MethodCPUCCTNode node) {
            boolean first = false;

            if (usedRequest == null) {
                if (node.getMethodId() != getSelectedMethodId()) {
                    return;
                }

                usedRequest = servletStack.isEmpty() ? null : servletStack.peek();
                first = true;
            }

            if (usedRequest == null) {
                return;
            }

            time0 += node.getNetTime0();
            time1 += node.getNetTime1();

            if (!first) {
                inCalls += node.getNCalls();
            }

            outCalls += node.getNCalls();
            lastCalls = node.getNCalls();
        }

        public void visit(ServletRequestCPUCCTNode node) {
            servletStack.push(node);
        }

        public void visitPost(ServletRequestCPUCCTNode node) {
            servletStack.pop();

            if ((usedRequest != null) && usedRequest.equals(node)) {
                paths.add(usedRequest.getServletPath());

                // clean up the timing helpers
                outCalls = 0;
                inCalls = 0;
                lastCalls = 0;
                time0 = 0;
                time1 = 0;

                // unset the used request
                usedRequest = null;
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String NO_METHOD_STRING = NbBundle.getMessage(HttpRequestTrackerPanel.class,
                                                                       "HttpRequestTrackerPanel_NoMethodString"); // NOI18N
    private static final String NO_DATA_STRING = NbBundle.getMessage(HttpRequestTrackerPanel.class,
                                                                     "HttpRequestTrackerPanel_NoDataString"); // NOI18N
    private static final String REQUEST_TRACKER_STRING = NbBundle.getMessage(HttpRequestTrackerPanel.class,
                                                                             "HttpRequestTrackerPanel_RequestTrackerString"); // NOI18N
    private static final String REQUEST_TRACKER_DESCR = NbBundle.getMessage(HttpRequestTrackerPanel.class,
                                                                            "HttpRequestTrackerPanel_RequestTrackerDescr"); // NOI18N
                                                                                                                            // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CompositeCPUCCTWalker treeWalker;
    private HttpRequestTrackerModel model;
    private JLabel noData = new JLabel(NO_DATA_STRING);
    private JLabel noMethods = new JLabel(NO_METHOD_STRING);
    private RuntimeCPUCCTNode lastAppNode;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of HttpRequestTrackerPanel */
    public HttpRequestTrackerPanel() {
        initComponents();
        model = new HttpRequestTrackerModel();
        treeWalker = new CompositeCPUCCTWalker();
        treeWalker.add(0, model);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setSelectedMethodId(int methodId) {
        int lastSelectedId = getSelectedMethodId();
        super.setSelectedMethodId(methodId);

        if (lastSelectedId != methodId) {
            lastSelectedId = methodId;
            refresh(lastAppNode);
        }
    }

    public void refresh(RuntimeCPUCCTNode appNode) {
        if (appNode == null) {
            return;
        }

        if (model != null) {
            appNode.accept(treeWalker);
            lastAppNode = appNode;
        }
    }

    public boolean supportsProject(Project project) {
        if (isWebProject(project)) {
            System.setProperty("org.netbeans.lib.profiler.servletTracking", "true"); // NOI18N

            return true;
        } else {
            System.setProperty("org.netbeans.lib.profiler.servletTracking", "false"); // NOI18N

            return false;
        }
    }

    private boolean isWebProject(Project project) {
        if (project == null) {
            return false;
        }

        return WebProjectUtils.getDeploymentDescriptorFileObjects(project, true) != null;
    }

    private void initComponents() {
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setName(REQUEST_TRACKER_STRING);
        setToolTipText(REQUEST_TRACKER_DESCR);

        noMethods.setOpaque(false);
        noMethods.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0), noMethods.getBorder()));
        noData.setOpaque(false);
        noData.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0), noData.getBorder()));

        add(noMethods);
    }

    private void refreshData() {
        if (model == null) {
            return;
        }

        Runnable uiUpdater = null;

        if (getSelectedMethodId() == -1) {
            uiUpdater = new Runnable() {
                    public void run() {
                        removeAll();
                        add(noMethods);
                        revalidate();
                        repaint();
                    }
                };
        } else {
            final Set<String> paths = model.getPaths(getSelectedMethodId());

            if ((paths == null) || paths.isEmpty()) {
                uiUpdater = new Runnable() {
                        public void run() {
                            removeAll();
                            add(noData);
                            revalidate();
                            repaint();
                        }
                    };
            } else {
                uiUpdater = new Runnable() {
                        public void run() {
                            removeAll();

                            for (String path : paths) {
                                JPanel panel = new JPanel(new BorderLayout());
                                panel.setOpaque(false);

                                JLabel data = new JLabel(path);
                                data.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0),
                                                                                  data.getBorder()));
                                data.setOpaque(false);
                                panel.add(data, BorderLayout.WEST);
                                add(panel);
                            }

                            revalidate();
                            repaint();
                        }
                    };
            }
        }

        if (EventQueue.isDispatchThread()) {
            uiUpdater.run();
        } else {
            EventQueue.invokeLater(uiUpdater);
        }
    }
}
