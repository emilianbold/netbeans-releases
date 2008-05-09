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
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;

/**
 * Completion proposal for the PHP code block. 
 * 
 * @author Victor G. Vasilyev
 */
class PHPBlockItem extends CompletionItem {
    //    private static final String ICON_FILE = 
    //      "org/netbeans/modules/php/editor/completion/php_block.png"; //NOI18N
    private static final ImageIcon ICON = null;
    //           new ImageIcon(org.openide.util.Utilities.loadImage(ICON_FILE));
    
    private static final String NAME="<?php >";
    private static final String INSERTED_TEXT="<?php  >";
    private static final String CUSTOM_INSERT_TEMPLATE="<?php ${cursor} ?>";
    private static final String RIGHT_SIDE_HTML="PHP Code Block";    
    private static final String DOCUMENTATION="<html>"+
      "<h2>The PHP Code Block.</h2>"+ 
      "<b>&lt;?php</b> | <b>?&gt;</b>";    

    public PHPBlockItem(CodeCompletionContext context) {
        super(context.getCaretOffset(), context.getFormatter());
    }

    public String getName() {
        return NAME;
    }

    public String getInsertPrefix() {
        // It seems, if the getCustomInsertTemplate() method will return 
        // non-null value and CUSTOM_INSERT_TEMPLATE will be actually used 
        // instead then the returned value should be non-null only to make this 
        // completion proposal applicable. Actually, the returned value will 
        // never be used elsewhere.
        // i.e. non-null value indicates that this completion proposal 
        // is applicable for inserting.
        return INSERTED_TEXT;
    }

    public String getLhsHtml() {
        HtmlFormatter formatter = getFormatter();
        formatter.reset();
        formatter.name(getKind(), true);
        formatter.appendText(getName());
        formatter.name(getKind(), false);
        return formatter.getText();
    }

    public String getRhsHtml() {
        return RIGHT_SIDE_HTML;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    public ImageIcon getIcon() {
        return ICON;
    }

    public Set<Modifier> getModifiers() {
        return null;
    }

    public boolean isSmart() {
        return false;
    }

    @Override
    public String getCustomInsertTemplate() {
        return CUSTOM_INSERT_TEMPLATE;
    }

    @Override
    public Element getElement() {
        return new DocumentableElement() {

                    public String getIn() {
                        return null;
                    }

                    public ElementKind getKind() {
                        return getKind();
                    }

                    public Set<Modifier> getModifiers() {
                        return getModifiers();
                    }

                    public String getName() {
                        return getName();
                    }

                    public String getDocumentation() {
                        return DOCUMENTATION;
                    }
                };
    }
   
}
