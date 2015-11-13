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
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.netbeans.modules.docker.remote.StreamResult;
import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class UiUtils {

    private static final Logger LOGGER = Logger.getLogger(UiUtils.class.getName());

    private static final int RESIZE_DELAY = 500;

    private static final Map<DockerContainer, LogConnect> LOGS = new WeakHashMap<>();

    private static final Map<DockerContainer, InputOutput> TERMS = new WeakHashMap<>();

    private UiUtils() {
        super();
    }

    public static String getValue(JComboBox<String> combo) {
        if (combo.isEditable()) {
            return getValue((String) combo.getEditor().getItem());
        }
        return getValue((String) combo.getSelectedItem());
    }

    public static String getValue(JTextComponent c) {
        return getValue(c.getText());
    }

    public static String getValue(String str) {
        String value = str;
        if (value != null) {
            value = value.trim();
            if (value.isEmpty()) {
                return null;
            }
        }
        return value;
    }

    public static void openLog(DockerContainer container) throws DockerException {
        LogConnect logIO = getLogInputOutput(container);
        if (logIO.isConnected()) {
            logIO.getInputOutput().select();
            return;
        }

        DockerRemote facade = new DockerRemote(container.getInstance());
        DockerRemote.LogResult result = facade.logs(container);
        try {
            logIO.getInputOutput().getOut().reset();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        logIO.connect(result);
        logIO.getInputOutput().select();
    }

    public static void openTerminal(DockerContainer container, StreamResult r, boolean stdin, boolean logs) throws DockerException {
        Pair<InputOutput, Boolean> termIO = getTerminalInputOutput(container);
        InputOutput io = termIO.first();
        if (IOTerm.isSupported(io)) {
            if (termIO.second()) {
                focusTerminal(io);
            } else {
                DockerRemote facade = new DockerRemote(container.getInstance());
                StreamResult result = r != null ? r : facade.attach(container, stdin, logs);

                try {
                    io.getOut().reset();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
                if (!result.hasTty() && IOEmulation.isSupported(io)) {
                    IOEmulation.setDisciplined(io);
                }
                IOTerm.connect(io, stdin ? result.getStdIn() : null,
                        new TerminalInputStream(io, result.getStdOut(), result), result.getStdErr(), "UTF-8");
                if (result.hasTty() && IOResizable.isSupported(io)) {
                    IONotifier.addPropertyChangeListener(io, new TerminalResizeListener(container));
                }
                focusTerminal(io);
            }
        } else {
            io.select();
        }
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "LBL_LogInputOutput=Log {0}"
    })
    private static LogConnect getLogInputOutput(DockerContainer container) {
        synchronized (LOGS) {
            LogConnect connect = LOGS.get(container);
            if (connect == null) {
                InputOutput io = IOProvider.getDefault().getIO(
                        Bundle.LBL_LogInputOutput(DockerUtils.getShortId(container)), true);
                connect = new LogConnect(io);
                LOGS.put(container, connect);
            }
            return connect;
        }
    }

    private static Pair<InputOutput, Boolean> getTerminalInputOutput(DockerContainer container) {
        synchronized (TERMS) {
            InputOutput io = TERMS.get(container);
            if (io == null) {
                io = IOProvider.get("Terminal") // NOI18N
                        .getIO(DockerUtils.getShortId(container), new Action[] {new TerminalOptionsAction()});
                TERMS.put(container, io);
                return Pair.of(io, false);
            }
            return Pair.of(io, IOConnect.isSupported(io) && IOConnect.isConnected(io));
        }
    }

    private static void focusTerminal(InputOutput io) {
        io.select();
        if (IOTerm.isSupported(io)) {
            // XXX is there a better way ?
            final Term term = IOTerm.term(io);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    term.requestFocusInWindow();
                }
            });
        }
    }

    private static class TerminalResizeListener implements PropertyChangeListener, Closeable {

        private static final RequestProcessor RP = new RequestProcessor(TerminalResizeListener.class);

        private final DockerContainer container;

        private final RequestProcessor.Task task;

        // GuardedBy("this")
        private Dimension value;

        private boolean initial = true;

        public TerminalResizeListener(DockerContainer container) {
            this.container = container;
            this.task = RP.create(new Runnable() {
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
                        LOGGER.log(Level.INFO, null, ex);
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

        @Override
        public void close() throws IOException {
            task.cancel();
        }
    }

    private static class TerminalInputStream extends FilterInputStream {

        private final InputOutput io;

        private final Closeable[] close;

        public TerminalInputStream(InputOutput io, InputStream in, Closeable... close) {
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
            for (Closeable c : close) {
                try {
                    c.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
            // disconnect all is needed as we call getOut().reset()
            // because of that getOut()
            if (IOConnect.isSupported(io)) {
                IOConnect.disconnectAll(io, null);
            }
            //IOTerm.disconnect(io, null);
            //LOGGER.log(Level.INFO, "Closing terminal", new Exception());
        }
    }

    private static class LogConnect {

        private final InputOutput io;

        private Future task;

        public LogConnect(InputOutput io) {
            this.io = io;
        }

        public InputOutput getInputOutput() {
            return io;
        }

        public synchronized void connect(DockerRemote.LogResult result) {
            task = new LogOutputTask(io, result).start();
        }

        public synchronized boolean isConnected() {
            return task != null && !task.isDone();
        }
    }
}
