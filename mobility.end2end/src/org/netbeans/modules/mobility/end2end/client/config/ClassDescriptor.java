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
 * ClassDescriptor.java
 *
 * Created on August 16, 2005, 3:47 PM
 *
 */
package org.netbeans.modules.mobility.end2end.client.config;

/**
 *
 * @author Michal Skvor
 */
public class ClassDescriptor {
    
    final private String location;
    private String mapping;
    final private String type;
    
    private String packageName;
    private String leafName;
    
    /** Creates a new instance of ClassDescriptor */
    public ClassDescriptor( String type, String location ) {
        this.type = type;
        this.location = location;
        
        cutLeafFromTree();
    }
    
    public String getType() {
        return type;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setMapping( final String mapping ) {
        this.mapping = mapping;
    }
    
    public String getMapping() {
        return mapping;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getLeafClassName() {
        return leafName;
    }
    
    private void cutLeafFromTree() {
        // Root
        // FIXME: maybe should be remade to more cute code :)
        final int pos = type.lastIndexOf( '.' );
        if( pos == -1 ) {
            packageName = "";
            leafName = type;
            return;
        }
        packageName = type.substring( 0, pos );
        leafName = type.substring( pos + 1 );
    }
    
}
