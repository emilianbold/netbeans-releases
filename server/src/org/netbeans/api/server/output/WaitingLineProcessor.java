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

package org.netbeans.api.server.output;

import java.util.regex.Pattern;
import org.openide.util.Parameters;

/**
 * LineProcessor implementing barrier for waiting on specific
 * pattern matching line.
 * <p>
 * This class is <i>ThreadSafe</i>.
 *
 * @author Petr Hejl
 */
public final class WaitingLineProcessor implements LineProcessor {

    private final Pattern pattern;

    /** <i>GuardedBy("this")</i> */
    private boolean processed = false;

    /**
     * Constructs the processor implementing the the barrier. Barrier is opened
     * when it will process the line that matches the pattern.
     *
     * @param pattern pattern triggering the opening
     */
    public WaitingLineProcessor(Pattern pattern) {
        Parameters.notNull("pattern", pattern);

        this.pattern = pattern;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks the line for the specified pattern. If the line matches the
     * pattern all waiting threads are notified.
     */
    public void processLine(String line) {
        if (line != null) {
            if (pattern.matcher(line).matches()) {
                synchronized (this) {
                    processed = true;
                    notifyAll();
                }
            }
        }
    }

    /**
     * Noop for this processor.
     */
    public synchronized void reset() {

    }

    /**
     * If the line matching the pattern not yet passed through processor
     * the calling thread will be suspended, waiting for this event. Otherwise
     * the thread just passes through.
     *
     * @throws java.lang.InterruptedException if the thread was interrupted
     *             while waiting
     */
    public synchronized void await() throws InterruptedException {
        while (!processed) {
            wait();
        }
    }

    /**
     * If the line matching the pattern not yet passed through processor
     * the calling thread will be suspended, waiting for this event.
     * The waiting period is limited by the timeout value. This method does
     * not prevent spurious wakeups.
     *
     * @param timeout wait timeout in millis
     * @throws java.lang.InterruptedException if the thread was interrupted
     *             while waiting
     */
    public synchronized void await(long timeout) throws InterruptedException {
        wait(timeout);
    }

}
