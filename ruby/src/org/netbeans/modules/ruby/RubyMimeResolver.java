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
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.io.InputStream;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;


/**
 * Recognize Ruby file types
 * 
 * @todo I saw a reference to .rbw in the pickaxe book - are these relevant?
 *
 * @author Tor Norbye
 */
public class RubyMimeResolver extends MIMEResolver {
    /**
     * MIME type for Ruby. Don't change this without also consulting the various XML files
     * that cannot reference this value directly.
     */
    public static final String RUBY_MIME_TYPE = "text/x-ruby"; // application/x-ruby is also used a fair bit.
    
    /** Number of bytes to sniff from the file headers */
    static final int HEADER_LENGTH = 40;
    
    public RubyMimeResolver() {
    }
    
    public String findMIMEType(FileObject fo) {
        String ext = fo.getExt();
        
        if (ext.equalsIgnoreCase("rb") || ext.equalsIgnoreCase("mab") || // NOI18N
                ext.equalsIgnoreCase("gemspec") || ext.equalsIgnoreCase("rake") || // NOI18N
                ext.equalsIgnoreCase("builder") || ext.equalsIgnoreCase("rxml") || // NOI18N
                ext.equalsIgnoreCase("rjs")) { // NOI18N
            
            return RUBY_MIME_TYPE;
        }
        
        //        // TODO - is this just a Rails thing? Maybe register in the rails support module
        //        if (ext.equalsIgnoreCase("conf")) {
        //            return RUBY_MIME_TYPE;
        //        }
        String name = fo.getName();
        
        if ("Rakefile".equals(name) || "rakefile".equals(name)) {
            return RUBY_MIME_TYPE;
        }
        
        // Read the file header and look for #!/usr/bin/ruby (or similar) markers
        // but only for files without extensions or with the extension .cgi
        // TODO: Check to see if parent is "script" or "bin" and if so, perform this check
        if ((ext.length() == 0) || ext.equals("cgi")) {
            byte[] header = readHeader(fo);
            
            if (header != null) {
                if (isRubyHeader(header)) {
                    return RUBY_MIME_TYPE;
                }
            }
        }
        
        return null;
    }
    
    private byte[] readHeader(FileObject fo) {
        // See if it looks like a Ruby file based on the shebang line
        byte[] header = new byte[HEADER_LENGTH];
        
        InputStream in = null;
        
        try {
            in = fo.getInputStream();
            
            for (int i = 0; i < HEADER_LENGTH;) {
                try {
                    int read = in.read(header, i, HEADER_LENGTH - i);
                    
                    if (read < 0) {
                        return null; // unexpected end
                    }
                    
                    i += read;
                } catch (IOException ex) {
                    return null; // unexpected end
                }
            }
        } catch (IOException openex) {
            return null; // unexpected end
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                // already closed
            }
        }
        
        return header;
    }    

    public static boolean isRubyHeader(byte[] header) {
        int max = header.length;
        
        if ((max < 2) || (header[0] != '#') || (header[1] != '!')) {
            return false;
        }
        
        // See if the first line contains the word "ruby" (but not as a path component)
        find:
        for (int index = 0; index < max - 3; index++) {
            byte b = header[index];
            if (b == '\n') {
                break;
            }
            if (b == 'r' && (header[index + 1] == 'u') &&
                    (header[index + 2] == 'b') && (header[index + 3] == 'y')) {
                // No slash/backslash before the end of the line or next word
                for (int j = index+4; j < max; j++) {
                    byte c = header[j];
                    if (c == ' ' || c == '\n') {
                        break;
                    } else if (c == '/' || c == '\\') {
                        continue find;
                    }
                }
                return true;
            }
        }
        
        return false;
    }
}
