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

/*
 * FieldData.java
 *
 * Created on July 7, 2005, 11:38 AM
 *
 */
package org.netbeans.modules.mobility.end2end.classdata;

/**
 *
 * @author misk
 */
public class FieldData {
    
    final private String name;
    final private String type;
    final private int access;
    
    /** Creates a new instance of FieldData */
    public FieldData( String name, String type, int access ) {
        this.name = name;
        this.type = type;
        
        this.access = access;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public int getAccess() {
        return access;
    }
}
