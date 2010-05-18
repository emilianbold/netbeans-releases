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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.shell;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import org.openide.util.Exceptions;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.CardState;
import org.netbeans.modules.javacard.spi.CardStateObserver;
import org.openide.util.NbBundle;

/**
 *
 * @author  Anki R Nelaturu
 */
final class ShellPanel extends javax.swing.JPanel implements  CardStateObserver, KeyListener {
    private static final String STYLE_COMMAND = "command"; //NOI18N
    private static final String STYLE_COPYRIGHT = "copyright"; //NOI18N
    private static final String STYLE_ERROR = "error"; //NOI18N
    private static final String STYLE_KEYWORD = "keyword"; //NOI18N
    private static final String STYLE_NORMAL = "normal"; //NOI18N
    private static final String STYLE_PROMPT = "prompt"; //NOI18N
    private static final String STYLE_RESPONSE = "response"; //NOI18N

    private StringBuffer command = new StringBuffer();
    private int guardPos = 0;
    private Vector<String> history = new Vector<String>();
    private int historyIndex = 0;
    private Card card = null;
    private final CommandManager commandManager = new CommandManager();

    public ShellPanel() {
        initComponents();
        addStyles();
        shellTextPane.registerKeyboardAction(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable clipboardContent = clipboard.getContents(this);
                if ((clipboardContent != null) &&
                        (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
                    String tempString;
                    try {
                        tempString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
                        appendPastedString(tempString);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }
        },
                KeyStroke.getKeyStroke(
                KeyEvent.VK_V, KeyEvent.CTRL_MASK), JTextPane.WHEN_FOCUSED);
        Font f = UIManager.getFont("controlFont"); //NOI18N
        int size = f == null ? 13 : f.getSize();
        shellTextPane.setFont (new Font ("Monospaced", Font.PLAIN, size)); //NOI18N
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public void setServer(Card server) {
        this.card = server;
        server.addCardStateObserver(this);
        clear();
    }

    public Card getCard() {
        return card;
    }

    @Override
    public void requestFocus() {
        shellTextPane.requestFocus();
    }

    @Override
    public boolean requestFocus(boolean temporary) {
        return shellTextPane.requestFocus(temporary);
    }

    @Override
    public boolean requestFocusInWindow() {
        return shellTextPane.requestFocusInWindow();
    }

    private void clear() {
        // append(APDUSender.getString("COPYRIGHT"), STYLE_COPYRIGHT); //NOI18N
        append(card.toString(),STYLE_COPYRIGHT);
        append(APDUSender.getString("INITIAL_TEXT"),STYLE_COPYRIGHT); //NOI18N
        prompt();
    }

    public Vector<String> getHistory() {
        return history;
    }

    private void appendPastedString(String str) {
        int cp = shellTextPane.getCaretPosition();
        if (cp < guardPos) {
            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
            cp = shellTextPane.getCaretPosition();
        }

        for (char ch : str.toCharArray()) {
            processKeyChar(ch, null);
            try {
                shellTextPane.getDocument().insertString(shellTextPane.getCaretPosition(), "" + ch, null); //NOI18N
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
//        Utils.sysOut("String is '" + str + "'");
//        int cp = shellTextPane.getCaretPosition();
//        if (cp < guardPos) {
//            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
//            cp = shellTextPane.getCaretPosition();
//        }
//        try {
//            shellTextPane.getDocument().insertString(shellTextPane.getCaretPosition(), str, null);
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        int index = cp - guardPos; // gives exact index of char we need to delete
//        int len = command.length();
//        if (index < len) {
//            command.insert(index, str);
//            hintsUpdate();
//        } else {
//            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
//            command.append(str);
//            hintsUpdate();
//        }
//        tryHighlighting();
    }

    private void append(String str, String styleName) {
        try {
            int end = shellTextPane.getDocument().getLength(); //EndPosition();//.getOffset();
            shellTextPane.getDocument().insertString(end, str, shellTextPane.getStyle(styleName));
            Rectangle r = shellTextPane.modelToView(end);
            if (r != null) {
                shellTextPane.scrollRectToVisible(r);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void addStyles() {
        // Makes text red
        Style style = null;

        style = shellTextPane.addStyle(STYLE_COPYRIGHT, null);
        StyleConstants.setForeground(style, Color.GRAY);
//        StyleConstants.setBold(style, true);

        style = shellTextPane.addStyle(STYLE_NORMAL, null);
        StyleConstants.setForeground(style, Color.BLACK);
//        StyleConstants.setBold(style, true);

        style = shellTextPane.addStyle(STYLE_KEYWORD, null);
        StyleConstants.setForeground(style, Color.MAGENTA);
        StyleConstants.setBold(style, true);
        StyleConstants.setUnderline(style, true);

        style = shellTextPane.addStyle(STYLE_PROMPT, null);
        StyleConstants.setForeground(style, Color.BLACK);
        StyleConstants.setBold(style, true);

        style = shellTextPane.addStyle(STYLE_COMMAND, null);
        StyleConstants.setForeground(style, Color.BLACK);
        StyleConstants.setBold(style, true);

        style = shellTextPane.addStyle(STYLE_RESPONSE, null);
        StyleConstants.setForeground(style, Color.BLUE);
//        StyleConstants.setBold(style, true);

        style = shellTextPane.addStyle(STYLE_ERROR, null);
        StyleConstants.setForeground(style, Color.RED);
//        StyleConstants.setBold(style, true);
    }

    private void historyUp() {
        if (historyIndex > 0) {
            historyIndex--;
        }
        updateCommandFromHistroy(history.elementAt(historyIndex));
    }

    private void histroyDown() {
        if (historyIndex < history.size()) {
            historyIndex++;
        }
        if (historyIndex < history.size()) {
            command.append(history.elementAt(historyIndex));
            updateCommandFromHistroy(history.elementAt(historyIndex));
        } else {
            updateCommandFromHistroy("");
        }
    }

    private void prompt() {
        append(PROMPT,STYLE_PROMPT);
        guardPos = shellTextPane.getDocument().getLength();
    }
    Random random = new Random();

    private void executeCommand(String cmd) throws ShellException {
        append("\n", "response"); //NOI18N
        boolean addToHistory = true;
        if (addToHistory) {
            history.addElement(cmd);
            historyIndex = history.size();
        }

        if ("cls".equals(cmd) || "clear".equals(cmd)) { //NOI18N
            prompt();
            shellTextPane.setSelectionStart(0);
            shellTextPane.setSelectionEnd(guardPos);
            shellTextPane.replaceSelection("");
            // append(APDUSender.getString("COPYRIGHT"), STYLE_COPYRIGHT); //NOI18N
            append(card.toString(),STYLE_COPYRIGHT);
            append(APDUSender.getString("INITIAL_TEXT"),STYLE_COPYRIGHT); //NOI18N
            return;
        }

        //if (cmd.startsWith("history ") || "history".equals(cmd)) {
        //    addToHistory = false;
        //}

        if (cmd.startsWith("!")) {
            //addToHistory = false;
            int index = 0;
            try {
                index = Integer.parseInt(cmd.substring(1));
            } catch (NumberFormatException e) {
                index = -1;
            }
            if (index < 0 || index >= history.size()) {
                append(APDUSender.getString("INVALID_INDEX"), "error"); //NOI18N
                return;
            }
            cmd = history.elementAt(index);
        }


        append(commandManager.execute(this, cmd), STYLE_RESPONSE);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        shellTextPane = new org.netbeans.modules.javacard.shell.NoWrapTextPane();
        hintLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("controlShadow")));
        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        shellTextPane.addKeyListener(this);
        jScrollPane2.setViewportView(shellTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.weighty = 0.9;
        add(jScrollPane2, gridBagConstraints);

        hintLabel.setFont(new java.awt.Font("Monospaced", 0, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(hintLabel, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void keyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getSource() == shellTextPane) {
            ShellPanel.this.shellTextPaneKeyPressed(evt);
        }
    }

    public void keyReleased(java.awt.event.KeyEvent evt) {
        if (evt.getSource() == shellTextPane) {
            ShellPanel.this.shellTextPaneKeyReleased(evt);
        }
    }

    public void keyTyped(java.awt.event.KeyEvent evt) {
        if (evt.getSource() == shellTextPane) {
            ShellPanel.this.shellTextPaneKeyTyped(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void shellTextPaneKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_shellTextPaneKeyTyped
        //    if (evt.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {

        processKeyChar(evt.getKeyChar(), evt);
    }//GEN-LAST:event_shellTextPaneKeyTyped

    private void shellTextPaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_shellTextPaneKeyReleased
        tryHighlighting();
        evt.consume();
}//GEN-LAST:event_shellTextPaneKeyReleased

    private void shellTextPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_shellTextPaneKeyPressed
        int key = evt.getKeyCode();

        if (key == KeyEvent.VK_UP) {
            if (evt.isControlDown()) {
                previousHint();
            } else {
                historyUp();
                hintsUpdate();
            }
            evt.consume();
            return;
        }

        if (key == KeyEvent.VK_DOWN) {
            if (evt.isControlDown()) {
                nextHint();
            } else {
                histroyDown();
                hintsUpdate();
            }
            evt.consume();
            return;
        }

        if (evt.isControlDown()) {
            if (key == KeyEvent.VK_C || key == KeyEvent.VK_X) {
                // let the default actions run
                return;
            }

            if (key == KeyEvent.VK_V) {
                return;
                //            evt.consume();
            }
        }

        if (key == KeyEvent.VK_BACK_SPACE) {
            int cp = shellTextPane.getCaretPosition();
            if (cp <= guardPos) {
                evt.consume();
                return;
            }
        }
        if (key == KeyEvent.VK_DELETE) {
            int cp = shellTextPane.getCaretPosition();
            if (cp < guardPos) {
                evt.consume();
                return;
            }
        }
        if (evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            int cp = shellTextPane.getCaretPosition();
            int index = cp - guardPos; // gives exact index of char we need to delete
            int len = command.length();
            if (index <= len) {
                command.deleteCharAt(index - 1);
                hintsUpdate();
            }
            return;
        }
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            int cp = shellTextPane.getCaretPosition();
            int index = cp - guardPos; // gives exact index of char we need to delete
            int len = command.length();
            if (index < len) {
                command.deleteCharAt(index);
                hintsUpdate();
            }
            return;
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (evt.isControlDown()) {
                if (hintsList.size() > 0) {
                    updateCommandFromHistroy(hintsList.get(hintIndex));
                    tryHighlighting();
                }
            }
            if (command.length() > 0) {
                try {
                    executeCommand(command.toString());
                } catch (ShellException ex) {
                    append("\n" + ex.getLocalizedMessage(), STYLE_ERROR);
                }
                command.setLength(0);
                hintsUpdate();
            }
            prompt();
            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
            evt.consume();
            return;
        }

        //    if (evt.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
        //        char ch = evt.getKeyChar();
        //        int cp = shellTextPane.getCaretPosition();
        //        if (cp < guardPos) {
        //            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
        //            cp = shellTextPane.getCaretPosition();
        //        }
        //        int index = cp - guardPos; // gives exact index of char we need to delete
        //        int len = command.length();
        //        if (index < len) {
        //            command.insert(index, ch);
        //            hintsUpdate();
        //        } else {
        //            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
        //            command.append(ch);
        //            Utils.sysOut("Appending '" + ch + "'  " + (int)ch);
        //            hintsUpdate();
        //        }
        //
        //    }
}//GEN-LAST:event_shellTextPaneKeyPressed

    private void processKeyChar(char ch, KeyEvent evt) {
//        if (ch == 10 || ch == 13) {
//            if (evt != null && evt.isControlDown()) {
//                if (hintsList.size() > 0) {
//                    updateCommandFromHistroy(hintsList.get(hintIndex));
//                    tryHighlighting();
//                }
//            }
//            if (command.length() > 0) {
//                executeCommand(command.toString());
//                command.setLength(0);
//                hintsUpdate();
//            }
//            prompt();
//            shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
//            if(evt != null) {
//                evt.consume();
//            }
//            return;
//        }

        if (evt != null && evt.isControlDown()) {
            return;
        }
        if (ch >= 32 && ch <= 126) {
            int cp = shellTextPane.getCaretPosition();
            if (cp < guardPos) {
                shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
                cp = shellTextPane.getCaretPosition();
            }
            int index = cp - guardPos; // gives exact index of char we need to delete
            int len = command.length();
            if (index < len) {
                command.insert(index, ch);
                hintsUpdate();
            } else {
                shellTextPane.setCaretPosition(shellTextPane.getDocument().getLength());
                command.append(ch);
                hintsUpdate();
            }
            tryHighlighting();
        }
//    }
    }
    private ArrayList<String> hintsList = new ArrayList<String>();
    private int hintIndex = 0;

    private void hintsUpdate() {
        hintsList.clear();
        hintIndex = -1;
        String cstr = command.toString();
        if (cstr.length() > 0) {
            for (String str : history) {
                if (str.startsWith(cstr)) {
                    hintsList.add(str);
                }
            }
        }
        if (hintsList.size() > 0) {
            hintIndex = 0;
            hintLabel.setText(NbBundle.getMessage(ShellPanel.class,
                    "HINT", hintIndex + 1, hintsList.size(), //NOI18N
                    hintsList.get(hintIndex)));
        } else {
            hintLabel.setText(" "); //NOI18N
        }
    }

    private void nextHint() {
        if (hintsList.size() <= 0) {
            return;
        }
        if (hintIndex < hintsList.size() - 1) {
            hintIndex++;
            hintLabel.setText(NbBundle.getMessage(ShellPanel.class,
                    "HINT", hintIndex + 1, hintsList.size(), //NOI18N
                    hintsList.get(hintIndex)));
        }
    }

    private void previousHint() {
        if (hintsList.size() <= 0) {
            return;
        }

        if (hintIndex > 0) {
            hintIndex--;
            hintLabel.setText(NbBundle.getMessage(ShellPanel.class,
                    "HINT", hintIndex + 1, hintsList.size(), //NOI18N
                    hintsList.get(hintIndex)));
        }
    }

    private void tryHighlighting() {
        int len = command.length();
        int i = 0;
        int s = 0;
        int e = 0;
        StyledDocument sd = shellTextPane.getStyledDocument();
        while (i < len) {
            while (i < len && command.charAt(i) == ' ') { //NOI18N
                i++;
            }
            s = i;
            while (i < len && command.charAt(i) != ' ') { //NOI18N
                i++;
                e = i;
                String token = command.substring(s, e);
                if (commandManager.isValidCommand(token)) {
                    sd.setCharacterAttributes(guardPos + s, e - s, shellTextPane.getStyle(STYLE_KEYWORD), true);
                } else {
                    sd.setCharacterAttributes(guardPos + s, e - s, shellTextPane.getStyle(STYLE_NORMAL), true);
                }
            }
        }
    }
    //shellTextPane.repaint();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hintLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private org.netbeans.modules.javacard.shell.NoWrapTextPane shellTextPane;
    // End of variables declaration//GEN-END:variables
    private String PROMPT = "\n>"; //NO18N

    private void updateCommandFromHistroy(String nc) {
        shellTextPane.setSelectionStart(guardPos);
        shellTextPane.setSelectionEnd(guardPos + command.length());
        shellTextPane.replaceSelection(nc);
        command.setLength(0);
        command.append(nc);
    }

    public void onStateChange(Card card, CardState old, CardState nue) {
        shellTextPane.setEnabled(nue == CardState.RUNNING || nue == CardState.RUNNING_IN_DEBUG_MODE);
    }

    void removeFromCard() {
        card.removeCardStateObserver(this);
    }
}
