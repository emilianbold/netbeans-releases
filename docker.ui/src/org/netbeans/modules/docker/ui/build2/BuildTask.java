/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.build2;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.docker.api.BuildEvent;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.ui.output.StatusOutputListener;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class BuildTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BuildTask.class.getName());

    private final WeakReference<DockerInstance> instance;

    private final WeakReference<InputOutput> inputOutput;

    private final Hook hook;

    private final File buildContext;

    private final File dockerfile;

    private final String repository;

    private final String tag;

    private final boolean pull;

    private final boolean noCache;

    public BuildTask(DockerInstance instance, InputOutput inputOutput, Hook hook, File buildContext,
            File dockerfile, String repository, String tag, boolean pull, boolean noCache) {
        this.instance = new WeakReference<>(instance);
        this.inputOutput = new WeakReference<>(inputOutput);
        this.hook = hook;
        this.buildContext = buildContext;
        this.dockerfile = dockerfile;
        this.repository = repository;
        this.tag = tag;
        this.pull = pull;
        this.noCache = noCache;
    }

    public WeakReference<DockerInstance> getInstance() {
        return instance;
    }

    public File getBuildContext() {
        return buildContext;
    }

    public File getDockerfile() {
        return dockerfile;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public boolean isPull() {
        return pull;
    }

    public boolean isNoCache() {
        return noCache;
    }

    @NbBundle.Messages({
        "# {0} - context",
        "MSG_Building=Building {0}",
        "# {0} - file",
        "MSG_Uploading=Sending file {0}",
        "MSG_BuildCancelled=Build cancelled"
    })
    @Override
    public void run() {
        final InputOutput io = inputOutput.get();
        if (io == null) {
            return;
        }
        final DockerInstance inst = instance.get();
        if (inst == null) {
            return;
        }
        DockerAction facade = new DockerAction(inst);
        final FutureTask<DockerImage> task = facade.createBuildTask(buildContext, dockerfile, repository, tag, pull, noCache, new BuildEvent.Listener() {
            @Override
            public void onEvent(BuildEvent event) {
                if (event.isUpload()) {
                    io.getOut().println(Bundle.MSG_Uploading(event.getMessage()));
                } else if (event.isError()) {
                    // FIXME should we display more details ?
                    io.getErr().println(event.getMessage());
                } else {
                    io.getOut().println(event.getMessage());
                }
            }
        }, new StatusOutputListener(io));
        hook.onStart(task);
        ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.MSG_Building(buildContext), new Cancellable() {
            @Override
            public boolean cancel() {
                return task.cancel(true);
            }
        }, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                io.select();
            }
        });
        handle.start();
        try {
            io.getOut().reset();
            io.select();
            task.run();
            if (!task.isCancelled()) {
                task.get();
            } else {
                io.getErr().println(Bundle.MSG_BuildCancelled());
            }
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            LOGGER.log(Level.INFO, null, cause);
            io.getErr().println(cause.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, null, ex);
            Thread.currentThread().interrupt();
        } finally {
            io.getOut().close();
            handle.finish();
            hook.onFinish();
        }
    }

    public static interface Hook {

        void onStart(FutureTask<DockerImage> task);

        void onFinish();
    }
}
