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

package org.netbeans.lib.editor.bookmarks.spi;

import javax.swing.text.Document;

/**
 * Implementation of the bookmark to which the bookmark
 * delegates.
 *
 * @author Miloslav Metelka
 */

public interface BookmarkImplementation {

    /**
     * Get the offset at which the bookmark resides.
     * <br>
     * Offsets are required to behave like {@link javax.swing.text.Position}s
     * (they track inserts/removals).    
     */
    int getOffset();

    /**
     * Called when a bookmark has been released from its bookmark list
     * and it's no longer active.
     */
    void release();

}

