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
 * File       : ImplementationClassChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author Aztec
 */
public class ImplementationClassChangeRequest
    extends ImplementationChangeRequest
    implements IImplementationClassChangeRequest
{
    IClassifier m_BeforeImplementing = null;
    IClassifier m_AfterImplementing = null;
    IClassifier m_BeforeInterface = null;
    IClassifier m_AfterInterface = null;
    IElement    m_BeforeConnection = null;
    IElement    m_AfterConnection = null;
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setAfterConnection(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setAfterConnection(IElement pElement)
    {
        m_AfterConnection = pElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setAfterImplementing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setAfterImplementing(IClassifier pClassifier)
    {
        m_AfterImplementing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setAfterInterface(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setAfterInterface(IClassifier pClassifier)
    {
        m_AfterInterface = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setBeforeConnection(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setBeforeConnection(IElement pElement)
    {
        m_BeforeConnection = pElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setBeforeImplementing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setBeforeImplementing(IClassifier pClassifier)
    {
        m_BeforeImplementing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setBeforeInterface(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setBeforeInterface(IClassifier pClassifier)
    {
        m_BeforeInterface = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterConnection()
     */
    public IElement getAfterConnection()
    {
        return m_AfterConnection;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterImplementing()
     */
    public IClassifier getAfterImplementing()
    {
        return m_AfterImplementing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterInterface()
     */
    public IClassifier getAfterInterface()
    {
        return m_AfterInterface;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeConnection()
     */
    public IElement getBeforeConnection()
    {
        return m_BeforeConnection;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeImplementing()
     */
    public IClassifier getBeforeImplementing()
    {
        return m_BeforeImplementing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeInterface()
     */
    public IClassifier getBeforeInterface()
    {
        return m_BeforeInterface;
    }

}
