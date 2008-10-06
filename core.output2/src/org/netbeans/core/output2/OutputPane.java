/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.core.output2;

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import org.netbeans.core.output2.ui.AbstractOutputPane;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import org.openide.util.NbPreferences;


class OutputPane extends AbstractOutputPane implements ComponentListener {
    protected void documentChanged() {
        super.documentChanged();
        findOutputTab().documentChanged();
    }

    protected void caretEnteredLine(int line) {
        findOutputTab().caretEnteredLine(line);
    }

    protected void lineClicked(int line, Point p) {
        if (!(getDocument() instanceof OutputDocument) || !inLeadingOrTrailingWhitespace(line, p)) {
            findOutputTab().lineClicked(line);
        }
    }
    
    private boolean linePressed (int line, Point p) {
        boolean result;
        if (!(getDocument() instanceof OutputDocument) || !inLeadingOrTrailingWhitespace(line, p)) {
            result = findOutputTab().linePressed (line, p);
        } else {
            result = false;
        }
        return result;
    }

    protected void postPopupMenu(Point p, Component src) {
        if (src.isShowing()) {
            findOutputTab().postPopupMenu(p, src);
        }
    }

    public void setMouseLine (int line, Point p) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            boolean link = line != -1 && ((OutputDocument) doc).getLines().isHyperlink(line);
            if (link && p != null) {
                //#47256 - Don't set the cursor if the mouse if over
                //whitespace
                if (inLeadingOrTrailingWhitespace(line, p)) {
                    link = false;
                    line = -1;
                }
            }
            textView.setCursor(link ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                    Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }
        super.setMouseLine(line, p);
    }
    
    private boolean inLeadingOrTrailingWhitespace (int line, Point p) {
        if (line == -1) {
            return true;
        }
        assert getDocument() instanceof OutputDocument;
        assert p != null;
        OutputDocument doc = (OutputDocument) getDocument();
        int lineStart = doc.getLineStart(line);
        int lineEnd = doc.getLineEnd(line);

        try {
            doc.getText (lineStart, lineEnd - lineStart, seg);
            char curr = seg.first();
            while (Character.isWhitespace(curr) && curr != Segment.DONE) {
                lineStart++;
                curr = seg.next();
            }
            curr = seg.last();
            while (Character.isWhitespace(curr) && curr != Segment.DONE) {
                lineEnd--;
                curr = seg.previous();
            }
            if (lineEnd <= lineStart) {
                line = -1;
            } else {
                Rectangle startRect = textView.modelToView(lineStart);
                Rectangle endRect = textView.modelToView(lineEnd);
                if (p.y >= startRect.y && p.y <= endRect.y && isWrapped()) {
                    endRect.x = 0;
                    endRect.width = getWidth();
                }
                boolean cursorIsNotOverLeadingOrTrailingWhitespace = 
                    p.x >= startRect.x && p.y >= startRect.y &&
                    p.x <= endRect.x + endRect.width &&
                    p.y <= endRect.y + endRect.height;
                if (!cursorIsNotOverLeadingOrTrailingWhitespace) {
                    line = -1;
                }
            }
        } catch (BadLocationException e) {
            //do nothing
        }
        return line == -1;
    }
    
    private Segment seg = new Segment();
    
    /**
     * Only calls super if there are hyperlinks in the document to avoid huge
     * numbers of calls to viewToModel if the cursor is never going to be 
     * changed anyway.
     */
    public void mouseMoved (MouseEvent evt) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            if (((OutputDocument) doc).getLines().hasHyperlinks()) {
                super.mouseMoved(evt);
            }
        }
    }
    
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            int pos = textView.viewToModel(e.getPoint());
            if (pos != -1) {
                int line = textView.getDocument().getDefaultRootElement().getElementIndex(pos);
                if (line >= 0) {
                    if (linePressed(line, e.getPoint())) {
                        e.consume();
                        return;
                    }
                }
            }
        }
        //Refine possibly to focus just what is important..
        findOutputTab().setToFocus((Component)e.getSource());
        findOutputTab().requestActive();
    }

    private OutputTab findOutputTab() {
        return  (OutputTab) SwingUtilities.getAncestorOfClass (OutputTab.class, this);
    }

    protected void setDocument (Document doc) {
        if (doc == null) {
            Document d = getDocument();
            if (d != null) {
                d.removeDocumentListener(this);
            }
            textView.setDocument (new PlainDocument());
            return;
        }
        textView.setEditorKit (new OutputEditorKit(isWrapped(), textView));
        super.setDocument(doc);
        updateKeyBindings();
    }
    
    
    public void setWrapped (boolean val) {
        if (val != isWrapped() || !(getEditorKit() instanceof OutputEditorKit)) {
            NbPreferences.forModule(OutputPane.class).putBoolean("wrap", val); //NOI18N
            final int pos = textView.getCaret().getDot();
            Cursor cursor = textView.getCursor();
            try {
                textView.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                setEditorKit (new OutputEditorKit(val, textView));
            } finally {
                textView.setCursor (cursor);
            }
            /*if (val) { #78191
                getViewport().addChangeListener(this);
            } else {
                getViewport().removeChangeListener(this);
            }*/
            
            //Don't try to set the caret position until the view has
            //been fully readjusted to its new dimensions, scroll bounds, etc.
            SwingUtilities.invokeLater (new Runnable() {
                private boolean first = true;
                public void run() {
                    if (first) {
                        first = false;
                        SwingUtilities.invokeLater(this);
                        return;
                    }
                    textView.getCaret().setDot(pos);
                }
            });
            if (getDocument() instanceof OutputDocument && ((OutputDocument) getDocument()).getLines().isGrowing()) {
                lockScroll();
            }
            if (!val) {
                //If there are long lines, it will suddenly get scrolled to the right
                //with the non-wrapping editor kit, so fix that
                getHorizontalScrollBar().setValue(getHorizontalScrollBar().getModel().getMinimum());
            }
            validate();
        }
    }
    
    public boolean isWrapped() {
        if (getEditorKit() instanceof OutputEditorKit) {
            return getEditorKit() instanceof OutputEditorKit 
              && ((OutputEditorKit) getEditorKit()).isWrapped();
        } else {
            return NbPreferences.forModule(OutputPane.class).getBoolean("wrap", false); //NOI18N
        }
    }
    
    private static final boolean GTK = "GTK".equals(UIManager.getLookAndFeel().getID());
    protected JEditorPane createTextView() {
        JEditorPane result = GTK ? new GEP() : new JEditorPane();
        result.addComponentListener(this);
        
        // we don't want the background to be gray even though the text there is not editable
        result.setDisabledTextColor(result.getBackground());
        
        //#83118 - remove the "control shift 0" from editor pane to lt the Open Project action through
        InputMap map = result.getInputMap();
        MyInputMap myMap = new MyInputMap();
        myMap.setParent(map);
        result.setInputMap(result.WHEN_FOCUSED, myMap);
        
        Action act = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                OutputDocument od =(OutputDocument)((JEditorPane)arg0.getSource()).getDocument();
                findOutputTab().inputSent(od.sendLine());
            }
        };
        result.getActionMap().put("SENDLINE", act);
        
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                OutputDocument od =(OutputDocument)((JEditorPane)arg0.getSource()).getDocument();
                findOutputTab().inputSent(od.sendLine());
                findOutputTab().inputEof();
            }
        };
        result.getActionMap().put("EOF", act);
        
        
        return result;
    }
    
    //#83118 - remove the "control shift 0" from editor pane to lt the Open Project action through
    protected class MyInputMap extends  InputMap {
        
        public MyInputMap() {
            super();
        }
        
        public Object get(KeyStroke keyStroke) {
            KeyStroke stroke = KeyStroke.getKeyStroke("control shift O");
            if (keyStroke.equals(stroke)) {
                return null;
            }
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.CTRL_MASK);
            if (keyStroke.equals(stroke)) {
                return null;
            }
            
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            if (keyStroke.equals(stroke)) {
                if (findOutputTab().isInputVisible()) {/* #105954 */
                    return "SENDLINE";
                }
            }
            
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK);
            if (keyStroke.equals(stroke)) {
                return "EOF";
            }
            Object retValue;
            retValue = super.get(keyStroke);
            return retValue;
        }
        
    }
    

    private int prevW = -1;
    public void componentResized(ComponentEvent e) {
        int w = textView.getWidth();
        if (prevW != w) {
            if (isWrapped()) {
                WrappedTextView view = ((OutputEditorKit) getEditorKit()).view();
                if (view != null) {
                    view.setChanged();
                    textView.repaint();
                }
            }
        }
        prevW = w;
    }

    public void componentMoved(ComponentEvent e) {
        //do nothing
    }

    public void componentShown(ComponentEvent e) {
        //do nothing
    }

    public void componentHidden(ComponentEvent e) {
        //do nothing
    }
    
    private static final class GEP extends JEditorPane {
        public java.awt.Color getBackground() {
            return UIManager.getColor("text");
        }
    }

}
