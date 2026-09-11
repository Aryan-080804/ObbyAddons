package me.obbyaddons.feature;

public abstract class Feature {

    private final String name;
    private boolean enabled;

    protected Feature(String name, boolean enabledByDefault) {
        this.name = name;
        this.enabled = enabledByDefault;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void initialize() {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}