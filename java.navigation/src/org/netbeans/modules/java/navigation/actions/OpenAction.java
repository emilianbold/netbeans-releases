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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * OpenAction.java
 *
 * Created on September 24, 2004, 8:41 PM
 */

package org.netbeans.modules.java.navigation.actions;

import java.awt.Toolkit;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.*;

import javax.swing.*;
import java.awt.event.*;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * An action that opens editor and jumps to the element given in constructor.
 * Similar to editor's go to declaration action.
 *
 * @author tim, Dafe Simonek
 */
public final class OpenAction extends AbstractAction {
    
    private ElementHandle<? extends Element> elementHandle;   
    private FileObject fileObject;
    private String displayName;

    public OpenAction( ElementHandle<? extends Element> elementHandle, FileObject fileObject, String displayName ) {
        this.elementHandle = elementHandle;
        this.fileObject = fileObject;
        this.displayName = displayName;
        putValue ( Action.NAME, NbBundle.getMessage ( OpenAction.class, "LBL_Goto" ) ); //NOI18N
    }
    
    public void actionPerformed (ActionEvent ev) {
        if( null == fileObject ) {
            Toolkit.getDefaultToolkit().beep();
            if( null != displayName ) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(OpenAction.class, "MSG_NoSource", displayName) );  //NOI18N
            }
        } else {
            FileObject file = fileObject;
            if (isClassFile(file)) {
                final FileObject src = findSource(file, elementHandle);
                if (src != null) {
                    file = src;
                }
            }
            ElementOpen.open(file, elementHandle);
        }
    }

    public boolean isEnabled () {
          return true;
    }

    private static boolean isClassFile(@NonNull final FileObject file) {
        return "application/x-class-file".equals(file.getMIMEType("application/x-class-file")) || "class".equals(file.getExt());  //NOI18N
    }

    @CheckForNull
    private static FileObject findSource(
            @NonNull final FileObject file,
            @NonNull final ElementHandle<?> elementHandle) {
        FileObject owner = null;
        for (String id : new String[] {
                ClassPath.EXECUTE,
                ClassPath.COMPILE,
                ClassPath.BOOT}) {
            final ClassPath cp = ClassPath.getClassPath(file, id);
            if (cp != null) {
                owner = cp.findOwnerRoot(file);
                if (owner != null) {
                    break;
                }
            }
        }
        return owner == null ?
            owner :
            SourceUtils.getFile(
                elementHandle,
                ClasspathInfo.create(
                    ClassPathSupport.createClassPath(owner),
                    ClassPath.EMPTY,
                    ClassPath.EMPTY));
    }
}
