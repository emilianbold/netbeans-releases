/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.procfs.reader.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.util.DLightLogger;

final class ReusableByteBuffer {

    private final static Logger log = DLightLogger.getLogger(ReusableByteBuffer.class);
    private final static boolean TRACE = Boolean.getBoolean("TraceReusableByteBuffer"); // NOI18N
    public final byte[] buffer;
    private final int size;
    private final int count;
    private final ConcurrentHashMap<Integer, Integer> marks;
    private final AtomicInteger idx = new AtomicInteger(0);

    public ReusableByteBuffer(int chunksize, int chunkcount) {
        if (chunksize > 0xFFFF && chunkcount > 0xFFFF) {
            throw new IllegalArgumentException();
        }

        this.size = chunksize;
        this.count = chunkcount;
        buffer = new byte[(0xFFFF & count) * (0xFFFF & size)];
        marks = new ConcurrentHashMap<Integer, Integer>(count);
    }

    public int getAndLockOffset() {
        while (true) {
            if (marks.size() == count) {
                if (TRACE) {
                    log.info("All chuncks are locked!"); // NOI18N
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }

            int offset = (0xFFFF & idx.getAndIncrement() % count) * size;
            Integer marker = marks.get(offset);

            if (marker != null) {
                continue;
            }

            if (marks.putIfAbsent(offset, offset) != null) {
                continue;
            }

            return offset;
        }
    }

    public void unlock(int offset) {
        marks.remove(offset);
    }
}
