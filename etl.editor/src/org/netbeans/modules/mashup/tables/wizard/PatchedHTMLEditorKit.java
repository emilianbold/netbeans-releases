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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mashup.tables.wizard;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.Document;
import javax.swing.text.ElementIterator;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author RamaChandraiah
 */
class PatchedHTMLEditorKit extends HTMLEditorKit {

    private javax.swing.text.Element tableElement;
    private int tableNumber;
    private PatchedHTMLEditorKit ek;
    private JEditorPane jep;
    private ChooseTableVisualPanel chooseTableVisualPanel;
    private SortedMap<String, Integer> tableDepth = new TreeMap<String, Integer>();
    private Map<String, javax.swing.text.Element> elementMap = new HashMap<String, javax.swing.text.Element>();

    public PatchedHTMLEditorKit(JEditorPane jEditorPane1, ChooseTableVisualPanel chooseTableVisualPanel) {
        this.chooseTableVisualPanel = chooseTableVisualPanel;
        jep = jEditorPane1;
    }

    public void setTableNumber(int tableNumberIn) {
        tableNumber = tableNumberIn;
    }

    public int getTableNumber() {
        calcTableNumber();
        return tableNumber;
    }

    public void setTableElement(javax.swing.text.Element element) {
        tableElement = element;
    }

    public javax.swing.text.Element getTableElement() {
        return tableElement;
    }

    public void highlightTable(JComboBox jcb) {
        int tableToHighlight = Integer.parseInt(jcb.getSelectedItem().toString());

        setTableNumber(tableToHighlight);
        ek.myController.highlightTable(tableToHighlight);
    }

    public void highlightTable(int tableToHighlight) {
        setTableNumber(tableToHighlight);
        ek.myController.highlightTable(tableToHighlight);
    }
    LinkController myController = new LinkController();
    LinkedList ranges;

    void calcTableNumber() {
        HTMLDocument hdoc = (HTMLDocument) jep.getDocument();

        ElementIterator it = new ElementIterator(hdoc);
        javax.swing.text.Element element = null;
        int count = 0;

        try {
            while ((element = it.next()) != null) {
                if ("table".equalsIgnoreCase(element.getName())) {
                    if (checkIfInnerMostTable(element)) {
                        tableDepth.put("Table #" + String.valueOf(count), tableNumber++);
                        elementMap.put("Table #" + String.valueOf(count++), element);
                    } else {
                        tableNumber++;
                    }
                    if ((element.getStartOffset() == tableElement.getStartOffset()) && (element.getEndOffset() == tableElement.getEndOffset()) && checkIfInnerMostTable(element)) {
                        break;
                    }
                }
                setTableNumber(tableNumber);
            }
        } catch (Exception e) {
            System.out.println("No Table is Clicked");
        }
    }

    private boolean checkIfInnerMostTable(javax.swing.text.Element element) {
        ElementIterator it = new ElementIterator(element);
        javax.swing.text.Element elem = null;
        it.next();
        while ((elem = it.next()) != null) {
            if ("table".equalsIgnoreCase(elem.getName())) {
                return false;
            }
        }
        return true;
    }

    class Node {

        int tableNo;
        long minRange;
        long maxRange;
        }

    @Override
    public void install(JEditorPane c) {
        c.addMouseListener(myController);
        c.addMouseMotionListener(myController);
    }

    public class LinkController extends MouseInputAdapter implements Serializable {

        URL currentUrl = null;
        boolean firstTime = true;
        Object tableWidth = null;

        @Override
        public void mouseClicked(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
            if (!editor.isEditable()) {
                Point pt = new Point(e.getX(), e.getY());
                int pos = editor.viewToModel(pt);
                if (pos >= 0) {
                    activateLink(pos, editor);
                }
                try {
                    Robot r = new Robot();
                    r.keyPress(java.awt.event.KeyEvent.VK_DOWN);
                    r.keyPress(java.awt.event.KeyEvent.VK_DOWN);
                    r.keyPress(java.awt.event.KeyEvent.VK_DOWN);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                int tableNumber = getTableNumber();
                System.out.println("Table:" + tableNumber);
                chooseTableVisualPanel.setTableNum(tableNumber);
            }
        }

        protected void activateLink(int pos, JEditorPane html) {
            Document doc = html.getDocument();

            if (doc instanceof HTMLDocument) {
                HTMLDocument hdoc = (HTMLDocument) doc;
                javax.swing.text.Element e = hdoc.getCharacterElement(pos);

                int textPos = e.getEndOffset();

                javax.swing.text.Element tableElement = null;

                try {
                    while (true) {
                        Object name = e.getAttributes().getAttribute(StyleConstants.NameAttribute);
                        if ((name instanceof HTML.Tag) && (name == HTML.Tag.TABLE)) {
                            tableElement = e;
                            break;
                        } else if ((name instanceof HTML.Tag) && (name == HTML.Tag.BODY)) {
                            break;
                        }
                        e = e.getParentElement();
                    }
                } catch (Exception ex) {

                }

                setTableNumber(tableNumber);
                highlightTable(tableElement);
                setTableElement(tableElement);
            }
        }

        public void highlightTable(javax.swing.text.Element element) {
            Highlighter h = jep.getHighlighter();
            h.removeAllHighlights();
            if (element != null) {
                javax.swing.text.AttributeSet tableAttributes = element.getAttributes();
                Enumeration en = tableAttributes.getAttributeNames();

                tableWidth = null;

                while (en.hasMoreElements()) {
                    Object attribName = en.nextElement();
                    if (attribName.toString().toLowerCase().equals("width")) {
                        tableWidth = tableAttributes.getAttribute(attribName);
                    }
                }

                if (tableWidth == null) {
                    int maxWidth = 0;
                    Rectangle r;

                    for (int i = element.getStartOffset(); i <= element.getEndOffset(); i++) {
                        try {
                            r = jep.modelToView(i);
                        } catch (Exception ex) {
                            break;
                        }

                        if (r.y > maxWidth) {
                            maxWidth = r.y;
                        }
                    }

                    System.out.println("calculated width :: " + maxWidth);

                    tableWidth = Integer.toString(maxWidth);
                }

                try {
                    h.addHighlight(element.getStartOffset(), element.getEndOffset(), new TableHighlightPainter(tableWidth));
                } catch (Exception ex) {
                    System.err.println("Error in Highlighting/No Table Clicked :: " + ex);
                }

                tableWidth = null;
            }
        }

        public void highlightTable(int tableNumber) {
            HTMLDocument hdoc = (HTMLDocument) jep.getDocument();

            ElementIterator iterator = new ElementIterator(hdoc);
            javax.swing.text.Element element = null;

            while (tableNumber > 0 && ((element = iterator.next()) != null)) {
                Object name = element.getAttributes().getAttribute(StyleConstants.NameAttribute);
                if ((name instanceof HTML.Tag) && (name == HTML.Tag.TABLE)) {
                    tableNumber--;
                }
            }

            if (element != null) {
                highlightTable(element);
            }
        }

        class TableHighlightPainter implements Highlighter.HighlightPainter {

            Object tableWidth;

            TableHighlightPainter(Object tableWidth) {
                this.tableWidth = tableWidth;
            }

            public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
                Rectangle r1, r2;

                try {
                    r1 = c.modelToView(p0);
                    r2 = c.modelToView(p1);

                    g = jep.getGraphics();

                    int w = -1;
                    String width = tableWidth.toString();

                    if (width.charAt(width.length() - 1) == '%') {
                        w = Integer.parseInt(width.substring(0, width.length() - 1)) * (jep.getWidth()) / 100;

                        g.setColor(new Color(255, 0, 0));

                        g.drawRect(r1.x - 1, r1.y - 1, w - 22, ((r1.y > r2.y) ? (r1.y - r2.y) : (r2.y - r1.y)) - 8);
                        g.drawRect(r1.x, r1.y, w - 23, ((r1.y > r2.y) ? (r1.y - r2.y) : (r2.y - r1.y)) - 9);
                        g.drawRect(r1.x, r1.y, w - 24, ((r1.y > r2.y) ? (r1.y - r2.y) : (r2.y - r1.y)) - 10);
                    } else {
                        w = 0;

                        for (int i = 0; i < width.length(); i++) {
                            Character ch = width.charAt(i);
                            if (Character.isDigit(ch)) {
                                w = w * 10 + (int) Integer.parseInt(ch.toString());
                            } else {
                                break;
                            }
                        }

                        g.setColor(new Color(255, 0, 0));
                        g.drawRect(r1.x - 1, r1.y - 1, w - 12, ((r1.y > r2.y) ? (r1.y - r2.y) : (r2.y - r1.y)) - 8);
                        g.drawRect(r1.x, r1.y, w - 13, ((r1.y > r2.y) ? (r1.y - r2.y) : (r2.y - r1.y)) - 9);
                        g.drawRect(r1.x, r1.y, w - 14, ((r1.y > r2.y) ? (r1.y - r2.y) : (r2.y - r1.y)) - 10);
                    }
                } catch (Exception e) {
                //e.printStackTrace();
                }
            }
            }
        }
    }
