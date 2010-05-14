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

package org.netbeans.modules.encoder.ui.tester;

import com.sun.encoder.EncoderType;
import java.io.File;
import org.netbeans.modules.encoder.ui.tester.impl.EncoderTestPerformerImpl;

/**
 * The encoder test performer interface.
 *
 * @author Cannis Meng, Jun Xu
 */
public interface EncoderTestPerformer {

    /**
     * Performs test on an encoder represented by the XSD file.
     *
     * @param xsdFile the XSD schema model.
     * @param encoderType EncoderType object.
     */
    void performTest(File xsdFile, EncoderType encoderType);

    /**
     * Factory class.
     */
    public static class Factory {

        /**
         *
         * @return EncoderTestPerformer implementation instance.
         */
        public static EncoderTestPerformer getDefault() {
            return new EncoderTestPerformerImpl();
        }
    }
}
