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

package org.netbeans.modules.refactoring.java.ui.tree;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class ElementGripFactory {

    private static ElementGripFactory instance;
    private WeakHashMap <FileObject, Interval> map = new WeakHashMap();
    
    /**
     * Creates a new instance of ElementGripFactory
     */
    private ElementGripFactory() {
    }
    
    public static ElementGripFactory getDefault() {
        if (instance == null) {
            instance = new ElementGripFactory();
        }
        return instance;
    }
    
    public void cleanUp() {
        map.clear();
    }
    
    public ElementGrip get(FileObject fileObject, int position) {
        Interval start = map.get(fileObject);
        if (start==null)
            return null;
        try {
            return (ElementGrip) start.get(position).item;
        } catch (RuntimeException e) {
            return start.item;
        }
    }
    
    public ElementGrip getParent(ElementGrip el) {
        Interval start = map.get(el.getFileObject());
        return (ElementGrip) start.getParent(el);
    }

    public void put(FileObject parentFile, TreePath tp, CompilationInfo info) {
        Interval root = map.get(parentFile);
        Interval i = Interval.createInterval(tp,info, root,null, parentFile);
        if (i!=null) {
            map.put(parentFile,i);
        }
    }
    
    private static class Interval {
        long from=-1,to=-1;
        Set<Interval> subintervals= new HashSet();
        ElementGrip item = null;
        
        Interval get(long position) {
            if (from<=position && to >=position) {
                for (Interval o:subintervals) {
                    Interval ob = o.get(position);
                    if (ob!=null)
                        return ob;
                }
                return this;
            }
            return null;
        }
        
        ElementGrip getParent(ElementGrip eh) {
            for (Interval i:subintervals) {
                if (i.item.equals(eh)) {
                    return this.item;
                } else {
                    ElementGrip e = i.getParent(eh);
                    if (e!=null) {
                        return e;
                    }
                }
            }
            return null;
        }
        
        public static Interval createInterval(TreePath tp, CompilationInfo info, Interval root, Interval p, FileObject parentFile) {
                Tree t = tp.getLeaf();
                long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
                long end = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);
                Element current = info.getTrees().getElement(tp);
                Tree.Kind kind = tp.getLeaf().getKind();
                if (kind != Tree.Kind.CLASS && kind != Tree.Kind.METHOD) {
                    if (tp.getParentPath()==null || tp.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                        //xxx: rather workaround. should be fixed better.
                        return null;
                    } else {
                        return createInterval(tp.getParentPath(), info, root, p, parentFile);
                    }
                }
                Interval i = null;
                if (root != null) {
                    Interval o = root.get(start);
                    if (o!= null && current!=null && current.equals(o.item.resolveElement(info))) {
                        if (p!=null)
                            o.subintervals.add(p);
                        return null;
                    }
                }
                if (i==null)
                    i = new Interval();
                if (i.from != start) {
                    i.from = start;
                    i.to = end;
                    ElementGrip currentHandle2 = new ElementGrip(tp, info);
                    i.item = currentHandle2;
                } 
                if (p!=null) {
                    i.subintervals.add(p);
                }
                if (tp.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                    return i;
                }
                return createInterval(tp.getParentPath(), info, root, i, parentFile);
            }
        }
    }
    
