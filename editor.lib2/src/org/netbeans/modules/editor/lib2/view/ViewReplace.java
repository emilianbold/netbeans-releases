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

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.ArrayUtilities;


/**
 * View building support.
 * 
 * @author Miloslav Metelka
 */

final class ViewReplace<V extends EditorBoxView, CV extends EditorView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewReplace.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewReplace.class.getName());

    final V view;

    int index;

    int removeCount;

    List<CV> added;

    ViewReplace(V view, int index) {
        assert (view != null);
        this.view = view;
        this.index = index;
    }

    void add(CV view) {
        if (added == null) {
            added = new ArrayList<CV>();
        }
        added.add(view);
    }

    int removeEndIndex() {
        return index + removeCount;
    }

    void removeTillEnd() {
        removeCount = view.getViewCount() - index;
    }

    private EditorView[] addedViews() {
        EditorView[] views;
        if (added != null) {
            views = new EditorView[added.size()];
            added.toArray(views);
        } else {
            views = new EditorView[0];
        }
        return views;
    }

    EditorBoxView.ReplaceResult replaceViews(int offsetDelta, Shape alloc) {
        if (removeCount > 0 || added != null) {
            return view.replace(index, removeCount, addedViews(), offsetDelta, alloc); // minor span modified
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("viewId=").append(view.getDumpId());
        sb.append(", index=").append(index);
        sb.append(", removeCount=").append(removeCount);
        EditorView[] addedViews = addedViews();
        sb.append(", addedCount=").append(addedViews.length);
        sb.append(", Added:\n");
        int maxDigitCount = ArrayUtilities.digitCount(addedViews.length);
        for (int i = 0; i < addedViews.length; i++) {
            sb.append("    ");
            ArrayUtilities.appendBracketedIndex(sb, i, maxDigitCount);
            sb.append(addedViews[i].toString());
            sb.append('\n');
        }
        return sb.toString();
    }

}
