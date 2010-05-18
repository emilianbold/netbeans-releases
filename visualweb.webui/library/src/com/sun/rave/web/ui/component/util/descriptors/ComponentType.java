/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
