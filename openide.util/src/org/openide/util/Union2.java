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

package org.openide.util;

import java.io.Serializable;

/**
 * A union type which can contain one of two kinds of objects.
 * {@link Object#equals} and {@link Object#hashCode} treat this as a container,
 * not identical to the contained object, but the identity is based on the contained
 * object. The union is serialiable if its contained object is.
 * {@link Object#toString} delegates to the contained object.
 * @author Jesse Glick
 * @since org.openide.util 7.1
 */
public abstract class Union2<First,Second> implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    Union2() {}

    /**
     * Retrieve the union member of the first type.
     * @return the object of the first type
     * @throws IllegalArgumentException if the union really contains the second type
     */
    public abstract First first() throws IllegalArgumentException;

    /**
     * Retrieve the union member of the second type.
     * @return the object of the second type
     * @throws IllegalArgumentException if the union really contains the first type
     */
    public abstract Second second() throws IllegalArgumentException;

    /**
     * Check if the union contains the first type.
     * @return true if it contains the first type, false if it contains the second type
     */
    public abstract boolean hasFirst();

    /**
     * Check if the union contains the second type.
     * @return true if it contains the second type, false if it contains the first type
     */
    public abstract boolean hasSecond();

    @Override
    public abstract Union2<First,Second> clone();

    /**
     * Construct a union based on the first type.
     * @param first an object of the first type
     * @return a union containing that object
     */
    public static <First,Second> Union2<First,Second> createFirst(First first) {
        return new Union2First<First,Second>(first);
    }

    /**
     * Construct a union based on the second type.
     * @param second an object of the second type
     * @return a union containing that object
     */
    public static <First,Second> Union2<First,Second> createSecond(Second second) {
        return new Union2Second<First,Second>(second);
    }

    private static final class Union2First<First,Second> extends Union2<First,Second> {

        private static final long serialVersionUID = 1L;

        private final First first;

        public Union2First(First first) {
            this.first = first;
        }

        @Override
        public First first() throws IllegalArgumentException {
            return first;
        }

        @Override
        public Second second() throws IllegalArgumentException {
            throw new IllegalArgumentException();
        }

        @Override
        public boolean hasFirst() {
            return true;
        }

        @Override
        public boolean hasSecond() {
            return false;
        }

        @Override
        public String toString() {
            return String.valueOf(first);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Union2First) && first.equals(((Union2First) obj).first);
        }

        @Override
        public int hashCode() {
            return first.hashCode();
        }

        @Override
        public Union2<First,Second> clone() {
            return createFirst(first);
        }

    }

    private static final class Union2Second<First,Second> extends Union2<First,Second> {

        private static final long serialVersionUID = 1L;

        private final Second second;

        public Union2Second(Second second) {
            this.second = second;
        }

        @Override
        public First first() throws IllegalArgumentException {
            throw new IllegalArgumentException();
        }

        @Override
        public Second second() throws IllegalArgumentException {
            return second;
        }

        @Override
        public boolean hasFirst() {
            return false;
        }

        @Override
        public boolean hasSecond() {
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(second);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Union2Second) && second.equals(((Union2Second) obj).second);
        }

        @Override
        public int hashCode() {
            return second.hashCode();
        }

        @Override
        public Union2<First,Second> clone() {
            return createSecond(second);
        }

    }

}
