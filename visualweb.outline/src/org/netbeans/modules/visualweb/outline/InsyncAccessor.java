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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.outline;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;


/**
 * XXX Utility class accessing insync. The refs to insync are only here from outline module,
 * so later the potential API from insync could be easier to specify.
 *
 * @author Peter Zavadsky
 */
final class InsyncAccessor {

    private InsyncAccessor() {
    }


    /** XXX Hacky method which tries to find out whether the design bean is valid or not
     * (after the infamous ressurection). */
    public static boolean isValidDesignBean(DesignBean designBean) {
        if (designBean == null) {
            return false;
        }
        DesignContext designContext = designBean.getDesignContext();
        if (designContext == null) {
            return false;
        }
        DesignBean rootContainer = designContext.getRootContainer();
        if (rootContainer == null) {
            return false;
        }

        DesignBean topParent = designBean;
        while (topParent != null) {
            DesignBean bean = topParent.getBeanParent();
            if (bean == null) {
                break;
            }
            topParent = bean;
        }
        return topParent == rootContainer;
    }

    public static DesignBean getNewCorrespondingDesignBean(DesignBean designBean) {
        if (designBean == null) {
            return null;
        }
        DesignContext designContext = designBean.getDesignContext();
        if (designContext == null) {
            return designBean;
        }
        return ((LiveUnit)designContext).getBeanEquivalentTo(designBean);
    }
}
