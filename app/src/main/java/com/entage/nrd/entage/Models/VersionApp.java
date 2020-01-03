package com.entage.nrd.entage.Models;

public class VersionApp {

   private String version_name;
   private boolean force_update;
   private int version_code;


    public VersionApp() {
    }

    public VersionApp(String version_name, boolean force_update, int version_code) {
        this.version_name = version_name;
        this.force_update = force_update;
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public boolean isForce_update() {
        return force_update;
    }

    public void setForce_update(boolean force_update) {
        this.force_update = force_update;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    @Override
    public String toString() {
        return "VersionApp{" +
                "version_name='" + version_name + '\'' +
                ", force_update=" + force_update +
                ", version_code=" + version_code +
                '}';
    }
}
