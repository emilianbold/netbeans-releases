/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.remote;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.DockerInstance;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerEventBus implements DockerEvent.Listener, DockerRemote.ConnectionListener {

    private static final Logger LOGGER = Logger.getLogger(DockerEventBus.class.getName());

    private static final long INTERVAL = 5000;

    private final RequestProcessor processor = new RequestProcessor(DockerEventBus.class);

    private final DockerInstance instance;

    private final List<DockerEvent.Listener> listeners = new ArrayList<>();

    private HttpURLConnection connection;

    private DockerEvent lastEvent;

    private boolean stop;

    private boolean blocked;

    public DockerEventBus(DockerInstance instance) {
        this.instance = instance;
    }

    public void start() {
        synchronized (this) {
            stop = false;
            blocked = false;
            lastEvent = null;
            processor.post(new Runnable() {
                @Override
                public void run() {
                    DockerRemote remote = new DockerRemote(instance);
                    for (;;) {
                        try {
                            synchronized (DockerEventBus.this) {
                                if (stop) {
                                    return;
                                }
                            }
                            remote.events(lastEvent != null ? lastEvent.getTime() : null,
                                    DockerEventBus.this, DockerEventBus.this);
                            Thread.sleep(INTERVAL);
                        } catch (DockerException ex) {
                            synchronized (DockerEventBus.this) {
                                if (stop) {
                                    return;
                                }
                            }
                            LOGGER.log(Level.INFO, null, ex);
                        } catch (InterruptedException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            Thread.currentThread().interrupt();
                            return;
                        }
                        blocked = true;
                    }
                }
            });
        }
    }

    public void stop() {
        HttpURLConnection current;
        synchronized (this) {
            stop = true;
            current = connection;
            connection = null;
        }
        //current.disconnect();
    }

    public void addListener(DockerEvent.Listener listener) {
        synchronized (this) {
            if (listeners.isEmpty()) {
                start();
            }
            listeners.add(listener);
        }
    }

    public void removeListener(DockerEvent.Listener listener) {
        synchronized (this) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                stop();
            }
        }
    }

    @Override
    public void onEvent(DockerEvent event) {
        synchronized (this) {
            if (blocked) {
                if (event.equals(lastEvent)) {
                    blocked = false;
                }
                return;
            }
            lastEvent = event;
        }
        LOGGER.log(Level.INFO, event.toString());
    }

    @Override
    public void onConnect(HttpURLConnection connection) {
        synchronized (this) {
            this.connection = connection;
        }
    }
}
