package sample;
public class Card {//each card has specific traits

    private String cSuit;//suit of the card
    private int cNumber;//number of the card
    private String pathName;//path of the card in the file resources
    private String cName;//name as a whole

    //precondition: name as a whole
    //postcondition: will initialize all the variables
    public Card(String cName)
    {
        pathName = "resources/" + cName + ".jpg";//creates the path
        this.cName = cName;
        cSuit = cName.substring(0,1); // creates the color of a card
        cNumber = Integer.parseInt(cName.substring(1)); // creates the value of a card
    }

    //precondition:none
    //postcondition:returns card number
    public int getCardNumber() // sends the card value when called
    {
        return cNumber;
    }

    //precondition: none
    //postcondition:returns card suit
    public String getCardSuit() // sends the card color when called
    {
        return cSuit;
    }

    //precondition:none
    //postcondition:returns the path of the card
    public String getCardPath() // sends card path when called/ loaction in resources
    {
        return pathName;
    }
}
