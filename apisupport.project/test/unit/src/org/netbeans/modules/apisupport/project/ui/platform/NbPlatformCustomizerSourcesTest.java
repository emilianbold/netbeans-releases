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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;

/**
 * @author Martin Krauskopf
 */
public class NbPlatformCustomizerSourcesTest extends TestBase {

    private JFrame frame;
    private NbPlatformCustomizerSources sourcesPane;
    private JPanel outerPane;
    private WeakReference sourcesPaneWR;
    
    public NbPlatformCustomizerSourcesTest(String testName) {
        super(testName);
    }
    
    public void testMemoryLeak_70032() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                sourcesPane = new NbPlatformCustomizerSources();
                sourcesPaneWR = new WeakReference(sourcesPane);
                sourcesPane.setPlatform(NbPlatform.getDefaultPlatform());
                
                // workaround for inability to GC JFrame/JDialog itself
                outerPane = new JPanel();
                outerPane.add(sourcesPane);
                
                frame = new JFrame("testMemoryLeak_70032");
                frame.getContentPane().setLayout(new BorderLayout());
                frame.getContentPane().add(outerPane, BorderLayout.CENTER);
                frame.pack();
                // NOTE: we have to show the frame to take combobox renderers into the game
                frame.setVisible(true);
                frame.getContentPane().remove(outerPane);
                
                // prevents KeyboardFocusManager.permanentFocusOwner to hold us
                JLabel dummyFocusEater = new JLabel();
                frame.getContentPane().add(dummyFocusEater);
                dummyFocusEater.requestFocus();
                frame.setVisible(false);
                frame.dispose();
            }
        });
        outerPane = null;
        sourcesPane = null;
        
        assertGC("GCing sourcesPane", sourcesPaneWR);
    }
    
}
