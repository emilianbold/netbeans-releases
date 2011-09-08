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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.javacard.ri.card;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

final class ConditionImpl implements Condition {
    private volatile int steps;
    private final RICard card;
    private final Object lock = new Object();

    ConditionImpl(int steps, RICard card) {
        this (steps, card, false);
    }

    ConditionImpl() {
        this (0, null, true);
    }

    private ConditionImpl(int steps, RICard card, boolean done) {
        this.card = card;
        this.steps = steps;
        this.done = done;
    }

    synchronized void countdown() {
        steps--;
        card.log("ConditionImpl " + this + " countdown " + steps); //NOI18N
        if (steps == 0) {
            signalAll();
        }
    }

    public void await() throws InterruptedException {
        if (done) return;
        card.log("ConditionImpl " + this + " await in " + Thread.currentThread()); //NOI18N
        synchronized (lock) {
            lock.wait();
        }
    }

    public void awaitUninterruptibly() {
        if (done) return;
        card.log("ConditionImpl " + this + " awaitUninterruptibly " + Thread.currentThread()); //NOI18N
        try {
            synchronized (lock) {
                await();
            }
        } catch (InterruptedException e) {
            awaitUninterruptibly();
        }
    }

    public long awaitNanos(long nanosTimeout) throws InterruptedException {
        if (done) return 0L;
        synchronized (lock) {
            lock.wait(0, (int) nanosTimeout);
        }
        return 0;
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        if (done) return true;
        long millis = unit.convert(time, TimeUnit.MILLISECONDS);
        synchronized (lock) {
            lock.wait(millis);
        }
        return card.getState().isRunning();
    }

    public boolean awaitUntil(Date deadline) throws InterruptedException {
        if (done) return true;
        long time = deadline.getTime() - System.currentTimeMillis();
        await(time, TimeUnit.MILLISECONDS);
        return card.getState().isRunning();
    }

    private volatile boolean done;
    public void signal() {
        signalAll();
    }

    public void signalAll() {
        done = true;
        card.log("ConditionImpl " + this + " signalAll "); //NOI18N
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
