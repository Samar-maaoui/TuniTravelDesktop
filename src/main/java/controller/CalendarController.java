package controller;
import javafx.event.ActionEvent;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import services.ServiceEvenement;
import entities.Evenement;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

public class CalendarController implements Initializable {

    ZonedDateTime dateFocus;
    ZonedDateTime today;

    @FXML
    private Text year;

    @FXML
    private Text month;

    @FXML
    private FlowPane calendar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateFocus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        drawCalendar();
    }

    @FXML
    void backOneMonth(ActionEvent event) {
        dateFocus = dateFocus.minusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    @FXML
    void forwardOneMonth(ActionEvent event) {
        dateFocus = dateFocus.plusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    private void drawCalendar(){
        year.setText(String.valueOf(dateFocus.getYear()));
        month.setText(String.valueOf(dateFocus.getMonth()));

        double calendarWidth = calendar.getPrefWidth();
        double calendarHeight = calendar.getPrefHeight();
        double strokeWidth = 1;
        double spacingH = calendar.getHgap();
        double spacingV = calendar.getVgap();


        Map<Integer, List<Evenement>> eventMap = getEventsForMonth(dateFocus);

        int monthMaxDate = dateFocus.getMonth().maxLength();

        if(dateFocus.getYear() % 4 != 0 && monthMaxDate == 29){
            monthMaxDate = 28;
        }
        int dateOffset = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), 1,0,0,0,0,dateFocus.getZone()).getDayOfWeek().getValue();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                StackPane stackPane = new StackPane();

                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(strokeWidth);
                double rectangleWidth =(calendarWidth/7) - strokeWidth - spacingH;
                rectangle.setWidth(rectangleWidth);
                double rectangleHeight = (calendarHeight/6) - strokeWidth - spacingV;
                rectangle.setHeight(rectangleHeight);
                stackPane.getChildren().add(rectangle);

                int calculatedDate = (j+1)+(7*i);
                if(calculatedDate > dateOffset){
                    int currentDate = calculatedDate - dateOffset;
                    if(currentDate <= monthMaxDate){
                        Text date = new Text(String.valueOf(currentDate));
                        double textTranslationY = - (rectangleHeight / 2) * 0.75;
                        date.setTranslateY(textTranslationY);
                        stackPane.getChildren().add(date);

                        List<Evenement> events = eventMap.get(currentDate);
                        if(events != null){
                            createEventActivity(events, rectangleHeight, rectangleWidth, stackPane);
                        }
                    }
                    if(today.getYear() == dateFocus.getYear() && today.getMonth() == dateFocus.getMonth() && today.getDayOfMonth() == currentDate){
                        rectangle.setStroke(Color.BLUE);
                    }
                }
                calendar.getChildren().add(stackPane);
            }
        }
    }


    private void createEventActivity(List<Evenement> events, double rectangleHeight, double rectangleWidth, StackPane stackPane) {
        VBox eventBox = new VBox();
        for (int k = 0; k < events.size(); k++) {
            if (k >= 2) {
                Text moreEvents = new Text("...");
                eventBox.getChildren().add(moreEvents);
                moreEvents.setOnMouseClicked(mouseEvent -> {
                    // On ... click print all events for given date
                    System.out.println(events);
                });
                break;
            }

            Text text = new Text(events.get(k).getNom());
            eventBox.getChildren().add(text);

            int finalK = k;
            text.setOnMouseClicked(mouseEvent -> {
                // On Text clicked, show an alert with event details
                showEventDetailsAlert(events.get(finalK));
            });
        }

        eventBox.setTranslateY((rectangleHeight / 2) * 0.20);
        eventBox.setMaxWidth(rectangleWidth * 0.8);  // Restrict the width of the eventBox
        eventBox.setMaxHeight(rectangleHeight * 0.65);  // Restrict the height of the eventBox
        eventBox.setStyle("-fx-background-color: rgba(0, 123, 255, 0.6); -fx-padding: 5; -fx-spacing: 2;");

        for (javafx.scene.Node node : eventBox.getChildren()) {
            if (node instanceof Text) {
                Text t = (Text) node;
                t.setWrappingWidth(rectangleWidth * 0.8);  // Set text wrapping to ensure it stays within bounds
            }
        }

        stackPane.getChildren().add(eventBox);
    }

    private void showEventDetailsAlert(Evenement event) {
        // Create a new alert dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event Details");
        alert.setHeaderText(event.getNom());

        // Create the content of the alert
        TextArea textArea = new TextArea();
        textArea.setEditable(false);  // Make sure the text area is not editable
        textArea.setText("Nom: " + event.getNom() + "\n" +
                "Location: " + event.getLieu() + "\n" +
                "Date: " + event.getDateEvenement() + "\n" +
                "Description: " + event.getDescription()); // Assuming `getDescription()` returns a description of the event
        textArea.setStyle("-fx-font-family: Arial; " +  // Font style
                "-fx-font-size: 16px; " +     // Font size


                "-fx-padding: 10px; " );    // Padding


        // Add the textArea to the alert
        alert.getDialogPane().setContent(textArea);

        alert.showAndWait();
    }


    private Map<Integer, List<Evenement>> createEventMap(List<Evenement> events) {
        Map<Integer, List<Evenement>> eventMap = new HashMap<>();

        for (Evenement event : events) {
            // Convert String to LocalDate
            LocalDate eventDate = LocalDate.parse(event.getDateEvenement());  // assuming 'getDateEvenement' returns String in "yyyy-MM-dd" format
            int eventDay = eventDate.getDayOfMonth();

            if (!eventMap.containsKey(eventDay)) {
                eventMap.put(eventDay, new ArrayList<>(List.of(event)));
            } else {
                List<Evenement> existingList = eventMap.get(eventDay);
                existingList.add(event);
            }
        }
        return eventMap;
    }


    private Map<Integer, List<Evenement>> getEventsForMonth(ZonedDateTime dateFocus) {
        List<Evenement> events = new ArrayList<>();
        int year = dateFocus.getYear();
        int month = dateFocus.getMonth().getValue();

        ServiceEvenement serviceEvenement = new ServiceEvenement();
        events = serviceEvenement.getEvenementsForMonth(year, month);

        return createEventMap(events);
    }
}
