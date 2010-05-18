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
        //FacesContext context = FacesContext.getCurrentInstance();
        //if (context == null) {
	    //return Locale.getDefault();
	//}

        //Locale locale = null;

        // context.getViewRoot() may not have been initialized at this point.
        //if (context.getViewRoot() != null)
            //locale = context.getViewRoot().getLocale();

        //return (locale != null) ? locale : Locale.getDefault();
        
        // Do not depend on FacesContext to get the locale. Here is the problem 
        // When Design time container initializes it creates the RI ConfigListener, 
        // which in turn instantiates all the Renderers in the Render Kit 
        // Some design time renderer calls this class to loaded messages
        // This causes problem because Faces Context is not yet initialized and throws 
        // Exception. Since this is a design time only renderer, I'm trying to fix the problem 
        // using just Locale.getDefault() - Winston
        return Locale.getDefault();
    }
}
