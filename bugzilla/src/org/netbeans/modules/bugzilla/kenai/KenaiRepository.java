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

package org.netbeans.modules.bugzilla.kenai;

import java.awt.Image;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.bugzilla.query.QueryParameter;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiRepository extends BugzillaRepository {

    static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/kenai-small.png"; // NOI18N
    private String urlParam;
    private Image icon;
    private String product;
    private KenaiQuery myIssues;
    private KenaiQuery allIssues;
    private String host;
    private final Object kenaiProject;

    KenaiRepository(KenaiProject kenaiProject, String repoName, String url, String host, String userName, String password, String urlParam, String product) {
        super(repoName, repoName, url, userName, password, null, null); // use name as id - can't be changed anyway
        this.urlParam = urlParam;
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.product = product;
        this.host = host;
        this.kenaiProject = kenaiProject;
    }

    public KenaiRepository(KenaiProject kenaiProject, String repoName, String url, String host, String urlParam, String product) {
        this(kenaiProject, repoName, url, host, getKenaiUser(), getKenaiPassword(), urlParam, product);
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public Query createQuery() {
        KenaiQuery q = new KenaiQuery(null, this, null, product, false, false);
        return q;
    }

    @Override
    public Issue createIssue() {
        return super.createIssue();
    }

    @Override
    public synchronized Query[] getQueries() {
        Query[] qs = super.getQueries();
        Query[] dq = getDefinedQueries();
        Query[] ret = new Query[qs.length + dq.length];
        System.arraycopy(qs, 0, ret, 0, qs.length);
        System.arraycopy(dq, 0, ret, qs.length, dq.length);
        return ret;
    }

    private Query[] getDefinedQueries() {
        List<Query> queries = new ArrayList<Query>();

        // my issues - only if username provided
        if(KenaiUtil.isLoggedIn()) {
            if(myIssues == null) {
                StringBuffer url = new StringBuffer();
                url.append(urlParam);

                // XXX escape @?
                // XXX what if user already mail address?
                String user = getKenaiUser();
                if(user == null) {
                    user = "";                                                  // NOI18N
                }
                String userMail = user + "@"+ host;                             // NOI18N
                url.append(MessageFormat.format(BugzillaConstants.MY_ISSUES_PARAMETERS_FORMAT, product, userMail));

                myIssues =
                    new KenaiQuery(
                        NbBundle.getMessage(KenaiRepository.class, "LBL_MyIssues"), // NOI18N
                        this,
                        url.toString(),
                        product,
                        true,
                        true);
            }
            queries.add(myIssues);
        }

        // all issues
        if(allIssues == null) {
            StringBuffer url = new StringBuffer();
            url = new StringBuffer();
            url.append(urlParam);
            url.append(MessageFormat.format(BugzillaConstants.ALL_ISSUES_PARAMETERS, product));
            allIssues =
                new KenaiQuery(
                    NbBundle.getMessage(KenaiRepository.class, "LBL_AllIssues"), // NOI18N
                    this,
                    url.toString(),
                    product,
                    true,
                    true);
        }
        queries.add(allIssues);
        return queries.toArray(new Query[queries.size()]);
    }

    @Override
    protected BugzillaConfiguration createConfiguration(boolean forceRefresh) {
        KenaiConfiguration kc = new KenaiConfiguration(this, product);
        kc.initialize(this, forceRefresh);
        return kc;
    }

    protected void setCredentials(String user, String password) {
        super.setTaskRepository(getDisplayName(), getUrl(), user, password, null, null, isShortUsernamesEnabled());
    }

    @Override
    public boolean authenticate(String errroMsg) {
        PasswordAuthentication pa = org.netbeans.modules.bugtracking.util.KenaiUtil.getPasswordAuthentication(true);
        if(pa == null) {
            return false;
        }

        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setCredentials(user, new String(password));

        return true;
    }

    @Override
    protected Object[] getLookupObjects() {
        Object[] obj = super.getLookupObjects();
        Object[] obj2 = new Object[obj.length + 1];
        System.arraycopy(obj, 0, obj2, 0, obj.length);
        obj2[obj.length] = kenaiProject;
        return obj2;
    }

    /**
     * Returns the name of the bz product - should be the same as the name of the kenai project that owns this repository
     * @return
     */
    public String getProductName () {
        return product;
    }

    private static String getKenaiUser() {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(false);
        if(pa != null) {
            return pa.getUserName();
        }
        return "";                                                              // NOI18N
    }

    private static String getKenaiPassword() {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(false);
        if(pa != null) {
            return new String(pa.getPassword());
        }
        return "";                                                              // NOI18N
    }

    @Override
    protected QueryParameter[] getSimpleSearchParameters() {
        return new QueryParameter[] {
            new QueryParameter.SimpleQueryParameter("product",          //NOI18N
                    new String[] { product } )
        };
    }
}
