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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.MIMEResolver.class, position=999)
public class PhpMimeResolver extends MIMEResolver {

    private static final int BYTES_FOR_PRECHECK = 255;
    private static final int BYTES_FOR_CHECK_IF_HTML_SIGNED = 4000;
    private static final Sign[] EMPTY_SIGNS = new Sign[]{};
    private static final String MIME_TYPE = "text/x-php5";//NOI18N
    private static final String UNKNOWN_MIME_TYPE = null;
    private static final String[] PHP_WELL_KNOWN_EXTENSION_PREFIXES = {"php", "phtml"};//NOI18N
    private static final String[] OTHER_WELL_KNOWN_EXTENSION_PREFIXES = {"java", "rb", "rhtml"};//NOI18N

    private static final String[] WELL_KNOWN_WINDOWS_TROUBLE_FILES = {
        "ntuser.dat",
        "ntuser.dat.log"};//NOI18N


    //looking for
    private static final byte[] OPEN_TAG = "<?php".getBytes();//NOI18N
    private static final byte[] SHORT_OPEN_TAG = "<?".getBytes();//NOI18N

    //html signs
    private static final Set<String> resolvedExt = new HashSet<String>();

    public PhpMimeResolver() {
        super(MIME_TYPE);
    }

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
        MutableInteger openTagIdx = new MutableInteger();
        MutableInteger shortOpenTagIdx = new MutableInteger();
        if (!fo.canRead()) {//NOI18N
            return UNKNOWN_MIME_TYPE;
        }

        if (Utilities.isWindows() && existsInArray(fo.getNameExt().toLowerCase(), 
                WELL_KNOWN_WINDOWS_TROUBLE_FILES)) {
            return UNKNOWN_MIME_TYPE;
        }
        try {
            InputStream inputStream = fo.getInputStream();
            try {
                byte[] bytes = new byte[BYTES_FOR_PRECHECK];
                int len = inputStream.read(bytes);
                Sign[] signs = new Sign[]{
                            new Sign("<!DOCTYPE HTML"),
                            new Sign("<!DOCTYPE HTML".toLowerCase()),
                            new Sign("<HTML>"),
                            new Sign("<HTML>".toLowerCase())
                        };
                if (len > 0 && resolve(bytes, len,openTagIdx, shortOpenTagIdx, signs)) {
                    return returnMimeType(fo, MIME_TYPE);
                } else if (isSigned(signs)) {
                    //just once 4000 bytes
                    //while (len > 0) {
                    bytes = new byte[BYTES_FOR_CHECK_IF_HTML_SIGNED];
                    len = inputStream.read(bytes);
                    signs = EMPTY_SIGNS;
                    if (len > 0 && resolve(bytes, len,openTagIdx, shortOpenTagIdx, signs)) {
                        return returnMimeType(fo, MIME_TYPE);
                    }
                //}
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            // #143815 - just log exception and continue
            Logger.getLogger(PhpMimeResolver.class.getName()).log(Level.INFO, null, ex);
            return null;
        }
        return returnMimeType(fo, UNKNOWN_MIME_TYPE);
    }

    private boolean resolve(byte[] bytes, int len, MutableInteger openTagIdx,
            MutableInteger shortOpenTagIdx, Sign[] signs) {
        for (int i = 0; i < len; i++) {
            byte b = bytes[i];
            //short open tags not tested for now - code not deleted yet 
            //until the decision whether to check or not will settle down
            if (isOpenTag(b, openTagIdx) /*|| isShortOpenTag(b,shortOpenTagIdx)*/) {
                return true;
            }
            for (int j = 0; j < signs.length; j++) {
                Sign s = signs[j];
                s.check(b);
            }
        }
        return false;
    }

    private boolean isSigned(Sign[] signs) {
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
        return existsInArray(ext, PHP_WELL_KNOWN_EXTENSION_PREFIXES);
    }

    private static boolean isWellKnownOtherExtension(String ext) {
        return existsInArray(ext, OTHER_WELL_KNOWN_EXTENSION_PREFIXES);
    }

    private static boolean existsInArray(String elem, String[] array) {
        if (elem != null && elem.trim().length() > 0) {
            for (String phpPrefix : array) {
                if (elem.startsWith(phpPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOpenTag(byte b, MutableInteger openTagIdx) {
        if (openTagIdx.getValue() < OPEN_TAG.length) {
            if (b == OPEN_TAG[openTagIdx.getValue()]) {
                openTagIdx.incr();
            } else {
                openTagIdx.setValue(0);
            }
        }
        return (openTagIdx.getValue() >= OPEN_TAG.length);
    }

    private boolean isShortOpenTag(byte b, MutableInteger shortOpenTagIdx) {
        if (shortOpenTagIdx.getValue() < SHORT_OPEN_TAG.length &&
                b == SHORT_OPEN_TAG[shortOpenTagIdx.getValue()]) {
            shortOpenTagIdx.incr();
        } else if (shortOpenTagIdx.getValue() >= SHORT_OPEN_TAG.length && Character.isWhitespace((char) b)) {
            shortOpenTagIdx.incr();
        } else {
            shortOpenTagIdx.setValue(0);
        }
        return (shortOpenTagIdx.getValue() >= SHORT_OPEN_TAG.length + 1);
    }

    private static class MutableInteger {
        private int value = 0;
        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(int value) {
            this.value = value;
        }

        public void incr() {
            this.value += 1;
        }
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
