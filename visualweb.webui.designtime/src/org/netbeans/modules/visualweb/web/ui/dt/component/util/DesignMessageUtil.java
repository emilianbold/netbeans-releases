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
package org.netbeans.modules.visualweb.web.ui.dt.component.util;

import com.sun.rave.web.ui.util.MessageUtil;
import java.util.Locale;
import javax.faces.context.FacesContext;

/**
 * Provides access to design-time resources with a Bundle-DT baseName.
 *
 * @author Edwin Goei
 */
public class DesignMessageUtil {

    /**
     * Get a message from a design-time resource bundle.
     *
     * @param clazz
     *            class determines package where resources are located
     * @param key
     *            The key for the desired string.
     * @return localized String
     */
    public static String getMessage(Class clazz, String key) {
        return getMessage(clazz, key, null);
    }

    /**
     * Get a formatted message from a design-time resource bundle.
     *
     * @param clazz
     *            class determines package where resources are located
     * @param key
     *            The key for the desired string.
     * @param args
     *            The arguments to be inserted into the string.
     * @return localized String
     */
    public static String getMessage(Class clazz, String key, Object args[]) {
        String baseName = clazz.getPackage().getName() + ".Bundle-DT";

        // XXX webui-designtime is module jar now, and current (wrong) impl
        // of project classloader doesn't know about it.
        // TODO This arch (missing arch) needs to be revised together with
        // new impl of project classloaders.
//        return MessageUtil.getMessage(baseName, key, args);
        return MessageUtil.getMessage(getLocale(),
                baseName, key, args, DesignMessageUtil.class.getClassLoader());
    }

    // XXX Copy from webui/runtime/library MessageUtils.
    // TODO Avoid copy/paste antipattern.
    private static Locale getLocale() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
	    return Locale.getDefault();
	}

        Locale locale = null;

        // context.getViewRoot() may not have been initialized at this point.
        if (context.getViewRoot() != null)
            locale = context.getViewRoot().getLocale();

        return (locale != null) ? locale : Locale.getDefault();
    }
}
