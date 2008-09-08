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
package org.netbeans.modules.cnd.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * expensive resolver (read any file) not based neither on extension, name nor magic hex number
 *
 * @author Vladimir Voskresensky
 */
public class CndSniffyMIMEResolver extends MIMEResolver {

    public CndSniffyMIMEResolver() {
        super(MIMENames.CPLUSPLUS_MIME_TYPE, MIMENames.SHELL_MIME_TYPE);
    }

    /**
     * Resolves FileObject and returns recognized MIME type
     * @param fo is FileObject which should be resolved
     * @return  recognized MIME type or null if not recognized
     */
    public String findMIMEType(FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }
        if (fo.getExt().length() > 0) {
            return null;
        }
        
        String line = getFirstLine(fo);
        // Recognize c++ file without extension
        if (detectCPPByLine(line)) {
            return MIMENames.CPLUSPLUS_MIME_TYPE;
        }
        // detect special sun headers
        if (detectShellByLine(line)) {
            return MIMENames.SHELL_MIME_TYPE;
        }
        return null;
    }

    private String getFirstLine(FileObject fo) {
        String line = ""; // NOI18N
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            if (fo.canRead()) {
                isr = new InputStreamReader(fo.getInputStream());
                br = new BufferedReader(isr);
                try {
                    line = br.readLine();
                } catch (IOException ex) {
                    line = ""; // NOI18N
                }
            }
        } catch (IOException ex) {
//            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
//                    ex.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ex) {
//                    ex.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     *  This is a special detector which samples suffix-less header files looking for the
     *  string "-*- C++ -*-".
     *  or 
     *  #include directive in the first line
     *  Note: Not all Sun Studio headerless includes contain this comment.
     */
    private boolean detectCPPByLine(String line) {
        if (line != null) {
            if (line.startsWith("//") && line.indexOf("-*- C++ -*-") > 0) { // NOI18N
                return true;
            } else {
                line = line.replaceAll("\\s", ""); // NOI18N
                if (line.startsWith("#include")) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private boolean detectShellByLine(String line) {
        if (line != null) {
            line = line.replaceAll("\\s", ""); // NOI18N
            if (line.startsWith("#!/bin/bash") ||  // NOI18N
                    line.startsWith("#!/bin/sh") ||  // NOI18N
                    line.startsWith("#!/bin/ksh") ||  // NOI18N
                    line.startsWith("#!/bin/csh") ||  // NOI18N
                    line.startsWith("#!/bin/csh") ||  // NOI18N
                    line.startsWith("#!/usr/bin/perl")) {  // NOI18N
                return true;
            }
        }
        return false;
    }
}