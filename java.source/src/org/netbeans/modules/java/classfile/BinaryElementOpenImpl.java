/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.classfile;

import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.BinaryElementOpen;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.BinaryElementOpen.class)
public class BinaryElementOpenImpl implements BinaryElementOpen {

    @Override
    public boolean open(ClasspathInfo cpInfo, ElementHandle<? extends Element> toOpen) {
        FileObject source = CodeGenerator.generateCode(cpInfo, toOpen);
        if (source != null) {
            return open(source, toOpen);
        } else {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean open(FileObject source, ElementHandle<? extends Element> toOpen) {
        return org.netbeans.api.java.source.UiUtils.open(source, toOpen);
    }

}
