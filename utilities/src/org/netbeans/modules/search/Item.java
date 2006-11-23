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

package org.netbeans.modules.search;

import java.util.List;
import org.netbeans.modules.search.types.TextDetail;

/**
 *
 * @author  Marian Petras
 */
final class Item {
    
    /** */
    private final ResultModel resultModel;
    /** */
    final MatchingObject matchingObj;
    /** */
    final int detailIndex;
    
    /**
     */
    Item(ResultModel resultModel, MatchingObject matchingObj, int detailIndex) {
        this.resultModel = resultModel;
        this.matchingObj = matchingObj;
        this.detailIndex = detailIndex;
    }
    
    /**
     */
    TextDetail getLocation() {
        if (detailIndex == -1) {
            return null;
        }
        
        List<TextDetail> textDetails
            = resultModel.fullTextSearchType.getTextDetails(matchingObj.object);
        return detailIndex < textDetails.size()
               ? textDetails.get(detailIndex)
               : null;
    }

}
