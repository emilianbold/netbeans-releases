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
package org.netbeans.modules.visualweb.propertyeditors;

import java.awt.Component;

/**
 * A place holder for an eventual, feature-complete editor for properties
 * of type <code>java.lang.String</code> that should contain a well-formed
 * JavaScript expression.
 *
 * @author gjmurphy
 */
//TODO Add support for selecting Javascript code clips defined in palette
public class JavaScriptPropertyEditor extends StringPropertyEditor implements 
        com.sun.rave.propertyeditors.JavaScriptPropertyEditor {

    public boolean supportsCustomEditor() {
        return true;
    }

    public Component getCustomEditor() {
        return new JavaScriptPropertyPanel(this);
    }

}
