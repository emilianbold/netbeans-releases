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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;

/**
 * simple test implementation of walker
 * @author Vladimir Voskresensky
 */
public class APTWalkerTest extends APTAbstractWalker {

    public APTWalkerTest(APTFile apt, APTPreprocHandler ppHandler) {
        super(apt, ppHandler);
    }

    private long resolvingTime = 0;
    private long lastTime = 0;
    public long getIncludeResolvingTime() {
        return resolvingTime;
    }

    protected void onInclude(APT apt) {
        lastTime = System.currentTimeMillis();
        super.onInclude(apt);
    }

    protected void onIncludeNext(APT apt) {
        lastTime = System.currentTimeMillis();
        super.onIncludeNext(apt);
    }

    protected void include(ResolvedPath resolvedPath, APTInclude aptInclude) {
        resolvingTime += System.currentTimeMillis() - lastTime;
        if (resolvedPath != null && getIncludeHandler().pushInclude(resolvedPath.getPath(), aptInclude.getToken().getLine())) {
            APTFile apt;
            boolean res = false;
            try {
                apt = APTDriver.getInstance().findAPTLight(new FileBufferFile(new File(resolvedPath.getPath())));
                APTWalkerTest walker = new APTWalkerTest(apt, getPreprocHandler());
                walker.visit();
                resolvingTime += walker.resolvingTime;
                res = true;               
            } catch (IOException ex) {
                APTUtils.LOG.log(Level.SEVERE, "error on include " + resolvedPath, ex);// NOI18N
            } finally {
                getIncludeHandler().popInclude(); 
            }
        }        
    }
}
