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
 * MappingContextFactory.java
 *
 * Created on April 5, 2002, 3:15 PM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.core.util;

import java.util.Map;
import java.util.HashMap;

import org.openide.filesystems.FileObject;
import com.sun.jdo.api.persistence.model.Model;
import org.netbeans.lib.j2ee.sun.persistence.utility.openide.DevelopmentModel;

/** 
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class MappingContextFactory
{
	// this should not be a weak map because the keys are internal and 
	// will never be referenced externally
	private static final Map _contextCache = new HashMap();

        // TODO - consider making this private or removing after Util in 
        // mapping/ejb no longer uses it
	public static Model getDefaultModel ()
	{
		return DevelopmentModel.getModel((FileObject)null); }

	public static MappingContext getDefault ()
	{
		return getMappingContext(getDefaultModel());
	}

	public static MappingContext getMappingContext (Model model)
	{
		return getMappingContext(model, null,
			MappingContextFactory.class.getClassLoader());
	}

	public static MappingContext getMappingContext (Model model,
		String brandingSuffix, ClassLoader classLoader)
	{
		return getMappingContext(model, brandingSuffix, true, classLoader);
	}

	public static synchronized MappingContext getMappingContext (Model model,
		String brandingSuffix, boolean isJDOExposed, ClassLoader classLoader)
	{
		ComplexKey key = new ComplexKey(model, brandingSuffix, isJDOExposed);
		MappingContext value = (MappingContext)_contextCache.get(key);

		if (value == null)
		{
			value = new MappingContext(model, 
				brandingSuffix, isJDOExposed, classLoader);
			_contextCache.put(key, value);
		}

		return value;
	}

	private static final class ComplexKey
	{
		private final Model _model;
		private final String _suffix;
		private final boolean _isJDOExposed;

		private ComplexKey (Model model, String suffix, boolean isJDOExposed)
		{
			_model = model;
			_suffix = suffix;
			_isJDOExposed = isJDOExposed;
		}

		public boolean equals (Object obj)
		{
			if ((obj != null) && getClass().equals(obj.getClass()))
			{
				ComplexKey o = (ComplexKey)obj;

				if (_model.equals(o._model) && 
					(_isJDOExposed == o._isJDOExposed))
				{
					if (((_suffix == null) && (o._suffix == null)) || 
						_suffix.equals(o._suffix))
					{
						return true;
					}
				}
			}

			return false;
		}

		public int hashCode ()
		{
			int hashCode = 0;

			hashCode += ((_model != null) ? _model.hashCode() : 0);
			hashCode += ((_suffix != null) ? _suffix.hashCode() : 0);
			hashCode += (_isJDOExposed ? 1 : 0);

			return hashCode;
		}
	}
}
