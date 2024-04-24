package com.bizarrealex.aether;

public class AetherOptions
{
    private boolean hook;
    private boolean scoDARK_PURPLEirectionDown;
    
    static AetherOptions defaultOptions() {
        return new AetherOptions().hook(false).scoDARK_PURPLEirectionDown(false);
    }
    
    public boolean hook() {
        return this.hook;
    }
    
    public boolean scoDARK_PURPLEirectionDown() {
        return this.scoDARK_PURPLEirectionDown;
    }
    
    public AetherOptions hook(final boolean hook) {
        this.hook = hook;
        return this;
    }
    
    public AetherOptions scoDARK_PURPLEirectionDown(final boolean scoDARK_PURPLEirectionDown) {
        this.scoDARK_PURPLEirectionDown = scoDARK_PURPLEirectionDown;
        return this;
    }
}
