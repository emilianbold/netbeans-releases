/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.api.ruby.platform;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ruby.platform.RubyPreferences;
import org.netbeans.modules.ruby.platform.execution.ExecutionUtils;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class RubyPlatformProvider {

    private static final Logger LOGGER = Logger.getLogger(RubyPlatformProvider.class.getName());
    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor(ExecutionUtils.namedThreadFactory("Ruby Platform AutoDetection"));//NOI18N
    private final PropertyEvaluator evaluator;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                EXECUTOR.shutdown();
            }
        });

    }

    public RubyPlatformProvider(final PropertyEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public RubyPlatform getPlatform() {
        String id = evaluator.getProperty("platform.active"); // NOI18N
        return getPlatform(id);
    }

    public static RubyPlatform getPlatform(String id) {
        ensurePlatformsReady();
        RubyPlatform platform = id == null ? RubyPlatformManager.getDefaultPlatform() : RubyPlatformManager.getPlatformByID(id);
        if (platform == null) {
            LOGGER.info("Platform with id '" + id + "' does not exist. Using default platform.");
            platform = RubyPlatformManager.getDefaultPlatform();
        }
        return platform;

    }

    /**
     * Ensures that platforms are ready, i.e. performs platform autodetection
     * if needed. Displays a progress handle for the process. 
     * 
     * Note that this method is blocking.
     */
    public static void ensurePlatformsReady() {
        if (!RubyPreferences.isFirstPlatformTouch()) {
            return;
        }
        String handleMessage = NbBundle.getMessage(RubyPlatformProvider.class, "RubyPlatformProvider.RubyPlatformAutoDetection");
        ProgressHandle ph = ProgressHandleFactory.createHandle(handleMessage);
        ph.start();

        Future<?> result = EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                RubyPlatformManager.performPlatformDetection();
            }
        });

        try {
            result.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (TimeoutException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            ph.finish();
        }
    }
}
