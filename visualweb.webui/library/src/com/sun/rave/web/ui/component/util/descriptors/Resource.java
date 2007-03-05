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

import com.sun.rave.web.ui.util.ResourceFactory;

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
 *  <P>	This class holds information that describes a Resource.  It
 *	provides access to a {@link ResourceFactory} for obtaining the
 *	actual Resource object described by this descriptor.  See the
 *	layout.dtd file for more information on how to define a Resource
 *	via XML.  The LayoutDefinition will add all defined Resources to
 *	the Request scope for easy access (including via JSF EL).</P>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class Resource implements java.io.Serializable {

    /**
     *	This is the id for the Resource
     */
    private String _id				= null;

    /**
     *	This holds "extraInfo" for the Resource, such as a ResourceBundle
     *	baseName.
     */
    private String _extraInfo			= null;


    /**
     *	This is a String className for the Factory.
     */
    private String _factoryClass		= null;


    /**
     *	The Factory that produces the desired UIComponent.
     */
    private transient ResourceFactory _factory	= null;


    /**
     *	Constructor
     */
    public Resource(String id, String extraInfo, String factoryClass) {
	if (id == null) {
	    throw new NullPointerException("'id' cannot be null!");
	}
	if (factoryClass == null) {
	    throw new NullPointerException("'factoryClass' cannot be null!");
	}
	_id = id;
	_extraInfo = extraInfo;
	_factoryClass = factoryClass;
	_factory = createFactory();
    }


    /**
     *	Accessor method for ID.  This is the key the resource will be stored
     *	under in the Request scope.
     */
    public String getId() {
	return _id;
    }

    /**
     *	This holds "extraInfo" for the Resource, such as a ResourceBundle
     *	baseName.
     */
    public String getExtraInfo() {
	return _extraInfo;
    }


    /**
     *	This method provides access to the ResourceFactory.
     *
     *	@return ResourceFactory
     */
    public ResourceFactory getFactory() {
	if (_factory == null) {
	    _factory = createFactory();
	}
	return _factory;
    }

    /**
     *	This method creates a new factory.
     *
     *	@return ResourceFactory
     */
    protected ResourceFactory createFactory() {
	try {
	    Class cls = Class.forName(_factoryClass);
	    return (ResourceFactory)cls.newInstance();
	} catch (ClassNotFoundException ex) {
	    throw new RuntimeException(ex);
	} catch (InstantiationException ex) {
	    throw new RuntimeException(ex);
	} catch (IllegalAccessException ex) {
	    throw new RuntimeException(ex);
	}
    }
}
