/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Vladimir Kvashin
 */
public class TestKeyFactory extends KeyFactory {

    private static final int TEST_KEY_HANDLER = 1;
    
    @Override
    protected SelfPersistent createObject(int handler, DataInput stream) throws IOException {
        assert handler == TEST_KEY_HANDLER;
        return new TestKey(stream);
    }

    @Override
    protected int getHandler(Object object) {
        assert object instanceof TestKey;
        return TEST_KEY_HANDLER;
    }

    @Override
    public Key readKey(DataInput aStream) throws IOException {
        return new TestKey(aStream.readUTF());
    }

    @Override
    public void readKeyCollection(Collection<Key> aCollection, DataInput aStream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeKey(Key aKey, DataOutput aStream) throws IOException {
        assert aKey instanceof TestKey;
        ((TestKey) aKey).write(aStream);
    }

    @Override
    public void writeKeyCollection(Collection<Key> aCollection, DataOutput aStream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
