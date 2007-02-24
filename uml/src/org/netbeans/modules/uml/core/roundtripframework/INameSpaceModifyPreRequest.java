/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * File       : INameSpaceModifyPreRequest.java
 * Created on : Nov 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;

/**
 * @author Aztec
 */
public interface INameSpaceModifyPreRequest extends IPreRequest
{
    public void setOldNamespaceName(String nsName);

    public String getOldNamespaceName();

    public void setNewNamespaceName(String nsName);

    public String getNewNamespaceName();

    public void setOldSourceDir(String srcDir);
    
    public String getOldSourceDir();
    
    public void setNewSourceDir(String srcDir);
    
    public String getNewSourceDir();

    public void setOldModifiedNamespaceQualifiedName(String qualName);
    
    public String getOldModifiedNamespaceQualifiedName();
    
    public void setNewModifiedNamespaceQualifiedName(String qualName);
    
    public String getNewModifiedNamespaceQualifiedName();
}
