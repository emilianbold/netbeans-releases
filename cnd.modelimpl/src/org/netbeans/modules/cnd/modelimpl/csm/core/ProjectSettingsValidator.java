/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.ProjectSettingsValidatorKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

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
    private static final Logger LOG = Logger.getLogger(ProjectSettingsValidator.class.getName());
    
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
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Start CRC counting for {0}", csmProject.getName()); // NOI18N
        }
	long time = 0;
	if( TraceFlags.TIMING ) {
	    System.err.printf("ProjectSettingsValidator.storeSettings for %s\n", csmProject.getName());
	    time = System.currentTimeMillis();
	}
	data = new Data();
        List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
        List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
        for(NativeFileItem item : nativeProject.getAllFiles()){
            if (!item.isExcluded()) {
                switch(item.getLanguage()){
                    case C:
                    case CPP:
                    case FORTRAN:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            }
        }
	updateMap(headers);
	updateMap(sources);
	Key key = new ProjectSettingsValidatorKey(csmProject.getUniqueName());
	RepositoryUtils.put(key, data);
	if( TraceFlags.TIMING ) {
	    time = System.currentTimeMillis() - time;
	    System.err.printf("ProjectSettingsValidator.storeSettings for %s took %d ms\n", csmProject.getName(), time);
	}
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Finish CRC counting for {0}", csmProject.getName()); // NOI18N
            LOG.log(Level.INFO, "Model state {0}", csmProject.getModel().getState()); // NOI18N
        }
        if (csmProject.getModel().getState() == CsmModelState.OFF) {
            LOG.log(Level.INFO, "Cannot store CRC for project {0}", csmProject.getName());// NOI18N
        }
    }
    
    private void updateMap(List<NativeFileItem> items) {
	for( NativeFileItem item : items ) {
	    long crc = calculateCrc(item);
	    data.setCrc(item.getAbsolutePath(), crc);
	}
    }
    
    public void restoreSettings() {
	if( nativeProject == null ) {
	    return;
	}
	Key key = new ProjectSettingsValidatorKey(csmProject.getUniqueName());
	data = (Data) RepositoryUtils.get(key);
        if( data == null ) {
            data = new Data();
            DiagnosticExceptoins.register(new IllegalStateException("Can not get project settings validator data by the key " + key)); //NOI18N
        }
    }
    
    public boolean exists(FileImpl fileImpl) {
	return data.exists(fileImpl.getAbsolutePath());
    }
    
    public boolean arePropertiesChanged(NativeFileItem item) {
	if( nativeProject == null ) {
	    return false;
	}
	assert data != null;
	long savedCrc = data.getCrc(item.getAbsolutePath());
	long currentCrc = calculateCrc(item);
	if( TRACE ) {
            System.err.printf("arePropertiesChanged %s OLD=%d CUR=%d %b\n", item.getName(), savedCrc, currentCrc, (savedCrc != currentCrc));
        }
	return savedCrc != currentCrc;
    }
    
    private long calculateCrc(NativeFileItem item) {
	if( TRACE ) {
            System.err.printf(">>> CRC %s\n", item.getName());
        }
	Checksum checksum = new Adler32();
	updateCrc(checksum, item.getLanguage().toString());
	updateCrc(checksum, item.getLanguageFlavor().toString());
	updateCrcByFSPaths(checksum, item.getSystemIncludePaths());
	updateCrcByFSPaths(checksum, item.getUserIncludePaths());
	updateCrcByStrings(checksum, item.getSystemMacroDefinitions());
	updateCrcByStrings(checksum, item.getUserMacroDefinitions());
	if( TRACE ) {
            System.err.printf("<<< CRC %s %d\n", item.getName(), checksum.getValue());
        }
	return checksum.getValue();
    }
    
    private void updateCrc(Checksum checksum, String s) {
	checksum.update(s.getBytes(), 0, s.length());
	if( TRACE ) {
            System.err.printf("\tupdateCrc %s -> %d\n", s, checksum.getValue());
        }
    }
    
    private void updateCrcByFSPaths(Checksum checksum, List<FSPath> fsPaths) {
	for( FSPath fsp : fsPaths ) {
	    updateCrc(checksum, fsp.getURL().toString());
	}
    }

    private void updateCrcByStrings(Checksum checksum, List<String> strings) {
        strings = new ArrayList<String>(strings);
        Collections.sort(strings);
	for( String s : strings ) {
	    updateCrc(checksum, s);
	}
    }
    
    public static PersistentFactory getPersistentFactory(int unitId) {
	// it isn't worth caching factory since it's too rarely used
	return new ValidatorPersistentFactory(unitId);
    }
    
    // Not SelfPersistent any more because I have to pass unitIndex into write() method
    // It is private, so I don't think it's a problem. VK.
    private static class Data implements Persistent {
	
	private Map<CharSequence, Long> map;
	
	public Data() {
	    map = new HashMap<CharSequence, Long>();
	}
	
	public long getCrc(CharSequence name) {
	    Long crc = map.get(FilePathCache.getManager().getString(name));
	    return crc == null ? 0 : crc.longValue();
	}
	
	public boolean exists(CharSequence name) {
	    return map.containsKey(FilePathCache.getManager().getString(name));
	}
	
	public void setCrc(CharSequence name, long crc) {
	    map.put(FilePathCache.getManager().getString(name), crc);
	}
	
	public Data(RepositoryDataInput stream, int unitID) throws IOException {
	    map = new HashMap<CharSequence, Long>();
	    int cnt = stream.readInt();
	    for (int i = 0; i < cnt; i++) {
		CharSequence name = APTSerializeUtils.readFileNameIndex(stream, FilePathCache.getManager(), unitID);
		long crc = stream.readLong();
		map.put(name, crc);
	    }
	}
	
	public void write(RepositoryDataOutput stream, int unitID) throws IOException {
	    stream.writeInt(map.size());
	    for( Map.Entry<CharSequence, Long> entry : map.entrySet()) {
                APTSerializeUtils.writeFileNameIndex(entry.getKey(), stream, unitID);
		stream.writeLong(entry.getValue().longValue());
	    }
	}
    }
    
    private static class ValidatorPersistentFactory implements PersistentFactory {

        private final int unitId;

        public ValidatorPersistentFactory(int unitId) {
            this.unitId = unitId;
        }

        @Override
	public void write(RepositoryDataOutput out, Persistent obj) throws IOException {
	    assert obj instanceof Data;
	    ((Data) obj).write(out, unitId);
	}

        @Override
	public Persistent read(RepositoryDataInput in) throws IOException {
	    return new Data(in, unitId);
	}
    }
	    
    private final ProjectBase csmProject;
    private NativeProject nativeProject;    
    private Data data;
}

