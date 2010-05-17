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

import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.awt.Shape;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;
import org.netbeans.core.nativeaccess.transparency.WindowUtils;


/**
 * Implementation of NativeWindowSystem based on JNA library.
 * 
 * @author S. Aubrecht
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.windows.nativeaccess.NativeWindowSystem.class)
public class NativeWindowSystemImpl extends NativeWindowSystem {

    private static final Logger LOG = Logger.getLogger(NativeWindowSystemImpl.class.getName());

    public NativeWindowSystemImpl() {
        extractNativeLibrary();
    }

    public boolean isWindowAlphaSupported() {
        if( !is32Bit() )
            return false;
        boolean res = false;
        try {
            res = WindowUtils.isWindowAlphaSupported();
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

    private static boolean is32Bit() {
        String osarch = System.getProperty("os.arch"); //NOI18N
        for (String x : new String[]{"x86", "i386", "i486", "i586", "i686"}) { //NOI18N
            if (x.equals(osarch)) {
                return true;
            }
        }
        return false;
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

    /**
     * Extract the native library from jna.jar to &lt;userdir&gt;/var/cache/jna
     * so that it's reusable on next startup.
     */
    private void extractNativeLibrary() {
        String userDir = System.getProperty("netbeans.user"); //NOI18N
        if( null == userDir ) {
            return;
        }
        File jnaDir = new File(new File(new File(userDir, "var"), "cache"), "jna"); //NOI18N
        if( !jnaDir.exists() && !jnaDir.mkdirs() ) {
            return;
        }
        if( !jnaDir.canWrite() ) {
            return;
        }

        //copied and adapted from com.sun.jna.Native
        String libname = System.mapLibraryName("jnidispatch"); //NOI18N
        String arch = System.getProperty("os.arch"); //NOI18N
        String name = System.getProperty("os.name"); //NOI18N

        File jnaLib = new File(jnaDir, libname);
        if( jnaLib.exists() && jnaLib.canRead() ) {
            System.setProperty("jna.boot.library.path", jnaDir.getAbsolutePath());
            return;
        }

        String resourceName = getNativeLibraryResourcePath(arch, name) + "/" + libname;
        URL url = Native.class.getResource(resourceName);

        // Add an ugly hack for OpenJDK (soylatte) - JNI libs use the usual .dylib extension
        if( url == null && Platform.isMac() && resourceName.endsWith(".dylib") ) { //NOI18N
            resourceName = resourceName.substring(0, resourceName.lastIndexOf(".dylib")) + ".jnilib"; //NOI18N
            url = Native.class.getResource(resourceName);
        }
        if( url == null ) {
            return;
        }

        if( !url.getProtocol().toLowerCase().equals("file") ) { //NOI18N
            InputStream is = Native.class.getResourceAsStream(resourceName);
            if( is == null ) {
                return;
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(jnaLib);
                int count;
                byte[] buf = new byte[1024];
                while( (count = is.read(buf, 0, buf.length)) > 0 ) {
                    fos.write(buf, 0, count);
                }
            } catch( IOException e ) {
                //ignore
            } finally {
                try {
                    is.close();
                } catch( IOException e ) {
                }
                if( fos != null ) {
                    try {
                        fos.close();
                    } catch( IOException e ) {
                    }
                }
            }
        }
        if( jnaLib.exists() && jnaLib.canRead() ) {
            System.setProperty("jna.boot.library.path", jnaDir.getAbsolutePath()); //NOI18N
        }
    }

    /**
     * copied and adapted from com.sun.jna.Native
     * @param arch
     * @param name
     * @return
     */
    static String getNativeLibraryResourcePath(String arch, String name) {
        String osPrefix;
        arch = arch.toLowerCase();
        if( Platform.isWindows() ) {
            if( "i386".equals(arch) ) { //NOI18N
                arch = "x86"; //NOI18N
            }
            osPrefix = "win32-" + arch; //NOI18N
        } else if( Platform.isMac() ) {
            osPrefix = "darwin"; //NOI18N
        } else if( Platform.isLinux() ) {
            if( "x86".equals(arch) ) { //NOI18N
                arch = "i386"; //NOI18N
            } else if( "x86_64".equals(arch) ) { //NOI18N
                arch = "amd64"; //NOI18N
            }
            osPrefix = "linux-" + arch; //NOI18N
        } else if( Platform.isSolaris() ) {
            osPrefix = "sunos-" + arch; //NOI18N
        } else {
            osPrefix = name.toLowerCase();
            if( "x86".equals(arch) ) { //NOI18N
                arch = "i386"; //NOI18N
            }
            if( "x86_64".equals(arch) ) { //NOI18N
                arch = "amd64"; //NOI18N
            }
            if( "powerpc".equals(arch) ) { //NOI18N
                arch = "ppc"; //NOI18N
            }
            int space = osPrefix.indexOf(" "); //NOI18N
            if( space != -1 ) {
                osPrefix = osPrefix.substring(0, space);
            }
            osPrefix += "-" + arch; //NOI18N
        }
        return "/com/sun/jna/" + osPrefix; //NOI18N
    }
}
