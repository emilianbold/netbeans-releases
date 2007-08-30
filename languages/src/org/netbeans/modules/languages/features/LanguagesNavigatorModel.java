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

import javax.swing.event.TreeModelListener;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
class LanguagesNavigatorModel implements TreeModel {

    private NbEditorDocument            document;
    private ASTNode                     astNode;
    private NavigatorNode               root;
    private Line.Set                    lineSet;
    private EventListenerList           listenerList = new EventListenerList ();

    private Map<ASTItem,ASTNavigatorNode> astToNode = new WeakHashMap<ASTItem,ASTNavigatorNode> ();
    private static NavigatorComparator  navigatorComparator;


    // TreeModel implementation ............................................

    LanguagesNavigatorModel () {
        root = new NavigatorNode (
            "","", null, true
        );
    }
    
    public Object getRoot () {
        return root;
    }

    public Object getChild (Object parent, int index) {
        return ((NavigatorNode) parent).getNodes ().get (index);
    }

    public int getChildCount (Object parent) {
        return ((NavigatorNode) parent).getNodes ().size ();
    }

    public boolean isLeaf (Object node) {
        return ((NavigatorNode) node).getNodes ().isEmpty ();
    }

    public void valueForPathChanged (TreePath path, Object newValue) {
    }

    public int getIndexOfChild (Object parent, Object child) {
        return ((NavigatorNode) parent).getNodes ().indexOf (child);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listenerList.add (TreeModelListener.class, l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listenerList.remove (TreeModelListener.class, l);
    }


    // other methods .......................................................

    void setContext (
        Line.Set            lineSet,
        NbEditorDocument    doc
    ) {
        this.lineSet = lineSet;
        this.document = doc;
        if (doc == null) {
            root = new NavigatorNode ("", "", null, true);
            astNode = null;
            setParserManager (null);
            fire ();
            return;
        }
        ParserManager parserManager = ParserManager.get (doc);
        setParserManager (parserManager);
        refreshASTNode ();
    }
    
    private ParserListener      parserListener;
    private ParserManager       parserManager;
    
    private void setParserManager (ParserManager parserManager) {
        if (parserManager == this.parserManager) return;
        if (parserListener == null)
            parserListener = new ParserListener ();
        if (this.parserManager != null)
            this.parserManager.removeListener (parserListener);
        if (parserManager != null)
            parserManager.addListener (parserListener);
        this.parserManager = parserManager;
    }
    
    private void refreshASTNode () {
        try {
            astNode = parserManager.getAST ();
        } catch (ParseException ex) {
            astNode = ex.getASTNode ();
        }
        if (astNode == null)
            root = new NavigatorNode ("", "", null, true);
        else {
            List<ASTItem> path = new ArrayList<ASTItem> ();
            path.add (astNode);
            root = new ASTNavigatorNode (document, astNode, path, "Root", "", null, false);
        }
        fire ();
    }
    
    private void fire () {
        TreeModelListener[] listeners = listenerList.getListeners (TreeModelListener.class);
        if (listeners.length == 0) return;
        TreeModelEvent e = new TreeModelEvent (this, new Object[] {getRoot ()});
        for (int i = 0; i < listeners.length; i++)
            listeners [i].treeStructureChanged (e);
    }

    private boolean cancel () {
        return parserManager.getState () == State.PARSING;
    }

    TreePath getTreePath (int position) {
        if (astNode == null) return null;
        if (!(root instanceof ASTNavigatorNode)) return null;
        ASTPath astPath = astNode.findPath (position);
        if (astPath == null) return null;
        List<ASTNavigatorNode> nodePath = new ArrayList<ASTNavigatorNode> ();
        ASTNavigatorNode n = (ASTNavigatorNode) root;
        Iterator<ASTItem> it = astPath.listIterator ();
        if (it.next () != n.item) return null;
        nodePath.add (n);
        while (it.hasNext ()) {
            ASTItem astItem = it.next ();
            Iterator<ASTNavigatorNode> it2 = n.getNodes ().iterator ();
            while (it2.hasNext ()) {
                ASTNavigatorNode nn = it2.next ();
                if (nn.item != astItem) continue;
                n = nn;
                nodePath.add (nn);
                break;
            }
        }
        if (nodePath.isEmpty ()) return null;
        return new TreePath (nodePath.toArray ());
    }
    
    
    
    String getTooltip (Object node) {
        return ((NavigatorNode) node).tooltip;
    }

    void show (Object node) {
        ((NavigatorNode) node).show ();
    }
    
    String getIcon (Object node) {
        return ((NavigatorNode) node).icon;
    }
    
    String getDisplayName (Object node) {
        return ((NavigatorNode) node).displayName;
    }


    // innerclasses ........................................................

    class ParserListener implements ParserManagerListener {
        
        public void parsed (State state, ASTNode ast) {
            if (state == State.PARSING) return;
            refreshASTNode ();
        }
    }

    static class NavigatorNode {

        String          displayName;
        String          tooltip;
        String          icon;
        boolean         isLeaf;

        /** Creates a new instance of NavigatorNode */
        NavigatorNode (
            String          displayName,
            String          tooltip,
            String          icon,
            boolean         isLeaf
        ) {
            this.displayName = displayName;
            this.tooltip =  tooltip;
            this.icon =     icon;
            this.isLeaf =   isLeaf;
        }
        
        void show () {}
        
        List<ASTNavigatorNode> getNodes () {
            return Collections.<ASTNavigatorNode>emptyList ();
        }
    }
    
    static class ASTNavigatorNode extends NavigatorNode {

        ASTItem         item;
        List<ASTItem>   path;
        private StyledDocument document;

        /** Creates a new instance of NavigatorNode */
        ASTNavigatorNode (
            StyledDocument  document,
            ASTItem         item,
            List<ASTItem>   path,
            String          displayName,
            String          tooltip,
            String          icon,
            boolean         isLeaf
        ) {
            super (displayName, tooltip, icon, isLeaf);
            this.document = document;
            this.item =     item;
            this.path =     path;
        }
        
        void show () {
            DataObject dataObject = NbEditorUtilities.getDataObject (document);
            LineCookie lineCookie = dataObject.getCookie (LineCookie.class);
            Line.Set lineSet = lineCookie.getLineSet ();
            Line line = lineSet.getCurrent (NbDocument.findLineNumber (document, item.getOffset ()));
            int column = NbDocument.findLineColumn (document, item.getOffset ());
            line.show (Line.SHOW_GOTO, column);
        }

        private List<ASTNavigatorNode> nodes;
        
        List<ASTNavigatorNode> getNodes () {
            if (nodes != null) return nodes;
            if (isLeaf)
                return nodes = Collections.<ASTNavigatorNode>emptyList ();
            nodes = new ArrayList<ASTNavigatorNode> ();
            getNavigatorNodes (
                item, 
                new ArrayList<ASTItem> (path), 
                nodes
            );
            try {
                Language language = LanguagesManager.getDefault ().
                    getLanguage (item.getMimeType ());
                Feature properties = language.getFeature ("PROPERTIES");
                if (properties != null &&
                    properties.getBoolean ("navigator-sort", false)
                ) {
                    if (navigatorComparator == null)
                        navigatorComparator = new NavigatorComparator ();
                    Collections.<ASTNavigatorNode>sort (nodes, navigatorComparator);
                }
            } catch (ParseException ex) {
            }
            return nodes;
        }

        private void getNavigatorNodes (
            ASTItem             item, 
            List<ASTItem>       path, 
            List<ASTNavigatorNode> nodes

        ) {
            Iterator<ASTItem> it = item.getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item2 = it.next ();
                path.add (item2);
                ASTNavigatorNode navigatorNode = createNavigatorNode (
                    item2,
                    path
                );
                if (navigatorNode != null) 
                    nodes.add (navigatorNode);
                else
                    getNavigatorNodes (item2, path, nodes);
                path.remove (path.size () - 1);
            }
            return;
        }

        private ASTNavigatorNode createNavigatorNode (
            ASTItem             item,
            List<ASTItem>       path
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
            Context context = SyntaxContext.create (document, astPath);
            String displayName = (String) navigator.getValue ("display_name", context);
            if (displayName == null || displayName.trim().length() == 0) {
                return null;
            }
            String tooltip = (String) navigator.getValue ("tooltip", context);
            String icon = (String) navigator.getValue ("icon", context);
            if (icon == null)
                icon = "org/netbeans/modules/languages/resources/node.gif";
            boolean isLeaf = navigator.getBoolean ("tooltip", context, false);
            return new ASTNavigatorNode (
                document,
                item,
                new ArrayList<ASTItem> (path),
                displayName, tooltip, icon,
                isLeaf
            );
        }
    }
    
    static class NavigatorComparator implements Comparator<NavigatorNode> {
        public int compare (NavigatorNode o1, NavigatorNode o2) {
            return o1.displayName.compareToIgnoreCase (o2.displayName);
        }
    }
}
