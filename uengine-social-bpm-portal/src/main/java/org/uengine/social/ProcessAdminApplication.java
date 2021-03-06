package org.uengine.social;

import org.metaworks.annotation.Face;
import org.metaworks.annotation.Hidden;
import org.uengine.codi.mw3.model.Application;
import org.uengine.processadmin.ProcessAdminWorkbench;

@Face(ejsPath="dwr/metaworks/genericfaces/CleanObjectFace.ejs")
public class ProcessAdminApplication extends Application{

    // hidden tray
    @Override
    @Hidden
    public String getTopCenterPanelType() {
        return super.getTopCenterPanelType();
    }

    public ProcessAdminApplication() throws Exception {
        this.processAdminWorkbench = new ProcessAdminWorkbench();
    }

//    StandaloneProcessModeler standaloneProcessModeler;
//        public StandaloneProcessModeler getStandaloneProcessModeler() {
//            return standaloneProcessModeler;
//        }
//        public void setStandaloneProcessModeler(StandaloneProcessModeler standaloneProcessModeler) {
//            this.standaloneProcessModeler = standaloneProcessModeler;
//        }


    ProcessAdminWorkbench processAdminWorkbench;
        public ProcessAdminWorkbench getProcessAdminWorkbench() {
            return processAdminWorkbench;
        }
        public void setProcessAdminWorkbench(ProcessAdminWorkbench processAdminWorkbench) {
            this.processAdminWorkbench = processAdminWorkbench;
        }


}
