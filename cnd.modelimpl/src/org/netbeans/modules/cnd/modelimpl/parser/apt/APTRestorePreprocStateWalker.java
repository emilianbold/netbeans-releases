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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeInfo;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
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
    private final Stack<IncludeInfo> inclStack;
    private final APTIncludeHandler.IncludeInfo stopDirective;
    private final boolean searchInterestedFile;
    
    /** Creates a new instance of APTRestorePreprocStateWalker */
    public APTRestorePreprocStateWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler, Stack<IncludeInfo> inclStack, String interestedFile) {
        super(base, apt, file, preprocHandler, null);
        this.searchInterestedFile = true;
        this.interestedFile = interestedFile;
        this.inclStack = inclStack;
        assert (!inclStack.empty());
        this.stopDirective = this.inclStack.pop();
        assert (stopDirective != null);
    }
    
    /** Creates a new instance of APTRestorePreprocStateWalker */
    public APTRestorePreprocStateWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler) {
        super(base, apt, file, preprocHandler, null);
        this.searchInterestedFile = false;
        this.interestedFile = null;
        this.inclStack = null;
        this.stopDirective = null;
    }
    
    protected FileImpl includeAction(ProjectBase inclFileOwner, CharSequence inclPath, int mode, APTInclude apt, APTMacroMap.State postIncludeState) throws IOException {
        FileImpl csmFile = null;
        boolean foundDirective = false;
        if (searchInterestedFile) {
            // check if stop directive is met
            if (apt.getToken().getLine() == stopDirective.getIncludeDirectiveLine()) {
                if (inclPath.equals(stopDirective.getIncludedPath())) {
                    foundDirective = true;
                } else {
                    // we restored by incorrect include stack, see comment to this.visit()
                    APTHandlersSupport.invalidatePreprocHandler(getPreprocHandler());
                    super.stop();
                    return null;
                }
            }
        }
        try {
            csmFile = inclFileOwner.getFile(new File(inclPath.toString()), false);
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

    @Override
    public void visit() {
        super.visit();
        if (searchInterestedFile && !super.isStopped()) {
            // For now we don't escalate changes to library headers which means 
            // you can get get obsolete preprocessor state for library headers.
            // In those cases library headers should be "presented" in their 
            // natural state with preprocessor state got from project properties only
            APTHandlersSupport.invalidatePreprocHandler(getPreprocHandler());
            // See IZ119620, IZ120478
        }
    }
    
    
}
