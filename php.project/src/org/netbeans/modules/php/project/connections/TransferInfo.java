/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TransferInfo {

    private final Set<TransferFile> transfered = new HashSet<TransferFile>();
    private final Set<TransferFile> failed = new HashSet<TransferFile>();
    private final Set<TransferFile> ignored = new HashSet<TransferFile>();
    private long runtime;

    public Set<TransferFile> getTransfered() {
        return Collections.unmodifiableSet(transfered);
    }

    public Set<TransferFile> getFailed() {
        return Collections.unmodifiableSet(failed);
    }

    public Set<TransferFile> getIgnored() {
        return Collections.unmodifiableSet(ignored);
    }

    public long getRuntime() {
        return runtime;
    }

    public boolean isTransfered(TransferFile transferFile) {
        return transfered.contains(transferFile);
    }

    public boolean isFailed(TransferFile transferFile) {
        return failed.contains(transferFile);
    }

    public boolean isIgnored(TransferFile transferFile) {
        return ignored.contains(transferFile);
    }

    void addTransfered(TransferFile transferFile) {
        assert !failed.contains(transferFile) && !ignored.contains(transferFile);
        transfered.add(transferFile);
    }

    void addFailed(TransferFile transferFile) {
        assert !transfered.contains(transferFile) && !ignored.contains(transferFile);
        failed.add(transferFile);
    }

    void addIgnored(TransferFile transferFile) {
        assert !transfered.contains(transferFile) && !failed.contains(transferFile);
        ignored.add(transferFile);
    }

    void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [transfered: "); // NOI18N
        sb.append(transfered);
        sb.append(", failed: "); // NOI18N
        sb.append(failed);
        sb.append(", ignored: "); // NOI18N
        sb.append(ignored);
        sb.append(", runtime: "); // NOI18N
        sb.append(runtime);
        sb.append(" ms]"); // NOI18N
        return sb.toString();
    }
}
