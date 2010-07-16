/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.core;

import java.awt.EventQueue;
import java.awt.Frame;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.CLIHandler;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Shows the main window, so it is fronted when second instance of
 * NetBeans tries to start.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=CLIHandler.class)
public class CLIOptions2 extends CLIHandler implements Runnable {
    /** number of invocations */
    private int cnt;
    private static final Logger LOG = Logger.getLogger(CLIOptions2.class.getName());
    /** Time (in milliseconds) to wait for the event queue to become active. */
    private static final int EQ_TIMEOUT = 10 * 1000;
    private final RequestProcessor.Task task;
    static CLIOptions2 INSTANCE;

    /**
     * Create a default handler.
     */
    public CLIOptions2 () {
        super(WHEN_INIT);
        INSTANCE = this;
        task = RequestProcessor.getDefault().create(this);
    }

    protected int cli(Args arguments) {
        return cli(arguments.getArguments());
    }

    final int cli(String[] args) {
        if (cnt++ == 0) return 0;
        
        /*
        for (int i = 0; i < args.length; i++) {
            if ("--nofront".equals (args[i])) {
                return 0;
            }
        }
         */
        LOG.fine("CLI running");
        SwingUtilities.invokeLater(this);
        task.schedule(EQ_TIMEOUT);
        
        return 0;
    }
    
    public void run () {
        if (!EventQueue.isDispatchThread()) {
            eqStuck();
            return;
        }
        LOG.fine("running in EQ");
        task.cancel();

        Frame f = WindowManager.getDefault().getMainWindow();

        // makes sure the frame is visible
        f.setVisible(true);
        // uniconifies the frame if it is inconified
        if ((f.getExtendedState() & Frame.ICONIFIED) != 0) {
            f.setExtendedState(~Frame.ICONIFIED & f.getExtendedState());
        }
        // moves it to front and requests focus
        f.toFront ();
        
    }

    @SuppressWarnings("deprecation") // Thread.stop
    private void eqStuck() {
        Thread eq = TimableEventQueue.eq;
        if (eq == null) {
            LOG.warning("event queue thread not determined");
            return;
        }
        LOG.log(Level.FINE, "EQ stuck in {0}", eq);
        LOG.log(Level.WARNING, null, new EQStuck(eq));
        eq.stop();
    }
    private static class EQStuck extends Throwable {
        EQStuck(Thread eq) {
            super("GUI is not responsive"); // NOI18N
            StackTraceElement[] stack = Thread.getAllStackTraces().get(eq);
            if (stack != null) {
                setStackTrace(stack);
            } else {
                LOG.log(Level.WARNING, "no stack trace available for {0}", eq);
            }
        }
        public @Override synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
    
    protected void usage(PrintWriter w) {}
    
}
