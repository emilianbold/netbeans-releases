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
package com.sun.rave.web.ui.component.util.descriptors;

import com.sun.rave.web.ui.component.util.factories.ComponentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIViewRoot;
import javax.faces.render.Renderer;

/**
 *  <P>	This class holds information that describes a {@link LayoutComponent}
 *	type.  It provides access to a {@link ComponentFactory} for
 *	instantiating an instance of a the UIComponent described by this
 *	descriptor.  See the layout.dtd file for more information on how to
 *	declare types via XML.</P>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ComponentType implements java.io.Serializable {

    /**
     *	Constructor
     */
    public ComponentType(String id, String factoryClass) {
	if (id == null) {
	    throw new NullPointerException("'id' cannot be null!");
	}
	if (factoryClass == null) {
	    throw new NullPointerException("'factoryClass' cannot be null!");
	}
	_id = id;
	_factoryClass = factoryClass;
	_factory = createFactory();
    }


    public String getId() {
	return _id;
    }


    /**
     *	This method provides access to the UIComponentFactory.
     *
     *	@return ComponentFactory
     */
    public ComponentFactory getFactory() {
	if (_factory == null) {
	    _factory = createFactory();
	}
	return _factory;
    }


    /**
     *	This method creates a new factory.
     *
     *	@return ComponentFactory
     */
    protected ComponentFactory createFactory() {
	try {
	    Class cls = Class.forName(_factoryClass);
	    return (ComponentFactory)cls.newInstance();
	} catch (ClassNotFoundException ex) {
	    throw new RuntimeException(ex);
	} catch (InstantiationException ex) {
	    throw new RuntimeException(ex);
	} catch (IllegalAccessException ex) {
	    throw new RuntimeException(ex);
	}
    }


    /**
     *	This is the id for the ComponentType
     */
    private String _id				= null;


    /**
     *	This is a String className for the Factory.
     */
    private String _factoryClass		= null;


    /**
     *	The Factory that produces the desired UIComponent.
     */
    private transient ComponentFactory _factory	= null;
}
