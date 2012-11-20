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
package org.netbeans.modules.web.browser.api;

import org.openide.awt.HtmlBrowser;

/**
 * Single browser registered in the IDE.
 */
public final class WebBrowser {

    private WebBrowserFactoryDescriptor factoryDesc;

    WebBrowser(WebBrowserFactoryDescriptor factoryDesc) {
        this.factoryDesc = factoryDesc;
    }
    
    /**
     * Unique ID of browser. Useful for example to store per project reference to
     * user's browser choice.
     */
    public String getId() {
        return factoryDesc.getId();
    }

    /**
     * Name eg. FireFox, WebView, ...
     *
     * @return
     */
    public String getName() {
        return factoryDesc.getName();
    }
    
    public BrowserFamilyId getBrowserFamily() {
        return factoryDesc.getBrowserFamily();
    }

    /**
     * Is IDE embedded browser or external browser.
     */
    public boolean isEmbedded() {
        return getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW;
    }

    /**
     * This methods creates new browser "pane", that is tab in external browser
     * or TopComponent for embedded browser. Through this method clients have control 
     * how many browser panes are opened. In case of embedded browser it is 
     * straightforward - each call of this method will result into a new TopComponent. 
     * In case of external browser situation depends on availability of NetBeans 
     * browser plugins. If browser plugins are available then the same behaviour as 
     * in the case of embedded browser is possible and user can via this method 
     * create multiple tabs in external browser or keep single tab and open all 
     * URLs in the single tab.
     */
    public WebBrowserPane createNewBrowserPane() {
        return createNewBrowserPane(true, false);
    }
    
    /**
     * The only difference from createNewBrowserPane() is that automatic TopComponent
     * creation in case of embedded browser can be prevented by setting 
     * wrapEmbeddedBrowserInTopComponent to false. Doing that means that client
     * of WebBrowserPane must call WebBrowserPane.getComponent method and 
     * take care about showing browser component in IDE. This is useful for example
     * in case when HTML file editor has multiview and one of its tabs is "Preview"
     * showing rendered view of the HTML document.
     */
    public WebBrowserPane createNewBrowserPane(boolean wrapEmbeddedBrowserInTopComponent,
            boolean disableNetBeansIntegration) {
        return new WebBrowserPane( factoryDesc, wrapEmbeddedBrowserInTopComponent, disableNetBeansIntegration);
    }

    /**
     * Retrieve HTMLBrowser factory wrapped in this instance.
     * @return HtmlBrowser factory.
     */
    public HtmlBrowser.Factory getHtmlBrowserFactory() {
        return factoryDesc.getFactory();
    }
}
