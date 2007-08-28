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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.swingapp.templates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.properties.UtilConvert;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

class AppProjectGenerator {

//    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project/3"; // NOI18N

    private AppProjectGenerator() {}

    static FileObject createProjectFromTemplate(FileObject zippedTemplate,
                                                File projectDir,
                                                String[] toReplace,
                                                String[] replaceWith)
        throws IOException
    {
        FileObject projectFolderFO;
        Stack<String> nameStack = new Stack<String>();
        while ((projectFolderFO = FileUtil.toFileObject(projectDir)) == null) {
            nameStack.push(projectDir.getName());
            projectDir = projectDir.getParentFile();            
        }
        while (!nameStack.empty()) {
            projectFolderFO = projectFolderFO.createFolder(nameStack.pop());
        }

        unzip(zippedTemplate, projectFolderFO, toReplace, replaceWith);
        // [revisit private properties]
//                    //update private properties
//                    File privateProperties = createPrivateProperties (prjLoc);
//                    //No need to load the properties the file is empty
//                    Properties p = new Properties ();                    
//                    p.put ("javadoc.preview","true");   //NOI18N
//                    FileOutputStream out = new FileOutputStream (privateProperties);
//                    try {
//                        p.store(out,null);                    
//                    } finally {
//                        out.close ();
//                    }
        projectFolderFO.refresh(false);
        return projectFolderFO;
    }

    private static void unzip(FileObject zippedTemplate, FileObject targetFolder,
                              String[] toReplace, String[] replaceWith)
        throws IOException
    {
        ZipInputStream zip = new ZipInputStream(zippedTemplate.getInputStream());
        ReplacingOutputStream replacer = new ReplacingOutputStream(toReplace, replaceWith);

        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String fileName = entry.getName(); // full relative path to file
                // replace strings in the file name
                ByteArrayOutputStream baos = new ByteArrayOutputStream(fileName.length());
                replacer.setOutput(baos);
                replacer.write(fileName.getBytes());
                replacer.close();
                fileName = baos.toString("UTF-8"); // NOI18N
//                String[] pathElements = fileName.split("/"); // NOI18N
//                boolean anySubst = false;
//
//                for (int i=0; i < pathElements.length; i++) {
//                    String pathEl = pathElements[i];
//                    String extension = ""; // NOI18N
//                    if (!entry.isDirectory() && i+1 == pathElements.length) {
//                        int dot = pathEl.lastIndexOf('.'); // NOI18N
//                        if (dot >= 0) {
//                            extension = pathEl.substring(dot);
//                            pathEl = pathEl.substring(0, dot);
//                        }
//                    }
//
//                    for (int j=0; j < toReplace.length; j++) {
//                        if (pathEl.equals(toReplace[j])) {
//                            pathEl = entry.isDirectory() ?
//                                     replaceWith[j].replace('.', '/') : replaceWith[j]; // NOI18N
//                            pathElements[i] = pathEl + extension;
//                            anySubst = true;
//                            break;
//                        }
//                    }
//                }
//
//                if (anySubst) {
//                    StringBuilder buf = new StringBuilder(fileName.length());
//                    for (int i=0; i < pathElements.length; i++) {
//                        buf.append(pathElements[i]);
//                        if (i+1 < pathElements.length)
//                            buf.append("/"); // NOI18N
//                    }
//                    fileName = buf.toString();
//                }

                if (entry.isDirectory()) {
                    FileUtil.createFolder(targetFolder, fileName);
                }
                else {
                    FileObject destFile = FileUtil.createData(targetFolder, fileName);
                    FileLock lock = destFile.lock();
                    try {
                        OutputStream output = destFile.getOutputStream(lock);
                        String encoding = null;
                        boolean propertiesEncoding = false;
                        String ext = destFile.getExt().toLowerCase();
                        if (ext.endsWith("java")) { // NOI18N
                            encoding = FileEncodingQuery.getDefaultEncoding().name();
                        } else if (ext.endsWith("form") || ext.endsWith("xml")) { // NOI18N
                            encoding = "UTF-8"; // NOI18N
                        } else if (ext.endsWith("properties")) { // NOI18N
                            encoding = "ISO-8859-1"; // NOI18N
                            propertiesEncoding = true;
                        } else if (fileName.startsWith("src/META-INF/")) { // NOI18N
                            encoding = FileEncodingQuery.getDefaultEncoding().name();
                        }
                        if (encoding != null || propertiesEncoding) {
                            replacer.setOutput(output, encoding, propertiesEncoding);
                            output = replacer;
                        }
                        try {
                            FileUtil.copy(zip, output);
                        }
                        finally {
                            output.close();
                        }
                    }
                    finally {
                        lock.releaseLock();
                    }
                }
            }
        }
        finally {
            zip.close();
        }
    }

    static FileObject getGeneratedFile(FileObject projectFolder, String templFileName,
                                       String[] templateNames, String[] substNames)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(templFileName.length());
        ReplacingOutputStream replacer = new ReplacingOutputStream(templateNames, substNames);
        replacer.setOutput(baos);
        try {
            replacer.write(templFileName.getBytes());
            replacer.close();
            templFileName = baos.toString("UTF-8"); // NOI18N
        }
        catch (IOException ex) {} // should not happen
        return projectFolder.getFileObject(templFileName);
    }

//    private static File createPrivateProperties (FileObject fo) throws IOException {
//        String[] nameElements = AntProjectHelper.PRIVATE_PROPERTIES_PATH.split("/");
//        for (int i=0; i<nameElements.length-1; i++) {
//            FileObject tmp = fo.getFileObject (nameElements[i]);
//            if (tmp == null) {
//                tmp = fo.createFolder(nameElements[i]);
//            }
//            fo = tmp;
//        }
//        fo = fo.createData(nameElements[nameElements.length-1]);
//        return FileUtil.toFile(fo);
//    }

    /**
     * Replaces given strings in the content written to the underlying stream.
     * Characteristics/limitations:
     * 1) None of the strings to replace can be a subset of another.
     *    E.g. if "Application" and "ApplicationFrame" are provided to
     *    replace, "ApplicationFrame" will never be replaced.
     *    Note for intersection in definition: if schema "AB"->"XY" and
     *    "BC"->"YZ" is used on "ABC", the result will be "XYC", not "XYZ".
     *    It is just coincidence that the substitute values also intersect, but
     *    this situation is not checked.
     * 2) Default conversion between String and bytes is used.
     */
    private static class ReplacingOutputStream extends OutputStream {
        private OutputStream output;

        private String[] toReplace;
        private String[] replaceWith;
        private int[] matchCounts;

        private StringBuilder pendingChars;

        private boolean forcePathNames;

        private String encoding;
        private boolean propertiesEncoding;

        ReplacingOutputStream(String[] toReplace, String[] replaceWith) {
            this.toReplace = toReplace;
            this.replaceWith = replaceWith;
            this.pendingChars = new StringBuilder(50);
        }

        void setOutput(OutputStream output) {
            this.output = output;
            this.forcePathNames = true;
            this.encoding = "UTF-8"; // NOI18N
            this.propertiesEncoding = false;
            matchCounts = new int[toReplace.length];
        }

        void setOutput(OutputStream output, String encoding, boolean propertiesEncoding) {
            this.output = output;
            this.forcePathNames = false;
            this.encoding = encoding;
            this.propertiesEncoding = propertiesEncoding;
            matchCounts = new int[toReplace.length];
        }

        public void write(int b) throws IOException {
            int completeMatch = -1;
            boolean charMatch = false;
            for (int i=0; i < toReplace.length; i++) {
               int count = matchCounts[i];
               String template = toReplace[i];
               if (template.charAt(count) == b) {
                   if (count+1 == template.length()) {
                       completeMatch = i;
                       break;
                   }
                   matchCounts[i] = count+1;
                   charMatch = true;
               }
               else matchCounts[i] = 0;
            }

            if (completeMatch >= 0) {
                int preCount = pendingChars.length() - matchCounts[completeMatch];
                if (preCount > 0) {
                    output.write(pendingChars.substring(0, preCount).getBytes());
                }
                String subst = replaceWith[completeMatch];
                if (forcePathNames) // convert possible package name to directory path
                    subst = subst.replace('.', '/'); // NOI18N
                output.write(encode(subst));
                for (int i=0; i < matchCounts.length; i++) {
                    matchCounts[i] = 0;
                }
                pendingChars.delete(0, pendingChars.length());
            }
            else if (charMatch) {
                pendingChars.append((char)b);
            }
            else {
                writePendingBytes();
                output.write(b);
            }
        }

        private byte[] encode(String str) {
            if (propertiesEncoding) {
                str = UtilConvert.saveConvert(str);
            }
            if (encoding != null) {
                try {
                    return str.getBytes(encoding);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(AppProjectGenerator.class.getName()).log(Level.INFO, "", ex); // NOI18N
                }
            }
            return str.getBytes();
        }

        private void writePendingBytes() throws IOException {
            if (pendingChars.length() > 0) {
                output.write(pendingChars.toString().getBytes());
                pendingChars.delete(0, pendingChars.length());
            }
        }

        public void flush() throws IOException {
            writePendingBytes();
            output.flush();
        }

        public void close() throws IOException {
            writePendingBytes();
            output.close();
        }
    }
}
