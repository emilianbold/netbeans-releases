/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ShowDocAction.java
 *
 * Created on 3. leden 2001, 11:23
 */

package org.netbeans.modules.javadoc.search;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.java.JavaDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

/**
 *  On selected node try to find generated and mounted documentation
 * @author  Petr Suchomel
 * @version 1.0
 */
public class ShowDocAction extends CookieAction {

    static final long serialVersionUID =3578357584245478L;
    
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle( ShowDocAction.class ).getString ("CTL_SHOWDOC_MenuItem");   //NOI18N
    }

    /** Cookie classes contains one class returned by cookie () method.
    */
    protected final Class[] cookieClasses () {
        return new Class[] { JavaDataObject.class };
    }

    /** All must be DataFolders or JavaDataObjects
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ShowDocAction.class);
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( Node[] nodes ) {
        IndexSearch indexSearch = IndexSearch.getDefault();
                
        if( nodes.length == 1 && nodes[0] != null ) {
            String toFind = findTextFromNode(nodes[0]);
            if (toFind != null)
                indexSearch.setTextToFind( toFind );
        }
        indexSearch.open ();
        indexSearch.requestFocus();        
    }

    protected String iconResource(){
        return "org/netbeans/modules/javadoc/resources/showjavadoc.gif"; //NOI18N
    }
    
    /**
     * Attempts to find a suitable text from the node. 
     */
    private String findTextFromNode(Node n) {
        EditorCookie ec = (EditorCookie)n.getCookie(EditorCookie.class);
        // no editor underneath the node --> node's name is the only searchable text.
        if (ec != null) {
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null) {
                TopComponent activetc = TopComponent.getRegistry().getActivated();
                for (int i = 0; i < panes.length; i++) {
                    if (activetc.isAncestorOf(panes[i])) {
                        // we have found the correct JEditorPane
                        String s = extractTextFromPane(panes[i]);
                        if (s != null)
                            return s;
                        else
                            break;
                    }
                }
            }
        }
        return n.getName();
    }
    
    private String extractTextFromPane(JEditorPane p) {
        int selStart = p.getSelectionStart();
        int selEnd = p.getSelectionEnd();
        try {
            if (selEnd > selStart) {
                // read the non-empty selection
                return p.getDocument().getText(selStart, selEnd - selStart);
            }
            // try to guess which word is underneath the caret's dot.
            Document doc = p.getDocument();
            Element lineRoot;

            if (doc instanceof StyledDocument) {
                lineRoot = NbDocument.findLineRootElement((StyledDocument)doc);
            } else {
                lineRoot = doc.getDefaultRootElement();
            }
            int dot = p.getCaret().getDot();
            Element line = lineRoot.getElement(lineRoot.getElementIndex(dot));
            String contents;
            if (line == null)
                return null;
            dot -= line.getStartOffset();
            contents = doc.getText(line.getStartOffset(), 
                line.getEndOffset() - line.getStartOffset());
            // search forwards and backwards for the identifier boundary:
            int begin = dot; 
            int end;
            if (begin < contents.length() && Character.isJavaIdentifierPart(contents.charAt(begin))) {
                while (begin > 0 && Character.isJavaIdentifierPart(contents.charAt(begin - 1)))
                    begin--;
                end = dot + 1;
            } else {
                while (begin < contents.length() &&
                    !Character.isJavaIdentifierStart(contents.charAt(begin)))
                    begin++;
                end = begin + 1;
            }
            if (begin >= contents.length())
                return null;
            while (end < contents.length() &&
                Character.isJavaIdentifierStart(contents.charAt(end)))
                end++;
            return contents.substring(begin, end);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
