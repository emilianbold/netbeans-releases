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
