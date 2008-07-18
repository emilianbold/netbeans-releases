/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public class PhpMimeResolver extends MIMEResolver {

    private static final int BYTES_FOR_PRECHECK = 255;
    private static final int BYTES_FOR_CHECK_IF_HTML_SIGNED = 4000;
    private static final Sign[] EMPTY_SIGNS = new Sign[]{};
    private static final String MIME_TYPE = "text/x-php5";//NOI18N
    private static final String UNKNOWN_MIME_TYPE = null;
    private static final String[] PHP_WELL_KNOWN_EXTENSION_PREFIXES = {"php", "phtml"};//NOI18N
    private static final String[] OTHER_WELL_KNOWN_EXTENSION_PREFIXES = {"java"};//NOI18N

    //looking for
    private static final byte[] OPEN_TAG = "<?php".getBytes();//NOI18N
    private static final byte[] SHORT_OPEN_TAG = "<?".getBytes();//NOI18N

    //html signs
    private int openTagIdx;
    private int shortOpenTagIdx;
    private Set<String> resolvedExt = new HashSet<String>();
    private Sign[] signs = null;

    @Override
    public String findMIMEType(FileObject fo) {
        String ext = fo.getExt();
        if (isWellKnownPhpExtension(ext)) {//shouldn't happen because this mime should go last
            return MIME_TYPE;
        } else if (isWellKnownOtherExtension(ext)) {
            return UNKNOWN_MIME_TYPE;
        } else if (resolvedExt.contains(ext)) {
            return MIME_TYPE;
        }
        openTagIdx = 0;
        shortOpenTagIdx = 0;
        try {
            InputStream inputStream = fo.getInputStream();
            try {
                byte[] bytes = new byte[BYTES_FOR_PRECHECK];
                int len = inputStream.read(bytes);
                signs = new Sign[]{
                            new Sign("<!DOCTYPE HTML"),
                            new Sign("<!DOCTYPE HTML".toLowerCase()),
                            new Sign("<HTML>"),
                            new Sign("<HTML>".toLowerCase())
                        };
                if (len > 0 && resolve(bytes, len)) {
                    return returnMimeType(fo, MIME_TYPE);
                } else if (isSigned()) {
                    //just once 4000 bytes
                    //while (len > 0) {
                    bytes = new byte[BYTES_FOR_CHECK_IF_HTML_SIGNED];
                    len = inputStream.read(bytes);
                    signs = EMPTY_SIGNS;
                    if (len > 0 && resolve(bytes, len)) {
                        return returnMimeType(fo, MIME_TYPE);
                    }
                //}
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return returnMimeType(fo, UNKNOWN_MIME_TYPE);
    }

    private boolean resolve(byte[] bytes, int len) {
        for (int i = 0; i < len; i++) {
            byte b = bytes[i];
            if (isOpenTag(b) || isShortOpenTag(b)) {
                return true;
            }
            for (int j = 0; j < signs.length; j++) {
                Sign s = signs[j];
                s.check(b);
            }
        }
        return false;
    }

    private boolean isSigned() {
        for (int j = 0; j < signs.length; j++) {
            Sign s = signs[j];
            if (s.isSigned()) {
                return true;
            }
        }
        return false;
    }
    
    public String returnMimeType(final FileObject fo, final String mimeType) {
        String ext = fo.getExt();
        if (ext != null && ext.trim().length() > 0) {
            if (MIME_TYPE.equals(mimeType)) {
                resolvedExt.add(ext);
            }
        }
        return mimeType;
    }

    private static boolean isWellKnownPhpExtension(String ext) {
        return isWellKnownExtension(ext, PHP_WELL_KNOWN_EXTENSION_PREFIXES);
    }

    private static boolean isWellKnownOtherExtension(String ext) {
        return isWellKnownExtension(ext, OTHER_WELL_KNOWN_EXTENSION_PREFIXES);
    }

    private static boolean isWellKnownExtension(String ext, String[] extensionList) {
        if (ext != null && ext.trim().length() > 0) {
            for (String phpPrefix : extensionList) {
                if (ext.startsWith(phpPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOpenTag(byte b) {
        if (openTagIdx < OPEN_TAG.length) {
            if (b == OPEN_TAG[openTagIdx]) {
                openTagIdx++;
            } else {
                openTagIdx = 0;
            }
        }
        return (openTagIdx >= OPEN_TAG.length);
    }

    private boolean isShortOpenTag(byte b) {
        if (shortOpenTagIdx < SHORT_OPEN_TAG.length &&
                b == SHORT_OPEN_TAG[shortOpenTagIdx]) {
            shortOpenTagIdx++;
        } else if (shortOpenTagIdx >= SHORT_OPEN_TAG.length && Character.isWhitespace((char) b)) {
            shortOpenTagIdx++;
        } else {
            shortOpenTagIdx = 0;
        }
        return (shortOpenTagIdx >= SHORT_OPEN_TAG.length + 1);
    }

    private static class Sign {

        private final byte[] signBytes;
        private int signBytesIdx;

        Sign(String sign) {
            signBytes = sign.getBytes();
        }

        void check(byte b) {
            if (signBytesIdx < signBytes.length) {
                if (b == signBytes[signBytesIdx]) {
                    signBytesIdx++;
                } else {
                    signBytesIdx = 0;
                }
            }
        }

        boolean isSigned() {
            return (signBytesIdx >= signBytes.length);
        }
    }
}
