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

//The class is invalid: terminalemulator was replaced by core/output2
//so, removing all the meet from it
package org.netbeans.modules.visualweb.gravy.debugger;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.netbeans.jellytools.actions.*;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator;

/** Operator for Output pane containing one (normal output)
 * or more (for example JavaDoc output) output terminals 
 * (org.netbeans.lib.terminalemulator.Term component).
 * <p>
 * Usage:<br>
 * <pre>
 *      // find term with given name
 *      TermOperator to = new TermOperator("MyClass - I/O");
 *      // wait for a message appears in output
 *      to.waitText("my message");
 *      // get the text
 *      String wholeOutput = to.getText();
 *      to.clearOutput();
 *      to.waitText("your message");
 *      // close this output either way
 *      to.close(); // or to.discard();
 * </pre>
 *
 */
public class TermOperator extends ComponentOperator {
    
    // only first 5 Terms and 5 Screens are to be cached
    ComponentOperator[] _screen=new ComponentOperator[5];
    JComponent[] _term=new JComponent[5];

    // Actions used only in TermOperator. 
    private static final Action findNextAction = 
        new Action(null,
                   "Find Next");//,
                   //null,
                   //new Shortcut(KeyEvent.VK_F3));
    
    private static final Action selectAllAction = 
        new Action(null,
                   "Select All");//,
                   //null,
                   //new Shortcut(KeyEvent.VK_A, InputEvent.CTRL_MASK));

    private static final Action clearAction = 
        new Action(null,
                   "Clear Output");

    private static final Action startRedirectionAction = 
        new Action(null,
                   "Start Redirection of This View to File");

    private static final Action stopRedirectionAction = 
        new Action(null,
                   "Stop Redirection of This View to File");

    private static final CopyAction copyAction = new CopyAction();
    private static final FindAction findAction = new FindAction();


    /** Create new instance of TermOperator from given component.
     * @param source TopComponent instance with Term
     */
    public TermOperator(JComponent source) {
        // used in OutputOperator
        super(source);
    }
    
    /** Waits for output term with given name.
     * It is activated by defalt.
     * @param name name of output term to look for
     *
    public TermOperator(String name) {
        this(name, 0);
    }
    
    /** Waits for index-th output term with given name.
     * It is activated by defalt.
     * @param name name of output term to look for
     * @param index index of requested output term with given name
     */

    /*public TermOperator(String name, int index) {
        super(new OutputOperator().waitSubComponent(new InnerTabSubchooser(name), index));
        makeComponentVisible();
    }*/
    
    /** Waits for output term with given name in specified container.
     * It is activated by defalt.
     * @param contOper container where to search
     * @param name name of output term to look for
     * @deprecated Use {@link #TermOperator(String)} or {@link #TermOperator(String, int)}
     * because term can only be placed inside Output top component
     */
    public TermOperator(ContainerOperator contOper, String name) {
        this(contOper, name, 0);
    }
    
    /** Creates TermOperator instance
     * @param contOper container where to search
     * @param index int index of requested output term with given name
     * @deprecated Use {@link #TermOperator(String)} or {@link #TermOperator(String, int)}
     * because term can only be placed inside Output top component
     */
    public TermOperator(ContainerOperator contOper, int index) {
        this(contOper, null, index);
    }
    
    /** Creates TermOperator instance
     * @param contOper container where to search
     * @param name String name of output term to look for
     * @param index int index of requested output term with given name
     * @deprecated Use {@link #TermOperator(String)} or {@link #TermOperator(String, int)}
     * because term can only be placed inside Output top component
     */
    public TermOperator(ContainerOperator contOper, String name, int index) {
        super(contOper.waitSubComponent(new InnerTabSubchooser(name), index));
        makeComponentVisible();
        copyEnvironment(contOper);
    }
    
    /** Creates TermOperator instance
     * @param contOper container where to search
     * @deprecated Use {@link #TermOperator(String)} or {@link #TermOperator(String, int)}
     * because term can only be placed inside Output top component
     */
    public TermOperator(ContainerOperator contOper) {
        super(contOper);
    }
    
    /** Activates this term. If this term is in tabbed pane, it is selected. If
     * it is only term in the Output top component, the Output top component 
     * is activated.
     */
    /*public void makeComponentVisible() {
        if(getParent() instanceof JTabbedPane) {
            super.makeComponentVisible();
            // Term is a tab of JTabbedPane
            new JTabbedPaneOperator((JTabbedPane)getParent()).setSelectedComponent(getSource());
        } else {
            // term is sub component of Output top component
            new OutputOperator().makeComponentVisible();
        }
    }*/

    /** Getter for Term object
     * @return instance of Term
     */
    public JComponent getTermSource() {
        return getTermSource(0);
    }
    
    /** Getter for Term object
     * @return instance of Term
     * @param termIndex index of Term inside (usualy 0, Javadoc Output has two Terms: 0 and 1).
     */
    public JComponent getTermSource(int termIndex) {
        if (termIndex>=_term.length)
            return (JComponent)ComponentOperator.waitComponent((Container)getSource(), new TermFinder(), termIndex);
        if (_term[termIndex]==null) {
            _term[termIndex]=(JComponent)ComponentOperator.waitComponent((Container)getSource(), new TermFinder(), termIndex);
        }
        return _term[termIndex];
    }
    
    /** Finds a row by text in the first Term.
     * @param rowText String row text
     * @return row number of specified text; -1 if text not found
     */
    public int findRow(String rowText) {
        return findRow(rowText, 0);
    }
    
    /** Finds a row by text.
     * @param rowText String row text
     * @param termIndex int Term index
     * @return row number of specified text; -1 if text not found
     */
    public int findRow(String rowText, int termIndex) {
        for(int i = 0; i < getLineCount(termIndex); i++) {
            if(getComparator().equals(getRowText(i, termIndex), rowText)) {
                return i;
            }
        }
        return -1;
    }
    
    /** Invokes <code>flush</code> and returns text from the first Term.
     * @return text from the first Term.
     */
    public String getText() {
        return getText(0);
    }
    
    /** Invokes <code>flush</code> and returns text.
     * @param termIndex int Term index
     * @return text from termIndex-th Term
     */
    public String getText(int termIndex) {
        flush(termIndex);
        return getText(0, getLineCount(termIndex) - 1, termIndex);
    }
    
    /** Get text between <code>startRow</code> and <code>endRow</code> from the first Term
     * @param startRow first row to be included
     * @param endRow last row to be included
     * @return text between <code>startRow</code> and <code>endRow</code> from the first Term
     */
    public String getText(int startRow, int endRow) {
        return getText(startRow, endRow);
    }
    
    /** Get text between <code>startRow</code> and <code>endRow</code>
     * @param startRow first row to be included
     * @param endRow last row to be included
     * @param termIndex int Term index
     * @return text between <code>startRow</code> and <code>endRow</code>
     */
    public String getText(int startRow, int endRow, int termIndex) {
        flush(termIndex);
        String result = "";
        for(int i = startRow; i < endRow; i++) {
            result = result + getRowText(i, termIndex) + "\n";
        }
        return(result);
    }
    
    /** Waits for text to be displayed in output term.
     * @param text text to wait for
     */
    public void waitText(String text) {
        waitText(text, 0);
    }
    
    /** Waits for text to be displayed in termIndex-th term.
     * @param text text to wait for
     * @param termIndex Term index
     */
    public void waitText(final String text, final int termIndex) {
        getOutput().printLine("Wait \"" + text + "\" text in component \n    : "+
        getTermSource(termIndex).toString());
        getOutput().printGolden("Wait \"" + text + "\" text");
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return(findRow(text, termIndex) > -1);
            }
            public String getDescription() {
                return("\"" + text + "\" text");
            }
        });
    }
    
    /** Returns count of filled lines of the first Term.
     * @return count of filled lines of the first Term.
     */
    public int getLineCount() {
        return getLineCount(0);
    }
    
    /** Returns count of filled lines.
     * @param termIndex int Term index
     * @return count of filled lines.
     */
    public int getLineCount(int termIndex) {
        flush(termIndex);
        return(getCursorRow(termIndex) + 1);
    }
    
    /** Returns the topmost component lying on the first Term.
     * All events should be dispatched to this component.
     * @return the topmost component lying on the first Term.
     */
    public ComponentOperator screen() {
        return screen(0);
    }
    
    /** Returns the topmost component lying on the term.
     * All events should be dispatched to this component.
     * @param termIndex int Term index
     * @return the topmost component lying on the termIndex-throws Term.
     */
    public ComponentOperator screen(int termIndex) {
        ComponentOperator sc;
        if (termIndex>=_screen.length || _screen[termIndex] == null) {
            sc = ComponentOperator.createOperator(ComponentOperator.waitComponent(getTermSource(termIndex), new ScreenFinder()));
            sc.copyEnvironment(this);
            if (termIndex<_screen.length)
                _screen[termIndex] = sc;
            return sc;
        }
        return _screen[termIndex];
    }
    
    ////////////////////////////////////////////////////////
    //Mapping                                             //
    /** Returns text from specified row.
     * Maps <code>Term.getRowText(int)</code> through queue for the first Term.
     * @param row row number to get text from
     * @return text from the specified row
     */
    public String getRowText(int row) {
        return getRowText(row, 0);
    }
    
    /** Returns text from specified row of termIndex-th term.
     * Maps <code>Term.getRowText(int)</code> through queue
     * @param row row number to get text from
     * @param termIndex int Term index
     * @return text from the specified row
     */
    public String getRowText(final int row, final int termIndex) {
        return((String)runMapping(new MapAction("getRowText") {
            public Object map() {
                return(null);
                //return(((Term)getTermSource(termIndex)).getRowText(row));
            }}));}
    
    /** Returns text within given coordinates.
     * Maps <code>Term.textWithin(...)</code> through queue for the first Term.
     * @param beginRow starting row
     * @param beginCol starting column
     * @param endRow ending row
     * @param endCol ending column
     * @return text within begin and end coordinates
     */
    public String textWithin(int beginRow, int beginCol, int endRow, int endCol) {
        return textWithin(beginRow, beginCol, endRow, endCol, 0);
    }
    
    /** Returns text within given coordinates of termIndex-th term.
     * Maps <code>Term.textWithin(...)</code> through queue
     * @param beginRow starting row
     * @param beginCol starting column
     * @param endRow ending row
     * @param endCol ending column
     * @param termIndex int Term index
     * @return text within begin and end coordinates
     */
    public String textWithin(int beginRow, int beginCol, int endRow, int endCol, final int termIndex) {
        //final Coord begin = Coord.make(beginRow, beginCol);
        //final Coord end = Coord.make(endRow, endCol);
	final Object begin=null;
	final Object end=null;
        return((String)runMapping(new MapAction("textWithin") {
            public Object map() {
                return(null);
                //return(((Term)getTermSource(termIndex)).textWithin(begin, end));
            }}));}
    
    /* commented to avoid confusing.
       public int getRows() {
       return(runMapping(new MapIntegerAction("getRows") {
       public int map() {
       return(((Term)getTerm()).getRows());
       }}));}
     */
    
    /** Returns row where the cursor stands.
     * Maps <code>Term.getCursorRow()</code> through queue for the first Term
     * @return int cursor row
     */
    public int getCursorRow() {
        return getCursorRow(0);
    }
    
    /** Returns row where the cursor stands.
     * Maps <code>Term.getCursorRow()</code> through queue
     * @param termIndex int Term index
     * @return int cursor row
     */
    public int getCursorRow(final int termIndex) {
        return(runMapping(new MapIntegerAction("getCursorRow") {
            public int map() {
                return(0);
                //return(((Term)getTermSource(termIndex)).getCursorRow());
            }}));}
    
    /** Flushes buffer.
     * Maps <code>Term.flush()</code> through queue for the first Term */
    public void flush() {
        flush(0);
    }
    
    /** Flushes buffer.
     * Maps <code>Term.flush()</code> through queue
     * @param termIndex int Term index
     */
    public void flush(final int termIndex) {
        runMapping(new MapVoidAction("flush") {
            public void map() {
                //((Term)getTermSource(termIndex)).flush();
            }});}
    
    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    static class ScreenFinder implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            Class cls = comp.getClass();
            do {
                if(cls.getName().equals("org.netbeans.lib.terminalemulator.Screen")) {
                    return(true);
                }
            } while((cls = cls.getSuperclass()) != null);
            return(false);
        }
        public String getDescription() {
            return("Screen component");
        }
    }

    static class TermFinder implements ComponentChooser {
        public boolean checkComponent(Component comp) {
        return false;
            //return comp instanceof Term;
        }
        public String getDescription() {
            return "org.netbeans.lib.terminalemulator.Term";
        }
    }
    
    /** SubChooser to determine Term TopComponent
     * Used in findTopComponent method.
     */
    private static final class InnerTabSubchooser implements ComponentChooser {
        
        /** Name of term to search for. */
        private String termName;
        
        public InnerTabSubchooser() {
        }
        
        public InnerTabSubchooser(String termName) {
            this.termName = termName;
        }
        
        public boolean checkComponent(Component comp) {
            if(comp.getClass().getName().endsWith("OutputTabInner")) {  // NOI18N
                return Operator.getDefaultStringComparator().equals(comp.getName(), termName);
            } else {
                return false;
            }
        }
        
        public String getDescription() {
            return "org.netbeans.core.output.OutputTabInner";  // NOI18N
        }
    }
    
    /** Performs verification by accessing all sub-components */
    public void verify() {
        screen();
    }
    
    /****************************** Actions *****************************/

    /** Performs copy action. */
    public void copy() {
        //copyAction.perform(screen());
    }
    
    /** Performs find action. */
    public void find() {
        //findAction.perform(screen());
    }
    
    /** Performs find next action. */
    public void findNext() {
        //findNextAction.perform(screen());
    }
    
    /** Performs select all action. */
    public void selectAll() {
        //selectAllAction.perform(screen());
    }
    
    /** Performs clear output action. */
    public void clearOutput() {
        //clearAction.perform(screen());
    }

    /** Performs start redirection action. */
    public void startRedirection() {
        //startRedirectionAction.perform(screen());
    }

    /** Performs stop redirection action. */
    public void stopRedirection() {
        //stopRedirectionAction.perform(screen());
    }
    
    /** Performs discard action. */
    public void discard() {
        //new DiscardOutputAction().perform(this);
    }

    /** Performs discard all action. */
    public void discardAll() {
        //new DiscardAllOutputsAction().perform(this);
    }
    
    /** Closes output teby by API. */
    public void close() {
        //new DiscardOutputAction().performAPI(this);
    }
}
