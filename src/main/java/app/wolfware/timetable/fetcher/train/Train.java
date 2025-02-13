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



    public Train(Node parent) {
        id = parent.getAttributes().getNamedItem("id").getTextContent();
        timestamp = LocalDateTime.parse(id.substring(id.lastIndexOf("-") + 1), TimetableFetcher.formatter);
        NodeList children = parent.getChildNodes();
        for (int i = 0 ; i < children.getLength() ; i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("tl")) {
                NamedNodeMap attributes = child.getAttributes();
                category = attributes.getNamedItem("c").getTextContent();
                number = attributes.getNamedItem("n").getTextContent();
                owner = attributes.getNamedItem("o").getTextContent();
            } else if (child.getNodeName().equals("ar")) {
                arrival = new JourneyInfo(JourneyInfo.Type.ARRIVAL, child);
            } else if (child.getNodeName().equals("dp")) {
                departure = new JourneyInfo(JourneyInfo.Type.DEPARTURE, child);
            }
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

    public JourneyInfo getDepature() {
        return departure;
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