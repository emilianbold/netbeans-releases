/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.repl;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
public class LogHighlight extends AbstractHighlightsContainer 
        implements DocumentListener, CaretListener {
    private final OffsetsBag      highlights;
    private final JTextComponent  component;
    private AttributeSet   messageAttrs;
    private AttributeSet   outputAttrs;
    private AttributeSet   errorAttrs;
    
    private int  rememberedDotPos;
    private boolean inited;

    public LogHighlight(JTextComponent component) {
        this.component = component;
        Document d = getDocument();
        if (d != null) {
            this.highlights = new OffsetsBag(d);
            d.addDocumentListener(WeakListeners.create(DocumentListener.class, this, d));
        } else {
            this.highlights = new OffsetsBag(new PlainDocument());
        }
        messageAttrs = AttributesUtilities.createImmutable(
            StyleConstants.Background, new Color(240, 240, 240), 
            ATTR_EXTENDS_EOL, Boolean.valueOf(true));
        outputAttrs = AttributesUtilities.createImmutable(
            StyleConstants.Background, new Color(255, 255, 204), 
            ATTR_EXTENDS_EOL, Boolean.valueOf(true));
        errorAttrs = AttributesUtilities.createImmutable(messageAttrs,
                AttributesUtilities.createImmutable(
            StyleConstants.Foreground, Color.red));
    }
    
    private boolean init() {
        component.addCaretListener(WeakListeners.create(CaretListener.class, this, component));
        return true;
    }
    
    @Override
    public HighlightsSequence getHighlights(int start, int end) {
        if (!inited) {
            inited = init();
        }
        return highlights.getHighlights(start, end);
    }
    
    private Document getDocument() {
        if (component == null) {
            return null;
        }
        return component.getDocument();
    }
    
    public void buildHighlights(int start, int len) {
        Document d = getDocument();
        if (d == null) {
            return;
        }
        d.render(new Runner(d, start, len));
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        
    }
    
    private class Runner implements Runnable {
        private final Document    doc;
        private OffsetsBag   newBag;
        private String[]     lines;
        private final int start;
        private final int changeLen;
        private int lineStart;
        private int previousLineStart;

        public Runner(Document doc, int start, int len) {
            this.doc = doc;
            this.start = start;
            this.changeLen = len;
        }
        
        public void run() {
            newBag = new OffsetsBag(doc);
            try {
                String s = doc.getText(0, doc.getLength());
                lines = s.split("\n");
            } catch (BadLocationException ex) {
                return;
            }
            int caretPos = component.getCaret().getDot();
            previousLineStart = -1;
            for (String l : lines) {
                int len = l.length();
                if (caretPos < lineStart || caretPos > lineStart + len) {
                    processLine(l);
                }
                previousLineStart = lineStart;
                lineStart += len + 1; // newline char
            }
            highlights.setHighlights(newBag);
            
            fireHighlightsChange(start, changeLen);
        }
        
        private void processLine(String l) {
            if (l.isEmpty()) {
                return;
            }
            char c = l.charAt(0);
            if (c == '|') {
                newBag.addHighlight(lineStart, lineStart + l.length() + 1, 
                        messageAttrs);
                
                // if the line contains an error marker, highlight part of the previous line
                Matcher m = Pattern.compile("^\\| *([\\^]-*[\\^]?)", 0).matcher(l);
                if (m.find()) {
                    int s = m.start(1);
                    int e = m.end(1);
                    
                    newBag.addHighlight(previousLineStart + s, previousLineStart + e, errorAttrs);
                    newBag.addHighlight(lineStart + s, lineStart + e, errorAttrs);
                }
            } else if (l.startsWith("->") || l.startsWith(">>")) {
                return;
            } else {
                newBag.addHighlight(lineStart, lineStart + l.length() + 1, 
                        outputAttrs);
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        String s;
        try {
            s = e.getDocument().getText(e.getOffset(), e.getLength());
        } catch (BadLocationException ex) {
            return;
        }
        if (s.contains("\n")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    buildHighlights(e.getOffset(), e.getLength());
                }
            });
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
    /*
    @MimeRegistration(mimeType = "text/x-repl", service = HighlightsLayerFactory.class)
    */
    public static final class F implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context cntxt) {
            return new HighlightsLayer[] {
                HighlightsLayer.create("repl-console",
                        ZOrder.DEFAULT_RACK, true, 
                        new LogHighlight(cntxt.getComponent()))
            };
        }
    
    }
}
