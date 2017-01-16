/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.j2seproject.moduletask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.Attribute;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.ClassFile;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.ConstantPool;

/**
 *
 * @author Tomas Zezula
 */
public final class ModuleMainClass extends Task {
    private static final String ATTR_MODULE_MAIN_CLZ = "ModuleMainClass";   //NOI18N
    private String mainClass;
    private File moduleInfo;

    public String getMainclass() {
        return mainClass;
    }

    public void setMainclass(final String mainClass) {
        this.mainClass = mainClass;
    }

    public File getModuleinfo() {
        return moduleInfo;
    }

    public void setModuleinfo(final File moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    @Override
    public void execute() throws BuildException {
        if (mainClass == null) {
            throw new BuildException("MainClass must be set.");             //NOI18N
        }
        if (moduleInfo == null) {
            throw new BuildException("ModuleInfo must be set.");            //NOI18N
        }
        if (!moduleInfo.canRead()) {
            throw new BuildException("MainClass must be readable.");        //NOI18N
        }
        if (!moduleInfo.canWrite()) {
            throw new BuildException("MainClass must be writable.");        //NOI18N
        }
        try {
            ClassFile cf = null;
            try (InputStream in = Files.newInputStream(moduleInfo.toPath())) {
                cf = new ClassFile(in);
                final ConstantPool cp = cf.getConstantPool();
                final int attrNameIndex = cp.add(new ConstantPool.CPUtf8(ATTR_MODULE_MAIN_CLZ));
                final int classNameIndex = cp.add(new ConstantPool.CPUtf8(internalName(mainClass)));
                final int classIndex = cp.add(new ConstantPool.CPClass(classNameIndex));
                final byte[] data = new byte[2];
                data[0] = (byte) (classIndex >>> 8);
                data[1] = (byte) classIndex;
                final Attribute[] attrs = cf.getAttributes();
                int toDelete = -1;
                for (int i = 0; i < attrs.length; i++) {
                    if (attrs[i].getNameIndex() == attrNameIndex) {
                        toDelete = i;
                        break;
                    }
                }
                if (toDelete != -1) {
                    cf.removeAttribute(toDelete);
                }
                cf.addAttribute(new Attribute(attrNameIndex, data));
            }
            if (cf != null) {
                try (OutputStream out = Files.newOutputStream(moduleInfo.toPath())) {
                    cf.write(out);
                }
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }

    private static String internalName(final String externalName) {
        return externalName.replace('.', '/');  //NOI18N
    }
}
