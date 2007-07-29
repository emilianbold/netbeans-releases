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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints.spi;

import java.util.List;
import org.netbeans.api.gsf.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 * Wrapper around org.netbeans.spi.editor.hints.ErrorDescription
 * 
 * @author Tor Norbye
 */
public class Description {
    private final String description;
    private final List<Fix> fixes;
    private final FileObject file;
    private final OffsetRange range;
    private final Rule rule;
    private int priority;
    
    public Description(Rule rule, String description, FileObject file, OffsetRange range, List<Fix> fixes, int priority) {
        this.rule = rule;
        this.description = description;
        this.file = file;
        this.range = range;
        this.fixes = fixes;
        this.priority = priority;
    }
    
    public Rule getRule() {
        return this.rule;
    }

    public String getDescription() {
        return description;
    }

    public FileObject getFile() {
        return file;
    }

    public List<Fix> getFixes() {
        return fixes;
    }

    public OffsetRange getRange() {
        return range;
    }
    
    public int getPriority() {
        return priority;
    }
}
