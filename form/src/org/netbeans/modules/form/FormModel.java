/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.event.EventListenerList;
import org.openide.src.*;
import org.netbeans.modules.form.layoutsupport.*;

import org.netbeans.modules.form.codestructure.CodeStructure;

/**
 * Holds all data of a form.
 * 
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModel
{
    // the top metacomponent of the form (null if form is based on Object)
    private RADComponent topRADComponent;

    // the class on which the form is based (which is extended in the java file)
    private Class formBaseClass;

    // other components - out of the main hierarchy under topRADComponent
    private ArrayList otherComponents = new ArrayList(10);

    // holds both topRADComponent and otherComponents
    private ComponentContainer modelContainer;

    // name of the form (name of the DataObject)
    private String formName;

    private boolean readOnly = false;
    private boolean formLoaded = false;

    private FormEventHandlers eventHandlers;

    // list of listeners registered on FormModel
    private final EventListenerList listenerList = new EventListenerList();

    private MetaComponentCreator metaCreator;

    private CodeStructure codeStructure = new CodeStructure();
    private CodeGenerator codeGenerator; // [this reference should be removed]

    // -------------
    // initialization

    FormModel() {
    }

    /** This methods sets the form base class (which is in fact the superclass
     * of the form class in source java file). It is used for initializing
     * the top meta component, and is also presented as the top component
     * in designer and inspector.
     */
    public void setFormBaseClass(Class formClass) throws Exception {
        if (formBaseClass != null)
            throw new IllegalStateException("Form type already initialized."); // NOI18N

        RADComponent topComp;
        if (java.awt.Component.class.isAssignableFrom(formClass)) {
            topComp = FormUtils.isContainer(formClass) ?
                          new RADVisualFormContainer() :
                          new RADVisualComponent();
        }
        else if (java.lang.Object.class != formClass)
            topComp = new RADFormContainer();
        else topComp = null;

        if (topComp != null) {
            topRADComponent = topComp;
            topComp.initialize(this);
            topComp.initInstance(formClass);
        }

        formBaseClass = formClass;
//        topRADComponent = topComp;
    }

    public Class getFormBaseClass() {
        return formBaseClass;
    }

    /** Initializes the form from SourceElement (representing a source file).
     */
    public void initialize(SourceElement source) throws Exception {
        initialize(source.getClasses()[0]);
    }

    /** Initializes the form from a ClassElement (representing one class
     * in a source file).
     */
    public void initialize(ClassElement cle) throws Exception {
        Class formClass;
        Identifier superClass = cle.getSuperclass();

        if (superClass != null)
            formClass = org.openide.TopManager.getDefault().currentClassLoader()
                                           .loadClass(superClass.getFullName());
        else formClass = Object.class;

        setFormBaseClass(formClass);
    }

    void setName(String name) {
        formName = name;
    }

    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    // -----------
    // getters

    public final String getName() {
        return formName;
    }

    public final boolean isReadOnly() {
        return readOnly;
    }

    public final boolean isFormLoaded() {
        return formLoaded;
    }

    public final FormDesigner getFormDesigner() {
        return FormEditorSupport.getFormDesigner(this);
    }

    // for compatibility with previous version
    public final FormDataObject getFormDataObject() {
        return FormEditorSupport.getFormDataObject(this);
    }

    public ComponentContainer getModelContainer() {
        if (modelContainer == null)
            modelContainer = new ModelContainer();
        return modelContainer;
    }

    public final RADComponent getTopRADComponent() {
        return topRADComponent;
    }

    public RADComponent findRADComponent(String name) {
        Iterator allComps = getMetaComponents().iterator();
        while (allComps.hasNext()) {
            RADComponent comp = (RADComponent) allComps.next();
            if (name.equals(comp.getName()))
                return comp;
        }

        return null;
    }

    /** Returns all meta components in the model. The components are collected
     * recursively, and placed in a List.
     */
    public java.util.List getMetaComponents() {
        ArrayList list = new ArrayList();
        collectMetaComponents(getModelContainer(), list);
        return list; //Collections.unmodifiableList(list);
    }

    /** Collects and returns all components in the main visual hierarchy.
     */
    public RADVisualComponent[] getVisualComponents() {
        ArrayList list = new ArrayList();
        if (topRADComponent instanceof RADVisualComponent)
            list.add(topRADComponent);
        if (topRADComponent instanceof RADVisualContainer)
            collectVisualMetaComponents((RADVisualContainer)topRADComponent, list);

        return (RADVisualComponent[])
               list.toArray(new RADVisualComponent[list.size()]);
    }

    /** Returns all "other components" (not in the main hierarchy).
     * @param recursively whether also all sub-componets should be collected
     */
    public RADComponent[] getOtherComponents(boolean recursively) {
        ArrayList list = new ArrayList();
        for (Iterator it=otherComponents.iterator(); it.hasNext(); ) {
            RADComponent comp = (RADComponent) it.next();
            list.add(comp);
            if (recursively && comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }

        return (RADComponent[]) list.toArray(new RADComponent[list.size()]);
    }

    // for compatibility with previous version
    public RADComponent[] getNonVisualComponents() {
        return (RADComponent[]) otherComponents.toArray(
                                new RADComponent[otherComponents.size()]); 
    }

    private static void collectMetaComponents(ComponentContainer cont,
                                              java.util.List list) {
        RADComponent[] comps = cont.getSubBeans();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }
    }

    private static void collectVisualMetaComponents(RADVisualContainer cont,
                                                    java.util.List list) {
        RADVisualComponent[] comps = cont.getSubComponents();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof RADVisualContainer)
                collectVisualMetaComponents((RADVisualContainer) comp, list);
        }
    }

    // -----------
    // adding/deleting components, setting layout, etc

    /** Returns MetaComponentCreator which is responsible for creating new
     * components and adding them to the model.
     */
    public MetaComponentCreator getComponentCreator() {
        if (metaCreator == null)
            metaCreator = new MetaComponentCreator(this);
        return metaCreator;
    }

    public void addComponent(RADComponent comp,
                             ComponentContainer parentContainer) {
        initComponentWithModelRecursively(comp);

        if (parentContainer != null) {
            parentContainer.add(comp);
        }
        else {
            comp.setParentComponent(null);
            otherComponents.add(comp);
        }

        fireComponentAdded(comp);
    }

    public void addVisualComponent(RADVisualComponent comp,
                                   RADVisualContainer parentContainer,
                                   LayoutConstraints constraints) {
        initComponentWithModelRecursively(comp);

        parentContainer.add(comp);

        parentContainer.getLayoutSupport().addComponent(comp, constraints);

        fireComponentAdded(comp);
    }

    public void setContainerLayout(RADVisualContainer metacont,
                                   LayoutSupportDelegate layoutDelegate)
    {
        LayoutSupportDelegate current =
            metacont.getLayoutSupport().getLayoutDelegate();
        metacont.setLayoutSupportDelegate(layoutDelegate);
        fireContainerLayoutChanged(metacont, current, layoutDelegate);
    }

    public void removeComponentFromContainer(RADComponent comp) {
        RADComponent parent = comp.getParentComponent();
        ComponentContainer parentContainer =
            parent instanceof ComponentContainer ?
                (ComponentContainer) parent : getModelContainer();

        parentContainer.remove(comp);

//        if (comp instanceof RADVisualComponent)
//            ((RADVisualComponent)comp).resetConstraintsProperties();

        fireComponentRemoved(comp, parentContainer);
    }

    public void removeComponent(RADComponent comp) {
        if (eventHandlers != null)
            removeEventHandlersRecursively(comp);

        removeCodeExpressionsRecursively(comp);

        removeComponentFromContainer(comp);

//        if (comp instanceof RADVisualComponent)
//            ((RADVisualComponent)comp).resetConstraintsProperties();
    }

    // sets FormModel for given component and all its subcomponents
    private void initComponentWithModelRecursively(RADComponent comp) {
        if (comp instanceof ComponentContainer) {
            RADComponent[] subcomps = ((ComponentContainer)comp).getSubBeans();
            for (int i=0; i < subcomps.length; i++)
                initComponentWithModelRecursively(subcomps[i]);
        }

        comp.initialize(this);
    }

    // removes all event handlers attached to given component and all
    // its subcomponents
    private void removeEventHandlersRecursively(RADComponent comp) {
        if (comp instanceof ComponentContainer) {
            RADComponent[] subcomps = ((ComponentContainer)comp).getSubBeans();
            for (int i=0; i<subcomps.length; i++)
                removeEventHandlersRecursively(subcomps[i]);
        }

        EventSet[] eventSets = comp.getEventHandlers().getEventSets();
        for (int i = 0; i < eventSets.length; i++) {
            Event[] events = eventSets[i].getEvents();
            for (int j = 0; j < events.length; j++) {
                if (events[j].getHandlers().size() > 0) {
                    eventHandlers.removeEventHandler(events[j]);
                }
            }
        }
    }

    private void removeCodeExpressionsRecursively(RADComponent comp) {
        comp.resetCodeExpression();
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] =((ComponentContainer) comp).getSubBeans();
            for (int i=0, n=comps.length; i<n; i++) {
                removeCodeExpressionsRecursively(comps[i]);
            }
        }
    }

    // ----------
    // events and listeners

    public void addFormModelListener(FormModelListener l) {
        listenerList.add(FormModelListener.class, l);
    }

    public void removeFormModelListener(FormModelListener l) {
        listenerList.remove(FormModelListener.class, l);
    }

    public void fireFormChanged() {
        t("formChanged"); // NOI18N
        FormModelEvent e = new FormModelEvent(this);
        
  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
  		((FormModelListener)listeners[i+1]).formChanged(e);
  	    }
  	}
    }

    public void fireFormLoaded() {
        t("formLoaded"); // NOI18N
        formLoaded = true;
        initializeCodeGenerator();

        FormModelEvent e = new FormModelEvent(this);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
  		((FormModelListener)listeners[i+1]).formLoaded(e);
  	    }
  	}
    }

    public void fireFormToBeSaved() {
        t("formToBeSaved"); // NOI18N
        FormModelEvent e = new FormModelEvent(this);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
  		((FormModelListener)listeners[i+1]).formToBeSaved(e);
  	    }
  	}
    }

//    public void fireFormToBeClosed() {
//        t("formToBeClosed fired"); // NOI18N
//        FormModelEvent e = new FormModelEvent(this);
//
//  	Object[] listeners = listenerList.getListenerList();
//  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
//  	    if (listeners[i] == FormModelListener.class) {
//  		((FormModelListener)listeners[i+1]).formToBeClosed(e);
//  	    }
//  	}
//    }

    public void fireContainerLayoutChanged(RADVisualContainer metacont,
                                           LayoutSupportDelegate oldLayoutSupp,
                                           LayoutSupportDelegate newLayoutSupp) {
        t("containerLayoutChanged, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacont,
                                              oldLayoutSupp, newLayoutSupp);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                l.containerLayoutChanged(e);
                l.formChanged(e);
            }
        }
    }

    public void fireComponentLayoutChanged(RADVisualComponent metacomp,
                                           String propName,
                                           Object propOldVal,
                                           Object propNewVal) {
        t("componentLayoutChanged, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp,
                                              propName, propOldVal, propNewVal);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                l.componentLayoutChanged(e);
                l.formChanged(e);
            }
        }
    }

    public void fireComponentAdded(RADComponent metacomp) {
        t("componentAdded, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentAdded(e);
                l.formChanged(e);
  	    }
  	}
    }
    
    public void fireComponentRemoved(RADComponent metacomp,
                                     ComponentContainer metacont) {
        t("componentRemoved, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp, metacont);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentRemoved(e);
                l.formChanged(e);
  	    }
  	}
    }

    public void fireComponentsReordered(ComponentContainer metacont) {
        t("componentsReordered, container: " // NOI18N
          + (metacont instanceof RADComponent ?
             ((RADComponent)metacont).getName() : "<top>")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacont);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentsReordered(e);
                l.formChanged(e);
  	    }
  	}
    }

    public void fireComponentPropertyChanged(RADComponent metacomp,
                                             String propName,
                                             Object propOldVal,
                                             Object propNewVal) {
        t("componentPropertyChanged, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "<null component>") // NOI18N
          + ", property: " + propName); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp,
                                              propName, propOldVal, propNewVal);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentPropertyChanged(e);
                l.formChanged(e);
  	    }
  	}
    }

    public void fireSyntheticPropertyChanged(RADComponent metacomp,
                                             String propName,
                                             Object propOldVal,
                                             Object propNewVal) {
        t("syntheticPropertyChanged, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp,
                                              propName, propOldVal, propNewVal);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.syntheticPropertyChanged(e);
                l.formChanged(e);
  	    }
  	}
    }

    // not implemented yet
    public void fireEventHandlerAdded(EventHandler handler) {
        t("eventHandlerAdded"); // NOI18N
    }

    // not implemented yet
    public void fireEventHandlerRemoved(EventHandler handler, String oldName) {
        t("eventHandlerRemoved"); // NOI18N
    }

    // not implemented yet
    public void fireEventHandlerRenamed(EventHandler handler) {
        t("eventHandlerRenamed"); // NOI18N
    }

    // -------------

    FormEventHandlers getFormEventHandlers() {
        if (eventHandlers == null)
            eventHandlers = new FormEventHandlers(getCodeGenerator());
        return eventHandlers;
    }

    CodeStructure getCodeStructure() {
        return codeStructure;
    }

    CodeGenerator getCodeGenerator() {
//        return FormEditorSupport.getCodeGenerator(this);
        if (codeGenerator == null)
            codeGenerator = new JavaCodeGenerator();
        return codeGenerator;
    }

    // [only for compatibility with old DesignLayout classes, should be removed]
    public int getMode() {
        return 1;//DesignLayout.DESIGN_MODE;
    }
    
    // [only for compatibility with old DesignLayout classes, should be removed]
    public void setMode(int mode) {
    }
    
    void initializeCodeGenerator() {
        getCodeGenerator().initialize(this);
    }

    // [only for compatibility with old DesignLayout classes, should be removed]
    public boolean isTestMode() {
        return false;
    }

    // [only for compatibility with old DesignLayout classes, should be removed]
    public void setTestMode(boolean test) {
    }

    // [only for compatibility with old DesignLayout classes, should be removed]
    public java.awt.Component getVisualRepresentation(RADComponent radComp) {
        if (radComp instanceof RADVisualComponent) {
//            Component comp =(Component)radToSelection.get(radComp);
//            if (comp == null) {
                return((RADVisualComponent)radComp).getComponent(); // [PENDING - this should not be necessary]
//            }
//            return comp;
        } else {
            return new javax.swing.JButton();
        }
    }

    // ---------------
    // ModelContainer innerclass

    final class ModelContainer implements ComponentContainer {
        public RADComponent[] getSubBeans() {
            int n = otherComponents.size();
            if (topRADComponent != null)
                n++;
            RADComponent[] comps = new RADComponent[n];
            otherComponents.toArray(comps);
            if (topRADComponent != null)
                comps[n-1] = topRADComponent;
            return comps;
        }

        public void initSubComponents(RADComponent[] initComponents) {
            otherComponents.clear();
            for (int i = 0; i < initComponents.length; i++)
                otherComponents.add(initComponents[i]);
        }

        public void reorderSubComponents(int[] perm) {
            for (int i = 0; i < perm.length; i++) {
                int from = i;
                int to = perm[i];
                if (from == to)
                    continue;
                Object value = otherComponents.remove(from);
                if (from < to)
                    otherComponents.add(to - 1, value);
                else
                    otherComponents.add(to, value);
            }
        }

        public void add(RADComponent comp) {
            comp.setParentComponent(null);
            otherComponents.add(comp);
        }

        public void remove(RADComponent comp) {
            if (otherComponents.remove(comp))
                comp.setParentComponent(null);
        }

        public int getIndexOf(RADComponent comp) {
            int index = otherComponents.indexOf(comp);
            if (index < 0 && comp == topRADComponent)
                index = otherComponents.size();
            return index;
        }
    }

    // ---------------

    /** For debugging purposes only. */
    static private int traceCount = 0;
    /** For debugging purposes only. */
    static private final boolean TRACE = false;
    /** For debugging purposes only. */
    static void t(String str) {
        if (TRACE)
            if (str != null)
                System.out.println(Integer.toString(++traceCount) + " Form: "+str); // NOI18N
            else
                System.out.println(""); // NOI18N
    }
}
