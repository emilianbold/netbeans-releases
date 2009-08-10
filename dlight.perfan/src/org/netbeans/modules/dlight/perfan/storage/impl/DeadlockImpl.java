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
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.module.dlight.threads.api.Deadlock;
import org.netbeans.module.dlight.threads.api.DeadlockThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;

/**
 * @author Alexey Vladykin
 */
public final class DeadlockImpl implements Deadlock {

    private final int id;
    private final boolean actual;
    private final List<DeadlockThreadSnapshot> threads;

    private DeadlockImpl(int id, boolean actual, List<DeadlockThreadSnapshot> threads) {
        this.id = id;
        this.actual = actual;
        this.threads = Collections.unmodifiableList(threads);
    }

    public boolean isActual() {
        return actual;
    }

    public List<DeadlockThreadSnapshot> getThreadStates() {
        return threads;
    }

    @Override
    public String toString() {
        return "Deadlock #" + id + " (" + (actual? "actual" : "potential") + ")"; // NOI18N
    }

    private static final Pattern DEADLOCK_PATTERN = Pattern.compile("Deadlock\\s+#(\\d+),\\s+(Actual|Potential)\\s+deadlock"); // NOI18N
    private static final Pattern THREAD_PATTERN = Pattern.compile("\\s+Thread\\s+#\\d+"); // NOI18N
    private static final Pattern LOCK_PATTERN = Pattern.compile("\\s+Lock being (held|requested):\\s+0x([0-9a-fA-F]+)"); // NOI18N

    public static List<DeadlockImpl> fromErprint(String[] lines) {
        List<DeadlockImpl> deadlocks = new ArrayList<DeadlockImpl>();
        ListIterator<String> it = Arrays.asList(lines).listIterator();
        while (it.hasNext()) {
            Matcher m = DEADLOCK_PATTERN.matcher(it.next());
            if (m.matches()) {
                deadlocks.add(parseDeadlock(it, m));
            }
        }
        return deadlocks;
    }

    private static DeadlockImpl parseDeadlock(ListIterator<String> it, Matcher firstLineMatch) {
        int id;
        try {
            id = Integer.parseInt(firstLineMatch.group(1));
        } catch (NumberFormatException ex) {
            id = -1;
        }
        boolean actual = firstLineMatch.group(2).equals("Actual"); // NOI18N

        List<DeadlockThreadSnapshot> threads = new ArrayList<DeadlockThreadSnapshot>();
        while (it.hasNext()) {
            Matcher m = THREAD_PATTERN.matcher(it.next());
            if (m.matches()) {
                threads.add(parseThreadSnapshot(it, m));
            } else {
                break;
            }
        }
        return new DeadlockImpl(id, actual, threads);
    }

    private static DeadlockThreadSnapshot parseThreadSnapshot(ListIterator<String> it, Matcher firstLineMatch) {
        long oldLockAddress = parseLockAddress(it.next(), "held"); // NOI18N
        List<FunctionCall> oldLockStack = FunctionCallImpl.parseStack(it);
        long newLockAddress = parseLockAddress(it.next(), "requested"); // NOI18N
        List<FunctionCall> newLockStack = FunctionCallImpl.parseStack(it);
        return new DeadlockThreadSnapshotImpl(oldLockAddress, oldLockStack, newLockAddress, newLockStack);
    }

    private static long parseLockAddress(String line, String lockType) {
        Matcher m = LOCK_PATTERN.matcher(line);
        if (m.matches() && m.group(1).equals(lockType)) {
            try {
                return Long.parseLong(m.group(2), 16);
            } catch (NumberFormatException ex) {
            }
        }
        return -1;
    }

    private static class DeadlockThreadSnapshotImpl implements DeadlockThreadSnapshot {

        private final long oldLockAddress;
        private final List<FunctionCall> oldLockStack;
        private final long newLockAddress;
        private final List<FunctionCall> newLockStack;

        private DeadlockThreadSnapshotImpl(long oldLockAddress, List<FunctionCall> oldLockStack, long newLockAddress, List<FunctionCall> newLockStack) {
            this.oldLockAddress = oldLockAddress;
            this.oldLockStack = Collections.unmodifiableList(oldLockStack);
            this.newLockAddress = newLockAddress;
            this.newLockStack = Collections.unmodifiableList(newLockStack);
        }

        public long getHeldLockAddress() {
            return oldLockAddress;
        }

        public List<FunctionCall> getHeldLockCallStack() {
            return oldLockStack;
        }

        public long getRequestedLockAddress() {
            return newLockAddress;
        }

        public List<FunctionCall> getRequestedLockCallStack() {
            return newLockStack;
        }
    }
}
