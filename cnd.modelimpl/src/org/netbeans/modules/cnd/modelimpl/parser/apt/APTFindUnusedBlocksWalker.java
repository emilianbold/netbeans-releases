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
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;

/**
 * This walker gathers information about code blocks being hidden by preprocessor
 * commands
 * 
 * @author Sergey Grinev
 */
public class APTFindUnusedBlocksWalker extends APTSelfWalker {

    public APTFindUnusedBlocksWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler);
        this.csmFile = csmFile;
    }
    
    private final List<CsmOffsetable> blocks = new ArrayList<CsmOffsetable>();
    private final CsmFile csmFile;

    public List<CsmOffsetable> getBlocks() {
        return blocks;
    }
    
    protected @Override boolean onIfdef(APT apt) {
        boolean val = super.onIfdef(apt);
        handleIf(apt, val);
        return val;
    }
    
    protected @Override boolean onIfndef(APT apt) {
        boolean val = super.onIfndef(apt);
        handleIf(apt, val);
        return val;
    }

    protected @Override boolean onIf(APT apt) {
        boolean val = super.onIf(apt);
        handleIf(apt, val);
        return val;
    }

    protected @Override boolean onElif(APT apt, boolean wasInPrevBranch) {
        boolean val = super.onElif(apt, wasInPrevBranch);
        handleIf(apt, val);
        return val;
    }

    protected @Override boolean onElse(APT apt, boolean wasInPrevBranch) {
        boolean val = super.onElse(apt, wasInPrevBranch);
        handleIf(apt, val);
        return val;
    }
    
    private void handleIf(APT opener, boolean value) {
        APT closer = opener.getNextSibling();
        if (closer != null && !value) {
            blocks.add(Utils.createOffsetable(csmFile, opener.getEndOffset(), closer.getOffset()-1));
        } 
    }
}