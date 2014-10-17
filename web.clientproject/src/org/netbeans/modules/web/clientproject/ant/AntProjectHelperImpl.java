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
package org.netbeans.modules.web.clientproject.ant;

import java.io.File;
import java.lang.reflect.Field;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.clientproject.indirect.AntProjectHelper;
import org.netbeans.modules.web.clientproject.indirect.PropertyEvaluator;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;

/**
 */
final class AntProjectHelperImpl extends AntProjectHelper {
    final org.netbeans.spi.project.support.ant.AntProjectHelper delegate;

    public AntProjectHelperImpl(org.netbeans.spi.project.support.ant.AntProjectHelper delegate) {
        this.delegate = delegate;
    }

    public void addAntProjectListener(AntProjectListener listener) {
        delegate.addAntProjectListener(listener);
    }

    public void removeAntProjectListener(AntProjectListener listener) {
        delegate.removeAntProjectListener(listener);
    }

    @Override
    public FileObject getProjectDirectory() {
        return delegate.getProjectDirectory();
    }

    @Override
    public void notifyDeleted() {
        delegate.notifyDeleted();
    }

    @Override
    public Element getPrimaryConfigurationData(boolean shared) {
        return delegate.getPrimaryConfigurationData(shared);
    }

    @Override
    public void putPrimaryConfigurationData(Element data, boolean shared) throws IllegalArgumentException {
        delegate.putPrimaryConfigurationData(data, shared);
    }

    @Override
    public AuxiliaryConfiguration createAuxiliaryConfiguration() {
        return delegate.createAuxiliaryConfiguration();
    }

    @Override
    public CacheDirectoryProvider createCacheDirectoryProvider() {
        return delegate.createCacheDirectoryProvider();
    }

    @Override
    public AuxiliaryProperties createAuxiliaryProperties() {
        return delegate.createAuxiliaryProperties();
    }

    @Override
    public PropertyEvaluator getStandardPropertyEvaluator() {
        return new PropertyEvaluatorImpl(delegate.getStandardPropertyEvaluator());
    }

    @Override
    public File resolveFile(String path) {
        return delegate.resolveFile(path);
    }

    @Override
    public FileObject resolveFileObject(String path) {
        return delegate.resolveFileObject(path);
    }

    @Override
    public void putProperties(Object path, org.openide.util.EditableProperties props) {
        EditableProperties copy = new EditableProperties(props);
        delegate.putProperties(mapPath(path), copy);
    }

    @Override
    public org.openide.util.EditableProperties getProperties(Object path) {
        return extract(delegate.getProperties(mapPath(path)));
    }


    @Override
    public SharabilityQueryImplementation2 createSharabilityQuery2(org.netbeans.modules.web.clientproject.indirect.PropertyEvaluator evaluator, String[] toArray, String[] string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addAntProjectListener(org.netbeans.modules.web.clientproject.indirect.AntProjectListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAntProjectListener(org.netbeans.modules.web.clientproject.indirect.AntProjectListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static final Field DELEGATE;
    static {
        try {
            DELEGATE = org.netbeans.spi.project.support.ant.EditableProperties.class.getDeclaredField("delegate");
            DELEGATE.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            throw new SecurityException(ex);
        }
    }
    
    private static org.openide.util.EditableProperties extract(
        org.netbeans.spi.project.support.ant.EditableProperties p
    ) {
        try {
            return (org.openide.util.EditableProperties) DELEGATE.get(p);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String mapPath(Object path) {
        if (path == AntProjectHelper.PRIVATE_PROPERTIES_PATH) {
            return org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_PROPERTIES_PATH;
        } else if (path == AntProjectHelper.PROJECT_PROPERTIES_PATH) {
            return org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_PROPERTIES_PATH;
        }
        return path.toString();
    }
}
