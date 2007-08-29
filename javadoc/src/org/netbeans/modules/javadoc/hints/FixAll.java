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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.javadoc.hints;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class FixAll implements Fix {
        
        private List<GenerateJavadocFix> allJavadocFixes = new ArrayList<GenerateJavadocFix>();
        
        public void addFix(GenerateJavadocFix f) {
            allJavadocFixes.add(f);
        }
        
        public boolean isReady() {
            return allJavadocFixes.size() > 1;
        }
        
        public String getText() {
            return NbBundle.getMessage(FixAll.class, "FIX_ALL_HINT"); // NOI18N
        }
        
        public ChangeInfo implement() {
            for (GenerateJavadocFix javadocFix : allJavadocFixes) {
                javadocFix.implement(false);
            }
            
            return null;
        }
        
}
