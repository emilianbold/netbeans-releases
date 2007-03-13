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
/*
 * CssRuleCreateAction.java
 * Created on September 23, 2005, 5:05 PM
 */


package org.netbeans.modules.css.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;

/**
 * Action to create a new CSS Rule
 * @author Winston Prakash
 */
public class CssRuleCreateAction extends BaseAction{

    public static final String createRuleAction = "create-rule"; // NOI18N
    /** Creates a new instance of CssRuleCreateAction */
    public CssRuleCreateAction() {
        super(createRuleAction);
        putValue("helpID", CssRuleCreateAction.class.getName()); // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(CssRuleCreateAction.class, "Create_Rule"));
        putValue(BaseAction.ICON_RESOURCE_PROPERTY, "org/netbeans/modules/css/resources/new_rule.png"); // NOI18N
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if(target == null) {
            return;
        }
        CssRuleCreateActionDialog cssRuleCreateActionDialog = new CssRuleCreateActionDialog();
        cssRuleCreateActionDialog.showDialog();
        String styleRuleName = cssRuleCreateActionDialog.getStyleRuleName();
        if((styleRuleName != null) && !styleRuleName.equals("")){
            Caret caret = target.getCaret();
            int searchPos = target.getCaret().getDot() -1 ;
            BaseDocument doc = (BaseDocument)target.getDocument();
            int insertPos = doc.getLength() + 1;
            try{
                if(searchPos > 0){
                    String txtBefore = doc.getText(searchPos, 1);
                    while(!(txtBefore.equals("{") || txtBefore.equals("}"))){
                        if(txtBefore.equals("/")){
                            if ((searchPos-1) >= 0){
                                if (doc.getText(searchPos-1, 1).equals("*")){
                                    break;
                                }else if((searchPos+1) <= doc.getLength()){
                                    if (doc.getText(searchPos+1, 1).equals("*")){
                                        break;
                                    }
                                }
                            }
                        }
                        searchPos--;
                        if (searchPos < 0) break;
                        txtBefore = doc.getText(searchPos, 1);
                    }
                    if(searchPos < 0){
                        insertPos = 0;
                    }else if(txtBefore.equals("}")){
                        insertPos = searchPos + 1;
                    }else if(txtBefore.equals("/")){
                        if ((searchPos-1) >= 0){
                            if (doc.getText(searchPos-1, 1).equals("*")){
                                insertPos = searchPos + 1;
                            }else if((searchPos+1) <= doc.getLength()){
                                if (doc.getText(searchPos+1, 1).equals("*")){
                                    insertPos = searchPos - 1;
                                }
                            }
                        }
                    } else if(txtBefore.equals("{")){
                        searchPos = target.getCaret().getDot();
                        String txtAfter = doc.getText(searchPos, 1);
                        while(!txtAfter.equals("}")){
                            searchPos++;
                            if(searchPos > doc.getLength()) break;
                            txtAfter = doc.getText(searchPos, 1);
                        }
                        if(txtAfter.equals("}")){
                            insertPos = searchPos + 1;
                        }
                    }
                }else{
                    insertPos = 0;
                }
                doc.insertString(insertPos,"\n" + styleRuleName + " {\n\n}", null);
                searchPos = insertPos;
                String txtAfter = doc.getText(searchPos, 1);
                while(!txtAfter.equals("{")){
                    searchPos++;
                    if(searchPos > doc.getLength()) break;
                    txtAfter = doc.getText(searchPos, 1);
                }
                searchPos += 2;
                target.setCaretPosition(searchPos);
            }catch(BadLocationException exc){
                exc.printStackTrace();
            }
        }
    }
    
}
