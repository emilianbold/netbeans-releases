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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;

/**
 * APT Walker which only gathers macromap. Shouldn't be used directly but
 * only overriden by walkers which don't need to gather any info in the includes
 * just want macromap from them
 * 
 * @author Sergey Grinev
 */
public class APTSelfWalker extends APTAbstractWalker {

    protected APTSelfWalker(APTFile apt, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler);
    }

    protected void include(ResolvedPath resolvedPath, APTInclude aptInclude) {
        if (resolvedPath != null && getIncludeHandler().pushInclude(resolvedPath.getPath(), aptInclude.getToken().getLine(), resolvedPath.getIndex())) {
            try {
                APTFile apt = APTDriver.getInstance().findAPTLight(new FileBufferFile(new File(resolvedPath.getPath())));
                APTSelfWalker walker = new APTSelfWalker(apt, getPreprocHandler());
                walker.visit();
            } catch (IOException ex) {
                APTUtils.LOG.log(Level.SEVERE, "error on include " + resolvedPath, ex); // NOI18N
            } finally {
                getIncludeHandler().popInclude();
            }
        }
    }
}