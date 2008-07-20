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

package org.netbeans.modules.editor.bookmarks;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.text.Annotation;
import java.text.MessageFormat;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 * Interface to a bookmark.
 *
 * @author Miloslav Metelka
 */

public final class NbBookmarkImplementation extends Annotation
implements BookmarkImplementation {

    static final String BOOKMARK_ANNOTATION_TYPE = "editor-bookmark"; // NOI18N

    private final NbBookmarkManager manager;
    
    private Document document;
    private Line line;
    
    NbBookmarkImplementation(NbBookmarkManager manager, int offset) {
        this.manager = manager;
        document = manager.getDocument();
        line = NbEditorUtilities.getLine (document, offset, false);
        if (line != null)
            attach (line);
    }
    
    public String getAnnotationType() {
        return BOOKMARK_ANNOTATION_TYPE;
    }

    public String getShortDescription() {
        String fmt = NbBundle.getBundle(NbBookmarkImplementation.class).getString("Bookmark_Tooltip"); // NOI18N
        int lineIndex = getLineIndex();
        return MessageFormat.format(fmt, new Object[] { new Integer(lineIndex + 1) });
    }

    public int getOffset() {
        if (line == null) return 0;
        return NbDocument.findLineOffset ((StyledDocument) document, line.getLineNumber());
    }
    
    public int getLineIndex() {
        return manager.getDocument().getDefaultRootElement().getElementIndex(getOffset());
    }

    public void release() {
        Document doc = manager.getDocument();
        if (doc instanceof StyledDocument) {
            NbDocument.removeAnnotation((StyledDocument)doc, this);
        }
    }
    
    public String toString() {
        return getShortDescription();
    }
}

