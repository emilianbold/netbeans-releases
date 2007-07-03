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

import java.net.URL;

import javax.swing.text.Document;

import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Tor Norbye
 */
public interface DeclarationFinder {
    /**
     * Find the declaration for the program element that is under the caretOffset
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     *
     * Return {@link Declaration.NONE} if the declaration can not be found, otherwise return
     *   a valid DeclarationLocation.
     */
    @NonNull
    DeclarationLocation findDeclaration(@NonNull CompilationInfo info, int caretOffset);

    /**
     * Check the caret offset in the document and determine if it is over a span
     * of text that should be hyperlinkable ("Go To Declaration" - in other words,
     * locate the reference and return it. When the user drags the mouse with a modifier
     * key held this will be hyperlinked, and so on.
     * <p>
     * Remember that when looking up tokens in the token hiearchy, you will get the token
     * to the right of the caret offset, so check for these conditions
     * {@code (sequence.move(offset); sequence.offset() == offset)} and check both
     * sides such that placing the caret between two tokens will match either side.
     *
     * @return {@link OffsetRange.NONE} if the caret is not over a valid reference span,
     *   otherwise return the character range for the given hyperlink tokens
     */
    @NonNull
    public OffsetRange getReferenceSpan(@NonNull Document doc, int caretOffset);

    /**
     * Holder object for return values from the DeclarationFinder#findDeclaration method.
     * The constant NONE object should be returned when finding a declaration failed.
     */
    public final class DeclarationLocation {
        /** DeclarationLocation representing no match or failure to find declaration */
        public static final DeclarationLocation NONE = new DeclarationLocation(null, -1);
        private final FileObject fileObject;
        private final int offset;
        private final URL url;

        public DeclarationLocation(final FileObject fileObject, final int offset) {
            this.fileObject = fileObject;
            this.offset = offset;
            this.url = null;
        }

        public DeclarationLocation(final URL url) {
            this.url = url;
            this.fileObject = null;
            this.offset = -1;
        }

        public URL getUrl() {
            return url;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public int getOffset() {
            return offset;
        }

        public String toString() {
            if (this == NONE) {
                return "NONE";
            }

            if (url != null) {
                return url.toExternalForm();
            }

            return fileObject.getNameExt() + ":" + offset;
        }
    }
}
