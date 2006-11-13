/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
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

package org.netbeans.modules.j2ee.common.queries.api;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Ask whether it is possible to use dependency injection in some class
 * @author Martin Adamek, Milan Kuchtiak
 */
public class InjectionTargetQuery {
    
    private static Lookup.Result<InjectionTargetQueryImplementation> implementations;
    /** Cache of all available InjectionTargetQueryImplementation instances. */
    private static List<InjectionTargetQueryImplementation> cache;

    private InjectionTargetQuery() {
    }
    
    /**
     * Decide if dependency injection can be used in given class
     * @param controller CompilationController related to JavaSource
     * @param typeElement class where annotated field or method should be inserted,
     * if null is provided, main public class from file is taken
     * @return true if any container or environment is able to inject resources in given class, false otherwise
     */
    public static boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (typeElement == null || controller==null) {
            throw new NullPointerException("Passed null to InjectionTargetQuery.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        for (InjectionTargetQueryImplementation elem : getInstances()) {
            if (elem.isInjectionTarget(controller, typeElement)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Decide if injected reference must be static in given class. 
     * For example, in application client injection can be used only in class with main method and all
     * injected fields must be static<br>
     * Implementation 
     * @param controller CompilationController related to JavaSource
     * @param typeElement class where annotated field or method should be inserted,rted,
     * if null is provided, main public class from file is taken
     * @return true if static reference is required in given class, false otherwise
     */
    public static boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        if (typeElement == null || controller==null) {
            throw new NullPointerException("Passed null to InjectionTargetQuery.isStaticReferenceRequired(CompilationController, TypeElement)"); // NOI18N
        }
        for (InjectionTargetQueryImplementation elem : getInstances()) {
            if (elem.isStaticReferenceRequired(controller, typeElement)) {
                return true;
            }
        }
        return false;
    }
    
    private static synchronized List<InjectionTargetQueryImplementation> getInstances() {
        if (implementations == null) {
            implementations = Lookup.getDefault().lookup(new Lookup.Template<InjectionTargetQueryImplementation>(InjectionTargetQueryImplementation.class));
            implementations.addLookupListener(new LookupListener() {
                public void resultChanged (LookupEvent ev) {
                    synchronized (InjectionTargetQuery.class) {
                        cache = null;
                    }
                }});
        }
        if (cache == null) {
            cache = new ArrayList<InjectionTargetQueryImplementation>(implementations.allInstances());
        }
        return cache;
    }

}
