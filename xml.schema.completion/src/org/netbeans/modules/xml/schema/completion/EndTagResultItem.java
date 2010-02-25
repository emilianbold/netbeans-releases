/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.schema.completion;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.openide.util.NbBundle;

/**
 *
 * @Alex Petrov (Alexey.Petrov@Sun.com)
 */
public class EndTagResultItem extends CompletionResultItem {
    private static final Logger _logger = Logger.getLogger(EndTagResultItem.class.getName());

    private int endTagSortPriority = -1;

    public EndTagResultItem(String tagName, TokenSequence tokenSequence) {
        super(null, null);
        this.itemText = tagName;
        setTokenSequence(tokenSequence);
    }

    @Override
    public String getDisplayText() {
        return (CompletionUtil.END_TAG_PREFIX + 
               (itemText != null ? itemText : NbBundle.getMessage(EndTagResultItem.class,
                   "UNKNOWN_TAG_NAME")) +
                CompletionUtil.TAG_LAST_CHAR);
    }

    @Override
    public String getReplacementText(){
        return getDisplayText();
    }

    @Override
    public int getCaretPosition() {
        return 0;
    }

    @Override
    public CompletionPaintComponent getPaintComponent() {
        if (component == null) {
            component = new CompletionPaintComponent.DefaultCompletionPaintComponent(this);
        }
        return component;
    }

    public void setSortPriority(int sortPriority) {
        this.endTagSortPriority = sortPriority;
    }

    @Override
    public int getSortPriority() {
        return endTagSortPriority;
    }

    @Override
    protected void replaceText(final JTextComponent component, final String text,
        final int offset, final int len) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    if (len > 0) doc.remove(offset, len);
                    
                    String insertingText = getInsertingText(component, text);
                    doc.insertString(offset, insertingText, null);
                } catch (Exception e) {
                    _logger.log(Level.SEVERE,
                        e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
                }
            }
        });
    }
}