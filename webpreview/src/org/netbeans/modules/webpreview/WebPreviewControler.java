/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.webpreview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.text.Document;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.modules.webpreview.api.WebPreviewable;

import org.openide.util.Lookup;

/**
 *
 * @author Milan Kubec
 */
public class WebPreviewControler implements PropertyChangeListener {

    public static Logger LOGGER = Logger.getLogger("WebPreview");

    private WebPreviewable preview;

    // User changed selected component in editor
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
            Document doc = EditorRegistry.focusedComponent().getDocument();
            String mimeType = (String) doc.getProperty("mimeType"); // NOI18N
            if (mimeType != null) {
                LOGGER.fine("Activated editor component with MIME type: " + mimeType);
                Lookup mimeLookup = MimeLookup.getLookup(mimeType);
                WebPreviewable previewable = mimeLookup.lookup(WebPreviewable.class);
                if (previewable != null) {
                    preview = previewable;
                    LOGGER.fine("Component contains WebPreviewable in MIME Lookup: " + previewable);
                    WebBrowser browser = WebPreviewTopComponent.findInstance().getWebBrowser();
                    final WebPreviewable.Impl previewImpl = Accessor.getDefault().getImpl(previewable);
                    previewImpl.removePropertyChangeListener(previewEnabledListener);
                    previewImpl.addPropertyChangeListener(previewEnabledListener);
                    if (previewImpl.isPreviewEnabled()) {
                        previewImpl.onAttach(browser);
                        WebPreviewTopComponent.findInstance().open();
                        WebPreviewTopComponent.findInstance().requestVisible();
                    } else { // preview is disabled
                        previewImpl.onDettach(browser);
                        WebPreviewTopComponent.findInstance().close();
                    }
                } else { // no previewable in MimeLookup => closing the preview TC
                    if (WebPreviewTopComponent.findInstance().isOpened()) {
                        // XXX tady by melo byt onDettach pro vsechny previewables
                        WebPreviewTopComponent.findInstance().close();
                    }
                }
            }
        }
    }

    // User enabled/disabled web preview for given document in editor tab
    private PropertyChangeListener previewEnabledListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (WebPreviewable.PROP_PREVIEW_ENABLED.equals(evt.getPropertyName())) {
                WebPreviewable.Impl previewImpl = Accessor.getDefault().getImpl(preview);
                if (previewImpl.isPreviewEnabled()) {
                    previewImpl.onAttach(WebPreviewTopComponent.findInstance().getWebBrowser());
                    WebPreviewTopComponent.findInstance().open();
                    WebPreviewTopComponent.findInstance().requestVisible();
                } else {
                    previewImpl.onDettach(WebPreviewTopComponent.findInstance().getWebBrowser());
                    WebPreviewTopComponent.findInstance().close();
                }
            }
        }
    };

}
