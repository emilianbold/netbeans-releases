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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.util.Context;

import java.awt.*;
import java.io.*;

/**
 * Top component of the Versioning view.
 *
 * @author Maros Sandor
 */
public class CvsSynchronizeTopComponent extends TopComponent implements Externalizable {

    private static final long serialVersionUID = 1L;    
    
    private SynchronizePanel        syncPanel;
    private Context                 context;
    private String                  contentTitle;
    private String                  branchTitle;
    private long                    lastUpdateTimestamp;
    
    private static CvsSynchronizeTopComponent instance;

    public CvsSynchronizeTopComponent() {
        putClientProperty("SlidingName", NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_Title")); //NOI18N 
        setName(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_Title"));
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/window-versioning.png", true));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "ACSD_Synchronize_TopComponent"));
        syncPanel = new SynchronizePanel(this);
        add(syncPanel);
        setFocusable(true);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    protected void componentActivated() {
        updateTitle();
        syncPanel.focus();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(context);
        out.writeObject(contentTitle);
        out.writeLong(lastUpdateTimestamp);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        context = (Context) in.readObject();
        contentTitle = (String) in.readObject();
        lastUpdateTimestamp = in.readLong();
        syncPanel.deserialize();
    }

    protected void componentOpened() {
        super.componentOpened();
        refreshContent();
    }

    protected void componentClosed() {
        super.componentClosed();
    }

    private void refreshContent() {
        if (syncPanel == null) return;  // the component is not showing => nothing to refresh
        updateTitle();
        syncPanel.setContext(context);        
    }

    /**
     * Sets the 'content' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     * 
     * @param contentTitle a new content title, e.g. "2 projects"
     */ 
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        updateTitle();
    }

    /**
     * Sets the 'branch' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     * 
     * @param branchTitle a new content title, e.g. "release40" branch
     */ 
    void setBranchTitle(String branchTitle) {
        this.branchTitle = branchTitle;
        updateTitle();
    }
    
    public void contentRefreshed(long timestamp) {
        lastUpdateTimestamp = timestamp;
        updateTitle();
    }
    
    private void updateTitle() {
        String age = computeAge(System.currentTimeMillis() - lastUpdateTimestamp);
        if (contentTitle == null) {
            setName(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_Title")); //NOI18N
        } else {
            if (branchTitle == null) {
                setName(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_MultiTitle", contentTitle, age));
            } else {
                setName(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_Title_ContentBranch", contentTitle, branchTitle, age));
            }
        }
    }

    String getContentTitle() {
        return contentTitle;
    }

    private String computeAge(long l) {
        if (lastUpdateTimestamp == 0) {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeUnknown");
        } else if (l < 1000) { // 1000 equals 1 second
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeCurrent");
        } else if (l < 2000) { // age between 1 and 2 seconds
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeOneSecond");
        } else if (l < 60000) { // 60000 equals 1 minute
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeSeconds", Long.toString(l / 1000));
        } else if (l < 120000) { // age between 1 and 2 minutes
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeOneMinute");
        } else if (l < 3600000) { // 3600000 equals 1 hour
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeMinutes", Long.toString(l / 60000));
        } else if (l < 7200000) { // age between 1 and 2 hours
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeOneHour");
        } else if (l < 86400000) { // 86400000 equals 1 day
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeHours", Long.toString(l / 3600000));
        } else if (l < 172800000) { // age between 1 and 2 days
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeOneDay");
        } else {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeDays", Long.toString(l / 86400000));
        }
    }

    public static synchronized CvsSynchronizeTopComponent getInstance() {
        if (instance == null) {
            instance = (CvsSynchronizeTopComponent) WindowManager.getDefault().findTopComponent("synchronize"); // NOI18N
            if (instance == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find Versioning component")); // NOI18N
                instance = new CvsSynchronizeTopComponent();
            }
        }
    
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }

    /**
     * Programmatically invokes the Refresh action.
     */ 
    public void performRefreshAction() {
        syncPanel.performRefreshAction();
    }

    public static final class ReadResolver implements java.io.Serializable {
        public Object readResolve() {
            synchronized(CvsSynchronizeTopComponent.class) {
                if(instance == null) {
                    instance = new CvsSynchronizeTopComponent();
                }
                return instance;
            }
        }
    }

    /**
     * Sets files/folders the user wants to synchronize.
     * They are typically activated (selected) nodes.
     * It cancels refresh task serving previous context.
     * 
     * @param ctx new context of the Versioning view.
     * <code>null</code> for preparation phase then it must
     * be followed by real context on preparation phase termination.
     */
    public void setContext(Context ctx) {
        syncPanel.cancelRefresh();
        if (ctx == null) {
            setName(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "BK1001"));
            setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            context = ctx;
            setBranchTitle(null);
            refreshContent();
        }
        setToolTipText(getContextFilesList(ctx, NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_Title")));            
    }

    private String getContextFilesList(Context ctx, String def) {
        if (ctx == null || ctx.getFiles().length == 0) return def;
        StringBuilder sb = new StringBuilder(200);
        sb.append("<html>"); // NOI18N
        for (File file : ctx.getFiles()) {
            sb.append(file.getAbsolutePath());
            sb.append("<br>"); // NOI18N
        }
        sb.delete(sb.length() - 4, Integer.MAX_VALUE);
        return sb.toString();
    }
    
    /** Tests whether it shows some content. */
    public boolean hasContext() {
        return context != null && context.getFiles().length > 0;
    }

    protected String preferredID() {
        return "synchronize";    // NOI18N
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
}
