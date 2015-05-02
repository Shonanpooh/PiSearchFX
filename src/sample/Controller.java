package sample;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;


import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.scene.AccessibleRole;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


public class Controller  implements Initializable{
    private Task worker;

    private int time_out = 1000;

    @FXML
    private ProgressBar searchProgress = new ProgressBar(0);

    @FXML
    private Button btnSearch;

    @FXML
    private ListView<String> resultList = new ListView<>();

    @FXML
    private Label statusLabel;

    @FXML
    private TextField fromIP1 = new TextField();

    @FXML
    private TextField fromIP2 = new TextField();

    @FXML
    private TextField fromIP3 = new TextField();

    @FXML
    private TextField fromIP4 = new TextField();

    @FXML
    private TextField toIP1 = new TextField();

    @FXML
    private TextField toIP2 = new TextField();

    @FXML
    private TextField toIP3 = new TextField();

    @FXML
    private TextField toIP4 = new TextField();

    private ObservableList<String> listRecords = FXCollections.observableArrayList();

    // memo
    // Class A 10.0.0.0～10.255.255.255 (10.0.0.0/8)           ->
    // Class B 172.16.0.0～172.31.255.255 (172.16.0.0/12)
    // Class C 192.168.0.0～192.168.255.255 (192.168.0.0/16)   ->Beagle

    @FXML
    void handleButtonAction(ActionEvent event) {

        if(btnSearch.getText().equals("Stop")) {
            // task cancel
            worker.cancel(true);

            // button initialize
            searchProgress.progressProperty().unbind();
            searchProgress.setProgress(0);
            btnSearch.setText("Search");
        }
        else if(inputCheck()){
            //setDisable() is used to enable or disable the elements
            btnSearch.setDisable(false);
            btnSearch.setText("Stop");
            listRecords.clear();

            searchProgress.progressProperty().unbind();
            searchProgress.setProgress(0);

            //creating a new task
            worker = createWorker();

            //binding it with that task
            searchProgress.progressProperty().bind(worker.progressProperty());

            //you can pass any value while task is executing and it can printed on console or set to label
            worker.messageProperty().addListener(new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    // add result List
                    listRecords.add(newValue);
                    resultList.setItems(listRecords);
                }
            });

            // task started on new thread
            new Thread(worker).start();
        }
    }

    public Task createWorker() {
        return new Task() {
            // call() should be overridden

            @Override
            protected Object call() throws Exception {

                byte searchAddress[] = new byte[4];
                searchAddress[0] = Integer.parseInt(fromIP1.getText()) < 127 ? (byte)(Integer.parseInt(fromIP1.getText())) : (byte)(Integer.parseInt(fromIP1.getText()) & 0xff);
                searchAddress[1] = Integer.parseInt(fromIP2.getText()) < 127 ? (byte)(Integer.parseInt(fromIP2.getText())) : (byte)(Integer.parseInt(fromIP2.getText()) & 0xff);

                // Setting IP
                int addr3s = Integer.parseInt(fromIP3.getText());
                int addr4s = Integer.parseInt(fromIP4.getText());
                long maxSearch = (Integer.parseInt(toIP3.getText()) + 1 - Integer.parseInt(fromIP3.getText())) *
                        (Integer.parseInt(toIP4.getText()) + 1  - Integer.parseInt(fromIP4.getText()));

                int i = 1;
                for(int addr3 = addr3s; addr3 < Integer.parseInt(toIP3.getText()) +1  ; addr3++){
                    for(int addr4 = addr4s; addr4 < Integer.parseInt(toIP4.getText()) + 1 ; addr4++){
                        if (isCancelled()) {
                            updateMessage("Search Canceled!");
                            break;
                        }

                        searchAddress[2] = addr3 < 127 ? (byte)(addr3) : (byte)(addr3 & 0xff);
                        searchAddress[3] = addr4 < 127 ? (byte)(addr4) : (byte)(addr4 & 0xff);

                        // update progress bar
                        updateProgress(i, maxSearch);

                        // isReachable?
                        InetAddress ipAdder = InetAddress.getByAddress(searchAddress);
                        if (ipAdder.isReachable(time_out)) {
                            updateMessage(fromIP1.getText() + "." + fromIP2.getText() + "." + Integer.toString(addr3) + "." + Integer.toString(addr4));
                        }
                        System.out.println(fromIP1.getText() + "." + fromIP2.getText() + "." + Integer.toString(addr3) + "." + Integer.toString(addr4));
                        i++;
                    }
                }

                updateMessage("Search End!");
                worker.cancel(true);
                btnSearch.setText("Search");

                return true;
            }
        };
    }

    // minimum level check
    private  boolean inputCheck() {
        boolean retVal = true;

        if(Integer.parseInt(fromIP4.getText()) < 1  || Integer.parseInt(fromIP4.getText()) > 256 ||
           Integer.parseInt(toIP4.getText()) < 1  || Integer.parseInt(toIP4.getText()) > 256){

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("IPまともに入れろや");
            alert.setContentText("ねぼけんな!");

            alert.showAndWait();

            retVal = false;
        }




        return retVal;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        assert searchProgress != null : "fx:id=\"srchProg\" was not injected: check your FXML file 'sample.fxml'.";
        assert btnSearch != null : "fx:id=\"btnSearch\" was not injected: check your FXML file 'sample.fxml'.";

        fromIP1.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue) {
                    toIP1.setText(fromIP1.getText());   // focus out only
                }
            }
        });

        fromIP2.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue){
                    toIP2.setText(fromIP2.getText());    // focus out only
                }
            }
        });


        btnSearch.setAccessibleRole(AccessibleRole.BUTTON);
        btnSearch.setAccessibleText("Please push button,so you will login. this is");
    }
}
