/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddCmpFieldAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddFinderMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddSelectMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.entity.EntityGenerator;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Type;
import org.openide.nodes.Node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pfiala
 */
public class EntityHelper extends EntityAndSessionHelper {

    private final Entity entity;
    public final CmpFields cmpFields;
    public final EntityHelper.Queries queries;
    private static final String PRIMARY_KEY_FINDER_METHOD = "findByPrimaryKey"; // NOI18N
    private EntityMethodController entityMethodController;


    public EntityHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, Entity entity) {
        super(ejbJarMultiViewDataObject, entity);
        this.entity = ((Entity) ejb);
        cmpFields = new CmpFields();
        queries = new Queries();
        entityMethodController = new EntityMethodController(entity, sourceClassPath,
                ejbJarMultiViewDataObject.getEjbJar());
        super.abstractMethodController = entityMethodController;
    }

    public Method createAccessMethod(String fieldName, Type type, boolean get) {
        entityMethodController.beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            JavaClass beanClass = getBeanClass();
            assert beanClass != null;
            entityMethodController.registerClassForSave(beanClass);
            Method prototype = JMIUtils.createMethod(beanClass);
            prototype.setName(Utils.getMethodName(fieldName, get));
            if (get) {
                prototype.setType(type);
            } else {
                prototype.getParameters().add(JMIUtils.createParameter(prototype, fieldName, type, false));
                prototype.setType(JMIUtils.resolveType("void"));
            }
            Utils.addMethod(beanClass, prototype, false, Modifier.PUBLIC | Modifier.ABSTRACT);
            Method accessMethod = Utils.getMethod(beanClass, prototype);
            rollback = false;
            return accessMethod;
        } finally {
            entityMethodController.endWriteJmiTransaction(rollback);
        }
    }

    public Method getSetterMethod(String fieldName, Method getterMethod) {
        if (getterMethod == null) {
            return null;
        } else {
            JMIUtils.beginJmiTransaction();
            try {
                return EntityMethodController.getSetterMethod(getBeanClass(), fieldName, getterMethod.getType());
            } finally {
                JMIUtils.endJmiTransaction();
            }
        }
    }

    public Method getGetterMethod(String fieldName) {
        return EntityMethodController.getGetterMethod(getBeanClass(), fieldName);
    }

    public void removeQuery(Query query) {
        entity.removeQuery(query);
        modelUpdatedFromUI();
    }

    public boolean hasLocalInterface() {
        return ejb.getLocal() != null;
    }

    public boolean hasRemoteInterface() {
        return ejb.getRemote() != null;
    }

    public String getPrimkeyField() {
        return entity.getPrimkeyField();
    }

    public String getPrimKeyClass() {
        return entity.getPrimKeyClass();
    }

    public void setPrimkeyFieldName(String fieldName) {
        entity.setPrimkeyField(fieldName);
    }

    public void setPrimkeyField(String fieldName) throws ClassNotFoundException {
        setPrimkeyFieldName(fieldName);
        if (fieldName != null) {
            CmpFieldHelper helper = cmpFields.getCmpFieldHelper(fieldName);
            helper.reloadType();
        }
        modelUpdatedFromUI();
    }

    public void setPrimKeyClass(Type newType) {
        List params = new LinkedList();
        params.add(JMIUtils.resolveType(entity.getPrimKeyClass()));
        changeFinderMethodParam(getLocalHomeInterfaceClass(), params, newType);
        changeFinderMethodParam(getHomeInterfaceClass(), params, newType);
        entity.setPrimKeyClass(newType.getName());
    }

    private void changeFinderMethodParam(JavaClass javaClass, List params, Type newType) {
        if (javaClass != null) {
            entityMethodController.registerClassForSave(javaClass);
            Method method = javaClass.getMethod(PRIMARY_KEY_FINDER_METHOD, params, false);
            Utils.changeParameterType(method, newType);
        }
    }

    protected EntityAndSessionGenerator getGenerator() {
        EntityGenerator generator = new EntityGenerator(entity.getPrimKeyClass());
        generator.setCMP(Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType()));
        return generator;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (queries == null) {
            return;
        }
        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        if (source == entity) {
            if ((oldValue instanceof CmpField || newValue instanceof CmpField)) {
                cmpFields.change(source, propertyName, oldValue, newValue);
            } else if ((oldValue instanceof Query || newValue instanceof Query)) {
                queries.change(source, propertyName, oldValue, newValue);
            }
        } else if (source instanceof CmpField) {
            cmpFields.change(source, propertyName, oldValue, newValue);
        } else if (source instanceof Query) {
            queries.change(source, propertyName, oldValue, newValue);
        }
    }

    public EntityMethodController getEntityMethodController() {
        return entityMethodController;
    }

    public void updateMethod(Method method, boolean local, boolean isComponent, boolean shouldExist) {
        entityMethodController.updateMethod(method, local, isComponent, shouldExist);
    }

    public void updateFieldAccessor(String fieldName, boolean getter, boolean local, boolean shouldExist) {
        if (local && hasLocalInterface() || !local && hasRemoteInterface()) {
            entityMethodController.updateFieldAccessor(fieldName, getter, local, shouldExist);
        }
    }

    public class CmpFields implements PropertyChangeSource {

        private List listeners = new LinkedList();
        private HashMap cmpFieldHelperMap = new HashMap();
        private CmpFieldsTableModel cmpFieldsTableModel = new CmpFieldsTableModel(this);

        public int getCmpFieldCount() {
            return entity.getCmpField().length;
        }

        public CmpFieldHelper getCmpFieldHelper(int row) {
            CmpField field = getCmpField(row);
            CmpFieldHelper cmpFieldHelper = (CmpFieldHelper) cmpFieldHelperMap.get(field);
            if (cmpFieldHelper == null) {
                cmpFieldHelper = createCmpFieldHelper(field);
            }
            return cmpFieldHelper;
        }

        private CmpFieldHelper getCmpFieldHelper(String fieldName) {
            CmpFieldHelper cmpFieldHelper = (CmpFieldHelper) cmpFieldHelperMap.get(fieldName);
            if (cmpFieldHelper == null) {
                CmpField[] cmpFields = entity.getCmpField();
                for (int i = 0; i < cmpFields.length; i++) {
                    CmpField field = cmpFields[i];
                    if (fieldName.equals(field.getFieldName())) {
                        cmpFieldHelper = createCmpFieldHelper(field);
                    }
                }
            }
            return cmpFieldHelper;
        }

        private CmpFieldHelper createCmpFieldHelper(CmpField field) {
            CmpFieldHelper cmpFieldHelper;
            cmpFieldHelper = new CmpFieldHelper(EntityHelper.this, field);
            cmpFieldHelperMap.put(field, cmpFieldHelper);
            return cmpFieldHelper;
        }

        private CmpField getCmpField(int row) {
            return getCmpFields()[row];
        }

        public CmpField[] getCmpFields() {
            CmpField[] cmpFields = entity.getCmpField();
            Arrays.sort(cmpFields, new Comparator() {
                public int compare(Object o1, Object o2) {
                    String s1 = ((CmpField) o1).getFieldName();
                    String s2 = ((CmpField) o2).getFieldName();
                    if (s1 == null) {
                        s1 = "";
                    }
                    if (s2 == null) {
                        s2 = "";
                    }
                    return s1.compareTo(s2);
                }
            });
            return cmpFields;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }

        public void change(Object source, String propertyName, Object oldValue, Object newValue) {
            if (source instanceof Entity) {
                cmpFieldHelperMap.keySet().retainAll(Arrays.asList(entity.getCmpField()));
            }
            firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
        }

        protected void firePropertyChange(PropertyChangeEvent evt) {
            for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
                ((PropertyChangeListener) iterator.next()).propertyChange(evt);
            }
        }

        public void addCmpField() {
            new AddCmpFieldAction().addCmpField(getBeanClass(), ejbJarFile);
            modelUpdatedFromUI();
        }

        public CmpFieldsTableModel getCmpFieldsTableModel() {
            return cmpFieldsTableModel;
        }

        public int getFieldRow(CmpField cmpField) {
            final CmpField[] fields = getCmpFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].equals(cmpField)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public class Queries implements PropertyChangeSource {
        private List listeners = new LinkedList();
        private HashMap queryMethodHelperMap = new HashMap();
        private Query[] selectMethods;
        private Query[] finderMethods;
        public static final String SELECT_PREFIX = "ejbSelect"; //NOI18N
        public static final String FIND_PREFIX = "find"; //NOI18N
        private FinderMethodsTableModel finderMethodsTableModel = new FinderMethodsTableModel(this);
        private SelectMethodsTableModel selectMethodsTableModel = new SelectMethodsTableModel(this);

        public Queries() {
            initQueryMethods();
        }

        private void initQueryMethods() {
            selectMethods = getQueries(SELECT_PREFIX);
            finderMethods = getQueries(FIND_PREFIX);
        }

        public QueryMethodHelper getQueryMethodHelper(Query query) {
            JMIUtils.beginJmiTransaction();
            try {
                QueryMethodHelper queryMethodHelper = (QueryMethodHelper) queryMethodHelperMap.get(query);
                if (queryMethodHelper == null) {
                    queryMethodHelper = new QueryMethodHelper(EntityHelper.this, query);
                    queryMethodHelperMap.put(query, queryMethodHelper);
                }
                return queryMethodHelper;
            } finally {
                JMIUtils.endJmiTransaction();
            }
        }

        public QueryMethodHelper getFinderMethodHelper(int row) {
            return getQueryMethodHelper(finderMethods[row]);
        }

        public QueryMethodHelper getSelectMethodHelper(int row) {
            return getQueryMethodHelper(selectMethods[row]);
        }

        private Query[] getQueries(String s) {
            List list = new LinkedList();
            Query[] queries = entity.getQuery();
            for (int i = 0; i < queries.length; i++) {
                Query query = queries[i];
                if (query.getQueryMethod().getMethodName().startsWith(s)) {
                    list.add(query);
                    final QueryMethodHelper helper = (QueryMethodHelper) queryMethodHelperMap.get(query);
                    if (helper != null) {
                        helper.init();
                    }

                }
            }
            return (Query[]) list.toArray(new Query[0]);
        }

        public void addFinderMethod() {
            new AddFinderMethodAction(null) {
                protected void performAction(Node[] activatedNodes) {
                    super.performAction(activatedNodes);
                }
            }.performAction(new Node[]{createEntityNode()});
            modelUpdatedFromUI();
        }

        public void addSelectMethod() {
            new AddSelectMethodAction(null) {
                protected void performAction(Node[] activatedNodes) {
                    super.performAction(activatedNodes);
                }
            }.performAction(new Node[]{createEntityNode()});
            modelUpdatedFromUI();
        }

        public int getFinderMethodCount() {
            return finderMethods.length;
        }

        public int getSelectMethodCount() {
            return selectMethods.length;
        }

        public Query getFinderMethod(int rowIndex) {
            return finderMethods[rowIndex];
        }

        public Query getSelecMethod(int rowIndex) {
            return selectMethods[rowIndex];
        }

        public void change(Object source, String propertyName, Object oldValue, Object newValue) {
            initQueryMethods();
            queryMethodHelperMap.keySet().retainAll(Arrays.asList(entity.getQuery()));
            firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
        }

        //todo
        public Query[] getQuery() {
            return entity.getQuery();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }

        protected void firePropertyChange(PropertyChangeEvent evt) {
            for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
                ((PropertyChangeListener) iterator.next()).propertyChange(evt);
            }
        }

        public String getLocal() {
            return EntityHelper.this.getLocal();
        }

        public String getRemote() {
            return EntityHelper.this.getRemote();
        }

        public FinderMethodsTableModel getFinderMethodsTableModel() {
            return finderMethodsTableModel;
        }

        public SelectMethodsTableModel getSelectMethodsTableModel() {
            return selectMethodsTableModel;
        }

        public int getSelectMethodRow(Query query) {
            for (int i = 0; i < selectMethods.length; i++) {
                if (query.equals(selectMethods[i])) {
                    return i;
                }
            }
            return -1;
        }

        public int getFinderMethodRow(Query query) {
            for (int i = 0; i < finderMethods.length; i++) {
                if (query.equals(finderMethods[i])) {
                    return i;
                }
            }
            return -1;
        }
    }
}
