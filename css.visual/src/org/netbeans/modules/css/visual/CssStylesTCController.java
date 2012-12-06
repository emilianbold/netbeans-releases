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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.api.CssStylesTC;
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
public class CssStylesTCController implements PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(CssStylesTCController.class);
    private static final Logger LOG = RuleEditorPanel.LOG;
    
    /**
     * Which mimetypes should cause the CssStyles window to be activated.
     */
    private static final Set<String> SUPPORTED_MIMES = new HashSet<String>(Arrays.asList("text/css", "text/html", "text/xhtml")); //NOI18N
    
    private static CssStylesTCController STATIC_INSTANCE;
    
    //called from CssCaretAwareSourceTask constructor
    static synchronized void init() {
        if (STATIC_INSTANCE == null) {
            STATIC_INSTANCE = new CssStylesTCController();
        }
    }
    
    private TopComponent activeCssContentTC = null;

    public CssStylesTCController() {
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

            if (activated instanceof CssStylesTC) {
                return; //ignore if its me
            }

            RP.post(new Runnable() {
                @Override
                public void run() {

                    //slow IO, do not run in EDT
                    final FileObject file = getFileObject(activated);
                    final boolean supported = file != null && isSupportedFileType(file);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            
                            if (supported) {
                                //editor with supported file has been opened
                                openCssStyles(activated, file);
                            } else {
                                //some foreign editor activated
                                //1. disable the content of the css styles window as the window group close (#2) 
                                //doesn't work if user opens the window manually
                                getCssStylesTC().setUnsupportedContext(file);
                                
                                //2. close the css styles window group
                                if (activeCssContentTC != null) {
                                    closeCssStyles();
                                }
                            }
                        }
                    });

                }
            });

        } else if (activeCssContentTC != null && TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
            TopComponent closedTC = (TopComponent) evt.getNewValue();
            if (closedTC == activeCssContentTC) {
                closeCssStyles();
            }
        }
    }
    
    private boolean isSupportedFileType(FileObject fob) {
        return SUPPORTED_MIMES.contains(fob.getMIMEType());
    }
    
    private FileObject getFileObject(TopComponent tc) {
        if (tc == null) {
            return null;
        }
        return tc.getLookup().lookup(FileObject.class);
    }

    private void openCssStyles(TopComponent tc, FileObject file) {
        this.activeCssContentTC = tc;
        getCssStylesTC().setContext(file);
        getCssStylesTCGroup().open();
    }

    private void closeCssStyles() {
        this.activeCssContentTC = null;
        getCssStylesTCGroup().close();
    }

    private CssStylesTC getCssStylesTC() {
        return (CssStylesTC)WindowManager.getDefault().findTopComponent("CssStylesTC");
    }
    
    private TopComponentGroup getCssStylesTCGroup() {
        return WindowManager.getDefault().findTopComponentGroup("CssStyles"); //NOI18N
    }
}
