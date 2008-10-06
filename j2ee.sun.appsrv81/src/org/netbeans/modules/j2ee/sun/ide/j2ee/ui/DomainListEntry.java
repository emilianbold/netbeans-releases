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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.io.File;
import java.io.IOException;
import org.openide.util.NbBundle;

/**
 *
 * @author vkraemer
 */
class DomainListEntry {
    private String hostPort;
    private File domainDir;
    private File location;

    public DomainListEntry(String hostPort, File domainDir, File location) {
        this.hostPort = hostPort;
        this.domainDir = domainDir;
        this.location = location;
    }
    
    @Override
    public String toString() {
        String abbrevDomainDir = stripCommonPrefix(location,domainDir);
        return  NbBundle.getMessage(DomainListEntry.class,
            "LBL_domainListEntry", new Object[] {hostPort,abbrevDomainDir});
    }
    
    File getDomainDir() {
        return domainDir;
    }

    private String stripCommonPrefix(File pattern, File value) {
        String cpath1;
        String cpath2;
        try {
            cpath1 = pattern.getCanonicalPath();
            cpath2 = value.getCanonicalPath();
        } catch (IOException ioe) {
            return value.toString();
        }
        int len1 = cpath1.length();
        int len2 = cpath2.length();
        int i;
        for (i = 0; i < len1 && i < len2; i++) {
            if (cpath1.charAt(i) != cpath2.charAt(i)) {
                break;
            }
        }
        String prefix = "";
        if (i > 0)
            prefix = "...";
        return prefix+cpath2.substring(i);
    }
    
}
