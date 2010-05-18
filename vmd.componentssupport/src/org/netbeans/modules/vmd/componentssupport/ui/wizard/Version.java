/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 * 
 * MidpVersionDescriptor wrapper.
 * 
 * @author avk
 */
// TODO move string values to attributes
public enum Version {
    FOREVER,
    MIDP,
    MIDP_1,
    MIDP_2;

    // string values for displaying this version in e.g. list
    private static final String DISPLAY_FOREVER 
                                = getMessage("MIDP_VERSION_DISPLAY_forever");   // NOI18N
    private static final String DISPLAY_MIDP 
                                =getMessage("MIDP_VERSION_DISPLAY_all");        // NOI18N
    private static final String DISPLAY_MIDP_1 
                                = getMessage("MIDP_VERSION_DISPLAY_1");         // NOI18N
    private static final String DISPLAY_MIDP_2 
                                = getMessage("MIDP_VERSION_DISPLAY_2");         // NOI18N
    // string values for using in java code
    private static final String JAVA_FOREVER = "MidpVersionDescriptor.FOREVER"; // NOI18N
    private static final String JAVA_MIDP    = "MidpVersionDescriptor.MIDP";    // NOI18N
    private static final String JAVA_MIDP_1  = "MidpVersionDescriptor.MIDP_1";  // NOI18N
    private static final String JAVA_MIDP_2  = "MidpVersionDescriptor.MIDP_2";  // NOI18N
    /**
     * returns display value of this Version
     * @return
     */
    @Override
    public String toString() {
        return displayValue();
    }
    
    public String displayValue(){
        switch(this) {
         case FOREVER: 
             return DISPLAY_FOREVER;
         case MIDP: 
             return DISPLAY_MIDP;
         case MIDP_1: 
             return DISPLAY_MIDP_1;
         case MIDP_2: 
             return DISPLAY_MIDP_2;
         default: 
             return DISPLAY_MIDP;
       }
    }

    public String javaCodeValue(){
        switch(this) {
         case FOREVER: 
             return JAVA_FOREVER;
         case MIDP: 
             return JAVA_MIDP;
         case MIDP_1: 
             return JAVA_MIDP_1;
         case MIDP_2: 
             return JAVA_MIDP_2;
         default: 
             return JAVA_MIDP;
       }
    }

    static ComboBoxModel getComboBoxModel(){
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Version[]{
                    Version.FOREVER,
                    Version.MIDP,
                    Version.MIDP_1,
                    Version.MIDP_2
                });
        return model;
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicModuleConfVisualPanel.class, key);
    }

}
