/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.nodes;

import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.modules.maven.LogicalViewProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  Milos Kleint
 */
class OthersRootNode extends AnnotatedAbstractNode {
    private FileObject file;
    private static final String SHOW_AS_PACKAGES = "show.as.packages"; //NOI18N
    private static final String PREF_RESOURCES_UI = "org/netbeans/modules/maven/resources/ui"; //NOI18N
    
    OthersRootNode(NbMavenProjectImpl mavproject, boolean testResource, FileObject fo) {
        super(new OthersRootChildren(mavproject, testResource), Lookups.fixed(fo, DataFolder.findFolder(fo), new ChildDelegateFind()));
        setName(testResource ? "OtherTestRoots" : "OtherRoots"); //NOI18N
        setDisplayName(testResource ? org.openide.util.NbBundle.getMessage(OthersRootNode.class, "LBL_Other_Test_Sources") : org.openide.util.NbBundle.getMessage(OthersRootNode.class, "LBL_Other_Sources"));
        // can do so, since we depend on it..
//        setIconBase("org/mevenide/netbeans/project/resources/defaultFolder"); //NOI18N
        file = fo;
    }
    
    @Override
    public Action[] getActions(boolean context) {
            List<Action> supers = Arrays.asList(super.getActions(context));
            List<Action> lst = new ArrayList<Action>(supers.size() + 5);
            lst.addAll(supers);
            lst.add(new ShowAsPackagesAction());

            Action[] retValue = new Action[lst.size()];
            retValue = lst.toArray(retValue);
            return retValue;

    }
    
    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/others-badge.png", true); //NOI18N
        return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
    }

    @Override
    protected Image getIconImpl(int param) {
        return getIcon(false);
    }

    @Override
    protected Image getOpenedIconImpl(int param) {
        return getIcon(true);
    }
    
    
    @Override
    public String getDisplayName () {
        String s = super.getDisplayName ();
        try {            
            s = file.getFileSystem ().getStatus ().annotateName (s, Collections.singleton(file));
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return s;
    }

    @Override
    public String getHtmlDisplayName() {
         try {
             FileSystem.Status stat = file.getFileSystem().getStatus();
             if (stat instanceof FileSystem.HtmlStatus) {
                 FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;

                 String result = hstat.annotateNameHtml (
                     super.getDisplayName(), Collections.singleton(file));

                 //Make sure the super string was really modified
                 if (!super.getDisplayName().equals(result)) {
                     return result;
                 }
             }
         } catch (FileStateInvalidException e) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
         }
         return super.getHtmlDisplayName();
    }

    static boolean showAsPackages() {
        Preferences prefs = NbPreferences.root().node(PREF_RESOURCES_UI); //NOI18N
        boolean b = prefs.getBoolean(SHOW_AS_PACKAGES, true); //NOI18N
        return b;
    }


    @SuppressWarnings("serial")
    private class ShowAsPackagesAction extends AbstractAction implements Presenter.Popup {

        public ShowAsPackagesAction() {
            String s = NbBundle.getMessage(DependenciesNode.class, "LBL_ShowAsPackages");
            putValue(Action.NAME, s);
        }

        public void actionPerformed(ActionEvent e) {
            boolean b = showAsPackages();
            Preferences prefs = NbPreferences.root().node(PREF_RESOURCES_UI); //NOI18N
            prefs.putBoolean(SHOW_AS_PACKAGES, !b); //NOI18N
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            ((OthersRootChildren)getChildren()).doRefresh();
        }

        public JMenuItem getPopupPresenter() {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(this);
            mi.setSelected(showAsPackages());
            return mi;
        }

    }

    static class ChildDelegateFind implements LogicalViewProviderImpl.FindDelegate {
        public Node[] getDelegates(Node current) {
            return current.getChildren().getNodes(true);
        }
    }
}

