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
package org.netbeans.bluej.ui.window;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.bluej.api.BluejOpenCloseCallback;
import org.openide.windows.WindowManager;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.bluej.api.BluejOpenCloseCallback.class)
public class OpenCloseImpl implements BluejOpenCloseCallback {

    /** Creates a new instance of OpenCloseImpl */
    public OpenCloseImpl() {
    }

    public void projectOpened(final org.netbeans.api.project.Project project) {
        // very ugly, needs to be like this because the component listens on 
        // opened project changes and needs to be opened to do so..
        // a better solution would be to start listening on opened project changes right at the start of IDE
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if (WindowManager.getDefault().getMainWindow().isVisible()) {
                        BluejViewTopComponent tc = BluejViewTopComponent.findInstance();
                        if (!tc.isOpened()) {
                            //TODO
                            tc.open();
                        }
                    }
                }
            });
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void projectClosed(final org.netbeans.api.project.Project project) {
        //TODO possibly close when empty??
    }
    
}
