import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.util.Collections;

public class Blackjack {
    private class Card{
        private String value;
        private String suit;

        public Card(String value, String suit){
            this.value = value;
            this.suit = suit;
        }

        @Override
        public String toString(){
            return this.value +"-"+this.suit;
        }

        public int getValue(){
            if("KQJ".contains(value)){
                return 10;
            } else if(value.equals("A")){
                return 11;
            }
            return Integer.parseInt(this.value);
        }

        public boolean isAce(){
            return value.equals("A");
        }

        public String getImagePath(){
            return "./cards/"+toString()+".png";
        }
    }

    private ArrayList<Card> deck;

    //dealer stuff
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //player stuff
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    //window
    int boardWidth = 600;
    int boardHeight = boardWidth;

    int cardWidth = 110; //1:1.4 ratio 
    int cardHeight = 154;

    boolean catActive = false;
    int catRoll = 0;

    Image catImg = new ImageIcon (getClass().getResource("./cards/bigcat.png")).getImage();

    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel(){
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            try{
                //chance to draw cat
                catRoll = ((int)(Math.random()*100));
                System.out.println("Cat Roll = " + catRoll);
                if (catRoll > 80) {
                    //draw cat
                    catActive = true;
                    g.drawImage(catImg,500,180,64,64,null);
                }
                //draw hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if(!stayButton.isEnabled()){
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg,20,20,cardWidth,cardHeight,null);

                //draw dealer's hand
                for(int i = 0; i<dealerHand.size();i++){
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg,cardWidth + 25 + (cardWidth + 5)*i,20,cardWidth,cardHeight,null);
                }

                //draw player's hand
                for(int i = 0; i<playerHand.size();i++){
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg,20+(cardWidth + 5)*i,320,cardWidth,cardHeight,null);
                }

                if(!stayButton.isEnabled()){
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();
                    System.out.println("STAY: ");
                    System.out.println("DEALER: " + dealerSum);
                    System.out.println("PLAYER: "+playerSum);

                    String message;

                    //if statements to update message is WIN, LOSE, PUSH(DRAW or TIE) based on score
                    if(playerSum > 21){
                        message = "Player busted";
                    } else if(dealerSum > 21){
                        message = "Dealer busted";
                    } else if(playerSum == dealerSum){
                        message = "Push";
                    } else if(playerSum > dealerSum){
                        message = "You win!";
                    }
                    else{
                        message = "Dealer wins";
                    }

                    g.setFont(new Font("Arial",Font.PLAIN,30));
                    g.setColor(Color.white);
                    g.drawString(message,220,250);

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton newGameButton = new JButton("New Game");

    public Blackjack(){
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53,101,77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        newGameButton.setFocusable(false);
        buttonPanel.add(newGameButton);
        frame.add(buttonPanel,BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce()? 1:0;
                playerHand.add(card);
                
                // if the player hand value is over 21
                // 1) they bust
                // 2) if they have an ace, subtract 10
                // check the above conditions and 
                // if they are above 21, the do hitButton.setEnabled(false)
                if(reducePlayerAce() > 21){
                    hitButton.setEnabled(false);
                    stayButton.setEnabled(false);
                }
                
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while(dealerSum < 17){
                    Card card = deck.get(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce()?1:0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });
        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                startGame();
                gamePanel.repaint();

            }
        });
        gamePanel.repaint();
    }

    public void startGame(){
        //deck
        buildDeck();
        shuffleDeck();

        //dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.removeLast();
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1:0;

        Card card = deck.removeLast();
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1:0;
        dealerHand.add(card);

        System.out.println("DEALER HAND");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);


        //player stuff
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;
        for(int i = 0; i < 2; i++){
            card = deck.removeLast();
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1:0;
            playerHand.add(card);
        }

        System.out.println("PLAYER HAND: ");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }

    public void buildDeck(){
        deck = new ArrayList<Card>();
        String[] values = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] suits = {"C","D","H","S"};

        // use some loop structure to make a deck of cards
        // 1) make Card object with (value, suit)
        // 2) add Card object to deck
        // do this for all value suit combos
        for(String value: values){
            for(String suit: suits){
                Card card = new Card(value,suit);
                deck.add(card);
            }
        }

        System.out.println("BUILD DECK: ");
        System.out.println(deck);
    }
    public void shuffleDeck(){
        Collections.shuffle(deck);
        /*
        Random random = new Random();
        for(int i = 0; i<deck.size(); i++){
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i,randomCard);
            deck.set(j,currCard);
        }*/
        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);

    }

    public int reducePlayerAce(){
        while(playerSum > 21 && playerAceCount >0){
            playerSum -= 10;
            playerAceCount -=1;
        }
        return playerSum;
    }

    public int reduceDealerAce(){
        while(dealerSum > 21 && dealerAceCount >0){
            dealerSum -= 10;
            dealerAceCount -=1;
        }
        return dealerSum;
    }

}
