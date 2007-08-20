/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Vector;

import javax.microedition.amms.GlobalManager;
import javax.microedition.amms.MediaProcessor;
import javax.microedition.amms.MediaProcessorListener;
import javax.microedition.amms.control.imageeffect.ImageTransformControl;
import javax.microedition.amms.control.imageeffect.ImageEffectControl;
import javax.microedition.amms.control.imageeffect.OverlayControl;
import javax.microedition.amms.control.ImageFormatControl;
import javax.microedition.amms.control.EffectOrderControl;
        
import javax.microedition.media.MediaException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageEffects extends MIDlet implements MediaProcessorListener, 
    CommandListener {
    
    static final String format_array[] = new String[] {
        "raw", 
        "jpeg", 
        "png"
    };
            
    static final String stream_array[] = new String[] {
        "/images/lenna96x96.jpg",
        "/images/su15.jpg",
        "/images/mig15.png"
    };
            
    static final String order_name_array[] = new String[] {
        "TransformControl", 
        "EffectControl", 
        "OverlayControl"
    };
            
    static final String effect_array[] = new String[] {
        "no", //null, 
        "monochrome", 
        "negative", 
        "emboss", 
        "sepia", 
        "solarize", 
        "redeyereduction"
    };
    
    static final int scale_array[] = new int[] {
        80, 100, 120
    };
    
    static final int overlay_size[] = new int[] {50, 10}; //2 overlays with size=50x10 & 10x50
    
    static final String color_name_array[] = new String[] {
        "A", "R", "G", "B"
    };
    
    private InputStream inputStream;
    private ByteArrayOutputStream outputStream;
    private Image sourceImage;
    private Image processedImage;
    
    private int indexImageEffect;
    private int indexOutputImageFormat;
    private int indexInputImageFormat;
    private int indexInputStreamName;

    private int indexOutputScaleW;
    private int indexOutputScaleH;
    
    private int indexOutputRotate; //can be 0,1,2,3 (*90 = clockwise rotation angle)
    private boolean indexOutputBorder[]; // = new boolean[1];
    private boolean indexOutputOverlay[]; // = new boolean[2];
    private int indexEffectOrder[]; // = new int[3];
    
    /*
     * these objects needed for overlay control
     */
    int argb_color[][]; // = new int[2][4]; //current overlay colors
    int argb_buffer[] = new int[overlay_size[0] * overlay_size[1]];
    
    /* 
     * Used to distinguish first appearance of Forms on the screen
     * i.e. case where we can not compare new value with previous.
     */
    private boolean first_form;
    
    /*
     * false - use process() based blocking wait (no callbacks)
     * true - use start() & callbacks ...
     */
    private final boolean need_callback = true;
    /*
     * false - use raw image from resources as input
     *        -> uses RAW ImageProcessor
     * true - use jpeg/png input stream to create jpeg input stream 
            -> uses JPEG/PNG ImageProcessor
     */
    private boolean need_input_stream;
    /*
     * false - use byte array to create processed image in RAW format,
     * true - use (byte array based) Input Stream to create 
     *        processed image in a compressed (JPEG or PNG) format.
     */
    private boolean need_output_stream;
    
    private Command cmdExit;
    private Command cmdDone;
    private Command cmdSetData;
    private Command cmdSetOrder;
    private Command cmdSetScale;
    private Command cmdSetEffect;
    private Command cmdSetOverlay;
    private Command cmdNoEffect;
    private Command cmdMonochrome;
    private Command cmdNegative;
    private Command cmdSepia;
    private Command cmdSolarize;
    private Command cmdEmboss;
    private Command cmdRedEye;
            
    private int processedImageItemIndex;
    private Form dataForm;
    private Form orderForm;
    private Form scaleForm;
    private Form effectForm;
    private Form overlayForm;
    private Form resultViewer;
    
    private ChoiceGroup inputFormatSelector;
    private ChoiceGroup outputFormatSelector;
    private ChoiceGroup widthScaleSelector;
    private ChoiceGroup heightScaleSelector;
    private ChoiceGroup borderScaleSelector;
    private ChoiceGroup rotateScaleSelector;
    private ChoiceGroup effectSelector;
    private ChoiceGroup overlaySelector;
    private TextField[][] overlayColor;
    private TextField[] effectOrder;

    private Display display;
    private Form currentForm;

    public ImageEffects() {
        int i, j;
        
        //SETUP STATE COMPONENTS
        indexImageEffect = 0;
        indexInputStreamName = 0;
        indexInputImageFormat = 0;
        indexOutputImageFormat = 0;
        indexOutputScaleW = 1;
        indexOutputScaleH = 1;
        
        indexOutputRotate = 0;
        indexOutputBorder = new boolean[] { false };
        indexOutputOverlay = new boolean[] { false, false };
        indexEffectOrder = new int[] { 0, 1, 2 };
        
        argb_color = new int[][] {
            { 0xFF, 0x00, 0xFF, 0x00 },
            { 0x7F, 0xFF, 0xFF, 0x00 }
        };
                
        need_input_stream = false;
        need_output_stream = false;
        
        // SETUP GUI COMPONENTS
        first_form = true;
        
        //create display
        display = Display.getDisplay(this);
        //create forms
        dataForm = new Form("Select Image Formats"); //selects input and output image formats
        orderForm = new Form("Select Effect Order"); //selects relative order of image "transform", "effect", "overlay"
        scaleForm = new Form("Select Image Transformations"); //selects output image resize (border only), scale & rotate  
        effectForm = new Form("Select Image Effect"); //selects one of 6 effects
        overlayForm = new Form("Select Image Overlays"); //selects 0,1,2,1&2 overlays 
        resultViewer = new Form("Images");
        
        //create commands
        cmdDone = new Command("Done", Command.BACK, 1);
        cmdExit = new Command("Exit", Command.EXIT, 2);
        cmdSetData = new Command("Set Formats", Command.ITEM, 3);
        cmdSetOrder = new Command("Set Effect Order", Command.ITEM, 3);
        cmdSetScale = new Command("Set Transforms", Command.ITEM, 3);
        cmdSetEffect = new Command("Set Effects", Command.ITEM, 3);
        cmdSetOverlay = new Command("Set Overlays", Command.ITEM, 3);
        cmdNoEffect = new Command("No Effect", Command.ITEM, 4);
        cmdMonochrome = new Command("Monochrome Effect", Command.ITEM, 4);
        cmdNegative = new Command("Negative Effect", Command.ITEM, 4);
        cmdEmboss = new Command("Emboss Effect", Command.ITEM, 4);
        cmdSepia = new Command("Sepia Effect", Command.ITEM, 4);
        cmdSolarize = new Command("Solarize Effect", Command.ITEM, 4);
        cmdRedEye = new Command("RedEye Effect", Command.ITEM, 4);
        
        //fill dataForm with components
        inputFormatSelector = new ChoiceGroup(
                "Input Object Type", 
                Choice.EXCLUSIVE);
        inputFormatSelector.append("Image object", null);
        inputFormatSelector.append("JPEG stream", null);
        inputFormatSelector.append("PNG stream", null);
        inputFormatSelector.setSelectedIndex(indexInputImageFormat, true);
        inputFormatSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        
        outputFormatSelector = new ChoiceGroup(
                "Output Image Format", 
                Choice.EXCLUSIVE);
        outputFormatSelector.append("image/raw", null);
        outputFormatSelector.append("image/jpeg", null);
        outputFormatSelector.append("image/png", null);
        outputFormatSelector.setSelectedIndex(indexOutputImageFormat, true);
        outputFormatSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        
        dataForm.append(inputFormatSelector);
        dataForm.append(outputFormatSelector);
        
        dataForm.addCommand(cmdExit);
        dataForm.addCommand(cmdDone);
        
        //fill orderForm with components
        effectOrder = new TextField[3];
        for (i = 0; i < effectOrder.length; ++i) {
            effectOrder[i] = new TextField(order_name_array[i] + "\n", 
                    Integer.toString(indexEffectOrder[i]), 
                    10, 
                    TextField.NUMERIC);
            effectOrder[i].setLayout(
                Item.LAYOUT_2 | 
                Item.LAYOUT_NEWLINE_AFTER | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | //Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_TOP);
            orderForm.append(effectOrder[i]);
        }
        
        StringItem orderNote = new StringItem("Attention!", 
                "\nAlthough you are allowed" +
                "\nto set any integer values," +
                "\nAll input values" +
                "\nwill be converted" +
                "\nto range[0..2]" +
                "\nby implementation.\n");
        orderNote.setLayout(
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_BOTTOM);
        orderForm.append(orderNote);

        orderForm.addCommand(cmdExit);
        orderForm.addCommand(cmdDone);
        
        //fill scaleForm with components
        widthScaleSelector = new ChoiceGroup(
                "Width Scale", 
                Choice.EXCLUSIVE);
        for (i = 0; i < scale_array.length; ++i) {
            widthScaleSelector.append(
                    Integer.toString(scale_array[i]) + "%", null);
        }
        widthScaleSelector.setSelectedIndex(indexOutputScaleW, true);
        widthScaleSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        /*
        widthScaleSelector.setLayout(
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_TOP);
         */

        heightScaleSelector = new ChoiceGroup(
                "Height Scale", 
                Choice.EXCLUSIVE);
        for (i = 0; i < scale_array.length; ++i) {
            heightScaleSelector.append(
                    Integer.toString(scale_array[i]) + "%", null);
        }
        heightScaleSelector.setSelectedIndex(indexOutputScaleH, true);
        heightScaleSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        /*
        heightScaleSelector.setLayout(
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_TOP);
         */

        borderScaleSelector = new ChoiceGroup(
                "Enlarge Source Image", 
                Choice.MULTIPLE);
        borderScaleSelector.append("Generate Border", null);
        borderScaleSelector.setSelectedFlags(indexOutputBorder);
        borderScaleSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        /*
        borderScaleSelector.setLayout(
                Item.LAYOUT_NEWLINE_BEFORE | 
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
         */
        
        rotateScaleSelector = new ChoiceGroup(
                "Rotate Target Image", 
                Choice.EXCLUSIVE);
                rotateScaleSelector.append("No rotation", null);
                rotateScaleSelector.append("Clockwise 90", null);
                rotateScaleSelector.append("Clockwise 180", null);
                rotateScaleSelector.append("Clockwise 270", null);
        rotateScaleSelector.setSelectedIndex(indexOutputRotate, true);
        rotateScaleSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        /*
        rotateScaleSelector.setLayout(
                Item.LAYOUT_NEWLINE_BEFORE | 
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_BOTTOM);
         */
        
        scaleForm.append(widthScaleSelector);
        scaleForm.append(heightScaleSelector);
        scaleForm.append(borderScaleSelector);
        scaleForm.append(rotateScaleSelector);
        
        scaleForm.addCommand(cmdExit);
        scaleForm.addCommand(cmdDone);
        
        //fill effectForm with components
        effectSelector = new ChoiceGroup(
                "Image Effect", 
                Choice.EXCLUSIVE);
        effectSelector.append("No Effects", null);
        effectSelector.append("Monochrome", null);
        effectSelector.append("Negative", null);
        effectSelector.append("Emboss", null);
        effectSelector.append("Sepia", null);
        effectSelector.append("Solarize", null);
        effectSelector.append("Red Eye Reduction", null);
        effectSelector.setSelectedIndex(indexImageEffect, true);
        effectSelector.setFitPolicy(Choice.TEXT_WRAP_OFF);
        /*
        effectSelector.setLayout(
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
         */
        
        effectForm.append(effectSelector);
        
        effectForm.addCommand(cmdExit);
        effectForm.addCommand(cmdDone);
        
        //fill overlayForm with components
        overlaySelector = new ChoiceGroup(
                "Draw Image Overlays", 
                Choice.MULTIPLE);
        overlaySelector.append("#1 (horizontal)", null);
        overlaySelector.append("#2 (vertical)", null);
        overlaySelector.setSelectedFlags(indexOutputOverlay);
        
        overlayForm.append(overlaySelector);
        
        overlayColor = new TextField[2][4];
        for (i = 0; i < 2; ++i) {
            for (j = 0; j < 4; ++j) {
                overlayColor[i][j] = 
                    new TextField(color_name_array[j] + "#" + (i+1), "", 3, TextField.NUMERIC);
                overlayColor[i][j].setString(Integer.toString(argb_color[i][j] & 0xFF));
                overlayColor[i][j].setLayout(
                        Item.LAYOUT_2 | 
                        //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                        Item.LAYOUT_SHRINK | //Item.LAYOUT_VSHRINK | 
                        //Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER |
                        ((j == 3) ? Item.LAYOUT_NEWLINE_AFTER : 0));
               overlayForm.append(overlayColor[i][j]);
           }
        }
        StringItem overlayNote = new StringItem("Attention!", 
                "\nAll input values" +
                "\nwill be converted" +
                "\nto range[0..255]\n");
        overlayNote.setLayout(
                Item.LAYOUT_2 | 
                //Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | 
                Item.LAYOUT_SHRINK | Item.LAYOUT_VSHRINK | 
                Item.LAYOUT_CENTER | Item.LAYOUT_BOTTOM);
        overlayForm.append(overlayNote);
                
        overlayForm.addCommand(cmdExit);
        overlayForm.addCommand(cmdDone);
       
        resultViewer.addCommand(cmdExit);
        resultViewer.addCommand(cmdSetData);
        resultViewer.addCommand(cmdSetOrder);
        resultViewer.addCommand(cmdSetScale);
        resultViewer.addCommand(cmdSetEffect);
        resultViewer.addCommand(cmdSetOverlay);
        
        //ADD shortcuts to some effects (instead of Form invokation)
        //resultViewer.addCommand(cmdNoEffect);
        resultViewer.addCommand(cmdMonochrome);
        resultViewer.addCommand(cmdNegative);
        //resultViewer.addCommand(cmdEmboss);
        resultViewer.addCommand(cmdSepia);
        resultViewer.addCommand(cmdSolarize);
        //resultViewer.addCommand(cmdRedEye);
        
        //set form event listeners
        dataForm.setCommandListener(this);
        orderForm.setCommandListener(this);
        scaleForm.setCommandListener(this);
        effectForm.setCommandListener(this);
        overlayForm.setCommandListener(this);
        resultViewer.setCommandListener(this);
        
        /*
        changeOriginalImageItem();
        currentForm = resultViewer;
        */
        currentForm = dataForm;
        
    }

    public void startApp() {
        //display original image
        display.setCurrent(currentForm);
    }

    public void pauseApp() {

    }

    public void destroyApp(boolean unconditional) {

    }

    public void commandAction(Command c, Displayable s) {
        if (c == cmdExit) {
            destroyApp(true);
            notifyDestroyed();
            
        } else if (c == cmdSetData) {
            currentForm = dataForm;
            display.setCurrent(currentForm);
            
        } else if (c == cmdSetOrder) {
            currentForm = orderForm;
            display.setCurrent(currentForm);
            
        } else if (c == cmdSetScale) {
            currentForm = scaleForm;
            display.setCurrent(currentForm);
            
        } else if (c == cmdSetEffect) {
            currentForm = effectForm;
            display.setCurrent(currentForm);
            
        } else if (c == cmdSetOverlay) {
            currentForm = overlayForm;
            display.setCurrent(currentForm);
            
        } else {
            boolean input_changed = first_form;
            boolean output_changed = first_form;
            
            if (c == cmdDone) {
                // check if currentForm == dataForm or scaleForm or ...
                if (currentForm == dataForm) {
                    int input_format_index = 
                            inputFormatSelector.getSelectedIndex();
                    int output_format_index = 
                            outputFormatSelector.getSelectedIndex();

                    if (input_format_index >= 0 && input_format_index < format_array.length) {
                        input_changed = input_changed ||
                                (input_format_index != indexInputImageFormat);
                        indexInputImageFormat = input_format_index;
                        need_input_stream = (indexInputImageFormat != 0);
                    }

                    if (output_format_index >= 0 && output_format_index < format_array.length) {
                        output_changed = output_changed ||
                                (output_format_index != indexOutputImageFormat);
                        indexOutputImageFormat = output_format_index;
                        need_output_stream = (indexOutputImageFormat != 0);
                    }
                    
                } else if (currentForm == orderForm) {
                    for (int i = 0; i < effectOrder.length; ++i) {
                        int value;
                        try {
                            value = Integer.parseInt(effectOrder[i].getString());
                        } catch (NumberFormatException e) {
                            value = i;
                        }
                        output_changed = output_changed ||
                                (value != indexEffectOrder[i]);
                        indexEffectOrder[i] = value;
                    }
                    
                } else if (currentForm == scaleForm) {
                    int width_index = 
                        widthScaleSelector.getSelectedIndex();
                    int height_index = 
                        heightScaleSelector.getSelectedIndex();
                    boolean border_flags[] = new boolean[/*1*/indexOutputBorder.length];
                    int border_number = 
                            borderScaleSelector.getSelectedFlags(border_flags);
                    int rotate_index = 
                        rotateScaleSelector.getSelectedIndex();
                    
                    if (width_index >= 0 && width_index < scale_array.length) {
                        output_changed = output_changed ||
                                (width_index != indexOutputScaleW);
                        indexOutputScaleW = width_index;
                    }
                    if (height_index >= 0 && height_index < scale_array.length) {
                        output_changed = output_changed ||
                                (height_index != indexOutputScaleH);
                        indexOutputScaleH = height_index;
                    }
                    if (border_number >= 0 && border_number <= borderScaleSelector.size()) {
                        output_changed =  output_changed ||
                                (indexOutputBorder[0] != border_flags[0]);
                        indexOutputBorder = border_flags;
                    }
                    if (rotate_index >= 0 && rotate_index <= 3) {
                        output_changed = output_changed ||
                                (rotate_index != indexOutputRotate);
                        indexOutputRotate = rotate_index;
                    }
                    
                } else if (currentForm == effectForm) {
                    int effect_index = 
                        effectSelector.getSelectedIndex();
                    
                    if (effect_index >= 0 && effect_index < effect_array.length) {
                        output_changed = output_changed || 
                                (effect_index != indexImageEffect);
                        indexImageEffect = effect_index;
                    }
                    
                } else if (currentForm == overlayForm) {
                    boolean overlay_flags[] = new boolean[/*2*/indexOutputOverlay.length];
                    int overlay_number = 
                            overlaySelector.getSelectedFlags(overlay_flags);
                    if (overlay_number >= 0 && overlay_number <= overlaySelector.size()) {
                        output_changed =  output_changed ||
                                (indexOutputOverlay[0] != overlay_flags[0]) ||
                                (indexOutputOverlay[1] != overlay_flags[1]);
                        indexOutputOverlay = overlay_flags;
                    }
                    
                    for (int i = 0; i < 2; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            int value;
                            try {
                                value = Integer.parseInt(overlayColor[i][j].getString());
                                value = Math.max(value, 0x00);
                                value = Math.min(value, 0xFF);
                                output_changed = output_changed ||
                                        (argb_color[i][j] != value);
                                argb_color[i][j] = value;
                            } catch (NumberFormatException e) {
                                value = argb_color[i][j] & 0xFF;
                            }
                            overlayColor[i][j].setString(Integer.toString(value));
                        }
                    }
                }
                
                currentForm = resultViewer;
                display.setCurrent(currentForm);
                first_form = false;
                
            } else if (c == cmdNoEffect) {
                indexImageEffect = 0;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else if (c == cmdMonochrome) {
                indexImageEffect = 1;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else if (c == cmdNegative) {
                indexImageEffect = 2;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else if (c == cmdEmboss) {
                indexImageEffect = 3;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else if (c == cmdSepia) {
                indexImageEffect = 4;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else if (c == cmdSolarize) {
                indexImageEffect = 5;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else if (c == cmdRedEye) {
                indexImageEffect = 6;
                effectSelector.setSelectedIndex(indexImageEffect, true);
                output_changed = true;
            } else {
            }

            if (input_changed) {
                changeOriginalImageItem();
            };
            if (input_changed || output_changed) {
                //run test
                createProcessedImage();

                if (!need_callback) {
                    // replace old image (if any) with new one
                    changeProcessedImageItem();
                }
            }
        }
    }
    
    //to implement MediaProcessorListener
    public void mediaProcessorUpdate(MediaProcessor processor,
                                     String event,
                                     Object eventData) {
        if (need_callback) {
            if (event == MediaProcessorListener.PROCESSING_COMPLETED) {
                final int[] processed_size = getProcessedImageSize();
                processedImage = createStreamARGBImage(
                    outputStream, need_output_stream, 
                    processed_size[0],
                    processed_size[1]);
                changeProcessedImageItem();
            }
            else if (event == MediaProcessorListener.PROCESSING_ABORTED ||
                     event == MediaProcessorListener.PROCESSING_ERROR ||
                     event == MediaProcessorListener.PROCESSING_STOPPED) {
                processedImage = createTestImage();
                changeProcessedImageItem();
            } else if (event == MediaProcessorListener.PROCESSING_STARTED) {
            } else if (event == MediaProcessorListener.PROCESSOR_REALIZED) {
            }
        }
        //debug print to console
        System.out.println(event);
    }

    private void changeOriginalImageItem() {
        // negative to show that there is no processed images to show
        processedImageItemIndex = -1;

        // prepare source image 
        inputStream = getClass().getResourceAsStream(stream_array[indexInputImageFormat]);
        try {
            sourceImage = Image.createImage(inputStream);
        } catch (java.io.IOException e) {
            System.out.println("IOException: " + 
                "Unable to get resource! Generating test image!");
            sourceImage = createTestImage();
        };
        
        resultViewer.deleteAll();
        resultViewer.append(new ImageItem("Original Image:\n"+
            "format=" + format_array[indexInputImageFormat], 
            sourceImage,   //the image to append
            ImageItem.LAYOUT_CENTER |
            ImageItem.LAYOUT_NEWLINE_BEFORE | ImageItem.LAYOUT_NEWLINE_AFTER, 
            null));
    }
    
    private int[] getProcessedImageSize() {
        int size[] = new int[2];
        size[0] = sourceImage.getWidth() * scale_array[indexOutputScaleW] / 100;
        size[1] = sourceImage.getHeight() * scale_array[indexOutputScaleH] / 100;
        //swap width & height in case of rotation to 90 & 270 degrees
        if ((indexOutputRotate & 0x01) != 0) {
            final int temp = size[0];
            size[0] = size[1];
            size[1] = temp;
        };
        return size; //[0] contains width & [1] contains height
    }
            
    private void changeProcessedImageItem() {
        //remove previous processed image 
        if (processedImageItemIndex >= 0) {
            resultViewer.delete(processedImageItemIndex);
            processedImageItemIndex = -1;
        }
        
        //display result
        processedImageItemIndex = 
            resultViewer.append(new ImageItem("Processed Image:\n" + 
            "format=" + format_array[indexOutputImageFormat] + " " +
            "effect=" + effect_array[indexImageEffect], 
            processedImage,   //the image to append
            ImageItem.LAYOUT_CENTER |
            ImageItem.LAYOUT_NEWLINE_BEFORE | ImageItem.LAYOUT_NEWLINE_AFTER, 
            null));
    }
    
    private void createProcessedImage() {

        try {

            MediaProcessor mp = GlobalManager.createMediaProcessor(
                    "image/" + format_array[indexInputImageFormat]);

            mp.addMediaProcessorListener(this);

            // create a OutputStream that will receive the resulting image
            outputStream = new ByteArrayOutputStream();

            if (need_input_stream) {
                //need to create stream again or rewind it ...
                inputStream = getClass().getResourceAsStream(stream_array[indexInputImageFormat]);
                mp.setInput(inputStream, MediaProcessor.UNKNOWN);
            } else {
                mp.setInput(sourceImage);
            }
            mp.setOutput(outputStream);
 
            // Define effects to be applied during processing
            ImageTransformControl itc = (ImageTransformControl)mp.getControl(
                "javax.microedition.amms.control.imageeffect.ImageTransformControl");
            ImageEffectControl iec = (ImageEffectControl)mp.getControl(
                "javax.microedition.amms.control.imageeffect.ImageEffectControl");
            OverlayControl ioc = (OverlayControl)mp.getControl(
                "javax.microedition.amms.control.imageeffect.OverlayControl");
            ImageFormatControl ifc = (ImageFormatControl)mp.getControl(
                "javax.microedition.amms.control.ImageFormatControl");
            EffectOrderControl eoc = (EffectOrderControl)mp.getControl(
                "javax.microedition.amms.control.EffectOrderControl");
            
            //ImageTransformControl settings
            {
                // if user requested border, then source image to transform will be increased, 
                // and thus processed image will have black rectangle around it
                final int border = indexOutputBorder[0] ? 10 : 0;
                itc.setSourceRect(
                    -border, -border, 
                    itc.getSourceWidth() + 2 * border, 
                    itc.getSourceHeight() + 2 * border);
                itc.setTargetSize(
                    itc.getSourceWidth() * scale_array[indexOutputScaleW] / 100,
                    itc.getSourceHeight() * scale_array[indexOutputScaleH] / 100, 
                    indexOutputRotate * 90);
                itc.setEnabled(true);
            }
                
            //ImageEffectControl settings
            if (indexImageEffect != 0) {
                iec.setPreset(effect_array[indexImageEffect]);
                iec.setEnabled(true);
            }
            else {
                iec.setEnabled(false);
            }
                
            //OverlayControl settings
            {
                final int processed_size[] = getProcessedImageSize();
                boolean ioc_enable = false;
                ioc.clear();
                for (int j = 0; j < 2; ++j) { 
                    if (indexOutputOverlay[j]) {
                        int color = 
                                (argb_color[j][0] << 24) | 
                                (argb_color[j][1] << 16) | 
                                (argb_color[j][2] <<  8) | 
                                (argb_color[j][3] <<  0); 
                        ioc_enable = true;
                        for (int i = 0; i < argb_buffer.length; ++i) {
                            argb_buffer[i] = color;
                        }
                        Image overlay_image = Image.createRGBImage(
                                argb_buffer, 
                                overlay_size[j], 
                                overlay_size[1-j], 
                                true);
                        int order = 
                        ioc.insertImage(overlay_image, 
                                (processed_size[0] - overlay_size[j]) / 2, 
                                (processed_size[1] - overlay_size[1-j]) / 2, 
                                j);
                    }
                }
                ioc.setEnabled(ioc_enable);
            }
            
            //ImageFormatControl settings
            ifc.setFormat("image/" + format_array[indexOutputImageFormat]);
            
            //EffectOrderControl settings
            {
                indexEffectOrder[0] = eoc.setEffectOrder(itc, indexEffectOrder[0]);
                indexEffectOrder[1] = eoc.setEffectOrder(iec, indexEffectOrder[1]);
                indexEffectOrder[2] = eoc.setEffectOrder(ioc, indexEffectOrder[2]);
                effectOrder[0].setString(Integer.toString(indexEffectOrder[0]));
                effectOrder[1].setString(Integer.toString(indexEffectOrder[1]));
                effectOrder[2].setString(Integer.toString(indexEffectOrder[2]));
            };

 
            // Do the actual processing. If you do not want to use a blocking call, 
            // use start() and MediaProcessorListener.
            if (need_callback) {
                mp.start();
            } else {
                mp.complete();

                final int[] processed_size = getProcessedImageSize();
                processedImage = createStreamARGBImage(
                    outputStream, need_output_stream, 
                    processed_size[0],
                    processed_size[1]);
            }

        } catch (MediaException me) {
            me.printStackTrace();
            System.out.println("MediaException: " + 
                "Unable to create processed image !");
            processedImage = createTestImage();
        }

    }

    private static Image createTestImage() {

        Image image  = Image.createImage(72, 72);

        Graphics g = image.getGraphics();

        g.setColor(0xff0000);
        g.fillRect(0, 0, 
                   image.getWidth(), image.getHeight()/3);

        g.setColor(0x00ff00);
        g.fillRect(0, image.getHeight()/3, 
                   image.getWidth(), image.getHeight()/3);

        g.setColor(0x0000ff);
        g.fillRect(0, (image.getHeight()*2)/3, 
                   image.getWidth(), image.getHeight()/3);

        return image;
    }

    private static Image createStreamARGBImage(
            ByteArrayOutputStream os, boolean from_stream, int w, int h) {
        byte[] bytearray = os.toByteArray();
        
        if (from_stream) {
            //return Image.createImage(bytearray, 0, bytearray.length);
            try {
            return Image.createImage(
                    new ByteArrayInputStream(bytearray, 0, bytearray.length));
            } catch (java.io.IOException e) {
                System.out.println("IOException: " + 
                    "Unable to get input stream! Generating test image!");
                return createTestImage();
            }
        } else {
            return Image.createRGBImage(
                byteArrayToIntArray(bytearray), w, h, true);
        }
    }

    private static int[] byteArrayToIntArray(byte[] bytes) {
        int[] ints = new int[bytes.length / 4];

        int intcount, bytecount;
        for (intcount = 0, bytecount = 0; bytecount < bytes.length; ) {
            ints[intcount] = 
                (( ((int)(bytes[bytecount + 0])) << 24) & 0xFF000000) |  //A
                (( ((int)(bytes[bytecount + 1])) << 16) & 0x00FF0000) |  //R
                (( ((int)(bytes[bytecount + 2])) << 8)  & 0x0000FF00) |  //G
                (( ((int)(bytes[bytecount + 3]))        & 0x000000FF) ); //B

            intcount++;
            bytecount+=4;

        }
        return ints;
    }
}    


