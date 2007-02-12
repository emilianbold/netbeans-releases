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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;


public class StoreFileFilter extends FileFilter {
    
    public static final String JKS_EXT = "jks";        //NOI18N
    public static final String PKCS12_EXT = "p12";     //NOI18N

    public StoreFileFilter() { }

    @Override
    public boolean accept(File f) {
        if ((f != null) && f.exists() && (f.getName() != null) && 
                ((f.getName().contains(JKS_EXT)  || f.getName().contains(PKCS12_EXT)) || (f.isDirectory()))) {
            return true;
        }
        return false;
    }
    @Override
    public String getDescription() {
        return NbBundle.getMessage(StoreFileFilter.class, "STORE_FILES");  //NOI18N
    }
}