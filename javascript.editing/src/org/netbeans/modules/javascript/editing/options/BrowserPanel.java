/*
 * BrowserPanel.java
 *
 * Created on December 27, 2007, 8:53 PM
 */

package org.netbeans.modules.javascript.editing.options;

import java.util.EnumSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.mozilla.nb.javascript.Context;
import org.netbeans.modules.javascript.editing.BrowserVersion;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Tor Norbye
 */
public class BrowserPanel extends JSPreferencesPanel {


    private final String[] VERSION_LABELS = new String[] {
         NbBundle.getMessage(BrowserPanel.class, "LanguageDefault"),
         "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8"
    };


   private enum BROWSER_VERSION {
       FF1(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ff1Rb.text")),
       FF2(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ff2Rb.text")) ,
       FF3(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ff3Rb.text")),
       SAFARI2(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.sf2Rb.text")),
       SAFARI3(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.sf3Rb.text")),
       IE5(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ie5Rb.text")),
       IE6(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ie6Rb.text")),
       IE7(NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.ie7Rb.text"));
       BROWSER_VERSION(String string){
           this.string = string;
       }
       String string;
       public String toString() {
           return string;
       }
   }

    
    private final BROWSER_VERSION[] FF_VERSION_LABELS =new BROWSER_VERSION[] {
        BROWSER_VERSION.FF1,
        BROWSER_VERSION.FF2,
        BROWSER_VERSION.FF3};
    
    private final BROWSER_VERSION[] IE_VERSION_LABELS = new BROWSER_VERSION[] {
        BROWSER_VERSION.IE5, 
        BROWSER_VERSION.IE6,
        BROWSER_VERSION.IE7
    };
    
    private final BROWSER_VERSION[] SAFARI_VERSION_LABELS = new BROWSER_VERSION[] { 
        BROWSER_VERSION.SAFARI2,
        BROWSER_VERSION.SAFARI3
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

    @Override
    public void load() {
        SupportedBrowsers query = SupportedBrowsers.getInstance();
        if (query.isSupported(BrowserVersion.FF1)) {
            cbFFVersion.setSelectedItem(BROWSER_VERSION.FF1);
            ffCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.FF2)) {
            cbFFVersion.setSelectedItem(BROWSER_VERSION.FF2);
            ffCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.FF3)) {
            cbFFVersion.setSelectedItem(BROWSER_VERSION.FF3);
            ffCb.setSelected(true);
        } else {
            ffCb.setSelected(false);
        }

        if (query.isSupported(BrowserVersion.IE55)) {
            cbIEVersion.setSelectedItem(BROWSER_VERSION.IE5);
            ieCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.IE6)) {
            cbIEVersion.setSelectedItem(BROWSER_VERSION.IE6);
            ieCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.IE7)) {
            cbIEVersion.setSelectedItem(BROWSER_VERSION.IE7);
            ieCb.setSelected(true);
        } else {
            ieCb.setSelected(false);
        }

        if (query.isSupported(BrowserVersion.SAFARI2)) {
            cbSafariVersion.setSelectedItem(BROWSER_VERSION.SAFARI2);
            safariCb.setSelected(true);
        } else if (query.isSupported(BrowserVersion.SAFARI3)) {
            cbSafariVersion.setSelectedItem(BROWSER_VERSION.SAFARI3);
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
        
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void store() {
        EnumSet<BrowserVersion> es = EnumSet.noneOf(BrowserVersion.class);

        if (ieCb.isSelected()) {
            assert cbIEVersion.getSelectedItem() instanceof BROWSER_VERSION;
            switch ( (BROWSER_VERSION) cbIEVersion.getSelectedItem()){
                case IE5:
                    es.add(BrowserVersion.IE55); //Fall Through On Purpose
                case IE6:
                    es.add(BrowserVersion.IE6);
                case IE7:
                    es.add(BrowserVersion.IE7);
                default:
                    break;
            }
        }
        
        if (ffCb.isSelected()) {
            assert cbFFVersion.getSelectedItem() instanceof BROWSER_VERSION;
            switch ( (BROWSER_VERSION) cbFFVersion.getSelectedItem()){
                case FF1:
                    es.add(BrowserVersion.FF1); //Fall Through On Purpose
                case FF2:
                    es.add(BrowserVersion.FF2);
                case FF3:
                    es.add(BrowserVersion.FF3);
                default:
                    break;
            }
        }
        
        if (safariCb.isSelected()) {
            assert cbSafariVersion.getSelectedItem() instanceof BROWSER_VERSION;
            switch ( (BROWSER_VERSION) cbSafariVersion.getSelectedItem()){
                case SAFARI2:
                    es.add(BrowserVersion.SAFARI2); //Fall Through On Purpose
                case SAFARI3:
                    es.add(BrowserVersion.SAFARI3);
                default:
                    break;
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

    private ComboBoxModel getFFVersionModel() {
        return new DefaultComboBoxModel(FF_VERSION_LABELS);
    }

    private ComboBoxModel getSafariVersionModel() {
        return new DefaultComboBoxModel(SAFARI_VERSION_LABELS);
    }

    private ComboBoxModel getIEVersionModel() {
        return new DefaultComboBoxModel(IE_VERSION_LABELS);
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
        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        cbSafariVersion = new javax.swing.JComboBox();
        cbIEVersion = new javax.swing.JComboBox();
        cbFFVersion = new javax.swing.JComboBox();

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

        languageLabel.setText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.languageLabel.text")); // NOI18N

        languageCombo.setModel(getVersionModel());
        languageCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                itemCheckChange(evt);
            }
        });

        cbSafariVersion.setModel(getSafariVersionModel());

        cbIEVersion.setModel(getIEVersionModel());

        cbFFVersion.setModel(getFFVersionModel());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(browserLabel)
                    .add(languageLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(ffCb)
                            .add(ieCb)
                            .add(safariCb)
                            .add(operaCb))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbSafariVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(cbFFVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(cbIEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(185, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(browserLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(ffCb)
                            .add(cbFFVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(ieCb)
                            .add(cbIEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(safariCb)
                            .add(cbSafariVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(operaCb)))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(languageLabel)
                    .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void itemCheckChange(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_itemCheckChange
// TODO add your handling code here:
        controller.changed();
}//GEN-LAST:event_itemCheckChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel browserLabel;
    private javax.swing.JComboBox cbFFVersion;
    private javax.swing.JComboBox cbIEVersion;
    private javax.swing.JComboBox cbSafariVersion;
    private javax.swing.JCheckBox ffCb;
    private javax.swing.ButtonGroup firefoxGroup;
    private javax.swing.JCheckBox ieCb;
    private javax.swing.ButtonGroup ieGroup;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JCheckBox operaCb;
    private javax.swing.JCheckBox safariCb;
    private javax.swing.ButtonGroup safariGroup;
    // End of variables declaration//GEN-END:variables

}
