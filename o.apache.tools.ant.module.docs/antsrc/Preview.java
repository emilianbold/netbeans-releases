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
