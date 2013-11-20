/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.storage.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.storage.FSConverter;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;

/**
 *
 * @author Alexander Simon
 */
public final class RepositoryDataOutputStream extends DataOutputStream implements RepositoryDataOutput {

    private final UnitsConverter unitIDConverter;
    private final FSConverter fsConverter;
    private final WriteLayerCapability wc;
    private final LayerKey layerKey;
    private static final OutputStreamEx outputStream = new OutputStreamEx();

    public RepositoryDataOutputStream(LayerKey layerKey, WriteLayerCapability wc, UnitsConverter unitIDConverter, FSConverter fsConverter) {
        super(outputStream.reset());
        this.unitIDConverter = unitIDConverter == null ? new NoopUnitIDConverter() : unitIDConverter;
        this.fsConverter = fsConverter == null ? new NoopFSConverter() : fsConverter;
        this.wc = wc;
        this.layerKey = layerKey;
    }

    @Override
    public void writeCharSequenceUTF(CharSequence s) throws IOException {
        UTF.writeUTF(s, this);
    }

    @Override
    public void writeUnitId(int unitId) throws IOException {
        writeInt(unitIDConverter.clientToLayer(unitId));
    }

    @Override
    public void writeFileSystem(FileSystem fileSystem) throws IOException {
        writeInt(fsConverter.clientToLayer(fileSystem));
    }

    @Override
    public void commit() {
        wc.write(layerKey, outputStream.getBuffer());
    }

    private static final class NoopUnitIDConverter implements UnitsConverter {

        @Override
        public int clientToLayer(int clientUnitID) {
            return clientUnitID;
        }

        @Override
        public int layerToClient(int unitIDInLayer) {
            return unitIDInLayer;
        }
    }

    private static final class NoopFSConverter implements FSConverter {

        @Override
        public FileSystem layerToClient(int fsIdx) {
            return new LocalFileSystem();
        }

        @Override
        public int clientToLayer(FileSystem fileSystem) {
            return 0;
        }
    }

    private static class OutputStreamEx extends OutputStream {

        private ByteBuffer buffer;

        public OutputStreamEx() {
            buffer = ByteBuffer.allocateDirect(1024);
        }

        public OutputStreamEx reset() {
            buffer.clear();
            return this;
        }

        public ByteBuffer getBuffer() {
            ByteBuffer result = buffer;
            if (buffer.capacity() > 4096) {
                buffer = ByteBuffer.allocateDirect(1024);
            }
            result.flip();
            return result;
        }

        @Override
        public void write(int b) throws IOException {
            int newposition = buffer.position() + 1;
            if (newposition > buffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(Math.max(buffer.capacity() << 1, newposition));
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
            buffer.put((byte) b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            assert off == 0;
            int newposition = buffer.position() + len;
            if (newposition > buffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(Math.max(buffer.capacity() << 1, newposition));
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
            buffer.put(b, off, len);
        }
    }
}
