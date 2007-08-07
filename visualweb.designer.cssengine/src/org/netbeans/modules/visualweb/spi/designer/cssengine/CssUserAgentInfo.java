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

package org.netbeans.modules.visualweb.spi.designer.cssengine;

import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Info about some user agent values.
 * XXX Is it correct to have this kind of dependency?
 * <p>
 * XXX This css engine impl uses it for determining sizes in <code>LengthManager</code>.
 * When having a better impl, this might not be needed here.
 * </p>
 * <p>
 * Also it uses the default font size for computing the relative font sizes.
 * </p>
 *
 * @author Peter Zavadsky
 */
public interface CssUserAgentInfo {
    public float getBlockWidth(Document document, Element element);
    public float getBlockHeight(Document document, Element element);

    public int getDefaultFontSize();

    // XXX Get it out of designer/cssengine (see CssEngineServiceImpl#getAllComputedStylesForElement).
    public String computeFileName(Object location);
    public int computeLineNumber(Object location, int lineno);

    public URL getDocumentUrl(Document document);

    public void displayErrorForLocation(String message, Object location, int lineno, int column);

    public Element getHtmlBodyForDocument(Document document);
    public DocumentFragment getHtmlDomFragmentForDocument(Document document);
}
