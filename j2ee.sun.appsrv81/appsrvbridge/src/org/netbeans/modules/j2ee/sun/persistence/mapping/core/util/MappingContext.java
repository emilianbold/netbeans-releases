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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


/*
 * MappingContext.java
 *
 * Created on January 25, 2002, 3:15 PM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.core.util;

import java.util.*;
/* no UI support in this version - help jar removed
import javax.help.HelpSet;
 */
import java.text.MessageFormat;

/* no UI support in this version - help jar removed
import org.openide.util.HelpCtx;
 */
import org.openide.util.NbBundle;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.spi.persistence.utility.MergedBundle;
import com.sun.jdo.spi.persistence.utility.logging.Logger;
import com.sun.jdo.spi.persistence.utility.logging.LogHelper;
/* no UI support in this version - help jar removed
import org.netbeans.lib.j2ee.sun.persistence.utility.openide.HelpUtils;
*/

/** 
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class MappingContext
{
	/** The component name for this component in logging
	 */
	protected static final String _componentName = "mapping.module"; // NOI18N

	private static final String _baseName = 
		"org.netbeans.modules.j2ee.sun.persistence.mapping.core.resources.Bundle"; // NOI18N

        /* no UI support in this version - help jar removed
	private static final String _baseHelp = 
		"Services/JavaHelp/com-sun-jdo-modules-persistence-mapping-core-helpset.xml"; // NOI18N

	// This is almost the same as the url in the helpset-decl.xml file
	// referenced by the xml layer, but there it specified with the 
	// nbresloc protocol which only works in the IDE.  Here it is specified as 
	// a standalone url which will be able to be used with HelpSet APIs 
	// directly to look up the help set outside the IDE.
	private static final String _baseHelpURL = 
		"com/sun/jdo/modules/persistence/docs/tp"; // NOI18N

         */
        
	/** Base HelpSet to be used as the parent for branding. */
        /* no UI support in this version - help jar removed
	private static HelpSet _parentHelp;
         */

	private final Model _model;
	private final String _brandingSuffix;
	private final boolean _isJDOExposed;
	private final ResourceBundle _bundle;
	private final ClassLoader _classLoader;

	protected MappingContext (Model model)
	{
		this(model, null, MappingContext.class.getClassLoader());
	}

	protected MappingContext (Model model, String brandingSuffix, 
		ClassLoader classLoader)
	{
		this(model, brandingSuffix, true, classLoader);
	}

	protected MappingContext (Model model, String brandingSuffix, 
		boolean isJDOExposed, ClassLoader classLoader)
	{
		_model = model;
		_brandingSuffix = brandingSuffix;
		_isJDOExposed = isJDOExposed;
		_classLoader = classLoader;
		_bundle = getBrandedBundle(_baseName);
	}


	public Model getModel () { return _model; }
	public String getBrandingSuffix () { return _brandingSuffix; }
	public boolean isJDOExposed () { return _isJDOExposed; }

	// ===================== i18n methods ===========================

	public ResourceBundle getBrandedBundle (String baseName)
	{
            // TODO - decide if this change (to use class loader) is 
            // okay - need it to work properly in the bridge
		//ResourceBundle bundle = NbBundle.getBundle(baseName);
		ResourceBundle bundle = NbBundle.getBundle(baseName, Locale.getDefault(), 
                    _classLoader);

		if (_brandingSuffix != null)
		{
			ResourceBundle extraBundle = null;

			try
			{
				extraBundle = NbBundle.getBundle(
					baseName + '_' + _brandingSuffix, Locale.getDefault(), 
					_classLoader);
			}
			catch (MissingResourceException e)
			{
				// extraBundle will be null -- log this
				// use bundle.getString instead of getString since _bundle
				// is not yet set
				String message = MessageFormat.format(bundle.getString(
					"mapping.module.context.extra_bundle_not_found"), // NOI18N
					new Object[] { _brandingSuffix });

				getLogger().log(Logger.WARNING, message, e);
			}

			if (extraBundle != null)
				bundle = new MergedBundle(extraBundle, bundle);
		}

		return bundle;
	}

	/** Computes the localized string for the key.
	 * @param key The key of the string.
	 * @return the localized string.
	 */
	public String getString (String key)
	{
		return _bundle.getString(key);
	}

	/** Get mnemonics from resource bundle
	 * @param key The key of the string.
	 */
	public char getMnemonic (String key)
	{
		return getString(key).trim().charAt(0);
	}

	/** Return the logger for the mapping module component
	 */
	public Logger getLogger ()
	{
		return LogHelper.getLogger(_componentName, _baseName, 
			MappingContext.class.getClassLoader());
	}

	// ===================== help id methods ===========================
        /* no UI support in this version - help jar removed
	public static synchronized HelpSet getHelpSet ()
	{
		if (_parentHelp == null)
			_parentHelp = HelpUtils.getHelpSet(_baseHelp, _baseHelpURL);

		return _parentHelp;
	}
        */

	/**
	 * Get context help id associated with an object.
	 * @param  key The object for which the help id is required.
	 * @return the help id.
	 */
        /* no UI support in this version - help jar removed
	public String getHelpID (Object key)
	{
		String keyString = 
			((key instanceof String) ? (String)key : key.getClass().getName());

		if (_brandingSuffix != null)
		{
			String brandedKey = keyString + '_' + _brandingSuffix;

			if (HelpUtils.isValidHelpID(brandedKey, getHelpSet()))
				return HelpUtils.getHelpID(brandedKey);
		}

		return HelpUtils.getHelpID(keyString);
	}
        */

	/**
	 * Get context help associated with an object.
	 * @param  obj The object for which the help context is required.
	 * @return the help context object.
	 */
        /* no UI support in this version - help jar removed
	public HelpCtx getHelpCtx (Object obj)
	{
		return HelpUtils.getHelpCtx(getHelpID(obj));
	}
        */
}
