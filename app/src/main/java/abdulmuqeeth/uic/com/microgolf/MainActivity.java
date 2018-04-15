package abdulmuqeeth.uic.com.microgolf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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

    Thread1 player1;
    Thread2 player2;

    Looper looper1;
    Looper looper2;

    Handler handler1;
    Handler handler2;

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


        player1 = new Thread1("player1");
        player2 = new Thread2("player2");

        player1.start();
        looper1 = player1.getLooper();
        handler1 = new Handler(looper1);

        player2.start();
        looper2 = player2.getLooper();
        handler2 = new Handler(looper2);

    }


    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(player1_win || player2_win){
                player1.quit();
                player2.quit();
                System.out.println("All threads quit cuz game won");
            }

            if(player1.isAlive()){
                System.out.println("player 1 alive");
                handler1.post(new Strategy1());
            } else{
                System.out.println("player 1 dead");
            }
            if(player2.isAlive()){
                System.out.println("player 2 alive");
                handler2.post(new Strategy2());
            } else{
                System.out.println("player 2 dead");
            }
        }
    };

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


    class Thread1 extends HandlerThread {
        Handler thread1Handler;

        public Thread1(String name){
            super (name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            thread1Handler = new Handler(getLooper()){
                public void handleMessageThread1(Message msg){
                    //TODO
                }
            };
        }
    }

    class Thread2 extends HandlerThread {
        Handler thread2Handler;

        public Thread2(String name){
            super (name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            thread2Handler = new Handler(getLooper()){
                public void handleMessageThread1(Message msg){
                    //TODO
                }
            };
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

            if(player2_previous_shots.size() < 10){
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
            } else{
                player2.quit();
                System.out.println("player 2 thread quit inside runnable");
                return;
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


