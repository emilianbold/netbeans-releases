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


package org.netbeans.modules.iep.model.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Description of the Class
 *
 * @author Bing Lu
 *
 * @since November 6, 2002
 */
public class StreamReader implements Runnable {

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(StreamReader.class.getName());

    private BufferedReader mReader;
    private IOException mException;
    private PrintWriter mWriter;

    /**
     * Constructor for the StreamReader object
     *
     * @param input Description of the Parameter
     * @param writer Description of the Parameter
     */
    public StreamReader(InputStream input, PrintWriter writer) {
        mReader = new BufferedReader(new InputStreamReader(input));
        mWriter = writer;
    }

    /**
     * Gets the exception attribute of the StreamReader object
     *
     * @return The exception value
     */
    public IOException getException() {
        return mException;
    }

    /**
     * Main processing method for the StreamReader object
     */
    public void run() {

        try {
            String line = null;

            while ((line = mReader.readLine()) != null) {
                mWriter.println(line);
            }

            mWriter.flush();
        } catch (IOException e) {
            mException = e;
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
