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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uihandler.interactive;

import org.netbeans.modules.uihandler.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.uihandler.api.Controller;
import org.openide.awt.HtmlBrowser;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Jaroslav Tulach
 */
public class AutosubmitTest extends NbTestCase {
    private ModuleInstall o;
    
    static {
    }
    
    public AutosubmitTest(String testName) {
        super(testName);
        
        System.setProperty("netbeans.full.hack", "true");
        MemoryURL.registerURL("memory://nic", "");
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    /*
    @Override
    protected int timeOut() {
        return 10000;
    }
    */
    
    
    protected void setUp() throws Exception {
        clearWorkDir();
        // tells the status icon to behave normaly
        System.getProperties().remove("netbeans.full.hack");
        System.setProperty("netbeans.user", getWorkDirPath());
        
        Locale.setDefault(new Locale("te", "ST1"));
        Class<? extends ModuleInstall> c = Class.forName("org.netbeans.modules.uihandler.Installer").asSubclass(ModuleInstall.class);
        o = SharedClassObject.findObject(c, true);
        assertNotNull("Installer created", o);
        
        MockServices.setServices(URLD.class);
    }

    protected void tearDown() throws Exception {
    }
    
    public void testCheckTheSubmitCanBeMadeAutomatic() throws Exception {
        o.restored();
        
        NbPreferences.forModule(Installer.class).putBoolean("autoSubmitWhenFull", true);
        
        Component icon = new SubmitStatus().getStatusLineElement();
        icon.setSize(10, 20); // dummy call, invokes refresh
        assertEquals("Unvisible now as nothing has been uploaded to server yet", 0, icon.getSize().width);
        
        String reply = "" +
            "<html>" + 
            "<form action='memory://uploaddone' method='post'>" +
            "  <input type='hidden' name='submit' value='&amp;Submit Data'></input>" +
            "</form>" +
            "</html>";
        
        MemoryURL.registerURL("memory://welcome", reply);

        String upload = "" +
            "<html>" +
            "  <meta http-equiv='Refresh' content='3; URL=http://www.xelfi.cz'>" +
            "</html>";
        
        MemoryURL.registerURL("memory://uploaddone", upload);
        
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        for (int i = 0; i < 1500; i++) {
            log.warning("" + i);
        }

        assertEquals("Full buffer", 1000, Controller.getDefault().getLogRecordsCount());
        for (;;) {
            Installer.RP.post(new Runnable() {
                public void run() {
                }
            }, 0, Thread.MIN_PRIORITY).waitFinished();
            if (Controller.getDefault().getLogRecordsCount() < 1000) {
                
                break;
            }
        }

        {
            final byte[] content = MemoryURL.getOutputForURL("memory://uploaddone");
            assertNotNull(content);
        }
        
        assertEquals("Mode is automatic", true, Installer.isHintsMode());
        assertEquals("Now the hint points to xelfi", new URL("http://www.xelfi.cz").toURI(), Installer.hintsURL().toURI());

        icon.setSize(10, 20); // dummy call, invokes refresh
        assertEquals("Visible now", new Dimension(16, 16), icon.getSize());
        JComponent jc = (JComponent)icon;
        if (jc.getToolTipText().indexOf("hint") < 0) {
            fail("There should be a note about hint:\n" + jc.getToolTipText());
        }
        MockServices.setServices(URLD.class);
        icon.dispatchEvent(new MouseEvent(icon, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 8, 1, false));
        
        assertEquals("Clicking on the component opens browser", URLD.url.toURI(), Installer.hintsURL().toURI());
    }
    
    public static final class URLD extends HtmlBrowser.URLDisplayer {
        static URL url;
        
        public void showURL(URL u) {
            assertNull("No previous", url);
            url = u;
        }
        
    }
}

