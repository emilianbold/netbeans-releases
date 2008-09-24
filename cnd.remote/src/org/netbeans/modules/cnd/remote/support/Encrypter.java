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

package org.netbeans.modules.cnd.remote.support;

/**
 *
 * @author Vladimir Voskresensky
 */
public class Encrypter {
    private final byte[] passPhrase;
    private final static String emptyPwd = "$$$EmptyPassword$$$"; // NOI18N
    private final static char delimeter = '\000';
    Encrypter(String passPhrase) {
        this.passPhrase = passPhrase.getBytes();
    }

    public String encrypt(String str) {
        // now just xor with passphrase :-(
        if (str != null && str.length() == 0) {
            str = emptyPwd;
        }
        return xor(str + delimeter);
    }

    public String decrypt(String str) {
        // now just xor with passphrase :-(
        String out = xor(str);
        int delimIndex = out == null ? -1 : out.indexOf(delimeter);
        if (delimIndex > -1) {
            out = out.substring(0, delimIndex);
        }
        if (emptyPwd.equals(out)) {
            out = "";
        }
        return out;
    }
    
    private String xor(String str) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes();
        int len = Math.max(passPhrase.length, bytes.length);
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = (byte) (bytes[i % bytes.length] ^ passPhrase[i % passPhrase.length]);
        }
        return new String(out);
    }    
}
