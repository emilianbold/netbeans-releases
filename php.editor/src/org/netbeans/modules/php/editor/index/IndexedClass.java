/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.index;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.ElementKind;


/**
 *
 * @author tomslot
 */
public class IndexedClass extends IndexedType {
    private String superClass;
    private final List<String> ifaces;

    public IndexedClass(String name, String in, PHPIndex index, String fileUrl,
            String superClass, List<String> ifaces, int offset,  int flags){
        super(name, in, index, fileUrl, offset, flags, ElementKind.CLASS);
        // empty string causes a serious performance problem
        if (superClass != null && superClass.length() == 0){
            throw new IllegalArgumentException("superClass cannot be empty string!");
        }
        this.superClass = superClass;
        this.ifaces = new ArrayList<String>(ifaces);
    }

    @CheckForNull
    public String getSuperClass(){
        return superClass;
    }

    public List<String> getInterfaces(){
        return ifaces;
    }

    public String getNamespaceName() {
        final String retval = getIn();
        return retval != null ? retval : "";//NOI18N
    }
}
