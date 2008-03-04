/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.filecreation;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ExtensionList;

/**
 * Unfortunately we can't change org.openide.loaders.ExtensionList way or
 * ignore case-sensitivity of OS.
 * 
 * @author Sergey Grinev
 */
public class CndExtensionList extends ExtensionList {
    
    private final List<String> extList = new ArrayList<String>();

    public CndExtensionList(String[] list) {
        for (int i = 0; i < list.length; i++) {
            this.extList.add(list[i]);
        }
    }
    
    @Override
    public void addExtension(String ext) {
        extList.add(ext);
    }
    
    @Override
    public void removeExtension(String ext) {
        extList.remove(ext);
        assert extList.size() > 0; // there is no reason one can want to remove all extensions and it should be checked on higher level
    }

    @Override
    public Enumeration<String> extensions() {
        return Collections.enumeration(extList);
    }
    
    @Override
    public boolean isRegistered (String s) {
        if (extList == null) {
            return false;
        }
      
        try {
            String ext = s.substring (s.lastIndexOf ('.') + 1);
            return extList.contains (ext);
        } catch (StringIndexOutOfBoundsException ex) {
            return false;
        }
    }

    @Override
    public boolean isRegistered (FileObject fo) {
        if (extList != null && extList.contains (fo.getExt ())) {
            return true;
        }

        for(Enumeration<String> mt = mimeTypes(); mt.hasMoreElements(); ) {
            if (mt.nextElement().equals(fo.getMIMEType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ExtensionList[" + extList + ", " + super.mimeTypes() + "]"; // NOI18N
    }

    @Override
    public int hashCode() {
        return super.hashCode() + extList.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CndExtensionList)) {
            return false;
        }
        CndExtensionList e = (CndExtensionList)o;
        return super.equals(o) && e.extList.equals(extList);
    }
    
}
