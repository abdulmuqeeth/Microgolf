package abdulmuqeeth.uic.com.microgolf;

import android.graphics.Color;
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

    private boolean gameOver = false;

    //Thread1 player1;
    //Thread2 player2;
    HandlerThread player1;
    HandlerThread player2;

    Looper looper1;
    Looper looper2;

    Handler handler1;
    Handler handler2;

    Random rand = new Random();

    private Button startButton;

    private int winning = 0;
    private int groupWinning;

    private int player1Shot;
    private int player1Group;

    private int player2Shot;
    private int player2Group;

    private ArrayList<Integer> player1_previous_shots = new ArrayList<Integer>();
    private ArrayList<Integer> player2_previous_shots = new ArrayList<Integer>();

    private boolean player1_win = false;
    private boolean player2_win = false;

    private int player1_outcome;
    private int player2_outcome;

    private final static int JACKPOT = 101;
    private final static int NEAR_MISS = 102;
    private final static int NEAR_GROUP = 103;
    private final static int BIG_MISS = 104;
    private final static int CATASTROPHE = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fixing the winning hole
        winning = rand.nextInt(50);
        //Determining the group for winning shot
        groupWinning =  group(winning);

        //First shot of player2 for determining group
        player2Shot = rand.nextInt(50);
        //Determining the group for player2
        player2Group = group(player2Shot);

        System.out.println("winning "+winning);

        Toast.makeText(getApplicationContext(), "Winning hole "+winning, Toast.LENGTH_SHORT).show();

        System.out.println("player 2 group "+ player2Group);

        startButton = (Button) findViewById(R.id.start_button);

        startButton.setOnClickListener(startButtonListener);


        //player1 = new Thread1("player1");
        //player2 = new Thread2("player2");

        player1 = new HandlerThread("player1");
        player2 = new HandlerThread("player2");

        player1.start();
        looper1 = player1.getLooper();
        handler1 = new Handler(looper1) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(!player2_win){
                    System.out.println("Handling player1 message");
                    int outcome = msg.arg1;
                    System.out.println("Player 1 outcome informed by UI thread is "+outcome);

                    if(outcome == CATASTROPHE) {
                        System.out.println("Player2 won");
                        player2.quit();
                        player1.quit();
                        return;
                    }

                    if (outcome == JACKPOT){
                        System.out.println("Player1 won");
                        player1.quit();
                        player2.quit();
                    } else {
                        handler1.post(new Strategy1());
                    }
                    /*if(player2_previous_shots.size() ==10){
                        handler1.post(new Strategy1());
                    }*/
                }
            }
        };

        player2.start();
        looper2 = player2.getLooper();
        handler2 = new Handler(looper2){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(!player1_win){
                    System.out.println("Handling player2 message");
                    int outcome = msg.arg1;
                    System.out.println("Player 2 outcome informed by UI thread is "+outcome);
                    if(outcome == CATASTROPHE) {
                        System.out.println("Player1 won");
                        player1.quit();
                        player2.quit();
                        return;
                    }
                    if(outcome == JACKPOT){
                        player2_win = true;
                        System.out.println("Player2 won");
                        player2.quit();
                        player1.quit();
                    }else {
                        handler2.post(new Strategy2());
                    }
                }
            }
        };

    }


    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            /*while(! (player1_win || player2_win)){
                System.out.println("Posting runnables");
                if(player1.isAlive()){
                    handler1.post(new Strategy1());
                }
                if(player2.isAlive()){
                    handler2.post(new Strategy2());
                }
            }
            System.out.println("out");
            player1.quit();
            player2.quit();
            System.out.println("All threads quit cuz game won");*/
            handler1.post(new Strategy1());

            handler2.post(new Strategy2());
            {
            //while(!(player1_win || player2_win)) {


                    /*try{
                        player1.sleep(2000);
                        //Thread.sleep(2000);
                    }catch (InterruptedException e){

                    }*/

                    //handler2.post(new Strategy2());
                    /*try{
                        Thread.sleep(2000);
                    }catch (InterruptedException e){

                    }*/
            }


            /*if(player1_win || player2_win){
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
            }*/
        }
    };

    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            Message message;
            int what = msg.what;
            switch (what){
                case PLAYER1_ID:
                    System.out.println("inside ui handler");

                    /*try{
                        player1.sleep(2000);
                    }catch (InterruptedException e){}*/

                    Toast.makeText(getApplicationContext(), "Player1 shot "+msg.arg1, Toast.LENGTH_SHORT).show();
                    player1_outcome = outcome(msg.arg1, "Player1");
                    message = handler1.obtainMessage();
                    message.arg1 = player1_outcome;
                    System.out.println("posting message to handler1: "+player1_outcome);
                    if(player1_outcome != CATASTROPHE){
                        if(!player2_win){
                            handler1.sendMessage(message);
                        }
                    } else{
                        Toast.makeText(getApplicationContext(), "Player2 won cuz catastrophe", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case PLAYER2_ID:
                    Toast.makeText(getApplicationContext(), "Player2 shot "+msg.arg1, Toast.LENGTH_SHORT).show();
                    player2_outcome = outcome(msg.arg1, "Player2");
                    message = handler2.obtainMessage(PLAYER2_ID);
                    message.arg1 = player2_outcome;
                    System.out.println("posting message to handler2");
                    if(player2_outcome != CATASTROPHE){
                        if(!player1_win){
                            handler2.sendMessage(message);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Player1 won cuz catastrophe", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }
    };

   /* class Thread1 extends HandlerThread {

        Handler mHandler;

        public Thread1(String name){
            super (name);
            System.out.println("Inside on thread1");
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            System.out.println("Inside on looper prepared");
            mHandler = new Handler(getLooper()){

                @Override
                public void handleMessage(Message msg) {

                    System.out.println("Handling player1 message");
                    super.handleMessage(msg);
                    int outcome = msg.arg1;
                    System.out.println("Player 1 outcome informed by UI thread is "+outcome);
                    //TODO
                }
            };
        }
    }*/


    /*class Thread2 extends HandlerThread {

        Handler mHandler;
        public Thread2(String name){
            super (name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            mHandler = new Handler(getLooper()){

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    int outcome = msg.arg1;
                    System.out.println("Player 1 outcome informed by UI thread is "+outcome);
                    //TODO
                }
            };
        }
    }*/

    final Object lock = new Object();
    boolean ready;

    class Strategy1 implements Runnable{

        @Override
        public void run() {

            try{
                player1.sleep(2000);
            }catch (InterruptedException e){

            }

            //Determining player1 shot
            player1Shot = rand.nextInt(50);
            //player1Group = group(player1Shot);


            while(player1_previous_shots.contains(player1Shot)){
                System.out.println("matched item already in list");
                player1Shot = rand.nextInt(50);
            }

            player1_previous_shots.add(player1Shot);

            //Passing the shot info to UI thread
            Message msg = uiHandler.obtainMessage(PLAYER1_ID);
            msg.arg1 = player1Shot;

            //Sleep maybe?

            //Retrieve response from UI thread

            System.out.println("player1Shot "+ player1Shot);

            uiHandler.sendMessage(msg);

            try{
                System.out.println("Sleeping");
                player1.sleep(2000);
            } catch (InterruptedException e){

            }

            System.out.println("response received");
            System.out.println("wokeup");

            synchronized (lock){
                ready = true;
                lock.notify();
            }

        }
    }


    //Strategy to shoot in same group
    class Strategy2 implements Runnable{

        @Override
        public void run() {

            synchronized (lock){
                while(!ready){
                    try{
                        lock.wait();
                    } catch (InterruptedException e){}
                }
            }

            try{
                player2.sleep(2000);
            }catch (InterruptedException e){

            }

            Message msg = uiHandler.obtainMessage(PLAYER2_ID);

            if(player2_previous_shots.size() < 10){
                while(player2_previous_shots.contains(player2Shot)){

                    System.out.println("matched item already in list");
                    switch (player2Group){
                        case 1: player2Shot = rand.nextInt(10);
                            break;
                        case 2: player2Shot = 10 + rand.nextInt(10);
                            break;
                        case 3: player2Shot = 20 + rand.nextInt(10);
                            break;
                        case 4: player2Shot = 30 + rand.nextInt(10);
                            break;
                        case 5: player2Shot = 40 + rand.nextInt(10);
                            break;
                    }
                }
            } else{
                player2.quit();
                System.out.println("player 2 thread quit inside runnable");
                return;
            }

            player2_previous_shots.add(player2Shot);

            System.out.println("player2Shot "+ player2Shot);

            msg.arg1 = player2Shot;
            uiHandler.sendMessage(msg);

            try{
                System.out.println("Sleeping");
                player2.sleep(2000);
            } catch (InterruptedException e){

            }
        }
    }

    //Method to determine the group of the shot
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

    //To determine outcome
    private int outcome(int shot , String player) {

        if (shot == winning) {
            Toast.makeText(getApplicationContext(), player+" Jackpot", Toast.LENGTH_SHORT).show();
            return JACKPOT;
        } else if(nearMiss(group(shot))) {
            Toast.makeText(getApplicationContext(), player+" Near Miss", Toast.LENGTH_SHORT).show();
            return NEAR_MISS;
        } else if(nearGroup(group(shot))) {
            Toast.makeText(getApplicationContext(), player+ "Near Group", Toast.LENGTH_SHORT).show();
            return NEAR_GROUP;
        } else if(bigMiss(group(shot))) {
            Toast.makeText(getApplicationContext(), player+ "Big Miss", Toast.LENGTH_SHORT).show();
            return BIG_MISS;
        } else if(catastrophe()) {
            Toast.makeText(getApplicationContext(), player+" catastrophe", Toast.LENGTH_SHORT).show();
            return CATASTROPHE;
        }
        else {
            return -1;
        }
        //TODO for catastrophe
    }

    //To determine if the shot was a near miss
    private boolean nearMiss(int group){
        if(group == groupWinning){
            return true;
        }else{
            return false;
        }
    }

    //To determine if the shot was a near group
    private boolean nearGroup(int group){
        if(Math.abs(group-groupWinning) == 1){
            return true;
        } else{
            return false;
        }
    }

    //To check if shot was a big miss
    private boolean bigMiss(int group) {
        //System.out.println(group-groupWinning);
        if(Math.abs(group-groupWinning) != 1){
            return true;
        } else{
            return false;
        }
    }

    //To check if it is a catastrophe
    private boolean catastrophe(){
        if(player1Shot == player2Shot){
            return true;
        }
        return false;
    }

}


