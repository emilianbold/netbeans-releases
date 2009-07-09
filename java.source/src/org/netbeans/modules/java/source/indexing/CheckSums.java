/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.source.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import org.netbeans.modules.java.source.usages.ClassFileUtil;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
public final class CheckSums {

    private static final String CHECK_SUMS_FILE = "checksums.properties"; //NOI18N
    private static final String DEPRECATED = "DEPRECATED"; //NOI18N

    public static CheckSums forContext(final Context context) throws IOException, NoSuchAlgorithmException {
        return new CheckSums(context);
    }

    private final Context context;
    private final Properties props = new Properties();
    private final MessageDigest md;

    private CheckSums(final Context context) throws IOException, NoSuchAlgorithmException {
        assert context != null;
        this.context = context;
        md = MessageDigest.getInstance("MD5"); //NOI18N
        load();
    }

    public boolean checkAndSet(final URL file, final Iterable<? extends TypeElement> topLevelElements, final Elements elements) {
        String fileId = file.toExternalForm();
        String sum = computeCheckSum(topLevelElements, elements);
        String value = (String) props.setProperty(fileId, sum);
        return value == null || value.equals(sum);
    }

    public void remove(URL file) {
        String fileId = file.toExternalForm();
        props.remove(fileId);
    }

    public void store () throws IOException {
        FileObject indexDir = context.getIndexFolder();
        FileObject f = FileUtil.createData(indexDir, CHECK_SUMS_FILE);
        assert f != null;
        final OutputStream out = f.getOutputStream();
        try {
            props.store(out, ""); //NOI18N
        } finally {
            out.close();
        }
    }

    private void load() throws IOException {
        FileObject indexDir = context.getIndexFolder();
        FileObject f = indexDir.getFileObject(CHECK_SUMS_FILE);
        if (f != null) {
            final InputStream in = f.getInputStream();
            try {
                props.load(in);
            } finally {
                in.close();
            }
        }
    }

    private String computeCheckSum(Iterable<? extends TypeElement> topLevelElements, Elements elements) {
        Queue<TypeElement> toHandle = new LinkedList<TypeElement>();
        for (TypeElement te : topLevelElements)
            toHandle.offer(te);
        List<String> sigs = new ArrayList<String>();
        while (!toHandle.isEmpty()) {
            TypeElement te = toHandle.poll();
            if (te == null) {
                //workaround for 6443073
                //see Symbol.java:601
                //see JavacTaskImpl.java:367
                continue;
            }
            sigs.add(ClassFileUtil.encodeClassName(te) + getExtendedModifiers(elements, te));
            for (Element e : te.getEnclosedElements()) {
                switch (e.getKind()) {
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case ANNOTATION_TYPE:
                        toHandle.offer((TypeElement) e);
                        break;
                    case CONSTRUCTOR:
                    case METHOD:
                        sigs.add(Arrays.toString(ClassFileUtil.createExecutableDescriptor((ExecutableElement) e)) + getExtendedModifiers(elements, e));
                        break;
                    case FIELD:
                    case ENUM_CONSTANT:
                        sigs.add(Arrays.toString(ClassFileUtil.createFieldDescriptor((VariableElement) e)) + getExtendedModifiers(elements, e));
                        break;
                }
            }
        }
        Collections.sort(sigs);
        StringBuilder sb = new StringBuilder();
        for (String s : sigs)
            sb.append(s);
        byte[] bytes = md.digest(sb.toString().getBytes());
        return new String(bytes);
    }

    private String getExtendedModifiers(Elements elements, Element el) {
        StringBuilder sb = new StringBuilder();
        for (Modifier m : el.getModifiers())
            sb.append(m.name());
        if (elements.isDeprecated(el))
            sb.append(DEPRECATED);
        if (el.getKind() == ElementKind.FIELD) {
            Object v = ((VariableElement) el).getConstantValue();
            if (v != null) {
                sb.append(v.getClass().getName());
                sb.append(String.valueOf(v));
            }
        }
        return sb.toString();
    }
}
