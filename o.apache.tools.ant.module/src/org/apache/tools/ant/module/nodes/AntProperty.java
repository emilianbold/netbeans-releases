/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick.
 */

package org.apache.tools.ant.module.nodes;

import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.apache.tools.ant.module.api.AntProjectCookie;
/** Wraps an Ant property in an IDE node property.
 */
public class AntProperty extends Node.Property {
    
    private Element el;
    private String name;
    private AntProjectCookie proj;
    
    public AntProperty (Element el, String name, AntProjectCookie proj) {
        this (name, proj);
        this.el = el;
    }
    
    protected AntProperty (String name, AntProjectCookie proj) {
        super (String.class);
        setName (name);
        this.name = name;
        this.proj = proj;
    }
    
    protected Element getElement () {
        return el;
    }
    
    public Object getValue () {
        Element el = getElement ();
        if (el == null) { // #9675
            return NbBundle.getMessage (AntProperty.class, "LBL_property_invalid_no_element");
        }
        return el.getAttribute (name);
    }
    
    public void setValue (Object value) throws IllegalArgumentException, InvocationTargetException {
        Element el = getElement ();
        if (el == null) return;
        if (value == null) value = ""; // NOI18N
        if (! (value instanceof String)) throw new IllegalArgumentException ();
        if (value.equals ("")) { // NOI18N
            try {
                el.removeAttribute (name);
            } catch (DOMException dome) {
                throw new InvocationTargetException (dome);
            }
        } else {
            el.setAttribute (name, (String) value);
        }
    }
    
    public boolean canRead () {
        return true;
    }
    
    public boolean canWrite () {
        return (getElement () != null && ! AntProjectNode.isScriptReadOnly(proj));
    }
    
    public boolean supportsDefaultValue () {
        return (getElement () != null);
    }
    
    public void restoreDefaultValue () throws IllegalArgumentException, InvocationTargetException {
        setValue (""); // NOI18N
    }
    
}
