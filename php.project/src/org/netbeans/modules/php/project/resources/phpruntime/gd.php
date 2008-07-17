<?php

// Start of gd v.

/**
 * Retrieve information about the currently installed GD library
 * @link http://php.net/manual/en/function.gd-info.php
 * @return array an associative array.
 * </p>
 * <p>
 * <table>
 * Elements of array returned by gd_info
 * <tr valign="top">
 * <td>Attribute</td>
 * <td>Meaning</td>
 * </tr>
 * <tr valign="top">
 * <td>GD Version</td>
 * <td>string value describing the installed
 * libgd version.</td>
 * </tr>
 * <tr valign="top">
 * <td>Freetype Support</td>
 * <td>boolean value. true
 * if Freetype Support is installed.</td>
 * </tr>
 * <tr valign="top">
 * <td>Freetype Linkage</td>
 * <td>string value describing the way in which
 * Freetype was linked. Expected values are: 'with freetype',
 * 'with TTF library', and 'with unknown library'. This element will
 * only be defined if Freetype Support evaluated to
 * true.</td>
 * </tr>
 * <tr valign="top">
 * <td>T1Lib Support</td>
 * <td>boolean value. true
 * if T1Lib support is included.</td>
 * </tr>
 * <tr valign="top">
 * <td>GIF Read Support</td>
 * <td>boolean value. true
 * if support for reading GIF
 * images is included.</td>
 * </tr>
 * <tr valign="top">
 * <td>GIF Create Support</td>
 * <td>boolean value. true
 * if support for creating GIF
 * images is included.</td>
 * </tr>
 * <tr valign="top">
 * <td>JPG Support</td>
 * <td>boolean value. true
 * if JPG support is included.</td>
 * </tr>
 * <tr valign="top">
 * <td>PNG Support</td>
 * <td>boolean value. true
 * if PNG support is included.</td>
 * </tr>
 * <tr valign="top">
 * <td>WBMP Support</td>
 * <td>boolean value. true
 * if WBMP support is included.</td>
 * </tr>
 * <tr valign="top">
 * <td>XBM Support</td>
 * <td>boolean value. true
 * if XBM support is included.</td>
 * </tr>
 * </table>
 * </p>
 */
function gd_info () {}

/**
 * Draws an arc
 * @link http://php.net/manual/en/function.imagearc.php
 * @param image resource 
 * @param cx int <p>
 * x-coordinate of the center
 * </p>
 * @param cy int <p>
 * y-coordinate of the center
 * </p>
 * @param width int <p>
 * The arc width
 * </p>
 * @param height int <p>
 * The arc height
 * </p>
 * @param start int <p>
 * The arc start angle, in degrees.
 * </p>
 * @param end int <p>
 * The arc end angle, in degrees.
 * 0&deg; is located at the three-o'clock position, and the arc is drawn
 * clockwise.
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagearc ($image, $cx, $cy, $width, $height, $start, $end, $color) {}

/**
 * Draw an ellipse
 * @link http://php.net/manual/en/function.imageellipse.php
 * @param image resource 
 * @param cx int <p>
 * x-coordinate of the center
 * </p>
 * @param cy int <p>
 * y-coordinate of the center
 * </p>
 * @param width int <p>
 * The ellipse width
 * </p>
 * @param height int <p>
 * The ellipse height
 * </p>
 * @param color int <p>
 * The color of the ellipse. A color identifier created with
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imageellipse ($image, $cx, $cy, $width, $height, $color) {}

/**
 * Draw a character horizontally
 * @link http://php.net/manual/en/function.imagechar.php
 * @param image resource 
 * @param font int 
 * @param x int <p>
 * x-coordinate of the start
 * </p>
 * @param y int <p>
 * y-coordinate of the start
 * </p>
 * @param c string <p>
 * The character to draw
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagechar ($image, $font, $x, $y, $c, $color) {}

/**
 * Draw a character vertically
 * @link http://php.net/manual/en/function.imagecharup.php
 * @param image resource 
 * @param font int 
 * @param x int <p>
 * x-coordinate of the start
 * </p>
 * @param y int <p>
 * y-coordinate of the start
 * </p>
 * @param c string <p>
 * The character to draw
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecharup ($image, $font, $x, $y, $c, $color) {}

/**
 * Get the index of the color of a pixel
 * @link http://php.net/manual/en/function.imagecolorat.php
 * @param image resource 
 * @param x int <p>
 * x-coordinate of the point
 * </p>
 * @param y int <p>
 * y-coordinate of the point
 * </p>
 * @return int the index of the color.
 * </p>
 */
function imagecolorat ($image, $x, $y) {}

/**
 * Allocate a color for an image
 * @link http://php.net/manual/en/function.imagecolorallocate.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @return int A color identifier or false if the allocation failed.
 * </p>
 */
function imagecolorallocate ($image, $red, $green, $blue) {}

/**
 * Copy the palette from one image to another
 * @link http://php.net/manual/en/function.imagepalettecopy.php
 * @param destination resource <p>
 * The destination image resource
 * </p>
 * @param source resource <p>
 * The source image resource
 * </p>
 * @return void &return.void;
 * </p>
 */
function imagepalettecopy ($destination, $source) {}

/**
 * Create a new image from the image stream in the string
 * @link http://php.net/manual/en/function.imagecreatefromstring.php
 * @param data string 
 * @return resource An image resource will be returned on success. false is returned if
 * the image type is unsupported, the data is not in a recognised format,
 * or the image is corrupt and cannot be loaded.
 * </p>
 */
function imagecreatefromstring ($data) {}

/**
 * Get the index of the closest color to the specified color
 * @link http://php.net/manual/en/function.imagecolorclosest.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @return int the index of the closest color, in the palette of the image, to
 * the specified one
 * </p>
 */
function imagecolorclosest ($image, $red, $green, $blue) {}

/**
 * Get the index of the color which has the hue, white and blackness
 * @link http://php.net/manual/en/function.imagecolorclosesthwb.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @return int an integer with the index of the color which has 
 * the hue, white and blackness nearest the given color.
 * </p>
 */
function imagecolorclosesthwb ($image, $red, $green, $blue) {}

/**
 * De-allocate a color for an image
 * @link http://php.net/manual/en/function.imagecolordeallocate.php
 * @param image resource 
 * @param color int <p>
 * The color identifier
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecolordeallocate ($image, $color) {}

/**
 * Get the index of the specified color or its closest possible alternative
 * @link http://php.net/manual/en/function.imagecolorresolve.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @return int a color index.
 * </p>
 */
function imagecolorresolve ($image, $red, $green, $blue) {}

/**
 * Get the index of the specified color
 * @link http://php.net/manual/en/function.imagecolorexact.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @return int the index of the specified color in the palette, or -1 if the
 * color does not exist.
 * </p>
 */
function imagecolorexact ($image, $red, $green, $blue) {}

/**
 * Set the color for the specified palette index
 * @link http://php.net/manual/en/function.imagecolorset.php
 * @param image resource 
 * @param index int <p>
 * An index in the palette
 * </p>
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @return void &return.void;
 * </p>
 */
function imagecolorset ($image, $index, $red, $green, $blue) {}

/**
 * Define a color as transparent
 * @link http://php.net/manual/en/function.imagecolortransparent.php
 * @param image resource 
 * @param color int[optional] <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return int The identifier of the new (or current, if none is specified)
 * transparent color is returned.
 * </p>
 */
function imagecolortransparent ($image, $color = null) {}

/**
 * Find out the number of colors in an image's palette
 * @link http://php.net/manual/en/function.imagecolorstotal.php
 * @param image resource 
 * @return int the number of colors in the specified image's palette or 0 for
 * truecolor images.
 * </p>
 */
function imagecolorstotal ($image) {}

/**
 * Get the colors for an index
 * @link http://php.net/manual/en/function.imagecolorsforindex.php
 * @param image resource 
 * @param index int <p>
 * </p>
 * @return array an associative array with red, green, blue and alpha keys that
 * contain the appropriate values for the specified color index.
 * </p>
 */
function imagecolorsforindex ($image, $index) {}

/**
 * Copy part of an image
 * @link http://php.net/manual/en/function.imagecopy.php
 * @param dst_im resource <p>
 * Destination image link resource
 * </p>
 * @param src_im resource <p>
 * Source image link resource
 * </p>
 * @param dst_x int <p>
 * x-coordinate of destination point
 * </p>
 * @param dst_y int <p>
 * y-coordinate of destination point
 * </p>
 * @param src_x int <p>
 * x-coordinate of source point
 * </p>
 * @param src_y int <p>
 * y-coordinate of source point
 * </p>
 * @param src_w int <p>
 * Source width
 * </p>
 * @param src_h int <p>
 * Source height
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecopy ($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h) {}

/**
 * Copy and merge part of an image
 * @link http://php.net/manual/en/function.imagecopymerge.php
 * @param dst_im resource <p>
 * Destination image link resource
 * </p>
 * @param src_im resource <p>
 * Source image link resource
 * </p>
 * @param dst_x int <p>
 * x-coordinate of destination point
 * </p>
 * @param dst_y int <p>
 * y-coordinate of destination point
 * </p>
 * @param src_x int <p>
 * x-coordinate of source point
 * </p>
 * @param src_y int <p>
 * y-coordinate of source point
 * </p>
 * @param src_w int <p>
 * Source width
 * </p>
 * @param src_h int <p>
 * Source height
 * </p>
 * @param pct int <p>
 * The two images will be merged according to pct
 * which can range from 0 to 100. When pct = 0,
 * no action is taken, when 100 this function behaves identically
 * to imagecopy for pallete images, while it
 * implements alpha transparency for true colour images.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecopymerge ($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h, $pct) {}

/**
 * Copy and merge part of an image with gray scale
 * @link http://php.net/manual/en/function.imagecopymergegray.php
 * @param dst_im resource <p>
 * Destination image link resource
 * </p>
 * @param src_im resource <p>
 * Source image link resource
 * </p>
 * @param dst_x int <p>
 * x-coordinate of destination point
 * </p>
 * @param dst_y int <p>
 * y-coordinate of destination point
 * </p>
 * @param src_x int <p>
 * x-coordinate of source point
 * </p>
 * @param src_y int <p>
 * y-coordinate of source point
 * </p>
 * @param src_w int <p>
 * Source width
 * </p>
 * @param src_h int <p>
 * Source height
 * </p>
 * @param pct int <p>
 * The two images will be merged according to pct
 * which can range from 0 to 100. When pct = 0,
 * no action is taken, when 100 this function behaves identically
 * to imagecopy for pallete images, while it
 * implements alpha transparency for true colour images.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecopymergegray ($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h, $pct) {}

/**
 * Copy and resize part of an image
 * @link http://php.net/manual/en/function.imagecopyresized.php
 * @param dst_image resource 
 * @param src_image resource 
 * @param dst_x int <p>
 * x-coordinate of destination point
 * </p>
 * @param dst_y int <p>
 * y-coordinate of destination point
 * </p>
 * @param src_x int <p>
 * x-coordinate of source point
 * </p>
 * @param src_y int <p>
 * y-coordinate of source point
 * </p>
 * @param dst_w int <p>
 * Destination width
 * </p>
 * @param dst_h int <p>
 * Destination height
 * </p>
 * @param src_w int <p>
 * Source width
 * </p>
 * @param src_h int <p>
 * Source height
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecopyresized ($dst_image, $src_image, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h) {}

/**
 * Create a new palette based image
 * @link http://php.net/manual/en/function.imagecreate.php
 * @param width int <p>
 * The image width
 * </p>
 * @param height int <p>
 * The image height
 * </p>
 * @return resource an image resource identifier on success, false on errors.
 * </p>
 */
function imagecreate ($width, $height) {}

/**
 * Create a new true color image
 * @link http://php.net/manual/en/function.imagecreatetruecolor.php
 * @param width int <p>
 * Image width
 * </p>
 * @param height int <p>
 * Image height
 * </p>
 * @return resource an image resource identifier on success, false on errors.
 * </p>
 */
function imagecreatetruecolor ($width, $height) {}

/**
 * Finds whether an image is a truecolor image
 * @link http://php.net/manual/en/function.imageistruecolor.php
 * @param image resource 
 * @return bool true if the image is truecolor, false
 * otherwise.
 * </p>
 */
function imageistruecolor ($image) {}

/**
 * Convert a true color image to a palette image
 * @link http://php.net/manual/en/function.imagetruecolortopalette.php
 * @param image resource 
 * @param dither bool <p>
 * Indicates if the image should be dithered - if it is true then
 * dithering will be used which will result in a more speckled image but
 * with better color approximation.
 * </p>
 * @param ncolors int <p>
 * Sets the maximum number of colors that should be retained in the palette.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagetruecolortopalette ($image, $dither, $ncolors) {}

/**
 * Set the thickness for line drawing
 * @link http://php.net/manual/en/function.imagesetthickness.php
 * @param image resource 
 * @param thickness int <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagesetthickness ($image, $thickness) {}

/**
 * Draw a partial ellipse and fill it
 * @link http://php.net/manual/en/function.imagefilledarc.php
 * @param image resource 
 * @param cx int <p>
 * x-coordinate of the center
 * </p>
 * @param cy int <p>
 * y-coordinate of the center
 * </p>
 * @param width int <p>
 * The arc width
 * </p>
 * @param height int <p>
 * The arc height
 * </p>
 * @param start int <p>
 * The arc start angle, in degrees.
 * </p>
 * @param end int <p>
 * The arc end angle, in degrees.
 * 0&deg; is located at the three-o'clock position, and the arc is drawn
 * clockwise.
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @param style int <p>
 * A bitwise OR of the following possibilities:
 * IMG_ARC_PIE
 * @return bool &return.success;
 * </p>
 */
function imagefilledarc ($image, $cx, $cy, $width, $height, $start, $end, $color, $style) {}

/**
 * Draw a filled ellipse
 * @link http://php.net/manual/en/function.imagefilledellipse.php
 * @param image resource 
 * @param cx int <p>
 * x-coordinate of the center
 * </p>
 * @param cy int <p>
 * y-coordinate of the center
 * </p>
 * @param width int <p>
 * The ellipse width
 * </p>
 * @param height int <p>
 * The ellipse height
 * </p>
 * @param color int <p>
 * The fill color. A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagefilledellipse ($image, $cx, $cy, $width, $height, $color) {}

/**
 * Set the blending mode for an image
 * @link http://php.net/manual/en/function.imagealphablending.php
 * @param image resource 
 * @param blendmode bool <p>
 * Whether to enable the blending mode or not. Default to false.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagealphablending ($image, $blendmode) {}

/**
 * Set the flag to save full alpha channel information (as opposed to single-color transparency) when saving PNG images
 * @link http://php.net/manual/en/function.imagesavealpha.php
 * @param image resource 
 * @param saveflag bool <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagesavealpha ($image, $saveflag) {}

/**
 * Allocate a color for an image
 * @link http://php.net/manual/en/function.imagecolorallocatealpha.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @param alpha int <p>
 * A value between 0 and 127.
 * 0 indicates completely opaque while 
 * 127 indicates completely transparent.
 * </p>
 * @return int A color identifier or false if the allocation failed.
 * </p>
 */
function imagecolorallocatealpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Get the index of the specified color + alpha or its closest possible alternative
 * @link http://php.net/manual/en/function.imagecolorresolvealpha.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @param alpha int <p>
 * A value between 0 and 127.
 * 0 indicates completely opaque while 
 * 127 indicates completely transparent.
 * </p>
 * @return int a color index.
 * </p>
 */
function imagecolorresolvealpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Get the index of the closest color to the specified color + alpha
 * @link http://php.net/manual/en/function.imagecolorclosestalpha.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @param alpha int <p>
 * A value between 0 and 127.
 * 0 indicates completely opaque while 
 * 127 indicates completely transparent.
 * </p>
 * @return int the index of the closest color in the palette.
 * </p>
 */
function imagecolorclosestalpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Get the index of the specified color + alpha
 * @link http://php.net/manual/en/function.imagecolorexactalpha.php
 * @param image resource 
 * @param red int <p>
 * Value of red component
 * </p>
 * @param green int <p>
 * Value of green component
 * </p>
 * @param blue int <p>
 * Value of blue component
 * </p>
 * @param alpha int <p>
 * A value between 0 and 127.
 * 0 indicates completely opaque while 
 * 127 indicates completely transparent.
 * </p>
 * @return int the index of the specified color+alpha in the palette of the
 * image, or -1 if the color does not exist in the image's palette.
 * </p>
 */
function imagecolorexactalpha ($image, $red, $green, $blue, $alpha) {}

/**
 * Copy and resize part of an image with resampling
 * @link http://php.net/manual/en/function.imagecopyresampled.php
 * @param dst_image resource 
 * @param src_image resource 
 * @param dst_x int <p>
 * x-coordinate of destination point
 * </p>
 * @param dst_y int <p>
 * y-coordinate of destination point
 * </p>
 * @param src_x int <p>
 * x-coordinate of source point
 * </p>
 * @param src_y int <p>
 * y-coordinate of source point
 * </p>
 * @param dst_w int <p>
 * Destination width
 * </p>
 * @param dst_h int <p>
 * Destination height
 * </p>
 * @param src_w int <p>
 * Source width
 * </p>
 * @param src_h int <p>
 * Source height
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagecopyresampled ($dst_image, $src_image, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h) {}

/**
 * Set the tile image for filling
 * @link http://php.net/manual/en/function.imagesettile.php
 * @param image resource 
 * @param tile resource <p>
 * The image resource to be used as a tile
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagesettile ($image, $tile) {}

/**
 * Set the brush image for line drawing
 * @link http://php.net/manual/en/function.imagesetbrush.php
 * @param image resource 
 * @param brush resource <p>
 * An image resource
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagesetbrush ($image, $brush) {}

/**
 * Set the style for line drawing
 * @link http://php.net/manual/en/function.imagesetstyle.php
 * @param image resource 
 * @param style array <p>
 * An array of pixel colors. You can use the 
 * IMG_COLOR_TRANSPARENT constant to add a 
 * transparent pixel.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagesetstyle ($image, array $style) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefrompng.php
 * @param filename string <p>
 * Path to the PNG image
 * </p>
 * @return resource an image resource identifier on success, false on errors.
 * </p>
 */
function imagecreatefrompng ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgif.php
 * @param filename string <p>
 * Path to the GIF image
 * </p>
 * @return resource an image resource identifier on success, false on errors.
 * </p>
 */
function imagecreatefromgif ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromjpeg.php
 * @param filename string <p>
 * Path to the JPEG image
 * </p>
 * @return resource an image resource identifier on success, false on errors.
 * </p>
 */
function imagecreatefromjpeg ($filename) {}

/**
 * Create a new image from file or URL
 * @link http://php.net/manual/en/function.imagecreatefromwbmp.php
 * @param filename string <p>
 * Path to the WBMP image
 * </p>
 * @return resource an image resource identifier on success, false on errors.
 * </p>
 */
function imagecreatefromwbmp ($filename) {}

/**
 * Create a new image from GD file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgd.php
 * @param filename string <p>
 * </p>
 * @return resource 
 */
function imagecreatefromgd ($filename) {}

/**
 * Create a new image from GD2 file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgd2.php
 * @param filename string <p>
 * </p>
 * @return resource 
 */
function imagecreatefromgd2 ($filename) {}

/**
 * Create a new image from a given part of GD2 file or URL
 * @link http://php.net/manual/en/function.imagecreatefromgd2part.php
 * @param filename string <p>
 * </p>
 * @param srcX int <p>
 * </p>
 * @param srcY int <p>
 * </p>
 * @param width int <p>
 * </p>
 * @param height int <p>
 * </p>
 * @return resource 
 */
function imagecreatefromgd2part ($filename, $srcX, $srcY, $width, $height) {}

/**
 * Output a PNG image to either the browser or a file
 * @link http://php.net/manual/en/function.imagepng.php
 * @param image resource 
 * @param filename string[optional] <p>
 * The path to save the file to. If not set or &null;, the raw image stream
 * will be outputted directly.
 * </p>
 * <p>
 * &null; is invalid if the quality and
 * filters arguments are not used.
 * </p>
 * @param quality int[optional] <p>
 * Compression level: from 0 (no compression) to 9.
 * </p>
 * @param filters int[optional] <p>
 * Allows reducing the PNG file size. It is a bitmask field which may be
 * set to any combination of the PNG_FILTER_XXX 
 * constants. PNG_NO_FILTER or 
 * PNG_ALL_FILTERS may also be used to respectively
 * disable or activate all filters.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagepng ($image, $filename = null, $quality = null, $filters = null) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.imagegif.php
 * @param image resource 
 * @param filename string[optional] <p>
 * The path to save the file to. If not set or &null;, the raw image stream
 * will be outputted directly.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagegif ($image, $filename = null) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.imagejpeg.php
 * @param image resource 
 * @param filename string[optional] <p>
 * The path to save the file to. If not set or &null;, the raw image stream
 * will be outputted directly.
 * </p>
 * <p>
 * To skip this argument in order to provide the 
 * quality parameter, use &null;.
 * </p>
 * @param quality int[optional] <p>
 * quality is optional, and ranges from 0 (worst
 * quality, smaller file) to 100 (best quality, biggest file). The 
 * default is the default IJG quality value (about 75).
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagejpeg ($image, $filename = null, $quality = null) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.imagewbmp.php
 * @param image resource 
 * @param filename string[optional] <p>
 * The path to save the file to. If not set or &null;, the raw image stream
 * will be outputted directly.
 * </p>
 * @param foreground int[optional] <p>
 * You can set the foreground color with this parameter by setting an
 * identifier obtained from imagecolorallocate.
 * The default foreground color is black.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagewbmp ($image, $filename = null, $foreground = null) {}

/**
 * Output GD image to browser or file
 * @link http://php.net/manual/en/function.imagegd.php
 * @param image resource 
 * @param filename string[optional] <p>
 * The path to save the file to. If not set or &null;, the raw image stream
 * will be outputted directly.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagegd ($image, $filename = null) {}

/**
 * Output GD2 image to browser or file
 * @link http://php.net/manual/en/function.imagegd2.php
 * @param image resource 
 * @param filename string[optional] <p>
 * The path to save the file to. If not set or &null;, the raw image stream
 * will be outputted directly.
 * </p>
 * @param chunk_size int[optional] <p>
 * </p>
 * @param type int[optional] <p>
 * Either IMG_GD2_RAW or 
 * IMG_GD2_COMPRESSED. Default is 
 * IMG_GD2_RAW.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagegd2 ($image, $filename = null, $chunk_size = null, $type = null) {}

/**
 * Destroy an image
 * @link http://php.net/manual/en/function.imagedestroy.php
 * @param image resource 
 * @return bool &return.success;
 * </p>
 */
function imagedestroy ($image) {}

/**
 * Apply a gamma correction to a GD image
 * @link http://php.net/manual/en/function.imagegammacorrect.php
 * @param image resource 
 * @param inputgamma float <p>
 * The input gamma
 * </p>
 * @param outputgamma float <p>
 * The output gamma
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagegammacorrect ($image, $inputgamma, $outputgamma) {}

/**
 * Flood fill
 * @link http://php.net/manual/en/function.imagefill.php
 * @param image resource 
 * @param x int <p>
 * x-coordinate of start point
 * </p>
 * @param y int <p>
 * y-coordinate of start point
 * </p>
 * @param color int <p>
 * The fill color. A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagefill ($image, $x, $y, $color) {}

/**
 * Draw a filled polygon
 * @link http://php.net/manual/en/function.imagefilledpolygon.php
 * @param image resource 
 * @param points array <p>
 * An array containing the x and y
 * coordinates of the polygons vertices consecutively
 * </p>
 * @param num_points int <p>
 * Total number of vertices, which must be bigger than 3
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagefilledpolygon ($image, array $points, $num_points, $color) {}

/**
 * Draw a filled rectangle
 * @link http://php.net/manual/en/function.imagefilledrectangle.php
 * @param image resource 
 * @param x1 int <p>
 * x-coordinate for point 1
 * </p>
 * @param y1 int <p>
 * y-coordinate for point 1
 * </p>
 * @param x2 int <p>
 * x-coordinate for point 2
 * </p>
 * @param y2 int <p>
 * y-coordinate for point 2
 * </p>
 * @param color int <p>
 * The fill color. A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagefilledrectangle ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Flood fill to specific color
 * @link http://php.net/manual/en/function.imagefilltoborder.php
 * @param image resource 
 * @param x int <p>
 * x-coordinate of start
 * </p>
 * @param y int <p>
 * y-coordinate of start
 * </p>
 * @param border int <p>
 * The border color. A color identifier created with 
 * imagecolorallocate
 * </p>
 * @param color int <p>
 * The fill color. A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagefilltoborder ($image, $x, $y, $border, $color) {}

/**
 * Get font width
 * @link http://php.net/manual/en/function.imagefontwidth.php
 * @param font int 
 * @return int the width of the pixel
 * </p>
 */
function imagefontwidth ($font) {}

/**
 * Get font height
 * @link http://php.net/manual/en/function.imagefontheight.php
 * @param font int 
 * @return int the height of the pixel.
 * </p>
 */
function imagefontheight ($font) {}

/**
 * Enable or disable interlace
 * @link http://php.net/manual/en/function.imageinterlace.php
 * @param image resource 
 * @param interlace int[optional] <p>
 * If non-zero, the image will be interlaced, else the interlace bit is
 * turned off.
 * </p>
 * @return int 1 if the interlace bit is set for the image, 0 otherwise.
 * </p>
 */
function imageinterlace ($image, $interlace = null) {}

/**
 * Draw a line
 * @link http://php.net/manual/en/function.imageline.php
 * @param image resource 
 * @param x1 int <p>
 * x-coordinate for first point
 * </p>
 * @param y1 int <p>
 * y-coordinate for first point
 * </p>
 * @param x2 int <p>
 * x-coordinate for second point
 * </p>
 * @param y2 int <p>
 * y-coordinate for second point
 * </p>
 * @param color int <p>
 * The line color. A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imageline ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Load a new font
 * @link http://php.net/manual/en/function.imageloadfont.php
 * @param file string <p>
 * The font file format is currently binary and architecture
 * dependent. This means you should generate the font files on the
 * same type of CPU as the machine you are running PHP on.
 * </p>
 * <p>
 * <table>
 * Font file format
 * <tr valign="top">
 * <td>byte position</td>
 * <td>C data type</td>
 * <td>description</td>
 * </tr>
 * <tr valign="top">
 * <td>byte 0-3</td>
 * <td>int</td>
 * <td>number of characters in the font</td>
 * </tr>
 * <tr valign="top">
 * <td>byte 4-7</td>
 * <td>int</td>
 * <td>
 * value of first character in the font (often 32 for space)
 * </td>
 * </tr>
 * <tr valign="top">
 * <td>byte 8-11</td>
 * <td>int</td>
 * <td>pixel width of each character</td>
 * </tr>
 * <tr valign="top">
 * <td>byte 12-15</td>
 * <td>int</td>
 * <td>pixel height of each character</td>
 * </tr>
 * <tr valign="top">
 * <td>byte 16-</td>
 * <td>char</td>
 * <td>
 * array with character data, one byte per pixel in each
 * character, for a total of (nchars*width*height) bytes.
 * </td>
 * </tr>
 * </table>
 * </p>
 * @return int The font identifier which is always bigger than 5 to avoid conflicts with
 * built-in fonts or false on errors.
 * </p>
 */
function imageloadfont ($file) {}

/**
 * Draws a polygon
 * @link http://php.net/manual/en/function.imagepolygon.php
 * @param image resource 
 * @param points array <p>
 * An array containing the polygon's vertices, i.e. points[0] = x0, 
 * points[1] = y0, points[2] = x1, points[3] = y1, etc.
 * </p>
 * @param num_points int <p>
 * Total number of points (vertices)
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagepolygon ($image, array $points, $num_points, $color) {}

/**
 * Draw a rectangle
 * @link http://php.net/manual/en/function.imagerectangle.php
 * @param image resource 
 * @param x1 int <p>
 * Upper left x coordinate
 * </p>
 * @param y1 int <p>
 * Upper left y coordinate
 * 0, 0 is the top left corner of the image.
 * </p>
 * @param x2 int <p>
 * Bottom right x coordinate
 * </p>
 * @param y2 int <p>
 * Bottom right y coordinate
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagerectangle ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Set a single pixel
 * @link http://php.net/manual/en/function.imagesetpixel.php
 * @param image resource 
 * @param x int <p>
 * x-coordinate
 * </p>
 * @param y int <p>
 * y-coordinate
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagesetpixel ($image, $x, $y, $color) {}

/**
 * Draw a string horizontally
 * @link http://php.net/manual/en/function.imagestring.php
 * @param image resource 
 * @param font int 
 * @param x int <p>
 * x-coordinate of the upper left corner
 * </p>
 * @param y int <p>
 * y-coordinate of the upper left corner
 * </p>
 * @param string string <p>
 * The string to be written
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagestring ($image, $font, $x, $y, $string, $color) {}

/**
 * Draw a string vertically
 * @link http://php.net/manual/en/function.imagestringup.php
 * @param image resource 
 * @param font int 
 * @param x int <p>
 * x-coordinate of the upper left corner
 * </p>
 * @param y int <p>
 * y-coordinate of the upper left corner
 * </p>
 * @param string string <p>
 * The string to be written
 * </p>
 * @param color int <p>
 * A color identifier created with 
 * imagecolorallocate
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagestringup ($image, $font, $x, $y, $string, $color) {}

/**
 * Get image width
 * @link http://php.net/manual/en/function.imagesx.php
 * @param image resource 
 * @return int Return the width of the image or false on 
 * errors.
 * </p>
 */
function imagesx ($image) {}

/**
 * Get image height
 * @link http://php.net/manual/en/function.imagesy.php
 * @param image resource 
 * @return int Return the height of the image or false on 
 * errors.
 * </p>
 */
function imagesy ($image) {}

/**
 * Draw a dashed line
 * @link http://php.net/manual/en/function.imagedashedline.php
 * @param image resource 
 * @param x1 int 
 * @param y1 int 
 * @param x2 int 
 * @param y2 int 
 * @param color int 
 * @return bool 
 */
function imagedashedline ($image, $x1, $y1, $x2, $y2, $color) {}

/**
 * Give the bounding box of a text using TrueType fonts
 * @link http://php.net/manual/en/function.imagettfbbox.php
 * @param size float <p>
 * The font size in pixels
 * </p>
 * @param angle float <p>
 * Angle in degrees in which text will be measured
 * </p>
 * @param fontfile string <p>
 * The name of the TrueType font file (can be a URL). Depending on
 * which version of the GD library that PHP is using, it may attempt to
 * search for files that do not begin with a leading '/' by appending
 * '.ttf' to the filename and searching along a library-defined font path
 * </p>
 * @param text string <p>
 * The string to be measured
 * </p>
 * @return array imagettfbbox returns an array with 8
 * elements representing four points making the bounding box of the
 * text:
 * <tr valign="top">
 * <td>0</td>
 * <td>lower left corner, X position</td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>lower left corner, Y position</td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>lower right corner, X position</td>
 * </tr>
 * <tr valign="top">
 * <td>3</td>
 * <td>lower right corner, Y position</td>
 * </tr>
 * <tr valign="top">
 * <td>4</td>
 * <td>upper right corner, X position</td>
 * </tr>
 * <tr valign="top">
 * <td>5</td>
 * <td>upper right corner, Y position</td>
 * </tr>
 * <tr valign="top">
 * <td>6</td>
 * <td>upper left corner, X position</td>
 * </tr>
 * <tr valign="top">
 * <td>7</td>
 * <td>upper left corner, Y position</td>
 * </tr>
 * </p>
 * <p>
 * The points are relative to the text regardless of the
 * angle, so "upper left" means in the top left-hand 
 * corner seeing the text horizontally.
 * </p>
 */
function imagettfbbox ($size, $angle, $fontfile, $text) {}

/**
 * Write text to the image using TrueType fonts
 * @link http://php.net/manual/en/function.imagettftext.php
 * @param image resource 
 * @param size float <p>
 * The font size. Depending on your version of GD, this should be
 * specified as the pixel size (GD1) or point size (GD2)
 * </p>
 * @param angle float <p>
 * The angle in degrees, with 0 degrees being left-to-right reading text.
 * Higher values represent a counter-clockwise rotation. For example, a 
 * value of 90 would result in bottom-to-top reading text.
 * </p>
 * @param x int <p>
 * The coordinates given by x and
 * y will define the basepoint of the first
 * character (roughly the lower-left corner of the character). This
 * is different from the imagestring, where
 * x and y define the
 * upper-left corner of the first character. For example, "top left"
 * is 0, 0.
 * </p>
 * @param y int <p>
 * The y-ordinate. This sets the position of the fonts baseline, not the
 * very bottom of the character.
 * </p>
 * @param color int <p>
 * The color index. Using the negative of a color index has the effect of
 * turning off antialiasing. See imagecolorallocate
 * </p>
 * @param fontfile string <p>
 * The path to the TrueType font you wish to use.
 * </p>
 * <p>
 * Depending on which version of the GD library PHP is using, when
 * fontfile does not begin with a leading
 * / then .ttf will be appended
 * to the filename and the library will attempt to search for that
 * filename along a library-defined font path.
 * </p>
 * <p>
 * When using versions of the GD library lower than 2.0.18, a space character,
 * rather than a semicolon, was used as the 'path separator' for different font files.
 * Unintentional use of this feature will result in the warning message:
 * Warning: Could not find/open font. For these affected versions, the
 * only solution is moving the font to a path which does not contain spaces.
 * </p>
 * <p>
 * In many cases where a font resides in the same directory as the script using it
 * the following trick will alleviate any include problems.
 * ]]>
 * </p>
 * @param text string <p>
 * The text string in UTF-8 encoding.
 * </p>
 * <p>
 * May include decimal numeric character references (of the form:
 * &amp;#8364;) to access characters in a font beyond position 127.
 * The hexadecimal format (like &amp;#xA9;) is supported as of PHP 5.2.0.
 * Strings in UTF-8 encoding can be passed directly.
 * </p>
 * <p>
 * Named entities, such as &amp;copy;, are not supported. Consider using 
 * html_entity_decode
 * to decode these named entities into UTF-8 strings (html_entity_decode()
 * supports this as of PHP 5.0.0).
 * </p>
 * <p>
 * If a character is used in the string which is not supported by the
 * font, a hollow rectangle will replace the character.
 * </p>
 * @return array an array with 8 elements representing four points making the
 * bounding box of the text. The order of the points is lower left, lower 
 * right, upper right, upper left. The points are relative to the text
 * regardless of the angle, so "upper left" means in the top left-hand 
 * corner when you see the text horizontally.
 * </p>
 */
function imagettftext ($image, $size, $angle, $x, $y, $color, $fontfile, $text) {}

/**
 * Give the bounding box of a text using fonts via freetype2
 * @link http://php.net/manual/en/function.imageftbbox.php
 * @param size float <p>
 * </p>
 * @param angle float <p>
 * </p>
 * @param font_file string <p>
 * </p>
 * @param text string <p>
 * </p>
 * @param extrainfo array[optional] <p>
 * </p>
 * @return array </p>
 */
function imageftbbox ($size, $angle, $font_file, $text, array $extrainfo = null) {}

/**
 * Write text to the image using fonts using FreeType 2
 * @link http://php.net/manual/en/function.imagefttext.php
 * @param image resource 
 * @param size float <p>The font size to use in points
 * </p>
 * @param angle float <p> 
 * The angle in degrees, with 0 degrees being left-to-right reading text.
 * Higher values represent a counter-clockwise rotation. For example, a 
 * value of 90 would result in bottom-to-top reading text.
 * </p>
 * @param x int <p>
 * The coordinates given by x and
 * y will define the basepoint of the first
 * character (roughly the lower-left corner of the character). This
 * is different from the imagestring, where
 * x and y define the
 * upper-left corner of the first character. For example, "top left"
 * is 0, 0.
 * </p>
 * @param y int <p>
 * The y-ordinate. This sets the position of the fonts baseline, not the
 * very bottom of the character.
 * </p>
 * @param color int <p>
 * The index of the desired color for the text, see imagecolorexact
 * </p>
 * @param font_file string <p>
 * The full path to the font being used.
 * </p>
 * @param text string <p>
 * Text to be inserted into image. 
 * </p>
 * @param extrainfo array[optional] <p>
 * </p>
 * @return array This function returns an array defining the four points of the box, starting in the lower left and moving counter-clockwise:
 * <tr valign="top">
 * <td>0</td>
 * <td>lower left x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>lower left y-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>lower right x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>3</td>
 * <td>lower right y-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>4</td>
 * <td>upper right x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>5</td>
 * <td>upper right y-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>6</td>
 * <td>upper left x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>7</td>
 * <td>upper left y-coordinate</td>
 * </tr>
 * </p>
 */
function imagefttext ($image, $size, $angle, $x, $y, $color, $font_file, $text, array $extrainfo = null) {}

/**
 * Load a PostScript Type 1 font from file
 * @link http://php.net/manual/en/function.imagepsloadfont.php
 * @param filename string <p>
 * </p>
 * @return resource In the case everything went right, a valid font index will be returned and
 * can be used for further purposes. Otherwise the function returns false.
 * </p>
 */
function imagepsloadfont ($filename) {}

/**
 * Free memory used by a PostScript Type 1 font
 * @link http://php.net/manual/en/function.imagepsfreefont.php
 * @param fontindex resource <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagepsfreefont ($fontindex) {}

/**
 * Change the character encoding vector of a font
 * @link http://php.net/manual/en/function.imagepsencodefont.php
 * @param font_index resource <p>
 * </p>
 * @param encodingfile string <p>
 * The exact format of this file is described in T1libs documentation. 
 * T1lib comes with two ready-to-use files, 
 * IsoLatin1.enc and 
 * IsoLatin2.enc.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagepsencodefont ($font_index, $encodingfile) {}

/**
 * Extend or condense a font
 * @link http://php.net/manual/en/function.imagepsextendfont.php
 * @param font_index int <p>
 * </p>
 * @param extend float <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagepsextendfont ($font_index, $extend) {}

/**
 * Slant a font
 * @link http://php.net/manual/en/function.imagepsslantfont.php
 * @param font_index resource <p>
 * </p>
 * @param slant float <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function imagepsslantfont ($font_index, $slant) {}

/**
 * Draws a text over an image using PostScript Type1 fonts
 * @link http://php.net/manual/en/function.imagepstext.php
 * @param image resource 
 * @param text string <p>
 * The text to be written.
 * </p>
 * @param font resource 
 * @param size int <p>
 * size is expressed in pixels.
 * </p>
 * @param foreground int <p>
 * The color in which the text will be painted.
 * </p>
 * @param background int <p>
 * The color to which the text will try to fade in with antialiasing.
 * No pixels with the color background are 
 * actually painted, so the background image does not need to be of solid
 * color.
 * </p>
 * @param x int <p>
 * x-coordinate for the lower-left corner of the first character
 * </p>
 * @param y int <p>
 * y-coordinate for the lower-left corner of the first character
 * </p>
 * @param space int[optional] <p>
 * Allows you to change the default value of a space in a font. This
 * amount is added to the normal value and can also be negative.
 * Expressed in character space units, where 1 unit is 1/1000th of an 
 * em-square.
 * </p>
 * @param tightness int[optional] <p>
 * tightness allows you to control the amount
 * of white space between characters. This amount is added to the
 * normal character width and can also be negative.
 * Expressed in character space units, where 1 unit is 1/1000th of an 
 * em-square.
 * </p>
 * @param angle float[optional] <p>
 * angle is in degrees.
 * </p>
 * @param antialias_steps int[optional] <p>
 * Allows you to control the number of colours used for antialiasing 
 * text. Allowed values are 4 and 16. The higher value is recommended
 * for text sizes lower than 20, where the effect in text quality is
 * quite visible. With bigger sizes, use 4. It's less computationally
 * intensive.
 * </p>
 * @return array This function returns an array containing the following elements:
 * <tr valign="top">
 * <td>0</td>
 * <td>lower left x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>lower left y-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>upper right x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>3</td>
 * <td>upper right y-coordinate</td>
 * </tr>
 * </p>
 */
function imagepstext ($image, $text, $font, $size, $foreground, $background, $x, $y, $space = null, $tightness = null, $angle = null, $antialias_steps = null) {}

/**
 * Give the bounding box of a text rectangle using PostScript Type1 fonts
 * @link http://php.net/manual/en/function.imagepsbbox.php
 * @param text string <p>
 * </p>
 * @param font int 
 * @param size int <p>
 * size is expressed in pixels.
 * </p>
 * @return array an array containing the following elements:
 * <tr valign="top">
 * <td>0</td>
 * <td>left x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>1</td>
 * <td>upper y-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>2</td>
 * <td>right x-coordinate</td>
 * </tr>
 * <tr valign="top">
 * <td>3</td>
 * <td>lower y-coordinate</td>
 * </tr>
 * </p>
 */
function imagepsbbox ($text, $font, $size) {}

/**
 * Return the image types supported by this PHP build
 * @link http://php.net/manual/en/function.imagetypes.php
 * @return int a bit-field corresponding to the image formats supported by the
 * version of GD linked into PHP. The following bits are returned, 
 * IMG_GIF | IMG_JPG |
 * IMG_PNG | IMG_WBMP | 
 * IMG_XPM.
 * </p>
 */
function imagetypes () {}

/**
 * Convert JPEG image file to WBMP image file
 * @link http://php.net/manual/en/function.jpeg2wbmp.php
 * @param jpegname string <p>
 * Path to JPEG file
 * </p>
 * @param wbmpname string <p>
 * Path to destination WBMP file
 * </p>
 * @param dest_height int <p>
 * Destination image height
 * </p>
 * @param dest_width int <p>
 * Destination image width
 * </p>
 * @param threshold int <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function jpeg2wbmp ($jpegname, $wbmpname, $dest_height, $dest_width, $threshold) {}

/**
 * Convert PNG image file to WBMP image file
 * @link http://php.net/manual/en/function.png2wbmp.php
 * @param pngname string <p>
 * Path to PNG file
 * </p>
 * @param wbmpname string <p>
 * Path to destination WBMP file
 * </p>
 * @param dest_height int <p>
 * Destination image height
 * </p>
 * @param dest_width int <p>
 * Destination image width
 * </p>
 * @param threshold int <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function png2wbmp ($pngname, $wbmpname, $dest_height, $dest_width, $threshold) {}

/**
 * Output image to browser or file
 * @link http://php.net/manual/en/function.image2wbmp.php
 * @param image resource 
 * @param filename string[optional] <p>
 * Path to the saved file. If not given, the raw image stream will be
 * outputed directly.
 * </p>
 * @param threshold int[optional] <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function image2wbmp ($image, $filename = null, $threshold = null) {}


/**
 * Used as a return value by imagetypes
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_GIF', 1);

/**
 * Used as a return value by imagetypes
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_JPG', 2);

/**
 * Used as a return value by imagetypes
 * <p>
 * This constant has the same value as IMG_JPG
 * </p>
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_JPEG', 2);

/**
 * Used as a return value by imagetypes
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_PNG', 4);

/**
 * Used as a return value by imagetypes
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_WBMP', 8);

/**
 * Used as a return value by imagetypes
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_XPM', 16);

/**
 * Special color option which can be used in stead of color allocated with
 * imagecolorallocate or
 * imagecolorallocatealpha
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_COLOR_TILED', -5);

/**
 * Special color option which can be used in stead of color allocated with
 * imagecolorallocate or
 * imagecolorallocatealpha
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_COLOR_STYLED', -2);

/**
 * Special color option which can be used in stead of color allocated with
 * imagecolorallocate or
 * imagecolorallocatealpha
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_COLOR_BRUSHED', -3);

/**
 * Special color option which can be used in stead of color allocated with
 * imagecolorallocate or
 * imagecolorallocatealpha
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_COLOR_STYLEDBRUSHED', -4);

/**
 * Special color option which can be used in stead of color allocated with
 * imagecolorallocate or
 * imagecolorallocatealpha
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_COLOR_TRANSPARENT', -6);

/**
 * A style constant used by the imagefilledarc function.
 * <p>
 * This constant has the same value as IMG_ARC_PIE
 * </p>
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_ARC_ROUNDED', 0);

/**
 * A style constant used by the imagefilledarc function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_ARC_PIE', 0);

/**
 * A style constant used by the imagefilledarc function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_ARC_CHORD', 1);

/**
 * A style constant used by the imagefilledarc function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_ARC_NOFILL', 2);

/**
 * A style constant used by the imagefilledarc function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_ARC_EDGED', 4);

/**
 * A type constant used by the imagegd2 function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_GD2_RAW', 1);

/**
 * A type constant used by the imagegd2 function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('IMG_GD2_COMPRESSED', 2);
define ('GD_BUNDLED', 0);

/**
 * The GD version PHP was compiled against.
 * (Available as of PHP 5.2.4)
 * @link http://php.net/manual/en/image.constants.php
 */
define ('GD_VERSION', "2.0.35");

/**
 * The GD major version PHP was compiled against.
 * (Available as of PHP 5.2.4)
 * @link http://php.net/manual/en/image.constants.php
 */
define ('GD_MAJOR_VERSION', 2);

/**
 * The GD minor version PHP was compiled against.
 * (Available as of PHP 5.2.4)
 * @link http://php.net/manual/en/image.constants.php
 */
define ('GD_MINOR_VERSION', 0);

/**
 * The GD release version PHP was compiled against.
 * (Available as of PHP 5.2.4)
 * @link http://php.net/manual/en/image.constants.php
 */
define ('GD_RELEASE_VERSION', 35);

/**
 * The GD "extra" version (beta/rc..) PHP was compiled against.
 * (Available as of PHP 5.2.4)
 * @link http://php.net/manual/en/image.constants.php
 */
define ('GD_EXTRA_VERSION', "");

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_NO_FILTER', 0);

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_FILTER_NONE', 8);

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_FILTER_SUB', 16);

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_FILTER_UP', 32);

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_FILTER_AVG', 64);

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_FILTER_PAETH', 128);

/**
 * A special PNG filter, used by the imagepng function.
 * @link http://php.net/manual/en/image.constants.php
 */
define ('PNG_ALL_FILTERS', 248);

// End of gd v.
?>
