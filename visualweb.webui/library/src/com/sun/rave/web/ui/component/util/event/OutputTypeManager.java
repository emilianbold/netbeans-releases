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
package com.sun.rave.web.ui.component.util.event;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;


/**
 *  <P>	This class provides the functionality for the OutputTypeManager.  The
 *	OutputTypeManager manages the various OutputTypes that can be used.
 *	The OutputTypes are managed statically.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class OutputTypeManager {


    /**
     *	Constructor.
     */
    protected OutputTypeManager() {
    }

    /**
     *
     */
    public static OutputTypeManager getInstance() {
	return _defaultInstance;
    }

    /**
     *	<P> This is a factory method for obtaining an OutputTypeManager
     *	    instance. This implementation uses the external context's
     *	    initParams to look for the OutputTypeManager class.  If it
     *	    exists, the specified concrete OutputTypeManager class will
     *	    be used.  Otherwise, the default will be used -- which is an
     *	    instance of this class.  The initParam key is:
     *	    {@link #OUTPUT_TYPE_MANAGER_KEY}.</P>
     *
     *	@param	context	    The FacesContext
     *
     *	@see #OUTPUT_TYPE_MANAGER_KEY
     */
    public static OutputTypeManager getManager(FacesContext context) {
	if (context == null) {
	    return _defaultInstance;
	}

	// If the context is non-null, check for init parameter specifying
	// the Manager
	String className = null;
	Map initParams = context.getExternalContext().getInitParameterMap();
	if (initParams.containsKey(OUTPUT_TYPE_MANAGER_KEY)) {
	    className = (String)initParams.get(OUTPUT_TYPE_MANAGER_KEY);
	}
	return getManager(className);
    }


    /**
     *	This method is a singleton factory method for obtaining an instance of
     *	a OutputTypeManager.  It is possible that multiple different
     *	implementations of OutputTypeManagers will be used within the
     *	same JVM.  This is OK, the purpose of the OutputTypeManager is
     *	primarily performance.  Someone may provide a different
     *	OutputTypeManager to locate OutputTypeManager's in a different way
     *	(XML, database, file, java code, etc.).
     */
    public static OutputTypeManager getManager(String className) {
	if (className == null) {
	    // Default case...
	    return _defaultInstance;
	}

	OutputTypeManager ldm =
	    (OutputTypeManager)_instances.get(className);
	if (ldm == null) {
	    try {
		ldm = (OutputTypeManager)Class.forName(className).
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
     *	<P> This method retrieves an OutputType.</P>
     *
     *	@param	name	The name of the OutputType.
     *
     *	@return	The requested OutputType.
     */
    public OutputType getOutputType(String name) {
	return (OutputType)_outputTypes.get(name);
    }

    /**
     *	<P> This method sets an OutputType.</P>
     *
     *	@param	name	    The name of the OutputType.
     *	@param	outputType  The OutputType.
     */
    public void setOutputType(String name, OutputType outputType) {
	_outputTypes.put(name, outputType);
    }

    /**
     *	<P> Cache different subclasses. </P>
     */
    private static Map _outputTypes = new HashMap(8);

    /**
     *	<P> Cache different subclasses. </P>
     */
    private static Map _instances = new HashMap(2);

    /**
     *	<P> This is the default implementation of the OutputTypeManager, which
     *	    happens to be an instance of this class (because I'm too lazy to
     *	    do this right).</P>
     */
    private static OutputTypeManager _defaultInstance =
	new OutputTypeManager();


    /**
     *	<P> This constant defines the layout definition manager implementation
     *	    key for initParams. The value for this initParam should be the
     *	    full class name of an {@link OutputTypeManager}.
     *	    ("outputTypeManagerImpl")</P>
     */
    public static final String OUTPUT_TYPE_MANAGER_KEY =
	"outputTypeManagerImpl";

    public static final String  REQUEST_ATTRIBUTE_TYPE	=   "attribute";
    public static final String  SESSION_ATTRIBUTE_TYPE	=   "session";

    static {
	_outputTypes.put(REQUEST_ATTRIBUTE_TYPE, new RequestAttributeOutputType());
	_outputTypes.put(SESSION_ATTRIBUTE_TYPE, new SessionAttributeOutputType());
    }
}
