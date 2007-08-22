--------------------------------------------------------------------------------
     Scalable 2D Vector Graphics API for J2ME (JSR 226 API) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    This suite contains MIDlets that demonstrate different ways of using the JSR 226 
    Scalable 2D Vector Graphics API for J2ME. This API provides ways to load, 
    manipulate, render, and play SVG content.


2. SVGDemo

    2.1 SVG Browser
        The SVGBrowser MIDlet displays SVG files residing in the phone file system. 
        - Before running this demo, place an SVG file in the following directory:
          <WTK_HOME>/appdb/DefaultColorPhone/filesystem/root1
        - Launch the demo. The application displays the contents of root1. 
        - Select your SVG file and choose the Open soft key. 
        
    2.2 Render SVG Image
        Render SVG Image loads an SVG image from a file and renders it. 
        Looking at the demo code you can see that the image is sized on the fly 
        to exactly fit the display area. The output is clear and sharp. 
        
    2.3 Play SVG Animation
        This application plays an SVG animation depicting a Halloween greeting card. 
        - Press 8 to play, 5 to start, and 0 to stop. If you press 8, pressing 5 
          resumes the animation. If you press 0, pressing 5 starts the animation 
          from the beginning.
        
        The SVG file contains a description of how the various image elements evolve 
        over time to provide this short animation.

    2.4 Create SVG Image from Scratch
        This demo builds an image using API calls. It creates an empty SVGImage, 
        populates it with a graphical content, and then displays that content. 
        
    2.5 Bouncing Balls
        Bouncing Balls plays an SVG animation. 
        - Press 8 to play, 5 to start, and 0 to stop. If you press 8, pressing 5 
          resumes the animation. If you press 0, pressing 5 starts the animation 
          from the beginning. 
          
    2.6 Optimized Menu
        In this demo, selected icons have a yellow border. As you move to a new 
        icon, it becomes selected and the previous icon flips to the unselected state. 
        If you navigate off the icon grid, selection loops around. That is, if the last 
        icon in a row is selected, moving right selects the first icon in the same row.

        This demo illustrates the flexibility that combining UI markup and Java 
        offers: a rich set of functionality (graphics, animations, high-end 2D rendering) 
        and flexibility in graphic manipulation, pre-rendering or playing.

        In this example, a graphic artist delivered an SVG animation defining the transition 
        state for the menu icons, from the unselected state to the selected state. 
        The program renders each icon's animation sequence separately into off-screen 
        buffers (for faster rendering later on), using the JSR 226 API.

        With buffering, the MIDlet is able to adapt to the device display resolution 
        (because the graphics are defined in SVG format) and still retain the speed of 
        bitmap rendering. In addition, the MIDlet is still leveraging the SVG animation 
        capabilities.

        The task of defining the look of the menu items and their animation effect 
        (the job of the graphic artist and designer) is cleanly separated from the task 
        of displaying the menu and starting actions based on menu selection (the job of 
        the developer). The two can vary independently as long as both the artist and 
        the developer observe the SVG document structure conventions. 
        
    2.7 Picture Decorator
        In this demo you use the phone keys to add decorations to a photograph.
        This demo provides 16 pictures for you to decorate.
        - Use the 2 and 6 keys to page forward and back through the photos.
        - To decorate, press # to display the picker. Use the arrow keys to highlight 
          a graphic object. The highlighted object is enlarged. Press SELECT to choose 
          the current graphic or press the arrow keys to highlight a different graphic. 
          Press SELECT again to add the graphic to the photo. When the decoration is added 
          you see a red + on the graphic. This means it is selected and can be moved, 
          resized, and manipulated. 
        - Use the navigation arrows to move the graphic. Use 1 to shrink the graphic, 
          and 3 to enlarge the graphic. Use 5 or 6 to flip, and 7 or 9 to rotate. 
          When you are satisfied with the position, press SELECT. Note that a green 
          triangle appears. This is a cursor. Use the navigation keys to move the green 
          triangle around the picture. When the cursor is over an object it is highlighted 
          with a red box. Press SELECT. The red + indicates the object is selected.
        - To remove a decoration (a property), select an object, then click the Menu 
          soft key. Press 2 to remove a property.
          
    2.8 Location Based Service
        Launch the application. A splash screen (also used as the help) appears. 
        The initial view is a map of your itinerary - a walk through San Francisco. 
        The bay (in blue) is on the right of your screen. Press 1 to start following 
        the itinerary. The application zooms in on your location on the map. 
        Turn-by-turn directions appear in white boxes on the horizontal axis. 
        While the itinerary is running, Press 7 to rotate the map counter-clockwise. 
        Note, the map rotates and the text now appears on the vertical axis. 
        Press 7 again to restore the default orientation. Press 4 to display the help screen. 


3. Required APIs
    
    JSR 139 - Connected Limited Device Configuration (CLDC) 1.1
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 172 - J2ME Web Services Specification
    JSR 226 - Scalable 2D Vector Graphics API for J2ME
    
