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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.vmd.midp.serialization;

import org.netbeans.modules.vmd.api.model.PresenterSerializer;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Arrays;

/**
 * @author David Kaspar
 */
public class MidpPropertyPresenterSerializer implements PresenterSerializer {

    private String displayName;
    private String editorID;
    private String propertyName;

    public MidpPropertyPresenterSerializer (String displayName, PropertyDescriptor property) {
        this.displayName = displayName;
        this.editorID = createEditorIDForPropertyDescriptor (property);
        this.propertyName = property.getName ();
    }

    public List<Element> serialize (Document document) {
        org.w3c.dom.Element element = document.createElement (MidpPropertyPresenterDeserializer.PROPERTY_NODE);
        XMLUtils.setAttribute (document, element, MidpPropertyPresenterDeserializer.DISPLAY_NAME_ATTR, displayName);
        XMLUtils.setAttribute (document, element, MidpPropertyPresenterDeserializer.EDITOR_ATTR, editorID);
        XMLUtils.setAttribute (document, element, MidpPropertyPresenterDeserializer.PROPERTY_NAME_ATTR, propertyName);
        return Arrays.asList (element);
    }

    private static String createEditorIDForPropertyDescriptor (PropertyDescriptor property) {
        return null; // TODO
    }

}
