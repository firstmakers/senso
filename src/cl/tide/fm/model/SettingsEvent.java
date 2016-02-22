/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.model;

/**
 *
 * @author edisondelgado
 */
public class SettingsEvent {
    
    public Boolean isAnimationAvailable;
    public Boolean isSaveAllAvailable;
    public String currentWorkspace;
    public long interval;
    public int samples;
    public Boolean isAvailableFutureSampling;
    public long futureSamplingMs;
    public Boolean isAllowUbidots;
    public String apikeyUbidots;

    public SettingsEvent(Boolean allowUbidots,String apikey,Boolean isAnimationAvailable, Boolean isSaveAllAvailable, String currentWorkspace, long interval, int samples, Boolean isAvailableFutureSampling, long futureSampling) {
        this.isAnimationAvailable = isAnimationAvailable;
        this.isSaveAllAvailable = isSaveAllAvailable;
        this.currentWorkspace = currentWorkspace;
        this.interval = interval;
        this.samples = samples;
        this.isAvailableFutureSampling = isAvailableFutureSampling;
        this.futureSamplingMs = futureSampling;
        this.isAllowUbidots = allowUbidots;
        this.apikeyUbidots = apikey;
    }
    
    
}
