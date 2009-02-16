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

package org.netbeans.modules.xml.schema.model.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 * Class is intended to do optimized resolving of a schema model by an 
 * Import/Include/Redefine. It provide caching and processing of changes in models.
 *
 * @author Nikita Krjukov
 */
public class RefCacheSupport {

    // Owning schema model.
    private SchemaModel mSModel;

    // The caching map.
    private WeakHashMap<SchemaModelReference, SchemaModelImpl> refModelCache =
            new WeakHashMap<SchemaModelReference, SchemaModelImpl>();

    // Listens self schema model.
    private PropertyChangeListener mPropertySelfListener = null;

    // Listens external schema models.
    private PropertyChangeListener mPropertyExtListener = null;

    public RefCacheSupport(SchemaModel sModel) {
        mSModel = sModel;
    }

    /**
     * Does optimized resolve. It means that the required schema model
     * can be taken from cache instead of being resolved again.
     * 
     * @param ref is an Import/Include/Redefine.
     * @return found schema model. 
     */
    public SchemaModelImpl optimizedResolve(SchemaModelReference ref) {
        try {
            SchemaModelImpl cachedModel = refModelCache.get(ref);
            if (cachedModel != null) {
                State cachedModelState = cachedModel.getState();
                if (cachedModelState == State.VALID) {
                    return cachedModel;
                } else {
                    // If the cached model is invalid then it has to be
                    // unsubscribed and removed from cached first.
                    // Otherwize it can remain in cache forever.
                    excludeModel(cachedModel);
                }
            }
            //
            SchemaModelImpl resolved = (SchemaModelImpl) ref.resolveReferencedModel();
            //
            if (resolved != null) {
                refModelCache.put(ref, resolved);
                lazySelfListenerInit();
                startListening(resolved);
            }
            return resolved;
        } catch (CatalogModelException ex) {
            return null;
        }
    }

    /**
     * Unsubscribes from all external schema models and clear the cache.
     * After execution the cash should be in the same state like it has 
     * just after creation.
     * The method can be helpful for finalization. 
     */
    public void discardCache() {
        for (SchemaModelImpl extSModel : refModelCache.values()) {
           extSModel.removePropertyChangeListener(mPropertyExtListener);
        }
        refModelCache.clear();
        mSModel.removePropertyChangeListener(mPropertySelfListener);
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
                    if (Schema.SCHEMA_REFERENCES_PROPERTY.equals(propName)) {
                        Object newValue = evt.getNewValue();
                        Object oldValue = evt.getOldValue();
                        if (newValue == null && oldValue != null &&
                                oldValue instanceof SchemaModelReference) {
                            //
                            // An Import/Include/Redefine is deleted.
                            SchemaModelReference sModelRef =
                                    SchemaModelReference.class.cast(oldValue);
                            excludeModelRef(sModelRef);
                            // System.out.println("removed from cache");
                        }
                    }
                    //
                    if (SchemaModelReference.SCHEMA_LOCATION_PROPERTY.equals(propName)) {
                        Object source = evt.getSource();
                        if (source instanceof Include) {
                            Include include = Include.class.cast(source);
                            if (include != null) {
                                excludeModelRef(include);
                            }
                            // System.out.println("schema location changed");
                        }
                    }
                    //
                    if (Model.STATE_PROPERTY.equals(propName)) {
                        Object newValue = evt.getNewValue();
                        Object source = evt.getSource();
                        if (newValue != State.VALID && source == mSModel) {
                            discardCache();
                            // System.out.println("schema is not valid");
                        }
                    }
                }
            };
            mSModel.addPropertyChangeListener(mPropertySelfListener);
        }
    }

    /**
     * Starts listening changes of the specified external schema model.
     */
    private void startListening(SchemaModel referencedModel) {
        if (mPropertyExtListener == null) {
            initExtListener();
        }
        referencedModel.addPropertyChangeListener(mPropertyExtListener);
    }

    /**
     * Stops listening changes of the specified external schema model.
     * @param referencedModel
     */
    private void stopListening(SchemaModel referencedModel) {
        referencedModel.removePropertyChangeListener(mPropertyExtListener);
    }

    /**
     * Excludes a schema model from the cache.
     * @param referencedModel
     */
    private void excludeModel(SchemaModel referencedModel) {
        //
        // Find cache entry to remove
        List<SchemaModelReference> toRemove = new ArrayList<SchemaModelReference>();
        for (Entry<SchemaModelReference, SchemaModelImpl> entry : refModelCache.entrySet()) {
           if (referencedModel.equals(entry.getValue())) {
               toRemove.add(entry.getKey());
           }
        }
        //
        // Remove
        for (SchemaModelReference smr : toRemove) {
            stopListening(referencedModel);
            refModelCache.remove(smr);
        }
    }

    /**
     * Excludes an Import/Include/Redefine from the cache.
     * @param sModelRef
     */
    private void excludeModelRef(SchemaModelReference sModelRef) {
        SchemaModelImpl sModel = refModelCache.get(sModelRef);
        if (sModel != null) {
            stopListening(sModel);
            refModelCache.remove(sModelRef);
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
                    Object newValue = evt.getNewValue();
                    Object source = evt.getSource();
                    if (newValue != State.VALID && source instanceof SchemaModel) {
                        excludeModel(SchemaModel.class.cast(source));
                        // System.out.println("schema is not valid");
                    }
                }
                if (Schema.TARGET_NAMESPACE_PROPERTY.equals(propName)) {
                    Object source = evt.getSource();
                    if (source instanceof SchemaModel) {
                        excludeModel(SchemaModel.class.cast(source));
                        // System.out.println("target namespace changed");
                    }
                }
            }
        };
    }
}
