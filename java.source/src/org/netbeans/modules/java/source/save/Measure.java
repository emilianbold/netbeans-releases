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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.save;

import com.sun.tools.javac.tree.JCTree;
import static com.sun.source.tree.Tree.Kind;

/**
 * Used for distance measuring of two elements.
 *
 * @author  Martin Matula
 * @author  Tomas Hurka
 * @author  Pavel Flaska
 */
class Measure {

    /**
     * Default measure based on equals.
     */
    static final Measure DEFAULT = new Measure();
    
    /**
     * Used for measuring distance of two class members.
     * (for fields, methods, constructors, annotation attributes etc.)
     */
    static final MemberMeasure MEMBER = new MemberMeasure();
    
    /** 
     * Used for measuring distance of two <code>Method invocation arguments</code>s.
     */
    static final Measure ARGUMENT = new ArgumentMeasure();

    /** 
     * Used for measuring distance of two <code>variables separated by comma</code>s.
     */
    static final Measure GROUP_VAR_MEASURE = new ArgumentMeasure();
    
    /** 
     * Value representing infinite distance - any distance value equal
     * or greater than this is represented as infinite (i.e. indicates
     * that the compared objects are distinct).
     */
    static final int INFINITE_DISTANCE = 1000;
    
    /**
     * Objects perfectly matches, they are identical.
     */
    static final int OBJECTS_MATCH = 0;

    /**
     * Objects are almost the same, kind is the same and pos is the same.
     */
    static final int ALMOST_THE_SAME = 250;
    
    /**
     * Objects are the same kind, but different pos
     */
    static final int THE_SAME_KIND = 750;

    /**
     * Prevent instance creation outside the class.
     */
    private Measure() {
    }
    
    /**
     * Compares two objects and returns distance between 
     * them. (Value expressing how far they are.)
     *
     * @param first First object to be compared.
     * @param second Second object to be compared.
     * @return Distance between compared objects (0 = objects perfectly match,
     * <code>INFINITE_DISTANCE</code> = objects are completely different)
     */
    int getDistance(Object first, Object second) {
        assert first != null && second != null : "Shouldn't pass null value!";
        
        if (first == second || first.equals(second)) {
            // pefectly match
            return OBJECTS_MATCH;
        } else {
            // does not match
            return INFINITE_DISTANCE;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////
    
    // MemberMeasure
    private static final class MemberMeasure extends Measure {
        @Override
        int getDistance(Object first, Object second) {
            int distance = DEFAULT.getDistance(first, second);
            if (distance == INFINITE_DISTANCE) {
                JCTree t1 = (JCTree) first;
                JCTree t2 = (JCTree) second;
                if (t1.getKind() == t2.getKind() && t1.pos == t2.pos) {
                    return ALMOST_THE_SAME;
                }
            }
            return distance;
        }
    }
    
    // Method invocation argument
    private static final class ArgumentMeasure extends Measure {
        @Override
        int getDistance(Object first, Object second) {
            int distance = DEFAULT.getDistance(first, second);
            if (distance == INFINITE_DISTANCE) {
                JCTree t1 = (JCTree) first;
                JCTree t2 = (JCTree) second;
                if (t1.getKind() == t2.getKind()) {
                    return t1.pos == t2.pos ? ALMOST_THE_SAME : THE_SAME_KIND;
                }
            }
            return distance;
        }
    }
}
