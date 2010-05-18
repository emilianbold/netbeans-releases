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


package org.netbeans.modules.visualweb.designer.markup;


import org.netbeans.modules.visualweb.api.insync.InSyncService;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
 * Utilities for markup packages.
 *
 * Delegated to MarkupService.
 *
 * @author Peter Zavadsky
 */
final class MarkupUtilities {

    // From org.apache.batik.util.XMLConstants:
    // XXX Then from org.netbeans.modules.visualweb.insync.Util.
   /**
     * The XML namespace URI.
     */
    private static String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace"; // NOI18N

    private MarkupUtilities() {
    }

    /**
     * Returns the xml:base attribute value of the given element
     * Resolving any dependency on parent bases if needed.
     */
    // THIS METHOD COPIED FROM org.apache.batik.dom.svg.XMLBaseSupport;
    // that code is in a separate batik jar I don't want to include (batik-dom).
    // I changed it to return a URL instead of converting to String, then back
    // to URL in the client below.
    //
    // THE LICENSE IS OBVIOUSLY THAT OF THE BATIK DISTRIBUTION, NOT THE
    // REST OF THE FILE.
    // XXX Then from org.netbeans.modules.visualweb.insync.Util.
    public static URL getCascadedXMLBase(Element elt) {
        URL base = null;
        Node n = elt.getParentNode();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                base = getCascadedXMLBase((Element)n);
                break;
            }
            /*
            if (n instanceof CSSImportedElementRoot) {
                n = ((CSSImportedElementRoot)n).getCSSParentElement();
            } else {
            */
                n = n.getParentNode();
            /*
            }
            */
        }
        if (base == null) {
//            RaveDocument xhtmlDoc = (RaveDocument)elt.getOwnerDocument();
//            URL url = xhtmlDoc.getUrl();
            Document document = elt.getOwnerDocument();
            URL url = InSyncService.getProvider().getUrl(document);
            if (url != null) {
                return url;
            }
        }
        Attr attr = elt.getAttributeNodeNS(XML_NAMESPACE_URI, "base");
        if (attr != null) {
            try {
                if (base == null) {
                    base = new URL(attr.getNodeValue());
                } else {
                    base = new URL(base, attr.getNodeValue());
                }
            } catch (MalformedURLException ue) {
//                org.openide.ErrorManager.getDefault().notify(ue);
                ue.printStackTrace();
            }
        }
        return base;
    }

//    // XXX From org.netbeans.modules.visualweb.insync.Util.
//    /**
//     * Given an element which may be in a rendered DocumentFragment, return the corresponding JSF
//     * element in the source.
//     */
//    public static Element getCorrespondingSourceElement(Element elem) {
//        if (!(elem instanceof RaveElement)) {
//            return elem;
//        }
//
//        RaveElement element = (RaveElement)elem;
//
//        if (!element.isRendered()) {
//            return element;
//        }
//
//        Node node = element;
//        while (node != null) {
//            if (node instanceof RaveElement) {
//                RaveElement xel = (RaveElement)node;
//                if (xel.isRendered()) {
//                    RaveElement src = xel.getSource();
//                    if (src != null) {
//                        return src;
//                    }
//                }
//            }
//            node = node.getParentNode();
//        }
//        return element.getSourceElement();
//    }
//
//
//    // <markup_separation> copied from insync/Util
//    // XXX This should be separate utility api, openide extension or what.
//    /**
//     * Show the given line in a particular file.
//     *
//     * @param filename The full path to the file
//     * @param lineno The line number
//     * @param openFirst Usually you'll want to pass false. When set to true, this will first open
//     *            the file, then request the given line number; this works around certain bugs for
//     *            some editor types like CSS files.
//     */
//    private static void show(String filename, int lineno, int column, boolean openFirst) {
//        File file = new File(filename);
//        FileObject fo = FileUtil.toFileObject(file);
//        if (fo != null) {
//            show(fo, lineno, column, openFirst);
//        }
//    }
//
//    /**
//     * @param fo
//     * @param lineno
//     * @param column
//     * @param openFirst
//     * @return
//     */
//    private  static void showLineAt(FileObject fo, int lineno, int column, boolean openFirst) {
//        DataObject dobj;
//        try {
//            dobj = DataObject.find(fo);
//        }
//        catch (DataObjectNotFoundException ex) {
//            ErrorManager.getDefault().notify(ex);
//            return;
//        }
//
//        // Try to open doc before showing the line. This SHOULD not be
//        // necessary, except without this the IDE hangs in its attempt
//        // to open the file when the file in question is a CSS file.
//        // Probably a bug in the xml/css module's editorsupport code.
//        // This has the negative effect of first flashing the top
//        // of the file before showing the destination line, so
//        // this operation is made conditional so only clients who
//        // actually need it need to use it.
//        if (openFirst) {
//            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//            if (ec != null) {
//                try {
//                    ec.openDocument(); // ensure that it has been opened - REDUNDANT?
//                    //ec.open();
//                }
//                catch (IOException ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//        }
//
//        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
//        if (lc != null) {
//            Line.Set ls = lc.getLineSet();
//            if (ls != null) {
//                // -1: convert line numbers to be zero-based
//                Line line = ls.getCurrent(lineno-1);
//                // TODO - pass in a column too?
//                line.show(Line.SHOW_GOTO, column);
//            }
//        }
//    }
//    // <markup_separation> copied form insync/Util
//
//    // <markup_separation> moved from insync/MarkupUnit
//    /** Convert the given URL to a path: decode spaces from %20's, etc.
//     * If the url does not begin with "file:" it will not do anything.
//     * @todo Find a better home for this method
//     */
//    public static String fromURL(String url) {
//        if (url.startsWith("file:")) { // NOI18N
//            int n = url.length();
//            StringBuffer sb = new StringBuffer(n);
//            for (int i = 5; i < n; i++) {
//                char c = url.charAt(i);
//                // TODO -- any File.separatorChar manipulation perhaps?
//                if (c == '%' && i < n-3) {
//                    char d1 = url.charAt(i+1);
//                    char d2 = url.charAt(i+2);
//                    if (Character.isDigit(d1) && Character.isDigit(d2)) {
//                        String numString = ""+d1+d2;
//                        try {
//                            int num = Integer.parseInt(numString, 16);
//                            if (num >= 0 && num <= 255) {
//                                sb.append((char)num);
//                                i += 2;
//                                continue;
//                            }
//                        } catch (NumberFormatException nex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nex);
//                        }
//                    }
//                    sb.append(c);
//                } else {
//                    sb.append(c);
//                }
//            }
//            return sb.toString();
//        }
//        return url;
//    }
//    // </markup_separation>


// <error_handling> Moved from RaveDocument.
// XXX These methods are suspicoius, they deal with openide output window.
//    // and there may not be any knowing about it from this impls.
//    /** Clear document related errors. 
//     * @param delayed When set, don't actually clear the errors right now;
//     * it clears the errors next time another error is added. */
//    public static void clearErrors(boolean delayed) {
//        if (delayed) {
//            clearErrors = true;
//        } else {
//            OutputWriter out = getOutputWriter();
//            try {
//                out.reset();
//            }
//            catch (IOException ioe) {
//                // This is lame - our own output window shouldn't
//                // throw IO exceptions!
//                ErrorManager.getDefault().notify(ioe);
//            }
//        }
//    }
//    
//    private static boolean clearErrors = false;
//
//    private static OutputWriter getOutputWriter() {
//        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(MarkupUtilities.class, "WindowTitle"), false);
//        OutputWriter out = io.getOut();
//        return out;
//    }
//    
//    /**
//     * Cause the panel/window within which errors are displayed to come to the front if possible.
//     *
//     */
//    public static void selectErrors() {
//        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(MarkupUtilities.class, "WindowTitle"), false);
//        io.select();
//    }
//    
//    public static void displayError(String message) {
//        displayErrorForFileObject(message, null, -1, -1);
//    }
//    
//    public static void displayErrorForLocation(String message, Object location, int line, int column) {
//        String fileName = computeFilename(location);
//        line = computeLineNumber(location, line);
//            
//        File file = new File(fileName);
//        FileObject fo = FileUtil.toFileObject(file);
//        
//        displayErrorForFileObject(message, fo, line >= 1 ? line : 1, column);
//    }
//    
//    /** Given a general location object provided from the CSS parser,
//     * compute the correct file name to use.
//     */
//    static String computeFilename(Object location) {
//        if (location instanceof String) {
//            return (String)location;
//        } else if (location instanceof URL) {
//            // <markup_separation>
////            return MarkupUnit.fromURL(((URL)location).toExternalForm());
//            // ====
//            return MarkupService.fromURL(((URL)location).toExternalForm());
//            // </markup_separation>
//        } else if (location instanceof Element) {
//            // Locate the filename for a given element
//            Element element = (Element)location;
//            element = MarkupService.getCorrespondingSourceElement(element);
//
//            // <markup_separation>
////            // XXX I should derive this from the engine instead, after all
////            // the engine can know the unit! (Since engines cannot be used
////            // with multiple DOMs anyway)
////            FileObject fo = unit.getFileObject();
//            // ====
//            FileObject fo = InSyncService.getProvider().getFileObject(element.getOwnerDocument());
//            // </markup_separation>
//            File f = FileUtil.toFile(fo);
//
//            return f.toString();
//        } else if (location != null) {
//            return location.toString();
//        }
//
//        return "";
//    }
//    
//    static int computeLineNumber(Object location, int lineno) {
//        if (location instanceof Element) {
//            /*
//            // The location is an XhtmlElement -- so the line number
//            // needs to be relative to it.... compute the line number
//            // of the element
//            if (lineno == -1)
//                lineno = 0;
//            Element element = (Element)location;
//            RaveDocument doc = (RaveDocument)element.getOwnerDocument();
//            lineno += doc.getLineNumber(element);
//             */
//            if (lineno == -1) {
//                lineno = 0;
//            }
//
//            Element element = (Element)location;
//            element = MarkupService.getCorrespondingSourceElement(element);
//            // <markup_separation>
////            lineno += unit.computeLine(element);
//            // ====
//            lineno += InSyncService.getProvider().computeLine(element.getOwnerDocument(), element);
//            // </markup_separation>
//        }
//
//        return lineno;
//    }
//    
//    public static void displayErrorForFileObject(String message, final FileObject fileObject, final int line, final int column){
////        final XhtmlElement e = Util.getSource(element);
//        OutputListener listener;
//        if (fileObject == null) {
//            listener = null;
//        } else {
//            listener = new OutputListener() {
//                public void outputLineSelected(OutputEvent ev) {
//                }
//                public void outputLineAction(OutputEvent ev) {
////                    Util.show(null, unit.getFileObject(), unit.getLine(e),
////                              0, true);
//                    // <markup_separation>
////                    Util.show(null, fileObject, lineNumber, 0, true);
//                    // ====
//                    showLineAt(fileObject, line, column, true);
//                    // </markup_separation>
//                }
//                public void outputLineCleared (OutputEvent ev) {
//                }
//            };
//        }
//
//        displayError(message, listener);
//    }
//    
//    /** 
//     * Display the given error message to the user. The optional listener argument
//     * (pass in null if not applicable) will make the line hyperlinked and the
//     * listener is invoked to process any user clicks.
//     * @param message The string to be displayed to the user
//     * @param listener null, or a listener to be notified when the user clicks
//     *   the linked message
//     */
//    private static void displayError(String message, OutputListener listener) {
//        OutputWriter out = getOutputWriter();
//        try {
//            if (clearErrors) {
//                out.reset();
//                clearErrors = false;
//            }
//            // Write the error message to the output tab:
//            out.println(message, listener);
//        }
//        catch (IOException ioe) {
//            // This is lame - our own output window shouldn't throw IO exceptions!
//            ErrorManager.getDefault().notify(ioe);
//        }
//    }
// </error_handling>

//    // XXX Moved from DesignerService.
//    /**
//     * Return an InputStream for the given CSS URI, if the corresponding CSS
//     * file is open and edited. Otherwise return null.
//     *
//     * @param uri The URI to the CSS file. <b>MUST</b> be an absolute file url!
//     * @return An InputStream for the live edited CSS
//     */
//    public static InputStream getOpenCssStream(String uriString) {
//        try {
//            URI uri = new URI(uriString);
//            File file = new File(uri);
//
//            if (file != null) {
//                FileObject fobj = FileUtil.toFileObject(file);
//
//                if (fobj != null) {
//                    try {
//                        DataObject dobj = DataObject.find(fobj);
//                        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//
//                        if (ec != null) {
//                            javax.swing.text.Document doc = ec.getDocument();
//
//                            if (doc != null) {
//                                // XXX Isn't there a better way to return an input stream
//                                // for a String? Should I have my own?
//                                String s = doc.getText(0, doc.getLength());
//
//                                return new StringBufferInputStream(s);
//                            }
//                        }
//                    } catch (BadLocationException ble) {
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
//                    } catch (DataObjectNotFoundException dnfe) {
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
//                    }
//                }
//            }
//        } catch (URISyntaxException e) {
//            ErrorManager.getDefault().notify(e);
//        }
//
//        return null;
//    }
}
