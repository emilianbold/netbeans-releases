/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.source.Source;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Martin
 */
final class SourcePosition {
    
    private static final Map<Source, Long> sourceId = new WeakHashMap<>();
    private static long nextId = 0;
    
    final long id;
    final String name;
    final String path;
    final int line;
    final String code;

    public SourcePosition(Source source, String name, String path, int line, String code) {
        this.id = getId(source);
        this.name = name;
        this.path = path;
        this.line = line;
        this.code = code;
    }

    private static synchronized long getId(Source s) {
        Long id = sourceId.get(s);
        if (id == null) {
            id = new Long(nextId++);
            sourceId.put(s, id);
        }
        return id;
    }
    
}
