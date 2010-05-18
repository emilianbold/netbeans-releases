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
 * File         : RoundTripRelationEventsSink.java
 * Version      : 1.2
 * Description  : Listener for Describe relationship change events
 * Authors      : Ashish
 */
package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;


/**
 *  Listens for relationship change events on the Describe model. It extends
 * from the class events sink because it fires class change events.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Added file and class comments.
 *   2  2002-05-30  Darshan     Added support for generalization and
 *                              implementation change events.
 *   3  2002-06-05  Darshan     Modified to allow handling of generalizations
 *                              between two interfaces.
 *   4  2002-06-18  Sumitabh    Modified to handle relationship create events.
 *   5  2002-06-19  Darshan     Included support for relationship delete events.
 *   6  2002-06-26  Darshan     Incorporated Sumitabh'schanges to handle
 *                              association moves.
 *
 * @author  Ashish
 * @version 1.2
 */
public class RoundTripRelationEventsSink extends RoundTripClassEventsSink
                implements IRoundTripRelationEventsSink {

    private RoundTripAttributeEventsSink attrSink =
                new RoundTripAttributeEventsSink();

    public RoundTripRelationEventsSink() {
    }

    public void onPreRelationChangeRequest(IChangeRequest newVal,
                                           IResultCell cell) {
    }

    public void onRelationChangeRequest(IChangeRequest newVal,
                                        IResultCell cell) {
        Log.out( "onRelationChangeRequest called" );

        int type = newVal.getRequestDetailType();

        Log.out("Got request detail type " + type);
        if (!isValidEventType(type)) {
            Log.out("Request detail type " + type + " is blocked, abandoning");
            return;
        }

        if (type == ChangeUtils.RDT_RELATION_CREATED) {
            if (newVal instanceof IGeneralizationChangeRequest)
                createGeneralization(newVal, false);
            else if (newVal instanceof IImplementationChangeRequest)
                createImplementation(newVal, false);
        } else if (type == ChangeUtils.RDT_ASSOCIATION_END_MODIFIED) {
            Log.out("Handling the association move event");
            handleAssociationMove(newVal, false);
        } else {
            if (newVal instanceof IGeneralizationChangeRequest)
                handleGeneralization(newVal, false);
            else if (newVal instanceof IImplementationChangeRequest)
                handleImplementation(newVal, false);
        }
    }

    protected void handleAssociationMove(IChangeRequest newVal,
                                         boolean before) {
        IElement bef = newVal.getBefore();
        IElement aft = newVal.getAfter();
        IClassifier befCl = null;
        IClassifier aftCl = null;
        INavigableEnd befAttr = null;
        INavigableEnd aftAttr = null;

        if (bef instanceof IAssociationEnd ) {
            IAssociationEnd befEnd = (IAssociationEnd)  bef;
            befCl = befEnd.getParticipant();
            befAttr = (INavigableEnd)  befEnd.getOtherEnd2();
        }
        if (aft instanceof IAssociationEnd ) {
            IAssociationEnd aftEnd = (IAssociationEnd)  aft;
            aftCl = aftEnd.getParticipant();
            aftAttr = (INavigableEnd)  aftEnd.getOtherEnd2();
        }

        if(!isValidEvent(befCl) || !isValidEvent(befAttr) || !isValidEvent(aftCl)
                                                || !isValidEvent(aftAttr))
            return;

        ClassInfo beforeClass = new ClassInfo(befCl);
        //ClassInfo afterClass = new ClassInfo(aftCl);

        MemberInfo beforeMember = new MemberInfo(beforeClass, aftAttr);
        MemberInfo afterMember = new MemberInfo(aftAttr);

        attrSink.fireAttributeDeletedEvent(beforeMember, before);
        attrSink.fireAttributeAddedEvent(afterMember, before);
    }

    protected void handleGeneralization(IChangeRequest req, boolean before) {
        int type = req.getRequestDetailType();

        IGeneralizationChangeRequest gcr =
                        (IGeneralizationChangeRequest)  req;
        IClassifier oldG = gcr.getBeforeGeneralizing();
        IClassifier newG = gcr.getAfterGeneralizing();
        IClassifier oldS = gcr.getBeforeSpecializing();
        IClassifier newS = gcr.getAfterSpecializing();

        if (newS == null || oldS == null)
            return ;

        if(!isValidEvent(oldG) || !isValidEvent(newG) || !isValidEvent(oldS)
                                                        || !isValidEvent(newS))
            return;
        if (oldG instanceof IInterface &&
                oldS instanceof IInterface) {
            handleImplementation(req, before);
            return ;
        }

        ClassInfo newC = new ClassInfo(newS);

        boolean oldNewSame = oldS.getXMIID().equals(newS.getXMIID());

        if (oldNewSame && type == ChangeUtils.RDT_RELATION_DELETED) {
            Log.out("Deleting generalization!");
            newC.setExtendedClass(null, null);
        }

        // Passing newC as both old and new parameters is fully intentional -
        // the IDE integration should/will only look at the qualified name from
        // the old element, and then change everything in the source code to
        // resemble the new element. Since this is a generalization change, the
        // qualified name will be the same for both old and new elements and
        // we can use the same object for both. QED.
        fireClassChangedEvent(newC, newC, before);

        if (!oldNewSame) {
            // The subclass was changed
            ClassInfo oldC = new ClassInfo(oldS);
            fireClassChangedEvent(oldC, oldC, before);
        }
    }

    protected void createGeneralization(IChangeRequest req, boolean before) {
        IGeneralizationChangeRequest gcr =
                        (IGeneralizationChangeRequest)  req;
        IClassifier newG = gcr.getAfterGeneralizing();
        IClassifier newS = gcr.getAfterSpecializing();

        if (newS == null || newG == null)
            return ;

        if(!isValidEvent(newG) || !isValidEvent(newS))
            return;

        Log.out("New superclass");
        ChangeUtils.sayElement(newG);
        Log.out("New subclass");
        ChangeUtils.sayElement(newS);


        ClassInfo newC = new ClassInfo(newS);

        // Passing newC as both old and new parameters is fully intentional -
        // the IDE integration should/will only look at the qualified name from
        // the old element, and then change everything in the source code to
        // resemble the new element. Since this is a generalization change, the
        // qualified name will be the same for both old and new elements and
        // we can use the same object for both. QED.
        fireClassChangedEvent(newC, newC, before);

    }

    protected void handleImplementation(IChangeRequest req, boolean before) {
        int type = req.getRequestDetailType();
        IClassifier beforeC = null,
                    afterC  = null,
                    beforeI = null,
                    afterI  = null;

        if (req instanceof IGeneralizationChangeRequest) {
            IGeneralizationChangeRequest gcr =
                    (IGeneralizationChangeRequest)  req;
            beforeI = gcr.getBeforeGeneralizing();
            afterI  = gcr.getAfterGeneralizing();
            beforeC = gcr.getBeforeSpecializing();
            afterC  = gcr.getAfterSpecializing();
        } else {
            IImplementationChangeRequest icr =
                    (IImplementationChangeRequest)  req;
            beforeC = icr.getBeforeImplementing();
            afterC  = icr.getAfterImplementing();
            beforeI = icr.getBeforeInterface();
            afterI  = icr.getAfterInterface();
        }

        if (beforeC == null || afterC == null || beforeI == null
                            || afterI == null)
            return ;

        if(!isValidEvent(beforeC) || !isValidEvent(afterC)
                            || !isValidEvent(beforeI) || !isValidEvent(afterI))
            return;

        String oldInterface = JavaClassUtils.getFullyQualifiedName(beforeI),
               newInterface = JavaClassUtils.getFullyQualifiedName(afterI);

        if (beforeC.getXMIID().equals(afterC.getXMIID())) {
            // The implemented interface has been changed
            ClassInfo c = new ClassInfo(afterC);
            c.removeInterface(oldInterface);
            c.addInterface(newInterface);

            if (type == ChangeUtils.RDT_RELATION_DELETED)
                c.removeInterface(newInterface);

            fireClassChangedEvent(c, c, before);
        } else if (type != ChangeUtils.RDT_RELATION_DELETED) {
            // The class that implements the interface has been changed.
            ClassInfo oldC = new ClassInfo(beforeC),
                      newC = new ClassInfo(afterC);
            oldC.removeInterface(oldInterface);
            newC.addInterface(newInterface);
            fireClassChangedEvent(oldC, oldC, before);
            fireClassChangedEvent(newC, newC, before);
        }
    }

    protected void createImplementation(IChangeRequest req, boolean before) {
        IClassifier afterC  = null,
                    afterI  = null;

        IImplementationChangeRequest icr =
                (IImplementationChangeRequest)  req;
        afterC  = icr.getAfterImplementing();
        afterI  = icr.getAfterInterface();

        if (afterC == null || afterI == null)
            return ;

        if(!isValidEvent(afterC) || !isValidEvent(afterI))
            return;

        String newInterface = JavaClassUtils.getFullyQualifiedName(afterI);

        ClassInfo c = new ClassInfo(afterC);
        c.addInterface(newInterface);

        fireClassChangedEvent(c, c, before);
    }
}

