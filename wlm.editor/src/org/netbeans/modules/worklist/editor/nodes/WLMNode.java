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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.nodes;

import javax.swing.Action;
import org.netbeans.modules.worklist.editor.nodes.children.WLMChildren;
import org.netbeans.modules.worklist.editor.utils.DisplayNameBuilder;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.editor.navigator.WLMNavigatorValidationSupport;
import org.netbeans.modules.worklist.editor.navigator.WLMValidationItems;
import org.netbeans.modules.worklist.editor.nodes.actions.GoToDesignAction;
import org.netbeans.modules.worklist.editor.nodes.actions.GoToSourceAction;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author anjeleevich
 */
public abstract class WLMNode<T extends WLMComponent> extends AbstractNode {
    private T component;
    private String htmlDisplayName;

    private boolean folder;
    private Map<IconKey, Image> iconCache = null;
    private Image badge = null;

    private String iconBase;

    protected final Object sync = new Object();

    private WLMAcceptableDescendants acceptableDescendants;
    private WLMValidationItems validationItems = null;

    public WLMNode(T component, WLMAcceptableDescendants acceptableDescendants,
            Children children, Lookup lookup, boolean folder, String iconName)
    {
        super(children, lookup);
        
        this.acceptableDescendants = (acceptableDescendants == null)
                ? WLMAcceptableDescendants.getAllAcceptable()
                : acceptableDescendants;

        this.component = component;

        this.folder = folder;
        this.iconBase = (iconName != null) ? ICON_FOLDER + iconName : null;

        if (!folder && iconBase != null) {
            setIconBaseWithExtension(iconBase);
        }

        updateValidationItems(false);
    }

    public WLMNode(T component, Children children, Lookup lookup, 
            boolean folder, String iconName)
    {
        this (component, getAcceptableDescendants(children), children, lookup,
                folder, iconName);
    }

    public WLMNode(T component, Children children, Lookup lookup,
            String iconName)
    {
        this(component, children, lookup, false, iconName);
    }

    public WLMNode(T component, Children children, Lookup lookup) {
        this(component, children, lookup, false, null);
    }

    public WLMNode(T component, WLMAcceptableDescendants acceptableDescendants,
            Children children, Lookup lookup)
    {
        this(component, acceptableDescendants, children, lookup, false, null);
    }

    private boolean isFolder() {
        return folder;
    }

    public T getWLMComponent() {
        return component;
    }

    public WLMComponent getGoToSourceWLMComponent() {
        return component;
    }

//    public WLMNode findNode(WLMNodeType nodeType, WLMComponent sourceComponent) {
//        WLMNode result = null;
//
//        if ((getType() == nodeType)
//                && (getGoToSourceWLMComponent() == sourceComponent))
//        {
//            result = this;
//        } else if (isAcceptableDescedand(nodeType)) {
//            Children children = getChildren();
//            if (children instanceof WLMChildren) {
//                WLMChildren wlmChildren = (WLMChildren) children;
//                if (wlmChildren.isWLMChildrenIntialized()) {
//                    Node[] childNodes = wlmChildren.getNodes();
//                    if (childNodes != null) {
//                        for (Node childNode : childNodes) {
//                            if (childNode instanceof WLMNode) {
//                                result = ((WLMNode) childNode)
//                                        .findNode(nodeType, sourceComponent);
//                                if (result != null) {
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return result;
//    }

    public abstract WLMNodeType getType();

    public boolean isAcceptableDescedand(WLMNode node) {
        return acceptableDescendants.isAcceptableDescendant(node.getType());
    }

    public boolean isAcceptableDescedand(WLMNodeType type) {
        return acceptableDescendants.isAcceptableDescendant(type);
    }

    @Override
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        
        if (arg instanceof WLMNode) {
            WLMNode node = (WLMNode) arg;
            return (node.getType() == getType())
                    && node.getWLMComponent() == getWLMComponent();
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Image getIcon(int type) {
        return getIconImpl(type, false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIconImpl(type, true);
    }

    private Image getIconImpl(int type, boolean opened) {
        synchronized (sync) {
            Image image = null;

            long longType = getLongType(type, opened);

            ResultType resultType = (validationItems == null) ? null
                    : validationItems.getType();

            IconKey iconKey = new IconKey(resultType, longType);

            if (iconCache == null || !iconCache.containsKey(iconKey)) {
                if (isFolder()) {
                    image = getFolderIcon(type, opened);
                }

                if (image == null) {
                    image = (opened)
                            ? super.getOpenedIcon(type)
                            : super.getIcon(type);
                }

                if (badge == null && iconBase != null && isFolder()) {
                    badge = ImageUtilities.loadImage(iconBase);
                }

                if (badge != null && image != null) {
                    image = ImageUtilities.mergeImages(image, badge, 8, 8);
                }

                Image validationBadge = null;
                if (resultType == ResultType.ERROR) {
                    validationBadge = ValidationBadges.ERROR;
                } else if (resultType == ResultType.WARNING) {
                    validationBadge = ValidationBadges.WARNING;
                }

                if (validationBadge != null && image != null) {
                    image = ImageUtilities.mergeImages(image,
                            validationBadge, 8, 0);
                }

                if (image != null) {
                    if (iconCache == null) {
                        iconCache = new HashMap<IconKey, Image>();
                    }

                    iconCache.put(iconKey, image);
                }
            } else {
                image = iconCache.get(iconKey);
            }

            if (image != null) {
                return image;
            }
        }

        return (opened)
                ? super.getOpenedIcon(type)
                : super.getIcon(type);
    }

    protected void setDisplayName(DisplayNameBuilder builder) {
        htmlDisplayName = builder.getHTML();
        setDisplayName(builder.getText());
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(GoToDesignAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { 
                SystemAction.get(GoToDesignAction.class),
                SystemAction.get(GoToSourceAction.class)
        };
    }

    public void reload() {
        updateDisplayName();
        fireWLMPropertiesChanged();

        Children children = getChildren();
        if (children instanceof WLMChildren) {
            WLMChildren wlmChildren = (WLMChildren) children;
            if (wlmChildren.reload()) {
                Node[] nodes = wlmChildren.getNodes();
                if (nodes != null) {
                    for (int i = nodes.length - 1; i >= 0; i--) {
                        if (nodes[i] instanceof WLMNode) {
                            ((WLMNode) nodes[i]).reload();
                        }
                    }
                }
            }
        }
    }

    public void reloadValidationItems() {
        updateValidationItems(true);

        Children children = getChildren();
        if (children instanceof WLMChildren) {
            WLMChildren wlmChildren = (WLMChildren) children;
            if (wlmChildren.isWLMChildrenIntialized()) {
                Node[] nodes = wlmChildren.getNodes();
                if (nodes != null) {
                    for (int i = nodes.length - 1; i >= 0; i--) {
                        if (nodes[i] instanceof WLMNode) {
                            ((WLMNode) nodes[i]).reloadValidationItems();
                        }
                    }
                }
            }
        }
    }

    public void updateValidationItems(boolean fireEvents) {
        WLMNavigatorValidationSupport support = getLookup()
                .lookup(WLMNavigatorValidationSupport.class);

        WLMValidationItems oldItems = this.validationItems;
        WLMValidationItems newItems = null;

        if (support != null) {
            newItems = support.getValidationItems(this);
        }

        if (oldItems != newItems) {
            this.validationItems = newItems;

            if (fireEvents) {
                ResultType oldType = (oldItems == null) ? null
                        : oldItems.getType();
                ResultType newType = (newItems == null) ? null 
                        : newItems.getType();

                if (oldType != newType) {
                    fireIconChange();
                }
            }
        }
    }

    public void updateDisplayName() {
        // default implemetation
    }

    protected static final String ICON_FOLDER =
            "org/netbeans/modules/worklist/editor/nodes/resources/"; // NOI18N

    protected static final int TRUNC_LENGTH = 40;

    private static Image getFolderIcon(int type, boolean opened) {
        synchronized (FOLDER_ICON_CACHE) {
            long longType = getLongType(type, opened);

            Image image = null;
            if (!FOLDER_ICON_CACHE.containsKey(longType)) {
                Node node = null;
                try {
                    node = DataFolder.findFolder(Repository
                            .getDefault().getDefaultFileSystem()
                            .getRoot()).getNodeDelegate();
                } catch (Exception ex) {
                    // Do nothing
                }

                if (node != null) {
                    if (opened) {
                        try {
                            image = node.getOpenedIcon(type);
                        } catch (Exception ex) {
                            // do nothing
                        }
                    }

                    if (image == null) {
                        try {
                            image = node.getIcon(type);
                        } catch (Exception ex) {
                            // do nothing
                        }
                    }
                }

                FOLDER_ICON_CACHE.put(longType, image);
            } else {
                image = FOLDER_ICON_CACHE.get(longType);
            }
            
            return image;
        }
    }

    private static long getLongType(int type, boolean opened) {
        return (opened) ? ((long) type << 32) : (((long) type) & 0xFFFFFFFFl);
    }

    private static final Map<Long, Image> FOLDER_ICON_CACHE
            = new HashMap<Long, Image>();
    private static final Map<ResultType, Image> VALIDATION_ICON_CACHE
            = new HashMap<ResultType, Image>();

    private static final WLMAcceptableDescendants getAcceptableDescendants(
            Object object)
    {
        WLMAcceptableDescendants result = null;
        
        if (object instanceof WLMAcceptableDescendants.Provider) {
            result = ((WLMAcceptableDescendants.Provider) object)
                    .getAcceptableDescendants();
        }

        if (object == Children.LEAF) {
            result = WLMAcceptableDescendants.getAllAcceptable();
        }

        if (result == null) {
            result = WLMAcceptableDescendants.getAllAcceptable();
        }

        return result;
    }

    protected void fireWLMPropertiesChanged() {

    }

    private static class IconKey {
        private ResultType resultType;
        private long iconType;

        public IconKey(ResultType resultType, long iconType) {
            this.resultType = resultType;
            this.iconType = iconType;
        }

        @Override
        public int hashCode() {
            return (int) (iconType ^ (iconType >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IconKey) {
                IconKey key = (IconKey) obj;
                return (resultType == key.resultType)
                        && (iconType == key.iconType);
            }
            return false;
        }
    }

    private static class ValidationBadges {
        static final Image ERROR = ImageUtilities.loadImage(ICON_FOLDER
                + "validation/error_badge.png"); // NOI18N
        static final Image WARNING = ImageUtilities.loadImage(ICON_FOLDER
                + "validation/warning_badge.png"); // NOI18N
    }
}
