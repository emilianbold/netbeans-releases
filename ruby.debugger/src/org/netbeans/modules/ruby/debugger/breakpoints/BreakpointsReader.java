/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ruby.debugger.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.ruby.debugger.EditorUtil;
import org.netbeans.modules.ruby.debugger.Util;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/** Stores {@link RubyBreakpoint}s. */
public final class BreakpointsReader implements Properties.Reader {
    
    private static final String PROPERTY_URL = "url"; // NOI18N
    private static final String PROPERTY_LINE_NUMBER = "lineNumber"; // NOI18N
    
    public String [] getSupportedClassNames() {
        return new String[] {
            RubyBreakpoint.class.getName(),
        };
    }
    
    public Object read(final String typeID, final Properties props) {
        if (!(typeID.equals(RubyBreakpoint.class.getName()))) {
            return null;
        }
        Line line = getLine(props.getString(PROPERTY_URL, null), props.getInt(PROPERTY_LINE_NUMBER, 1));
        if (line == null) {
            return null;
        }
        return RubyBreakpointManager.createBreakpoint(line);
    }
    
    public void write(final Object object, final Properties props) {
        RubyBreakpoint bp = (RubyBreakpoint) object;
        FileObject fo = bp.getFileObject();
        try {
            props.setString(PROPERTY_URL, fo.getURL().toString());
            props.setInt(PROPERTY_LINE_NUMBER, bp.getLine().getLineNumber());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static Line getLine(final String url, final int lineNumber) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            Util.finest("Did not find FileObject. Malformed URL '" + url + "'");
            return null;
        }
        if (fo == null) {
            return null;
        }
        LineCookie lineCookie = EditorUtil.getLineCookie(fo);
        if (lineCookie == null) {
            return null;
        }
        Line.Set ls = lineCookie.getLineSet();
        if (ls == null) {
            return null;
        }
        try {
            return ls.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException e) {
            Util.LOGGER.log(Level.FINE, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Util.LOGGER.log(Level.FINE, e.getMessage(), e);
        }
        return null;
    }
    
}
