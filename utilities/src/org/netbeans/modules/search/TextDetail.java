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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;

import java.awt.Toolkit;
import java.io.CharConversionException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.xml.XMLUtil;
import org.openidex.search.SearchHistory;
import org.openidex.search.SearchPattern;


/**
 * Holds details about one search hit in the text document.
 *
 * @author Tomas Pavek
 * @author Marian Petras
 */
final class TextDetail {

    /** Property name which indicates this detail to show. */
    static final int DH_SHOW = 1;
    /** Property name which indicates this detail to go to. */
    static final int DH_GOTO = 2;
    /** Property name which indicates this detail to hide. */
    static final int DH_HIDE = 3;
    
    /** Data object. */
    private DataObject dobj;
    /** Line number where search result occures.*/
    private int line;
    /** Text of the line. */ 
    private String lineText;
    /** Column where search result starts. */
    private int column;
    /** Length of search result which to mark. */
    private int markLength;
    /** Line. */
    private Line lineObj;
    /** SearchPattern used to create the hit of this DetailNode */
    private SearchPattern searchPattern;

    
    
    /** Constructor using data object. 
     * @param pattern  SearchPattern used to create the hit of this DetailNode 
     */
    TextDetail(DataObject dobj, SearchPattern pattern) {
        this.dobj = dobj;
        this.searchPattern = pattern;
    }

    /**
     * Shows the search detail on the DataObject.
     * The document is opened in the editor, the caret is positioned on the right line and column 
     * and searched string is marked.
     *
     * @param how indicates how to show detail. 
     * @see #DH_GOTO 
     * @see #DH_SHOW 
     * @see #DH_HIDE */
    void showDetail(int how) {
        if ((dobj == null) || !dobj.isValid()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (lineObj == null) { // try to get Line from DataObject
            LineCookie lineCookie = dobj.getCookie(LineCookie.class);
            if (lineCookie != null) {
                Line.Set lineSet = lineCookie.getLineSet();
                try {
                    lineObj = lineSet.getOriginal(line - 1);
                } catch (IndexOutOfBoundsException ioobex) {
                    // The line doesn't exist - go to the last line
                    lineObj = lineSet.getOriginal(findMaxLine(lineSet));
                    column = markLength = 0;
                }
            }
            if (lineObj == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }

        if (how == DH_HIDE) {
            return;
        }
        EditorCookie edCookie = dobj.getCookie(EditorCookie.class);
        if (edCookie != null) {
            edCookie.open();
	}
        if (how == DH_SHOW) {
            lineObj.show(Line.SHOW_TRY_SHOW, column - 1);
        } else if (how == DH_GOTO) {
            lineObj.show(Line.SHOW_GOTO, column - 1);
        }
        if ((markLength > 0) && (edCookie != null)) {
            final JEditorPane[] panes = edCookie.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                // Necessary since above lineObj.show leads to invoke later as well.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Caret caret = panes[0].getCaret(); // http://www.netbeans.org/issues/show_bug.cgi?id=23626
                        caret.moveDot(caret.getDot() + markLength);
                    }
                });
            }
        }
        SearchHistory.getDefault().setLastSelected(searchPattern);
    }

    /** Getter for <code>lineText</code> property. */
    String getLineText() {
        return lineText;
    }
    
    /** Setter for <code>lineText</code> property. */
    void setLineText(String text) {
        lineText = text;
    }
    

    /**
     * Gets the <code>DataObject</code> where the searched text was found. 
     *
     * @return data object or <code>null</code> if no data object is available
     */
    DataObject getDataObject() {
        return dobj;
    }

    /** Gets the line position of the text. */
    int getLine() {
        return line;
    }

    /** Sets the line position of the text. */
    void setLine(int line) {
        this.line = line;
    }

    /** Gets the column position of the text or 0 (1 based). */
    int getColumn() {
        return column;
    }

    /** Sets the column position of the text. */
    void setColumn(int col) {
        column = col;
    }

    /** Gets the length of the text that should be marked when the detail is shown. */
    void setMarkLength(int len) {
        markLength = len;
    }

    /** @return length or 0 */
    int getMarkLength() {
        return markLength;
    }
    
    /**
     * Returns the maximum line in the <code>set</code>.
     * Used to display the end of file when the corresponding
     * line no longer exists. (Copied from org.openide.text)
     *
     * @param set the set we want to search.
     * @return maximum line in the <code>set</code>.
     */
    private static int findMaxLine(Line.Set set) {
        int from = 0;
        int to = 32000;
        
        for (;;) {
            try {
                set.getOriginal(to);
                // if the line exists, double the max number, but keep
                // for reference that it exists
                from = to;
                to *= 2;
            } catch (IndexOutOfBoundsException ex) {
                break;
            }
        }
        
        while (from < to) {
            int middle = (from + to + 1) / 2;
            
            try {
                set.getOriginal(middle);
                // line exists
                from = middle;
            } catch (IndexOutOfBoundsException ex) {
                // line does not exists, we have to search lower
                to = middle - 1;
            }
        }
        
        return from;
    }

    /**
     * Node that represents information about one occurence of a matching
     * string.
     *
     * @see  TextDetail
     */
    static final class DetailNode extends AbstractNode
                                          implements OutputListener {
        
        /** Detail to represent. */
        private TextDetail txtDetail;
        

        
        /**
         * Constructs a node representing the specified information about
         * a matching string.
         *
         * @param txtDetail  information to be represented by this node
         */
        public DetailNode(TextDetail txtDetail) {
            super(Children.LEAF);
            
            this.txtDetail = txtDetail;
            
            setShortDescription(DetailNode.getShortDesc(txtDetail));
            setValue(SearchDisplayer.ATTR_OUTPUT_LINE,
                     DetailNode.getFullDesc(txtDetail));
        }
        
        @Override
        public Action[] getActions(boolean context) {
            if (!context) {
                return new Action[] { getPreferredAction() };
            } else {
                return new Action[0];
            }
        }
        
        @Override
        public Action getPreferredAction() {
            return SystemAction.get(GotoDetailAction.class);
        }

        @Override
        public boolean equals(Object anotherObj) {
            return (anotherObj != null)
                && (anotherObj.getClass() == DetailNode.class)
                && (((DetailNode) anotherObj).txtDetail.equals(this.txtDetail));
        }
        
        @Override
        public int hashCode() {
            return txtDetail.hashCode() + 1;
        }
        
        /** */
        @Override
        public String getName() {
            return txtDetail.getLineText() + "      [" + DetailNode.getName(txtDetail) + "]";  // NOI18N
        }

        @Override
        public String getHtmlDisplayName() {
            String colored;
            if (txtDetail.getMarkLength() > 0 && txtDetail.getColumn() > 0) {
                try {
                    StringBuffer bold = new StringBuffer();
                    String plain = txtDetail.getLineText();
                    int col0 =   txtDetail.getColumn() -1;  // base 0

                    bold.append(XMLUtil.toElementContent(plain.substring(0, col0)));  // NOI18N
                    bold.append("<b>");  // NOi18N
                    int end = col0 + txtDetail.getMarkLength();
                    bold.append(XMLUtil.toElementContent(plain.substring(col0, end)));
                    bold.append("</b>"); // NOi18N
                    if (txtDetail.getLineText().length() > end) {
                        bold.append(XMLUtil.toElementContent(plain.substring(end)));
                    }
                    colored = bold.toString();
                } catch (CharConversionException ex) {
                    return null;
                }
            } else {
                try {
                    colored = XMLUtil.toElementContent( txtDetail.getLineText());
                } catch (CharConversionException e) {
                    return null;
                }
            }

            try {
                return colored + "      <font color='!controlShadow'>[" + XMLUtil.toElementContent(DetailNode.getName(txtDetail)) + "]";  // NOI18N
            } catch (CharConversionException e) {
                return null;
            }
        }
      
        /** Displays the matching string in a text editor. */
        private void gotoDetail() {
            txtDetail.showDetail(TextDetail.DH_GOTO);
        }

        /** Show the text occurence. */
        private void showDetail() {
            txtDetail.showDetail(TextDetail.DH_SHOW);
        }

        /** Implements <code>OutputListener</code> interface method. */
        public void outputLineSelected (OutputEvent evt) {
            txtDetail.showDetail(TextDetail.DH_SHOW);
        }

        /** Implements <code>OutputListener</code> interface method. */        
        public void outputLineAction (OutputEvent evt) {
            txtDetail.showDetail(TextDetail.DH_GOTO);
        }

        /** Implements <code>OutputListener</code> interface method. */
        public void outputLineCleared (OutputEvent evt) {
            txtDetail.showDetail(TextDetail.DH_HIDE);
        }

        /**
         * Returns name of a node representing a <code>TextDetail</code>.
         *
         * @param  det  detailed information about location of a matching string
         * @return  name for the node
         */
        private static String getName(TextDetail det) {
            int line = det.getLine();
            int col = det.getColumn();
            
            if (col > 0) {
                
                /* position <line>:<col> */
                return NbBundle.getMessage(DetailNode.class, "TEXT_DETAIL_FMT_NAME1",      //NOI18N
                        Integer.toString(line),
                        Integer.toString(col));
            } else {
                
                /* position <line> */
                return NbBundle.getMessage(DetailNode.class, "TEXT_DETAIL_FMT_NAME2",      //NOI18N
                        Integer.toString(line));
            }
        }

        /**
         * Returns short description of a visual representation of
         * a <code>TextDetail</code>. The description may be used e.g.
         * for a tooltip text of a node.
         *
         * @param  det  detailed information about location of a matching string
         * @return  short description of a visual representation
         */
        private static String getShortDesc(TextDetail det) {
            int line = det.getLine();
            int col = det.getColumn();
            
            if (col > 0) {
                
                /* line <line>, column <col> */
                return NbBundle.getMessage(DetailNode.class, "TEXT_DETAIL_FMT_SHORT1",   //NOI18N
                        new Object[] {Integer.toString(line),
                                      Integer.toString(col)});
            } else {
                
                /* line <line> */
                return NbBundle.getMessage(DetailNode.class, "TEXT_DETAIL_FMT_SHORT2",   //NOI18N
                        Integer.toString(line));
            }
        }

        /**
         * Returns full description of a visual representation of
         * a <code>TextDetail</code>. The description may be printed e.g. to
         * an OutputWindow.
         *
         * @param  det  detailed information about location of a matching string
         * @return  full description of a visual representation
         */
        private static String getFullDesc(TextDetail det) {
            String filename = det.getDataObject().getPrimaryFile().getNameExt();
            String lineText = det.getLineText();
            int line = det.getLine();
            int col = det.getColumn();

            if (col > 0) {

                /* [<filename> at line <line>, column <col>] <text> */
                return NbBundle.getMessage(DetailNode.class, "TEXT_DETAIL_FMT_FULL1",    //NOI18N
                        new Object[] {lineText,
                                      filename,
                                      Integer.toString(line),
                                      Integer.toString(col)});
            } else {

                /* [<filename> line <line>] <text> */
                return NbBundle.getMessage(DetailNode.class, "TEXT_DETAIL_FMT_FULL2",    //NOI18N
                        new Object[] {lineText,
                                      filename,
                                      Integer.toString(line)});
            }
        }
        
    } // End of DetailNode class.

    /**
     * This action displays the matching string in a text editor.
     * This action is to be used in the window/dialog displaying a list of
     * found occurences of strings matching a search pattern.
     */
    private static class GotoDetailAction extends NodeAction {
        
        /** */
        public String getName() {
            return NbBundle.getBundle(GotoDetailAction.class).getString("LBL_GotoDetailAction");
        }
        
        /** */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(GotoDetailAction.class);
        }

        /**
         * @return  <code>true</code> if at least one node is activated and
         *          the first node is an instance of <code>DetailNode</code>
         *          (or its subclass), <code>false</code> otherwise
         */
        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes != null && activatedNodes.length != 0
                   && activatedNodes[0] instanceof DetailNode;
        }

        /**
         * Displays the matching string in a text editor.
         * Works only if condition specified in method {@link #enable} is met,
         * otherwise does nothing.
         */
        protected void performAction(Node[] activatedNodes) {
            if (enable(activatedNodes)) {
                ((DetailNode) activatedNodes[0]).gotoDetail();
            }
        }
        
        /**
         */
        @Override
        protected boolean asynchronous() {
            return false;
        }
        
    } // End of GotoDetailAction class.
        
}
