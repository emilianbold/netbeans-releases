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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade.systemoptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author Radek Matous
 */
class ListProcessor extends PropertyProcessor {
    ListProcessor() {
        super("java.util.ArrayList");//NOI18N
    }

    void processPropertyImpl(String propertyName, Object value) {
        if ("openProjectsURLs".equals(propertyName) 
         || "recentProjectsURLs".equals(propertyName)
         || "recentTemplates".equals(propertyName)) {//NOI18N
            int s = 0;
            List l = ((SerParser.ObjectWrapper)value).data;
            for (Iterator it = l.iterator(); it.hasNext();) {
                String prop = null;
                Object elem = (Object) it.next();
                if (elem instanceof SerParser.ObjectWrapper) {
                    List list2 = ((SerParser.ObjectWrapper)elem).data;
                    try {
                        URL url = URLProcessor.createURL(list2);
                        prop = url.toExternalForm();
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                } else if (elem instanceof String) {
                    prop = (String)elem;
                }
                if (prop != null) {
                    addProperty(propertyName + "." + s, prop);
                    s = s + 1;
                }
            }
        }  
    }
}
