/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.c2c.tasks.issue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.c2c.tasks.C2C;
import org.netbeans.modules.c2c.tasks.repository.C2CRepository;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class C2CIssue {

    private TaskData data;
    private final C2CRepository repository;
    private final PropertyChangeSupport support;

    private static final SimpleDateFormat MODIFIED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // NOI18N
    private static final SimpleDateFormat CREATED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");       // NOI18N
    
    public C2CIssue(TaskData data, C2CRepository repo) {
        this.data = data;
        this.repository = repo;
        support = new PropertyChangeSupport(this);
    }
    
    /**
     * Determines the issue display name depending on the issue new state
     * @param td
     * @return 
     */
    public static String getDisplayName(TaskData td) {
        return td.isNew() ?
                NbBundle.getMessage(C2CIssue.class, "CTL_NewIssue") : // NOI18N
                NbBundle.getMessage(C2CIssue.class, "CTL_Issue", new Object[] {getID(td), getSummary(td)}); // NOI18N
    }

    /**
     * Determines the issue id
     * @param td
     * @return 
     */
    public String getID() {
        return getID(data);
    }
       
    /**
     * determines the given TaskData id
     * @param td
     * @return 
     */
    public static String getID(TaskData td) {
        return td.getTaskId();
    }

    /**
     * Returns the id from the given taskData or null if taskData.isNew()
     * @param taskData
     * @return id or null
     */
    public static String getSummary(TaskData taskData) {
        if(taskData.isNew()) {
            return null;
        }
        return getFieldValue(IssueField.SUMMARY, taskData);
    }    

    public void setSeen(boolean b) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setTaskData(TaskData taskData) {
        assert !taskData.isPartial();
        data = taskData;
        
        // XXX
//        attributes = null; // reset
//        availableOperations = null;
        C2C.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
//        XXX        ((C2CIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
//             XXX   refreshViewData(false);
            }
        });
    }

    public String getRecentChanges() {
        return ""; // XXX 
    }

    public Date getLastModifyDate() {
        String value = getFieldValue(IssueField.MODIFIED);
        if(value != null && !value.trim().equals("")) {
            try {
                return MODIFIED_DATE_FORMAT.parse(value);
            } catch (ParseException ex) {
                C2C.LOG.log(Level.WARNING, value, ex);
            }
        }
        return null;
    }

    public long getLastModify() {
        Date lastModifyDate = getLastModifyDate();
        if(lastModifyDate != null) {
            return lastModifyDate.getTime();
        } else {
            return -1;
        }
    }

    public Date getCreatedDate() {
        String value = getFieldValue(IssueField.CREATED);
        if(value != null && !value.trim().equals("")) {
            try {
                return CREATED_DATE_FORMAT.parse(value);
            } catch (ParseException ex) {
                C2C.LOG.log(Level.WARNING, value, ex);
            }
        }
        return null;
    }

    public long getCreated() {
        Date createdDate = getCreatedDate();
        if (createdDate != null) {
            return createdDate.getTime();
        } else {
            return -1;
        }
    }

    public Map<String, String> getAttributes() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    /**************************************************************************
     * private
     **************************************************************************/

    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    public String getFieldValue(IssueField f) {
        return getFieldValue(f, data);
    }

    private static String getFieldValue(IssueField f, TaskData taskData) {
        if(f.isSingleFieldAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, f.getKey());
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
    private static String listValues(TaskAttribute a) {
        if(a == null) {
            return "";                                                          // NOI18N
        }
        StringBuilder sb = new StringBuilder();
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

//    void setFieldValue(IssueField f, String value) {
//        if(f.isReadOnly()) {
//            assert false : "can't set value into IssueField " + f.getKey();       // NOI18N
//            return;
//        }
//        TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
//        if(a == null) {
//            a = new TaskAttribute(data.getRoot(), f.getKey());
//        }
//        if(f == IssueField.PRODUCT) {
//            handleProductChange(a);
//        }
//        Bugzilla.LOG.log(Level.FINER, "setting value [{0}] on field [{1}]", new Object[]{value, f.getKey()}) ;
//        a.setValue(value);
//    }
//
//    void setFieldValues(IssueField f, List<String> ccs) {
//        TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
//        if(a == null) {
//            a = new TaskAttribute(data.getRoot(), f.getKey());
//        }
//        a.setValues(ccs);
//    }

    public List<String> getFieldValues(IssueField f) {
        if(f.isSingleFieldAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
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

    /**
     * Notify listeners on this issue that its data were changed
     */
    private void fireDataChanged() {
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_REFRESHED, null, null);
    }
}
