///*
// * The contents of this file are subject to the terms of the Common Development
// * and Distribution License (the License). You may not use this file except in
// * compliance with the License.
// *
// * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
// * or http://www.netbeans.org/cddl.txt.
// *
// * When distributing Covered Code, include this CDDL Header Notice in each file
// * and include the License file at http://www.netbeans.org/cddl.txt.
// * If applicable, add the following below the CDDL Header, with the fields
// * enclosed by brackets [] replaced by your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// * Microsystems, Inc. All Rights Reserved.
// */
//
//package org.netbeans.modules.j2ee.persistence.wizard;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//import javax.swing.event.ChangeListener;
//import org.netbeans.jmi.javamodel.Annotation;
//import org.netbeans.jmi.javamodel.Feature;
//import org.netbeans.jmi.javamodel.Field;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.ParameterizedType;
//import org.netbeans.jmi.javamodel.Type;
//import org.netbeans.modules.j2ee.common.JMIUtils;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Entity;
//import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ChangeSupport;
//
///**
// *
// * @author Pavel Buzek
// */
// TODO: RETOUCHE
//public class EntityClosure {
//    
//    private final ChangeSupport changeSupport = new ChangeSupport(this);
//    
//    private Set<Entity> availableEntities = new HashSet<Entity>();
//    private Set<Entity> wantedEntities = new HashSet<Entity>();
//    private Set<Entity> selectedEntities = new HashSet<Entity>();
//    private Set<Entity> referencedEntities = new HashSet<Entity>();
//    private HashMap<String, Entity> name2entity = new HashMap<String, Entity>();
//    
//    private boolean closureEnabled = true;
//    
//    public EntityClosure(Set<Entity> entities) {
//        addAvaliableEntities(entities);
//    }
//    
//    public void addAvaliableEntities(Set<Entity> entities) {
//        availableEntities.addAll(entities);
//        for(Entity e : entities) {
//            name2entity.put(e.getClass2(), e);
//        }
//        changeSupport.fireChange();
//    }
//    
//    public void addChangeListener(ChangeListener listener) {
//        changeSupport.addChangeListener(listener);
//    }
//    
//    public Set<Entity> getAvailableEntities() {
//        return availableEntities;
//    }
//    
//    public Set<Entity> getWantedEntities() {
//        return wantedEntities;
//    }
//    
//    public Set<Entity> getSelectedEntities() {
//        return selectedEntities;
//    }
//    
//    public void addEntities(Set<Entity> entities) {
//        if (isClosureEnabled()) {
//            if (wantedEntities.addAll(entities)) {
//                Set<Entity> refEntities = getReferencedEntitiesTransitively(entities);
//                Set<Entity> addedEntities = new HashSet<Entity>(entities);
//                addedEntities.addAll(refEntities);
//                
//                selectedEntities.addAll(addedEntities);
//                referencedEntities.addAll(refEntities);
//                availableEntities.removeAll(addedEntities);
//                
//                changeSupport.fireChange();
//            }
//        } else {
//            wantedEntities.addAll(entities);
//            selectedEntities.addAll(entities);
//            availableEntities.removeAll(entities);
//            
//            changeSupport.fireChange();
//        }
//    }
//    
//    public void removeEntities(Set<Entity> Entities) {
//        if (isClosureEnabled()) {
//            if (wantedEntities.removeAll(Entities)) {
//                redoClosure();
//                
//                changeSupport.fireChange();
//            }
//        } else {
//            wantedEntities.removeAll(Entities);
//            selectedEntities.removeAll(Entities);
//            availableEntities.addAll(Entities);
//            
//            changeSupport.fireChange();
//        }
//    }
//    
//    public void addAllEntities() {
//        wantedEntities.addAll(availableEntities);
//        
//        if (isClosureEnabled()) {
//            redoClosure();
//            
//            changeSupport.fireChange();
//        } else {
//            selectedEntities.addAll(wantedEntities);
//            availableEntities.clear();
//            
//            changeSupport.fireChange();
//        }
//    }
//    
//    public void removeAllEntities() {
//        availableEntities.addAll(selectedEntities);
//        wantedEntities.clear();
//        selectedEntities.clear();
//        referencedEntities.clear();
//        
//        changeSupport.fireChange();
//    }
//    
//    /**
//     * Returns the tables transitively referenced by the contents of the tables parameter
//     * (not including tables passed in this parameter). If a table references itself,
//     * it is not added to the result.
//     */
//    private Set getReferencedEntitiesTransitively(Set<Entity> entities) {
//        Queue<Entity> entityQueue = new Queue<Entity>(entities);
//        Set<Entity> refEntities = new HashSet<Entity>();
//        
//        while (!entityQueue.isEmpty()) {
//            Entity entity = entityQueue.poll();
//            
//            Set<Entity> referenced = getReferencedEntities(entity);
//            for (Entity refEntity : referenced) {
//                
//                if (!refEntity.equals(entity)) {
//                    refEntities.add(refEntity);
//                }
//                entityQueue.offer(refEntity);
//            }
//        }
//        
//        return refEntities;
//    }
//    
//    private Set<Entity> getReferencedEntities(Entity entity) {
//        Set<Entity> referenced = new HashSet<Entity>();
//        JavaClass jc = JMIUtils.findClass(entity.getClass2());
//        List<Feature> features;
//        if (isFieldAccess(jc)) {
//            for (Field f : JMIUtils.getFields(jc)) {
//                Type type = f.getType();
//                addReferences(referenced, type);
//            }
//        } else {
//            for (Method m : JMIUtils.getMethods(jc)) {
//                if (m.getName().startsWith("get")) { //NOI18N
//                    Type type = m.getType();
//                    addReferences(referenced, type);
//                }
//            }
//        }
//        
//        return referenced;
//    }
//
//    private void addReferences(final Set<Entity> referenced, final Type type) {
//        String clsName = null;
//        if (type instanceof ParameterizedType) {
//            ParameterizedType pt = (ParameterizedType) type;
//            JavaClass referencedClass = null;
//            for (Object param : pt.getParameters()) {
//                if (param instanceof JavaClass) {
//                    clsName = ((JavaClass) param).getName();
//                    break;
//                }
//            }
//        }
//        if (clsName == null) {
//            clsName = type.getName();
//        }
//        Entity e = name2entity.get(clsName);
//        if (e != null) {
//            referenced.add(e);
//        }
//    }
//
//    private void redoClosure() {
//        Set<Entity> allEntities = new HashSet<Entity>(availableEntities);
//        allEntities.addAll(selectedEntities);
//        
//        referencedEntities.clear();
//        referencedEntities.addAll(getReferencedEntitiesTransitively(wantedEntities));
//
//        selectedEntities.clear();
//        selectedEntities.addAll(wantedEntities);
//        selectedEntities.addAll(referencedEntities);
//
//        availableEntities.clear();
//        availableEntities.addAll(allEntities);
//        availableEntities.removeAll(selectedEntities);
//    }
//    
//    public boolean isClosureEnabled() {
//        return closureEnabled;
//    }
//
//    public void setClosureEnabled(boolean closureEnabled) {
//        if (this.closureEnabled == closureEnabled) {
//            return;
//        }
//        this.closureEnabled = closureEnabled;
//        if (closureEnabled) {
//            redoClosure();
//        } else {
//            Set<Entity> allEntities = new HashSet<Entity>(availableEntities);
//            allEntities.addAll(selectedEntities);
//
//            referencedEntities.clear();
//
//            selectedEntities.clear();
//            selectedEntities.addAll(wantedEntities);
//
//            availableEntities.clear();
//            availableEntities.addAll(allEntities);
//            availableEntities.removeAll(selectedEntities);
//        }
//        changeSupport.fireChange();
//    }
//    
//    private static boolean isFieldAccess(JavaClass jc) {
//        //detect access type
//        List features = jc.getFeatures();
//        boolean fieldAccess = false;
//        boolean accessTypeDetected = false;
//        for (Iterator featuresIter = features.iterator(); featuresIter.hasNext() && !accessTypeDetected;) {
//            Feature feature = (Feature) featuresIter.next();
//            for (Iterator it = feature.getAnnotations().iterator(); it.hasNext() && !accessTypeDetected;) {
//                Annotation ann = (Annotation) it.next();
//                if (ann != null && ann.getType() != null && "javax.persistence.Id".equals(ann.getType().getName())) {
//                    if (feature instanceof Field) {
//                        fieldAccess = true;
//                    }
//                    accessTypeDetected = true;
//                }
//            }
//        }
//        return fieldAccess;
//    }
//    
//    /**
//     * A simple queue. An object can only be added once, even
//     * if it has already been removed from the queue. This class could implement
//     * the {@link java.util.Queue} interface, but it doesn't because that
//     * interface has too many unneeded methods. Not private because of the tests.
//     */
//    static final class Queue<T> {
//        
//        /**
//         * The queue. Implemented as ArrayList since will be iterated using get().
//         */
//        private final List<T> queue;
//        
//        /**
//         * The contents of the queue, needed in order to quickly (ideally
//         * in a constant time) tell if a table has been already added.
//         */
//        private final Set<T> contents;
//        
//        /**
//         * The position in the queue.
//         */
//        private int currentIndex;
//        
//        /**
//         * Creates a queue with an initial contents.
//         */
//        public Queue(Set<T> initialContents) {
//            assert !initialContents.contains(null);
//            
//            queue = new ArrayList(initialContents);
//            contents = new HashSet(initialContents);
//        }
//        
//        /**
//         * Adds an elements to the queue if it hasn't been already added.
//         */
//        public void offer(T element) {
//            assert element != null;
//            
//            if (!contents.contains(element)) {
//                contents.add(element);
//                queue.add(element);
//            }
//        }
//        
//        /**
//         * Returns the element at the top of the queue without removing it or null
//         * if the queue is empty.
//         */
//        public boolean isEmpty() {
//            return currentIndex >= queue.size();
//        }
//        
//        /**
//         * Returns and removes the elements at the top of the queue or null if
//         * the queue is empty.
//         */
//        public T poll() {
//            T result = null;
//            if (!isEmpty()) {
//                result = queue.get(currentIndex);
//                currentIndex++;
//            }
//            return result;
//        }
//    }
//
//}
