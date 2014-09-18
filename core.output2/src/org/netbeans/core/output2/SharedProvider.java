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
package org.netbeans.core.output2;

import java.util.WeakHashMap;
import javax.swing.Action;
import org.openide.windows.IOContainer;

/**
 * Shared provider for both the original (org.openide.io/org.openide.windows)
 * and the base (org.openide.io.base) APIs.
 *
 * @author jhavlin
 */
public class SharedProvider {

    private static final WeakHashMap<IOContainer, PairMap> containerPairMaps
            = new WeakHashMap<IOContainer, PairMap>();
    private static final String NAME = "output2"; // NOI18N

    private static class SingletonHolder {

        static SharedProvider INSTANCE = new SharedProvider();
    }

    public static SharedProvider getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private SharedProvider() {
        System.out.println("x");
    }

    public String getName() {
        return NAME;
    }

    NbIO getIO(String name, boolean newIO,
            Action[] toolbarActions, IOContainer ioContainer) {
        if (Controller.LOG) {
            Controller.log("GETIO: " + name + " new:" + newIO);
        }
        IOContainer realIoContainer = ioContainer == null
                ? IOContainer.getDefault() : ioContainer;
        NbIO result;
        synchronized (containerPairMaps) {
            PairMap namesToIos = containerPairMaps.get(realIoContainer);
            result = namesToIos != null ? namesToIos.get(name) : null;
        }
        if (result == null || newIO) {
            result = new NbIO(name, toolbarActions, realIoContainer);
            synchronized (containerPairMaps) {
                PairMap namesToIos = containerPairMaps.get(realIoContainer);
                if (namesToIos == null) {
                    namesToIos = new PairMap();
                    containerPairMaps.put(realIoContainer, namesToIos);
                }
                namesToIos.add(name, result);
            }
            NbIO.post(new IOEvent(result, IOEvent.CMD_CREATE, newIO));
        }
        return result;
    }

    static void dispose(NbIO io) {
        IOContainer ioContainer = io.getIOContainer();
        if (ioContainer == null) {
            ioContainer = IOContainer.getDefault();
        }
        synchronized (containerPairMaps) {
            PairMap namesToIos = containerPairMaps.get(ioContainer);
            if (namesToIos != null) {
                namesToIos.remove(io);
                if (namesToIos.isEmpty()) {
                    containerPairMaps.remove(ioContainer);
                }
            }
        }
    }
}
