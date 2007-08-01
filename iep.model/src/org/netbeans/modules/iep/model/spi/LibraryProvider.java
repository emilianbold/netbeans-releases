package org.netbeans.modules.iep.model.spi;

import java.io.InputStream;

import javax.swing.ImageIcon;

public interface LibraryProvider {

	public InputStream getLibraryXml();
	
	public ImageIcon resolveIcon(String iconName);
	
	public Object newInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException;
}
