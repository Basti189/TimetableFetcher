package app.wolfware.timetable.fetcher.sql;

import app.wolfware.timetable.fetcher.Station;
import app.wolfware.timetable.fetcher.security.Credentials;
import app.wolfware.timetable.fetcher.train.JourneyInfo;
import app.wolfware.timetable.fetcher.train.Train;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtils {

    private final static String insert_Train = "INSERT IGNORE INTO train (id, category, number, owner, timestamp) VALUES (?, ?, ?, ?, ?)";
    private final static String insert_Station = "INSERT IGNORE INTO station (id, name, alias) VALUES (?, ?, ?)";
    private final static String insert_Journey = "INSERT IGNORE INTO journey (id, position, station, " +
            "arrival_line, arrival_pt, arrival_pp, arrival_ppth, arrival_wings, " +
            "departure_line, departure_pt, departure_pp, departure_ppth, departure_wings) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static boolean createDatabase() {
        String createTable_Train = "CREATE TABLE IF NOT EXISTS train (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "category VARCHAR(10) NOT NULL, " +
                "number INT UNSIGNED NOT NULL, " +
                "owner VARCHAR(10) NOT NULL, " +
                "timestamp DATETIME NOT NULL);";

        String createTable_Station = "CREATE TABLE IF NOT EXISTS station (" +
                "id INT UNSIGNED PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "alias VARCHAR(100) DEFAULT NULL);";

        String createTable_Journey = "CREATE TABLE IF NOT EXISTS journey (" +
                "id VARCHAR(50) NOT NULL, " +
                "position TINYINT UNSIGNED NOT NULL, " +
                "station INT UNSIGNED NOT NULL, " +
                "arrival_line VARCHAR(3) DEFAULT NULL, " +
                "arrival_pt DATETIME DEFAULT NULL, " +
                "arrival_pp VARCHAR(3) DEFAULT NULL, " +
                "arrival_ppth TEXT DEFAULT NULL, " +
                "arrival_wings TEXT DEFAULT NULL, " +
                "departure_line VARCHAR(3) DEFAULT NULL, " +
                "departure_pt DATETIME DEFAULT NULL, " +
                "departure_pp VARCHAR(3) DEFAULT NULL, " +
                "departure_ppth TEXT DEFAULT NULL, " +
                "departure_wings TEXT DEFAULT NULL, " +
                "PRIMARY KEY (id, position), " +
                "FOREIGN KEY (id) REFERENCES train(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (station) REFERENCES station(id) ON DELETE CASCADE);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(insert_Station)) {

            stmt.executeUpdate(createTable_Train);
            System.out.println("Tabelle 'train' erstellt oder existiert bereits.");
            stmt.executeUpdate(createTable_Station);
            System.out.println("Tabelle 'station' erstellt oder existiert bereits.");
            stmt.executeUpdate(createTable_Journey);
            System.out.println("Tabelle 'journey' erstellt oder existiert bereits.");

            conn.setAutoCommit(false);
            s(pstmt, 8000193, "Kassel Hbf");
            // RB 83
            s(pstmt, 8004415, "Vellmar-Niedervellmar", "Abzw Vellmar-=Niedervellmar Hp");
            s(pstmt, 8003053, "Fuldatal-Ihringshausen", "F-Ihringshausen");
            s(pstmt, 8005623, "Speele", "Staufenberg-Speele");
            s(pstmt, 8006707, "Hann Münden");
            s(pstmt, 8002677, "Hedemünden");
            s(pstmt, 8002259, "Gertenbach", "Gertenbach Hp");
            s(pstmt, 8006524, "Witzenhausen Nord", "Witzenhausen N");
            // RB 83, RB 87
            s(pstmt, 8000090, "Eichenberg");
            s(pstmt, 8002102, "Friedland(Han)", "Friedland");
            s(pstmt, 8000128, "Göttingen", "Göttingen Pbf");
             // RB 87
            s(pstmt, 8000753, "Bad Sooden-Allendorf", "Bad Sooden-Alldf");
            s(pstmt, 8001895, "Eschwege-Niederhone", "Eschwege-Niederhone Hp");
            s(pstmt, 8001884, "Eschwege");
            s(pstmt, 8005003, "Wehretal-Reichensachsen", "Wehretal-=Reichensachsen");
            s(pstmt, 8005610, "Sontra");
            // RB 5
            s(pstmt, 8003200, "Kassel-Wilhelmshöhe", "Ksl-Wilhelmshöhe");
            s(pstmt, 8003199, "Kassel-Oberzwehren", "Ksl-Oberzw Hp");
            s(pstmt, 8005039, "Baunatal-Rengershausen", "B-Rengersh Hp");
            s(pstmt, 8000140, "Baunatal-Guntershausen", "B-Guntershausen");
            s(pstmt, 8002484, "Guxhagen", "Guxhagen Hp");
            s(pstmt, 8003388, "Körle");
            s(pstmt, 8005125, "Melsungen-Röhrenfurth", "Mels-Röhrenfurth");
            s(pstmt, 8003962, "Melsungen-Schwarzenberg", "Melsungen-Schwarzenberg Hp"); // Schwarzenberg
            s(pstmt, 8003971, "Melsungen Bartenwetzerbrücke", "Melsungen-Bartenwbrücke Hp");
            s(pstmt, 8003961, "Melsungen");
            s(pstmt, 8000243, "Malsfeld", "Malsfeld Hp");
            s(pstmt, 8000859, "Malsfeld-Beiseförth", "Malsf-Beiseförth");
            s(pstmt, 8000553, "Altmorschen", "Morschen-Altmorschen Hp");
            s(pstmt, 8002716, "Heinebach");
            s(pstmt, 8005182, "Rotenburg a.d. Fulda", "Rotenburg Hp");
            s(pstmt, 8003711, "Lispenhausen", "Lispenhausen Hp");
            // RB 5, RB 87, RB 6
            s(pstmt, 8000029	, "Bebra", "Bft Bebra Pbf");
            // RB 5
            s(pstmt, 8002103, "Ludwigsau-Friedlos", "Ludw-Friedlos Hp");
            s(pstmt, 8000020, "Bad Hersfeld");
            s(pstmt, 8004297, "Haunetal-Neukirchen", "Haunetal-Neukch");
            s(pstmt, 8001283, "Burghaun(Hünfeld)", "Burghaun");
            s(pstmt, 8003016, "Hünfeld");
            s(pstmt, 8000115, "Fulda", "Bft Fulda Pbf");
            // RB 6
            s(pstmt, 8005161, "Ronshausen", "Ronshausen Hp");
            s(pstmt, 8002903, "Wildeck-Hönebach");
            s(pstmt, 8001106, "Wildeck-Bosserode", "Wildeck-BosserodeHp");
            s(pstmt, 8004589, "Wildeck-Obersuhl", "Wild-Obersuhl Hp");
            s(pstmt, 8011629, "Gerstungen");
            s(pstmt, 8013548, "Herleshausen", "Herleshausen Hp");
            s(pstmt, 8013547, "Hörschel", "Hörschel Hp");
            s(pstmt, 8010105, "Eisenach Opelwerke", "Eisenach Opel Hp");
            s(pstmt, 8011468, "Eisenach West", "Eisenach West Hp");
            s(pstmt, 8010097, "Eisenach Hauptbahnhof", "Bft Eisenach Hbf");

            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void s(PreparedStatement pstmt, int evaNo, String name) throws SQLException {
        s(pstmt, evaNo, name, null);
    }

    private static void s(PreparedStatement pstmt, int evaNo, String name, String alias) throws SQLException {
        pstmt.setInt(1, evaNo);
        pstmt.setString(2, name);
        pstmt.setString(3, alias);
        pstmt.addBatch();
    }

    public static boolean insertTrains(int evaNo, List<Train> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insert_Train)) {
            conn.setAutoCommit(false);
            for (Train train : trains) {
                if (!train.getOwner().startsWith("N4")) {
                    continue;
                }
                pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                pstmt.setString(2, train.getCategory());
                pstmt.setInt(3, Integer.parseInt(train.getNumber()));
                pstmt.setString(4, train.getOwner());
                pstmt.setString(5, train.getTimestamp().toString());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean insertJourneys(int evaNo, List<Train> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insert_Journey)) {
            conn.setAutoCommit(false);
            for (Train train : trains) {
                if (!train.getOwner().startsWith("N4")) {
                    continue;
                }
                pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                pstmt.setInt(2, Integer.parseInt(train.getId().substring(train.getId().lastIndexOf("-")+1)));
                pstmt.setInt(3, evaNo);
                if (train.getArrival() != null) {
                    JourneyInfo journey = train.getArrival();
                    pstmt.setString(4, journey.getLine());
                    pstmt.setString(5, journey.getPlannedTime().toString());
                    pstmt.setString(6, journey.getPlannedPlattform());
                    pstmt.setString(7, journey.getPlannedPath());
                    pstmt.setString(8, journey.getWings());

                } else {
                    pstmt.setString(4, null);
                    pstmt.setString(5, null);
                    pstmt.setString(6, null);
                    pstmt.setString(7, null);
                    pstmt.setString(8, null);
                }
                if (train.getDepature() != null) {
                    JourneyInfo journey = train.getDepature();
                    pstmt.setString(9, journey.getLine());
                    pstmt.setString(10, journey.getPlannedTime().toString());
                    pstmt.setString(11, journey.getPlannedPlattform());
                    pstmt.setString(12, journey.getPlannedPath());
                    pstmt.setString(13, journey.getWings());
                } else {
                    pstmt.setString(9, null);
                    pstmt.setString(10, null);
                    pstmt.setString(11, null);
                    pstmt.setString(12, null);
                    pstmt.setString(13, null);
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();
        String select_station = "SELECT * FROM station";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select_station)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String alias = rs.getString("alias");
                stations.add(new Station(id, name, alias));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stations;
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Credentials.URL, Credentials.USER, Credentials.PASSWORD);
    }
}
