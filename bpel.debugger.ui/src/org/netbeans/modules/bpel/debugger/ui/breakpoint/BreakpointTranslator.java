/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JEditorPane;

import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



public class BreakpointTranslator {
    
    static BreakpointTranslator mInstance;
    
    static {
        mInstance = new BreakpointTranslator ();
    }
    
    public int translateBreakpointLine(String url, int lineNumber) {
        
        try {
            BPELActivityFinderSaxHandler saxHandler = setUPMap(getText(url), lineNumber);
            BPELNode bpelnode = getActNode(saxHandler.getFoundNode());
            if (lineNumber < saxHandler.getFirstActivity().getLineNo()) {
                bpelnode = saxHandler.getFirstActivity();
            } else if (lineNumber > saxHandler.getLastActivity().getLineNo()) {
                bpelnode = saxHandler.getLastActivity();
            }
//            BPELNode bpelnode = getActivity(lineNumber, saxHandler.getAllActMap(), saxHandler.getFirstActivity(), saxHandler.getLastActivity());
            return bpelnode.getLineNo();
        } catch (Exception e) {
            return lineNumber;
        }
    }
     public int translateBreakpointLineWithUrl (String url, int lineNumber) {
        
        try {
            BPELActivityFinderSaxHandler saxHandler = setUPMapWithUrl(url, lineNumber);
            BPELNode bpelnode = getActNode(saxHandler.getFoundNode());
//          BPELNode bpelnode = getActivity(lineNumber, saxHandler.getAllActMap(), saxHandler.getFirstActivity(), saxHandler.getLastActivity());
            return bpelnode.getLineNo();
        } catch (Exception e) {
            return lineNumber;
        }
    }
     
     private BPELActivityFinderSaxHandler setUPMapWithUrl(String url, int lineNumber) throws Exception {
//       Map activityLineXpathMap = new LinkedHashMap();
       BPELActivityFinderSaxHandler saxHandler = new BPELActivityFinderSaxHandler(lineNumber);
       XMLReader xr = XMLReaderFactory.createXMLReader();
       xr.setFeature("http://xml.org/sax/features/namespaces", true);
       xr.setContentHandler(saxHandler);
       xr.setErrorHandler(saxHandler);
       xr.parse(new InputSource(new FileReader(url)));
       return saxHandler;
   }     

    private BPELActivityFinderSaxHandler setUPMap(String fileText, int lineNumber) throws Exception {
//        Map activityLineXpathMap = new LinkedHashMap();
        BPELActivityFinderSaxHandler saxHandler = new BPELActivityFinderSaxHandler(lineNumber);
        XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setContentHandler(saxHandler);
        xr.setErrorHandler(saxHandler);
        xr.parse(new InputSource(new StringReader(fileText)));
        return saxHandler;
    }
    
    private static BPELNode getActNode (BPELNode bpelNode) {
        if (bpelNode == null)
            return null;
        if (bpelNode.isActivity()) {
            return bpelNode;
        }else {
            return getActNode(bpelNode.getParent());            
        }
        
    }

    private static String getText (String url) {
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
    
    private static Line getLine(String url, int line) {
        DataObject dataObject = getDataObject(url);
        
        if (dataObject == null) {
            return null;
        }
        LineCookie lineCookie = (LineCookie) dataObject.getCookie(LineCookie.class);
        
        if (lineCookie == null) {
            return null;
        }
        Line.Set lineSet = lineCookie.getLineSet();
        
        if (lineSet == null) {
            return null;
        }
        try {
            return lineSet.getCurrent(line);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
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
//    public static String checkBreakpoints(List<LineBreakpoint> lineBreakpoints) {
//        // TODO Auto-generated method stub
//        HashMap<String, BPELActivityFinderSaxHandler> saxHandlers = new HashMap (lineBreakpoints.size());
//        StringBuffer resultBuffer = new StringBuffer ();
//        List<LineBreakpoint> removeList = new ArrayList ();
//        for (int i=0; i<lineBreakpoints.size(); i++) {
//            LineBreakpoint linebreakpoint = lineBreakpoints.get(i);
//            BPELActivityFinderSaxHandler saxHandler = saxHandlers.get(linebreakpoint.getURL());
//            if (saxHandler == null) {
//                try {
//                    saxHandler = mInstance.setUPMapWithUrl(linebreakpoint.getURL(), linebreakpoint.getLineNumber());
//                    saxHandlers.put(linebreakpoint.getURL(), saxHandler);                    
//                }catch(Exception e) {
//                    removeList.add(linebreakpoint);
//                    //@Todo, fix using bundle
//                    resultBuffer.append(NbBundle.getMessage(BreakpointTranslator.class, "ERROR_INVALID_BREAKPOINT", getNameInPath(linebreakpoint.getURL(), "/") + " line:" + linebreakpoint.getLineNumber()));
//                }                
//            }
//            
//            if (saxHandler != null && !checkBreakpoints(saxHandler, linebreakpoint)) {
//                int lineNumber = linebreakpoint.getLineNumber();
//                HashMap allactMap = saxHandler.getAllActMap();
//                BPELNode bpelnode =  (BPELNode) allactMap.get(new Integer (lineNumber));
//                try {
//                    if (bpelnode == null) {
//                             saxHandler = mInstance.setUPMapWithUrl(linebreakpoint.getURL(), lineNumber);
//                         
//                        saxHandlers.put(linebreakpoint.getURL(), saxHandler);
//                        bpelnode = getActNode(saxHandler.getFoundNode());
//                    } else {
//                        bpelnode = getActNode(bpelnode);
//                    }
//                    if (lineNumber < saxHandler.getFirstActivity().getLineNo()) {
//                        bpelnode = saxHandler.getFirstActivity();
//                    } else if (lineNumber > saxHandler.getLastActivity().getLineNo()) {
//                        bpelnode = saxHandler.getLastActivity();
//                    }
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    removeList.add(linebreakpoint);
//                    resultBuffer.append(NbBundle.getMessage(BreakpointTranslator.class, "ERROR_INVALID_BREAKPOINT", getNameInPath(linebreakpoint.getURL(), "/") + " line:" + linebreakpoint.getLineNumber()));
//                }
//                removeList.add(linebreakpoint);
//                resultBuffer.append(NbBundle.getMessage(BreakpointTranslator.class, "ERROR_INVALID_BREAKPOINT_SET", new Object [] {getNameInPath(linebreakpoint.getURL(), "/") + " line:" + linebreakpoint.getLineNumber(), bpelnode.getLineNo()}));
//            }
//        }
//        lineBreakpoints.removeAll(removeList);
//        return resultBuffer.toString();
//    }
//    
//    private static String getNameInPath(String path, String separator) {
//        if (path.length() == 1 && path.equals(separator)) {
//            path = "";
//        }
//        else if (path.endsWith(separator)) {
//            path = path.substring(0, path.length() - 1);
//        }
//        int lastSlashIndex = path.lastIndexOf(separator);
//        int nameStart = lastSlashIndex >= 0 ? lastSlashIndex + 1 : 0;
//        return path.substring(nameStart);
//    }    
//    private static String getFileName(String url) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//    private static boolean checkBreakpoints(BPELActivityFinderSaxHandler saxHandler, LineBreakpoint linebreakpoint) {
//        // TODO Auto-generated method stub
//        HashMap allactMap = saxHandler.getAllActMap();
//        BPELNode bpelnode =  (BPELNode) allactMap.get(new Integer (linebreakpoint.getLineNumber()));
//        if (bpelnode == null || !bpelnode.isActivity()) {
//            return false;
//        }
//        return true;        
//    }    

}
