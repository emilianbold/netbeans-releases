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

package org.netbeans.modules.profiler.categorization.ui;

import org.netbeans.lib.profiler.results.cpu.TimingAdjusterOld;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.utils.StringUtils;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor;
import org.netbeans.modules.profiler.categorization.api.ProjectCategorization;
import org.netbeans.modules.profiler.categorization.api.Category;
import org.netbeans.modules.profiler.categorization.api.ProjectAwareStatisticalModule;
import org.netbeans.modules.profiler.utilities.Visitable;
import org.netbeans.modules.profiler.utilities.Visitor;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;


/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "ForwardCategoryDistributionPanel_NoMethodLabelText=No method selected",
    "ForwardCategoryDistributionPanel_NoDataLabelText=No data available",
    "ForwardCategoryDistributionPanel_MethodCategoriesString=Method categories",
    "ForwardCategoryDistributionPanel_DescrString=Shows a per category distribution of the time spent in the selected method"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule.class)
public class ForwardCategoryDistributionPanel extends ProjectAwareStatisticalModule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class MarkTime {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        public static final Comparator COMPARATOR = new Comparator() {
            public int compare(Object o1, Object o2) {
                if ((o1 == null) || (o2 == null)) {
                    return 0;
                }

                if (!(o1 instanceof MarkTime && o2 instanceof MarkTime)) {
                    return 0;
                }

                if (((MarkTime) o1).time < ((MarkTime) o2).time) {
                    return 1;
                } else if (((MarkTime) o1).time > ((MarkTime) o2).time) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };


        //~ Instance fields ------------------------------------------------------------------------------------------------------

        public Mark mark;
        public long time;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public MarkTime(final Mark mark, final long time) {
            this.mark = mark;
            this.time = time;
        }
    }

    private class Model extends RuntimeCCTNodeProcessor.PluginAdapter {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Map<Mark, Long> markMap = new HashMap<Mark, Long>();
        private Mark usedMark;
        private Stack<Mark> markStack = new Stack<Mark>();
        private int inCalls;
        private int lastCalls;
        private int outCalls;
        private long time0;
        private long time1;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Map<Mark, Long> getDistribution() {
            return new HashMap<Mark, Long>(markMap);
        }

        @Override
        public void onBackout(MarkedCPUCCTNode node) {                        
            if (time0 > 0L && isMarkEligible(usedMark)) {
                // fill the timing data structures
                Long markTime = markMap.get(usedMark);

                if (markTime == null) {
                    markTime = Long.valueOf(0L);
                }

                long cleansedTime = (long) TimingAdjusterOld.getDefault().adjustTime(time0, inCalls, outCalls - lastCalls, false);

                if (cleansedTime > 0L || inCalls > 0) {
                    markMap.put(usedMark, markTime + (cleansedTime > 0L ? cleansedTime : inCalls));
                }
            }

            // clean up the timing helpers
            outCalls = 0;
            inCalls = 0;
            lastCalls = 0;
            time0 = 0;
            time1 = 0;

            usedMark = markStack.pop();
        }

        @Override
        protected void onNode(MarkedCPUCCTNode node) {                        
            if (time0 > 0L && isMarkEligible(usedMark)) {
                // fill the timing data structures
                Long markTime = markMap.get(usedMark);

                if (markTime == null) {
                    markTime = Long.valueOf(0L);
                }

                long cleansedTime = (long) TimingAdjusterOld.getDefault().adjustTime(time0, inCalls - lastCalls, outCalls, false);

                if (cleansedTime > 0L || inCalls > 0) {
                    markMap.put(usedMark, markTime + (cleansedTime > 0L ? cleansedTime : inCalls));
                }
            }

            // clean up the timing helpers
            outCalls = 0;
            inCalls = 0;
            lastCalls = 0;
            time0 = 0;
            time1 = 0;

            markStack.push(usedMark);
            usedMark = node.getMark();
        }
        
        

        @Override
        public void onNode(MethodCPUCCTNode node) {
            if (node.getMethodId() != getSelectedMethodId()) {
                return;
            }

            time0 += node.getNetTime0();
            time1 += node.getNetTime1();
            inCalls += node.getNCalls();
            outCalls += node.getNCalls();
            lastCalls = node.getNCalls();
        }

        @Override
        public void onStart() {
            markStack.clear();
            markMap.clear();

            usedMark = Mark.DEFAULT;
        }

        @Override
        public void onStop() {
            refreshData();
        }
        
        private boolean isMarkEligible(Mark mark) {
            return forwardMarks.contains(mark != null ? mark : Mark.DEFAULT);
        }
    }

    private static final Logger LOG = Logger.getLogger(ForwardCategoryDistributionPanel.class.getName());
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private JLabel noData = new JLabel(Bundle.ForwardCategoryDistributionPanel_NoDataLabelText());
    private JLabel noMethods = new JLabel(Bundle.ForwardCategoryDistributionPanel_NoMethodLabelText());
    private Model model;
    private RuntimeCPUCCTNode lastAppNode;
    
    private ProjectCategorization categorization;
    private List<MarkTime> slots = Collections.EMPTY_LIST;
    private Map<Mark, Integer> slotMap = Collections.EMPTY_MAP;
    private Set<Mark> forwardMarks = Collections.EMPTY_SET;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of ForwardCategoryDistributionPanel */
    public ForwardCategoryDistributionPanel() {
        initComponents();
        model = new Model();
    }

    @Override
    protected void onProjectChange(Provider oldValue, Provider newValue) {
        if (oldValue != null && newValue == null) {
            categorization = null;
            return;
        }
        if (newValue != null) {
            categorization = new ProjectCategorization(newValue);
            setupSlots(categorization.getRoot().getAssignedMark());
        }
    }
    
    @Override
    public boolean supportsProject(Lookup.Provider project) {
        return ProjectCategorization.isAvailable(project);
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onMethodSelectionChange(int oldMethodId, int newMethodId) {
        refresh(lastAppNode);
    }

    @Override
    protected void onMarkSelectionChange(Mark oldMark, Mark newMark) {
        refresh(lastAppNode);
    }
    
    private void setupSlots(Mark catMark) {
        if (categorization == null) return;
        
        Category cat = categorization.getCategoryForMark(catMark);
        
        Set<Category> subs = cat.getSubcategories();
        List<Category> subsList = new ArrayList<Category>(subs);
        
        slots = new ArrayList(subsList.size());
        slotMap = new HashMap<Mark, Integer>();
        forwardMarks = new HashSet<Mark>();
        
        for(int i=0;i<subsList.size();i++) {
            Category thisCat = subsList.get(i);
            Set<Mark> marks = getMarks(thisCat);
            for(Mark m : marks) {
                slotMap.put(m, i);
            }
            Mark assignedMark = thisCat.getAssignedMark();
            slots.add(new MarkTime(assignedMark, 0));
            forwardMarks.addAll(marks);
        }
        forwardMarks.add(catMark);
    }
    
    private Set<Mark> getMarks(Category cat) {
        Set<Mark> marks = new HashSet<Mark>();
        
        Deque<Category> stack = new ArrayDeque<Category>();
        stack.push(cat);
        while (!stack.isEmpty()) {
            cat = stack.pop();
            marks.add(cat.getAssignedMark());
            for(Category sub : cat.getSubcategories()) {
                stack.push(sub);
            }
        }
        
        return marks;
    }

    synchronized public void refresh(RuntimeCPUCCTNode appNode) {
        if (appNode != null) {
            try {
                setupSlots(getSelectedMark());
                RuntimeCCTNodeProcessor.process(appNode, model);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            } finally {
                lastAppNode = appNode;
            }
        }
    }

    private void initComponents() {
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        noMethods.setOpaque(false);
        noMethods.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0), noMethods.getBorder()));
        noData.setOpaque(false);
        noData.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0), noData.getBorder()));

        add(noMethods);
        setName(Bundle.ForwardCategoryDistributionPanel_MethodCategoriesString());
        setToolTipText(Bundle.ForwardCategoryDistributionPanel_DescrString());

        //    setPreferredSize(new Dimension(60, 10));
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
            Map<Mark, Long> catTimes = model.getDistribution();

            if ((catTimes == null) || catTimes.isEmpty()) {
                uiUpdater = new Runnable() {
                        public void run() {
                            removeAll();
                            add(noData);
                            revalidate();
                            repaint();
                        }
                    };
            } else {
                final long[] shownTime = new long[1];
                
                for (Map.Entry<Mark, Long> entry : catTimes.entrySet()) {
                    long time = entry.getValue();
                    Integer index = slotMap.get(entry.getKey());
                    if (index != null) {
                        MarkTime mt = slots.get(index);
                        assert mt != null;
                        
                        mt.time += time;                        
                    }
                }

                final List<MarkTime> shownCats = new ArrayList<MarkTime>();
                for(MarkTime mt : slots) {
                    if (mt.time > 0L) {
                        shownCats.add(mt);
                        shownTime[0] += mt.time;
                    }
                }
                
                if (shownCats.isEmpty()) {
                    shownCats.add(new MarkTime(getSelectedMark(), 1));
                    shownTime[0] = 1;
                } else {
                    Collections.sort(shownCats, MarkTime.COMPARATOR);
                }

                uiUpdater = new Runnable() {
                        public void run() {                            
                            removeAll();

                            for (final MarkTime cat : shownCats) {
                                float ratio = (float) cat.time / (float) shownTime[0];
                                float percent = 100f * ratio;

                                JPanel panel = new JPanel(new BorderLayout());
                                panel.setOpaque(false);

                                Category displayedCat = categorization.getCategoryForMark(cat.mark);
                                StringBuilder labelBuilder = new StringBuilder();
                                if (displayedCat != null) {
                                    labelBuilder.append(displayedCat.getLabel());
                                } else {
                                    labelBuilder.append("Not categorized");
                                }
                                JLabel data = new JLabel(labelBuilder.toString() + " (" + StringUtils.floatPerCentToString(percent)
                                                         + "%)"); // NOI18N
                                data.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0),
                                                                                  data.getBorder()));
                                data.setOpaque(false);
                                panel.add(data, BorderLayout.WEST);

                                JProgressBar prgbar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
                                prgbar.setOpaque(false);
                                prgbar.setPreferredSize(new Dimension(120, data.getPreferredSize().height + 2));
                                prgbar.setMaximumSize(prgbar.getPreferredSize());
                                prgbar.setMinimumSize(prgbar.getPreferredSize());
                                prgbar.setForeground(new Color(Color.HSBtoRGB(100f, ratio, 0.7f)));
                                prgbar.setString(""); // NOI18N
                                prgbar.setStringPainted(true);
                                prgbar.setValue((int) percent);

                                JPanel prgbarContainer = new JPanel(new FlowLayout(0, 2, FlowLayout.LEADING));
                                prgbarContainer.setOpaque(false);
                                prgbarContainer.add(prgbar);
                                panel.add(prgbarContainer, BorderLayout.EAST);
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
