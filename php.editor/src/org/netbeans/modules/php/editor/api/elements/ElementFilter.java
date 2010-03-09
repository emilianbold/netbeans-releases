/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.api.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public abstract class ElementFilter {

    public ElementFilter() {
    }

    public static ElementFilter allOf(final Collection<ElementFilter> filters) {
        return ElementFilter.allOf(filters.toArray(new ElementFilter[filters.size()]));
    }
    
    public static ElementFilter allOf(final ElementFilter... filters) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (ElementFilter elementFilter : filters) {
                    if (!elementFilter.isAccepted(element)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static ElementFilter anyOf(final Collection<ElementFilter> filters) {
        return ElementFilter.allOf(filters.toArray(new ElementFilter[filters.size()]));
    }

    public static ElementFilter anyOf(final ElementFilter... filters) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (ElementFilter elementFilter : filters) {
                    if (elementFilter.isAccepted(element)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ElementFilter forName(final NameKind name) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return name.matchesName(element);
            }
        };
    }

    public static ElementFilter forIncludedNames(final Collection<String> includedNames, final PhpElementKind kind) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (String name : includedNames) {
                    if (NameKind.exact(name).matchesName(kind, element.getName())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ElementFilter forExcludedNames(final Collection<String> excludedNames, final PhpElementKind kind) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (String name : excludedNames) {
                    if (NameKind.exact(name).matchesName(kind, element.getName())) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static ElementFilter forOffset(final int offset) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getOffset() == offset;
            }
        };
    }

    public static ElementFilter forKind(final PhpElementKind kind) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpElementKind().equals(kind);
            }
        };
    }

    public static ElementFilter forFiles(final FileObject... files) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                boolean retval = true;
                for (FileObject fileObject : files) {
                    String nameExt = fileObject.getNameExt();
                    String elementURL = element.getFilenameUrl();
                    if (elementURL.indexOf(nameExt) < 0 || element.getFileObject() != fileObject) {
                        retval = false;
                        break;
                    }
                }
                return retval;
            }
        };
    }

    public static ElementFilter forSuperClassName(final QualifiedName supeClassNameQuery) {
        return new ElementFilter() {
            final NameKind.Exact superNameKind = NameKind.exact(supeClassNameQuery);
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof ClassElement) {
                    final QualifiedName nextSuperName = ((ClassElement) element).getSuperClassName();
                    return nextSuperName != null ? superNameKind.matchesName(PhpElementKind.CLASS, nextSuperName): false;
                }
                return true;
            }
        };
    }

    public static ElementFilter forSuperInterfaceNames(final Set<QualifiedName> supeIfaceNameQueries) {
        final Set<ElementFilter> filters = new HashSet<ElementFilter>();
        for (final QualifiedName qualifiedName : supeIfaceNameQueries) {
            filters.add(forSuperInterfaceName(qualifiedName));
        }
        return ElementFilter.allOf(filters.toArray(new ElementFilter[filters.size()]));
    }

    public static ElementFilter forSuperInterfaceName(final QualifiedName supeIfaceNameQuery) {
        return new ElementFilter() {
            final NameKind.Exact superNameKind = NameKind.exact(supeIfaceNameQuery);
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeElement) {
                    Set<QualifiedName> superInterfaces = ((TypeElement) element).getSuperInterfaces();
                    for (QualifiedName nextSuperName : superInterfaces) {
                        if (superNameKind.matchesName(PhpElementKind.IFACE, nextSuperName)) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        };
    }

    public static ElementFilter forMembersOfType(final TypeElement typeElement) {
        return new ElementFilter() {
            private ElementFilter filterDelegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    if (filterDelegate == null) {
                        filterDelegate = ElementFilter.allOf(
                                ElementFilter.forFiles(typeElement.getFileObject()),
                                ElementFilter.forOffset(typeElement.getOffset()));
                    }
                    //return thisTypeElement.equals(typeElement);
                    return filterDelegate.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }

    public static <T extends PhpElement> ElementFilter forInstanceOf(final Class<T> cls) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return cls.isAssignableFrom(element.getClass());
            }
        };
    }

    public static ElementFilter forAnyOfFlags(final int flags) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return (element.getPhpModifiers().toFlags() & flags) != 0;
            }
        };
    }

    public static ElementFilter forAllOfFlags(final int flags) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return (element.getPhpModifiers().toFlags() & flags) == flags;
            }
        };
    }

    /**
     * @param publicOrNot true means that not public elements are filtered and only public are returned.
     * False means that pulic elements are filtered and and only not public are returned
     */
    public static ElementFilter forPublicModifiers(final boolean publicOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpModifiers().isPublic() == publicOrNot;
            }
        };
    }

    public static ElementFilter forPrivateModifiers(final boolean privateOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpModifiers().isPrivate() == privateOrNot;
            }
        };
    }

    public static ElementFilter forStaticModifiers(final boolean staticOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpModifiers().isStatic() == staticOrNot;
            }
        };
    }

    public abstract boolean isAccepted(PhpElement element);

    public <T extends PhpElement> Set<T> filter(Set<T> original) {
        Set<T> retval = new HashSet<T>();
        for (T baseElement : original) {
            if (isAccepted(baseElement)) {
                retval.add(baseElement);
            }
        }
        return Collections.unmodifiableSet(retval);
    }
    public <T extends PhpElement> Set<T> reverseFilter(Set<T> original) {
        Set<T> retval = new HashSet<T>();
        for (T baseElement : original) {
            if (!isAccepted(baseElement)) {
                retval.add(baseElement);
            }
        }
        return Collections.unmodifiableSet(retval);
    }

    public <T extends PhpElement> Set<T> prefer(Set<T> original) {
        Set<T> retval = original;
        Set<T> remove = new HashSet<T>();
        Map<Pair<PhpElementKind, String>, T> map = new HashMap<Pair<PhpElementKind, String>, T>();
        for (T baseElement : original) {
            final PhpElementKind kind = baseElement.getPhpElementKind();
            final String name = baseElement.getName();
            final Pair<PhpElementKind, String> key =
                    Pair.<PhpElementKind, String>of(kind, name);
            T old = map.put(key, baseElement);
            if (old != null) {
                if (isAccepted(baseElement)) {
                    remove.add(old);
                } else {
                    remove.add(map.put(key, old));
                }
            }
        }
        if (!remove.isEmpty()) {
            retval = new HashSet<T>(original);
            retval.removeAll(remove);
        }
        return Collections.unmodifiableSet(retval);
    }
}
