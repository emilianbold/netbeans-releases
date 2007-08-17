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
package org.netbeans.modules.java.source.usages;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types;
import static com.sun.tools.javac.code.TypeTags.*;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Name.Table;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javadoc.JavadocClassReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Lahoda
 */
public class SymbolClassReader extends JavadocClassReader {        
    
    public static void preRegister(final Context context, final boolean loadDocEnv) {
        context.put(classReaderKey, new Context.Factory<ClassReader>() {
            public ClassReader make() {
                return new SymbolClassReader(context, loadDocEnv);
            }
        });
    }
    
    private Symtab syms;
    private Table  table;
    private Types  types;
    private JavacTypes jTypes;
    private Log logger;
    private Source source;
    
    private char[] buffer;
    private int currentBufferIndex;
    
    private boolean readingClassSignature = false;
    private boolean readingEnclMethod = false;
    private List<Type> missingTypeVariables = List.nil();
    private List<Type> foundTypeVariables = List.nil();

    protected SymbolClassReader(Context context, boolean loadDocEnv) {
        super(context, loadDocEnv);
        syms = Symtab.instance(context);
        table = Table.instance(context);
        types = Types.instance(context);
        jTypes = JavacTypes.instance(context);
        logger = Log.instance(context);
        source = Source.instance(context);
        allowGenerics = true;
        allowVarargs = true;
        allowAnnotations = true;
        
        buffer = new char[127];
    }
    
    private void addCharToBuffer(char c) {
        if (currentBufferIndex >= buffer.length) {
            char[] newBuffer = new char[buffer.length * 2];
            
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
        buffer[currentBufferIndex++] = c;
    }
    
    private void clearBuffer() {
        currentBufferIndex = 0;
    }
    
    private int bufferLength() {
        return currentBufferIndex;
    }

    protected @Override void fillIn(ClassSymbol c) {
        Symbol oldCurrentOwner = currentOwner;
        try {
            fillInImpl(c);
        } finally {
            currentOwner = oldCurrentOwner;
        }
    }
    
    private boolean isSignatureFile (final JavaFileObject jfo) {
        if (jfo instanceof FileObjects.Base) {
            return ((FileObjects.Base)jfo).getExt().equals(FileObjects.SIG);
        }
        else {
            //Should never happen, but for sure
            return jfo.toUri().toString().endsWith('.'+FileObjects.SIG);
        }
    }
    
    private void fillInImpl(ClassSymbol c) {
        if (c.classfile == null ||
            c.classfile.getKind() != JavaFileObject.Kind.CLASS ||
            !isSignatureFile(c.classfile)) {
            //will throw a CompletionFailure
            super.fillIn(c);
            return ;
        }        
        
        try {
            final BufferedReader r = new BufferedReader(new InputStreamReader(c.classfile.openInputStream(), "UTF-8")); // NOI18N            
            try {
                int magic = r.read();
                if ( magic != 'G') {
                    throw new IOException ("Wrong signature file: " + c.classfile.toUri());
                }
                fillInFromSig(c, r);
                if (!missingTypeVariables.isEmpty() && !foundTypeVariables.isEmpty()) {
                    List<Type> missing = missingTypeVariables;
                    List<Type> found = foundTypeVariables;
                    missingTypeVariables = List.nil();
                    foundTypeVariables = List.nil();
                    ClassType ct = (ClassType)currentOwner.type;
                    ct.supertype_field =
                            types.subst(ct.supertype_field, missing, found);
                    ct.interfaces_field =
                            types.subst(ct.interfaces_field, missing, found);
                } else if (missingTypeVariables.isEmpty() !=
                        foundTypeVariables.isEmpty()) {
                    Name name = missingTypeVariables.head.tsym.name;
                    throw badClassFile("undecl.type.var", name);
                }
            } finally {
                try {
                    r.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                missingTypeVariables = List.nil();
                foundTypeVariables = List.nil();
            }                        
        } catch (IOException e) {
            throw new CompletionFailure(c, e.getMessage()).initCause(e);
        }
    }
    
    private void fillInFromSig(ClassSymbol c, Reader r) throws IOException {
        ClassType ct = (ClassType)c.type;

        // allocate scope for members
        c.members_field = new Scope(c);

        currentOwner = c;
        
        // prepare type variable table
        typevars = typevars.dup(currentOwner);
        if (ct.getEnclosingType().tag == CLASS) enterTypevars(ct.getEnclosingType());

        // read flags, or skip if this is an inner class
        long flags = readFlags(r) | Flags.FROMCLASS;
        c.flags_field = flags;

        int read = r.read();
        readingClassSignature = true;
        try {
            //read type variables:
            List<TypeVar> typevarsList;
            
            if (read == '<') {
                typevarsList = readTypeParamsWithName(r, c);
                read = r.read();
            } else {
                typevarsList = List.<TypeVar>nil();
            }
            
            ct.typarams_field = (List<Type>) ((List<?>) typevarsList);
            
            assert read == 'N' : read;
            
            Name name = readPlainNameIntoTable(r);
            
            if (name != c.flatname) {
                throw badClassFile("class.file.wrong.class",
                        name);
            }
            
            // handle enclosing method for anonymous and local classes
            if ((read = r.read()) != ';') {
                readEnclosingMethodAttr(r, c, read);
            }
            
            read = r.read();
            
            if (read != ';') {
                ct.supertype_field = readType(r, read); //XXX: Maybe add ct.supertype_field.tsym.erase () as the ClassReader does
            } else {
                //noType as the parent, for j.l.Object:
                ct.supertype_field = Type.noType;
            }
            
            if (ct.supertype_field.getClass() == Type.class) {
                if (!ct.supertype_field.isPrimitive()) {
                    System.err.println("PROBLEM");
                    System.err.println("c=" + c.flatname.toString());
                }
            }
            
            //read super interfaces:
            List<Type> superInterfaces = List.nil();
            
            while ((read = r.read()) != ';') {
                superInterfaces = superInterfaces.prepend(readType(r, read));   //XXX: Maybe add erase () as the ClassReader does
            }
            
            ct.interfaces_field = superInterfaces.reverse();
            
        } finally {
            readingClassSignature = false;
        }
        
        //handle innerclasses
        while ((read = r.read()) != ';') {
            assert read == 'N';
            
            String innerClassName = readPlainName(r);
            ClassSymbol innerClass = enterClass(findName(innerClassName), c);
            
            markInnerClassOwner(c, innerClass, readFlags(r));
        }
        
        read = r.read();
        
        assert read == '\n' || read == (-1) : c.classfile + ", char: " + ((char) read) + read;
        
        Symbol s;
        
        while ((s = readMember(r)) != null) {
            enterMember(c, s);
        }
        
        attachAnnotations(c, r);
        
        typevars = typevars.leave();
    }
    
    void readEnclosingMethodAttr(Reader r, ClassSymbol self, int read) throws IOException {
        readingEnclMethod = true;
        try{
            assert read == 'N' : read;
            // sym is a nested class with an "Enclosing Method" attribute
            // remove sym from it's current owners scope and place it in
            // the scope specified by the attribute
            //jlahoda: the owner may already be set correctly, do not remove from it in such a case:
            if (self.owner.kind == PCK) {
                self.owner.members().remove(self);
            }
            ClassSymbol c = enterClass(readPlainNameIntoTable(r));
            MethodSymbol m = null;
            read = r.read();
            if (read == 'N') {
                Name methodName = readPlainNameIntoTable(r);
                read = r.read();
                assert read == '(' : read;
                List<Type> paramsTypes = List.nil();
                while ((read = r.read()) != ')') {
                    paramsTypes = paramsTypes.prepend(readType(r, read));
                }
                paramsTypes = paramsTypes.reverse();
                read = r.read();
                assert read == '(' : read;
                List<Type> throwsTypes = List.nil();
                while ((read = r.read()) != ')') {
                    throwsTypes = throwsTypes.prepend(readType(r, read));
                }
                throwsTypes = throwsTypes.reverse();
                Type returnType = readType(r, r.read());
                assert (read = r.read()) == ';' : read;
                MethodType methodType = new MethodType(paramsTypes, returnType, throwsTypes, syms.methodClass);
                
                m = findMethod(methodName, methodType, c.members_field, self.flags());
                if (methodName != null && methodType != null && m == null)
                    throw badClassFile("bad.enclosing.method", self);
            }
            self.name = simpleBinaryName(self.flatname, c.flatname) ;
            self.owner = m != null ? m : c;
            if (self.name.len == 0)
                self.fullname = null;
            else
                self.fullname = ClassSymbol.formFullName(self.name, self.owner);
            
            if (m != null) {
                ((ClassType)self.type).setEnclosingType(m.type);
            } else if ((self.flags_field & STATIC) == 0) {
                ((ClassType)self.type).setEnclosingType(c.type);
            } else {
                ((ClassType)self.type).setEnclosingType(Type.noType);
            }
            enterTypevars(self);
            if (!missingTypeVariables.isEmpty()) {
                ListBuffer<Type> typeVars =  new ListBuffer<Type>();
                for (Type typevar : missingTypeVariables) {
                    typeVars.append(findTypeVar(typevar.tsym.name));
                }
                foundTypeVariables = typeVars.toList();
            } else {
                foundTypeVariables = List.nil();
            }
        } finally {
            readingEnclMethod = false;
        }
    }
    
    private MethodSymbol findMethod(Name name, MethodType type, Scope scope, long flags) {
        if (name == null || type == null)
            return null;

        for (Scope.Entry e = scope.lookup(name); e.scope != null; e = e.next())
            if (e.sym.kind == MTH && isSameBinaryType(e.sym.type.asMethodType(), type))
                return (MethodSymbol)e.sym;

        if (name != table.init)
            // not a constructor
            return null;
        if ((flags & INTERFACE) != 0)
            // no enclosing instance
            return null;
        if (type.getParameterTypes().isEmpty())
            // no parameters
            return null;

        // A constructor of an inner class.
        // Remove the first argument (the enclosing instance)
        type = new MethodType(type.getParameterTypes().tail,
                                 type.getReturnType(),
                                 type.getThrownTypes(),
                                 syms.methodClass);
        // Try searching again
        return findMethod(name, type, scope, flags);
    }
    
    private void markInnerClassOwner(ClassSymbol owner, ClassSymbol innerClass, long flags) {
        innerClass.complete();
        innerClass.flags_field = flags | FROMCLASS;
        if ((innerClass.flags_field & STATIC) == 0) {
            ((ClassType)innerClass.type).setEnclosingType(owner.type);
            if (innerClass.erasure_field != null)
                ((ClassType)innerClass.erasure_field).setEnclosingType(types.erasure(owner.type));
        }
        enterMember(owner, innerClass);
    }

    private List<TypeVar> readTypeParamsWithName(Reader r, Symbol owner) throws IOException {
        List<TypeVar> result = List.<TypeVar>nil();
        int read;
        
        while ((read =r.read()) != '>') {
            clearBuffer();
            
            addCharToBuffer((char) read);
            
            while ((read = r.read()) != ':') {
                addCharToBuffer((char) read);
            }
            
            Name typeName = findName(buffer, bufferLength());
            
            TypeVar tvar = null;
            List<Type> last = null;
            for (List<Type> l = missingTypeVariables; l.nonEmpty(); l = l.tail) {
                if (typeName == l.head.tsym.name) {
                    tvar = (TypeVar)l.head;
                    if (last != null)
                        last.tail = l.tail;
                    else
                        missingTypeVariables = l.tail;
                    break;
                }
                last = l;
            }
            if (tvar == null)
                tvar = new TypeVar(typeName, owner, syms.botType);
            
            typevars.enter(tvar.tsym);
            
            List<Type> bounds = List.<Type>nil();
            
            while ((read = r.read()) != ';') {
                bounds = bounds.prepend(readType(r, read));
            }
            
            bounds = bounds.reverse();
            
            types.setBounds(tvar, bounds, null);
            
            result = result.prepend(tvar);
        }
        
        return result.reverse();
    }
    
    private java.util.List<? extends TypeMirror> readTypeParams(Reader r) throws IOException {
        java.util.List<TypeMirror> params = new java.util.ArrayList<TypeMirror>();
        int read;
        
        while ((read = r.read()) != '>') {
            params.add(readType(r, read));
        }
        
        return params;
    }
    
    private String readPlainName(Reader r) throws IOException {
        clearBuffer();
        
        int read;
        
        while ((read = r.read()) != ';') {
            addCharToBuffer((char) read);
        }
        
        return new String(buffer, 0, bufferLength());
    }
    
    private String readName(Reader r) throws IOException {
        int read = r.read();
        
        assert read == 'N' : (char) read;
        
        return readPlainName(r);
    }
    
    private Name readPlainNameIntoTable(Reader r) throws IOException {
        clearBuffer();
        
        int read;
        
        while ((read = r.read()) != ';') {
            addCharToBuffer((char) read);
        }
        
        return findName(buffer, bufferLength());
    }
    
    private Name readNameIntoTable(Reader r) throws IOException {
        int read = r.read();
        
        assert read == 'N' : (char) read;
        
        return readPlainNameIntoTable(r);
    }
    
    private long readPlainFlags(Reader r) throws IOException {
        return Long.parseLong(readPlainName(r), 16);
    }
    
    private long readFlags(Reader r) throws IOException {
        int read = r.read();
        
        assert read == 'M';
        
        return readPlainFlags(r);
    }
    
    private Type readType(Reader r, int read) throws IOException {
        switch (read) {
            case 'Z':
                return syms.booleanType;
            case 'B':
                return syms.byteType;
            case 'S':
                return syms.shortType;
            case 'I':
                return syms.intType;
            case 'J':
                return syms.longType;
            case 'C':
                return syms.charType;
            case 'F':
                return syms.floatType;
            case 'D':
                return syms.doubleType;
            case 'V':
                return syms.voidType;
            case 'L':
                return readReferenceType(r, null);
            case '[':
                return (Type) jTypes.getArrayType(readType(r, r.read()));
            case 'R':
                return new ErrorType(readPlainNameIntoTable(r), syms.noSymbol);
            case 'Q':
                return findTypeVar(readPlainNameIntoTable(r));
            case '+':
                return new WildcardType(readType(r, r.read()), BoundKind.EXTENDS, syms.boundClass);
            case '-':
                return new WildcardType(readType(r, r.read()), BoundKind.SUPER, syms.boundClass);
            case '?':
                return new WildcardType(syms.objectType, BoundKind.UNBOUND, syms.boundClass);
            default:
                throw new IllegalArgumentException("Completing: " + ((ClassSymbol) currentOwner).flatname.toString() + ", read=" + ((char) read) + ":" + read);
        }
    }
    
    /** Find type variable with given name in `typevars' scope.
     */
    Type findTypeVar(Name name) {
        Scope.Entry e = typevars.lookup(name);
        if (e.scope != null) {
            return e.sym.type;
        } else {
            if (readingClassSignature || readingEnclMethod) {
                // While reading the class attribute, the supertypes
                // might refer to a type variable from an enclosing element
                // (method or class).
                // If the type variable is defined in the enclosing class,
                // we can actually find it in
                // currentOwner.owner.type.getTypeArguments()
                // However, until we have read the enclosing method attribute
                // we don't know for sure if this owner is correct.  It could
                // be a method and there is no way to tell before reading the
                // enclosing method attribute.
                TypeVar t = new TypeVar(name, currentOwner, syms.botType);
                missingTypeVariables = missingTypeVariables.prepend(t);
                // System.err.println("Missing type var " + name);
                return t;
            }
            throw badClassFile("undecl.type.var", name);
        }
    }

    private Name findName(CharSequence s) {
        char[] data = s.toString().toCharArray();
        
        return findName(data, data.length);
    }
    
    private Name findName(char[] data, int len) {
        return Name.fromChars(table, data, 0, len);
    }

    private Type readReferenceType(Reader r, Type outer) throws IOException {
        clearBuffer();
        
        int read;
        
        while (";<".indexOf(read = r.read()) == (-1)) {
            addCharToBuffer((char) read);
        }
        
        Name name = findName(buffer, bufferLength());
        ClassSymbol symbol = outer != null ? enterClass(name, outer.tsym) : enterClass(name);
        
        Symbol oldCurrentOwner = currentOwner;
        java.util.List<? extends TypeMirror> typeParams = read == '<' ? readTypeParams(r) : null;//Collections.<TypeMirror>emptyList();
        Type result;
        
        if (typeParams != null) {
            result = new ClassType(outer != null ? outer : Type.noType, List.from(typeParams.toArray(new Type[0])), symbol) {
                boolean completed = false;
                public Type getEnclosingType() {
                    if (!completed) {
                        completed = true;
                        tsym.complete();
                        Type enclosingType = tsym.type.getEnclosingType();
                        if (enclosingType != Type.noType) {
                            List<Type> typeArgs =
                                    super.getEnclosingType().allparams();
                            List<Type> typeParams =
                                    enclosingType.allparams();
                            if (typeParams.length() != typeArgs.length()) {
                                // no "rare" types
                                super.setEnclosingType(types.erasure(enclosingType));
                            } else {
                                super.setEnclosingType(types.subst(enclosingType,
                                        typeParams,
                                        typeArgs));
                            }
                        } else {
                            super.setEnclosingType(Type.noType);
                        }
                    }
                    return super.getEnclosingType();
                }
                public void setEnclosingType(Type outer) {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            result = outer != null ? new ClassType(outer, List.<Type>nil(), symbol) : symbol.erasure(types);
        }
        
        currentOwner = oldCurrentOwner;
        
        if (read == '<') {
            read = r.read();
            if (read == '$') {
                return readReferenceType(r, result);
            }
            assert read == ';' : (char) read;
        }
        
        return result;
    }
    
    private Symbol readMember(Reader r) throws IOException {
        int read = r.read();
        
        if ('E' == read) {
            return readExecutableMember(r);
        }
        
        if ('A' == read) {
            return readField(r);
        }
        
        if ('W' == read)
            return null;
        
        if (read == (-1))
            return null;
        
        throw new IllegalArgumentException("Unknown type: " + read);
    }

    private Symbol readExecutableMember(Reader r) throws IOException {
        long flags = readFlags(r);
        
        typevars = typevars.dup(currentOwner);
        
        MethodSymbol method = new MethodSymbol(flags, null, null, currentOwner);
        
        List<TypeVar> typevarsList = null;
        int read = r.read();                
        
        if (read == '<') {
            typevarsList = readTypeParamsWithName(r, method);
            
            read = r.read();
        }
        
        assert read == 'N' : read;
        
        method.name = readPlainNameIntoTable(r);
        
        read = r.read();
        
        assert read == '(' : read;
        
        List<VarSymbol> params = List.nil();
        List<Type> paramsTypes = List.nil();
        
        while ((read = r.read()) != ')') {
            assert read == 'M';
            
            long argFlags = readPlainFlags(r);
            Type argType = readType(r, r.read());
            Name argName = readNameIntoTable(r);
            
            paramsTypes = paramsTypes.prepend(argType);
            
            params = params.prepend(new VarSymbol(argFlags, argName, argType, null));
        }
        
        paramsTypes = paramsTypes.reverse();
        params = params.reverse();
        read = r.read();
        
        assert read == '(' : read;
        
        List<Type> throwsTypes = List.nil();
        
        while ((read = r.read()) != ')') {
            throwsTypes = throwsTypes.prepend(readType(r, read));
        }
        
        Type returnType = readType(r, r.read());
        Type methodType = new MethodType(paramsTypes, returnType, throwsTypes.reverse(), syms.methodClass/*???*/);;
        
        if (typevarsList != null) {
            methodType = new ForAll((List<Type>) ((List<?>) typevarsList), methodType);
        }
        
        method.type = methodType;
        
        method.params = params;
        
        //change owner for all arguments:
        for (VarSymbol s : params) {
            s.owner = method;
        }
        
        attachAnnotations(method, r);

        if ((read = r.read()) != ';') {
            annotate.later(new AnnotationDefaultCompleter(method, readAnnotationValue(r, read)));
        }
        
        typevars = typevars.leave();
        
        read = r.read();
        
        while (read == ' ')
            read = r.read();
        
        assert read == '\n' || read == (-1) : read;
        
        return method;
    }
    
    private Symbol readField(Reader r) throws IOException {
        long flags = readFlags(r);
        
        Type type = readType(r, r.read());
        Name name = readNameIntoTable(r);
        
        Object constantValue = readConstantField(r);
        
        VarSymbol field = new VarSymbol(flags, name, type, currentOwner);
        
        field.setData(constantValue);
        
        attachAnnotations(field, r);
        
        int read = r.read();
        
        while (read == ' ')
            read = r.read();
        
        assert read == '\n' || read == (-1) : read;
        
        return field;
    }
    
    private String readEscapedString(Reader r) throws IOException {
        String value = readPlainName(r);
        StringBuffer result = new StringBuffer();
        
        for (int cntr = 0; cntr < value.length(); cntr++) {
            char c = value.charAt(cntr);
            
            if (c == '\\') {
                char next = value.charAt(cntr + 1);
                
                switch (next) {
                    case '0': case '1': case '2': case '3':
                    case '4': case '5': case '6': case '7':
                    case '8': case '9':
                        result.append((char) Integer.parseInt(value.substring(cntr + 1, cntr + 5), 16));
                        cntr += 4;
                        break;
                    case 'a':
                        result.append('@');
                        cntr++;
                        break;
                    case 'b':
                        result.append(';');
                        cntr++;
                        break;
                    case '\\':
                        result.append('\\');
                        cntr++;
                        break;
                    default:
                        throw new IllegalStateException("Unsupported escape: " + next);
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private Object readConstantField(Reader r) throws IOException {
        Object result = null;
        int read = r.read();
        
        switch (read) {
            case 'Z': result = Boolean.parseBoolean(readPlainName(r)) ? Integer.valueOf(1) : Integer.valueOf(0); break;
            case 'B': result = Integer.valueOf(readPlainName(r)); break;
            case 'S': result = Integer.valueOf(readPlainName(r)); break;
            case 'I': result = Integer.valueOf(readPlainName(r)); break;
            case 'J': result = Long.valueOf(readPlainName(r)); break;
            case 'C': result = new Integer(Character.valueOf(readEscapedString(r).charAt(0))); break;
            case 'F': result = Float.valueOf(readPlainName(r)); break;
            case 'D': result = Double.valueOf(readPlainName(r)); break;
            case 'L': result = readEscapedString(r); break;
            case 'X':
                read = r.read();
                
                assert read == ';';
                break;
            default:
                throw badClassFile("Unknown constant type: " + (char) read); //TODO: key!
        }
        
        return result;
    }
    
    private void attachAnnotations(Symbol sym, Reader r) throws IOException {
        List<CompoundAnnotationProxy> annotations = readAnnotations(r);
        
        if (!annotations.isEmpty()) {
            annotate.later(new AnnotationCompleter(sym, annotations));
        }
    }
    
    private List<CompoundAnnotationProxy> readAnnotations(Reader r) throws IOException {
        List<CompoundAnnotationProxy> attributes = List.nil();
        int read;
        
        while ((read = r.read()) != ';') {
            attributes = attributes.prepend(readAnnotation(r, read));
        }
        
        return attributes.reverse();
    }
    
    private CompoundAnnotationProxy readAnnotation(Reader r, int read) throws IOException {
        Type annotationType = readType(r, read);
        
        ListBuffer<Pair<Name,Attribute>> values = new ListBuffer<Pair<Name,Attribute>>();
        
        while ((read = r.read()) != ';') {
            assert read == 'N';
            
            Name attributeName = readPlainNameIntoTable(r);
            
            Attribute a = readAnnotationValue(r, r.read());
            values = values.append(new Pair<Name, Attribute>(attributeName, a));
        }
        
        return new CompoundAnnotationProxy(annotationType, values.toList());
    }
    
    private Attribute readEnum(Reader r) throws IOException {
        Type enumType = readType(r, r.read());
        Name constantName = readNameIntoTable(r);
        VarSymbol constant = null;
        
        for (Scope.Entry e = ((ClassType) enumType).tsym.members().lookup(constantName); e.scope != null; e = e.next()) {
            if (e.sym.kind == VAR) {
                constant = (VarSymbol)e.sym;
                break;
            }
        }
        if (constant == null) {
            logger.error("unknown.enum.constant", currentClassFile, enumType, constant);
            return new Attribute.Error(enumType);
        } else {
            return new Attribute.Enum(enumType, constant);
        }
    }
    
    private Attribute readAnnotationValue(Reader r, int read) throws IOException {
        switch (read) {
            case 'Z': return new Attribute.Constant(syms.booleanType, Boolean.parseBoolean(readPlainName(r)) ? Integer.valueOf(1) : Integer.valueOf(0));
            case 'B': return new Attribute.Constant(syms.byteType, Integer.parseInt(readPlainName(r)));
            case 'S': return new Attribute.Constant(syms.shortType, Integer.parseInt(readPlainName(r)));
            case 'I': return new Attribute.Constant(syms.intType, Integer.parseInt(readPlainName(r)));
            case 'J': return new Attribute.Constant(syms.longType, Long.parseLong(readPlainName(r)));
            case 'C': return new Attribute.Constant(syms.charType, Integer.valueOf(Character.valueOf(readEscapedString(r).charAt(0))));
            case 'F': return new Attribute.Constant(syms.floatType, Float.parseFloat(readPlainName(r)));
            case 'D': return new Attribute.Constant(syms.doubleType, Double.parseDouble(readPlainName(r)));
            case 'L': return new Attribute.Constant(syms.stringType, readEscapedString(r));
            case 'O': return new EnumAttributeProxy(readType(r, r.read()), readNameIntoTable(r));
            case 'P': return readAnnotation(r, r.read());
            case '[': 
                ListBuffer<Attribute> items = new ListBuffer<Attribute>();
                
                while ((read = r.read()) != ';') {
                    items.append(readAnnotationValue(r, read));
                }
                
                return new ArrayAttributeProxy(items.toList());
            case 'Y':
                return new Attribute.Class(types, readType(r, r.read()));
        }
        
        throw new IllegalStateException("Completing: " + ((ClassSymbol) currentOwner).flatname.toString() + ", read=" + ((char) read) + ":" + read);
    }

    //for tests:
    protected @Override void includeClassFile(PackageSymbol p, JavaFileObject file) {
        super.includeClassFile(p, file);
    }

}
