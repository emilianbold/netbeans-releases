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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import java.io.*;


/**
 * util for encode and decode filecontent
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class Base64 {
    /**
     *
     *
     */
    private Base64() {
        super();
    }

    /**
     * Encodes an array of bytes into a Base64 string, without compression.
     * Resulting string is uniform with no carriage returns.
     *
     * @param bytes binary data to be encoded
     * @return Base64 encoded string
     */
    public static String encode(byte[] bytes) {
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        String result = encoder.encodeBuffer(bytes);

        // Since the JavaSoft guys put in a carriage return at 57 bytes,
        // we need to remove them so we have one long encoded string
        result = result.replaceAll("\r", "");
        result = result.replaceAll("\n", "");

        return result;
    }

    /**
     * Decodes a string using Base64 decoding into an array of bytes
     * without compression.  Strings not previously Base64 encoded will
     * succeed.
     *
     * @param s string to be decoded (assumed to be Base64 encoded string)
     * @return decoded array of bytes or <code>s.getBytes()</code> on exception during execution
     */
    public static byte[] decode(String s) {
        try {
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

            return decoder.decodeBuffer(s);
        } catch (IOException e) {
            e.printStackTrace();

            // Ignore, use the provided text directly
            return s.getBytes();
        }
    }
}
