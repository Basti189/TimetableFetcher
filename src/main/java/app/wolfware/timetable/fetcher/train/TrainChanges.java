package app.wolfware.timetable.fetcher.train;

import app.wolfware.timetable.fetcher.NodeHelper;
import app.wolfware.timetable.fetcher.TimetableFetcher;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;

public class TrainChanges {

    private final String id;
    private String category;
    private String number;
    private String owner;
    private final LocalDateTime timestamp;
    private JourneyChangesInfo arrival;
    private JourneyChangesInfo departure;
    private String origin;
    private String destination;



    public TrainChanges(Node parent, String actualStationName) {
        id = parent.getAttributes().getNamedItem("id").getTextContent();
        String idWithoutPosition = id.substring(0, id.lastIndexOf("-"));
        timestamp = LocalDateTime.parse(idWithoutPosition.substring(idWithoutPosition.lastIndexOf("-") + 1), TimetableFetcher.formatter);
        NodeList children = parent.getChildNodes();
        for (int i = 0 ; i < children.getLength() ; i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("tl")) {
                NamedNodeMap attributes = child.getAttributes();
                category = NodeHelper.getTextContent(attributes, "c");
                number = NodeHelper.getTextContent(attributes, "n");
                owner = NodeHelper.getTextContent(attributes, "o");
            } else if (child.getNodeName().equals("ar")) {
                arrival = new JourneyChangesInfo(JourneyChangesInfo.Type.ARRIVAL, child, actualStationName);
            } else if (child.getNodeName().equals("dp")) {
                departure = new JourneyChangesInfo(JourneyChangesInfo.Type.DEPARTURE, child, actualStationName);
            }
        }
        if (arrival != null) {
            origin = arrival.getPlannedJourneyPoint();
        } else {
            origin = actualStationName;
        }
        if (departure != null) {
            destination = departure.getPlannedJourneyPoint();
        } else {
            destination = actualStationName;
        }
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getNumber() {
        return number;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public JourneyChangesInfo getArrival() {
        return arrival;
    }

    public JourneyChangesInfo getDeparture() {
        return departure;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "TrainChanges{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", number='" + number + '\'' +
                ", owner='" + owner + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", origin=" + origin + '\'' +
                ", destination=" + destination + '\'' +
                ", arrival=" + arrival + '\'' +
                ", departure=" + departure +
                '}';
    }
}
