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

package org.netbeans.modules.cnd.apt.support;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.impl.structure.*;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * builds APT from TokenStream and APTLight from APT
 * @author Vladimir Voskresensky
 */
public class APTBuilder {
    /** Creates a new instance of APTBuilder */
    private APTBuilder() {
    }

    public static APTFile buildAPT(String path, TokenStream ts) {
        return new APTBuilderImpl().buildAPT(path, ts);
    }
    
    public static APT buildAPTLight(APT apt) {
        return APTBuilderImpl.buildAPTLight(apt);
    }
}
