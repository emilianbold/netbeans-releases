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
 */
package org.netbeans.test.java.editor.jelly;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.java.editor.codegen.CodeGenerator;
import org.netbeans.modules.java.editor.codegen.ConstructorGenerator;
import org.netbeans.modules.java.editor.codegen.DelegateMethodGenerator;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
import org.netbeans.modules.java.editor.codegen.GetterSetterGenerator;
import org.netbeans.modules.java.editor.codegen.ImplementOverrideMethodGenerator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class GenerateCodeOperator {
    
        
    public static final String GENERATE_CONSTRUCTOR = org.openide.util.NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor"); //NOI18N
       
    public static final String GENERATE_GETTER = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter"); //NOI18N
    
    public static final String GENERATE_SETTER = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_setter"); //NOI18N
    
    public static final String GENERATE_GETTER_SETTER = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_and_setter"); //NOI18N
    
    public static final String GENERATE_EQUALS_HASHCODE = org.openide.util.NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_equals_and_hashcode"); //NOI18N
    
    public static final String OVERRIDE_METHOD = org.openide.util.NbBundle.getMessage(ImplementOverrideMethodGenerator.class, "LBL_override_method"); //NOI18N
            
    public static final String IMPLEMENT_METHOD = org.openide.util.NbBundle.getMessage(ImplementOverrideMethodGenerator.class, "LBL_implement_method"); //NOI18N
    
    public static final String DELEGATE_METHOD = org.openide.util.NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method"); //NOI18N
    
    /**
     * Opens requested code generation dialog
     * @param type Displayname of menu item
     * @return true is item is found, false elsewhere
     */
    public static boolean openDialog(String type, EditorOperator editor) {
        editor.pushKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();        
        JListOperator list = new JListOperator(jdo);
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            if(cg.getDisplayName().equals(type)) {
                list.setSelectedIndex(i);
                jdo.pushKey(KeyEvent.VK_ENTER);
                return true;
            }
        }
        return false;        
    }
    
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
