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
