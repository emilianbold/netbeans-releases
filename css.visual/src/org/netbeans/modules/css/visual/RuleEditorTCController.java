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
package org.netbeans.modules.css.visual;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.api.RuleEditorTC;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author mfukala@netbeans.org
 */
public class RuleEditorTCController implements PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(RuleEditorTCController.class);
    
    private static RuleEditorTCController STATIC_INSTANCE;

    //called from CssCaretAwareSourceTask constructor
    public static synchronized void init() {
        if (STATIC_INSTANCE == null) {
            STATIC_INSTANCE = new RuleEditorTCController();
        }
    }
    private TopComponent activeCssContentTC = null;

    public RuleEditorTCController() {
        //register a weak property change listener to the window manager registry
        //XXX is the weak listener really necessary? Is the registry ever GCed?
        Registry reg = WindowManager.getDefault().getRegistry();
        reg.addPropertyChangeListener(
                WeakListeners.propertyChange(this, reg));

        //called from CssCaretAwareSourceTask constructor when the caret is set to a css source code
        //for the first time, which means if we initialize the window listener now, we won't get the component
        //activated event since it happened just before the caret was set.
    
        //fire an artificial even so the rule editor possibly opens
        //the active TC should be the editor which triggered the css caret event
        propertyChange(new PropertyChangeEvent(this, TopComponent.Registry.PROP_ACTIVATED, null,
                TopComponent.getRegistry().getActivated()));
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {

            final TopComponent activated = (TopComponent) evt.getNewValue();

            if (!WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                return; //not editor TC, ignore
            }

            if (activated instanceof RuleEditorTC) {
                return; //ignore if its me
            }

            RP.post(new Runnable() {
                @Override
                public void run() {

                    //slow IO, do not run in EDT
                    final boolean cssContent = isCssContentTC(activated);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (cssContent) {
                                //editor with css file has been opened
                                openRuleEditor(activated);
                            } else {
                                //some foreign editor activated, close the rule editor
                                if (activeCssContentTC != null) {
                                    closeRuleEditor();
                                }
                            }
                        }
                    });

                }
            });

        } else if (activeCssContentTC != null && TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
            TopComponent closedTC = (TopComponent) evt.getNewValue();
            if (closedTC == activeCssContentTC) {
                closeRuleEditor();
            }
        }
    }

    private boolean isCssContentTC(TopComponent tc) {
        if (tc == null) {
            return false;
        }
        FileObject fob = tc.getLookup().lookup(FileObject.class);
        if (fob != null) {
            if ("text/css".equals(fob.getMIMEType())) { //NOI18N
                return true;
            }
        }
        return false;
    }

    private void openRuleEditor(TopComponent tc) {
        this.activeCssContentTC = tc;
        getRuleEditorTopComponent().open();
    }

    private void closeRuleEditor() {
        this.activeCssContentTC = null;
        getRuleEditorTopComponent().close();
    }

    private TopComponentGroup getRuleEditorTopComponent() {
        return WindowManager.getDefault().findTopComponentGroup("RuleEditor"); //NOI18N
    }
}
