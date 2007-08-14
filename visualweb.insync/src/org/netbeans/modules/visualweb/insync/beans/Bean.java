
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.beans;

import com.sun.rave.designtime.Position;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.filesystems.FileObject;


/**
 * Representation of a JavaBean instance field within our outer host BeansUnit being built. Initial
 * property settings are maintained as Property instances, and handled events are maintained as
 * Events within EventSets.
 *
 * @author cquinn
 */
public class Bean extends BeansNode {

    public static final Bean[] EMPTY_ARRAY = {};

    static final boolean CREATE_GETTER = true;  // generate host getter for this bean
    static final boolean CREATE_SETTER = true;  // generate host setter for this bean

    protected final BeanInfo beanInfo;
    protected final ArrayList properties = new ArrayList();
    protected final ArrayList eventSets = new ArrayList();

    private String name;
    private List<String> typeParameterNames;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a new created bean
     *
     * @param unit
     * @param beanInfo
     * @param name
     */
    protected Bean(BeansUnit unit, BeanInfo beanInfo, String name) {
        super(unit);
        this.beanInfo = beanInfo;
        this.name = name;
    }

    /**
     * Construct a new bean bound to existing field & accessor methods
     *
     * @param unit Owning host unit
     * @param beanInfo
     * @param name
     * @param typeNames
     */
    protected Bean(BeansUnit unit, BeanInfo beanInfo, String name, List<String> typeNames) {
        this(unit, beanInfo, name);
        typeParameterNames = typeNames;
        bindCleanup();
    }

    /**
     * Create the underlying field and accessor methods
     *
     * @param after existing bean that this bean's entries will be added after
     */
    public void insertEntry(Bean after) {
        Class type = beanInfo.getBeanDescriptor().getBeanClass();
        unit.getThisClass().addProperty(name, type, CREATE_GETTER, CREATE_SETTER);
        String cmn = getCleanupMethod();
        if (cmn != null) {
            Method method = unit.getCleanupMethod();
            if (method != null) {
                method.addPropertyStatement(name, cmn, null);
            }
        }
    }

    /**
     * Remove this bean's field, methods and statements from the host class. This bean instance is
     * dead & should not be used.
     *
     * @return true iff the source entry for this bean was actually removed.
     */
    protected boolean removeEntry() {
        assert Trace.trace("insync.beans", "B.removeEntry: " + this);
        boolean removed = false;
        for (Iterator i = properties.iterator(); i.hasNext(); ) {
            Property p = (Property)i.next();
            removed |= p.removeEntry();
            i.remove();
        }
        for (Iterator i = eventSets.iterator(); i.hasNext(); ) {
            EventSet es = (EventSet)i.next();
            removed |= es.removeEntry();
            i.remove();
        }
        String cmn = getCleanupMethod();  // the name of this bean's cleanup method, if any
        if (cmn != null && unit.getCleanupMethod() != null) {
                unit.getCleanupMethod().removeStatement(name, cmn);
        }
        unit.getThisClass().removeProperty(name);

        removed |= true; //!CQ don't really know since clazz didn't tell us...
        return removed;
    }

    /**
     * Bind to an existing cleanup method call within the unit's cleanup method body
     */
    public void bindCleanup() {
    }

    //------------------------------------------------------------------------------------ Parenting

    /**
     * @return the parent of this bean, null if top-level bean or dead
     */
    public Bean getParent() {
        return null;
    }

    /**
     * Take the opportinuty to scan for and bind to this bean's parent
     *
     * @return the parent of this bean iff not previously bound
     */
    public Bean bindParent() {
        return null;
    }

    /**
     * @return true if this bean is capable of being a parent
     */
    public boolean isParentCapable() {
        return false;  // plain old Java beans don't know about any kind of parenting
    }

    /**
     * Add a child bean to this bean at a given location.
     *
     * @param child The child bean to add
     * @param pos The position within the children to add the given child
     */
    public void addChild(Bean child, Position pos) {
    }

    /**
     * Remove a child bean from this bean.
     *
     * @param child
     */
    public void removeChild(Bean child) {
    }

    /**
     * @return the array of child beans--zero size if none currently, or null if this is not a
     *         parent
     * @see isParentCapable
     */
    public Bean[] getChildren() {
        return null;
    }

    /**
     * Given an instance for this bean and it's parent, perform the appropriate operation on those
     * objects to establish the live parent-child relationship.
     * Overridden in subclasses that know about specific bean parent-child relationships
     *
     * @param instance  the bean instance to parent
     * @param parent  the parent to parent to
     * @return true if done parenting, or false to be re-called with parent's parent
     */
    public boolean performInstanceParenting(Object instance, Object parent, Position pos) {
        return true;
    }

    /**
     * Same as performInstanceParenting(), except performs the un-parenting.
     *
     * @param instance  the bean instance to parent
     * @param parent  the parent to parent to
     */
    public void performInstanceUnparenting(Object instance, Object parent) {
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @return the beanInfo for this bean
     */
    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

    /**
     * @return the type of this bean
     */
    public String getType() {
        return beanInfo.getBeanDescriptor().getBeanClass().getName();
    }

    /**
     * @return the instance name of this bean, null if dead.
     */
    public String getName() {
        return name;
    }

    /**
     * @return whether there is a getter method available
     */
    public boolean hasGetter() {
        return true;
    }

    /**
     * Get the cleanup method name for this bean if it has one
     * @return the cleanup method name
     */
    public String getCleanupMethod() {
        Object cleanupO = beanInfo.getBeanDescriptor().getValue(Constants.BeanDescriptor.CLEANUP_METHOD);
        if (cleanupO instanceof String)
            return (String)cleanupO;
        return null;
    }

    /**
     * Can the name of this bean be set? Default is to always say yes.
     *
     * @return true iff the name of this bean can be set
     */
    public boolean canSetName() {
        return true;
    }

    /**
     * Set the name of this bean, affects the field name as well as the accessor method names
     *
     * @param newname  The new name to give this bean, possibly as a base for suffixes.
     * @param autoNumber  If true, name will be suffixed with a number if needed to make it unique
     * @param liveBean  The referencing liveBean for possible naming fixup callback
     * @return The new name, after any fixup or numbering. Null if naming failed.
     */
    public String setName(String newname, boolean autoNumber, DesignBean liveBean) {
        if (autoNumber) {
            newname = unit.nextAvailableName(newname, this, false);
        }else if (!unit.isBeanNameAvailable(newname, this)) {
            return null;
        }
        
        String oldname = name;
        if (!oldname.equals(newname)) {
            //System.err.println("B.setName " + oldname + "=>" + name);
            name = newname;
            List<FileObject> fObjs = new ArrayList<FileObject>();
            FacesModel currentModel = (FacesModel)unit.getModel();
            fObjs.add(currentModel.getJavaFile());
            if (!currentModel.isPageBean()) {
                //In case of non-page beans, it is necessary to update the property 
                //binding expression and accessor methods in lesser scoped beans
                FacesModel[] models = ((FacesModelSet) currentModel.getOwner()).getFacesModels();
                for (int i = 0; i < models.length; i++) {
                    FileObject fObj = models[i].getJavaFile();
                    //If the faces model is not yet synced(because it may not be open), then
                    //get the file object for java file via its corresponding jsp file
                    if (fObj == null && (models[i].getFile() == models[i].getMarkupFile())) {
                        fObj = FacesModel.getJavaForJsp(models[i].getFile());
                    }
                    fObjs.add(fObj);
                }
            }

            unit.getThisClass().renameProperty(oldname, newname, fObjs);
        }
        return newname;
    }

    /**
     * @return The DOM element underlying this bean, if applicable.
     */
    public org.w3c.dom.Element getElement() {
        return null;
    }

    //----------------------------------------------------------------------------------- Properties

    /**
     * Get the PropertyDescriptor for a property of this bean indicated by the property name
     *
     * @param propertyName the property name to look for
     * @return the PropertyDescriptor for the property
     */
    public PropertyDescriptor getPropertyDescriptor(String propertyName) {
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            if (pds[i].getName().equals(propertyName))
                return pds[i];
        }
        return null;
    }

    /**
     * Get the PropertyDescriptor for a property of this bean indicated by the property setter
     * method name
     *
     * @param setterName the setter method name to lookup the property by
     * @return the PropertyDescriptor for the property
     */
    public PropertyDescriptor getPropertyDescriptorForSetter(String setterName) {
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            java.lang.reflect.Method m = pds[i].getWriteMethod();
            if (m != null && m.getName().equals(setterName))
                return pds[i];
        }
        return null;
    }

    /**
     * Determines if a given Property, defined by its PropertyDescriptor, is a markup based Property
     * or not.
     *
     * @param pd  PropertyDescriptor that identifies the property
     * @return True iff this bean is markup based and the particular property is also.
     */
    public boolean isMarkupProperty(PropertyDescriptor pd) {
        return false;
    }

    /**
     * @return An array of Property instances representing the set properties for this bean.
     */
    public Property[] getProperties() {
        return (Property[])properties.toArray(Property.EMPTY_ARRAY);
    }

    /**
     * Get a Property of this bean by name.
     *
     * @param name  The Property name to look up
     * @return The Property if found, null if not.
     */
    public Property getProperty(String name) {
        for (Iterator i = properties.iterator(); i.hasNext(); ) {
            Property p = (Property)i.next();
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    /**
     *
     *
    public Property getProperty(PropertyDescriptor pd) {
        for (Iterator i = properties.iterator(); i.hasNext(); ) {
            Property p = (Property)i.next();
            if (p.getDescriptor() == pd)
                return p;
        }
        return null;
    }*/

    /**
     * Create a new property object ready to have its value set. Overridden in subclasses to create
     * different property subclasses based on information in the descriptor.
     *
     * @param pd  The descriptor that defines the property
     * @return The newly created property object, never null.
     */
    protected Property newCreatedProperty(PropertyDescriptor pd) {
        return new Property(this, pd);
    }

    /**
     * Set a property, identified by name, to a given value or value source. Both the value instance
     * and source must be provided. A new property will be created to hold this setting if needed.
     *
     * @param name  The name of the property to set.
     * @param value  The instance of the value to set
     * @param valueSource  The source string that represents the value
     * @return The property that was set and possibly created, or null if the property was not found
     */
    public Property setProperty(String name, Object value, String valueSource) {
        Property p = getProperty(name);
        if (p == null) {
            PropertyDescriptor pd = getPropertyDescriptor(name);
            if (pd == null)
                return null;
            p = newCreatedProperty(pd);
            properties.add(p);
        }
        p.setValue(value, valueSource);
        return p;
    }

    /**
     * Unset (remove) a given property from this bean.
     *
     * @param p  Property to unset & remove.
     */
    public void unsetProperty(Property p) {
        properties.remove(p);
        p.removeEntry();
    }

    /**
     * Unset (remove) a given property, indicated by name, from this bean.
     *
     * @param name  Property name to unset and remove.
     */
    public void unsetProperty(String name) {
        Property p = getProperty(name);
        if (p != null)
            unsetProperty(p);
    }

    //------------------------------------------------------------------------------------ EventSets

    /**
     * Get the descriptor for an event set of this bean indicated by the event set name.
     *
     * @param name  the event set name to lookup
     * @return The EventSetDescriptor for the given event set
     */
    public EventSetDescriptor getEventSetDescriptor(String name) {
        EventSetDescriptor[] esds = beanInfo.getEventSetDescriptors();
        for (int i = 0; i < esds.length; i++) {
            if (esds[i].getName().equals(name))
                return esds[i];
        }
        return null;
    }

    /**
     * Get the descriptor for an event set of this bean indicated by the event set's adder method
     * name.
     *
     * @param adderName  Event set adder method name.
     * @return The EventSetDescriptor for the given event set
     */
    public EventSetDescriptor getEventSetDescriptorForAdder(String adderName) {
        EventSetDescriptor[] esds = beanInfo.getEventSetDescriptors();
        for (int i = 0; i < esds.length; i++) {
            java.lang.reflect.Method m = esds[i].getAddListenerMethod();
            if (m != null && m.getName().equals(adderName))
                return esds[i];
        }
        return null;
    }

    /**
     * @return An array of EventSet instances representing the hooked event sets for this bean.
     */
    public EventSet[] getEventSets() {
        return (EventSet[])eventSets.toArray(EventSet.EMPTY_ARRAY);
    }

    /**
     * Get an EventSet of this bean by name.
     *
     * @param name  The EventSet name to look for
     * @return EventSet of this bean with the given name, or null if not found.
     */
    public EventSet getEventSet(String name) {
        for (Iterator i = eventSets.iterator(); i.hasNext(); ) {
            EventSet es = (EventSet)i.next();
            if (es.getName().equals(name))
                return es;
        }
        return null;
    }

    /**
     * Create a new EventSet object. Overridden in subclasses to create different EventSet
     * subclasses based on information in the descriptor.
     *
     * @param esd The descriptor that defines the EventSet.
     * @return The newly created EventSet object, never null.
     */
    protected EventSet newCreatedEventSet(EventSetDescriptor esd) {
        return new EventSet(this, esd, true);
    }

    /**
     * Set (hook) a given event set indicated by name. A new EventSet will be created if needed.
     *
     * @param name  The EventSet name to set.
     * @return The existing or newly created EventSet.
     */
    public EventSet setEventSet(String name) {
        EventSet es = getEventSet(name);
        if (es == null) {
            EventSetDescriptor esd = getEventSetDescriptor(name);
            es = newCreatedEventSet(esd);
            eventSets.add(es);
        }
        return es;
    }

    /**
     * Remove a logical event set and release its hold on the source elements, but do not actually
     * remove the source. Used when some other modelling code will take over.
     *
     * @param es EventSet to release.
     */
    public void releaseEventSet(EventSet es) {
       eventSets.remove(es);
       es.releaseEntry();
    }

    /**
     * Unset (unhook) a given EventSet and remove its source representation.
     *
     * @param es  EventSet to unset.
     */
    public void unsetEventSet(EventSet es) {
        eventSets.remove(es);
        es.removeEntry();
    }

    /**
     * Unset (unhook) a given EventSet, indicated by name, and remove its source representation.
     *
     * @param name  EventSet name to unset.
     */
    public void unsetEventSet(String name) {
        EventSet es = getEventSet(name);
        if (es != null)
            unsetEventSet(es);
    }

    //--------------------------------------------------------------------------------------- Object

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansNode#toString(java.lang.StringBuffer)
     */
    public void toString(StringBuffer sb) {
        sb.append(" name:");
        sb.append(getName());
        sb.append(" props:");
        Property[] props = getProperties();
        for (int i = 0; i < props.length; i++)
            sb.append(props[i].toString());
        sb.append(" eventSets:");
        EventSet[] eventSets = getEventSets();
        for (int i = 0; i < eventSets.length; i++)
            sb.append(eventSets[i].toString());
        if (isParentCapable()) {
            sb.append(" kids:");
            Bean[] kids = getChildren();
            for (int i = 0; i < kids.length; i++)
                sb.append(kids[i].toString());
        }
    }

    /**
     * If I return null, indicates I could not determine scope.
     * "request", "session", "application"
     * @return
     */
    public String getScope() {
        if (unit.getModel() instanceof FacesModel) {
            FacesModel facesModel = (FacesModel) unit.getModel();
            ManagedBean.Scope scope = facesModel.getManagedBeanEntryScope();
            if (scope == null)
                return null;
            return scope.toString();
        }
        return null;
    }
    
    public boolean shouldInsertCleanupEntry() {
        String scope = getScope();
        if (scope == null) {
            return true;
        }
        if ("request".equals(scope)) {
            return true;
        }
        return false;
    }
    
    public List<String> getTypeParameterNames() {
        return typeParameterNames;
    }

}
