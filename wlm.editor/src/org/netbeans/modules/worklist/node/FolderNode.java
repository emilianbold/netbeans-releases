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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.node;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Action;
import org.netbeans.modules.worklist.node.cookie.DataObjectCookieDelegate;
import org.netbeans.modules.worklist.util.Util;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;


import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.ComponentPasteType;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.cookies.CountChildrenCookie;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.netbeans.modules.xml.xam.ui.highlight.Highlighted;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author skini
 */
public abstract class FolderNode extends AbstractNode
        implements Highlighted, Node.Cookie, CountChildrenCookie {
    private Set<Component> referenceSet;
    /** Ordered list of highlights applied to this node. */
    private List<Highlight> highlights;
    private Class<? extends WSDLComponent> childType;
    private WSDLComponent mElement;
    private InstanceContent mLookupContents;
    protected Image BADGE_ICON; 

    private static final SystemAction[] ACTIONS = new SystemAction[] {
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
    };

    protected FolderNode(Children children, WSDLComponent comp,
            Class<? extends WSDLComponent> childType) {
        this(children, new InstanceContent(), comp, childType);
    }

    protected FolderNode(Children children, InstanceContent contents,
            WSDLComponent comp, Class<? extends WSDLComponent> childType) {
        super(children, new AbstractLookup(contents));
        mLookupContents = contents;
        this.childType = childType;
        this.mElement = comp;
        contents.add(this);
        DataObject dobj = Util.getDataObject(comp);
        if (dobj != null) {
            contents.add(dobj);
        }
        
        contents.add(new DataObjectCookieDelegate(dobj));
        
        referenceSet = new HashSet<Component>();
        highlights = new LinkedList<Highlight>();
        HighlightManager hm = HighlightManager.getDefault();
        // Must check for the existence of the highlight manager
        // since the component selection panel does not highlight.
        List<? extends WSDLComponent> subcomps = comp.getChildren(childType);
        Iterator<? extends WSDLComponent> iter = subcomps.iterator();
        while (iter.hasNext()) {
            referenceSet.add(iter.next());
        }
        hm.addHighlighted(this);
    }

    protected InstanceContent getLookupContents() {
        return mLookupContents;
    }

    @Override
    public Image getIcon(int type) {
        Image folderIcon = FolderIcon.getIcon(type);
        if (BADGE_ICON != null) {
            return ImageUtilities.mergeImages(folderIcon, BADGE_ICON, 8, 8);
        }
        return folderIcon;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        Image folderIcon = FolderIcon.getOpenedIcon(type);
        if (BADGE_ICON != null) {
            return ImageUtilities.mergeImages(folderIcon, BADGE_ICON, 8, 8);
        }
        return folderIcon;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }

    public abstract Class getType();

    /**
     * Gets the type of child nodes this folder contains.
     *
     * @return  WSDL component type.
     */
    public Class<? extends WSDLComponent> getChildType() {
        return childType;
    }

    public int getChildCount() {
        return mElement.getChildren(getChildType()).size();
    }

    /**
     * Determines if this node represents a component that is contained
     * is editable
     *
     * @return  true if component is editable, false otherwise.
     */
    
    protected boolean isEditable() {
        Model model = mElement.getModel();
        return model != null && XAMUtils.isWritable(model);
    }
    
    
    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return isEditable();
    }

    
    
    @Override
    protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
        // Make sure this node is still valid.
        if (mElement != null && mElement.getModel() != null && isEditable()) {
            PasteType type = ComponentPasteType.getPasteType(
                    mElement, transferable, childType);
            if (type != null) {
                list.add(type);
            }
        }
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        // Make sure this node is still valid.
        if (mElement != null && mElement.getModel() != null && isEditable()) {
            PasteType type = ComponentPasteType.getDropType(
                    mElement, transferable, childType, action, index);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    public boolean canHold(WSDLComponent comp) {
        return getType().isInstance(comp);
    }

    public Set<Component> getComponents() {
        return referenceSet;
    }

    public void highlightAdded(Highlight hl) {
        highlights.add(hl);
        fireDisplayNameChange("TempName", getDisplayName());
    }

    public void highlightRemoved(Highlight hl) {
        highlights.remove(hl);
        fireDisplayNameChange("TempName", getDisplayName());
    }

    /**
     * Given a display name, add the appropriate HTML tags to highlight
     * the display name as dictated by any Highlights associated with
     * this node.
     *
     * @param  name  current display name.
     * @return  marked up display name.
     */
    protected String applyHighlights(String name) {
        int count = highlights.size();
        if (count > 0) {
            // Apply the last highlight that was added to our list.
            String code = null;
            Highlight hl = highlights.get(count - 1);
            String type = hl.getType();
            if (type.equals(Highlight.SEARCH_RESULT) ||
                    type.equals(Highlight.SEARCH_RESULT_PARENT)) {
                // Always use the parent color for search results, as
                // a category cannot possibly be a search result.
                code = "ffc73c";
            } else if (type.equals(Highlight.FIND_USAGES_RESULT) ||
                    type.equals(Highlight.FIND_USAGES_RESULT_PARENT)) {
                // Always use the parent color for search results, as
                // a category cannot possibly be a search result.
                // color = chartreuse
                code = "c7ff3c";
            }

            name = "<strong><font color=\"#" + code + "\">" + name +
                    "</font></strong>";
        }
        return name;
    }

    @Override
    public String getHtmlDisplayName() {
        String name = getDisplayName();
        // Need to escape any HTML meta-characters in the name.
        if (name != null) {
            name = name.replace("<", "&lt;").replace(">", "&gt;");
        }
        return applyHighlights(name);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }
    
    /**
     * Copied from bpel.
     * @author Vitaly Bychkov
     * @version 1.0
     *
     */
    public static class FolderIcon {

        private static AtomicReference<Image> CLOSED_FOLDER_ICON =
                new AtomicReference<Image>();
        
        private static AtomicReference<Image> OPENED_FOLDER_ICON =
                new AtomicReference<Image>();

        private FolderIcon() {
        }
        
        public static Image getOpenedIcon(int type) {
            if (OPENED_FOLDER_ICON.get() == null) {
                Image image = getSystemFolderImage(true, type);
                OPENED_FOLDER_ICON.compareAndSet(null, image);
            }
            return OPENED_FOLDER_ICON.get();
        }
        
        public static Image getIcon(int type) {
            if (CLOSED_FOLDER_ICON.get() == null) {
                Image image = getSystemFolderImage(false, type);
                CLOSED_FOLDER_ICON.compareAndSet(null, image);
            }
            return CLOSED_FOLDER_ICON.get();
        }
        
        private static Image getSystemFolderImage(boolean isOpened, int type) {
                Node n = DataFolder.findFolder(Repository.getDefault()
                                    .getDefaultFileSystem().getRoot()).getNodeDelegate();
                return isOpened ? n.getOpenedIcon(type) : n.getIcon(type);
        }
    }
}
