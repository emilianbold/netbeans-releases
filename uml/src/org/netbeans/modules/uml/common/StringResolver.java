/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
