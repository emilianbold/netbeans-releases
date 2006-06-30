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

package org.netbeans.modules.j2ee.ddloaders.app;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.impl.application.ApplicationProxy;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author  mkuchtiak
 */
public class EarDDUtils {
     private EarDDUtils() {}
     
    /** Finds a name similar to requested that uniquely identifies 
     *  element between the other elements of the same name.
     *
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return a free element name
     */
    public static String findFreeName (CommonDDBean[] elements, String identifier, String name) {
        if (checkFreeName (elements, identifier, name)) {
            return name;
        }
        for (int i = 1;;i++) {
            String destName = name + "_"+i; // NOI18N
            if (checkFreeName (elements, identifier, destName)) {
                return destName;
            }
        }
    }
    
    /** Test if given name is free in given context.
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return true, if such name does not exists
     */
    private static boolean checkFreeName (CommonDDBean [] elements, String identifier, Object o) {
        for (int i=0; i<elements.length; i++) {
            Object val = elements[i].getValue (identifier);
            if (val != null && val.equals (o)) {
                return false;
            }
        }
        return true;
    }

    /**  Convenient method for getting the BaseBean object from CommonDDBean object
    */
    public static BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) return (BaseBean)bean;
        else if (bean instanceof ApplicationProxy) return (BaseBean) ((ApplicationProxy)bean).getOriginal();
        return null;
    }
    
    public static Application createApplication(InputSource is) throws IOException, SAXException {
        return DDProvider.getDefault().getDDRoot(is);
    }
}
