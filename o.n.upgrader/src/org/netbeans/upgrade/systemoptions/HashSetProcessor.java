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
package org.netbeans.upgrade.systemoptions;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Tomas Stupka
 */
class HashSetProcessor extends PropertyProcessor {

    static final String CVS_PERSISTENT_HASHSET = "org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig.PersistentHashSet";  // NOI18N
    static final String SVN_PERSISTENT_HASHSET = "org.netbeans.modules.subversion.settings.SvnModuleConfig.PersistentHashSet";              // NOI18N
    
    HashSetProcessor(String className) {
        super(className);
    }
    
    void processPropertyImpl(String propertyName, Object value) {
        if ("commitExclusions".equals(propertyName)) { // NOI18N
            List l = ((SerParser.ObjectWrapper) value).data;
            int c = 0;
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object elem = it.next();
                if(elem instanceof String) {
                    addProperty(propertyName + "." + c, (String) elem);
                    c = c + 1;
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }    
}
