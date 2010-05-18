/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.dlight.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;

/**
 * Manual test for PercentageGraph class
 * @author Vladimir Kvashin
 */
public class TestGraph {

    private static final int INITIAL_HEIGHT = 18;
    private static final int MAX_DATA = 100;

    public static void main(String[] args) {
        new TestGraph(64, INITIAL_HEIGHT).testPercentageGraph_1();
    }

    private int[] currData;
    private boolean ascending = true;
    private final int initialWidth;
    private final int initialHeight;

    public TestGraph(int width, int height) {
        this.initialWidth = width;
        this.initialHeight = height;
    }

    private void testPercentageGraph_1() {

        final JDialog dlg = new JDialog();
        dlg.getRootPane().setLayout(new BorderLayout());

        JButton event = new JButton("Event");
        JButton measure = new JButton("Measure");
        
        final JCheckBox byTimer = new JCheckBox("Timer");
        final JTextField freq = new JTextField("  20");

        final JLabel lblMulty = new JLabel("Multy");
        final JTextField tfMulty = new JTextField("  1");

        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new FlowLayout());
        buttonsPane.add(byTimer);
        buttonsPane.add(freq);
        buttonsPane.add(lblMulty);
        buttonsPane.add(tfMulty);
        buttonsPane.add(event);
        buttonsPane.add(measure);
        dlg.getRootPane().add(buttonsPane, BorderLayout.SOUTH);

        Dimension size = new Dimension(initialWidth, initialHeight);
        currData = new int[] { 0, 0 };
        final Graph graph = new Graph(INITIAL_HEIGHT,
                new GraphDescriptor(Color.RED, "Red"),
                new GraphDescriptor(Color.BLUE, "Blue"));
        graph.setPreferredSize(size);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        wrapper.add(graph, BorderLayout.CENTER);
        wrapper.setPreferredSize(size);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(Box.createHorizontalGlue());
        pane.add(wrapper);
        pane.add(Box.createHorizontalGlue());

        pane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        dlg.getRootPane().add(pane /*wrapper*/ /*indicator*/, BorderLayout.CENTER);

        final Timer timer = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < getInt(tfMulty.getText(), 1); i++) {
                    addData(graph);
                }
            }
        });

        byTimer.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                if (byTimer.isSelected()) {
                    try {
                        int delay = Integer.parseInt(freq.getText().trim());
                        timer.setDelay(delay);
                        timer.start();
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }


                } else {
                    timer.stop();
                }
            }
        });

        event.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < getInt(tfMulty.getText(), 1); i++) {
                    addData(graph);
                }
            }
        });
        measure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                measure(graph, byTimer);
            }
        });

        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent arg0) {
                System.exit(0);
            }
        });
        dlg.pack();
        //dlg.setLocation(512, 256);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dlg.setVisible(true);
        //indicator.setCurrPos(indicator.getWidth()*7/8);
    }


    private int getInt(String text, int defultValue) {
        try {
            return Integer.parseInt(text.trim());

        } catch (NumberFormatException nfe) {
            return defultValue;
        }
    }

    private void addData(Graph graph) {

        int sum = 0;
        for (int i = 0; i < currData.length; i++) {
            sum += currData[i];
        }

        if (sum + currData.length >= MAX_DATA) {
            ascending = false;
        } else if(sum < currData.length) {
            ascending = true;

        }
        for (int i = 0; i < currData.length; i++) {
            if (ascending) {
                currData[i] += 1;
            } else {
                currData[i] -= 1;
            }
        }
        graph.addData(currData);
        if (sum > graph.getUpperLimit()) {
            graph.setUpperLimit(sum*3/2);
        }
    }

    private void measure(Graph graph, JCheckBox byTimer) {
        byTimer.setSelected(false);
        long time = System.currentTimeMillis();
        int cycles = 100000;
        for (int i = 0; i < cycles; i++) {
            addData(graph);
        }
        time = System.currentTimeMillis() - time;
        String message = String.format("cycles: %d    time: %d ms", cycles, time);
        JOptionPane.showMessageDialog(null, message, "Measurements", JOptionPane.INFORMATION_MESSAGE);
    }

}
