/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html4j;

import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import org.netbeans.api.html4j.HTMLComponent;
import org.netbeans.modules.openide.util.ProxyURLStreamHandlerFactory;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ComponentsTest {
    public ComponentsTest() {
    }
    
    @BeforeClass public void initNbResLoc() {
        URLStreamHandlerFactory f = Lookup.getDefault().lookup(URLStreamHandlerFactory.class);
        assertNotNull(f, "Factory found");
        URL.setURLStreamHandlerFactory(f);
    }

    @Test public void loadSwing() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        JFXPanel p = TestPages.getSwing(10, cdl);
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
        cdl.await();
    }

    @Test public void loadFX() throws Exception {
        final CountDownLatch cdl = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final JFXPanel p = new JFXPanel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView wv = TestPages.getFX(10, cdl);
                Scene s = new Scene(wv);
                p.setScene(s);
                done.countDown();
            }
        });
        done.await();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
        cdl.await();
    }

    @HTMLComponent(
        url = "simple.html", className = "TestPages",
        type = JFXPanel.class
    ) 
    static void getSwing(int param, CountDownLatch called) {
        assertEquals(param, 10, "Correct value passed in");
        called.countDown();
    }

    @HTMLComponent(
        url = "simple.html", className = "TestPages",
        type = WebView.class
    ) 
    static void getFX(int param, CountDownLatch called) {
        assertEquals(param, 10, "Correct value passed in");
        called.countDown();
    }

}
