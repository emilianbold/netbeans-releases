/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

/** Obsolete setting class
 *
 * @author  Jan Pokorsky
 */
public class ObsoleteClass implements java.io.Serializable {
    
    private static final long serialVersionUID = 3465637344523787865L;
    private String prop;
    
    public ObsoleteClass() {
    }
    public ObsoleteClass(String t) {
        prop = t;
    }
    
    private Object readResolve() throws ObjectStreamException {
        return new FooSetting(prop);
    }
    
}
