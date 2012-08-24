/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.inspect.ui.DomTC;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * Manager of the {@code TopComponentGroup} for web-page inspection.
 *
 * @author Jan Stola
 */
public class PageInspectionTCGroupManager implements PropertyChangeListener {
    /** The only instance of this class. */
    private static PageInspectionTCGroupManager INSTANCE = new PageInspectionTCGroupManager();
    /** Determines whether DOM Tree view should be shown with the page inspection group. */
    private static boolean showDomTree = Boolean.getBoolean("org.netbeans.modules.web.inspect.showDomTree"); // NOI18N
    
    /**
     * Creates a new {@code PageInspectionTCGroupManager}.
     */
    private PageInspectionTCGroupManager() {
    }

    /**
     * Returns the only instance of this class.
     * 
     * @return the only instance of this class.
     */
    public static PageInspectionTCGroupManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Opens/closes the page inspection window group.
     * 
     * @param visible determines whether the group should be closed or opened.
     */
    void setPageInspectionGroupVisible(final boolean visible) {
        if (EventQueue.isDispatchThread()) {
            setPageInspectionGroupVisibleAWT(visible);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setPageInspectionGroupVisibleAWT(visible);
                }
            });
        }
    }

    /**
     * Opens/closes the page inspection window group. This method should
     * be called from event-dispatch thread only.
     * 
     * @param visible determines whether the group should be closed or opened.
     */
    private void setPageInspectionGroupVisibleAWT(boolean visible) {
        WindowManager manager = WindowManager.getDefault();
        TopComponentGroup group = manager.findTopComponentGroup("webinspect"); // NOI18N
        if (group == null) {
            Logger.getLogger(Installer.class.getName()).log(
                    Level.INFO, "TopComponentGroup webinspect not found!"); // NOI18N
        } else if (visible) {
            group.open();
            if (showDomTree) {
                TopComponent tc = manager.findTopComponent(DomTC.ID);
                if (tc == null) {
                    Logger.getLogger(Installer.class.getName()).log(
                            Level.INFO, "TopComponent {0} not found!", DomTC.ID); // NOI18N
                } else {
                    tc.open();
                    tc.requestVisible();
                }
            }
        } else {
            group.close();
            TopComponent tc = manager.findTopComponent(DomTC.ID);
            if (tc != null) {
                tc.close();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
            PageModel pageModel = PageInspectorImpl.getDefault().getPage();
            setPageInspectionGroupVisible(pageModel != null);
        }
    }

}
