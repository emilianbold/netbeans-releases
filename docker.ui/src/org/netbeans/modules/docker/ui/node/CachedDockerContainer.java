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
package org.netbeans.modules.docker.ui.node;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.netbeans.modules.docker.api.DockerEvent;
import org.netbeans.modules.docker.api.DockerException;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class CachedDockerContainer implements Refreshable {

    private static final Logger LOGGER = Logger.getLogger(CachedDockerContainer.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(CachedDockerContainer.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final DockerContainer container;

    private DockerContainerDetail detail;

    public CachedDockerContainer(DockerContainer container) {
        this.container = container;
        this.detail = new DockerContainerDetail(container.getName(), container.getStatus(), false, false);
        container.getInstance().addContainerListener(new DockerEvent.Listener() {
            @Override
            public void onEvent(DockerEvent event) {
                if (event.getId().equals(CachedDockerContainer.this.container.getId())) {
                    DockerContainer.Status fresh = getStatus(event);
                    if (fresh != null) {
                        update(fresh);
                    } else {
                        refresh();
                    }
                }
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public DockerContainer getContainer() {
        return container;
    }

    public DockerContainerDetail getDetail() {
        synchronized (this) {
            return detail;
        }
    }

    @Override
    public void refresh() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                DockerAction action = new DockerAction(container.getInstance());
                try {
                    update(action.getDetail(container));
                } catch (DockerException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        });
    }

    private void update(DockerContainer.Status status) {
        synchronized (this) {
            detail = new DockerContainerDetail(detail.getName(), status, detail.isStdin(), detail.isTty());
        }
        changeSupport.fireChange();
    }

    private void update(DockerContainerDetail value) {
        synchronized (this) {
            detail = value;
        }
        changeSupport.fireChange();
    }

    private static DockerContainer.Status getStatus(DockerEvent event) {
        DockerEvent.Status status = event.getStatus();
        switch (status) {
            case DIE:
                return DockerContainer.Status.STOPPED;
            case START:
                return DockerContainer.Status.RUNNING;
            case PAUSE:
                return DockerContainer.Status.PAUSED;
            case UNPAUSE:
                return DockerContainer.Status.RUNNING;
            default:
                return null;
        }
    }
}
