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

package org.netbeans.modules.bugtracking.vcshooks;

import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.vcshooks.HgHookImpl.PushAction;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class VCSHooksConfig {
    private static VCSHooksConfig instance = null;

    private static final String HG_HOOK_COMMENT_FORMAT  = "vcshook.hg_comment_format";
    private static final String SVN_HOOK_COMMENT_FORMAT = "vcshook.svn_comment_format";
    private static final String HG_HOOK_PUSH_           = "vcshook.hg_push_hook_";
    private static final String DELIMITER               = "<=>";
    
    private VCSHooksConfig() { }

    static VCSHooksConfig getInstance() {
        if(instance == null) {
            instance = new VCSHooksConfig();
        }
        return instance;
    }

    Preferences getPreferences() {
        return NbPreferences.forModule(VCSHooksConfig.class);
    }

    void setHgCommentFormat(String value) {
        getPreferences().put(HG_HOOK_COMMENT_FORMAT, value);
    }

    void setSvnCommentFormat(String value) {
        getPreferences().put(SVN_HOOK_COMMENT_FORMAT, value);
    }

    String getHgCommentFormat() {
        return getPreferences().get(HG_HOOK_COMMENT_FORMAT, getDefaultHgFormat());
    }

    String getSvnCommentFormat() {
        return getPreferences().get(SVN_HOOK_COMMENT_FORMAT, getDefaultSvnFormat());
    }

    void setHgPushAction(String changeset, PushAction pushAction) {
        StringBuffer sb = new StringBuffer();
        sb.append(pushAction.getIssueID());
        sb.append(DELIMITER);
        sb.append(pushAction.getMsg());
        sb.append(DELIMITER);
        sb.append((pushAction.isClose() ? "1" : "0"));
        getPreferences().put(HG_HOOK_PUSH_ + changeset,  sb.toString());
    }

    PushAction popHGPushAction(String changeset) {
        String value = getPreferences().get(HG_HOOK_PUSH_ + changeset, null);
        if(value == null) return null;
        String values[] = value.split(DELIMITER);
        getPreferences().remove(HG_HOOK_PUSH_ + changeset);
        return new PushAction(values[0], values[1], values[2].equals("1") ? true : false);
    }

    private String getDefaultHgFormat() {
        return normalizeFormat(new String[] {
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Changeset"),
            "{changeset}\n",
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Author"),
            "{author}\n",
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Date"),
            "{date}\n",
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Message"),
            "{message}"
        });
    }

    private String getDefaultSvnFormat() {
        return normalizeFormat(new String[] {
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Revision"),
            "{revision}\n",
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Author"),
            "{author}\n",
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Date"),
            "{date}\n",
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Message"),
            "{message}"
        });
    }

    private String normalizeFormat(String [] params) {
        int l = 0;
        for (int i = 0; i < params.length; i = i + 2) {
            if(l < params[i].length()) l = params[i].length();
        }
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            ret.append(params[i]);
            StringBuffer s = new StringBuffer();
            for (int j = 0; j < l - params[i].length() + 1; j++) s.append(" ");
            ret.append(s.toString());
            ret.append(params[++i]);
        }
        return ret.toString();
    }
}
