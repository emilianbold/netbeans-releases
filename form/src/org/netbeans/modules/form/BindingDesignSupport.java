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

package org.netbeans.modules.form;

import java.lang.ref.*;
import java.lang.reflect.*;
import javax.beans.binding.*;
import javax.beans.binding.ext.PropertyDelegateProvider;
import javax.swing.binding.SwingBindingSupport;
import java.util.*;
import java.beans.*;
import org.netbeans.modules.form.FormUtils.TypeHelper;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.ErrorManager;

/**
 * Design support for beans binding.
 *
 * @author Jan Stola, Tomas Pavek
 */
public class BindingDesignSupport {
    /** Form model. */
    private FormModel formModel;

    /** Realizations of bindings among replicated components. */
    private Map<MetaBinding, List<Binding>> bindingsMap = new HashMap();
    /** Realizations of bindings among metacomponents. */
    private Map<MetaBinding, Binding> modelBindings = new HashMap();
    /** Binding context for reference instances in metacomponents. */
    private BindingContext bindingContext;

    private static Map<Class,Object> classToInstance = new WeakHashMap<Class,Object>();
    private static Object NO_INSTANCE = new Object();
    private static Binding.Parameter<ModifiableBoolean> INVALID_BINDING = new Binding.Parameter<ModifiableBoolean>(ModifiableBoolean.class, ""); // NOI18N

    /**
     * Create binding design support for the given form model.
     *
     * @param model form model to create the binding support for.
     */
    public BindingDesignSupport(FormModel model) {
        formModel = model;

        bindingContext = new BindingContext();
        bindingContext.bind();

        formModel.addFormModelListener(new ModelListener());
    }

    /**
     * Changes the binding between two components (affects only replicated components).
     * 
     * @param oldBinding the old definition of the binding.
     * @param newBinding the new definition of the binding.
     */
    private void changeBinding(MetaBinding oldBinding, MetaBinding newBinding) {
        if (oldBinding != null) {
            removeBindings(oldBinding);
        }
        // non-model bindings are added from VisualReplicator
    }

    /**
     * Changes the binding between two components (affects only reference instances in the model).
     * 
     * @param oldBinding the old definition of the binding.
     * @param newBinding the new definition of the binding.
     */
    public void changeBindingInModel(MetaBinding oldBinding, MetaBinding newBinding) {
        if (oldBinding != null) {
            removeBindingInModel(oldBinding);
        }
        if (newBinding != null) {
            addBindingInModel(newBinding);
        }
    }

    /**
     * Turns given string (usually dot-separated path) into EL expression
     * by adding <code>${</code> and <code>}</code> braces.
     * 
     * @param path string to transform into EL expression.
     * @return EL expression corresponding to the given path.
     */
    public static String elWrap(String path) {
        return (path == null) ? null : "${" + path + "}"; // NOI18N
    }

    /**
     * Determines whether the given string is simple EL expression. 
     * 
     * @param expression string to check.
     * @return <code>true</code> if the given string starts with
     * <code>${</code> and ends with <code>}</code>, returns <code>false</code>
     * otherwise.
     */
    public static boolean isSimpleExpression(String expression) {
        return (expression.startsWith("${") && expression.endsWith("}")); // NOI18N
    }

    /**
     * Removes <code>${</code> and <code>}</code> braces from a simple
     * EL expression. Non-simple expressions are left untouched.
     * 
     * @param expression expression to unwrap.
     * @return unwrapped expression or the given string
     * (if it is not a simple EL expression).
     */
    public static String unwrapSimpleExpression(String expression) {
        if (isSimpleExpression(expression)) {
            expression = expression.substring(2, expression.length()-1);
        }
        return expression;
    }

    private static boolean hasRelativeType(Class clazz, String property) {
        return ("elements".equals(property) // NOI18N
            || "selectedElement".equals(property) // NOI18N
            || "selectedElements".equals(property)) // NOI18N
          && (javax.swing.JTable.class.isAssignableFrom(clazz)
            || javax.swing.JList.class.isAssignableFrom(clazz)
            || javax.swing.JComboBox.class.isAssignableFrom(clazz));
    }

    public List<BindingDescriptor>[] getBindingDescriptors(RADComponent component) {
        BeanDescriptor beanDescriptor = component.getBeanInfo().getBeanDescriptor();
        return getBindingDescriptors(null, beanDescriptor, component.getBeanInstance());
    }

    private List<BindingDescriptor>[] getBindingDescriptors(TypeHelper type, BeanDescriptor beanDescriptor, Object instance) {
        List<BindingDescriptor> bindingList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> prefList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> observableList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> nonObservableList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> list;
        Class beanClass = beanDescriptor.getBeanClass();
        Object[] propsCats = FormUtils.getPropertiesCategoryClsf(beanClass, beanDescriptor);
        Iterable<FeatureDescriptor> fds;
        if (instance == NO_INSTANCE) {
            PropertyDescriptor[] pd;
            try {
                 pd = FormUtils.getBeanInfo(beanClass).getPropertyDescriptors();
            } catch (Exception ex) {
                ex.printStackTrace();
                pd = new PropertyDescriptor[0];
            }
            List pds = Arrays.asList(pd);
            fds = (Iterable<FeatureDescriptor>)pds;
        } else {
            fds = bindingContext.getFeatureDescriptors(instance);
        }
        for (FeatureDescriptor fd : fds) {
            if (Boolean.TRUE.equals(fd.getValue(PropertyDelegateProvider.PREFERRED_BINDING_PROPERTY))) {
                // preferred binding property
                list = bindingList;
            } else {
                Object propCat = FormUtils.getPropertyCategory(fd, propsCats);
                if (propCat == FormUtils.PROP_HIDDEN) {
                    // hidden property => hide also the binding property
                    continue;
                } else {
                    if (fd instanceof PropertyDescriptor) {
                        PropertyDescriptor pd = (PropertyDescriptor)fd;
                        if (pd.isBound()) {
                            // observable property
                            if (propCat == FormUtils.PROP_PREFERRED) {
                                list = prefList;
                            } else {
                                list = observableList;
                            }
                        } else {
                            // non-observable property
                            list = nonObservableList;
                        }
                    } else {
                        list = observableList; // Is that correct? Hopefully will not happen.
                    }                        
                }
            }

            PropertyDescriptor pd = null;
            if (fd instanceof PropertyDescriptor) {
                pd = (PropertyDescriptor)fd;
            }
            BindingDescriptor bd;
            if (pd == null) {
                // fallback - hopefully will not happen
                bd = new BindingDescriptor(fd.getName(), Object.class);
            } else {
                Method method = pd.getReadMethod();
                if ((method != null) && ("getClass".equals(method.getName()))) continue; // NOI18N
                Type retType = (method == null) ? pd.getPropertyType() : method.getGenericReturnType();
                if (type == null) {
                    bd = new BindingDescriptor(pd.getName(), retType);
                } else {
                    TypeHelper t = new TypeHelper(retType, type.getActualTypeArgs()).normalize();
                    bd = new BindingDescriptor(pd.getName(), t);
                }
                bd.setDisplayName(pd.getDisplayName());
                bd.setShortDescription(pd.getShortDescription());
            }

            if (hasRelativeType(beanClass, bd.getPath())) {
                bd.markTypeAsRelative();
            }
            
            list.add(bd);
        }

        if (bindingList.isEmpty()) {
            bindingList = prefList;
        } else {
            observableList.addAll(prefList);
        }
        Comparator bdComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                String path1 = ((BindingDescriptor)o1).getPath();
                String path2 = ((BindingDescriptor)o2).getPath();
                return path1.compareToIgnoreCase(path2);
            }
        };
        Collections.sort(bindingList, bdComparator);
        Collections.sort(observableList, bdComparator);
        Collections.sort(nonObservableList, bdComparator);

        return new List[] {bindingList, observableList, nonObservableList};
    }

    public List<BindingDescriptor> getAllBindingDescriptors(TypeHelper type) {
        List<BindingDescriptor>[] descs = getBindingDescriptors(type);
        List<BindingDescriptor> list = new LinkedList<BindingDescriptor>();
        for (int i=0; i<descs.length; i++ ){
            list.addAll(descs[i]);
        }
        return list;
    }
    
    /**
     * Returns possible bindings for the given type.
     *
     * @param type type whose possible bindings should be returned.
     * @return list of <code>BindingDescriptor</code>s describing possible bindings.
     */
    public List<BindingDescriptor>[] getBindingDescriptors(TypeHelper type) {
        Class clazz = FormUtils.typeToClass(type);
        if (clazz.getName().startsWith("java.lang.") // NOI18N
                || Collection.class.isAssignableFrom(clazz)
                || clazz.isArray()) {
            return new List[] {Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST};
        }

        List<BindingDescriptor>[] list;
        try {
            BeanInfo beanInfo = FormUtils.getBeanInfo(clazz);
            list = getBindingDescriptors(type, beanInfo.getBeanDescriptor(), getInstance(clazz));
        } catch (Exception ex) {
            ex.printStackTrace();
            list = new List[] {Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST};
        }
        return list;
    }

    /**
     * Determines type of RAD component.
     *
     * @param comp RAD component whose type should be returned.
     * @return <code>TypeHelper</code> that corresponds to the type of the given component.
     */
    static TypeHelper determineType(RADComponent comp) {
        Type t = null;
        Map<String,Type> newMap = null;
        Class clazz = comp.getBeanClass();
        t = clazz;
        // PENDING generalize
        if (clazz.getTypeParameters().length == 1) {
            try {
                Object value = comp.getSyntheticProperty("typeParameters").getValue(); // NOI18N
                if (value instanceof String) {
                    // PENDING generalize
                    String type = (String)value;
                    if (type.startsWith("<")) { // NOI18N
                        type = type.substring(1, type.length()-1);
                        try {
                            Class elemType = ClassPathUtils.loadClass(type, FormEditor.getFormDataObject(comp.getFormModel()).getFormFile());
                            newMap = new HashMap<String,Type>();
                            newMap.put(clazz.getTypeParameters()[0].getName(), elemType);
                        } catch (ClassNotFoundException cnfex) {
                            cnfex.printStackTrace();
                        }
                    }                    
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new TypeHelper(t, newMap);
    }

    /**
     * Determines type of the binding described by the given component and source path.
     *
     * @param comp source of the binding.
     * @param sourcePath binding path from the source.
     * @return type of the binding.
     */
    public TypeHelper determineType(RADComponent comp, String sourcePath) {
        String[] path = parsePath(sourcePath);
        TypeHelper type = determineType(comp);
        for (int i=0; i<path.length; i++) {
            String pathItem = path[i];
            List<BindingDescriptor> descriptors = getAllBindingDescriptors(type);
            BindingDescriptor descriptor = findDescriptor(descriptors, pathItem);
            if (descriptor == null) return new TypeHelper();
            type = descriptor.getGenericValueType();
            if (type == null) {
                if (javax.swing.JTable.class.isAssignableFrom(comp.getBeanClass())
                        || javax.swing.JList.class.isAssignableFrom(comp.getBeanClass())
                        || javax.swing.JComboBox.class.isAssignableFrom(comp.getBeanClass())) {
                    MetaBinding binding = (MetaBinding)comp.getBindingProperty("elements").getValue(); // NOI18N
                    if (binding != null) {
                        RADComponent subComp = binding.getSource();
                        String subSourcePath = binding.getSourcePath();
                        // PENDING beware of stack overflow
                        TypeHelper t = determineType(subComp, subSourcePath);
                        if ("selectedElement".equals(pathItem)) { // NOI18N
                            type = typeOfElement(t);
                        } else if ("selectedElements".equals(pathItem) || "elements".equals(pathItem)) { // NOI18N
                            type = t;
                        }
                    } else {
                        type = new TypeHelper();
                    }
                }
            }
        }
        return type;
    }

    /**
     * Finds descriptor that corresponds to the given binding path.
     *
     * @param descriptors list of descriptors that should be searched.
     * @param path binding path to find descriptor for.
     * @return descriptor that corresponds to the given binding path.
     */
    private static BindingDescriptor findDescriptor(List<BindingDescriptor> descriptors, String path) {
        for (BindingDescriptor descriptor : descriptors) {
            if (descriptor.getPath().equals(path)) return descriptor;
        }
        return null;
    }

    /**
     * Parses binding path into segments.
     *
     * @param path path to parse.
     * @return segments of the binding path. The returned value cannot be <code>null</code>.
     */
    private static String[] parsePath(String path) {
        if (path == null) return new String[0];
        List pathItems = new LinkedList();
        int index;
        while ((index = path.indexOf('.')) != -1) {
            pathItems.add(path.substring(0,index));
            path = path.substring(index+1);
        }
        pathItems.add(path);
        return (String[])pathItems.toArray(new String[pathItems.size()]);
    }

    /**
     * Returns type of element of the given type - expects type that implements
     * <code>Collection</code> interface.
     *
     * @param type type that implements <code>Collection</code> interface.
     * @return type of element of the given type.
     */
    static TypeHelper typeOfElement(TypeHelper type) {
        Type t = type.getType();
        TypeHelper elemType = new TypeHelper();
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            Type[] args = pt.getActualTypeArguments();
            // PENDING generalize and improve - track the type variables to the nearest
            // known collection superclass or check parameter type of add(E o) method
            if (args.length == 1) { // The only argument should be type of the collection element
                Type tt = args[0];
                elemType = new TypeHelper(tt, type.getActualTypeArgs());
            }
        } else if (t instanceof Class) {
            Class classa = (Class)t;
            TypeVariable[] tvar = classa.getTypeParameters();
            // PENDING dtto
            Map<String,Type> actualTypeArgs = type.getActualTypeArgs();
            if ((actualTypeArgs != null) && (tvar.length == 1)) {
                Type tt = actualTypeArgs.get(tvar[0].getName());
                if (tt != null) {
                    elemType = new TypeHelper(tt, actualTypeArgs);
                }
            }
        }
        return elemType;
    }

    // PENDING - is it still needed? Check what it did in the past.
    /**
     * @return List of descriptors available on given path; the list can be modified
     */
//    public static List<BindingDescriptor> getPossibleNestedSourceBindings(
//                        RADComponent source, BindingDescriptor desc)
//    {
//        Object value = getSourceValue(source, desc.getPath());
//        if (value == null) {
//            BindingDescriptor related = desc.getRelatedBinding();
//            if (related != null) {
//                List<BindingDescriptor> list = getPossibleNestedSourceBindings(source, related);
//                if (!list.isEmpty())
//                    return list;
//            }
//            return getPossibleSourceBindings(desc.getValueType());
//        }
//
//        if (value instanceof Collection) {
//            // TODO need introspection of generics...
//            Collection col = (Collection) value;
//            if (col.size() > 0) {
//                value = col.iterator().next();
//            }
//            else value = null;
//
//            if (value == null) // can't determine the element type
//                return Collections.EMPTY_LIST;
//        }
//
//        if (value instanceof Map) {
//            Map map = (Map) value;
//            List<BindingDescriptor> descList = new ArrayList(map.size());
//            Iterator it = map.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry en = (Map.Entry) it.next();
//                Object key = en.getKey();
//                if (key instanceof String) {
//                    Object val = en.getValue();
//                    descList.add(new BindingDescriptor(
//                            null,
//                            (String) key,
//                            val != null ? val.getClass() : Object.class));
//                }
//            }
//            return descList;
//        }
//        else {
//            return getPossibleSourceBindings(value.getClass());
//        }
//    }

//    private static Class getSourceValueType(RADComponent source, String path) {
//        FormProperty prop = source.getBeanProperty(path);
//        return prop != null ? prop.getValueType() : null;
//    }

    // [would be better to this via a binding resolver]
    private static Object getSourceValue(RADComponent source, String path) {
        FormProperty prop = source.getBeanProperty(path);
        if (prop != null) {
            try {
                return prop.getRealValue();
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return null;
    }

    public void establishUpdatedBindings(RADComponent metacomp,
                                         boolean recursive,
                                         Map map,
                                         BindingContext context, boolean inModel)
    {
        for (MetaBinding bindingDef : collectBindingDefs(metacomp, recursive)) {
            RADComponent sourceComp = bindingDef.getSource();
            RADComponent targetComp = bindingDef.getTarget();
            if (sourceComp.isInModel() && targetComp.isInModel()) {
                if (inModel) {
                    addBindingInModel(bindingDef);
                } else {
                    Object source = null;
                    if (map != null)
                        source = map.get(sourceComp.getId());
                    if (source == null)
                        source = sourceComp.getBeanInstance(); // also used if clone not available
                    Object target = map != null ?
                        map.get(targetComp.getId()) : targetComp.getBeanInstance();
                    if (source != null && target != null)
                        addBinding(bindingDef, source, target, context, false);
                }
            }
        }
    }

    public static void establishOneOffBindings(RADComponent metacomp,
                                               boolean recursive,
                                               Map map,
                                               BindingContext context)
    {
        for (MetaBinding bindingDef : collectBindingDefs(metacomp, recursive)) {
            RADComponent sourceComp = bindingDef.getSource();
            RADComponent targetComp = bindingDef.getTarget();
            Object source = null;
            if (map != null)
                source = map.get(sourceComp.getId());
            if (source == null)
                source = sourceComp.getBeanInstance(); // also used if clone not available
            Object target = map != null ?
                map.get(targetComp.getId()) : targetComp.getBeanInstance();
            if (source != null && target != null)
                createBinding(bindingDef, source, target, context);
        }
    }

    private void releaseBindings(RADComponent metacomp, boolean recursive) {
        for (MetaBinding bindingDef : collectBindingDefs(metacomp, recursive)) {
            removeBindings(bindingDef); // unbinds and removes all bindings
                                        // created according to this definition
        }
    }

    private static Collection<MetaBinding> collectBindingDefs(RADComponent metacomp, boolean recursive) {
        Collection<MetaBinding> col = collectBindingDefs(metacomp, recursive, null);
        if (col == null)
            col = Collections.EMPTY_LIST;
        return col;
    }

    private static Collection<MetaBinding> collectBindingDefs(
            RADComponent metacomp, boolean recursive, Collection<MetaBinding> col)
    {
        for (BindingProperty bProp : metacomp.getKnownBindingProperties()) {
            MetaBinding bindingDef = (MetaBinding) bProp.getValue();
            if (bindingDef != null) {
                if (col == null)
                    col = new LinkedList();
                col.add(bindingDef);
            }
        }

        if (recursive && metacomp instanceof ComponentContainer) {
            for (RADComponent subcomp : ((ComponentContainer)metacomp).getSubBeans()) {
                col = collectBindingDefs(subcomp, recursive, col);
            }
        }

        return col;
    }

    private void addBindingInModel(MetaBinding bindingDef) {
        addBinding(bindingDef,
            bindingDef.getSource().getBeanInstance(),
            bindingDef.getTarget().getBeanInstance(),
            bindingContext, true);
    }
    
    /**
     * Creates binding according to given MetaBinding between given source and
     * target objects. The binding is registered, so it is automatically unbound
     * and removed when the MetaBinding is removed (or the source/target component).
     */
    public void addBinding(MetaBinding bindingDef,
                           Object source, Object target,
                           BindingContext context, boolean inModel)
    {
        if (inModel) {
            if (modelBindings.get(bindingDef) == null) {
                modelBindings.put(bindingDef, createBinding(bindingDef, source, target, context));
            }
        } else {
            List<Binding> establishedBindings = bindingsMap.get(bindingDef);
            if (establishedBindings != null) {
                for (Binding binding : establishedBindings) {
                    if (binding.getSource() == source
                        && binding.getTarget() == target)
                        return; // this binding already exists
                }
            }
            else {
                establishedBindings = new LinkedList();
                bindingsMap.put(bindingDef, establishedBindings);
            }
            establishedBindings.add(createBinding(bindingDef, source, target, context));
        }
    }

    public static Class getBindingDescriptionType(MetaBinding bindingDef) {
        return Binding.class;
    }

    private static Binding createBinding(MetaBinding bindingDef,
                                         Object source, Object target,
                                         BindingContext context)
    {
        Binding binding;
        Collection<MetaBinding> subBindings = bindingDef.getSubBindings();
        List parameters = new LinkedList();
        String changeStrategy = bindingDef.getParameter(MetaBinding.TEXT_CHANGE_STRATEGY);
        if (changeStrategy != null) {
            Object value = null;
            if (MetaBinding.TEXT_CHANGE_ON_ACTION_OR_FOCUS_LOST.equals(changeStrategy)) {
                value = SwingBindingSupport.TextChangeStrategy.CHANGE_ON_ACTION_OR_FOCUS_LOST;
            } else if (MetaBinding.TEXT_CHANGE_ON_FOCUS_LOST.equals(changeStrategy)) {
                value = SwingBindingSupport.TextChangeStrategy.CHANGE_ON_FOCUS_LOST;
            } else if (MetaBinding.TEXT_CHANGE_ON_TYPE.equals(changeStrategy)) {
                value = SwingBindingSupport.TextChangeStrategy.CHANGE_ON_TYPE;
            }
            if (value != null) {
                parameters.add(SwingBindingSupport.TextChangeStrategyParameter);
                parameters.add(value);
            }
        }
        binding = new Binding(source, bindingDef.getSourcePath(), target, bindingDef.getTargetPath(), parameters.toArray());
        Binding.UpdateStrategy updateStrategy = null;
        switch (bindingDef.getUpdateStratedy()) {
            case MetaBinding.UPDATE_STRATEGY_READ_WRITE:
                updateStrategy = Binding.UpdateStrategy.READ_WRITE;
                break;
            case MetaBinding.UPDATE_STRATEGY_READ_FROM_SOURCE:
                updateStrategy = Binding.UpdateStrategy.READ_FROM_SOURCE;
                break;
            case MetaBinding.UPDATE_STRATEGY_READ_ONCE:
                updateStrategy = Binding.UpdateStrategy.READ_ONCE;
                break;
            default: assert false;
        }
        binding.setUpdateStrategy(updateStrategy);
        if (bindingDef.isNullValueSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty nullProp = prop.getNullValueProperty();
            try {
                Object value = nullProp.getRealValue();
                if (value != null) {
                    binding.setNullSourceValue(value);
                }
            } catch (IllegalAccessException iaex) {
                iaex.printStackTrace();
            } catch (InvocationTargetException itex) {
                itex.printStackTrace();
            }
        }
        if (bindingDef.isIncompletePathValueSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty incompleteProp = prop.getIncompleteValueProperty();
            try {
                Object value = incompleteProp.getRealValue();
                if (value != null) {
                    binding.setValueForIncompleteSourcePath(value);
                }
            } catch (IllegalAccessException iaex) {
                iaex.printStackTrace();
            } catch (InvocationTargetException itex) {
                itex.printStackTrace();
            }
        }
        if (bindingDef.isConverterSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty converterProp = prop.getConverterProperty();
            try {
                Object value = converterProp.getRealValue();
                if ((value != null) && (value instanceof BindingConverter)) {
                    binding.setConverter((BindingConverter)value);
                }
            } catch (IllegalAccessException iaex) {
                iaex.printStackTrace();
            } catch (InvocationTargetException itex) {
                itex.printStackTrace();
            }
        }
        if (bindingDef.isValidatorSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty validatorProp = prop.getValidatorProperty();
            try {
                Object value = validatorProp.getRealValue();
                if ((value != null) && (value instanceof BindingValidator)) {
                    binding.setValidator((BindingValidator)value);
                }
            } catch (IllegalAccessException iaex) {
                iaex.printStackTrace();
            } catch (InvocationTargetException itex) {
                itex.printStackTrace();
            }
        }
        if (bindingDef.hasSubBindings()) {
            for (MetaBinding sub : subBindings) {
                List subParameters = new LinkedList();
                String tableColumn = sub.getParameter(MetaBinding.TABLE_COLUMN_PARAMETER);
                if (tableColumn != null) {
                    try {
                        int column = Integer.parseInt(tableColumn);
                        subParameters.add(SwingBindingSupport.TableColumnParameter);
                        subParameters.add(column);
                    } catch (NumberFormatException nfex) {
                        nfex.printStackTrace();
                    }
                }
                String columnClass = sub.getParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER);
                if (columnClass != null) {
                    try {
                        if ((columnClass != null) && columnClass.trim().endsWith(".class")) { // NOI18N
                            columnClass = columnClass.trim();
                            columnClass = columnClass.substring(0, columnClass.length()-6);
                        }
                        if (columnClass.indexOf('.') == -1) {
                            columnClass = "java.lang." + columnClass; // NOI18N
                        }
                        Class clazz = FormUtils.loadClass(columnClass, bindingDef.getSource().getFormModel());
                        subParameters.add(SwingBindingSupport.TableColumnClassParameter);
                        subParameters.add(clazz);
                    } catch (ClassNotFoundException cnfex) {
                        cnfex.printStackTrace();
                    }
                }
                binding.addBinding(sub.getSourcePath(), sub.getTargetPath(), subParameters.toArray());
            }
        }
        binding.setValue(INVALID_BINDING, new ModifiableBoolean());
        context.addBinding(binding);
        try {
            binding.bind();
        } catch (PropertyResolverException prex) {
            // PENDING implement better error handling and reporting
            String message = prex.getMessage();
            // simple heuristics that gets rid of JComponent.toString()
            int index = message.indexOf('[');
            if (index != -1) {
                index = message.lastIndexOf(' ', index);
                message = message.substring(0, index+1) + bindingDef.getTarget().getName() + message.substring(message.lastIndexOf(']')+1);
            }
            System.err.println(message);
            ModifiableBoolean invalid = binding.getValue(INVALID_BINDING, null);
            invalid.value = true;
        }
        return binding;
    }

    private void removeBindings(MetaBinding bindingDef) {
        removeBindingInModel(bindingDef);
        List<Binding> establishedBindings = bindingsMap.get(bindingDef);
        if (establishedBindings != null) {
            for (Binding binding : establishedBindings) {
                removeBinding(binding);
            }
            bindingsMap.remove(bindingDef);
        }
    }

    private static void removeBinding(Binding binding) {
        BindingContext context = binding.getContext();
        if (!(binding.getValue(INVALID_BINDING, null).value)) { // Issue 104960
            binding.unbind();
        } else {
            try {
                binding.unbind();
            } catch (NullPointerException npex) {} // ugly implementation detail of binding library
        }
        context.removeBinding(binding);
    }

    private void removeBindingInModel(MetaBinding bindingDef) {
        Binding binding = modelBindings.remove(bindingDef);
        if (binding != null) {
            removeBinding(binding);
        }
    }

    private static Object getInstance(Class clazz) {
        Object instance = classToInstance.get(clazz);
        if (instance instanceof Reference) {
            instance = ((Reference)instance).get();
        }
        if (instance == null) {
            try {
                instance = CreationFactory.createDefaultInstance(clazz);
            } catch (Exception ex) {
                instance = NO_INSTANCE;
            }
            classToInstance.put(clazz, (instance == NO_INSTANCE) ? instance : new WeakReference(instance));
        }
        return instance;
    }

    /**
     * Form model listener that updates the bindings.
     */
    private class ModelListener implements FormModelListener {
        public void formChanged(FormModelEvent[] events) {
            if (events == null)
                return;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];
                switch (ev.getChangeType()) {
                    case FormModelEvent.BINDING_PROPERTY_CHANGED:
                        changeBinding(ev.getOldBinding(), ev.getNewBinding());
                        break;
                    case FormModelEvent.COMPONENT_REMOVED:
                        releaseBindings(ev.getComponent(), true);
                        break;
                    case FormModelEvent.COMPONENT_ADDED:
                        if (!ev.getCreatedDeleted()) {
                            establishUpdatedBindings(ev.getComponent(), true, null, bindingContext, true);
                        }
                        break;
                }
            }
        }
    }

    static class ModifiableBoolean {
        boolean value;
    }
    
}
