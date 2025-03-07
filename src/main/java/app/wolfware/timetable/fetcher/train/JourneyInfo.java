package app.wolfware.timetable.fetcher.train;

import app.wolfware.timetable.fetcher.NodeHelper;
import app.wolfware.timetable.fetcher.TimetableFetcher;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class JourneyInfo {

    public enum Type {
        ARRIVAL, DEPARTURE
    }

    private Type type;
    private LocalDateTime plannedTime;
    private String plannedPlattform;
    private String plannedPath;
    private String plannedJourneyPoint;
    private String line;
    private String wings;
    private List<Train> trainWings;
    private String plannedDestination;
    private String transition;

    public JourneyInfo(Type type, Node parent, String actualStationName) {
        this.type = type;

        NamedNodeMap attributes = parent.getAttributes();
        plannedTime = LocalDateTime.parse(attributes.getNamedItem("pt").getTextContent(), TimetableFetcher.formatter);
        plannedPlattform = attributes.getNamedItem("pp").getTextContent();
        plannedDestination = NodeHelper.getTextContent(attributes, "pde");
        transition = NodeHelper.getTextContent(attributes, "tra");
        if (transition != null && !transition.isEmpty()) {
            transition = transition.substring(0, transition.lastIndexOf("-"));
        }

        String ppth = attributes.getNamedItem("ppth").getTextContent();
        if (type == Type.ARRIVAL) {
            plannedJourneyPoint = ppth.split("\\|")[0];
            plannedPath = ppth;
        } else if (type == Type.DEPARTURE) {
            String[] splitted_ppth = ppth.split("\\|");
            plannedJourneyPoint = splitted_ppth[splitted_ppth.length - 1];
            plannedPath = ppth;
        }
        line = NodeHelper.getTextContent(attributes, "l");
        wings = NodeHelper.getTextContent(attributes, "wings");
    }

    public Type getType() {
        return type;
    }

    public LocalDateTime getPlannedTime() {
        return plannedTime;
    }

    public String getPlannedPlattform() {
        return plannedPlattform;
    }

    public String getPlannedPath() {
        return plannedPath;
    }

    public String getPlannedJourneyPoint() {
        return plannedJourneyPoint;
    }

    public String getLine() {
        return line;
    }

    public String getWings() {
        return wings;
    }

    public List<Train> getTrainWings() {
        return trainWings;
    }

    public void setTrainWings(List<Train> trainWings) {
        this.trainWings = trainWings;
    }

    public String getPlannedDestination() {
        return plannedDestination;
    }

    public String getTransition() {
        return transition;
    }

    @Override
    public String toString() {
        return "JourneyInfo{" +
                "type=" + type +
                ", plannedTime=" + plannedTime +
                ", plannedPlattform='" + plannedPlattform + '\'' +
                ", plannedPath='" + plannedPath + '\'' +
                ", plannedJourneyPoint='" + plannedJourneyPoint + '\'' +
                ", line='" + line + '\'' +
                ", wings='" + wings + '\'' +
                ", trainWings=" + trainWings +
                ", plannedDestination=" + plannedDestination +
                ", transition=" + transition +
                '}';
    }
}
