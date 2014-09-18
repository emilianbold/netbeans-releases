/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.io.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import org.openide.util.Lookup;

/**
 * An I/O connection to one tab on the Output Window. To acquire an instance to
 * write to, call, e.g.,
 * <code>BaseIOProvider.getDefault().getIO("someName", false)</code>. To get
 * actual streams to write to, call <code>getOut()</code> or <code>
 * getErr()</code> on the returned instance.
 * <p>
 * Generally it is preferable not to hold a reference to an instance of
 * {@link org.openide.io.base.BaseInputOutput}, but rather to fetch it by name
 * from {@link org.openide.io.base.BaseIOProvider} as needed.
 * </p>
 *
 * @author Ian Formanek, Jaroslav Tulach, Petr Hamernik, Ales Novak, Jan
 * Jancura, Jaroslav Havlin
 */
public interface BaseInputOutput extends Lookup.Provider {

    /**
     * Acquire an output writer to write to the tab. This is the usual use of a
     * tab--it writes to the main output pane.
     *
     * @return the writer
     */
    public PrintWriter getOut();

    /**
     * Get a reader to read from the tab. If a reader is ever requested, an
     * input line is added to the tab and used to read one line at a time.
     *
     * @return the reader
     */
    public Reader getIn();

    /**
     * Get an output writer to write to the tab in error mode. This might show
     * up in a different color than the regular output, e.g., or appear in a
     * separate pane.
     *
     * @return the writer
     */
    public PrintWriter getErr();

    /**
     * Closes this tab. The effect of calling any method on an instance of
     * {@link BaseInputOutput} after calling {@link #closeInputOutput()} on it
     * is undefined.
     */
    public void closeInputOutput();

    /**
     * Test whether this tab has been closed, either by a call to
     * <code>closeInputOutput()</code> or by the user closing the tab in the UI.
     *
     * @see #closeInputOutput
     * @return <code>true</code> if it is closed
     */
    public boolean isClosed();

    /**
     * Clean the output area.
     * @throws java.io.IOException
     */
    void reset() throws IOException;
}
