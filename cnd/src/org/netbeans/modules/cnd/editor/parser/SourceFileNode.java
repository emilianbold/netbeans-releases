/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package  org.netbeans.modules.cnd.editor.parser;

import org.openide.loaders.DataObject;
import org.netbeans.modules.cnd.loaders.CDataObject;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.FortranDataObject;
import org.netbeans.modules.cnd.loaders.HDataObject;

public class SourceFileNode extends ViewNode {

    public SourceFileNode(DataObject dao, String name, int lineno,
		char kind, String scope, int scopeCluster, int cluster) {
        super(name, dao, lineno, kind, scope, scopeCluster, cluster);
        if (dao instanceof CDataObject) {
            setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/CSrcIcon.gif"); // NOI18N
        }
        else if (dao instanceof CCDataObject) {
            setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); // NOI18N
        }
        else if (dao instanceof HDataObject) {
            setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/HDataIcon.gif"); // NOI18N
        }
        else if (dao instanceof FortranDataObject) {
            setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/FortranSrcIcon.gif"); // NOI18N
        }
        else {
            setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/CSrcIcon.gif"); // NOI18N 
	}
    }
}
