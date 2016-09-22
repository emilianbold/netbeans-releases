/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

import java.util.Arrays;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

// Implementation of a StringBuffer-like object that does not have the
// unfortunate side-effect of creating Strings with very large buffers.

public class ANTLRStringBuffer {
    protected char[] buffer = null;
    protected int length = 0;		// length and also where to store next char


    public ANTLRStringBuffer() {
        buffer = new char[50];
    }

    public ANTLRStringBuffer(int n) {
        buffer = new char[n];
    }

    public final void append(char c) {
        // This would normally be  an "ensureCapacity" method, but inlined
        // here for speed.
        if (length >= buffer.length) {
            // Compute a new length that is at least double old length
            int newSize = buffer.length;
            while (length >= newSize) {
                newSize *= 2;
            }
            // Allocate new array and copy buffer
            buffer = Arrays.copyOf(buffer, newSize);
        }
        buffer[length] = c;
        length++;
    }

    public final void append(String s) {
        for (int i = 0; i < s.length(); i++) {
            append(s.charAt(i));
        }
    }

    public final char charAt(int index) {
        return buffer[index];
    }

    final public char[] getBuffer() {
        return buffer;
    }

    public final int length() {
        return length;
    }

    public final void setCharAt(int index, char ch) {
        buffer[index] = ch;
    }

    public final void setLength(int newLength) {
        if (newLength < length) {
            length = newLength;
        }
        else {
            while (newLength > length) {
                append('\0');
            }
        }
    }

    public final String toString() {
        return new String(buffer, 0, length);
    }
}
