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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
/**
 *
 * @author Owner
 */
public class RestPaletteUtils {

    public static String getLocalizedString(String bundleName, String key, String nonLocalized) {
        if(bundleName != null && key != null) {
            try {
                ResourceBundle bundle = NbBundle.getBundle(bundleName);
                if(bundle != null && bundle.getString(key) != null)
                    return bundle.getString(key);
            } catch(Exception ex) {
                //ignore
            } catch(Throwable th) {
                //ignore
            }
        }
        return nonLocalized;
    }
    
    /**
     * Allow action to be enabled only if REST framework is selected by user
     * for this project
     */
    public static boolean ready(Node[] activatedNodes) {
        if(activatedNodes == null || activatedNodes.length == 0)
            return false;
        Node n = activatedNodes[0];
        DataObject d = n.getCookie(DataObject.class);
        if (d != null) {
            Project p = FileOwnerQuery.getOwner(d.getPrimaryFile());
            if(p != null) {
                RestSupport support = p.getLookup().lookup(RestSupport.class);
                if(support != null)
                    return support.isReady();
            }
        }
        return false;
    }    
}
