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

package org.netbeans.modules.uml.common;

import java.util.*;

/**
 * Resolves a message id to the locale dependant message.
 */
public class StringResolver
{

	/** Constructor. */
	public StringResolver(String bundleName)
	{
		iBundleName = bundleName;
	}

	/** Gets the locale dependent message for the default locale. */
	public final String get(String id)
	{
		return get(id, StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale.
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1)
	{
		return get(id, p1, StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, boolean p1)
	{
		return get(id, String.valueOf(p1), StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, short p1)
	{
		return get(id, String.valueOf(p1), StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, int p1)
	{
		return get(id, String.valueOf(p1), StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, long p1)
	{
		return get(id, String.valueOf(p1), StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, float p1)
	{
		return get(id, String.valueOf(p1), StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, double p1)
	{
		return get(id, String.valueOf(p1), StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1, Object p2)
	{
		return get(id, p1, p2, StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1, Object p2, Object p3)
	{
		return get(id, p1, p2, p3, StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the default locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1, Object p2, Object p3, Object p4)
	{
		return get(id, p1, p2, p3, p4, StringResolver.getLocale());
	}

	/** Gets the locale dependent message corresponding to the given id.
		 Replaces occurrences of %x with given parameters. */
	public final String get(String id, Object[] params)
	{
		return get(id, params, StringResolver.getLocale());
	}

	/** Gets the locale dependent message for the given locale. */
	public final String get(String id, Locale locale)
	{
		return get(id, null, locale);
	}

	/** Gets the locale dependent message for the given locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1, Locale locale)
	{
		return get(id, new Object[] { p1 }, locale);
	}

	/** Gets the locale dependent message for the given locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1, Object p2, Locale locale)
	{
		return get(id, new Object[] { p1, p2 }, locale);
	}

	/** Gets the locale dependent message for the given locale. 
		 The occurences of %x are replaced by the given parameters. */
	public final String get(String id, Object p1, Object p2, Object p3, Locale locale)
	{
		return get(id, new Object[] { p1, p2, p3 }, locale);
	}

	/** Gets the message from the given id for the given locale. */
	public final String get(String id, Object p1, Object p2, Object p3, Object p4, Locale locale)
	{
		return get(id, new Object[] { p1, p2, p3, p4 }, locale);
	}

	/** Gets the locale dependent message corresponding to the given id.
		 Replaces occurrences of %x with given parameters. */
	public final String get(String id, Object[] params, Locale locale)
	{
		// Get the message bundle
		ResourceBundle messages = (ResourceBundle) iBundles.get(locale);

		if (messages == null)
		{ // load the bundle from the system 
			try
			{
				messages = ResourceBundle.getBundle(iBundleName, locale);
				if (messages == null)
					return id;
				iBundles.put(locale, messages);
			} catch (Throwable exc)
			{
				return "Error loading: " + iBundleName + " resource bundle for locale: " + locale + " " + exc;
			}
		}

		// Get the message
		String m = null;
		try
		{
			m = messages.getString(id);
			if (m == null)
				return id;
		} catch (MissingResourceException exc)
		{
			return ETStrings.E_CMN_UNKNOWN_MSGID.get(id, iBundleName);
		}

		// replaced occurrences of %x with the appropriate parameter
		if (params == null)
			return m;
		int pos = m.indexOf('%');
		if (pos == -1)
			return m;
		StringBuffer buffer = new StringBuffer(100);
		int index = 0;
		int endPos = m.length() - 1;

		while (pos != -1 && pos < endPos)
		{
			buffer.append(m.substring(index, pos));
			index = pos;

			// check to make sure a number follows
			int numBegin = pos + 1;
			int numEnd = pos + 2;
			int skip = 2;

			if (Character.isDigit(m.charAt(numBegin)))
			{

				if (numEnd <= endPos)
				{ // make sure we don't try to go beyond the string...
					// Check for another number
					if (Character.isDigit(m.charAt(numEnd)))
					{
						numEnd++;
						skip++;
					}
				}

				int x = Integer.parseInt(m.substring(numBegin, numEnd));
				x--; // array indices start at 0, not 1

				if (params.length > x)
				{
					Object tmp = params[x];
					if (tmp != null)
					{
						if (tmp instanceof Object[])
						{
							// Special treatment for Object array, show all elements in the array
							Object oa[] = (Object[]) tmp;
							buffer.append('(');
							for (int i = 0; i < oa.length; i++)
							{
								if (i > 0)
									buffer.append(',');
								buffer.append(oa[i]);
							}
							buffer.append(')');
						} else
						{
							buffer.append(tmp.toString());
						}
					}
					index += skip; // skip past %x
				}
			}

			// find the next %
			pos = m.indexOf('%', pos + 1);
		}

		buffer.append(m.substring(index));
		return buffer.toString();
	}

	public final static Locale getLocale()
	{
		return StringResolver.cLocale;
	}

	private Map iBundles = new HashMap();
	private String iBundleName;
	private static Locale cLocale = Locale.US;

}
