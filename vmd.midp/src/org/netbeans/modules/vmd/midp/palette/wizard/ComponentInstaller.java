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
 *
 */

package org.netbeans.modules.vmd.midp.palette.wizard;

import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.openide.ErrorManager;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.*;

/**
 * @author David Kaspar
 */
public class ComponentInstaller {

    public static void install () {
        // TODO
    }

    public static Map<String, ComponentDescriptor> search (Project project) {
        ClasspathInfo info = MidpProjectSupport.getClasspathInfo (project);
        if (info == null)
            return Collections.emptyMap ();
        final Set<ElementHandle<TypeElement>> allHandles = info.getClassIndex ().getDeclaredTypes ("", ClassIndex.NameKind.PREFIX, EnumSet.of (ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES)); // NOI18N
        final HashMap<String, ComponentDescriptor> registry = resolveRegistryMap (project);
        final HashMap<String, ComponentDescriptor> result = new HashMap<String, ComponentDescriptor> ();

        try {
            JavaSource.create (info).runUserActionTask (new CancellableTask<CompilationController>() {
                public void cancel () {
                }
                public void run (CompilationController parameter) throws Exception {
                    HashSet<TypeElement> elements = new HashSet<TypeElement> ();
                    for (ElementHandle<TypeElement> handle : allHandles) {
                        TypeElement element = handle.resolve (parameter);
                        if (element != null  &&  element.getKind () != ElementKind.CLASS)
                            continue;
                        elements.add (element);
                    }

                    for (;;) {
                        Iterator<TypeElement> iterator = elements.iterator ();
                        if (! iterator.hasNext ())
                            break;
                        TypeElement element = iterator.next ();
                        iterator.remove ();
                        search (element, elements, registry, result);
                    }
                }
            }, true);
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        }
        return result;
    }

    private static HashMap<String,ComponentDescriptor> resolveRegistryMap (Project project) {
        final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry (MidpDocumentSupport.PROJECT_TYPE_MIDP, ProjectUtils.getProjectID (project));
        final HashMap<String, ComponentDescriptor> registryMap = new HashMap<String, ComponentDescriptor> ();
        registry.readAccess (new Runnable() {
            public void run () {
                for (ComponentDescriptor descriptor : registry.getComponentDescriptors ()) {
                    TypeID thisType = descriptor.getTypeDescriptor ().getThisType ();
                    if (! registry.isInHierarchy (ClassCD.TYPEID, thisType))
                        continue;
                    String string = thisType.getString ();
                    if (! checkForJavaIdentifierCompliant (string))
                        continue;
                    registryMap.put (string, descriptor);
                }
            }
        });
        return registryMap;
    }

    private static ComponentDescriptor search (TypeElement element, Set<TypeElement> elements, Map<String, ComponentDescriptor> registry, Map<String, ComponentDescriptor> result) {
        if (element == null)
            return null;
        if (element.getKind () != ElementKind.CLASS)
            return null;

        String fqn = element.getQualifiedName ().toString ();

        ComponentDescriptor descriptor = registry.get (fqn);
        if (descriptor != null)
            return descriptor;
        descriptor = result.get (fqn);
        if (descriptor != null)
            return descriptor;
        // TODO - search and check whether it is deriving from any component which is already in the registry and derives from ClassCD
        return descriptor;
    }

    private static boolean checkForJavaIdentifierCompliant (String fqn) {
        if (fqn == null || fqn.length () < 1)
            return false;
        if (! Character.isJavaIdentifierStart (fqn.charAt (0)))
            return false;
        boolean dot = false;
        for (int index = 1; index < fqn.length (); index ++) {
            char c = fqn.charAt (index);
            if (Character.isJavaIdentifierPart (c)) {
                dot = false;
                continue;
            }
            if (c != '.')
                return false;
            if (dot)
                return false;
            dot = true;
        }
        return ! dot;
    }

}
