/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.model.jclank.bridge.trace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 *
 * @author Vladimir Voskresensky
 */
public class PrintStreamDuplex extends PrintStream {
    private final PrintStream dup;

    public PrintStreamDuplex(OutputStream out, PrintStream dup) {
        super(out);
        this.dup = dup;
    }

    @Override
    public void flush() {
        super.flush();
        dup.flush();
    }

    @Override
    public void close() {
        super.close();
        dup.close();
    }

    @Override
    public boolean checkError() {
        return super.checkError() && dup.checkError();
    }

    @Override
    public void write(int b) {
        super.write(b);
        dup.write(b);
        dup.flush();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        dup.write(buf, off, len);
        dup.flush();
    }

    @Override
    public void print(boolean b) {
        super.print(b);
        dup.print(b);
        dup.flush();
    }

    @Override
    public void print(char c) {
        super.print(c);
        dup.print(c);
        dup.flush();
    }

    @Override
    public void print(int i) {
        super.print(i);
        dup.print(i);
        dup.flush();
    }

    @Override
    public void print(long l) {
        super.print(l);
        dup.print(l);
        dup.flush();
    }

    @Override
    public void print(float f) {
        super.print(f);
        dup.print(f);
        dup.flush();
    }

    @Override
    public void print(double d) {
        super.print(d);
        dup.print(d);
        dup.flush();
    }

    @Override
    public void print(char[] s) {
        super.print(s);
        dup.print(s);
        dup.flush();
    }

    @Override
    public void print(String s) {
        super.print(s);
        dup.print(s);
        dup.flush();
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
        dup.print(obj);
        dup.flush();
    }

    @Override
    public void println() {
        super.println();
        dup.println();
        dup.flush();
    }

    @Override
    public void println(boolean x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(char x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(int x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(long x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(float x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(double x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(char[] x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(String x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public void println(Object x) {
        super.println(x);
        dup.println(x);
        dup.flush();
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        dup.printf(format, args);
        dup.flush();
        return super.printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        dup.printf(l, format, args);
        dup.flush();
        return super.printf(l, format, args);
    }

    @Override
    public PrintStream format(String format, Object... args) {
        dup.format(format, args);
        dup.flush();
        return super.format(format, args);
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        dup.format(l, format, args);
        dup.flush();
        return super.format(l, format, args);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        dup.append(csq);
        dup.flush();
        return super.append(csq);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        dup.append(csq, start, end);
        dup.flush();
        return super.append(csq, start, end);
    }

    @Override
    public PrintStream append(char c) {
        dup.append(c);
        dup.flush();
        return super.append(c);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        dup.write(b);
        dup.flush();
    }
    
}
