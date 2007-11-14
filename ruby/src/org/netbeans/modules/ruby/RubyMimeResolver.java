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
    private static final int HEADER_LENGTH = 40;
    
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
    
    public static boolean isRubyHeader(byte[] header) {
        int max = header.length;
        
        if ((max < 2) || (header[0] != '#') || (header[1] != '!')) {
            return false;
        }
        
        // See if we have either
        // #!/usr/bin/ruby
        // or some variation of that, e.g.
        // #! C:\programs\ruby.exe
        // or the env variety
        // #!/usr/bin/env ruby
        // or some variety of that
        
        // Skip spaces
        int index = 2;
        
        while ((index < max) && (header[index] == ' ')) {
            index++;
        }
        
        // Look for the end of the path
        while ((index < max) && (header[index] != '\n') && (header[index] != ' ')) {
            index++;
        }
        
        index--;
        
        // Back up and see what the last word was
        while ((index >= 2) && (header[index] != '/') && (header[index] != '\\') &&
                (header[index] != ' ')) {
            index--;
        }
        
        index++;
        
        // See if it's "ruby", "jruby", or "env" ?
        if ((((index + 3) < max) && (header[index] == 'r') && (header[index + 1] == 'u') &&
                (header[index + 2] == 'b') && (header[index + 3] == 'y')) ||
                (((index + 4) < max) && (header[index] == 'j') && (header[index + 1] == 'r') &&
                (header[index + 2] == 'u') && (header[index + 3] == 'b') &&
                (header[index + 4] == 'y'))) {
            // It's ruby or jruby
            // See if the suffix is okay
            if (header[index] == 'j') {
                index += 5;
            } else {
                index += 4;
            }
            
            if ((index >= max) || (header[index] == '\n') || (header[index] == ' ')) {
                return true;
            }
            
            if ((header[index] == '.') && ((index + 3) < max) && (header[index + 1] == 'e') &&
                    (header[index + 2] == 'x') && (header[index + 3] == 'e')) {
                return true;
            }
            
            return false;
        } else if (((index + 2) < max) && (header[index] == 'e') && (header[index + 1] == 'n') &&
                (header[index + 2] == 'v')) {
            index += 3;
            
            // It's env
            if ((header[index] == ' ') ||
                    ((header[index] == '.') && ((index + 4) < max) && (header[index + 1] == 'e') &&
                    (header[index + 2] == 'x') && (header[index + 3] == 'e') &&
                    (header[index + 4] == ' '))) {
                // Find the next space and look for ruby or jruby
                if (header[index] == '.') {
                    index += 4;
                }
                
                while ((index < max) && (header[index] == ' ')) {
                    index++;
                }
                
                // Make sure we have "ruby" or "jruby" (or ruby.exe)?
                if ((((index + 3) < max) && (header[index] == 'r') && (header[index + 1] == 'u') &&
                        (header[index + 2] == 'b') && (header[index + 3] == 'y')) ||
                        (((index + 4) < max) && (header[index] == 'j') &&
                        (header[index + 1] == 'r') && (header[index + 2] == 'u') &&
                        (header[index + 3] == 'b') && (header[index + 4] == 'y'))) {
                    // Ensure that nothing FOLLOWS ruby or jruby
                    if (header[index] == 'j') {
                        index += 5;
                    } else {
                        index += 4;
                    }
                    
                    if ((index == max) || (header[index] == '\n') || (header[index] == ' ') ||
                            ((header[index] == '.') && ((index + 3) < max) &&
                            (header[index + 1] == 'e') && (header[index + 2] == 'x') &&
                            (header[index + 3] == 'e'))) {
                        return true;
                    }
                }
            }
        }
        
        return false;
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
}
