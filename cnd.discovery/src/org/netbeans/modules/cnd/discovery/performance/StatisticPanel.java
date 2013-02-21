/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.performance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author alsimon
 */
public class StatisticPanel extends JPanel {
    private static final RequestProcessor RP = new RequestProcessor("statistic", 1); //NOI18N
    private final PerformanceIssueDetector activeInstance;
    private final RequestProcessor.Task update;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Creates new form StatisticPanel
     */
    public StatisticPanel() {
        initComponents();
        activeInstance = PerformanceIssueDetector.getActiveInstance();
        update = RP.post(new Runnable() {
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    countStatistic();
                } else {
                    SwingUtilities.invokeLater(this);
                }
            }
        });
        addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
                    if (!e.getChanged().isVisible()){
                        closed.set(true);
                    }
                }
            }
        });
    }

    private void countStatistic() {
        if (closed.get()) {
            return;
        }
        if (activeInstance == null) {
            return;
        }
        TreeMap<String, AnalyzeStat.AgregatedStat> statistic = activeInstance.getStatistic();
        if (statistic != null) {
            {
                long count = 0;
                long time = 0;
                long cpu = 0;
                for (Map.Entry<String, AnalyzeStat.AgregatedStat> entry : statistic.entrySet()) {
                    count += entry.getValue().itemNumber;
                    time += entry.getValue().itemTime;
                    cpu += entry.getValue().itemCPU;
                }
                if (time > 0) {
                    long speed = (count * PerformanceIssueDetector.NANO_TO_SEC) / time;
                    itemSpeed.setText(PerformanceIssueDetector.format(speed));
                    getLimit(itemSpeedPanel, PerformanceIssueDetector.CREATION_SPEED_LIMIT, PerformanceIssueDetector.CREATION_SPEED_LIMIT*10, (int)speed);
                    itemNumber.setText(PerformanceIssueDetector.format(count));
                    itemWallTime.setText(PerformanceIssueDetector.format(time/PerformanceIssueDetector.NANO_TO_MILLI));
                    itemCpuTime.setText(PerformanceIssueDetector.format(cpu/PerformanceIssueDetector.NANO_TO_MILLI));
                    itemRatio.setText(PerformanceIssueDetector.format(cpu*100/time));
                }
            }
            {
                long count = 0;
                long time = 0;
                long cpu = 0;
                for (Map.Entry<String, AnalyzeStat.AgregatedStat> entry : statistic.entrySet()) {
                    count += entry.getValue().readBytes;
                    time += entry.getValue().readTime;
                    cpu += entry.getValue().readCPU;
                }
                if (time > 0) {
                    long speed = (count * PerformanceIssueDetector.NANO_TO_SEC) / time / 1024;
                    readSpeed.setText(PerformanceIssueDetector.format(speed));
                    getLimit(readSpeedPanel, PerformanceIssueDetector.READING_SPEED_LIMIT, PerformanceIssueDetector.READING_SPEED_LIMIT*10, (int)speed);
                    readNumber.setText(PerformanceIssueDetector.format(count / 1024));
                    readWallTime.setText(PerformanceIssueDetector.format(time/PerformanceIssueDetector.NANO_TO_MILLI));
                    readCpuTime.setText(PerformanceIssueDetector.format(cpu/PerformanceIssueDetector.NANO_TO_MILLI));
                    readRatio.setText(PerformanceIssueDetector.format(cpu*100/time));
                }
            }
            {
                long count = 0;
                long time = 0;
                long cpu = 0;
                for (Map.Entry<String, AnalyzeStat.AgregatedStat> entry : statistic.entrySet()) {
                    count += entry.getValue().parseLines;
                    time += entry.getValue().parseTime;
                    cpu += entry.getValue().parseCPU;
                }
                if (time > 0) {
                    //PerformanceIssueDetector.PARSING_SPEED_LIMIT = 1000;
                    //PerformanceIssueDetector.PARSING_RATIO_LIMIT = 5;
                    long speed = (count * PerformanceIssueDetector.NANO_TO_SEC) / time;
                    parsingSpeed.setText(PerformanceIssueDetector.format(speed));
                    getLimit(parsingSpeedPanel,PerformanceIssueDetector.PARSING_SPEED_LIMIT, PerformanceIssueDetector.PARSING_SPEED_LIMIT*10, (int)speed);
                    parsingLines.setText(PerformanceIssueDetector.format(count));
                    parsingWallTime.setText(PerformanceIssueDetector.format(time/PerformanceIssueDetector.NANO_TO_MILLI));
                    parsingCpuTime.setText(PerformanceIssueDetector.format(cpu/PerformanceIssueDetector.NANO_TO_MILLI));
                    parsingRatio.setText(PerformanceIssueDetector.format(cpu*100/time));
                    getLimit(parsingRatioPanel, 100/PerformanceIssueDetector.PARSING_RATIO_LIMIT, 100/2, (int)(cpu*100/time));
                }
            }
        }
        update.schedule(2000);
    }
    
    private JPanel getLimit(JPanel parent,int low, int normal, int fact) {
        parent.removeAll();
        JPanel panel = new MyPanel(low, normal, fact);
        parent.add(panel, BorderLayout.CENTER);
        parent.validate();
        parent.repaint();
        return panel;
    }

    private static final class MyPanel extends JPanel {
        private static final int MY_WIDTH = 200;
        private static final int MY_HEIGHT= 20;
        private static final int DARK_COLOR = 160;
        private static final int NORMAL_COLOR = 192;
        private static final int BRIGHT_COLOR = 224;
        private final int low;
        private final int normal;
        private final int fact;
        private MyPanel(int low, int normal, int fact) {
            this.low = low;
            this.normal = normal;
            this.fact = fact;
        }

        @Override
        public Color getBackground() {
            return Color.WHITE;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(MY_WIDTH, MY_HEIGHT);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D graphics = (Graphics2D)g;
            graphics.drawRect(0, 0, MY_WIDTH, MY_HEIGHT);
            double m1 = Math.log(low);
            double m2 = Math.log(normal);
            double m3;
            if (fact == 0) {
                m3 = low - 2;
            } else {
                m3 = Math.log(fact);
            }
            double min = Math.min(Math.min(m1, m2),m3);
            double max = Math.max(Math.max(m1, m2),m3);
            double point = 150/(max - min);
            int m1x = (int)(25 + (m1 - min) * point);
            int m2x = (int)(25 + (m2 - min) * point);
            int m3x = (int)(25 + (m3 - min) * point);
            Color def = graphics.getColor();
            
            float[] fractions = new float[]{0f, 1f};  
            Color[] colors = new Color[]{new Color(BRIGHT_COLOR, 0, 0), new Color(NORMAL_COLOR, 0, 0)};  
            LinearGradientPaint gradient = new LinearGradientPaint(1, 1, m1x-1, 1, fractions, colors);  
            graphics.setPaint(gradient);
            graphics.fillRect(1, 1, m1x-1, MY_HEIGHT-1);

            fractions = new float[]{0f, 0.3f, 0.5f, 0.7f, 1f};
            colors = new Color[]{new Color(NORMAL_COLOR, 0, 0),
                                 new Color(NORMAL_COLOR, NORMAL_COLOR, 0),
                                 new Color(BRIGHT_COLOR, BRIGHT_COLOR, 0),
                                 new Color(NORMAL_COLOR, NORMAL_COLOR, 0),
                                 new Color(0, NORMAL_COLOR, 0)};  
            gradient = new LinearGradientPaint(m1x+1, 1, m2x-1 ,1, fractions, colors);  
            graphics.setPaint(gradient);
            graphics.fillRect(m1x+1, 1, m2x-m1x-1 ,MY_HEIGHT-1);

            fractions = new float[]{0f, 1f};
            colors = new Color[]{new Color(0, NORMAL_COLOR, 0), new Color(0, BRIGHT_COLOR, 0)};  
            gradient = new LinearGradientPaint(m2x+1, 1, MY_WIDTH-1 ,1, fractions, colors);  
            graphics.setPaint(gradient);
            graphics.fillRect(m2x+1, 1, MY_WIDTH-m2x-1 ,MY_HEIGHT-1);

            graphics.setColor(Color.blue);
            graphics.fillOval(m3x-MY_HEIGHT/4, MY_HEIGHT/4, MY_HEIGHT/2, MY_HEIGHT/2);
            graphics.setColor(def);

            graphics.drawLine(m1x, MY_HEIGHT-1, m1x, 1);
            String what = ""+low;
            int shift = graphics.getFontMetrics().getStringBounds(what, g).getBounds().width/2;
            graphics.drawString(what, m1x-shift, MY_HEIGHT -5);
            
            graphics.drawLine(m2x, MY_HEIGHT-1, m2x, 1);
            what = ""+normal;
            shift = graphics.getFontMetrics().getStringBounds(what, g).getBounds().width/2;
            graphics.drawString(what, m2x-shift, MY_HEIGHT -5);

        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        itemLabel = new javax.swing.JLabel();
        itemSpeedLabel = new javax.swing.JLabel();
        itemSpeed = new javax.swing.JTextField();
        itemNumberLabel = new javax.swing.JLabel();
        itemNumber = new javax.swing.JTextField();
        itemWallTimeLabel = new javax.swing.JLabel();
        itemWallTime = new javax.swing.JTextField();
        itemCpuTimeLabel = new javax.swing.JLabel();
        itemCpuTime = new javax.swing.JTextField();
        itemRatioLabel = new javax.swing.JLabel();
        itemRatio = new javax.swing.JTextField();
        readLabel = new javax.swing.JLabel();
        readSpeedLabel = new javax.swing.JLabel();
        readSpeed = new javax.swing.JTextField();
        readNumberLabel = new javax.swing.JLabel();
        readNumber = new javax.swing.JTextField();
        readWallTimeLabel = new javax.swing.JLabel();
        readWallTime = new javax.swing.JTextField();
        readCpuTimeLabel = new javax.swing.JLabel();
        readCpuTime = new javax.swing.JTextField();
        readRatioLabel = new javax.swing.JLabel();
        readRatio = new javax.swing.JTextField();
        parsingLabel = new javax.swing.JLabel();
        parsingSpeedLabel = new javax.swing.JLabel();
        parsingSpeed = new javax.swing.JTextField();
        parsingLinesLabel = new javax.swing.JLabel();
        parsingLines = new javax.swing.JTextField();
        parsingWallTimeLabel = new javax.swing.JLabel();
        parsingWallTime = new javax.swing.JTextField();
        parsingCpuTimeLabel = new javax.swing.JLabel();
        parsingCpuTime = new javax.swing.JTextField();
        parsingRatioLabel = new javax.swing.JLabel();
        parsingRatio = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        itemSpeedPanel = new javax.swing.JPanel();
        readSpeedPanel = new javax.swing.JPanel();
        parsingSpeedPanel = new javax.swing.JPanel();
        parsingRatioPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(500, 350));
        setPreferredSize(new java.awt.Dimension(700, 550));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(itemLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemSpeedLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemSpeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemSpeedLabel, gridBagConstraints);

        itemSpeed.setEditable(false);
        itemSpeed.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemSpeed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemNumberLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemNumberLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemNumberLabel, gridBagConstraints);

        itemNumber.setEditable(false);
        itemNumber.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemNumber, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemWallTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemWallTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemWallTimeLabel, gridBagConstraints);

        itemWallTime.setEditable(false);
        itemWallTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemWallTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemCpuTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemCpuTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemCpuTimeLabel, gridBagConstraints);

        itemCpuTime.setEditable(false);
        itemCpuTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemCpuTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemRatioLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemRatioLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemRatioLabel, gridBagConstraints);

        itemRatio.setEditable(false);
        itemRatio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemRatio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readSpeedLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readSpeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readSpeedLabel, gridBagConstraints);

        readSpeed.setEditable(false);
        readSpeed.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readSpeed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readNumberLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readNumberLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readNumberLabel, gridBagConstraints);

        readNumber.setEditable(false);
        readNumber.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readNumber, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readWallTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readWallTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readWallTimeLabel, gridBagConstraints);

        readWallTime.setEditable(false);
        readWallTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readWallTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readCpuTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readCpuTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readCpuTimeLabel, gridBagConstraints);

        readCpuTime.setEditable(false);
        readCpuTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readCpuTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readRatioLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readRatioLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readRatioLabel, gridBagConstraints);

        readRatio.setEditable(false);
        readRatio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readRatio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingSpeedLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingSpeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingSpeedLabel, gridBagConstraints);

        parsingSpeed.setEditable(false);
        parsingSpeed.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingSpeed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingLinesLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingLinesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingLinesLabel, gridBagConstraints);

        parsingLines.setEditable(false);
        parsingLines.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingLines, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingWallTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingWallTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingWallTimeLabel, gridBagConstraints);

        parsingWallTime.setEditable(false);
        parsingWallTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingWallTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingCpuTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingCpuTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingCpuTimeLabel, gridBagConstraints);

        parsingCpuTime.setEditable(false);
        parsingCpuTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingCpuTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingRatioLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingRatioLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingRatioLabel, gridBagConstraints);

        parsingRatio.setEditable(false);
        parsingRatio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingRatio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jSeparator3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        itemSpeedPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        itemSpeedPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        itemSpeedPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(itemSpeedPanel, gridBagConstraints);

        readSpeedPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        readSpeedPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        readSpeedPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(readSpeedPanel, gridBagConstraints);

        parsingSpeedPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        parsingSpeedPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        parsingSpeedPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(parsingSpeedPanel, gridBagConstraints);

        parsingRatioPanel.setInheritsPopupMenu(true);
        parsingRatioPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        parsingRatioPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        parsingRatioPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(parsingRatioPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField itemCpuTime;
    private javax.swing.JLabel itemCpuTimeLabel;
    private javax.swing.JLabel itemLabel;
    private javax.swing.JTextField itemNumber;
    private javax.swing.JLabel itemNumberLabel;
    private javax.swing.JTextField itemRatio;
    private javax.swing.JLabel itemRatioLabel;
    private javax.swing.JTextField itemSpeed;
    private javax.swing.JLabel itemSpeedLabel;
    private javax.swing.JPanel itemSpeedPanel;
    private javax.swing.JTextField itemWallTime;
    private javax.swing.JLabel itemWallTimeLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField parsingCpuTime;
    private javax.swing.JLabel parsingCpuTimeLabel;
    private javax.swing.JLabel parsingLabel;
    private javax.swing.JTextField parsingLines;
    private javax.swing.JLabel parsingLinesLabel;
    private javax.swing.JTextField parsingRatio;
    private javax.swing.JLabel parsingRatioLabel;
    private javax.swing.JPanel parsingRatioPanel;
    private javax.swing.JTextField parsingSpeed;
    private javax.swing.JLabel parsingSpeedLabel;
    private javax.swing.JPanel parsingSpeedPanel;
    private javax.swing.JTextField parsingWallTime;
    private javax.swing.JLabel parsingWallTimeLabel;
    private javax.swing.JTextField readCpuTime;
    private javax.swing.JLabel readCpuTimeLabel;
    private javax.swing.JLabel readLabel;
    private javax.swing.JTextField readNumber;
    private javax.swing.JLabel readNumberLabel;
    private javax.swing.JTextField readRatio;
    private javax.swing.JLabel readRatioLabel;
    private javax.swing.JTextField readSpeed;
    private javax.swing.JLabel readSpeedLabel;
    private javax.swing.JPanel readSpeedPanel;
    private javax.swing.JTextField readWallTime;
    private javax.swing.JLabel readWallTimeLabel;
    // End of variables declaration//GEN-END:variables

}
