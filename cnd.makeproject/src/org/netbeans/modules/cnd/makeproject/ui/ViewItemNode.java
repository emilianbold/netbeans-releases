/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.actions.NewTestActionFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Alexander Simon
 */
final class ViewItemNode extends FilterNode implements ChangeListener {
    
    private static final RequestProcessor RP = new RequestProcessor("ViewItemNode", 1); //NOI18N

    private static final MessageFormat ITEM_VIEW_FLAVOR = new MessageFormat("application/x-org-netbeans-modules-cnd-makeproject-uidnd; class=org.netbeans.modules.cnd.makeproject.ui.ViewItemNode; mask={0}"); // NOI18N

    private RefreshableItemsContainer childrenKeys;
    private Folder folder;
    private Item item;
    private final MakeProject project;

    public ViewItemNode(RefreshableItemsContainer childrenKeys, Folder folder, Item item, DataObject dataObject, MakeProject project) {
        super(dataObject.getNodeDelegate());//, null, Lookups.fixed(item));
        this.childrenKeys = childrenKeys;
        this.folder = folder;
        this.item = item;
        setShortDescription(item.getNormalizedPath());
        this.project = project;
    }

    @Override
    public void setName(final String s) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                ViewItemNode.super.setName(s.trim()); // IZ #152560
            }
        });
    }

    public Folder getFolder() {
        return folder;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return addViewItemTransferable(super.clipboardCopy(), DnDConstants.ACTION_COPY);
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        return addViewItemTransferable(super.clipboardCut(), DnDConstants.ACTION_MOVE);
    }

    @Override
    public Transferable drag() throws IOException {
        return addViewItemTransferable(super.drag(), DnDConstants.ACTION_NONE);
    }

    private ExTransferable addViewItemTransferable(Transferable t, int operation) {
        try {
            ExTransferable extT = ExTransferable.create(t);
            ViewItemTransferable viewItem = new ViewItemTransferable(this, operation);
            extT.put(viewItem);
            return extT;
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    // The node will be removed when the Item gets notification that the file has been destroyed.
    // No need to do it here.

    @Override
    public void destroy() throws IOException {
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    ViewItemNode.super.destroy();
                    folder.removeItemAction(item);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return getFolder();
        } else if (valstring.equals("Project")) // NOI18N
        {
            return project;
        } else if (valstring.equals("Item")) // NOI18N
        {
            return getItem();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        }
        return super.getValue(valstring);
    }

    @Override
    public Action[] getActions(boolean context) {
        // Replace DeleteAction with Remove Action
        // Replace PropertyAction with customizeProjectAction
        Action[] oldActions = super.getActions(false);
        List<Action> newActions = new ArrayList<Action>();
        if (getItem().getFolder() == null) {
            return oldActions;
        } else if (getItem().getFolder().isDiskFolder()) {
            for (int i = 0; i < oldActions.length; i++) {
                String key = null; // Some actions are now openide.awt.GenericAction. Use key instead
                if (oldActions[i] != null) {
                    key = (String) oldActions[i].getValue("key"); // NOI18N
                }
                if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(null);
//                        newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
//                        newActions.add(null);
                } else if (oldActions[i] != null && oldActions[i] instanceof PasteAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_COMPILE_SINGLE, NbBundle.getMessage(getClass(), "CTL_CompileSingleAction"), null));
                } else if (oldActions[i] != null && oldActions[i] instanceof RenameAction) {
                    newActions.add(NodeActionFactory.createRenameAction());
                    NodeActionFactory.addSyncActions(newActions);
                } else if (key != null && key.equals("delete")) { // NOI18N
                    newActions.add(NodeActionFactory.createDeleteAction());
                } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                    newActions.add(SystemAction.get(PropertiesItemAction.class));
                } else if (key != null && ("CndCompileAction".equals(key)||"CndCompileRunAction".equals(key)||"CndCompileDebugAction".equals(key))) { // NOI18N
                    // skip
                } else {
                    newActions.add(oldActions[i]);
                }
            }
            return newActions.toArray(new Action[newActions.size()]);
        } else {
            for (int i = 0; i < oldActions.length; i++) {
                String key = null; // Some actions are now openide.awt.GenericAction. Use key instead
                if (oldActions[i] != null) {
                    key = (String) oldActions[i].getValue("key"); // NOI18N
                }
                if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(null);
//                        newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
//                        newActions.add(null);
                } else if (oldActions[i] != null && oldActions[i] instanceof PasteAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_COMPILE_SINGLE, NbBundle.getMessage(getClass(), "CTL_CompileSingleAction"), null));
                    if (!getItem().getFolder().isTest()) {
                        newActions.add(NewTestActionFactory.createNewTestsSubmenu());
                    }
                } else if (oldActions[i] != null && oldActions[i] instanceof RenameAction) {
                    newActions.add(NodeActionFactory.createRenameAction());
                    NodeActionFactory.addSyncActions(newActions);
                } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                    newActions.add(SystemAction.get(PropertiesItemAction.class));
                } else if (key != null && key.equals("delete")) { // NOI18N
                    newActions.add(SystemAction.get(RemoveItemAction.class));
                    newActions.add(NodeActionFactory.createDeleteAction());
                } else if (key != null && ("CndCompileAction".equals(key)||"CndCompileRunAction".equals(key)||"CndCompileDebugAction".equals(key))) { // NOI18N
                    // skip
                } else {
                    newActions.add(oldActions[i]);
                }
            }
            return newActions.toArray(new Action[newActions.size()]);
        }
    }

    @Override
    public Image getIcon(int type) {
        Image image = super.getIcon(type);
        if (isExcluded() && (image instanceof BufferedImage)) {
            image = getGrayImage((BufferedImage)image);
        }
        return image;
    }

    private static final Map<BufferedImage,Image> grayImageCache = new WeakHashMap<BufferedImage, Image>();
    private static Image getGrayImage(BufferedImage image) {
        Image gray = grayImageCache.get(image);
        if (gray == null) {
            ColorSpace gray_space = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp convert_to_gray_op = new ColorConvertOp(gray_space, null);
            gray = convert_to_gray_op.filter(image, null);
            grayImageCache.put(image, gray);
        }
        return gray;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (isExcluded()) {
            String baseName = super.getHtmlDisplayName();
            if (baseName != null && baseName.toLowerCase().contains("color=")) { // NOI18N
                // decorating node already has color, leave it
                return baseName;
            } else {
                // add own "disabled" color
                baseName = baseName != null ? baseName : getDisplayName();
                return "<font color='!controlShadow'>" + baseName; // NOI18N
            }
        }
        return super.getHtmlDisplayName();
    }

    private boolean isExcluded() {
        if (item == null || item.getFolder() == null || item.getFolder().getConfigurationDescriptor() == null || item.getFolder().getConfigurationDescriptor().getConfs() == null) {
            return false;
        }
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return false;
        }
        BooleanConfiguration excl = itemConfiguration.getExcluded();
        return excl.getValue();
    }

    private class VisualUpdater implements Runnable {

        @Override
        public void run() {
            fireIconChange();
            fireOpenedIconChange();
            String displayName = getDisplayName();
            fireDisplayNameChange(displayName, "");
            fireDisplayNameChange("", displayName);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
//            String displayName = getDisplayName();
//            fireDisplayNameChange(displayName, "");
//            fireDisplayNameChange("", displayName);
        EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
//            fireIconChange(); // ViewItemNode
//            fireOpenedIconChange();
    }

    private static final class ViewItemTransferable extends ExTransferable.Single {

        private ViewItemNode node;

        public ViewItemTransferable(ViewItemNode node, int operation) throws ClassNotFoundException {
            super(new DataFlavor(ITEM_VIEW_FLAVOR.format(new Object[]{Integer.valueOf(operation)}), null, MakeLogicalViewProvider.class.getClassLoader()));
            this.node = node;
        }

        @Override
        protected Object getData() throws IOException, UnsupportedFlavorException {
            return this.node;
        }
    }

}
