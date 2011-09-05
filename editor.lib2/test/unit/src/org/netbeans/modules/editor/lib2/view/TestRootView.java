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
package org.netbeans.modules.editor.lib2.view;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import javax.swing.undo.UndoManager;
import org.netbeans.lib.editor.util.GapList;
import org.openide.util.WeakSet;

/**
 *
 * @author Miloslav Metelka
 */
public class TestRootView extends View {

    private final Document doc;
    
    private final JEditorPane pane;
    
    private final DocumentView documentView;
    
    private UndoManager undoManager;
    
    private final BufferedImage bufferedImage;
    
    private TestHighlightsViewFactory testViewFactory;
    
    private Rectangle.Float lastAlloc = new Rectangle.Float();
    
    private int dumpId;
    
    private static int lastId;
    
    private static WeakSet<TestRootView> allRootViews = new WeakSet<TestRootView>();
    
    static {
        EditorViewFactory.registerFactory(new TestHighlightsViewFactory.FactoryImpl());
    }
    
    public static int id(JEditorPane pane) {
        return (Integer) pane.getClientProperty("id");
    }

    public static List<TestHighlight> getHighlights(JTextComponent component) {
        @SuppressWarnings("unchecked")
        List<TestHighlight> highlights = (List<TestHighlight>) component.getClientProperty(TestHighlight.class);
        return highlights;
    }
    
    public void setHighlights(JTextComponent component, List<TestHighlight> highlights) {
        component.putClientProperty(TestHighlight.class, highlights);
    }
    
    public TestRootView() {
        this(new PlainDocument());
    }

    public TestRootView(Document doc) {
        super(doc.getDefaultRootElement());
        this.doc = doc;
        undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        if (undoManager == null) {
            undoManager = new UndoManager();
            doc.addUndoableEditListener(undoManager);
            doc.putProperty(UndoManager.class, undoManager);
        }
        bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        pane = new JEditorPane() {
            @Override
            public Graphics getGraphics() {
                return bufferedImage.getGraphics();
            }
        };
        pane.putClientProperty(TestRootView.class, this);
        pane.putClientProperty("id", lastId++);
        pane.setDocument(doc);
        documentView = new DocumentView(doc.getDefaultRootElement());
        documentView.setParent(this);
        allRootViews.add(this);
    }
    
    public void setBounds(int startOffset, int endOffset) throws BadLocationException {
        assert (startOffset < endOffset);
        Position startPos = doc.createPosition(startOffset);
        Position endPos = doc.createPosition(endOffset);
        pane.putClientProperty(DocumentView.START_POSITION_PROPERTY, startPos);
        pane.putClientProperty(DocumentView.END_POSITION_PROPERTY, endPos);
    }
    
    public int id() {
        return id(pane);
    }

    public UndoManager undoManager() {
        return undoManager;
    }
    
    public DocumentView documentView() {
        return documentView;
    }
    
    public JEditorPane pane() {
        return pane;
    }
    
    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }

    @Override
    public Container getContainer() {
        return pane;
    }
    
    private List<TestHighlight> getHighlights() {
        return getHighlights(pane);
    }
    
    public List<TestHighlight> getHighlightsCopy() {
        return new GapList<TestHighlight>(getValidHighlights());
    }
    
    private List<TestHighlight> getValidHighlights() {
        List<TestHighlight> highlights = getHighlights();
        if (highlights == null) {
            setHighlights(highlights = new GapList<TestHighlight>());
        }
        return highlights;
    }
    
    public TestHighlight createHighlight(int startOffset, int endOffset, AttributeSet attrs) throws Exception {
        return new TestHighlight(createPosition(startOffset), createPosition(endOffset), attrs);
    }
    
    public Position createPosition(int offset) throws BadLocationException {
        if (offset > doc.getLength() + 1) {
            // Need an explicit check since PlainDocument allows creation of pos beyond end of its content!
            throw new BadLocationException("offset=" + offset + " > docLen+1=" + (doc.getLength()+1), offset);
        }
        return doc.createPosition(offset);
    }

    public void setHighlights(List<TestHighlight> highlights) {
        setHighlights(pane, highlights);
    }
    
    public void fireChange(int fireStartOffset, int fireEndOffset) {
        testViewFactory.fireChange(fireStartOffset, fireEndOffset);
    }
    
    void updateFactory(TestHighlightsViewFactory testViewFactory) {
        this.testViewFactory = testViewFactory;
    }

    @Override
    public float getPreferredSpan(int axis) {
        ((AbstractDocument)doc).readLock();
        try {
            return documentView.getPreferredSpan(axis);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void paint(Graphics g) {
        paint(g, updatedAlloc());
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
        ((AbstractDocument)doc).readLock();
        try {
            documentView.paint(g, allocation);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public Shape modelToView(int pos) throws BadLocationException {
        return modelToView(pos, updatedAlloc(), Position.Bias.Forward);
    }

    @Override
    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
        ((AbstractDocument)doc).readLock();
        try {
            return documentView.modelToView(pos, a, b);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public int viewToModel(float x, float y) {
        return viewToModel(x, y, updatedAlloc(), null);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Bias[] biasReturn) {
        ((AbstractDocument)doc).readLock();
        try {
            return documentView.viewToModel(x, y, a, biasReturn);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }
    
    public void dump() {
        System.out.println("View hierarchy DUMP #" + (dumpId++) + ":\n" + toString());
    }
    
    public void checkIntegrity() {
        ((AbstractDocument)doc).readLock();
        try {
            documentView.checkIntegrity();
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }
    
    private Rectangle.Float updatedAlloc() {
        ((AbstractDocument)doc).readLock();
        try {
            lastAlloc.width = documentView.getPreferredSpan(View.X_AXIS);
            lastAlloc.height = documentView.getPreferredSpan(View.Y_AXIS);
            documentView.setSize(lastAlloc.width, lastAlloc.height);
            return lastAlloc;
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public static void checkIntegrityAll() {
        for (TestRootView rootView : allRootViews) {
            rootView.checkIntegrity();
        }
    }
    
    @Override
    public String toString() {
        return documentView.toStringDetail();
    }
    
    public static void appendInfoAll(StringBuilder sb) {
        for (TestRootView rootView : allRootViews) {
            sb.append(rootView).append("\n\n");
        }
    }
    
}
