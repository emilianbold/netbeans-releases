/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.localhistory.ui.view;

import java.io.File;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka
 */
public class ShowLocalHistoryAction extends NodeAction {
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public ShowLocalHistoryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    protected void performAction(final Node[] activatedNodes) {                        
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        final Set<File> rootSet = ctx.getRootFiles();                    

                File[] files = rootSet.toArray(new File[rootSet.size()]);                

        if(!files[0].isFile()) {
            return;
        }

        File file = files[0];
        FileObject fo = FileUtil.toFileObject(file);
        if(fo != null) {
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if(dataObject != null) {
                if(findInOpenTC(dataObject)) {
                    return;
                }
                if(findInMVDescriptions(dataObject)) {
                    return;
                }
            }
        }

        // fallback opening a LHTopComponent
        openTC(files);
    }
    
    private void openTC(final File[] files) throws MissingResourceException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final LocalHistoryTopComponent tc = new LocalHistoryTopComponent();
                tc.setName(NbBundle.getMessage(this.getClass(), "CTL_LocalHistoryTopComponent", files[0].getName())); // NOI18N
                tc.open();
                tc.requestActive();                                
                tc.init(files);
            }
        });
    }
                
    /**
     * XXX HACK temporary solution to find out if the given dataobject provides a multiview
     */
    private boolean findInMVDescriptions(DataObject dataObject) {
        String mime = dataObject.getPrimaryFile().getMIMEType();
        Lookup l = MimeLookup.getLookup(MimePath.get(mime));
        Collection<? extends MultiViewDescription> descs = l.lookupAll(MultiViewDescription.class);
        if (descs.size() > 1) {
            // LH is registred for every mimetype, so we need at least two
            for (MultiViewDescription desc : descs) {
                if (desc.preferredID().equals(LocalHistoryTopComponent.PREFERRED_ID)) {
                    EditCookie cookie = dataObject.getLookup().lookup(EditCookie.class);
                    if (cookie != null) {
                        cookie.edit();
                        findInOpenTC(dataObject);
                        return true;
                            }
                        } 
                } 
            }
        return false;
    }

    private boolean findInOpenTC(DataObject dataObject) {
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (final TopComponent tc : tcs) {
            Lookup l = tc.getLookup();
            DataObject tcDataObject = l.lookup(DataObject.class);
            if (tcDataObject != null && dataObject.equals(tcDataObject)) {
                final MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                if (handler != null) {
                    MultiViewPerspective[] perspectives = handler.getPerspectives();
                    for (final MultiViewPerspective p : perspectives) {
                        if(p.preferredID().equals(LocalHistoryTopComponent.PREFERRED_ID)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    handler.requestActive(p);
                                    tc.requestActive();
                                }
                            });
                            break;
                        } 
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {     
        if(activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<File> rootSet = ctx.getRootFiles();                
        if(rootSet == null || rootSet.isEmpty()) { 
            return false;
        }                        
        for (File file : rootSet) {            
            if(file != null && !file.isFile()) {
                return false;
            }
        }        
        return true;           
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "CTL_ShowLocalHistory");    // NOI8N    
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowLocalHistoryAction.class);
    }
    
}
