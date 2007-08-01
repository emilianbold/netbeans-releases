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

package org.netbeans.modules.applemenu;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.lang.reflect.*;
import org.openide.modules.ModuleInstall;

/** Module installer that installs an com.apple.eawt.ApplicationListener on
 * the com.apple.eawt.Application object for this session, which will interpret
 * apple events and call the appropriate action from the actions pool.
 *
 * @author  Tim Boudreau
 */
public class Install extends ModuleInstall {
    private CtrlClickHack listener;

    public void restored () {
        listener = new CtrlClickHack();
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK);
        if (System.getProperty("mrj.version") != null) { // NOI18N
//            FontReferenceQueue.install();
            try {
                Class adapter = Class.forName("org.netbeans.modules.applemenu.NbApplicationAdapter");
                Method m = adapter.getDeclaredMethod("install", new Class[0] );
                m.invoke(adapter, new Object[0]);
            } catch (NoClassDefFoundError e) {
            } catch (ClassNotFoundException e) {
            } catch (Exception e) {
            }
            String pn = "apple.laf.useScreenMenuBar"; // NOI18N
            if (System.getProperty(pn) == null) {
                System.setProperty(pn, "true"); // NOI18N
            }
        }
    }
    
    public void uninstalled () {
         if (listener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            listener = null;
         }
        if (System.getProperty("mrj.version") != null) { // NOI18N

            try {
                Class adapter = Class.forName("org.netbeans.modules.applemenu.NbApplicationAdapter");
                Method m = adapter.getDeclaredMethod("uninstall", new Class[0] );
                m.invoke(adapter, new Object[0]);
            } catch (NoClassDefFoundError e) {
            } catch (ClassNotFoundException e) {
            } catch (Exception e) {
            }
        }
    }
}
