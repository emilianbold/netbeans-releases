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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.Tree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import static com.sun.source.tree.Tree.Kind;

/**
 * Used for distance measuring of two elements.
 * todo (#pf): Describe mechanism.
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
     * Used for measuring distance of two <code>MethodTree</code>s.
     * It is used also for constructors.
     */
    static final MethodMeasure METHOD = new MethodMeasure();
    
    /**
     * Used for measuring distance of two <code>VariableTree</code>s.
     */
    static final VariableMeasure FIELD = new VariableMeasure();
    
    /**
     * Used for measuring distance of two class members.
     * (for fields, methods, constructors, annotation attributes etc.)
     */
    static final MemberMeasure MEMBER = new MemberMeasure();
    
    /** 
     * Used for measuring distance of two <code>VariableTree</code>s.
     */
    static final VariableMeasure PARAMETER = FIELD;

    /**
     * Used for measuring distance of two class names.
     */
    static final ClassNameMeasure CLASS_NAME = new ClassNameMeasure();
    
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
    private static final StringMeasure STRING = new StringMeasure();
    
    // MemberMeasure
    private static final class MemberMeasure extends Measure {
        int getDistance(Object first, Object second) {
            Tree t1 = (Tree) first;
            Tree t2 = (Tree) second;
            
            switch (t1.getKind()) {
                case METHOD:
                    return METHOD.getDistance(first, second);
                    
                case VARIABLE:
                    return FIELD.getDistance(first, second);
                    
                // todo (#pf): missing implementation of other kinds of
                // measure!
                    
                default:
                    return DEFAULT.getDistance(first, second);
            }
        }
    }
    
    // MethodMeasure
    private static final class MethodMeasure extends Measure {

        private static final int NAME_WEIGHT = 60;
        private static final int PARAMETERS_WEIGHT = 40;

        int getDistance(Object first, Object second) {
            // the object is the same...
            if (super.getDistance(first, second) == OBJECTS_MATCH) {
                return OBJECTS_MATCH;
            }
            // they aren't the same, ensure that diff mechanism will
            // dive inside the tree. -- There can be changes not detected
            // by this distance mechanism, e.g. throws clause change,
            // body changes, parameters name change etc.
            int result = 100;
            Tree t1 = (Tree) first;
            Tree t2 = (Tree) second;
            
            // one of the object is not instance of MethodTree -- there cannot
            // be any transformation from first to second... They are totally
            // different
            if (t1.getKind() != Kind.METHOD || t2.getKind() != Kind.METHOD) {
                return INFINITE_DISTANCE;
            }
            // now it is safe, both are METHOD
            MethodTree tree1 = (MethodTree) first;
            MethodTree tree2 = (MethodTree) second;
            
            // measure name distances.
            // Note: for constructor, skip this measurement
            if (!"<init>".contentEquals(tree1.getName()) &&
                !"<init>".contentEquals(tree2.getName()))
            {
                result += Measure.STRING.getDistance(
                              tree1.getName().toString(), 
                              tree2.getName().toString()
                          ) * NAME_WEIGHT;
            } else {
                result += 60000;
            }

            Tree[] types1 = new Tree[tree1.getParameters().size()];
            Tree[] types2 = new Tree[tree2.getParameters().size()];
            
            int i = 0;
            for (VariableTree item : tree1.getParameters())
                types1[i++] = item.getType();
            
            i = 0;
            for (VariableTree item : tree2.getParameters())
                types2[i++] = item.getType();
            
            result += new OrderedArrayMeasure(Measure.CLASS_NAME).
                        getDistance(types1, types2) * PARAMETERS_WEIGHT;
            result /= 100;
            return result > INFINITE_DISTANCE ? INFINITE_DISTANCE : result;
        }
    }

    // VariableMeasure
    private final static class VariableMeasure extends Measure {

        private static final int NAME_WEIGHT = 40;
        private static final int TYPE_WEIGHT = 60;

        public int getDistance(Object first, Object second) {
            // the object is the same...
            if (super.getDistance(first, second) == OBJECTS_MATCH) {
                return OBJECTS_MATCH;
            }
            // they aren't the same, ensure that diff mechanism will
            // dive inside the tree. -- There can be changes not detected
            // by this distance mechanism, e.g. throws clause change,
            // body changes, parameters name change etc.
            int result = 100;
            Tree t1 = (Tree) first;
            Tree t2 = (Tree) second;
            
            // one of the object is not instance of MethodTree -- there cannot
            // be any transformation from first to second... They are totally
            // different
            if (t1.getKind() != Kind.VARIABLE || t2.getKind() != Kind.VARIABLE) {
                return INFINITE_DISTANCE;
            }
            
            VariableTree vt1 = (VariableTree) first;
            VariableTree vt2 = (VariableTree) second;
            
            int nameDist = Measure.STRING.getDistance(vt1.getName().toString(), vt2.getName().toString());
            int typeDist = Measure.CLASS_NAME.getDistance(vt1.getType(), vt2.getType());
            
            if (nameDist > 0 && typeDist > 0) {
                // do not compute the distance, both items changed, consider
                // as a new element
                return INFINITE_DISTANCE;
            }
            result += nameDist * NAME_WEIGHT;
            result += typeDist * TYPE_WEIGHT;
            result /= 100;
            
            return result > INFINITE_DISTANCE ? INFINITE_DISTANCE : result;
        }
    }
    
    // StringMeasure
    private static final class StringMeasure extends Measure {
        
        private static final int SAME = 0;
        private static final int CASE_SAME = 1;
        private static final int DIFFERENT = 10;
        
        /**
         * This method implements metrics on Strings.
         *
         * @param  first  first string
         * @param  second second string
         * @return value between 0 and 100, where 0 means strings are 
         *         identical, 100 means strings are completly different.
         */
        public final int getDistance(final Object first, final Object second) {
            if (first == second)
                return SAME;
            
            if (first == null || second == null)
                return INFINITE_DISTANCE;
            
            final String x = (String) first;
            final String y = (String) second;
            final int xlen = x.length();
            final int ylen = y.length();
            int errors = 0;
            int xindex = 0, yindex = 0;
            final char xarr[] = new char[xlen+1];
            final char yarr[] = new char[ylen+1];
            
            x.getChars(0, xlen, xarr, 0);
            y.getChars(0, ylen, yarr, 0);
            
            while (xindex < xlen && yindex < ylen) {
                final char xchar = xarr[xindex];
                final char ychar = yarr[yindex];
                final int cherr = compareChars(xchar, ychar);
                
                if (cherr != DIFFERENT) {
                    errors += cherr;
                    xindex++;
                    yindex++;
                    continue;
                }
                final char xchar1 = xarr[xindex+1];
                final char ychar1 = yarr[yindex+1];
                if (xchar1 != 0 && ychar1 != 0) {
                    final int cherr1 = compareChars(xchar1, ychar1);
                    
                    if (cherr1 != DIFFERENT) {
                        errors += DIFFERENT + cherr1;
                        xindex += 2;
                        yindex += 2;
                        continue;
                    }
                    final int xerr = compareChars(xchar, ychar1);
                    final int xerr1= compareChars(xchar1, ychar);
                    
                    if (xerr != DIFFERENT && xerr1 != DIFFERENT) {
                        errors += DIFFERENT + xerr + xerr1;
                        xindex += 2;
                        yindex += 2;
                        continue;
                    }
                }
                if (xlen-xindex > ylen-yindex) {
                    xindex++;
                } else if (xlen-xindex < ylen-yindex) {
                    yindex++;
                } else {
                    xindex++;
                    yindex++;
                }
                errors += DIFFERENT;
            }
            errors += (xlen-xindex+ylen-yindex) * DIFFERENT;
            return (INFINITE_DISTANCE*errors)/Math.max(ylen,xlen)/DIFFERENT;
        }
        
        private static final int compareChars(final char xc, final char yc) {
            if (xc == yc) 
                return SAME;
            
            char xlower = Character.toLowerCase(xc);
            char ylower = Character.toLowerCase(yc);
            
            return xlower == ylower ? CASE_SAME : DIFFERENT;
        }
    }
   
    // ClassNameMeasure
    private static final class ClassNameMeasure extends Measure {

        public int getDistance(Object first, Object second) {
            if (first == second) return OBJECTS_MATCH;
            if (first == null || second == null) {
                return INFINITE_DISTANCE;
            }
            
            Tree t1 = (Tree) first;
            Tree t2 = (Tree) second;
            
            if (t1.getKind() == t2.getKind())
                // todo (#pf): check that toString() is correct here, perhaps
                // some better mechanism should be implemented.
                return Measure.STRING.getDistance(t1.toString(), t2.toString());
            
           return INFINITE_DISTANCE;
        }
    }

    // OrderedArrayMeasure
    private static final class OrderedArrayMeasure extends Measure {
        
        private final Measure measure;
        
        OrderedArrayMeasure(Measure elementsMeasure) {
            measure = elementsMeasure;
        }

        public int getDistance(Object first, Object second) {
            Object[] array1 = (Object[]) first;
            Object[] array2 = (Object[]) second;
            int minSize = Math.min(array1.length, array2.length);
            int difference = Math.abs(array1.length - array2.length);
            int result = 0;
            
            if (minSize == 0) {
                if (difference != 0)
                    result = INFINITE_DISTANCE;
                return result;
            }
            for (int i = 0; i < minSize; i++) {
                result += measure.getDistance(array1[i], array2[i]);
            }
            result += difference * INFINITE_DISTANCE;
            result /= (minSize+difference);
            return result > INFINITE_DISTANCE ? INFINITE_DISTANCE : result;
        }
    }
}
