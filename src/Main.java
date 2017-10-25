import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.util.Scanner;

// Card faces, in ascending order of value
enum CardFace { Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace };

// Card suits in alpha order
enum CardSuit { Club, Diamond, Heart, Spade }

// Game stages
enum GameStage { Deal, Draw, Score, End }

// Hand ranks, in ascending order of rank
enum HandRank { Unranked, Nothing, Pair, TwoPair, ThreeOfAKind, Straight, Flush, FullHouse, FourOfAKind, StraightFlush, RoyalFlush }

class Card { // Definition for a card
    // Holds card face
    public CardFace face;

    // Holds card suit
    public CardSuit suit;

    // Class constructor that takes a card face and suit
    public Card(CardFace cardFace, CardSuit cardSuit)
    {
        face = cardFace;
        suit = cardSuit;
    }
}

class CardDeck { // Definition for a deck of cards (52 card deck - no jokers)
    // Holds collection of cards
    public List<Card> cards = new ArrayList<Card>();

    // Constructor - creates the initial deck
    public CardDeck()
    {
        // Deck is created in order of suit, then face value
        for (CardSuit suit : CardSuit.values())
        {
            for (CardFace face : CardFace.values())
            {
                Card newCard = new Card(face, suit);
                cards.add(newCard);
            }
        }
    }

    // Shuffles the deck a single time
    public void Shuffle() {
        Random random = new Random();
        List<Card> shuffledCards = new ArrayList<Card>();
        // Loop until the new shuffled cards collection has 52 cards
        while (shuffledCards.size() < 52)
        {
            // Get random card number from 0-51 (cards collection is zero-based)
            int randomCardNum = random.nextInt(52);
            Card randomCard = cards.get(randomCardNum);
            // Only add this randomly chosen card if it is not already in the shuffled deck
            if (!shuffledCards.contains(randomCard))
            {
                shuffledCards.add(randomCard);
            }
        }
        // Replace original cards with these shuffled cards
        cards = shuffledCards;
    }

    // Shuffles the deck a specified number of times
    public void Shuffle(int numTimes) {
        for (int i = 1; i <= numTimes; i++)
        {
            Shuffle(); // Call single shuffle
        }
    }

    // Deal single card from top of deck to specified hand
    public Hand DealCard(Hand hand)
    {
        // Cards are dealt from the top of the deck
        Card dealtCard = cards.get(0);
        hand.cards.add(dealtCard);
        // Remove top card from deck since it is now in player hand
        cards.remove(0);
        return hand;
    }

    // Draw single card from top of deck and replace specified card (number) in specified hand
    public Hand DrawCard(Hand hand, int drawCard) {
        // Cards are dealt from the top of the deck, original card is discarded
        Card dealtCard = cards.get(0);
        // Discard original card in hand (zero-based)
        hand.cards.remove(drawCard - 1);
        // Insert new drawn card in hand at the same location
        hand.cards.add(drawCard - 1, dealtCard);
        // Remove top card from deck since it is now in player hand
        cards.remove(0);
        return hand;
    }
}

class Hand { // Definition for a card hand
    // Holds collection of cards
    public List<Card> cards = new ArrayList<Card>();

    // Holds hand rank - starts with being unranked
    public HandRank rank = HandRank.Unranked;

    // Holds face value of high card in rank
    public CardFace highCard = CardFace.values()[0];

    // Holds the player number associated with this hand
    public int playerNumber;

    // Returns multiple lines as a string with the card number and face value plus suit
    public String DisplayHand()
    {
        String returnHand = "";
        // Loop through collection of cards in hand (zero-based)
        for (int cardCount = 0; cardCount < cards.size(); cardCount++) {
            returnHand += "Card " + String.valueOf(cardCount+1) + ": " + cards.get(cardCount).face.toString() + " of " + cards.get(cardCount).suit.toString() + "s\n";
        }
        return returnHand;
    }

    // Private method to check rank - "Pair"
    private void CheckPair() {
        // Create hashmap of grouping by face value
        Map<Integer, List<Card>> tmpCards = cards.stream().collect(Collectors.groupingBy(w -> w.face.ordinal()));
        Set keys = tmpCards.keySet();
        // Iterate through all items in hashmap
        for (Iterator cardKey = keys.iterator(); cardKey.hasNext();) {
            Integer key = (Integer) cardKey.next();
            if (tmpCards.get(key).size() >= 2) { // Look for a count of 2 per face
                rank = HandRank.Pair;
                highCard = CardFace.values()[key]; // Face value of pair
            }
        }
    }

    // Private method to check rank - "Two Pair"
    private void CheckTwoPair() {
        // Holds the number of pairs that are found
        int numPairs = 0;
        // Holds the highest card found with a pair
        CardFace tempHigh = CardFace.Two;
        // Create hashmap of grouping by face value
        Map<Integer, List<Card>> tmpCards = cards.stream().collect(Collectors.groupingBy(w -> w.face.ordinal()));
        Set keys = tmpCards.keySet();
        // Iterate through all items in hashmap
        for (Iterator cardKey = keys.iterator(); cardKey.hasNext();) {
            Integer key = (Integer) cardKey.next();
            if (tmpCards.get(key).size() >= 2) { // Look for a count of 2 per face
                numPairs ++;
                if (key >= tempHigh.ordinal()) {
                    tempHigh = CardFace.values()[key]; // Set new high card
                }
            }
        }
        if (numPairs >= 2) { // Only set rank if we find 2 pairs
            rank = HandRank.TwoPair;
            highCard = tempHigh;
        }
    }

    // Private method to check rank - "Three of a Kind"
    private void CheckThreeOfAKind() {
        // Create hashmap of grouping by face value
        Map<Integer, List<Card>> tmpCards = cards.stream().collect(Collectors.groupingBy(w -> w.face.ordinal()));
        Set keys = tmpCards.keySet();
        // Iterate through all items in hashmap
        for (Iterator cardKey = keys.iterator(); cardKey.hasNext();) {
            Integer key = (Integer) cardKey.next();
            if (tmpCards.get(key).size() >= 3) { // Look for a count of 3 per face
                rank = HandRank.ThreeOfAKind;
                highCard = CardFace.values()[key]; // Face value of pair
            }
        }
    }

    // Private method to check rank - "Straight"
    private void CheckStraight() {
        // Holds the total difference in values between cards (cards are already sorted by value)
        int[] diffCount = new int[4];
        // Calculate difference in card values between cards
        for (int numCard = 1; numCard <= 4; numCard++) {
            diffCount[numCard] = cards.get(numCard).face.ordinal() - cards.get(numCard-1).face.ordinal();
        }
        if (diffCount[0] == 1 && diffCount[1] == 1 && diffCount[2] == 1 && diffCount[3] ==1) {
            rank = HandRank.Straight; // Each card is 1 away from the next card
        }
        if (cards.get(0).face == CardFace.Two && cards.get(1).face == CardFace.Three && cards.get(2).face == CardFace.Four && cards.get(3).face == CardFace.Five && cards.get(4).face == CardFace.Ace) {
            rank = HandRank.Straight; // Special case for Ace at the start of a straight
        }
        if (rank == HandRank.Straight) {
            highCard = cards.get(4).face; // Set high card
        }
    }

    // Private method to check rank - "Flush"
    private void CheckFlush() {
        // Create hashmap of grouping by face value
        Map<Integer, List<Card>> tmpCards = cards.stream().collect(Collectors.groupingBy(w -> w.face.ordinal()));
        Set keys = tmpCards.keySet();
        // Iterate through all items in hashmap
        for (Iterator cardKey = keys.iterator(); cardKey.hasNext();) {
            Integer key = (Integer) cardKey.next();
            if (tmpCards.get(key).size() >= 5) { // Look for a single grouping of all 5 cards
                rank = HandRank.Flush;
            }
        }
        if (rank == HandRank.Flush) {
            highCard = cards.get(4).face; // Set high card
        }
    }

    // Private method to check rank - "Full House"
    private void CheckFullHouse() {
        // Holds the number of two pair
        int numTwoPair = 0;
        // Holds the number of three pair
        int numThreePair = 0;
        // Holds the highest card found with a pair
        CardFace tempHigh = CardFace.Two;
        // Create hashmap of grouping by face value
        Map<Integer, List<Card>> tmpCards = cards.stream().collect(Collectors.groupingBy(w -> w.face.ordinal()));
        Set keys = tmpCards.keySet();
        // Iterate through all items in hashmap
        for (Iterator cardKey = keys.iterator(); cardKey.hasNext();) {
            Integer key = (Integer) cardKey.next();
            if (tmpCards.get(key).size() == 2) { // Look for a two pair
                numTwoPair ++;
            }
            if (tmpCards.get(key).size() == 3) { // Look for a three pair (use the three pair for high card)
                numThreePair ++;
                if (key >= tempHigh.ordinal()) {
                    tempHigh = CardFace.values()[key]; // Set new high card
                }
            }
        }
        if (numTwoPair == 1 && numThreePair == 1) { // Only set rank if we find a 2 and a 3 pair
            rank = HandRank.FullHouse;
            highCard = tempHigh;
        }
    }

    // Private method to check rank - "Four of a Kind"
    private void CheckFourOfAKind() {
        // Create hashmap of grouping by face value
        Map<Integer, List<Card>> tmpCards = cards.stream().collect(Collectors.groupingBy(w -> w.face.ordinal()));
        Set keys = tmpCards.keySet();
        // Iterate through all items in hashmap
        for (Iterator cardKey = keys.iterator(); cardKey.hasNext();) {
            Integer key = (Integer) cardKey.next();
            if (tmpCards.get(key).size() >= 4) { // Look for a count of 4 per face
                rank = HandRank.FourOfAKind;
                highCard = CardFace.values()[key]; // Face value of pair
            }
        }
    }

    // Private method to check rank - "Straight Flush" or "Royal Flush"
    private void CheckStraightFlush() {
        // First check to see if this is a flush at all
        CheckFlush();
        if (rank == HandRank.Flush) // If at least flush, check for straight or royal
        {
            // Holds the total difference in values between cards (cards are already sorted by value)
            int[] diffCount = new int[4];
            // Calculate difference in card values between cards
            for (int numCard = 1; numCard <= 4; numCard++) {
                diffCount[numCard] = cards.get(numCard).face.ordinal() - cards.get(numCard-1).face.ordinal();
            }
            if (cards.get(0).face == CardFace.Two && cards.get(1).face == CardFace.Three && cards.get(2).face == CardFace.Four && cards.get(3).face == CardFace.Five && cards.get(4).face == CardFace.Ace) {
                rank = HandRank.StraightFlush; // Special case for Ace at the start of a straight
            }
            if ((diffCount[0] == 1 && diffCount[1] == 1 && diffCount[2] == 1 && diffCount[3] ==1) && cards.get(4).face != CardFace.Ace){
                rank = HandRank.StraightFlush; // Standard straight flush
            }
            if ((diffCount[0] == 1 && diffCount[1] == 1 && diffCount[2] == 1 && diffCount[3] ==1) && cards.get(4).face == CardFace.Ace){
                rank = HandRank.RoyalFlush; // Ace at the end of a straight makes it a royal flush
            }
            if (rank == HandRank.StraightFlush || rank == HandRank.RoyalFlush) {
                highCard = cards.get(4).face; // Set high card
            }
        }
    }

    // Private method to sort cards based on face value
    private void SortCards() {
        Collections.sort(cards, new Comparator<Card>() { // Ascending
            public int compare(Card c1, Card c2) {
                if (c1.face.ordinal() > c2.face.ordinal()) return 1;
                if (c1.face.ordinal() < c2.face.ordinal()) return -1;
                return 0;
            }});
    }

    // Calculates the rank of the hand
    // First cards are sorted by face value, then each rank is "checked" in descending
    // order of rank (and it making a rank will keep it from any further checks)
    public final void GetRank()
    {
        // Sort cards by face value
        SortCards();

        if (rank == HandRank.Unranked) {
            CheckStraightFlush();
        }

        if (rank == HandRank.Unranked) {
            CheckFourOfAKind();
        }

        if (rank == HandRank.Unranked) {
            CheckFullHouse();
        }

        if (rank == HandRank.Unranked) {
            CheckFlush();
        }

        if (rank == HandRank.Unranked) {
            CheckStraight();
        }

        if (rank == HandRank.Unranked) {
            CheckThreeOfAKind();
        }

        if (rank == HandRank.Unranked) {
            CheckTwoPair();
        }

        if (rank == HandRank.Unranked) {
            CheckPair();
        }

        if (rank == HandRank.Unranked) { // If no rank is valud, then rank is "Nothing" and we just store the high card
            rank = HandRank.Nothing;
            highCard = cards.get(4).face; // Set high card
        }
    }
}

public class Main {
    // Returns the number of players based on input
    private static int GetPlayers() {
        // Holds the input as a string
        String getInput;
        // Holds the input coverted to an int
        int inputResult = 0;
        // Used by loop to continue to prompt until proper input
        boolean valid = false;
        do
        {
            System.out.print("Please input number of players (2-7): ");
            // Get user input
            getInput = new Scanner(System.in).nextLine();
            System.out.println("");
            try {
                inputResult = Integer.parseInt(getInput);
                if (inputResult >= 2 && inputResult <= 7) { // Only allow an int between 2 and 7
                    valid = true;
                }
            }
            catch (NumberFormatException e) {}
        } while (!valid);
        return inputResult;
    }

    // Returns a collection of ints that represent the cards chosen from players
    // hand for the draw
    private static ArrayList<Integer> GetDrawCards() {
        // Holds input as a string
        String getInput;
        // Holds collection of ints to be returned (start empty)
        ArrayList<Integer> returnDraw = new ArrayList<Integer>();
        // Get input
        getInput = new Scanner(System.in).nextLine();
        // Convert input to array of strings, split using ',' character
        String[] inputItems = getInput.split("[,]", -1);
        // Loop through array results
        for (int numItem = 0; numItem < inputItems.length; numItem++) {
            // Holds int value of element in the array
            int itemVal = 0;
            try {
                itemVal = Integer.parseInt(inputItems[numItem].trim());
                if (returnDraw.size() < 5) { // Only allow a maximum of 5 cards in draw - others are ignored
                    returnDraw.add(itemVal);
                }
            }
            catch (NumberFormatException e) {}
        }
        return returnDraw;
    }

    // Private method to sort hands descending by rank, then by high card, and then by player number
    private static List<Hand> OrderHands(List<Hand> passHands) {
        Collections.sort(passHands, new Comparator<Hand>() { // Descending
            public int compare(Hand h1, Hand h2) {
                if (h1.rank.ordinal() > h2.rank.ordinal()) return -1;
                if (h1.rank.ordinal() < h2.rank.ordinal()) return 1;
                if (h1.rank.ordinal() == h2.rank.ordinal()) {
                    if (h1.highCard.ordinal() > h2.highCard.ordinal()) return -1;
                    if (h1.highCard.ordinal() < h2.highCard.ordinal()) return 1;
                    if (h1.highCard.ordinal() == h2.highCard.ordinal()) {
                        if (h1.playerNumber > h2.playerNumber) return 1;
                        if (h1.playerNumber < h2.playerNumber) return -1;
                        return 0;
                    }
                }
                return 0;
            }});
        return passHands;
    }

    public static void main(String[] args) {
        System.out.println("Welcome to Poker!\n");

        // Get number of players
        int numPlayers = GetPlayers();

        // Create deck and shuffle it (3 times)
        CardDeck deck = new CardDeck();
        deck.Shuffle(3);

        // Tracks stage of game - Deal, Draw, Score and End
        GameStage gameStage = GameStage.Deal;

        // Create a collection of hands to represent each player
        ArrayList<Hand> hands = new ArrayList<Hand>();
        for (int playerHands = 1; playerHands <= numPlayers; playerHands++) {
            Hand newHand = new Hand();
            newHand.playerNumber = playerHands; // Holds the player number - used at scoring since these will be sorted
            hands.add(newHand);
        }

        // Main game loop
        do {
            switch (gameStage) {
                case Deal: // Deal cards to players
                    // Card loop - 5 cards
                    for (int numCards = 1; numCards <= 5; numCards++) {
                        // Player loop
                        for (int numPlayer = 1; numPlayer <= numPlayers; numPlayer++) {
                            // Hands collection is zero-based
                            hands.set(numPlayer - 1, deck.DealCard(hands.get(numPlayer - 1)));
                        }
                    }
                    System.out.println("\nAll hands are dealt\n");
                    // Display hands for each player
                    for (int numPlayer = 1; numPlayer <= numPlayers; numPlayer++) {
                        System.out.println("Player " + String.valueOf(numPlayer) + " hand:");
                        System.out.println(hands.get(numPlayer - 1).DisplayHand());
                    }
                    // Change game stage
                    gameStage = GameStage.Draw;
                    break;
                case Draw: // Allow each player to draw cards
                    System.out.println("\nNow time to choose draw\n");
                    // Redisplay hand for each player, then get input on which cards are part of the
                    // draw - input is card numbers seperated by commas
                    for (int numPlayer = 1; numPlayer <= numPlayers; numPlayer++) {
                        System.out.println("Player " + String.valueOf(numPlayer) + " hand:");
                        System.out.println(hands.get(numPlayer - 1).DisplayHand());
                        System.out.println("\nEnter the cards you would like to use in the draw");
                        System.out.print("(card numbers seperated by commas, hit enter for none): ");
                        // Gets a collection of ints representing the card numbers in the draw (zero-based)
                        ArrayList<Integer> drawCards = GetDrawCards();
                        for (int drawCard = 0; drawCard < drawCards.size(); drawCard++) {
                            // Only do draw if the input card is a valid card number (1-5)
                            if (drawCards.get(drawCard).compareTo(1) >= 0 && drawCards.get(drawCard).compareTo(5) <= 0) {
                                hands.set(numPlayer - 1, deck.DrawCard(hands.get(numPlayer - 1), drawCards.get(drawCard)));
                            }
                        }
                        System.out.println("");
                    }
                    // Change game stage
                    gameStage = GameStage.Score;
                    break;
                case Score: // Calculate score by ranking each hand for players
                    System.out.println("\nFinal hands of players\n");
                    // Redisplay hand for each player so that they see the results of the previous draw
                    // and can see both the rank of the hand and what the "high" card was for that rank
                    for (int numPlayer = 1; numPlayer <= numPlayers; numPlayer++) {
                        hands.get(numPlayer - 1).GetRank();
                        System.out.println("Player " + String.valueOf(numPlayer) + " hand:");
                        System.out.println(hands.get(numPlayer - 1).DisplayHand());
                        System.out.println("High Card: " + hands.get(numPlayer - 1).highCard.toString());
                        System.out.println("RANK: " + hands.get(numPlayer - 1).rank.toString() + "\n");
                    }
                    // Order hands based on descending rank, then desending high card, then player number
                    // This means there are no ties - a tie is broken by the player number (should it be?)
                    List<Hand> orderedHands = OrderHands(hands);

                    // Display winner player and that player's hand rank and high card
                    System.out.println("Winner is player " + String.valueOf(orderedHands.get(0).playerNumber));
                    System.out.println("with a rank of: " + orderedHands.get(0).rank.toString() + ", high card: " + orderedHands.get(0).highCard.toString());
                    // Change game stage
                    gameStage = GameStage.End;
                    break;
            }
        } while (gameStage != GameStage.End);

        // End of game
        System.out.println("\nThanks for playing!");
    }
}
