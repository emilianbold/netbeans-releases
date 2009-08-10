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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.wag.codegen.java;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.wag.codegen.Constants;
import org.netbeans.modules.wag.codegen.WagClientCodeGenerator;
import org.netbeans.modules.wag.codegen.util.Util;
import org.netbeans.modules.wag.manager.model.WagService;
import org.netbeans.modules.wag.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.wag.codegen.java.support.JavaUtil;
import org.netbeans.modules.wag.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.wag.manager.model.WagServiceParameter;
import org.openide.filesystems.FileObject;

/**
 * Code generator for Accessing Saas services.
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.wag.codegen.spi.WagCodeGenerationProvider.class)
public class WagJavaClientCodeGenerator extends WagClientCodeGenerator {

    private JavaSource targetSource;
    private FileObject defaultPkg;
    private String packageName;
    private WagService service;

    public WagJavaClientCodeGenerator() {
        setDropFileType(Constants.DropFileType.JAVA_CLIENT);
    }

    public boolean canAccept(WagService service, Document doc) {
        if (Util.isJava(doc)) {
            return true;
        }
        return false;
    }

    @Override
    public void init(WagService service, Document doc) throws IOException {
        super.init(service, doc);
        targetSource = JavaSource.forFileObject(getTargetFile());
        packageName = JavaSourceHelper.getPackageName(targetSource);
        this.service = service;
    }

    protected JavaSource getTargetSource() {
        return this.targetSource;
    }

    @Override
    protected void preGenerate() throws IOException {
        super.preGenerate();
        addWagLib();

        try {
            Util.createDataObjectFromTemplate("Templates/WAG/zcl.properties",
                    getDefaultFolder(), "zcl");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private FileObject getDefaultFolder() {
        SourceGroup srcGroup = SourceGroupSupport.findSourceGroupForFile(getProject(), getTargetFile());

        return srcGroup.getRootFolder();
    }

    @Override
    public Set<FileObject> generate() throws IOException {

        preGenerate();

        //No need to check scanning, since we are not locking document
        //Util.checkScanning();
        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        addImportsToTargetFile();

        finishProgressReporting();

        return new HashSet<FileObject>(Collections.EMPTY_LIST);
    }

    protected void addWagLib() throws IOException {
        JavaUtil.addWagLib(getProject());
    }

    protected void addImportsToTargetFile() throws IOException {
        List<String> imports = Arrays.asList("com.zembly.gateway.client.Zembly");
        JavaUtil.addImportsToSource(getTargetSource(), imports);
    }

    protected String getSaasServiceAccessCode() {
        String paramStr = "new String[][] {";

        int index = 0;
        for (WagServiceParameter p : service.getParameters()) {
            if (index++ > 0) {
                paramStr += ", ";
            }
            paramStr += "{\"" + p.getName() + "\", " + "null}";
        }

        paramStr += "}";

        String code = "    try {\n";
        code += "        String result = Zembly.getInstance().callService(\"" + service.getCallableName() +
                "\",\n            " + paramStr + ");\n" +
                "        " + this.getDropFileType().getPrintWriterType() + ".println(\"result: \" + result);\n";
        code += "    } catch (Exception ex) {\n" +
                "        ex.printStackTrace();\n" +
                "    }";

        return code;
    }

    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            insert(getSaasServiceAccessCode(), true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
