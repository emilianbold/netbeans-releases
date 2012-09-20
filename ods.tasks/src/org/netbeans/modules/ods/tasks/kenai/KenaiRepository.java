/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.ods.tasks.kenai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiAccessor;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiProject;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.ods.tasks.C2CConnector;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import static org.netbeans.modules.ods.tasks.kenai.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondra Vrabec
 */
public class KenaiRepository extends C2CRepository implements PropertyChangeListener {

    private final KenaiProject kenaiProject;

    public KenaiRepository (KenaiProject kenaiProject, String repoName, String url) {
        super(createInfo(repoName, url)); // use name as id - can't be changed anyway
        assert kenaiProject != null;
        this.kenaiProject = kenaiProject;
        KenaiUtil.getKenaiAccessor(url).addPropertyChangeListener(this, kenaiProject.getWebLocation().toString());
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(KenaiAccessor.PROP_LOGIN)) {

            String user;
            char[] psswd;
            PasswordAuthentication pa =
                    KenaiUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false); // do not force login
            if (pa != null) {
                user = pa.getUserName();
                psswd = pa.getPassword();
            } else {
                user = ""; //NOI18N
                psswd = new char[0];
            }

            setCredentials(user, psswd, null, null);
        }
    }
    
    @Override
    public void ensureCredentials() {
        authenticate(null);
    }

    public boolean authenticate (String errroMsg) {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), true);
        if(pa == null) {
            return false;
        }
        
        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setCredentials(user, password, null, null);

        return true;
    }
    
    public KenaiProject getKenaiProject () {
        return kenaiProject;
    }
    
    @Override
    protected Object[] getLookupObjects() {
        Object[] obj = super.getLookupObjects();
        Object[] obj2 = new Object[obj.length + 1];
        System.arraycopy(obj, 0, obj2, 0, obj.length);
        obj2[obj2.length - 1] = kenaiProject;
        return obj2;
    }

    private static String getRepositoryId (String name, String url) {
        return TextUtils.encodeURL(url) + ":" + name; //NOI18N
    }

    @Messages({"# {0} - repository name", "# {1} - url", "LBL_RepositoryTooltipNoUser={0} : {1}"})
    private static RepositoryInfo createInfo (String repoName, String url) {
        String id = getRepositoryId(repoName, url);
        String tooltip = LBL_RepositoryTooltipNoUser(repoName, url);
        return new RepositoryInfo(id, C2CConnector.ID, url, repoName, tooltip);
    }
}
