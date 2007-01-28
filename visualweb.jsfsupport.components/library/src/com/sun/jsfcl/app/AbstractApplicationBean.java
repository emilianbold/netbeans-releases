/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.jsfcl.app;


import java.util.Locale;


/**
 * <p><strong>AbstractApplicationBean</strong> is the abstract base class for
 * data bean(s) that are stored in application scope attributes.  It extends
 * {@link FacesBean}, so it inherits all of the default behavior
 * found there.</p>
 */
public abstract class AbstractApplicationBean extends FacesBean {


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Create a new application scope bean.</p>
     */
    public AbstractApplicationBean() {
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Mapping from the String version of the <code>Locale</code> for
     * this response to the corresponding character encoding.  For each
     * row, the first String is the value returned by the toString() method
     * for the java.util.Locale for the current view, and the second
     * String is the name of the character encoding to be used.</p>
     *
     * <p>Only locales that use an encoding other than the default (UTF-8)
     * need to be listed here.</p>
     */
    protected String encoding[][] = {
	{ "zh_CN", "GB2312" }, // NOI18N
    };


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return an appropriate character encoding based on the
     * <code>Locale</code> defined for the current JavaServer Faces
     * view.  If no more suitable encoding can be found, return
     * "UTF-8" as a general purpose default.</p>
     *
     * <p>This method makes a convenient value binding target for the
     * <code>value</code> property of a <em>Set Encoding</em> component.
     * Applications that wish to specialize this behavior can override
     * this method in their own application bean class.</p>
     */
    public String getLocaleCharacterEncoding() {

	// Return the appropriate character encoding for this locale (if any)
	Locale locale = getFacesContext().getViewRoot().getLocale();
	if (locale == null) {
	    locale = Locale.getDefault();
	}
	String match = locale.toString();
	for (int i = 0; i < encoding.length; i++) {
	    if (match.equals(encoding[i][0])) {
		return encoding[i][1];
	    }
	}

	// Return the default encoding value
	return "UTF-8"; // NOI18N

    }


}
