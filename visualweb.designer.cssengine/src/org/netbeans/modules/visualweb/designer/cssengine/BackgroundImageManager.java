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
package org.netbeans.modules.visualweb.designer.cssengine;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;


/**
 * This class provides support for the "background-image" property.
 *
 * @author Tor Norbye
 */
public class BackgroundImageManager extends AbstractValueManager {

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
        return CssConstants.CSS_BACKGROUND_IMAGE_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.NONE_VALUE;
    }

    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
	switch (lu.getLexicalUnitType()) {
	case LexicalUnit.SAC_INHERIT:
	    return CssValueConstants.INHERIT_VALUE;

        case LexicalUnit.SAC_URI:
            String stringValue = lu.getStringValue();

            java.net.URL docUrl = null;

            if(stringValue.length() > 1 && stringValue.startsWith("/")) {
                // XXX #6336303 If the value starts with '/', it is context-relative,
                // therefore this hack chops it off to assure resolution of the URI.
                // TODO Check if it is correct to provide base URI ending with '/'.
                stringValue = stringValue.substring(1);

//                org.w3c.dom.Document doc = engine.getDocument();
////                if(doc instanceof RaveDocument) {
////                    RaveDocument rDoc = (RaveDocument)doc;
//                    // <markup_separation>
////                    DesignProject designProject = rDoc.getRoot().getDesignBean().getDesignContext().getProject();
////                    if(designProject instanceof FacesModelSet) {
////                        FacesModelSet fModelSet = (FacesModelSet)designProject;
////                        FileObject documentRoot = JsfProjectHelper.getDocumentRoot(fModelSet.getProject());
////                        try {
////                            docUrl = documentRoot.getURL();
////                        } catch(FileStateInvalidException fsie) {
////                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
////                        }
////                    }
//                    // ====
//                    docUrl = InSyncService.getProvider().getDocumentUrl(doc);
                if (engine instanceof XhtmlCssEngine) {
                    docUrl = ((XhtmlCssEngine)engine).getDocumentUrl();
                }
                    // </markup_separation>
//                }
            }
//            else {
//                TODO It is not context-relative, but relative to the page location.
//            }

            if(docUrl != null) {
                return new URIValue(lu.getStringValue(), resolveURI(docUrl, stringValue));
            } else {
                return new URIValue(lu.getStringValue(), resolveURI(engine.getCSSBaseURI(), stringValue));
            }
        case LexicalUnit.SAC_IDENT:
            String s = lu.getStringValue().toLowerCase().intern();
            if (s == CssConstants.CSS_NONE_VALUE) {
                return CssValueConstants.NONE_VALUE;
            }
            throw createInvalidIdentifierDOMException(lu.getStringValue(), engine);
        }
        throw createInvalidLexicalUnitDOMException(lu.getLexicalUnitType(), engine);
    }
}
