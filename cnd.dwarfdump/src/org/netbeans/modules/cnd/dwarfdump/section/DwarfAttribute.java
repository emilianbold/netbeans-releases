/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * DwardAttribute.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.FORM;
import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class DwarfAttribute {
    public final ATTR attrName;
    public final FORM valueForm;

    public DwarfAttribute(int nameOrdinal, int formOrdinal) {
        this.attrName = ATTR.get(nameOrdinal);
        this.valueForm = FORM.get(formOrdinal);
    }
    
    public void dump(PrintStream out, Object value) {
        out.print("\t" + attrName + " [" + valueForm + "]"); // NOI18N
        
        if (value != null) {
            if (valueForm.equals(FORM.DW_FORM_ref4)) {
                out.printf(" <%x>", value); // NOI18N
            } else if (valueForm.equals(FORM.DW_FORM_block1)) {
                byte[] data = (byte[])value;
                out.printf(" %d bytes: ", data.length); // NOI18N
                for (int i = 0; i < data.length; i++) {
                    out.printf(" 0x%x", data[i]); // NOI18N
                }
            } else {
                out.printf(" %s", value.toString()); // NOI18N
            }
            
            out.printf("\n"); // NOI18N
        } else {
            out.println(""); // NOI18N
        }
    }
    
    public void dump(PrintStream out) {
        dump(out, null);
    }
    
    public void dump() {
        dump(System.out, null);
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(Object value) {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out, value);
        return st.toString();
    }
}

