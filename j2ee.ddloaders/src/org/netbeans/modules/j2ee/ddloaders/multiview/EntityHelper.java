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
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;

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


    public EntityHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, Entity entity) {
        super(ejbJarMultiViewDataObject, entity);
        this.entity = ((Entity) ejb);
        cmpFields = new CmpFields();
        queries = new Queries();
    }

    public MethodElement createAccessMethod(String fieldName, Type type, boolean get) {
        MethodElement method = new MethodElement();
        try {
            method.setName(Identifier.create(Utils.getMethodName(fieldName, get)));
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
        if (get) {
            try {
                method.setReturn(type);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        } else {
            try {
                method.setParameters(
                        new MethodParameter[]{new MethodParameter(fieldName, type, false)});
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
        Utils.addMethod(beanClass, method, false, Modifier.PUBLIC | Modifier.ABSTRACT);
        return Utils.getMethod(beanClass, method);
    }

    public MethodElement getSetterMethod(String fieldName, MethodElement getterMethod) {
        return getterMethod == null ?
                null : EntityMethodController.getSetterMethod(beanClass, fieldName, getterMethod);
    }

    public MethodElement getGetterMethod(String fieldName) {
        return EntityMethodController.getGetterMethod(beanClass, fieldName);
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

    public void setPrimkeyField(String fieldName) throws ClassNotFoundException {
        entity.setPrimkeyField(fieldName);
        if (fieldName != null) {
            CmpFieldHelper helper = cmpFields.getCmpFieldHelper(fieldName);
            helper.reloadType();
        }
        modelUpdatedFromUI();
    }

    public void setPrimKeyClass(Type newType) {
        Identifier primaryMethod = Identifier.create("findByPrimaryKey");
        ClassElement classElement = getLocalHomeInterfaceClass();
        Type[] origArguments = new Type[]{Type.parse(entity.getPrimKeyClass())};
        if (classElement != null) {
            MethodElement method = classElement.getMethod(primaryMethod, origArguments);
            Utils.changeParameterType(method, newType);
        }
        classElement = getHomeInterfaceClass();
        if (classElement != null) {
            MethodElement method = classElement.getMethod(primaryMethod, origArguments);
            Utils.changeParameterType(method, newType);
        }
        entity.setPrimKeyClass(newType.getFullString());
        modelUpdatedFromUI();
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
        } else if (source instanceof ClassElement) {
            cmpFields.change(source, propertyName, oldValue, newValue);
            queries.change(source, propertyName, oldValue, newValue);
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
            } else if (source instanceof ClassElement) {
                for (Iterator it = cmpFieldHelperMap.values().iterator(); it.hasNext();) {
                    CmpFieldHelper cmpFieldHelper = (CmpFieldHelper) it.next();
                    cmpFieldHelper.initAccessMethods();
                }
            }
            firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
        }

        protected void firePropertyChange(PropertyChangeEvent evt) {
            for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
                ((PropertyChangeListener) iterator.next()).propertyChange(evt);
            }
        }

        public void addCmpField() {
            new AddCmpFieldAction().addCmpField(beanClass, ejbJarFile);
            modelUpdatedFromUI();
        }

        public CmpFieldsTableModel getCmpFieldsTableModel() {
            return cmpFieldsTableModel;
        }
    }

    public class Queries implements PropertyChangeSource {
        private List listeners = new LinkedList();
        private HashMap queryMethodHelperMap = new HashMap();
        private Query[] selectMethods;
        private Query[] finderMethods;
        private static final String SELECT_PREFIX = "ejbSelect";
        private static final String FIND_PREFIX = "find";

        public Queries() {
            initQueryMethods();
        }

        private void initQueryMethods() {
            selectMethods = getQueries(SELECT_PREFIX);
            finderMethods = getQueries(FIND_PREFIX);
        }

        public QueryMethodHelper getQueryMethodHelper(Query finderMethod) {
            QueryMethodHelper queryMethodHelper = (QueryMethodHelper) queryMethodHelperMap.get(finderMethod);
            if (queryMethodHelper == null) {
                queryMethodHelper = new QueryMethodHelper(EntityHelper.this, finderMethod);
                queryMethodHelperMap.put(finderMethod, queryMethodHelper);
            }
            return queryMethodHelper;
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
    }
}
