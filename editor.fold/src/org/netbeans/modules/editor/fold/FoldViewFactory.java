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

package org.netbeans.modules.editor.fold;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.netbeans.modules.editor.lib2.view.EditorViewFactory;
import org.netbeans.modules.editor.lib2.view.ViewUtils;

/**
 * View factory creating views for collapsed folds.
 *
 * @author Miloslav Metelka
 */

@SuppressWarnings("ClassWithMultipleLoggers")
public final class FoldViewFactory extends EditorViewFactory implements FoldHierarchyListener {

    /**
     * Component's client property which can be set to view folds expanded for tooltip fold preview.
     */
    static final String VIEW_FOLDS_EXPANDED_PROPERTY = "view-folds-expanded"; // NOI18N

    // -J-Dorg.netbeans.editor.view.change.level=FINE
    static final Logger CHANGE_LOG = Logger.getLogger("org.netbeans.editor.view.change");

    // -J-Dorg.netbeans.modules.editor.fold.FoldViewFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(FoldViewFactory.class.getName());

    static void register() {
        EditorViewFactory.registerFactory(new FoldFactory());
    }

    private FoldHierarchy foldHierarchy;

    private boolean foldHierarchyLocked;

    private Fold fold;

    private int foldStartOffset;

    private Iterator<Fold> collapsedFoldIterator;

    private boolean viewFoldsExpanded;

    public FoldViewFactory(View documentView) {
        super(documentView);
        foldHierarchy = FoldHierarchy.get(textComponent());
        foldHierarchy.addFoldHierarchyListener(this);
        viewFoldsExpanded = Boolean.TRUE.equals(textComponent().getClientProperty(VIEW_FOLDS_EXPANDED_PROPERTY));
    }

    @Override
    public void restart(int startOffset, int matchOffset) {
        foldHierarchy.lock(); // this.finish() always called in try-finally
        foldHierarchyLocked = true;
        @SuppressWarnings("unchecked")
        Iterator<Fold> it = FoldUtilities.collapsedFoldIterator(foldHierarchy, startOffset, Integer.MAX_VALUE);
        collapsedFoldIterator = it;
        foldStartOffset = -1; // Make a next call to updateFold() to fetch a fold
    }

    private void updateFold(int offset) {
        if (foldStartOffset < offset) {
            while (collapsedFoldIterator.hasNext()) {
                fold = collapsedFoldIterator.next();
                foldStartOffset = fold.getStartOffset();
                if (foldStartOffset >= offset) {
                    return;
                }
            }
            fold = null;
            foldStartOffset = Integer.MAX_VALUE;
        }
    }

    @Override
    public int nextViewStartOffset(int offset) {
        if (!viewFoldsExpanded) {
            updateFold(offset);
            return foldStartOffset;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset) {
        assert (startOffset == foldStartOffset) : "startOffset=" + startOffset + " != foldStartOffset=" + foldStartOffset; // NOI18N
        if (fold.getEndOffset() <= limitOffset) {
            return new FoldView(textComponent(), fold);
        } else {
            return null;
        }
    }

    @Override
    public int viewEndOffset(int startOffset, int limitOffset) {
        int foldEndOffset = fold.getEndOffset();
        if (foldEndOffset <= limitOffset) {
            return foldEndOffset;
        } else {
            return -1;
        }
    }

    @Override
    public void finishCreation() {
        fold = null;
        collapsedFoldIterator = null;
        if (foldHierarchyLocked) {
            foldHierarchy.unlock();
        }
    }

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        // For fold state changes use a higher priority
        int priority = (evt.getFoldStateChangeCount() > 0) ? 1 : 0;
        int startOffset = evt.getAffectedStartOffset();
        int endOffset = evt.getAffectedEndOffset();
        if (CHANGE_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(CHANGE_LOG, "CHANGE in FoldViewFactory: <" + // NOI18N
                    startOffset + "," + endOffset + ">\n"); // NOI18N
        }
        fireEvent(Collections.singletonList(EditorViewFactory.createChange(startOffset, endOffset)), priority);
    }

    public static final class FoldFactory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new FoldViewFactory(documentView);
        }

        @Override
        public int importance() {
            return 100;
        }

    }

}
