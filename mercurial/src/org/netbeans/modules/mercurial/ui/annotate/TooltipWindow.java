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
package org.netbeans.modules.mercurial.ui.annotate;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.HyperlinkProvider;

/**
 * Window displaying the line annotation with links to bugtracking in the commit message.
 * @author Ondrej Vrabec
 */
class TooltipWindow implements AWTEventListener, MouseMotionListener, MouseListener {

    /**
     * Full displayed message
     */
    private String message;
    /**
     * Original commit message
     */
    private String commitMessage;
    /**
     * Parent caller
     */
    private final AnnotationBar master;
    private JTextPane textPane;
    private final AnnotateLine annotateLine;
    /**
     * Start of the commit message inside the full displayed message
     */
    private int messageOffset;
    /**
     * Positions of links in the commit message, for fast access
     */
    private Linker positions[];
    /**
     * Positions of links in the commit message
     */
    private LinkedList<Linker> linkers;

    /**
     * Currently showing popup
     */
    private Popup popup;

    public TooltipWindow(AnnotationBar master, final AnnotateLine al) {
        this.annotateLine = al;
        this.master = master;
    }

    public void show(Point location) {
        prepareMessage();
        Rectangle screenBounds = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        for (GraphicsDevice device : gds) {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            screenBounds = gc.getBounds();
            if (screenBounds.contains(location)) {
                break;
            }
        }
        
        // showing the popup tooltip
        TooltipContentPanel cp = new TooltipContentPanel(master.getTextComponent());
        popup = PopupFactory.getSharedInstance().getPopup(SwingUtilities.windowForComponent(master.getTextComponent()),
                cp, (int)location.getX(), (int)location.getY());
        popup.show();
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED || event.getID() == KeyEvent.KEY_PRESSED) {
            onClick(event);
        }
    }

    /**
     * Delegates the click to the hyperlink provider.
     * @param l
     */
    private void click(Linker l) {
        l.provider.onClick(master.getCurrentFile(), commitMessage, l.startOffset, l.endOffset);
    }

    /**
     * Returns a linker behind the current mouse position
     * @param p
     * @return linker behind the current mouse position, or <code>null</code> if no linker is found
     */
    private Linker getLinkerInPosition (Point p) {
        Linker retval = null;
        int offset = textPane.viewToModel(p) - messageOffset;
        if (offset > -1) {
            retval = getLinkerInPosition(offset);
        }
        return retval;
    }

    /**
     * Returns a linker behind the offset in the <strong>commit message</strong> not the <strong>displayed message</strong>.
     * @param p
     * @return linker behind the current mouse position, or <code>null</code> if no linker is found
     */
    private Linker getLinkerInPosition (int offset) {
        Linker l = null;
        if (offset < positions.length) {
            l = positions[offset];
        }
        return l;
    }

    /**
     *
     * @return all linkers behind the commit message as an unmodifiableList
     */
    private List<Linker> getLinkers () {
        return Collections.unmodifiableList(linkers);
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        if (outsideOfTooltipWindow(component)) {
            // hide the tooltip if event occurs outside of the tooltip
            shutdown();
        }
    }

    /**
     *
     * @param component
     * @return <code>true</code> if the <code>component</code> is not part of the tooltip window descendants, <code>false</code> otherwise
     */
    private boolean outsideOfTooltipWindow (Component component) {
        boolean retval = true;
        while (component != null) {
            if (component instanceof TooltipContentPanel) {
                retval = false;
                break;
            }
            component = component.getParent();
        }
        return retval;
    }

    /**
     * Closes the window
     */
    void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (popup != null) {
            popup.hide();
        }
        popup = null;
    }

    /**
     * Prepares the displayed message and populates linkers list
     */
    private void prepareMessage() {
        commitMessage = annotateLine.getCommitMessage();
        StringBuilder sb = new StringBuilder(100);
        sb.append(annotateLine.getRevision()).append(":").append(annotateLine.getId()).append(" - ") // NOI18N
                .append(annotateLine.getAuthor());
        if (annotateLine.getDate() != null) {
            sb.append(" ").append(DateFormat.getDateInstance().format(annotateLine.getDate())); // NOI18N
        }
        positions = new Linker[commitMessage.length()];
        linkers = new LinkedList<Linker>();
        List<HyperlinkProvider> providers = Mercurial.getInstance().getHyperlinkProviders();
        for (HyperlinkProvider hp : providers) {
            int[] spans = hp.getSpans(commitMessage);
            if (spans == null) {
                break;
            }
            int maxIndex = spans.length;
            if ((maxIndex & 1) != 0) {
                // cut to even count
                --maxIndex;
            }
            for (int i = 0; i < maxIndex;) {
                Linker l = new Linker();
                l.startOffset = spans[i++];
                l.endOffset = spans[i++];
                l.provider = hp;
                for (int j = l.startOffset; j < l.endOffset; ++j) {
                    // add linker reference to all relevant positions in the commit message
                    positions[j] = l;
                }
                // and add the linker to the list
                linkers.add(l);
            }
        }

        messageOffset = sb.toString().length() + 1;
        sb.append("\n").append(commitMessage);
        message = sb.toString();
    }

    public void mouseDragged(MouseEvent e) {
        // not interested
    }

    public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(textPane)) {
            Linker l = null;
            if ((l = getLinkerInPosition(e.getPoint())) != null) {
                textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                textPane.setToolTipText(l.provider.getTooltip(commitMessage, l.startOffset, l.endOffset));
                return;
            }
            textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        textPane.setToolTipText("");  // NOI18N
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(textPane)) {
            Linker l = getLinkerInPosition(e.getPoint());
            if (l != null) {
                click(l);
                shutdown(); // close this window
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        // not interested
    }

    public void mouseReleased(MouseEvent e) {
        // not interested
    }

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mouseExited(MouseEvent e) {
        // not interested
    }

    private class TooltipContentPanel extends JComponent {

        public TooltipContentPanel(JTextComponent parentPane) {
            textPane = new JTextPane();
            textPane.setText(message);

            StyledDocument doc = (StyledDocument) textPane.getDocument();

            Style normalStyle = textPane.getStyle("normal"); // NOI18N
            Style hyperlinkStyle = textPane.addStyle("hyperlink", normalStyle); // NOI18N
            StyleConstants.setForeground(hyperlinkStyle, Color.BLUE);
            StyleConstants.setUnderline(hyperlinkStyle, true);

            textPane.setDocument(doc);
            textPane.setEditable(false);
            Color color = new Color(233, 241, 255);
            textPane.setBackground(color);

            Element rootElement = org.openide.text.NbDocument.findLineRootElement(doc);
            int lineCount = rootElement.getElementCount();
            int height = textPane.getFontMetrics(textPane.getFont()).getHeight() * (lineCount + 1);

            List<Linker> linkers = getLinkers();
            for (Linker l : linkers) {
                doc.setCharacterAttributes(messageOffset + l.startOffset, l.endOffset - l.startOffset, hyperlinkStyle, true);
            }

            int maxWidth = 0;
            for (int line = 0; line < lineCount; line++) {
                Element lineElement = rootElement.getElement(line);
                String text = null;
                try {
                    text = doc.getText(lineElement.getStartOffset(), lineElement.getEndOffset() - lineElement.getStartOffset());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                int lineLength = textPane.getFontMetrics(textPane.getFont()).stringWidth(text);
                if (lineLength > maxWidth) {
                    maxWidth = lineLength;
                }
            }

            if (maxWidth < 50) {
                maxWidth = 50;   // too thin component causes repaint problems
            }
            textPane.setPreferredSize(new Dimension(maxWidth * 7 / 6, height));

            if (!textPane.isEditable()) {
                textPane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
            }

            textPane.addMouseListener(TooltipWindow.this);
            textPane.addMouseMotionListener(TooltipWindow.this);

            JScrollPane jsp = new JScrollPane(textPane);
            jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

            setLayout(new BorderLayout());
            add(jsp);
        }
    }

    private class Linker {
        private int startOffset;
        private int endOffset;
        private HyperlinkProvider provider;
    }
}
