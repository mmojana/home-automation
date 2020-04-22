package ch.mojanam.shadowtoinflux.entities;

import java.util.Map;

public class ShadowReportedStateEvent {
    private String thingName;
    private Map<String, Object> reportedState;

    public String getThingName() {
        return thingName;
    }

    public void setThingName(String thingName) {
        this.thingName = thingName;
    }

    public Map<String, Object> getReportedState() {
        return reportedState;
    }

    public void setReportedState(Map<String, Object> reportedState) {
        this.reportedState = reportedState;
    }

    @Override
    public String toString() {
        return "ShadowReportedStateEvent{" +
                "thingName='" + thingName + '\'' +
                ", reportedState=" + reportedState +
                '}';
    }
}
