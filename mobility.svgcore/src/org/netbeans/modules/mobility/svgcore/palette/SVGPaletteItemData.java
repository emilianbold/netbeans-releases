package org.netbeans.modules.mobility.svgcore.palette;

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.microedition.m2g.SVGImage;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.export.AnimationRasterizer;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

public final class SVGPaletteItemData {
    private static final String PROP_NAME         = "name"; //NOI18N
    private static final String PROP_DISPLAY_NAME = "displayName"; //NOI18N
    private static final String PROP_FOLDER_NAME  = "folderName"; //NOI18N
    private static final String PROP_FILE         = "file"; //NOI18N
    
    private final Properties m_props;
    private       Image      m_icon32 = null;

    private static final Map<String, SVGPaletteItemData> s_pool = new HashMap<String, SVGPaletteItemData>(10);
    
    public static synchronized SVGPaletteItemData get( FileObject fo) throws FileNotFoundException, IOException {
        String key = fo.getPath();
        SVGPaletteItemData data = s_pool.get(key);
        if (data == null) {
            InputStream in = fo.getInputStream();
            try {
                data = new SVGPaletteItemData(in);
            } finally {
                in.close();
            }
            s_pool.put(key, data);
        } 
        return data;
    }
    
    public static synchronized void set( String path, SVGPaletteItemData data) {
        s_pool.put(path, data);
    }
    
    private SVGPaletteItemData( InputStream in) throws IOException {
        m_props = new Properties();
        m_props.loadFromXML(in);
    }
    
    public SVGPaletteItemData( String name, String folderName, String filePath) {
        m_props = new Properties();
        m_props.put(PROP_NAME, name);
        int i = name.lastIndexOf('.');
        if ( i != -1) {
            name = name.substring(0, i);
        }
        m_props.put(PROP_DISPLAY_NAME, name);
        m_props.put(PROP_FILE, filePath);
        m_props.put(PROP_FOLDER_NAME, folderName);
    }
    
    public String getName() {
        return m_props.getProperty(PROP_NAME);
    }

    public String getDisplayName() {
        return m_props.getProperty(PROP_DISPLAY_NAME);
    }

    public String getFilePath() {
        return m_props.getProperty(PROP_FILE);
    }

    public String getFolderName() {
        return m_props.getProperty(PROP_FOLDER_NAME);
    }
    
    public void serialize(OutputStream out) throws IOException {
        m_props.storeToXML(out, ""); //NOI18N
    }

    public synchronized Image getIcon32() {
        if ( m_icon32 == null) {
            SceneManager.log(Level.INFO, "Obtaining icon for " + getFilePath()); //NOI18N
            try {
                String folderName = getFolderName();
                FileObject thumbsFolder = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject( SVGPaletteFactory.SVG_PALETTE_THUMBNAIL_FOLDER);                
                FileObject thumbFO;
                FileObject fo;    
                
                if ( (fo=thumbsFolder.getFileObject(folderName)) != null) {
                    thumbsFolder = fo;
                    thumbFO = thumbsFolder.getFileObject(getDisplayName(), "png"); //NOI18N
                } else {
                    thumbsFolder = thumbsFolder.createFolder(folderName);
                    thumbFO = null;
                }

                if (thumbFO == null) {
                    SceneManager.log(Level.INFO, "Constructing icon..."); //NOI18N
                    File svgFile = new File(getFilePath());
                    if ( svgFile.exists() && svgFile.isFile()) {
                        FileObject svgFO = FileUtil.toFileObject( FileUtil.normalizeFile(svgFile));
                        DataObject dObj  = DataObject.find(svgFO);
                        if ( dObj != null && dObj instanceof SVGDataObject) {
                            thumbFO = createThumb( (SVGDataObject) dObj, thumbsFolder);
                            SceneManager.log(Level.INFO, "Icon constructed."); //NOI18N
                        } else {
                            SceneManager.log( Level.SEVERE, "File " + svgFO.getPath() + " is not valid SVG file."); //NOI18N
                        }
                    } else {
                        SceneManager.log( Level.SEVERE, "File " + svgFile.getPath() + " not found"); //NOI18N
                    }                
                }    

                if (thumbFO != null) {
                    m_icon32 = ImageIO.read( thumbFO.getInputStream());
                }    
            } catch( Exception e) {
                SceneManager.error("Could not load icon", e); //NOI18N
            }
        }
        return m_icon32;
    }
    
    private static FileObject createThumb( final SVGDataObject dObj, FileObject parent) throws IOException {
        FileObject thumbFO = AnimationRasterizer.export(dObj, new AnimationRasterizer.Params() {
            public SVGImage getSVGImage() throws IOException, BadLocationException {
                return Util.createSVGImage(dObj.getPrimaryFile(), false);
            }

            public int getImageWidth() {
                return 32;
            }

            public int getImageHeight() {
                return 32;
            }

            public float getStartTime() {
                return 0;
            }

            public float getEndTime() {
                return 0;
            }

            public float getFramesPerSecond() {
                return 0;
            }

            public boolean isForAllConfigurations() {
                return false;
            }

            public double getRatio() {
                return 1;
            }

            public float getCompressionQuality() {
                return 100;
            }

            public boolean isProgressive() {
                return false;
            }

            public boolean isInSingleImage() {
                return true;
            }

            public boolean isTransparent() {
                return true;
            }

            public AnimationRasterizer.ImageType getImageType() {
                return AnimationRasterizer.ImageType.PNG24;
            }

            public void setImageWidth(int w) {
            }

            public void setImageHeight(int h) {
            }

            public int getNumberFrames() {
                return 1;
            }

            public J2MEProject getProject() {
                return null;
            }

            public String getElementId() {
                return null;
            }

            public AnimationRasterizer.ColorReductionMethod getColorReductionMethod() {
                return null;
            }
        }, parent);
        
        assert thumbFO != null : "Null thumbnail image file"; //NOI18N
        return thumbFO;
    }
}
