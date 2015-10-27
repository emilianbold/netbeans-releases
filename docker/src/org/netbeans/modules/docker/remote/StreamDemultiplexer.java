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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public class StreamDemultiplexer {

    private static final Logger LOGGER = Logger.getLogger(StreamDemultiplexer.class.getName());

    private final InputStream is;

    public StreamDemultiplexer(InputStream is) {
        this.is = is;
    }

    public Result getNext() {
        byte[] buffer = new byte[8];
        byte[] content = new byte[256];

        try {
            int sum = 0;
            do {
                int read = is.read(buffer, sum, buffer.length - sum);
                if (read < 0) {
                    return null;
                }
                sum += read;
            } while (sum < 8);
            // now we have 8 bytes
            assert buffer.length == 8;

            boolean error;
            int size = ByteBuffer.wrap(buffer).getInt(4);
            if (buffer[0] == 0 || buffer[0] == 1) {
                error = false;
            } else if (buffer[0] == 2) {
                error = true;
            } else {
                throw new IOException("Unparsable stream " + buffer[0]);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
            sum = 0;
            do {
                int read = is.read(content, 0, Math.min(size, content.length));
                if (read < 0) {
                    return null;
                }
                bos.write(content, 0, read);
                sum += read;
            } while (sum < size);
            return new Result(bos.toByteArray(), error);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }

    public static class Result {

        private final byte[] data;

        private final boolean error;

        private Result(byte[] data, boolean error) {
            this.data = data;
            this.error = error;
        }

        public byte[] getData() {
            return data;
        }

        public boolean isError() {
            return error;
        }
    }
}
