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

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer.BufferType;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;

/**
 * implementation of APTDriver
 * This driver supports synchronized access with waiting when necessary to the
 * file's APT.
 * Wait if need to create and another process already creating.
 */
public class APTDriverImpl {
    /** map of active creators */
    private final ConcurrentHashMap<CharSequence, APTSyncCreator> file2creator = new ConcurrentHashMap<CharSequence, APTSyncCreator>();
    /** static shared sync map */
    private Map<CharSequence, Reference<APTFile>> file2ref2apt = new ConcurrentHashMap<CharSequence, Reference<APTFile>>();
    private Map<CharSequence, APTFile> file2apt = new ConcurrentHashMap<CharSequence, APTFile>();

    /** instance fields */

    /** Creates a new instance of APTCreator */
    public APTDriverImpl() {
    }

    public APTFile findAPT(APTFileBuffer buffer, boolean withTokens, APTFile.Kind aptKind) throws IOException {
        CharSequence path = buffer.getAbsolutePath();
        APTFile apt = _getAPTFile(path, withTokens);
        if (apt == null) {
            APTSyncCreator creator = file2creator.get(path);
            if (creator == null) {
                // no need to sync on ConcurrentHashMap due to putIfAbsent method
                creator = new APTSyncCreator();
                APTSyncCreator old = file2creator.putIfAbsent(path, creator);
                if (old != null) {
                    creator = old;
                }
            }
            assert (creator != null);
            // use instance synchronized method to prevent
            // multiple apt creating for the same file
            apt = creator.findAPT(buffer, withTokens, aptKind);
            file2creator.remove(path);
        }
        return apt;
    }

    public void invalidateAPT(APTFileBuffer buffer) {
        CharSequence path = buffer.getAbsolutePath();
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            file2ref2apt.remove(path);
        } else {
            file2apt.remove(path);
        }
    }

    public void invalidateAll() {
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            file2ref2apt.clear();
        } else {
            file2apt.clear();
        }
    }

    private class APTSyncCreator {
        private APTFile fullAPT = null;
        private APTFile lightAPT = null;
        public APTSyncCreator() {
        }

        private TokenStream getTokenStream(APTFileBuffer buffer, APTFile.Kind aptKind, boolean isLight) throws IOException {
            String bufName = buffer.getAbsolutePath().toString();
            char[] charBuffer = buffer.getCharBuffer();
            trackActivity(bufName, charBuffer.length, isLight);
            if (isLight) {
                return APTTokenStreamBuilder.buildLightTokenStream(bufName, charBuffer, aptKind);
            } else {
                return APTTokenStreamBuilder.buildTokenStream(bufName, charBuffer, aptKind);
            }
        }

        /** synchronized on instance */
        public synchronized APTFile findAPT(APTFileBuffer buffer, boolean withTokens, APTFile.Kind aptKind) throws IOException {
            CharSequence path = buffer.getAbsolutePath();
            // quick exit: check if already was added by another creator
            // during wait
            if (withTokens && fullAPT != null) {
                return fullAPT;
            } else if (!withTokens && lightAPT != null) {
                return lightAPT;
            }
            APTFile apt = _getAPTFile(path, withTokens);
            if (apt == null) {
                // ok, create new apt
                TokenStream ts = getTokenStream(buffer, aptKind, !withTokens);
                // build apt from light token stream
                apt = APTBuilder.buildAPT(buffer.getFileSystem(), path, ts, aptKind);
                BufferType type = buffer.getType();
                if (!withTokens) {
                    fullAPT = null;
                    if (apt != null) {
                        if (APTTraceFlags.TEST_APT_SERIALIZATION) {
                            APTFile test = (APTFile) APTSerializeUtils.testAPTSerialization(buffer, apt);
                            if (test != null) {
                                apt = test;
                            } else {
                                System.err.println("error on serialization apt for file " + path); // NOI18N
                            }
                        }
                        lightAPT = apt;
                        _putAPTFile(path, lightAPT, false, type);
                    }
                } else {
                    fullAPT = apt;
                    if (apt != null) {
                        if (APTTraceFlags.TEST_APT_SERIALIZATION) {
                            APTFile test = (APTFile) APTSerializeUtils.testAPTSerialization(buffer, apt);
                            if (test != null) {
                                apt = test;
                            } else {
                                System.err.println("error on serialization apt for file " + path); // NOI18N
                            }
                        }
                        _putAPTFile(path, fullAPT, true, type);
                        lightAPT = (APTFile) APTBuilder.buildAPTLight(apt);
                        _putAPTFile(path, lightAPT, false, type);
                    }
                }
            }
            return apt;
        }
    }

    private APTFile _getAPTFile(CharSequence path, boolean withTokens) {
        if (withTokens) {
            // we do not cache full apt
            return null;
        }
        APTFile apt;
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            Reference<APTFile> aptRef = file2ref2apt.get(path);
            apt = aptRef == null ? null : aptRef.get();
        } else {
            apt = file2apt.get(path);
        }
        return apt;
    }

    private void _putAPTFile(CharSequence path, APTFile apt, boolean withTokens, APTFileBuffer.BufferType bufType) {
        if (withTokens) {
            // we do not cache full apt
            return;
        }
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            if (bufType == APTFileBuffer.BufferType.START_FILE) {
                // this kind of buffer worth to cache to not loose in scalability as shown by measurements.
                // It helps to do multi reparses of the same file
                // without touching disk
                file2ref2apt.put(path, new WeakReference<APTFile>(apt));
            } else {
                file2ref2apt.put(path, new SoftReference<APTFile>(apt));
            }
        } else {
            file2apt.put(path, apt);
        }
    }

    public void close() {
        invalidateAll();
    }

    public void traceActivity() {
        long totalReads = 0;
        long fileSizes = 0;
        int ligthNrReads = 0;
        int nrReads = 0;
        int noAPTLightFiles = 0;
        for (Map.Entry<CharSequence, FileTraceData> entry : readFiles.entrySet()) {
            final FileTraceData data = entry.getValue();
            final long readBytes = data.totalBytes();
            assert data.totalLightReads() < data.totalReads() : "strange params " + data + " " + entry.getKey();
//            System.err.printf("[%d|%d][%d|%d]%d\t:%s\n", data.totalReads(), data.getLength(), data.totalLightReads(), data.totalReads(), readBytes, entry.getKey());
            totalReads += readBytes;
            fileSizes += data.getLength();
            int lightReads = data.totalLightReads();
            if (lightReads == 0) {
                noAPTLightFiles++;
            }
            ligthNrReads += lightReads;
            nrReads += data.totalReads();
        }
        double ratio = fileSizes == 0 ? 0 : (1.0 * totalReads) / fileSizes;
        totalReads /= 1024;
        fileSizes /= 1024;
        System.err.printf("StreamBuilder has %d (%d no APTLight queries) entries, ratio is %f (%d reads where %d Light) [read %dKb from files of total size %dKb]%n", readFiles.size(), noAPTLightFiles, ratio, nrReads, ligthNrReads, totalReads, fileSizes);
        readFiles.clear();
    }

    private static final class FileTraceData {

        private final int length;
        private final AtomicInteger nrLightReads = new AtomicInteger(0);
        private final AtomicInteger nrReads = new AtomicInteger(0);

        public FileTraceData(int length) {
            this.length = length;
        }

        public void add(int bytes, CharSequence name, boolean light) {
            assert bytes == length;
            nrReads.incrementAndGet();
            if (light) {
                int lights = nrLightReads.incrementAndGet();
                assert lights == 1 : "more that one APT Light created for " + name;
            }
        }

        public int totalBytes() {
            return nrReads.get() * length;
        }

        public int totalReads() {
            return nrReads.get();
        }

        public int totalLightReads() {
            return nrLightReads.get();
        }

        public int getLength() {
            return length;
        }

        @Override
        public String toString() {
            return "Pair{" + "length=" + length + ", nrLightReads=" + nrLightReads + ", nrReads=" + nrReads + '}'; // NOI18N
        }
    }

    private void trackActivity(CharSequence name, int len, boolean light) {
        if (true) {
            return;
        }
        FileTraceData size = readFiles.get(name);
        if (size == null) {
            size = new FileTraceData(len);
            FileTraceData prev = readFiles.putIfAbsent(name, size);
            if (prev != null) {
                size = prev;
            }
        }
        size.add(len, name, light);
    }
    private final ConcurrentMap<CharSequence, FileTraceData> readFiles = new ConcurrentHashMap<CharSequence, FileTraceData>();

}
