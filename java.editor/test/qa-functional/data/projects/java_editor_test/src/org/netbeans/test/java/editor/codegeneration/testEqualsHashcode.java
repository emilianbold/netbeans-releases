
package org.netbeans.test.java.editor.codegeneration;

import java.util.List;

public class testEqualsHashcode {
    
    String a;
    
    int b;
    
    List<String> c;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final testEqualsHashcode other = (testEqualsHashcode) obj;
        if (this.a != other.a && (this.a == null || !this.a.equals(other.a))) {
            return false;
        }
        if (this.c != other.c && (this.c == null || !this.c.equals(other.c))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
    
    
}
