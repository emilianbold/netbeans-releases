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
