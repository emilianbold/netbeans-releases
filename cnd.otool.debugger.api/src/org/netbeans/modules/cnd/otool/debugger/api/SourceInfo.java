/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

/**
 *
 * @author Nikolay Koldunov
 */
public /*package*/ class SourceInfo {
    private String srcFile;
    private int line;

    public void set(String srcFile, int line) {
        this.srcFile = srcFile;
        this.line = line;
    }
    
    public String getFile() {
        return srcFile;
    }
    
    public int getLine() {
        return line;
    }
    
    @Override
    public String toString() {
        return "{file:"+ srcFile + ",line:" + line + "}"; // NOI18N
    }
}
