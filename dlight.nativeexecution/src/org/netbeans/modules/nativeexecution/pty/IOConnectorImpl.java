/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.nativeexecution.pty;

import java.awt.Dimension;
import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport.Pty;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.nativeexecution.pty.PtyCreatorImpl.PtyImplementation;
import org.netbeans.modules.nativeexecution.spi.pty.IOConnector;
import org.netbeans.modules.nativeexecution.spi.pty.PtyImpl;
import org.netbeans.modules.nativeexecution.spi.support.pty.PtyImplAccessor;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
@ServiceProvider(service = IOConnector.class)
public class IOConnectorImpl implements IOConnector {

    private static final RequestProcessor rp = new RequestProcessor("IOConnectorImpl", 2); // NOI18N

    public IOConnectorImpl() {
    }

    @Override
    public boolean connect(final InputOutput io, final NativeProcess process) {
        if (!IOTerm.isSupported(io)) {
            return false;
        }

        final Pty pty = PtySupport.getPty(process);
        final PtyImpl ptyImpl = PtyImplAccessor.getDefault().getImpl(pty);

        if ((ptyImpl == null) && IOEmulation.isSupported(io)) {
            IOEmulation.setDisciplined(io);
        }

        if (ptyImpl == null || !(ptyImpl instanceof PtyImplementation)) {
            IOTerm.connect(io, process.getOutputStream(), process.getInputStream(), process.getErrorStream());
        } else {
            PtyImplementation impl = (PtyImplementation) ptyImpl;
            IOTerm.connect(io, impl.getOutputStream(), impl.getInputStream(), process.getErrorStream());

            if (IOResizable.isSupported(io)) {
                IOResizable.addListener(io, new ResizeListener(impl));
            }

            NativeTaskExecutorService.submit(new Reaper(io, process, impl),
                    "IOConnectorImpl reaper for " + pty.getSlaveName()); // NOI18N
        }

        return true;
    }

    @Override
    public boolean connect(final InputOutput io, final Pty pty) {
        if (pty == null || io == null) {
            throw new NullPointerException();
        }

        if (!IOTerm.isSupported(io)) {
            return false;
        }

        final PtyImpl ptyImpl = PtyImplAccessor.getDefault().getImpl(pty);

        if (!(ptyImpl instanceof PtyImplementation)) {
            return false;
        }

        PtyImplementation impl = (PtyImplementation) ptyImpl;
        IOTerm.connect(io, impl.getOutputStream(), impl.getInputStream(), impl.getErrorStream());

        if (IOResizable.isSupported(io)) {
            IOResizable.addListener(io, new ResizeListener(impl));
        }

        return true;
    }

    private static class ResizeListener implements IOResizable.Listener {

        private Task task = null;
        private Dimension cells;
        private Dimension pixels;

        public ResizeListener(final PtyImplementation pty) {
            this.task = rp.create(new Runnable() {

                @Override
                public void run() {
                    Dimension c, p;

                    synchronized (ResizeListener.this) {
                        c = new Dimension(cells);
                        p = new Dimension(pixels);
                    }

                    pty.masterTIOCSWINSZ(c.width, c.height,
                            p.width, p.height);
                }
            }, true);
        }

        @Override
        public synchronized void sizeChanged(Dimension cells, Dimension pixels) {
            if (cells == null || pixels == null) {
                throw new NullPointerException();
            }

            if (cells.equals(this.cells) && pixels.equals(this.pixels)) {
                return;
            }

            this.cells = new Dimension(cells);
            this.pixels = new Dimension(pixels);
            task.schedule(1000);
        }
    }

    private final static class Reaper implements Runnable {

        private final NativeProcess process;
        private final PtyImplementation pty;
        private final InputOutput io;

        public Reaper(final InputOutput io, final NativeProcess process, final PtyImplementation pty) {
            this.process = process;
            this.pty = pty;
            this.io = io;
        }

        @Override
        public void run() {
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
            }

            try {
                pty.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    io.closeInputOutput();
//                }
//            });
        }
    }
}
