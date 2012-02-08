package org.netbeans.modules.java.editor.semantic.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface ExtensionMethod<T> extends List<T> {
    
    extension void sort(Comparator<T> c) default Collections.<T>sort;

}
