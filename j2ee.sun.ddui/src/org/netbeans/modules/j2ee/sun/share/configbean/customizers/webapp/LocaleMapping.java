/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * LocaleMapping.java
 *
 * Created on December 10, 2003, 3:22 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.StringTokenizer;

import java.text.MessageFormat;

/** Object for nice usage of Locales in comboboxes, sorted lists, etc.
 *  Provides same equality properties as Locale (but with LocaleMapping)
 *  but a better "display name" via toString().
 *
 *  There are also several static utility methods for finding and creating
 *  Locales and LocaleMappings.
 *
 * @author Peter Williams
 */
public class LocaleMapping implements Comparable {
	
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private Locale locale;
	private String displayText;
	private boolean textOutOfDate;

	/** Create a mapping for a locale
	 *
	 * @param l The locale for this mapping
	 */
	public LocaleMapping(final Locale l) {
		locale = l;
		displayText = buildDisplayText();
	}

	/** equals() maps to Locale.equals()
	 *
	 * @return true/false based on whether the embedded locale objects compare
	 *  as equal.
	 */
	public boolean equals(Object o) {
		boolean result = false;

		if(o instanceof LocaleMapping) {
			LocaleMapping lm = (LocaleMapping) o;
			result = locale.equals(lm.getLocale());
		}

		return result;
	}

	/** hashCode() maps to Locale.hashCode()
	 *
	 * @return the hashcode
	 */
	public int hashCode() {
		return locale.hashCode();
	}

	/** A more readable display string
	 *
	 * @return A descriptive string
	 */
	public String toString() {
		if(textOutOfDate) {
			displayText = buildDisplayText();
		}

		return displayText;
	}

	/** The locale
	 *
	 * @return the locale this is a mapping for
	 */
	public Locale getLocale() {
		return locale;
	}

	/** Force the display text to be recalculated.  Recalculation won't happen
	 *  until next time text is requested.
	 */
	public void updateDisplayText() {
		textOutOfDate = true;
	}

	private String buildDisplayText() {
		Object [] args = new Object [] { locale.toString(), locale.getDisplayName() };
		String result = MessageFormat.format(
			webappBundle.getString("LBL_LocaleComboBoxDisplayText"), args);	// NOI18N

		if(result == null || result.length() == 0) {
			result = webappBundle.getString("LBL_UnnamedLocale");	// NOI18N
		}

		textOutOfDate = false;

		return result;
	}

	/** For sorted collections.  We compare the string representations of the 
	 *  embedded locale.
	 *
	 * @param obj the LocaleMapping to compare to
	 * @return result of comparison (negative, 0, or positive depending on match)
	 */
	public int compareTo(Object obj) {
		int result = -1;

		if(obj instanceof LocaleMapping) {
			LocaleMapping targetMapping = (LocaleMapping) obj;
			result = locale.toString().compareTo(targetMapping.getLocale().toString());
		}

		return result;
	}

	private static SortedMap sortedLocaleMappings = getSortedAvailableLocaleMappings();

	/** Return a sorted map containg mappings for all locales supported by the 
	 *  current JVM.
	 *
	 * @return SortedMap containing LocaleMapping objects
	 */
	public static SortedMap getSortedAvailableLocaleMappings() {
		if(sortedLocaleMappings == null) {
			Locale [] isoInstalledLocales = Locale.getAvailableLocales();

			sortedLocaleMappings = new TreeMap(new LocaleComparator());
			for(int i = 0; i < isoInstalledLocales.length; i++) {
				sortedLocaleMappings.put(isoInstalledLocales[i], new LocaleMapping(isoInstalledLocales[i]));
			}
		}

		return sortedLocaleMappings;
	}

	/** Retrieve the LocaleMapping object matching this locale.
	 *
	 * @param l Locale to search for.
	 * @return LocaleMapping matching the passed in locale.  Null if not found.
	 */
	public static LocaleMapping getLocaleMapping(Locale l) {
		return (LocaleMapping) sortedLocaleMappings.get(l);
	}

	/** Retrieve the LocaleMapping object matching the locale string
	 *
	 * @param ls String representing a locale in AA_AA_AA format (e.g. en_US)
	 * @return LocaleMapping matching the passed in locale.  Null if not found.
	 */
	public static LocaleMapping getLocaleMapping(String ls) {
		return (LocaleMapping) sortedLocaleMappings.get(getLocale(ls));
	}

	/** Construct a locale from a locale string.  We break up the string and use
	 *  the correct Locale constructor.
	 *
	 * @param localeSpec String representing a locale in AA_AA_AA format (e.g. en_US)
	 * @return A new Locale object representing the passed in locale or null if
	 *  string was blank or empty.
	 */
	public static Locale getLocale(String localeSpec) {
		Locale result = null;

		if(localeSpec != null) {
			// !PW Split locale string into it's component parts, as needed for 
			//     Locale constructor
			String [] parts = localeSpec.split("_", 3);	// NOI18N

			if(parts.length >= 1) { // LANGUAGE ONLY
				String language = parts[0];
				if(language == null) {
					language = "";	// NOI18N
				}

				if(parts.length >= 2) { // LANGUAGE, COUNTRY
					String country = parts[1];
					if(country == null) {
						country = "";	// NOI18N
					}

					if(parts.length >= 3) { // LANGUAGE, COUNTRY, VARIANT
						String variant = parts[2];
						if(variant == null) {
							variant = "";	// NOI18N
						}
						// three arguments
						result = new Locale(language, country, variant);
					} else {
						// two arguments
						result = new Locale(language, country);
					}
				} else {
					// one argument
					result = new Locale(language);
				}
			}
		}

		return result;
	}

	/** Comparator used to compare two LocaleMappings for equivalency.  Used by
	 *  the SortedMap of Locales maintained by LocaleMapping.
	 */
	public static class LocaleComparator implements Comparator {
		/** Compare's two object for equivalency.  In this case, both are expected
		 *  to be instances of LocaleMapping and will compare based on their
		 *  string representation (e.g. en_US).
		 *
		 * @param o1 First object to compare
		 * @param o2 Second object to compare
		 * @return negative, zero, or positive based on string representation.
		 */
		public int compare(Object o1, Object o2) {
			int result = -1;

			if(o1 instanceof Locale && o2 instanceof Locale) {
				Locale l1 = (Locale) o1;
				Locale l2 = (Locale) o2;

				result = l1.toString().compareTo(l2.toString());
			}

			return result;
		}
	}
}
