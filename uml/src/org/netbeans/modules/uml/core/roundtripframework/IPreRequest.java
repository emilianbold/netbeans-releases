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
 * File       : IPreRequest.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;

/**
 * @author Aztec
 */
public interface IPreRequest
{
    public boolean postEvent(IRelationProxy pRel);
    
    public boolean postEvent(IElement pElement);

    public IChangeRequest createChangeRequest(IElement pElement, /*ChangeKind*/ int type, /*RequestDetailKind*/ int detail);
    
    public void populateChangeRequest(IChangeRequest req);
    
    /**
     *
     * Called right after a ChangeRequest has been
     * fully intialized, but before it has been processed.
     * This method gives us the ability to handle event
     * specific situations that may affect the ChangeRequest.
     * The base implementation does nothing.
     *
     * @param req[in] The request object to pre process.
     *
     */
   
    public void preProcessRequest(IChangeRequest req);
   
    public IElement preElement();
    
    public IElement origElement();
    
    public IRelationProxy relation();
    
    public IEventPayload payload();
    
    public IElement getPreOwnerElement();    
    public void setPreOwnerElement(IElement element );

    public void setOrigElement(IElement val);
    public IElement getOrigElement();

    public void setDupeElement(IElement val);
    public IElement getDupeElement();

    public INamespace getModifiedNamespace();
    public void setModifiedNamespace(INamespace newVal);

    public IElement getElementWithArtifact();

    public IRequestProcessor getRequestProcessor(IRequestProcessor proc);
    
    public String getFileName();
    
    public String getLanguage();
    
    public int getDetail();
    
    public boolean inCreateState(IElement element);

}
