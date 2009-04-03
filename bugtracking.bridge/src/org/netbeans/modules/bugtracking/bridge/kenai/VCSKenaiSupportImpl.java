/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.bridge.kenai;

import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.versioning.util.VCSKenaiSupport;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSKenaiSupport.class)
public class VCSKenaiSupportImpl extends VCSKenaiSupport {

    private final Set<String> kenaiUrls = new HashSet<String>();

    private static Pattern repositoryPattern = Pattern.compile("(https|http)://(testkenai|kenai)\\.com/(svn|hg)/(\\S*)~(.*)");

    @Override
    public boolean isKenai(String url) {
        synchronized(kenaiUrls) {
            if(kenaiUrls.contains(url)) {
                return true;
            }
        }
        boolean ret = false;
//        try {
//            ret = KenaiProject.forRepository(url) != null;
//        } catch (KenaiException ex) { }

        // XXX need query on kenai forRepository might take too long
        Matcher m = repositoryPattern.matcher(url);
        ret = m.matches();

        if(ret) {
            synchronized(kenaiUrls) {
                kenaiUrls.add(url);
            }
        }
        return ret;
    }
    
    @Override
    public PasswordAuthentication getPasswordAuthentication(String url) {
        PasswordAuthentication a = Kenai.getDefault().getPasswordAuthentication();
        if(a != null) {
            return a;
        } else {
            if(!UIUtils.showLogin()) {
                return null;
            }
        }
        return Kenai.getDefault().getPasswordAuthentication();
    }
}
