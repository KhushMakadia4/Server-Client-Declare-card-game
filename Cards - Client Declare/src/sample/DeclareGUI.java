/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import javax.swing.*;

import javafx.scene.shape.Rectangle;
import org.omg.CORBA.IMP_LIMIT;
import org.w3c.dom.css.Rect;
import socketfx.Constants;
import socketfx.FxSocketClient;
import socketfx.SocketListener;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */
public class DeclareGUI implements Initializable {//all documentation can be found on the server file

    @FXML
    private AnchorPane startPane;
    @FXML
    private Button connectButton, readyButton,playAgainBtn, discardBtn, declareBtn;
    @FXML
    private ImageView imgC0, imgC1, imgC2, imgC3, imgC4, imgC5, imgC6, imgC7, imgC8, imgC9,
            imgS0D, imgS1D, imgS2D, imgS3D, imgS4D, imgS5D, imgS6D, imgS7D, imgS8D, imgS9D,
            imgDiscDrop, deckPickUp;
    @FXML
    private GridPane gPaneClient, gPaneServerDiscard;

    @FXML
    private Label turnInstLbl,declareLbl;

    @FXML
    private TableColumn clientCol = new TableColumn<TableCellData, Integer>("Client");
    @FXML
    private TableColumn serverCol = new TableColumn<TableCellData,Integer>("Server");

    @FXML
    private TableView roundInfoTbl;

    private ArrayList<String> cHand = new ArrayList<>();
    private ArrayList<String> discHand = new ArrayList<>();
    private ArrayList<String> serverDiscard = new ArrayList<>();
    private ArrayList<ImageView> cImgs = new ArrayList<>();
    private ArrayList<ImageView> sImgs = new ArrayList<>();//all backs just reducing in size once server discard comes in
    private ArrayList<ImageView> sDiscImgs = new ArrayList<>();
    private double startX;
    private double startY;
    private double startTranslateX;
    private double startTranslateY;
    private boolean discardDone = false;
    private boolean cardPicked = false;
    private boolean serverDone = false;
    private boolean gameOver = false;
    ArrayList<TableCellData> data = new ArrayList<>();


    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());


    private FxSocketClient socket;

    private void connect() {

        socket = new FxSocketClient(new FxSocketListener(),
                "localhost",
                2015,
                Constants.instance().DEBUG_NONE);
        socket.connect();
        readyButton.setDisable(false);
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientPoints"));
        serverCol.setCellValueFactory(new PropertyValueFactory<>("serverPoints"));

        Runtime.getRuntime().addShutdownHook(new ShutDownThread());

        //arraylists
        cImgs.add(imgC0);
        cImgs.add(imgC1);
        cImgs.add(imgC2);
        cImgs.add(imgC3);
        cImgs.add(imgC4);
        cImgs.add(imgC5);
        cImgs.add(imgC6);
        cImgs.add(imgC7);
        cImgs.add(imgC8);
        cImgs.add(imgC9);

        sDiscImgs.add(imgS0D);
        sDiscImgs.add(imgS1D);
        sDiscImgs.add(imgS2D);
        sDiscImgs.add(imgS3D);
        sDiscImgs.add(imgS4D);
        sDiscImgs.add(imgS5D);
        sDiscImgs.add(imgS6D);
        sDiscImgs.add(imgS7D);
        sDiscImgs.add(imgS8D);
        sDiscImgs.add(imgS9D);

        imgDiscDrop.setImage(new Image("resources/discBack.jpg"));
        deckPickUp.setImage(new Image("resources/BACK-7.jpg"));
    }

    class ShutDownThread extends Thread {

        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");
                }
                socket.shutdown();
            }
        }
    }

    class FxSocketListener implements SocketListener {

        @Override
        public void onMessage(String line) {
            switch (line.substring(0, line.indexOf(":"))) {
                case "cHand"://gives a hand to the client
                    startPane.setVisible(false);
                    cHand.clear();
                    String[] tempHand = line.substring(line.indexOf(":") + 1).split(",");
                    for (int i = 0; i < tempHand.length; i++) {//creates the array of hand
                        cHand.add(tempHand[i]);
                    }
                    if (tempHand.length < (cHand.size() + discHand.size())) {//if the discarded hand needs to be cleared, shows if the new hand is less than the previous total
                        discHand.clear();
                    }
                    updHand();

                    break;
                //error of discard: return discHand to cHand array and display error in displaying cards
                case "errorDisc":
                    for (String x : discHand) {
                        cHand.add(x);
                    }
                    discHand.clear();
                    updHand();
                    updDiscHand();
                    JOptionPane.showMessageDialog(null, "Your discard was either not complying any of the possible combinations or in wrong order");
                    break;
                //successful discard: clear discard imgviews and array
                case "discard Successful":
                    discardDone = true;
                    turnUpdate();
                    discHand.clear();
                    updDiscHand();
                    //server's discard
                    break;
                case "serverDone"://when the server has finished their turn
                    String[] tempArr = line.substring(line.indexOf(":") + 1).split(",");
                    for (String s : tempArr) {//updates the serverdiscard array
                        serverDiscard.add(s);
                    }
                    for (int i = 0; i < sDiscImgs.size(); i++) {//shows server discard on imageview
                        try {
                            sDiscImgs.get(i).setImage(new Image(serverDiscard.get(i)));
                        }catch (Exception e) {
                            sDiscImgs.get(i).setImage(null);
                        }

                    }
                    serverDone = true;
                    turnUpdate();
                    break;
                case "deckCard"://accepting output from request made to pick a card up from the deck
                    cHand.add(line.substring(line.indexOf(":")+1));
                    updHand();
                    updDiscHand();
                    declareBtn.setDisable(true);
                    break;
                case "serverWins"://server has x points, client has 0 //update tableview etc
                    declareLbl.setText("SERVER WINS");
                    playAgainBtn.setVisible(true);
                    int points = Integer.parseInt(line.substring(line.indexOf(":")+1));
                    data.add(new TableCellData(0,points));
                    roundInfoTbl.setItems(FXCollections.observableList(data));
                    //tableview
                    break;
                case "clientWins"://server has 0 client has x points
                    declareLbl.setText("CLIENT WINS");
                    playAgainBtn.setVisible(true);
                    int pts = Integer.parseInt(line.substring(line.indexOf(":")+1));
                    data.add(new TableCellData(pts,0));
                    roundInfoTbl.setItems(FXCollections.observableList(data));
                    break;
                case "draw"://server has x points, client has y points
                    declareLbl.setText("THE GAME WAS A TIE");
                    playAgainBtn.setVisible(true);
                    String[] temp = line.substring(line.indexOf(":")+1).split(",");
                    data.add(new TableCellData(Integer.parseInt(temp[0]), Integer.parseInt(temp[1])));
                    roundInfoTbl.setItems(FXCollections.observableList(data));
                    break;
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {

        }
    }


    public void turnUpdate() {//step 1: make discard, step 2: pick a card, step 3: wait for server
        if (discardDone) {//card pickup enabled step 1 finished enable step 2
            for (ImageView x : sDiscImgs) {
                x.setDisable(false);
            }
            deckPickUp.setDisable(false);
            turnInstLbl.setText("Please pick a card from the deck or the server's discards");
            discardBtn.setDisable(true);
            declareBtn.setDisable(true);
            for (ImageView x : cImgs) {//step 1 imgviews
                x.setDisable(true);
            }
        }
        if (discardDone && cardPicked) {//picked a card and dealt a discard, step 1 and step 2 done
            discardDone = false;
            cardPicked = false;
            serverDiscard.clear();
            String msg = "";
            for (String x: discHand) {
                msg+=x+",";
            }
            socket.sendMessage("clientDone:"+msg);//allow server to
            for (ImageView x : sDiscImgs) {//step 2 imgviews
                x.setDisable(true);
            }
            for (ImageView x : cImgs) {//step 1 imgviews
                x.setDisable(true);
            }
            imgDiscDrop.setDisable(true);//step 1 imgview
            deckPickUp.setDisable(true);//step 2 imgview
            turnInstLbl.setText("Wait for server");
        }

        if (serverDone) {//step 3 done, enable steps 1
            discHand.clear();
            serverDone = false;
            for (ImageView x : cImgs) {//step 1 imgviews
                x.setDisable(false);
            }
            imgDiscDrop.setDisable(false);//step 1 imgview
            turnInstLbl.setText("Pick your discards");
            declareBtn.setDisable(false);
            discardBtn.setDisable(false);
        }

        if (gameOver) {//disable everything
            for (ImageView x : sDiscImgs) {//step 2 imgviews
                x.setDisable(true);
            }
            for (ImageView x : cImgs) {//step 1 imgviews
                x.setDisable(true);
            }
            imgDiscDrop.setDisable(true);//step 1 imgview
            deckPickUp.setDisable(true);//step 2 imgview
        }

    }

    @FXML
    public void handleConnectButton(ActionEvent event) {
        connectButton.setDisable(true);
        connect();
    }

    @FXML
    public void clientReady() {
        socket.sendMessage("clientReady:");
        playAgainBtn.setVisible(false);
        declareLbl.setText("");

        for (ImageView x: cImgs) {
            x.setDisable(false);
        }
        discardBtn.setDisable(false);
        declareBtn.setDisable(false);
    }




    public void updHand() {
        try {
            for (int i =0;i<cImgs.size();i++) {
                cImgs.get(i).setImage(null);
                cImgs.get(i).setImage(new Image(cHand.get(i)));
            }
        }catch (Exception e) {}
    }

    public void updDiscHand() {
        try {
            if (discHand.size()>0) {
                imgDiscDrop.setDisable(false);
                imgDiscDrop.setImage(new Image(discHand.get(discHand.size() - 1)));
            } else {
                if (!discardDone) {
                    declareBtn.setDisable(false);
                }
                imgDiscDrop.setImage(new Image("resources/discBack.jpg"));
                imgDiscDrop.setDisable(true);
            }
        }catch (Exception e) {}
    }

    @FXML
    public void sendDiscHand() {
        if (!(discHand.size()==0)) {
            String msg = "";
            for (String x : discHand) {
                msg += x + ",";
            }
            socket.sendMessage("clientDiscard:" + msg);
        }
    }

    @FXML
    public void declare() {
        socket.sendMessage("clientDeclares:");
        gameOver = true;
        turnUpdate();
    }


    @FXML
    public void dragDetected(MouseEvent event) {
        ((ImageView)(event.getSource())).setTranslateX(startTranslateX + (event.getSceneX()-startX));
        ((ImageView)(event.getSource())).setTranslateY(startTranslateY + (event.getSceneY()-startY));
    }

    @FXML
    public void pressDetect(MouseEvent event) {
        startX = event.getSceneX();
        startY = event.getSceneY();
        startTranslateX = ((ImageView) (event.getSource())).getTranslateX();
        startTranslateY = ((ImageView) (event.getSource())).getTranslateY();
    }

    @FXML
    public void deckPickup(MouseEvent e) {
        Rectangle imgViewRect = new Rectangle(deckPickUp.getLayoutX()+((ImageView)(e.getSource())).getTranslateX(), deckPickUp.getLayoutY()+((ImageView)(e.getSource())).getTranslateY(),
                ((ImageView)(e.getSource())).getFitWidth(), ((ImageView)(e.getSource())).getFitHeight());
        if (imgViewRect.intersects(gPaneClient.getLayoutX(), gPaneClient.getLayoutY(), gPaneClient.getWidth(), gPaneClient.getHeight())) {
            cardPicked = true;
            turnUpdate();
            for (ImageView x: sDiscImgs) {
                x.setImage(null);
            }
            socket.sendMessage("getDeckCard:"); //make new case in onmessage on the accepting of the card
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

    @FXML
    public void getServerDiscard(MouseEvent e) {
        //if it is their turn, if they havent already picked a card,
        int imgNum = GridPane.getColumnIndex((ImageView)(e.getSource()));
        Rectangle imgViewRect = new Rectangle(gPaneServerDiscard.getLayoutX()+(60*imgNum)+((ImageView)(e.getSource())).getTranslateX(),gPaneServerDiscard.getLayoutY()+((ImageView)(e.getSource())).getTranslateY(),
                ((ImageView)(e.getSource())).getFitWidth(), ((ImageView)(e.getSource())).getFitHeight());
        if (imgViewRect.intersects(gPaneClient.getLayoutX(), gPaneClient.getLayoutY(), gPaneClient.getWidth(), gPaneClient.getHeight())) {
            cardPicked = true;
            turnUpdate();
            try {
                cHand.add(serverDiscard.remove(imgNum));
            }catch (Exception c){}
            updHand();
            for (ImageView x: sDiscImgs) {
                x.setImage(null);
            }
            serverDiscard.clear();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

    @FXML
    public void putDiscard(MouseEvent e) {
        int imgNum = GridPane.getColumnIndex((ImageView) (e.getSource()));
        Rectangle imgViewRect = new Rectangle(gPaneClient.getLayoutX()+(60*imgNum)+((ImageView)(e.getSource())).getTranslateX(), gPaneClient.getLayoutY()+((ImageView)(e.getSource())).getTranslateY(), ((ImageView)(e.getSource())).getFitWidth(), ((ImageView)(e.getSource())).getFitHeight());
        if (imgViewRect.intersects(imgDiscDrop.getLayoutX(),imgDiscDrop.getLayoutY(),imgDiscDrop.getFitWidth(),imgDiscDrop.getFitHeight())) {
            declareBtn.setDisable(true);
            discHand.add(cHand.remove(imgNum));
            updHand();
            updDiscHand();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

    @FXML
    public void undoDiscHand(MouseEvent e) {
        Rectangle imgViewRect = new Rectangle(imgDiscDrop.getLayoutX() + ((ImageView) (e.getSource())).getTranslateX(), imgDiscDrop.getLayoutY() + ((ImageView) (e.getSource())).getTranslateY(), ((ImageView) (e.getSource())).getFitWidth(), ((ImageView) (e.getSource())).getFitHeight());
        if (imgViewRect.intersects(gPaneClient.getLayoutX(), gPaneClient.getLayoutY(), gPaneClient.getWidth(), gPaneClient.getHeight())) {
            cHand.add(discHand.remove(discHand.size()-1));
            updHand();
            updDiscHand();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

}