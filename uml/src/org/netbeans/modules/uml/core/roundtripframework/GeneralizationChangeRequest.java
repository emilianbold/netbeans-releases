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
 * File       : GeneralizationChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;

/**
 * @author Aztec
 */
public class GeneralizationChangeRequest
    extends ChangeRequest
    implements IGeneralizationChangeRequest
{
    private boolean m_SpecializeEffected = false;
    private boolean m_ArtifactIsFrom = false;
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterConnection()
     */
    public IElement getAfterConnection()
    {
        return getAfter();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterGeneralizing()
     */
    public IClassifier getAfterGeneralizing()
    {
        IGeneralization gen = afterGeneralization();
        if(gen != null)
            return gen.getGeneral();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterSpecializing()
     */
    public IClassifier getAfterSpecializing()
    {
        IGeneralization gen = afterGeneralization();
        if(gen != null)
            return gen.getSpecific();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getArtifactIsFrom()
     */
    public boolean getArtifactIsFrom()
    {
        return m_ArtifactIsFrom;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeConnection()
     */
    public IElement getBeforeConnection()
    {
        return getBefore();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeGeneralizing()
     */
    public IClassifier getBeforeGeneralizing()
    {
        int isCreate = getState();
        if (isCreate != ChangeKind.CT_CREATE)
        {
            boolean splEffected = getSpecializationEffected();
        
            if (splEffected)
            {
                // The generalizing class is not effected. So, it should be the same, so we
                // can use the after connection and not worry about the clone problem.
        
                IGeneralization gen = afterGeneralization();
                if(gen != null)
                    return gen.getGeneral();
            }
            else
            {
                // We have to worry about the clone problem.  The clone problem is 
                // that a node has been cloned, and so might not be able to able
                // retrieve an item out of the model based on an ID.
                        
                IGeneralization gen = beforeGeneralization();
                if(gen != null)
                    return gen.getGeneral();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeSpecializing()
     */
    public IClassifier getBeforeSpecializing()
    {
        int isCreate = getState();
        if (isCreate != ChangeKind.CT_CREATE)
        {
            boolean splEffected = getSpecializationEffected();
        
            if (splEffected == false)
            {
                // The implementing class is not effected. So, it should be the same, so we
                // can use the after connection and not worry about the clone problem.
        
                IGeneralization gen = afterGeneralization();
                if(gen != null)
                    return gen.getSpecific();
            }
            else
            {
                // We have to worry about the clone problem.  The clone problem is 
                // that a node has been cloned, and so might not be able to able
                // retrieve an item out of the model based on an ID.
                        
                IGeneralization gen = beforeGeneralization();
                if(gen != null)
                    return gen.getSpecific();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getSpecializationEffected()
     */
    public boolean getSpecializationEffected()
    {
        return m_SpecializeEffected;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#setArtifactIsFrom(boolean)
     */
    public void setArtifactIsFrom(boolean artIsFrom)
    {
        m_ArtifactIsFrom = artIsFrom;        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#setSpecializationEffected(boolean)
     */
    public void setSpecializationEffected(boolean splEffected)
    {
        m_SpecializeEffected = splEffected;
    }
    
    protected IGeneralization beforeGeneralization()
    {
        IElement retVal = getBefore();
        if(retVal != null && retVal instanceof IGeneralization)
            return (IGeneralization)retVal;
        return null;
    }

    protected IGeneralization afterGeneralization()
    {
        IElement retVal = getAfter();
        if(retVal != null && retVal instanceof IGeneralization)
            return (IGeneralization)retVal;
        return null;
    }

}
