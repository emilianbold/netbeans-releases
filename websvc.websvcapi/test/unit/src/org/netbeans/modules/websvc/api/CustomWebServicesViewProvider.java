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

package org.netbeans.modules.websvc.api;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.websvc.api.webservices.WebServicesView;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewFactory;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesViewProvider implements WebServicesViewProvider {
    
    private Map<FileObject, WebServicesView> cache = new HashMap<FileObject, WebServicesView>();
    
    /** Creates a new instance of CustomJAXWSViewProvider */
    public CustomWebServicesViewProvider() {
    }
    
    public WebServicesView findWebServicesView(FileObject file) {
        if (file.getExt().equals("ws")) {
            WebServicesView em  =  cache.get(file.getParent());
            if (em == null) {
                em = WebServicesViewFactory.createWebServicesView(new CustomWebServicesViewImpl(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesViewImpl implements WebServicesViewImpl {
        
        private FileObject fo;
        
        CustomWebServicesViewImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public Node createWebServicesView(FileObject srcRoot) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
