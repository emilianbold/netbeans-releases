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

package org.netbeans.modules.j2ee.persistence.dd;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.openide.util.NbBundle;

/**
 * Utility class that allows checking whether given string is 
 * <em>Java Persistence API QL keyword</em>
 * and thus cannot be used as a name for a persistent class or field.
 * @author Tomasz Slota
 */
public class JavaPersistenceQLKeywords {
    
    private static Set<String> keywords ;
    
    private JavaPersistenceQLKeywords() {}
    
    static{
        String rawKeywords = NbBundle.getBundle(JavaPersistenceQLKeywords.class).getString("JavaPersistenceQLKeywords");
        keywords = new TreeSet<String>(Arrays.asList(rawKeywords.split(","))); //NOI18N
    }
    
    public static boolean isKeyword(String string){
        if (string == null){
            throw new NullPointerException();
        }
        
        boolean result = keywords.contains(string.toUpperCase());
        return result;
    }
}
