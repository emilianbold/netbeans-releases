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

package org.netbeans.modules.junit;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Name;

/**
 * Data structure that holds overview of class' members and their positions
 * within the class.
 * <p>To get an instance for a class, use static method
 * {@link #forClass(ClassTree)}.</p>
 *
 * @author  Marian Petras
 */
final class ClassMap {
    
    private static final int SETUP_POS_INDEX = 0;
    private static final int TEARDOWN_POS_INDEX = 1;
    private static final int FIRST_METHOD_POS_INDEX = 2;
    private static final int LAST_INIT_POS_INDEX = 3;
    private static final int FIRST_NESTED_POS_INDEX = 4;
    
    /**
     */
    private final List<String> signatures;
    /** */
    private final int[] positions;
    
    /** Creates a new instance of ClassMap */
    private ClassMap(List<String> signatures) {
        this.signatures = signatures;
    }
    
    {
        positions = new int[5];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = -1;
        }
    }
    
    /**
     * 
     * @exception  java.lang.IllegalArgumentException
     *             if the {@code classTree} argument is {@code null}
     */
    static ClassMap forClass(ClassTree cls) {
        List<? extends Tree> members = cls.getMembers();
        if (members.isEmpty()) {
            return new ClassMap(new ArrayList<String>());
        }

        List<String> entries = new ArrayList<String>(members.size());
        ClassMap map = new ClassMap(entries);

        int index = 0;
        for (Tree member : members) {
            String signature;
            switch (member.getKind()) {
                case BLOCK:
                    signature = "* ";                                   //NOI18N
                    map.setLastInitializerIndex(index);
                    break;
                case VARIABLE:
                    signature = "- ";                                   //NOI18N
                    break;
                case CLASS:
                    signature = "[ ";                                   //NOI18N
                    if (map.getFirstNestedClassIndex() == -1) {
                        map.setFirstNestedClassIndex(index);
                    }
                    break;
                case METHOD:
                    MethodTree method = (MethodTree) member;
                    boolean hasParams = !method.getParameters().isEmpty();
                    Name methodName = method.getName();
                    if (methodName.contentEquals("<init>")) {           //NOI18N
                        signature = hasParams ? "*+" : "* ";            //NOI18N
                        map.setLastInitializerIndex(index);
                    } else {
                        if (!hasParams) {
                            if ((map.getSetUpIndex() == -1)
                                    && methodName.contentEquals("setUp")) {     //NOI18N
                                map.setSetUpIndex(index);
                            }
                            if ((map.getTearDownIndex() == -1)
                                    && methodName.contentEquals("tearDown")) {  //NOI18N
                                map.setTearDownIndex(index);
                            }
                        }
                        signature = (hasParams ? "!+" : "! ")           //NOI18N
                                    + methodName.toString();
                        if (map.getFirstMethodIndex() == -1) {
                            map.setFirstMethodIndex(index);
                        }
                    }
                    break;
                default:
                    signature = "x ";                                   //NOI18N
            }
            entries.add(signature);
            index++;
        }
        
        return map;
    }
    
    /**
     */
    int getSetUpIndex() {
        return positions[SETUP_POS_INDEX];
    }
    
    /**
     */
    private void setSetUpIndex(int setUpIndex) {
        positions[SETUP_POS_INDEX] = setUpIndex;
    }
    
    /**
     */
    int getTearDownIndex() {
        return positions[TEARDOWN_POS_INDEX];
    }
    
    /**
     */
    private void setTearDownIndex(int tearDownIndex) {
        positions[TEARDOWN_POS_INDEX] = tearDownIndex;
    }
    
    /**
     */
    int getFirstMethodIndex() {
        return positions[FIRST_METHOD_POS_INDEX];
    }
    
    /**
     */
    private void setFirstMethodIndex(int firstMethodIndex) {
        positions[FIRST_METHOD_POS_INDEX] = firstMethodIndex;
    }
    
    /**
     */
    int getFirstNestedClassIndex() {
        return positions[FIRST_NESTED_POS_INDEX];
    }
    
    /**
     */
    private void setFirstNestedClassIndex(int firstNestedClassIndex) {
        positions[FIRST_NESTED_POS_INDEX] = firstNestedClassIndex;
    }
    
    /**
     */
    int getLastInitializerIndex() {
        return positions[LAST_INIT_POS_INDEX];
    }
    
    /**
     */
    private void setLastInitializerIndex(int lastInitializerIndex) {
        positions[LAST_INIT_POS_INDEX] = lastInitializerIndex;
    }
    
    /**
     */
    boolean containsSetUp() {
        return getSetUpIndex() != -1;
    }
    
    /**
     */
    boolean containsTearDown() {
        return getTearDownIndex() != -1;
    }
    
    /**
     */
    boolean containsNoArgMethod(String name) {
        return findNoArgMethod(name) != -1;
    }
    
    /**
     */
    boolean containsMethods() {
        return getFirstMethodIndex() != -1;
    }
    
    /**
     */
    boolean containsInitializers() {
        return getLastInitializerIndex() != -1;
    }
    
    /**
     */
    boolean containsNestedClasses() {
        return getFirstNestedClassIndex() != -1;
    }
    
    /**
     */
    int findNoArgMethod(String name) {
        if (!containsMethods()) {
            return -1;
        }
        if (name.equals("setUp")) {                                     //NOI18N
            return getSetUpIndex();
        }
        if (name.equals("tearDown")) {                                  //NOI18N
            return getTearDownIndex();
        }
        
        return signatures.indexOf("! " + name);                         //NOI18N
    }
    
    /**
     */
    void addNoArgMethod(String name) {
        addNoArgMethod(size(), name);
    }
    
    /**
     */
    void addNoArgMethod(int index, String name) {
        int currSize = size();
        
        if (index > currSize) {
            throw new IndexOutOfBoundsException("index: " + index       //NOI18N
                                               + ", size: " + currSize);//NOI18N
        }
        
        String signature = "! " + name;                                 //NOI18N
        if (index != currSize) {
            signatures.add(index, signature);
            shiftPositions(index, 1);
        } else {
            signatures.add(signature);                                  //NOI18N
        }
        
        if (name.equals("setUp")) {                                     //NOI18N
            setSetUpIndex(index);
        } else if (name.equals("tearDown")) {                           //NOI18N
            setTearDownIndex(index);
        }
        if (getFirstMethodIndex() == -1) {
            setFirstMethodIndex(index);
        }
    }
    
    /**
     */
    void removeNoArgMethod(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("negative index ("      //NOI18N
                                                + index + ')');
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("index: " + index       //NOI18N
                                               + ", size: " + size());  //NOI18N
        }
        
        String signature = signatures.get(index);
        
        if (!signature.startsWith("! ")) {                              //NOI18N
            throw new IllegalArgumentException(
                    "not a no-arg method at the given index ("          //NOI18N
                    + index + ')');
        }
        
        if (index == getSetUpIndex()) {
            setSetUpIndex(-1);
        } else if (index == getTearDownIndex()) {
            setTearDownIndex(-1);
        }
        if (index == getFirstMethodIndex()) {
            int currSize = size();
            if (index == (currSize - 1)) {
                setFirstMethodIndex(-1);
            } else {
                int newFirstMethodIndex = -1;
                int memberIndex = index + 1;
                for (String sign : signatures.subList(index + 1, currSize)) {
                    if (sign.startsWith("! ")) {
                        newFirstMethodIndex = memberIndex;
                        break;
                    }
                    memberIndex++;
                }
                setFirstMethodIndex(newFirstMethodIndex);
            }
        }
        shiftPositions(index + 1, -1);
    }
    
    /**
     */
    int size() {
        return signatures.size();
    }
    
    /**
     */
    private void shiftPositions(int fromIndex,
                                int shiftSize) {
        for (int i = 0; i < positions.length; i++) {
            int pos = positions[i];
            if ((pos != -1) && (pos >= fromIndex)) {
                positions[i] = pos + shiftSize;
            }
        }
    }
    
}
