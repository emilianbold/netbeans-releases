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
 * ClassData.java
 *
 * Created on July 7, 2005, 11:37 AM
 *
 */
package org.netbeans.modules.mobility.end2end.classdata;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Skvor
 */
public class ClassData {
    
    protected String type;
    protected String packageName;
    protected String leafName;
    transient private String proxyClassType;
    
    protected List<OperationData> operations;
    
    /** Creates a new instance of ClassData */
    public ClassData( String type ) {
        
        this.type = type;
        cutLeafFromTree();
        operations = new ArrayList<OperationData>();
    }
    
    /**
     * Returns fully qualified class name
     *
     * @return class name
     */
    public String getType() {
        return type;
    }
    
    /**
     * Returns name of the package
     *
     * @return package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Returns leaf class name
     *
     * @return class name
     */
    public String getClassName() {
        return leafName;
    }
    
    public void setOperations( final List<OperationData> operations ) {
        this.operations = operations;
    }
    
    public List<OperationData> getOperations() {
        return operations;
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
    
    public String getProxyClassType() {
        return proxyClassType;
    }
    
    public void setProxyClassType(final String proxyClassType) {
        this.proxyClassType = proxyClassType;
    }
}
