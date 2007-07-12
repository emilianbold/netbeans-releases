/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.ReferenceRepositoryImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Vladimir Voskresensky
 */
public class TraceXRef extends TraceModel {
    private String refFile = "";
    private String declarationName = "";
    private int line = 0;
    private int column = 0;
    public TraceXRef() {
    }
    
    public static void main(String[] args) {
        setUp();
        TraceXRef trace = new TraceXRef();
        trace.test(args);
    }    
    
    private void test(String[] args) {
        try {
            processArguments(args);
            doTest();
            long time = 0;
            if (super.isShowTime()) {
                time = System.currentTimeMillis();
            }
            CsmObject object = null;
            if (declarationName.length() > 0) {
                System.err.println("looking for object with name: " + declarationName);
                object = super.getProject().findDeclaration(declarationName);
                if (object == null) {
                    System.err.println("No object with name " + declarationName + " in model");
                }
            } else if ((refFile.length() > 0) && (line > 0) && (column > 0)) {
                System.out.println("looking for object on position: line=" + line + " column="+column);
                System.out.println("in file:" + refFile);
                CsmFile file = getCsmFile(refFile);
                if (!(file instanceof FileImpl)) {
                    System.err.println("No CsmFile was found with name: " + refFile);
                } else {
                    FileImpl implFile = (FileImpl)file;
                    int offset = implFile.getOffset(line, column);
                    if (offset < 0) {
                        System.err.println("incorrect offset for position line="+line+" col="+column);
                    } else {
                        CsmReference ref = CsmReferenceResolver.getDefault().findReference(implFile, offset);
                        if (ref == null) {
                            System.err.println("no any references were found on position line="+line+" col="+column);
                        } else {
                            object = ref.getReferencedObject();
                        }
                    }
                }
            } else {
                System.err.println("To run xref tests start script with parameter:");
                System.err.println("should be --xref#file_path#1_based_line#1_based_column or --xref#name");
            }
            if (object == null) {
                System.out.println("Nothing to search");
            } else {
                System.out.println("TARGET OBJECT IS\n  " + CsmTracer.toString(object));
                if (CsmKindUtilities.isNamedElement(object)) {
                    System.out.println("NAME IS: " + ((CsmNamedElement)object).getName());
                }
                if (CsmKindUtilities.isDeclaration(object)) {
                    System.out.println("UNIQUE NAME IS: " + ((CsmDeclaration)object).getUniqueName());
                }
                
                ReferenceRepositoryImpl xRefRepository = new ReferenceRepositoryImpl();
                CsmObject[] decDef = ReferenceRepositoryImpl.getDefinitionDeclaration(object);
                CsmObject decl = decDef[0];
                CsmObject def = decDef[1];                
                Collection<CsmReference> refs = xRefRepository.getReferences(decl, getProject(), true);
                if (super.isShowTime()) {
                    time = System.currentTimeMillis() - time;
                }            
                traceRefs(refs, decl, def, System.out);
                if (super.isShowTime()) {
                    System.out.println("search took " + time + "ms");
                }       
                ReferenceRepositoryImpl.getDefinitionDeclaration(object);
            }
        }
        finally {
            super.shutdown();
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().close();
            } else {
                APTDriver.getInstance().close();
            }            
        }        
    }

    @SuppressWarnings("deprecation")
    private static void setUp() {
        // this is the only way to init extension-based recognizer
        FileUtil.setMIMEType("cc", "text/x-c++"); // NOI18N
        FileUtil.setMIMEType("h", "text/x-c++"); // NOI18N
        FileUtil.setMIMEType("c", "text/x-c"); // NOI18N
        
        JEditorPane.registerEditorKitForContentType("text/x-c++", "org.netbeans.modules.cnd.editor.cplusplus.CCKit"); // NOI18N
        
        JEditorPane.registerEditorKitForContentType("text/x-c", "org.netbeans.modules.cnd.editor.cplusplus.CKit"); // NOI18N
    }
    
    private CsmFile getCsmFile(String path) {
        return super.getProject().findFile(new File(path).getAbsolutePath());
    }
    
    protected void processFlag(String flag) {
        String xRef = "xref"; // NOI18N
        if (flag.startsWith(xRef)) {
            String[] split = flag.split("#"); // NOI18N
            boolean error = false;
            if (split.length == 2) {
                declarationName = split[1];
                error = (declarationName == null) || (declarationName.length() == 0);
            } else if (split.length == 4) {
                refFile = split[1];
                try {
                    line = Integer.parseInt(split[2]);
                    column = Integer.parseInt(split[3]);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    line = 0;
                    column = 0;
                }
                error = (refFile == null) || (refFile.length() == 0) || line <= 0 || column <= 0;
            }
            if (error) {
                declarationName ="";
                refFile ="";
                System.err.println("unexpected parameter " + flag);
                System.err.println("should be --xref#file_path#1_based_line#1_based_column or --xref#name");
            }
        }
    }
    
    public static void traceRefs(Collection<CsmReference> out, CsmObject target, PrintStream streamOut) {
        assert target != null;
        CsmObject[] decDef = ReferenceRepositoryImpl.getDefinitionDeclaration(target);
        CsmObject decl = decDef[0];
        CsmObject def = decDef[1];        
        assert decl != null;
        traceRefs(out, decl, def, streamOut);
    }
    
    public static void traceRefs(Collection<CsmReference> out, CsmObject targetDecl, CsmObject targetDef, PrintStream streamOut) {
        if (out.size() == 0) {
            streamOut.println("REFERENCES ARE NOT FOUND"); // NOI18N
        } else {
            streamOut.println("REFERENCES ARE:" ); // NOI18N
            out = sortRefs(out);
            for (CsmReference ref : out) {
                streamOut.println(toString(ref, targetDecl, targetDef));
            }
        }        
    }
    
    public static final class RefHyperlink implements OutputListener {
        private final CsmReference ref;
        public RefHyperlink(CsmReference ref) {
            this.ref = ref;
        }
        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
            CsmUtilities.openSource((CsmOffsetable)ref);
        }

        public void outputLineCleared(OutputEvent ev) {
        }
        
    }
    public static void traceRefs(Collection<CsmReference> out, CsmObject targetDecl, CsmObject targetDef, OutputWriter writer) throws IOException {
        if (out.size() == 0) {
            writer.println("REFERENCES ARE NOT FOUND"); // NOI18N
        } else {
            writer.println("REFERENCES ARE:" ); // NOI18N
            out = sortRefs(out);
            for (CsmReference ref : out) {
                writer.println(toString(ref, targetDecl, targetDef), new RefHyperlink(ref), true);
            }
        }        
    }
    
    private static String toString(CsmReference ref, CsmObject targetDecl, CsmObject targetDef) {
        String out = CsmTracer.getOffsetString(ref, true);
        ReferenceRepositoryImpl.ReferenceKind kind = ReferenceRepositoryImpl.getReferenceKind(ref, targetDecl, targetDef);
        String postfix;
        if (kind == ReferenceRepositoryImpl.ReferenceKind.DECLARATION) {
            postfix = " (DECLARATION)"; // NOI18N
        } else if (kind == ReferenceRepositoryImpl.ReferenceKind.DEFINITION) {
            postfix = " (DEFINITION)"; // NOI18N
        } else {
            assert kind == ReferenceRepositoryImpl.ReferenceKind.USAGE : "unknown reference kind" + kind;
            postfix = "";
        }
        return out + postfix;
    }
        
    public static Collection<CsmReference> sortRefs(Collection<CsmReference> refs) {
        List<CsmReference> out = new ArrayList(refs); 
        Collections.sort(out, FILE_NAME_START_OFFSET_COMPARATOR);
        return out;
    }    
    
    public static final Comparator<CsmOffsetable> FILE_NAME_START_OFFSET_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            CsmOffsetable i1 = (CsmOffsetable)o1;
            CsmOffsetable i2 = (CsmOffsetable)o2; 
            String path1 = i1.getContainingFile().getAbsolutePath();
            String path2 = i2.getContainingFile().getAbsolutePath();
            int res = path1.compareTo(path2);
            if (res == 0) {
                int ofs1 = i1.getStartOffset();
                int ofs2 = i2.getStartOffset();
                res = ofs1 - ofs2;
            }
            return res;
        }   
        
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        public int hashCode() {
            return 11; // any dummy value
        }          
    };    
}
