/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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

    private static final String BOOKMARK_ANNOTATION_TYPE = "editor-bookmark"; // NOI18N

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

