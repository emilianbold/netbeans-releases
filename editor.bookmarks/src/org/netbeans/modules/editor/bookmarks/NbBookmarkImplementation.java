/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.bookmarks;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.text.Annotation;
import java.text.MessageFormat;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;
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
    
    private Position pos;
    
    NbBookmarkImplementation(NbBookmarkManager manager, int offset) {
        this.manager = manager;
        Document doc = manager.getDocument();
        try {
            pos = doc.createPosition(offset);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            pos = doc.getStartPosition();
        }
        if (doc instanceof StyledDocument) {
            NbDocument.addAnnotation((StyledDocument)doc, pos, -1, this);
        }
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
        return pos.getOffset();
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

