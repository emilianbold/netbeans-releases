/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.visualweb.extension.openide.text;


import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.Timer;

import org.openide.ErrorManager;
import org.openide.windows.TopComponent;

// XXX Copied from previously located openide/src/../text/ dir, this is not a NB code.

/* A transfer handler for the editor component. This seems necessary because
 * the NetBeans editor doesn't inherit from the Swing plaf.basic package,
 * so it's missing a bunch of drag & drop behavior.
 * <p>
 * This code is basically a merged version of text-related code in
 * javax.swing.plaf.basic: BasicTextUI, BasicTransferable, ...
 * I had to copy it since it has package protected access in
 * javax.swing.plaf.basic.
 * <p>
 * <b>There is one important difference</b>. In order to allow the DESTINATION
 * to decide if the transferable should be moved or copied (e.g. if the
 * destination is the same document, move, if it's the clipboard palette,
 * copy), there's a global flag that can be set which basically turns off
 * moving when a drag is in progress. Yup, this is a bit of a hack, but
 * I couldn't find a better way. With Swing, the copy-vs-move decision is
 * made when the drag is -started-, and at that point we don't know yet
 * where you're going to drop. The docs for TransferHandler exportAsDrag says:
 *  <blockquote> action - the transfer action initially requested; this should
 *  be a value of either <code>COPY</code> or <code>MOVE</code>;
 *  the value may be changed during the course of the drag operation
 *  </blockquote>.
 * However, it does not say HOW you can change the action, and from looking
 * at the code, I suspect it cannot be done, since the action is passed
 * to a gesture listener that has private access.
 * <p>
 * @author Tor Norbye
 */

public class TextTransferHandler extends TransferHandler implements UIResource {

    /** Flag which is only defined during a drag & drop operation.
     * Clients (typically drop zones) can set it to true to indicate
     * that the data being dragged should be copied, not moved.
     * For example, the clipboard viewer sets this when it handles
     * the import. That way, the exportDone method knows not to remove
     * the text being placed on the clipboard from the document, since
     * text dragging (without modified keys) defaults to moving, not
     * copying. And we don't want to disallow copying in getSourceActions,
     * since dragging text from one place in the document to another
     * SHOULD be moved, not copied. */
    public static boolean dontRemove = false;


    private JTextComponent exportComp;
    private boolean shouldRemove;
    private int p0;
    private int p1;

    /**
     * Try to find a flavor that can be used to import a Transferable.
     * The set of usable flavors are tried in the following order:
     * <ol>
     *     <li>First, an attempt is made to find a flavor matching the content type
     *         of the EditorKit for the component.
     *     <li>Second, an attempt to find a text/plain flavor is made.
     *     <li>Third, an attempt to find a flavor representing a String reference
     *         in the same VM is made.
     *     <li>Lastly, DataFlavor.stringFlavor is searched for.
     * </ol>
     */
    protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c) {
        DataFlavor plainFlavor = null;
        DataFlavor refFlavor = null;
        DataFlavor stringFlavor = null;
        if (c instanceof JEditorPane) {
            for (int i = 0; i < flavors.length; i++) {
                String mime = flavors[i].getMimeType();
                if (mime.startsWith(((JEditorPane)c).getEditorKit().getContentType())) {
                    return flavors[i];
                } else if (plainFlavor == null && mime.startsWith("text/plain")) {
                    plainFlavor = flavors[i];
                } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref")
                           && flavors[i].getRepresentationClass() == java.lang.String.class) {
                    refFlavor = flavors[i];
                } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                    stringFlavor = flavors[i];
                }
            }
            if (plainFlavor != null) {
                return plainFlavor;
            } else if (refFlavor != null) {
                return refFlavor;
            } else if (stringFlavor != null) {
                return stringFlavor;
            }
            return null;
        }
            
        for (int i = 0; i < flavors.length; i++) {
            String mime = flavors[i].getMimeType();
            if (mime.startsWith("text/plain")) {
                return flavors[i];
            } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref")
                       && flavors[i].getRepresentationClass() == java.lang.String.class) {
                refFlavor = flavors[i];
            } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                stringFlavor = flavors[i];
            }
        }
        if (refFlavor != null) {
            return refFlavor;
        } else if (stringFlavor != null) {
            return stringFlavor;
        }
        return null;
    }

    /**
     * Import the given stream data into the text component.
     */
    protected void handleReaderImport(Reader in, JTextComponent c, boolean useRead)
        throws BadLocationException, IOException {
        if (useRead) {
            int startPosition = c.getSelectionStart();
            int endPosition = c.getSelectionEnd();
            int length = endPosition - startPosition;
            EditorKit kit = c.getUI().getEditorKit(c);
            Document doc = c.getDocument();
            if (length > 0) {
                doc.remove(startPosition, length);
            }
            kit.read(in, doc, startPosition);
        } else {
            char[] buff = new char[1024];
            int nch;
            boolean lastWasCR = false;
            int last;
            StringBuffer sbuff = null;
                
            // Read in a block at a time, mapping \r\n to \n, as well as single
            // \r to \n.
            while ((nch = in.read(buff, 0, buff.length)) != -1) {
                if (sbuff == null) {
                    sbuff = new StringBuffer(nch);
                }
                last = 0;
                for(int counter = 0; counter < nch; counter++) {
                    switch(buff[counter]) {
                    case '\r':
                        if (lastWasCR) {
                            if (counter == 0) {
                                sbuff.append('\n');
                            } else {
                                buff[counter - 1] = '\n';
                            }
                        } else {
                            lastWasCR = true;
                        }
                        break;
                    case '\n':
                        if (lastWasCR) {
                            if (counter > (last + 1)) {
                                sbuff.append(buff, last, counter - last - 1);
                            }
                            // else nothing to do, can skip \r, next write will
                            // write \n
                            lastWasCR = false;
                            last = counter;
                        }
                        break;
                    default:
                        if (lastWasCR) {
                            if (counter == 0) {
                                sbuff.append('\n');
                            } else {
                                buff[counter - 1] = '\n';
                            }
                            lastWasCR = false;
                        }
                        break;
                    }
                }
                if (last < nch) {
                    if (lastWasCR) {
                        if (last < (nch - 1)) {
                            sbuff.append(buff, last, nch - last - 1);
                        }
                    } else {
                        sbuff.append(buff, last, nch - last);
                    }
                }
            }
            if (lastWasCR) {
                sbuff.append('\n');
            }
            c.replaceSelection(sbuff != null ? sbuff.toString() : "");
        }
    }

    // --- TransferHandler methods ------------------------------------

    /**
     * This is the type of transfer actions supported by the source.  Some models are 
     * not mutable, so a transfer operation of COPY only should
     * be advertised in that case.
     * 
     * @param c  The component holding the data to be transfered.  This
     *  argument is provided to enable sharing of TransferHandlers by
     *  multiple components.
     * @return  This is implemented to return NONE if the component is a JPasswordField
     *  since exporting data via user gestures is not allowed.  If the text component is
     *  editable, COPY_OR_MOVE is returned, otherwise just COPY is allowed.
     */
    public int getSourceActions(JComponent c) {
        int actions = NONE;
        if (! (c instanceof JPasswordField)) {
            if (((JTextComponent)c).isEditable()) {
                actions = COPY_OR_MOVE;
            } else {
                actions = COPY;
            }
        }
        return actions;
    }

    /**
     * Create a Transferable to use as the source for a data transfer.
     *
     * @param comp  The component holding the data to be transfered.  This
     *  argument is provided to enable sharing of TransferHandlers by
     *  multiple components.
     * @return  The representation of the data to be transfered. 
     *  
     */
    protected Transferable createTransferable(JComponent comp) {
        exportComp = (JTextComponent)comp;
        shouldRemove = true;
        dontRemove = false;
        p0 = exportComp.getSelectionStart();
        p1 = exportComp.getSelectionEnd();
        return (p0 != p1) ? (new TextTransferable(exportComp, p0, p1)) : null;
    }

    /**
     * This method is called after data has been exported.  This
     * method should remove the data that was transfered if the action
     * was MOVE.
     *
     * @param source The component that was the source of the data.
     * @param data   The data that was transferred or possibly null
     *               if the action is <code>NONE</code>.
     * @param action The actual action that was performed.  
     */
    protected void exportDone(JComponent source, Transferable data, int action) {
        // only remove the text if shouldRemove has not been set to
        // false by importData and only if the action is a move
        if (shouldRemove && action == MOVE) {
            TextTransferable t = (TextTransferable)data;
            if (!dontRemove) {
                t.removeText();
            }
        }
            
        exportComp = null;
    }

    /**
     * This method causes a transfer to a component from a clipboard or a 
     * DND drop operation.  The Transferable represents the data to be
     * imported into the component.  
     *
     * @param comp  The component to receive the transfer.  This
     *  argument is provided to enable sharing of TransferHandlers by
     *  multiple components.
     * @param t     The data to import
     * @return  true if the data was inserted into the component, false otherwise.
     */
    public boolean importData(JComponent comp, Transferable t) {
        JTextComponent c = (JTextComponent)comp;

        // if we are importing to the same component that we exported from
        // then don't actually do anything if the drop location is inside
        // the drag location and set shouldRemove to false so that exportDone
        // knows not to remove any data
        if (c == exportComp && c.getCaretPosition() >= p0 && c.getCaretPosition() <= p1) {
            shouldRemove = false;
            return true;
        }

        boolean imported = false;
        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
        if (importFlavor != null) {
            try {
                boolean useRead = false;
                if (comp instanceof JEditorPane) {
                    JEditorPane ep = (JEditorPane)comp;
                    if (!ep.getContentType().startsWith("text/plain") &&
                        importFlavor.getMimeType().startsWith(ep.getContentType())) {
                        useRead = true;
                        
                    }

                    // XXX The hack, in order to call the callback (which in this case is 
                    // expected to show the dialog letting user to deal with parametrized code clip at the drop time),
                    // masked into special flavor.
                    if(t.isDataFlavorSupported(CodeClipTransferData.CODE_CLIP_DATA_FLAVOR)) {
                        try {
                            Runnable r = (Runnable)t.getTransferData(CodeClipTransferData.CODE_CLIP_DATA_FLAVOR);
                            if(r != null) {
                                r.run();
                            }
                        } catch(IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        } catch(UnsupportedFlavorException ufe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ufe);
                        }
                    }
                }
                Reader r = importFlavor.getReaderForText(t);
                handleReaderImport(r, c, useRead);
                imported = true;
                
                // #4946925 Trying to put the activation to the drop target.
                TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, c);
                if(tc != null) {
                    tc.requestActive();
                }
            } catch (UnsupportedFlavorException ufe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ufe);
            } catch (BadLocationException ble) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        return imported;
    }

    /**
     * This method indicates if a component would accept an import of the given
     * set of data flavors prior to actually attempting to import it. 
     *
     * @param comp  The component to receive the transfer.  This
     *  argument is provided to enable sharing of TransferHandlers by
     *  multiple components.
     * @param flavors  The data formats available
     * @return  true if the data can be inserted into the component, false otherwise.
     */
    public boolean canImport(JComponent comp, DataFlavor[] flavors) {
        JTextComponent c = (JTextComponent)comp;
        if (!(c.isEditable() && c.isEnabled())) {
            return false;
        }
        return (getImportFlavor(flavors, c) != null);
    }

    /**
     * A possible implementation of the Transferable interface
     * for text components.  For a JEditorPane with a rich set
     * of EditorKit implementations, conversions could be made
     * giving a wider set of formats.  This is implemented to
     * offer up only the active content type and text/plain
     * (if that is not the active format) since that can be
     * extracted from other formats.
     */
    static class TextTransferable implements Transferable, UIResource {
            
        // begin copied from BasicTransferable
        protected String plainData = null;
        protected String htmlData = null;

        private static DataFlavor[] htmlFlavors;
        private static DataFlavor[] stringFlavors;
        private static DataFlavor[] plainFlavors;

        static {
            try {
                htmlFlavors = new DataFlavor[3];
                htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
                htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
                htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");

                plainFlavors = new DataFlavor[3];
                plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String");
                plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
                plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");

                stringFlavors = new DataFlavor[2];
                stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=java.lang.String");
                stringFlavors[1] = DataFlavor.stringFlavor;
 
            } catch (ClassNotFoundException cle) {
                System.err.println("error initializing javax.swing.plaf.basic.BasicTranserable");
            }
        }
    
        /**
         * Returns an array of DataFlavor objects indicating the flavors the data 
         * can be provided in.  The array should be ordered according to preference
         * for providing the data (from most richly descriptive to least descriptive).
         * @return an array of data flavors in which this data can be transferred
         */
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] richerFlavors = getRicherFlavors();
            int nRicher = (richerFlavors != null) ? richerFlavors.length : 0;
            int nHTML = (isHTMLSupported()) ? htmlFlavors.length : 0;
            int nPlain = (isPlainSupported()) ? plainFlavors.length: 0;
            int nString = (isPlainSupported()) ? stringFlavors.length : 0;
            int nFlavors = nRicher + nHTML + nPlain + nString;
            DataFlavor[] flavors = new DataFlavor[nFlavors];
	
            // fill in the array
            int nDone = 0;
            if (nRicher > 0) {
                System.arraycopy(richerFlavors, 0, flavors, nDone, nRicher);
                nDone += nRicher;
            }
            if (nHTML > 0) {
                System.arraycopy(htmlFlavors, 0, flavors, nDone, nHTML);
                nDone += nHTML;
            }
            if (nPlain > 0) {
                System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
                nDone += nPlain;
            }
            if (nString > 0) {
                System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
                nDone += nString;
            }
            return flavors;
        }

        /**
         * Returns whether or not the specified data flavor is supported for
         * this object.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns an object which represents the data to be transferred.  The class 
         * of the object returned is defined by the representation class of the flavor.
         *
         * @param flavor the requested flavor for the data
         * @see DataFlavor#getRepresentationClass
         * @exception IOException                if the data is no longer available
         *              in the requested flavor.
         * @exception UnsupportedFlavorException if the requested data flavor is
         *              not supported.
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            DataFlavor[] richerFlavors = getRicherFlavors();
            if (isRicherFlavor(flavor)) {
                return getRicherData(flavor);
            } else if (isHTMLFlavor(flavor)) {
                String data = getHTMLData();
                data = (data == null) ? "" : data;
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new StringBufferInputStream(data);
                }
                // fall through to unsupported
            } else if (isPlainFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new StringBufferInputStream(data);
                }
                // fall through to unsupported

            } else if (isStringFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;
                return data;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        // --- richer subclass flavors ----------------------------------------------

        protected boolean isRicherFlavor(DataFlavor flavor) {
            DataFlavor[] richerFlavors = getRicherFlavors();
            int nFlavors = (richerFlavors != null) ? richerFlavors.length : 0;
            for (int i = 0; i < nFlavors; i++) {
                if (richerFlavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
	
        // --- html flavors ----------------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is an HTML flavor that
         * is supported.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isHTMLFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = htmlFlavors;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Should the HTML flavors be offered?  If so, the method
         * getHTMLData should be implemented to provide something reasonable.
         */
        protected boolean isHTMLSupported() {
            return htmlData != null;
        }

        /**
         * Fetch the data in a text/html format
         */
        protected String getHTMLData() {
            return htmlData;
        }

        // --- plain text flavors ----------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is an plain flavor that
         * is supported.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isPlainFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = plainFlavors;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Should the plain text flavors be offered?  If so, the method
         * getPlainData should be implemented to provide something reasonable.
         */
        protected boolean isPlainSupported() {
            return plainData != null;
        }

        /**
         * Fetch the data in a text/plain format.
         */
        protected String getPlainData() {
            return plainData;
        }

        // --- string flavorss --------------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is a String flavor that
         * is supported.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isStringFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = stringFlavors;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        // end copied from BasicTransferable


        TextTransferable(JTextComponent c, int start, int end) {
            this.c = c;
                
            Document doc = c.getDocument();

            try {
                p0 = doc.createPosition(start);
                p1 = doc.createPosition(end);

                plainData = c.getSelectedText();

                if (c instanceof JEditorPane) {
                    JEditorPane ep = (JEditorPane)c;
                        
                    mimeType = ep.getContentType();

                    if (mimeType.startsWith("text/plain")) {
                        return;
                    }

                    StringWriter sw = new StringWriter(p1.getOffset() - p0.getOffset());
                    ep.getEditorKit().write(sw, doc, p0.getOffset(), p1.getOffset() - p0.getOffset());
                        
                    if (mimeType.startsWith("text/html")) {
                        htmlData = sw.toString();
                    } else {
                        richText = sw.toString();
                    }
                }
            } catch (BadLocationException ble) {
            } catch (IOException ioe) {
            }
        }

        void removeText() {
            if ((p0 != null) && (p1 != null) && (p0.getOffset() != p1.getOffset())) {
                try {
                    Document doc = c.getDocument();
                    doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
                } catch (BadLocationException e) {
                }
            }
        }

        // ---- EditorKit other than plain or HTML text -----------------------

        /** 
         * If the EditorKit is not for text/plain or text/html, that format
         * is supported through the "richer flavors" part of BasicTransferable.
         */
        protected DataFlavor[] getRicherFlavors() {
            if (richText == null) {
                return null;
            }

            try {
                DataFlavor[] flavors = new DataFlavor[3];
                flavors[0] = new DataFlavor(mimeType + ";class=java.lang.String");
                flavors[1] = new DataFlavor(mimeType + ";class=java.io.Reader");
                flavors[2] = new DataFlavor(mimeType + ";class=java.io.InputStream;charset=unicode");
                return flavors;
            } catch (ClassNotFoundException cle) {
                // fall through to unsupported (should not happen)
            }

            return null;
        }

        /**
         * The only richer format supported is the file list flavor
         */
        protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (richText == null) {
                return null;
            }

            if (String.class.equals(flavor.getRepresentationClass())) {
                return richText;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(richText);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringBufferInputStream(richText);
            }
            throw new UnsupportedFlavorException(flavor);
        }

        Position p0;
        Position p1;
        String mimeType;
        String richText;
        JTextComponent c;
    }
    
    // XXX Hack to enable poping up a dialog when DnD of code clip.
    public static class CodeClipTransferData extends StringSelection {
        
        // XXX Fake DataFlavor.. for cheating to retrieve the callback.
        private static final DataFlavor CODE_CLIP_DATA_FLAVOR
            = new DataFlavor(CodeClipTransferData.class, CodeClipTransferData.class.getName()); // TEMP
        
        // XXX Callback to provide the popup.
        private Runnable callback;
        // XXX We need to manipulate the data in this class (the superclass is private).
        private String data;
        
        
        public CodeClipTransferData(String data) {
            super(data); // Just fake.
            this.data = data;
        }

        
        public void setCallback(Runnable callback) {
            this.callback = callback;
        }
        
        public void resetData(String data) {
            this.data = data;
        }

        // XXX Overriden to manipulate with our data field.
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] dfs = super.getTransferDataFlavors();
            List flavors = new ArrayList(Arrays.asList(dfs));
            flavors.add(CODE_CLIP_DATA_FLAVOR);
            return (DataFlavor[])flavors.toArray(new DataFlavor[0]);
        }
        
        // XXX Overriden to manipulate with our data field.
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if(flavor == CODE_CLIP_DATA_FLAVOR) {
                return true;
            }
            
            return super.isDataFlavorSupported(flavor);
        }
        
        // XXX Overriden to manipulate with our data field and provide the hacking callback.
        public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            if(flavor == CODE_CLIP_DATA_FLAVOR) {
                return callback;
            }
            
            // JCK Test StringSelection0007: if 'flavor' is null, throw NPE
            if (flavor.equals(DataFlavor.stringFlavor)) {
                return (Object)data;
            } else if (flavor.equals(DataFlavor.plainTextFlavor)) { // deprecated
                return new StringReader(data);
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

    }
}
