/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import java.beans.*;
import java.awt.Image;


/** BeanInfo for plain options
 *
 * @author Libor Karmolis
 */
public class DTDOptionsBeanInfo extends org.netbeans.modules.editor.options.PlainOptionsBeanInfo {

    public DTDOptionsBeanInfo () {
        super ("/org/netbeans/modules/editor/resources/htmlOptions"); // NOI18N
    }

    protected Class getBeanClass() {
        return DTDOptions.class;
    }
}
