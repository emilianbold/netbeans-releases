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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.editor.property.IElement;
import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.IProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyGroup;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class PropertyGroup implements IPropertyGroup, Comparable {

    private String displayName;
    private String name;
    private INode parent;
    private int position;
    private List propertyList = new ArrayList();

    private PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    private String toolTip;

    private VetoableChangeSupport vSupport = new VetoableChangeSupport(this);

    /** Creates a new instance of PropertyGroup */
    public PropertyGroup() {
    }

    /**
     * add a element in the node
     * 
     * @param element element to add
     */
    public void add(IElement element) {
        element.setParent(this);
        addProperty((IProperty) element);
    }

    /**
     * add a property in this gropu
     * 
     * @param property
     */
    public void addProperty(IProperty property) {
        propertyList.add(property);
    }

    /**
     * add a property change listener
     * 
     * @param listener property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    /**
     * add a vetoable change listener
     * 
     * @param listener vetoable change listener
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vSupport.addVetoableChangeListener(listener);
    }

    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     * <p>
     * In the foregoing description, the notation <tt>sgn(</tt> <i>expression </i>
     * <tt>)</tt> designates the mathematical <i>signum </i> function, which is defined
     * to return one of <tt>-1</tt>,<tt>0</tt>, or <tt>1</tt> according to
     * whether the value of <i>expression </i> is negative, zero or positive. The
     * implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for
     * all <tt>x</tt> and <tt>y</tt>. (This implies that <tt>x.compareTo(y)</tt>
     * must throw an exception iff <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt> implies that
     * <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for all <tt>z</tt>.
     * <p>
     * It is strongly recommended, but <i>not </i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. Generally speaking, any class
     * that implements the <tt>Comparable</tt> interface and violates this condition
     * should clearly indicate this fact. The recommended language is "Note: this class
     * has a natural ordering that is inconsistent with equals."
     * 
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less
     *         than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it from being
     *         compared to this Object.
     */
    public int compareTo(Object o) {
        if (!(o instanceof IPropertyGroup)) {
            throw new ClassCastException("Object " + o + " being compared is not same as this object");
        }

        IPropertyGroup propertyG = (IPropertyGroup) o;

        if (this.getPosition() < propertyG.getPosition()) {
            return -1;
        } else if (this.getPosition() > propertyG.getPosition()) {
            return 1;
        }

        return 0;
    }

    public void firePropertyChangeEvent() {
        pSupport.firePropertyChange(IPropertyGroup.VALID_ALL, this.isValid(), this.isValid());
    }

    public void firePropertyChangeEvent(String propertyName, Object oldVal, Object newVal) {
        pSupport.firePropertyChange(propertyName, oldVal, newVal);
    }

    public void fireVetoableChangeEvent(String propertyName, Object oldVal, Object newVal) throws PropertyVetoException {
        vSupport.fireVetoableChange(propertyName, oldVal, newVal);
    }

    /**
     * get the display name of of element
     * 
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * get the name of of element
     * 
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * get the parent element
     * 
     * @return parent
     */
    public INode getParent() {
        return parent;
    }

    /**
     * get the position where this property should appear in the property sheet gui
     * 
     * @return position
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * get all the properties in this group
     * 
     * @return all the properties in this group
     */
    public List getProperties() {
        return propertyList;
    }

    /**
     * get the tooltip of of element
     * 
     * @return tooltip
     */
    public String getToolTip() {
        return this.toolTip;
    }

    /**
     * is valid value
     * 
     * @return valid value
     */
    public boolean isValid() {
        Iterator it = propertyList.iterator();
        while (it.hasNext()) {
            IProperty property = (IProperty) it.next();
            if (property.isRequired() && !property.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * remove a property change listener
     * 
     * @param listener property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    /**
     * remove a vetoable change listener
     * 
     * @param listener vetoable change listener
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vSupport.removeVetoableChangeListener(listener);
    }

    /**
     * set the display name of the element
     * 
     * @param dName display name
     */
    public void setDisplayName(String dName) {
        this.displayName = dName;
    }

    /**
     * set the name of the element
     * 
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * set parent parent element
     */
    public void setParent(INode parent) {
        this.parent = parent;
    }

    /**
     * set the position where this property should appear in the property sheet gui
     * 
     * @return position
     */
    public void setPosition(String position) {
        this.position = Integer.parseInt(position);
    }

    /**
     * set the tooltip of the element
     * 
     * @param tTip tool tip
     */
    public void setToolTip(String tTip) {
        this.toolTip = tTip;
    }
}

