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
package org.netbeans.modules.sql.project.anttasks;

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;


public class SQLProLookup extends Lookup {
    static Lookup mylookup = null;
    static SQLProLookup m_instance = null;
    
    public SQLProLookup() {
        
    }
    private static Lookup getInstance(){
        if (m_instance == null ) {
            m_instance = new SQLProLookup();
        }
        if (mylookup == null) {
            mylookup = Lookups.metaInfServices(m_instance.getClass().getClassLoader());
        }
        return mylookup;
    }
    
    public Object lookup(Class clazz) {
        return getInstance().lookup(clazz);
    }
    
    public  Lookup.Result lookup(Lookup.Template template) {
        return getInstance().lookup(template);
    }
    
    public Lookup.Item  lookupItem(Lookup.Template template) {
        return getInstance().lookupItem(template);
    }
    void setLookups(Object[] instances) {
        ClassLoader l = SQLProLookup.class.getClassLoader();
        setLookups(new Lookup[] {
            Lookups.metaInfServices(l),
            Lookups.singleton(l),
            Lookups.fixed(instances),
        });
    }
}
