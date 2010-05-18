/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
