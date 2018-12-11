/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * StackTraceTranslator.java
 *
 * Created on August 19, 2005, 9:42 PM
 *
 */
package org.netbeans.modules.j2me.common.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.classfile.ByteCodes;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.Method;

/**
 *
 */
public class StackTraceTranslator
{
    
    private final ClassLoader cl;
    private final Map<ClassName,ClassFile> parsedClasses;
    
    public StackTraceTranslator(File cpRoot, String[] classPath)
    {
        ArrayList<URL> urls = new ArrayList<URL>();
        for (int i=0; i<classPath.length; i++) addURL(urls, cpRoot, classPath[i]);
        cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
        parsedClasses = new HashMap<ClassName,ClassFile>();
    }
    
    public String translate(final String stackTrace, boolean isBci )
    {
        final String splitTrace[] = stackTrace.split("[\n\r]+", -1); //NOI18N
        String clNames[] = new String[splitTrace.length+1];
        String mNames[] = new String[splitTrace.length+1];
        int offsets[] = new int[splitTrace.length];
        boolean catches[] = new boolean[splitTrace.length];
        String result[] = new String[splitTrace.length];
        if ( isBci ){
            fillArraysBci( splitTrace, clNames , mNames , offsets );
        }
        else {
            fillArrays( splitTrace, clNames , mNames , offsets , catches );
        }

        String sig[] = new String[]{null};
        for (int i=splitTrace.length - 1 ; i>=0; i--)
        {
            if (clNames[i] == null)
            {
                sig[0] = null;
                result[i] = splitTrace[i];
            }
            else
            {
                final Method m = findMethod(clNames[i], mNames[i], sig[0], 
                        i > 0 ? clNames[i-1] : null, i > 0 ? mNames[i-1] : null,
                        offsets[i], sig);
                final int ln = m ==null ? -1 : getLineNumber(m.getCode(), offsets[i]);
                if (ln < 0) {
                    if ( isBci ){
                        result[i] = "\tat " + splitTrace[i].substring(
                            splitTrace[i].indexOf(clNames[i])); //NOI18N
                    }
                    else {
                        result[i] = splitTrace[i];
                    }
                }
                else {
                        result[i] = (catches[i]&&!isBci ? "[catch] at " : "\tat ") +
                            clNames[i] + "." + mNames[i] + "(" +
                            m.getClassFile().getSourceFileName() + ":" +
                            String.valueOf(ln) + ")"; //NOI18N
                }
            }
        }
        final StringBuffer sb = new StringBuffer();
        for (int i=0; i<result.length; i++) sb.append(result[i]).append('\n');
        return sb.toString();
    }

    private void fillArrays(String[] trace, String[] clNames,
            String[] mNames, int[] offsets, boolean[] catches)
    {
        for (int i=0; i<trace.length; i++)
        {
            final String st = trace[i].trim();
            final int at = st.indexOf("at "); //NOI18N
            final int dot = st.lastIndexOf('.'); //NOI18N
            final int lb = st.lastIndexOf("(+"); //NOI18N
            final int rb = st.lastIndexOf(')'); //NOI18N
            if ((st.startsWith("[catch]") || at == 0) && dot > 3 && lb > 5 && rb > 8) try
            {
                clNames[i] = st.substring(at + 3, dot).trim();
                mNames[i] = st.substring(dot + 1, lb).trim();
                offsets[i] = Integer.parseInt(st.substring(lb + 2, rb).trim());
                catches[i] = at > 0;
            }
            catch (NumberFormatException nfe)
            {}
        }
    }

    private void fillArraysBci(String[] trace, String[] clNames,
            String[] mNames, int[] offsets)
    {
        for (int i=0; i<trace.length; i++)
        {
            final String st = trace[i].trim();
            final int at = st.indexOf("- "); //NOI18N
            final int dot = st.lastIndexOf('.'); //NOI18N
            final int brackets = st.lastIndexOf("(),"); //NOI18N
            final int bci = st.lastIndexOf("bci="); //NOI18N
            if ( dot > 2 && brackets > 4 && bci > 7) try
            {
                clNames[i] = st.substring(at + 2, dot).trim();
                mNames[i] = st.substring(dot + 1, brackets).trim();
                // bci is offset with in "+num" plus three
                offsets[i] = Integer.parseInt(st.substring(bci+4).trim())+3;
            }
            catch (NumberFormatException nfe)
            {}
        }
    }
    
    private static void addURL(final List<URL> l, final File root, final String path)
    {
        if (path == null) return;
        File f = new File(path);
        if (root != null && (!f.exists() || !f.isAbsolute())) f = new File(root, path);
        try
        {
            if (f.exists()) l.add(f.toURI().toURL());
        }
        catch (MalformedURLException mue)
        {}
    }
    
    private ClassFile getClass(final ClassName className)
    {
        if (parsedClasses.containsKey(className)) return parsedClasses.get(className);
        ClassFile cf = null;
        final InputStream is = cl.getResourceAsStream(className.getInternalName() + ".class"); //NOI18N
        if (is != null) try
        {
            cf = new ClassFile(is);
        }
        catch (IOException ioe)
        {}
        parsedClasses.put(className, cf);
        return cf;
    }
    
    private int getLineNumber(final Code code, int offset)
    {
        //#170792
        if (code == null) {
            return -1;
        }
        offset -= 3;
        //#214786
        final byte byteCodes[] = code.getByteCodes();
        if (offset < 0 || byteCodes == null || offset >= byteCodes.length) {
            return -1;
        }
        final int lineTable[] = code.getLineNumberTable();
        if (lineTable == null || lineTable.length == 0) {
            return -1;
        }
        for (int i=2; i<lineTable.length; i+=2)
        {
            if (lineTable[i] > offset) {
                return lineTable[i-1];
            }
        }
        return lineTable[lineTable.length - 1];
    }
    
    private Method findMethod(final String className, final String methodName,
            final String expectedSinature, final String expectedClassCall,
            final String expectedMethodCall, final int atOffset,
            String[] expCallMethodSignature)
    {
        if (expCallMethodSignature != null && expCallMethodSignature.length == 1) {
            expCallMethodSignature[0] = null;
        }
        final ClassFile cf = getClass(getClassName(className));
        if (cf == null) return null;
        if (expectedSinature != null)
        {
            final Method m = cf.getMethod(methodName, expectedSinature);
            if (m != null && checkMethod(cf, m, expectedClassCall, 
                    expectedMethodCall, atOffset, expCallMethodSignature))
            {
                return m;
            }
        }
        for ( final Method m : (Collection<Method>)cf.getMethods() ) {
            if (m.getName().equals(methodName) && checkMethod(cf, m, expectedClassCall,
                    expectedMethodCall, atOffset, expCallMethodSignature))
            {
                return m;
            }
        }
        return null;
    }
    
    private boolean checkMethod(final ClassFile cf, final Method method, final String expectedClassCall, final String expectedMethodCall, final int atOffset, String[] expCallMethodSignature)
    {
        final Set<String> expectedParents = getParents(expectedClassCall);
        if (expectedParents.isEmpty() || expectedMethodCall == null) return true;
        final CPMethodInfo mi = getMethodCall(cf, method.getCode(), atOffset);
        if (mi != null && expectedParents.contains(mi.getClassName().getInternalName())
                && expectedMethodCall.equals(mi.getName()))
        {
            if (expCallMethodSignature != null && expCallMethodSignature.length == 1)
            {
                expCallMethodSignature[0] = mi.getDescriptor();
            }
            return true;
        }
        return false;
    }
    
    private ClassName getClassName(final String name)
    {
        return name == null ? null : ClassName.getClassName(name.replace('.', '/'));
    }
    
    private Set<String> getParents(final String expectedClassCall)
    {
        final HashSet<String> hs = new HashSet<String>();
        collectParents(getClassName(expectedClassCall), hs);
        return hs;
    }
    
    private void collectParents(final ClassName name, final Set<String> s)
    {
        if (name == null) return ;
        final ClassFile cf = getClass(name);
        if (cf == null) return;
        s.add(cf.getName().getInternalName());
        collectParents(cf.getSuperClass(), s);
        for (ClassName cn : (Collection<ClassName>)cf.getInterfaces() ) {
        	collectParents(cn, s);
        }
    }
    
    private CPMethodInfo getMethodCall(final ClassFile cf, final Code code, int offset)
    {
        offset -=3;
        if (offset < 0 || code == null || offset >= code.getByteCodes().length) return null;
        final byte bc[] = code.getByteCodes();
        final int b = bc[offset] & 0xff;
        if (b != ByteCodes.bc_invokespecial && b != ByteCodes.bc_invokestatic && b != ByteCodes.bc_invokevirtual)
        {
            offset -=2;
            if (offset < 0 || (bc[offset] & 0xff) != ByteCodes.bc_invokeinterface) return null;
        }
        final int cpIndex = (bc[offset+1] & 0xff << 8) + (bc[offset+2] & 0xff);
        try
        {
            final CPEntry e = cf.getConstantPool().get(cpIndex);
            if (e instanceof CPMethodInfo) return (CPMethodInfo)e;
        }
        catch (IndexOutOfBoundsException ioobe)
        {}
        return null;
    }
    
}

