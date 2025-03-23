package app.wolfware.timetable.fetcher.train;


import app.wolfware.timetable.fetcher.NodeHelper;
import app.wolfware.timetable.fetcher.TimetableFetcher;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class JourneyChangesInfo {

    public enum Type {
        ARRIVAL, DEPARTURE
    }

    private Type type;
    private LocalDateTime plannedTime, changedTime;
    private String plannedPlattform, changedPlattform;
    private String plannedPath, changedPath;
    private String plannedJourneyPoint, changedJourneyPoint;
    private String line;
    private String wings, changedWings;
    private List<Train> trainWings;
    private String plannedDestination, changedDestination;
    private String transition;
    private boolean canceled = false;
    private LocalDateTime canceledTime;
    private String eventStatus;
    private boolean hidden;

    public JourneyChangesInfo(Type type, Node parent, String actualStationName) {
        this.type = type;

        NamedNodeMap attributes = parent.getAttributes();
        if (attributes.getNamedItem("pt") != null) {
            plannedTime = LocalDateTime.parse(attributes.getNamedItem("pt").getTextContent(), TimetableFetcher.formatter);
        }

        plannedPlattform = NodeHelper.getTextContent(attributes, "pp");
        plannedDestination = NodeHelper.getTextContent(attributes, "pde");
        transition = NodeHelper.getTextContent(attributes, "tra");
        if (transition != null && !transition.isEmpty()) {
            transition = transition.substring(0, transition.lastIndexOf("-"));
        }

        plannedPath = NodeHelper.getTextContent(attributes, "ppth");
        if (plannedPath != null) {
            String ppth = attributes.getNamedItem("ppth").getTextContent();
            if (type == Type.ARRIVAL) {
                plannedJourneyPoint = ppth.split("\\|")[0];
                plannedPath = ppth;
            } else if (type == Type.DEPARTURE) {
                String[] splitted_ppth = ppth.split("\\|");
                plannedJourneyPoint = splitted_ppth[splitted_ppth.length - 1];
                plannedPath = ppth;
            }
        }

        line = NodeHelper.getTextContent(attributes, "l");
        wings = NodeHelper.getTextContent(attributes, "wings");

        String ct = NodeHelper.getTextContent(attributes, "ct");
        changedPlattform = NodeHelper.getTextContent(attributes, "cp");
        changedDestination = NodeHelper.getTextContent(attributes, "cde");
        changedPath = NodeHelper.getTextContent(attributes, "cpth");
        changedWings = NodeHelper.getTextContent(attributes, "wings");
        String cs = NodeHelper.getTextContent(attributes, "cs");
        String clt = NodeHelper.getTextContent(attributes, "clt");

        if (ct != null) {
            changedTime = LocalDateTime.parse(ct, TimetableFetcher.formatter);
        }

        if (changedPath != null) {
            String cpth = changedPath;
            if (type == Type.ARRIVAL) {
                changedJourneyPoint = cpth.split("\\|")[0];
                changedPath = cpth;
            } else if (type == Type.DEPARTURE) {
                String[] splitted_cpth = cpth.split("\\|");
                changedJourneyPoint = splitted_cpth[splitted_cpth.length - 1];
                changedPath = cpth;
            }
        }

        if (cs != null) {
            eventStatus = cs;
            if (cs.equals("c")) {
                canceled = true;
            } else if (cs.equals("p")) {
                canceled = false;
                canceledTime = null;
            }
        }
        if (clt != null) {
            canceledTime = LocalDateTime.parse(clt, TimetableFetcher.formatter);
        }
    }

    public Type getType() {
        return type;
    }

    public LocalDateTime getPlannedTime() {
        return plannedTime;
    }

    public LocalDateTime getChangedTime() {
        return changedTime;
    }

    public String getPlannedPlattform() {
        return plannedPlattform;
    }

    public String getChangedPlattform() {
        return changedPlattform;
    }

    public String getPlannedPath() {
        return plannedPath;
    }

    public String getChangedPath() {
        return changedPath;
    }

    public String getPlannedJourneyPoint() {
        return plannedJourneyPoint;
    }

    public String getChangedJourneyPoint() {
        return changedJourneyPoint;
    }

    public String getLine() {
        return line;
    }

    public String getWings() {
        return wings;
    }

    public String getChangedWings() {
        return changedWings;
    }

    public List<Train> getTrainWings() {
        return trainWings;
    }

    public void setTrainWings(List<Train> trainWings) {
        this.trainWings = trainWings;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public LocalDateTime getCanceledTime() {
        return canceledTime;
    }

    public long getDelayTime() {
        return Duration.between(plannedTime, changedTime).toMinutes();
    }

    public String getPlannedDestination() {
        return plannedDestination;
    }

    public String getTransition() {
        return transition;
    }

    public String getChangedDestination() {
        return changedDestination;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public boolean isHidden() {
        return hidden;
    }
    @Override
    public String toString() {
        return "JourneyChangesInfo{" +
                "type=" + type +
                ", plannedTime=" + plannedTime +
                ", changedTime=" + changedTime +
                ", plannedPlattform='" + plannedPlattform + '\'' +
                ", changedPlattform='" + changedPlattform + '\'' +
                ", plannedPath='" + plannedPath + '\'' +
                ", changedPath='" + changedPath + '\'' +
                ", plannedJourneyPoint='" + plannedJourneyPoint + '\'' +
                ", changedJourneyPoint='" + changedJourneyPoint + '\'' +
                ", line='" + line + '\'' +
                ", wings='" + wings + '\'' +
                ", changedWings='" + changedWings + '\'' +
                ", trainWings=" + trainWings +
                ", plannedDestination=" + plannedDestination +
                ", transition=" + transition +
                ", canceled=" + canceled +
                ", canceledTime=" + canceledTime +
                '}';
    }
}
