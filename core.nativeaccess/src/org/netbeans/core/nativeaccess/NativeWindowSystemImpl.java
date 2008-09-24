/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core.nativeaccess;

import java.awt.Shape;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;
import org.netbeans.core.nativeaccess.transparency.WindowUtils;
import org.openide.util.Utilities;


/**
 * Implementation of NativeWindowSystem based on JNA library.
 * 
 * @author S. Aubrecht
 */
public class NativeWindowSystemImpl extends NativeWindowSystem {

    private static final Logger LOG = Logger.getLogger(NativeWindowSystemImpl.class.getName());

    public boolean isWindowAlphaSupported() {
        boolean res = false;
        try {
            String osArch = System.getProperty("os.arch");
            // jna currently doesn't work on 64 bit java on Win, this check should
            // be deleted when https://jna.dev.java.net/issues/show_bug.cgi?id=59 is implemented
            boolean isWin64 = Utilities.isWindows() && osArch != null && osArch.indexOf("amd64") != -1;
            res = WindowUtils.isWindowAlphaSupported() && !isWin64;
        } catch( ThreadDeath td ) {
            throw td;
        } catch (UnsatisfiedLinkError e) {
            // E.g. "Unable to load library 'X11': libX11.so: cannot open shared object file: No such file or directory"
            // on headless build machine (missing libx11-dev.deb)
            LOG.log(Level.FINE, null, e);
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
        return res;
    }

    public void setWindowAlpha(Window w, float alpha) {
        try {
            WindowUtils.setWindowAlpha(w, alpha);
        } catch( ThreadDeath td ) {
            throw td;
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
    }

    public void setWindowMask(Window w, Shape mask) {
        try {
            WindowUtils.setWindowMask(w, mask);
        } catch( ThreadDeath td ) {
            throw td;
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
    }

    public void setWindowMask(Window w, Icon mask) {
        try {
            WindowUtils.setWindowMask(w, mask);
        } catch( ThreadDeath td ) {
            throw td;
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
    }
}
