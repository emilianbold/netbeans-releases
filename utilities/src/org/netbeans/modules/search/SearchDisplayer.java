/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.compiler.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.openide.util.NbBundle;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;

import org.netbeans.editor.*;

/**
 * Presents search results in output window. It can display
 * just nodes marked by SearchDetailCookie.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class SearchDisplayer extends Object implements NodeAcceptor {

    //0 found text, 1 file name, 2 line number
    String FMT_FOUND = "{0} [{1}:{2}]"; //NOI18N

    /** output tab */
    private InputOutput searchIO;
    /** writer to that tab */
    private OutputWriter ow = null;

    /** Creates new SearchDisplayer */
    public SearchDisplayer() {
    }

    private void setOw (String name) {
        if (ow != null) return;
        searchIO = TopManager.getDefault().getIO(name);
        searchIO.setFocusTaken (false);
        ow = searchIO.getOut();
    }


    private void displayDetail(StructuredDetail detail) {
        Object[] args = new Object[] {
                            detail.text.trim(),
                            detail.fo.getName(),
                            new Integer(detail.line)
                        };

        String text = MessageFormat.format(FMT_FOUND, args);

        try {
            IOCtl ec = new IOCtl (
                           detail.fo,
                           Math.max(detail.line - 1, 0),
                           Math.max(detail.column - 1, 0)
                       );
            ow.println(text, ec);
        } catch (IOException ex) {
            ow.println(text);
        }

    }

    private void displayNode(Node node) {
        DetailCookie cake = (DetailCookie) node.getCookie(DetailCookie.class);

        if (cake != null) {
            Enumeration en = cake.detail();
            while (en.hasMoreElements()) {
                Object next = en.nextElement();
                if (next instanceof StructuredDetail) {
                    displayDetail((StructuredDetail) next);
                }
            }
        }
    }

    /** Accepted nodes should be displayed.
     * @param nodes the nodes to consider
     * @return <CODE>true</CODE> if so
     */
    public synchronized boolean acceptNodes(Node[] nodes) {

        if (nodes == null) return false;

        if (nodes.length > 0) setOw(Res.text("TITLE_SEARCH_RESULTS"));

        for (int i = 0; i<nodes.length; i++)
            displayNode(nodes[i]);

        return true;
    }

    final class IOCtl implements OutputListener {
        /** file we check */
        FileObject file;

        /** line we check */
        Line xline;

        /** column with the err */
        int column;

        /** text to display */
        private String text;

        /**
        * @param fo is a FileObject with an error
        * @param line is a line with the error
        * @param column is a column with the error
        * @param text text to display to status line
        * @exception FileNotFoundException
        */
        public IOCtl (FileObject fo, int line, int column)
        throws java.io.IOException {
            file = fo;
            this.column = column;
            DataObject data = DataObject.find (file);
            LineCookie cookie = (LineCookie)data.getCookie(LineCookie.class);
            if (cookie == null) {
                throw new java.io.FileNotFoundException ();
            }
            xline = cookie.getLineSet ().getOriginal (line);
        }

        public void outputLineSelected (OutputEvent ev) {
            try {
                xline.markCurrentLine();
                xline.show(Line.SHOW_TRY_SHOW, column);
            } catch (IndexOutOfBoundsException ex) {
            }
        }

        public void outputLineAction (OutputEvent ev) {
            try {
                xline.markCurrentLine();
                xline.show(Line.SHOW_GOTO, column);
            } catch (IndexOutOfBoundsException ex) {
            }
        }

        public void outputLineCleared (OutputEvent ev) {
            try {
                xline.unmarkCurrentLine();
            } catch (IndexOutOfBoundsException ex) {
            }
        }

        /** Select the resu;t in editor such as Find action does. */
        //TODO
        private void select() {
            /*
                  // check existence of suitable editor.
                  try {
                    Class.forName(FindSupport.class.getName());
                  } catch (Exception ex) {
                    return;
                  }
                
                  // the actual parameters should be obtained from criterion that produced it
                  FindSupport supp = FindSupport.getFindSupport();
                  Map props = new HashMap();
                  props.put(SettingNames.FIND_MATCH_CASE, new Boolean(true));
                  props.put(SettingNames.FIND_SMART_CASE, new Boolean(false));
                  props.put(SettingNames.FIND_WHOLE_WORDS, new Boolean(false));
                  props.put(SettingNames.FIND_REG_EXP, new Boolean(false));      
                  props.put(SettingNames.FIND_WHAT, "com");
                  supp.putFindProperties(props);
                  supp.find(null, false);
            */      
        }
    }

}