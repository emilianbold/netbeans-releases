/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.livehtml.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.web.domdiff.Change;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class DiffHighlighter implements DocumentListener, CaretListener, MouseListener, MouseMotionListener {

    private static final AttributeSet addedColor =
            AttributesUtilities.createImmutable(StyleConstants.Background,
            new Color(189, 230, 170));
    
    private static final AttributeSet removedColor =
            AttributesUtilities.createImmutable(StyleConstants.Background,
            new Color(252, 157, 159), StyleConstants.StrikeThrough,
            new Color(0, 0, 0));
    
    private static final AttributeSet historyColor =
            AttributesUtilities.createImmutable(StyleConstants.Background,
            new Color(190, 190, 190));
    
    private static final AttributeSet hoverColor =
            AttributesUtilities.createImmutable(StyleConstants.Background,
            new Color(229, 177, 95));
    
    private OffsetsBag bag;
    private OffsetsBag bag2;
    
    private JTextComponent comp;
    private final WeakReference weakDoc;
    
    private List<Change> changes;
    
    private RequestProcessor rp;
    private RequestProcessor.Task lastRefreshTask;    
    private int selectedOffset = -1;
    
    public DiffHighlighter(Document doc, JTextComponent comp) {
        this.comp = comp;
        bag = new OffsetsBag(doc);
        bag2 = new OffsetsBag(doc);
        weakDoc = new WeakReference<Document>((Document) doc);
        rp = new RequestProcessor(DiffHighlighter.class);
        lastRefreshTask = rp.create(new Runnable() {
            @Override
            public void run() {
                updateHover();
            }
        });
        doc.addDocumentListener(this);
        comp.addMouseListener(this);
        comp.addMouseMotionListener(this);
        comp.addCaretListener(this);
    }

    public OffsetsBag getHighlightsBag() {
        return bag;
    }

    public OffsetsBag getHighlightsBag2() {
        return bag2;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateBag();
            }
        });
    }

    private void updateBag() {
        bag.removeHighlights(0, Integer.MAX_VALUE, true);
        changes = (List<Change>)((Document)weakDoc.get()).getProperty(Change.class);
        if (changes != null ) {
            showAddedElements();
        }
    }
    
    private void showAddedElements() {
        for (Change ch : changes) {
            if (ch.isAdd()) {
                bag.addHighlight(ch.getOffset(), ch.getEndOffsetOfNewText(), addedColor);
            } else if (ch.isRemove()){
                bag.addHighlight(ch.getOffset(), ch.getEndOffsetOfRemovedText(), removedColor);
            } else {
                bag.addHighlight(ch.getOffset(), ch.getOffset()+ch.getLength(), historyColor);
            }
        }
    }

    
    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void caretUpdate(CaretEvent e) {
//        if (!(comp instanceof RealContent.MyEditorPane)) {
//            return;
//        }
        selectedOffset = comp.getCaretPosition();
        lastRefreshTask.schedule(300);
    }
    
    private void updateHover() {
        bag2.clear();
        if (!(comp instanceof AnalysisPanel.MyEditorPane)) {
            return;
        }
        int offset = selectedOffset;//comp.getCaretPosition();
        if (changes == null) {
            ((AnalysisPanel.MyEditorPane)comp).showToolTip(-1);
            return;
        }
        for (Change o : changes) {
            if (offset >= o.getOffset() && offset <= o.getOffset()+o.getLength()) {
                if (o.isOrigin()) {
                    for (Change oo : changes) {
                        if (oo.getRevisionIndex() == o.getRevisionIndex()) {
                            bag2.addHighlight(oo.getOffset(), oo.getOffset()+oo.getLength(), hoverColor);
                        }
                    }
                }
                if (o.isOrigin()) {
                    ((AnalysisPanel.MyEditorPane)comp).showToolTip(o.getRevisionIndex());
                } else {
                    ((AnalysisPanel.MyEditorPane)comp).showToolTip();
                }
                return;
            } else if (o.isRemove() && offset >= o.getOffset() && offset <= o.getEndOffsetOfRemovedText()) {
                ((AnalysisPanel.MyEditorPane)comp).showToolTip();
            }
        }
        ((AnalysisPanel.MyEditorPane)comp).showToolTip(-1);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
//        if (!(comp instanceof RealContent.MyEditorPane)) {
//            return;
//        }
        Point p = comp.getMousePosition();
        if (p == null) {
            return;
        }
        int offset = comp.viewToModel(p);
        if (offset == -1) {
            return;
        }
        selectedOffset = offset;
        lastRefreshTask.schedule(300);
   }

}