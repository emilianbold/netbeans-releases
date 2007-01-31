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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.File;
import java.util.Stack;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 * special walker to restore preprocessor state from include stack
 * @author Vladimir Voskresensky
 */
public class APTRestorePreprocStateWalker extends APTParseFileWalker {
    private String interestedFile;
    private Stack/*<IncludeInfo>*/ inclStack;
    private APTIncludeHandler.IncludeInfo stopDirective;
    
    /** Creates a new instance of APTRestorePreprocStateWalker */
    public APTRestorePreprocStateWalker(APTFile apt, FileImpl file, APTPreprocState preprocState, Stack/*<IncludeInfo>*/ inclStack, String interestedFile) {
        super(apt, file, preprocState);
        this.interestedFile = interestedFile;
        this.inclStack = inclStack;
        assert (!inclStack.empty());
        this.stopDirective = (APTIncludeHandler.IncludeInfo) this.inclStack.pop();
        assert (stopDirective != null);
    }
    
    protected FileImpl includeAction(ProjectBase inclFileOwner, String inclPath, APTPreprocState preprocState, int mode, APTInclude apt) {
        if (!inclStack.empty()) {
            // need to continue restoring
            FileImpl csmFile = inclFileOwner.getFile(new File(inclPath));
            assert csmFile != null;
            APTFile aptLight = inclFileOwner.getAPTLight(csmFile);
            if (aptLight != null) {
                // one more interation
                APTWalker walker = new APTRestorePreprocStateWalker(aptLight, csmFile, preprocState, inclStack, interestedFile);
                walker.visit();
            }
        }
        // in fact, we know, that the first correct inclusion has priority,
        // may be check for inclPath and interestedFile correspondence only?
        if (apt.getToken().getLine() == stopDirective.getIncludeDirectiveLine() ||
                inclPath.equals(interestedFile)) {
            // we restored everything. Time to stop
            super.stop();
        } else {
            getIncludeHandler().popInclude(); 
        }
        return null;
    }    
}
