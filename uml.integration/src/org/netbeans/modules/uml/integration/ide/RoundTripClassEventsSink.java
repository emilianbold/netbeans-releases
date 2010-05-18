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
 * File         : RoundTripClassEventsSink.java
 * Version      : 1.2
 * Description  : Listens for class change events in the Describe model
 * Author       : Ashish
 */
package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import java.util.Vector;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.integration.ide.listeners.IAttributeChangeListener;
import org.netbeans.modules.uml.integration.ide.listeners.IClassChangeListener;
import org.netbeans.modules.uml.integration.ide.listeners.IOperationChangeListener;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IClassTransformChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IElementDuplicatedChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaChangeHandlerUtilities;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.Debug;

/**
 *  Listens for changes to classes in the Describe model.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Added file and class comments, tweaked to
 *                              allow redirecting events to a subsidiary
 *                              listener.
 *   2  2002-04-24  Darshan     Fixed bug using the wrong element for addClass.
 *   3  2002-04-24  Darshan     Added a hack to distinguish between create and
 *                              modify class events when on build 45.
 *   4  2002-04-26  Darshan     Removed diagnostic messages from second
 *                              (post-change) event functions.
 *   5  2002-05-06  Darshan     Removed hack (for build 45) to distinguish
 *                              create and modify classes. Added preliminary
 *                              code to handle relationship events.
 *   6  2002-05-13  Darshan     Updated to handle both class and interface
 *                              events by manipulating IClassifier references
 *                              instead of directly dealing with IClass and
 *                              IInterface references.
 *   7  2002-05-29  Darshan     No longer add implements clauses on realize
 *                              relationships - instead use implementation
 *                              relationship.
 *   8  2002-06-05  Darshan     Modified to allow the handling of
 *                              generalizations between interfaces.
 *
 * @author  Ashish
 * @version 1.0
 */
public class RoundTripClassEventsSink extends RoundTripSource
                                      implements IRoundTripClassEventsSink, IRoundTripEnumEventsSink {

    public RoundTripClassEventsSink() {
        Log.out("!--------- Class events sink instantiated");
    }

    public void onPreClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
    }

    public void onClassChangeRequest(IChangeRequest newVal, IResultCell cell) {
        //if (EventManager.isRoundTripActive())
        //    return 0;

        try {
            Log.out("!-------- Got a classChange event");
            ChangeUtils.say(newVal);
            fireClassChangeEvent(newVal, false);
        } catch (Exception e) {
            Log.stackTrace(e);
        }
    }

    public void onPreEnumChangeRequest(IChangeRequest newVal, IResultCell cell) {
    }

    public void onEnumChangeRequest(IChangeRequest newVal, IResultCell cell) {
        //if (EventManager.isRoundTripActive())
        //    return 0;

        try {
            Log.out("!-------- Got a enumChange event");
            ChangeUtils.say(newVal);
            fireClassChangeEvent(newVal, false);
        } catch (Exception e) {
            Log.stackTrace(e);
        }
    }
    
    protected void fireClassChangeEvent(IChangeRequest newVal,
                                        final boolean beforeChange) {

        int reqType = newVal.getRequestDetailType();

        Log.out("Got request detail type " + reqType);
        if (!isValidEventType(reqType)) {
            Log.out("Request detail type " + reqType + " is blocked, abandoning");
            return ;
        }

        // Get change type
        int changeType = newVal.getState();
//Jyothi Part of the fix for Bug#6309925
//        IClassifier before   = (IClassifier)  newVal.getBefore(),
//                    after    = (IClassifier)  newVal.getAfter();
        
        IElement iElement_before = newVal.getBefore();
        IClassifier before = null;
        JavaChangeHandlerUtilities m_utils = new JavaChangeHandlerUtilities();
        
        if (iElement_before instanceof IClassifier) {
            before = (IClassifier)iElement_before;
        }
        else if (iElement_before instanceof IParameterableElement) {
            before = m_utils.getClass(iElement_before);
        }
        if (before == null) {            
            return;
        }
        
        IElement iElement_after = newVal.getAfter();
        IClassifier after = null;
        
         if (iElement_after instanceof IClassifier) {
            after = (IClassifier)iElement_after;
        }
        else if (iElement_after instanceof IParameterableElement) {
            after = m_utils.getClass(iElement_after);
        }
        if (after == null) {        
            return;
        }
        
         switch (changeType) {
            case ChangeUtils.CT_CREATE: {
                if (isValidEvent(after)) {
                    if(newVal.getRequestDetailType() != ChangeUtils.RDT_TRANSFORM){
                        ClassInfo clazz = new ClassInfo(after);
                        clazz.setMethodsAndMembers(after);
                        fireClassAddedEvent(clazz, beforeChange);
                    } else {
                        if (before instanceof IEnumeration && !(after instanceof IEnumeration)) {
                            transformClassifier(before, after, beforeChange);
                        } else if (before instanceof IActor && !(after instanceof IActor)) {
                            transformClassifier(before, after, beforeChange);
                        }
                    }
                }
                break;
            }
            case ChangeUtils.CT_DELETE: {
                if (isValidEvent(before)) {
                    if (after instanceof IEnumeration && !(before instanceof IEnumeration)) {
                        transformClassifier(before, after, beforeChange);
                    } else {
                        ClassInfo clazz = new ClassInfo(before);
                        fireClassDeletedEvent(clazz, beforeChange);
                    }
                }
                break;
            }
			case ChangeUtils.CT_MODIFY :
			{
				if (isValidEvent(before) && isValidEvent(after)) {
					if (newVal.getRequestDetailType() == ChangeUtils.RDT_TRANSFORM) {
						transform(newVal, beforeChange);
					} else if (newVal.getRequestDetailType() == ChangeUtils.RDT_FEATURE_DUPLICATED) {
						
						IElementDuplicatedChangeRequest duplicateRequest 
                                = (IElementDuplicatedChangeRequest) newVal;
                        
						IElement duplicatedElement 
                                    = duplicateRequest.getDuplicatedElement();
                        
						if (duplicatedElement instanceof IOperation){
                            IOperation duplicatedOperation 
							    = (IOperation) duplicatedElement;
							IOperation originalOperation 
							    = (IOperation) duplicateRequest.getOriginalElement();
                            
                            final MethodInfo originalMethod = 
                                new MethodInfo(null, originalOperation);
							final MethodInfo duplicatedMethod = 
                                new MethodInfo(null, duplicatedOperation);
							
                            
                            for (int i = 0; i < changeListeners.size(); ++i) {
								final IOperationChangeListener listener
								    = (IOperationChangeListener) changeListeners
										.elementAt(i);
								RoundtripThread r = new RoundtripThread() {
									public void work() {
										setDefaultProject(duplicatedMethod); 
										listener
                                        .operationDuplicated(originalMethod, 
                                                             duplicatedMethod,
															 beforeChange);
									}
								};
								UMLSupport.getUMLSupport()
										.getRoundtripQueue().queueRunnable(
												r);
							}
						} else if (duplicatedElement instanceof IAttribute) {
							IAttribute dupAttr = (IAttribute) duplicatedElement;
							final MemberInfo originalMember = new MemberInfo(
									null, (IAttribute) duplicateRequest
											.getOriginalElement());
                            
							final MemberInfo duplicatedMember = new MemberInfo(
									null, dupAttr);
                            
							final ClassInfo oldClazz = originalMember.getContainingClass();
                            final ClassInfo newClazz = duplicatedMember.getContainingClass();
                             
							for (int i = 0; i < changeListeners.size(); ++i) {
								final IAttributeChangeListener listener = 
                                    (IAttributeChangeListener) changeListeners
										.elementAt(i);
								RoundtripThread r = new RoundtripThread() {
									public void work() {
										setDefaultProject(duplicatedMember);
										listener.attributeDuplicated(
												originalMember,
												duplicatedMember,
												beforeChange);
									}
								};
								UMLSupport.getUMLSupport()
										.getRoundtripQueue().queueRunnable(
												r);
							}
						}
					} else {
						ClassInfo oldC = new ClassInfo(before), newC = new ClassInfo(
								after);
						// Need special handling for relationships
						if (reqType == ChangeUtils.RDT_RELATION_VALIDATE
								|| reqType == ChangeUtils.RDT_RELATION_MODIFIED
								|| reqType == ChangeUtils.RDT_RELATION_DELETED) {
							IRelationProxy rel = newVal.getRelation();
							IClassifier cl = (IClassifier) rel.getFrom();
							oldC = new ClassInfo(cl);
							newC = new ClassInfo(cl);
							addRelationshipInfo(oldC, newC, reqType, rel);
						}
						if (reqType == ChangeUtils.RDT_DEPENDENCY_ADDED) {
							if (newVal instanceof IDependencyChangeRequest) {
								Log.out("Its a dependency change request");
								IDependencyChangeRequest depReq = (IDependencyChangeRequest) newVal;
								handleDependencyAdded(depReq, newC,
										beforeChange);
							}
						}
						fireClassChangedEvent(oldC, newC, beforeChange);
					}
				}
                break;
            }
        }
    }

    protected void handleDependencyAdded(IDependencyChangeRequest depReq, ClassInfo newC, boolean beforeChange)
    {
      IElement ele = depReq.getIndependentElement();
      if (ele instanceof IPackage){
        IPackage pkg = (IPackage)  ele;
        String str = JavaClassUtils.getFullyQualifiedName(pkg);
        Log.out("Got the package name as " + str);
        newC.addImport(str + ".*");
      }
    }

    protected void transform(IChangeRequest newVal, boolean beforeChange){
      if (newVal instanceof IClassTransformChangeRequest){
        Log.out("Its a class transform change request");
        IClassTransformChangeRequest chgReq = (IClassTransformChangeRequest)  newVal;
        IElement beforeE = chgReq.getBefore();
        IElement afterE = chgReq.getAfter();
        if (!(beforeE instanceof IClass || beforeE instanceof IInterface) ||
            !(afterE instanceof IClass || afterE instanceof IInterface)) {
            transformClassifier((IClassifier) beforeE, (IClassifier) afterE, beforeChange);
        }
        Log.out("Before element type name = " + beforeE.getElementType() );
        Log.out("After element type name = " + afterE.getElementType() );
        ClassInfo before=null, after=null;
        if (beforeE instanceof IClass){
          IClass beforeClass = (IClass)  beforeE;
          ETList<IGeneralization> gens = beforeClass.getGeneralizations();
          ETList<IGeneralization> specs = beforeClass.getSpecializations();
          ETList<IImplementation> imps = beforeClass.getImplementations();
          IInterface afterInt = (IInterface) afterE;
          if (gens != null &&  gens.getCount() > 0 ){
            Log.out("This class has associated implementation classes, not transforming");
            return;
          }
          if ( imps != null && imps.getCount() > 0){
             Log.out("This class has associated implementation classes, not transforming");
             return;
          }
          if (specs != null && specs.getCount() > 0 ){
            Log.out("This class has associated implementation classes, not transforming");
            return;
          }

          before = new ClassInfo(beforeClass);
          after = new ClassInfo(afterInt);
//          transformClass(beforeClass, afterInt);
        }
        else if (afterE instanceof IClass){
          IClass afterClass = (IClass)  afterE;
          IInterface beforeInt = (IInterface) beforeE;
          ETList<IImplementation> imps = beforeInt.getImplementations();
          ETList<IGeneralization> gens = beforeInt.getGeneralizations();
          ETList<IGeneralization> specs = beforeInt.getSpecializations();
          //Need to somehow get the implementing classes info and exclude that case too.
          if (gens != null &&  gens.getCount() > 0 ){
            Log.out("This class has associated implementation classes, not transforming");
            return;
          }
          if ( imps != null && imps.getCount() > 0){
             Log.out("This class has associated implementation classes, not transforming");
             return;
          }
          if (specs != null && specs.getCount() > 0 ){
            Log.out("This class has associated implementation classes, not transforming");
            return;
          }

          before = new ClassInfo(beforeInt);
          after = new ClassInfo(afterClass);
//          transformInterface(beforeInt, afterClass);
        }
        if (before != null && after != null){
          before.setMethodsAndMembers((IClassifier) beforeE);
		  before.setComment(beforeE.getDocumentation());
          after.setMethodsAndMembers((IClassifier) afterE);
		  after.setComment(afterE.getDocumentation());
          Log.out("Calling transform function");
          fireClassTransformEvent(before, after, beforeChange);
        }

      }
      return;
    }

    protected void transformClassifier(IClassifier before, IClassifier after, boolean beforeChange) {
        ClassInfo beforeInfo = new ClassInfo(before);
        ClassInfo afterInfo = new ClassInfo(after);
        afterInfo.setMethodsAndMembers(after);
        beforeInfo.setMethodsAndMembers(before);
		beforeInfo.setComment(before.getDocumentation());
		afterInfo.setComment(after.getDocumentation());
        fireClassTransformEvent(beforeInfo, afterInfo, beforeChange);
    }
        
    protected void addRelationshipInfo(ClassInfo oldC, ClassInfo newC,
                                       int reqType, IRelationProxy rel) {
        INamedElement to = (INamedElement)  rel.getTo();
        String toClass = JavaClassUtils.getFullyQualifiedName(to);
        String pack     = JavaClassUtils.getPackageName(toClass),
               clname   = JavaClassUtils.getFullInnerClassName(toClass);

        String relationType = rel.getConnectionElementType();
        if (relationType.equals(ChangeUtils.REL_GENER) && !oldC.isInterface()) {
            newC.setExtendedClass(pack, clname);
            if (reqType == ChangeUtils.RDT_RELATION_DELETED)
                newC.setExtendedClass(null, null);
        } else if (relationType.equals(ChangeUtils.REL_IMPL) ||
                    (oldC.isInterface() &&
                        relationType.equals(ChangeUtils.REL_GENER))) {
            switch (reqType) {
                case ChangeUtils.RDT_RELATION_VALIDATE:
                    Log.out("Superinterface is " + toClass);
                    newC.addInterface(pack, clname);
                    break;
                case ChangeUtils.RDT_RELATION_DELETED:
                    newC.removeInterface(pack, clname);
                    break;
            }
        }
    }

    protected void fireClassAddedEvent(final ClassInfo clazz, final boolean before) {
        if (!isValidEvent(clazz, clazz.getOuterClass())
                || clazz.isReferenceClass())
            return ;
        Log.out("Class added (M->S): " + clazz);
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IClassChangeListener listener = (IClassChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(clazz);
                    listener.classAdded(clazz, before);
                }
            };
            queue(r);
        }
    }

    protected void fireClassDeletedEvent(final ClassInfo clazz,
                                         final boolean before) {
        if (!isValidEvent(clazz, clazz.getOuterClass())
                || clazz.isReferenceClass())
            return ;

        Log.out("Class deleted (M->S): " + clazz);
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IClassChangeListener listener = (IClassChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(clazz);
                    listener.classDeleted(clazz, before);
                }
            };
            queue(r);
        }
    }

    protected void fireClassTransformEvent(final ClassInfo oldC,
                                           final ClassInfo newC,
                                           final boolean before) {
        if (!isValidEvent(oldC, oldC.getOuterClass())
                || oldC.isReferenceClass() || newC.isReferenceClass())
            return ;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IClassChangeListener listener = (IClassChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(newC);
                    listener.classTransformed(oldC, newC, before);
                }
            };
            queue(r);
        }
    }

    protected void fireBulkDeleteEvent(final Vector classesToBeDeleted,
                                       final String packageToBeDeleted,
                                       final IProject affectedProject) {
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IClassChangeListener listener = (IClassChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    UMLSupport.setDefaultProject(affectedProject);
                    listener.classesDeleted(classesToBeDeleted, 
                                            packageToBeDeleted);
                }
            };
            queue(r);
        }
    }

    protected void fireClassChangedEvent(final ClassInfo oldC,
                                         final ClassInfo newC,
                                         final boolean before) {
        if (!isValidEvent(oldC, oldC.getOuterClass())
                || oldC.isReferenceClass() || newC.isReferenceClass())
            return ;

        Log.out("Class changed (M->S) from " + oldC + " to " + newC);
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IClassChangeListener listener = (IClassChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(newC);
                    listener.classChanged(oldC, newC, before);
                }
            };
            queue(r);
        }
    }

    public static void addClassChangeListener(IClassChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener))
            changeListeners.add(listener);
    }

    public static void removeClassChangeListener(IClassChangeListener listener) {
        if (listener != null)
            changeListeners.remove(listener);
    }

    private static Vector changeListeners = new Vector();
}
