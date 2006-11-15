/*
 * JspMimeResolver.java
 *
 * Created on September 6, 2006, 10:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.core.jsploader;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author Petr Pisl
 */
public class JspMimeResolver extends MIMEResolver {
    
    /** Creates a new instance of JspMimeResolver */
    public JspMimeResolver() {
    }

    public String findMIMEType(FileObject fo) {
        String value = null;
        if ("xhtml".equals(fo.getExt())){
            value = "text/x-jsp";
        }
        return value;
    }
    
}
