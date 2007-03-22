package org.netbeans.modules.cnd.repository.testbench.sfs;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;


class TestKey implements Key {
    
    private String key;
    
    public TestKey(String key) {
	this.key = key;
    }
    
    public String getAt(int level) {
	return key;
    }
    
    public int getDepth() {
	return 1;
    }
    
    public PersistentFactory getPersistentFactory() {
	return TestFactory.instance();
    }
    
    public int getSecondaryAt(int level) {
	return 0;
    }
    
    public int getSecondaryDepth() {
	return 0;
    }
    
    public int hashCode() {
	return key.hashCode();
    }
    
    public boolean equals(Object obj) {
	if (obj != null && TestKey.class == obj.getClass()) {
	    return this.key.equals(((TestKey) obj).key);
	}
	return false;
    }    
}
