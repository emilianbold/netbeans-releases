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
package org.netbeans.modules.vmd.api.codegen;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.util.*;

/**
 * 
 */
public final class InitCodeGenerator {

    private static final int PRICE_OF_REQUIRED_PARAM = 3;
    private static final int PRICE_OF_DEFAULT_PARAM = -1;

    public static void generateInitializationCode (MultiGuardedSection section, DesignComponent component) {
        Collection<? extends CodeSetterPresenter> presenters = component.getPresenters (CodeSetterPresenter.class);
        HashMap<String,Parameter> parametersMap = new HashMap<String,Parameter> ();
        ArrayList<Setter> constructors = new ArrayList<Setter> ();
        ArrayList<Setter> setters = new ArrayList<Setter> ();
        HashSet<String> requiredToBeSet = new HashSet<String> ();

        parametersMap.put (Parameter.PARAM_INDEX, Parameter.INDEX);

        for (CodeSetterPresenter presenter : presenters) {
            for (Parameter parameter : presenter.getParameters ()) {
                String parameterName = parameter.getParameterName ();
                assert parameterName != null;
                Parameter currentParameter = parametersMap.get (parameterName);
                if (currentParameter != null) {
                    int currentPriority = currentParameter.getParameterPriority ();
                    int priority = parameter.getParameterPriority ();
                    if (currentPriority >= priority) {
                        if (currentPriority == priority)
                            Debug.warning ("Duplicate parameter found", component, parameterName); // NOI18N
                        continue;
                    }
                    requiredToBeSet.remove (parameterName);
                }
                parametersMap.put (parameterName, parameter);
                if (parameter.isRequiredToBeSet (component))
                    requiredToBeSet.add (parameterName);
            }
            for (Setter setter : presenter.getSetters ()) {
                if (! setter.getVersionable ().isAvailable (component.getDocument ()))
                    continue;
                if (setter.isConstructor ()) {
                    if (component.getType ().equals (setter.getConstructorRelatedTypeID ()))
                      constructors.add (setter);
                } else
                    setters.add (setter);
            }
        }

        HashSet<String> unusedParameters = new HashSet<String> (parametersMap.keySet ());

        for (Setter setter : constructors) {
            for (String parameter : setter.getParameters ()) {
                unusedParameters.remove (parameter);
                if (! parametersMap.containsKey (parameter))
                    Debug.warning ("No setter parameter found", component, setter.getSetterName (), parameter); // NOI18N
            }
        }
        for (Setter setter : setters) {
            for (String parameter : setter.getParameters ()) {
                unusedParameters.remove (parameter);
                if (! parametersMap.containsKey (parameter))
                    Debug.warning ("No setter parameter found", component, setter.getSetterName (), parameter); // NOI18N
            }
        }
        unusedParameters.remove (Parameter.PARAM_INDEX);
        if (! unusedParameters.isEmpty ())
            Debug.warning ("Unused parameters", component, unusedParameters); // NOI18N


        // TODO - static instance of SetterComparator
        Collections.sort (constructors, new SetterComparator ());

        Setter setter = findSetter (constructors, requiredToBeSet, false);
        if (setter == null) {
//            Debug.warning ("No constructor found", component); // NOI18N
        } else {
            do {
                for (String parameter : setter.getParameters ())
                    requiredToBeSet.remove (parameter);
                setter.generateSetterCode (section, component, parametersMap);
                setter = findSetter (setters, requiredToBeSet, true);
            } while (setter != null  &&  ! setter.getParameters ().isEmpty ());
        }
    }

    private static Setter findSetter (ArrayList<Setter> setters, HashSet<String> requiredToBeSet, boolean mustUseChangedProperty) {
        Setter bestSetter = null;
        int bestPrice = Integer.MIN_VALUE;

        for (Setter setter : setters) {
            boolean usesChangedProperty = false;
            int price = 0;

            for (String parameterName : setter.getParameters ()) {
                if (requiredToBeSet.contains (parameterName)) {
                    price += PRICE_OF_REQUIRED_PARAM;
                    usesChangedProperty = true;
                } else {
                    price += PRICE_OF_DEFAULT_PARAM;
                }
                price += setter.getPriority();
            }

            if (! mustUseChangedProperty  ||  usesChangedProperty) {
                if (price > bestPrice) {
                    bestSetter = setter;
                    bestPrice = price;
                }
            }
        }

        return bestSetter;
    }

    private static int compareStrings (String s1, String s2) {
        if (s1 != null)
            return s2 != null ? s1.compareTo (s2) : 1;
        else
            return s2 != null ? -1 : 0;
    }

    // TODO - setter name for constructors have to be "null" - setting constructors as preferred
    private static class SetterComparator implements Comparator<Setter> {

        public int compare (Setter setter1, Setter setter2) {
            List<String> params1 = setter1.getParameters ();
            List<String> params2 = setter2.getParameters ();
            int diff;
            diff = (params1 != null ? params1.size () : 0) - (params2 != null ? params2.size () : 0);
            if (diff != 0)
                return diff;
            diff = compareStrings (setter1.getSetterName (), setter2.getSetterName ());
            if (diff != 0)
                return diff;
            if (params1 != null && params2 != null)
                for (int a = 0; a < params1.size (); a++) {
                    diff = compareStrings (params1.get (a), params2.get (a));
                    if (diff != 0)
                        return diff;
                }
            return 0;
        }

    }

}
