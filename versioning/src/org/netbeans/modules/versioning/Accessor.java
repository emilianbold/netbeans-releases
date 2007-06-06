/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;

import java.io.File;
import java.util.*;

/**
 * Make it possible to hide contructors and factory methods in VCSContext.
 * 
 * @author Maros Sandor
 */
public abstract class Accessor {
    
    public static Accessor VCSContextAccessor;
    
    static {
        // invokes static initializer of VCSContext.class
        // that will assign value to the DEFAULT field above
        Class c = VCSContext.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public abstract VCSContext createContextForFiles(Set<File> files);
}
