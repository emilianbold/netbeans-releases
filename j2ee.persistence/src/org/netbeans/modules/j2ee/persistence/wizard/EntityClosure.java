/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.persistence.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper.State;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Buzek
 */
public class EntityClosure {
    
    // XXX this class needs a complete rewrite: the computing of the available 
    // entity classes and of the referenced classes need to be moved away.
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    private Set<Entity> availableEntityInstances = new HashSet<Entity>();
    private Set<String> availableEntities = new HashSet<String>();
    private Set<String> wantedEntities = new HashSet<String>();
    private Set<String> selectedEntities = new HashSet<String>();
    private Set<String> referencedEntities = new HashSet<String>();
    
    private boolean closureEnabled = true;
    private Project project;
    
    private final MetadataModelReadHelper<EntityMappingsMetadata, List<Entity>> readHelper;
    
    private final MetadataModel<EntityMappingsMetadata> model;
    
    public static EntityClosure create(EntityClassScope entityClassScope, Project project) {
        EntityClosure ec = new EntityClosure(entityClassScope, project);
        ec.initialize();
        return ec;
    }

    public static ComboBoxModel getAsComboModel(EntityClosure ec) {
        return new EntityClosureComboBoxModel(ec);
    }
    
    private EntityClosure(EntityClassScope entityClassScope, Project project) {
        this.model = entityClassScope.getEntityMappingsModel(true);
        this.project = project;
        readHelper = MetadataModelReadHelper.create(model, new MetadataModelAction<EntityMappingsMetadata, List<Entity>>() {
            public List<Entity> run(EntityMappingsMetadata metadata) {
                return Arrays.<Entity>asList(metadata.getRoot().getEntity());
            }
        });
    }
    
    private void initialize() {
        readHelper.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (readHelper.getState() == State.FINISHED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                addAvaliableEntities(new HashSet<Entity>(readHelper.getResult()));
                                changeSupport.fireChange();
                            } catch (ExecutionException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    });
                }
            }
        });
        readHelper.start();
    }
    
    public void addAvaliableEntities(Set<Entity> entities) {
        availableEntityInstances.addAll(entities);
        for (Entity en : entities) {
            availableEntities.add(en.getClass2());
        }
        availableEntities.removeAll(selectedEntities);
        changeSupport.fireChange();
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public Set<String> getAvailableEntities() {
        return availableEntities;
    }

    public Set<Entity> getAvailableEntityInstances() {
        return availableEntityInstances;
    }
    
    public Set<String> getWantedEntities() {
        return wantedEntities;
    }
    
    public Set<String> getSelectedEntities() {
        return selectedEntities;
    }
    
    public void addEntities(Set<String> entities) {
        if (isClosureEnabled()) {
            if (wantedEntities.addAll(entities)) {
                try{
                    Set<String> refEntities = getReferencedEntitiesTransitively(entities);
                    Set<String> addedEntities = new HashSet<String>(entities);
                    addedEntities.addAll(refEntities);
                    
                    selectedEntities.addAll(addedEntities);
                    referencedEntities.addAll(refEntities);
                    availableEntities.removeAll(addedEntities);
                    
                    changeSupport.fireChange();
                } catch (IOException ioe){
                    Exceptions.printStackTrace(ioe);
                }
            }
        } else {
            wantedEntities.addAll(entities);
            selectedEntities.addAll(entities);
            availableEntities.removeAll(entities);
            
            changeSupport.fireChange();
        }
    }
    
    public void removeEntities(Set<String> entities) {
        if (isClosureEnabled()) {
            if (wantedEntities.removeAll(entities)) {
                redoClosure();
                
                changeSupport.fireChange();
            }
        } else {
            wantedEntities.removeAll(entities);
            selectedEntities.removeAll(entities);
            availableEntities.addAll(entities);
            
            changeSupport.fireChange();
        }
    }
    
    public void addAllEntities() {
        wantedEntities.addAll(availableEntities);
        
        if (isClosureEnabled()) {
            redoClosure();
            
            changeSupport.fireChange();
        } else {
            selectedEntities.addAll(wantedEntities);
            availableEntities.clear();
            
            changeSupport.fireChange();
        }
    }
    
    public void removeAllEntities() {
        availableEntities.addAll(selectedEntities);
        wantedEntities.clear();
        selectedEntities.clear();
        referencedEntities.clear();
        
        changeSupport.fireChange();
    }
    
    
    /**
     * Returns the tables transitively referenced by the contents of the tables parameter
     * (not including tables passed in this parameter). If a table references itself,
     * it is not added to the result.
     */
    private Set<String> getReferencedEntitiesTransitively(Set<String> entities) throws IOException {
        
        Queue<String> entityQueue = new Queue<String>(entities);
        Set<String> refEntities = new HashSet<String>();
        
        while (!entityQueue.isEmpty()) {
            String entity = entityQueue.poll();
            
            Set<String> referenced = getReferencedEntities(entity);
            for (String refEntity : referenced) {
                
                if (!refEntity.equals(entity)) {
                    refEntities.add(refEntity);
                }
                entityQueue.offer(refEntity);
            }
        }
        
        return refEntities;
    }
    
    private Set<String> getReferencedEntities(final String entityClass) throws IOException {
        
        if (readHelper.getState() != State.FINISHED) {
            return Collections.emptySet();
        }

        JavaSource source = model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, JavaSource>() {
            public JavaSource run(EntityMappingsMetadata metadata) throws Exception {
                return metadata.createJavaSource();
            }
        });
                
        final Set<String> result = new HashSet<String>();

        source.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                TypeElement entity = parameter.getElements().getTypeElement(entityClass);
                for (Element element : entity.getEnclosedElements()){
                    if (ElementKind.METHOD.equals(element.getKind()) || ElementKind.FIELD.equals(element.getKind())){
                        String typeClass = element.asType().toString();
                        if (readHelper.getResult().contains(typeClass)){
                            result.add(typeClass);
                        }
                    }
                }
            }
        }, true);
        
        return result;
    }

    private void redoClosure() {
        Set<String> allEntities = new HashSet<String>(availableEntities);
        allEntities.addAll(selectedEntities);
        
        referencedEntities.clear();
        try{
            referencedEntities.addAll(getReferencedEntitiesTransitively(wantedEntities));
        }catch (IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        
        
        selectedEntities.clear();
        selectedEntities.addAll(wantedEntities);
        selectedEntities.addAll(referencedEntities);
        
        availableEntities.clear();
        availableEntities.addAll(allEntities);
        availableEntities.removeAll(selectedEntities);
    }
    
    public boolean isClosureEnabled() {
        return closureEnabled;
    }
    
    public void setClosureEnabled(boolean closureEnabled) {
        if (this.closureEnabled == closureEnabled) {
            return;
        }
        this.closureEnabled = closureEnabled;
        if (closureEnabled) {
            redoClosure();
        } else {
            Set<String> allEntities = new HashSet<String>(availableEntities);
            allEntities.addAll(selectedEntities);
            
            referencedEntities.clear();
            
            selectedEntities.clear();
            selectedEntities.addAll(wantedEntities);
            
            availableEntities.clear();
            availableEntities.addAll(allEntities);
            availableEntities.removeAll(selectedEntities);
        }
        changeSupport.fireChange();
    }
    
    public boolean isModelReady() {
        return true; //TODO
    }
    
    void waitModelIsReady(){
        try {
            Future result = model.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, Boolean>() {

                public Boolean run(EntityMappingsMetadata metadata) throws Exception {
                    return true;
                }
            });
            result.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    private boolean isFieldAccess(final String entity) throws MetadataModelException, IOException {
        Boolean result = model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Boolean>() {
            
            public Boolean run(EntityMappingsMetadata metadata) throws Exception {
                for (Entity e : metadata.getRoot().getEntity()){
                    if (e.getClass2().equals(entity)){
                        return e.getAccess().equals(Entity.FIELD_ACCESS);
                    }
                }
                return false;
            }
        });
        
        return result;
    }
    
    /**
     * A simple queue. An object can only be added once, even
     * if it has already been removed from the queue. This class could implement
     * the {@link java.util.Queue} interface, but it doesn't because that
     * interface has too many unneeded methods. Not private because of the tests.
     */
    static final class Queue<T> {
        
        /**
         * The queue. Implemented as ArrayList since will be iterated using get().
         */
        private final List<T> queue;
        
        /**
         * The contents of the queue, needed in order to quickly (ideally
         * in a constant time) tell if a table has been already added.
         */
        private final Set<T> contents;
        
        /**
         * The position in the queue.
         */
        private int currentIndex;
        
        /**
         * Creates a queue with an initial contents.
         */
        public Queue(Set<T> initialContents) {
            assert !initialContents.contains(null);
            
            queue = new ArrayList(initialContents);
            contents = new HashSet(initialContents);
        }
        
        /**
         * Adds an elements to the queue if it hasn't been already added.
         */
        public void offer(T element) {
            assert element != null;
            
            if (!contents.contains(element)) {
                contents.add(element);
                queue.add(element);
            }
        }
        
        /**
         * Returns the element at the top of the queue without removing it or null
         * if the queue is empty.
         */
        public boolean isEmpty() {
            return currentIndex >= queue.size();
        }
        
        /**
         * Returns and removes the elements at the top of the queue or null if
         * the queue is empty.
         */
        public T poll() {
            T result = null;
            if (!isEmpty()) {
                result = queue.get(currentIndex);
                currentIndex++;
            }
            return result;
        }
    }

    private static class EntityClosureComboBoxModel extends DefaultComboBoxModel implements ChangeListener {

        private EntityClosure entityClosure;
        private List<String> entities = new ArrayList<String>();

        EntityClosureComboBoxModel(EntityClosure entityClosure) {
            this.entityClosure = entityClosure;
            entityClosure.addChangeListener(this);
            refresh();
        }

        @Override
        public int getSize() {
            return entities.size();
        }

        @Override
        public Object getElementAt(int index) {
            return entities.get(index);
        }

        /**
         * @return the fully qualified names of the entities in this model.
         */
        public List<String> getEntityClasses() {
            return entities;
        }

        public void stateChanged(ChangeEvent e) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            entities = new ArrayList<String>(entityClosure.getAvailableEntities());
            Collections.sort(entities);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }
    }


}
