/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 *      jdeva <deva@neteans.org>, jwinblad <jwinblad@netbeans.org>
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

#include "stdafx.h"
#include "base64.h"

/** 
 * Converts a string from Unicode to UTF-8
 * str [in] unicode string to be converted
 * ppBytes [out] pointer to a freshly allocated char array on the heap
 *    storing the UTF-8 string
 * pBytesLen [out] size of output buffer
 * return value - whether the conversion was successful
 */
BOOL unicodeToUTF8(tstring str, char **ppBytes, int *pBytesLen) {
    char *data = NULL;
    int dataLen = WideCharToMultiByte(CP_UTF8, 0, str.c_str(), -1, data, 0, 0, 0);
    if(dataLen) {
        data = new char[dataLen];
        dataLen = WideCharToMultiByte(CP_UTF8, 0, str.c_str(), -1, data, dataLen, 0, 0);
        if(dataLen) {
            *ppBytes = data;
            *pBytesLen = dataLen;
            return TRUE;
        }
    }
    return FALSE;
}

/** 
 * Converts a string from UTF-8 to Unicode
 * str [in] UTF-8 string to be converted
 * ppChars [out] pointer to a freshly allocated char array on the heap
 *    storing the UTF-8 string
 * return value - whether the conversion was successful
 */
BOOL UTF8toUnicode(char *str, TCHAR **ppChars) {
    TCHAR *out = NULL;
    int len = MultiByteToWideChar(CP_UTF8, 0, str, -1, out, 0);
    if(len) {
        out = new TCHAR[len];
        len = MultiByteToWideChar(CP_UTF8, 0, str, -1, out, len);
        if(len) {
            *ppChars = out;
            return TRUE;
        }
    }
    return FALSE;
}

/** 
 * Encodes a tstring into a Base-64 encoded version of the string.
 * value [in] non-encoded string to be converted
 * return value - base64 encoded string
 */
tstring encodeToBase64(tstring value) {
    USES_CONVERSION;
    char *data = NULL;
    int dataLen;
    if(unicodeToUTF8(value, &data, &dataLen)) {
        int destLen = Base64EncodeGetRequiredLength(dataLen, ATL_BASE64_FLAG_NOCRLF);
        char *dest = new char[destLen+1];
        if(Base64Encode((BYTE *)data, dataLen, dest, &destLen, ATL_BASE64_FLAG_NOCRLF)) {
            dest[destLen] = 0;
            return tstring(A2T(dest));
        }
    }
    return NULL;
}