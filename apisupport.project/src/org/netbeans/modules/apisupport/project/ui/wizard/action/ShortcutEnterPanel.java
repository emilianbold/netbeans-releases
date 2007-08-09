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

package org.netbeans.modules.apisupport.project.ui.wizard.action;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Radek Matous
 */
public class ShortcutEnterPanel extends javax.swing.JPanel {
    private final Listener listener = new Listener();
    private final JButton bTab;
    private final JButton bClear;
    
    
    /** Creates new form ShortcutCustomizerPanel */
    public ShortcutEnterPanel() {
        initComponents();
        bTab = new JButton();
        bClear = new JButton();
        loc(bTab, "CTL_Tab");
        loc(bClear, "CTL_Clear");
        tfShortcut.setFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                Collections.<AWTKeyStroke>emptySet()
                );
        tfShortcut.setFocusTraversalKeys(
                KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS,
                Collections.<AWTKeyStroke>emptySet()
                );
        tfShortcut.setFocusTraversalKeys(
                KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS,
                Collections.<AWTKeyStroke>emptySet()
                );
        
        tfShortcut.addKeyListener(listener);        
    }
    
    private String getTitle() {
        return loc("LBL_AddShortcutTitle");
    }
    
    private Object[] getAdditionalOptions() {
        return new Object[] {bClear, bTab};
    }
    
    private String getShortcutText() {
        return tfShortcut.getText();
    }
    
    
    static KeyStroke[] showDialog() {        
        Object[] buttons = new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        };
        final ShortcutEnterPanel sepPanel = new ShortcutEnterPanel();
        
        DialogDescriptor descriptor = new DialogDescriptor(sepPanel,sepPanel.getTitle(),
                true,buttons,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null,sepPanel.listener);
        descriptor.setClosingOptions(new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        });
        descriptor.setAdditionalOptions(sepPanel.getAdditionalOptions());
        
        DialogDisplayer.getDefault().notify(descriptor);
        String shortcut = sepPanel.getShortcutText();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION && shortcut != null && shortcut.trim().length() > 0) {
            return UIUtil.stringToKeyStrokes(shortcut);//NOI18N
            
        } else {
            return null;
        }
    }
    
    private static void loc(Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText(
                    (AbstractButton) c,
                    loc(key)
                    );
        else
            Mnemonics.setLocalizedText(
                    (JLabel) c,
                    loc(key)
                    );
    }
    
    private static String loc(String key) {
        return NbBundle.getMessage(ShortcutEnterPanel.class, key);
    }
    
    private class Listener implements ActionListener, KeyListener {
        
        private KeyStroke backspaceKS = KeyStroke.getKeyStroke
                (KeyEvent.VK_BACK_SPACE, 0);
        private KeyStroke tabKS = KeyStroke.getKeyStroke
                (KeyEvent.VK_TAB, 0);
        
        private String key = "";
        
        public void keyTyped(KeyEvent e) {
            e.consume();
        }
        
        public void keyPressed(KeyEvent e) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(
                    e.getKeyCode(),
                    e.getModifiers()
                    );
            
            boolean add = e.getKeyCode() != e.VK_SHIFT &&
                    e.getKeyCode() != e.VK_CONTROL &&
                    e.getKeyCode() != e.VK_ALT &&
                    e.getKeyCode() != e.VK_META &&
                    e.getKeyCode() != e.VK_ALT_GRAPH;
            
            if (keyStroke.equals(backspaceKS) && !key.equals("")) {
                // delete last key
                int i = key.lastIndexOf(' ');
                if (i < 0)
                    key = "";
                else
                    key = key.substring(0, i);
                tfShortcut.setText(key);
            } else
                // add key
                addKeyStroke(keyStroke, add);
            
            e.consume();
        }
        
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bClear) {
                key = "";
                tfShortcut.setText(key);
                tfShortcut.requestFocusInWindow();
            } else if (e.getSource() == bTab) {
                addKeyStroke(tabKS, true);
                tfShortcut.requestFocusInWindow();
            }
        }
        
        
        private void addKeyStroke(KeyStroke keyStroke, boolean add) {
            String k = UIUtil.keyStrokeToString(keyStroke);
            if (key.equals("")) {
                tfShortcut.setText(k);
                if (add) key = k;
            } else {
                tfShortcut.setText(key + " " + k);
                if (add) key += " " + k;
            }
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        tfShortcut = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(tfShortcutLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/action/Bundle").getString("LBL_Shortcut"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tfShortcutLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfShortcut, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfShortcutLabel)
                    .add(tfShortcut, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField tfShortcut;
    private final javax.swing.JLabel tfShortcutLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables
    
}
