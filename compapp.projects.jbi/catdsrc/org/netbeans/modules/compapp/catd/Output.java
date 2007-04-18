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

/*
 * Output.java
 *
 * Created on March 25, 2005, 2:22 PM
 */

package org.netbeans.modules.compapp.catd;

import org.netbeans.modules.compapp.catd.util.Util;
import java.io.*;

/**
 *
 * @author blu
 */
public class Output {
    private String mName;
    private File mActual;
    private File mExpected;

    /** Creates a new instance of Output */
    public Output(String name, File actual, File expected) {
        mName = name;
        mActual = actual;
        mExpected = expected;
    }

    public String getName() {
        return mName;
    }

    public String getExpected() {
        String ret = Util.getFileContent(mExpected);
        return ret;
    }

    public String getExpectedWithoutCRNL() {
        String ret = Util.getFileContentWithoutCRNL(mExpected);
        return ret;
    }

    public String getActual() {
        String ret = Util.getFileContent(mActual);
        return ret;
    }

    public String getActualWithoutCRNL() {
        String ret = Util.getFileContentWithoutCRNL(mActual);
        return ret;
    }

    public void removeActual() {
        if (mActual != null && mActual.exists()) {
            mActual.delete();
        }
    }

}
