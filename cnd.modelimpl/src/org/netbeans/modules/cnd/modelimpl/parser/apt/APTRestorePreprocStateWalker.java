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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 * special walker to restore preprocessor state from include stack
 * @author Vladimir Voskresensky
 */
public class APTRestorePreprocStateWalker extends APTProjectFileBasedWalker {
    private final String interestedFile;
    private final Stack/*<IncludeInfo>*/ inclStack;
    private final APTIncludeHandler.IncludeInfo stopDirective;
    private final boolean searchInterestedFile;
    
    /** Creates a new instance of APTRestorePreprocStateWalker */
    public APTRestorePreprocStateWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler, Stack/*<IncludeInfo>*/ inclStack, String interestedFile) {
        super(base, apt, file, preprocHandler);
        this.searchInterestedFile = true;
        this.interestedFile = interestedFile;
        this.inclStack = inclStack;
        assert (!inclStack.empty());
        this.stopDirective = (APTIncludeHandler.IncludeInfo) this.inclStack.pop();
        assert (stopDirective != null);
    }
    
    /** Creates a new instance of APTRestorePreprocStateWalker */
    public APTRestorePreprocStateWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler) {
        super(base, apt, file, preprocHandler);
        this.searchInterestedFile = false;
        this.interestedFile = null;
        this.inclStack = null;
        this.stopDirective = null;
    }
    
    protected FileImpl includeAction(ProjectBase inclFileOwner, String inclPath, int mode, APTInclude apt) throws IOException {
        FileImpl csmFile = null;
        boolean foundDirective = false;
        if (searchInterestedFile) {
            // in fact, we know, that the first correct inclusion has priority,
            // may be check for inclPath and interestedFile correspondence only?
            if ((apt.getToken().getLine() == stopDirective.getIncludeDirectiveLine() ||
                    inclPath.equals(interestedFile))) {
                foundDirective = true;
            }            
        }
        try {
            csmFile = inclFileOwner.getFile(new File(inclPath));
            if( csmFile == null ) {
		// this might happen if the file has been just deleted from project
		return null;
	    }
            if (foundDirective) {
                // we met candidate to stop on #include directive
                assert inclStack != null;
                // look if it the real target or target is in sub-#include
                if (!inclStack.empty()) {
                    // this is not the target
                    // need to continue restoring in sub-#includes
                    APTFile aptLight = inclFileOwner.getAPTLight(csmFile);
                    if (aptLight != null) {
                        APTWalker walker = new APTRestorePreprocStateWalker(getStartProject(), aptLight, csmFile, getPreprocHandler(), inclStack, interestedFile);
                        walker.visit();
                    } else {
                        // expected #included file was deleted
                        csmFile = null;
                    }
                }
            } else {
                // usual gathering macro map without check on #include directives
                APTFile aptLight = inclFileOwner.getAPTLight(csmFile);
                if (aptLight != null) {
                    APTWalker walker = new APTRestorePreprocStateWalker(getStartProject(), aptLight, csmFile, getPreprocHandler());
                    walker.visit();
                } else {
                    // expected #included file was deleted
                    csmFile = null;
                }                
            }
        } finally {
            if (foundDirective) {
                // we restored everything. Time to stop
                // but do not clear includes way => no popInclude
                super.stop();
            } else {
                getIncludeHandler().popInclude(); 
            }
        }
        return csmFile;
    }    
}
