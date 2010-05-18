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
/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/
//package org.apache.batik.swing.svg;
package org.netbeans.modules.visualweb.designer.cssengine;

import org.netbeans.modules.visualweb.spi.designer.cssengine.CssUserAgentInfo;
import java.awt.geom.Dimension2D;
import java.net.URL;

import org.apache.batik.css.engine.CSSEngineUserAgent;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;


/**
 * Represents the user agent, for computing things like dpi etc.
 * Some of this code was copied or derived from Batik's JSVGComponent class,
 * so I've left the Batik copyright on the file.
 *
 * XXX This should represent the designer, so the impl should delegate there,
 * probably via the <code>CssUserAgentInfo</code>.
 *
 * @author Tor Norbye
 * @author Peter Zavadsky (the CssUserAgentInfo stuff)
 */
public class UserAgent implements CSSEngineUserAgent {

    private final CssUserAgentInfo userAgentInfo;


    public UserAgent(CssUserAgentInfo userAgentInfo) {
        this.userAgentInfo = userAgentInfo;
    }


    /**
     * Returns the default size of the viewport of this user agent (0, 0).
     */
    public Dimension2D getViewportSize() {
        throw new RuntimeException("Not yet implemented");

        //return getSize();
    }

    /**
     * Displays an error message in the User Agent interface.
     */
    public void displayError(String message) {
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, message);
    }

    /**
     * Displays an error resulting from the specified Exception.
     */
    public void displayError(Exception ex) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
    }

    /**
     * Displays a message in the User Agent interface.
     */
    public void displayMessage(String message) {
        ErrorManager.getDefault().log(ErrorManager.USER, message);
    }

    /**
     * Shows an alert dialog box.
     */
    public void showAlert(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message,
                NotifyDescriptor.WARNING_MESSAGE));
    }

    /**
    * Shows a prompt dialog box.
    */
    public String showPrompt(String message) {
        // XXX TODO Not Yet Implemented!
        Thread.dumpStack();

        return "";
    }

    /**
     * Shows a prompt dialog box.
     */
    public String showPrompt(String message, String defaultValue) {
        // XXX TODO Not Yet Implemented!
        Thread.dumpStack();

        return defaultValue;
    }

    /**
     * Shows a confirm dialog box.
     */
    public boolean showConfirm(String message) {
        // XXX TODO Not Yet Implemented!
        Thread.dumpStack();

        return false;
    }

    /**
     * Returns the size of a px CSS unit in millimeters.
     */
    public float getPixelUnitToMillimeter() {
        return 0.264583333333333333333f; // 96 dpi
    }

    /**
     * Returns the size of a px CSS unit in millimeters.
     * This will be removed after next release.
     * @see #getPixelUnitToMillimeter()
     */
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }

    /**
     * Returns the default font family.
     */
    public String getDefaultFontFamily() {
        return "Arial, Helvetica, sans-serif";
    }

    /**
     * Returns the  medium font size.
     */
    public float getMediumFontSize() {
        // 9pt (72pt = 1in)
//        return (9f * 25.4f) / (72f * getPixelUnitToMillimeter());
        // #6477752 The default font size could be adjusted.
        return userAgentInfo.getDefaultFontSize();
    }

    /**
     * Returns a lighter font-weight.
     */
    public float getLighterFontWeight(float f) {
        // Round f to nearest 100...
        int weight = ((int)((f + 50) / 100)) * 100;

        switch (weight) {
        case 100:
            return 100;

        case 200:
            return 100;

        case 300:
            return 200;

        case 400:
            return 300;

        case 500:
            return 400;

        case 600:
            return 400;

        case 700:
            return 400;

        case 800:
            return 400;

        case 900:
            return 400;

        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }

    /**
     * Returns a bolder font-weight.
     */
    public float getBolderFontWeight(float f) {
        // Round f to nearest 100...
        int weight = ((int)((f + 50) / 100)) * 100;

        switch (weight) {
        case 100:
            return 600;

        case 200:
            return 600;

        case 300:
            return 600;

        case 400:
            return 600;

        case 500:
            return 600;

        case 600:
            return 700;

        case 700:
            return 800;

        case 800:
            return 900;

        case 900:
            return 900;

        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }

    /**
     * Returns the language settings.
     */
    public String getLanguages() {
        return "en";
    }

    /**
     * Returns the user stylesheet uri.
     * @return null if no user style sheet was specified.
     */
    public String getUserStyleSheetURI() {
        return null;
    }

    /**
     * Returns this user agent's CSS media.
     */
    public String getMedia() {
        return "screen";
    }

    /**
     * Returns this user agent's alternate style-sheet title.
     */
    public String getAlternateStyleSheet() {
        return null;
    }

    String computeFileName(Object location) {
        return userAgentInfo.computeFileName(location);
    }

    int computeLineNumber(Object location, int lineno) {
        return userAgentInfo.computeLineNumber(location, lineno);
    }

    URL getDocumentUrl(Document document) {
        if (document == null) {
            return null;
        }
        return userAgentInfo.getDocumentUrl(document);
    }

    void displayErrorForLocation(String message, Object location, int lineno, int column) {
        userAgentInfo.displayErrorForLocation(message, location, lineno, column);
    }

    Element getHtmlBodyForDocument(Document document) {
        return userAgentInfo.getHtmlBodyForDocument(document);
    }

    DocumentFragment getHtmlDomFragmentForDocument(Document document) {
        return userAgentInfo.getHtmlDomFragmentForDocument(document);
    }
}
