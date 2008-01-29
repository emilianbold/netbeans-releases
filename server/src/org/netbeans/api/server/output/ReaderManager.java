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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.server.output.ReaderThread;
import org.openide.util.Parameters;

/**
 * Manager for reading and processing the lines provided {@link LineReader}s.
 * <p>
 * Note that <i>it is highly recommended to have all used {@link LineReader}s
 * responsive to interruption</i>. Otherwise the delay between the {@link #stop()}
 * call and actual finish of the processing can be significant.
 * <p>
  * The simple example for processing the stream:
 * <p>
 * <pre>
 *     InputStream&nbsp;is&nbsp;=&nbsp;...;<br>
 *     Charset&nbsp;charset&nbsp;=&nbsp;...;<br>
 *     LineProcessor&nbsp;myProcessor&nbsp;=&nbsp;...;<br>
 *     <br>
 *     ReaderManager&nbsp;manager&nbsp;=&nbsp;ReaderManager.newManager(<br>
 *     &nbsp;&nbsp;&nbsp;&nbsp;LineReaders.forStream(is,&nbsp;charset),&nbsp;myProcessor);<br>
 *     manager.start();<br>
 *     ...<br>
 *     manager.stop();<br>
 * </pre> 
 * <p>
 * This class is <i>ThreadSafe</i>.
 * 
 * @author Petr Hejl
 */
public final class ReaderManager {

    static {
        ReaderThread.Accessor.DEFAULT = new ReaderThread.Accessor() {

            @Override
            public void notifyFinished(ReaderManager manager, ReaderThread thread) {
                manager.notifyFinished(thread);
            }
        };
    }

    private static final Logger LOGGER = Logger.getLogger(ReaderManager.class.getName());

    private final List<ReaderThread> threads = new ArrayList<ReaderThread>();

    /** <i>GuardedBy("this")</i> */
    private boolean started = false;

    /** <i>GuardedBy("this")</i> */
    private int finishedCount = 0;

    private ReaderManager(Pair... pairs) {
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i] != null) {
                threads.add(new ReaderThread(this, pairs[i].getReader(),
                        pairs[i].getProcessor()));
            }
        }
    }

    /**
     * Creates the manager managing any amount of {@link LineReader}s with
     * the {@link LineProcessor}s associated to them.
     *
     * @param pairs the pairs of the {@link LineReader}s with the {@link LineProcessor}s
     *             associated to them
     * @return the manager for hadling all given {@link LineReader}s
     */
    public static ReaderManager newManager(Pair... pairs) {
        return new ReaderManager(pairs);
    }

    /**
     * Creates the manager managing the single {@link LineReader} with
     * corresponding {@link LineProcessor}.
     *
     * @param lineReader the source of the lines to process, must not be <code>null</code>
     * @param lineProcessor representation of custom line processing code, may be <code>null</code>
     * @return the manager for handling the given {@link LineReader}
     */
    public static ReaderManager newManager(LineReader lineReader, LineProcessor lineProcessor) {
        return newManager(new ReaderManager.Pair(lineReader, lineProcessor));
    }

    /**
     * Starts the manager. Starts tailing all managed {@link LineReader}s
     * and processing the data with associated {@link LineProcessor}s (if any).
     * <p>
     * Calling start on the instance once started has no effect.
     */
    public synchronized void start() {
        if (started) {
            return;
        }

        LOGGER.log(Level.FINE, "Starting reader manager"); // NOI18N

        for (ReaderThread thread : threads) {
            thread.start();
        }

        started = true;
    }

    /**
     * Stops the manager. All owned {@link LineReader}s are closed
     * through {@link LineReader#close()}.
     * <p>
     * When this method exits it is not guaranteed that processing was already
     * finished. Use {@link #isRunning()} to check if it was already finished.
     */
    public synchronized void stop() {
        LOGGER.log(Level.FINE, "Stopping reader manager"); // NOI18N

        for (ReaderThread thread : threads) {
            thread.interrupt();
        }
    }

    /**
     * Returns <code>false</code> if the manager already finished the processing.
     *
     * @return <code>false</code> if the manager already finished the processing
     */
    public synchronized boolean isRunning() {
        return finishedCount < threads.size();
    }

    /**
     * Helper method for tests.
     *
     * @return
     */
    List<ReaderThread> getThreads() {
        return Collections.unmodifiableList(threads);
    }

    private synchronized void notifyFinished(ReaderThread thread) {
        finishedCount++;
    }

    /**
     * The container class for reader and processor pairs.
     */
    public static final class Pair {

        private final LineReader reader;

        private final LineProcessor processor;

        /**
         * Creates the new pair of the reader and processor.
         *
         * @param reader the reader value, must not be <code>null</code>
         * @param processor the associated processor, <code>null</code> is allowed
         */
        public Pair(LineReader reader, LineProcessor processor) {
            Parameters.notNull("reader", reader);

            this.reader = reader;
            this.processor = processor;
        }

        private LineReader getReader() {
            return reader;
        }

        private LineProcessor getProcessor() {
            return processor;
        }

    }

}
