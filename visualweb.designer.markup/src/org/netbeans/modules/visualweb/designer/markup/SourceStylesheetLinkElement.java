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

package org.netbeans.modules.visualweb.designer.markup;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleRefreshable;
import org.netbeans.modules.visualweb.api.insync.InSyncService;
import java.net.URL;

import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.DOMException;

import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.w3c.dom.Document;

/**
 * This class is derived from
 * org.apache.batik.dom.svg.SVGStyleSheetProcessingInstruction so I've
 * left the Batik copyright on it.  This class represents a
 * &lt;link&gt; element with a stylesheet rel in the DOM.
 * (The original class was a ProcessingInstruction; according to
 * http://www.w3.org/TR/xml-stylesheet/  we should be supporting that,
 * but I don't see any mention in the XHTML spec, and Mozilla doesn't
 * handle it well on a test I tried. So I guess we'll return to this.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @author Tor Norbye
 */
class SourceStylesheetLinkElement extends RaveElement
implements CSSStyleSheetNode, StyleRefreshable {

    /**
     *
     */
    private static final long serialVersionUID = 3905243420396959284L;

    public SourceStylesheetLinkElement(
        CoreDocumentImpl ownerDocument,
        String namespaceURI,
        String qualifiedName)
        throws DOMException {
        // XXX Is this redundant? Should we just hardcode CSS_LINK_TAG here
        // and not have it passed in?
        super(ownerDocument, namespaceURI, qualifiedName);
    }

    /**
     * The style-sheet.
     */
    protected StyleSheet styleSheet;
    
    public void refresh() {
        styleSheet = null;
    }

    /**
     * Returns the associated style-sheet.
     */
    public StyleSheet getCSSStyleSheet() {
        if (styleSheet == null) {
            String rel = getAttribute(HtmlAttribute.REL);
            if (rel.equalsIgnoreCase("stylesheet")) {
                String type = getAttribute(HtmlAttribute.TYPE);
                if (type.length() == 0 || type.equalsIgnoreCase("text/css")) {
                    String title     = getAttribute(HtmlAttribute.TITLE);
                    String media     = getAttribute(HtmlAttribute.MEDIA);
                    String href      = getAttribute(HtmlAttribute.HREF);
                    String alternate = getAttribute("alternate");
//                    RaveDocument doc = (RaveDocument)getOwnerDocument();
//                    URL durl = doc.getUrl();
                    Document doc = getOwnerDocument();
                    URL durl = InSyncService.getProvider().getUrl(doc);
                    URL burl = InSyncService.getProvider().resolveUrl(durl, doc, href);
                    
//                    CSSEngine e = doc.getCssEngine();
//                    CSSEngine e = CssEngineServiceProvider.getDefault().getCssEngine(doc);
//                    styleSheet = e.parseStyleSheet
//                        (burl, media, burl);
                    styleSheet = CssProvider.getEngineService().parseStyleSheetForDocument(doc, burl, media, burl);
                    
                    boolean isAlternate = "yes".equals(alternate);
                    styleSheet.setAlternate(isAlternate);
                    if (title.length() == 0 && !isAlternate) {
                        // If a title wasn't specified at least provide a somewhat useful one
                        title = href;
                    }
                    styleSheet.setTitle(title);
                    styleSheet.setupFilters();
                }
            }
        }
        return styleSheet;
    }
    
    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.ProcessingInstruction#setData(String)}.
     */
    /*
    public void setData(String data) throws DOMException {
	super.setData(data);
        styleSheet = null;
    }
    */
    
    public String toString() {
        return super.toString()+"[href=" + getAttribute(HtmlAttribute.HREF) + "]";
    }
}
