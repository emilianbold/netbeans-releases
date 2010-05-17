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
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * wrapper for Palette categories specified in MidpPaletteProvider.
 * @author avk
 */
public enum PaletteCategory {
CATEGORY_COMMANDS,
CATEGORY_DISPLAYABLES,
CATEGORY_ELEMENTS,
CATEGORY_ITEMS,
CATEGORY_PROCESS_FLOW,
CATEGORY_RESOURCES,
CATEGORY_CUSTOM,
CATEGORY_DATABINDING;

private static final String JAVA_CATEGORY_COMMANDS 
                                = "MidpPaletteProvider.CATEGORY_COMMANDS";      // NOI18N
private static final String JAVA_CATEGORY_DISPLAYABLES 
                                = "MidpPaletteProvider.CATEGORY_DISPLAYABLES";  // NOI18N
private static final String JAVA_CATEGORY_ELEMENTS 
                                = "MidpPaletteProvider.CATEGORY_ELEMENTS";      // NOI18N
private static final String JAVA_CATEGORY_ITEMS 
                                = "MidpPaletteProvider.CATEGORY_ITEMS";         // NOI18N
private static final String JAVA_CATEGORY_PROCESS_FLOW 
                                = "MidpPaletteProvider.CATEGORY_PROCESS_FLOW";  // NOI18N
private static final String JAVA_CATEGORY_RESOURCES 
                                = "MidpPaletteProvider.CATEGORY_RESOURCES";     // NOI18N
private static final String JAVA_CATEGORY_CUSTOM 
                                = "MidpPaletteProvider.CATEGORY_CUSTOM";        // NOI18N
private static final String JAVA_CATEGORY_DATABINDING 
                                = "DatabindingPaletteProvider.CATEGORY_DATABINDING";// NOI18N

private static final String BUNDLE_PREFIX = "vmd-midp/palette/";// NOI18N
    /**
     * returns display value of this Version
     * @return
     */
    @Override
    public String toString() {
        return displayValue();
    }

    public String displayValue() {
        switch (this) {
            case CATEGORY_COMMANDS:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_COMMANDS);
            case CATEGORY_DISPLAYABLES:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_DISPLAYABLES);
            case CATEGORY_ELEMENTS:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_ELEMENTS);
            case CATEGORY_ITEMS:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_ITEMS);
            case CATEGORY_PROCESS_FLOW:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_PROCESS_FLOW);
            case CATEGORY_RESOURCES:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_RESOURCES);
            case CATEGORY_CUSTOM:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_CUSTOM);
            case CATEGORY_DATABINDING:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_DATABINDING);
            default:
                return getBundleMessage(MidpPaletteProvider.class,
                        MidpPaletteProvider.CATEGORY_DISPLAYABLES);
        }
    }
    private String getBundleMessage(Class clazz, String resName){
        return NbBundle.getMessage(clazz, BUNDLE_PREFIX+resName);
    }
    
    public String javaCodeValue() {
        switch (this) {
            case CATEGORY_COMMANDS:
                return JAVA_CATEGORY_COMMANDS;
            case CATEGORY_DISPLAYABLES:
                return JAVA_CATEGORY_DISPLAYABLES;
            case CATEGORY_ELEMENTS:
                return JAVA_CATEGORY_ELEMENTS;
            case CATEGORY_ITEMS:
                return JAVA_CATEGORY_ITEMS;
            case CATEGORY_PROCESS_FLOW:
                return JAVA_CATEGORY_PROCESS_FLOW;
            case CATEGORY_RESOURCES:
                return JAVA_CATEGORY_RESOURCES;
            case CATEGORY_CUSTOM:
                return JAVA_CATEGORY_CUSTOM;
            case CATEGORY_DATABINDING:
                return JAVA_CATEGORY_DATABINDING;
            default:
                return JAVA_CATEGORY_DISPLAYABLES;
        }
    }
    
    static ComboBoxModel getComboBoxModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new PaletteCategory[]{
                    PaletteCategory.CATEGORY_DISPLAYABLES,
                    PaletteCategory.CATEGORY_COMMANDS,
                    PaletteCategory.CATEGORY_ELEMENTS,
                    PaletteCategory.CATEGORY_ITEMS,
                    PaletteCategory.CATEGORY_PROCESS_FLOW,
                    PaletteCategory.CATEGORY_RESOURCES,
                    PaletteCategory.CATEGORY_DATABINDING,
                    PaletteCategory.CATEGORY_CUSTOM
                
                });
        return model;
    }
}
