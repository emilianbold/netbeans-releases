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
package com.sun.rave.web.ui.renderer.template.xml;

import  com.sun.rave.web.ui.renderer.template.LayoutDefinitionManager;
import  com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition;

import java.beans.Beans;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *  <P>	This class is a concrete implmentation of the abstract class
 *	{@link LayoutDefinitionManager}.  It obtains {@link LayoutDefinition}
 *	objects by interpreting the <code>key</code> passed to
 *	{@link #getLayoutDefinition(String)} as a path to an XML file
 *	describing the {@link LayoutDefinition}.  It will first attempt to
 *	resolve this path from the document root of the ServletContext or
 *	PortletCotnext.  If that fails, it will attempt to use the Classloader
 *	to resolve it.</P>
 *
 *  <P>	Locating the dtd for the XML file is done in a similar manner.  It
 *	will first attempt to locate the dtd relative to the ServletContext
 *	(or PortletContext) root.  If that fails it will attempt to use the
 *	ClassLoader to resolve it.  Optionally a different EntityResolver may
 *	be supplied to provide a custom way of locating the dtd, this is done
 *	via {@link #setEntityResolver}.</P>
 *
 *  <P>	This class is a singleton.  This means modifications to this class
 *	effect all threads using this class.  This includes setting
 *	EntityResolvers and ErrorHandlers.  These values only need to be set
 *	once to remain in effect as long as the JVM is running.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class XMLLayoutDefinitionManager extends LayoutDefinitionManager {


    /**
     *	Constructor.
     */
    protected XMLLayoutDefinitionManager() {
	super();

	// Set the default XMLError Handler
	try {
	    setErrorHandler(new XMLErrorHandler(new PrintWriter(
		    new OutputStreamWriter(System.err, "UTF-8"), true)));
	} catch (UnsupportedEncodingException ex) {
	    throw new RuntimeException(ex);
	}

	// Set the default EntityResolver
	setEntityResolver(new ClassLoaderEntityResolver());
    }


    /**
     *	This method returns an instance of this LayoutDefinitionManager.  The
     *	object returned is a singleton (only 1 instance will be created per
     *	JVM).
     *
     *	@return	<code>XMLLayoutDefinitionManager</code> instance
     */
    public static LayoutDefinitionManager getInstance() {
	if (instance == null) {
	    instance = new XMLLayoutDefinitionManager();
	}
	return instance;
    }


    /**
     *	<p> This method is responsible for finding the requested
     *	    {@link LayoutDefinition} for the given <code>key</code>.</p>
     *
     *	@param	key Key identifying the desired {@link LayoutDefinition}
     *
     *	@return	The requested {@link LayoutDefinition}.
     */
    public LayoutDefinition getLayoutDefinition(String key) {
	LayoutDefinition ld = (LayoutDefinition) layouts.get(key);
	if (DEBUG) {
	    // Disable caching
	    ld = null;
	}
	if (ld == null) {
	    String baseURI = getBaseURI();

	    // Attempt to load the LayoutDefinition from the CLASSPATH...
	    // Check for XML file in docroot.  Use docroot for the baseURI,
	    // and get the full path to the xml file
	    URL ldURL = null;
	    Object ctx = FacesContext.getCurrentInstance().
		getExternalContext().getContext();
	    String url;

	    // The following should work w/ a ServletContext or PortletContext
	    Method method = null;
	    try {
		method = ctx.getClass().getMethod(
			"getRealPath", GET_REAL_PATH_ARGS);
	    } catch (NoSuchMethodException ex) {
		throw new RuntimeException(ex);
	    }
	    try {
		if (baseURI == null) {
		    baseURI = "file:///"
			+ method.invoke(ctx, new Object [] {"/"});
		}
		url = (String) method.invoke(ctx, new Object [] {key});
	    } catch (IllegalAccessException ex) {
		throw new RuntimeException(ex);
	    } catch (InvocationTargetException ex) {
		throw new RuntimeException(ex);
	    }

	    // Verify file exists...
	    if (!(new File(url).canRead())) {
		url = null;
	    }

	    // Create a URL to the xml file
	    if (url != null) {
		try {
		    ldURL = new URL("file:///" + url);
		} catch (Exception ex) {
		    throw new RuntimeException(
			"Unable to create URL: 'file:///" + url
			+ "' while attempting to locate '" + key + "'", ex);
		}
	    }

	    if (ldURL == null) {
		// Check the classpath for the xml file
		ldURL = getClass().getClassLoader().getResource(key);
		if (ldURL == null) {
		    int idx = key.indexOf('/');
		    if (idx > -1) {
			ldURL = getClass().getClassLoader().getResource(key.substring(idx+1));
		    }
		}
                if (Beans.isDesignTime()) {
                    String path = ldURL.getPath();
                    int i = path.indexOf("/" + key);
                    if (i > -1) {
                        baseURI = path.substring(0, i);
		    }
                }
	    }

	    // Make sure we found the url
	    if (ldURL == null) {
		throw new RuntimeException("Unable to locate '" + key + "'");
	    }

            if (baseURI == null && Beans.isDesignTime()) {
                 String path = ldURL.getPath();
                 int i = path.indexOf("/" + key);
                 if (i > -1) {
                     baseURI = path.substring(0, i);
		 }
            }

	    // Read the XML file
	    try {
		ld  = new XMLLayoutDefinitionReader(
		    ldURL, getEntityResolver(), getErrorHandler(), baseURI).
			read();
	    } catch (IOException ex) {
		throw new RuntimeException(ex);
	    }

	    // Cache the LayoutDefinition
	    synchronized (layouts) {
		layouts.put(key, ld);
	    }
	}
	return ld;
    }


    /**
     *	This returns the LDM's entity resolver, null if not set.
     *
     *	@return EntityResolver
     */
    public EntityResolver getEntityResolver() {
	return (EntityResolver) getAttribute(ENTITY_RESOLVER);
    }


    /**
     *	This method sets the LDM's entity resolver.
     *
     *	@param	entityResolver	The EntityResolver to use.
     */
    public void setEntityResolver(EntityResolver entityResolver) {
	setAttribute(ENTITY_RESOLVER, entityResolver);
    }


    /**
     *	This returns the LDM's XML parser ErrorHandler, null if not set.
     *
     *	@return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {
	return (ErrorHandler) getAttribute(ERROR_HANDLER);
    }


    /**
     *	This method sets the LDM's ErrorHandler.
     *
     *	@param	errorHandler	The ErrorHandler to use.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
	setAttribute(ERROR_HANDLER, errorHandler);
    }


    /**
     *	This returns the LDM's XML parser baseURI which will be used to
     *	resolve relative URI's, null if not set.
     *
     *	@return The base URI as a String
     */
    public String getBaseURI() {
	return (String) getAttribute(BASE_URI);
    }


    /**
     *	This method sets the LDM's BaseURI.
     *
     *	@param	baseURI		The BaseURI to use.
     */
    public void setBaseURI(String baseURI) {
	setAttribute(BASE_URI, baseURI);
    }



    /**
     *	This class handles XML parser errors.
     */
    private static class XMLErrorHandler implements ErrorHandler {
	/** Error handler output goes here */
	private PrintWriter out;

	XMLErrorHandler(PrintWriter outWriter) {
	    this.out = outWriter;
	}

	/**
	 * Returns a string describing parse exception details
	 */
	private String getParseExceptionInfo(SAXParseException spe) {
	    String systemId = spe.getSystemId();
	    if (systemId == null) {
		systemId = "null";
	    }
	    String info = "URI=" + systemId + " Line=" + spe.getLineNumber()
		+ ": " + spe.getMessage();
	    return info;
	}

	// The following methods are standard SAX ErrorHandler methods.
	// See SAX documentation for more info.

	public void warning(SAXParseException spe) throws SAXException {
	    out.println("Warning: " + getParseExceptionInfo(spe));
	}

	public void error(SAXParseException spe) throws SAXException {
	    String message = "Error: " + getParseExceptionInfo(spe);
	    throw new SAXException(message, spe);
	}

	public void fatalError(SAXParseException spe) throws SAXException {
	    String message = "Fatal Error: " + getParseExceptionInfo(spe);
	    throw new SAXException(message, spe);
	}
    }

    /**
     *  <P> This entity reolver looks for xml &amp; dtd files that are
     *	    included as SYSTEM entities in the java class-path. If the file is
     *	    not found in the class path the resolver returns null, allowing
     *	    default mechanism to search for the file on the file system.</P>
     */
    public static class ClassLoaderEntityResolver implements EntityResolver {

	/**
	 *  Constructor.
	 */
	public ClassLoaderEntityResolver() {
	    super();
	}

	/**
	 *  <P>	This method attempts resolves the <code>systemId</code>.  The
	 *	systemId must end in <code>.dtd</code> or <code>.xml</code>
	 *	for this method to do anything.  If it does, it will attempt
	 *	to resolve the value via the classpath.  If it is unable to
	 *	locate it in the classpath, it will return null to single
	 *	default behavior (locate the file on the filesystem).</P>
	 *
	 *  <P>	The dtd in the LayoutDefinition XML file should be specified
	 *	as follows:</P>
	 *
	 *  <P><code>&lt;!DOCTYPE layoutDefinition SYSTEM
	 *	"/layout/layout.dtd"&gt;</code></P>
	 *
	 *  @param  publicId	Not used.
	 *  @param  systemId    The id to resolve.
	 *
	 *  @return The InputSource (null if it should use default behavior)
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
	    InputSource source = null;
	    if ((systemId != null) && (systemId.endsWith(".xml")
			|| systemId.endsWith(".dtd"))) {
                if (systemId.startsWith("file:")) {
                    int i = 5;
                    while (systemId.charAt(i) == '/') {
                        i++;
		    }
                    systemId = systemId.substring(i);
                }
		InputStream resourceStream =
		    getClass().getClassLoader().getResourceAsStream(systemId);
		if (resourceStream != null) {
		    source = new InputSource(resourceStream);
		}
	    }

	    // Return the InputSource (if null, it will use default behavior)
	    return source;
	}
    }


    /**
     *	Static map of LayoutDefinitionManagers.  Normally this will only
     *	contain the default LayoutManager.
     */
    private static Map layouts = new HashMap();


    /**
     *	This is used to ensure that only 1 instance of this class is created
     *	(per JVM).
     */
    private static LayoutDefinitionManager instance = null;

    /**
     *
     */
    private static final Class [] GET_REAL_PATH_ARGS =
	    new Class[] {String.class};

    /**
     *
     */
    private static final int FILE_PREFIX_LENGTH = "file:///".length();


    /**
     *	This is an attribute key which can be used to provide an
     *	EntityResolver to the XML parser.
     */
    public static final String ENTITY_RESOLVER	= "entityResolver";


    /**
     *
     */
    public static final String ERROR_HANDLER	= "errorHandler";


    /**
     *
     */
    public static final String BASE_URI		= "baseURI";

    public static boolean DEBUG = Boolean.getBoolean("com.sun.rave.web.ui.DEBUG");
}
