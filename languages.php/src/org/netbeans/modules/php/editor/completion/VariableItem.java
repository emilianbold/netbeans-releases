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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.editor.completion;

import java.util.Set;

import javax.swing.ImageIcon;

import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;


/**
 * @author ads
 *
 */
class VariableItem extends CompletionItem {
    
    enum VarTypes {
        PSEUDO("Pseudo-variable"), // NOI18N  a pseudo-variable $this
        GLOBAL("Global"), // NOI18N  variable that declared with global keyword
        STATIC("Static"), // NOI18N  variable that declared with static keyword
        LOCAL("Local"), // NOI18N  local variable with scope ( inside file, method )
        ATTRIBUTE("Attribute"), // NOI18N  static attribute in class f.e. Clazz::$attr
        /**
         * Class Constant.
         * @see {@link http://www.php.net/manual/en/language.oop5.constants.php}
         */
        CONSTANT("Constant"), // NOI18N 
        PREDEFINED("Superglobal"); // NOI18N  one of predefined variables ( $_POST, ... )
        
        VarTypes(String type){
            myType = type;
        }

        String getType() {
            return myType;
        }
        
        private final String myType;
    }

    /**
     * Constructs <code>VariableItem</code>.
     * @param varName
     * @param caretOffset
     * @param type
     * @param formatter
     * @param isPredefined <code>true</code> if this <code>VariableItem</code>
     * describes a predefined variable, otherwise - <code>true</code>, i.e. when
     * it describes a user defined variable.
     */
    VariableItem( String varName, int caretOffset , VarTypes type ,
            HtmlFormatter formatter, boolean isPredefined) 
    {
        super(caretOffset , formatter );
        myVariable = varName;
        myType = type;
        this.isPredefined = isPredefined;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getIcon()
     */
    public ImageIcon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getInsertPrefix()
     */
    public String getInsertPrefix() {
        return myVariable;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getKind()
     */
    public ElementKind getKind() {
        return ElementKind.VARIABLE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getLhsHtml()
     */
    public String getLhsHtml() {
        ElementKind kind = getKind();
        HtmlFormatter formatter = getFormatter();
        formatter.reset();
        formatter.name(kind, true);
        formatter.appendText(getName());
        formatter.name(kind, false);

        return formatter.getText();

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getModifiers()
     */
    public Set<Modifier> getModifiers() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getName()
     */
    public String getName() {
        return myVariable;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getRhsHtml()
     */
    public String getRhsHtml() {
        getFormatter().reset();

        getFormatter().appendText( myType.getType() );
        return getFormatter().getText();
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#isSmart()
     */
    public boolean isSmart() {
        return !isPredefined;
    }
    
    private String myVariable;
    
    private VarTypes myType;
    
    private boolean isPredefined;
    
}
