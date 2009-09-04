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

package org.netbeans.modules.webpreview.api;

import java.beans.PropertyChangeListener;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.modules.webpreview.Accessor;

/**
 * Editor windows must have an instance of this class in their Lookup to indicate
 * that they support web preview.
 * When such an editor is activated then the framework will open web preview window
 * and attach its embedded browser. It's up to the editor to interact with the browser
 * (set content, reload on save etc).
 * When a different window/editor is activated the embedded browser is detached.
 *
 * @author S. Aubrecht
 */
public final class WebPreviewable {

    final Impl impl;

    static {
        Accessor.setDefault(new AccessorImpl());
    }

    /**
     * Name of boolean property which is fired when web preview for associated
     * editor window is enabled/disabled (user toggles appropriate 'Preview' button
     * in editor toolbar).
     */
    public static final String PROP_PREVIEW_ENABLED = "previewEnabled"; //NOI18N

    private WebPreviewable( Impl impl ) {
        this.impl = impl;
    }

    public static WebPreviewable create( Impl previewableImpl ) {
        return new WebPreviewable(previewableImpl);
    }

    public static interface Impl {

        /**
         * Return true if web preview is enabled, otherwise false
         * @return True if web preview is enabled.
         */
        boolean isPreviewEnabled();

        /**
         * Invoked by the framework when the associated editor window is activated
         * or when user clicks the Preview button. Implementing classes should keep
         * reference to this browser and show appropriate content in it.
         * @param browser Embedded browser component.
         */
        void onAttach( WebBrowser browser );

        /**
         * Invoked by the framework the associated editor window is no longer showing
         * or when another previewable editor has been activated. Implementing class
         * should detach any listeners from the browser.
         * @param browser Embedded browser component.
         */
        void onDettach( WebBrowser browser );

        /**
         * Invoked by the framework when user closes the Preview window. The associated
         * editor should deselect the Preview button in its toolbar and detach any
         * listeners from the browser.
         * @param browser Embedded browser component.
         */
        void onClose( WebBrowser browser );

        /**
         * Add a listener to watch for changes in PROP_PREVIEW_ENABLED
         * @param l
         */
        void addPropertyChangeListener( PropertyChangeListener l );

        /**
         * Remove listener for watching changes in PROP_PREVIEW_ENABLED
         * @param l
         */
        void removePropertyChangeListener( PropertyChangeListener l );

    }
    
}
