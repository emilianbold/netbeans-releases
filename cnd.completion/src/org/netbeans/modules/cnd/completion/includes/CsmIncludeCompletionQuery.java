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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeCompletionQuery {
    private final CsmFile file;
    public CsmIncludeCompletionQuery(CsmFile file) {
        this.file = file;
    }
    
    public List<CompletionItem> query(JComponent component, BaseDocument doc, Boolean usrInclude) {
        return Collections.<CompletionItem>emptyList();
    }
}
