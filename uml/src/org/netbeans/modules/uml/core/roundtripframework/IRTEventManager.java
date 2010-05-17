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
  File       : IRTEventManager.java
  Created on : Nov 5, 2003
  Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 * @author Aztec
 */
public interface IRTEventManager
{
    public void processRequests(IElement element, /*ChangeKind*/ int type, IBehavioralFeature feature);
    public void processRequests( IRelationProxy proxy, /*ChangeKind*/ int type );
    public void processRelModRequests ( IRelationProxy proxy, /*ChangeKind*/ int type );
    public void processDuplicateRequests ( IElement pReqElement, IElement pOrigElement, IElement pDupeElement, /*ChangeKind*/ int type );
    public void processImpactedRequests( IElement element, IClassifier pModifiedClass, /*ChangeKind*/ int type );

    public void onRTDocumentationPreModified( IElement element, String proposedValue, IResultCell cell );
    public void onRTElementPreDelete( IVersionableElement element, IResultCell cell );
    public void onRTPreNameModified( INamedElement element, String proposedValue, IResultCell cell );
    public void onRTPreVisibilityModified( INamedElement element, /*VisibilityKind*/ int proposedValue, IResultCell cell );
    public void onRTPreElementAddedToNamespace(INamespace space, INamedElement  element, IResultCell cell );
    public void onRTPreRelationValidate(IRelationProxy  proxy, IResultCell cell );
    public void onRTDefaultPreModified(IAttribute attr, IExpression exp, IResultCell cell );
    public void onRTPreDefaultBodyModified(IAttribute attr, String body, IResultCell cell);
    public void onRTPreDefaultLanguageModified(IAttribute attr, String lang, IResultCell cell );
    public void onRTConcurrencyPreModified(IBehavioralFeature feat, /*CallConcurrencyKind*/ int kind, IResultCell cell );
    public void onRTPreHandledSignalAdded(IBehavioralFeature  feat, ISignal  sig, IResultCell cell );
    public void onRTPreHandledSignalRemoved(IBehavioralFeature feat, ISignal  sig, IResultCell cell );
    public void onRTPreParameterAdded(IBehavioralFeature  feat, IParameter  parm, IResultCell cell );
    public void onRTPreParameterRemoved(IBehavioralFeature feat, IParameter  parm, IResultCell cell );
    public void onRTPreAbstractModified(IBehavioralFeature feat, boolean flag, IResultCell cell);
    public void onRTFeaturePreAdded(IClassifier  classifier, IFeature  feat, IResultCell cell );
    public void onRTPreAbstractModified(IClassifier element, boolean flag, IResultCell cell);
    public void onRTPreLeafModified(IClassifier element, boolean flag, IResultCell cell);
    public void onRTPreTransientModified(IClassifier element, boolean flag, IResultCell cell);
    public void onRTPreStaticModified(IFeature  feat, boolean flag, IResultCell cell );
    public void onRTPreNativeModified(IFeature  feat, boolean flag, IResultCell cell );
    public void onRTPreTransientModified(IStructuralFeature  feat, boolean flag, IResultCell cell );
    public void onRTPreVolatileModified(IStructuralFeature  feat, boolean flag, IResultCell cell );

    public void onRTConditionPreAdded(IOperation  oper, IConstraint  cons, boolean flag, IResultCell cell );
    public void onRTConditionPreRemoved(IOperation  oper, IConstraint  cons, boolean flag, IResultCell cell);
    public void onRTPreQueryModified(IOperation  oper, boolean flag, IResultCell cell );
    public void onRTPreDefaultExpModified(IParameter parm, IExpression  exp, IResultCell cell );
    public void onRTPreDefaultExpBodyModified(IParameter parm, String exp, IResultCell cell );
    public void onRTPreDefaultExpLanguageModified(IParameter parm, String exp, IResultCell cell);
    public void onRTPreDirectionModified(IParameter parm, /*ParameterDirectionKind*/ int kind, IResultCell cell );
    public void onRTPreChangeabilityModified(IStructuralFeature feat, /*ChangeableKind*/ int kind, IResultCell cell );
    public void onRTPreMultiplicityModified(ITypedElement  element, IMultiplicity  mult, IResultCell cell );
    public void onRTPreTypeModified(ITypedElement element, IClassifier  classifier, IResultCell cell );
    public void onRTPreLowerModified(ITypedElement element, IMultiplicity  mult, IMultiplicityRange  range, String lower, IResultCell cell );
    public void onRTPreUpperModified(ITypedElement element, IMultiplicity  mult, IMultiplicityRange  range, String upper, IResultCell cell );
    public void onRTPreRangeAdded(ITypedElement element, IMultiplicity  mult, IMultiplicityRange  range, IResultCell cell);
    public void onRTPreRangeRemoved(ITypedElement element, IMultiplicity  mult, IMultiplicityRange  range, IResultCell cell );
    public void onRTPreOrderModified(ITypedElement element, IMultiplicity  mult, boolean flag, IResultCell cell );

    public void onRTPreRelationEndModified ( IRelationProxy proxy, IResultCell cell );
    public void onRTPreRelationEndAdded    ( IRelationProxy proxy, IResultCell cell );
    public void onRTPreRelationEndRemoved  ( IRelationProxy proxy, IResultCell cell );

    public void onRTPreRelationCreated ( IRelationProxy proxy, IResultCell cell );
    public void onRTPreRelationDeleted ( IRelationProxy proxy, IResultCell cell );

    // Posts

    public void onRTDocumentationModified ( IElement element, IResultCell cell );

    public void onRTElementDelete      ( IVersionableElement element, IResultCell cell );

    public void onRTNameModified       ( INamedElement element, IResultCell cell );
    public void onRTVisibilityModified ( INamedElement element, IResultCell cell );

    public void onRTElementAddedToNamespace ( INamespace space, INamedElement element, IResultCell cell );

    public void onRTRelationValidate  ( IRelationProxy proxy, IResultCell cell );

    public void onRTDefaultModified         ( IAttribute attr, IResultCell cell );
    public void onRTDefaultBodyModified     ( IAttribute attr, IResultCell cell);
    public void onRTDefaultLanguageModified ( IAttribute attr, IResultCell cell );

    public void onRTConcurrencyModified  ( IBehavioralFeature feat, IResultCell cell );
    public void onRTHandledSignalAdded   ( IBehavioralFeature feat, IResultCell cell );
    public void onRTHandledSignalRemoved ( IBehavioralFeature feat, IResultCell cell );
    public void onRTParameterAdded       ( IBehavioralFeature feat, IParameter parm, IResultCell cell );
    public void onRTParameterRemoved     ( IBehavioralFeature feat, IParameter parm, IResultCell cell );
    public void onRTAbstractModified     ( IBehavioralFeature feat, IResultCell cell );

    public void onRTFeatureAdded      ( IClassifier classifier, IFeature feat, IResultCell cell );
    public void onRTAbstractModified  ( IClassifier classifier, IResultCell cell );
    public void onRTLeafModified      ( IClassifier classifier, IResultCell cell );
    public void onRTTransientModified ( IClassifier classifier, IResultCell cell );

    public void onRTStaticModified ( IFeature feat, IResultCell cell );
    public void onRTNativeModified ( IFeature feat, IResultCell cell );

    public void onRTTransientModified     ( IStructuralFeature feat, IResultCell cell );
    public void onRTVolatileModified      ( IStructuralFeature feat, IResultCell cell );
    public void onRTChangeabilityModified ( IStructuralFeature feat, IResultCell cell );

    public void onRTConditionAdded   ( IOperation oper, IConstraint cons, boolean flag, IResultCell cell );
    public void onRTConditionRemoved ( IOperation oper, IConstraint cons, boolean flag, IResultCell cell );
    public void onRTQueryModified    ( IOperation oper, IResultCell cell );

    public void onRTDefaultExpModified         ( IParameter parm, IResultCell cell );
    public void onRTDefaultExpBodyModified     ( IParameter parm, IResultCell cell );
    public void onRTDefaultExpLanguageModified ( IParameter parm, IResultCell cell );
    public void onRTDirectionModified          ( IParameter parm, IResultCell cell );

    public void onRTTypeModified         ( ITypedElement element, IResultCell cell );
    public void onRTMultiplicityModified ( ITypedElement element, IResultCell cell );
    public void onRTLowerModified        ( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );
    public void onRTUpperModified        ( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );
    public void onRTRangeAdded           ( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );
    public void onRTRangeRemoved         ( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );
    public void onRTOrderModified        ( ITypedElement element, IMultiplicity mult, IResultCell cell );
    public void onRTCollectionTypeModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

    public void onRTRelationEndModified ( IRelationProxy proxy, IResultCell cell );
    public void onRTRelationEndAdded    ( IRelationProxy proxy, IResultCell cell );
    public void onRTRelationEndRemoved  ( IRelationProxy proxy, IResultCell cell );
    public void onRTRelationCreated     ( IRelationProxy proxy, IResultCell cell );
    public void onRTRelationDeleted     ( IRelationProxy proxy, IResultCell cell );

    // Feature move

    public void onRTFeaturePreMoved ( IClassifier classifier, IFeature feature, IResultCell cell );
    public void onRTFeatureMoved    ( IClassifier classifier, IFeature feature, IResultCell cell );

    public void onRTFeaturePreDuplicatedToClassifier ( IClassifier classifier, IFeature feature, IResultCell cell );
    public void onRTFeatureDuplicatedToClassifier    ( IClassifier pOldClassifier,  IFeature pOldFeature,  IClassifier pNewClassifier,  IFeature pNewFeature, IResultCell cell );

    // Classifier transform

    public void onRTPreTransform ( IClassifier classifier, String newForm, IResultCell cell );
    public void onRTTransformed  ( IClassifier classifier, IResultCell cell );

    // AssociationEnd transform

    public void onRTPreTransform ( IAssociationEnd pEnd, String newForm, IResultCell cell );
    public void onRTTransformed  ( IAssociationEnd pEnd, IResultCell cell );

    // RedefinableElement

    public void onRTPreFinalModified ( IRedefinableElement element, boolean newVal, IResultCell cell );
    public void onRTFinalModified    ( IRedefinableElement element, IResultCell cell );
    public void onRTPreRedefinedElementAdded(IRedefinableElement redefiningElement,  IRedefinableElement redefinedElement,  IResultCell cell);
    public void onRTRedefinedElementAdded( IRedefinableElement redefiningElement,  IRedefinableElement redefinedElement,  IResultCell cell);
    public void onRTPreRedefinedElementRemoved( IRedefinableElement redefiningElement,  IRedefinableElement redefinedElement,  IResultCell cell);
    public void onRTRedefinedElementRemoved( IRedefinableElement redefiningElement,  IRedefinableElement redefinedElement,  IResultCell cell);
    public void onRTPreRedefiningElementAdded( IRedefinableElement redefinedElement,  IRedefinableElement redefiningElement,  IResultCell cell);
    public void onRTRedefiningElementAdded( IRedefinableElement redefinedElement,  IRedefinableElement redefiningElement,  IResultCell cell);
    public void onRTPreRedefiningElementRemoved( IRedefinableElement redefinedElement,  IRedefinableElement redefiningElement,  IResultCell cell);
    public void onRTRedefiningElementRemoved( IRedefinableElement redefinedElement,  IRedefinableElement redefiningElement,  IResultCell cell);

    // BehavioralFeature StrictFP

    public void onRTPreStrictFPModified ( IBehavioralFeature feat, boolean flag, IResultCell cell );
     public void onRTStrictFPModified    ( IBehavioralFeature feat, IResultCell cell );

    // IAffectedElementEventsSink

    public void onRTPreImpacted ( IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell );
    public void onRTImpacted ( IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell );

    // IOperationEventsSink

    public void onRTRaisedExceptionPreAdded   ( IOperation oper, IClassifier pException, IResultCell cell );
    public void onRTRaisedExceptionPreRemoved ( IOperation oper, IClassifier pException, IResultCell cell );
    public void onRTRaisedExceptionAdded      ( IOperation oper, IClassifier pException, IResultCell cell );
    public void onRTRaisedExceptionRemoved    ( IOperation oper, IClassifier pException, IResultCell cell );
    public void onRTPreOperationPropertyModified    ( IOperation oper, /*OperationPropertyKind*/ int nKind,  boolean proposedValue,  IResultCell cell );
    public void onRTOperationPropertyModified       ( IOperation oper, /*OperationPropertyKind*/ int nKind,  IResultCell cell );

     // IEventFrameworkEventsSink
    // -------------------------------------------------------------------------

    public void onRTPreEventContextPushed  ( IEventContext pContext, IResultCell pCell );
    public void onRTEventContextPushed     ( IEventContext pContext, IResultCell pCell );
    public void onRTPreEventContextPopped  ( IEventContext pContext, IResultCell pCell );
    public void onRTEventContextPopped     ( IEventContext pContext, IResultCell pCell );
    public void onRTEventDispatchCancelled ( ETList<Object> pListeners, 
                                                   Object listenerWhoCancelled, 
                                                 IResultCell pCell );

    // -------------------------------------------------------------------------
     // IWSProjectEventsSink
    // -------------------------------------------------------------------------

    public void onRTWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell );
    public void onRTWSProjectCreated(IWSProject project, IResultCell cell );
    public void onRTWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell );
    public void onRTWSProjectOpened(IWSProject project, IResultCell cell );
    public void onRTWSProjectPreRemove(IWSProject project, IResultCell cell );
    public void onRTWSProjectRemoved(IWSProject project, IResultCell cell );
    public void onRTWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell );
    public void onRTWSProjectInserted(IWSProject project, IResultCell cell );
    public void onRTWSProjectPreRename(IWSProject project, String newName, IResultCell cell );
    public void onRTWSProjectRenamed(IWSProject project, String oldName, IResultCell cell );
    public void onRTWSProjectPreClose(IWSProject project, IResultCell cell );
    public void onRTWSProjectClosed(IWSProject project, IResultCell cell );
    public void onRTWSProjectPreSave(IWSProject project, IResultCell cell );
    public void onRTWSProjectSaved(IWSProject project, IResultCell cell );

    // -------------------------------------------------------------------------
     // IProjectEventsSink
    // -------------------------------------------------------------------------
    public void onRTPreSourceDirModified(IPackage element, String proposedSourceDir, IResultCell cell);
    public void onRTSourceDirModified(IPackage element, IResultCell cell);
}
