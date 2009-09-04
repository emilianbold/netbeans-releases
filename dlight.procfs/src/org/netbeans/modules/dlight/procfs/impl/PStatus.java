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

import java.io.IOException;
import java.io.InputStream;

public final class PStatus {

    public static class ThreadsInfo {

        public final int pr_nlwp;
        public final int pr_nzomb;

        private ThreadsInfo() {
            reader.seek(4);
            pr_nlwp = reader._int();
            reader.seek(268); // TODO
            pr_nzomb = reader._int();
        }
    }

    public static class PIDInfo {

        public final int pr_pid;

        private PIDInfo() {
            reader.seek(8);
            pr_pid = reader._int();
        }
    }
    private final static int s_total = 272; // x64... TODO: other platforms...
    private final static byte[] buffer = new byte[s_total];
    private final static DataReader reader = new DataReader(buffer);

    private PStatus() {
    }

    private static synchronized void readData(InputStream inputStream) throws IOException {
        try {
            inputStream.read(buffer, 0, s_total);
        } finally {
            inputStream.close();
        }
    }

    static synchronized PStatus get(InputStream inputStream) throws IOException {
        readData(inputStream);
        return new PStatus();
    }

    static synchronized ThreadsInfo getThreadsInfo(InputStream inputStream) throws IOException {
        readData(inputStream);
        return new ThreadsInfo();
    }

    static synchronized PIDInfo getPIDInfo(InputStream inputStream) throws IOException {
        readData(inputStream);
        return new PIDInfo();
    }
}
