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

 /*
 * CodeSnippetPanel.java
 *
 * Created on 01.02.2011, 14:23:31
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.diagnostics.clank.ui.Utilities;
import org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet.CodeSnippet.AnnotatedCode;
import org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet.CodeSnippet.AnnotatedCode.LineInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CodeSnippetPanel extends javax.swing.JPanel {

    /*package*/
    static final String CODE_SNIPPET_PROPERTY = "CODE_SNIPPET_PROPERTY"; // NOI18N
//    private final ResultElement element;
    private final CodeSnippet snippet;
    private final ActionListener goToSnippetListener;
    private final boolean annotatedLine;
    private ArrayList<HighlightInfo> highlights = new ArrayList<>();

    /**
     * Creates new form CodeSnippetPanel
     */
    public CodeSnippetPanel(CodeSnippet snippet, boolean annotatedLine) {
        this.annotatedLine = annotatedLine;
        this.snippet = snippet;
        this.goToSnippetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CodeSnippetPanel.class, "OpeningFile"));//NOI18N
                RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        FileObject fo = snippet.getFileObject();
                        if (fo == null) {
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CodeSnippetPanel.class, "CannotOpen", snippet.getFilePath()));//NOI18N
                        } else {
                            Utilities.show(fo, snippet.getLine());
                        }
                    }
                });
            }
        };
        initComponents();
        showCode(true, true);
    }

    @Override
    public void setInheritsPopupMenu(boolean value) {
        super.setInheritsPopupMenu(value);
        this.codeNavPanel.setInheritsPopupMenu(value);
        this.codeTextPane.setInheritsPopupMenu(value);
    }

    private void cleanDocument(JTextComponent textComp) throws BadLocationException {
        Document doc = codeTextPane.getDocument();
        if (doc != null && doc.getLength() > 0) {
            textComp.getHighlighter().removeAllHighlights();
            doc.remove(0, doc.getLength());
            HintsController.setErrors(doc, CodeSnippetPanel.class.getName(), Collections.<ErrorDescription>emptyList());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codeNavPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        codeTextPane = createEditorPane();

        setLayout(new java.awt.BorderLayout());

        codeNavPanel.setBackground(Utilities.getDefaultBackground());

        javax.swing.GroupLayout codeNavPanelLayout = new javax.swing.GroupLayout(codeNavPanel);
        codeNavPanel.setLayout(codeNavPanelLayout);
        codeNavPanelLayout.setHorizontalGroup(
            codeNavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 349, Short.MAX_VALUE)
        );
        codeNavPanelLayout.setVerticalGroup(
            codeNavPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        add(codeNavPanel, java.awt.BorderLayout.NORTH);

        codeTextPane.setEditable(false);
        codeTextPane.setFocusable(false);
        scrollPane.setViewportView(codeTextPane);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenFileActionPerformed(java.awt.event.ActionEvent evt) {
        goToSnippetListener.actionPerformed(new ActionEvent(this.snippet, 0, ""));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel codeNavPanel;
    private javax.swing.JTextPane codeTextPane;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    private static final String ANALYTICS_CODESNIPPET_MARKER = "ClankDiagnosticsCodeSnippetMarker";//NOI18N

    public static JTextPane createEditorPane() {
        initColors();
   //     if (true) {
            final JTextPane out = new JTextPane();
            out.setFont(editorFont);
            out.setBackground(Utilities.getDefaultBackground());
            out.putClientProperty(ANALYTICS_CODESNIPPET_MARKER, Boolean.TRUE);
            //out.setContentType("text/html");//NOI18N
            out.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            out.setEditable(false);
            return out;
     //   }
//        JComponent[] editor = org.netbeans.editor.Utilities.createSingleLineEditor("text/x-c++");//NOI18N
//        JEditorPane pane = (JEditorPane) editor[1];
//        pane.putClientProperty(ANALYTICS_CODESNIPPET_MARKER, Boolean.TRUE);
//        // not supported yet by editor, but leave it here...
//        pane.putClientProperty(SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE, Boolean.FALSE);
//        pane.putClientProperty(SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE, Boolean.FALSE);
//        pane.getPreferredSize();
//        return pane;
    }

    public static boolean isCodeSnippet(JComponent comp) {
        return comp.getClientProperty(ANALYTICS_CODESNIPPET_MARKER) == Boolean.TRUE;
    }

    public final void showCode(boolean showCode, boolean viewed) {
        codeTextPane.setVisible(showCode);
        Font codeFont = codeTextPane.getFont().deriveFont(viewed ? Font.PLAIN : Font.BOLD);
        codeTextPane.setFont(codeFont);
        if (showCode) {
            String message = NbBundle.getMessage(CodeSnippetPanel.class, "CodeSnippetPanel.code.loading");//NOI18N
            try {
                Document doc = codeTextPane.getDocument();
                cleanDocument(codeTextPane);
                doc.insertString(0, message, null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (snippet != null && snippet.isAnnotatedCodeReady()) {
                try {
                    processInitializedCodeSnippets(Collections.singleton(snippet.getCode()));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                new CodeSnippetWorker().execute();
            }
//            else if (element != null) {
//                new CodeSnippetWorker(element).execute();
//            }
        }
        codeTextPane.setFont(codeFont);
        codeTextPane.invalidate();
        revalidate();
    }

    private void setCodeSnippetText(CodeSnippet.AnnotatedCode code, boolean clean) {
//        codeTextPane.setEditorKit(MimeLookup.getLookup(mime).lookup(EditorKit.class));
        try {
            initColors();

            final StyledDocument doc = codeTextPane.getStyledDocument();
            if (clean) {
                cleanDocument(codeTextPane);
            }
            final Iterator<LineInfo> lines = code.getLines().iterator();
//            boolean nextLineIsAnnotated = false;
//            int startHL = -1;
//            int endHL = -1;
            //we will goo through lines to annotate and add highlight attribute
            //in parallel we will colorize it
            Language<CppTokenId> languageCpp = CppTokenId.languageCpp();
            //int docLength = 0;
            while (lines.hasNext()) {
                CodeSnippet.AnnotatedCode.LineInfo line = lines.next();
                if (line.getType() == CodeSnippet.AnnotatedCode.LineType.ANNOTATION && annotatedLine) {
                    int startHL = doc.getLength();
                    doc.insertString(doc.getLength(), line.getPrefix(), null);
                    int lineStartOffsetInDoc = doc.getLength();
                    final String text = line.getText();
                    //for cycle
                    int[] startColumns = line.getStartHLColumns();
                    int[] endColumns = line.getEndHLColumns();
//                    int start = 0;
//                    int[] startUs = new int[startColumns.length];
//                    int[] endUs = new int[startColumns.length];
                    doc.insertString(doc.getLength(), text, null);
                    for (AnnotatedCode.LineAttribute attr : line.attrs) {
                        doc.setCharacterAttributes(lineStartOffsetInDoc + attr.column, attr.length, attr.attribute, true);
                    }                       
                    int endHL = doc.getLength();
                    
                    for (int i = 0; i < startColumns.length; i++) {
//                        int start_ = Math.max(start, startColumns[i] - 1);
                        addAttribute(lineStartOffsetInDoc + startColumns[i] - 1, endColumns[i] == -1 ? endHL -1  : lineStartOffsetInDoc + endColumns[i] -1, 
                                CodeSnippet.COLORIZATION_ENABLED ? Painter.FILLED_RECTANGLE : Painter.RECTANGLE);
                    }                    
//                        if (start < startColumns[i] -1) {
//                            doc.insertString(doc.getLength(), text.substring(start, startColumns[i] -1), null);  
//                        }
//                        
//                        startUs[i] = doc.getLength();
//                        doc.insertString(doc.getLength(), endColumns[i] - 1 < start_ ? 
//                                text.substring(start_): text.substring(start_, endColumns[i] - 1), null);
//                        endUs[i] = doc.getLength();
//                        start = endColumns[i] -1;
//                    }
//                    if (endColumns[startColumns.length-1] -1 >=  startColumns[startColumns.length-1]) {
//                        doc.insertString(doc.getLength(), text.substring( endColumns[startColumns.length-1] -1), null); 
//                    }
//                    //addAttribute(startHL, endHL, Painter.HIGHLIGHT);
//                    for (int i = 0; i < startUs.length; i++) {
//                        addAttribute(startUs[i], endUs[i], Painter.RECTANGLE);
//                    }
                    addAttribute(startHL, endHL, Painter.HIGHLIGHT);
                 
                } else {
                    doc.insertString(doc.getLength(), line.getPrefix(), null);
                    int lineStartOffsetInDoc = doc.getLength();
                    doc.insertString(doc.getLength(), line.getText(), null);
                    for (AnnotatedCode.LineAttribute attr : line.attrs) {
                        doc.setCharacterAttributes(lineStartOffsetInDoc + attr.column, attr.length, attr.attribute, true);
                    }                    
                    
                }
                                
                if (lines.hasNext()) {
                    doc.insertString(doc.getLength(), "\n", null);//NOI18N
                }
            }
            for (HighlightInfo highlight : highlights) {
                markGuarded(codeTextPane, highlight.startOffset, highlight.endOffset, highlight.getPainter());
            }
//            if (startHL >= 0) {
//                markGuarded(codeTextPane, startHL, endHL);
//            }
            codeTextPane.repaint();
            codeTextPane.setCaretPosition(0);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void addAttribute(int startOffset, int endOffset, Painter painter) {
        HighlightInfo i = new HighlightInfo();
        switch (painter) {
            case HIGHLIGHT:
                i.painter = CodeSnippetPanel.painter;
                break;
            case WAVE_UNDERLINE:
                i.painter = CodeSnippetPanel.squigglePainter;
                break;
            case RECTANGLE:
                i.painter = CodeSnippetPanel.rectanglePainter;
                break;                
            case FILLED_RECTANGLE:
                i.painter = CodeSnippetPanel.filledRectanglePainter;
                break;                                
        }
        i.startOffset = startOffset;
        i.endOffset = endOffset;       
        highlights.add(i);
    }

    private void processInitializedCodeSnippets(Collection<AnnotatedCode> chunks) {
        boolean clean = true;
        for (CodeSnippet.AnnotatedCode code : chunks) {
            setCodeSnippetText(code, clean);
            clean = false;
        }
        this.firePropertyChange(CODE_SNIPPET_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }

    void updateViewedStatus(boolean viewed) {
        Font codeFont = codeTextPane.getFont().deriveFont(viewed ? Font.PLAIN : Font.BOLD);
        codeTextPane.setFont(codeFont);
//        Font linkFont = btnOpenFile.getFont().deriveFont(viewed ? Font.PLAIN : Font.BOLD);
//        btnOpenFile.setFont(linkFont);
    }
    
    private void markGuarded(JTextComponent textComp, int start, int end) throws BadLocationException {
        textComp.getHighlighter().addHighlight(start, end, painter);
        textComp.getHighlighter().addHighlight(start, end, squigglePainter);        
    }    

    private void markGuarded(JTextComponent textComp, int start, int end, Highlighter.HighlightPainter painter) throws BadLocationException {
        textComp.getHighlighter().addHighlight(start, end, painter);
//        textComp.getHighlighter().addHighlight(start, end, squigglePainter);
//        textComp.getHighlighter().addHighlight(start+1, start+6, thePainter);

//        textComp.getHighlighter().addHighlight(end, end, painter)
    }

    private final class CodeSnippetWorker extends SwingWorker<Collection<CodeSnippet>, CodeSnippet.AnnotatedCode> {

//        private final ResultElement element;
        public CodeSnippetWorker() {//ResultElement element) {
            //this.element = element;
        }

        @Override
        protected Collection<CodeSnippet> doInBackground() throws Exception {
            publish(snippet.getCode());
            return Collections.singleton(snippet);
        }

        @Override
        protected void process(List<CodeSnippet.AnnotatedCode> chunks) {
            processInitializedCodeSnippets(chunks);
        }

        @Override
        protected void done() {
            revalidate();
        }
    }

    private static Highlighter.HighlightPainter painter;
    private static SquigglePainter squigglePainter;
    private static RectanglePainter rectanglePainter;
    private static FillRectanglePainter filledRectanglePainter;
    private static Font editorFont;

    private synchronized static void initColors() {
        if (painter == null) {
            painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(233, 239, 248));
            squigglePainter = new SquigglePainter(Color.getColor("fef65b"));//NOI18N
            rectanglePainter = new RectanglePainter(Color.RED);
            filledRectanglePainter = new FillRectanglePainter(new Color(233, 239, 248).darker());
            String family = "Monospaced";    // NOI18N
            int defaultEditorFontSize = getDefaultFontSize();
            editorFont = new Font(family, Font.PLAIN, defaultEditorFontSize);            
        }
    }

    private static int getDefaultFontSize() {
        Integer defaultFontSize = (Integer) UIManager.get("customFontSize"); // NOI18N
        if (defaultFontSize == null) {
            int s = UIManager.getFont("TextField.font").getSize(); // NOI18N
            if (s < 12) {
                s = 12;
            }
            defaultFontSize = new Integer(s);
        }
        return defaultFontSize.intValue();
    }

    static enum Painter {
        HIGHLIGHT,
        WAVE_UNDERLINE,
        RECTANGLE,
        FILLED_RECTANGLE
    }

    private class HighlightInfo implements Highlighter.Highlight {

        @Override
        public int getStartOffset() {
            return startOffset;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public Highlighter.HighlightPainter getPainter() {
            return painter;
        }

        int startOffset;
        int endOffset;
        Highlighter.HighlightPainter painter;
    }
}
