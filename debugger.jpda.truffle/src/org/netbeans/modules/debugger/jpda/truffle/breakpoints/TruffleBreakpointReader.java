/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.truffle.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(types={Properties.Reader.class})
public class TruffleBreakpointReader implements Properties.Reader {

    private static final Logger LOG = Logger.getLogger(TruffleBreakpointReader.class.getName());
    
    @Override
    public String[] getSupportedClassNames() {
        return new String[] {
            TruffleLineBreakpoint.class.getName (), 
        };
    }

    @Override
    public Object read(String className, Properties properties) {
        TruffleLineBreakpoint b = null;
        if (className.equals(TruffleLineBreakpoint.class.getName())) {
            String urlStr = properties.getString (TruffleLineBreakpoint.PROP_URL, null);
            int lineNumber = properties.getInt (TruffleLineBreakpoint.PROP_LINE_NUMBER, 1);
            try {
                URL url = new URL(urlStr);
                FileObject fo = URLMapper.findFileObject(url);
                if (fo == null) {
                    if (isTransientURL(url)) {
                        EditorLineHandler line = EditorLineHandlerFactory.getHandler(url, lineNumber);
                        b = new TruffleLineBreakpoint(line);
                    } else {
                        return null;
                    }
                } else {
                    EditorLineHandler line = EditorLineHandlerFactory.getHandler(fo, lineNumber);
                    if (line != null) {
                        b = new TruffleLineBreakpoint(line);
                    } else {
                        return null;
                    }
                }
            } catch (MalformedURLException ex) {
                LOG.log(Level.CONFIG, "urlStr = "+urlStr, ex);
                return null;
            }
            
        }
        if (b == null) {
            throw new IllegalStateException("Unknown breakpoint type: \""+className+"\"");
        }
        b.setCondition(properties.getString(TruffleLineBreakpoint.PROP_CONDITION, null));
        /*b.setPrintText (
            properties.getString (JSBreakpoint.PROP_PRINT_TEXT, "")
        );*/
        b.setGroupName(
            properties.getString (Breakpoint.PROP_GROUP_NAME, "")
        );
        int hitCountFilter = properties.getInt(Breakpoint.PROP_HIT_COUNT_FILTER, 0);
        Breakpoint.HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle;
        if (hitCountFilter > 0) {
            hitCountFilteringStyle = Breakpoint.HIT_COUNT_FILTERING_STYLE.values()
                    [properties.getInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", 0)]; // NOI18N
        } else {
            hitCountFilteringStyle = null;
        }
        b.setHitCountFilter(hitCountFilter, hitCountFilteringStyle);
        if (properties.getBoolean (Breakpoint.PROP_ENABLED, true))
            b.enable ();
        else
            b.disable ();
        if (b.canHaveDependentBreakpoints()) {
            // TODO
        }
        return b;
    }
    
    private boolean isTransientURL(URL url) {
        return Source.URL_PROTOCOL.equals(url.getProtocol());
    }

    @Override
    public void write(Object object, Properties properties) {
        TruffleLineBreakpoint b = (TruffleLineBreakpoint) object;
        properties.setString (
            Breakpoint.PROP_GROUP_NAME, 
            b.getGroupName ()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER, b.getHitCountFilter());
        Breakpoint.HIT_COUNT_FILTERING_STYLE style = b.getHitCountFilteringStyle();
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", style != null ? style.ordinal() : 0); // NOI18N
        if (b.canHaveDependentBreakpoints()) {
            // TODO
        }
        
        properties.setString(TruffleLineBreakpoint.PROP_CONDITION, b.getCondition());
        URL url = b.getURL();
        int line = b.getLineNumber();
        properties.setString(TruffleLineBreakpoint.PROP_URL, url.toExternalForm());
        properties.setInt(TruffleLineBreakpoint.PROP_LINE_NUMBER, line);
    }
    
}
