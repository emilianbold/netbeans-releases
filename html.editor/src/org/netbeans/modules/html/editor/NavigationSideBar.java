/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.gsf.api.DataLoadersBridge;
import org.netbeans.modules.html.editor.HtmlCaretAwareSourceTask.Source;
import org.netbeans.modules.html.editor.gsf.HtmlParserResult;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.support.CaretAwareSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author marekfukala
 */
public class NavigationSideBar extends JPanel implements Accessible {

    private JTextComponent component;
    private volatile AttributeSet attribs;
    private Lookup.Result<? extends FontColorSettings> fcsLookupResult;
    private FileObject fileObject;
    private final LookupListener fcsTracker = new LookupListener() {

        public void resultChanged(LookupEvent ev) {
            attribs = null;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    NavigationSideBar.this.repaint();
                }
            });
        }
    };
    private boolean enabled = true;
    private List<AstNode> nesting = new ArrayList<AstNode>(5);

    public NavigationSideBar() {
    }

    public NavigationSideBar(JTextComponent component) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        this.component = component;
        Document doc = component.getDocument();
        this.fileObject = DataLoadersBridge.getDefault().getFileObject(doc);

        Source source = HtmlCaretAwareSourceTask.forDocument(doc);
        System.out.println("NavigationSideBar's Source = " + source);
        source.addChangeListener(new HtmlCaretAwareSourceTask.SourceListener() {

            public void parsed(CompilationInfo info) {
                System.out.println("NavigationSideBar - parsed");
                NavigationSideBar.this.change(info);
            }
        });

        updatePreferredSize();
    }

    private void change(CompilationInfo info) {
        int caretPosition = CaretAwareSourceTaskFactory.getLastPosition(fileObject);

        HtmlParserResult result = (HtmlParserResult) info.getEmbeddedResult("text/html", caretPosition);
        if (result == null) {
            return;
        }

        AstNode root = result.root();

        AstNode current = AstNodeUtils.findDescendant(root, caretPosition);
        if (current == null) {
            return;
        }

//        System.out.println("current ast node for position " + caretPosition + ": " + current.path().toString());

        updateNestingInfo(root, current);

    }

    private void updateNestingInfo(AstNode root, AstNode node) {
        List<AstNode> newNesting = new ArrayList<AstNode>();
        do {
            if(node.type() == AstNode.NodeType.TAG) {
                newNesting.add(0, node);
            }
            node = node.parent();

        } while (node != null && node != root);

        nesting = newNesting;

        //update UI
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
//                NavigationSideBar.this.repaint();
                updatePanelUI();
            }
        });

    }

    private void updatePanelUI() {
        removeAll();


    }

    protected
    @Override
    void paintComponent(Graphics g) {
        if (!enabled) {
            return;
        }
        Rectangle clip = getVisibleRect();//g.getClipBounds();

        //background
//        g.setColor(Color.LIGHT_GRAY);
//        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(Color.BLUE);
        g.drawString(nestingToString(), 0, getColoring().getFont().getSize());

    }

    //XXX cache the string
    private String nestingToString() {
        StringBuilder sb = new StringBuilder();
        Iterator<AstNode> i = nesting.iterator();
        while(i.hasNext()) {
            sb.append(i.next().name() + (i.hasNext() ? "  " : ""));
        }
        return sb.toString();

    }

    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(component.getWidth(), getColoring().getFont().getSize() + 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
        }
        revalidate();
    }

    private Coloring getColoring() {
        if (attribs == null) {
            if (fcsLookupResult == null) {
                fcsLookupResult = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component)).lookupResult(FontColorSettings.class);
                fcsLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, fcsTracker, fcsLookupResult));
            }

            FontColorSettings fcs = fcsLookupResult.allInstances().iterator().next();
//            AttributeSet attr = fcs.getFontColors(FontColorNames.CODE_FOLDING_BAR_COLORING);
//            if (attr == null) {
//                attr = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
//            } else {
            attribs = AttributesUtilities.createComposite(attribs, fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
//            }
//            attribs = attr;
        }
        return Coloring.fromAttributeSet(attribs);
    }

    public static final class NavigationSideBarFactory implements SideBarFactory {

        public JComponent createSideBar(JTextComponent target) {
            return new NavigationSideBar(target);
        }
    }
}
