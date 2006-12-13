/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.util.Iterator;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * Implementation of the hyperlink provider for java language.
 * <br>
 * The hyperlinks are constructed for #include directives.
 * <br>
 * The click action corresponds to performing the open file action.
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeHyperlinkProvider extends CsmAbstractHyperlinkProvider {
    
    /** Creates a new instance of CsmIncludeHyperlinkProvider */
    public CsmIncludeHyperlinkProvider() {
    }
    
    protected boolean isValidToken(Token token) {
        if ((token != null) &&
                ((token.getTokenID() == CCTokenContext.SYS_INCLUDE) ||
                (token.getTokenID() == CCTokenContext.USR_INCLUDE))) {
            return true;
        } else {
            return false;
        }
    }
    
    protected void performAction(final BaseDocument originalDoc, final JTextComponent target, final int offset) {
        goToInclude(originalDoc, target, offset);
    }
    
    public boolean goToInclude(BaseDocument doc, JTextComponent target, int offset) {
        if (!preJump(doc, target, offset, "opening-include-element")) {
            return false;
        }
        CsmInclude incl = findInclude(doc, offset);
        CsmOffsetable item = incl == null ? null : new IncludeTarget(incl);
        return postJump(item, "goto_source_source_not_found", "cannot-open-include-element");
    }
    
    private CsmInclude findInclude(BaseDocument doc, int offset) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, true);
        if (csmFile != null) {
            for (Iterator it = csmFile.getIncludes().iterator(); it.hasNext();) {
                CsmInclude incl = (CsmInclude) it.next();
                if (incl.getStartOffset() <= offset && 
                        offset <= incl.getEndOffset()) {
                    return incl;
                }
            }
        }
        return null;
    }
    
    private static final class IncludeTarget implements CsmOffsetable {
        private CsmInclude include;
        
        public IncludeTarget(CsmInclude include) {
            this.include = include;
        }
        
        public CsmFile getContainingFile() {
            return include.getIncludeFile();
        }
        
        public int getStartOffset() {
            // start of the file
            return 0;
        }
        
        public int getEndOffset() {
            // start of the file
            return 1;
        }
        
        public CsmOffsetable.Position getStartPosition() {
            return START_POSITION;
        }
        
        public CsmOffsetable.Position getEndPosition() {
            return START_POSITION;
        }
        
        public String getText() {
            return include.getIncludeName();
        }
        
    }
    
    private static final CsmOffsetable.Position START_POSITION = new CsmOffsetable.Position() {
        public int getOffset() {
            return 1;
        }

        public int getLine() {
            return 1;
        }

        public int getColumn() {
            return 1;
        }
    };
}
