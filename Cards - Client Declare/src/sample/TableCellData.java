package sample;


import javafx.collections.ObservableArray;

public class TableCellData {

    private int serverPoints;
    private int clientPoints;

    public TableCellData(int sP, int cP) {
        serverPoints = sP;
        clientPoints = cP;
    }

    public int getServerPoints() {
        return serverPoints;
    }

    public int getClientPoints() {
        return clientPoints;
    }
}
