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
package com.sun.rave.web.ui.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


/**
 *  This class caches ResourceBundle objects per locale.
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ResourceBundleManager {

    /**
     *	Singleton
     */
    private static ResourceBundleManager _instance =
	    new ResourceBundleManager();

    /**
     *	The cache of ResourceBundles.
     */
    private Map	_cache = new HashMap();

    /**
     *	Use getInstance() to obtain an instance.
     */
    protected ResourceBundleManager() {
    }


    /**
     *	Use this method to get the instance of this class.
     */
    public static ResourceBundleManager getInstance() {
	if (_instance == null) {
	    _instance = new ResourceBundleManager();
	}
	return _instance;
    }

    /**
     *	This method checks the cache for the requested resource bundle.
     *
     *	@param	baseName    Name of the bundle
     *	@param	locale	    The locale
     *
     *	@return	The requested ResourceBundle in the most appropriate locale.
     */
    protected ResourceBundle getCachedBundle(String baseName, Locale locale) {
	return (ResourceBundle)_cache.get(getCacheKey(baseName, locale));
    }

    /**
     *	This method generates a unique key for setting / getting Resources
     *	bundles from the cache.  It is important to have different keys per
     *	locale (obviously).
     */
    protected String getCacheKey(String baseName, Locale locale) {
	return baseName+"__"+locale.toString();
    }

    /**
     *	This method adds a ResourceBundle to the cache.
     */
    protected void addCachedBundle(String baseName, Locale locale, ResourceBundle bundle) {
	// Copy the old Map to prevent changing a Map while someone is
	// accessing it.
	Map map = new HashMap(_cache);

	// Add the new bundle
	map.put(getCacheKey(baseName, locale), bundle);

	// Set this new Map as the shared cache Map
	_cache = map;
    }

    /**
     *	This method obtains the requested resource bundle as specified by the
     *	given basename and locale.
     */
    public ResourceBundle getBundle(String baseName, Locale locale) {
	ResourceBundle bundle = getCachedBundle(baseName, locale);
	if (bundle == null) {
	    bundle = ResourceBundle.getBundle(baseName, locale, 
                    ClassLoaderFinder.getCurrentLoader(MessageUtil.class));
	    if (bundle != null) {
		addCachedBundle(baseName, locale, bundle);
	    }
	}
	return bundle;
    }

    /**
     *	This method obtains the requested resource bundle as specified by the
     *	given basename, locale, and classloader.
     */
    public ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
	ResourceBundle bundle = getCachedBundle(baseName, locale);
	if (bundle == null) {
	    bundle = ResourceBundle.getBundle(baseName, locale, loader);
	    if (bundle != null) {
		addCachedBundle(baseName, locale, bundle);
	    }
	}
	return bundle;
    }
}
