/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.api;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;


/**
 * Source represents some part of text. Source can be created from file or document
 * opened in editor. And source can represent some block of code written 
 * in different language embedded inside some top level language too. It can contain
 * some generated parts of code that is not contained in the original 
 * file. Source is read only and immutable. It means that source created 
 * from document opened in editor contains some copy of original text. 
 * You do not need to call source methods under 
 * any locks, but on other hand source may not be up to date.
 *
 * Following example shows how to create Source for block of code 
 * embedded in other source:
 * {@link EmbeddingProvider}:
 * 
 * <pre> 
 *           int start = findStartJavaOffset (source);
 *           int length = getJavaBlockLength (source, start);
 *           return source.create (Arrays.asList (new Source[] {
 *               source.create ("some prefix code", "text/x-java"),
 *               source.create (start, length, "text/x-java"),
 *               source.create ("some postfix code", "text/x-java")
 *           })));
 * </pre>
 * 
 * @author Jan Jancura
 */
public final class Source {
    
    static {
        SourceAccessor.setINSTANCE(new MySourceAccessor());
    }
    
    private CharSequence    text;
    private String          mimeType;
    private Document        document;
    private FileObject      fileObject;
    private final Set<SourceFlags> flags = EnumSet.noneOf(SourceFlags.class);
    private Parser parser;
    
   
    private Source (
        CharSequence        text, 
        String              mimeType, 
        Document            document,
        FileObject          fileObject
    ) {
        this.text =         text;
        this.mimeType =     mimeType;
        this.document =     document;
        this.fileObject =   fileObject;
    }
    
    /**
     * Creates source for given file.
     * 
     * @param fileObject    A file object.
     * @return              source for given file.
     */
    public static Source create (
        FileObject          fileObject
    ) {
        return null;
    }
    
    /**
     * Creates source for given document.
     * 
     * @param document      A document.
     * @return              source for given document.
     */
    public static Source create (
        Document            document
    ) {
        if (document instanceof AbstractDocument)
            ((AbstractDocument) document).readLock ();
        try {
            return new Source (
                document.getText (0, document.getLength ()),
                (String) document.getProperty ("mimeType"),
                document,
                null
            );
        } catch (BadLocationException ex) {
            ex.printStackTrace ();
            return new Source (
                "",
                (String) document.getProperty ("mimeType"),
                document,
                null
            );
        } finally {
            if (document instanceof AbstractDocument)
                ((AbstractDocument) document).readUnlock ();
        }
    }
    
    /**
     * Creates a new source form part of this source defined by offset and length.
     * The new source has the same Document and FileObjet property values, 
     * but mimeType should be different than original one. 
     * 
     * @param offset        A start offset of the new source. Start offset
     *                      is relative to the current source.
     * @param length        A length of the new source.
     * @param mimeType      Mime type of the new source.
     * @return              The new source.
     * @throws IndexOutOfBoundsException when bounds of the new source exceeds 
     *                      original source.
     */
    public Source create (
        int                 offset, 
        int                 length, 
        String              mimeType
    ) {
        return new Source (
            getText ().subSequence (offset, offset + length),
            mimeType,
            getDocument (),
            getFileObject ()
        );
    }
    
    /**
     * Creates a new source for given charSequence. 
     * The new source has the same Document and FileObjet property values, 
     * but mimeType should be different than original one. 
     * 
     * @param charSequence  A text of new source.
     * @param mimeType      Mime type of the new source.
     * @return              The new source.
     */
    public Source create (
        CharSequence        charSequence, 
        String              mimeType
    ) {
        return null;
    }
    
    /**
     * Creates source from a parts of this source. New source contains one 
     * or more blocks of text from this source. All sources must have the same
     * DataObject and Document like this document. All sources must have the
     * same mime type, but this mime type have to be different than current
     * source mime type.
     * 
     * @param sources       A list of some sources created from this source.
     * @return              A new source compound from given pieces.
     */
    public Source create (
        List<Source>        sources 
    ) {
        return null;
    }
    
    /**
     * Returns content of source.
     * 
     * @return              text of this source
     */
    public CharSequence getText (
    ) {
        return text;
    }

    /**
     * Returns source mime type.
     * 
     * @return              this source mime type.
     */
    public String getMimeType (
    ) {
        return mimeType;
    }
    
    /**
     * Returns position of given offset in the original document or file, 
     * or <code>-1</code>. <code>-1</code> is returned if the text on 
     * the given position is "virtual" - generated by some preprocessor, 
     * and it has no representation in the top level code.
     * 
     * @param offset        a offset related to this source
     * @return              position of given offset in original document or file
     */
    public int getOriginalOffset (
        int                 offset
    ) {
        return -1;
    }
    
    /**
     * Returns document this source has originally been created from or null.
     * 
     * @return              a document.
     */
    public Document getDocument () {
        return document;
    }
    
    /**
     * Returns file this source has originally been created from or null.
     * 
     * @return              a file.
     */
    public FileObject getFileObject () {
        return null;
    }
    
    
    private static class MySourceAccessor extends SourceAccessor {

        @Override
        public Set<SourceFlags> getFlags(Source source) {
            assert source != null;
            return source.flags;
        }

        @Override
        public Parser getParser(Source source) {
            assert source != null;
            return source.parser;
        }
        
    }
}



