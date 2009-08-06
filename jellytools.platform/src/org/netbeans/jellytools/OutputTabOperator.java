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

package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator;

/** Operator for Output tab. It resides in output top component.
 * <p>
 * Usage:<br>
 * <pre>
 *      // find output tab with given name
 *      OutputTabOperator oto = new OutputTabOperator("compile-single");
 *      // wait for a message appears in output
 *      oto.waitText("my message");
 *      // get the text
 *      String wholeOutput = oto.getText();
 *      // close this output
 *      oto.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see OutputOperator
 */
public class OutputTabOperator extends JComponentOperator {

    // operator of OutputPane component
    ComponentOperator outputPaneOperator;

    // actions used only in OutputTabOperator
    private static final Action findNextAction = 
        new Action(null,
                   Bundle.getString("org.netbeans.core.output2.Bundle", 
                                    "ACTION_FIND_NEXT"),
                   null,
                   KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    
    private static final Action selectAllAction = 
        new Action(null, null, null, 
                   System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
                       KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_MASK) :
                       KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));

    private static final Action wrapTextAction = 
        new Action(null,
                   Bundle.getString("org.netbeans.core.output2.Bundle", 
                                    "ACTION_WRAP"),
                   null,
                   System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
                       KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_MASK) :
                       KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));

    private static final ActionNoBlock saveAsAction = 
        new ActionNoBlock(null,
                   Bundle.getString("org.netbeans.core.output2.Bundle", 
                                    "ACTION_SAVEAS"),
                   null,
                   System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
                       KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK) :
                       KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    
    private static final Action nextErrorAction = 
        new Action(null, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

    private static final Action previousErrorAction = 
        new Action(null, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.SHIFT_MASK));

    private static final Action closeAction = 
        new Action(null,
                   Bundle.getString("org.netbeans.core.output2.Bundle", 
                                    "ACTION_CLOSE"),
                   null,
                   System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
                       KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.META_MASK) :
                       KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK));

    private static final Action clearAction = 
        new Action(null,
                   Bundle.getString("org.netbeans.core.output2.Bundle", 
                                    "ACTION_CLEAR"),
                   null,
                   System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
                       KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_MASK) :
                       KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
        
    private static final CopyAction copyAction = new CopyAction();
    private static final FindAction findAction = new FindAction();

    /** Create new instance of OutputTabOperator from given component.
     * @param source JComponent source
     */
    public OutputTabOperator(JComponent source) {
        // used in OutputOperator
        super(source);
    }
    
    /** Waits for output tab with given name.
     * It is activated by defalt.
     * @param name name of output tab to look for
     */
    public OutputTabOperator(String name) {
        this(name, 0);
    }
    
    /** Waits for index-th output tab with given name.
     * It is activated by defalt.
     * @param name name of output tab to look for
     * @param index index of requested output tab with given name
     */
    public OutputTabOperator(String name, int index) {
        super((JComponent)new OutputOperator().waitSubComponent(new OutputTabSubchooser(name), index));
        makeComponentVisible();
    }
    
    /** Activates this output tab. If this output tab is in tabbed pane, it is selected. If
     * it is only tab in the Output top component, the Output top component 
     * is activated.
     */
    public void makeComponentVisible() {
        if(getParent() instanceof JTabbedPane) {
            super.makeComponentVisible();
            // output tab is a tab of JTabbedPane
            new JTabbedPaneOperator((JTabbedPane)getParent()).setSelectedComponent(getSource());
        } else {
            // output tab is sub component of Output top component
            new OutputOperator().makeComponentVisible();
        }
    }

    /** Returns length of written text. It is a number of written characters.
     * @return length of already written text
     */
    public int getLength() {
        // ((OutputTab)getSource()).getDocument().getLength();
        return runMapping(new MapIntegerAction("getLength") {
            public int map() {
                Document document = ((AbstractOutputTab)getSource()).getOutputPane().getDocument();
                try {
                    Class clazz = Class.forName("org.netbeans.core.output2.OutputDocument");
                    Method getLengthMethod = clazz.getDeclaredMethod("getLength", (Class[])null);
                    getLengthMethod.setAccessible(true);
                    return ((Integer)getLengthMethod.invoke(document, (Object[])null)).intValue();
                } catch (Exception e) {
                    throw new JemmyException("getLength() by reflection failed.", e);
                }
            }});
    }

    /** Finds a line number by text.
     * @param lineText String line text
     * @return line number of specified text starting at 0; -1 if text not found
     */
    public int findLine(String lineText) {
        int lineCount = getLineCount();
        if(lineCount < 1) {
            // no line yet
            return -1;
        }
        for(int i = 0; i < lineCount; i++) {
            if(getComparator().equals(getLine(i), lineText)) {
                return i;
            }
        }
        return -1;
    }
    
    /** Returns text from this output tab.
     * @return text from this output tab.
     */
    public String getText() {
        final int length = getLength();
        return (String)runMapping(new MapAction("getText") {
            public Object map() {
                Document document = ((AbstractOutputTab)getSource()).getOutputPane().getDocument();
                try {
                    Class clazz = Class.forName("org.netbeans.core.output2.OutputDocument");
                    Method getTextMethod = clazz.getDeclaredMethod("getText", new Class[] {int.class, int.class});
                    getTextMethod.setAccessible(true);
                    return getTextMethod.invoke(document, new Object[] {Integer.valueOf(0), Integer.valueOf(length)}).toString();
                } catch (Exception e) {
                    throw new JemmyException("Getting text by reflection failed.", e);
                }
            }});
    }
    
    /** Get text between <code>startLine</code> and <code>endLine</code> from this output tab.
     * Both <code>startLine</code> and <code>endLine</code> are included.
     * @param startLine first line to be included (starting at 0)
     * @param endLine last line to be included
     * @return text between <code>startLine</code> and <code>endLine</code> from this output tab
     */
    public String getText(int startLine, int endLine) {
        StringBuffer result = new StringBuffer();
        for(int i = startLine; i <= endLine; i++) {
            result.append(getLine(i));
            result.append('\n');;
        }
        return result.toString();
    }
    
    /** Waits for text to be displayed in this output tab.
     * @param text text to wait for
     */
    public void waitText(final String text) {
        getOutput().printLine("Wait \"" + text + "\" text in component \n    : "+toStringSource());
        getOutput().printGolden("Wait \"" + text + "\" text");
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return (findLine(text) > -1);
            }
            public String getDescription() {
                return("\"" + text + "\" text");
            }
        });
    }
    
    /** Returns count of filled lines of this output tab.
     * @return count of filled lines of this output tab.
     */
    public int getLineCount() {
        return ((Integer)runMapping(new MapAction("getLineCount") {
            public Object map() {
                Document document = ((AbstractOutputTab)getSource()).getOutputPane().getDocument();
                try {
                    Class clazz = Class.forName("org.netbeans.core.output2.OutputDocument");
                    Method getElementCountMethod = clazz.getDeclaredMethod("getElementCount", (Class[])null);
                    getElementCountMethod.setAccessible(true);
                    return (Integer)getElementCountMethod.invoke(document, (Object[])null);
                } catch (Exception e) {
                    throw new JemmyException("getElementCount() by reflection failed.", e);
                }
            }})).intValue();
    }
    
    /** Returns operator for OutputPane component.
     * All events should be dispatched to this component.
     * @return operator for OutputPane component
     */
    public ComponentOperator outputPaneOperator() {
        // first make component visible because tab must be visible to dispatch events
        makeComponentVisible();
        if(outputPaneOperator == null) {
            outputPaneOperator = ComponentOperator.createOperator(((AbstractOutputTab)getSource()).getOutputPane());
            outputPaneOperator.copyEnvironment(this);
        }
        return outputPaneOperator;
    }
    
    /** Returns text from specified line.
     * @param line line number to get text from
     * @return text from the specified line (starting at 0)
     */
    public String getLine(final int line) {
        return (String)runMapping(new MapAction("getText") {
            public Object map() {
                Document document = ((AbstractOutputTab)getSource()).getOutputPane().getDocument();
                try {
                    Class clazz = Class.forName("org.netbeans.core.output2.OutputDocument");
                    Method getLineStartMethod = clazz.getDeclaredMethod("getLineStart", new Class[] {int.class});
                    getLineStartMethod.setAccessible(true);
                    Integer lineStart = (Integer) getLineStartMethod.invoke(document, new Object[] {Integer.valueOf(line)});
                    Method getLineEndMethod = clazz.getDeclaredMethod("getLineEnd", new Class[] {int.class});
                    getLineEndMethod.setAccessible(true);
                    Integer lineEnd = (Integer)getLineEndMethod.invoke(document, new Object[] {Integer.valueOf(line)});
                    if(lineStart.intValue() == lineEnd.intValue()) {
                        // line is empty
                        return "";
                    }
                    Method getTextMethod = clazz.getDeclaredMethod("getText", new Class[] {int.class, int.class});
                    getTextMethod.setAccessible(true);
                    return getTextMethod.invoke(document, new Object[] {lineStart, new Integer(lineEnd.intValue()-lineStart.intValue())}).toString();
                } catch (Exception e) {
                    throw new JemmyException("Getting text by reflection failed.", e);
                }
            }});
    }
    
    /** SubChooser to determine OutputTab component
     * Used in findTopComponent method.
     */
    protected static final class OutputTabSubchooser implements ComponentChooser {
        
        /** Name of OutputTab to search for. */
        private String tabName = null;
        
        public OutputTabSubchooser() {
        }
        
        public OutputTabSubchooser(String tabName) {
            this.tabName = tabName;
        }
        
        public boolean checkComponent(Component comp) {
            if(comp.getClass().getName().endsWith("OutputTab")) {  // NOI18N
                return Operator.getDefaultStringComparator().equals(comp.getName(), tabName);
            } else {
                return false;
            }
        }
        
        public String getDescription() {
            return "org.netbeans.core.output2.OutputTab" + // NOI18N
                    ((tabName != null) ? " with \"" + tabName + "\" name" : "");  // NOI18N
        }
    }
    
    /** Performs verification by accessing all sub-components */
    public void verify() {
        outputPaneOperator();
    }
    
    /****************************** Actions *****************************/

    /** Performs copy action. */
    public void copy() {
        copyAction.perform(outputPaneOperator());
    }
    
    /** Performs find action. */
    public void find() {
        findAction.perform(outputPaneOperator());
    }
    
    /** Performs find next action. */
    public void findNext() {
        findNextAction.perform(outputPaneOperator());
    }
    
    /** Performs next error action. */
    public void nextError() {
        nextErrorAction.perform(outputPaneOperator());
    }
    
    /** Performs next error action. */
    public void previousError() {
        previousErrorAction.perform(outputPaneOperator());
    }

    /** Performs wrap text action. */
    public void wrapText() {
        wrapTextAction.perform(outputPaneOperator());
    }
    
    /** Performs save as action. */
    public void saveAs() {
        saveAsAction.perform(outputPaneOperator());
    }
    
    /** Performs close action. */
    public void close() {
        closeAction.perform(outputPaneOperator());
    }
    
    /** Performs clear action. */
    public void clear() {
        clearAction.perform(outputPaneOperator());
    }
    
    /** Performs select all action. */
    public void selectAll() {
        selectAllAction.perform(outputPaneOperator());
    }
}
