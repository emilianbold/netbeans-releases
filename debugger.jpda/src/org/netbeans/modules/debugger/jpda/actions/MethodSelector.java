/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.debugger.jpda.actions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class MethodSelector implements KeyListener, MouseListener, MouseMotionListener, FocusListener {

    private static AttributeSet defaultHyperlinkHighlight;
    
    private String url;
    private Segment[] segments;
    private int selectedIndex = -1;
    private String hintText;
    private ReleaseListener releaseListener;
    private KeyCode[] stopEvents;
    private KeyCode[] confirmEvents;
    
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;
    
    private AttributeSet attribsArea = null;
    private AttributeSet attribsMethod = null;
    private AttributeSet attribsHyperlink = null;

    private Cursor handCursor;
    private Cursor arrowCursor;
    private Cursor originalCursor;
    
    private JEditorPane editorPane;
    private Document doc;

    private int startLine;
    private int endLine;
    private ArrayList<Annotation> annotations;
    private int mousedIndex = -1;
    private boolean isInSelectMode = false;

    public MethodSelector(String url, Segment[] segments, int initialIndex, ReleaseListener releaseListener) {
        this(url, segments, initialIndex, releaseListener, null, new KeyCode[0], new KeyCode[0]);
    }

    public MethodSelector(String url, Segment[] segments, int initialIndex,
            ReleaseListener releaseListener, String hintText,
            KeyCode[] stopEvents, KeyCode[] confirmEvents) {
        this.url = url;
        this.segments = segments;
        this.selectedIndex = initialIndex;
        this.hintText = hintText;
        this.releaseListener = releaseListener;
        this.stopEvents = stopEvents;
        this.confirmEvents = confirmEvents;
    }

    static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MethodSelector.class);
        if (bag == null) {
            doc.putProperty(MethodSelector.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }
    
    public boolean showUI() {
        DataObject dobj = getDataObject(url);
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JEditorPane[] openedPanes = ec.getOpenedPanes();
                    if (openedPanes != null) {
                        editorPane = openedPanes[0];
                    }
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (editorPane == null) {
            return false; // cannot do anything without editor
        }
        doc = editorPane.getDocument();
        // compute start line and end line
        int minOffs = Integer.MAX_VALUE;
        int maxOffs = 0;
        for (int x = 0; x < segments.length; x++) {
            minOffs = Math.min(segments[x].getStartOffset(), minOffs);
            maxOffs = Math.max(segments[x].getEndOffset(), maxOffs);
        }
        try {
            startLine = Utilities.getLineOffset((BaseDocument)doc, minOffs) + 1;
            endLine = Utilities.getLineOffset((BaseDocument)doc, maxOffs) + 1;
        } catch (BadLocationException e) {
        }
        // continue by showing method selection ui
        editorPane.putClientProperty(MethodSelector.class, this);
        editorPane.addKeyListener(this);
        editorPane.addMouseListener(this);
        editorPane.addMouseMotionListener(this);
        editorPane.addFocusListener(this);
        originalCursor = editorPane.getCursor();
        handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        arrowCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        editorPane.setCursor(arrowCursor);
        Caret caret = editorPane.getCaret();
        if (caret instanceof BaseCaret) {
            ((BaseCaret)caret).setVisible(false);
        }
        annotateLines();
        requestRepaint();
        if (hintText != null && hintText.trim().length() > 0) {
            Utilities.setStatusText(editorPane, " " + hintText);
        }
        isInSelectMode = true;
        return true;
    }

    public synchronized void releaseUI(boolean performAction) {
        getHighlightsBag(doc).clear();
        editorPane.removeKeyListener(this);
        editorPane.removeMouseListener(this);
        editorPane.removeMouseMotionListener(this);
        editorPane.removeFocusListener(this);
        editorPane.putClientProperty(MethodSelector.class, null);
        editorPane.setCursor(originalCursor);
        Caret caret = editorPane.getCaret();
        if (caret instanceof BaseCaret) {
            ((BaseCaret)caret).setVisible(true);
        }
        clearAnnotations();

        if (hintText != null && hintText.trim().length() > 0) {
            Utilities.clearStatusText(editorPane);
        }
        isInSelectMode = false;
        if (releaseListener != null) {
            releaseListener.released(performAction);
        }
    }

    public boolean isUIActive() {
        return isInSelectMode;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private DataObject getDataObject(String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            return null;
        }
        if (file == null) {
            return null;
        }
        try {
            return DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    private void requestRepaint() {
        if (attribsLeft == null) {
            Color foreground = editorPane.getForeground();

            attribsLeft = createAttribs(EditorStyleConstants.LeftBorderLineColor, foreground, EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);
            attribsRight = createAttribs(EditorStyleConstants.RightBorderLineColor, foreground, EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);
            attribsMiddle = createAttribs(EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);
            attribsAll = createAttribs(EditorStyleConstants.LeftBorderLineColor, foreground, EditorStyleConstants.RightBorderLineColor, foreground, EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);

            attribsHyperlink = getHyperlinkHighlight();
            
            attribsMethod = createAttribs(StyleConstants.Foreground, foreground,
                    StyleConstants.Bold, Boolean.TRUE);
            
            attribsArea = createAttribs(
                    StyleConstants.Foreground, foreground,
                    StyleConstants.Italic, Boolean.FALSE,
                    StyleConstants.Bold, Boolean.FALSE);
        }
        
        OffsetsBag newBag = new OffsetsBag(doc, true);
        int start = segments[0].getStartOffset();
        int end = segments[segments.length - 1].getEndOffset();
        newBag.addHighlight(start, end, attribsArea);
        
        for (int i = 0; i < segments.length; i++) {
            int startOffset = segments[i].getStartOffset();
            int endOffset = segments[i].getEndOffset();
            newBag.addHighlight(startOffset, endOffset, attribsMethod);
            if (selectedIndex == i) {
                int size = endOffset - startOffset;
                if (size == 1) {
                    newBag.addHighlight(startOffset, endOffset, attribsAll);
                } else if (size > 1) {
                    newBag.addHighlight(startOffset, startOffset + 1, attribsLeft);
                    newBag.addHighlight(endOffset - 1, endOffset, attribsRight);
                    if (size > 2) {
                        newBag.addHighlight(startOffset + 1, endOffset - 1, attribsMiddle);
                    }
                }
            }
            if (mousedIndex == i) {
                AttributeSet attr = AttributesUtilities.createComposite(
                    attribsHyperlink,
                    AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, new TooltipResolver())
                );
                newBag.addHighlight(startOffset, endOffset, attr);
            }
        }
        
        OffsetsBag bag = getHighlightsBag(doc);
        bag.setHighlights(newBag);
    }

    private void annotateLines() {
        annotations = new ArrayList<Annotation>();
        // [TODO]
//        for (int lineNum = startLine; lineNum <= endLine; lineNum++) {
//            if (lineNum != currentLine) {
//                Object anno = context.annotate(url, lineNum, annoType, null);
//                if (anno instanceof Annotation) {
//                    annotations.add((Annotation)anno);
//                }
//            } // if
//        } // for
    }
    
    private void clearAnnotations() {
        if (annotations != null) {
            for (Annotation anno : annotations) {
                anno.detach();
            }
        }
    }
    
    private AttributeSet createAttribs(Object... keyValuePairs) {
        List<Object> list = new ArrayList<Object>();
        for (int i = keyValuePairs.length / 2 - 1; i >= 0; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            if (attrKey != null && attrValue != null) {
                list.add(attrKey);
                list.add(attrValue);
            }
        }
        return AttributesUtilities.createImmutable(list.toArray());
    }

    private AttributeSet getHyperlinkHighlight() {
        //FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        //AttributeSet hyperlinksHighlight = fcs.getFontColors("hyperlinks"); //NOI18N
        synchronized(this) {
            if (defaultHyperlinkHighlight == null) {
                defaultHyperlinkHighlight = AttributesUtilities.createImmutable(
                        StyleConstants.Foreground, Color.BLUE, StyleConstants.Underline, Color.BLUE);
            }
        }
        return defaultHyperlinkHighlight;
    }
    
    // **************************************************************************
    // KeyListener implementation
    // **************************************************************************
    
    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        boolean consumeEvent = true;
        switch (code) {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                // selection confirmed
                releaseUI(true);
                break;
            case KeyEvent.VK_ESCAPE:
                // action canceled
                releaseUI(false);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_TAB:
                selectedIndex++;
                if (selectedIndex == segments.length) {
                    selectedIndex = 0;
                }
                requestRepaint();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_UP:    
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = segments.length - 1;
                }
                requestRepaint();
                break;
            case KeyEvent.VK_HOME:
                selectedIndex = 0;
                requestRepaint();
                break;
            case KeyEvent.VK_END:
                selectedIndex = segments.length - 1;
                requestRepaint();
                break;
            default:
                int mods = e.getModifiersEx();
                for (int x = 0; x < stopEvents.length; x++) {
                    if (stopEvents[x].getCode() == code &&
                            (stopEvents[x].getModifiers() & mods) == stopEvents[x].getModifiers()) {
                        releaseUI(false);
                        consumeEvent = false;
                        break;
                    }
                }
                for (int x = 0; x < confirmEvents.length; x++) {
                    if (confirmEvents[x].getCode() == code &&
                            (confirmEvents[x].getModifiers() & mods) == confirmEvents[x].getModifiers()) {
                        releaseUI(true);
                        break;
                    }
                }
        }
        if (consumeEvent) {
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    // **************************************************************************
    // MouseListener and MouseMotionListener implementation
    // **************************************************************************
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            return;
        }
        e.consume();
        int position = editorPane.viewToModel(e.getPoint());
        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
            if (position < 0) {
                return;
            }
            if (mousedIndex != -1) {
                selectedIndex = mousedIndex;
                releaseUI(true);
                return;
            }
        }
        try {
            int line = Utilities.getLineOffset((BaseDocument) doc, position) + 1;
            if (line < startLine || line > endLine) {
                releaseUI(false);
                return;
            }
        } catch (BadLocationException ex) {
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        e.consume();
        int position = editorPane.viewToModel(e.getPoint());
        int newIndex = -1;
        if (position >= 0) {
            for (int x = 0; x < segments.length; x++) {
                int start = segments[x].getStartOffset();
                int end = segments[x].getEndOffset();
                if (position >= start && position <= end) {
                    newIndex = x;
                    break;
                }
            } // for
        } // if
        if (newIndex != mousedIndex) {
            if (newIndex == -1) {
                editorPane.setCursor(arrowCursor);
            } else {
                editorPane.setCursor(handCursor);
            }
            mousedIndex = newIndex;
            requestRepaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        e.consume();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        e.consume();
    }

    // **************************************************************************
    // FocusListener implementation
    // **************************************************************************
    
    @Override
    public void focusGained(FocusEvent e) {
        editorPane.getCaret().setVisible(false);
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    
    // **************************************************************************
    // inner classes
    // **************************************************************************

    public static class Segment {
        int startOffset;
        int endOffset;
        
        public Segment(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        /**
         * @return the startOffset
         */
        public int getStartOffset() {
            return startOffset;
        }

        /**
         * @return the endOffset
         */
        public int getEndOffset() {
            return endOffset;
        }

    } // Segment
    public static class KeyCode {
        int code;
        int modifiers;

        public KeyCode(int code, int modifiers) {
            this.code = code;
            this.modifiers = modifiers;
        }

        /**
         * @return the code
         */
        public int getCode() {
            return code;
        }

        /**
         * @return the modifiers
         */
        public int getModifiers() {
            return modifiers;
        }

    }

    public interface ReleaseListener {

        public void released(boolean performAction);

    }

    private static final class TooltipResolver implements HighlightAttributeValue<String> {

        public TooltipResolver() {
        }

        @Override
        public String getValue(JTextComponent component, Document document, Object attributeKey, int startOffset, int endOffset) {
            return NbBundle.getMessage(MethodSelector.class, "MSG_Step_Into_Method");
        }
        
    }
    
}
