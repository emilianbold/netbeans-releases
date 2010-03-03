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
package org.netbeans.modules.java.stackanalyzer;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 * @author Jan Becicka
 */
final class AnalyzeStackTopComponent extends TopComponent {

    private static AnalyzeStackTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "AnalyzeStackTopComponent";

    private AnalyzeStackTopComponent () {
        initComponents ();
        setName (NbBundle.getMessage (AnalyzeStackTopComponent.class, "CTL_AnalyzeStackTopComponent"));
        setToolTipText (NbBundle.getMessage (AnalyzeStackTopComponent.class, "HINT_AnalyzeStackTopComponent"));
        getActionMap ().put (DefaultEditorKit.pasteAction, new AbstractActionImpl ());
        insertButton.getActionMap ().put (DefaultEditorKit.pasteAction, new AbstractActionImpl ());
        scrollPane.getActionMap ().put (DefaultEditorKit.pasteAction, new AbstractActionImpl ());
        list.getActionMap ().put (DefaultEditorKit.pasteAction, new AbstractActionImpl ());
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        list.setCellRenderer (new AnalyserCellRenderer ());
        list.addKeyListener (new KeyAdapter () {

            @Override
            public void keyTyped (KeyEvent e) {
                if (e.getKeyChar () == KeyEvent.VK_ENTER) {
                    String currentLine = (String) list.getSelectedValue ();
                    select (currentLine);
                }
            }
        });
        list.addMouseListener (new MouseAdapter () {

            @Override
            public void mouseClicked (MouseEvent e) {
                if (e.getClickCount () != 2) return;
                int i = list.locationToIndex (e.getPoint ());
                if (i < 0) return;
                String currentLine = (String) list.getModel ().getElementAt (i);
                select (currentLine);
            }
        });
    }

    private void fillIn (Reader stackTrace) {
        list.setModel (new StackListModel (stackTrace));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        insertButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();

        setName("Form"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(insertButton, org.openide.util.NbBundle.getBundle(AnalyzeStackTopComponent.class).getString("AnalyzeStackTopComponent.insertButton.text")); // NOI18N
        insertButton.setName("insertButton"); // NOI18N
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        scrollPane.setName("scrollPane"); // NOI18N

        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setName("list"); // NOI18N
        scrollPane.setViewportView(list);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                    .add(insertButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(insertButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addContainerGap())
        );

        insertButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AnalyzeStackTopComponent.class, "AnalyzeStackTopComponent.insertButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AnalyzeStackTopComponent.class, "AnalyzeStackTopComponent.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AnalyzeStackTopComponent.class, "AnalyzeStackTopComponent.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        try {
            Clipboard clipBoard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
            Reader stackTrace = DataFlavor.stringFlavor.getReaderForText(clipBoard.getContents(this));//GEN-LAST:event_insertButtonActionPerformed
            fillIn (stackTrace);
        } catch (UnsupportedFlavorException ex) {
            Exceptions.printStackTrace (ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace (ex);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton insertButton;
    private javax.swing.JList list;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized AnalyzeStackTopComponent getDefault () {
        if (instance == null) {
            instance = new AnalyzeStackTopComponent ();
        }
        return instance;
    }

    /**
     * Obtain the AnalyzeStackTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized AnalyzeStackTopComponent findInstance () {
        TopComponent win = WindowManager.getDefault ().findTopComponent (PREFERRED_ID);
        if (win == null) {
            Logger.getLogger (AnalyzeStackTopComponent.class.getName ()).warning (
                "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault ();
        }
        if (win instanceof AnalyzeStackTopComponent) {
            return (AnalyzeStackTopComponent) win;
        }
        Logger.getLogger (AnalyzeStackTopComponent.class.getName ()).warning (
            "There seem to be multiple components with the '" + PREFERRED_ID +
            "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault ();
    }

    @Override
    public int getPersistenceType () {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened () {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed () {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace () {
        return new ResolvableHelper ();
    }

    @Override
    protected String preferredID () {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve () {
            return AnalyzeStackTopComponent.getDefault ();
        }
    }
    /**
     * Regexp matching one line (not the first) of a stack trace.
     * Captured groups:
     * <ol>
     * <li>package
     * <li>filename
     * <li>line number
     * </ol>
     */
    static final Pattern STACK_LINE_PATTERN = Pattern.compile (
        "(\\s)+(|catch )at ((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*)[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$<][a-zA-Z0-9_$>]*\\(([a-zA-Z_$][a-zA-Z0-9_$]*\\.java):([0-9]+)\\)"); // NOI18N
    /**
     * Regexp matching the first line of a stack trace, with the exception message.
     * Captured groups:
     * <ol>
     * <li>unqualified name of exception class plus possible message
     * </ol>
     */
    static final Pattern FIRST_LINE_PATTERN = Pattern.compile (
        // #42894: JRockit uses "Main Thread" not "main"
        "(?:Exception in thread \"(?:main|Main Thread)\" )?(?:(?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)+)([a-zA-Z_$][a-zA-Z0-9_$]*(?:: .+)?)"); // NOI18N

    private void select (String line) {
        Matcher m = STACK_LINE_PATTERN.matcher (line);
        if (m.matches ()) {
            String pkg = m.group (3);
            String filename = m.group (4);
            String resource = pkg.replace ('.', '/') + filename;
            int lineNumber = Integer.parseInt (m.group (5));
            ClassPath cp = ClassPathSupport.createClassPath (GlobalPathRegistry.getDefault ().getSourceRoots ().toArray (new FileObject[0]));
            FileObject source = cp.findResource (resource);
            if (source != null) {
                doOpen (source, lineNumber);
            }
        }
    }

    private static boolean doOpen (FileObject fo, int line) {
        try {
            DataObject od = DataObject.find (fo);
            EditorCookie ec = (EditorCookie) od.getCookie (EditorCookie.class);
            LineCookie lc = (LineCookie) od.getCookie (LineCookie.class);

            if (ec != null && lc != null && line != -1) {
                StyledDocument doc = ec.openDocument ();
                if (doc != null) {
                    if (line != -1) {
                        Line l = lc.getLineSet ().getCurrent (line - 1);

                        if (l != null) {
                            l.show (Line.SHOW_GOTO);
                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = (OpenCookie) od.getCookie (OpenCookie.class);

            if (oc != null) {
                oc.open ();
                return true;
            }
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

        return false;
    }

    private class AbstractActionImpl extends AbstractAction {

        public AbstractActionImpl () {
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            insertButtonActionPerformed (null);
        }
    }
}
