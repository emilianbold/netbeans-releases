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
package org.netbeans.modules.debugger.ui;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * A GUI panel for customizing a Watch.
 *
 * @author Maros Sandor
 */
public class WatchPanel {

    private JPanel panel;
    private JTextComponent editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }

    public JComponent getPanel() {
        if (panel != null) return panel;

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString ("CTL_Watch_Name")); // NOI18N
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);

        final FileObject file = EditorContextDispatcher.getDefault().getMostRecentFile();
        int line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
        String mimeType = file != null ? file.getMIMEType() : "text/plain"; // NOI18N
        boolean doBind = true;
        if (!mimeType.startsWith("text/")) { // NOI18N
            // If the current file happens to be of unknown or not text MIME type, use the ordinary text one.
            mimeType = "text/plain"; // NOI18N
            doBind = false; // Do not do binding to an unknown file content.
        }

        //Add JEditorPane and context
        JComponent [] editorComponents = Utilities.createSingleLineEditor(mimeType);
        JScrollPane sp = (JScrollPane) editorComponents[0];
        editorPane = (JTextComponent) editorComponents[1];
        int h = sp.getPreferredSize().height;
        int w = Math.min(70*editorPane.getFontMetrics(editorPane.getFont()).charWidth('a'),
                         org.openide.windows.WindowManager.getDefault().getMainWindow().getSize().width);
        sp.setPreferredSize(new Dimension(w, h));
        if (doBind) {
            line = adjustLine(file, line);
            if (file != null && line >= 0) {
                DialogBinding.bindComponentToFile(file, line - 1, 0, 0, editorPane);
            }
            final int theLine = line;
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    final int adjustedLine = adjustLine(file, theLine);
                    if (adjustedLine != theLine) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                DialogBinding.bindComponentToFile(file, adjustedLine - 1, 0, 0, editorPane);
                            }
                        });
                    }
                }
            });
        }
        panel.add (BorderLayout.CENTER, sp);
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        String t = Utils.getIdentifier ();
        if (t != null) {
            editorPane.setText (t);
        } else {
            editorPane.setText (expression);
        }
        editorPane.selectAll ();

        textLabel.setLabelFor (editorPane);
        editorPane.requestFocus ();
        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }

    public static int adjustLine(FileObject fo, int theLine) {
        if (theLine == -1) {
            theLine = 1;
        }
        if (fo == null) {
            return theLine;
        }
        if (!"java".equalsIgnoreCase(fo.getExt())) {
            // we do not understand other languages
            return theLine;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
        } catch (FileNotFoundException ex) {
            return theLine;
        }
        try {
            int line = findClassLine(br);
            line = findMethodLine(line, br);
            if (theLine < line) {
                theLine = line;
            }
        } catch (IOException ioex) {
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
            }
        }
        return theLine;
    }

    private static int findClassLine(BufferedReader br) throws IOException {
        int l = 1;
        String line;
        boolean comment = false;
        boolean classDecl = false;
        for (; (line = br.readLine()) != null; l++) {
            if (classDecl) {
                if (line.indexOf('{') >= 0) {
                    return l + 1;
                } else {
                    continue;
                }
            }
            boolean slash = false;
            boolean asterix = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (comment) {
                    if (asterix && c == '/') {
                        comment = false;
                        asterix = false;
                        continue;
                    }
                    asterix = c == '*';
                    continue;
                }
                if (slash && c == '*') {
                    comment = true;
                    slash = false;
                    continue;
                }
                if (c == '/') {
                    if (slash) {
                        // comment, ignore the rest of the line
                        break;
                    }
                    slash = true;
                }
                if (c == 'c' && line.length() >= (i+"class".length()) && "lass".equals(line.substring(i+1, i+5))) {
                    // class declaration
                    classDecl = true;
                    if (line.indexOf('{', i+5) > 0) {
                        return l + 1;
                    }
                }
            }
        }
        return 1; // Did not find anything interesting
    }

    private static int findMethodLine(int l, BufferedReader br) throws IOException {
        int origLine = l;
        String line;
        boolean isParenthesis = false;
        boolean isThrows = false;
        for (; (line = br.readLine()) != null; l++) {
            int i = 0;
            if (!isParenthesis && (i = line.indexOf(')')) >= 0 || isParenthesis) {
                isParenthesis = true;
                if (!isThrows) {
                    for (i++; i < line.length() && Character.isWhitespace(line.charAt(i)); i++) ;
                    if ((i+"throws".length()) < line.length() && "throws".equals(line.substring(i, i+"throws".length()))) {
                        isThrows = true;
                    }
                }
                if (isThrows) {
                    i = line.indexOf("{", i);
                    if (i < 0) i = line.length();
                }
                if (i < line.length()) {
                    if (line.charAt(i) == '{') {
                        return l;
                    } else {
                        isParenthesis = false;
                    }
                }
            }
        }
        return origLine;
    }

}
