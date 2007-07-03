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

package org.netbeans.modules.ruby.rubyproject.ui;

import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


/**
 * Action for showing RDoc. The action looks up
 * the {@link ShowRDocAction.RDocProvider} in the
 * activated node's Lookup and delegates to it.
 * 
 * (Based on the ShowJavaDocAction in J2SE projects)
 * 
 * @author Tomas Zezula
 * @author Tor Norbye
 */
final class ShowRDocAction extends NodeAction {

    /**
     * Implementation of this interfaces has to be placed
     * into the node's Lookup to allow {@link ShowRDocAction}
     * on the node.
     */
    public static interface RDocProvider {

        /**
         * Checks if the node can provide RDoc
         * @return true if the action should be enabled
         */
        public abstract boolean hasRDoc ();

        /**
         * Opens rdoc page in the browser
         */
        public abstract void showRDoc ();
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length!=1) {
            return;
        }
        RDocProvider jd = (RDocProvider) activatedNodes[0].getLookup().lookup(RDocProvider.class);
        if (jd == null) {
            return;
        }
        jd.showRDoc();
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length!=1) {
            return false;
        }
        RDocProvider jd = (RDocProvider) activatedNodes[0].getLookup().lookup(RDocProvider.class);
        if (jd == null) {
            return false;
        }
        return jd.hasRDoc();
    }

    public final String getName() {
        return NbBundle.getMessage(ShowRDocAction.class,"CTL_ShowRDoc");
    }

    public final HelpCtx getHelpCtx() {
        return new HelpCtx (ShowRDocAction.class);
    }

    public final boolean asynchronous () {
        return false;
    }

    /**
     * Opens the IDE default browser with given URL
     * @param rdoc URL of the rdoc page
     * @param displayName the name of file to be displayed, typically the package name for class
     * or project name for project.
     */
    static void showRDoc (URL rdoc, String displayName) {
        if (rdoc!=null) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(rdoc);
        }
        else {
            StatusDisplayer.getDefault().setStatusText(MessageFormat.format(NbBundle.getMessage(ShowRDocAction.class,
                    "TXT_NoRDoc"), new Object[] {displayName}));   //NOI18N
        }
    }

    /**
     * Locates a rdoc page by a relative name and an array of rdoc roots
     * @param resource the relative name of rdoc page
     * @param urls the array of rdoc roots
     * @return the URL of found rdoc page or null if there is no such a page.
     */
    static  URL findRDoc (String resource, URL urls[]) {
        for (int i=0; i<urls.length; i++) {
            String base = urls[i].toExternalForm();
            if (!base.endsWith("/")) { // NOI18N
                base+="/"; // NOI18N
            }
            try {
                URL u = new URL(base+resource);
                FileObject fo = URLMapper.findFileObject(u);
                if (fo != null) {
                    return u;
                }
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "Cannot create URL for "+base+resource+". "+ex.toString());   //NOI18N
                continue;
            }
        }
        return null;
    }
}
