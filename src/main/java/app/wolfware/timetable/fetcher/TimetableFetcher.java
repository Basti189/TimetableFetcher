package app.wolfware.timetable.fetcher;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import app.wolfware.timetable.fetcher.security.Credentials;
import app.wolfware.timetable.fetcher.sql.DBUtils;
import app.wolfware.timetable.fetcher.train.JourneyChangesInfo;
import app.wolfware.timetable.fetcher.train.Train;
import app.wolfware.timetable.fetcher.train.TrainChanges;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TimetableFetcher {

    private static final String API_URL = "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/";
    private static final long INTERVAL_MS = 1000; // 1 Sekunde zwischen Anfragen
    public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
    private final static DateTimeFormatter formatterHour = DateTimeFormatter.ofPattern("HH");
    private final static DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyMMdd");

    public final static boolean FIRST_START = false;

    private boolean callFullChangeData = true;

    public TimetableFetcher() {

        if (!DBUtils.createDatabase()) {
            System.out.println("Beim Erstellen oder Verbinden zur Datenbank ist ein Fehler aufgetreten!");
            return;
        }

        List<Station> stations = DBUtils.getAllStations();

        if (stations.isEmpty()) {
            System.out.println("Es sind keine Stationen gelistet!");
            return;
        }
        if (FIRST_START) {
            fetchDataOnStartUp(stations);
        } else {
            System.out.println("Abruf 'OnStartUp' wird übersprungen!");
        }

        Thread thread = new Thread(() -> {
            while (true) {
                long endTime = System.currentTimeMillis() + (waitTime() * 1000);
                while (System.currentTimeMillis() < endTime) {
                    fetchChangesEveryTime(stations);
                    try {
                        Thread.sleep(5000); // Kleiner Delay, um CPU-Last zu vermeiden
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Unterbrechen behandeln
                        return;
                    }
                }
                int deleteTrains = DBUtils.cleanTrains();
                if (deleteTrains > 0) {
                    System.out.println("Gelöschte Datensätze: " + deleteTrains);
                }
                System.out.println("----------------------");
                fetchDataEveryHour(stations);
                System.out.println("----------------------");
            }
        });
        thread.start();
    }

    private long waitTime() {
        // Berechne die Wartezeit bis zur nächsten vollen Stunde und 2 Minuten
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextFullHour = now.withMinute(2).withSecond(0).withNano(0).plusHours(1); // Nächste volle Stunde + 2 Min

        // Berechne, wie lange es noch bis zur nächsten vollen Stunde dauert
        long waitTimeInSeconds = ChronoUnit.SECONDS.between(now, nextFullHour);
        System.out.println("Warte bis zur nächsten vollen Stunde: " + nextFullHour);
        return waitTimeInSeconds;
    }

    private void fetchDataEveryHour(List<Station> stations) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(18);
        System.out.println("Abruf von SOLL-Daten " + future.toString());
        for (Station station : stations) {
            //System.out.println("Abruf " + station.getName() + "[" + station.getId() + "]" + " " + future.toString());
            long startTime = System.nanoTime();
            String response = fetchData(station.getId(), formatterDate.format(future), formatterHour.format(future));

            if (response != null) {
                List<Train> list = processXMLResponse(response, station.getName());
                //processXMLLiveData(fetchChanges(evaNo), list);
                mergeWingTrains(list);
                DBUtils.insertTrains(station.getId(), list);
                DBUtils.insertJourneys(station.getId(), list);
                DBUtils.insertAdditionalTrainInformation(list);
                //DBUtils.insertAdditionalJourneyInformation(list);
            }
            long elapsedTime = (System.nanoTime() - startTime) / 1_000_000;
            long sleepTime = INTERVAL_MS - elapsedTime; // Verbleibende Wartezeit berechnen
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime); // Nur warten, wenn nötig
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread wurde unterbrochen!");
                }
            }
        }
    }

    private void fetchDataOnStartUp(List<Station> stations) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH");
        System.out.println("Abruf von SOLL-Daten " + now.toString());
        // for (int i = -10; i <= 18; i++) {
        for (int i = -10; i <= 18; i++) {
            LocalDateTime time = now.plusHours(i);
            System.out.println("Abruf von SOLL-Daten " + time.toString());
            for (Station station : stations) {
                //System.out.println("Abruf " + station.getName() + "[" + station.getId() + "]" + " " + formatter.format(time) + ":00");
                long startTime = System.nanoTime();
                String response = fetchData(station.getId(), formatterDate.format(time), formatterHour.format(time));

                if (response != null) {
                    List<Train> list = processXMLResponse(response, station.getName());
                    //processXMLLiveData(fetchChanges(evaNo), list);
                    mergeWingTrains(list);
                    DBUtils.insertTrains(station.getId(), list);
                    DBUtils.insertJourneys(station.getId(), list);
                    DBUtils.insertAdditionalTrainInformation(list);
                    //DBUtils.insertAdditionalJourneyInformation(list);
                }
                long elapsedTime = (System.nanoTime() - startTime) / 1_000_000;
                long sleepTime = INTERVAL_MS - elapsedTime; // Verbleibende Wartezeit berechnen
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime); // Nur warten, wenn nötig
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Thread wurde unterbrochen!");
                    }
                }
            }
        }
    }

    private void fetchChangesEveryTime(List<Station> stations) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Abruf von IST-Daten " + now.toString());
        for (Station station : stations) {
            //System.out.println("Abruf " + station.getName() + "[" + station.getId() + "]" + " " + now.toString());
            long startTime = System.nanoTime();
            String response = fetchChanges(station.getId());

            if (response != null) {
                List<TrainChanges> list = processXMLChangesResponse(response, station.getName());
                DBUtils.insertTrainsFromChanges(station.getId(), list);
                DBUtils.insertJourneyFromChanges(station.getId(), list);
                DBUtils.insertJourneyChanges(station.getId(), list);
            }
            try {
                Thread.sleep(1000); // Nur warten, wenn nötig
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread wurde unterbrochen!");
            }
        }
        if (callFullChangeData) {
            callFullChangeData = false;
            System.out.println("Nächste Runde nur noch relevante Changes abrufen");
        }
    }

    private String fetchData(int evaNo, String date, String hour) {
        try {
            // Construct the full API URL
            String urlString = API_URL + "plan/" + evaNo + "/" + date + "/" + hour;
            //System.out.println(urlString);
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Set up the connection properties
            connection.setRequestMethod("GET");
            connection.setRequestProperty("DB-Api-Key", Credentials.API_KEY);
            connection.setRequestProperty("DB-Client-Id", Credentials.CLIENT_ID);

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                System.out.println("Failed to fetch data. HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String fetchChanges(int evaNo) {
        try {
            // Construct the full API URL
            String urlString = API_URL;
            if (callFullChangeData) {
                urlString += "fchg/" + evaNo;
            } else  {
                urlString += "rchg/" + evaNo;
            }
            //System.out.println(urlString);
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Set up the connection properties
            connection.setRequestMethod("GET");
            connection.setRequestProperty("DB-Api-Key", Credentials.API_KEY);
            connection.setRequestProperty("DB-Client-Id", Credentials.CLIENT_ID);

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                System.out.println("Failed to fetch changes. HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void mergeWingTrains(List<Train> list) {
        for (Train train : new ArrayList<>(list)) {
            //System.out.println("Merge: " + train.getNumber());
            if (train.getDeparture() != null && train.getDeparture().getWings() != null) {
                List<Train> trainWings = new ArrayList<>();
                String[] wings = train.getDeparture().getWings().split("\\|");
                for (String wing : wings) {
                    Train searchedTrain = getTrainFromListByID(list, wing);
                    if (searchedTrain != null) {
                        trainWings.add(searchedTrain);
                    }
                }
                if (trainWings.size() > 0) {
                    train.getDeparture().setTrainWings(trainWings);
                }

            }
            if (train.getArrival() != null && train.getArrival().getWings() != null) {
                List<Train> trainWings = new ArrayList<>();
                String[] wings = train.getArrival().getWings().split("\\|");
                for (String wing : wings) {
                    Train searchedTrain = getTrainFromListByID(list, wing);
                    if (searchedTrain != null) {
                        trainWings.add(searchedTrain);
                    }
                }
                if (trainWings.size() > 0) {
                    train.getArrival().setTrainWings(trainWings);
                }

            }
        }
    }

    private Train getTrainFromListByID(List<Train> list, String id) {
        for (Train train : list) {
            if (train.getId().contains(id)) {
                return train;
            }
        }
        return null;
    }

    private List<TrainChanges> processXMLChangesResponse(String xmlData, String actualStationName) {
        List<TrainChanges> list = new ArrayList<>();
        try {
            // Parse the XML data
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes()));

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            LocalDateTime currentTime = LocalDateTime.now();
            //System.out.println("Abfrage um " + currentTime.getHour() + ":" + currentTime.getMinute() + ":" + currentTime.getSecond());

            // Extract timetable information
            NodeList stations = doc.getElementsByTagName("s");
            for (int i = 0; i < stations.getLength(); i++) {
                TrainChanges train = new TrainChanges(stations.item(i), actualStationName);
                if (train.getArrival() == null && train.getDeparture() == null) {
                    continue;
                }
                list.add(train);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Train> processXMLResponse(String xmlData, String actualStationName) {
        List<Train> list = new ArrayList<>();
        try {
            // Parse the XML data
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes()));

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            LocalDateTime currentTime = LocalDateTime.now();
            //System.out.println("Abfrage um " + currentTime.getHour() + ":" + currentTime.getMinute() + ":" + currentTime.getSecond());

            // Extract timetable information
            NodeList stations = doc.getElementsByTagName("s");
            for (int i = 0; i < stations.getLength(); i++) {
                Train train = new Train(stations.item(i), actualStationName);
                list.add(train);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}