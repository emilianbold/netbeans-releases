/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.LazyTreeLoader;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.java.source.usages.SymbolDumper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class TreeLoader extends LazyTreeLoader {

    public static void preRegister(final Context context, final ClasspathInfo cpInfo) {
        context.put(lazyTreeLoaderKey, new TreeLoader(context, cpInfo));
    }
    
    private Context context;
    private ClasspathInfo cpInfo;

    private TreeLoader(Context context, ClasspathInfo cpInfo) {
        this.context = context;
        this.cpInfo = cpInfo;
    }
    
    @Override
    public boolean loadTreeFor(final ClassSymbol clazz) {
        if (clazz != null) {
            try {
                FileObject fo = SourceUtils.getFile(clazz, cpInfo);                
                JavacTaskImpl jti = context.get(JavacTaskImpl.class);
                if (fo != null && jti != null) {
                    Log.instance(context).nerrors = 0;
                    JavaFileObject jfo = FileObjects.nbFileObject(fo, null);
                    try {
                        jti.analyze(jti.enter(jti.parse(jfo)));
                        dumpSymFile(clazz);
                        return true;                        
                    } catch (CouplingAbort ca) {
                        RepositoryUpdater.couplingAbort(ca, jfo);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    private void dumpSymFile(ClassSymbol clazz) throws IOException {
        PrintWriter writer = null;
        try {
            JavaFileManager fm = ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo);
            String binaryName = null;
            if (clazz.classfile != null) {
                binaryName = fm.inferBinaryName(StandardLocation.PLATFORM_CLASS_PATH, clazz.classfile);
                if (binaryName == null)
                    binaryName = fm.inferBinaryName(StandardLocation.CLASS_PATH, clazz.classfile);                
            }
            else if (clazz.sourcefile != null) {
                binaryName = fm.inferBinaryName(StandardLocation.SOURCE_PATH, clazz.sourcefile);
            }
            if (binaryName == null) {
                return;
            }
            String surl = clazz.classfile.toUri().toURL().toExternalForm();
            int index = surl.lastIndexOf(FileObjects.convertPackage2Folder(binaryName));
            assert index > 0;
            File classes = Index.getClassFolder(new URL(surl.substring(0, index)));
            String pkg, name;
            index = binaryName.lastIndexOf('.');
            if (index < 0) {
                pkg = null;
                name = binaryName;
            } else {
                pkg = binaryName.substring(0, index);
                assert binaryName.length() > index;
                name = binaryName.substring(index + 1);
            }
            if (pkg != null) {
                classes = new File(classes, pkg.replace('.', File.separatorChar));
                if (!classes.exists())
                    classes.mkdirs();
            }
            File outputFile = new File(classes, name + '.' + FileObjects.SIG);
            if (outputFile.exists())
                return ;//no point in dumping again already existing sig file
            writer = new PrintWriter(outputFile, "UTF-8");
            Symbol owner;
            if (clazz.owner.kind == Kinds.PCK) {
                owner = null;
            }
            else if (clazz.owner.kind == Kinds.VAR) {
                owner = clazz.owner.owner;
            }
            else {
                owner = clazz.owner;
            }
            SymbolDumper.dump(writer, Types.instance(context), clazz, owner);
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
