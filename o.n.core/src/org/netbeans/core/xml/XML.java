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

package org.netbeans.core.xml;

import java.util.*;
import org.openide.loaders.Environment;
import org.openide.xml.EntityCatalog;

/** Global utils for XML related stuff.
 *
 * @author  Jaroslav Tulach
 */
public final class XML extends Object {
    private static FileEntityResolver DEFAULT;

    /** Getter of the default Environment.Provider
     */
    public static Environment.Provider getEnvironmentProvider () {
        if (DEFAULT == null) {
            DEFAULT = new FileEntityResolver ();
        }

        return DEFAULT;
    }

    /** Getter of the EntityCatalog of the system.
     */
    public static EntityCatalog getEntityCatalog () {
        if (DEFAULT == null) {
            DEFAULT = new FileEntityResolver ();
        }
        
        return DEFAULT;
    }
    
}
