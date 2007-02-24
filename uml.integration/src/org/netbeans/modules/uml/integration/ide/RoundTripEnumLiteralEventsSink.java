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

package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
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
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumLiteralEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.integration.ide.events.LiteralInfo;
import org.netbeans.modules.uml.integration.ide.listeners.IEnumLiteralChangeListener;

/**
 * @author  Daniel Prusa
 * @version 1.0
 */
public class RoundTripEnumLiteralEventsSink extends RoundTripSource implements IRoundTripEnumLiteralEventsSink {

    public void onPreEnumLiteralChangeRequest(IChangeRequest newVal, IResultCell cell) {
    }
    
    public void onEnumLiteralChangeRequest(IChangeRequest newVal, IResultCell cell) {
        try {
            //ChangeUtils.say(newVal);
            fireLiteralChangeEvent(newVal, false);
        } catch (Exception e) {
            Log.stackTrace(e);
        }
    }
    
    protected void fireLiteralChangeEvent(IChangeRequest newVal, boolean beforeChange) {
        // Get change type
        int changeType = newVal.getState();

        IEnumerationLiteral before = null, after = null;
        
        try {
            before = (IEnumerationLiteral) newVal.getBefore();
            after = (IEnumerationLiteral) newVal.getAfter();
        } catch (ClassCastException e) {
            Log.stackTrace(e);
        }

        switch (changeType) {
            case ChangeUtils.CT_CREATE:
            {
                if (isValidEvent(after)) {
                    LiteralInfo lit = new LiteralInfo(after);
                    fireEnumLiteralAddedEvent(lit, beforeChange);
                    scheduleForNavigation(after);
                }
                break;
            }
            case ChangeUtils.CT_DELETE:
            {
                if (isValidEvent(before)) {
                    LiteralInfo lit = new LiteralInfo(before);
                    fireEnumLiteralDeletedEvent(lit, beforeChange);
                }
                break;
            }
            case ChangeUtils.CT_MODIFY:
            {
                if (isValidEvent(before) && isValidEvent(after)) {
                    LiteralInfo oldC = new LiteralInfo(before),
                                newC = new LiteralInfo(after);
                    if (oldC.getContainingClass() == null)
                        oldC.setContainingClass(newC.getContainingClass());
                    fireEnumLiteralChangedEvent(oldC, newC, beforeChange);
                }
                break;
            }
        }
    }

    protected void fireEnumLiteralAddedEvent(final LiteralInfo lit, final boolean before) {
        if (!isValidEvent(lit, lit.getContainingClass()))
            return;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IEnumLiteralChangeListener listener = (IEnumLiteralChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(lit);
                    listener.enumLiteralAdded(lit, before);
                }
            };
            queue(r);
        }
    }

    protected void fireEnumLiteralDeletedEvent(final LiteralInfo lit, final boolean before) {
        if (!isValidEvent(lit, lit.getContainingClass()))
            return;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IEnumLiteralChangeListener listener = (IEnumLiteralChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(lit);
                    listener.enumLiteralDeleted(lit, before);
                }
            };
            queue(r);
        }
    }

    protected void fireEnumLiteralChangedEvent(final LiteralInfo oldF, final LiteralInfo newF, final boolean before) {
        if (!isValidEvent(oldF, oldF.getContainingClass()))
            return;

        for (int i = 0; i < changeListeners.size(); ++i) {
            final IEnumLiteralChangeListener listener = (IEnumLiteralChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(newF);
                    listener.enumLiteralChanged(oldF, newF, before);
                }
            };
            queue(r);
        }
    }

    public static void addEnumLiteralChangeListener(IEnumLiteralChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener))
            changeListeners.add(listener);
    }

    public static void removeEnumLiteralChangeListener(IEnumLiteralChangeListener listener) {
        if (listener != null)
            changeListeners.remove(listener);
    }

    private static Vector changeListeners = new Vector();
}