/*
 * Annotation.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.util.Collection;

/**
 * Annotation:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public abstract class Annotation {
    CPClassInfo type;
    AnnotationComponent[] components;
    boolean runtimeVisible;

    Annotation(ConstantPool pool, int iClass, AnnotationComponent[] components,
			boolean runtimeVisible) {
	this.type = (CPClassInfo)pool.get(iClass);
	this.components = components;
	this.runtimeVisible = runtimeVisible;
    }

    /**
     * Returns the annotation type.
     */
    public final CPClassInfo getType() {
	return type;
    }

    /**
     * Returns the named components for this annotation, as an
     * array of AnnotationComponents.
     */
    public final AnnotationComponent[] getComponents() {
	return (AnnotationComponent[])components.clone();
    }

    /**
     * Returns the named component for this annotation, or null if 
     * no component with that name exists.
     */
    public final AnnotationComponent getComponent(String name) {
	for (int i = 0; i < components.length; i++) {
	    AnnotationComponent comp = components[i];
	    if (comp.getName().equals(name))
		return comp;
	}
	return null;
    }

    /**
     * Returns true if this annotation is loaded by the Java Virtual
     * Machine to be available via the Java reflection facility.
     */
    public boolean isRuntimeVisible() {
	return runtimeVisible;
    }

    public String toString() {
	return "@" + type.getClassName() + " runtimeVisible=" + runtimeVisible;
    }
}
