/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.exec;

import java.util.concurrent.Future;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.util.ChangeSupport;

public final class KarmaServer {

    private final int port;
    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    private Future<Integer> server;

    volatile boolean starting = false;


    KarmaServer(int port, Project project) {
        assert project != null;
        this.port = port;
        this.project = project;
    }

    public synchronized void start() {
        assert Thread.holdsLock(this);
        if (isStarted()) {
            return;
        }
        starting = true;
        fireChange();
        KarmaExecutable karmaExecutable = KarmaExecutable.forProject(project, true);
        if (karmaExecutable == null) {
            // some error
            starting = false;
            fireChange();
            return;
        }
        server = karmaExecutable.start(port, new Runnable() {
            @Override
            public void run() {
                starting = false;
                fireChange();
            }
        });
    }

    public synchronized void runTests() {
        assert Thread.holdsLock(this);
        // XXX handle isStarting() == true
        if (!isStarted()) {
            start();
        }
        if (server == null) {
            // some error
            return;
        }
        KarmaExecutable karmaExecutable = KarmaExecutable.forProject(project, true);
        assert karmaExecutable != null;
        karmaExecutable.runTests(port);
    }

    public synchronized void stop() {
        assert Thread.holdsLock(this);
        if (server == null) {
            return;
        }
        if (server.isDone()
                || server.isCancelled()) {
            return;
        }
        server.cancel(true);
        server = null;
        fireChange();
    }

    public boolean isStarting() {
        return starting;
    }

    public synchronized boolean isStarted() {
        assert Thread.holdsLock(this);
        return server != null;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public int getPort() {
        return port;
    }

    public Project getProject() {
        return project;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @Override
    public String toString() {
        return "KarmaServer{" + "port=" + port + ", project=" + project.getProjectDirectory() + '}'; // NOI18N
    }

}
