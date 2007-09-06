/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.ProjectSettingsValidatorKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * When project restored, we should validate whether settings for each item 
 * (include path, system and user macros) have changed since storing project in persistence.
 * This class performes this validation.
 *
 * Its lifecycle supposed to be short. 
 * It is created (and used) twice: 
 *
 * 1) when project is shutting down, it is created, then  storeSettings() is called
 *
 * 2) when project is restored, it is created, restoreSettings() is called,
 * the isChanged() is called for each native file item
 *
 * @author Vladimir Kvashin
 */
public class ProjectSettingsValidator {
    
    private static final boolean TRACE = Boolean.getBoolean("cnd.modelimpl.validator.trace");
    
    public ProjectSettingsValidator(ProjectBase csmProject) {
	this.csmProject = csmProject;
	Object platformProject = csmProject.getPlatformProject();
	if( platformProject instanceof NativeProject ) {
	    nativeProject = (NativeProject) platformProject;
	}
    }
    
    public void storeSettings() {
	if( nativeProject == null ) {
	    return;
	}
	long time = 0;
	if( TraceFlags.TIMING ) {
	    System.err.printf("ProjectSettingsValidator.storeSettings for %s\n", csmProject.getName());
	    time = System.currentTimeMillis();
	}
	data = new Data();
	updateMap(nativeProject.getAllHeaderFiles());
	updateMap(nativeProject.getAllSourceFiles());
	Key key = new ProjectSettingsValidatorKey(csmProject.getUniqueName());
	RepositoryUtils.put(key, data);
	if( TraceFlags.TIMING ) {
	    time = System.currentTimeMillis() - time;
	    System.err.printf("ProjectSettingsValidator.storeSettings for %s took %d ms\n", csmProject.getName(), time);
	}
    }
    
    private void updateMap(List<NativeFileItem> items) {
	for( NativeFileItem item : items ) {
	    long crc = calculateCrc(item);
	    data.setCrc(item.getFile().getAbsolutePath(), crc);
	}
    }
    
    public void restoreSettings() {
	if( nativeProject == null ) {
	    return;
	}
	Key key = new ProjectSettingsValidatorKey(csmProject.getUniqueName());
	data = (Data) RepositoryUtils.get(key);
	assert data != null;
    }
    
    public boolean exists(FileImpl fileImpl) {
	return data.exists(fileImpl.getAbsolutePath());
    }
    
    public boolean arePropertiesChanged(NativeFileItem item) {
	if( nativeProject == null ) {
	    return false;
	}
	assert data != null;
	long savedCrc = data.getCrc(item.getFile().getAbsolutePath());
	long currentCrc = calculateCrc(item);
	if( TRACE ) System.err.printf("arePropertiesChanged %s OLD=%d CUR=%d %b\n", item.getFile().getName(), savedCrc, currentCrc, (savedCrc != currentCrc));
	return savedCrc != currentCrc;
    }
    
    private long calculateCrc(NativeFileItem item) {
	if( TRACE ) System.err.printf(">>> CRC %s\n", item.getFile().getName());
	Checksum checksum = new Adler32();
	updateCrc(checksum, item.getLanguage().toString());
	updateCrc(checksum, item.getLanguageFlavor().toString());
	updateCrc(checksum, item.getSystemIncludePaths());
	updateCrc(checksum, item.getUserIncludePaths());
	updateCrc(checksum, item.getSystemMacroDefinitions());
	updateCrc(checksum, item.getUserMacroDefinitions());
	if( TRACE ) System.err.printf("<<< CRC %s %d\n", item.getFile().getName(), checksum.getValue());
	return checksum.getValue();
    }
    
    private void updateCrc(Checksum checksum, String s) {
	checksum.update(s.getBytes(), 0, s.length());
	if( TRACE ) System.err.printf("\tupdateCrc %s -> %d\n", s, checksum.getValue());
    }
    
    private void updateCrc(Checksum checksum, List<String> strings) {
	for( String s : strings ) {
	    updateCrc(checksum, s);
	}
    }
    
    public static PersistentFactory getPersistentFactory() {
	// it isn't worth caching factory since it's too rarely used
	return new ValidatorPersistentFactory();
    }
    
    private static class Data implements Persistent, SelfPersistent {
	
	private Map<String, Long> map;
	
	public Data() {
	    map = new HashMap<String, Long>();
	}
	
	public long getCrc(String name) {
	    Long crc = map.get(name);
	    return crc == null ? 0 : crc.longValue();
	}
	
	public boolean exists(String name) {
	    return map.containsKey(name);
	}
	
	public void setCrc(String name, long crc) {
	    map.put(name, crc);
	}
	
	public Data(DataInput stream) throws IOException {
	    map = new HashMap<String, Long>();
	    int cnt = stream.readInt();
	    for (int i = 0; i < cnt; i++) {
		String name = stream.readUTF();
		long crc = stream.readLong();
		map.put(name, crc);
	    }
	}
	
	public void write(DataOutput stream ) throws IOException {
	    stream.writeInt(map.size());
	    for( Map.Entry<String, Long> entry : map.entrySet()) {
		stream.writeUTF(entry.getKey());
		stream.writeLong(entry.getValue().longValue());
	    }
	}
    }
    
    private static class ValidatorPersistentFactory implements PersistentFactory {
	
	public boolean canWrite(Persistent obj) {
	    assert obj instanceof Data;
	    return true;
	}

	public void write(DataOutput out, Persistent obj) throws IOException {
	    assert obj instanceof Data;
	    ((Data) obj).write(out);
	}

	public Persistent read(DataInput in) throws IOException {
	    return new Data(in);
	}
    }
	    
    private ProjectBase csmProject;
    private NativeProject nativeProject;    
    private Data data;
}

