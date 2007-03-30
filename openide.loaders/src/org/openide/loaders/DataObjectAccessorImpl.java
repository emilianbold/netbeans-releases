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


package org.openide.loaders;

import java.io.IOException;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.openide.nodes.CookieSet;

/**
 * API trampoline to access package private methods in DataObject class.
 * @since 6.3
 * @author S. Aubrecht
 */
final class DataObjectAccessorImpl extends DataObjectAccessor {
    
    public DataObject copyRename( DataObject dob, DataFolder f, String name, String ext ) throws IOException {
        return dob.copyRename( f, name, ext );
    }
    
    public CookieSet getCookieSet(MultiDataObject dob) {
        return dob.getCookieSet();
    }
}
