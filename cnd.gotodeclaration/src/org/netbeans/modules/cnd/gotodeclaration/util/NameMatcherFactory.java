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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.util;

import java.util.regex.Pattern;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 * A factory that provides comparators 
 * depending on SearchType
 * 
 * @author Vladimir Kvashin
 */
public class NameMatcherFactory {

    private NameMatcherFactory() {
    }
    
    public static abstract class BaseNameMatcher implements NameMatcher {
	
        protected String patternText;
	
	protected BaseNameMatcher(String patternText) {
	    this.patternText = patternText;
	}
    }
    
    
    public static final class ExactNameMatcher extends BaseNameMatcher implements NameMatcher {
	
	public ExactNameMatcher(String patternText) {
	    super(patternText);
	}

	public final boolean matches(String name) {
	    return patternText.equals(name);
	}
    }

    public static final class PrefixNameMatcher extends BaseNameMatcher implements NameMatcher {
	
	public PrefixNameMatcher(String patternText) {
	    super(patternText);
	}
	
	public final boolean matches(String name) {
	    return name != null && name.startsWith(patternText);
	}
    }

    public static final class CaseInsensitivePrefixNameMatcher extends BaseNameMatcher implements NameMatcher {
	
	public CaseInsensitivePrefixNameMatcher(String patternText) {
	    super(patternText.toLowerCase());
	}
	
	public final boolean matches(String name) {
	    return name != null && name.toLowerCase().startsWith(patternText);
	}
    }

    public static final class RegExpNameMatcher implements NameMatcher {

	Pattern pattern;
	
	public RegExpNameMatcher(String patternText, boolean caseSensitive) {
	    pattern = Pattern.compile(patternText, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
	}
	
	public final boolean matches(String name) {
	    return name != null && pattern.matcher(name).matches();
	}
    }

    private static final class CamelCaseNameMatcher implements NameMatcher {

	Pattern pattern;
	
	public CamelCaseNameMatcher(String name) {
            if (name.length() == 0) {
                throw new IllegalArgumentException ();
            }        
            final StringBuilder patternString = new StringBuilder ();                        
            for (int i=0; i<name.length(); i++) {
                char c = name.charAt(i);
                patternString.append(c);
                if (i == name.length()-1) {
                    patternString.append("\\w*");  // NOI18N
                }
                else {
                    patternString.append("[\\p{Lower}\\p{Digit}]*");  // NOI18N
                }
            }
            pattern = Pattern.compile(patternString.toString());
	}
	
	public final boolean matches(String name) {
	    return name != null && pattern.matcher(name).matches();
	}
    }

    /**
     * Get a name
     */
    public static NameMatcher createNameMatcher(String text, SearchType type) {
	switch( type ) {
            case EXACT_NAME:
		return new ExactNameMatcher(text);
            case PREFIX:
		return new PrefixNameMatcher(text);
            case REGEXP:
		return new RegExpNameMatcher(text, true);
            case CASE_INSENSITIVE_REGEXP:
		return new RegExpNameMatcher(text, false);
            case CASE_INSENSITIVE_PREFIX:
                 return new CaseInsensitivePrefixNameMatcher(text);
            case CAMEL_CASE:
		return new CamelCaseNameMatcher(text);
            default:
                return null;
	}
    }    
}
