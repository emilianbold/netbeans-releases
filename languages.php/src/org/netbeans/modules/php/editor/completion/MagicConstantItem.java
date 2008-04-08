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

import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.languages.php.lang.MagicConstants;
import org.netbeans.modules.php.model.FormalParameter;


/**
 * This is PHP keyword completion proposal. 
 * 
 * @author Victor G. Vasilyev
 *
 */
class MagicConstantItem extends CompletionItem {

    private MagicConstants myMagicConstant;

    MagicConstantItem( MagicConstants magicConstant, int caretOffset , 
            HtmlFormatter formatter ) 
    {
        super(caretOffset , formatter );
        myMagicConstant = magicConstant;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getIcon()
     */
    public ImageIcon getIcon() {
        // TODO An Icon for Keyword is required.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getInsertPrefix()
     */
    public String getInsertPrefix() {
        return myMagicConstant.value();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getKind()
     */
    public ElementKind getKind() {
        return ElementKind.KEYWORD;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getLhsHtml()
     */
    public String getLhsHtml() {
        HtmlFormatter formatter = getFormatter();
        formatter.reset();
        formatter.name(getKind(), true);
        formatter.appendText(myMagicConstant.value());
        formatter.name(getKind(), false);
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
        return myMagicConstant.value();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getRhsHtml()
     */
    public String getRhsHtml() {
        // TODO If it is possible then a text explained the return type 
        // should be returned.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#isSmart()
     */
    public boolean isSmart() {
        return true;
    }
    
    
    private List<FormalParameter> getFormalParameterList() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getElement()
     */
    @Override
    public Element getElement() {
        return new DocumentableElement() {

            @Override
            public String getIn() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ElementKind getKind() {
                return ElementKind.CONSTANT;
            }

            @Override
            public Set<Modifier> getModifiers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getName() {
                return myMagicConstant.value();
            }
            
            public String getDocumentation() {
                // TODO: documentation for the myKeyword
                return null;
            }
            
        };
    }
       
}
