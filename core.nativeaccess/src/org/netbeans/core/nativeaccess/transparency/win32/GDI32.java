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

import java.awt.Rectangle;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

/** Definition (incomplete) of <code>gdi32.dll</code>. */
public interface GDI32 extends W32API {
    
    GDI32 INSTANCE = (GDI32)
        Native.loadLibrary("gdi32", GDI32.class, DEFAULT_OPTIONS);

    public static class RECT extends Structure {
        public int left;
        public int top;
        public int right;
        public int bottom;
        public Rectangle toRectangle() {
            return new Rectangle(left, top, right-left, bottom-top);
        }
        public String toString() {
            return "[(" + left + "," + top + ")(" + right + "," + bottom + ")]";
        }
    }

    int RDH_RECTANGLES = 1;
    public static class RGNDATAHEADER extends Structure {
        public int dwSize = size();
        public int iType = RDH_RECTANGLES; // required
        public int nCount;
        public int nRgnSize;
        public RECT rcBound; 
    }
    public static class RGNDATA extends Structure {
        public RGNDATAHEADER rdh;
        public byte[] Buffer;
        public RGNDATA(int bufferSize) {
            Buffer = new byte[bufferSize];
            allocateMemory();
        }
    }
    
    public HRGN ExtCreateRegion(Pointer lpXform, int nCount, RGNDATA lpRgnData);

    int RGN_AND = 1;
    int RGN_OR = 2;
    int RGN_XOR = 3;
    int RGN_DIFF = 4;
    int RGN_COPY = 5;
    
    int ERROR = 0;
    int NULLREGION = 1;
    int SIMPLEREGION = 2;
    int COMPLEXREGION = 3;
    int CombineRgn(HRGN hrgnDest, HRGN hrgnSrc1, HRGN hrgnSrc2, int fnCombineMode);
    
    HRGN CreateRectRgn(int nLeftRect, int nTopRect,
                       int nRightRect, int nBottomRect);
    
    HRGN CreateRoundRectRgn(int nLeftRect, int nTopRect,
                            int nRightRect, int nBottomRect,
                            int nWidthEllipse, 
                            int nHeightEllipse);
    
    boolean SetRectRgn(HRGN hrgn, int nLeftRect, int nTopRect, int nRightRect, int nBottomRect);
    
    int SetPixel(HDC hDC, int x, int y, int crColor);
    
    HDC CreateCompatibleDC(HDC hDC);
    boolean DeleteDC(HDC hDC);
    
    int BI_RGB = 0;
    int BI_RLE8 = 1;
    int BI_RLE4 = 2;
    int BI_BITFIELDS = 3;
    int BI_JPEG = 4;
    int BI_PNG = 5;
    public static class BITMAPINFOHEADER extends Structure {
        public int biSize = size();
        public int biWidth;
        public int biHeight;
        public short biPlanes;
        public short biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;
    }
    public static class RGBQUAD extends Structure {
        public byte rgbBlue;
        public byte rgbGreen;
        public byte rgbRed;
        public byte rgbReserved = 0;
    }
    public static class BITMAPINFO extends Structure {
        public BITMAPINFOHEADER bmiHeader = new BITMAPINFOHEADER();
        //RGBQUAD:
        //byte rgbBlue;
        //byte rgbGreen;
        //byte rgbRed;
        //byte rgbReserved = 0;
        int[] bmiColors = new int[1];
        public BITMAPINFO() { this(1); }
        public BITMAPINFO(int size) {
            bmiColors = new int[size];
            allocateMemory();
        }
    }
    int DIB_RGB_COLORS = 0;
    int DIB_PAL_COLORS = 1;
    HBITMAP CreateDIBitmap(HDC hDC, BITMAPINFOHEADER lpbmih, int fdwInit,
                           Pointer lpbInit, BITMAPINFO lpbmi, int fuUsage);
    HBITMAP CreateDIBSection(HDC hDC, BITMAPINFO pbmi, int iUsage,
                             PointerByReference ppvBits, Pointer hSection,
                             int dwOffset);
    HBITMAP CreateCompatibleBitmap(HDC hDC, int width, int height);
    
    HANDLE SelectObject(HDC hDC, HANDLE hGDIObj);
    boolean DeleteObject(HANDLE p);
}
