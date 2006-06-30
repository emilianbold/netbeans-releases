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
 */
package org.netbeans.modules.xml.text.syntax;

import java.beans.*;
import java.awt.Image;
import org.netbeans.modules.editor.options.OptionSupport;

/** BeanInfo for plain options
 *
 * @author Libor Kramolis
 */
public class XMLOptionsBeanInfo extends org.netbeans.modules.editor.options.BaseOptionsBeanInfo {

    public XMLOptionsBeanInfo () {
        super ("/org/netbeans/modules/xml/text/resources/xmlEditorOptions"); // NOI18N
    }

    protected Class getBeanClass() {
        return XMLOptions.class;
    }
	
    protected String[] getPropNames() {
        String parentNames[] = super.getPropNames();
        return OptionSupport.mergeStringArrays(parentNames, XMLOptions.XML_PROP_NAMES);
    }
	
}
