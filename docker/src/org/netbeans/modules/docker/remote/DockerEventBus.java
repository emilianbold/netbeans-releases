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

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.DockerInstance;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerEventBus implements Closeable, DockerEvent.Listener, DockerRemote.ConnectionListener {

    private static final Logger LOGGER = Logger.getLogger(DockerEventBus.class.getName());

    private static final long INTERVAL = 5000;

    private static final Set<String> IMAGE_EVENTS = new HashSet<>();

    static {
        Collections.addAll(IMAGE_EVENTS, "delete", "import", "pull", "push", "tag", "untag"); // NOI18N
    }

    private final RequestProcessor processor = new RequestProcessor(DockerEventBus.class);

    private final DockerInstance instance;

    private final List<DockerEvent.Listener> imageListeners = new ArrayList<>();

    private final List<DockerEvent.Listener> containerListeners = new ArrayList<>();

    private HttpURLConnection connection;

    private DockerEvent lastEvent;

    private boolean stop;

    private boolean blocked;

    public DockerEventBus(DockerInstance instance) {
        this.instance = instance;
    }

    public void addImageListener(DockerEvent.Listener listener) {
        synchronized (this) {
            if (imageListeners.isEmpty() && containerListeners.isEmpty()) {
                start();
            }
            imageListeners.add(listener);
        }
    }

    public void removeImageListener(DockerEvent.Listener listener) {
        synchronized (this) {
            imageListeners.remove(listener);
            if (imageListeners.isEmpty() && containerListeners.isEmpty()) {
                stop();
            }
        }
    }

    public void addContainerListener(DockerEvent.Listener listener) {
        synchronized (this) {
            if (imageListeners.isEmpty() && containerListeners.isEmpty()) {
                start();
            }
            containerListeners.add(listener);
        }
    }

    public void removeContainerListener(DockerEvent.Listener listener) {
        synchronized (this) {
            containerListeners.remove(listener);
            if (imageListeners.isEmpty() && containerListeners.isEmpty()) {
                stop();
            }
        }
    }

    @Override
    public void onEvent(DockerEvent event) {
        List<DockerEvent.Listener> toFire;
        synchronized (this) {
            if (blocked) {
                if (event.equals(lastEvent)) {
                    blocked = false;
                    return;
                } else if (lastEvent == null || lastEvent.getTime() < event.getTime()) {
                    blocked = false;
                } else {
                    return;
                }
            }
            lastEvent = event;
            if (IMAGE_EVENTS.contains(event.getStatus())) {
                toFire = new ArrayList<>(imageListeners);
            } else {
                toFire = new ArrayList<>(containerListeners);
            }
        }
        LOGGER.log(Level.INFO, event.toString());
        for (DockerEvent.Listener l : toFire) {
            l.onEvent(event);
        }
    }

    @Override
    public void onConnect(HttpURLConnection connection) {
        synchronized (this) {
            this.connection = connection;
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void start() {
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
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            synchronized (DockerEventBus.this) {
                                if (stop) {
                                    return;
                                }
                            }
                            remote.events(lastEvent != null ? lastEvent.getTime() : null,
                                    DockerEventBus.this, DockerEventBus.this);
                        } catch (DockerException ex) {
                            synchronized (DockerEventBus.this) {
                                if (stop) {
                                    return;
                                }
                            }
                            LOGGER.log(Level.INFO, null, ex);
                        }

                        blocked = true;
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            });
        }
    }

    private void stop() {
        HttpURLConnection current;
        synchronized (this) {
            stop = true;
            current = connection;
            connection = null;
        }
        //current.disconnect();
    }
}
