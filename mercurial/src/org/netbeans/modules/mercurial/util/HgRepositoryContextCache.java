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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.mercurial.util;

import java.io.File;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * A class to encapsulate a Repository and allow us to cache some values
 *
 * @author John Rice
 */
public class HgRepositoryContextCache {
    private static boolean hasHistory;
    private static boolean hasHeads;
    private static String pushDefault;
    private static String pullDefault;
    private static File root;
    
    private static VCSContext rootCtx;
    private static VCSContext historyCtx;
    private static VCSContext headsCtx;
    private static VCSContext pushCtx;
    private static VCSContext pullCtx;

    public static boolean hasHistory(VCSContext ctx) {
        if(ctx == historyCtx && ctx != null && hasHistory){
            return hasHistory;
        }else{
            root = getRoot(ctx);
            hasHistory = HgCommand.hasHistory(root);
            historyCtx = ctx;
            return hasHistory;
        }
    }
    
    public static void resetHasHeads() {
        headsCtx = null;
    }

    public static boolean hasHeads(VCSContext ctx) {
        
        if(ctx == headsCtx && ctx != null){
            return hasHeads;
        }else{
            root = getRoot(ctx);
            hasHeads = HgCommand.isMergeRequired(root);
            headsCtx = ctx;
            return hasHeads;
        }
    }
    
    public static void resetPullDefault() {
        pullCtx = null;
    }

    public static String getPullDefault(VCSContext ctx) {
        if(ctx == pullCtx && ctx != null){
            return pullDefault;
        }else{
            root = getRoot(ctx);
            pullDefault = HgCommand.getPullDefault(root);
            pullCtx = ctx;
            return pullDefault;
        }
    }
    
    public static void resetPushDefault() {
        pushCtx = null;
    }

    public static String getPushDefault(VCSContext ctx) {
        if(ctx == pushCtx && ctx != null){
            return pushDefault;
        }else{
            root = getRoot(ctx);
            pushDefault = HgCommand.getPushDefault(root);
            pushCtx = ctx;
            return pushDefault;
        }
    }
    
    private static File getRoot(VCSContext ctx){
        if(ctx == rootCtx && root != null){
            return root;
        }else{
            root = HgUtils.getRootFile(ctx);
            rootCtx = ctx;
            return root;
        }

    }
}

