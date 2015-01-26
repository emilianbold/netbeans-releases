/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.query;

import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.codemodel.bridge.impl.NativeProjectBridge;
import org.openide.util.RequestProcessor;

/**
 * A placeholder for methods that are absent in current API
 *
 * @author Vladimir Kvashin
 */
public class CMPlatformStubs {

    private CMPlatformStubs() {}

    public static boolean isCMModelAvailable() {
//        new UnsupportedOperationException().printStackTrace();
        return true;
    }
    
    public static void disableProject(NativeProject np) {
        new UnsupportedOperationException().printStackTrace();
    }

    public static void enableProject(NativeProject np) {
        new UnsupportedOperationException().printStackTrace();
    }

    public static void enableProjectListeners(NativeProject np, boolean enable) {
        new UnsupportedOperationException().printStackTrace();
    }
    
    public static void shutdownModel() {
//        new UnsupportedOperationException().printStackTrace();
    }
    
    public static void addNativeProject(final NativeProject np) {
        //new UnsupportedOperationException().printStackTrace();
        new RequestProcessor().submit(new Runnable() {
            @Override
            public void run() {
                NativeProjectBridge.getInstance().open(np);
            }
        });
    }
    
    public static void closeNativeProject(NativeProject np) {
        //new UnsupportedOperationException().printStackTrace();
    }
    
    public static void addProgressListener(CMProgressListener l) {
        //new UnsupportedOperationException().printStackTrace();
        NativeProjectBridge.getInstance().addProgressListener(l);
    }

    public static void removeProgressListener(CMProgressListener l) {
        //new UnsupportedOperationException().printStackTrace();
        NativeProjectBridge.getInstance().removeProgressListener(l);
    }

    public static NativeFileItem getNativeFileItem(CMTranslationUnit next) {
        new UnsupportedOperationException().printStackTrace();
        return null;
    }

    public static NativeFileItem getNativeFileItem(CMFile next) {
        new UnsupportedOperationException().printStackTrace();
        return null;
    }
}
