/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.bookmarks;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Management of the bookmarks persistence.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class BookmarksXMLHandler {
    
    // Element names
    static final String EDITOR_BOOKMARKS_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/1"; // NOI18N
    static final String EDITOR_BOOKMARKS_ELEM = "editor-bookmarks"; // NOI18N
    private static final String FILE_ELEM = "file"; // NOI18N
    private static final String URL_ELEM = "url"; // NOI18N
    private static final String LINE_ELEM = "line"; // NOI18N
    
    private BookmarksXMLHandler() {
    }

    public static void loadFileBookmarksMap(
    FileBookmarksMap fileBookmarksMap, Element bookmarksElem, URL baseURL) {
        
        Node fileElem = skipNonElementNode(bookmarksElem.getFirstChild());
        while (fileElem != null) {
            assert FILE_ELEM.equals(fileElem.getNodeName());
            Node urlElem = skipNonElementNode(fileElem.getFirstChild());
            assert URL_ELEM.equals(urlElem.getNodeName());
            Node lineElem = skipNonElementNode(urlElem.getNextSibling());
            int[] lineIndexesArray = new int[1];
            int lineCount = 0;
            while (lineElem != null) {
                assert LINE_ELEM.equals(lineElem.getNodeName());
                // Check whether there is enough space in the line number array
                if (lineCount == lineIndexesArray.length) {
                    lineIndexesArray = reallocateIntArray(lineIndexesArray, lineCount, lineCount << 1);
                }
                // Fetch the line number from the node
                try {
                    Node lineElemText = lineElem.getFirstChild();
                    String lineNumberString = lineElemText.getNodeValue();
                    int lineNumber = Integer.parseInt(lineNumberString);
                    lineIndexesArray[lineCount++] = lineNumber;
                } catch (DOMException e) {
                    ErrorManager.getDefault().notify(e);
                } catch (NumberFormatException e) {
                    ErrorManager.getDefault().notify(e);
                }
                lineElem = skipNonElementNode(lineElem.getNextSibling());
            }
            
            try {
                URL url;
                try {
                    Node urlElemText = urlElem.getFirstChild();
                    String relOrAbsURLString = urlElemText.getNodeValue();
                    URI uri = new URI(relOrAbsURLString);
                    if (!uri.isAbsolute() && baseURL != null) { // relative URI
                        url = new URL(baseURL, relOrAbsURLString);
                    } else { // absolute URL or don't have base URL
                        url = new URL(relOrAbsURLString);
                    }
                } catch (URISyntaxException e) {
                    ErrorManager.getDefault().notify(e);
                    url = null;
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().notify(e);
                    url = null;
                }
                
                if (url != null) {
                    if (lineCount != lineIndexesArray.length) {
                        lineIndexesArray = reallocateIntArray(lineIndexesArray, lineCount, lineCount);
                    }
                    fileBookmarksMap.put(new FileBookmarks(url, lineIndexesArray));
                }
            } catch (DOMException e) {
                ErrorManager.getDefault().notify(e);
            }

            fileElem = skipNonElementNode(fileElem.getNextSibling());
        }
    }
    
    private static Node skipNonElementNode(Node node) {
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
            node = node.getNextSibling();
        }
        return node;
    }
    
    public static Element saveFileBookmarksMap(
    FileBookmarksMap fileBookmarksMap, URI baseURI) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.newDocument();
            Element bookmarksElem = xmlDoc.createElementNS(
                    EDITOR_BOOKMARKS_NAMESPACE_URI, EDITOR_BOOKMARKS_ELEM);
            for (Iterator it = fileBookmarksMap.all().iterator(); it.hasNext();) {
                FileBookmarks bookmarks = (FileBookmarks)it.next();
                if (bookmarks.getBookmarkCount() > 0) {
                    saveFileBookmarks(bookmarks, xmlDoc, bookmarksElem, baseURI);
                }
            }
            return bookmarksElem;

        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }

    private static void saveFileBookmarks(FileBookmarks fileBookmarks,
    Document xmlDoc, Element bookmarksElem, URI baseURI) throws DOMException {
        
        Element fileElem = xmlDoc.createElementNS(EDITOR_BOOKMARKS_NAMESPACE_URI, FILE_ELEM);

        Element urlElem = xmlDoc.createElementNS(EDITOR_BOOKMARKS_NAMESPACE_URI, URL_ELEM);
        String relOrAbsURL = fileBookmarks.getURL().toExternalForm();
        // Possibly relativize the URL
        if (baseURI != null) {
            try {
                URI absURI = new URI(relOrAbsURL);
                URI relURI = baseURI.relativize(absURI);
                relOrAbsURL = relURI.toString();
            } catch (URISyntaxException e) {
                ErrorManager.getDefault().notify(e);
                // leave the original full URL
            }
        }

        urlElem.appendChild(xmlDoc.createTextNode(relOrAbsURL));
        fileElem.appendChild(urlElem);
        
        int bookmarkCount = fileBookmarks.getBookmarkCount();
        for (int i = 0; i < bookmarkCount; i++) {
            int bookmarkLine = fileBookmarks.getBookmarkLineIndex(i);
            Element lineElem = xmlDoc.createElementNS(EDITOR_BOOKMARKS_NAMESPACE_URI, LINE_ELEM);
            lineElem.appendChild(xmlDoc.createTextNode(Integer.toString(bookmarkLine)));
            fileElem.appendChild(lineElem);
        }
        
        bookmarksElem.appendChild(fileElem);
    }
    
   static int[] reallocateIntArray(int[] intArray, int count, int newLength) {
        int[] newIntArray = new int[newLength];
        System.arraycopy(intArray, 0, newIntArray, 0, count);
        return newIntArray;
    }
    
}

