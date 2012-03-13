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
 * Software is Sun Microsystems, Inc. Portions Copyright 2003-2007 Sun
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

package org.netbeans.modules.search;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.spi.search.SearchScopeDefinition;
import sun.nio.cs.ThreadLocalCoders;

/**
 *
 * @author  Marian Petras
 * @author  kaktus
 */
final class Utils {

    private Utils() { }

    /**
     * Returns a border for explorer views.
     *
     * @return  border to be used around explorer views
     *          (<code>BeanTreeView</code>, <code>TreeTableView</code>,
     *          <code>ListView</code>).
     */
    static Border getExplorerViewBorder() {
        Border border;
        border = (Border) UIManager.get("Nb.ScrollPane.border");        //NOI18N
        if (border == null) {
            border = BorderFactory.createEtchedBorder();
        }
        return border;
    }    
    
    /**
     * Converts an input file stream into a char sequence.
     *
     * @throws IOException
     */
    static CharBuffer getCharSequence(final FileInputStream stream, Charset encoding) throws IOException {
        FileChannel channel = stream.getChannel();
        ByteBuffer bbuf = ByteBuffer.allocate((int) channel.size());
        try {
            channel.read(bbuf, 0);
        } catch (ClosedByInterruptException cbie) {
            return null;        //this is actually okay
        } finally {
            channel.close();
        }
        bbuf.rewind();
        CharBuffer cbuf = decodeByteBuffer(bbuf, encoding);

        return cbuf;
 }

    /**
     * Decodes a given {@code ByteBuffer} with a given charset decoder.
     * This is a workaround for a broken
     * {@link Charset.decode(ByteBuffer) Charset#decode(java.nio.ByteBuffer}
     * method in JDK 1.5.x.
     *
     * @param  in  {@code ByteBuffer} to be decoded
     * @param  charset  charset whose decoder will be used for decoding
     * @return  {@code CharBuffer} containing chars produced by the decoder
     * @see  <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/Charset.html#decode(java.nio.ByteBuffer)">Charset.decode(ByteBuffer)</a>
     * @see  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6221056">JDK bug #6221056</a>
     * @see  <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103193">NetBeans bug #103193</a>
     * @see  <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103067">NetBeans bug #103067</a>
     */
    static CharBuffer decodeByteBuffer(final ByteBuffer in, final Charset charset) throws CharacterCodingException {
        final CharsetDecoder decoder = ThreadLocalCoders.decoderFor(charset.name())
                                              .onMalformedInput(CodingErrorAction.REPLACE)
                                              .onUnmappableCharacter(CodingErrorAction.REPLACE);
        int remaining = in.remaining();
        if (remaining == 0) {
            return CharBuffer.allocate(0);
        }

        int n = (int) (remaining * decoder.averageCharsPerByte());
        if (n < 16) {
            n = 16;             //make sure some CharBuffer is allocated
                                //even when decoding small number of bytes
                                //and averageCharsPerByte() is less than 1
        }
        CharBuffer out = CharBuffer.allocate(n);

        decoder.reset();
        for (;;) {
            CoderResult cr = in.hasRemaining()
                    ? decoder.decode(in, out, true)
                    : CoderResult.UNDERFLOW;
            if (cr.isUnderflow()) {
                cr = decoder.flush(out);
            }
            if (cr.isUnderflow()) {
                break;
            }
            if (cr.isOverflow()) {
                CharBuffer o = CharBuffer.allocate(n <<= 1);
                out.flip();
                o.put(out);
                out = o;
                continue;
            }
            cr.throwException();
        }
        out.flip();
        return out;
    }

    /**
     * Check whether there is a projects-related search scope.
     */
    public static boolean hasProjectSearchScope() {
        boolean hasProjectSearchScope = false;
        SearchScopeList ssl = new SearchScopeList();
        for (SearchScopeDefinition ssd : ssl.getSeachScopeDefinitions()) {
            if (ssd.getTypeId().matches(".*project.*")) {
                hasProjectSearchScope = true;
                break;
            }
        }
        ssl.clean();
        return hasProjectSearchScope;
    }
}
