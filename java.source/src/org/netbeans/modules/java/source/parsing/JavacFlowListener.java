/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.FlowListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
class JavacFlowListener extends FlowListener {
        
        private final Set<URL> flowCompleted = new HashSet<URL>();
        
        public static JavacFlowListener instance (final Context context) {
            final FlowListener flowListener = FlowListener.instance(context);
            return (flowListener instanceof JavacFlowListener) ? (JavacFlowListener) flowListener : null;
        }
        
        static void preRegister(final Context context) {
            context.put(flowListenerKey, new JavacFlowListener());
        }
        
        final boolean hasFlowCompleted (final FileObject fo) {
            if (fo == null) {
                return false;
            }
            else {
                try {
                    return this.flowCompleted.contains(fo.getURL());
                } catch (FileStateInvalidException e) {
                    return false;
                }
            }
        }
        
        @Override
        public void flowFinished (final Env<AttrContext> env) {
            if (env.toplevel != null && env.toplevel.sourcefile != null) {
                try {
                    this.flowCompleted.add (env.toplevel.sourcefile.toUri().toURL());
                } catch (MalformedURLException e) {
                    //never thrown
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }