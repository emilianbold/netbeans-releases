/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

/**
 * Simple lookup with modifiable content.
 *
 * @author Marian Petras
 * @since 1.9.1
 */
public class SimpleLookup extends Lookup {

    private static final Logger LOG = Logger.getLogger(SimpleLookup.class.getName());

    protected final Object dataSetLock = new Object();

    private final List<AbstractResult> results = new CopyOnWriteArrayList<AbstractResult>();
    private final Map<Lookup.Template,AbstractResult> resultsCache
                        = new HashMap<Lookup.Template,AbstractResult>(20, .75f);

    private Object[] data = new Object[0];

    public SimpleLookup() {
        super();
    }

    public void setData(Object... data) {
        validateData(data);
        data = rectifyData(data);
        if (data == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        setValidatedData(data);
    }

    /**
     * Validates the data.
     * The default implementation just checks that the input array is not
     * {@code null}.
     * @param  data  the input data
     * @exception  java.lang.IllegalArgumentException
     *             if the data are invalid, e.g. if the passed array is
     *             {@code null}
     */
    protected void validateData(Object[] data) throws IllegalArgumentException {
        if (data == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
    }

    /**
     * Rectifies the data.
     * This method is called after the data had passed
     * method {@code validateData()}.
     * The default implementation removes {@code null}s and duplicate items
     * from the array.
     *
     * @param  data  data to be rectified
     * @return  rectified data
     * @see  #validateData
     */
    protected Object[] rectifyData(Object[] data) {
        return CollectionUtils.removeItem(
                    CollectionUtils.removeDuplicates(data),
                    null);
    }

    protected void setValidatedData(Object[] data) {
        synchronized (dataSetLock) {
            setDataImpl(data);
        }
    }

    protected final void setDataImpl(Object[] data) {
        if (!Thread.holdsLock(dataSetLock)) {
            throw new IllegalStateException(
                    "This method must be called with the dataSetLock held being held by the current thread."); //NOI18N
        }
        if (data == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        this.data = data;
        dataChanged();
    }

    private final void dataChanged() {
        LOG.log(FINER, "dataChanged()");                                //NOI18N

        notifyResults();
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        LOG.log(FINER, "lookup ({0})", clazz.getName());                //NOI18N
        Object[] currData = data;
        for (Object o : currData) {
            if (clazz.isInstance(o)) {
                return clazz.cast(o);
            }
        }
        return null;
    }

    @Override
    public <T> Lookup.Result<T> lookup(Template<T> template) {
        if (template.getId() != null) {
            return new EmptyResult<T>();    //no items with Id's can be present
        }

        AbstractResult<T> result;

        synchronized (results) {
            result = resultsCache.get(template);
            if (result == null) {
                result = createLookupResult(template);
                resultsCache.put(template, result);
                results.add(result);
            }
        }
        return result;
    }

    private <T> AbstractResult<T> createLookupResult(Template<T> template) {
        Class<T> templateType = template.getType();
        T templateInstance = template.getInstance();
        assert template.getId() == null;

        AbstractResult<T> result;
        if (templateInstance == null) {
            if (templateType == null) {
                result = new AllResult();
            } else {
                result = new TypeResult(templateType);
            }
        } else {
            if (templateType == null) {
                result = new InstanceResult(templateInstance);
            } else {
                result = new TypeInstanceResult(templateType, templateInstance);
            }
        }
        return result;
    }

    private void notifyResults() {
        for (AbstractResult result : results) {
            result.dataChanged();
        }
    }

    public void cleanup() {
        synchronized (results) {
            resultsCache.clear();
        }
    }

    abstract class AbstractResult<T> extends Lookup.Result<T> {

        private final LookupListener[] listenersArray
                                       = new LookupListener[0];
        private final LookupEvent lookupEvent = new LookupEvent(this);
        private final List<LookupListener> listeners
                                   = new CopyOnWriteArrayList<LookupListener>();

        protected Collection<? extends T> allInstances;
        protected Collection<? extends Item<T>> allItems;
        protected Set<Class<? extends T>> allClasses;

        @Override
        public Collection<? extends T> allInstances() {
            Collection<? extends T> currentInstances = allInstances;
            if (currentInstances != null) {
                if (LOG.isLoggable(FINEST)) {
                    LOG.log(FINEST,
                            "allInstances({0}) - cache HIT",            //NOI18N
                            paramString());
                }
                return currentInstances;
            }

            return (allInstances = allInstancesImpl());
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            Collection<? extends Item<T>> currentItems = allItems;
            if (currentItems != null) {
                if (LOG.isLoggable(FINEST)) {
                    LOG.log(FINEST,
                            "allItems({0}) - cache HIT",                //NOI18N
                            paramString());
                }
                return currentItems;
            }

            return (allItems = allItemsImpl());
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            Set<Class<? extends T>> currentClasses = allClasses;
            if (currentClasses != null) {
                if (LOG.isLoggable(FINEST)) {
                    LOG.log(FINEST,
                            "allClasses({0}) - cache HIT",              //NOI18N
                            paramString());
                }
                return currentClasses;
            }

            return (allClasses = allClassesImpl());
        }

        protected abstract Collection<? extends T> allInstancesImpl();

        protected abstract Collection<? extends Item<T>> allItemsImpl();

        protected abstract Set<Class<? extends T>> allClassesImpl();

        @Override
        public void addLookupListener(LookupListener l) {
            assert listeners.getClass() == CopyOnWriteArrayList.class : "synchronization required"; //NOI18N
            listeners.add(l);
        }

        @Override
        synchronized public void removeLookupListener(LookupListener l) {
            assert listeners.getClass() == CopyOnWriteArrayList.class : "synchronization required"; //NOI18N
            listeners.remove(l);
        }

        protected LookupListener[] getListeners() {
            assert listeners.getClass() == CopyOnWriteArrayList.class : "synchronization required"; //NOI18N
            return listeners.toArray(listenersArray);
        }

        protected void notifyListeners(LookupListener[] listeners) {
            for (LookupListener l : listeners) {
                l.resultChanged(lookupEvent);
            }
        }

        protected void dataChanged() {
            LookupListener[] currListeners = getListeners();

            boolean resultChanged = updateData(currListeners.length != 0);

            if (resultChanged) {
                allInstances = null;
                allItems = null;
                allClasses = null;

                if (currListeners.length != 0) {
                    notifyListeners(currListeners);
                }
            }
        }

        /**
         * Updates internal data structures such that this result returns
         * the correct information when queried since now.
         * <p>
         * This method is called from the default implementation
         * of method {@link #dataChanged dataChanged()}.
         * If method {@link #dataChanged dataChanged()} is overridden
         * in such a way that it does not call this method,
         * this method may be implemented as a no-op, returning {@code true}
         * or {@code false}.
         *
         * @param  listenersRegistered  {@code true} if at least one listener
         *                              is registered, {@code false} otherwise
         * @return  {@code true} if the previous data were found out-of-date
         *          so the cache needs to be refreshed and listeners notified;
         *          {@code false} otherwise
         */
        protected abstract boolean updateData(boolean listenersRegistered);

        protected abstract String paramString();

    }

    class AllResult<T> extends AbstractResult<T> {

        /** matching instances */
        protected T[] instances;

        AllResult() {
            super();
        }

        protected Collection<? extends T> allInstancesImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "allInstances({0}) - cache miss",               //NOI18N
                        paramString());
            }
            return CollectionUtils.unmodifiableList(getInstances());
        }

        protected Collection<? extends Item<T>> allItemsImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "allItems({0}) - cache miss",                   //NOI18N
                        paramString());
            }
            T[] insts = getInstances();
            Item[] items = new Item[insts.length];
            for (int i = 0; i < items.length; i++) {
                items[i] = new LookupItem(insts[i]);
            }
            return CollectionUtils.unmodifiableList((Item<T>[]) items);
        }

        protected Set<Class<? extends T>> allClassesImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "allClasses({0}) - cache miss",                 //NOI18N
                        paramString());
            }
            T[] insts = getInstances();
            Class[] classes = new Class[insts.length];

            for (int i = 0; i < classes.length; i++) {
                classes[i] = insts[i].getClass();
            }
            return CollectionUtils.unmodifiableSet(
                                            (Class<? extends T>[]) classes);
        }

        protected T[] getInstances() {
            T[] currentInstances = instances;
            if (currentInstances != null) {
                if (LOG.isLoggable(FINEST)) {
                    LOG.log(FINEST,
                            "getInstances({0}) - cache HIT",            //NOI18N
                            paramString());
                }
                return currentInstances;
            }

            return (instances = getInstancesImpl());
        }

        protected T[] getInstancesImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "getInstances({0}) - cache miss",               //NOI18N
                        paramString());
            }
            return (T[]) SimpleLookup.this.data;
        }

        protected boolean updateData(boolean listenersRegistered) {
            boolean dataChanged;

            T[] oldInstances;
            T[] newInstances;

            oldInstances = instances;
            if (listenersRegistered) {
                newInstances = getInstancesImpl();
                dataChanged = !CollectionUtils.containSameObjects(oldInstances,
                                                                  newInstances);
            } else {
                dataChanged = true;
                newInstances = null;
            }
            instances = newInstances;

            return dataChanged;
        }

        @Override
        protected String paramString() {
            return "AllResults";                                        //NOI18N
        }

    }

    class TypeResult<T> extends AllResult<T> {

        /** kind of data held by this lookup result */
        protected final Class<T> requestedType;

        TypeResult(Class<T> requestedType) {
            super();
            this.requestedType = requestedType;
        }

        @Override
        protected T[] getInstancesImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "getInstances({0}) - cache miss",               //NOI18N
                        paramString());
            }
            Object[] currData = SimpleLookup.this.data;
            Object[] result = new Object[currData.length];
            int count = 0;
            for (Object o : currData) {
                if (requestedType.isInstance(o)) {
                    result[count++] = o;
                }
            }
            return (T[]) CollectionUtils.shortenArray(result, count);
        }

        @Override
        protected String paramString() {
            return "TypeResult(" + requestedType.getSimpleName() + ')'; //NOI18N
        }

    }

    class InstanceResult<T> extends AbstractResult<T> {

        protected final T UNKNOWN = (T) new Object();

        /** kind of data held by this lookup result */
        protected final T requestedInstance;

        protected T instance = UNKNOWN;

        InstanceResult(T requestedInstance) {
            if (requestedInstance == null) {
                throw new IllegalArgumentException("null instance"); //NOI18N
            }
            this.requestedInstance = requestedInstance;
        }

        protected Collection<? extends T> allInstancesImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "allInstances({0}) - cache miss",               //NOI18N
                        paramString());
            }
            T inst = getInstance();
            return (inst != null)
                   ? Collections.<T>singletonList(inst)
                   : Collections.<T>emptyList();
        }

        protected Collection<? extends Item<T>> allItemsImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "allItems({0}) - cache miss",                   //NOI18N
                        paramString());
            }
            T inst = getInstance();
            Item<T> item = (inst != null)
                           ? new LookupItem<T>(inst)
                           : null;
            return (item != null)
                   ? Collections.<Item<T>>singletonList(item)
                   : Collections.<Item<T>>emptyList();
        }

        protected Set<Class<? extends T>> allClassesImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "allClasses({0}) - cache miss",                 //NOI18N
                        paramString());
            }
            T inst = getInstance();
            Class<? extends T> clazz = (inst != null)
                                       ? (Class<? extends T>) inst.getClass()
                                       : null;
            return (clazz != null)
                   ? Collections.<Class<? extends T>>singleton(clazz)
                   : Collections.<Class<? extends T>>emptySet();
        }

        protected T getInstance() {
            T currentInstance = instance;
            if (currentInstance != UNKNOWN) {
                if (LOG.isLoggable(FINEST)) {
                    LOG.log(FINEST,
                            "getInstance({0}) - cache HIT",             //NOI18N
                            paramString());
                }
                return currentInstance;
            }

            return (instance = getInstanceImpl());
        }

        protected T getInstanceImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "getInstance({0}) - cache miss",                //NOI18N
                        paramString());
            }
            Object[] currData = SimpleLookup.this.data;
            Object result = null;
            for (Object o : currData) {
                if (o == requestedInstance) {
                    result = o;
                    break;
                }
            }
            return (T) result;
        }

        protected boolean updateData(boolean listenersRegistered) {
            boolean dataChanged;

            T oldInstance;
            T newInstance;

            oldInstance = instance;
            if (listenersRegistered) {
                newInstance = getInstanceImpl();
                dataChanged = (newInstance != oldInstance);
            } else {
                dataChanged = true;
                newInstance = UNKNOWN;
            }
            instance = newInstance;

            return dataChanged;
        }

        @Override
        protected String paramString() {
            return "InstanceResult(0x"                                  //NOI18N
                   + Integer.toHexString(System.identityHashCode(requestedInstance))
                   + ')';
        }

    }

    class TypeInstanceResult<T> extends InstanceResult<T> {

        protected final Class<T> requestedType;

        TypeInstanceResult(Class<T> requestedType, T requestedInstance) {
            super(requestedInstance);
            this.requestedType = requestedType;
        }

        @Override
        protected T getInstanceImpl() {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST,
                        "getInstance({0}) - cache miss",                //NOI18N
                        paramString());
            }
            Object[] currData = SimpleLookup.this.data;
            Object result = null;
            for (Object o : currData) {
                if (o == requestedInstance) {
                    if (requestedType.isInstance(o)) {
                        result = o;
                    }
                    break;
                }
            }
            return (T) result;
        }

        @Override
        protected String paramString() {
            StringBuilder buf = new StringBuilder(100);
            buf.append("TypeInstanceResult(")                           //NOI18N
               .append(requestedType.getSimpleName())
               .append(", 0x")                                          //NOI18N
               .append(Integer.toHexString(System.identityHashCode(requestedInstance)))
               .append(')');
            return buf.toString();
        }

    }

    static class EmptyResult<T> extends Lookup.Result<T> {

        @Override
        public void addLookupListener(LookupListener l) {
            //the data never change - no need to register/unregister currListeners
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            //the data never change - no need to register/unregister currListeners
        }

        @Override
        public Collection<? extends T> allInstances() {
            return Collections.emptyList();
        }

    }

    static class LookupItem<T> extends Lookup.Item<T> {

        private final T instance;

        LookupItem(T instance) {
            this.instance = instance;
        }

        @Override
        public T getInstance() {
            return instance;
        }

        @Override
        public Class<? extends T> getType() {
            return (Class<? extends T>) instance.getClass();
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return instance.toString();
        }

    }

}
