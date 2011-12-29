/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.persistence.jpa.jpql.spi.IConstructor;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.eclipse.persistence.jpa.jpql.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.spi.ITypeRepository;

/**
 *
 * @author sp153251
 */
public class JavaType implements IType {

    /**
     * The cached {@link IConstructor IConstructors}.
     */
    private Collection<IConstructor> constructors;
    /**
     * The list of names for the {@link Enum}'s constants otherwise an empty array.
     */
    private String[] enumConstants;
    /**
     * The actual Java type.
     */
    private Class<?> type;
    /**
     * The cached {@link ITypeDeclaration} for this {@link IType}.
     */
    private ITypeDeclaration typeDeclaration;
    /**
     * The fully qualified name of the Java type.
     */
    private String typeName;
    /**
     * The external form of a type repository.
     */
    private ITypeRepository typeRepository;

    /**
     * Creates a new <code>JavaType</code>.
     *
     * @param typeRepository The external form of a type repository
     * @param type The actual Java type wrapped by this class
     */
    JavaType(ITypeRepository typeRepository, Class<?> type) {
        super();
        this.type = type;
        this.typeName = type.getName();
        this.typeRepository = typeRepository;
    }

    private IConstructor buildConstructor(java.lang.reflect.Constructor<?> constructor) {
        return new JavaConstructor(this, constructor);
    }

    private Collection<IConstructor> buildConstructors() {

        java.lang.reflect.Constructor<?>[] javaConstructors = type.getDeclaredConstructors();
        Collection<IConstructor> constructors = new ArrayList<IConstructor>(javaConstructors.length);

        for (java.lang.reflect.Constructor<?> javaConstructor : javaConstructors) {
            constructors.add(buildConstructor(javaConstructor));
        }

        return constructors;
    }

    private String[] buildEnumConstants() {

        if (!type.isEnum()) {
            return new String[]{};
        }

        Object[] enumConstants = type.getEnumConstants();
        String[] names = new String[enumConstants.length];

        for (int index = enumConstants.length; --index >= 0;) {
            names[index] = ((Enum<?>) enumConstants[index]).name();
        }

        return names;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<IConstructor> constructors() {
        if (constructors == null) {
            constructors = buildConstructors();
        }
        return Collections.unmodifiableCollection(constructors);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(IType type) {
        return (this == type) ? true : typeName.equals(type.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        return (this == object) || equals((IType) object);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getEnumConstants() {
        if (enumConstants == null) {
            enumConstants = buildEnumConstants();
        }
        return enumConstants;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return typeName;
    }

    /**
     * Returns the encapsulated {@link Class}, which is the actual type.
     *
     * @return The actual Java type, if <code>null</code> is returned; then the class could not be resolved
     */
    Class<?> getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public ITypeDeclaration getTypeDeclaration() {
        if (typeDeclaration == null) {
            typeDeclaration = new JavaTypeDeclaration(typeRepository, this, null, (type != null) ? type.isArray() : false);
        }
        return typeDeclaration;
    }

    /**
     * Returns the type repository for the application.
     *
     * @return The repository of {@link IType ITypes}
     */
    ITypeRepository getTypeRepository() {
        return typeRepository;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return type.isAnnotationPresent(annotationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return typeName.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAssignableTo(IType type) {

        if (this == type) {
            return true;
        }

        // TODO
        if (type instanceof JavaType) {
            Class<?> otherType = ((JavaType) type).type;
            return otherType.isAssignableFrom(this.type);
        } else if (type instanceof Type) {
            // TODO
            return false;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnum() {
        return (type != null) && type.isEnum();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolvable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setTypeDeclaration(ITypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return typeName;
    }
}
