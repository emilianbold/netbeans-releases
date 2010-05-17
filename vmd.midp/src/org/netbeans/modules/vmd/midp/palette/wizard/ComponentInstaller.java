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
package org.netbeans.modules.vmd.midp.palette.wizard;

import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.serialization.MidpPropertyPresenterSerializer;
import org.netbeans.modules.vmd.midp.serialization.MidpSetterPresenterSerializer;
import org.netbeans.modules.vmd.midp.serialization.MidpTypesConvertor;
import org.netbeans.modules.vmd.midp.serialization.MidpAddImportPresenterSerializer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class ComponentInstaller {

    public static void install (final Map<String,Item> allFoundComponents, final List<Item> componentsToInstall) {
        ComponentSerializationSupport.runUnderDescriptorRegistryWriteAccess (MidpDocumentSupport.PROJECT_TYPE_MIDP, new Runnable () {
            public void run () {
                installCore (allFoundComponents, componentsToInstall);
            }
        });
        ComponentSerializationSupport.refreshDescriptorRegistry (MidpDocumentSupport.PROJECT_TYPE_MIDP);
    }

    private static void installCore (Map<String,Item> allFoundComponents, List<Item> componentsToInstall) {
        HashMap<String,Item> toInstall = new HashMap<String,Item> ();
        for (Item item : componentsToInstall)
            recursiveAdd (toInstall, allFoundComponents, item);
        for (Item item : toInstall.values ())
            ComponentSerializationSupport.serialize (MidpDocumentSupport.PROJECT_TYPE_MIDP, item.getTypeDescriptor (), item.getPaletteDescriptor (), item.getProperties (), item.getPresenters ());
    }

    private static void recursiveAdd (HashMap<String,Item> toInstall, Map<String,Item> allFoundComponents, Item item) {
        if (item == null)
            return;
        if (toInstall.containsKey (item.getFQN ()))
            return;
        toInstall.put (item.getFQN (), item);
        recursiveAdd (toInstall, allFoundComponents, allFoundComponents.get (item.getSuperFQN ()));
    }

    public static Map<String,Item> search (final Project project) {
        final Object[] ret = new Object[1];
        ComponentSerializationSupport.runUnderDescriptorRegistryReadAccess (MidpDocumentSupport.PROJECT_TYPE_MIDP, new Runnable () {
            public void run () {
                ret[0] = searchCore (project);
            }
        });
        return (Map<String, Item>) ret[0];
    }

    public static Map<String,Item> searchCore (Project project) {
        final ClasspathInfo info = MidpProjectSupport.getClasspathInfo (project);
        if (info == null)
            return Collections.emptyMap ();
        final SourceGroup sourceGroup = MidpProjectSupport.getSourceGroup (project);
        final Set<ElementHandle<TypeElement>> allHandles = info.getClassIndex ().getDeclaredTypes ("", ClassIndex.NameKind.PREFIX, EnumSet.of (ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES)); // NOI18N
        final Map<String, ComponentDescriptor> registry = resolveRegistryMap (project);
        final HashMap<String, Item> result = new HashMap<String, Item> ();

        try {
            JavaSource.create (info).runUserActionTask (new Task<CompilationController>() {

                public void run (CompilationController parameter) throws Exception {
                    HashSet<TypeElement> elements = new HashSet<TypeElement> ();
                    for (ElementHandle<TypeElement> handle : allHandles) {
                        TypeElement element = handle.resolve (parameter);
                        if (element != null  &&  element.getKind () == ElementKind.CLASS)
                            elements.add (element);
                    }

                    for (;;) {
                        Iterator<TypeElement> iterator = elements.iterator ();
                        if (! iterator.hasNext ())
                            break;
                        TypeElement element = iterator.next ();
                        search (element, elements, registry, info, sourceGroup, result);
                    }
                }
            }, true);
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        }
        return result;
    }

    private static Map<String,ComponentDescriptor> resolveRegistryMap (Project project) {
        final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry (MidpDocumentSupport.PROJECT_TYPE_MIDP, ProjectUtils.getProjectID (project));
        final HashMap<String, ComponentDescriptor> registryMap = new HashMap<String, ComponentDescriptor> ();

        registry.readAccess (new Runnable() {
            public void run () {
                for (ComponentDescriptor descriptor : registry.getComponentDescriptors ()) {
                    TypeID thisType = descriptor.getTypeDescriptor ().getThisType ();

                    String string = thisType.getString ();
                    if (! checkForJavaIdentifierCompliant (string))
                        continue;

                    if (! registry.isInHierarchy (ClassCD.TYPEID, thisType)  ||  ClassCD.TYPEID.equals (thisType))
                        continue;

                    registryMap.put (string, descriptor);
                }
            }
        });

        return registryMap;
    }

    private static boolean search (TypeElement element, Set<TypeElement> elements, Map<String, ComponentDescriptor> registry, ClasspathInfo info, SourceGroup sourceGroup, Map<String, Item> result) {
        if (element == null)
            return false;

        elements.remove (element);

        if (element.getKind () != ElementKind.CLASS)
            return false;

        Name tempQualifiedName = element.getQualifiedName ();
        if (tempQualifiedName == null)
            return false;
        String fqn = tempQualifiedName.toString ();

        ComponentDescriptor descriptor = registry.get (fqn);
        if (descriptor != null)
            return true;
        Item item = result.get (fqn);
        if (item != null)
            return true;

        TypeElement superElement = getSuperElement (element);
        if (superElement == null)
            return false;
        if (! search (superElement, elements, registry, info, sourceGroup, result))
            return false;

        String superFQN = superElement.getQualifiedName ().toString ();
        if (! registry.containsKey(superFQN)  &&  ! result.containsKey (superFQN))
            return false;

        boolean isAbstract = element.getModifiers ().contains (Modifier.ABSTRACT);
        boolean isFinal = element.getModifiers ().contains (Modifier.FINAL);
        FileObject file = SourceUtils.getFile (ElementHandle.create (element), info);
        boolean isInSource = file != null  &&  sourceGroup != null  &&  FileUtil.isParentOf (sourceGroup.getRootFolder (), file);
        item = new Item (superFQN, fqn, isAbstract, isFinal, isInSource);
        item.addPresenter (new MidpAddImportPresenterSerializer ());

        inspectElement (item, element);
//        boolean hasConstructor = inspectElement (item, element);
//        if (! isAbstract  &&  ! hasConstructor)
//            return false;

        result.put (fqn, item);
        return true;
    }

    private static boolean inspectElement (Item item, TypeElement clazz) {
        String fqn = clazz.getQualifiedName ().toString ();
        boolean hasConstructor = false;
        int constructorIndex = 1;

        for (Element el : clazz.getEnclosedElements ()) {
            if (! el.getModifiers ().contains (Modifier.PUBLIC))
                continue;

            if (el.getKind () == ElementKind.CONSTRUCTOR) {
                ExecutableElement method = (ExecutableElement) el;
                ArrayList<String> properties = new ArrayList<String> ();
                int index = 1;
                for (VariableElement parameter : method.getParameters ()) {
                    PropertyDescriptor property = MidpTypesConvertor.createPropertyDescriptorForParameter (fqn + "#" + constructorIndex + "#" + index, true, parameter); // NOI18N
                    item.addProperty (property);
                    properties.add (property.getName ());
                    String displayName = NbBundle.getMessage (ComponentInstaller.class, "NAME_ConstructorParam", new Object[] { parameter.getSimpleName (), constructorIndex, index, fqn }); // NOI18N
                    item.addPresenter (new MidpPropertyPresenterSerializer (displayName, property));
                    index ++;
                }
                item.addPresenter (new MidpSetterPresenterSerializer (null, properties));

            } else if (el.getKind () == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) el;
                String name = method.getSimpleName ().toString ();
                if (! name.startsWith ("set")  ||  name.length () < 4  ||  ! Character.isUpperCase (name.charAt (3))) // NOI18N
                    continue;
                ArrayList<String> properties = new ArrayList<String> ();
                List<? extends VariableElement> parameters = method.getParameters ();
                if (parameters.size () != 1)
                    continue;
                VariableElement parameter = parameters.iterator ().next ();

                PropertyDescriptor property = MidpTypesConvertor.createPropertyDescriptorForParameter (fqn + "#" + name, false, parameter); // NOI18N
                item.addProperty (property);
                properties.add (property.getName ());
                String displayName = NbBundle.getMessage (ComponentInstaller.class, "NAME_SetterParam", parameter.getSimpleName (), name, fqn); // NOI18N
                item.addPresenter (new MidpPropertyPresenterSerializer (displayName, property));

                item.addPresenter (new MidpSetterPresenterSerializer (name, properties));
            }
        }

        return hasConstructor;
    }

    private static TypeElement getSuperElement (TypeElement element) {
        TypeMirror superType = element.getSuperclass ();
        if (superType.getKind () != TypeKind.DECLARED)
            return null;
        return (TypeElement) ((DeclaredType) superType).asElement ();
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

    public static class Item {

        private TypeDescriptor typeDescriptor;
        private PaletteDescriptor paletteDescriptor;
        private String superFQN;
        private String fqn;
        private boolean inSource;
        private ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor> ();
        private ArrayList<PresenterSerializer> presenters = new ArrayList<PresenterSerializer> ();

        public Item (String superFQN, String fqn, boolean isAbstract, boolean isFinal, boolean inSource) {
            this.superFQN = superFQN;
            this.fqn = fqn;
            this.inSource = inSource;
            TypeID typeID = new TypeID (TypeID.Kind.COMPONENT, fqn);
            typeDescriptor = new TypeDescriptor (new TypeID (TypeID.Kind.COMPONENT, superFQN), typeID, ! isAbstract, ! isFinal);
            paletteDescriptor = new PaletteDescriptor (MidpPaletteProvider.CATEGORY_CUSTOM, MidpTypes.getSimpleClassName (typeID), fqn, "org/netbeans/modules/vmd/midp/resources/components/custom_component_16.png", "org/netbeans/modules/vmd/midp/resources/components/custom_component_32.png"); // NOI18N
        }

        public String getSuperFQN () {
            return superFQN;
        }

        public String getFQN () {
            return fqn;
        }

        public boolean isInSource () {
            return inSource;
        }

        public TypeDescriptor getTypeDescriptor () {
            return typeDescriptor;
        }

        public PaletteDescriptor getPaletteDescriptor () {
            return paletteDescriptor;
        }

        public List<PropertyDescriptor> getProperties () {
            return properties;
        }

        public List<PresenterSerializer> getPresenters () {
            return presenters;
        }

        public void addPresenter (PresenterSerializer serializer) {
            presenters.add (serializer);
        }

        public void addProperty (PropertyDescriptor property) {
            properties.add (property);
        }

    }

}
