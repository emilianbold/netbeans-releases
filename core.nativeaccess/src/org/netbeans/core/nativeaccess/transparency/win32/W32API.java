/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Original file is from http://jna.dev.java.net/
 */
package org.netbeans.core.nativeaccess.transparency.win32;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.FromNativeContext;
import com.sun.jna.IntegerType;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

/** Base type for most W32 API libraries.  Provides standard options
 * for unicode/ASCII mappings.  Set the system property <code>w32.ascii</code>
 * to <code>true</code> to default to the ASCII mappings.
 */
public interface W32API extends StdCallLibrary, W32Errors {
    
    /** Standard options to use the unicode version of a w32 API. */
    Map UNICODE_OPTIONS = new HashMap() {
        {
            put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
        }
    };
    /** Standard options to use the ASCII/MBCS version of a w32 API. */
    Map ASCII_OPTIONS = new HashMap() {
        {
            put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);
        }
    };
    Map DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;
    
    public static class HANDLE extends PointerType {
        /** Override to the appropriate object for INVALID_HANDLE_VALUE. */
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            if (INVALID_HANDLE_VALUE.equals(o))
                return INVALID_HANDLE_VALUE;
            return o;
        }
    }
    public static class HDC extends HANDLE { }
    public static class HICON extends HANDLE { }
    public static class HBITMAP extends HANDLE { }
    public static class HRGN extends HANDLE { }
    public static class HWND extends HANDLE { }
    public static class HINSTANCE extends HANDLE { }
    public static class HMODULE extends HINSTANCE { }

    /** Constant value representing an invalid HANDLE. */
    HANDLE INVALID_HANDLE_VALUE = new HANDLE() { 
        { super.setPointer(Pointer.createConstant(-1)); }
        public void setPointer(Pointer p) { 
            throw new UnsupportedOperationException("Immutable reference");
        }
    };
    /** Special HWND value. */
    HWND HWND_BROADCAST = new HWND() { 
        { super.setPointer(Pointer.createConstant(0xFFFF)); }
        public void setPointer(Pointer p) { 
            throw new UnsupportedOperationException("Immutable reference");
        }
    };
    /** LPHANDLE */
    public static class HANDLEByReference extends ByReference {
        public HANDLEByReference() {
            this(null);
        }
        public HANDLEByReference(HANDLE h) {
            super(Pointer.SIZE);
            setValue(h);
        }
        public void setValue(HANDLE h) {
            getPointer().setPointer(0, h != null ? h.getPointer() : null);
        }
        public HANDLE getValue() {
            Pointer p = getPointer().getPointer(0);
            if (p == null)
                return null;
            if (INVALID_HANDLE_VALUE.getPointer().equals(p)) 
                return INVALID_HANDLE_VALUE;
            HANDLE h = new HANDLE();
            h.setPointer(p);
            return h;
        }
    }
    
    public static class LONG_PTR extends IntegerType { 
        public LONG_PTR() { this(0); }
        public LONG_PTR(long value) { super(Pointer.SIZE, value); }
    }
    public static class SSIZE_T extends LONG_PTR {
        public SSIZE_T() { this(0); }
        public SSIZE_T(long value) { super(value); }
    }
    public static class ULONG_PTR extends IntegerType { 
        public ULONG_PTR() { this(0); }
        public ULONG_PTR(long value) { super(Pointer.SIZE, value); }
    }
    public static class SIZE_T extends ULONG_PTR {
        public SIZE_T() { this(0); }
        public SIZE_T(long value) { super(value); }
    }
    public static class LPARAM extends LONG_PTR { 
        public LPARAM() { this(0); }
        public LPARAM(long value) { super(value); }
    } 
    public static class LRESULT extends LONG_PTR { 
        public LRESULT() { this(0); }
        public LRESULT(long value) { super(value); }
    }
    public static class UINT_PTR extends IntegerType {
        public UINT_PTR() { super(Pointer.SIZE); }
        public UINT_PTR(long value) { super(Pointer.SIZE, value); }
        public Pointer toPointer() { return Pointer.createConstant(longValue()); }
    }
    public static class WPARAM extends UINT_PTR {
        public WPARAM() { this(0); }
        public WPARAM(long value) { super(value); }
    }
}
