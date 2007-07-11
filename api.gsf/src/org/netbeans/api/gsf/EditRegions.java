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

import java.util.Set;
import javax.swing.text.BadLocationException;

import org.netbeans.api.gsf.annotations.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 * Provide access to initiate editing of synchronized regions in a document
 * for a file.
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public abstract class EditRegions {
    /**
     * Initiate editing. The document for the file must already been open in the editor.
     * @param file The file whose document we want to edit
     * @param regions The set of ranges in the document that we want to edit
     * @param caretOffset The initial location for the caret (which MUST be within
     *   one of the regions)
     */
    public abstract void edit(@NonNull FileObject file, @NonNull Set<OffsetRange> regions, int caretOffset) throws BadLocationException;

    public static EditRegions getInstance() {
        return Lookup.getDefault().lookup(EditRegions.class);
    }
}
