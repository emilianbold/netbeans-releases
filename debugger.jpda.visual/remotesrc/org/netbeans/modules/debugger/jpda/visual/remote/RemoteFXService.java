/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.lang.reflect.Field;

/**
 *
 * @author Jaroslav Bachorik
 */
public class RemoteFXService {
//    final private static Logger LOGGER = Logger.getAnonymousLogger();
    
    static {
        try {
            Class.forName("javafx.scene.image.Image", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.AudioClip", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.MediaManager", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.MediaPlayer", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.events.PlayerStateEvent$PlayerState", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
    }
    
    final private static Thread accessThread = new Thread(new Runnable() {
        public void run() {
            while (!Thread.interrupted()) {
                access();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }, "FX Access Thread (Visual Debugger)"); // NOI18N
    
    public static void startAccessLoop() {
//        preloadFxClasses();
        setDebugMode();
        accessThread.setDaemon(true);
        accessThread.setPriority(Thread.MIN_PRIORITY);
        accessThread.start();
    }

    /**
     * JavaFX runtime is boobietrapped with various checks for {@linkplain com.sun.javafx.runtime.SystemProperties#isDebug() }
     * which lead to spurious NPEs. Need to make it happy and force the runtime into debug mode
     */
    private static void setDebugMode() {
        try {
            Class spClz = Class.forName("com.sun.javafx.runtime.SystemProperties", true, RemoteFXService.class.getClassLoader());
            Field dbgFld = spClz.getDeclaredField("isDebug");
            dbgFld.setAccessible(true);
            dbgFld.set(null, Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
//    private static void preloadFxClasses() {
//        LOGGER.info("initializing classes");
//        try {
//            Class.forName("javafx.scene.image.Image", true, Thread.currentThread().getContextClassLoader());
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, null, e);
//        }
//    }
    
    private static void access() {};
}
