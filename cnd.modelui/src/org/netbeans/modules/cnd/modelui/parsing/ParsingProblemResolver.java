/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelui.parsing;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.cnd.modelui.parsing.ParsingProblemDetectorImpl.Measure;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class ParsingProblemResolver extends javax.swing.JPanel {

    //private static final RequestProcessor RP = new RequestProcessor(ParsingProblemResolver.class.getName(), 2);
    private final ParsingProblemDetectorImpl detector;

    /** Creates new form ParsingProblemResolver */
    public ParsingProblemResolver(ParsingProblemDetectorImpl detector) {
        initComponents();
        this.detector = detector;
        if (!ParsingProblemDetectorImpl.TIMING) {
            this.remove(chartPanel);
        } else {
            ((ChartPanel) chartPanel).setModel(detector);
        }
        explanation.setEditorKit(new HTMLEditorKit());
        explanation.setBackground(getBackground());
        explanation.setForeground(getForeground());
        explanation.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        explanation.setText(NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Explanation")); // NOI18N

    }

    public static void showParsingProblemResolver(final ParsingProblemDetectorImpl detector) {
        ActionListener onClickAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ParsingProblemResolver panel = new ParsingProblemResolver(detector);
                DialogDescriptor descriptor = new DialogDescriptor(panel,
                        NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Dialog_Title"), // NOI18N
                        true, new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                try {
                    dlg.setVisible(true);
                } catch (Throwable th) {
                    if (!(th.getCause() instanceof InterruptedException)) {
                        throw new RuntimeException(th);
                    }
                    descriptor.setValue(DialogDescriptor.CANCEL_OPTION);
                } finally {
                    dlg.dispose();
                }
            }
        };
        String title = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Dialog_Short_Title"); // NOI18N
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/modelui/parsing/exclamation.gif", false); // NOI18N
        final Notification notification = NotificationDisplayer.getDefault().notify(title, icon,
                NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Dialog_Action"), onClickAction, NotificationDisplayer.Priority.HIGH); // NOI18N
        //RP.post(new Runnable() {
        //    @Override
        //    public void run() {
        //        notification.clear();
        //    }
        //}, 150 * 1000);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chartPanel = new ChartPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        explanation = new javax.swing.JTextPane();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 1, 6));
        setLayout(new java.awt.BorderLayout());

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 200));

        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 448, Short.MAX_VALUE)
        );
        chartPanelLayout.setVerticalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        add(chartPanel, java.awt.BorderLayout.NORTH);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 200));

        explanation.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 6, 1, 6));
        explanation.setEditable(false);
        jScrollPane1.setViewportView(explanation);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JTextPane explanation;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private static final class ChartPanel extends JPanel {

        private ParsingProblemDetectorImpl detector;
        private static final int shift = 10;
        private static final int tick = 3;
        private static final String Y_memory_units = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Y_memory_units"); // NOI18N
        private static final String Y_speed_units = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Y_speed_units"); // NOI18N
        private static final String X_units = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "X_units"); // NOI18N


        private void setModel(ParsingProblemDetectorImpl detector) {
            this.detector = detector;
        }

        @Override
        public void paint(Graphics g) {
            if (detector == null) {
                super.paint(g);
            }
            List<Measure> data = detector.getData();
            if (data.size() < 10) {
                super.paint(g);
            }
            paintScale(g);
            paintMemory(data, g);
            paintSpeed(data, g);
        }

        private void paintScale(Graphics g) {
            int h = getHeight();
            int w = getWidth();
            g.drawLine(shift, h - shift, shift, shift - tick);
            g.drawLine(shift - tick, shift, shift + tick, shift);
            g.drawLine(shift, h - shift, w - shift + tick, h - shift);
            g.drawLine(w - shift, h - shift - tick, w - shift, h - shift + tick);
        }

        private void paintMemory(List<Measure> data, Graphics g) {
            Measure last = data.get(data.size() - 1);
            int allTime = last.time;
            int h = getHeight();
            int w = getWidth();
            Color prevColor = g.getColor();
            g.setColor(Color.red);
            int xPrev = (int) (((double) data.get(0).time / (double) allTime) * (w-shift*2)) + shift;
            int myPrev = (int) (((double) data.get(0).memory / (double) detector.maxMemory) * (h-shift*2)) + shift;
            for (int i = 1; i < data.size(); i++) {
                Measure m = data.get(i);
                int x = (int) (((double) m.time / (double) allTime) * (w-shift*2)) + shift;
                int my = (int) (((double) m.memory / (double) detector.maxMemory) * (h-shift*2)) + shift;
                g.drawLine(xPrev, h - myPrev, x, h - my);
                myPrev = my;
                xPrev = x;
            }
            FontMetrics fontMetrics = g.getFontMetrics();
            g.drawString(""+detector.maxMemory+" "+Y_memory_units, shift+3, shift+fontMetrics.getHeight()/2); // NOI18N
            g.setColor(prevColor);
        }

        private void paintSpeed(List<Measure> data, Graphics g) {
            Measure last = data.get(data.size() - 1);
            int allTime = last.time;
            int allLine = last.lines;
            int h = getHeight();
            int w = getWidth();
            Color prevColor = g.getColor();

            int fragment = 5;
            int currentPercent = 1;
            int curentTime = 0;
            int curentLines = 0;
            int maxSpeed = 0;
            for (Measure m : data) {
                int p = m.lines * 100 / allLine;
                if (p - currentPercent * fragment >= 0) {
                    int l = m.lines - curentLines;
                    curentLines = m.lines;
                    int t = m.time - curentTime;
                    curentTime = m.time;
                    currentPercent++;
                    if (t != 0) {
                        maxSpeed = Math.max(maxSpeed, l * 1000 / t);
                    }
                }
            }
            g.setColor(Color.blue);
            currentPercent = 1;
            curentTime = 0;
            curentLines = 0;
            int xPrev = shift;
            int syPrev = shift;
            for (Measure m : data) {
                int p = m.lines * 100 / allLine;
                if (p - currentPercent * fragment >= 0) {
                    int l = m.lines - curentLines;
                    curentLines = m.lines;
                    int t = m.time - curentTime;
                    curentTime = m.time;
                    currentPercent++;
                    if (t != 0) {
                        int currSpeed = l * 1000 / t;
                        int x = (int) (((double) m.time / (double) allTime) * (w-shift*2)) + shift;
                        int sy = (int) (((double) currSpeed / (double) maxSpeed) * (h-shift*2)) + shift;
                        g.drawLine(xPrev, h - syPrev, x, h - sy);
                        syPrev = sy;
                        xPrev = x;
                    }
                }
            }
            FontMetrics fontMetrics = g.getFontMetrics();
            g.drawString(""+(maxSpeed/1000)+" "+Y_speed_units, shift+3, shift+fontMetrics.getHeight()+fontMetrics.getHeight()/2); // NOI18N
            g.setColor(prevColor);
            String time = ""+(allTime/1000)+" "+X_units;// NOI18N
            int len = fontMetrics.stringWidth(time);
            g.drawString(time, w-shift-len, h-shift-3);
            
        }
    }
}
