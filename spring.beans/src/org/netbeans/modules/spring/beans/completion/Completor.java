/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.spring.beans.completion.CompletionContext.CompletionType;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class Completor {

    private List<SpringXMLConfigCompletionItem> items = new ArrayList<SpringXMLConfigCompletionItem>();
    
    private int anchorOffset = -1;
    private int caretOffset;
    private QueryProgress progress = new QueryProgress();
    private boolean additionalItems = false;
    private String additionalItemsText = "";
    
    protected Completor() {
    }
    
    public final List<SpringXMLConfigCompletionItem> complete(CompletionContext context) {
        caretOffset = context.getCaretOffset();
        computeCompletionItems(context, progress);
        return items;
    }
    
    protected abstract void computeCompletionItems(CompletionContext context, QueryProgress progress);

    public final boolean canFilter(CompletionContext context) {
        int newCaretOffset = context.getCaretOffset();
        int substitutionOffset = getSubstitutionOffset(context);
        if(anchorOffset == -1 || newCaretOffset < caretOffset) {
            return false;
        }
        
        // XXX: Check for some chars like () etc to invalidate filtering easily
        try {
            Document doc = context.getDocument();
            String prefix = doc.getText(substitutionOffset, newCaretOffset - substitutionOffset);
            List<SpringXMLConfigCompletionItem> currentItems = getCurrentItems();
            if(isFilteringPossible(prefix, currentItems)) {
                return true;
            } else {
                progress.cancel();
                return false;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    public final List<SpringXMLConfigCompletionItem> filter(CompletionContext context) {
        try {
            String prefix = computePrefixText(context);
            return filterByPrefix(prefix, items);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.<SpringXMLConfigCompletionItem>emptyList();
        }
    }
    
    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }
    
    protected final void addItem(SpringXMLConfigCompletionItem item) {
        synchronized(items) {
            items.add(item);
        }
    }
    
    private List<SpringXMLConfigCompletionItem> getCurrentItems() {
        List<SpringXMLConfigCompletionItem> currentItems;
        synchronized(items) {
            currentItems = new ArrayList<SpringXMLConfigCompletionItem>(items);
        }
        
        return currentItems;
    }
    
    private String computePrefixText(CompletionContext context) throws BadLocationException {
        int newCaretOffset = context.getCaretOffset();
        Document doc = context.getDocument();
        int substitutionOffset = getSubstitutionOffset(context);
        return doc.getText(substitutionOffset, newCaretOffset - substitutionOffset);
    }

    private boolean isFilteringPossible(String prefix, List<SpringXMLConfigCompletionItem> items) {
        for(SpringXMLConfigCompletionItem item : items) {
            if(item.getInsertPrefix().toString().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
    
    private List<SpringXMLConfigCompletionItem> filterByPrefix(String prefix, List<SpringXMLConfigCompletionItem> items) {
        List<SpringXMLConfigCompletionItem> fItems = new ArrayList<SpringXMLConfigCompletionItem>();
        for(SpringXMLConfigCompletionItem item : items) {
            if(item.getInsertPrefix().toString().startsWith(prefix)) {
                fItems.add(item);
            }
        }
        
        return fItems;
    }
    
    protected int getSubstitutionOffset(CompletionContext context) {
        if(context.getCompletionType() == CompletionType.ATTRIBUTE_VALUE) {
            return context.getCurrentToken().getOffset() + 1;
        } else if(context.getCompletionType() == CompletionType.ATTRIBUTE) {
            return context.getCaretOffset() - context.getTypedPrefix().length();
        } else if(context.getCompletionType() == CompletionType.TAG) {
            return context.getCurrentToken().getOffset();
        }
        
        return -1;
    }
    
    public final boolean hasAdditionalItems() {
        return additionalItems;
    }
    
    protected final void setAdditionalItems(boolean additionalItems) {
        this.additionalItems = additionalItems;
    }

    protected final void setAdditionalItemsText(String additionalItemsText) {
        this.additionalItemsText = additionalItemsText;
    }

    public final String getAdditionalItemsText() {
        return additionalItemsText;
    }
}
