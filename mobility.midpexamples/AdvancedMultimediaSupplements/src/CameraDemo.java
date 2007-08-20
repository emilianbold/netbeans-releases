/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.VideoControl;
import javax.microedition.amms.*;
import javax.microedition.amms.control.camera.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;


public class CameraDemo extends MIDlet 
           implements CommandListener, ItemStateListener{
    
    private Player player;
    private CameraControl camCtrl = null;
    private VideoControl vidCtrl = null;
    private ExposureControl expCtrl = null;
    private FlashControl flsCtrl = null;
    private FocusControl fcsCtrl = null;
    private ZoomControl zomCtrl = null;
    private SnapshotControl snpCtrl = null;

    private Command exitCommand = new Command("Exit", Command.ITEM, 1);
    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command backToBrowserCommand = 
                                           new Command("Back", Command.BACK, 1);
     private Command deleteImageFile = 
                                           new Command("Delete from disk", Command.ITEM, 1);
    private final Command snapCommand = 
                                       new Command("Snapshot", Command.STOP, 1);
    private final Command stopShottingCommand = 
                                       new Command("Stop Shooting", Command.ITEM, 1);
    private final Command saveSnapCommand = 
                                       new Command("Save it", Command.STOP, 1);
    private final Command deleteSnapCommand = 
                                       new Command("Delete it", Command.ITEM, 1);
    private final Command changeExposureCommand =
                          new Command("Choose exposure modes", Command.ITEM, 3);
    private final Command enabledShutterFeedbackCommand =
                        new Command("Enable shutter feedback", Command.ITEM, 2);
    private final Command disabledShutterFeedbackCommand =
                       new Command("Disable shutter feedback", Command.ITEM, 2);
    private final Command setSupportedF_StopsCommand =
                           new Command("Change F_Stop number", Command.ITEM, 4);
    private final Command setFlashModeCommand =
                           new Command("Set flash mode", Command.ITEM, 5);
    private final Command setSnapshotCommand =
                           new Command("Snapshot setting", Command.ITEM, 1);
    private final Command viewGalleryCommand =
                           new Command("View gallery", Command.ITEM, 6);
    private final Command displayCommand =
                           new Command("Display", Command.ITEM, 6);
    private final Command setFocusCommand =
                           new Command("Focus settings", Command.ITEM, 7);
    private final Command setZoomCommand =
                           new Command("Zoom settings", Command.ITEM, 8);
                           

    private Display display;
    private Form vidForm;
    private Form snapForm;
    private Item videoItem;
    private ChoiceGroup exposureModes;
    private Form exposureModesForm;
    private ChoiceGroup F_StopsCG;
    private Form supportedF_StopsForm;
    private ChoiceGroup flashModes;
    private ChoiceGroup focusModes;
    private ChoiceGroup zoomOptModes;
    private ChoiceGroup zoomDigModes;
    private Form flashModesForm;
    private Form focusModesForm;
    private Form zoomModesForm;
    private String focusSupportedModes[];
    private int focusModesInt[];
    private String zoomOptSupportedModes[];
    private int zoomOptModesInt[];
    private String zoomDigSupportedModes[];
    private int zoomDigModesInt[];
    private List browser;
    private Form imageViewer;
    private FileConnection currImage;
    
    private Form snapshotSettingsForm;
    private ChoiceGroup snapSetDisOrSave;
    private String[] snapSetOption = {"Display picture on screen",
                                       "Save picture to file"};
    private ChoiceGroup snapSetFreezeOrConfirm;
    private String[] snapSetFreezeOption = {"Freeze the viewfinder",
                                             "Freeze and confirm",
                                             "Save without freezing"};
    private TextField burstNum;
    private int NumOfPic = -2;
    private boolean savedMessageAppear = false;
                                       
    
    private static String[] flashAllModes = {"OFF",
                                              "AUTO",
                                              "AUTO_WITH_REDEYEREDUCE",
                                              "FORCE",
                                              "FORCE_WITH_REDEYEREDUCE",
                                              "FILLIN"
    };

    /* separator character as defined by FC specification */
    private final static char   SEP = '/';
    
    private String PHOTOS_DIR = null;

    public CameraDemo() {
        vidForm = new Form("Camera Player");
        display = Display.getDisplay(this);
        display.setCurrent(vidForm);
        vidForm.addCommand(exitCommand);
        vidForm.setCommandListener(this);
        
        try {           
            player = Manager.createPlayer("capture://video");
            player.prefetch();
            player.realize();
            
            vidCtrl = (VideoControl)player.getControl(
                "VideoControl");
            if (vidCtrl != null) {
                videoItem = (Item)vidCtrl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
                vidCtrl.setDisplaySize(140, 140);
                vidForm.append(videoItem);
            } else {
                System.out.println("Error: Can not create VideoControl");
                return;
            }
            camCtrl = (CameraControl)player.getControl(
                "javax.microedition.amms.control.camera.CameraControl");
            if (camCtrl != null) {
                vidForm.addCommand(snapCommand);
                vidForm.addCommand(changeExposureCommand);
                vidForm.addCommand(enabledShutterFeedbackCommand);
            }
            expCtrl = (ExposureControl)player.getControl(
                "javax.microedition.amms.control.camera.ExposureControl");
            if (expCtrl != null) {
                vidForm.addCommand(setSupportedF_StopsCommand);
            }
            flsCtrl = (FlashControl)player.getControl(
                "javax.microedition.amms.control.camera.FlashControl");
            if (flsCtrl != null) {
                vidForm.addCommand(setFlashModeCommand);
            }
            fcsCtrl = (FocusControl)player.getControl(
                "javax.microedition.amms.control.camera.FocusControl");
            if (fcsCtrl != null) {
                vidForm.addCommand(setFocusCommand);
            }
            zomCtrl = (ZoomControl)player.getControl(
                "javax.microedition.amms.control.camera.ZoomControl"); 
            if (zomCtrl != null) {
                vidForm.addCommand(setZoomCommand);
            }
            snpCtrl = (SnapshotControl)player.getControl(
                "javax.microedition.amms.control.camera.SnapshotControl");
            if (snpCtrl != null) {
                vidForm.addCommand(setSnapshotCommand);
                vidForm.addCommand(viewGalleryCommand);
                vidForm.addCommand(stopShottingCommand);
            }
        } catch (MediaException me) {
            me.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void startApp() {
        try {
            player.start();
        } catch (MediaException me) {
            me.printStackTrace();
        }
    }

    public void pauseApp() {
        if (camCtrl != null) {
            stopShooting();
        }
        try {
            player.stop();
        } catch (MediaException me) {
            me.printStackTrace();
        }
        
    }

    public void destroyApp(boolean unconditional) {
        closePlayer();
        notifyDestroyed();
    }

    /*
     * Respond to commands, including back
     */
    public void commandAction(Command c, Displayable s) {
        if (savedMessageAppear == true && vidForm.size() > 1) {
            vidForm.delete(vidForm.size() - 1);
            savedMessageAppear = false;
        }
        if (c == backCommand) {
            if (snapshotSettingsForm != null && 
                        snapshotSettingsForm.isShown() && 
                        snapSetFreezeOrConfirm.getSelectedIndex() == 2) {
                NumOfPic = Integer.parseInt(burstNum.getString());                
            } else  if (focusModesForm != null &&  focusModesForm.isShown()) {
                try {
                    fcsCtrl.setFocus(
                        focusModesInt[focusModes.getSelectedIndex()]);
                } catch (MediaException me) {
                    me.printStackTrace();
                }
            } else  if (zoomModesForm != null &&  zoomModesForm.isShown()) {                
                zomCtrl.setDigitalZoom(
                    zoomDigModesInt[zoomDigModes.getSelectedIndex()]);
                zomCtrl.setOpticalZoom(
                    zoomOptModesInt[zoomOptModes.getSelectedIndex()]);
                
            }
            display.setCurrent(vidForm);
        } else if (c == backToBrowserCommand) {
            display.setCurrent(browser);
            imageViewer.deleteAll();
            if (currImage != null) {
                try {
                    currImage.close();
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }  
            }
        } else if (c == saveSnapCommand) {            
            unfreezeSnpCtrl(true);
            vidForm.append("Picture has been saved to disk");
            savedMessageAppear = true;
        } else if (c == deleteSnapCommand) {
           unfreezeSnpCtrl(false);
        } else if (c == deleteImageFile) {
            new Thread(new Runnable() {
                public void run() {
                    imageViewer.deleteAll();
                    if (currImage != null) {
                        try {
                            currImage.delete();
                            currImage.close();
                        } catch(IOException ioe) {
                            ioe.printStackTrace();
                        }                
                    }
                    browser.delete(browser.getSelectedIndex());
                    display.setCurrent(browser);
                }
            }).start();
        }
        if (s == vidForm) {
            if (c == exitCommand) {
                destroyApp(false);
            } else if (c == snapCommand) {
                doSnapshot(NumOfPic);
            } else if (camCtrl != null && 
                        c == enabledShutterFeedbackCommand) {
                enabledShutterFeedback(true);
            } else if (camCtrl != null && 
                        c == disabledShutterFeedbackCommand) {
                enabledShutterFeedback(false);
            } else if (camCtrl != null && c == changeExposureCommand) {
                setExposureModes();
            } else if (camCtrl != null && c == setSupportedF_StopsCommand) {
                setSupportedF_Stops();
            } else if (camCtrl != null && c == setFlashModeCommand) {
                setFlashMode();
            } else if (camCtrl != null && c == setSnapshotCommand) {
                setSnapshotSettings();
            } else if (camCtrl != null && c == stopShottingCommand) {
                stopShooting();
            } else if (camCtrl != null && c == viewGalleryCommand) {
                new Thread(new Runnable() {
                    public void run() {
                        viewGallery();
                    }
                }).start();
            } else if (camCtrl != null && c == setFocusCommand) {
                focusSetting();
            } else if (camCtrl != null && c == setZoomCommand) {
                zoomSetting();
            }
        } else if (camCtrl != null && c == displayCommand) {
            new Thread(new Runnable() {
                public void run() {
                    viewImage();
                }
            }).start();
         }
    }
    
    private void stopShooting() {
        snpCtrl.stop();
    }
    
    
    private void unfreezeSnpCtrl(boolean save) {
        snpCtrl.unfreeze(save);                
        snapForm.deleteAll();
        vidForm.append(videoItem);
        display.setCurrent(vidForm);
    }
    
    /**
     * Show file list in the photos directory .
     */
    private void viewGallery() {
        if (PHOTOS_DIR == null) {
            PHOTOS_DIR = "file://" + snpCtrl.getDirectory();
        }
        Enumeration e;
        FileConnection photosDir = null;        
        try {
            photosDir = (FileConnection)Connector.open(PHOTOS_DIR);
            e = photosDir.list();
            browser = new List(snpCtrl.getDirectory(), List.IMPLICIT);
            while (e.hasMoreElements()) {
                String fileName = (String)e.nextElement();
                if (fileName.charAt(fileName.length()-1) == SEP) {
                    // This is directory
                } else {
                    // this is regular file
                    browser.append(fileName, null);
                }
            }
            
            if (photosDir != null) {
                photosDir.close();
            }
            browser.setSelectCommand(displayCommand);
            browser.addCommand(backCommand);
            browser.setCommandListener(this);
            display.setCurrent(browser);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    private void viewImage() {
        if(browser.size() == 0) {
            return;
        }
        if(imageViewer == null) {
            imageViewer = new Form("Image Viewer");
            imageViewer.addCommand(backToBrowserCommand);
            imageViewer.addCommand(deleteImageFile);
            imageViewer.setCommandListener(this);
        }
        byte [] b;
        try {
            currImage = (FileConnection)
                    Connector.open(PHOTOS_DIR + SEP + 
                        browser.getString(browser.getSelectedIndex()));
            if (!currImage.exists()) {
                throw new IOException("Image file does not exists");
            }
            InputStream fis = currImage.openInputStream();
            int fileSize = (int)currImage.fileSize();
            b = new byte[fileSize];
            
            int length = fis.read(b, 0, fileSize);
            
            fis.close();                        
            
            if (b != null) {
                Image im = Image.createImage(b, 0, b.length);
                ImageItem imi = new ImageItem("", im, Item.	PLAIN, "");                
                imageViewer.append(imi);
            }
            
            display.setCurrent(imageViewer);
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        
    }
    
    public void itemStateChanged(Item item) {
        if (savedMessageAppear == true && vidForm.size() > 1) {
            vidForm.delete(vidForm.size() - 1);
            savedMessageAppear = false;
        }
        try {
            if (item != null) {
                if (item == exposureModes) {
                    camCtrl.setExposureMode(exposureModes.getString(
                                        exposureModes.getSelectedIndex()));
                } else if (item == F_StopsCG) {
                    expCtrl.setFStop(Integer.parseInt(
                            F_StopsCG.getString(F_StopsCG.getSelectedIndex())));
                    int fpStop = expCtrl.getFStop();
                    int expTime = expCtrl.getExposureTime();
                    int EV = expCtrl.getExposureValue();
                    System.out.println("The EV is: " + EV);
                } else if (item == flashModes) {
                    String mode = 
                        flashModes.getString(flashModes.getSelectedIndex());
                    for (int i = 0; i < flashAllModes.length; i++) {
                        if (mode.equals(flashAllModes[i])) {
                            flsCtrl.setMode(i + 1);
                            break;
                        }
                    }
                }  else if (item == snapSetDisOrSave) {
                    if(snapSetDisOrSave.getSelectedIndex() == 0) {
                        NumOfPic = 0;
                        snapSetFreezeOrConfirm.setSelectedIndex(0, true);
                        if (snapshotSettingsForm.size() > 2) {
                            snapshotSettingsForm.delete(2);
                        }
                        if (snapshotSettingsForm.size() > 1) {
                            snapshotSettingsForm.delete(1);
                        }
                    } else {
                        NumOfPic = -2;
                        if (snapshotSettingsForm.size() == 1) {
                            snapshotSettingsForm.append(snapSetFreezeOrConfirm);
                            if (snapSetFreezeOrConfirm.getSelectedIndex() == 2) {
                                snapshotSettingsForm.append(burstNum);
                            }
                        }                     
                    }
                } else if (item == snapSetFreezeOrConfirm) {
                    if(snapSetFreezeOrConfirm.getSelectedIndex() == 0) {
                        if(snapshotSettingsForm.size() == 3) {
                            snapshotSettingsForm.delete(2);
                        }
                        NumOfPic = -2;
                    } else if(snapSetFreezeOrConfirm.getSelectedIndex() == 1) {
                        if(snapshotSettingsForm.size() == 3) {
                            snapshotSettingsForm.delete(2);
                        }
                        NumOfPic = -1;
                    } else if(snapSetFreezeOrConfirm.getSelectedIndex() == 2) {
                        snapshotSettingsForm.append(burstNum);
                        NumOfPic = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void doSnapshot(final int maxShots) {
        if (maxShots == 0) {
            doSnapshot();
            return;
        }
        if(snpCtrl != null) {            
            new Thread() {
                public void run() {
                    snpCtrl.start(maxShots);
                    if(maxShots != snpCtrl.FREEZE_AND_CONFIRM) {
                        vidForm.append("Picture has been saved to disk");
                        savedMessageAppear = true;
                    }
                }
            }.start();
            if (maxShots == -1) {
                if (snapForm == null) {
                    snapForm = new Form("Do you want to save the picture ?");                     
                    snapForm.addCommand(saveSnapCommand);
                    snapForm.addCommand(deleteSnapCommand);
                    snapForm.setCommandListener(this);
                }
                vidForm.deleteAll();
                snapForm.append(videoItem);
                display.setCurrent(snapForm);
            } 
        }
    }
    
    private void doSnapshot() {
        new Thread() {
            public void run() {
                try {
                    byte [] snap = vidCtrl.getSnapshot("encoding=jpeg");
                    if (snap != null) {
                        Image im = Image.createImage(snap, 0, snap.length);
                        ImageItem imi = new ImageItem("", im, Item.	PLAIN, "");
                        
                        vidForm.append(imi);
                    }
                } catch (MediaException me) {
                    System.err.println(me);
                }
            }
        }.start();
    }
    
    private void enabledShutterFeedback(boolean enabledFeedback) {
            final boolean bool = enabledFeedback;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        camCtrl.enableShutterFeedback(bool);
                    } catch (Exception me) {
                        return;
                    } 
                }
            }).start();            
            if (enabledFeedback) {
                vidForm.removeCommand(enabledShutterFeedbackCommand);
                vidForm.addCommand(disabledShutterFeedbackCommand);
            } else {
                vidForm.removeCommand(disabledShutterFeedbackCommand);
                vidForm.addCommand(enabledShutterFeedbackCommand);
            }
    }
    
    private void setExposureModes() {
        try {
            if (exposureModesForm == null) {
                exposureModesForm = new Form("Select exposure modes:");
                exposureModes = new ChoiceGroup(null,
                                            Choice.EXCLUSIVE,
                                            camCtrl.getSupportedExposureModes(),
                                            null);
                exposureModesForm.append(exposureModes);
                exposureModesForm.setItemStateListener(this);
                exposureModesForm.addCommand(backCommand);
                exposureModesForm.setCommandListener(this);
            }
            display.setCurrent(exposureModesForm);
        } catch (Exception e) {
            e.printStackTrace();
        }            
    }
    
    private void setSupportedF_Stops() {
        try {
            if (supportedF_StopsForm == null) {
                supportedF_StopsForm = new Form("Select F_Stop number:");
                int fpIntArray[] = expCtrl.getSupportedFStops();
                String[] fpStops = new String[fpIntArray.length];
                for (int i = 0; i < fpIntArray.length; i++) {
                    fpStops[i] = Integer.toString(fpIntArray[i]);
                }
                F_StopsCG = new ChoiceGroup(null,
                                            Choice.EXCLUSIVE,
                                            fpStops,
                                            null);
                supportedF_StopsForm.append(F_StopsCG);
                supportedF_StopsForm.setItemStateListener(this);
                supportedF_StopsForm.addCommand(backCommand);
                supportedF_StopsForm.setCommandListener(this);
            }
            display.setCurrent(supportedF_StopsForm);
        } catch (Exception e) {
            e.printStackTrace();
        }            
    }
    
    private void setFlashMode() {
        try {
            if (flashModesForm == null) {
                flashModesForm = new Form("Select flash mode:");
                int flashIntArray[] = flsCtrl.getSupportedModes();                
                String flashSupportedModes[] = new String[flashIntArray.length];
                for (int i = 0; i < flashIntArray.length; i++) {
                    flashSupportedModes[i] = flashAllModes[i];
                }
                int initialMode = flsCtrl.getMode();
                flashModes = new ChoiceGroup(null,
                                             Choice.EXCLUSIVE,
                                             flashSupportedModes,
                                             null);
                flashModes.setSelectedIndex((initialMode - 1), true);
                flashModesForm.append(flashModes);
                flashModesForm.setItemStateListener(this);
                flashModesForm.addCommand(backCommand);
                flashModesForm.setCommandListener(this);
            }
            display.setCurrent(flashModesForm);
        } catch (Exception e) {
            e.printStackTrace();
        }                        
    }
    
    private void focusSetting() {
        try {
            if (focusModesForm == null) {
                focusModesForm = new Form("Select focus mode:");                
                focusSupportedModes = new String[fcsCtrl.getFocusSteps() + 1];
                focusModesInt = new int[focusSupportedModes.length];
                focusSupportedModes[0] = "Auto Mode";
                focusModesInt[0] = fcsCtrl.AUTO;
                fcsCtrl.setFocus(fcsCtrl.getMinFocus());
                for (int i = 1; i < focusSupportedModes.length; i++) {
                    focusModesInt[i] = fcsCtrl.getFocus();
                    focusSupportedModes[i] = Integer.toString(focusModesInt[i]);
                    fcsCtrl.setFocus(fcsCtrl.NEXT);
                }
                if(focusModesInt[focusModesInt.length - 1] == Integer.MAX_VALUE) {
                    focusSupportedModes[focusModesInt.length - 1] = "infinity";
                }
                fcsCtrl.setFocus(fcsCtrl.AUTO);
                int initialMode = flsCtrl.getMode();
                focusModes = new ChoiceGroup(null,
                                             Choice.EXCLUSIVE,
                                             focusSupportedModes,
                                             null);                                             
                focusModes.setSelectedIndex(0, true);
                focusModesForm.append(focusModes);
                focusModesForm.setItemStateListener(this);
                focusModesForm.addCommand(backCommand);
                focusModesForm.setCommandListener(this);
            }
            display.setCurrent(focusModesForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void zoomSetting() {
        try {
            if (zoomModesForm == null) {
                zoomModesForm = new Form("Select zoom mode:");  
                //Digital zoom
                zoomDigSupportedModes = new String[zomCtrl.getDigitalZoomLevels()];
                zoomDigModesInt = new int[zoomDigSupportedModes.length];
                zomCtrl.setDigitalZoom(100);
                for (int i = 0; i < zoomDigSupportedModes.length; i++) {                    
                    zoomDigModesInt[i] = zomCtrl.getDigitalZoom();
                    zoomDigSupportedModes[i] = Integer.toString(zoomDigModesInt[i]);
                    zomCtrl.setDigitalZoom(zomCtrl.NEXT);
                }
                
                zoomDigModes = new ChoiceGroup(null,
                                             Choice.EXCLUSIVE,
                                             zoomDigSupportedModes,
                                             null);                                             
                zoomDigModes.setSelectedIndex(0, true);
                zoomModesForm.append("Set digital zoom");
                zoomModesForm.append(zoomDigModes);
                //Optical zoom
                zoomOptSupportedModes = new String[zomCtrl.getOpticalZoomLevels()];
                zoomOptModesInt = new int[zoomOptSupportedModes.length];
                zomCtrl.setOpticalZoom(100);
                for (int i = 0; i < zoomOptSupportedModes.length; i++) {                    
                    zoomOptModesInt[i] = zomCtrl.getOpticalZoom();
                    zoomOptSupportedModes[i] = Integer.toString(zoomOptModesInt[i]);
                    zomCtrl.setOpticalZoom(zomCtrl.NEXT);
                }
                
                zoomOptModes = new ChoiceGroup(null,
                                             Choice.EXCLUSIVE,
                                             zoomOptSupportedModes,
                                             null);                                             
                zoomOptModes.setSelectedIndex(0, true);
                zoomModesForm.append("Set optical zoom");
                zoomModesForm.append(zoomOptModes);
                zoomModesForm.setItemStateListener(this);
                zoomModesForm.addCommand(backCommand);
                zoomModesForm.setCommandListener(this);
            }
            display.setCurrent(zoomModesForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setSnapshotSettings() {
        try {
            if (snapshotSettingsForm == null) {
                snapshotSettingsForm = new Form("Set the snapshot setting:");
                snapshotSettingsForm.addCommand(backCommand);
                snapSetDisOrSave = new ChoiceGroup("Display or save", ChoiceGroup.POPUP, snapSetOption, null);
                snapshotSettingsForm.setItemStateListener(this);
                snapshotSettingsForm.setCommandListener(this);
                snapSetDisOrSave.setSelectedIndex(1, true);
                burstNum = new TextField("Number of picture", "1", 3, TextField.NUMERIC);
                snapSetFreezeOrConfirm = new ChoiceGroup("Freeze option", ChoiceGroup.POPUP, snapSetFreezeOption, null);
                snapSetFreezeOrConfirm.setSelectedIndex(0, true);
                snapshotSettingsForm.append(snapSetDisOrSave);
                snapshotSettingsForm.append(snapSetFreezeOrConfirm);
                NumOfPic = -2;
            }
            display.setCurrent(snapshotSettingsForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void closePlayer() {
        if (player != null) {
            player.close();
            player = null;
        }
    }
}
