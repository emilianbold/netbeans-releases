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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
