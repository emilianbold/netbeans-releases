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

package org.netbeans.modules.versioning.util;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.versioning.util.VCSKenaiSupport.KenaiUser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class VCSHyperlinkSupport {
    private static Logger LOG = Logger.getLogger(VCSHyperlinkSupport.class.getName());
    private Map<Integer, List<Linker>> linkers = new HashMap<Integer, List<Linker>>();

    public <T extends Linker> T getLinker(Class<T> t, int idx) {
        List<Linker> list = linkers.get(idx);
        if(list == null) return null;
        for (Linker linker : list) {
            if(linker.getClass() == t) return (T) linker;
        }
        return null;
    }

    public void add(Linker l, int idx) {
        if(l == null) return;
        List<Linker> list = linkers.get(idx);
        if(list == null) {
            list = new ArrayList<Linker>();
        }
        list.add(l);
        linkers.put(idx, list);
    }

    public boolean mouseMoved(Point p, JComponent component, int idx) {
        List<Linker> list = linkers.get(idx);
        if(list == null) return false;
        for (Linker linker : list) {
            if(linker.mouseMoved(p, component)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseClicked(Point p, int idx) {
        List<Linker> list = linkers.get(idx);
        if(list == null) return false;
        for (Linker linker : list) {
            if(linker.mouseClicked(p)) {
                return true;
            }
        }
        return false;
    }

    public void computeBounds(JTextPane textPane, int idx) {
        List<Linker> list = linkers.get(idx);
        if(list == null) return ;
        for (Linker linker : list) {
            linker.computeBounds(textPane);
        }
    }

    public static abstract class Linker {
        public abstract boolean mouseMoved(Point p, JComponent component);
        public abstract boolean mouseClicked(Point p);
        public abstract void computeBounds(JTextPane textPane);
        public abstract void insertString(StyledDocument sd, Style style) throws BadLocationException;
    }

    public static class IssueLinker extends Linker {

        private Rectangle bounds[];
        private final int docstart[];
        private final int docend[];
        private final int start[];
        private final int end[];
        private final String text;
        private final HyperlinkProvider hp;
        private final File root;
        private final int length;
        private final Style issueHyperlinkStyle;

        private IssueLinker(HyperlinkProvider hp, Style issueHyperlinkStyle, File root, StyledDocument sd, String text, int[] spans) {
            this.length = spans.length / 2;
            this.docstart = new int[length];
            this.docend = new int[length];
            this.start = new int[length];
            this.end = new int[length];
            this.hp = hp;
            this.root = root;
            this.text = text;
            this.issueHyperlinkStyle = issueHyperlinkStyle;

            for (int i = 0; i < spans.length;) {
                int linkeridx = i / 2;
                int spanstart = spans[i++];
                int spanend = spans[i++];
                if(spanend < spanstart) {
                    LOG.warning("Hyperlink provider " + hp.getClass().getName() + " returns wrong spans [" + spanstart + "," + spanend + "]");
                    continue;
                }

                int doclen = sd.getLength();
                this.start[linkeridx] = spanstart;
                this.end[linkeridx] = spanend;
                this.docstart[linkeridx] = doclen + spanstart;
                this.docend[linkeridx] = doclen + spanend;
            }
        }

        public static IssueLinker create(HyperlinkProvider hp, Style issueHyperlinkStyle, File root, StyledDocument sd, String text) {
            int[] spans = hp.getSpans(text);
            if (spans == null) {
                return null;
            }
            if(spans.length % 2 != 0) {
                // XXX more info and log only _ONCE_
                LOG.warning("Hyperlink provider " + hp.getClass().getName() + " returns wrong spans");
                return null;
            }
            if(spans.length > 0) {
                IssueLinker l = new IssueLinker(hp, issueHyperlinkStyle, root, sd, text, spans);
                return l;
            }
            return null;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle[length];
            for (int i = 0; i < length; i++) {
                try {
                    Rectangle startr = tui.modelToView(textPane, docstart[i], Position.Bias.Forward).getBounds();
                    Rectangle endr = tui.modelToView(textPane, docend[i], Position.Bias.Backward).getBounds();
                    this.bounds[i] = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                } catch (BadLocationException ex) { }
            }
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            for (int i = 0; i < start.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            for (int i = 0; i < start.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    hp.onClick(root, text, start[i], end[i]);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void insertString(StyledDocument sd, Style style) throws BadLocationException {
            sd.insertString(sd.getLength(), text, style);
            for (int i = 0; i < length; i++) {
                sd.setCharacterAttributes(sd.getLength() - text.length() + start[i], end[i] - start[i], issueHyperlinkStyle, false);
            }
        }
    }

    public static class AuthorLinker extends Linker {
        private static final String AUTHOR_ICON_STYLE   = "authorIconStyle";    // NOI18N

        private Rectangle bounds;
        private final int docstart;
        private final int docend;
        private final KenaiUser kenaiUser;
        private final String author;
        private final Style authorStyle;

        public AuthorLinker(KenaiUser kenaiUser, Style authorStyle, StyledDocument sd, String author) throws BadLocationException {
            this.kenaiUser = kenaiUser;
            this.author = author;
            this.authorStyle = authorStyle;

            int doclen = sd.getLength();
            int textlen = author.length();

            docstart = doclen;
            docend = doclen + textlen;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle();
            try {
                Rectangle startr = tui.modelToView(textPane, docstart, Position.Bias.Forward).getBounds();
                Rectangle endr = tui.modelToView(textPane, docend, Position.Bias.Backward).getBounds();
                Rectangle bounds = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                this.bounds = bounds;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                kenaiUser.startChat();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(VCSHyperlinkSupport.class, "LBL_StartChat", author));
                return true;
            }
            return false;
        }

        @Override
        public void insertString(StyledDocument sd, Style style) throws BadLocationException {
            if(style == null) {
                style = authorStyle;
            }
            sd.insertString(sd.getLength(), author, style);

            String iconStyleName = AUTHOR_ICON_STYLE + author;
            Style iconStyle = sd.getStyle(iconStyleName);
            if(iconStyle == null) {
                iconStyle = sd.addStyle(iconStyleName, null);
                StyleConstants.setIcon(iconStyle, kenaiUser.getIcon());
            }
            sd.insertString(sd.getLength(), " ", style);
            sd.insertString(sd.getLength(), " ", iconStyle);
        }
    }
}

