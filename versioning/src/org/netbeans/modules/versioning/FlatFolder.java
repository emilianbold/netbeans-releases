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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning;

import java.io.File;
import java.net.URI;

/**
 * Marker class, specifies that the folder is NOT recursive for actions that operate on it.
 * 
 * @author Maros Sandor
 */
public final class FlatFolder extends File {

    private static final long serialVersionUID = 1L;

    public FlatFolder(String pathname) {
        super(pathname);
    }

    public FlatFolder(URI uri) {
        super(uri);
    }

    public FlatFolder(File parent, String child) {
        super(parent, child);
    }

    public FlatFolder(String parent, String child) {
        super(parent, child);
    }
}
