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
