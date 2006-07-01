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

package org.netbeans.core.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.*;
import org.xml.sax.*;



/**
 * Will be loaded by different classloader by FactoriesTest.
 * @author Jaroslav Tulach
 */
public class FactoriesRunnableHid extends HashMap implements Runnable {
    public void run () {
        try {
            put ("dom", javax.xml.parsers.DocumentBuilderFactory.newInstance());
            put ("sax", javax.xml.parsers.SAXParserFactory.newInstance ());
        } catch (Exception ex) {
            IllegalStateException e = new IllegalStateException (ex.getMessage ());
            e.initCause (ex);
            throw e;
        }
    }
}
