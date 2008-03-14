/*
 * BrowserPanel.java
 *
 * Created on December 27, 2007, 8:53 PM
 */

package org.netbeans.modules.javascript.editing.options;

import java.util.EnumSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.mozilla.javascript.Context;
import org.netbeans.modules.javascript.editing.BrowserVersion;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.openide.util.NbBundle;

/**
 *
 * @author  Tor Norbye
 */
public class BrowserPanel extends javax.swing.JPanel {

    private final String[] VERSION_LABELS = new String[] {
         NbBundle.getMessage(BrowserPanel.class, "LanguageDefault"),
         "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8"
    };
    private final int[] VERSION_VALUES = new int[] {
        Context.VERSION_DEFAULT,
        Context.VERSION_1_0,
        Context.VERSION_1_1,
        Context.VERSION_1_2,
        Context.VERSION_1_3,
        Context.VERSION_1_4,
        Context.VERSION_1_5,
        Context.VERSION_1_6,
        Context.VERSION_1_7,
        Context.VERSION_1_8
    };

    private final JsOptionsController controller;

    /** Creates new form BrowserPanel */
    public BrowserPanel(JsOptionsController controller) {
        this.controller = controller;
        initComponents();
    }

    public void load() {
        SupportedBrowsers query = SupportedBrowsers.getInstance();
        if (query.isSupported(BrowserVersion.FF1)) {
            ff1Rb.setSelected(true);
            ffCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.FF2)) {
            ff2Rb.setSelected(true);
            ffCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.FF3)) {
            ff3Rb.setSelected(true);
            ffCb.setSelected(true);
        } else {
            ffCb.setSelected(false);
        }

        if (query.isSupported(BrowserVersion.IE55)) {
            ie5Rb.setSelected(true);
            ieCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.IE6)) {
            ie6Rb.setSelected(true);
            ieCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.IE7)) {
            ie7Rb.setSelected(true);
            ieCb.setSelected(true);
        } else {
            ieCb.setSelected(false);
        }

        if (query.isSupported(BrowserVersion.SAFARI2)) {
            sf2Rb.setSelected(true);
            safariCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.SAFARI3)) {
            sf3Rb.setSelected(true);
            safariCb.setSelected(true);
        } else {
            safariCb.setSelected(false);
        }

        operaCb.setSelected(query.isSupported(BrowserVersion.OPERA));

        int version = query.getLanguageVersion();
        for (int i = 0, n = VERSION_VALUES.length; i < n; i++) {
            if (VERSION_VALUES[i] == version) {
                languageCombo.setSelectedIndex(i);
                break;
            }
        }
        // XXX TODO
    }

    public void store() {
        EnumSet<BrowserVersion> es = EnumSet.noneOf(BrowserVersion.class);

        if (ieCb.isSelected()) {
            if (ie5Rb.isSelected()) {
                es.add(BrowserVersion.IE55);
                es.add(BrowserVersion.IE6);
                es.add(BrowserVersion.IE7);
            } else if (ie6Rb.isSelected()) {
                es.add(BrowserVersion.IE6);
                es.add(BrowserVersion.IE7);
            } else if (ie7Rb.isSelected()) {
                es.add(BrowserVersion.IE7);
            }
        }

        if (ffCb.isSelected()) {
            if (ff1Rb.isSelected()) {
                es.add(BrowserVersion.FF1);
                es.add(BrowserVersion.FF2);
                es.add(BrowserVersion.FF3);
            } else if (ff2Rb.isSelected()) {
                es.add(BrowserVersion.FF2);
                es.add(BrowserVersion.FF3);
            } else if (ff3Rb.isSelected()) {
                es.add(BrowserVersion.FF3);
            }
        }

        if (safariCb.isSelected()) {
            if (sf2Rb.isSelected()) {
                es.add(BrowserVersion.SAFARI2);
                es.add(BrowserVersion.SAFARI3);
            } else if (sf3Rb.isSelected()) {
                es.add(BrowserVersion.SAFARI3);
            }
        }

        if (operaCb.isSelected()) {
            es.add(BrowserVersion.OPERA);
        }

        final SupportedBrowsers supported = SupportedBrowsers.getInstance();
        JsOptionsController.Accessor.DEFAULT.setSupported(supported, es);
        JsOptionsController.Accessor.DEFAULT.setLanguageVersion(supported,
                VERSION_VALUES[languageCombo.getSelectedIndex()]);
    }

    private ComboBoxModel getVersionModel() {
        return new DefaultComboBoxModel(VERSION_LABELS);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        firefoxGroup = new javax.swing.ButtonGroup();
        ieGroup = new javax.swing.ButtonGroup();
        safariGroup = new javax.swing.ButtonGroup();
        browserLabel = new javax.swing.JLabel();
        ffCb = new javax.swing.JCheckBox();
        ieCb = new javax.swing.JCheckBox();
        safariCb = new javax.swing.JCheckBox();
        operaCb = new javax.swing.JCheckBox();
        ff1Rb = new javax.swing.JRadioButton();
        ff2Rb = new javax.swing.JRadioButton();
        ff3Rb = new javax.swing.JRadioButton();
        ie5Rb = new javax.swing.JRadioButton();
        ie6Rb = new javax.swing.JRadioButton();
        ie7Rb = new javax.swing.JRadioButton();
        sf2Rb = new javax.swing.JRadioButton();
        sf3Rb = new javax.swing.JRadioButton();
        unsupportedCb = new javax.swing.JCheckBox();
        limitCb = new javax.swing.JCheckBox();
        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();

        browserLabel.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.browserLabel.text")); // NOI18N

        ffCb.setSelected(true);
        ffCb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ffCb.text")); // NOI18N
        ffCb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        ieCb.setSelected(true);
        ieCb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ieCb.text")); // NOI18N
        ieCb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        safariCb.setSelected(true);
        safariCb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.safariCb.text")); // NOI18N
        safariCb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        operaCb.setSelected(true);
        operaCb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.operaCb.text")); // NOI18N
        operaCb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        firefoxGroup.add(ff1Rb);
        ff1Rb.setSelected(true);
        ff1Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ff1Rb.text")); // NOI18N
        ff1Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        firefoxGroup.add(ff2Rb);
        ff2Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ff2Rb.text")); // NOI18N
        ff2Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        firefoxGroup.add(ff3Rb);
        ff3Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ff3Rb.text")); // NOI18N
        ff3Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        ieGroup.add(ie5Rb);
        ie5Rb.setSelected(true);
        ie5Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ie5Rb.text")); // NOI18N
        ie5Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        ieGroup.add(ie6Rb);
        ie6Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ie6Rb.text")); // NOI18N
        ie6Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        ieGroup.add(ie7Rb);
        ie7Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ie7Rb.text")); // NOI18N
        ie7Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        safariGroup.add(sf2Rb);
        sf2Rb.setSelected(true);
        sf2Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.sf2Rb.text")); // NOI18N
        sf2Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        safariGroup.add(sf3Rb);
        sf3Rb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.sf3Rb.text")); // NOI18N
        sf3Rb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheckChange(evt);
            }
        });

        unsupportedCb.setSelected(true);
        unsupportedCb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.unsupportedCb.text")); // NOI18N
        unsupportedCb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        limitCb.setSelected(true);
        limitCb.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.limitCb.text")); // NOI18N
        limitCb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        languageLabel.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.languageLabel.text")); // NOI18N

        languageCombo.setModel(getVersionModel());
        languageCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(browserLabel)
                    .add(layout.createSequentialGroup()
                        .add(ffCb)
                        .add(18, 18, 18)
                        .add(ff1Rb)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ff2Rb)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ff3Rb))
                    .add(layout.createSequentialGroup()
                        .add(ieCb)
                        .add(18, 18, 18)
                        .add(ie5Rb)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ie6Rb)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ie7Rb))
                    .add(unsupportedCb)
                    .add(limitCb)
                    .add(layout.createSequentialGroup()
                        .add(safariCb)
                        .add(20, 20, 20)
                        .add(sf2Rb)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sf3Rb))
                    .add(operaCb)
                    .add(layout.createSequentialGroup()
                        .add(languageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(93, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(browserLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ffCb)
                    .add(ff1Rb)
                    .add(ff2Rb)
                    .add(ff3Rb))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ieCb)
                    .add(ie5Rb)
                    .add(ie6Rb)
                    .add(ie7Rb))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(safariCb)
                    .add(sf2Rb)
                    .add(sf3Rb))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(operaCb)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(languageLabel)
                    .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 60, Short.MAX_VALUE)
                .add(limitCb)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(unsupportedCb)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void actionCheckChange(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionCheckChange
// TODO add your handling code here:
        controller.changed();
}//GEN-LAST:event_actionCheckChange

private void itemCheckChange(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_itemCheckChange
// TODO add your handling code here:
        controller.changed();
}//GEN-LAST:event_itemCheckChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel browserLabel;
    private javax.swing.JRadioButton ff1Rb;
    private javax.swing.JRadioButton ff2Rb;
    private javax.swing.JRadioButton ff3Rb;
    private javax.swing.JCheckBox ffCb;
    private javax.swing.ButtonGroup firefoxGroup;
    private javax.swing.JRadioButton ie5Rb;
    private javax.swing.JRadioButton ie6Rb;
    private javax.swing.JRadioButton ie7Rb;
    private javax.swing.JCheckBox ieCb;
    private javax.swing.ButtonGroup ieGroup;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JCheckBox limitCb;
    private javax.swing.JCheckBox operaCb;
    private javax.swing.JCheckBox safariCb;
    private javax.swing.ButtonGroup safariGroup;
    private javax.swing.JRadioButton sf2Rb;
    private javax.swing.JRadioButton sf3Rb;
    private javax.swing.JCheckBox unsupportedCb;
    // End of variables declaration//GEN-END:variables

}
