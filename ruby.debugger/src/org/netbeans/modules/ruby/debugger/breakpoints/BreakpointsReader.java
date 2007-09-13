/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
