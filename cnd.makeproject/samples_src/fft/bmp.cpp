/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "bmp.h"

Bitmap::Bitmap() {
}

Bitmap::Bitmap(string name) {
    Load(name);
}

Bitmap::Bitmap(const Bitmap& orig) {
}

Bitmap::~Bitmap() {
    delete[] bitmapInfo;
    delete[] bitmap;
}

void Bitmap::Load(string filename) {
    FILE* file = fopen(filename.c_str(), (char *) &"a+b");
    // obtaining file size
    fseek(file, 0, SEEK_END);
    int fileSize = ftell(file);
    rewind(file);
    // reading header
    fread(&fileHeader, 1, sizeof (fileHeader), file);
    // initializing data structures
    if (fileHeader.bfType != 0x4d42) {
        printf("It's not BMP!\n");
    } else {
        bitmapSize = fileSize - sizeof (fileHeader);
        bitmap = new unsigned char[bitmapSize];
        // reading data
        fread(bitmap, 1, bitmapSize, file);
        bitmapInfo = (BITMAPINFO*) bitmap;
        bitmapHeader = (BITMAPINFOHEADER*) bitmap;
        int usedColorNumber = (int) bitmapHeader->biClrUsed;
        int colotTableSize = usedColorNumber * sizeof (RGBQUAD);
        data = bitmap + bitmapHeader->biSize + colotTableSize;
    }
    fclose(file);
}

void Bitmap::Save(string filename) {
    FILE* file = fopen(filename.c_str(), (char *) &"a+b");
    // writing header
    fwrite(&fileHeader, 1, sizeof (fileHeader), file);
    // writing data
    fwrite(bitmap, 1, bitmapSize, file);
    fclose(file);
}

void Bitmap::PrintInfo() {
    printf("BMP Image:\n");
    printf("    Width: %d\n", GetWidth());
    printf("    Height: %d\n", GetHeight());
    printf("    Bits per color: %d\n", GetBitCount());
}

int Bitmap::GetBitCount() {
    return bitmapInfo->bmiHeader.biBitCount;
}

int Bitmap::GetWidth() {
    return bitmapInfo->bmiHeader.biWidth;
}

int Bitmap::GetHeight() {
    return bitmapInfo->bmiHeader.biHeight;
}

unsigned char* Bitmap::GetData() {
    return data;
}

int Bitmap::GetImageSize() {
    return bitmapInfo->bmiHeader.biSizeImage;
}

void Bitmap::GenerateTestImage() {

    // setting file header
    fileHeader.bfType = 0x4d42;
    fileHeader.bfOffBits = 54;
    fileHeader.bfReserved1 = 0;
    fileHeader.bfReserved2 = 0;
    fileHeader.bfSize = 12582966;

    // allocating memory
    bitmapSize = 12582952;
    bitmap = new unsigned char[bitmapSize];
    bitmapInfo = (BITMAPINFO*) bitmap;
    bitmapHeader = (BITMAPINFOHEADER*) bitmap;

    // setting bitmap header
    bitmapHeader->biBitCount = 24;
    bitmapHeader->biClrImportant = 0;
    bitmapHeader->biClrUsed = 0;
    bitmapHeader->biCompression = 0;
    bitmapHeader->biHeight = 2048;
    bitmapHeader->biPlanes = 1;
    bitmapHeader->biSize = 40;
    bitmapHeader->biSizeImage = 12582912;
    bitmapHeader->biWidth = 2048;
    bitmapHeader->biXPelsPerMeter = 2835;
    bitmapHeader->biYPelsPerMeter = 2835;

    // initializing data
    int usedColorNumber = (int) bitmapHeader->biClrUsed;
    int colotTableSize = usedColorNumber * sizeof (RGBQUAD);
    data = bitmap + bitmapHeader->biSize + colotTableSize;

    unsigned char* d = data;

    for (int i = 0; i < GetWidth(); i++) {
        for (int j = 0; j < GetHeight(); j++) {
            d[0] = i%255;
            d[1] = j%255;
            d[2] = (i+j)%255;

            d += 3;
        }
    }
}
