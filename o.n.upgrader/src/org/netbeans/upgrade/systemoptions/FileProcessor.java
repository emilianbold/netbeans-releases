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

package org.netbeans.upgrade.systemoptions;

import java.util.Iterator;
import java.util.List;

/**
 * @author Radek Matous
 */
class FileProcessor extends PropertyProcessor {
    FileProcessor() {
        super("java.io.File");//NOI18N
    }

    void processPropertyImpl(String propertyName, Object value) {
        StringBuffer sb = new StringBuffer();
        if ("antHome".equals(propertyName) || "projectsFolder".equals(propertyName)) {//NOI18N
            List l = ((SerParser.ObjectWrapper)value).data;
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object elem = (Object) it.next();
                if (elem instanceof SerParser.NameValue) {
                    SerParser.NameValue nv = (SerParser.NameValue)elem;
                    if (nv.value != null && nv.name != null) {
                        if (nv.name.name.equals("path")) {//NOI18N
                            addProperty(propertyName,nv.value.toString());//NOI18N
                        }
                    }
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
}
