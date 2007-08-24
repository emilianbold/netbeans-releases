/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.status;

import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.*;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.Mercurial;

/**
 * Top component of the Versioning view.
 * 
 * @author Maros Sandor
 */
public class HgVersioningTopComponent extends TopComponent {
   
    private static final long serialVersionUID = 1L;    
    
    private VersioningPanel         syncPanel;
    private VCSContext              context;
    private String                  contentTitle;
    private String                  branchTitle;
    private long                    lastUpdateTimestamp;
    private static final String PREFERRED_ID = "hgversioning"; // NOI18N
    
    private static HgVersioningTopComponent instance;

    public HgVersioningTopComponent() {
        putClientProperty("SlidingName", NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); //NOI18N

        setName(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        setIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/mercurial/resources/icons/versioning-view.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        syncPanel = new VersioningPanel(this);
        add(syncPanel);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    protected void componentActivated() {
        updateTitle();
        syncPanel.focus();
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
     * @param contentTitle a new content title, e.g. "2 projects" // NOI18N
     */ 
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        updateTitle();
    }

    /**
     * Sets the 'branch' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     * 
     * @param branchTitle a new content title, e.g. "release40" branch // NOI18N
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
        SwingUtilities.invokeLater(new Runnable (){
            public void run() {
                if (contentTitle == null) {
                    setName(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
                } else {
                    if (branchTitle == null) {
                        setName(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_MultiTitle", contentTitle));  // NOI18N
                    } else {
                        setName(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title_ContentBranch", contentTitle, branchTitle)); // NOI18N
                    }
                }                
            }
        });
    }

    String getContentTitle() {
        return contentTitle;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized HgVersioningTopComponent getDefault() {
        if (instance == null) {
            instance = new HgVersioningTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the HgVersioningTopComponent  instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HgVersioningTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Mercurial.LOG.log(Level.FINE, "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof HgVersioningTopComponent) {
            return (HgVersioningTopComponent)win;
        }
        Mercurial.LOG.log(Level.FINE, 
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return HgVersioningTopComponent.getDefault();
        }
    }
    

    /**
     * Programmatically invokes the Refresh action.
     */ 
    public void performRefreshAction() {
        syncPanel.performRefreshAction();
    }

    /**
     * Sets files/folders the user wants to synchronize. They are typically activated (selected) nodes.
     * 
     * @param ctx new context of the Versioning view
     */
    public void setContext(VCSContext ctx) {
        syncPanel.cancelRefresh();

        if (ctx == null) {
            setName(NbBundle.getMessage(HgVersioningTopComponent.class, "MSG_Preparing")); // NOI18N
            setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            context = ctx;
            //syncPanel.setContext(ctx);
            setBranchTitle("current"); // TODO: setContext() get Branch title - hg branch  // NOI18N
            refreshContent();
        }
        setToolTipText(getContextFilesList(ctx, NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title"))); // NOI18N            
    }
    
    private String getContextFilesList(VCSContext ctx, String def) {
        if (ctx == null || ctx.getRootFiles().size() == 0) return def;
        StringBuffer sb = new StringBuffer(200);
        sb.append("<html>"); // NOI18N
        for (File file : ctx.getRootFiles()) {
            sb.append(file.getAbsolutePath());
            sb.append("<br>"); // NOI18N
        }
        sb.delete(sb.length() - 4, Integer.MAX_VALUE);
        return sb.toString();
    }

    /** Tests whether it shows some content. */
    public boolean hasContext() {
        return context != null && context.getRootFiles().size() > 0;
    }
}
