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

package org.netbeans.modules.extexecution.input;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputReader;

/**
 *
 * This class is <i>NotThreadSafe</i>.
 * @author Petr.Hejl
 */
public class StreamInputReader implements InputReader {

    private static final int BUFFER_SIZE = 512;

    private final InputStream is;

    private final ReadableByteChannel channel;

    private final ByteBuffer buffer;

    private final boolean greedy;

    private boolean closed;

    public StreamInputReader(InputStream stream, boolean greedy) {
        assert stream != null;

        InputStream is = stream;
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        this.is = is;
        this.channel = Channels.newChannel(is);
        this.greedy = greedy;
        this.buffer = ByteBuffer.allocateDirect(greedy ? BUFFER_SIZE * 2 : BUFFER_SIZE);
    }

    public int readOutput(InputProcessor outputProcessor) throws IOException {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        // TODO is it safe to mix channel and stream ?
        if (!channel.isOpen() || is.available() <= 0) {
            return 0;
        }

        int fetched = 0;
        // TODO optimization possible
        ByteArrayOutputStream read = new ByteArrayOutputStream(BUFFER_SIZE);
        do {
            buffer.clear();
            int size = channel.read(buffer);
            if (size > 0) {
                buffer.position(0).limit(size);
                fetched += size;

                byte[] toProcess = new byte[size];
                buffer.get(toProcess);
                read.write(toProcess);
            }
        } while (channel.isOpen() && is.available() > 0 && greedy);

        if (outputProcessor != null && fetched > 0) {
            outputProcessor.processInput(read.toByteArray());
        }

        return fetched;
    }

    public void close() throws IOException {
        closed = true;
        if (channel.isOpen()) {
            channel.close();
        }
    }

}
