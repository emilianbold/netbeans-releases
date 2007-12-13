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

package org.netbeans.modules.reglib;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BrowserSupport class.
 *
 * The implementation of the com.sun.servicetag API needs to be
 * compiled with JDK 5 as well since the consumer of this API
 * may require to support JDK 5 (e.g. NetBeans). 
 *
 * The Desktop.browse() method can be backported in this class 
 * if needed.  The current implementation only supports JDK 6.
 */
public class BrowserSupport {
    private final static boolean isBrowseSupported;
    private final static Method browseMethod;
    private final static Object desktop;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.reglib.BrowserSupport"); // NOI18N
    

    static {
        boolean supported = false;
        Method browseM = null;
        Object desktopObj = null;
        try {
            // Determine if java.awt.Desktop is supported
            Class desktopCls = Class.forName("java.awt.Desktop", true, null);
            Method getDesktopM = desktopCls.getMethod("getDesktop"); 
            browseM = desktopCls.getMethod("browse", URI.class); 

            Class actionCls = Class.forName("java.awt.Desktop$Action", true, null);
            Method isDesktopSupportedMethod = desktopCls.getMethod("isDesktopSupported"); 
            Method isSupportedMethod = desktopCls.getMethod("isSupported", actionCls); 
            Field browseField = actionCls.getField("BROWSE");

            // support only if Desktop.isDesktopSupported() and 
            // Desktop.isSupported(Desktop.Action.BROWSE) return true.
            Boolean result = (Boolean) isDesktopSupportedMethod.invoke(null);
            if (result.booleanValue()) {
                desktopObj = getDesktopM.invoke(null);
                result = (Boolean) isSupportedMethod.invoke(desktopObj, browseField.get(null));
                supported = result.booleanValue();
            }
        } catch (ClassNotFoundException e) {
            LOG.log(Level.INFO,"Browser not supported:");
        } catch (NoSuchMethodException e) {
            LOG.log(Level.INFO,"Browser not supported:");
        } catch (NoSuchFieldException e) {
            LOG.log(Level.INFO,"Browser not supported:");
        } catch (IllegalAccessException e) {
            // should never reach here
            InternalError x = 
                new InternalError("Desktop.getDesktop() method not found");
            x.initCause(e);
                throw x;
        } catch (InvocationTargetException e) {
            LOG.log(Level.INFO,"Browser not supported:");
        }
        isBrowseSupported = supported;
        browseMethod = browseM;
        desktop = desktopObj;
    }

    public static boolean isSupported() {
        return isBrowseSupported; 
    }

    /**
     * Launches the default browser to display a {@code URI}.
     * If the default browser is not able to handle the specified
     * {@code URI}, the application registered for handling
     * {@code URIs} of the specified type is invoked. The application
     * is determined from the protocol and path of the {@code URI}, as
     * defined by the {@code URI} class.
     * <p>
     * This method calls the Desktop.getDesktop().browse() method.
     * <p>
     * @param uri the URI to be displayed in the user default browser
     *
     * @throws NullPointerException if {@code uri} is {@code null}
     * @throws UnsupportedOperationException if the current platform
     * does not support the {@link Desktop.Action#BROWSE} action
     * @throws IOException if the user default browser is not found,
     * or it fails to be launched, or the default handler application
     * failed to be launched
     * @throws IllegalArgumentException if the necessary permissions
     * are not available and the URI can not be converted to a {@code URL}
     */
    public static void browse(URI uri) throws IOException {
        if (uri == null) {
            throw new NullPointerException("null uri");
        }
        if (!isSupported()) {
            throw new UnsupportedOperationException("Browse operation is not supported");
        }

        // Call Desktop.browse() method
        try { 
            browseMethod.invoke(desktop, uri);
        } catch (IllegalAccessException e) {
            // should never reach here
            InternalError x = 
                new InternalError("Desktop.getDesktop() method not found");
            x.initCause(e);
                throw x;
        } catch (InvocationTargetException e) {
            Throwable x = e.getCause();
            if (x != null) {
                if (x instanceof UnsupportedOperationException) {
                    throw (UnsupportedOperationException) x;
                } else if (x instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) x;
                } else if (x instanceof IOException) {
                    throw (IOException) x;
                } else if (x instanceof SecurityException) {
                    throw (SecurityException) x;
                } else {
                    // ignore
                } 
            } 
        }
    }
}
