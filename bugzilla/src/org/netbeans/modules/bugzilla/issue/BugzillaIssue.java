/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.issue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.commands.BugzillaCommand;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 * @author Jan Stola
 */
public class BugzillaIssue extends Issue {

    public static final String RESOLVE_FIXED = "FIXED";                         // NOI18N
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";               // NOI18N
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private static final int SHORTENED_SUMMARY_LENGTH = 22;

    private TaskData data;
    private BugzillaRepository repository;

    private IssueController controller;
    private IssueNode node;

    static final String LABEL_NAME_ID           = "bugzilla.issue.id";          // NOI18N
    static final String LABEL_NAME_SEVERITY     = "bugzilla.issue.severity";    // NOI18N
    static final String LABEL_NAME_PRIORITY     = "bugzilla.issue.priority";    // NOI18N
    static final String LABEL_NAME_STATUS       = "bugzilla.issue.status";      // NOI18N
    static final String LABEL_NAME_RESOLUTION   = "bugzilla.issue.resolution";  // NOI18N
    static final String LABEL_NAME_SUMMARY      = "bugzilla.issue.summary";     // NOI18N
    static final String LABEL_NAME_ASSIGNED_TO  = "bugzilla.issue.assigned";    // NOI18N

    /**
     * Issue wasn't seen yet
     */
    static final int FIELD_STATUS_IRELEVANT = -1;

    /**
     * Field wasn't changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_UPTODATE = 1;

    /**
     * Field has a value in oposite to the last time when it was seen
     */
    static final int FIELD_STATUS_NEW = 2;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_MODIFIED = 4;
    private Map<String, String> seenAtributes;

    enum IssueField {
        SUMMARY(BugzillaAttribute.SHORT_DESC.getKey(), "LBL_SUMMARY"),
        STATUS(TaskAttribute.STATUS, "LBL_STATUS"),
        PRIORITY(BugzillaAttribute.PRIORITY.getKey(), "LBL_PRIORITY"),
        RESOLUTION(TaskAttribute.RESOLUTION, "LBL_RESOLUTION"),
        PRODUCT(BugzillaAttribute.PRODUCT.getKey(), "LBL_PRODUCT"),
        COMPONENT(BugzillaAttribute.COMPONENT.getKey(), "LBL_COMPONENT"),
        VERSION(BugzillaAttribute.VERSION.getKey(), "LBL_VERSION"),
        PLATFORM(BugzillaAttribute.REP_PLATFORM.getKey(), "LBL_PLATFORM"),
        OS(BugzillaAttribute.OP_SYS.getKey(), "LBL_OS"),
        MILESTONE(BugzillaAttribute.TARGET_MILESTONE.getKey(), "LBL_MILESTONE"),
        REPORTER(BugzillaAttribute.REPORTER.getKey(), "LBL_REPORTER"),
        REPORTER_NAME(BugzillaAttribute.REPORTER_NAME.getKey(), "LBL_REPORTER_NAME"),
        ASSIGNED_TO(BugzillaAttribute.ASSIGNED_TO.getKey(), "LBL_ASSIGNED_TO"),
        ASSIGNED_TO_NAME(BugzillaAttribute.ASSIGNED_TO_NAME.getKey(), "LBL_ASSIGNED_TO_NAME"),
        QA_CONTACT(BugzillaAttribute.QA_CONTACT.getKey(), "LBL_QA_CONTACT"),
        QA_CONTACT_NAME(BugzillaAttribute.QA_CONTACT_NAME.getKey(), "LBL_QA_CONTACT_NAME"),
        DEPENDS_ON(BugzillaAttribute.DEPENDSON.getKey(), "LBL_DEPENDS_ON"),
        BLOCKS(BugzillaAttribute.BLOCKED.getKey(), "LBL_BLOCKS"),
        URL(BugzillaAttribute.BUG_FILE_LOC.getKey(), "LBL_URL"),
        KEYWORDS(BugzillaAttribute.KEYWORDS.getKey(), "LBL_KEYWORDS"),
        SEVERITY(BugzillaAttribute.BUG_SEVERITY.getKey(), "LBL_SEVERITY"),
        DESCRIPTION(BugzillaAttribute.LONG_DESC.getKey(), "LBL_DESCRIPTION"),
        CREATION(TaskAttribute.DATE_CREATION, "LBL_CREATION"),
        CC(BugzillaAttribute.CC.getKey(), "LBL_CC"),
        MODIFICATION(TaskAttribute.DATE_MODIFICATION, null),
        NEWCC(BugzillaAttribute.NEWCC.getKey(), null),
        REMOVECC(BugzillaAttribute.REMOVECC.getKey(), null),
        COMMENT_COUNT(TaskAttribute.TYPE_COMMENT, null, false),
        ATTACHEMENT_COUNT(TaskAttribute.TYPE_ATTACHMENT, null, false);

        private final String key;
        private final String displayNameKey;
        private boolean singleAttribute;

        IssueField(String key, String displayNameKey) {
            this(key, displayNameKey, true);
        }

        IssueField(String key, String displayNameKey, boolean singleAttribute) {
            this.key = key;
            this.singleAttribute = singleAttribute;
            this.displayNameKey = displayNameKey;
        }

        public String getKey() {
            return key;
        }
        public boolean isSingleAttribute() {
            return singleAttribute;
        }
        public boolean isReadOnly() {
            return !singleAttribute;
        }

        public String getDisplayName() {
            assert displayNameKey != null; // shouldn't be called for a field with a null display name
            return NbBundle.getMessage(BugzillaIssue.class, displayNameKey);
        }

    }

    private Map<String, String> attributes;

    public BugzillaIssue(TaskData data, BugzillaRepository repo) {
        super(repo);
        this.data = data;
        this.repository = repo;
    }

    @Override
    public boolean isNew() {
        return data == null || data.isNew();
    }

    void opened() {
        if(Bugzilla.LOG.isLoggable(Level.FINE)) Bugzilla.LOG.log(Level.FINE, "issue {0} open start", new Object[] {getID()});
        if(!data.isNew()) {
            // 1.) to get seen attributes makes no sense for new issues
            // 2.) set seenAtributes on issue open, before its actuall
            //     state is written via setSeen().
            seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
        }
        String refresh = System.getProperty("org.netbeans.modules.bugzilla.noIssueRefresh"); // NOI18N
        if(refresh != null && refresh.equals("true")) {                                      // NOI18N
            return;
        }
        repository.scheduleForRefresh(getID());
        if(Bugzilla.LOG.isLoggable(Level.FINE)) Bugzilla.LOG.log(Level.FINE, "issue {0} open finish", new Object[] {getID()});
    }

    void closed() {
        if(Bugzilla.LOG.isLoggable(Level.FINE)) Bugzilla.LOG.log(Level.FINE, "issue {0} close start", new Object[] {getID()});
        repository.stopRefreshing(getID());
        seenAtributes = null;
        if(Bugzilla.LOG.isLoggable(Level.FINE)) Bugzilla.LOG.log(Level.FINE, "issue {0} close finish", new Object[] {getID()});
    }

    @Override
    public String getDisplayName() {
        return data.isNew() ?
                NbBundle.getMessage(BugzillaIssue.class, "CTL_NewIssue") : // NOI18N
                NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue", new Object[] {getID(), getSummary()}); // NOI18N
    }

    @Override
    public String getShortenedDisplayName() {
        if (data.isNew()) {
            return getDisplayName();
        }

        String shortSummary = TextUtils.shortenText(getSummary(),
                                                    2,    //try at least 2 words
                                                    SHORTENED_SUMMARY_LENGTH);
        return NbBundle.getMessage(BugzillaIssue.class,
                                   "CTL_Issue",                         //NOI18N
                                   new Object[] {getID(), shortSummary});
    }

    @Override
    public String getTooltip() {
        return getDisplayName();
    }

    public static ColumnDescriptor[] getColumnDescriptors(BugzillaRepository repository) {
        ResourceBundle loc = NbBundle.getBundle(BugzillaIssue.class);
        BugzillaConfiguration bc = repository.getConfiguration();
        JTable t = new JTable();
        return new ColumnDescriptor[] {
            new ColumnDescriptor<String>(LABEL_NAME_ID, String.class,
                                              loc.getString("CTL_Issue_ID_Title"),              // NOI18N
                                              loc.getString("CTL_Issue_ID_Desc"),               // NOI18N
                                              BugtrackingUtil.getColumnWidthInPixels(7, t)),
            new ColumnDescriptor<String>(LABEL_NAME_SUMMARY, String.class,
                                              loc.getString("CTL_Issue_Summary_Title"),         // NOI18N
                                              loc.getString("CTL_Issue_Summary_Desc")),         // NOI18N
            new ColumnDescriptor<String>(LABEL_NAME_SEVERITY, String.class,
                                              loc.getString("CTL_Issue_Severity_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Severity_Desc"),         // NOI18N
                                              BugtrackingUtil.getLongestWordWidth(
                                                loc.getString("CTL_Issue_Severity_Title"),      // NOI18N
                                                bc.getSeverities(), t)),
            new ColumnDescriptor<String>(LABEL_NAME_PRIORITY, String.class,
                                              loc.getString("CTL_Issue_Priority_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Priority_Desc"),         // NOI18N
                                              BugtrackingUtil.getLongestWordWidth(
                                                loc.getString("CTL_Issue_Priority_Title"),
                                                bc.getPriorities(), t, true)),
            new ColumnDescriptor<String>(LABEL_NAME_STATUS, String.class,
                                              loc.getString("CTL_Issue_Status_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Status_Desc"),           // NOI18N
                                              BugtrackingUtil.getLongestWordWidth(
                                                loc.getString("CTL_Issue_Status_Title"),        // NOI18N
                                                bc.getStatusValues(), t)),
            new ColumnDescriptor<String>(LABEL_NAME_RESOLUTION, String.class,
                                              loc.getString("CTL_Issue_Resolution_Title"),      // NOI18N
                                              loc.getString("CTL_Issue_Resolution_Desc"),       // NOI18N
                                              BugtrackingUtil.getLongestWordWidth(
                                                loc.getString("CTL_Issue_Resolution_Title"),    // NOI18N
                                                bc.getResolutions(), t)),
            new ColumnDescriptor<String>(LABEL_NAME_ASSIGNED_TO, String.class,
                                              loc.getString("CTL_Issue_Assigned_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Assigned_Desc"),         // NOI18N
                                              BugtrackingUtil.getColumnWidthInPixels(20, t))
        };
    }

    @Override
    public BugtrackingController getController() {
        if (controller == null) {
            controller = new IssueController(this);
        }
        return controller;
    }

    @Override
    public String toString() {
        String str = getID() + " : "  + getSummary(); // NOI18N
        return str;
    }

    @Override
    public IssueNode getNode() {
        if(node == null) {
            node = createNode();
        }
        return node;
    }

    @Override
    public Map<String, String> getAttributes() {
        if(attributes == null) {
            attributes = new HashMap<String, String>();
            String value;
            for (IssueField field : IssueField.values()) {
                value = getFieldValue(field);
                if(value != null && !value.trim().equals("")) {                 // NOI18N
                    attributes.put(field.key, value);
                }
            }
        }
        return attributes;
    }

    @Override
    public void setSeen(boolean seen) throws IOException {
        super.setSeen(seen);
    }

    @Override
    public String getRecentChanges() {
        if(wasSeen()) {
            return "";                                                          // NOI18N
        }
        int status = repository.getIssueCache().getStatus(getID());
        if(status == Issue.ISSUE_STATUS_NEW) {
            return NbBundle.getMessage(BugzillaIssue.class, "LBL_NEW_STATUS");
        } else if(status == Issue.ISSUE_STATUS_MODIFIED) {
            List<IssueField> changedFields = new ArrayList<IssueField>();
            assert getSeenAttributes() != null;
            for (IssueField f : IssueField.values()) {
                switch(f) {
                    case MODIFICATION :
                    case REPORTER_NAME :
                    case QA_CONTACT_NAME :
                    case ASSIGNED_TO_NAME :
                        continue;
                }
                String value = getFieldValue(f);
                String seenValue = getSeenValue(f);
                if(!value.trim().equals(seenValue)) {
                    changedFields.add(f);
                }
            }
            int changedCount = changedFields.size();
            if(changedCount == 1) {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_SUMMARY_CHANGED_STATUS");
                            break;
                        case CC :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CC_FIELD_CHANGED_STATUS");
                            break;
                        case KEYWORDS :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_KEYWORDS_CHANGED_STATUS");
                            break;
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_DEPENDENCE_CHANGED_STATUS");
                            break;
                        case COMMENT_COUNT :
                            String value = getFieldValue(changedField);
                            String seenValue = getSeenValue(changedField);
                            if(seenValue.equals("")) {
                                seenValue = "0";
                            }
                            int count = 0;
                            try {
                                count = Integer.parseInt(value) - Integer.parseInt(seenValue);
                            } catch(NumberFormatException ex) {
                                Bugzilla.LOG.log(Level.WARNING, ret, ex);
                            }
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_COMMENTS_CHANGED", new Object[] {count});
                            break;
                        case ATTACHEMENT_COUNT :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_ATTACHMENTS_CHANGED");
                            break;
                        default :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGED_TO", new Object[] {changedField.getDisplayName(), getFieldValue(changedField)});
                    }
                }
                return ret;
            } else {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_SUMMARY", new Object[] {changedCount});
                            break;
                        case PRIORITY :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_PRIORITY", new Object[] {changedCount});
                            break;
                        case SEVERITY :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_SEVERITY", new Object[] {changedCount});
                            break;
                        case PRODUCT :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_PRODUCT", new Object[] {changedCount});
                            break;
                        case COMPONENT :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_COMPONENT", new Object[] {changedCount});
                            break;
                        case PLATFORM :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_PLATFORM", new Object[] {changedCount});
                            break;
                        case VERSION :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_VERSION", new Object[] {changedCount});
                            break;
                        case MILESTONE :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_MILESTONE", new Object[] {changedCount});
                            break;
                        case KEYWORDS :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_KEYWORDS", new Object[] {changedCount});
                            break;
                        case URL :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_URL", new Object[] {changedCount});
                            break;
                        case ASSIGNED_TO :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_ASSIGNEE", new Object[] {changedCount});
                            break;
                        case QA_CONTACT :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCL_QA_CONTACT", new Object[] {changedCount});
                            break;
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES_INCLUSIVE_DEPENDENCE", new Object[] {changedCount});
                            break;
                        default :
                            ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGES", new Object[] {changedCount});
                    }
                    return ret;
                }
            }
        }
        return "";                                                              // NOI18N
    }

    /**
     * Returns the id from the given taskData or null if taskData.isNew()
     * @param taskData
     * @return id or null
     */
    public static String getID(TaskData taskData) {
        if(taskData.isNew()) {
            return null;
        }
        return taskData.getTaskId();
    }

    TaskRepository getTaskRepository() {
        return repository.getTaskRepository();
    }

    BugzillaRepository getBugzillaRepository() {
        return repository;
    }

    public String getID() {
        return getID(data);
    }

    public String getSummary() {
        return getFieldValue(IssueField.SUMMARY);
    }

    public void setTaskData(TaskData taskData) {
        assert !taskData.isPartial();
        data = taskData;
        attributes = null; // reset
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                ((BugzillaIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
            }
        });
    }

    TaskData getTaskData() {
        return data;
    }

    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    String getFieldValue(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, f.key);
            // returning 0 would set status MODIFIED instead of NEW
            return "" + ( attrs != null && attrs.size() > 0 ?  attrs.size() : ""); // NOI18N
        }
    }

    /**
     * Returns a comma separated list created
     * from the values returned by TaskAttribute.getValues()
     *
     * @param a
     * @return
     */
    private String listValues(TaskAttribute a) {
        if(a == null) {
            return "";                                                          // NOI18N
        }
        StringBuffer sb = new StringBuffer();
        List<String> l = a.getValues();
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            sb.append(s);
            if(i < l.size() -1) {
                sb.append(",");                                                 // NOI18N
            }
        }
        return sb.toString();
    }

    void setFieldValue(IssueField f, String value) {
        if(f.isReadOnly()) {
            assert false : "can't set value into IssueField " + f.name();       // NOI18N
            return;
        }
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValue(value);
    }

    List<String> getFieldValues(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
            if(a != null) {
                return a.getValues();
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> ret = new ArrayList<String>();
            ret.add(getFieldValue(f));
            return ret;
        }
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValues(ccs);
    }

    /**
     * Returns a status value for the given field<br>
     * <ul>
     *  <li>{@link #FIELD_STATUS_IRELEVANT} - issue wasn't seen yet
     *  <li>{@link #FIELD_STATUS_UPTODATE} - field value wasn't changed
     *  <li>{@link #FIELD_STATUS_MODIFIED} - field value was changed
     *  <li>{@link #FIELD_STATUS_NEW} - field has a value for the first time since it was seen
     * </ul>
     * @param f IssueField
     * @return a status value
     */
    int getFieldStatus(IssueField f) {
        String seenValue = getSeenValue(f);
        if(seenValue.equals("") && !seenValue.equals(getFieldValue(f))) {       // NOI18N
            return FIELD_STATUS_NEW;
        } else if (!seenValue.equals(getFieldValue(f))) {
            return FIELD_STATUS_MODIFIED;
        }
        return FIELD_STATUS_UPTODATE;
    }

    private IssueNode createNode() {
        return new BugzillaIssueNode(this);
    }

    void resolve(String resolution) {
        assert !data.isNew();

        String value = getFieldValue(IssueField.STATUS);
        if(!value.equals("RESOLVED")) {                                         // NOI18N
            setOperation(BugzillaOperation.resolve);
            TaskAttribute rta = data.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.resolve.getInputId());
            if(ta != null) { // ta can be null when changing status from CLOSED to RESOLVED
                ta.setValue(resolution);
            }
        }
    }

    void accept() {
        setOperation(BugzillaOperation.accept);
    }

    void duplicate(String id) {
        setOperation(BugzillaOperation.duplicate);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.duplicate.getInputId());
        ta.setValue(id);
    }

    boolean canReassign() {
        boolean oldRepository = (getBugzillaRepository().getConfiguration().getInstalledVersion().compareMajorMinorOnly(BugzillaVersion.BUGZILLA_3_2) < 0);
        if (oldRepository) {
            TaskAttribute rta = data.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.reassign.getInputId());
            return (ta != null);
        } else {
            return true;
        }
    }

    void reassign(String user) {
        setOperation(BugzillaOperation.reassign);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.reassign.getInputId());
        if(ta != null) ta.setValue(user);
        ta = rta.getMappedAttribute(BugzillaAttribute.ASSIGNED_TO.getKey());
        if(ta != null) ta.setValue(user);
    }

    void verify() {
        setOperation(BugzillaOperation.verify);
    }

    void close() {
        setOperation(BugzillaOperation.close);
    }

    void reopen() {
        setOperation(BugzillaOperation.reopen);
    }

    private void setOperation(BugzillaOperation operation) {
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue(operation.name());
    }

    Attachment[] getAttachments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    void addAttachment(final File file, final String comment, final String desc, String contentType, final boolean patch) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        final FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(file);
        if (contentType == null) {
            String ct = FileUtil.getMIMEType(FileUtil.toFileObject(file));
            if ((ct != null) && (!"content/unknown".equals(ct))) { // NOI18N
                contentType = ct;
            } else {
                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file.getName());
            }
        }
        attachmentSource.setContentType(contentType);

        final TaskAttribute attAttribute = new TaskAttribute(data.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
        TaskAttributeMapper mapper = attAttribute.getTaskData().getAttributeMapper();
        TaskAttribute a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
        a.setValue(desc);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH);
        mapper.setBooleanValue(a, patch);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE);
        a.setValue(contentType);

        BugzillaCommand cmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                refresh();
                Bugzilla.getInstance().getClient(repository)
                        .postAttachment(
                            getID(),
                            comment,
                            attachmentSource,
                            attAttribute,
                            new NullProgressMonitor());
                refresh(); // XXX to much refresh - is there no other way?
            }
        };
        repository.getExecutor().execute(cmd);
    }

    Comment[] getComments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        if (attrs == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>();
        for (TaskAttribute taskAttribute : attrs) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }

    // XXX carefull - implicit refresh
    public void addComment(String comment, boolean close) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        if(comment == null && !close) {
            return;
        }
        refresh();

        // resolved attrs
        if(close) {
            resolve(RESOLVE_FIXED); // XXX constant?
        }
        if(comment != null) {
            addComment(comment);
        }

        BugzillaCommand submitCmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                submitAndRefresh();
            }
        };
        repository.getExecutor().execute(submitCmd);
    }

    public void addComment(String comment) {
        if(comment != null) {
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
        }
    }

    @Override
    public void attachPatch(File file, String description) {
        addAttachment(file, null, description, null, true);
    }

    boolean submitAndRefresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean wasNew = data.isNew();
        final boolean wasSeenAlready = wasNew || repository.getIssueCache().wasSeen(getID());
        final RepositoryResponse[] rr = new RepositoryResponse[1];
        final BugzillaCommand submitCmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                // submit
                rr[0] = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getTaskRepository(), data, null, new NullProgressMonitor());
                // XXX evaluate rr
            }
        };
        repository.getExecutor().execute(submitCmd);

        BugzillaCommand refreshCmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                if (!wasNew) {
                    refresh();
                } else {
                    if(!submitCmd.hasFailed()) {
                        assert rr[0] != null;
                        refresh(rr[0].getTaskId(), true);
                    }
                }
            }
        };
        repository.getExecutor().execute(refreshCmd);

        if(submitCmd.hasFailed()) {
            return false;
        }

        // it was the user who made the changes, so preserve the seen status if seen already
        if (wasSeenAlready) {
            try {
                repository.getIssueCache().setSeen(getID(), true);
                // it was the user who made the changes, so preserve the seen status if seen already
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
        }
        if(wasNew) {
            // a new issue was created -> refresh all queries
            repository.refreshAllQueries();
        }

        try {
            seenAtributes = null;
            setSeen(true);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public boolean refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        return refresh(getID(), false);
    }

    public boolean refresh(String id, boolean cacheThisIssue) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        try {
            TaskData td = BugzillaUtil.getTaskData(repository, id);
            if(td == null) {
                return false;
            }
            getBugzillaRepository().getIssueCache().setIssueData(id, td, this); // XXX
            if (controller != null) {
                controller.refreshViewData();
            }
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private Map<String, String> getSeenAttributes() {
        if(seenAtributes == null) {
            seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
            if(seenAtributes == null) {
                seenAtributes = new HashMap<String, String>();
            }
        }
        return seenAtributes;
    }

    private String getSeenValue(IssueField f) {
        Map<String, String> attr = getSeenAttributes();
        String seenValue = attr != null ? attr.get(f.key) : null;
        if(seenValue == null) {
            seenValue = "";                                                     // NOI18N
        }
        return seenValue;
    }

    private String getMappedValue(TaskAttribute a, String key) {
        TaskAttribute ma = a.getMappedAttribute(key);
        if(ma != null) {
            return ma.getValue();
        }
        return null;
    }

    class Comment {
        private final Date when;
        private final String author;
        private final String authorName;
        private final Long number;
        private final String text;

        public Comment(TaskAttribute a) {
            Date d = null;
            try {
                String s = getMappedValue(a, TaskAttribute.COMMENT_DATE);
                if(s != null && !s.trim().equals("")) {                         // NOI18N
                    d = CC_DATE_FORMAT.parse(s);
                }
            } catch (ParseException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
            when = d;
            TaskAttribute authorAttr = a.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR);
            if (authorAttr != null) {
                author = authorAttr.getValue();
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                authorName = nameAttr != null ? nameAttr.getValue() : null;
            } else {
                author = authorName = null;
            }
            String n = getMappedValue(a, TaskAttribute.COMMENT_NUMBER);
            number = n != null ? Long.parseLong(n) : null;
            text = getMappedValue(a, TaskAttribute.COMMENT_TEXT);
        }

        public Long getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }

        public Date getWhen() {
            return when;
        }

        public String getAuthor() {
            return author;
        }

        public String getAuthorName() {
            return authorName;
        }
    }

    class Attachment {
        private final String desc;
        private final String filename;
        private final String author;
        private final Date date;
        private final String id;
        private String contentType;
        private String isDeprected;
        private String size;
        private String isPatch;
        private String url;


        public Attachment(TaskAttribute ta) {
            id = ta.getValue();
            Date d = null;
            try {
                String s = getMappedValue(ta, TaskAttribute.ATTACHMENT_DATE);
                if(s != null && !s.trim().equals("")) {                         // NOI18N
                    d = CC_DATE_FORMAT.parse(s);
                }
            } catch (ParseException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
            date = d;
            filename = getMappedValue(ta, TaskAttribute.ATTACHMENT_FILENAME);
            desc = getMappedValue(ta, TaskAttribute.ATTACHMENT_DESCRIPTION);

            String who = null;
            TaskAttribute authorAttr = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR);
            if(authorAttr != null) {
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                who = nameAttr != null ? nameAttr.getValue() : null;
            }
            if ( ((who == null) || who.trim().equals("")) && authorAttr != null) { // NOI18N
                who = authorAttr.getValue();
            }
            author = who;
            contentType = getMappedValue(ta, TaskAttribute.ATTACHMENT_CONTENT_TYPE);
            isDeprected = getMappedValue(ta, TaskAttribute.ATTACHMENT_IS_DEPRECATED);
            isPatch = getMappedValue(ta, TaskAttribute.ATTACHMENT_IS_PATCH);
            size = getMappedValue(ta, TaskAttribute.ATTACHMENT_SIZE);
            url = getMappedValue(ta, TaskAttribute.ATTACHMENT_URL);
        }

        public String getAuthor() {
            return author;
        }

        public Date getDate() {
            return date;
        }

        public String getDesc() {
            return desc;
        }

        public String getFilename() {
            return filename;
        }

        public String getContentType() {
            return contentType;
        }

        public String getId() {
            return id;
        }

        public String getIsDeprected() {
            return isDeprected;
        }

        public String getIsPatch() {
            return isPatch;
        }

        public String getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        public void getAttachementData(final OutputStream os) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
            BugzillaCommand cmd = new BugzillaCommand() {
                @Override
                public void execute() throws CoreException, IOException, MalformedURLException {
                    Bugzilla.getInstance().getClient(repository).getAttachmentData(id, os, new NullProgressMonitor());
                }
            };
            repository.getExecutor().execute(cmd);
        }
    }

}
