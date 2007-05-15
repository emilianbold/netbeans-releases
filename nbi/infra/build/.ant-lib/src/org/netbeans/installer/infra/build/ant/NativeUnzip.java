/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.netbeans.installer.infra.build.ant.utils.Utils;

public class NativeUnzip extends Expand {
    private File dest; //req
    private File source; // req
    
    @Override
    public void setDest(File d) {
        this.dest = d;
        
        super.setDest(d);
    }
    
    @Override
    public void setSrc(File s) {
        this.source = s;
        
        super.setSrc(s);
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            Utils.setProject(getProject());
            log("trying native unzip");
            
            Utils.nativeUnzip(source, dest);
        } catch (IOException e) {
            log("native unzip failed, falling back to java implementation");
            
            Utils.delete(dest);
            super.execute();
        }
    }
}
