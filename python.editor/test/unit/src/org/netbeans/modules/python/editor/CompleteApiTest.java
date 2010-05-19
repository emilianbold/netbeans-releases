/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.gsf.api.ParserResult;
import org.openide.filesystems.FileObject;

/**
 * This test ensures that we're picking up all the expected
 * APIs for the various core classes (str, list, etc).
 *
 * @author Tor Norbye
 */
public class CompleteApiTest extends PythonTestBase {
    public CompleteApiTest(String name) {
        super(name);
    }

    // Computed by doing things like "dir(5),dir(5.0),dir("s"),dir(1==1),dir(complex(5,1)),dir([]),dir({}),dir(5L),dir((1,2)),dir(u"s")" and
    // so on for various literal types in the python console. 
    // This is for Python 2.6.1.
    String[] INT_ATTRS = new String[]{"__abs__", "__add__", "__and__", "__class__", "__cmp__", "__coerce__", "__delattr__", "__div__", "__divmod__", "__doc__", "__float__", "__floordiv__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__hex__", "__index__", "__init__", "__int__", "__invert__", "__long__", "__lshift__", "__mod__", "__mul__", "__neg__", "__new__", "__nonzero__", "__oct__", "__or__", "__pos__", "__pow__", "__radd__", "__rand__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__", "__ror__", "__rpow__", "__rrshift__", "__rshift__", "__rsub__", "__rtruediv__", "__rxor__", "__setattr__", "__sizeof__", "__str__", "__sub__", "__subclasshook__", "__truediv__", "__trunc__", "__xor__", "conjugate", "denominator", "imag", "numerator", "real"};
    String[] FLOAT_ATTRS = new String[] {"__abs__", "__add__", "__class__", "__coerce__", "__delattr__", "__div__", "__divmod__", "__doc__", "__eq__", "__float__", "__floordiv__", "__format__", "__ge__", "__getattribute__", "__getformat__", "__getnewargs__", "__gt__", "__hash__", "__init__", "__int__", "__le__", "__long__", "__lt__", "__mod__", "__mul__", "__ne__", "__neg__", "__new__", "__nonzero__", "__pos__", "__pow__", "__radd__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rmod__", "__rmul__", "__rpow__", "__rsub__", "__rtruediv__", "__setattr__", "__setformat__", "__sizeof__", "__str__", "__sub__", "__subclasshook__", "__truediv__", "__trunc__", "as_integer_ratio", "conjugate", "fromhex", "hex", "imag", "is_integer", "real"};
    String[] COMPLEX_ATTRS = new String[] {"__abs__", "__add__", "__class__", "__coerce__", "__delattr__", "__div__", "__divmod__", "__doc__", "__eq__", "__float__", "__floordiv__", "__format__", "__ge__", "__getattribute__", "__getnewargs__", "__gt__", "__hash__", "__init__", "__int__", "__le__", "__long__", "__lt__", "__mod__", "__mul__", "__ne__", "__neg__", "__new__", "__nonzero__", "__pos__", "__pow__", "__radd__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rmod__", "__rmul__", "__rpow__", "__rsub__", "__rtruediv__", "__setattr__", "__sizeof__", "__str__", "__sub__", "__subclasshook__", "__truediv__", "conjugate", "imag", "real"};
    String[] BOOL_ATTRS = new String[] {"__abs__", "__add__", "__and__", "__class__", "__cmp__", "__coerce__", "__delattr__", "__div__", "__divmod__", "__doc__", "__float__", "__floordiv__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__hex__", "__index__", "__init__", "__int__", "__invert__", "__long__", "__lshift__", "__mod__", "__mul__", "__neg__", "__new__", "__nonzero__", "__oct__", "__or__", "__pos__", "__pow__", "__radd__", "__rand__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__", "__ror__", "__rpow__", "__rrshift__", "__rshift__", "__rsub__", "__rtruediv__", "__rxor__", "__setattr__", "__sizeof__", "__str__", "__sub__", "__subclasshook__", "__truediv__", "__trunc__", "__xor__", "conjugate", "denominator", "imag", "numerator", "real"};
    String[] STR_ATTRS = new String[] {"__add__", "__class__", "__contains__", "__delattr__", "__doc__", "__eq__", "__format__", "__ge__", "__getattribute__", "__getitem__", "__getnewargs__", "__getslice__", "__gt__", "__hash__", "__init__", "__le__", "__len__", "__lt__", "__mod__", "__mul__", "__ne__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__rmod__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "_formatter_field_name_split", "_formatter_parser", "capitalize", "center", "count", "decode", "encode", "endswith", "expandtabs", "find", "format", "index", "isalnum", "isalpha", "isdigit", "islower", "isspace", "istitle", "isupper", "join", "ljust", "lower", "lstrip", "partition", "replace", "rfind", "rindex", "rjust", "rpartition", "rsplit", "rstrip", "split", "splitlines", "startswith", "strip", "swapcase", "title", "translate", "upper", "zfill"};
    String[] LIST_ATTRS = new String[] {"__add__", "__class__", "__contains__", "__delattr__", "__delitem__", "__delslice__", "__doc__", "__eq__", "__format__", "__ge__", "__getattribute__", "__getitem__", "__getslice__", "__gt__", "__hash__", "__iadd__", "__imul__", "__init__", "__iter__", "__le__", "__len__", "__lt__", "__mul__", "__ne__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__reversed__", "__rmul__", "__setattr__", "__setitem__", "__setslice__", "__sizeof__", "__str__", "__subclasshook__", "append", "count", "extend", "index", "insert", "pop", "remove", "reverse", "sort"};
    String[] DICT_ATTRS = new String[] {"__class__", "__cmp__", "__contains__", "__delattr__", "__delitem__", "__doc__", "__eq__", "__format__", "__ge__", "__getattribute__", "__getitem__", "__gt__", "__hash__", "__init__", "__iter__", "__le__", "__len__", "__lt__", "__ne__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__setattr__", "__setitem__", "__sizeof__", "__str__", "__subclasshook__", "clear", "copy", "fromkeys", "get", "has_key", "items", "iteritems", "iterkeys", "itervalues", "keys", "pop", "popitem", "setdefault", "update", "values"};
    String[] TUPLE_ATTRS = new String[] {"__add__", "__class__", "__contains__", "__delattr__", "__doc__", "__eq__", "__format__", "__ge__", "__getattribute__", "__getitem__", "__getnewargs__", "__getslice__", "__gt__", "__hash__", "__init__", "__iter__", "__le__", "__len__", "__lt__", "__mul__", "__ne__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "count", "index"};
    String[] LONG_ATTRS = new String[] {"__abs__", "__add__", "__and__", "__class__", "__cmp__", "__coerce__", "__delattr__", "__div__", "__divmod__", "__doc__", "__float__", "__floordiv__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__hex__", "__index__", "__init__", "__int__", "__invert__", "__long__", "__lshift__", "__mod__", "__mul__", "__neg__", "__new__", "__nonzero__", "__oct__", "__or__", "__pos__", "__pow__", "__radd__", "__rand__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__", "__ror__", "__rpow__", "__rrshift__", "__rshift__", "__rsub__", "__rtruediv__", "__rxor__", "__setattr__", "__sizeof__", "__str__", "__sub__", "__subclasshook__", "__truediv__", "__trunc__", "__xor__", "conjugate", "denominator", "imag", "numerator", "real"};
    String[] UNICODE_ATTRS = new String[] {"__add__", "__class__", "__contains__", "__delattr__", "__doc__", "__eq__", "__format__", "__ge__", "__getattribute__", "__getitem__", "__getnewargs__", "__getslice__", "__gt__", "__hash__", "__init__", "__le__", "__len__", "__lt__", "__mod__", "__mul__", "__ne__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__rmod__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "_formatter_field_name_split", "_formatter_parser", "capitalize", "center", "count", "decode", "encode", "endswith", "expandtabs", "find", "format", "index", "isalnum", "isalpha", "isdecimal", "isdigit", "islower", "isnumeric", "isspace", "istitle", "isupper", "join", "ljust", "lower", "lstrip", "partition", "replace", "rfind", "rindex", "rjust", "rpartition", "rsplit", "rstrip", "split", "splitlines", "startswith", "strip", "swapcase", "title", "translate", "upper", "zfill"};

    private static void appendList(StringBuilder sb, List<String> list) {
        sb.append("{ ");
        boolean first = true;
        for (String m : list) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append('"');
            sb.append(m);
            sb.append('"');
        }
        sb.append(" }; ");
    }

    public void checkFile(String clz, String[] attrs, String... relFilePaths) throws Exception {
        Set<String> defined = new HashSet<String>(100);
        boolean foundClass = false;

        for (String relFilePath : relFilePaths) {
            List<IndexDocument> result = indexFile(relFilePath);

            IndexDocumentImpl clzDoc = null;
            for (IndexDocument doc : result) {
                assertTrue(doc instanceof IndexDocumentImpl);
                IndexDocumentImpl idoc = (IndexDocumentImpl)doc;
                for (int i = 0; i < idoc.indexedKeys.size(); i++) {
                    if (PythonIndexer.FIELD_CLASS_NAME.equals(idoc.indexedKeys.get(i))) {
                        if (clz.equals(idoc.indexedValues.get(i))) {
                            clzDoc = idoc;
                            break;
                        }
                    }
                    if (clzDoc != null) {
                        break;
                    }
                }
            }

            if (clzDoc == null) {
                continue;
            }
            foundClass = true;

            for (int i = 0; i < clzDoc.indexedKeys.size(); i++) {
                String key = clzDoc.indexedKeys.get(i);
                if (PythonIndexer.FIELD_MEMBER.equals(key)) {
                    String value = clzDoc.indexedValues.get(i);
                    int semi = value.indexOf(';');
                    if (semi != -1) {
                        value = value.substring(0, semi);
                    }
                    defined.add(value);
                }
            }
        }

        if (!foundClass) {
            StringBuilder sb = new StringBuilder();
            sb.append("No class definition whatsoever for ");
            sb.append(clz);
            sb.append(" : ");
            appendList(sb, Arrays.asList(attrs));

            fail(sb.toString());
        }

        // Now check that all attributes are accounted for (and produce a complete list)
        List<String> missing = new ArrayList<String>();
        for (String attribute : attrs) {
            // Current exceptions - not yet handled
            if ("__reduce__".equals(attribute) ||
                    "__reduce_ex__".equals(attribute)) {
                continue;
            }


            if (!defined.contains(attribute)) {
                missing.add(attribute);
            }
        }

        if (missing.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Class ");
            sb.append(clz);
            sb.append(" is missing definitions for: ");
            appendList(sb, missing);

            fail(sb.toString());
        }

        Set<String> extra = new HashSet<String>(defined);
        for (String attribute : attrs) {
            extra.remove(attribute);
        }

        if (extra.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Class ");
            sb.append(clz);
            sb.append(" is defining extra names that should not be present: ");
            List<String> extraList = new ArrayList<String>(extra);
            Collections.sort(extraList);
            appendList(sb, extraList);

            fail(sb.toString());
        }
    }

    public void testFloat() throws Exception {
        checkFile("float", FLOAT_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testInt() throws Exception {
        checkFile("int", INT_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testComplex() throws Exception {
        checkFile("complex", COMPLEX_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testBool() throws Exception {
        checkFile("bool", BOOL_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testStr() throws Exception {
        checkFile("str", STR_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testList() throws Exception {
        checkFile("list", LIST_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testDict() throws Exception {
        checkFile("dict", DICT_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testTuple() throws Exception {
        checkFile("tuple", TUPLE_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testLong() throws Exception {
        checkFile("long", LONG_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

    public void testUnicode() throws Exception {
        checkFile("unicode", UNICODE_ATTRS, "testfiles/rst/stdtypes.rst", "testfiles/rst/operator.rst", "testfiles/rst/stub_missing.rst");
    }

// Generate a list of documented APIs
//    public void testGenerateDocumentedNames() throws Exception {
//        FileObject fo = getClusterHome().getFileObject("pythonstubs/pythonstubs-2_6_1.egg");
//        assertNotNull(fo);
//        GsfTestCompilationInfo info = getInfo(fo);
//        ParserResult rpr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
//        assertNotNull(rpr);
//
//        PythonIndexer indexer = new PythonIndexer();
//        IndexDocumentFactory factory = new IndexDocumentFactoryImpl(/*info.getIndex(info.getPreferredMimeType())*/);
//        List<IndexDocument> result = indexer.index(rpr, factory);
//        assertNotNull(result);
//
//        String[] RELEVANT = { "int", "float", "long", "complex", "list", "dict", "tuple", "str", "unicode" };
//
//        // Generate a name document for Python inclusion into the extract_rst file
//        // This lists all the names for each class that we've defined
//        Map<String,Set<String>> classes = new HashMap<String,Set<String>>();
//
//        for (IndexDocument idoc : result) {
//            IndexDocumentImpl doc = (IndexDocumentImpl)idoc;
//
//            if (doc.overrideUrl.indexOf("stub_missing") != -1) {
//                continue;
//            }
//
//            List<String> members = new ArrayList<String>();
//            String cls = null;
//            for (int i = 0, n = doc.indexedKeys.size(); i < n; i++) {
//                String key = doc.indexedKeys.get(i);
//                if (PythonIndexer.FIELD_MEMBER.equals(key)) {
//                    String member = doc.indexedValues.get(i);
//                    int idx = member.indexOf(';');
//                    if (idx != -1) {
//                        member = member.substring(0, idx);
//                    }
//                    members.add(member);
//                } else if (PythonIndexer.FIELD_CLASS_NAME.equals(key)) {
//                    cls = doc.indexedValues.get(i);
//                }
//            }
//            if (members.size() > 0) {
//                assertNotNull(cls);
//                boolean found = false;
//                for (String s : RELEVANT) {
//                    if (s.equals(cls)) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    continue;
//                }
//                Set<String> memberSet = classes.get(cls);
//                if (memberSet == null) {
//                    memberSet = new HashSet<String>();
//                    classes.put(cls, memberSet);
//                }
//                //memberSet.addAll(members);
//                for (String member : members) {
//                    if (memberSet.contains(member)) {
//                        System.err.println("WARNING: Class " + cls + " already contains " + member);
//                    }
//                    memberSet.add(member);
//                }
//            }
//        }
//
//        List<String> classNames = new ArrayList<String>(classes.keySet());
//        Collections.sort(classNames);
//        StringBuilder sb = new StringBuilder();
//        for (String cls : classNames) {
//            Set<String> memberSet = classes.get(cls);
//            assertNotNull(memberSet);
//
//            List<String> members = new ArrayList<String>(memberSet);
//            Collections.sort(members);
//            sb.append("'");
//            sb.append(cls);
//            sb.append("': [");
//            boolean first = true;
//            for (String member : members) {
//                if (first) {
//                    first = false;
//                } else {
//                    sb.append(", ");
//                }
//                sb.append("'");
//                sb.append(member);
//                sb.append("'");
//            }
//            sb.append("],\n");
//        }
//
//        System.out.println(sb.toString());
//    }

}
