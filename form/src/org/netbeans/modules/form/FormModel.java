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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.text.MessageFormat;
import javax.swing.event.EventListenerList;

import org.openide.*;
import org.openide.nodes.*;

import org.netbeans.modules.form.forminfo.*;
import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupport;

/**
 * Holds all data of a form.
 * 
 * @author Tran Duc Trung
 */

public class FormModel
{
    private FormDataObject formDataObject;
    private boolean readOnly = false;
    private boolean formLoaded = false;

    private FormInfo formInfo;
    private RADComponent topRADComponent;

    private CodeGenerator codeGenerator;
    private FormEventHandlers eventHandlers;
    private VariablePool variablePool;

    private ArrayList nonVisualComponents = new ArrayList(10);
    private NonVisualChildren nonVisualChildren;
    private ComponentContainer nonVisualsContainer;

    private final EventListenerList listenerList = new EventListenerList();

    private FormDesigner formDesigner;

    FormModel() {
    }

    void setFormDataObject(FormDataObject fdo) {
        formDataObject = fdo;
        readOnly = fdo.isReadOnly();
    }

    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }

    public final boolean isReadOnly() {
        return readOnly;
    }

    public final boolean isFormLoaded() {
        return formLoaded;
    }

    void setFormInfo(FormInfo fi) {
        formInfo = fi;
        if (formInfo.getTopContainer() != null) {
            topRADComponent = new RADVisualFormContainer(formInfo);
        }
        else {
            topRADComponent = new RADFormContainer(formInfo);
        }
        topRADComponent.initialize(this);
        topRADComponent.setComponent(formInfo.getFormInstance().getClass());

        Object refInstance = formInfo.getFormInstance();
        Object instance = topRADComponent.getBeanInstance();
        if (refInstance instanceof java.applet.Applet
            || refInstance instanceof Frame
            || refInstance instanceof Panel
            || refInstance instanceof Dialog)
        {
            Component refComp = (Component) refInstance;
            Component comp = (Component) instance;
            
            comp.setBackground(refComp.getBackground());
            comp.setForeground(refComp.getForeground());
            comp.setFont(refComp.getFont());
        }
        topRADComponent.setName(formDataObject.getName());
    }

    FormInfo getFormInfo() {
        return formInfo;
    }

    public RADComponent getTopRADComponent() {
        return topRADComponent;
    }

    public List getMetaComponents() {
        ArrayList list = new ArrayList();

        list.add(topRADComponent);
        if (topRADComponent instanceof ComponentContainer)
            collectMetaComponents((ComponentContainer)topRADComponent, list);

        for (Iterator it=nonVisualComponents.iterator(); it.hasNext(); ) {
            Object comp = it.next();
            list.add(comp);
            if (comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer)comp, list);
        }

        return Collections.unmodifiableList(list);
    }

    public RADComponent[] getVisualComponents() {
        ArrayList list = new ArrayList();
        list.add(topRADComponent);
        if (topRADComponent instanceof ComponentContainer)
            collectMetaComponents((ComponentContainer) topRADComponent, list);
        return (RADComponent[]) list.toArray(new RADComponent[list.size()]);
    }

    private static void collectMetaComponents(ComponentContainer cont, ArrayList list) {
        RADComponent[] comps = cont.getSubBeans();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }
    }

    RADComponent getParentComponent(RADComponent comp) {
        if (comp instanceof RADVisualComponent)
            return ((RADVisualComponent)comp).getParentContainer();

        if (comp instanceof RADMenuItemComponent)
            return ((RADMenuItemComponent)comp).getParentMenu();

        return null;
    }

    //
    // {{{ non-visual components
    //

    void initNonVisualComponents(RADComponent[] comps) {
        for (int i = 0; i < comps.length; i++) {
            nonVisualComponents.add(comps[i]);
        }
    }

    void addNonVisualComponent(RADComponent comp, ComponentContainer parentContainer) {
//        comp.setName(variablePool.getNewName(comp.getBeanClass()));
        checkComponentNames(comp);
        if (parentContainer == null) {
            nonVisualComponents.add(comp);
            getNonVisualChildren().updateKeys(this);
        } else {
            parentContainer.add(comp);
        }

        fireComponentAdded(comp, parentContainer);
    }
    
    public ComponentContainer getNonVisualsContainer() {
        if (nonVisualsContainer == null) {
            nonVisualsContainer = new ComponentContainer() {
                public RADComponent[] getSubBeans() {
                    return getNonVisualComponents();
                }

                public void initSubComponents(RADComponent[] initComponents) {
                    initNonVisualComponents(initComponents);
                }

                public void reorderSubComponents(int[] perm) {
                    reorderNonVisualComponents(perm);
                }

                public void add(RADComponent comp) {
                    throw new InternalError(); // should not be used
                }

                public void remove(RADComponent comp) {
                    throw new InternalError(); // should not be used
                }

                public int getIndexOf(RADComponent comp) {
                    throw new InternalError(); // should not be used
                }
            };
        }
        return nonVisualsContainer;
    }

    public RADComponent[] getNonVisualComponents() {
        return (RADComponent[]) nonVisualComponents.toArray(
                                new RADComponent[nonVisualComponents.size()]); 
    }

    void reorderNonVisualComponents(int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = nonVisualComponents.remove(from);
            if (from < to) {
                nonVisualComponents.add(to - 1, value);
            } else {
                nonVisualComponents.add(to, value);
            }
        }
        fireComponentsReordered(null); // no container for non-visuals...
    }

    NonVisualChildren getNonVisualChildren() {
        if (nonVisualChildren == null) {
            nonVisualChildren = new NonVisualChildren(this);
        }
        return nonVisualChildren;
    }

    // }}}


    //
    // {{{ adding/deleting components, setting layout
    //

    /**
     * Check if the names of all subcomponents are valid variables.  This check
     * must be done for cut/paste between two different forms since names of
     * pasted components(valid in form from which they were cutted) can be
     * already used in target form.
     */
    private void checkComponentNames(RADComponent comp) {
        comp.useStoredName();
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] =((ComponentContainer) comp).getSubBeans();
            for (int i=0, n=comps.length; i<n; i++) {
                checkComponentNames(comps[i]);
            }
        }
    }

    public void setContainerLayout(RADVisualContainer metacont,
                                   LayoutSupport layoutSupport) {
        LayoutSupport current = metacont.getLayoutSupport();
        metacont.setLayoutSupport(layoutSupport);
        ((RADChildren)metacont.getNodeReference().getChildren()).updateKeys();
        fireContainerLayoutChanged(metacont, current, layoutSupport);
    }

    public void addVisualComponent(
        RADVisualComponent comp,
        RADVisualContainer parentContainer,
        LayoutSupport.ConstraintsDesc constraints)
    {
        boolean isContainer = comp instanceof RADVisualContainer;
        checkComponentNames(comp);
        parentContainer.add(comp);

        LayoutSupport layoutSup = parentContainer.getLayoutSupport();
        if (layoutSup != null)
            layoutSup.addComponent(comp, constraints);

        fireComponentAdded(comp, parentContainer);
    }

    void removeComponent(RADComponent comp) {
        if (comp instanceof RADVisualComponent) {
            RADVisualComponent vcomp = (RADVisualComponent) comp;
            vcomp.getParentContainer().remove(vcomp);
            vcomp.resetConstraintsProperties();
        }
        else if (comp instanceof RADMenuItemComponent) {
            RADMenuItemComponent menuComp = (RADMenuItemComponent)comp;
            if (menuComp.getParentMenu() == null) { // top-level menu
                nonVisualComponents.remove(comp);
                getNonVisualChildren().updateKeys(this);

                // if removing menu currently used as form's main menu, remove it
                if (comp instanceof RADMenuComponent) {
                    RADVisualFormContainer form = (RADVisualFormContainer)topRADComponent;
                    if (form.getFormMenu() != null
                            && form.getFormMenu().equals(comp.getName()))
                        form.setFormMenu(null);
                }
            }
            else menuComp.getParentMenu().remove(menuComp);
        }
        else {
            nonVisualComponents.remove(comp);
            getNonVisualChildren().updateKeys(this);
        }
    }

    void deleteComponent(RADComponent comp) {
        // remove component from its parent
        removeComponent(comp);

        if (comp instanceof RADMenuItemComponent)
            ((RADMenuItemComponent)comp).freeMenu();

        // delete attached events
        EventSet[] eventSets = comp.getEventHandlers().getEventSets();
        for (int i = 0; i < eventSets.length; i++) {
            Event[] events = eventSets[i].getEvents();
            for (int j = 0; j < events.length; j++) {
                if (events[j].getHandlers().size() > 0) {
                    eventHandlers.removeEventHandler(events[j]);
                }
            }
        }

        deleteVariables(comp);

        fireComponentRemoved(comp);
    }

    // }}}

    //
    // {{{ event and listeners
    // 

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

        if (!readOnly) // form should be marked as modified explicitly
            getFormEditorSupport().markFormModified();
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
                                           LayoutSupport oldLayoutSupp,
                                           LayoutSupport newLayoutSupp) {
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

        // Form should be marked as modified explicitly in case of a property
        // change of serialized bean (the change didn't affect generated code).
        if (!readOnly)
            getFormEditorSupport().markFormModified();
    }

    public void fireComponentAdded(RADComponent metacomp,
                                   ComponentContainer metacont) {
        t("componentAdded, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp, metacont);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentAdded(e);
                l.formChanged(e);
  	    }
  	}
    }
    
    public void fireComponentRemoved(RADComponent metacomp) {
        t("componentRemoved, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N
        FormModelEvent e = new FormModelEvent(this, metacomp);

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
          + (metacont != null ? ((RADComponent)metacont).getName() : "null")); // NOI18N
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
          + (metacomp != null ? metacomp.getName() : "null") // NOI18N
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

        // If changed property is of a serialized component, it may not change
        // generated code of the source file - so the form data object is not
        // marked as modified and cannot be saved - though there are unsaved
        // changes. So form should be marked as modified explicitly if needed.
        if (!readOnly)
            getFormEditorSupport().markFormModified();
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

        // Form should be marked as modified explicitly in case the change
        // did not affect generated code.
        if (!readOnly)
            getFormEditorSupport().markFormModified();
    }

    public void fireEventHandlerAdded(EventHandler handler) {
        t("eventHandlerAdded"); // NOI18N
    }

    public void fireEventHandlerRemoved(EventHandler handler, String oldName) {
        t("eventHandlerRemoved"); // NOI18N
    }

    public void fireEventHandlerRenamed(EventHandler handler) {
        t("eventHandlerRenamed"); // NOI18N
    }

    // }}}

    private void deleteVariables(RADComponent comp) {
        variablePool.deleteVariable(comp.getName());
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] =((ComponentContainer) comp).getSubBeans();
            for (int i=0, n=comps.length; i<n; i++) {
                deleteVariables(comps[i]);
            }
        }
    }

    FormEventHandlers getFormEventHandlers() {
        if (eventHandlers == null)
            eventHandlers = new FormEventHandlers(getCodeGenerator());
        return eventHandlers;
    }

    public VariablePool getVariablePool() {
        if (variablePool == null)
            variablePool = new VariablePool();
        return variablePool;
    }

    FormEditorSupport getFormEditorSupport() {
        return formDataObject.getFormEditor();
    }

    CodeGenerator getCodeGenerator() {
        if (codeGenerator == null) {
            codeGenerator = new JavaCodeGenerator();
//            codeGenerator.initialize(this);
        }
        return codeGenerator;
    }

    public int getMode() {
        return 1;//DesignLayout.DESIGN_MODE;
    }
    
    public void setMode(int mode) {
    }
    
    void initializeCodeGenerator() {
        getCodeGenerator().initialize(this);
    }

    public boolean isTestMode() {
        return false;
    }

    public void setTestMode(boolean test) {
    }

    public Component getVisualRepresentation(RADComponent radComp) {
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

    void setFormDesigner(FormDesigner designer) {
        formDesigner = designer;
    }
    
    public FormDesigner getFormDesigner() {
        if (formDesigner == null) {
            if (!formLoaded)
                System.err.println("[Warning] Form designer requested before form loaded.");

            formDesigner = new FormDesigner(this);
            String name = formDataObject.getName();
            if (isReadOnly())
                name += " " + FormEditor.getFormBundle().getString("CTL_FormTitle_RO"); // NOI18N
            formDesigner.setName(name);

//            formDesigner.setName(java.text.MessageFormat.format(
//                FormEditor.getFormBundle().getString(formDataObject.isReadOnly() ?
//                             "FMT_FormWindowTitle_RO" : "FMT_FormWindowTitle"), // NOI18N
//                new Object[] { formDataObject.getName() }));
        }
        return formDesigner;
    }

    public RADComponent findRADComponent(String name) {
        for (Iterator allComps = getMetaComponents().iterator(); allComps.hasNext();) {
            RADComponent comp =(RADComponent)allComps.next();
            if (name.equals(comp.getName())) return comp;
        }

        return null;
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
