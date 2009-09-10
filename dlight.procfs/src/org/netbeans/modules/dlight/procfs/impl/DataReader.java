/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.procfs.impl;

import java.util.concurrent.atomic.AtomicInteger;

class DataReader {

    private static volatile int bigendian = 0;
    private final byte[] buffer;
    private final AtomicInteger offset;

    DataReader(final byte[] buffer) {
        this.buffer = buffer;
        offset = new AtomicInteger(0);
    }

    static void switchEndian() {
        bigendian = 1 - bigendian;
    }

    void seek(int pos) {
        offset.set(pos);
    }

    int _int() {
        int b1 = 0xFF & buffer[offset.getAndIncrement()];
        int b2 = 0xFF & buffer[offset.getAndIncrement()];
        int b3 = 0xFF & buffer[offset.getAndIncrement()];
        int b4 = 0xFF & buffer[offset.getAndIncrement()];

        return bigendian == 0 ? (b4 << 24 | b3 << 16 | b2 << 8 | b1) : (b1 << 24 | b2 << 16 | b3 << 8 | b4);
    }

    Timestruc _time() {
        return new Timestruc(_int(), _int());
    }
}
