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

package org.netbeans.modules.cnd.apt.support;

import java.io.IOException;
import org.netbeans.modules.cnd.apt.impl.support.APTDriverImpl;
import org.netbeans.modules.cnd.apt.structure.APTFile;

/**
 * Thread safe driver to obtain APT for the file.
 * Wait till APT for file will be created.
 * @author Vladimir Voskresensky
 */
public final class APTDriver {
    private static final APTDriver singleton = new APTDriver();
    
    /** Creates a new instance of APTCreator */
    private APTDriver() {
    }
    
    public static APTDriver getInstance() {
        return singleton;
    }

    public APTFile findAPTLight(APTFileBuffer buffer) throws IOException {
        return APTDriverImpl.findAPT(buffer, false);
    }
    
    public APTFile findAPT(APTFileBuffer buffer) throws IOException {
        return APTDriverImpl.findAPT(buffer, true);
    }
    
    public void invalidateAPT(APTFileBuffer buffer) {
        APTDriverImpl.invalidateAPT(buffer);
    }
    
    public void invalidateAll() {
        APTDriverImpl.invalidateAll();
    }
}
