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
package org.netbeans.modules.subversion.ui.properties;

/**
 *
 * @author Peter Pis
 */
public class SvnPropertiesNode {
    
    private  String name;
    private String value;
    
    /** Creates a new instance of SvnPropertiesNodes */
    public SvnPropertiesNode(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }    
    
    public String getValue() {
        return value;
    }    
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SvnPropertiesNode) {
            if (name.equals(((SvnPropertiesNode) o).getName()) && value.equals(((SvnPropertiesNode) o).getValue())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
