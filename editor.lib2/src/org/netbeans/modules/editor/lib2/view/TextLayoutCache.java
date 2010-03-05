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

package org.netbeans.modules.editor.lib2.view;

import java.awt.font.TextLayout;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * Cache for text layouts of most used lines (paragraph views) from all text components.
 * <br/>
 * The cache is small
 * 
 * @author Miloslav Metelka
 */

public final class TextLayoutCache {

    // -J-Dorg.netbeans.modules.editor.lib2.view.TextLayoutCache.level=FINE
    private static final Logger LOG = Logger.getLogger(TextLayoutCache.class.getName());

    /**
     * Cache text layouts for the following count of paragraph views.
     */
    private static final int MAX_SIZE = 100;

    private final Map<ParagraphView, Entry> paragraph2entry = new HashMap<ParagraphView, Entry>();

    /**
     * Most recently used entry.
     */
    private Entry head;

    /**
     * Least recently used entry.
     */
    private Entry tail;

    public TextLayoutCache() {
    }

    /**
     * Clear the whole cache in case of changes of e.g. font-render-context etc.
     */
    synchronized void clear() {
        paragraph2entry.clear();
    }

    /**
     * Get valid text layout for the given view. If it's not cached it gets created.
     *
     * @param paragraphView non-null paragraph view.
     * @param childView non-null view which is a child of paragraph view.
     * @return text layout or null if it could not be created.
     */
    synchronized TextLayout get(ParagraphView paragraphView, TextLayoutView childView) {
        assert (paragraphView != null && childView != null);
        Entry entry = paragraph2entry.get(paragraphView);
        if (entry == null) {
            entry = new Entry(paragraphView);
            paragraph2entry.put(paragraphView, entry);
            if (paragraph2entry.size() >= MAX_SIZE) { // Cache full => remove LRU
                Entry lru = paragraph2entry.remove(tail.paragraphView);
                assert (lru == tail);
                removeChainEntry(lru);
            }
            addChainEntryFirst(entry);
        }
        if (head != entry) { // Possibly move entry to head
            removeChainEntry(entry);
            addChainEntryFirst(entry);
        }
        TextLayout textLayout = entry.view2layout.get(childView);
        if (textLayout == null) {
            textLayout = childView.createTextLayout();
            if (textLayout != null) {
                entry.view2layout.put(childView, textLayout);
            } else {
                entry.view2layout.remove(childView);
            }
        }
        return textLayout;
    }

    /**
     * Allow to either clear or refresh already cached text layout explicitly for the particular view
     * in case of modification/removal of the view. If the given paragraphView is currently not cached
     * the method does nothing.
     *
     * @param paragraphView non-null paragraph view.
     * @param childView non-null child view for which the layout is being modified.
     * @param textLayout layout or null to modify/clear the cached layout.
     */
    synchronized void put(ParagraphView paragraphView, TextLayoutView childView, TextLayout textLayout) {
        Entry entry = paragraph2entry.get(paragraphView);
        if (entry == null) {
            return;
        }
        if (textLayout != null) {
            entry.view2layout.put(childView, textLayout);
        } else {
            entry.view2layout.remove(childView);
        }
    }

    synchronized void removeParagraph(ParagraphView paragraphView) {
        Entry entry = paragraph2entry.remove(paragraphView);
        if (entry != null) {
            removeChainEntry(entry);
        }
    }

    synchronized void remove(ParagraphView paragraphView, int childIndex, int childCount) {
        Entry entry = paragraph2entry.get(paragraphView);
        if (entry != null) {
            for (int i = 0; i < childCount; i++) {
                EditorView childView = paragraphView.getEditorView(childIndex + i);
                if (childView instanceof TextLayoutView) {
                    entry.view2layout.remove((TextLayoutView)childView);
                }
            }
        }
    }

    private void addChainEntryFirst(Entry entry) {
        assert (entry.previous == null && entry.next == null);
        if (head == null) {
            assert (tail == null);
            head = tail = entry;
            // Leave entry.previous == entry.next == null;
        } else {
            entry.next = head;
            head.previous = entry;
            head = entry;
        }
    }

    private void removeChainEntry(Entry entry) {
        if (entry.previous != null) {
            entry.previous.next = entry.next;
        } else {
            assert (head == entry);
            head = entry.next;
        }
        if (entry.next != null) {
            entry.next.previous = entry.previous;
        } else {
            assert (tail == entry);
            tail = entry.previous;
        }
        entry.previous = entry.next = null;
    }

    private static final class Entry {

        final ParagraphView paragraphView;

        final Map<TextLayoutView, TextLayout> view2layout;

        Entry previous;

        Entry next;

        Entry(ParagraphView paragraphView) {
            this.paragraphView = paragraphView;
            int viewCount = paragraphView.getViewCount();
            assert (viewCount > 0);
            view2layout = new HashMap<TextLayoutView, TextLayout>((int)(viewCount / 0.6 + 1), 0.6f);
        }

    }

}
