package sample;

public class TableCellData {//created for using a tableview with data

    private int serverPoints;//points server had for a game
    private int clientPoints;//points client had for a game

    //precondition: integers for each variable
    //postcondition: initializes the variables
    public TableCellData(int sP, int cP) {
        serverPoints = sP;
        clientPoints = cP;
    }

    //precondition:none
    //postcondition:returns the points of the server
    public int getServerPoints() {
        return serverPoints;
    }

    //precondition:none
    //postcondition: returns the points of the client
    public int getClientPoints() {
        return clientPoints;
    }
}
