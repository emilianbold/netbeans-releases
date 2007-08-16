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

package org.netbeans.modules.languages.features;


import java.awt.Component;
import java.awt.Image;
import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.loaders.DataObject;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesNavigator implements NavigatorPanel {

    /** holds UI of this panel */
    private JComponent          panelUI;
    private JTree               tree;
    private NbEditorDocument    lastDocument;
    private JEditorPane         lastEditor;
    private DocumentListener    parserListener;
    private DataObject          dataObject;
    private MyLookupListener    lookupListener;
    

    public String getDisplayHint () {
        return "This is Navigator";
    }

    public String getDisplayName () {
        return "Navigator";
    }

    public JComponent getComponent () {
        if (panelUI == null) {
            tree = new JTree () {
                public String getToolTipText (MouseEvent ev) {
                    TreePath selPath = tree.getPathForLocation 
                        (ev.getX (), ev.getY ());
                    if (selPath == null) return null;
                    Object selObj = selPath.getLastPathComponent ();
                    if (selObj == null || !(selObj instanceof NavigatorNode))
                        return null;
                    NavigatorNode node = (NavigatorNode)selObj;
                    return node.tooltip;
                }
            };
            ToolTipManager.sharedInstance ().registerComponent (tree);
            tree.setRootVisible (false);
            tree.setShowsRootHandles (true);
            tree.addMouseListener (new Listener ());
            tree.setToggleClickCount(Integer.MAX_VALUE); // [PENDING]
            /*
            tree.addTreeSelectionListener (new TreeSelectionListener () {
                public void valueChanged (TreeSelectionEvent e) {
                    selectionChanged ();
                }
            });
            tree.addFocusListener (new FocusListener () {
                public void focusGained (FocusEvent e) {
                    selectionChanged ();
                }
                public void focusLost (FocusEvent e) {
                    selectionChanged ();
                }
            });
            */
            panelUI = new JScrollPane (tree);
        }
        return panelUI;
    }

    public void panelActivated (Lookup context) {
        Result<DataObject> result = context.<DataObject>lookupResult (DataObject.class);
        if (lookupListener != null)
            lookupListener.remove ();
        lookupListener = new MyLookupListener (result);
        dataObject = result.allInstances ().iterator ().next ();
        refresh ();
    }

    public void panelDeactivated () {
        if (lastEditor != null)
            lastEditor.removeCaretListener (parserListener);
        lastEditor = null;
        if (lastDocument != null) {
            ParserManager lastPM = ParserManagerImpl.get (lastDocument);
            lastPM.removeListener (parserListener);
            lastDocument = null;
        }
        dataObject = null;
        if (lookupListener != null)
            lookupListener.remove ();
    }
    
    public Lookup getLookup () {
        return null;
    }
    
    
    // other methods ...........................................................
        
    private void refresh () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                EditorCookie ec = null;
                LineCookie lc = null;
                try {
                    ec = dataObject.getCookie (EditorCookie.class);
                    if (ec == null) return;
                    lc = dataObject.getCookie (LineCookie.class);
                } catch (NullPointerException e) {
                    // dataObject == null, panelDeactivated() called
                    return;
                }
                try {
                    NbEditorDocument document = (NbEditorDocument) ec.openDocument ();
                    ASTNode ast = null;
                    ParserManager parserManager = ParserManagerImpl.get(document);
                    if (parserManager != null) {
                        try {
                            ast = parserManager.getAST ();
                        } catch (ParseException ex) {
                            ast = ex.getASTNode ();
                        }
                    }
                    if (parserListener == null)
                        parserListener = new DocumentListener ();
                    if (lastEditor != null)
                        lastEditor.removeCaretListener (parserListener);
                    if (ec.getOpenedPanes () != null && 
                        ec.getOpenedPanes ().length > 0
                    ) {
                        lastEditor = ec.getOpenedPanes () [0];
                        lastEditor.addCaretListener (parserListener);
                    } else
                        lastEditor = null;
                    if (lastDocument != document) {
                        if (lastDocument != null) {
                            ParserManager lastPM = ParserManagerImpl.get (lastDocument);
                            if (lastPM != null) {
                                lastPM.removeListener (parserListener);
                            }
                        }
                        if (parserManager != null)
                            parserManager.addListener (parserListener);
                        lastDocument = document;
                    }
                    getComponent ();
                    if (ast != null) {
                        Model model = new Model ();
                        model.setContext (ast, lc.getLineSet (), document);
                        tree.setModel (model);
                    } else if (parserManager == null) {
                        tree.setModel (new DefaultTreeModel (new DefaultMutableTreeNode ()));
                    } else {
                        DefaultMutableTreeNode root = new DefaultMutableTreeNode ();
                        State state = parserManager.getState ();
                        if (state == State.PARSING) {
                            root.add (new DefaultMutableTreeNode ("Parsing ..."));
                        } else 
                        if (state != State.NOT_PARSED) {
                            root.add (new DefaultMutableTreeNode ("?!?!"));
                        }
                        tree.setModel (new DefaultTreeModel (root));
                    }
                    tree.setCellRenderer (new Renderer ());

                    while (tree.getRowCount () < 50) {
                        int c = tree.getRowCount ();
                        int i, k = tree.getRowCount ();
                        for (i = 0; i < k; i++)
                            tree.expandRow (i);
                        if (tree.getRowCount () == c) break;
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        });
    }

    private static NavigatorNode createNavigatorNode (
        ASTItem             item,
        List<ASTItem>       path,
        Line.Set            lineSet,
        NbEditorDocument    doc
    ) {
        ASTPath astPath = ASTPath.create (path);
        Feature navigator = null;
        try {
            Language language = LanguagesManager.getDefault ().
                getLanguage (item.getMimeType ());
            navigator = language.getFeature ("NAVIGATOR", astPath);
        } catch (ParseException ex) {
            return null;
        }
        if (navigator == null) return null;
        Line line = lineSet != null ? 
            lineSet.getCurrent (NbDocument.findLineNumber (doc, item.getOffset ())) 
            : null;
        int column = NbDocument.findLineColumn (doc, item.getOffset ());
        Context context = SyntaxContext.create (doc, astPath);
        String displayName = (String) navigator.getValue ("display_name", context);
        if (displayName == null || displayName.trim().length() == 0) {
            return null;
        }
        String tooltip = (String) navigator.getValue ("tooltip", context);
        String icon = (String) navigator.getValue ("icon", context);
        if (icon == null)
            icon = "org/netbeans/modules/languages/resources/node.gif";
        boolean isLeaf = navigator.getBoolean ("tooltip", context, false);
        return new NavigatorNode (
            item,
            new ArrayList<ASTItem> (path),
            line, column, 
            displayName, tooltip, icon,
            isLeaf
        );
    }
    
    private static Map<String,Icon> icons = new HashMap<String,Icon> ();
    
    private static Icon getCIcon (String resourceName) {
        if (!icons.containsKey (resourceName)) {
            Image image = Utilities.loadImage (resourceName);
            if (image == null)
                image = Utilities.loadImage (
                    "org/netbeans/modules/languages/resources/node.gif"
                );
            icons.put (
                resourceName,
                new ImageIcon (image)
            );
        }
        return icons.get (resourceName);
    }
    
    private void markSelected (int position) {
        if (!(tree.getModel () instanceof Model)) return;
        Model model = (Model) tree.getModel ();
        ASTPath astPath = model.root.findPath (position);
        if (astPath == null) return;
        List<NavigatorNode> nodePath = new ArrayList<NavigatorNode> ();
        Iterator it = astPath.listIterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            NavigatorNode nn = model.astToNode.get (o);
            if (nn == null) continue;
            nodePath.add (nn);
        }
        if (nodePath.isEmpty ()) return;
        TreePath treePath = new TreePath (nodePath.toArray ());
        tree.setSelectionPath (treePath);
        tree.scrollPathToVisible (treePath);
    }

    // highlight selected node in editor ...
    
//    private Document        highlightedDocument = null;
//    private Object          highlighted = null;
//    private JEditorPane     highlightedEditor = null;
//
//    private void selectionChanged () {
//        removeHighlight ();
//        if (!tree.hasFocus ()) return;
//        TreePath selPath = tree.getSelectionPath ();
//        if (selPath == null) return;
//        Object selObj = selPath.getLastPathComponent ();
//        if (selObj == null || !(selObj instanceof NavigatorNode))
//            return;
//        NavigatorNode node = (NavigatorNode)selObj;
//        if (node.line == null) return;
//        node.line.show (Line.SHOW_SHOW, node.column);
//        //S ystem.out.println ("highlight " + lastDocument + " : " + lastEditor);
//        highlighted = node.item;
//        Highlighting.getHighlighting (highlightedDocument = lastDocument).
//            highlight (node.item, getHighlightAS ());
//        DataObject dataObject = (DataObject) node.line.getLookup ().
//            lookup (DataObject.class);
//        EditorCookie ec = (EditorCookie) dataObject.getCookie 
//            (EditorCookie.class);
//        highlightedEditor = ec.getOpenedPanes () [0];
//        highlightedEditor.repaint ();
//    }
//    
//    private void removeHighlight () {
//        if (highlighted == null) return;
//        if (highlighted instanceof ASTToken)
//            Highlighting.getHighlighting (highlightedDocument).removeHighlight 
//                ((ASTToken) highlighted);
//        else
//            Highlighting.getHighlighting (highlightedDocument).removeHighlight 
//                ((ASTNode) highlighted);
//        highlightedEditor.repaint ();
//        highlighted = null;
//        highlightedDocument = null;
//        highlightedEditor = null;
//    }
//    
//    private static AttributeSet highlightAS = null;
//    
//    private static AttributeSet getHighlightAS () {
//        if (highlightAS == null) {
//            SimpleAttributeSet as = new SimpleAttributeSet ();
//            as.addAttribute (StyleConstants.Background, Color.yellow); //new Color (230, 230, 230));
//            highlightAS = as;
//        }
//        return highlightAS;
//    }
    
    
    // innerclasses ............................................................
    
    static class Model implements TreeModel {
        
        private NbEditorDocument            doc;
        private ASTNode                     root;
        private Line.Set                    lineSet;
        
        private Map<NavigatorNode,List<NavigatorNode>>  nodeToNodes = new WeakHashMap<NavigatorNode,List<NavigatorNode>> ();
        private Map<ASTItem,NavigatorNode>  astToNode = new WeakHashMap<ASTItem,NavigatorNode> ();
        private NavigatorComparator         navigatorComparator;
        
        
        public Object getRoot () {
            if (astToNode.get (root) == null) {
                List<ASTItem> path = new ArrayList<ASTItem> ();
                path.add (root);
                NavigatorNode navigatorNode = new NavigatorNode (
                    root,
                    path,
                    null, 0, 
                    "root", null,
                    "org/netbeans/modules/languages/resources/node.gif",
                    false
                );
                astToNode.put (root, navigatorNode);
            }
            return astToNode.get (root);
        }

        public Object getChild (Object parent, int index) {
            return getNavigatorNodes ((NavigatorNode) parent).get (index);
        }

        public int getChildCount (Object parent) {
            return getNavigatorNodes ((NavigatorNode) parent).size ();
        }

        public boolean isLeaf (Object node) {
            return getNavigatorNodes ((NavigatorNode) node).isEmpty ();
        }

        public void valueForPathChanged (TreePath path, Object newValue) {
        }

        public int getIndexOfChild (Object parent, Object child) {
            return getNavigatorNodes ((NavigatorNode) parent).indexOf (child);
        }

        public void addTreeModelListener (TreeModelListener l) {
        }

        public void removeTreeModelListener (TreeModelListener l) {
        }

        
        // other methods .......................................................
        
        void setContext (
            ASTNode             root,
            Line.Set            lineSet,
            NbEditorDocument    doc
        ) {
            this.root = root;
            this.lineSet = lineSet;
            this.doc = doc;
        }
    
        private List<NavigatorNode> getNavigatorNodes (NavigatorNode n) {
            List<NavigatorNode> nodes = nodeToNodes.get (n);
            if (nodes == null) {
                if (n.isLeaf)
                    nodes = Collections.emptyList ();
                else {
                    nodes = getNavigatorNodes (
                        n.item, 
                        new ArrayList<ASTItem> (n.path), 
                        new ArrayList<NavigatorNode> ()
                    );
                    try {
                        Language language = LanguagesManager.getDefault ().
                            getLanguage (n.item.getMimeType ());
                        Feature properties = language.getFeature ("PROPERTIES");
                        if (properties != null &&
                            properties.getBoolean ("navigator-sort", false)
                        ) {
                            if (navigatorComparator == null)
                                navigatorComparator = new NavigatorComparator ();
                            Collections.<NavigatorNode>sort (nodes, navigatorComparator);
                        }
                    } catch (ParseException ex) {
                    }
                }
                nodeToNodes.put (n, nodes);
            }
            return nodes;
        }
    
        private List<NavigatorNode> getNavigatorNodes (
            ASTItem             item, 
            List<ASTItem>       path, 
            List<NavigatorNode> nodes
        ) {
            Iterator<ASTItem> it = item.getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item2 = it.next ();
                path.add (item2);
                NavigatorNode navigatorNode = astToNode.get (item2);
                if (navigatorNode == null) {
                    navigatorNode = createNavigatorNode (
                        item2,
                        path,
                        lineSet,
                        doc
                    );
                    astToNode.put (item2, navigatorNode);
                }
                if (navigatorNode != null) 
                    nodes.add (navigatorNode);
                else
                    getNavigatorNodes (item2, path, nodes);
                path.remove (path.size () - 1);
            }
            return nodes;
        }
    }
    
    static class Renderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent (
            JTree tree, Object value,
            boolean sel,
            boolean expanded,
            boolean leaf, int row,
            boolean hasFocus
        ) {
            JLabel l = (JLabel) super.getTreeCellRendererComponent (
                tree, value, sel, expanded, leaf, row, hasFocus
            );

            if (value instanceof DefaultMutableTreeNode) {
                l.setIcon (null);
                l.setText ((String) ((DefaultMutableTreeNode) value).getUserObject ());
                return l;
            }
            NavigatorNode node = (NavigatorNode) value;
            l.setIcon (getCIcon (node.icon));
            l.setText (node.displayName);
            return l;
        }
    }
    
    
    static class NavigatorNode {

        Line            line;
        int             column;
        String          displayName;
        String          tooltip;
        String          icon;
        ASTItem         item;
        List<ASTItem>   path;
        boolean         isLeaf;

        /** Creates a new instance of NavigatorNode */
        NavigatorNode (
            ASTItem         item,
            List<ASTItem>   path,
            Line            line,
            int             column,
            String          displayName,
            String          tooltip,
            String          icon,
            boolean         isLeaf
        ) {
            this.item =     item;
            this.path =     path;
            this.line =     line;
            this.column =   column;
            this.displayName = displayName;
            this.tooltip =  tooltip;
            this.icon =     icon;
            this.isLeaf =   isLeaf;
        }
    }
    
    static class NavigatorComparator implements Comparator<NavigatorNode> {
        public int compare (NavigatorNode o1, NavigatorNode o2) {
            return o1.displayName.compareToIgnoreCase (o2.displayName);
        }
    }
    
    class Listener implements MouseListener {
        
        public void mouseClicked (MouseEvent ev) {
            if (ev.getClickCount () != 2) return;
            TreePath selPath = tree.getPathForLocation 
                (ev.getX (), ev.getY ());
            if (selPath == null) return;
            Object selObj = selPath.getLastPathComponent ();
            if (selObj == null || !(selObj instanceof NavigatorNode))
                return;
            NavigatorNode node = (NavigatorNode)selObj;
            node.line.show (Line.SHOW_GOTO, node.column);
        }
        
        public void mouseEntered (MouseEvent e) {
        }
        public void mouseExited (MouseEvent e) {
        }
        public void mousePressed (MouseEvent e) {
        }
        public void mouseReleased (MouseEvent e) {
        }
    }
    
    class DocumentListener implements CaretListener, ParserManagerListener {
        public void parsed (State state, ASTNode ast) {
            if (state == State.PARSING) return;
            refresh ();
        }

        public void caretUpdate (CaretEvent e) {
            markSelected (e.getDot ());
        }
    }
    
    class MyLookupListener implements LookupListener {
        
        private Result<DataObject> result;

        MyLookupListener (Result<DataObject> result) {
            this.result = result;
            result.addLookupListener (this);
        }
        
        void remove () {
            result.removeLookupListener (this);
        }
        
        public void resultChanged (LookupEvent ev) {
            Iterator<? extends DataObject> it = result.allInstances ().iterator ();
            if (!it.hasNext ()) return;
            dataObject = it.next ();
            refresh ();
        }
    }
}
