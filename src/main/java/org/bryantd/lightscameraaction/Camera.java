/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bryantd.lightscameraaction;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import mmcorej.CMMCore;

/**
 *
 * @author bryantd
 */
public class Camera {
    private final CMMCore cmmcore_;
    private final String imageSaveLocation_;
    
    public Camera(CMMCore cmmcore, String imageSaveLocation) {
        this.cmmcore_ = cmmcore;
        this.imageSaveLocation_ = imageSaveLocation;
    }
    
    public boolean snapImage(String filename) throws Exception {
        Boolean success = true;

        try {
            success = saveImageFile(filename, getImage());
        } catch (Exception e) {
            throw(e);
        }        
        
        return success;
    }

    private Object getImage() throws Exception {
        Object image = null;
        try {
            cmmcore_.snapImage();
            image = cmmcore_.getImage();
        } catch(NullPointerException e) {
            throw new NullPointerException("Error: No live CMMCore!");
        }
        return image;
    }

    private boolean saveImageFile(String fname, Object img) {
        int width = (int)cmmcore_.getImageWidth();
        int height = (int)cmmcore_.getImageHeight();
        ImageProcessor ip;
        if (img instanceof byte[]) {
            ip = new ByteProcessor(width, height);
            ip.setPixels((byte[])img);
        }
        else if (img instanceof short[]) {
           ip = new ShortProcessor(width, height);
           ip.setPixels((short[])img);
        }
        else
           return false;

        fname = imageSaveLocation_ + fname + ".tif";
        ImagePlus imp = new ImagePlus(fname, ip);
        FileSaver fs = new FileSaver(imp);
        return fs.saveAsTiff(fname);
   }
}
