package abdulmuqeeth.uic.com.microgolf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private Button startButton;
    private int winning = 0;
    private int player1_shot = 0;
    private int player2_shot = 0;
    private ArrayList<Integer> player1_previous_shots = new ArrayList<Integer>();
    private ArrayList<Integer> player2_previous_shots = new ArrayList<Integer>();

    private boolean player1_win = false;
    private boolean player2_win = false;

    Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fixing the winning hole
        winning = rand.nextInt(50);

        System.out.println("winning "+winning);

        startButton = (Button) findViewById(R.id.start_button);

        startButton.setOnClickListener(startButtonListener);

    }


    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        Thread player1 = new Thread(new Strategy1());
        Thread player2 = new Thread(new Strategy2());
        player1.start();
        player2.start();
        }
    };

    class Strategy1 implements Runnable{

        @Override
        public void run() {
            player1_shot = rand.nextInt(50);

            while(player1_previous_shots.contains(player1_shot)){
                System.out.println("matched item already in list");
                player1_shot = rand.nextInt(50);
            }

            player1_previous_shots.add(player1_shot);

            System.out.println("player1_shot "+player1_shot);
            if(player1_shot == winning){
                player1_win = true;
                System.out.println("player 1 won");
            }
        }
    }

    class Strategy2 implements Runnable{

        @Override
        public void run() {
            player2_shot = rand.nextInt(50);

            while(player2_previous_shots.contains(player2_shot)){
                System.out.println("matched item already in list");
                player2_shot = rand.nextInt(50);
            }

            player2_previous_shots.add(player2_shot);

            System.out.println("player2_shot "+player2_shot);
            if(player2_shot == winning){
                player2_win = true;
                System.out.println("player 2 won");
            }
        }
    }

}


