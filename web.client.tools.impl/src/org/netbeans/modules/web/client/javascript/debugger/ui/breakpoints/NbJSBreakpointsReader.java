/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSUtil;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;

/** Stores {@linkNbJSBreakpoint}s. */
public final class NbJSBreakpointsReader implements Properties.Reader {

    private static final String PROPERTY_URL = "url"; // NOI18N
    private static final String PROPERTY_LINE_NUMBER = "lineNumber"; // NOI18N
    private static final String PROPERTY_CONDITION = "condition"; // NOI18N
    private static final String PROPERTY_HITCOUNTFILTER = "hitCountFilter"; // NOI18N
    private static final String PROPERTY_HITCOUNTFILTERSTYLE = "hitCountFilterStyle";

    public String[] getSupportedClassNames() {
        return new String[] { NbJSBreakpoint.class.getName(), };
    }

    public Object read(final String typeID, final Properties props) {
        NbJSBreakpoint breakpoint = null;
        if (typeID.equals(NbJSFileObjectBreakpoint.class.getName())) {
            Line line = getLine(props.getString(PROPERTY_URL, null), props
                    .getInt(PROPERTY_LINE_NUMBER, 1));
            if (line == null) {
                /* This may mean the file has been deleted */
                return null;
            }
            breakpoint = NbJSBreakpointManager.createBreakpoint(line);
        } else if (typeID.equals(NbJSURIBreakpoint.class.getName())) {
            breakpoint = NbJSBreakpointManager.createURIBreakpoint(props
                    .getString(PROPERTY_URL, null), props.getInt(
                    PROPERTY_LINE_NUMBER, 1));
        }

        if (breakpoint != null) {
            String condition = props.getString(PROPERTY_CONDITION, null);
            int hitCountFilter = props.getInt(PROPERTY_HITCOUNTFILTER, 0);
            int style =  props.getInt(PROPERTY_HITCOUNTFILTERSTYLE, -1);
            if (condition != null && condition.length() > 0) {
                breakpoint.setCondition(condition);
            }
            if (hitCountFilter > 0 ) {
                HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle = HIT_COUNT_FILTERING_STYLE.values()[style];
                breakpoint.setHitCountFilter(hitCountFilter,
                        hitCountFilteringStyle);
                
            }
        }
        return breakpoint;

    }


    public void write(final Object object, final Properties props) {
        assert object instanceof NbJSBreakpoint;
        NbJSBreakpoint bp = (NbJSBreakpoint) object;
        if (bp instanceof NbJSURIBreakpoint) {
            writeURIFileObject((NbJSURIBreakpoint) bp, props);
        } else {
            FileObject fo = bp.getFileObject();
            assert fo != null;

            String url = null;
            try {
                url = fo.getURL().toString();
            } catch (FileStateInvalidException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int lineNum = bp.getLineNumber() - 1;
            String condition = bp.getCondition();
            int hitCountFilter = bp.getHitCountFilter();
            HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle = bp
                    .getHitCountFilteringStyle();

            props.setString(PROPERTY_URL, url);
            props.setInt(PROPERTY_LINE_NUMBER, lineNum);
            

            props.setString(PROPERTY_CONDITION, condition);
            props.setInt(PROPERTY_HITCOUNTFILTER, hitCountFilter);
            if( hitCountFilteringStyle != null ) {
                props.setInt(PROPERTY_HITCOUNTFILTERSTYLE,
                    hitCountFilteringStyle.ordinal());
            }
        }
    }

    public void writeURIFileObject(final NbJSURIBreakpoint bp,
            final Properties props) {
        props.setString(PROPERTY_URL, bp.getLocation().getURI().toString());
        props.setInt(PROPERTY_LINE_NUMBER, bp.getLineNumber() - 1);
    }

    private static Line getLine(final String url, final int lineNumber) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            NbJSUtil.finest("Did not find FileObject. Malformed URL '" + url
                    + "'");
            return null;
        }
        if (fo == null) {
            return null;
        }
        LineCookie lineCookie = NbJSEditorUtil.getLineCookie(fo);
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
            NbJSUtil.LOGGER.log(Level.FINE, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            NbJSUtil.LOGGER.log(Level.FINE, e.getMessage(), e);
        }
        return null;
    }

}
