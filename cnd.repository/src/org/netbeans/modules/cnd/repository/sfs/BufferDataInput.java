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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;
import java.nio.*;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.util.UnitCodec;

/**
 * ByteBuffer based DataInput implementation
 * @author Vladimir Kvashin
 */
public class BufferDataInput implements RepositoryDataInput, SharedStringBuffer {
    
    private final ByteBuffer buffer;
    private final UnitCodec unitCodec;
    
    public BufferDataInput(ByteBuffer buffer, UnitCodec unitCodec) {
        this.buffer = buffer;
        this.unitCodec = unitCodec;
    }

    @Override
    public byte readByte() throws IOException {
        return buffer.get();
    }
    
    @Override
    public char readChar() throws IOException {
        return buffer.getChar();
    }
    
    @Override
    public int readInt() throws IOException {
        return buffer.getInt();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        short result = buffer.getShort();
        return ((int) result) & 0x0000FFFF;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return buffer.get() != 0;
    }

    @Override
    public double readDouble() throws IOException {
        return buffer.getDouble();
    }
    
    @Override
    public float readFloat() throws IOException {
        return buffer.getFloat();
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b;
        read:
        while (buffer.hasRemaining()) {
            switch (b = buffer.get()) {
                case '\r':
                    if (buffer.hasRemaining()) {
                        byte next = buffer.get();
                        if (next != '\n') {
                            buffer.position(buffer.position() - 1);
                        }
                    }
                    break read;
                case '\n':
                    break read;
                default:
                    sb.append((char) b);
                    break;
            }
        }
        return sb.toString();
    }
    
    @Override
    public long readLong() throws IOException {
        return buffer.getLong();
    }

    @Override
    public short readShort() throws IOException {
        return buffer.getShort();
    }

    @Override
    public String readUTF() throws IOException {
        return UTF.readUTF(this);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        byte b = buffer.get();
        return ((short) b) & 0x00FF;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int skip = Math.min(n, buffer.remaining());
        buffer.position(buffer.position() + skip);
        return skip;
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        buffer.get(b, off, len);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        buffer.get(b);
    }

    @Override
    public CharSequence readCharSequenceUTF() throws IOException {
        return UTF.readCharSequenceUTF(this);
    }

    @Override
    public int readUnitId() throws IOException {
        return unitCodec.addRepositoryID(readInt());
    }

    private static final int sharedArrySize = 1024;
    private final byte[] sharedByteArray = new byte[sharedArrySize];
    private final char[] sharedCharArray = new char[sharedArrySize];
    
    @Override
    public final byte[] getSharedByteArray() {
        return sharedByteArray;
    }

    @Override
    public final char[] getSharedCharArray() {
        return sharedCharArray;
    }

    @Override
    public final int getSharedArrayLehgth() {
        return sharedArrySize;
    }
}
