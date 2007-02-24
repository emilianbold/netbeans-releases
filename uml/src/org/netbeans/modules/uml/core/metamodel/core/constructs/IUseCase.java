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

package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehavioredClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public interface IUseCase extends IBehavioredClassifier
{
       public void addExtend(IExtend extend);
       public void removeExtend(IExtend extend);
       public ETList <IExtend> getExtends();
       public void addInclude(IInclude include);
       public void removeInclude(IInclude include);
       public ETList <IInclude> getIncludes();
       public void addExtensionPoint(IExtensionPoint extPt);
       public void removeExtensionPoint(IExtensionPoint extPt);
       public ETList <IExtensionPoint> getExtensionPoints();
       public void addExtendedBy(IExtend extend);
       public void removeExtendedBy(IExtend extend);
       public ETList <IExtend> getExtendedBy();
       public void addIncludedBy(IInclude include);
       public void removeIncludedBy(IInclude include);
       public ETList <IInclude> getIncludedBy();
       public void addUseCaseDetail(IUseCaseDetail useCaseDetail);
       public void removeUseCaseDetail(IUseCaseDetail useCaseDetail);
       public ETList <IUseCaseDetail> getDetails();
       public IUseCaseDetail createUseCaseDetail();
       public IExtensionPoint createExtensionPoint();
}
