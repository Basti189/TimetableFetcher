package app.wolfware.timetable.fetcher;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NodeHelper {

    public static String getTextContent(NamedNodeMap nnm, String name) {
        Node n = nnm.getNamedItem(name);
        if (n != null) {
            return n.getTextContent();
        }
        return null;
    }
}
