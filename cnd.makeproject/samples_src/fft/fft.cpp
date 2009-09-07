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

#include <math.h>

#include "fft.h"
#include "bmp.h"

FastFourierTransform::FastFourierTransform() {
}

FastFourierTransform::~FastFourierTransform() {
    delete[] x2x;
    delete[] y2x;
    delete[] transformationCurve;
    delete[] x;
    delete[] y;
}

void FastFourierTransform::Transform(Bitmap& bmp) {

    int bmpMaxLineSize = (bmp.GetHeight() > bmp.GetWidth()) ? bmp.GetHeight() : bmp.GetWidth();
    int transformationSizeLog2 = (int) log2(bmpMaxLineSize);
    int transformationSize = (int) (pow(2.0, transformationSizeLog2));

    x2x = new double [transformationSize * transformationSize];
    y2x = new double [transformationSize * transformationSize];
    transformationCurve = new double [transformationSize];
    x = new double [transformationSize];
    y = new double [transformationSize];

    double *a, *b;
    unsigned char* data;

    InitTransformationCurve(transformationSizeLog2);

    int addBits = (int) ((bmp.GetImageSize())-((bmp.GetHeight())*(bmp.GetHeight())*3)) / (bmp.GetHeight());
    int shift = bmp.GetImageSize() - (bmp.GetWidth() * 3 + addBits)*(transformationSize) +  3;

    for (int Color = 0; Color <= 2; Color++) {
        printf("    Transforming ");
        printf( (Color == 0 ? "red " : ((Color == 1) ? "green " : "blue ")));
        printf("layer.\n");

        a = x2x;
        b = y2x;
        data = bmp.GetData() + shift;
        for (int i = 0; i < transformationSize; i++) {
            for (int j = 0; j < transformationSize; j++) {
                *b = 0;
                *a = *(data + Color);
                a++;
                b++;
                data += 3;
            }
            data = data - transformationSize * 3 + bmp.GetWidth() * 3 + addBits;
        }
        FFT2D(transformationSizeLog2);
        a = x2x;
        b = y2x;
        data = bmp.GetData() + shift;
        for (int i = 0; i < transformationSize; i++) {
            for (int j = 0; j < transformationSize; j++) {
                *(data + Color) = (unsigned char)(sqrt(pow(*a, 2) + pow(*b, 2)));
                if (sqrt(pow(*a, 2) + pow(*b, 2)) > 255)
                    *(data + Color) = 255;
                a++;
                b++;
                data += 3;
            }
            data = data - transformationSize * 3 + bmp.GetWidth() * 3 + addBits;
        }
    }
}

void FastFourierTransform::FFT(Direction direction, int transformationSizeLog2) {
    int d = (direction == DIRECT) ? -1 : 1;
    int i1, j1, n, e, o, f;
    double r, u, t, v, q, p, c, s, z, w, a;
    n = (int) (pow(2.0, transformationSizeLog2));
    for (int l = 1; l <= transformationSizeLog2; l++) {
        e = (int) (pow(2.0, transformationSizeLog2 + 1 - l));
        f = e / 2;
        u = 1;
        v = 0;
        z = 3.14 / f;
        c = cos(z);
        s = d * sin(z);
        for (int j = 1; j <= f; j++) {
            for (int i = j - 1; i < n; i = i + e) {
                o = i + f;
                p = x[i] + x[o];
                q = y[i] + y[o];
                r = x[i] - x[o];
                t = y[i] - y[o];
                x[o] = r * u - t*v;
                y[o] = t * u + r*v;
                x[i] = p;
                y[i] = q;
            }
            w = u * c - v*s;
            v = v * c + u*s;
            u = w;
        }
    }
    int j = 1;
    for (int i = 1; i <= n - 1; i++) {
        if (i < j) {
            j1 = j - 1;
            i1 = i - 1;
            p = x[j1];
            q = y[j1];
            x[j1] = x[i1];
            y[j1] = y[i1];
            x[i1] = p;
            y[i1] = q;
        }
        int k = n / 2;
        while (k < j) {
            j = j - k;
            k = k / 2;
        }
        j = j + k;
    }
    if (d != -1) {
        for (int k = 0; k <= n - 1; k++) {
            x[k] = x[k] / n;
            y[k] = y[k] / n;
        }
    } else {
        for (int k = 0; k <= n - 1; k++) {
            a = sqrt(x[k] * x[k] + y[k] * y[k]);
            q = 0;
            if (a != 0) {
                q = acos(x[k] / a);
                if (y[k] < 0)
                    q = -q;
            }
        }
    }
}

void FastFourierTransform::FFT2D(int transformationSizeLog2) {

    int r = (int) (pow(2.0, transformationSizeLog2));
    int i, j;
    double *a, *b, *f, s;

    // direct FFT
    // columns
    for (i = 0; i < r; i++) {
        a = x2x;
        a += i;
        for (j = 0; j < r; j++) {
            x[j] = *a;
            y[j] = 0;
            a += r;
        }
        FFT(DIRECT, transformationSizeLog2);
        s = x[0];
        f = transformationCurve;
        for (j = 0; j < r; j++) {
            x[j] += x[j]*((*f - 50) / 50);
            y[j] += y[j]*((*f - 50) / 50);
            f++;
        }
        x[0] = transformationCurveBase;
        a = x2x;
        b = y2x;
        a += i;
        b += i;
        for (j = 0; j < r; j++) {
            *a = x[j];
            *b = y[j];
            b += r;
            a += r;
        }
    }
    // rows
    a = x2x;
    b = y2x;
    for (i = 0; i < r; i++) {
        for (j = 0; j < r; j++) {
            x[j] = *a;
            y[j] = *b;
            a++;
            b++;
        }
        FFT(DIRECT, transformationSizeLog2);
        s = x[0];
        f = transformationCurve;
        for (j = 0; j < r; j++) {
            x[j] += x[j]*((*f - 50) / 50);
            y[j] += y[j]*((*f - 50) / 50);
            f++;
        }
        x[0] = transformationCurveBase;
        a = a - r;
        b = b - r;
        for (j = 0; j < r; j++) {
            *a = x[j];
            *b = y[j];
            b++;
            a++;
        }

    }

    // reverse FFT
    // rows
    a = x2x;
    b = y2x;
    for (i = 0; i < r; i++) {
        for (j = 0; j < r; j++) {
            x[j] = *a;
            y[j] = *b;
            a++;
            b++;
        }
        FFT(REVERSE, transformationSizeLog2);
        a = a - r;
        b = b - r;
        for (j = 0; j < r; j++) {
            *a = x[j];
            *b = y[j];
            b++;
            a++;
        }
    }
    //	columns
    for (i = 0; i < r; i++) {
        a = x2x;
        b = y2x;
        b += i;
        a += i;
        for (j = 0; j < r; j++) {
            x[j] = *a;
            y[j] = *b;
            a += r;
            b += r;
        }
        FFT(REVERSE, transformationSizeLog2);
        a = x2x;
        b = y2x;
        b += i;
        a += i;
        for (j = 0; j < r; j++) {
            *a = x[j];
            *b = y[j];
            b += r;
            a += r;
        }
    }
}

void FastFourierTransform::InitTransformationCurve(int m) {
    transformationCurveBase = 0;
    int n = (int) (pow(2.0, m));
    for (int i = 0; i <= 5; i++) {
        transformationCurve[i] = 0;
    }
    for (int i = 5; i <= n/2; i++) {
        transformationCurve[i] = 100;
    }
    for (int i = 0; i <= (int) n / 2; i++)
        transformationCurve[n - i - 1] = transformationCurve[i];
}

