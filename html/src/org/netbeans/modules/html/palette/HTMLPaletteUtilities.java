/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html.palette;
import java.awt.Component;
import java.awt.Container;
import java.util.StringTokenizer;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLTokenContext;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Libor Kotouc
 */
public final class HTMLPaletteUtilities {
    
    public static int wrapTags(HTMLSyntaxSupport sup, int start, int end, BaseDocument doc) {
        
        try {
            TokenItem token = sup.getTokenChain(start, start + 1);
            
            if (token == null)
                return end;
            
            while (token.getOffset() < end) { // interested only in the tokens inside the body
                token = token.getNext();
                if (token.getTokenID() == HTMLTokenContext.TAG_OPEN_SYMBOL) { // it's '<' token
                    int offset = token.getOffset();
                    doc.insertString(offset, "\n", null);   // insert a new-line before '<'
                    end++;  // remember new body end
                    token = sup.getTokenChain(offset + 1, offset + 2); // create new token chain reflecting changed document
                }
            }
            
        } catch (IllegalStateException ise) {
        } catch (BadLocationException ble) {
        }
        
        return end;
    }

    public static SourceGroup[] getSourceGroups(FileObject fObj) {
    
        Project proj = FileOwnerQuery.getOwner(fObj);
        SourceGroup[] sg = new SourceGroup[] {};
        if (proj != null) {
            Sources sources = (Sources)proj.getLookup().lookup(Sources.class);
            sg = sources.getSourceGroups("doc_root");
//            if (sg.length == 0)
//                sg = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        
        return sg;
    }

    public static JTree findTreeComponent(Component component) {
        if (component instanceof JTree) {
            return (JTree) component;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                JTree tree = findTreeComponent(components[i]);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    public static String getRelativePath(FileObject base, FileObject target) {
        
        final String DELIM = "/";
        final String PARENT = ".." + DELIM;
        
        String targetPath = target.getPath();
        String basePath = base.getPath();

        //paths begin either with '/' or with '<letter>:/' - ensure that in the latter case the <letter>s equal
        String baseDisc = basePath.substring(0, basePath.indexOf(DELIM));
        String targetDisc = targetPath.substring(0, targetPath.indexOf(DELIM));
        if (!baseDisc.equals(targetDisc))
            return ""; //different disc letters, thus returning an empty string to signalize this fact

        //cut a filename at the end taking last index for case of the same dir name as file name, really obscure but possible ;)
        basePath = basePath.substring(0, basePath.lastIndexOf(base.getNameExt()));
        targetPath = targetPath.substring(0, targetPath.lastIndexOf(target.getNameExt()));

        //iterate through prefix dirs until difference occurres
        StringTokenizer baseST = new StringTokenizer(basePath, DELIM);
        StringTokenizer targetST = new StringTokenizer(targetPath, DELIM);
        String baseDir = "";
        String targetDir = "";
        while (baseST.hasMoreTokens() && targetST.hasMoreTokens() && baseDir.equals(targetDir)) {
            baseDir = baseST.nextToken();
            targetDir = targetST.nextToken();
        }
        //create prefix consisting of parent dirs ("..")
        StringBuffer parentPrefix = new StringBuffer(!baseDir.equals(targetDir) ? PARENT : "");
        while (baseST.hasMoreTokens()) {
            parentPrefix.append(PARENT);
            baseST.nextToken();
        }
        //append remaining dirs with delimiter ("/")
        StringBuffer targetSB = new StringBuffer(!baseDir.equals(targetDir) ? targetDir + DELIM : "");
        while (targetST.hasMoreTokens())
            targetSB.append(targetST.nextToken() + DELIM);

        //resulting path
        targetPath = parentPrefix.toString() + targetSB.toString() + target.getNameExt();
        
        return targetPath;
    }

    public static void insert(String s, JTextComponent target) 
    throws BadLocationException 
    {
        insert(s, target, true);
    }
    
    public static void insert(String s, JTextComponent target, boolean reformat) 
    throws BadLocationException
    {

        if (s == null)
            s = "";
        
        EditorUI eui = Utilities.getEditorUI(target);
        BaseDocument doc = eui.getDocument();

        int start = insert(s, target, doc);
        
        if (reformat && start >= 0) {  // format the inserted text
            int end = start + s.length();
            Formatter f = doc.getFormatter();
            f.reformat(doc, start, end);
        }

//        if (select && start >= 0) { // select the inserted text
//            Caret caret = target.getCaret();
//            int current = caret.getDot();
//            caret.setDot(start);
//            caret.moveDot(current);
//            caret.setSelectionVisible(true);
//        }
        
    }
    
    private static int insert(String s, JTextComponent target, BaseDocument doc) 
    throws BadLocationException 
    {

        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        }
        catch (BadLocationException ble) {}
        
        return start;
    }
    
}
