/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import javax.swing.JEditorPane;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class BreakpointTranslator {
    
    static BreakpointTranslator mInstance = new BreakpointTranslator();
    
    public int translateBreakpointLine(
            final String url, 
            final int lineNumber) {
        
        try {
            final BPELActivityFinderSaxHandler saxHandler = 
                    parse(getText(url), lineNumber);
            
            final BPELNode bpelNode = getValidNode(saxHandler.getFoundNode());
//            if (lineNumber < saxHandler.getFirstActivity().getLineNumber()) {
//                bpelnode = saxHandler.getFirstActivity();
//            } else if (lineNumber > saxHandler.getLastActivity().getLineNumber()) {
//                bpelnode = saxHandler.getLastActivity();
//            }
            
            if (bpelNode != null) {
                return bpelNode.getLineNumber();
            }
        } catch (Exception e) {
            // Does nothing
        }
        
        return -1;
    }
    
    private BPELActivityFinderSaxHandler parse(
            final String fileText, 
            final int lineNumber) throws Exception {
            
        final BPELActivityFinderSaxHandler saxHandler = 
                new BPELActivityFinderSaxHandler(lineNumber);
        
        final XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setContentHandler(saxHandler);
        xr.setErrorHandler(saxHandler);
        xr.parse(new InputSource(new StringReader(fileText)));
        
        return saxHandler;
    }
    
    private static BPELNode getValidNode(BPELNode bpelNode) {
        if (bpelNode == null)
            return null;
        
        if (bpelNode.isActivity() || 
                bpelNode.getName().equals("copy") ||
                bpelNode.getName().equals("elseif") ||
                bpelNode.getName().equals("else") ||
                bpelNode.getName().equals("condition") ||
                bpelNode.getName().equals("onAlarm") ||
                bpelNode.getName().equals("onEvent")) {
            return bpelNode;
        } else {
            return getValidNode(bpelNode.getParent());            
        }
        
    }
    
    private static String getText(String url) {
        DataObject dataObject = getDataObject(url);
        if (dataObject == null) {
            return "";
        }  
        EditorCookie editorCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        
        if (editorCookie == null) {
            return "";
        }
        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();
        
        if (editorPanes == null) {
              return "" ;
        }
        if (editorPanes.length == 0) {
            return "";
        }
        return editorPanes[0].getText();

        
    }
    
    private static DataObject getDataObject(String url) {
        FileObject fileObject = FileUtil.toFileObject(new File(url));
        
        if (fileObject == null) {
            return null;
        }
        try {
            return DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {
            return null;
        }
    }
}
