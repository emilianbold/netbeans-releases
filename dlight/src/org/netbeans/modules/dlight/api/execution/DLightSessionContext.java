/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.api.execution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.netbeans.modules.dlight.api.impl.DLightSessionContextAccessor;

/**
 * This class represents DLight Session context
 * The following keys are supported:
 * <ul>
 * <li>executable</li>
 *  <li>line</li>
 *  <li>os</li>
 *  <li>offset</li>
 *  <li>useCollectors</li>
 *  <li>collectors</li>
 * </ul>
 *
 */
public final class DLightSessionContext {
    private final Map<String, String> map;

    static{
        DLightSessionContextAccessor.setDefault(new DLightSessionContextAccessorImpl());
    }

    private DLightSessionContext(){
        map = new ConcurrentHashMap<String, String>();
    }

    public String get(String key){
        return map.get(key);
    }

    String put(String key, String value){
        return map.put(key, value);
    }

    void clear(){
        map.clear();
    }

    private static class DLightSessionContextAccessorImpl extends DLightSessionContextAccessor{

        @Override
        public void clear(DLightSessionContext context) {
            context.clear();
        }

        @Override
        public String put(DLightSessionContext context, String key, String value) {
            return context.put(key, value);
        }

        @Override
        public DLightSessionContext newContext() {
            return new DLightSessionContext();
        }

    }
}
