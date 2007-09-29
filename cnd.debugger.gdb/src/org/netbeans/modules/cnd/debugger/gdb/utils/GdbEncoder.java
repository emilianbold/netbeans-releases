/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 *
 * @author gordonp
 */
public class GdbEncoder extends CharsetEncoder {
    
    public GdbEncoder(Charset cs, CharsetEncoder encoder) {
        super(cs, encoder.averageBytesPerChar(), encoder.maxBytesPerChar());
    }
    
    public CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        char[] sa = src.array();
        int sp = src.arrayOffset() + src.position();
        int sl = src.arrayOffset() + src.limit();
        assert (sp <= sl);
        sp = (sp <= sl ? sp : sl);
        
        byte[] da = dst.array();
        int dp = dst.arrayOffset() + dst.position();
        int dl = dst.arrayOffset() + dst.limit();
        assert (dp <= dl);
        dp = (dp <= dl ? dp : dl);
        
        try {
            while (sp < sl) {
                char c = sa[sp];

                if (c < 0x80) {
                    // Have at most seven bits
                    if (dp >= dl) {
                        return CoderResult.OVERFLOW;
                    }
                    da[dp++] = (byte) c;
                    sp++;
                    continue;
                }

                // 2 bytes, 11 bits
                if (c < 0x800) {
                    if (dl - dp < 8) {
                        return CoderResult.OVERFLOW;
                    }
                    String b1 = Integer.toOctalString(0xc0 | ((c >> 06)));
                    da[dp++] = '\\';
                    da[dp++] = (byte) (b1.charAt(0) & 0x7f);
                    da[dp++] = (byte) (b1.charAt(1) & 0x7f);
                    da[dp++] = (byte) (b1.charAt(2) & 0x7f);
                    String b2 = Integer.toOctalString(0x80 | ((c >> 00) & 0x3f));
                    da[dp++] = '\\';
                    da[dp++] = (byte) (b2.charAt(0) & 0x7f);
                    da[dp++] = (byte) (b2.charAt(1) & 0x7f);
                    da[dp++] = (byte) (b2.charAt(2) & 0x7f);
                    sp++;
                    continue;
                }
                
                if (c <= '\uFFFF') {
                    // 3 bytes, 16 bits
                    if (dl - dp < 3) {
                        return CoderResult.OVERFLOW;
                    }
                    da[dp++] = (byte) (0xe0 | ((c >> 12)));
                    da[dp++] = (byte) (0x80 | ((c >> 06) & 0x3f));
                    da[dp++] = (byte) (0x80 | ((c >> 00) & 0x3f));
                    sp++;
                    continue;
                }
            }
            return CoderResult.UNDERFLOW;
        } finally {
            src.position(sp - src.arrayOffset());
            dst.position(dp - dst.arrayOffset());
        }
    }

}
