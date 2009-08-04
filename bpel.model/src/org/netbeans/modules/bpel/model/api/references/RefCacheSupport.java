/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.model.api.references;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.util.WeakListeners;

/**
 * Class is intended to do optimized resolving of a schema/wsdl model by a BPEL
 * Import. It provide caching and processing of changes in models.
 *
 * @author Nikita Krjukov
 */
public class RefCacheSupport {

    public static final long UNRESOLVED_EXPIRATION_DELAY = 5000;

    // Owning schema model.
    private BpelModel mModel;

    // The caching map.
    private WeakHashMap<Import, Object> refModelCache =
            new WeakHashMap<Import, Object>();

    // Listens self schema model. TODO: REMOVE
    private PropertyChangeListener mPropertySelfListener = null;

    // Listens self schema model.
    private ChangeEventListener mChangeEventListener = null;

    // Listens external schema models.
    private PropertyChangeListener mPropertyExtListener = null;

    public RefCacheSupport(BpelModel sModel) {
        mModel = sModel;
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public int getCachedModelsSize() {
        return refModelCache.size();
    }

    /**
     * Checks that all cached schema model references relate to the cache's owner model.
     * It is mainly intended to be used by JUnit tests.
     *
     * @return count of unappropriate items in the cache
     */
    public int checkKeys() {
        int wrongModelCounter = 0;
        for (Import imp : refModelCache.keySet()) {
            if (imp.getModel() != mModel) {
                wrongModelCounter++;
            }
        }
        return wrongModelCounter;
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public WSDLModel getCachedWsdlModel(Import imp) {
        Object cachedValue = refModelCache.get(imp);
        if (cachedValue != null && cachedValue instanceof WsdlMAttachment) {
            return WsdlMAttachment.class.cast(cachedValue).mWModel;
        } else {
            return null;
        }
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public SchemaModel getCachedSchemaModel(Import imp) {
        Object cachedValue = refModelCache.get(imp);
        if (cachedValue != null && cachedValue instanceof SmAttachment) {
            return SmAttachment.class.cast(cachedValue).mSModel;
        } else {
            return null;
        }
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public boolean contains(Object model) {
        return refModelCache.containsValue(model);
    }

    /**
     * Does optimized resolve. It means that the required schema model
     * can be taken from cache instead of being resolved again.
     *
     * @param imp.
     * @return found schema model.
     */
    public SchemaModel optimizedSchemaResolve(Import imp) {
        return optimizedResolve(imp, SchemaModel.class);
    }

    /**
     * Does optimized resolve. It means that the required WSDL model
     * can be taken from cache instead of being resolved again.
     *
     * @param imp.
     * @return found WSDL model.
     */
    public WSDLModel optimizedWsdlResolve(Import imp) {
        return optimizedResolve(imp, WSDLModel.class);
    }

    private <T extends Model> T optimizedResolve(Import imp, Class<T> modelClass) {
        Object cachedValue = refModelCache.get(imp);
        if (cachedValue != null) {
            if (cachedValue instanceof SmAttachment) {
                assert modelClass == SchemaModel.class;
                //
                SmAttachment sma = SmAttachment.class.cast(cachedValue);
                SchemaModel cachedModel = sma.mSModel;
                State cachedModelState = cachedModel.getState();
                if (cachedModelState == State.VALID) {
                    return (T)cachedModel;
                } else {
                    // If the cached model is invalid then it has to be
                    // unsubscribed and removed from cached first.
                    // Otherwize it can remain in cache forever.
                    excludeModel(cachedModel);
                }
            } else if (cachedValue instanceof WsdlMAttachment) {
                assert modelClass == WSDLModel.class;
                //
                WsdlMAttachment wma = WsdlMAttachment.class.cast(cachedValue);
                WSDLModel cachedModel = wma.mWModel;
                State cachedModelState = cachedModel.getState();
                if (cachedModelState == State.VALID) {
                    return (T)cachedModel;
                } else {
                    // If the cached model is invalid then it has to be
                    // unsubscribed and removed from cached first.
                    // Otherwize it can remain in cache forever.
                    excludeModel(cachedModel);
                }
            } else if (cachedValue instanceof UnresolvedRef) {
                if (System.currentTimeMillis() <
                        UnresolvedRef.class.cast(
                        cachedValue).expiratoinTime) {
                    //
                    // The unresolved schema reference hasn't expared yet.
                    return null;
                }
            }
        }
        //
        if (modelClass == SchemaModel.class) {
            SchemaModel resolved = ImportHelper.getSchemaModel(imp);
            //
            if (resolved != null) {
                attachSchemaModel(imp, resolved);
            } else {
                refModelCache.put(imp, new UnresolvedRef());
            }
            return (T)resolved;
        } else if (modelClass == WSDLModel.class) {
            WSDLModel resolved = ImportHelper.getWsdlModel(imp);
            //
            if (resolved != null) {
                attachWsdlModel(imp, resolved);
            } else {
                refModelCache.put(imp, new UnresolvedRef());
            }
            return (T)resolved;
        } else {
            assert false : "Only SchemaModel or WSDLModel are allowed here!";
        }
        return null;
    }

    /**
     * Unsubscribes from all external models and clear the cache.
     * After execution the cash should be in the same state like it has 
     * just after creation.
     * The method can be helpful for finalization. 
     */
    public void discardCache() {
        for (Object cachedValue : refModelCache.values()) {
            if (cachedValue instanceof SmAttachment) {
                SmAttachment sma = SmAttachment.class.cast(cachedValue);
                sma.mSModel.removePropertyChangeListener(sma.mPCL);
            }
            if (cachedValue instanceof WsdlMAttachment) {
                WsdlMAttachment wma = WsdlMAttachment.class.cast(cachedValue);
                wma.mWModel.removePropertyChangeListener(wma.mPCL);
            }
        }
        refModelCache.clear();
        mModel.removePropertyChangeListener(mPropertySelfListener);
    }

    /**
     * Initializes a listener, which listens self model.
     * It looks for removing of an Import/Include/Redefine.
     * If an Import/Include/Redefine is removed, it is excluded from
     * the cache. 
     */
    private void lazySelfListenerInit() {
        //
        if (mPropertySelfListener == null) {
            mPropertySelfListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String propName = evt.getPropertyName();
                    //
                    if (Model.STATE_PROPERTY.equals(propName)) {
                        Object oldValue = evt.getOldValue();
                        Object newValue = evt.getNewValue();
                        Object source = evt.getSource();
                        if (newValue != State.VALID && source == mModel) {
                            discardCache();
                            // System.out.println("BPEL is not valid");
                        }
                        if (oldValue != State.VALID && newValue == State.VALID
                                && source == mModel) {
                            // Discard again for other case. 
                            discardCache();
                            // System.out.println("BPEL has become valid");
                        }
                    }
                }
            };
            mModel.addPropertyChangeListener(mPropertySelfListener);
        }
        //
        if (mChangeEventListener == null) {
            //
            mChangeEventListener = new ChangeEventListener() {
                public void notifyPropertyRemoved(PropertyRemoveEvent event) {
                    BpelEntity parentEntity = event.getParent();
                    if (parentEntity instanceof Import) {
                        // An attribute is removed from an Import
                        // Remove the modified Import from the cache.
                        excludeModelRef(Import.class.cast(parentEntity));
                    }
                }

                public void notifyPropertyUpdated(PropertyUpdateEvent event) {
                    BpelEntity parentEntity = event.getParent();
                    if (parentEntity instanceof Import) {
                        // An attribute is modified in an Import
                        // Remove the modified Import from the cache.
                        excludeModelRef(Import.class.cast(parentEntity));
                    }
                }

                public void notifyEntityInserted(EntityInsertEvent event) {
                    // Do nothing
                }

                public void notifyEntityRemoved(EntityRemoveEvent event) {
                    BpelEntity oldEntity = event.getOldValue();
                    if (oldEntity instanceof Import) {
                        // Remove the deleted Import from the cache.
                        excludeModelRef(Import.class.cast(oldEntity));
                    }
                }

                public void notifyEntityUpdated(EntityUpdateEvent event) {
                    // Do nothing
                }

                public void notifyArrayUpdated(ArrayUpdateEvent event) {
                    // Do nothing
                }
            };
            //
            mModel.addEntityChangeListener(mChangeEventListener);
        }
    }

    /**
     * Creates attachment between a BPEL Import and the corresponding model.
     * All required activities are made synchronized:
     *  - new weak listener is created and subscribtion is adde to listen
     * changes of the referenced model.
     *  - the model and the listener is added to cache
     */
    private synchronized void attachSchemaModel(Import imp, SchemaModel referencedModel) {
        //
        lazySelfListenerInit();
        //
        if (mPropertyExtListener == null) {
            initExtListener();
        }
        PropertyChangeListener weakListener = WeakListeners.propertyChange(
                mPropertySelfListener, referencedModel);
        //
        referencedModel.addPropertyChangeListener(weakListener);
        //
        refModelCache.put(imp, new SmAttachment(referencedModel, weakListener));
    }

    /**
     * Creates attachment between a BPEL Import and the corresponding model.
     * All required activities are made synchronized:
     *  - new weak listener is created and subscribtion is adde to listen
     * changes of the referenced model.
     *  - the model and the listener is added to cache
     */
    private synchronized void attachWsdlModel(Import imp, WSDLModel referencedModel) {
        //
        lazySelfListenerInit();
        //
        if (mPropertyExtListener == null) {
            initExtListener();
        }
        PropertyChangeListener weakListener = WeakListeners.propertyChange(
                mPropertySelfListener, referencedModel);
        //
        referencedModel.addPropertyChangeListener(weakListener);
        //
        refModelCache.put(imp, new WsdlMAttachment(referencedModel, weakListener));
    }

    /**
     * Excludes a imported (Schema or WSDL) model from the cache.
     * @param referencedModel
     */
    private synchronized void excludeModel(Model referencedModel) {
        //
        // Find cache entry to remove
        List<Import> toRemove = new ArrayList<Import>();
        for (Entry<Import, Object> entry : refModelCache.entrySet()) {
            Object cachedValue = entry.getValue();
            if (cachedValue instanceof SmAttachment) {
                SmAttachment sma = SmAttachment.class.cast(cachedValue);
                if (referencedModel.equals(sma.mSModel)) {
                    toRemove.add(entry.getKey());
                }
            } else if (cachedValue instanceof WsdlMAttachment) {
                WsdlMAttachment wma = WsdlMAttachment.class.cast(cachedValue);
                if (referencedModel.equals(wma.mWModel)) {
                    toRemove.add(entry.getKey());
                }
            }
        }
        //
        // Remove
        for (Import imp : toRemove) {
            excludeModelRef(imp);
        }
    }

    /**
     * Excludes an Import/Include/Redefine from the cache.
     * @param sModelRef
     */
    private synchronized void excludeModelRef(Import imp) {
        Object oldValue = refModelCache.remove(imp);
        //
        if (oldValue != null) {
            if (oldValue instanceof SmAttachment) {
                SmAttachment sma = SmAttachment.class.cast(oldValue);
                sma.mSModel.removePropertyChangeListener(sma.mPCL);
            } else if (oldValue instanceof WsdlMAttachment) {
                WsdlMAttachment wma = WsdlMAttachment.class.cast(oldValue);
                wma.mWModel.removePropertyChangeListener(wma.mPCL);
            }
        }
    }

    /**
     * Creates a listener which processes change events from external models.
     * Only relevant changes are taken into account.
     */
    private void initExtListener() {
        mPropertyExtListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (Model.STATE_PROPERTY.equals(propName)) {
                    Object oldValue = evt.getOldValue();
                    Object newValue = evt.getNewValue();
                    Object source = evt.getSource();
                    if (newValue != State.VALID && source instanceof SchemaModel) {
                        excludeModel(SchemaModel.class.cast(source));
                        // System.out.println("schema is not valid");
                    }
                    if (oldValue != State.VALID && newValue == State.VALID
                            && source == mModel) {
                        // Remove schema again for other case.
                        excludeModel(SchemaModel.class.cast(source));
                        // System.out.println("schema has become valid");
                    }
                }
                if (Schema.TARGET_NAMESPACE_PROPERTY.equals(propName)) {
                    Object source = evt.getSource();
                    if (source instanceof Schema) {
                        Schema schema = Schema.class.cast(source);
                        excludeModel(schema.getModel());
                        // System.out.println("target namespace changed");
                    }
                }
            }
        };
    }

    private static class WsdlMAttachment {
        public WSDLModel mWModel;
        public PropertyChangeListener mPCL;

        public WsdlMAttachment(WSDLModel wModel, PropertyChangeListener pcl) {
            assert wModel != null && pcl != null;
            //
            mWModel = wModel;
            mPCL = pcl;
        }
    }

    private static class SmAttachment {
        public SchemaModel mSModel;
        public PropertyChangeListener mPCL;

        public SmAttachment(SchemaModel sModel, PropertyChangeListener pcl) {
            assert sModel != null && pcl != null;
            //
            mSModel = sModel;
            mPCL = pcl;
        }
    }

    private static class UnresolvedRef {
        public long expiratoinTime;

        public UnresolvedRef() {
            expiratoinTime = System.currentTimeMillis() + UNRESOLVED_EXPIRATION_DELAY;
        }
    }

}
