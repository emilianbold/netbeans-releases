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

package org.netbeans.modules.masterfs.filebasedfs;

import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.openide.util.Exceptions;

//TODO: JDK problems with URL, URI, File conversion for UNC
/*
There must be consistently called conversion from FileUtil and URLMapper.
new File (URI.create (fo.getURL ().toExternalForm ())) is typical scenario that leads to this
bug: java.lang.IllegalArgumentException: URI has an authority component
        at java.io.File.<init>(File.java:326)


Maybe there would be also possible to return a little special URL from FileBasedURLMapper that
would get special subclass of URLStreamHandler in constructor. This subclass of URLStreamHandler
would provided external form (method toExternalForm) that would be suitable for above mentioned 
conversion from URL to File. 
        
Known problems :
1/     at java.io.File.<init>(File.java:326)
     at org.netbeans.modules.javacore.parser.ASTProvider.getClassPath(ASTProvider.java:477)
     at org.netbeans.lib.gjast.ASParser$BridgeContext.getClassPath(ASParser.java:421)

2/
       at java.io.File.<init>(File.java:326)
       at org.netbeans.modules.javacore.scanning.FileScanner.<init>(FileScanner.java:85)
catch] at org.netbeans.modules.javacore.JMManager.scanFiles(JMManager.java:1112)

3/ org.netbeans.modules.javacore.parser.ECRequestDescImpl.getFileName(FileObject fo,StringBuffer buf)
    at java.io.File.<init>(File.java:326)
     
    
    
    
*/

public final class FileBasedURLMapper extends URLMapper {
    public final URL getURL(final FileObject fo, final int type) {
        URL retVal = null;
        try {
            if (fo instanceof BaseFileObj) {
                final BaseFileObj bfo = (BaseFileObj) fo;
                retVal = FileBasedURLMapper.fileToURL(bfo.getFileName().getFile(), fo);
            }
        } catch (MalformedURLException e) {
            retVal = null;
        }
        return retVal;
    }

    public final FileObject[] getFileObjects(final URL url) {
        if (!"file".equals(url.getProtocol())) return null;  //NOI18N
        //TODO: review and simplify         
        FileObject retVal = null;
        File file;
        try {
            final String host = url.getHost();
            final String f = url.getFile();
            //TODO: UNC workaround     
            //TODO: string concatenation
            if (host != null && host.trim().length() != 0) {
                file = new File("////" + host + f);//NOI18N    
            } else {
                if (f.startsWith("//")) {
                    file = new File(f);
                } else {
                    file = FileUtil.normalizeFile(new File(URI.create(url.toExternalForm())));
                }
            }
        } catch (IllegalArgumentException e) {
            file = new File(url.getFile());
            if (!file.exists()) {
                final StringBuffer sb = new StringBuffer();
                sb.append(e.getLocalizedMessage()).append(" [").append(url.toExternalForm()).append(']');//NOI18N
                Exceptions.printStackTrace(new IllegalArgumentException(sb.toString()));
                return null;
            }
        }

        final FileBasedFileSystem instance = FileBasedFileSystem.getInstance(file);

        if (instance != null) {
            retVal = instance.findFileObject(file);
        }

        return new FileObject[]{retVal};
    }

    private static URL fileToURL(final File file, final FileObject fo) throws MalformedURLException {
        URL retVal = null;
        if (fo.isFolder() && (!fo.isValid() || fo.isVirtual())) {
            final String urlDef = file.toURI().toURL().toExternalForm();
            final String pathSeparator = "/";//NOI18N
            if (!urlDef.endsWith(pathSeparator)) {
                //TODO: string concatenation
                retVal = new URL(urlDef + pathSeparator);
            }
        }
        retVal = (retVal == null) ? file.toURI().toURL() : retVal;
        return retVal;
    }


}
