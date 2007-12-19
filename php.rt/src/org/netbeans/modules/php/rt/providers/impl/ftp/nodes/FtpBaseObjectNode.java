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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp.nodes;

import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpFileInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author avk
 */
public class FtpBaseObjectNode  extends AbstractNode {

    private FtpFileInfo file;

    FtpBaseObjectNode(FtpHostImpl ftpHost, FtpFileInfo file){
        super(Children.LEAF, 
                createLookup(new FtpBaseObjectNodeChildren(), ftpHost, file));
        setChildren(getLookup().lookup(Children.class));
        setFtpFileInfo(file);
    }

    /*
    FtpBaseObjectNode(FtpHostImpl ftpHost, FtpFileInfo file){
        super(new FtpBaseObjectNodeChildren(), createLookup(ftpHost, file));
        setFtpFileInfo(file);
    }
     */

    public FtpFileInfo getFtpFileInfo(){
        return file;
    }
    
    public void setFtpFileInfo(FtpFileInfo file){
        this.file = file;
    }
    
    static Lookup createLookup( Children child, FtpHostImpl host, FtpFileInfo file ) {
        if (host != null && file != null) {
            return Lookups.fixed(new Object[] { 
                child,
                host, 
                file,
            });
        }
        else {
            return null;
        }
    }
    
}
