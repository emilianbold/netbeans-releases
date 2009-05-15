/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.util;

import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author mt154047
 */
public final class MangledNameType {//implements Comparable<MangledNameType> {

    
    private static final CppSymbolDemangler demanglingService;


    static {
        CppSymbolDemanglerFactory factory = Lookup.getDefault().lookup(CppSymbolDemanglerFactory.class);
        if (factory != null) {
            demanglingService = factory.getForCurrentSession();
        } else {
            demanglingService = null;
        }
    }
    private final String mangled_name;
    private String demanled_name = null;

    /**
     * Creates new instance.
     * @param mangled_name   mangled function name
     */
    public MangledNameType(String mangled_name) {
        this.mangled_name = mangled_name;
    }

    /**
     * @return mangled name
     */
    public String getMangledName() {
        return mangled_name;
    }

    public String demangle() {
        if (demanled_name != null) {
            return demanled_name;
        }
        demangleName();
        return demanled_name;

    }

    private void demangleName() {
        if (demanglingService == null) {
            demanled_name = mangled_name;
            return;
        }
        demanled_name = demanglingService.demangle(mangled_name);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(mangled_name);
        return buf.toString();
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof MangledNameType) {
//            String nameWithoutOffset = mangled_name.indexOf("+") == -1 ? mangled_name : mangled_name.substring(0, mangled_name.indexOf("+"));
//            MangledNameType that = (MangledNameType) obj;
//            String thatNameWithoutOffset = that.mangled_name.indexOf("+") == -1 ? that.mangled_name : that.mangled_name.substring(0, mangled_name.indexOf("+"));
//            return nameWithoutOffset.equals(thatNameWithoutOffset);
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public int hashCode() {
//        String nameWithoutOffset = mangled_name.indexOf("+") == -1 ? mangled_name : mangled_name.substring(0, mangled_name.indexOf("+"));
//        return nameWithoutOffset.hashCode();
//    }

//    public int compareTo(MangledNameType that) {
//        String nameWithoutOffset = mangled_name.indexOf("+") == -1 ? mangled_name : mangled_name.substring(0, mangled_name.indexOf("+"));
//        String thatNameWithoutOffset = that.mangled_name.indexOf("+") == -1 ? that.mangled_name : that.mangled_name.substring(0, mangled_name.indexOf("+"));
//        return nameWithoutOffset.compareTo(thatNameWithoutOffset);
//
//
//    }


    static {
        PropertyEditorManager.registerEditor(MangledNameType.class, MangledNameTypeEditor.class);
    }
}
