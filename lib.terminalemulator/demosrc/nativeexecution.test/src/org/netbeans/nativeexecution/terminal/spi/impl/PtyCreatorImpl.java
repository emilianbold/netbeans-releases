/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.nativeexecution.terminal.spi.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.spi.pty.PtyAllocator;
import org.netbeans.modules.nativeexecution.spi.pty.PtyImpl;
import org.netbeans.nativeexecution.terminal.spi.impl.PtySatellite.PtySatelliteInfo;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ak119685
 */
@ServiceProvider(service = PtyAllocator.class)
public class PtyCreatorImpl implements PtyAllocator {

    public PtyImplementation allocate(final ExecutionEnvironment env) throws IOException {
        PtyImplementation result = null;
        OutputStream output = null;
        InputStream input = null;

        final String satellitePath = PtySatellite.getInstance().getPath(env);

        try {
            if (env.isLocal()) {
                ProcessBuilder pb = new ProcessBuilder(satellitePath);
                Process satellite = pb.start();
                output = satellite.getOutputStream();
                input = satellite.getInputStream();
            } else {
                ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();
                Session session = access.getConnectionSession(ConnectionManager.getInstance(), env, false);
                ChannelExec echannel = null;

                if (session != null) {
                    synchronized (session) {
                        echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                        echannel.setCommand(satellitePath);
                        echannel.connect();
                        output = echannel.getOutputStream();
                        input = echannel.getInputStream();
                    }
                }
            }

            PtySatelliteInfo satelliteInfo = PtySatellite.getInstance().readSatelliteOutput(input);
            result = new PtyImplementation(env, satelliteInfo.tty, satelliteInfo.pid, input, output);
        } catch (Exception ex) {
            if (input != null) {
                input.close();
            }

            if (output != null) {
                output.close();
            }

            throw new IOException(ex);
        }

        return result;
    }

    public boolean isApplicable(ExecutionEnvironment env) {
        return true;
    }

    public final static class PtyImplementation implements PtyImpl {

        private final String tty;
        private final int pid;
        private final InputStream istream;
        private final OutputStream ostream;
        private final ExecutionEnvironment env;
        private final boolean pxlsAware;

        PtyImplementation(ExecutionEnvironment env, String tty, int pid, InputStream istream, OutputStream ostream) throws IOException {
            this.tty = tty;
            this.pid = pid;
            this.istream = istream;
            this.ostream = ostream;
            this.env = env;

            try {
                if (OSFamily.SUNOS.equals(HostInfoUtils.getHostInfo(env).getOSFamily())) {
                    pxlsAware = true;
                } else {
                    pxlsAware = false;
                }
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public final void close() throws IOException {
            ostream.close();
            istream.close();
        }

        @Override
        public String toString() {
            return tty + " (" + pid + ")";
        }

        @Override
        public InputStream getInputStream() {
            return istream;
        }

        @Override
        public OutputStream getOutputStream() {
            return ostream;
        }

        InputStream getErrorStream() {
            return null;
        }

        public void slaveTIOCSWINSZ(int rows, int cols, int height, int width) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void masterTIOCSWINSZ(int cols, int rows, int xpixels, int ypixels) {
            String cmd = pxlsAware
                    ? String.format("cols %d rows %d xpixels %d ypixels %d", cols, rows, xpixels, ypixels)
                    : String.format("cols %d rows %d", cols, rows); // NOI18N
            try {
                SttySupport.getFor(env).apply(this, cmd);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getSlaveName() {
            return tty;
        }
    }
}
