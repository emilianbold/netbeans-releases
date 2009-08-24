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

package org.netbeans.modules.bugtracking.vcs;

import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class VCSHooksConfig {
    private static VCSHooksConfig instance = null;

    private static final String HG_HOOK_RESOLVE              = "vcshook.hg_resolve";         // NOI18N
    private static final String HG_HOOK_LINK                 = "vcshook.hg_link";            // NOI18N
    private static final String HG_HOOK_AFTER_COMMIT         = "vcshook.hg_after_commit";    // NOI18N
    private static final String SVN_HOOK_RESOLVE             = "vcshook.svn_resolve";        // NOI18N
    private static final String SVN_HOOK_LINK                = "vcshook.svn_link";           // NOI18N

    private static final String HG_HOOK_ISSUE_INFO_TEMPLATE  = "vcshook.hg_issue_format";    // NOI18N
    private static final String HG_HOOK_REVISION_TEMPLATE    = "vcshook.hg_comment_format";  // NOI18N
    
    private static final String SVN_HOOK_ISSUE_INFO_TEMPLATE = "vcshook.svn_issue_format";   // NOI18N
    private static final String SVN_HOOK_REVISION_TEMPLATE   = "vcshook.svn_comment_format"; // NOI18N

    private static final String HG_HOOK_PUSH_           = "vcshook.hg_push_hook_";      // NOI18N
    private static final String DELIMITER               = "<=>";                        // NOI18N
    
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

    void setHgIssueInfoTemplate(Format format) {
        getPreferences().put(HG_HOOK_ISSUE_INFO_TEMPLATE, format.toString());
    }

    void setHgRevisionTemplate(Format format) {
        getPreferences().put(HG_HOOK_REVISION_TEMPLATE, format.toString());
    }

    void setSvnIssueInfoTemplate(Format format) {
        getPreferences().put(SVN_HOOK_ISSUE_INFO_TEMPLATE, format.toString());
    }

    void setSvnRevisionTemplate(Format format) {
        getPreferences().put(SVN_HOOK_REVISION_TEMPLATE, format.toString());
    }

    void setSvnResolve(boolean bl) {
        getPreferences().putBoolean(SVN_HOOK_RESOLVE, bl);
    }

    void setSvnLink(boolean bl) {
        getPreferences().putBoolean(SVN_HOOK_LINK, bl);
    }

    void setHgResolve(boolean bl) {
        getPreferences().putBoolean(HG_HOOK_RESOLVE, bl);
    }

    void setHgAfterCommit(boolean bl) {
        getPreferences().putBoolean(HG_HOOK_AFTER_COMMIT, bl);
    }

    void setHgLink(boolean bl) {
        getPreferences().putBoolean(HG_HOOK_LINK, bl);
    }

    boolean getSvnResolve() {
        return getPreferences().getBoolean(SVN_HOOK_RESOLVE, false);
    }

    boolean getSvnLink() {
        return getPreferences().getBoolean(SVN_HOOK_LINK, true);
    }

    boolean getHgResolve() {
        return getPreferences().getBoolean(HG_HOOK_RESOLVE, false);
    }

    boolean getHgLink() {
        return getPreferences().getBoolean(HG_HOOK_LINK, true);
    }

    boolean getHgAfterCommit() {
        return getPreferences().getBoolean(HG_HOOK_AFTER_COMMIT, false);
    }

    Format getHgRevisionTemplate() {
        return getFormat(getPreferences().get(HG_HOOK_REVISION_TEMPLATE, null), getDefaultHgRevisionTemplate());
    }

    Format getHgIssueInfoTemplate() {
        return getFormat(getPreferences().get(HG_HOOK_ISSUE_INFO_TEMPLATE, null), getDefaultIssueInfoTemplate());
    }

    Format getSvnRevisionTemplate() {
        return getFormat(getPreferences().get(SVN_HOOK_REVISION_TEMPLATE, null), getDefaultSvnRevisionTemplate());
    }

    Format getSvnIssueInfoTemplate() {
        return getFormat(getPreferences().get(SVN_HOOK_ISSUE_INFO_TEMPLATE, null), getDefaultIssueInfoTemplate());
    }

    void setHgPushAction(String changeset, PushOperation pushAction) {
        getPreferences().put(HG_HOOK_PUSH_ + changeset,  pushAction.toString());
    }

    PushOperation popHGPushAction(String changeset) {
        String value = getPreferences().get(HG_HOOK_PUSH_ + changeset, null);
        if(value == null) return null;
        String values[] = value.split(DELIMITER);
        getPreferences().remove(HG_HOOK_PUSH_ + changeset);
        return new PushOperation(values[0], values[1], values[2].equals("1") ? true : false); // NOI18N
    }

    static Format getDefaultHgRevisionTemplate() {
        return new Format(false, normalizeFormat(new String[] {
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Changeset"),         // NOI18N
            "{changeset}\n",                                                    // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Author"),            // NOI18N
            "{author}\n",                                                       // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Date"),              // NOI18N
            "{date}\n",                                                         // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Message"),           // NOI18N
            "{message}"                                                         // NOI18N
        }));
    }

    static Format getDefaultIssueInfoTemplate() {
        return new Format(false, NbBundle.getMessage(VCSHooksConfig.class, "LBL_Issue") + "{id} - {summary}");  // NOI18N
    }

    static Format getDefaultSvnRevisionTemplate() {
        return new Format(false, normalizeFormat(new String[] {
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Revision"),          // NOI18N
            "{revision}\n",                                                     // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Author"),            // NOI18N
            "{author}\n",                                                       // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Date"),              // NOI18N
            "{date}\n",                                                         // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Message"),           // NOI18N
            "{message}"                                                         // NOI18N
        }));
    }

    private Format getFormat(String value, Format defaultFormat) {
        Format format;
        if (value == null) {
            format = defaultFormat;
        } else {
            String[] values = value.split(DELIMITER);
            format = new Format(values[0].equals("1"), values[1]);              // NOI18N
        }
        return format;
    }

    private static String normalizeFormat(String [] params) {
        int l = 0;
        for (int i = 0; i < params.length; i = i + 2) {
            if(l < params[i].length()) l = params[i].length();
        }
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            ret.append(params[i]);
            StringBuffer s = new StringBuffer();
            for (int j = 0; j < l - params[i].length() + 1; j++) s.append(" "); // NOI18N
            ret.append(s.toString());
            ret.append(params[++i]);
        }
        return ret.toString();
    }

    static class PushOperation {
        private final String issueID;
        private final String msg;
        private final boolean close;
        public PushOperation(String issueID, String msg, boolean close) {
            this.issueID = issueID;
            this.msg = msg;
            this.close = close;
        }
        public String getIssueID() {
            return issueID;
        }
        public boolean isClose() {
            return close;
        }
        public String getMsg() {
            return msg;
        }
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(getIssueID());
            sb.append(DELIMITER);
            sb.append(getMsg());
            sb.append(DELIMITER);
            sb.append(isClose() ? "1" : "0");                                   // NOI18N
            return sb.toString();
        }
    }

    static class Format {
        private boolean above;
        private String format;
        public Format(boolean above, String format) {
            this.above = above;
            this.format = format;
        }
        public boolean isAbove() {
            return above;
        }
        public String getFormat() {
            return format;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(above ? "1" : "0");                                       // NOI18N
            sb.append(DELIMITER);
            sb.append(format);
            return sb.toString();
        }

    }
}
