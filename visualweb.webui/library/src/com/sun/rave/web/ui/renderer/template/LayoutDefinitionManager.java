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
package com.sun.rave.web.ui.renderer.template;

import com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition;

import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
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
 *  <P>	This abstract class provides the base functionality for all
 *	<code>LayoutDefinitionManager</code> implementations.  It provides an
 *	static method used to obtain an instance of a concrete
 *	<code>LayoutDefinitionManager</code>: {@link #getManager(FacesContext)}.
 *	It also provides another version of this method which allows a specific
 *	instance to be specified by classname:
 *	{@link #getManager(String className)} (typically not used, the
 *	environment should be setup to provide the correct
 *	<code>LayoutDefinitionManager</code>).  Once an instance is obtained,
 *	the {@link #getLayoutDefinition(String key)} method can be invoked to
 *	obtain a {@link com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition}.
 *  </P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public abstract class LayoutDefinitionManager {


    /**
     *	Constructor.
     */
    protected LayoutDefinitionManager() {
	super();
    }


    /**
     *	This method is responsible for finding/creating the requested
     *	LayoutDefinition.
     *
     *	@param	key	The key used to identify the requested LayoutDefintion
     */
    public abstract LayoutDefinition getLayoutDefinition(String key);


    /**
     *	This is a factory method for obtaining the LayoutDefinitionManager.
     *	This implementation uses the external context's initParams to look for
     *	the LayoutDefinitionManager class.  If it exists, the specified
     *	concrete LayoutDefinitionManager class will be used.  Otherwise, the
     *	default LayoutDefinitionManager will be used.  The initParam key is:
     *	{@link #LAYOUT_DEFINITION_MANAGER_KEY}.
     *
     *	@param	context	The FacesContext
     *
     *	@see #LAYOUT_DEFINITION_MANAGER_KEY
     */
    public static LayoutDefinitionManager getManager(FacesContext context) {
// FIXME: Decide how to define the LAYOUT_DEFINITION_MANAGER
// FIXME: Properties should be settable on the LDM, such as entity resolvers...
	Map initParams = context.getExternalContext().getInitParameterMap();
	String className = DEFAULT_LAYOUT_DEFINITION_MANAGER_IMPL;
	if (initParams.containsKey(LAYOUT_DEFINITION_MANAGER_KEY)) {
	    className = (String)initParams.get(LAYOUT_DEFINITION_MANAGER_KEY);
	}
	return getManager(className);
    }


    /**
     *	This method is a singleton factory method for obtaining an instance of
     *	a LayoutDefintionManager.  It is possible that multiple different
     *	implementations of LayoutDefinitionManagers will be used within the
     *	same JVM.  This is OK, the purpose of the LayoutDefinitionManager is
     *	primarily performance.  Someone may provide a different
     *	LayoutDefinitionManager to locate LayoutDefiniton's in a different way
     *	(XML, database, file, java code, etc.).
     */
    public static LayoutDefinitionManager getManager(String className) {
	LayoutDefinitionManager ldm =
	    (LayoutDefinitionManager)_instances.get(className);
	if (ldm == null) {
	    try {
		ldm = (LayoutDefinitionManager)Class.forName(className).
		    getMethod("getInstance", null).
		    invoke(null, null);
	    } catch (ClassNotFoundException ex) {
		throw new RuntimeException(ex);
	    } catch (NoSuchMethodException ex) {
		throw new RuntimeException(ex);
	    } catch (IllegalAccessException ex) {
		throw new RuntimeException(ex);
	    } catch (InvocationTargetException ex) {
		throw new RuntimeException(ex);
	    } catch (NullPointerException ex) {
		throw new RuntimeException(ex);
	    } catch (ClassCastException ex) {
		throw new RuntimeException(ex);
	    }
	    _instances.put(className, ldm);
	}
	return ldm;
    }


    /**
     *	Retrieve an attribute by key
     *
     *	@param	key	The key used to retrieve the attribute
     *
     *	@return	The requested attribute or null
     */
    public Object getAttribute(String key) {
	return _attributes.get(key);
    }


    /**
     *	Associate the given key with the given Object as an attribute.
     *
     *	@param	key	The key associated with the given object (if this key
     *	    is already in use, it will replace the previously set attribute
     *	    object).
     *
     *	@param	value	The Object to store.
     */
    public void setAttribute(String key, Object value) {
	_attributes.put(key, value);
    }


    /**
     *	This map contains sub-class specific attributes that may be needed by
     *	specific implementations of LayoutDefinitionManagers.  For example,
     *	setting an EntityResolver on a LayoutDefinitionManager that creates
     *	LayoutDefinitions from XML files.
     */
    private Map _attributes = new HashMap();


// FIXME: Rethink this... since I am allowing LDM's to be parameterized via
// FIXME: attributes, it is not enough to have 1 LDM... we will need 1 per
// FIXME: application.  Or... I need to move the attributes.
    /**
     *	Static map of LayoutDefinitionManagers.  Normally this will only
     *	contain the default LayoutManager.
     */
    private static Map _instances = new HashMap(2);


    /**
     *	This constant defines the default layout definition manager
     *	implementation class name.
     */
    public static final String DEFAULT_LAYOUT_DEFINITION_MANAGER_IMPL =
	"com.sun.rave.web.ui.renderer.template.xml.XMLLayoutDefinitionManager";


    /**
     *	This constant defines the layout definition manager implementation key
     *	for initParams. ("layoutManagerImpl")
     */
    public static final String LAYOUT_DEFINITION_MANAGER_KEY =
	"layoutManagerImpl";
}
