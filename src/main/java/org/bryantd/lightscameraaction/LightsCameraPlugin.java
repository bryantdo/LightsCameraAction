package org.bryantd.lightscameraaction;

import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.AcquisitionEngine;
import org.micromanager.api.ScriptInterface;

public class LightsCameraPlugin implements org.micromanager.api.MMPlugin {
    public static String menuName = "LightsCameraAction";
    public static String tooltipDescription = "Tightly integrating imaging platforms with Heliospectra lights.";
    private CMMCore core_;
    private MMStudio gui_;
    private AcquisitionEngine acq_;
    private static PluginInterface dialog_;

    @Override
    public void dispose() {
        if(dialog_ != null) {
            dialog_.setVisible(false);
            dialog_.dispose();
            dialog_ = null;
        }
    }

    @Override
    public void setApp(ScriptInterface app) {
        gui_ = (MMStudio) app;
        core_ = app.getMMCore();
        acq_ = getGui_().getAcquisitionEngine();
    }

    @Override
    public void show() {
        if (dialog_ == null) {
            dialog_ = new PluginInterface(this);
            dialog_.setVisible(true);
        } else {
            dialog_.toFront();
        }
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getInfo() {
        return "Info: LightsCameraAction: Tightly integrating imaging platforms with Heliospectra lights.";
    }

    @Override
    public String getVersion() { return "1.0"; }

    @Override
    public String getCopyright() {
        return "(C) 2014 Doug Bryant, DDPSC, USA. This software is released under the BSD license";
    }

    public CMMCore getCore_() {
        return core_;
    }

    public MMStudio getGui_() {
        return gui_;
    }
}
