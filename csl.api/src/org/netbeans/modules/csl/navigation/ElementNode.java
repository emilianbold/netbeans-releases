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
package org.netbeans.modules.csl.navigation;


import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureItem.CollapsedDefault;
import org.netbeans.modules.csl.core.GsfHtmlFormatter;
import org.netbeans.modules.csl.navigation.actions.OpenAction;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * GSF changes made: Instead of accessing fields on Description object,
 * replace references to Description with StructureItem interface (descriptions
 * supplied by language plugins), make method calls on this interface rather
 * than accessing fields directly. Some data such as the "ui" field was moved
 * into ElementNode itself rather than sitting on the description object which
 * is no longer under our control.
 * <p>
 * Node representing an element
 * 
 * 
 * @author Petr Hrebejk
 */
public class ElementNode extends AbstractNode {

    /**
     * This RP will collect children keys, while not blocking the actual getChildren() call.
     */
    private static final RequestProcessor CHILD_RP = new RequestProcessor("Child node fetcher"); // NOI18N
    
    private static final Logger LOG = Logger.getLogger(ElementNode.class.getName());
    
    static Node WAIT_NODE;
    
    private OpenAction openAction;
    private StructureItem description;
    private ClassMemberPanelUI ui;
    private FileObject fileObject; // For the root description
    
    private static final int WAIT_PERIOD = 100; // max 100ms
           
    /** Creates a new instance of TreeNode */
    public ElementNode( StructureItem description, ClassMemberPanelUI ui, FileObject fileObject) {
        super(description.isLeaf() ? Children.LEAF: 
                new ElementChildren(description, ui, fileObject));
        this.description = description;
        this.ui = ui;
        this.fileObject = fileObject;
        setDisplayName( description.getName() ); 
    }
    
    private ElementNode(Children ch) {
        super(ch);
    }
    
    StructureItem getModel() {
        return description;
    }
    
    @Override
    public Image getIcon(int type) {
        if (description.getCustomIcon() != null) {
            return ImageUtilities.icon2Image(description.getCustomIcon());
        }
        Icon icon = Icons.getElementIcon(description.getKind(), description.getModifiers());
        if (icon != null) {
            return ImageUtilities.icon2Image(icon);
        } else {
            return super.getIcon(type);
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
                   
    @Override
    public java.lang.String getDisplayName() {
        return description.getName();
    }
            
    @Override
    public String getHtmlDisplayName() {
        return description.getHtml(new NavigatorFormatter());
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        
        if ( context || description.getName() == null ) {
            return ui.getActions();
        }
        else {
            Action panelActions[] = ui.getActions();
            
            Action actions[]  = new Action[ 2 + panelActions.length ];
            actions[0] = getOpenAction();
            actions[1] = null;
            for( int i = 0; i < panelActions.length; i++ ){
                actions[2 + i] = panelActions[i];
            }
            return actions;
        }
    }        
    
    @Override
    public Action getPreferredAction() {
        return getOpenAction();
    }
    
    
    private synchronized Action getOpenAction() {
        if ( openAction == null ) {
            FileObject fo = ui.getFileObject();
            try {
                openAction = new OpenAction(description.getElementHandle(), fo,
                        description.getPosition());
            } catch (UnsupportedOperationException uo) {
                return null; // root node does not have element handle
            }
        }
        return openAction;
    }
    
    static synchronized Node getWaitNode() {
        if ( WAIT_NODE == null ) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }

    boolean isExpandedByDefault() {
        return ui.isExpandedByDefault(this);
    }
    
    /**
     * Runs the Runnable after all pending Children keys requests were completed.
     * 
     * @param r code to execute
     * @return Task handle for the runnable.
     */
    static Task runWithChildren(Runnable r) {
        return CHILD_RP.post(r);
    }
    
    /**
     * Refreshes the Node recursively. Only initiates the refresh; the refresh
     * itself may happen asynchronously.
     */
    public void refreshRecursively() {
        List<Node> toExpand = new ArrayList<Node>();
        refreshRecursively(Collections.singleton(this), toExpand);
        ui.performExpansion(toExpand, Collections.<Node>emptyList());
    }

    private void refreshRecursively(Collection<? extends Node> toDo, final Collection<Node> toExpand) {
        if (toDo.isEmpty()) {
            return;
        }
        Collection<Node> nextRound = new ArrayList<Node>(toDo.size());
        for (Node nod : toDo) {
            if (!(nod instanceof ElementNode)) {
                continue;
            }
            ElementNode elnod = (ElementNode)nod;
            LOG.fine("Refreshing: " + elnod);
            final Children ch = elnod.getChildren();
            if ( ch instanceof ElementChildren ) {
                ElementChildren ech = (ElementChildren)ch;
                if (ech.wasInitialized()) {
                    ech.refreshChildren();
                    nextRound.addAll(Arrays.asList(ech.getNodes()));
                }
            }
        }
        refreshRecursively(nextRound, toExpand);
    }
    
    private ElementNode findMimeRootNode(ParserResult info, int offset) {
        if (getDescription().getPosition() > offset) {
            return null;
        }
        
        // Look up the current mime type
        Document document = info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return null;
        }
        BaseDocument doc = (BaseDocument)document;
        List<Language> languages = LanguageRegistry.getInstance().getEmbeddedLanguages(doc, offset);

        // Look specifically within the
        if (languages.size() > 0) {
            LOG.fine("Found embedded languages: " + languages);
            Children ch = getChildren();
            if ( ch instanceof ElementChildren ) {
                Node[] children = ch.getNodes();
                for (Language language : languages) {
                    // Inefficient linear search because the children may not be
                    // ordered according to the source
                    for (int i = 0; i < children.length; i++) {
                        ElementNode c = (ElementNode) children[i];
                        if (c.getDescription() instanceof ElementScanningTask.MimetypeRootNode) {
                            ElementScanningTask.MimetypeRootNode mr = (ElementScanningTask.MimetypeRootNode)c.getDescription();
                            if (mr.language == language) {
                                LOG.fine("Found MIME root: " + c);
                                return c;
                            }
                        }
                    }
                }
            }
        }
        LOG.fine("No MIME type root found");
        return null;
    }
    
    void doWithNodeAtOffset(ParserResult info, int offset, NodeAction exec) {
        ElementNode rootNode = findMimeRootNode(info, offset);
        if (rootNode == null) {
            rootNode = this;
        }
        rootNode.doWithNodeAtOffset(offset, exec, false);
    }
    
    private void doWithNodeAtOffset(final int offset, final NodeAction exec, boolean dontWait) {
        LOG.log(Level.FINE, "Searching for offset {0} under {1}", new Object[] { offset, this });
        final Node n = getNodeForOffset(offset);
        
        // exec if not a wait node, or if AGAIN our own wait node.
        if (!isWaitNode(n) ||
            (dontWait && n.getParentNode() == this)) {
            LOG.log(Level.FINE, "Search terminated; waitNode = {0}", n);
            exec.runWith(n);
            return;
        }
        
        if (exec.isCanceled()) {
            // terminate
            return;
        }
        LOG.log(Level.FINE, "Got wait node under {0}", n.getParentNode());
        runWithChildren(new Runnable() {
           public void run() {
               ((ElementNode)n.getParentNode()).doWithNodeAtOffset(offset, exec, true);
           } 
        });
    }
    
    private ElementNode getNodeForOffset(int offset) {
        if (getDescription().getPosition() > offset) {
            return null;
        }

        // Inefficient linear search because the children may not be
        // ordered according to the source
        Children ch = getChildren();
        if ( ch instanceof ElementChildren ) {
            Node[] children = ch.getNodes();
            for (int i = 0; i < children.length; i++) {
                ElementNode c = (ElementNode) children[i];
                if (isWaitNode(c)) {
                    return c;
                }
                long start = c.getDescription().getPosition();
                if (start <= offset) {
                    long end = c.getDescription().getEndPosition();
                    if (end >= offset) {
                        return c.getNodeForOffset(offset);
                    }
                }
            }
        }

        return this;
    }

    public void updateRecursively( StructureItem newDescription ) {
           TreeUpdater u = new TreeUpdater(ui);
           u.execute(this, newDescription);
    }
    
    /**
     * Updates the node itself, and schedules an update for its children,
     * if children have been initialized.
     * 
     * @param newDescription
     * @param updater 
     */
    void updateSelf(StructureItem newDescription, TreeUpdater updater) {
        StructureItem oldDescription = description; // Remember old description        
        description = newDescription; // set new descrioption to the new node
        String oldHtml = oldDescription.getHtml(new NavigatorFormatter());
        String descHtml = description.getHtml(new NavigatorFormatter());
        if ( oldHtml != null && !oldHtml.equals(descHtml)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldHtml, descHtml);
        }
        if( oldDescription.getModifiers() != null &&  !oldDescription.getModifiers().equals(newDescription.getModifiers())) {
            fireIconChange();
            fireOpenedIconChange();
        }

        Children ch = getChildren();

        //If a node that was a LEAF now has children the child type has to be changed from Children.LEAF
        //to ElementChildren to be able to hold the new child data
        ElementChildren ech = ch instanceof ElementChildren ? (ElementChildren)ch : null;

        if (ech == null) {
            LOG.log(Level.FINE, "LEAF children found, stop update at {0}", this);
            if (!newDescription.isLeaf()) {
                ech= new ElementChildren(newDescription, ui, fileObject);
                setChildren(ech);
                LOG.log(Level.FINE, "Changing children from nonleaf > leaf for: {0}", this);
            }
            return;
        }
        // add the children potentially to the update list
        updater.runUpdate(this, description);
    }

    /**
     * Callback action to perform at a certain node. Currently
     * used to select node after it is found in the tree.
     */
    interface NodeAction {
        public void runWith(Node n);
        /**
         * Informs that the action has been canceled, e.g. a selection was
         * superseded by another caret movement.
         * 
         * @return true, if the action is canceled.
         */
        public boolean isCanceled();
    }
    
    public StructureItem getDescription() {
        return description;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    /**
     * Key for the wait node
     */
    private static final StructureItem WAIT_KEY = new StructureItem() {
        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSortText() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ElementHandle getElementHandle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ElementKind getKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Modifier> getModifiers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isLeaf() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getPosition() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getEndPosition() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ImageIcon getCustomIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    };
    
    static final class ElementChildren extends Children.Keys<StructureItem> {
        private ClassMemberPanelUI ui;
        private FileObject fileObject;
        private volatile boolean initialized;
        /**
         * Handle for the update. The async ChildR task will not update the keys
         * if the handle on the Children object changes in the meantime.
         */
        private volatile int processing;
        private StructureItem parentItem;
        private List<StructureItem> keys;
        
        public ElementChildren(List<StructureItem> children, ClassMemberPanelUI ui, FileObject fileObject) {
            this.parentItem = null;
            this.ui = ui;
            this.fileObject = fileObject;
        }
        
        public ElementChildren(StructureItem parentItem, ClassMemberPanelUI ui, FileObject fileObject) {
            this.parentItem = parentItem;
            this.ui = ui;
            this.fileObject = fileObject;
        }
        
        synchronized List<StructureItem> getKeys() {
            return keys == null ? Collections.EMPTY_LIST : keys;
        }
        
        boolean wasInitialized() {
            return initialized;
        }

        @Override
        protected void addNotify() {
            if (parentItem != null) {
                initialize(parentItem, ui.getFilters());
            }
        }
        
        protected Node[] createNodes(StructureItem key) {
            if (key == WAIT_KEY) {
                return new Node[] { new FilterNode(getWaitNode()) };
            }
            return new Node[] {new  ElementNode(key, ui, fileObject)};
        }
        
        void refreshChildren() {
            resetKeys(processing, getKeys(), ui.getFilters());
        }
        
        /**
         * Replaces our own StructureItem with the new version. If nodes were not initialized,
         * returns null. If nodes WERE initialized, it attempts to reset keys 
         * and potentially defers the keys computation to a RP, if the fetching takes too long.
         * Returns the immediate node snapshot.
         * 
         * @return node snapshot or {@code null} to indicate that no nodes were created yet
         */
        Collection<Node> replaceItem(StructureItem description) {
            Collection<Node>    items;
            
            synchronized (this) {
                this.parentItem = description;
                if (!initialized && processing == 0) {
                    return null;
                }
                items = snapshot();
                processing++;
            }
            initialize(description, ui.getFilters());
            return items;
        }
        
        private void initialize(StructureItem parentDescription, ClassMemberFilters filters) {
            // #212895: cannot wait for getChildren() forever, as it may involve parsing;
            // wait for a short while, then create a wait node
            ChildR r = new ChildR(
                    this,
                    parentDescription,
                    ui.getFilters());
            Task t = CHILD_RP.post(r);
            try {
                t.waitFinished(WAIT_PERIOD);
                List<StructureItem> items = r.getNestedItemsIfReady();
                if (items != null) {
                    LOG.log(Level.FINE, "Got nested items within limit for: {0}", this);
                    resetKeys(processing, items, filters);
                }
            } catch (InterruptedException ex) {
                // no op
            }
            synchronized (this) {
                if (!initialized) {
                    // only if never initialized, otherwise keep the current content
                    setKeys(new StructureItem[] { WAIT_KEY });
                }
            }
        }
        
        synchronized void resetKeys(int key, List<StructureItem> descriptions, ClassMemberFilters filters ) {
            if (processing != key) {
                return;
            }
            this.keys = descriptions;
            initialized = true;
            setKeys( filters.filter(descriptions) );
        }
    }
        
    /**
     * This Runnable actually acquires nested items and reset children
     * keys.
     */
    private static class ChildR implements Runnable {
        private final ElementChildren children;
        private final StructureItem description;
        private final ClassMemberFilters filters;
        private List<StructureItem> nestedItems;
        private boolean delayed;

        public ChildR(ElementChildren children, StructureItem description, ClassMemberFilters filters) {
            this.children = children;
            this.description = description;
            this.filters = filters;
        }
        
        synchronized List<StructureItem> getNestedItemsIfReady() {
            if (nestedItems != null) {
                return nestedItems;
            } else {
                delayed = true;
                return null;
            }
        }
        
        public void run() {
            int p = children.processing;
            this.nestedItems = (List<StructureItem>)description.getNestedItems();
            synchronized (this) {
                if (!delayed) {
                    return;
                }
            }
            children.resetKeys(p, nestedItems, filters);
        }
    }
    
    /** Stores all interesting data about given element.
     */    
    static class Description {
        
        public static final Comparator<StructureItem> ALPHA_COMPARATOR =
            new DescriptionComparator(true);
        public static final Comparator<StructureItem> POSITION_COMPARATOR = 
            new DescriptionComparator(false);    
        
        ClassMemberPanelUI ui;
                
        //FileObject fileObject; // For the root description
        
        String name;
        ElementHandle elementHandle;
        ElementKind kind;
        Set<Modifier> modifiers;        
        List<Description> subs; 
        String htmlHeader;
        long pos;
        
        Description( ClassMemberPanelUI ui ) {
            this.ui = ui;
        }
                                
        @Override
        public boolean equals(Object o) {
                        
            if ( o == null ) {
                //System.out.println("- f nul");
                return false;
            }
            
            if ( !(o instanceof Description)) {
                // System.out.println("- not a desc");
                return false;
            }
            
            Description d = (Description)o;
            
            if ( kind != d.kind ) {
                // System.out.println("- kind");
                return false;
            }
            
            // Findbugs warns about this field being uninitialized on the following line!
            if ( !name.equals(d.name) ) {
                // System.out.println("- name");
                return false;
            }

//            if ( !this.elementHandle.signatureEquals(d.elementHandle) ) {
//                return false;
//            }
            
            /*
            if ( !modifiers.equals(d.modifiers)) {
                // E.println("- modifiers");
                return false;
            }
            */
            
            // System.out.println("Equals called");            
            return true;
        }
        
        
        @Override
        public int hashCode() {
            int hash = 7;

            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }

        private static class DescriptionComparator implements Comparator<StructureItem> {
            
            boolean alpha;
            
            DescriptionComparator( boolean alpha ) {
                this.alpha = alpha;
            }
            
            public int compare(StructureItem d1, StructureItem d2) {
                if ( alpha ) {
                    if ( k2i(d1.getKind()) != k2i(d2.getKind()) ) {
                        return k2i(d1.getKind()) - k2i(d2.getKind());
                    } 
                    
                    return d1.getSortText().compareTo(d2.getSortText());
                }
                else {
                    return d1.getPosition() == d2.getPosition() ? 0 : d1.getPosition() < d2.getPosition() ? -1 : 1;
                }
            }
            
            int k2i( ElementKind kind ) {
                switch( kind ) {
                    case CONSTRUCTOR:
                        return 1;
                    case METHOD:
                    case DB:
                        return 2;
                    case FIELD:
                        return 3;
                    case CLASS:
                    case INTERFACE:
//                    case ENUM:
//                    case ANNOTATION_TYPE:                        
//                        return 4;
                        
                        // TODO - what about other types?
                    default:
                        return 100;
                }
            }
        }
        
    }
        
    private static class WaitNode extends ElementNode {
        
        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/wait.gif"); // NOI18N
        private String displayName;
        
        WaitNode( ) {
            super( Children.LEAF );
            displayName = NbBundle.getMessage(ElementNode.class, "LBL_WaitNode");
            getCookieSet().assign(WaitNode.class, this);
        }
        
        @Override
        public Image getIcon(int type) {
             return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return displayName;
        }

        @Override
        public String getHtmlDisplayName() {
            return displayName;
        }
    }
    
    private static class NavigatorFormatter extends GsfHtmlFormatter {
        @Override
        public void name(ElementKind kind, boolean start) {
            // No special formatting for names
        }
    }
    
    public static boolean isWaitNode(Node n) {
        return n != null && n.getLookup().lookup(WaitNode.class) != null;
    }
    
    public static boolean isWaitNode(StructureItem si) {
        return si == WAIT_KEY;
    }
}
