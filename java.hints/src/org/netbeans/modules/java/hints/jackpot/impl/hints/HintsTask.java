/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.hints;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class HintsTask implements CancellableTask<CompilationInfo> {

    public static final String KEY_HINTS = HintsInvoker.class.getName() + "-hints";
    public static final String KEY_SUGGESTIONS = HintsInvoker.class.getName() + "-suggestions";

    private static final Logger TIMER = Logger.getLogger("TIMER");
    private static final Logger TIMER_EDITOR = Logger.getLogger("TIMER.editor");
    private static final Logger TIMER_CARET = Logger.getLogger("TIMER.caret");
    
    private final AtomicBoolean cancel = new AtomicBoolean();

    private final boolean caretAware;

    public HintsTask(boolean caretAware) {
        this.caretAware = caretAware;
    }
    
    public void run(CompilationInfo info) {
        cancel.set(false);

        long startTime = System.currentTimeMillis();

        HintsInvoker inv = caretAware ? new HintsInvoker(info, CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject()), cancel) : new HintsInvoker(info, cancel);
        List<ErrorDescription> result = inv.computeHints(info);

        if (cancel.get()) {
            return;
        }

        HintsController.setErrors(info.getFileObject(), caretAware ? KEY_SUGGESTIONS : KEY_HINTS, result);

        long endTime = System.currentTimeMillis();
        
        TIMER.log(Level.FINE, "Jackpot 3.0 Hints Task" + (caretAware ? " - Caret Aware" : ""), new Object[] {info.getFileObject(), endTime - startTime});

        Logger l = caretAware ? TIMER_CARET : TIMER_EDITOR;

        for (Entry<String, Long> e : inv.getTimeLog().entrySet()) {
            l.log(Level.FINE, e.getKey(), new Object[] {info.getFileObject(), e.getValue()});
        }
    }

    public void cancel() {
        cancel.set(true);
    }


    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory {

        public FactoryImpl() {
            super(Phase.RESOLVED, Priority.LOW);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new HintsTask(false);
        }
        
    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class CaretFactoryImpl extends CaretAwareJavaSourceTaskFactory {

        public CaretFactoryImpl() {
            super(Phase.RESOLVED, Priority.LOW);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new HintsTask(true);
        }

    }
}
