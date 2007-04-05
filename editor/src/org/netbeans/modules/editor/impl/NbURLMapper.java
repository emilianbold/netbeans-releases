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

package org.netbeans.modules.editor.impl;

import java.awt.Container;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.lib.URLMapper;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 *
 * @author Vita Stejskal
 */
public final class NbURLMapper extends URLMapper {

    private static final Logger LOG = Logger.getLogger(NbURLMapper.class.getName());
    
    public NbURLMapper() {
    }

    protected JTextComponent getTextComponent(URL url) {
        FileObject f = org.openide.filesystems.URLMapper.findFileObject(url);
        
        if (f != null) {
            DataObject d = null;
            
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, "Can't get DataObject for " + f, e); //NOI18N
            }
            
            if (d != null) {
                EditorCookie cookie = d.getLookup().lookup(EditorCookie.class);
                if (cookie != null) {
                    JEditorPane [] allJeps = cookie.getOpenedPanes();
                    if (allJeps != null) {
                        return allJeps[0];
                    }
                }
            }
        }
        
        return null;
    }

    protected URL getUrl(JTextComponent comp) {
        FileObject f = null;
        
        if (comp instanceof Lookup.Provider) {
            f = ((Lookup.Provider) comp).getLookup().lookup(FileObject.class);
        }
        
        if (f == null) {
            Container container = comp.getParent();
            while (container != null) {
                if (container instanceof Lookup.Provider) {
                    f = ((Lookup.Provider) container).getLookup().lookup(FileObject.class);
                    if (f != null) {
                        break;
                    }
                }
                
                container = container.getParent();
            }
        }
        
        if (f != null) {
            try {
                return f.getURL();
            } catch (FileStateInvalidException e) {
                LOG.log(Level.WARNING, "Can't get URL for " + f, e); //NOI18N
            }
        }
        
        return null;
    }

}
