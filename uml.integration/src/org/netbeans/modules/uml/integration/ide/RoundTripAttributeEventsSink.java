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
 * File         : RoundTripAttributeEventsSink.java
 * Version      : 1.2
 * Description  : Listens for attribute changes in the Describe model
 * Author       : Ashish
 */
package org.netbeans.modules.uml.integration.ide;

import java.util.Vector;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.listeners.IAttributeChangeListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.roundtripframework.IAssociationEndTransformChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IAttributeTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 *  Listens for attribute changes in the Describe model.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Added debug log statements.
 *   2  2002-04-25  Darshan     Added support for IDE integrations to add
 *                              secondary listeners to this.
 *   3  2002-04-26  Darshan     Removed diagnostic messages from second
 *                              (post-change) event functions. Also added
 *                              calls to fire attribute change events to the
 *                              secondary (IDE) listener.
 *   4  2002-05-03  Darshan     Removed the 'Unnamed' hack that attempted to
 *                              recognize spurious create attribute events - the
 *                              hack wasn't working anyway.
 *   5  2002-06-22  Darshan     Replaced use of IAttribute with
 *                              IStructuralFeature, included Sumitabh's fix for
 *                              the NullPE when dragging navigable ends.
 *   6  2002-08-06  Mukta       Added code to handle IAttributeTypeChangeRequest
 *
 * @author  Ashish
 * @version 1.0
 */
public class RoundTripAttributeEventsSink extends RoundTripSource
                        implements IRoundTripAttributeEventsSink {

    public void onPreAttributeChangeRequest(IChangeRequest newVal,
                                            IResultCell cell) {
    }

    public void onAttributeChangeRequest(IChangeRequest newVal,
                                         IResultCell cell) {

        try {
            //ChangeUtils.say(newVal);
            fireAttributeChangeEvent(newVal, false);
        } catch (Exception e) {
            Log.stackTrace(e);
        }
    }

    protected void fireAttributeChangeEvent(IChangeRequest newVal,
                                            boolean beforeChange) {
        // Get change type
        int changeType = newVal.getState();

        IStructuralFeature before = null, after = null;
        
        try {
            before = (IStructuralFeature) newVal.getBefore();
            after = (IStructuralFeature) newVal.getAfter();
        } catch (ClassCastException e) {
            Log.stackTrace(e);
        }

        switch (changeType) {
            case ChangeUtils.CT_CREATE:
            {
                if (isValidEvent(after)) {
                    MemberInfo clazz = new MemberInfo(after);
                    fireAttributeAddedEvent(clazz, beforeChange);
                    scheduleForNavigation(after);
                }
                break;
            }
            case ChangeUtils.CT_DELETE:
            {
                if (isValidEvent(before)) {
                    if (newVal.getRequestDetailType() == ChangeUtils.RDT_TRANSFORM){
                        handleAssociationTransform(newVal, false);
                    }
                    else{
                        MemberInfo clazz = new MemberInfo(before);
                        fireAttributeDeletedEvent(clazz, beforeChange);
                    }
                }
                break;
            }
            case ChangeUtils.CT_MODIFY:
            {
                if (isValidEvent(before) && isValidEvent(after)) {
                    MemberInfo oldC = new MemberInfo(before),
                               newC = new MemberInfo(after);
                    if (oldC.getContainingClass() == null)
                        oldC.setContainingClass(newC.getContainingClass());
                    if(newVal instanceof IAttributeTypeChangeRequest){
                        IAttributeTypeChangeRequest req = (IAttributeTypeChangeRequest)  newVal;
                        IAttribute impactedAttribute = req.getImpactedAttribute();
                        oldC = new MemberInfo(impactedAttribute);
                        newC = new MemberInfo(impactedAttribute);
                        if(oldC.getType().equals(req.getNewTypeName()))
                            oldC.setType(req.getOldTypeName(), oldC.getInitializer());
                    }
                    fireAttributeChangedEvent(oldC, newC, beforeChange);
                }
                break;
            }
        }
    }

     protected void handleAssociationTransform(IChangeRequest newVal,
                                         boolean before) {
      if (newVal instanceof IAssociationEndTransformChangeRequest)
      {
        IAssociationEndTransformChangeRequest req = (IAssociationEndTransformChangeRequest)  newVal;
        IElement bef = req.getBefore();
        IClassifier befCl = req.getOldReferencingClassifier();
        INavigableEnd befAttr = null;
        if (bef instanceof INavigableEnd ) {
            befAttr = (INavigableEnd)  bef;
            //ClassInfo beforeClass = new ClassInfo(befCl);
            ClassInfo beforeClass = ClassInfo.getRefClassInfo(befCl, true);
            MemberInfo beforeMember = new MemberInfo(beforeClass, befAttr);
            fireAttributeDeletedEvent(beforeMember, before);
        }
      }
    }


    protected void fireAttributeAddedEvent(final MemberInfo field,
                                           final boolean before) {
        if (!isValidEvent(field, field.getContainingClass()))
            return;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IAttributeChangeListener listener = (IAttributeChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(field);
                    listener.attributeAdded(field, before);
                }
            };
            queue(r);
        }
    }

    protected void fireAttributeDeletedEvent(final MemberInfo field,
                                             final boolean before) {

        if (!isValidEvent(field, field.getContainingClass()))
            return;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IAttributeChangeListener listener = (IAttributeChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(field);
                    listener.attributeDeleted(field, before);
                }
            };
            queue(r);
        }
    }

    protected void fireAttributeChangedEvent(final MemberInfo oldF,
                                             final MemberInfo newF,
                                             final boolean before) {

        if (!isValidEvent(oldF, oldF.getContainingClass()))
            return;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IAttributeChangeListener listener = (IAttributeChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(newF);
                    listener.attributeChanged(oldF, newF, before);
                }
            };
            queue(r);
        }
    }

    public static void addAttributeChangeListener(IAttributeChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener))
            changeListeners.add(listener);
    }

    public static void removeAttributeChangeListener(IAttributeChangeListener listener) {
        if (listener != null)
            changeListeners.remove(listener);
    }

    private static Vector changeListeners = new Vector();
}