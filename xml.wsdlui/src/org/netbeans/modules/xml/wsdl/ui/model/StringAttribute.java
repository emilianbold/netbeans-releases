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

/*
 * StringAttribute.java
 *
 * Created on April 14, 2006, 11:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.model;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author radval
 */
public class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        public Class getType() { return String.class; }
        public String getName() { return name; }
        public Class getMemberType() { return null; }
    }
