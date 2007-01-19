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
package org.netbeans.modules.versioning.diff;

import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ExtCaret;
import org.netbeans.api.diff.Difference;
import org.openide.text.CloneableEditorSupport;

import javax.swing.*;
import javax.swing.text.*;
import java.io.StringReader;
import java.io.IOException;
import java.awt.*;

/**
 * @author Maros Sandor
 */
class DiffTooltipContentPanel extends JComponent {

    public DiffTooltipContentPanel(JTextComponent parentPane, String mimeType, Difference diff) {
        
        JEditorPane originalTextPane = new JEditorPane();

        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        originalTextPane.setEditorKit(kit);

        Document xdoc = kit.createDefaultDocument();
        if (!(xdoc instanceof StyledDocument)) {
            xdoc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            originalTextPane.setEditorKit(kit);
        }

        StyledDocument doc = (StyledDocument) xdoc;
        try {
            kit.read(new StringReader(diff.getFirstText()), doc, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        originalTextPane.setDocument(doc);
        originalTextPane.setEditable(false);
        Color color = (diff.getType() == Difference.DELETE) ? new Color(255, 225, 232) : new Color(233, 241, 255);
        originalTextPane.setBackground(color);

        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(originalTextPane);

        Element rootElement = org.openide.text.NbDocument.findLineRootElement(doc);
        int lineCount = rootElement.getElementCount();
        int height = eui.getLineHeight() * lineCount;

        int maxWidth = 0;
        for(int line = 0; line < lineCount; line++) {
            Element lineElement = rootElement.getElement(line);
            String text = null;
            try {
                text = doc.getText(lineElement.getStartOffset(), lineElement.getEndOffset() - lineElement.getStartOffset());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            int lineLength = parentPane.getFontMetrics(parentPane.getFont()).stringWidth(text);
            if (lineLength > maxWidth) maxWidth = lineLength;
        }

        if (maxWidth < 50) maxWidth = 50;   // too thin component causes repaint problems
        originalTextPane.setPreferredSize(new Dimension(maxWidth * 7 / 6, height));

        if (!originalTextPane.isEditable()) {
            originalTextPane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        }

        JScrollPane jsp = new JScrollPane(originalTextPane);
        jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        setLayout(new BorderLayout());
        add(jsp);
    }
}
