/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui;

import java.net.MalformedURLException;
import org.netbeans.modules.kenai.ui.spi.LinkNode;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 * @author Jan Becicka
 */
final class KenaiTopComponent extends TopComponent {

    private static KenaiTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/kenai/ui/resources/kenai-small.png";
    private static final String PREFERRED_ID = "KenaiTopComponent";
    private JTree tree;

    private KenaiTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(KenaiTopComponent.class, "CTL_KenaiTopComponent"));
        setToolTipText(NbBundle.getMessage(KenaiTopComponent.class, "HINT_KenaiTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        setLayout(new BorderLayout());

        createTree();
        add(tree, BorderLayout.CENTER);
        tree.setModel(new KenaiProjectsTreeModel());


//        setLayout(new VerticalLayout());
//        add(new KenaiProjectWidget(null));
//        add(new KenaiProjectWidget(null));
//        add(new JPanel());

//        TableModel dm = new DefaultTableModel(new String[][]{{"Foo", "Foo2"}, {"Bar", "Bar2"}}, new String[]{"Name","Name2" });
//        TableColumnModel cm = new DefaultTableColumnModel();
//        ListSelectionModel sm = new DefaultListSelectionModel();
//        add(new BaseTable(dm, null, null));
    //add(new JTable(dm,null,null));
    }
    TreePath oldLeadSelectionPath;
    private void createTree() {
        tree = new JTree();
        tree.setRootVisible(false);

        //tree.putClientProperty("JTree.lineStyle", "None");
//        tree.setUI(new BasicTreeUI() {
//
//            @Override
//            protected MouseListener createMouseListener() {
//                return new BasicTreeUI.MouseHandler() {
//                    /**
//                     * Invoked when a mouse button has been pressed on a component.
//                     */
//                    @Override
//                    public void mousePressed(MouseEvent e) {
//                        JTree tree = (JTree) e.getComponent();
//                        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
//                        Rectangle bounds = tree.getPathBounds(path);
//                        if (bounds.contains(e.getPoint())) {
//                            TreeNode node = (TreeNode) path.getLastPathComponent();
//                            final int indexOf = node.toString().indexOf("<a");
//                            if (indexOf > 0) {
//                                final String link = getLink(node.toString(), e.getX() - bounds.x);
//                                if (link != null) {
//                                    ((LinkNode) ((DefaultMutableTreeNode) node).getUserObject()).handleLink(link);
//                                    e.consume();
//                                }
//                            }
//                        }
//                        super.mousePressed(e);
//                    }
//
//                    @Override
//                    public void mouseMoved(MouseEvent e) {
//                        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
//                        Rectangle bounds = tree.getPathBounds(path);
//                        if (!bounds.contains(e.getPoint())) {
//                            tree.setCursor(Cursor.getDefaultCursor());
//                        } else {
//                            TreeNode node = (TreeNode) path.getLastPathComponent();
//                            final int indexOf = node.toString().indexOf("<a");
//                            if (indexOf > 0) {
//                                if (getLink(node.toString(), e.getX() - bounds.x) != null) {
//                                    tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                                } else {
//                                    tree.setCursor(Cursor.getDefaultCursor());
//                                }
//                            } else {
//                                tree.setCursor(Cursor.getDefaultCursor());
//                            }
//                        }
//                        super.mouseMoved(e);
//                    }
//
//                };
//            }
//        }) ;

        BasicTreeUI ui = (BasicTreeUI) tree.getUI();
        tree.setToggleClickCount(1);
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                oldLeadSelectionPath = e.getOldLeadSelectionPath();
                System.out.println("");
            }
        });
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        //renderer.setOpenIcon(null);
        //renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);
        //tree.setCellRenderer(renderer);
        //ui.setLeftChildIndent(0);
        //ui.setRightChildIndent(0);
        //ui.setCollapsedIcon(null);
        //ui.setExpandedIcon(null);

        tree.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                Rectangle bounds = tree.getPathBounds(path);
                if (!bounds.contains(e.getPoint())) {
                    tree.setCursor(Cursor.getDefaultCursor());
                } else {
                    TreeNode node = (TreeNode) path.getLastPathComponent();
                    final int indexOf = node.toString().indexOf("<a");
                    if (indexOf > 0) {
                        if (getLink(node.toString(), e.getX() - bounds.x) != null) {
                            tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            tree.setCursor(Cursor.getDefaultCursor());
                        }
                    } else {
                        tree.setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        });


        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JTree tree = (JTree) e.getComponent();
                TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                Rectangle bounds = tree.getPathBounds(path);
                if (bounds.contains(e.getPoint())) {
                    TreeNode node = (TreeNode) path.getLastPathComponent();
                    final int indexOf = node.toString().indexOf("<a");
                    if (indexOf > 0) {
                        final String link = getLink(node.toString(), e.getX() - bounds.x);
                        if (link != null) {
                            tree.getSelectionModel().setSelectionPath(oldLeadSelectionPath);
                            try {
                                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(link));
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized KenaiTopComponent getDefault() {
        if (instance == null) {
            instance = new KenaiTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the KenaiTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized KenaiTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(KenaiTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof KenaiTopComponent) {
            return (KenaiTopComponent) win;
        }
        Logger.getLogger(KenaiTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return KenaiTopComponent.getDefault();
        }
    }

    private String getLink(String htmlText, int x) {
        StringReader r = new StringReader(htmlText);
        HTMLEditorKit.Parser parse = new HTMLParse().getParser();
        final ParserCB parserCallback = new ParserCB(x);
        try {
            parse.parse(r, parserCallback, true);
        } catch (IOException ex) {
            Logger.getLogger(KenaiTopComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parserCallback.result;
    }

    public static class HTMLParse extends HTMLEditorKit {
        public HTMLEditorKit.Parser getParser() {
            return super.getParser();
        }
    }

    public class ParserCB extends ParserCallback {

        private int currentPos = 0;
        private int mousePosition;
        private boolean a = false;
        private String href;
        public String result;
        private final Font basicFont = UIManager.getFont("Tree.font");
        private final Font boldFont =  basicFont.deriveFont(Font.BOLD);
        private Font currentFont =  basicFont;

        public ParserCB(int x) {
            mousePosition = x;
        }

        @Override
        public void handleEndTag(Tag t, int pos) {
            if (result!=null) return;
            if (t == Tag.A) {
                a = false;
            } else if (t==Tag.B) {
                currentFont= basicFont;
            }
        }

        @Override
        public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
            if (result!=null) return;
            if (t == t.A) {
                this.a = true;
                href = (String) a.getAttribute(HTML.Attribute.HREF);
            } else if (t==Tag.B) {
                currentFont=boldFont;
            }
        }

        @Override
        public void handleText(char[] data, int pos) {
            if (result!=null) return;
            boolean inLink = false;
            if (a) {
                if (mousePosition >= currentPos) {
                    inLink = true;
                }
                a = false;
            }
            currentPos += tree.getFontMetrics(currentFont).charsWidth(data, 0, data.length);
            if (inLink && mousePosition <= currentPos) {
                result = href;
            }

        }
    }
}
