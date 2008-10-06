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

package org.netbeans.modules.css.editor.properties;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author marekfukala
 */
public class Length extends NumberPostfixAcceptor {

    /*
    
    relative units:
    * em: the 'font-size' of the relevant font
    * ex: the 'x-height' of the relevant font
    * px: pixels, relative to the viewing device 
    
    absolute units:
     
    * in: inches -- 1 inch is equal to 2.54 centimeters.
    * cm: centimeters
    * mm: millimeters
    * pt: points -- the points used by CSS2 are equal to 1/72th of an inch.
    * pc: picas -- 1 pica is equal to 12 points. 
    */
    private static final List<String> POSTFIXES = Arrays.asList(new String[]{"px", "ex", "em", "in", "cm", "mm", "pt", "pc"}); //NOI18N
    
    public String id() {
        return "length"; //NOI18N
    }

    public List<String> postfixes() {
        return POSTFIXES;
    }
    
    @Override
    public boolean accepts(String text) {
        boolean sa = super.accepts(text);
        if(!sa) {
            return "0".equals(text); //NOI18N
        } else {
            return sa;
        }
    }
    
}
