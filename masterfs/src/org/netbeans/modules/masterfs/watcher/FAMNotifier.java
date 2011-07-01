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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.watcher;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.ByReference;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Notifier implementation using fam library
 *
 * @author Egor Ushakov
 */
public class FAMNotifier extends Notifier<Integer> {
    private final FAMLibrary.FAMConnection conn;
    private final FAMLibrary lib;
    private final Map<Integer, String> map = Collections.synchronizedMap(new HashMap<Integer, String>());

    private final BlockingQueue<String> events = new LinkedBlockingQueue<String>();
    private final Thread eventReader;
    private volatile boolean stopped = false;
            
    // limit unanswered requests to avoid FAM hang, see IZ 199497
    private static int ACTIVE_REQUESTS_LIMIT = 100;
    private final Set<Integer> activeRequests = Collections.synchronizedSet(new HashSet<Integer>());
    
    private static final Logger LOG = Logger.getLogger(FAMNotifier.class.getName());

    public FAMNotifier() {
        FAMLibrary library;
        try {
            // first try gamin
            library = (FAMLibrary) Native.loadLibrary("gamin-1", FAMLibrary.class); //NOI18N
        } catch (LinkageError x) {
            // then fam
            library = (FAMLibrary) Native.loadLibrary("fam", FAMLibrary.class); //NOI18N
        }
        this.lib = library;
        this.conn = new FAMLibrary.FAMConnection();
        if (lib.FAMOpen(conn) != 0) {
            throw new IllegalStateException();
        }
        eventReader = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopped) {
                    while (lib.FAMPending(conn) > 0) {
                        FAMLibrary.FAMEvent evt = new FAMLibrary.FAMEvent();
                        if (lib.FAMNextEvent(conn, evt) != -1) {
                            if (evt.code == FAMLibrary.FAMEndExist || evt.code == FAMLibrary.FAMAcknowledge) {
                                synchronized (activeRequests) {
                                    activeRequests.remove(evt.fr.reqnum);
                                    activeRequests.notifyAll();
                                }
                            }
                            String path = map.get(evt.fr.reqnum);
                            if (path != null) {
                                events.add(path);
                            }
                        }
                    }
                    // now sleep
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }, "FAM events reader"); //NOI18N
        eventReader.start();
    }

    @Override
    public Integer addWatch(String path) throws IOException {
        startRequest();
        FAMLibrary.FAMRequest request = new FAMLibrary.FAMRequest();
        lib.FAMMonitorDirectory(conn, path, request, null);
        activeRequests.add(request.reqnum);
        map.put(request.reqnum, path);
        return request.reqnum;
    }
    
    private void startRequest() {
        synchronized (activeRequests) {
            while (activeRequests.size() > ACTIVE_REQUESTS_LIMIT) {
                long start = System.currentTimeMillis();
                try {
                    activeRequests.wait();
                } catch (InterruptedException ex) {
                }
                LOG.warning("Blocking FAM requests for " + (System.currentTimeMillis()-start) + "ms, requests queue is full"); //NOI18N
            }
        }
    }
    
    @Override
    public String nextEvent() throws IOException, InterruptedException {
        return events.take();
    }

    @Override
    public void removeWatch(Integer key) throws IOException {
        startRequest();
        activeRequests.add(key);
        lib.FAMCancelMonitor(conn, new FAMLibrary.FAMRequest(key));
        map.remove(key);
    }

    @Override
    protected void stop() throws IOException {
        stopped = true;
        try {
            eventReader.join(2000);
        } catch (InterruptedException ex) {
        }
        lib.FAMClose(conn);
    }

    interface FAMLibrary extends Library {
        static class FAMConnection extends Structure {
            public static class Reference extends FAMConnection implements ByReference {}
            public int fd;
            public Pointer client;
        }

        static class FAMRequest extends Structure {

            public FAMRequest() {
                super();
            }

            public FAMRequest(int reqnum) {
                super();
                this.reqnum = reqnum;
            }

            public int reqnum;
        };

        public static final int PATH_MAX = 1024;

        public static final int FAMChanged = 1;
        public static final int FAMDeleted = 2;
        public static final int FAMStartExecuting = 3;
        public static final int FAMStopExecuting = 4;
        public static final int FAMCreated = 5;
        public static final int FAMMoved = 6;
        public static final int FAMAcknowledge = 7;
        public static final int FAMExists = 8;
        public static final int FAMEndExist = 9;

        static class FAMEvent extends Structure {
            public FAMConnection.Reference fc;         /* The fam connection that event occurred on */
            public FAMRequest fr;             /* Corresponds to the FamRequest from monitor */
            public String hostname;            /* host and filename - pointer to which */
            public byte[] filename = new byte[PATH_MAX];   /* file changed */
            public Pointer userdata;            /* userdata associated with this monitor req. */
            public int code;             /* What happened to file - see above */
        }


        int FAMOpen(FAMConnection fc);
        int FAMClose(FAMConnection fc);

        int FAMMonitorDirectory(FAMConnection fc,
                                String filename,
                                FAMRequest fr,
                                Pointer userData);
        int FAMCancelMonitor(FAMConnection fc,
				 FAMRequest fr);

        int FAMPending(FAMConnection fc);
        int FAMNextEvent(FAMConnection fc,
                         FAMEvent fe);
    }

}
