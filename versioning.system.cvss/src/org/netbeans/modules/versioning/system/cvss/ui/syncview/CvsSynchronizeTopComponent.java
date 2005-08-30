/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;

import java.awt.BorderLayout;
import java.io.*;
import java.util.*;

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
        setName(NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_Title")); //NOI18N
        setIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/versioning/ui/panels/resources/tc_sync.png"));  //NOI18N        
//        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_DOWN_MASK), "discard"); //NOI18N        
//        getActionMap().put("discard", null);//NOI18N
        setLayout(new BorderLayout());
        syncPanel = new SynchronizePanel(this);
        add(syncPanel);
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
    
    public void contentRefreshed() {
        lastUpdateTimestamp = System.currentTimeMillis();
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
        } else if (l < 1000) {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeCurrent");
        } else if (l < 1000 * 60) {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeSeconds", Long.toString(l / 1000));
        } else if (l < 1000 * 60 * 60) {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeMinutes", Long.toString(l / 1000 / 60));
        } else if (l < 1000 * 60 * 60 * 24) {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeHours", Long.toString(l / 1000 / 60 / 60));
        } else {
            return NbBundle.getMessage(CvsSynchronizeTopComponent.class, "CTL_Synchronize_TopComponent_AgeDays", Long.toString(l / 1000 / 60 / 60 / 24));
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
            if(instance == null) {
                instance = new CvsSynchronizeTopComponent();
            }
            return instance;
        }
    }

    /**
     * Sets files/folders the user wants to synchronize. They are typically activated (selected) nodes.
     * 
     * @param ctx new context of the Versioning view
     */
    public void setContext(Context ctx) {
        context = removeDuplicates(ctx);
        setBranchTitle(null);
        refreshContent();
    }

    private List removeDuplicates(File [] roots) {
        List newFiles = new ArrayList();
        outter: for (int i = 0; i < roots.length; i++) {
            File file = roots[i];
            for (Iterator j = newFiles.iterator(); j.hasNext();) {
                File includedFile = (File) j.next();
                if (Utils.isParentOrEqual(includedFile, file)) continue outter;
                if (Utils.isParentOrEqual(file, includedFile)) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        return newFiles;
    }

    private Context removeDuplicates(Context ctx) {
        return new Context(removeDuplicates(ctx.getFiles()), removeDuplicates((File[]) ctx.getRoots().toArray(new File[0])), ctx.getExclusions());
    }
    
    /** Tests whether it shows some content. */
    public boolean hasContext() {
        return context != null && context.getFiles().length > 0;
    }

    protected String preferredID() {
        return "synchronize";    //NOI18N       
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
}
