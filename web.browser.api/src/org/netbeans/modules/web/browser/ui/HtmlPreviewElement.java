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
package org.netbeans.modules.web.browser.ui;

import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * A quick prototype of a Preview tab in HTML document Multiview window.
 *
 * Note: It is currently disabled. Uncomment the class annotations below to enable again.
 *
 * @author S. Aubrecht
 */
//@MultiViewElement.Registration(
//        displayName = "#CTL_PreviewTabCaption", // NOI18N
//// no icon
//persistenceType = TopComponent.PERSISTENCE_NEVER,
//preferredID = "HtmlPreviewTab", // NOI18N
//mimeType = "text/html", // NOI18N
//position = 9501)
public class HtmlPreviewElement implements MultiViewElement {

    private final JPanel panel = new JPanel(new BorderLayout());
    private final DeveloperToolbar toolbar = DeveloperToolbar.create();
    private HtmlBrowser.Impl browser = null;

    private URL url;

    public HtmlPreviewElement( Lookup lkp ) {
        final FileObject fileObject = lkp.lookup(FileObject.class);
        if (fileObject != null) {
            url = fileObject.toURL();
        }
    }

    @Override
    public JComponent getVisualRepresentation() {
        return panel;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return ( JComponent ) toolbar.getComponent();
    }

    @Override
    public void setMultiViewCallback( MultiViewElementCallback callback ) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public Action[] getActions() {
        return null;
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public void componentOpened() {
        if( null != browser )
            return;

        WebBrowser web = WebBrowsers.getInstance().getPreferred();
        if( null != web && !web.isEmbedded() ) {
            for( WebBrowser wb : WebBrowsers.getInstance().getAll(true) ) {
                if( wb.isEmbedded() ) {
                    web = wb;
                    break;
                }
            }
        }
        panel.removeAll();
        if( null == web || !web.isEmbedded() ) {
            panel.add( new JLabel("No embedded browser available"), BorderLayout.CENTER );
        } else {
            browser = web.getHtmlBrowserFactory().createHtmlBrowserImpl();
            toolbar.intialize( browser.getLookup() );
            if( null != url )
                browser.setURL( url );
            panel.add( browser.getComponent(), BorderLayout.CENTER );
        }
    }

    @Override
    public void componentClosed() {
        panel.removeAll();
        browser = null;
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
        if( null != browser ) {
            browser.reloadDocument();
        }
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

}
