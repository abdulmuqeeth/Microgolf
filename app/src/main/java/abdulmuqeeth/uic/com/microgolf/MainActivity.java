package abdulmuqeeth.uic.com.microgolf;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int PLAYER1_ID = 1;
    private static final int PLAYER2_ID = 2;

    private Handler myHandler = new Handler(){
        public void handleMessage(Message msg){
            int what = msg.what;
                switch (what){
                    case PLAYER1_ID:
                        Toast.makeText(getApplicationContext(), "player 1 won", Toast.LENGTH_SHORT).show();
                        System.out.println("player 1 won");
                        break;
                    case PLAYER2_ID:
                        Toast.makeText(getApplicationContext(), "player 2 won", Toast.LENGTH_SHORT).show();
                        System.out.println("player 2 won");
                        break;
                }
        }
    };


    Random rand = new Random();

    private Button startButton;
    private int winning = 0;
    private int player1_shot = 0;

    private int player2_shot;

    private int group_player2;

    private ArrayList<Integer> player1_previous_shots = new ArrayList<Integer>();

    private ArrayList<Integer> player2_previous_shots = new ArrayList<Integer>();
    private boolean player1_win = false;

    private boolean player2_win = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fixing the winning hole
        winning = rand.nextInt(50);

        player2_shot = rand.nextInt(50);

        group_player2 = group(player2_shot);

        System.out.println("winning "+winning);
        System.out.println("player 2 group "+group_player2);


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

    private int group(int shot) {
        if(shot >=0 && shot <10){
            return 1;
        } else if(shot >=10 && shot <20){
            return 2;
        } else if(shot >=20 && shot <30){
            return 3;
        } else if(shot >=30 && shot <40){
            return 4;
        } else if(shot >=40 && shot <50){
            return 5;
        } else {
            return -1;
        }
    }
    class Strategy1 implements Runnable{

        @Override
        public void run() {
            Message msg = myHandler.obtainMessage(PLAYER1_ID);
            msg.arg1 = 10;


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
                myHandler.sendMessage(msg);
            }
        }
    }

    //Strategy to shoot in same group
    class Strategy2 implements Runnable{

        @Override
        public void run() {
            Message msg = myHandler.obtainMessage(PLAYER2_ID);
            msg.arg1 = 20;

            while(player2_previous_shots.contains(player2_shot)){
                System.out.println("matched item already in list");
                switch (group_player2){
                    case 1: player2_shot = rand.nextInt(10);
                            break;
                    case 2: player2_shot = 10 + rand.nextInt(10);
                            break;
                    case 3: player2_shot = 20 + rand.nextInt(10);
                            break;
                    case 4: player2_shot = 30 + rand.nextInt(10);
                            break;
                    case 5: player2_shot = 40 + rand.nextInt(10);
                            break;
                }
            }

            player2_previous_shots.add(player2_shot);

            System.out.println("player2_shot "+player2_shot);
            if(player2_shot == winning){
                player2_win = true;
                System.out.println("player 2 won");
                myHandler.sendMessage(msg);
            }
        }
    }

}


