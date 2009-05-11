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

package org.netbeans.modules.masterfs.filebasedfs;

import java.net.URISyntaxException;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObj;
import org.openide.util.Exceptions;

//TODO: JDK problems with URL, URI, File conversion for UNC
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
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
        if (type == URLMapper.NETWORK) return null;        
        URL retVal = null;
        try {
            if (fo instanceof BaseFileObj)  {
                final BaseFileObj bfo = (BaseFileObj) fo;
                retVal = FileBasedURLMapper.fileToURL(bfo.getFileName().getFile(), fo);
            } else if (fo instanceof RootObj) {
                final RootObj rfo = (RootObj) fo;
                return getURL(rfo.getRealRoot(), type);                
            }
        } catch (MalformedURLException e) {
            retVal = null;
        }
        return retVal;
    }

    public final FileObject[] getFileObjects(final URL url) {
        if (!"file".equals(url.getProtocol())) return null;  //NOI18N
        // return null for UNC root
        if(url.getPath().equals("//") || url.getPath().equals("////")) {  //NOI18N
            return null;
        }
        //TODO: review and simplify         
        FileObject retVal = null;
        File file;
        try {
            file = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (IllegalArgumentException iax) {
            // catch possible IAE from File constructor and re-throw with URL
            Exceptions.printStackTrace(Exceptions.attachMessage(iax, "URL="+url));  //NOI18N
            return null;
        }
        
        retVal = FileBasedFileSystem.getFileObject(file, FileObjectFactory.Caller.ToFileObject);
        return new FileObject[]{retVal};
    }

    private static URL fileToURL(final File file, final FileObject fo) throws MalformedURLException {
        URL retVal = file.toURI().toURL();
        if (fo.isFolder()) {
            // #155742 - URL for folder must always end with slash
            final String urlDef = retVal.toExternalForm();
            final String pathSeparator = "/";//NOI18N
            if (!urlDef.endsWith(pathSeparator)) {
                retVal = new URL(urlDef + pathSeparator);
            }
        }
        return retVal;
    }
}
