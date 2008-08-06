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
 * Contributor(s):
 *
 */
package org.netbeans.jellytools.modules.java.editor;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.spi.editor.codegen.CodeGenerator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class GenerateCodeOperator {
    
        
    //public static final String GENERATE_CONSTRUCTOR = Bundle.getStringTrimmed("org.netbeans.modules.java.editor.codegen.Bundle.properties", "LBL_constructor"); //NOI18N
    
    public static final String GENERATE_CONSTRUCTOR = "Constructor..."; //NOI18N

    public static final String GENERATE_GETTER = "Getter..."; //NOI18N
    
    public static final String GENERATE_SETTER = "Setter..."; //NOI18N
    
    public static final String GENERATE_GETTER_SETTER = "Getter and Setter..."; //NOI18N
    
    public static final String GENERATE_EQUALS_HASHCODE = "equals() and hashCode()..."; //NOI18N
    
    public static final String OVERRIDE_METHOD = "Override Method..."; //NOI18N
            
    public static final String IMPLEMENT_METHOD = "Implement Method..."; //NOI18N
    
    public static final String DELEGATE_METHOD = "Delegate Method..."; //NOI18N
    /**
     * Opens requested code generation dialog
     * @param type Displayname of menu item
     * @param editor Operator of editor window where should be menu opened
     * @return true is item is found, false elsewhere
     */
    public static boolean openDialog(String type, EditorOperator editor) {
        new EventTool().waitNoEvent(1000);
        editor.pushKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();
        new EventTool().waitNoEvent(1000);
        JListOperator list = new JListOperator(jdo);        
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            if(cg.getDisplayName().equals(type)) {
                list.setSelectedIndex(i);
                jdo.pushKey(KeyEvent.VK_ENTER);
                new EventTool().waitNoEvent(1000);
                return true;
            }
        }
        return false;        
    }

    /**
     * Compares list of items provided in the Insert Code dialog with the list of expected items
     * @param editor Operator of editor window where should Insert Code should be caled
     * @param items Expested items
     * @return true is both list are the same, false otherwise
     */
    public static boolean containsItems(EditorOperator editor, String ... items) {
        Set<String> actItems = new HashSet<String>();
        List<String> expItems = Arrays.asList(items);
        editor.pushKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();        
        JListOperator list = new JListOperator(jdo);
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            actItems.add(cg.getDisplayName());
            if(!expItems.contains(cg.getDisplayName())) return false;
        }
        for (String string : expItems) {
            if(!actItems.contains(string)) return false;            
        }
        return true;       
    }

}
