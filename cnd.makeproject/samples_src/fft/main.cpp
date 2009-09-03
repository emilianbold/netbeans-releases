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

#include <stdlib.h>
#include <string>

#include "bmp.h"
#include "fft.h"

using namespace std;

int main(int argc, char** argv) {

    printf("It's NetBeans C++ sample.\n");
    printf("It's transforms 24 bit BMPs with FFT.\n");

    string inputBitmapFileName;
    string outputBitmapFileName;

    if (argc == 1) {
        printf("No input params.\n");
        printf("Usage: fft input.bmp output.bmp\n");
        printf("Generating test image \"in.bmp\".\n");
        Bitmap bmp;
        bmp.GenerateTestImage();
        bmp.Save("in.bmp");
        inputBitmapFileName = "in.bmp";
        outputBitmapFileName = "out.bmp";

    } else if (argc == 3) {
        inputBitmapFileName = argv[1];
        outputBitmapFileName = argv[2];
    } else {
        printf("Bad params!\n");
        printf("Usage: fft input.bmp output.bmp\n");
        return EXIT_FAILURE;
    }

    printf("Loading bitmap.\n");
    Bitmap bmp(inputBitmapFileName);
    bmp.PrintInfo();
    if (bmp.GetBitCount() != 24) {
        printf("It's not 24 bit BMP!\n");
        return EXIT_FAILURE;
    }

    FastFourierTransform fft;
    printf("FFT:\n");
    fft.Transform(bmp);

    printf("Saving bitmap.\n");
    bmp.Save(outputBitmapFileName);

    return EXIT_SUCCESS;
}
