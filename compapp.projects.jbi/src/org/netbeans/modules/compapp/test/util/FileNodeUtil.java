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


package org.netbeans.modules.compapp.test.util;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

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
        
        DataObject srcDO = srcNode.getLookup().lookup(DataObject.class);
        FileObject srcFO = srcDO.getPrimaryFile();
        
        DataObject destDO = destNode.getLookup().lookup(DataObject.class);
        FileObject destFO = destDO.getPrimaryFile();
        
        
        // To avoid any confusion, save modified source first.
        if (srcDO.isModified()) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(FileNodeUtil.class, "MSG_SaveModifiedSource", srcFO.getNameExt()), // NOI18N
                    NbBundle.getMessage(FileNodeUtil.class, "TTL_SaveModifiedSource"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                EditorCookie srcEditorCookie = srcDO.getCookie(EditorCookie.class);
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
            
            EditorCookie destEditorCookie = destDO.getCookie(EditorCookie.class);
            
            InputStream inputStream = null;
            try {
                inputStream = srcFO.getInputStream();
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
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    ;
                }
            }
        } else {
            FileLock lock = destFO.lock();
            InputStream inputStream = null;
            OutputStream outputStream = null;
            
            try {
                outputStream = destFO.getOutputStream(lock);
                inputStream = srcFO.getInputStream();
                FileUtil.copy(inputStream, outputStream);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    
                    lock.releaseLock();
                } catch (Exception e) {
                    ;
                }
            }
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
        try {
            return getInputStreamContents(inputStream);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                ;
            }
        }
    }
    
    /**
     * Utility method to load the contents of an InputStream as a String
     */
    private static String getInputStreamContents(InputStream inputStream) throws IOException {
        int chunksize = 512;
        BufferedReader reader = 
                new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"));  //?  // NOI18N
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
