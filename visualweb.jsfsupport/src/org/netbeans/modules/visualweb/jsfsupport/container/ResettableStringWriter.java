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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.StringWriter;

/**
 * StringWriter with one additional capability: it supports querying the next output position, and
 * resetting the output back to a particular position. This is used to recover from errors when
 * generating output.
 *
 * @author Tor Norbye
 */
public class ResettableStringWriter extends StringWriter {

    /**
     * Returns the index of the next character to be written to the stream, or put another way,
     * the number of characters since the writer was created.
     * @returns character count (not including reset content) since writer was created.
     */
    public int getPosition() {
        return getBuffer().length();
    }

    /**
     * Truncates the written content back to a particular position/index specified.
     * @param position The position to jump back to.
     */
    public void reset(int position) {
        assert position >= 0 && position <= getBuffer().length();
        getBuffer().setLength(position);
    }
}
