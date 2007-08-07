/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        Designer[] designers = JsfForm.findDesignersForDocument(document);
        for (Designer designer : designers) {
            Box box = designer.findBoxForElement(element);
            if (box != null) {
                return box;
            }
        }
        
        return null;
    }
    
    private static void log(String message) {
        Logger logger = Logger.getLogger(CssUserAgentInfoImpl.class.getName());
        logger.log(Level.FINE, message);
    }
}
