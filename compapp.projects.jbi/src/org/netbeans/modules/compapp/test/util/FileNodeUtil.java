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


package org.netbeans.modules.compapp.test.util;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

/**
 * Utility to copy file node.
 *
 * @author Jun Qian
 */
public class FileNodeUtil {    
    
    public static void overwriteFile(Node srcNode, Node destNode) throws IOException {
        
        DataObject srcDO = (DataObject) srcNode.getLookup().lookup(DataObject.class);
        FileObject srcFO = srcDO.getPrimaryFile();
        
        DataObject destDO = (DataObject) destNode.getLookup().lookup(DataObject.class);
        FileObject destFO = destDO.getPrimaryFile();
        
        
        // To avoid any confusion, save modified source first.
        if (srcDO.isModified()) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(FileNodeUtil.class, "MSG_SaveModifiedSource", srcFO.getNameExt()), // NOI18N
                    NbBundle.getMessage(FileNodeUtil.class, "TTL_SaveModifiedSource"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                EditorCookie srcEditorCookie =
                        (EditorCookie) srcDO.getCookie(EditorCookie.class);
                srcEditorCookie.saveDocument();
            }
        }
//        // Alternatively, we could use the in-memory copy of the source file
//        EditorCookie srcEditorCookie =
//                (EditorCookie) srcDO.getCookie(EditorCookie.class);
//        Document srcDocument = srcEditorCookie.getDocument();
//        if (srcDocument == null) {
//            Task loadTask = srcEditorCookie.prepareDocument();
//            new RequestProcessor().post(loadTask);
//
//            loadTask.waitFinished();
//            srcDocument = srcEditorCookie.getDocument();
//        }
//        String srcText = srcDocument.getText(0, srcDocument.getLength());
        
        // From now on, we will only be using the on-disk copy of the source file.
        
        if (destDO.isModified()) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(FileNodeUtil.class, "MSG_OverwriteModifiedDestination", destFO.getNameExt()), // NOI18N
                    NbBundle.getMessage(FileNodeUtil.class, "TTL_OverwriteModifiedDestination"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.CANCEL_OPTION) {
                return;
            }
            
            EditorCookie destEditorCookie =
                    (EditorCookie) destDO.getCookie(EditorCookie.class);
            
            try {
                InputStream inputStream = srcFO.getInputStream();
                String srcText = getInputStreamContents(inputStream);
                
                Document outputDocument = destEditorCookie.getDocument();
                try {
                    outputDocument.remove(0, outputDocument.getLength());
                } catch (java.lang.Exception e) {
                    // Ignore exception here on purpose.
                    // One of the listener from xml module throws NPE:
                    // at org.netbeans.modules.xml.text.completion.GrammarManager.isGuarded(GrammarManager.java:170)
                    // at org.netbeans.modules.xml.text.completion.GrammarManager.removeUpdate(GrammarManager.java:140)
                    // at org.netbeans.lib.editor.util.swing.PriorityDocumentListenerList.removeUpdate(PriorityDocumentListenerList.java:63)
                    // at javax.swing.text.AbstractDocument.fireRemoveUpdate(AbstractDocument.java:242)
                    // at org.netbeans.editor.BaseDocument.fireRemoveUpdate(BaseDocument.java:1305)
                    // at org.netbeans.editor.BaseDocument.remove(BaseDocument.java:737)
                }
                outputDocument.insertString(0, srcText, null);
                destEditorCookie.saveDocument();
            } catch (BadLocationException e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
        } else {
            FileLock lock = destFO.lock();
            OutputStream outputStream = destFO.getOutputStream(lock);
            InputStream inputStream = srcFO.getInputStream();
            FileUtil.copy(inputStream, outputStream);
            outputStream.close();
            lock.releaseLock();
        }
    }
    
    public static void overwriteFile(FileObject srcFO, FileObject destFO) throws IOException {
        
        DataObject srcDO = DataObject.find(srcFO);
        Node srcNode = srcDO.getNodeDelegate();
        
        DataObject destDO = DataObject.find(destFO);
        Node destNode = destDO.getNodeDelegate();
        
        overwriteFile(srcNode, destNode);
    }
    
    public static void overwriteFile(FileObject srcFO, Node destNode) throws IOException {
        
        DataObject srcDO = DataObject.find(srcFO);
        Node srcNode = srcDO.getNodeDelegate();
        
        overwriteFile(srcNode, destNode);
    }
    
    public static void overwriteFile(Node srcNode, FileObject destFO) throws IOException {
        
        DataObject destDO = DataObject.find(destFO);
        Node destNode = destDO.getNodeDelegate();
        
        overwriteFile(srcNode, destNode);
    }    
    
    /**
     * Utility method to get the String contents of a FileObject
     */
    private static String getFileObjectContents(FileObject fileObject) throws IOException {
        InputStream inputStream = fileObject.getInputStream();
        return getInputStreamContents(inputStream);
    }
    
    /**
     * Utility method to load the contents of an InputStream as a String
     */
    private static String getInputStreamContents(InputStream inputStream) throws IOException {
        int chunksize = 512;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));  //?  // NOI18N
        StringBuffer output = new StringBuffer();
        char[] buff = new char[chunksize];
        int len = reader.read(buff);
        while (len > 0) {
            output.append(buff, 0, len);
            len = reader.read(buff);
        }
        return output.toString();
    }
}
