/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform.ui;

import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.javacard.common.JCConstants;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import java.util.List;
import org.netbeans.modules.javacard.api.JavacardPlatformChildren;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
public final class ServicesNode extends AbstractNode {

    public ServicesNode() {
        super(Children.create(new JCChildren(), true));
        setIconBaseWithExtension("org/netbeans/modules/javacard/platform/ui/root.png"); //NOI18N
        setDisplayName(NbBundle.getMessage(ServicesNode.class, "UI/Runtime/ServicesNode.instance")); //NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.javacard.UsingRuntimeNodeCustomizer"); //NOI18N
    }

    private static class JCChildren extends JavacardPlatformChildren {

        @Override
        protected Children createChildren(FileObject key) {
            try {
                JavacardPlatform p = DataObject.find(key).getNodeDelegate().getLookup().lookup(JavacardPlatform.class);
                if (p != null) {
                    Cards c = p.getCards();
                    return c.createChildren();
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            FileObject fld = Utils.sfsFolderForDeviceConfigsForPlatformNamed(key.getName(), true);
            return Children.create(new ServersChildren(fld), true);
//            return Children.LEAF;
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        FileObject platformsAction = FileUtil.getConfigFile(
                "Menu/Tools/JavaPlatformsCustomizerAction.shadow"); //NOI18N
        if (platformsAction != null) {
            try {
                DataObject dob = DataObject.find(platformsAction);
                InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
                try {
                    if (ic != null && Action.class.isAssignableFrom(ic.instanceClass())) {
                        return new Action[]{(Action) ic.instanceCreate()};
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return new Action[0];
    }

    private static class ServersChildren extends ChildFactory<DataObject> implements FileChangeListener {

        private FileObject deviceDir;

        ServersChildren(FileObject deviceDir) {
            this.deviceDir = deviceDir;
            deviceDir.addFileChangeListener(FileUtil.weakFileChangeListener(this, deviceDir));
        }

        @Override
        protected boolean createKeys(List<DataObject> keys) {
            DataFolder df = DataFolder.findFolder(deviceDir);
            for (DataObject dob : df.getChildren()) {
                //Try extension first, then Lookup.Item to attempt to avoid
                //really instantiating a SunJavaCardServer instance
                if (JCConstants.JAVACARD_DEVICE_FILE_EXTENSION.equals(dob.getPrimaryFile().getExt())
                        || dob.getLookup().lookupResult(Card.class).allItems().size() > 0) {
                    keys.add(dob);
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(DataObject key) {
            return new HelpFN(key.getNodeDelegate());
        }

        public void fileFolderCreated(FileEvent arg0) {
            //do nothing
        }

        public void fileDataCreated(FileEvent arg0) {
            refresh(false);
        }

        public void fileChanged(FileEvent arg0) {
            //do nothing
        }

        public void fileDeleted(FileEvent arg0) {
            refresh(false);
        }

        public void fileRenamed(FileRenameEvent arg0) {
            //do nothing
        }

        public void fileAttributeChanged(FileAttributeEvent arg0) {
            //do nothing
        }
    }

    private static final class HelpFN extends FilterNode {
        private HelpFN(Node orig) {
            super(orig);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.javacard.CustomizeDevice"); //NOI18N
        }
    }
}
