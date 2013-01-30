/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.libs.oracle.cloud.ext;

import java.util.List;
import org.netbeans.libs.oracle.cloud.scanningwrapper.ConfigurationFactory;
import org.netbeans.libs.oracle.cloud.scanningwrapper.IClassConfiguration;
import org.netbeans.libs.oracle.cloud.scanningwrapper.Result;

public final class ConfigurationFactoryImpl implements ConfigurationFactory {

    private ClassLoader classLoader;
    
    public ConfigurationFactoryImpl(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    @Override
    public IClassConfiguration getDefaultClassConfiguration() {
        try {
            return new ClassConfigurationImpl(oracle.cloud.scanning.spi.config.factory.ConfigurationFactory.
                getInstance().getDefaultClassConfiguration());
        } catch (oracle.cloud.paas.exception.ManagerException ex) {
            throw ApplicationManagerImpl.wrapException(ex);
        }
    }

    private static class ClassConfigurationImpl implements IClassConfiguration {

        private oracle.cloud.scanning.api.config.IClassConfiguration original;

        public ClassConfigurationImpl(oracle.cloud.scanning.api.config.IClassConfiguration original) {
            this.original = original;
        }
        
        @Override
        public Result checkClassAllowed(String string) {
            return convert(original.checkClassAllowed(string));
        }

        @Override
        public Result checkFieldAllowed(String string, String string1) {
            return convert(original.checkFieldAllowed(string, string1));
        }

        @Override
        public Result checkMethodAllowed(String string, String string1, List<String> list) {
            return convert(original.checkMethodAllowed(string, string1, list));
        }
        
        private Result convert(oracle.cloud.scanning.api.config.Result r) {
            return new Result(r.isAllowed(), 
                    r.getSeverity() == oracle.cloud.scanning.api.config.Result.ResultSeverity.WARNING,
                    r.getMessage());
        }
        
    }
}
