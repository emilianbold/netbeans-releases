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
 * File       : NamespaceChangeRequest.java
 * Created on : Nov 24, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;

/**
 * @author Aztec
 */
public class NamespaceChangeRequest
    extends ChangeRequest
    implements INamespaceChangeRequest
{
    private String m_OldNamespaceName;
    private String m_NewNamespaceName;
    private INamespace m_ModifiedNamespace;
    private String m_OldModifiedNamespaceQualifiedName;
    private String m_NewModifiedNamespaceQualifiedName;
    private String m_OldSourceDir;
    private String m_NewSourceDir;


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getModifiedNamespace()
     */
    public INamespace getModifiedNamespace()
    {
        return m_ModifiedNamespace;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getNewModifiedNamespaceQualifiedName()
     */
    public String getNewModifiedNamespaceQualifiedName()
    {
        return m_NewModifiedNamespaceQualifiedName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getNewNamespaceName()
     */
    public String getNewNamespaceName()
    {
        return m_NewNamespaceName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getNewSourceDir()
     */
    public String getNewSourceDir()
    {
        return m_NewSourceDir;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getOldModifiedNamespaceQualifiedName()
     */
    public String getOldModifiedNamespaceQualifiedName()
    {
        return m_OldModifiedNamespaceQualifiedName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getOldNamespaceName()
     */
    public String getOldNamespaceName()
    {
        return m_OldNamespaceName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#getOldSourceDir()
     */
    public String getOldSourceDir()
    {
        return m_OldSourceDir;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setModifiedNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
     */
    public void setModifiedNamespace(INamespace newVal)
    {
        m_ModifiedNamespace = newVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setNewModifiedNamespaceQualifiedName(java.lang.String)
     */
    public void setNewModifiedNamespaceQualifiedName(String newVal)
    {
        m_NewModifiedNamespaceQualifiedName = newVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setNewNamespaceName(java.lang.String)
     */
    public void setNewNamespaceName(String newVal)
    {
        m_NewNamespaceName = newVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setNewSourceDir(java.lang.String)
     */
    public void setNewSourceDir(String newVal)
    {
        m_NewSourceDir = newVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setOldModifiedNamespaceQualifiedName(java.lang.String)
     */
    public void setOldModifiedNamespaceQualifiedName(String newVal)
    {
        m_OldModifiedNamespaceQualifiedName = newVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setOldNamespaceName(java.lang.String)
     */
    public void setOldNamespaceName(String newVal)
    {
        m_OldNamespaceName = newVal;        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.INamespaceChangeRequest#setOldSourceDir(java.lang.String)
     */
    public void setOldSourceDir(String newVal)
    {
        m_OldSourceDir = newVal;    
    }
}
