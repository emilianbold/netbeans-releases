/*
 * ShortcutPanel.java
 *
 * Created on August 14, 2005, 10:41 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

// Hardly inspired by org.netbeans.org

/**
 * Serves for catching key stroke.
 *
 * @author Martin Krauskopf
 */
final class ShortcutEnterPanel extends JPanel {
    
    private JLabel shortcutLabel;
    private JTextField shortcut;
    
    private int currentKeyCode;
    private int currentModifiers;
    
    ShortcutEnterPanel() {
        shortcut = new JTextField();
        shortcutLabel = new JLabel();
        Mnemonics.setLocalizedText(shortcutLabel,
                NbBundle.getMessage(ShortcutEnterPanel.class, "LBL_Shortcut"));
        shortcutLabel.setLabelFor(shortcut);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        add(shortcutLabel, gbc);
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(shortcut, gbc);
        
        shortcutLabel.setLabelFor(shortcut);
        
        shortcut.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent evt) {
                evt.consume();
                currentKeyCode = evt.getKeyCode();
                currentModifiers = evt.getModifiers();
                shortcut.setText(ShortcutEnterPanel.getKeyText(
                        evt.getKeyCode(), evt.getModifiers()));
            }
            public void keyReleased(KeyEvent evt) {
                evt.consume();
                switch (currentKeyCode) {
                    case KeyEvent.VK_ALT:
                    case KeyEvent.VK_ALT_GRAPH:
                    case KeyEvent.VK_CONTROL:
                    case KeyEvent.VK_SHIFT:
                    case KeyEvent.VK_META:
                        // Not finished entering key
                        shortcut.setText("");
                        break;
                }
            }
            public void keyTyped(KeyEvent evt) {
                evt.consume();
            }
        });
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(300, 50);
    }
    
    KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke(currentKeyCode, currentModifiers);
    }
    
    String getKeyText() {
        return ShortcutEnterPanel.getKeyText(currentKeyCode, currentModifiers);
    }
    
    static String getKeyText(int keyCode, int modifiers) {
        String modifText = KeyEvent.getKeyModifiersText(modifiers);
        if ("".equals(modifText)) return KeyEvent.getKeyText(keyCode); // NOI18N
        else {
            if ((keyCode == KeyEvent.VK_ALT) ||
                    (keyCode == KeyEvent.VK_ALT_GRAPH) ||
                    (keyCode == KeyEvent.VK_CONTROL) ||
                    (keyCode == KeyEvent.VK_SHIFT)) {
                return modifText + "+"; // in this case the keyCode text is also among the modifiers // NOI18N
            } else {
                return modifText + "+" + KeyEvent.getKeyText(keyCode); // NOI18N
            }
        }
    }
    
}

