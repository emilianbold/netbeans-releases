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
package org.netbeans.modules.j2ee.sun.share;

/** Mapping of principal name and optional class-name field.
 *
 * The one interesting characteristic of this class is that equals and hashCode
 * only take into account the principal name field so two instances with the same
 * principal name but different classnames would be considered equal.  The reason
 * for this is because only the principal name is used as a key when searching
 * for this object in a collection.  It does not make sense to have two instances
 * that differ only by classname.
 *
 * @author Peter Williams
 */
public final class PrincipalNameMapping {
    
    private String principalName;
    private String className;

    public PrincipalNameMapping(String pn) {
        this(pn, null);
    }

    public PrincipalNameMapping(String pn, String cn) {
        assert(pn != null) : "Principal name cannnot be null";
        
        principalName = pn;
        className = cn;
    }

    public String toString() {
        if(className == null || className.length() == 0) {
            return principalName;
        }
        StringBuffer buffer = new StringBuffer(principalName.length() + className.length() + 10);
        buffer.append(principalName);
        buffer.append(" [cn=");
        buffer.append(className);
        buffer.append("]");
        return buffer.toString();
    }

    public String getPrincipalName() {
        return principalName;
    }

    public String getClassName() {
        return className;
    }

    public boolean equals(Object obj) {
        boolean result = false;

        if(obj instanceof PrincipalNameMapping) {
            result = principalName.equals(((PrincipalNameMapping) obj).getPrincipalName());
        }

        return result;
    }

    public int hashCode() {
        return principalName.hashCode();
    }
}
