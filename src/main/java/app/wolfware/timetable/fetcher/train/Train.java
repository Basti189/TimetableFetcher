package app.wolfware.timetable.fetcher.train;

import app.wolfware.timetable.fetcher.TimetableFetcher;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.Time;
import java.time.LocalDateTime;

public class Train {

    private final String id;
    private String category;
    private String number;
    private String owner;
    private final LocalDateTime timestamp;
    private JourneyInfo arrival;
    private JourneyInfo departure;
    private String origin;
    private String destination;



    public Train(Node parent, String actualStationName) {
        id = parent.getAttributes().getNamedItem("id").getTextContent();
        String idWithoutPosition = id.substring(0, id.lastIndexOf("-"));
        timestamp = LocalDateTime.parse(idWithoutPosition.substring(idWithoutPosition.lastIndexOf("-") + 1), TimetableFetcher.formatter);
        NodeList children = parent.getChildNodes();
        for (int i = 0 ; i < children.getLength() ; i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("tl")) {
                NamedNodeMap attributes = child.getAttributes();
                category = attributes.getNamedItem("c").getTextContent();
                number = attributes.getNamedItem("n").getTextContent();
                owner = attributes.getNamedItem("o").getTextContent();
            } else if (child.getNodeName().equals("ar")) {
                arrival = new JourneyInfo(JourneyInfo.Type.ARRIVAL, child, actualStationName);
            } else if (child.getNodeName().equals("dp")) {
                departure = new JourneyInfo(JourneyInfo.Type.DEPARTURE, child, actualStationName);
            }
        }
        if (arrival != null) {
            destination = arrival.getPlannedJourneyPoint();
        } else {
            destination = actualStationName;
        }
        if (departure != null) {
            origin = departure.getPlannedJourneyPoint();
        } else {
            origin = actualStationName;
        }
    }

    public void setLiveData(Node parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0 ; i < children.getLength() ; i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("ar")) {
                if (arrival != null) {
                    arrival.setLiveData(child);
                }
            } else if (child.getNodeName().equals("dp")) {
                if (departure != null) {
                    departure.setLiveData(child);
                }
            }
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

    public JourneyInfo getArrival() {
        return arrival;
    }

    public JourneyInfo getDeparture() {
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
        return "Train{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", number='" + number + '\'' +
                ", owner='" + owner + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", arrival=" + arrival +
                ", departure=" + departure +
                '}';
    }
}