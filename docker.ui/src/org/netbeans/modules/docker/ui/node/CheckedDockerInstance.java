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

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerInstance;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Hejl
 */
public class CheckedDockerInstance implements Refreshable {

    private static final RequestProcessor RP = new RequestProcessor(CheckedDockerInstance.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final AtomicBoolean available = new AtomicBoolean();

    private final InstanceListener listener = new InstanceListener();

    private final DockerInstance instance;

    public CheckedDockerInstance(DockerInstance instance) {
        this.instance = instance;
        instance.addConnectionListener(
                WeakListeners.create(DockerInstance.ConnectionListener.class, listener, instance));
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public DockerInstance getInstance() {
        return instance;
    }

    public boolean isAvailable() {
        return available.get();
    }

    @Override
    public void refresh() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                update(instance.isAvailable());
            }
        });
    }

    private void update(boolean newValue) {
        boolean oldValue = available.getAndSet(newValue);
        if (oldValue != newValue) {
            changeSupport.fireChange();
        }
    }

    private class InstanceListener implements DockerInstance.ConnectionListener {

        @Override
        public void onConnect() {
            update(true);
        }

        @Override
        public void onDisconnect() {
            update(false);
        }
    }
}
