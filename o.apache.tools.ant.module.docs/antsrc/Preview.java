/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFrame;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

// XXX does not work inside NB, why?

/**
 * Previews the manual.
 * @author Jesse Glick
 */
public class Preview extends Task {

    private File custom;
    public void setCustom(File f) {
        custom = f;
    }

    public void execute() throws BuildException {
        assert custom != null;
        try {
            final URL url = custom.toURI().toURL();
            final HelpSet hs = new HelpSet(Preview.class.getClassLoader(), url);
            final JHelp jh = new JHelp(hs);
            final JFrame[] f = new JFrame[1];
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        f[0] = new JFrame(hs.getTitle());
                        f[0].addWindowListener(new WindowAdapter() {
                            public void windowClosing(WindowEvent e) {
                                f[0].dispose();
                                synchronized (f) {
                                    f.notify();
                                }
                            }
                        });
                        f[0].add(jh);
                        f[0].pack();
                        f[0].setExtendedState(JFrame.MAXIMIZED_BOTH);
                        f[0].setVisible(true);
                    } catch (Exception x) {
                        throw new RuntimeException(x);
                    }
                }
            });
            synchronized (f) {
                f.wait();
            }
        } catch (Exception x) {
            throw new BuildException(x);
        }
    }

}