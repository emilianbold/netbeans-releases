/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.navigation;


import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Action;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.OpenAction;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Node representing an Element
 *
 * @author Petr Hrebejk
 */
public class ElementNode extends AbstractNode {

    
    private static Node WAIT_NODE;
    
    private OpenAction openAction;
    private Description description;
           
    /** Creates a new instance of TreeNode */
    public ElementNode( Description description ) {
        super(description.subs == null ? Children.LEAF: new ElementChilren(description.subs, description.ui.getFilters()), description.treePathHandle==null ? null : prepareLookup(description));
        this.description = description;
        setDisplayName( description.name ); 
    }
    
    @Override
    public Image getIcon(int type) {
         return description.kind == null ? super.getIcon(type) : ImageUtilities.icon2Image(ElementIcons.getElementIcon(description.kind, description.modifiers));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
                   
    @Override
    public java.lang.String getDisplayName() {
        if (description.name != null) {
            return description.name;
        }
        if (description.fileObject != null) {
            return description.fileObject.getNameExt();
        }
        return null;
    }
            
    @Override
    public String getHtmlDisplayName() {
        return description.htmlHeader;
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        
        if ( context || description.name == null ) {
            return description.ui.getActions();
        }
        else {
            Action panelActions[] = description.ui.getActions();
            
            Action actions[]  = new Action[ 4 + panelActions.length ];
            actions[0] = getOpenAction();
            actions[1] = RefactoringActionsFactory.whereUsedAction();
            actions[2] = RefactoringActionsFactory.popupSubmenuAction();
            actions[3] = null;
            for( int i = 0; i < panelActions.length; i++ ){
                actions[4 + i] = panelActions[i];
            }
            return actions;
        }
    }        
    
    @Override
    public Action getPreferredAction() {
        return getOpenAction();
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    @Override
    public Transferable drag() throws IOException {
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        // Do nothing
    }
        
    private synchronized Action getOpenAction() {
        if ( openAction == null ) {
            FileObject fo = description.getFileObject();
            openAction = new OpenAction(description.elementHandle, fo, description.name);
        }
        return openAction;
    }
    
    static synchronized Node getWaitNode() {
        if ( WAIT_NODE == null ) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }
    
    public void refreshRecursively() {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {
            boolean scrollOnExpand = description.ui.getScrollOnExpand();
            description.ui.setScrollOnExpand( false );
           ((ElementChilren)ch).resetKeys(description.subs, description.ui.getFilters());
           for( Node sub : ch.getNodes() ) {
               description.ui.expandNode(sub);
               ((ElementNode)sub).refreshRecursively();
           }
           description.ui.setScrollOnExpand( scrollOnExpand );
        }        
    }
    
    public ElementNode getNodeForElement( ElementHandle<Element> eh ) {
        
        if ( getDescritption().elementHandle != null &&
             eh.signatureEquals(getDescritption().elementHandle)) {
            return this;
        }
        
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {
           for( Node sub : ch.getNodes() ) {
               ElementNode result = ((ElementNode)sub).getNodeForElement(eh);
               if ( result != null ) {
                   return result;
               }
           }
        }
        
        return null;
    }
    
    public void updateRecursively( Description newDescription ) {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {           
           HashSet<Description> oldSubs = new HashSet<Description>( description.subs );

           
           // Create a hashtable which maps Description to node.
           // We will then identify the nodes by the description. The trick is 
           // that the new and old description are equal and have the same hashcode
           Node[] nodes = ch.getNodes( true );           
           HashMap<Description,ElementNode> oldD2node = new HashMap<Description,ElementNode>();           
           for (Node node : nodes) {
               oldD2node.put(((ElementNode)node).description, (ElementNode)node);
           }
           
           // Now refresh keys
           ((ElementChilren)ch).resetKeys(newDescription.subs, newDescription.ui.getFilters());

           
           // Reread nodes
           nodes = ch.getNodes( true );
           
           for( Description newSub : newDescription.subs ) {
                ElementNode node = oldD2node.get(newSub);
                if ( node != null ) { // filtered out
                    if ( !oldSubs.contains(newSub) && node.getChildren() != Children.LEAF) {                                           
                        description.ui.expandNode(node); // Make sure new nodes get expanded
                    }     
                    node.updateRecursively( newSub ); // update the node recursively
                }
           }
        }
                        
        Description oldDescription = description; // Remember old description        
        description = newDescription; // set new descrioption to the new node
        if ( oldDescription.htmlHeader != null && !oldDescription.htmlHeader.equals(description.htmlHeader)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldDescription.htmlHeader, description.htmlHeader);
        }
        if( oldDescription.modifiers != null &&  !oldDescription.modifiers.equals(newDescription.modifiers)) {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    
    public Description getDescritption() {
        return description;
    }

    private static Lookup prepareLookup(Description d) {
        InstanceContent ic = new InstanceContent();

        ic.add(d.treePathHandle);
        ic.add(d, new InstanceContent.Convertor<Description, FileObject>() {
            public FileObject convert(Description d) {
                return d.getFileObject();
            }
            public Class<? extends FileObject> type(Description obj) {
                return FileObject.class;
            }
            public String id(Description obj) {
                return "IL[" + obj.toString();
            }
            public String displayName(Description obj) {
                return id(obj);
            }
        });

        return new AbstractLookup(ic);
    }
    
    private static final class ElementChilren extends Children.Keys<Description> {
            
        public ElementChilren(List<Description> descriptions, ClassMemberFilters filters ) {
            resetKeys( descriptions, filters );            
        }
        
        protected Node[] createNodes(Description key) {
            return new Node[] {new  ElementNode(key)};
        }
        
        void resetKeys( List<Description> descriptions, ClassMemberFilters filters ) {            
            setKeys( filters.filter(descriptions) );
        }
        
        
                        
    }
                       
    /** Stores all interesting data about given element.
     */    
    static class Description {
        
        public static final Comparator<Description> ALPHA_COMPARATOR =
            new DescriptionComparator(true);
        public static final Comparator<Description> POSITION_COMPARATOR = 
            new DescriptionComparator(false);    
        
        ClassMemberPanelUI ui;
                
        FileObject fileObject; // For the root description
        
        final String name;
        final ElementHandle<? extends Element> elementHandle;
        final TreePathHandle treePathHandle;
        final ElementKind kind;
        Set<Modifier> modifiers;        
        List<Description> subs; 
        String htmlHeader;
        long pos;
        boolean isInherited;
        ClasspathInfo cpInfo;
        
        Description( ClassMemberPanelUI ui ) {
            this.ui = ui;
            this.name = null;
            this.elementHandle = null;
            this.treePathHandle = null;
            this.kind = null;
            this.isInherited = false;
        }
         
        Description(ClassMemberPanelUI ui,
                    String name,
                    ElementHandle<? extends Element> elementHandle,
                    ElementKind kind,
                    TreePathHandle tpHandle,
                    boolean inherited ) {
            this.ui = ui;
            this.name = name;
            this.elementHandle = elementHandle;
            this.kind = kind;
            this.isInherited = inherited;
            this.treePathHandle = tpHandle;
        }

        public FileObject getFileObject() {
            if( !isInherited )
                return ui.getFileObject();
            return SourceUtils.getFile( elementHandle, cpInfo );
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
            
            if ( !name.equals(d.name) ) {
                // System.out.println("- name");
                return false;
            }
            
            if ( !this.elementHandle.signatureEquals(d.elementHandle) ) {
                return false;
            }
            
            /*
            if ( !modifiers.equals(d.modifiers)) {
                // E.println("- modifiers");
                return false;
            }
            */
            
            // System.out.println("Equals called");            
            return true;
        }
        
        
        public int hashCode() {
            int hash = 7;

            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }
        
        private static class DescriptionComparator implements Comparator<Description> {
            
            boolean alpha;
            
            DescriptionComparator( boolean alpha ) {
                this.alpha = alpha;
            }
            
            public int compare(Description d1, Description d2) {
                
                if ( alpha ) {
                    return alphaCompare( d1, d2 );
                }
                else {
                    if( d1.isInherited && !d2.isInherited )
                        return 1;
                    if( !d1.isInherited && d2.isInherited )
                        return -1;
                    if( d1.isInherited && d2.isInherited ) {
                        return alphaCompare( d1, d2 );
                    }
                    return d1.pos == d2.pos ? 0 : d1.pos < d2.pos ? -1 : 1;
                }
            }
            
            int alphaCompare( Description d1, Description d2 ) {
                if ( k2i(d1.kind) != k2i(d2.kind) ) {
                    return k2i(d1.kind) - k2i(d2.kind);
                } 

                return d1.name.compareTo(d2.name);
            }
            
            int k2i( ElementKind kind ) {
                switch( kind ) {
                    case CONSTRUCTOR:
                        return 1;
                    case METHOD:
                        return 2;
                    case FIELD:
                        return 3;
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case ANNOTATION_TYPE:                        
                        return 4;
                    default:
                        return 100;
                }
            }
            
        }
        
    }
        
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/java/navigation/resources/wait.gif"); // NOI18N
        
        WaitNode( ) {
            super( Children.LEAF );
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
            return NbBundle.getMessage(ElementNode.class, "LBL_WaitNode"); // NOI18N
        }
        
    }
    
    
}
