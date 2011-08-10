/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * Utilities.java
 * Created on November 5, 2004, 5:08 PM
 */

package org.netbeans.modules.css.visual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JFileChooser;

/**
 * Some useful utilities
 * @author  Winston Prakash
 * @version 1.0
 */
public class Utilities {
    
    public static final String VISUAL_EDITOR_LOGGER = "css.visual";
    
    /**
     * This does a special instantiation of JFileChooser
     * to workaround floppy access bug 5037322.
     * Using privileged code block.
     */
    public static JFileChooser getJFileChooser() {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return new JFileChooser() ;
            }
        });
    }
    
    public static JFileChooser getJFileChooser(final String currentDirectoryPath) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return new JFileChooser(currentDirectoryPath);
            }
        });
    }
    
    public static JFileChooser getJFileChooser(final java.io.File currentDirectory) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return new JFileChooser(currentDirectory) ;
            }
        });
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
