/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.visualweb.designer.jsf;

import org.netbeans.modules.visualweb.spi.designer.cssengine.CssUserAgentInfo;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.insync.Util;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Implementation of <code>CssUserAgentInfo</code>. Delegates to css box model
 * implementation.
 *
 * @author Peter Zavadsky
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.visualweb.spi.designer.cssengine.CssUserAgentInfo.class)
public class CssUserAgentInfoImpl implements CssUserAgentInfo {

    /** Creates a new instance of CssUserAgentInfoImpl */
    public CssUserAgentInfoImpl() {
    }

    public float getBlockWidth(Document document, Element element) {
        Box box = findBoxForDocumentElement(document, element);
        if (box != null) {
            return box.getBlockWidth();
        }
        log("No containing block available for element " + element); // NOI18N
        return 0.0f; // if no available containing block, just use 0
    }

    public float getBlockHeight(Document document, Element element) {
        Box box = findBoxForDocumentElement(document, element);
        if (box != null) {
            return box.getBlockHeight();
        }
        log("No containing block available for element " + element); // NOI18N
        return 0.0f; // if no available containing block, just use 0
    }

    public int getDefaultFontSize() {
        return JsfDesignerPreferences.getInstance().getDefaultFontSize();
    }

    // XXX Get rid of it.
    public String computeFileName(Object location) {
        return Util.computeFileName(location);
    }
    // XXX Get rid of it.
    public int computeLineNumber(Object location, int lineno) {
        return Util.computeLineNumber(location, lineno);
    }

    public URL getDocumentUrl(Document document) {
        return Util.getDocumentUrl(document);
    }

    public void displayErrorForLocation(String message, Object location, int lineno, int column) {
        Util.displayErrorForLocation(message, location, lineno, column);
    }

    public Element getHtmlBodyForDocument(Document document) {
        return Util.getHtmlBodyForDocument(document);
    }

    public DocumentFragment getHtmlDomFragmentForDocument(Document document) {
        return Util.getHtmlDomFragmentForDocument(document);
    }
    

    // XXX #110849 Be aware that elemnt might be owned by external document (fragments),
    // not the specified one.
    private static Box findBoxForDocumentElement(Document document, Element element) {
         {
            Designer[] designers = JsfForm.findDesignersForDocument(document);
            for (Designer designer : designers) {
                Box box = designer.findBoxForElement(element);
                if (box != null) {
                    return box;
                }
            }
        }
        
        // XXX #119888 There are funky things done with elements from fragment (inside page),
        // they might be part of different document (engine), try also original document in that case.
        Document doc = element == null ? null : element.getOwnerDocument();
        if (doc != document) {
            Designer[] designers = JsfForm.findDesignersForDocument(doc);
            for (Designer designer : designers) {
                Box box = designer.findBoxForElement(element);
                if (box != null) {
                    return box;
                }
            }
        }
        
        return null;
    }
    
    private static void log(String message) {
        Logger logger = Logger.getLogger(CssUserAgentInfoImpl.class.getName());
        logger.log(Level.FINE, message);
    }
}
