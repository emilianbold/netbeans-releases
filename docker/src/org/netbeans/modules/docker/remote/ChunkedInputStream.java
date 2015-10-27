/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.remote;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Petr Hejl
 */
public class ChunkedInputStream extends FilterInputStream {

    private int remaining;

    private boolean end;

    public ChunkedInputStream(InputStream is) {
        super(is);
    }

    @Override
    public int read() throws IOException {
        if (end) {
            return -1;
        }

        if (remaining == 0) {
            String line = HttpUtils.readResponseLine(in);
            if (line == null) {
                end = true;
                return -1;
            }
            int semicolon = line.indexOf(';');
            if (semicolon > 0) {
                line = line.substring(0, semicolon);
            }
            try {
                remaining = Integer.parseInt(line, 16);
                if (remaining == 0) {
                    end = true;
                    return -1;
                }
            } catch (NumberFormatException ex) {
                throw new IOException("Wrong chunk size");
            }
        }
        remaining--;
        int ret = in.read();
        if (remaining == 0) {
            HttpUtils.readResponseLine(in);
        }
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (end) {
            return -1;
        }
        if (len == 0) {
            return 0;
        }
        if (remaining == 0) {
            int ret = read();
            if (ret < 0) {
                return ret;
            }
            b[off] = (byte) ret;
            return 1;
        } else {
            int count = 0;
            int limit = Math.min(len, remaining);
            for (int i = off; i < limit; i++) {
                int value = read();
                if (value < 0) {
                    return count;
                }
                count++;
                b[off + i] = (byte) value;
            }
            return count;
        }
    }
}
