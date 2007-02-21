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
import org.w3c.dom.Document;

import java.util.List;
import java.util.Arrays;

/**
 * @author David Kaspar
 */
public class MidpSetterPresenterSerializer implements PresenterSerializer {

    private String name;
    private List<String> parameters;

    public MidpSetterPresenterSerializer (String name, List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public List<org.w3c.dom.Element> serialize (Document document) {
        org.w3c.dom.Element element = document.createElement (MidpSetterPresenterDeserializer.SETTER_NODE);
        if (name != null)
            XMLUtils.setAttribute (document, element, MidpSetterPresenterDeserializer.NAME_ATTR, name);
        StringBuffer buffer = new StringBuffer ();
        for (String s : parameters) {
            if (buffer.length () > 0)
                buffer.append (',');
            buffer.append (s);
        }
        XMLUtils.setAttribute (document, element, MidpSetterPresenterDeserializer.PARAMETERS_ATTR, buffer.toString ());
        return Arrays.asList (element);
    }

}
