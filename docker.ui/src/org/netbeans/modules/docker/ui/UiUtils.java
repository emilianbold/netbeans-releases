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
import java.awt.Font;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.netbeans.modules.docker.api.ActionStreamResult;

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

    public static void configureRowHeight(JTable table) {
        int height = table.getRowHeight();
        Font cellFont = UIManager.getFont("TextField.font");
        if (cellFont != null) {
            FontMetrics metrics = table.getFontMetrics(cellFont);
            if (metrics != null) {
                height = metrics.getHeight() + 2;
            }
        }
        table.setRowHeight(Math.max(table.getRowHeight(), height));
    }

    public static Collection<String> getAddresses(boolean includeIpv6, boolean includeDocker) {
        Set<InetAddress> addresses = new HashSet<>();
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                NetworkInterface iface = e.nextElement();
                if (includeDocker || !iface.getName().contains("docker")) { // NOI18N
                    for (Enumeration<InetAddress> ei = iface.getInetAddresses(); ei.hasMoreElements();) {
                        InetAddress addr = ei.nextElement();
                        if (!addr.isLinkLocalAddress() && (includeIpv6 || !(addr instanceof Inet6Address))) {
                            addresses.add(addr);
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        try {
            addresses.add(InetAddress.getLocalHost());
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        Set<String> ret = new TreeSet<>();
        for (InetAddress addr : addresses) {
            String host = addr.getHostAddress();
            if (addr instanceof Inet6Address) {
                int index = host.indexOf('%'); // NOI18N
                if (index > 0) {
                    host = host.substring(0, index);
                }
                // compress IPv6 address
                host = host.replaceFirst("(^|:)(0+(:|$)){2,8}", "::").replaceAll("(:|^)0+([0-9A-Fa-f])", "$1$2"); // NOI18N
            }
            ret.add(host);
        }
        return ret;
    }

    public static void loadRepositories(final DockerInstance instance, final JComboBox<String> combo) {
        assert SwingUtilities.isEventDispatchThread();

        if (!(combo.getEditor().getEditorComponent() instanceof JTextComponent)) {
            return;
        }

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                DockerAction facade = new DockerAction(instance);
                List<DockerImage> images = facade.getImages();
                final Set<String> repositories = new TreeSet<>();
                for (DockerImage image : images) {
                    for (DockerTag tag : image.getTags()) {
                        int index = tag.getTag().lastIndexOf(':'); // NOI18N
                        if (index > 0) {
                            repositories.add(tag.getTag().substring(0, index));
                        }
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // FIXME can we load items without clearing editor
                        if (UiUtils.getValue(combo) == null) {
                            int i = 0;
                            for (String repo : repositories) {
                                combo.insertItemAt(repo, i++);
                            }
                        }
                    }
                });
            }
        });

    }

    public static void openLog(DockerContainer container) throws DockerException {
        LogConnect logIO = getLogInputOutput(container);
        if (logIO.isConnected()) {
            logIO.getInputOutput().select();
            return;
        }

        DockerAction facade = new DockerAction(container.getInstance());
        DockerAction.LogResult result = facade.logs(container);
        try {
            logIO.getInputOutput().getOut().reset();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        logIO.connect(result);
        logIO.getInputOutput().select();
    }

    public static void openTerminal(DockerContainer container, ActionStreamResult r, boolean stdin, boolean logs) throws DockerException {
        Pair<InputOutput, Boolean> termIO = getTerminalInputOutput(container);
        InputOutput io = termIO.first();
        if (IOTerm.isSupported(io)) {
            if (termIO.second()) {
                focusTerminal(io);
            } else {
                DockerAction facade = new DockerAction(container.getInstance());
                ActionStreamResult result = r != null ? r : facade.attach(container, stdin, logs);

                try {
                    io.getOut().reset();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
                if (!result.hasTty() && IOEmulation.isSupported(io)) {
                    IOEmulation.setDisciplined(io);
                }

                TerminalResizeListener l = null;
                if (result.hasTty() && IOResizable.isSupported(io)) {
                    l = new TerminalResizeListener(io, container);
                    IONotifier.addPropertyChangeListener(io, l);
                }

                IOTerm.connect(io, stdin ? result.getStdIn() : null,
                        new TerminalInputStream(io, result.getStdOut(), result, l), result.getStdErr(), "UTF-8");
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
                        Bundle.LBL_LogInputOutput(container.getShortId()), true);
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
                        .getIO(container.getShortId(), new Action[] {new TerminalOptionsAction()});
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

        private final InputOutput io;

        private final DockerContainer container;

        private final RequestProcessor.Task task;

        // GuardedBy("this")
        private Dimension value;

        private boolean initial = true;

        public TerminalResizeListener(InputOutput io, DockerContainer container) {
            this.io = io;
            this.container = container;
            this.task = RP.create(new Runnable() {
                @Override
                public void run() {
                    Dimension newValue;
                    synchronized (TerminalResizeListener.this) {
                        newValue = value;
                    }
                    DockerAction remote = new DockerAction(TerminalResizeListener.this.container.getInstance());
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
            if (IONotifier.isSupported(io)) {
                IONotifier.removePropertyChangeListener(io, this);
            }
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
                    if (c != null) {
                        c.close();
                    }
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

        public synchronized void connect(DockerAction.LogResult result) {
            task = new LogOutputTask(io, result).start();
        }

        public synchronized boolean isConnected() {
            return task != null && !task.isDone();
        }
    }
}
