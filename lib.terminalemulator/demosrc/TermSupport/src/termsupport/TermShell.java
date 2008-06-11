/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package termsupport;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermListener;
import pty.Pty.Mode;
import pty.PtyProcess;
import pty.OS;
import pty.JNAPty;

/**
 * Run a shell under a Term.
 * <br>
 * On unix ...
 * <br>
 * The shell defined by <code>$SHELL</code> is run.
 * If <code>$SHELL</code> is empty <code>/bin/bash</code> is run.
 * <br>
 * On windows ...
 * <br>
 * <code>cmd.exe</code> is run.
 * @author ivan
 */
public class TermShell {
    private final static OS os = OS.get();

    private final StreamTerm term;

    // defaults setup for unix
    private Mode mode = Mode.REGULAR;
    private Boolean lineDiscipline = null;
    private boolean debug = false;

    private PtyProcess ptyProcess;
    private Runnable reaper;

    private static void error(String fmt, Object...args) {
        String msg = String.format(fmt, args);
        throw new IllegalStateException(msg);
    }

    public TermShell(StreamTerm term) {
        this.term = term;

        switch (os) {
            case WINDOWS:
                mode= Mode.NONE;
        }
    }

    /**
     * Set the Pty mode.
     * Should be called before run().
     */
    public void setMode(Mode mode) {
        if (ptyProcess != null)
            error("Already running");
        this.mode = mode;
    }

    /**
     * Set whether Terms line discipline should be used.
     * Should be called before run().
     */
    public void setLineDiscipline(Boolean lineDiscipline) {
        if (ptyProcess != null)
            error("Already running");
        this.lineDiscipline = lineDiscipline;
    }

    /**
     * Allows control of Term debugging
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Run the shell.
     */
    public void run() {
        if (ptyProcess != null)
            error("Already running");

        //
        // Check and adjust arguments
        //
        switch (os) {
            case WINDOWS:
                if (mode != Mode.NONE)
                    error("Can only use 'pipe' mode on windows");
                break;
            case UNIX:
                break;
        }

        //
        // Create pty
        //
        JNAPty pty = null;
        switch (mode) {
            case NONE:
                break;
            case REGULAR:
                pty = new JNAPty(JNAPty.Mode.REGULAR);
                try {
                    pty.setup();
                } catch (Exception x) {
                    System.out.printf("Exception %s\n",x);
                }
                break;
            case RAW:
                error("raw pty mode not supported yet");
                break;
            case PACKET:
                error("packet pty mode not supported yet");
                break;
        }

        //
        // Create Term
        //
        if (debug)
            term.setDebugFlags(Term.DEBUG_OUTPUT | Term.DEBUG_KEYS);

        if (pty != null) {
            final JNAPty fpty = pty;
            term.addListener(new TermListener() {
                public void sizeChanged(Dimension cells, Dimension pixels) {
                    /* LATER
                    if (pty.isRaw())
                        return;     // otherwise SWINSZ will give us an IOException
                    */
                    fpty.masterTIOCSWINSZ(cells.height, cells.width,
                                          pixels.height, pixels.width);
                }
            });
        }
        
        // 
        // Push own line discipline if needed or overriden
        //
        if (lineDiscipline != null) {
            if (lineDiscipline)
                term.pushStream(new LineDiscipline());
        } else {
            switch (mode) {
                case NONE:
                case RAW:
                    term.pushStream(new LineDiscipline());
                    break;
            }
        }
        
        //
        // Build up a command to run under the terminal
        // 
        String shell = System.getenv("SHELL");

        List<String> cmd = new ArrayList<String>();

        switch (os) {
            case WINDOWS:
                cmd.add("cmd.exe");
                cmd.add("/q");  // turn echo off
                cmd.add("/a");  // use ANSI
                break;

            case UNIX:
//		cmd.add("/usr/bin/strace");
//		cmd.add("-o");
//		cmd.add("/tmp/term-cmd.tr");
                if (shell != null)
                    cmd.add(shell);
                else
                    cmd.add("/bin/bash");
                break;
            default:
                error("Unsupported os '%s'", os);
                break;
	}

        //
        // Start cmd and connect it to term
        //
	ptyProcess = new PtyProcess(cmd, pty);
        Map<String, String> env = ptyProcess.environment();
        env.put("TERM", term.getEmulation());
	final Process process = ptyProcess.start();

        if (pty == null) {
            term.connect(process.getOutputStream(), process.getInputStream(), null);
        } else {
            term.connect(pty.getOutputStream(), pty.getInputStream(), null);
        }

        reaper = ptyProcess.getReaper();
    }

    /**
     * Wait for the shell to exit.
     */
    public void waitFor() {
        if (reaper == null)
            error("Not running yet");
        reaper.run();   // blocks
    }

    /**
     * Hangup the connection to the shell.
     *
     * On unix ...
     * java.lang.Process.terminate() sends a SIGTERM to the process. 
     * While that usually works for regular processes, shells tend to
     * ignore SIGTERM and instead are sensitive to SIGHUP.
     */
    public void hangup() {
        if (ptyProcess == null)
            error("Not running yet");
        ptyProcess.hangup();
    }
}
