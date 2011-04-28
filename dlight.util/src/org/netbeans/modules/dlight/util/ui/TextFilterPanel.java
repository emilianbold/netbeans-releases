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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * TextFilterPanel.java
 *
 * Created on 15.11.2010, 10:37:14
 */
package org.netbeans.modules.dlight.util.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

/**
 *
 * @author ak119685
 */
public final class TextFilterPanel extends javax.swing.JPanel {

    private static final int FIRING_DELAY = 500;
    private final RequestProcessor rp = new RequestProcessor(TextFilterPanel.class.getName(), 1);
    private final Task filterTask;
    private final ChangeSupport cs;
    private final LinkedList<String> model;

    /** Creates new form TextFilterPanel */
    public TextFilterPanel() {
        initComponents();

        cs = new ChangeSupport(this);
        model = new LinkedList<String>();
        updateModel();

        filterTask = rp.create(new Runnable() {

            @Override
            public void run() {
                cs.fireChange();
            }
        });

        cmbFilter.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateModel();
                    updateFilter(true);
                } else {
                    updateFilter(false);
                }
            }
        });

        cmbFilter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateFilter(true);
            }
        });

        cmbFilter.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                updateModel();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }

    private void updateFilter(boolean immediately) {
        filterTask.schedule(immediately ? 0 : FIRING_DELAY);
    }

    private void updateModel() {
        String text = getText();

        if (text.isEmpty()) {
            return;
        }

        model.remove(text);
        model.addFirst(text);

        if (model.size() > 5) {
            model.removeLast();
        }

        cmbFilter.setModel(new DefaultComboBoxModel(model.toArray(new String[model.size()])));
    }

    public String getText() {
        final AtomicReference<String> result = new AtomicReference<String>();

        if (SwingUtilities.isEventDispatchThread()) {
            result.set(cmbFilter.getEditor().getItem().toString());
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        result.set(cmbFilter.getEditor().getItem().toString());
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return result.get();
    }

    public void addWeakChangeListener(ChangeListener l) {
        cs.addChangeListener(WeakListeners.change(l, this));
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFilter = new javax.swing.JLabel();
        cmbFilter = new javax.swing.JComboBox();
        btnClean = new javax.swing.JButton();

        lblFilter.setFont(lblFilter.getFont().deriveFont(lblFilter.getFont().getStyle() | java.awt.Font.BOLD));
        lblFilter.setLabelFor(cmbFilter);
        lblFilter.setText(org.openide.util.NbBundle.getMessage(TextFilterPanel.class, "TextFilterPanel.lblFilter.text")); // NOI18N

        cmbFilter.setEditable(true);

        btnClean.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/dlight/util/resources/clean.png"))); // NOI18N
        btnClean.setText(org.openide.util.NbBundle.getMessage(TextFilterPanel.class, "TextFilterPanel.btnClean.text")); // NOI18N
        btnClean.setContentAreaFilled(false);
        btnClean.setFocusPainted(false);
        btnClean.setFocusable(false);
        btnClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCleanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbFilter, 0, 215, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClean, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblFilter)
                .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnClean))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanActionPerformed
        cmbFilter.getEditor().setItem(null);
        updateFilter(true);
    }//GEN-LAST:event_btnCleanActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClean;
    private javax.swing.JComboBox cmbFilter;
    private javax.swing.JLabel lblFilter;
    // End of variables declaration//GEN-END:variables
}
