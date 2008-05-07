/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.hibernate.hqleditor;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.hibernate.hqleditor.ui.HQLEditorTopComponent;
import org.netbeans.modules.hibernate.hqleditor.ui.error.ErrorMultiViewDescription;
import org.netbeans.modules.hibernate.hqleditor.ui.result.ResultMultiViewDescription;
import org.netbeans.modules.hibernate.hqleditor.ui.sql.SQLMultiViewDescription;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vadiraj
 */
public class HQLEditorController {

    private static HQLEditorController instance;

    public static synchronized HQLEditorController getDefault() {
        if (instance == null) {
            instance = new HQLEditorController();
        }
        return instance;
    }

    public void init() {
        HQLEditorTopComponent editorTopComponent = HQLEditorTopComponent.findInstance();
        editorTopComponent.open();
        editorTopComponent.requestActive();

        editorTopComponent.fillHibernateConfigurations();
        
        MultiViewDescription[] mvDescriptions = new MultiViewDescription[]{
            new ResultMultiViewDescription(),
            new SQLMultiViewDescription(),
            new ErrorMultiViewDescription()
        };
        TopComponent mvTopComponent = MultiViewFactory.createMultiView(
                mvDescriptions, mvDescriptions[0]);

        mvTopComponent.setDisplayName("HQLEditor-Output");

        Mode m = WindowManager.getDefault().findMode("output"); //NOI18N

        if (m != null) {
            m.dockInto(mvTopComponent);
        }
        mvTopComponent.open();
        
    }
}
