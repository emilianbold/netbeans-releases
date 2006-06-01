/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.naming;


import java.io.File;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;

/**
 * @author Radek Matous
 */
public interface FileNaming {
    String getName();
    
    FileNaming getParent();
    
    boolean isRoot();
    
    File getFile();

    //not to touch disk by getFile().isFile()...
    boolean isFile();
    boolean isDirectory();
    
    Integer getId();
    
    Integer getId(boolean recompute);
    
    boolean rename(String name);
    boolean rename(String name, ProvidedExtensions.IOHandler handler);
    
}
