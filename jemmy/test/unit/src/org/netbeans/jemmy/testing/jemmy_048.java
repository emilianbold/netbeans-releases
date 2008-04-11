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
 * Contributor(s): Jiri Skrivanek.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 */
package org.netbeans.jemmy.testing;

import java.util.*;
import javax.swing.*;
import org.netbeans.jemmy.operators.*;

/** Test whether Component.toString() is called in dispatch thread. It
 * is used in jemmy log messages.
 */
public class jemmy_048 extends JemmyTest {
    public int runIt(Object obj) {
        try {
            JFrame frame = new JFrame();
            frame.setTitle("Test Frame");
            frame.pack();
            frame.show();
            final JFrameOperator frameOper = new JFrameOperator("Test Frame");
            new Thread(new Runnable() {
                public void run() {
                    JLabelOperator labelOper = new JLabelOperator(frameOper);
                }
            }).start();
            MyLabel myLabel = new MyLabel();
            myLabel.setText("AAAAAAAAAAAAAA");
            frame.getContentPane().add(myLabel);
            frame.pack();
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
        return(0);
    }
    
    static class MyLabel extends JLabel {
        
        String dummy;
        
        public MyLabel() {
            super();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dummy = "even more dummy";
                }
            });
            //System.out.println("Constructor finished...");
        }
        
        public String toString() {
            return dummy.toString();
        }
    }
}
