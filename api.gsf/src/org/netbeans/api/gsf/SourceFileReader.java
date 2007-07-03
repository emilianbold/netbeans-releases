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

package org.netbeans.api.gsf;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Implementations of this interface can read the contents of a FileObject.
 * The IDE can for example provide a reader which either fetches the contents
 * from disk, or from an open editor buffer if it has been edited.
 * 
 * @author Tor Norbye
 */
public interface SourceFileReader {
    /** 
     * Return a character sequence for the contents of the given file,
     * which could be on disk, or modified in an open document, etc.
     */
    CharSequence read(ParserFile file) throws IOException;
    
    /**
     * Return the last known caret offset of the given file, if it is being edited.
     * Return -1 if the file is not open, or if the caret offset is not currently
     * known.
     */
    int getCaretOffset(ParserFile file);
}
