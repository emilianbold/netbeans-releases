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

    public APTFindUnusedBlocksWalker(APTFile apt, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler);
    }
    
    private List<CsmOffsetable> blocks = new ArrayList<CsmOffsetable>();

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

    private void handleIf(APT opener, boolean value) {
        handleIf(opener, value, true);
    }
    
    private void handleIf(APT opener, boolean value, boolean evaluateElif) {
        APT closer = opener.getNextSibling();
        if (closer == null) {
            return; // malformed file -- giveup
        }
        if (!value || !evaluateElif) {
            addBlock(opener.getEndOffset(), closer.getOffset()-1);
        } 
        if (closer.getType()==APT.Type.ELSE) {
            APT closer2 = closer.getNextSibling();
            if (closer2 == null || closer2.getType()!=APT.Type.ENDIF) {
                return; // malformed file -- giveup
            }
            if (value || !evaluateElif) {
                addBlock(closer.getEndOffset(), closer2.getOffset()-1);
            }
        }
        if (closer.getType()==APT.Type.ELIF) {
            // once we are entering sequence of elif's while value==true we never 
            // interested in real elif value, that's what evaluateElif is about
            handleIf(closer, super.onElif(closer, value), evaluateElif && !value);
        }
        // closer.getType()==APT.Type.ENDIF or malformed code
    }

   private void addBlock(int start, int end) {
        blocks.add(Utils.createOffsetable(null, start, end));
    }
}