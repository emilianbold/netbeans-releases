
package org.netbeans.modules.javafx2.editor.actions;

import java.util.HashMap;
import java.util.Map;

public class FieldsWithTypeParams {

    private class SimpleTypeParam<T> {
        // something
    }
    
    private class BoundParam<T extends Comparable & Runnable> {
        
    }
    
    private class NestedTypeParam<K, V, T extends Map<K, V>> {
        
    }
    
    private class ExtendsTyped<T, V> extends HashMap<T, V> {
        
    }
    
    private NestedTypeParam<?, ?, Map<?, ?>> field;
}
