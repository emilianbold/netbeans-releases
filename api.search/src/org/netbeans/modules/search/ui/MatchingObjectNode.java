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
package org.netbeans.modules.search.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.MatchingObject.InvalidityStatus;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class MatchingObjectNode extends AbstractNode {

    @StaticResource
    private static final String INVALID_ICON =
            "org/netbeans/modules/search/res/invalid.png";              //NOI18N
    @StaticResource
    private static final String WARNING_ICON =
            "org/netbeans/modules/search/res/warning.gif";              //NOI18N
    private static RequestProcessor RP = new RequestProcessor(
            "MatchingObjectNode");                                      //NOI18N

    private MatchingObject matchingObject;
    private Node original;
    private OrigNodeListener origNodeListener;
    private boolean valid = true;
    private PropertyChangeListener validityListener;
    private PropertyChangeListener selectionListener;
    PropertySet[] propertySets;

    public MatchingObjectNode(Node original,
            org.openide.nodes.Children children,
            MatchingObject matchingObject, final boolean replacing) {
        this(original, children, matchingObject,
                new ReplaceCheckableNode(matchingObject, replacing));
    }

    private MatchingObjectNode(Node original,
            org.openide.nodes.Children children,
            final MatchingObject matchingObject,
            ReplaceCheckableNode checkableNode) {
        super(children, Lookups.fixed(matchingObject, checkableNode,
                matchingObject.getFileObject()));
        Parameters.notNull("original", original);                       //NOI18N
        this.matchingObject = matchingObject;
        if (matchingObject.isObjectValid()) {
            this.original = original;
            setValidOriginal();
            origNodeListener = new OrigNodeListener();
            original.addNodeListener(origNodeListener);
        } else {
            setInvalidOriginal();
        }
        validityListener = new ValidityListener(matchingObject);
        matchingObject.addPropertyChangeListener(
                MatchingObject.PROP_INVALIDITY_STATUS,
                validityListener);
        selectionListener = new SelectionListener();
        matchingObject.addPropertyChangeListener(MatchingObject.PROP_SELECTED,
                selectionListener);
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
    }

    @Override
    public Transferable drag() throws IOException {
        return UiUtils.DISABLE_TRANSFER;
    }

    @Override
    public Image getIcon(int type) {
        if (valid) {
            return original.getIcon(type);
        } else {
            InvalidityStatus is = matchingObject.getInvalidityStatus();
            String icon = (is == null || is == InvalidityStatus.DELETED)
                    ? INVALID_ICON : WARNING_ICON;
            return ImageUtilities.loadImage(icon);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        if (!context) {
            return new Action[]{
                        SystemAction.get(OpenMatchingObjectsAction.class),
                        new CopyPathAction()
                    };
        } else {
            return new Action[0];
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        return original.getHtmlDisplayName();
    }

    @Override
    public String getDisplayName() {
        return original.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return matchingObject.getFileObject().getPath();
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenMatchingObjectsAction.class);
    }

    private void setValidOriginal() {
        fireIconChange();
        fireDisplayNameChange(null, null);
    }

    private void resetValidOriginal() {
        try {
            valid = true;
            original = matchingObject.getDataObject().getNodeDelegate();
            setValidOriginal();
        } catch (NullPointerException npe) {
            setInvalidOriginal();
        }
    }

    private void setInvalidOriginal() {
        if (valid) {
            valid = false;
        } else {
            fireIconChange();
            return; // already invalid
        }
        if (origNodeListener != null && original != null) {
            original.removeNodeListener(origNodeListener);
            origNodeListener = null;
        }
        String oldDisplayName = original == null
                ? null : original.getDisplayName();
        original = new AbstractNode(Children.LEAF);
        original.setDisplayName(matchingObject.getFileObject().getNameExt());
        fireIconChange();
        fireDisplayNameChange(oldDisplayName,
                matchingObject.getFileObject().getNameExt());
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    public void clean() {
        if (original != null && origNodeListener != null && valid) {
            original.removeNodeListener(origNodeListener);
        }
        if (validityListener != null) {
            matchingObject.removePropertyChangeListener(
                    MatchingObject.PROP_INVALIDITY_STATUS, validityListener);
            validityListener = null;
        }
        if (selectionListener != null) {
            matchingObject.removePropertyChangeListener(
                    MatchingObject.PROP_SELECTED, selectionListener);
            selectionListener = null;
        }
    }

    @Override
    public PropertySet[] getPropertySets() {

        if (propertySets == null) {

            propertySets = new PropertySet[2];
            PropertySet set = new PropertySet() {
                @Override
                public Property<?>[] getProperties() {
                    Property[] properties = new Property[]{
                        new DetailsCountProperty(),};
                    return properties;
                }
            };
            propertySets[0] = set;
            propertySets[1] = new FileObjectPropertySet(
                    matchingObject.getFileObject());
        }
        return propertySets;
    }

    /**
     * Check whether the file object is valid and a valid data object can be
     * found for it. It should be checked after original node is destroyed. It
     * does not have to mean the the file was deleted, but also that a module
     * that contain data loader was enabled. In the letter case, the node should
     * be updated for new data object.
     */
    private void checkFileObjectValid() {
        FileObject fo = matchingObject.getFileObject();
        if (fo != null && fo.isValid()) {
            try {
                DataObject reloaded = DataObject.find(fo);
                matchingObject.updateDataObject(reloaded);
                valid = reloaded.isValid();
                if (valid) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            resetValidOriginal();
                        }
                    });
                }
            } catch (DataObjectNotFoundException ex) {
                // still invalid, the file was probably really deleted
            }
        }
    }

    private class DetailsCountProperty extends Property<Integer> {

        public DetailsCountProperty() {
            super(Integer.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Integer getValue() throws IllegalAccessException,
                InvocationTargetException {
            return matchingObject.getDetailsCount();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Integer val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();                  //NOI18N
        }

        @Override
        public String getName() {
            return "detailsCount";                                      //NOI18N
        }
    }

    private class OrigNodeListener implements NodeListener {

        public OrigNodeListener() {
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setInvalidOriginal();
                    /**
                     * Check if the object is valid again. It can happen when a
                     * module with real data loader is enabled.
                     */
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            checkFileObjectValid();
                        }
                    }, 2500);
                }
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setValidOriginal();
        }
    }

    private class CopyPathAction extends AbstractAction {

        public CopyPathAction() {
            super(UiUtils.getText("LBL_CopyFilePathAction"));           //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File f = FileUtil.toFile(
                    matchingObject.getFileObject());
            if (f != null) {
                String path = f.getPath();
                Clipboard clipboard = Lookup.getDefault().lookup(
                        ExClipboard.class);
                if (clipboard == null) {
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    if (toolkit != null) {
                        clipboard = toolkit.getSystemClipboard();
                    }
                }
                if (clipboard != null) {
                    StringSelection strSel = new StringSelection(path);
                    clipboard.setContents(strSel, null);
                }
            }
        }
    }

    private class ValidityListener implements PropertyChangeListener {

        private final MatchingObject matchingObject;

        public ValidityListener(MatchingObject matchingObject) {
            this.matchingObject = matchingObject;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (matchingObject.getInvalidityStatus()
                    == MatchingObject.InvalidityStatus.DELETED) {
                matchingObject.removePropertyChangeListener(
                        MatchingObject.PROP_INVALIDITY_STATUS,
                        this);
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setInvalidOriginal();
                }
            });
        }
    }

    private class SelectionListener implements PropertyChangeListener {

        public SelectionListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {

            fireIconChange();
            ResultsOutlineSupport.toggleParentSelected(
                    MatchingObjectNode.this);
        }
    }
}
