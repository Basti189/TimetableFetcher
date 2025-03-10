package app.wolfware.timetable.fetcher.sql;

import app.wolfware.timetable.fetcher.Station;
import app.wolfware.timetable.fetcher.security.Credentials;
import app.wolfware.timetable.fetcher.train.JourneyChangesInfo;
import app.wolfware.timetable.fetcher.train.JourneyInfo;
import app.wolfware.timetable.fetcher.train.Train;
import app.wolfware.timetable.fetcher.train.TrainChanges;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DBUtils {

    private final static String insert_Train = "INSERT IGNORE INTO train (id, category, number, owner, timestamp, origin, destination) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final static String insert_Station = "INSERT IGNORE INTO station (id, name, alias) VALUES (?, ?, ?)";
    private final static String insert_Journey = "INSERT IGNORE INTO journey (id, position, station, " +
            "arrival_line, arrival_pt, arrival_pp, arrival_ppth, arrival_wings, " +
            "departure_line, departure_pt, departure_pp, departure_ppth, departure_wings, transition, event) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final static String insert_additionalTrainInformation = "INSERT IGNORE INTO additional_train_info (id, field, value) VALUES (?, ?, ?)";
    private final static String insert_additionalJourneyInformation = "INSERT IGNORE INTO additional_journey_info (id, position, field, value) VALUES (?, ?, ?, ?)";

    private final static String replaceInto_JourneyChanges = "REPLACE INTO journey_changes (id, position, station, " +
            "arrival_line, arrival_ct, arrival_cp, arrival_cpth, arrival_wings, " +
            "departure_line, departure_ct, departure_cp, departure_cpth, departure_wings, transition, arrival_cs, departure_cs, arrival_clt, departure_clt) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";;

    public static boolean createDatabase() {
        String createTable_Train = "CREATE TABLE IF NOT EXISTS train (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "category VARCHAR(10) NOT NULL, " +
                "number INT UNSIGNED NOT NULL, " +
                "owner VARCHAR(10) NOT NULL, " +
                "timestamp DATETIME NOT NULL," +
                "origin VARCHAR(100) NOT NULL, " +
                "destination VARCHAR(100) NOT NULL" +
                "locked BOOLEAN NOT NULL DEFAULT FALSE);";

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
                "transition VARCHAR(50) DEFAULT NULL, " +
                "event CHAR(1) NOT NULL DEFAULT 'p', " +
                "PRIMARY KEY (id, position), " +
                "FOREIGN KEY (id) REFERENCES train(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (station) REFERENCES station(id) ON DELETE CASCADE);";

        String createTable_additionalTrainInformation = "CREATE TABLE IF NOT EXISTS additional_train_info (" +
                "id VARCHAR(50) NOT NULL, " +
                "field VARCHAR(50) NOT NULL, " +
                "value VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY (id, field), " +
                "FOREIGN KEY (id) REFERENCES train(id) ON DELETE CASCADE);";

        String createTable_JourneyChanges = "CREATE TABLE IF NOT EXISTS journey_changes (" +
                "id VARCHAR(50) NOT NULL, " +
                "position TINYINT UNSIGNED NOT NULL, " +
                "station INT UNSIGNED NOT NULL, " +
                "arrival_line VARCHAR(3) DEFAULT NULL, " +
                "arrival_ct DATETIME DEFAULT NULL, " +
                "arrival_cp VARCHAR(3) DEFAULT NULL, " +
                "arrival_cpth TEXT DEFAULT NULL, " +
                "arrival_wings TEXT DEFAULT NULL, " +
                "departure_line VARCHAR(3) DEFAULT NULL, " +
                "departure_ct DATETIME DEFAULT NULL, " +
                "departure_cp VARCHAR(3) DEFAULT NULL, " +
                "departure_cpth TEXT DEFAULT NULL, " +
                "departure_wings TEXT DEFAULT NULL, " +
                "transition VARCHAR(50) DEFAULT NULL, " +
                "arrival_cs CHAR(1) DEFAULT NULL, " +
                "departure_cs CHAR(1) DEFAULT NULL, " +
                "arrival_clt DATETIME DEFAULT NULL, " +
                "departure_clt DATETIME DEFAULT NULL, " +
                "PRIMARY KEY (id, position), " +
                "FOREIGN KEY (id) REFERENCES train(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (station) REFERENCES station(id) ON DELETE CASCADE);";

        /*
        String createTable_additionalJourneyInformation = "CREATE TABLE IF NOT EXISTS additional_journey_info (" +
                "id VARCHAR(50) NOT NULL, " +
                "position TINYINT UNSIGNED NOT NULL," +
                "field VARCHAR(50) NOT NULL, " +
                "value VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY (id, position, field), " +
                "FOREIGN KEY (id, position) REFERENCES journey(id, position) ON DELETE CASCADE);";
         */


        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(insert_Station)) {

            stmt.executeUpdate(createTable_Train);
            System.out.println("Tabelle 'train' erstellt oder existiert bereits.");
            stmt.executeUpdate(createTable_Station);
            System.out.println("Tabelle 'station' erstellt oder existiert bereits.");
            stmt.executeUpdate(createTable_Journey);
            System.out.println("Tabelle 'journey' erstellt oder existiert bereits.");
            stmt.executeUpdate(createTable_additionalTrainInformation);
            System.out.println("Tabelle 'additional_train_info' erstellt oder existiert bereits.");
            stmt.executeUpdate(createTable_JourneyChanges);
            System.out.println("Tabelle 'journey_changes' erstellt oder existiert bereits.");
            //stmt.executeUpdate(createTable_additionalJourneyInformation);
            //System.out.println("Tabelle 'additional_journey_info' erstellt oder existiert bereits.");

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
                pstmt.setString(6, train.getOrigin());
                pstmt.setString(7, train.getDestination());
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

    public static boolean insertTrainsFromChanges(int evaNo, List<TrainChanges> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insert_Train)) {
            conn.setAutoCommit(false);
            for (TrainChanges train : trains) {
                if (!(train.getOwner() != null && train.getOwner().startsWith("N4"))) {
                    continue;
                }
                if (train.getNumber() == null || train.getOrigin() == null || train.getDestination() == null) {
                    continue;
                }
                pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                pstmt.setString(2, train.getCategory());
                pstmt.setInt(3, Integer.parseInt(train.getNumber()));
                pstmt.setString(4, train.getOwner());
                pstmt.setString(5, train.getTimestamp().toString());
                pstmt.setString(6, train.getOrigin());
                pstmt.setString(7, train.getDestination());
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
                if (train.getDeparture() != null) {
                    JourneyInfo journey = train.getDeparture();
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
                if (train.getDeparture() != null && train.getDeparture().getTransition() != null) {
                    pstmt.setString(14, train.getDeparture().getTransition());
                } else if (train.getArrival() != null && train.getArrival().getTransition() != null) {
                    pstmt.setString(14, train.getArrival().getTransition());
                } else {
                    pstmt.setString(14, null);
                }
                pstmt.setString(15, "p");
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

    public static boolean insertJourneyFromChanges(int evaNo, List<TrainChanges> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insert_Journey)) {
            conn.setAutoCommit(false);
            for (TrainChanges train : trains) {
                if (!(train.getOwner() != null && train.getOwner().startsWith("N4"))) {
                    continue;
                }
                if (train.getNumber() == null || train.getOrigin() == null || train.getDestination() == null) {
                    continue;
                }
                pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                pstmt.setInt(2, Integer.parseInt(train.getId().substring(train.getId().lastIndexOf("-")+1)));
                pstmt.setInt(3, evaNo);
                if (train.getArrival() != null) {
                    JourneyChangesInfo journey = train.getArrival();
                    pstmt.setString(4, journey.getLine());
                    if (journey.getPlannedTime() != null) {
                        pstmt.setString(5, journey.getPlannedTime().toString());
                    } else {
                        pstmt.setString(5, null);
                    }
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
                if (train.getDeparture() != null) {
                    JourneyChangesInfo journey = train.getDeparture();
                    pstmt.setString(9, journey.getLine());
                    if (journey.getPlannedTime() != null) {
                        pstmt.setString(10, journey.getPlannedTime().toString());
                    } else {
                        pstmt.setString(10, null);
                    }
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
                if (train.getDeparture() != null && train.getDeparture().getTransition() != null) {
                    pstmt.setString(14, train.getDeparture().getTransition());
                } else if (train.getArrival() != null && train.getArrival().getTransition() != null) {
                    pstmt.setString(14, train.getArrival().getTransition());
                } else {
                    pstmt.setString(14, null);
                }

                if (train.getDeparture() != null && train.getDeparture().getEventStatus() != null) {
                    pstmt.setString(15, train.getDeparture().getEventStatus());
                } else if (train.getArrival() != null && train.getArrival().getEventStatus() != null) {
                    pstmt.setString(15, train.getArrival().getEventStatus());
                } else {
                    pstmt.setString(15, "a");
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

    public static boolean insertJourneyChanges(int evaNo, List<TrainChanges> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(replaceInto_JourneyChanges)) {
            conn.setAutoCommit(false);
            for (TrainChanges train : trains) {
                try {
                    pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                    pstmt.setInt(2, Integer.parseInt(train.getId().substring(train.getId().lastIndexOf("-")+1)));
                    pstmt.setInt(3, evaNo);
                    if (train.getArrival() != null) {
                        JourneyChangesInfo journey = train.getArrival();
                        pstmt.setString(4, journey.getLine());
                        if (journey.getChangedTime() != null) {
                            pstmt.setString(5, journey.getChangedTime().toString());
                        } else {
                            pstmt.setString(5, null);
                        }
                        pstmt.setString(6, journey.getChangedPlattform());
                        pstmt.setString(7, journey.getChangedPath());
                        pstmt.setString(8, journey.getChangedWings());

                    } else {
                        pstmt.setString(4, null);
                        pstmt.setString(5, null);
                        pstmt.setString(6, null);
                        pstmt.setString(7, null);
                        pstmt.setString(8, null);
                    }
                    if (train.getDeparture() != null) {
                        JourneyChangesInfo journey = train.getDeparture();
                        pstmt.setString(9, journey.getLine());
                        if (journey.getChangedTime() != null) {
                            pstmt.setString(10, journey.getChangedTime().toString());
                        } else {
                            pstmt.setString(10, null);
                        }
                        pstmt.setString(11, journey.getChangedPlattform());
                        pstmt.setString(12, journey.getChangedPath());
                        pstmt.setString(13, journey.getChangedWings());
                    } else {
                        pstmt.setString(9, null);
                        pstmt.setString(10, null);
                        pstmt.setString(11, null);
                        pstmt.setString(12, null);
                        pstmt.setString(13, null);
                    }
                    if (train.getDeparture() != null && train.getDeparture().getTransition() != null) {
                        pstmt.setString(14, train.getDeparture().getTransition());
                    } else if (train.getArrival() != null && train.getArrival().getTransition() != null) {
                        pstmt.setString(14, train.getArrival().getTransition());
                    } else {
                        pstmt.setString(14, null);
                    }

                    if (train.getArrival() != null && train.getArrival().getEventStatus() != null) {
                        pstmt.setString(15, train.getArrival().getEventStatus());
                    } else {
                        pstmt.setString(15, null);
                    }

                    if (train.getDeparture() != null && train.getDeparture().getEventStatus() != null) {
                        pstmt.setString(16, train.getDeparture().getEventStatus());
                    } else {
                        pstmt.setString(16, null);
                    }

                    if (train.getArrival() != null && train.getArrival().getCanceledTime() != null) {
                        pstmt.setString(17, train.getArrival().getCanceledTime().toString());
                    } else {
                        pstmt.setString(17, null);
                    }

                    if (train.getDeparture() != null && train.getDeparture().getCanceledTime() != null) {
                        pstmt.setString(18, train.getDeparture().getCanceledTime().toString());
                    } else {
                        pstmt.setString(18, null);
                    }
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    // System.err.println("Fehler beim Einfügen von Train-ID: " + train.getId() + " -> " + e.getMessage());
                    // Einzelnen fehlerhaften Eintrag ignorieren, aber nicht den ganzen Batch abbrechen
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean insertAdditionalTrainInformation(List<Train> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insert_additionalTrainInformation)) {
            conn.setAutoCommit(false);
            for (Train train : trains) {
                if (!train.getOwner().startsWith("N4")) {
                    continue;
                }
                if (train.getDeparture() != null && train.getDeparture().getPlannedDestination() != null) {
                    pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                    pstmt.setString(2, "planned_destination");
                    pstmt.setString(3, train.getDeparture().getPlannedDestination());
                    pstmt.addBatch();
                } else if (train.getArrival() != null && train.getArrival().getPlannedDestination() != null) {
                    pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                    pstmt.setString(2, "planned_destination");
                    pstmt.setString(3, train.getArrival().getPlannedDestination());
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*public static boolean insertAdditionalJourneyInformation(List<Train> trains) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insert_additionalJourneyInformation)) {
            conn.setAutoCommit(false);
            for (Train train : trains) {
                if (!train.getOwner().startsWith("N4")) {
                    continue;
                }
                if (train.getDeparture() != null && train.getDeparture().getTransition() != null) {
                    pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                    pstmt.setInt(2, Integer.parseInt(train.getId().substring(train.getId().lastIndexOf("-")+1)));
                    pstmt.setString(3, "transition");
                    pstmt.setString(4, train.getDeparture().getTransition());
                    pstmt.addBatch();
                } else if (train.getArrival() != null && train.getArrival().getTransition() != null) {
                    pstmt.setString(1, train.getId().substring(0, train.getId().lastIndexOf("-")));
                    pstmt.setInt(2, Integer.parseInt(train.getId().substring(train.getId().lastIndexOf("-")+1)));
                    pstmt.setString(3, "transition");
                    pstmt.setString(4, train.getArrival().getTransition());
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }*/

    public static int cleanTrains() {
        if (LocalTime.now().getHour() == 0) {
            String sql = "DELETE FROM train WHERE timestamp < ? AND locked = 0;";
            String dateTwoDaysAgo = LocalDate.now().minusDays(2).toString();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, dateTwoDaysAgo);

                return stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
        return -1;
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
