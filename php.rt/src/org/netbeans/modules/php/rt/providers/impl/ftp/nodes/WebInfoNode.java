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

import org.netbeans.modules.php.rt.providers.impl.DefaultServerCustomizer;
import org.netbeans.modules.php.rt.providers.impl.ServerCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpServerFileCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpServerHttpCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.nodes.AbstractWebInfoNode;
import org.netbeans.modules.php.rt.spi.providers.Host;

/**
 *
 * @author avk
 */
public class WebInfoNode extends AbstractWebInfoNode {
    
    public WebInfoNode( FtpHostImpl host ) {
        super(host);
    }
    
    @Override
    protected ServerCustomizerComponent getCustomizerComponent(
            DefaultServerCustomizer parentDialog) 
    {
        Host host = getLookup().lookup(Host.class);
        if (host != null && host instanceof FtpHostImpl) {
            FtpHostImpl impl = (FtpHostImpl) host;
            return new FtpServerHttpCustomizerComponent(impl, parentDialog);
        }
        return null;
    }
}
