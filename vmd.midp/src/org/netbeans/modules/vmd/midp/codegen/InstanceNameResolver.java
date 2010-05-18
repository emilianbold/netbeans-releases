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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.codegen;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.ValidatorPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.HashSet;
import java.util.Collection;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class InstanceNameResolver {

    private InstanceNameResolver () {
    }

    public static PropertyValue createFromSuggested (DesignComponent component, String suggestedMainName) {
        return createFromSuggested (component, suggestedMainName, null);
    }

    public static PropertyValue createFromSuggested (DesignComponent component, String suggestedMainName, Collection<String> additionalReservedNames) {
        assert component.getDocument ().getTransactionManager ().isAccess ();
        Collection<? extends CodeNamePresenter> presenters = component.getPresenters (CodeNamePresenter.class);
        if (presenters.isEmpty ())
            Debug.warning ("CodeNamePresenter is missing for", component); // NOI18N
        HashSet<String> names = new HashSet<String> ();
        gatherNames (component.getDocument ().getRootComponent (), component, names);
        if (additionalReservedNames != null)
            names.addAll (additionalReservedNames);

        suggestedMainName = checkForJavaIdentifierCompliant (suggestedMainName);
        if (checkIfNameAlreadyReserved (presenters, suggestedMainName, names)) {
            int index = suggestedMainName.length ();
            while (index >= 1  &&  Character.isDigit (suggestedMainName.charAt (index - 1)))
                index --;
            int number = index < suggestedMainName.length () ? Integer.parseInt (suggestedMainName.substring (index)) : 1;
            suggestedMainName = suggestedMainName.substring (0, index);

            suggestedMainName = findNumberedInstanceName (presenters, suggestedMainName, number, names);
        }
        return MidpTypes.createStringValue (suggestedMainName);
    }

    private static String findNumberedInstanceName (Collection<? extends CodeNamePresenter> presenters, String suggestedMainName, int number, HashSet<String> names) {
        for (; ; number ++) {
            String testName = suggestedMainName + number;
            if (! checkIfNameAlreadyReserved (presenters, testName, names))
                return testName;
        }
    }

    private static boolean checkIfNameAlreadyReserved (Collection<? extends CodeNamePresenter> presenters, String suggestedMainName, HashSet<String> names) {
        for (CodeNamePresenter presenter : presenters) {
            List<String> reservedNamesFor = presenter.getReservedNamesFor (suggestedMainName);
            if (reservedNamesFor != null)
                for (String name : reservedNamesFor) {
                    if (names.contains (name))
                        return true;
                }
        }
        return false;
    }

    private static String checkForJavaIdentifierCompliant (String instanceName) {
        if (instanceName == null  ||  instanceName.length () < 1)
            return "object"; // NOI18N
        StringBuffer buffer = new StringBuffer ();
        int index = 0;
        if (Character.isJavaIdentifierStart (instanceName.charAt (0))) {
            buffer.append (instanceName.charAt (0));
            index ++;
        } else {
            buffer.append ('a'); // NOI18N
        }
        while (index < instanceName.length ()) {
            char c = instanceName.charAt (index);
            if (Character.isJavaIdentifierPart (c))
                buffer.append (c);
            index ++;
        }
        return buffer.toString ();
    }

    private static void gatherNames (DesignComponent component, DesignComponent excludeComponent, HashSet<String> names) {
        assert component.getDocument ().getTransactionManager ().isAccess ();
        if (component == excludeComponent)
            return;
        Collection<? extends CodeNamePresenter> presenters = component.getPresenters (CodeNamePresenter.class);
        for (CodeNamePresenter presenter : presenters) {
            List<String> reservedNames = presenter.getReservedNames ();
            if (reservedNames != null)
                names.addAll (reservedNames);
        }
        for (DesignComponent child : component.getComponents ())
            gatherNames (child, excludeComponent, names);
    }

    public static Presenter createValidatorPresenter () {
        return new ValidatorPresenter() {
            @Override
            protected void checkCustomValidity () {
                InstanceNameResolver.checkValidity (getComponent ().getDocument ());
            }
        };
    }

    private static void checkValidity (DesignDocument document) {
    //XXX: build hotfix
//        checkValidity (document.getRootComponent (), new HashSet<String> (), new HashSet<String> ());
    }

}
