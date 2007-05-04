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
 *
 */
package org.netbeans.installer.utils.helper;

import java.io.File;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;

/**
 * Class is depricated since NetBeans M9.<br>
 * The new style is to use shortcut implementations in shortcut package : 
 * <ul>
 *      <li><code>FileShortcut</code></li>  
 *      <li><code>InternetShortcut</code></li>
 * </ul>
 */ 
@Deprecated
public class Shortcut extends FileShortcut {
    public Shortcut(String name, File file) {
        super(name, file);
    }    
}