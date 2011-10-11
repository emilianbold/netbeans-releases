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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer;

/**
 *
 * @author Miloslav Metelka
 */
public class ViewHierarchyTest extends NbTestCase {
    
    private static AttributeSet[] colorAttrs = {
        AttributesUtilities.createImmutable(StyleConstants.Background, Color.red),
        AttributesUtilities.createImmutable(StyleConstants.Background, Color.green),
        AttributesUtilities.createImmutable(StyleConstants.Background, Color.blue)
    };

    public ViewHierarchyTest(String testName) {
        super(testName);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimple1");
//        includes.add("testSimpleUndoRedo");
//        includes.add("testCustomBounds");
//        includes.add("testRemoveNewline");
//        includes.add("testRandom");
//        includes.add("testLock");
//        filterTests(includes);
    }

    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[includeTests.size()]));
        setFilter(filter);
    }

    private RandomTestContainer createContainer() throws Exception {
        RandomTestContainer container = RootViewRandomTesting.createContainer();
        container.setName(this.getName());
        DocumentTesting.setLogDoc(container, false); // can be overriden if necessary
        return container;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO; // null;
//        return Level.FINEST;
    }

    private static void loggingOn() {
        Level LOG_LEVEL = Level.FINEST;
        // FINEST throws ISE for integrity error in EditorView
        Logger.getLogger("org.netbeans.editor.view.check").setLevel(Level.FINEST);
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // Check gap-storage correctness
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorBoxViewChildren").setLevel(Level.FINE);
        Logger.getLogger("org.netbeans.editor.BaseDocument.EDT").setLevel(Level.FINE);
        Logger.getLogger("org.netbeans.editor.BaseCaret.EDT").setLevel(Level.FINE);
    }
    
    public void testSimple1() throws Exception {
//        loggingOn();    
        TestRootView rootView = new TestRootView();
        Document doc = rootView.getDocument();
        rootView.modelToView(0);
        doc.insertString(0, "hello", null);
        List<TestHighlight> hlts = rootView.getHighlightsCopy();
        TestHighlight hi = rootView.createHighlight(3, 5, colorAttrs[0]);
        doc.insertString(doc.getLength(), " world", null);
        hlts.add(hi);
        rootView.setHighlights(hlts);
        rootView.fireChange(0, 10);
        // Insert at offset == 0
        doc.insertString(0, "test ", null);
        // Test insert newline at offset == 0
        doc.insertString(0, "\ntest2\n", null);
        doc.remove(0, 1);
        doc.insertString(6, "a", null);
//        rootView.dump();
        doc.remove(0, doc.getLength());
    }
    
    public void testSimpleUndoRedo() throws Exception {
//        loggingOn();
        TestRootView rootView = new TestRootView();
        Document doc = rootView.getDocument();
        doc.insertString(0, "abc\ndef\nghi\n", null);
        rootView.setBounds(4, 8);
        rootView.modelToView(0);
        doc.insertString(4, "o", null);
        doc.remove(3, 3);
        doc.insertString(4, "ab", null);
        doc.remove(7, 2);
        rootView.modelToView(0);
        rootView.undo(); // insert(7,2)
        rootView.undo(); // remove(4,2)
        rootView.undo(); // insert(3,3)
        rootView.undo();
        rootView.redo();
        rootView.redo();
        rootView.redo();
        rootView.redo();
    }

    public void testRemoveNewline() throws Exception {
//        loggingOn();
        TestRootView rootView = new TestRootView();
        Document doc = rootView.getDocument();
        rootView.modelToView(0);
        doc.insertString(0, "a\nb", null);
        doc.remove(1, 1);
        rootView.undoManager().undo();
        rootView.modelToView(0);
    }

    public void testCustomBounds() throws Exception {
//        loggingOn();    
        TestRootView rootView = new TestRootView();
        Document doc = rootView.getDocument();
        JEditorPane pane = rootView.pane();
        rootView.modelToView(0);
        doc.insertString(0, "hello\nworld\ngood\nmorning", null);
        Position startPos = doc.createPosition(3);
        pane.putClientProperty(DocumentView.START_POSITION_PROPERTY, startPos);
        rootView.modelToView(0); // Force rebuild of VH
        doc.insertString(startPos.getOffset(), "a", null);
        doc.insertString(startPos.getOffset() - 1, "a", null);
        Element line0 = doc.getDefaultRootElement().getElement(0);
        Position endPos = doc.createPosition(line0.getEndOffset() + 3); // Middle of line 1
        pane.putClientProperty(DocumentView.END_POSITION_PROPERTY, endPos);
        rootView.modelToView(0); // Force rebuild of VH

        List<TestHighlight> hlts = rootView.getHighlightsCopy();
        int hlStartOffset = startPos.getOffset() + 1;
        int hlEndOffset = endPos.getOffset() - 1; 
        TestHighlight hi = rootView.createHighlight(hlStartOffset, hlEndOffset, colorAttrs[0]);
        hlts.add(hi);
        rootView.setHighlights(hlts);
        rootView.fireChange(hlStartOffset, hlEndOffset);
        rootView.modelToView(0); // Force rebuild of VH
        
        doc.insertString(doc.getLength(), "test\ntest2", null);
        Position endPos2 = doc.createPosition(doc.getLength() - 3);
        pane.putClientProperty(DocumentView.END_POSITION_PROPERTY, endPos2);
        rootView.modelToView(0); // Force rebuild of VH
        doc.remove(endPos2.getOffset() - 2, 3);
        pane.putClientProperty(DocumentView.START_POSITION_PROPERTY, null);
        rootView.modelToView(0); // Force rebuild of VH
    }
    
    public void testEmptyCustomBounds() throws Exception {
//        loggingOn();    
        TestRootView rootView = new TestRootView();
        Document doc = rootView.getDocument();
        JEditorPane pane = rootView.pane();
        rootView.modelToView(0);
        doc.insertString(0, "hello\nworld\ngood\nmorning", null);
        Position startPos = doc.createPosition(3);
        pane.putClientProperty(DocumentView.START_POSITION_PROPERTY, startPos);
        pane.putClientProperty(DocumentView.END_POSITION_PROPERTY, startPos);
        rootView.modelToView(0); // Force rebuild of VH

        Position endPos = doc.createPosition(2);
        pane.putClientProperty(DocumentView.END_POSITION_PROPERTY, endPos);
        rootView.modelToView(0); // Force rebuild of VH
    }
    
    public void testInsertNewlineIntoPartial() throws Exception {
//        loggingOn();    
        TestRootView rootView = new TestRootView();
        Document doc = rootView.getDocument();
        rootView.modelToView(0);
        doc.insertString(0, "x\nya\n\nbc\nmorning", null);
        rootView.setBounds(3, doc.getLength() - 3);
        rootView.modelToView(0);
        doc.insertString(8, "\n\n", null);
        rootView.modelToView(0);
    }
    
    public void testRandom() throws Exception {
        loggingOn();
        RandomTestContainer container = createContainer();
        DocumentTesting.setLogDoc(container, false);
        RandomTestContainer.Round round = RootViewRandomTesting.addRound(container);
        round.setRatio(RootViewRandomTesting.CREATE_ROOT_VIEW, 0);
        round.setRatio(RootViewRandomTesting.DESTROY_ROOT_VIEW, 0);
        RandomTestContainer.Context context = container.context();
        DocumentTesting.insert(context, 0, "test\n\nab\nxyz\n\nfd\nhjk");
        TestRootView rootView = RootViewRandomTesting.addNewRootView(context);
        rootView.setBounds(3, 10);
        rootView = RootViewRandomTesting.addNewRootView(context);
        rootView.setBounds(1, 5);
        rootView = RootViewRandomTesting.addNewRootView(context);
        rootView.setBounds(8, 15);

//        round.setRatio(RootViewRandomTesting.CREATE_ROOT_VIEW, 0f); // No new root views
        round.setOpCount(500);
        container.runInit(1305718603094L);
        container.runOps(355);
        container.runOps(0);
//        container.run(1305713030265L);
        container.runInit(1305643331093L);
        container.runOps(356);
        container.runOps(0);
        container.run(0L); // truly pseudo-random
    }

    public void testLock() throws Exception {
        loggingOn();
        TestRootView rootView = new TestRootView();
        final Document doc = rootView.getDocument();
        doc.insertString(0, "hello\nworld\ngood\nmorning", null);
        final JEditorPane pane = rootView.pane();
        doc.render(new Runnable() {
            @Override
            public void run() {
                ViewHierarchy vh = ViewHierarchy.get(pane);
                LockedViewHierarchy lvh = vh.lock();
                try {
                    float defaultRowHeight = lvh.getDefaultRowHeight();
                } finally {
                    lvh.unlock();
                }
                boolean ok = false;
                try {
                    lvh.unlock();
                } catch (IllegalStateException ex) {
                    // Expected
                    ok = true;
                }
                if (!ok) {
                    fail("Extra unlock() did not fail.");
                }

                // Nested locking
                lvh = vh.lock();
                try {
                    float defaultRowHeight = lvh.getDefaultRowHeight();
                    LockedViewHierarchy lvh2 = vh.lock();
                    try {
                        float defaultRowHeight2 = lvh2.getDefaultRowHeight();
                        assertEquals(defaultRowHeight, defaultRowHeight2);
                    } finally {
                        lvh2.unlock();
                    }
                    
                } finally {
                    lvh.unlock();
                }
                ok = false;
                try {
                    lvh.unlock();
                } catch (IllegalStateException ex) {
                    // Expected
                    ok = true;
                }
                if (!ok) {
                    fail("Extra unlock() did not fail.");
                }

            }
        });
    }
    
}
