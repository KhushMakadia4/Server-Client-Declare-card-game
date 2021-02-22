/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample;


import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import javax.swing.JOptionPane;

import javafx.scene.shape.Rectangle;
import org.w3c.dom.css.Rect;
import socketfx.Constants;
import socketfx.FxSocketServer;
import socketfx.SocketListener;
import sun.plugin.javascript.navig.Anchor;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */

//ASK WHY IMAGES AREN'T SHOWING UP


public class DeclareGUI implements Initializable {//controller

    @FXML
    private AnchorPane startPane;//its the first screen that appears, will be invisible after game starts
    @FXML
    private Button connectButton, readyButton, playAgainBtn, discardBtn, declareBtn;//all the buttons that are disabled in coordination
    @FXML
    private Label turnInstLbl,declareLbl;//labels that help instruct or inform of an outcome or instructions
    @FXML
    private ImageView imgS0,imgS1,imgS2,imgS3,imgS4,imgS5,imgS6,imgS7,imgS8,imgS9,
                      imgC0D,imgC1D,imgC2D,imgC3D,imgC4D,imgC5D,imgC6D,imgC7D,imgC8D,imgC9D,
                      imgDiscDrop,deckPickUp;


    @FXML
    private TableColumn clientCol = new TableColumn<TableCellData, Integer>("Client");//the column that shows client's points per round
    @FXML
    private TableColumn serverCol = new TableColumn<TableCellData,Integer>("Server");//the column that shows server's points per round

    @FXML
    private TableView roundInfoTbl;//the table with all the points and data

    @FXML
    private GridPane gPaneServer,gPaneClientDiscard;//gridpanes used for collision check

    private ArrayList<Card> deck = new ArrayList<>();//the arraylists keep track of all the cards each person has except for deck which is a literal deck
    private ArrayList<Card> sHand = new ArrayList<>();
    private ArrayList<Card> cHand = new ArrayList<>();
    private ArrayList<Card> discHand = new ArrayList<>();
    private ArrayList<Card> cDiscHand = new ArrayList<>();
    private ArrayList<ImageView> sImgs = new ArrayList<>();//imageview arraylist in order to create more efficient image updating system
    private ArrayList<ImageView> cDiscImgs = new ArrayList<>();
    private double startX;//next four for each imageview for drag and drop feature
    private double startY;
    private double startTranslateX;
    private double startTranslateY;
    private boolean cardPicked;//booleans used to progress the game and go turn by turn
    private boolean discardDone;
    private boolean clientDone;
    private boolean cReady,sReady;
    private boolean gameOver =false;
    ArrayList<TableCellData> data = new ArrayList<>();//contains data that has to be put onto the imageviews

    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());


    private FxSocketServer socket;

    //precondition:none
    //postcondition:connects the server and client by creating a new server socket
    private void connect() {
        socket = new FxSocketServer(new FxSocketListener(),
                2015,
                Constants.instance().DEBUG_NONE);
        socket.connect();
    }


    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    //precondition:no important param except for ones needed for initialize
    //postcondition:initializes imagviews, gives some images etc
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientPoints"));//tableColumns are given properties for later use
        serverCol.setCellValueFactory(new PropertyValueFactory<>("serverPoints"));

        Runtime.getRuntime().addShutdownHook(new ShutDownThread());

        sImgs.add(imgS0);//imageviews are added to corresponding lists
        sImgs.add(imgS1);
        sImgs.add(imgS2);
        sImgs.add(imgS3);
        sImgs.add(imgS4);
        sImgs.add(imgS5);
        sImgs.add(imgS6);
        sImgs.add(imgS7);
        sImgs.add(imgS8);
        sImgs.add(imgS9);
        cDiscImgs.add(imgC0D);
        cDiscImgs.add(imgC1D);
        cDiscImgs.add(imgC2D);
        cDiscImgs.add(imgC3D);
        cDiscImgs.add(imgC4D);
        cDiscImgs.add(imgC5D);
        cDiscImgs.add(imgC6D);
        cDiscImgs.add(imgC7D);
        cDiscImgs.add(imgC8D);
        cDiscImgs.add(imgC9D);
        imgDiscDrop.setImage(new Image("resources/discBack.jpg"));
        deckPickUp.setImage(new Image("resources/BACK-7.jpg"));

        JOptionPane.showMessageDialog(null, "Instructions: The client will start first in this rummy style game. Each turn has 2 steps: step 1 is to make a discard, you make a discard by " +
                "\nputting cards from your hand into the discard imagview, the combinations are: same number in each card regardless of suit, difference of 1 with same suit (4,5,6 of hearts), royal flush (10,J,Q,K,A)," +
                "\n(3,6,9) specifically, (A,5,10) specifically, double combo (you put a 3 first and then a 6, or a 2 first and then a 4), lastly a (A,4,8,Q) combo. After you have made your discard, click the discard button" +
                "\nwhich will let you know if the discard is possible or not. If possible, you may move to step 2, which is picking up exactly one card. You have two options to pick a card, 1 is to pick up a card" +
                "\nfrom the deck or 2, taking one card from the other person's discard meaning that if someone went before you, they have to have had a discard and if you see a good card in their discard, you can" +
                "\ntake it from that collection after they have made their discard. The point of the game is to have the lowest total sum of cards (Jacks,Queens, and kings are all 10 points and the rest are what" +
                "\ntheir face says. You have to select the declare button to end the round and see who wins. Enjoy");
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
        //precondition:String that isn't empty, for messaging back and forth
        //postcondition:sees which string it is and then it does the action corresponding with it
        @Override
        public void onMessage(String line) {
            switch (line.substring(0, line.indexOf(":"))) {
                case "clientDiscard"://accepts the client's discard to check
                    cDiscHand.clear();
                    String[] tempArr = line.substring(line.indexOf(":")+1).split(",");
                    for (String x: tempArr) {//set clients discarded hand
                        for (Card c : cHand) {
                            if (x.equals(c.getCardPath())) {
                                cDiscHand.add(c);
                            }
                        }
                    }
                    checkDisc(cDiscHand, "c");//checks the discard
                    break;
                case "clientDone"://client is done with their turn
                    clientDone = true;
                    turnUpdate();
                    for (int i =0;i<cDiscImgs.size();i++) {//sets the discard imagviews to their corresponding discard
                        try {
                            cDiscImgs.get(i).setImage(new Image(cDiscHand.get(i).getCardPath()));
                        }catch (Exception e) {
                            cDiscImgs.get(i).setImage(null);
                        }
                    }
                    for (Card c : cDiscHand) {//keeping track of the clien's hand
                        cHand.remove(c);
                    }
                    break;
                case "getDeckCard"://client's request for a deck card
                    int remNum = (int)(Math.random()*deck.size());
                    socket.sendMessage("deckCard:"+deck.get(remNum).getCardPath());
                    cHand.add(deck.get(remNum));
                    deck.remove(remNum);
                    break;
                case "clientReady"://client is ready to play the game
                    cReady = true;
                    dealCard();
                    break;
                case "clientDeclares"://client has declared
                    declare();
                    break;
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
           
        }
    }

    //precondition:Non-empty list of cards, string to see for who we are checking
    //postcondition:
    public void checkDisc(ArrayList<Card> x, String editPer) {
        //all the different combinations
        boolean run = true;
        boolean same = true;
        boolean royalFlush = true;
        boolean vectorCombo = true;//ace, 4, 8, queen
        boolean strap = true;//3,6,9
        boolean quantumCombo = true;//ace, 5, 10
        boolean doubleCombo = true;//any card and its double amount with same suit

        //run
        for (int i =0;i<x.size()-1;i++) {//checks for a run, example: 3,4,5,6 of hearts
            if (!(x.get(i+1).getCardNumber()-x.get(i).getCardNumber()==1) || !(x.get(i).getCardSuit().equals(x.get(i).getCardSuit()))) {
                System.out.println("run");
                run = false;
            }
        }

        //same
        for (int i = 0;i<x.size()-1;i++) {//checks for cards with the same number regardless of suit
            if (!(x.get(i).getCardNumber() - x.get(i+1).getCardNumber() == 0)) {
                System.out.println("same");
                same = false;
            }
        }

        //royalflush
        if (x.size() ==5) {//checks for the royal flush from poker
            if(!(x.get(0).getCardNumber()==10) || !(x.get(0).getCardSuit().equals(x.get(1).getCardSuit()))
            || !(x.get(1).getCardNumber()==11) || !(x.get(1).getCardSuit().equals(x.get(2).getCardSuit()))
            || !(x.get(2).getCardNumber()==12) || !(x.get(2).getCardSuit().equals(x.get(3).getCardSuit()))
            || !(x.get(3).getCardNumber()==13) || !(x.get(3).getCardSuit().equals(x.get(4).getCardSuit()))
            || !(x.get(4).getCardNumber()==1)) {
                System.out.println("flush");
                royalFlush = false;
            }
        } else {
            royalFlush = false;
        }

        if (x.size()==3) {//checks for a 3,6,9 combo and a ace,5,10 combo
            if (!(x.get(0).getCardNumber()==3) || !(x.get(0).getCardSuit().equals(x.get(1).getCardSuit()))
               || !(x.get(1).getCardNumber()==6) || !(x.get(1).getCardSuit().equals(x.get(2).getCardSuit()))
               || !(x.get(2).getCardNumber()==9) ) {
                System.out.println("strap");
                strap = false;
            }

            if (!(x.get(0).getCardNumber()==1) || !(x.get(0).getCardSuit().equals(x.get(1).getCardSuit()))
               || !(x.get(1).getCardNumber()==5) || !(x.get(1).getCardSuit().equals(x.get(2).getCardSuit()))
               || !(x.get(2).getCardNumber()==10)) {
                System.out.println("quantum");
                quantumCombo = false;
            }
        } else {
            strap = false;
            quantumCombo = false;
        }

        if (x.size() == 2) {//checks if the first card is half that of the second card given
            if (!((double)(x.get(1).getCardNumber())/x.get(0).getCardNumber()==2.0) || !(x.get(0).getCardSuit().equals(x.get(1).getCardSuit()))) {
                System.out.println("double");
                doubleCombo =false;
            }
        } else {
            doubleCombo = false;
        }

        //vectorCombo
        if (x.size() == 4) {//checks for a ace,4,8,queen combo
            if (!(x.get(0).getCardNumber()==1) || !(x.get(0).getCardSuit().equals(x.get(1).getCardSuit()))
            || !(x.get(1).getCardNumber()==4) || !(x.get(1).getCardSuit().equals(x.get(2).getCardSuit()))
            || !(x.get(2).getCardNumber()==8) || !(x.get(2).getCardSuit().equals(x.get(3).getCardSuit()))
            || !(x.get(3).getCardNumber()==12)) {
                System.out.println("vector");
                vectorCombo = false;
            }
        } else {
            vectorCombo = false;
        }

        if (run || same || royalFlush || vectorCombo || strap || doubleCombo || quantumCombo || x.size()==1) {//if any of the combinations are true, it will
            if (editPer.equals("c")) {//the call was made for the client
                socket.sendMessage("discard Successful:");
            } else {//for server
                for (Card c: discHand) {//keeps track of hands
                    sHand.remove(c);
                }
                discardDone = true;
                turnUpdate();//progresses game
                imgDiscDrop.setImage(new Image("resources/discBack.jpg"));
            }
        } else {//not acceptable combination
            if (editPer.equals("c")) {
                socket.sendMessage("errorDisc:");
                cDiscHand.clear();
            } else {
                for (Card c: discHand) {//puts the discard back in the server's hand
                    sHand.add(c);
                }
                discHand.clear();
                updHand();
                updDiscHand();
                JOptionPane.showMessageDialog(null, "Your discard was either not complying any of the possible combinations or in wrong order");
            }
        }
    }

    //precondition:none
    //postcondition:creates a deck and distributes 10 cards to everyone
    @FXML
    public void dealCard() {
        if (cReady && sReady) {//checks if both server and client are ready
            for (ImageView x: cDiscImgs) {
                x.setImage(null);
            }
            gameOver =false;
            startPane.setVisible(false);
            deck.clear();
            sHand.clear();
            cHand.clear();
            for (int i = 1; i < 14; i++) {//creates the actual deck
                deck.add(new Card("C" + i));
                deck.add(new Card("D" + i));
                deck.add(new Card("H" + i));
                deck.add(new Card("S" + i));
            }

            int randInd;
            for (int i = 0; i < 10; i++) {//gives server and client 10 separate cards
                randInd = (int) (Math.random() * deck.size());
                sHand.add(deck.get(randInd));
                deck.remove(randInd);
                randInd = (int) (Math.random() * deck.size());
                cHand.add(deck.get(randInd));
                deck.remove(randInd);
            }

            String handMsg = "";
            for (Card x : cHand) {
                handMsg += x.getCardPath() + ",";
            }

            socket.sendMessage("cHand:" + handMsg);//gives the client his hand
            updHand();
        }
    }

    //precondition:none
    //postcondition:disables and enables imageviews that need to be disabled/enabled as game progresses
    public void turnUpdate() {
        if (discardDone) {//server has finished discard
            for (ImageView x : cDiscImgs) {//allows person to pick up another card as per the rules
                x.setDisable(false);
            }
            deckPickUp.setDisable(false);
            turnInstLbl.setText("Please pick a card from the deck or the server's discards");//instructions
            discardBtn.setDisable(true);
            declareBtn.setDisable(true);
            for (ImageView x : sImgs) {//can't change discard with this so no cheating
                x.setDisable(true);
            }
        }
        if (discardDone && cardPicked) {//when server will have finished his/her turn by also picking up a card
            for (int i =0;i<cDiscHand.size();i++) {//when server is done with picking cards, the client's remaining discard is put into the deck to cycle the deck
                deck.add(cDiscHand.remove(i));
            }
            discardDone = false;
            cardPicked = false;
            String msg="";
            for (Card x: discHand) {
                msg+=x.getCardPath()+",";
            }
            discHand.clear();
            socket.sendMessage("serverDone:"+msg);//sends the client the server's discard
            for (ImageView x : cDiscImgs) {//disables all imageviews and buttons because its client's turn
                x.setDisable(true);
            }
            for (ImageView x : sImgs) {
                x.setDisable(true);
            }

            imgDiscDrop.setDisable(true);
            deckPickUp.setDisable(true);
            turnInstLbl.setText("Wait for server");
        }
        if (clientDone) {//when client is done the imagviews with the first part (discard) will be enabled
            discHand.clear();
            clientDone =false;
            for (ImageView x : sImgs) {
                x.setDisable(false);
            }

            declareBtn.setDisable(false);
            discardBtn.setDisable(false);
            imgDiscDrop.setDisable(false);
            turnInstLbl.setText("Pick discards");
        }

        if (gameOver) {//when game is over everything is disabled
            for (ImageView x : cDiscImgs) {
                x.setDisable(true);
            }
            for (ImageView x : sImgs) {
                x.setDisable(true);
            }

            imgDiscDrop.setDisable(true);
            deckPickUp.setDisable(true);
        }
    }

    //precondition:none
    //postcondition: shows that server is ready
    @FXML
    public void serverReady() {
        sReady = true;
        dealCard();
        playAgainBtn.setVisible(false);
        declareLbl.setText("");

    }

    //precondition: none
    //postcondition: connects us to client
    @FXML
    public void handleConnectButton() {
        connectButton.setDisable(true);
        readyButton.setDisable(false);
        connect();
    }

    //precondition: none
    //postcondition: will update the hand of the server
    public void updHand() {
        try {
            for (int i =0;i<sImgs.size();i++) {
                sImgs.get(i).setImage(null);
                sImgs.get(i).setImage(new Image(sHand.get(i).getCardPath()));
            }
        }catch (Exception e) {}
    }

    //precondition: none
    //postcondition: updates the last card of the server's discard on the imagview and if not, a background
    public void updDiscHand() {
        try {
            if (discHand.size()>0) {
                imgDiscDrop.setDisable(false);
                imgDiscDrop.setImage(new Image(discHand.get(discHand.size()-1).getCardPath()));
            } else {
                declareBtn.setDisable(false);
                imgDiscDrop.setDisable(true);
                imgDiscDrop.setImage(new Image("resources/discBack.jpg"));
            }
        }catch (Exception e) {}
    }

    //precondition:none
    //postcondition: checks the sum of the people's cards and compares, person with lowest score wins
    @FXML
    public void declare() {
        int cSum = 0;
        int sSum = 0;
        playAgainBtn.setVisible(true);//gets ready if players want to play again
        cReady = false;
        sReady = false;
        for (Card c: cHand) {//client sum
            if (c.getCardNumber()>10) {
                cSum+=10;
            } else {
                cSum+=c.getCardNumber();
            }
        }
        for (Card c: sHand) {//server sum
            if (c.getCardNumber()>10) {
                sSum+=10;
            } else {
                sSum+=c.getCardNumber();
            }
        }

        if (cSum>sSum) {//server wins
            sSum = 0;//updates the label and tableview
            socket.sendMessage("serverWins:"+cSum);
            declareLbl.setText("SERVER WINS");
            data.add(new TableCellData(0,cSum));
        } else if (sSum>cSum) {//client wins
            cSum = 0;
            declareLbl.setText("CLIENT WINS");
            socket.sendMessage("clientWins:"+sSum);
            data.add(new TableCellData(sSum,0));
        } else {//tie
            declareLbl.setText("DRAW");
            socket.sendMessage("draw:"+sSum+","+cSum);
            data.add(new TableCellData(sSum,cSum));
        }
        roundInfoTbl.setItems(FXCollections.observableList(data));
        gameOver =true;
        turnUpdate();
    }

    //precondition:none
    //postcondition: calls the function to check the discard
    @FXML
    public void discard() {
        if (!(discHand.size()==0)) {
            checkDisc(discHand, "s");
        }
    }

    //precondition:MouseEvent for dragging
    //postcondition:drags an imageview
    @FXML
    public void dragDetected(MouseEvent event) {//onmousedrag handler
        ((ImageView)(event.getSource())).setTranslateX(startTranslateX + (event.getSceneX()-startX));
        ((ImageView)(event.getSource())).setTranslateY(startTranslateY + (event.getSceneY()-startY));
    }

    //precondition:MouseEvent for detecting the press
    //postcondition:initializes the variables for dragging and dropping each time imagview is pressed
    @FXML
    public void pressDetect(MouseEvent event) {//onmousepressed handler
        startX = event.getSceneX();
        startY = event.getSceneY();
        startTranslateX = ((ImageView) (event.getSource())).getTranslateX();
        startTranslateY = ((ImageView) (event.getSource())).getTranslateY();
    }

    //precondition: MouseEvent for checking if user wants to pick up from the deck
    //postcondition:uses rectangles to check for collision and then updates properly
    @FXML
    public void deckPickup(MouseEvent e) {
        Rectangle imgViewRect = new Rectangle(deckPickUp.getLayoutX()+((ImageView)(e.getSource())).getTranslateX(), deckPickUp.getLayoutY()+((ImageView)(e.getSource())).getTranslateY(),
                ((ImageView)(e.getSource())).getFitWidth(), ((ImageView)(e.getSource())).getFitHeight());
        if (imgViewRect.intersects(gPaneServer.getLayoutX(), gPaneServer.getLayoutY(), gPaneServer.getWidth(), gPaneServer.getHeight())) {//checks if the deck image collides with the server gridpane
            cardPicked = true;
            turnUpdate();
            for (ImageView x: cDiscImgs) {//gives new card and clears imagviews
                x.setImage(null);
            }
            sHand.add(deck.remove((int)(Math.random()*deck.size())));
            updHand();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

    //precondition: MouseEvent for checking if server wants one of the client's discards
    //postcondition:uses rectangles to check for collision and then updates properly
    @FXML
    public void getClientDiscard(MouseEvent e) {
        int imgNum = GridPane.getColumnIndex((ImageView)(e.getSource()));
        Rectangle imgViewRect = new Rectangle(gPaneClientDiscard.getLayoutX()+(60*imgNum)+((ImageView)(e.getSource())).getTranslateX(),gPaneClientDiscard.getLayoutY()+((ImageView)(e.getSource())).getTranslateY(),
                ((ImageView)(e.getSource())).getFitWidth(), ((ImageView)(e.getSource())).getFitHeight());
        if (imgViewRect.intersects(gPaneServer.getLayoutX(), gPaneServer.getLayoutY(), gPaneServer.getWidth(), gPaneServer.getHeight())) {//checks if a client discard collides with server's grid pane
            sHand.add(cDiscHand.remove(imgNum));
            updHand();
            for (ImageView x: cDiscImgs) {//clears imageviews
                x.setImage(null);
            }
            cardPicked = true;//progresses game
            turnUpdate();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

    //precondition: Mouseevent for putting a discard on the imageview
    //postcondition:uses rectangles to check for collision and then updates properly
    @FXML
    public void putDiscard(MouseEvent e) {//on mouse released handler for hand imageviews
        int imgNum = GridPane.getColumnIndex((ImageView) (e.getSource()));
        Rectangle imgViewRect = new Rectangle(gPaneServer.getLayoutX()+(60*imgNum)+((ImageView)(e.getSource())).getTranslateX(), gPaneServer.getLayoutY()+((ImageView)(e.getSource())).getTranslateY(), ((ImageView)(e.getSource())).getFitWidth(), ((ImageView)(e.getSource())).getFitHeight());
        if (imgViewRect.intersects(imgDiscDrop.getLayoutX(),imgDiscDrop.getLayoutY(),imgDiscDrop.getFitWidth(),imgDiscDrop.getFitHeight())) {//checks if a hand card of server collides with discard imageview
            declareBtn.setDisable(true);
            discHand.add(sHand.remove(imgNum));
            updHand();
            updDiscHand();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }

    //precondition://puts discard back onto the hand
    //postcondition:uses rectangles to check for collision and then updates properly
    @FXML
    public void undoDiscHand(MouseEvent e) {
        Rectangle imgViewRect = new Rectangle(imgDiscDrop.getLayoutX() + ((ImageView) (e.getSource())).getTranslateX(), imgDiscDrop.getLayoutY() + ((ImageView) (e.getSource())).getTranslateY(), ((ImageView) (e.getSource())).getFitWidth(), ((ImageView) (e.getSource())).getFitHeight());
        if (imgViewRect.intersects(gPaneServer.getLayoutX(), gPaneServer.getLayoutY(), gPaneServer.getWidth(), gPaneServer.getHeight())) {//discard imageview collides with hand imagview(s)
            sHand.add(discHand.remove(discHand.size()-1));
            updHand();
            updDiscHand();
        }
        ((ImageView) (e.getSource())).setTranslateX(startTranslateX);
        ((ImageView) (e.getSource())).setTranslateY(startTranslateY);
    }
}