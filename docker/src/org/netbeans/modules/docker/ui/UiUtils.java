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
package org.netbeans.modules.docker.ui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class UiUtils {

    private static final Logger LOGGER = Logger.getLogger(UiUtils.class.getName());

    private static final RequestProcessor RP = new RequestProcessor("Docker Remote Action", 5);

    private static final int RESIZE_DELAY = 500;

    private UiUtils() {
        super();
    }

    public static void performRemoteAction(final String displayName, final Callable<Void> action, final Runnable eventFinish) {
        final ProgressHandle handle = ProgressHandle.createHandle(displayName);
        handle.start();
        Runnable wrapped = new Runnable() {
            @Override
            public void run() {
                try {
                    action.call();
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // FIXME dialog ?
                            LOGGER.log(Level.WARNING, null, ex);
                            StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                        }
                    });
                } finally {
                    handle.finish();
                    if (eventFinish != null) {
                        SwingUtilities.invokeLater(eventFinish);
                    }
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(wrapped);
        } else {
            wrapped.run();
        }
    }

    @NbBundle.Messages("MSG_NoTerminalSupport=No terminal support installed")
    public static void openTerminal(final DockerContainer container, DockerRemote.AttachResult result) {
        IOProvider provider = IOProvider.get("Terminal"); // NOI18N
        InputOutput io = provider.getIO(DockerUtils.getShortId(container.getId()), true);
        if (IOTerm.isSupported(io)) {
            IOTerm.connect(io, result.getStdIn(),
                    new TerminalInputStream(io, result.getStdOut(), result), result.getStdErr());
            if (IOResizable.isSupported(io)) {
                IONotifier.addPropertyChangeListener(io, new TerminalResizeListener(container));
            }
            io.select();
            if (IOTerm.isSupported(io)) {
                final Term term = IOTerm.term(io);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        term.requestFocusInWindow();
                    }
                });
            }
        } else {
            StatusDisplayer.getDefault().setStatusText(Bundle.MSG_NoTerminalSupport());
        }
    }

    private static class TerminalResizeListener implements PropertyChangeListener {

        private final DockerContainer container;

        private final RequestProcessor.Task task;

        // GuardedBy("this")
        private Dimension value;

        private boolean initial = true;

        public TerminalResizeListener(DockerContainer container) {
            this.container = container;
            this.task = RequestProcessor.getDefault().create(new Runnable() {
                @Override
                public void run() {
                    Dimension newValue;
                    synchronized (TerminalResizeListener.this) {
                        newValue = value;
                    }
                    DockerRemote remote = new DockerRemote(TerminalResizeListener.this.container.getInstance());
                    try {
                        remote.resizeTerminal(TerminalResizeListener.this.container, newValue.height, newValue.width);
                    } catch (DockerException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }
                }
            }, true);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (IOResizable.PROP_SIZE.equals(evt.getPropertyName())) {
                IOResizable.Size newVal = (IOResizable.Size) evt.getNewValue();
                synchronized (this) {
                    value = newVal.cells;
                }
                if (initial) {
                    initial = false;
                    task.schedule(0);
                } else {
                    task.schedule(RESIZE_DELAY);
                }
            }
        }
    }

    private static class TerminalInputStream extends FilterInputStream {

        private final InputOutput io;

        private final Closeable close;

        public TerminalInputStream(InputOutput io, InputStream in, Closeable close) {
            super(in);
            this.io = io;
            this.close = close;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                int i = super.read(b, off, len);
                if (i < 0) {
                    closeTerminal();
                }
                return i;
            } catch (IOException ex) {
                closeTerminal();
                throw ex;
            }
        }

        @Override
        public int read() throws IOException {
            try {
                int i = super.read();
                if (i < 0) {
                    closeTerminal();
                }
                return i;
            } catch (IOException ex) {
                closeTerminal();
                throw ex;
            }
        }

        private void closeTerminal() {
            IOTerm.disconnect(io, null);
            try {
                close.close();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }
}
