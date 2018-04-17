package abdulmuqeeth.uic.com.microgolf;

import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mainListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static final int PLAYER1_ID = 1;
    private static final int PLAYER2_ID = 2;

    private final static int JACKPOT = 101;
    private final static int NEAR_MISS = 102;
    private final static int NEAR_GROUP = 103;
    private final static int BIG_MISS = 104;
    private final static int CATASTROPHE = 105;

    HandlerThread player1;
    HandlerThread player2;

    Looper looper1;
    Looper looper2;

    Handler handler1;
    Handler handler2;

    Random rand = new Random();

    private Button startButton;
    private int winning;

    private int player1Shot;
    private int player2Shot;

    private int player2Group;
    private int winningGroup;

    private ArrayList<Integer> player1_previous_shots = new ArrayList<Integer>();
    private ArrayList<Integer> player2_previous_shots = new ArrayList<Integer>();

    private ArrayList<Integer> player2_all_groups = new ArrayList<Integer>();

    private boolean player1_win = false;
    private boolean player2_win = false;

    private int player1_outcome;
    private int player2_outcome;

    private int player1_lastshot = -1000;
    private int player2_lastshot = -1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainListView = (RecyclerView) findViewById(R.id.recycler_list);
        mainListView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mainListView.setLayoutManager(mLayoutManager);
        mainListView.setBackgroundColor(Color.parseColor("#EEEEEE"));

        ArrayList<String> holes = new ArrayList<>();

        for(int i=0; i<50 ; i++){
            holes.add("O");
        }

        // Defining the adapter
        mAdapter = new MyAdapter(this, holes);

        mainListView.setAdapter(mAdapter);

        //Fixing the winning hole
        winning = rand.nextInt(50);
        Log.i("UI","winning hole= "+winning);
        //Determining the group for winning shot
        winningGroup =  group(winning);

        Toast.makeText(getApplicationContext(), "Winning hole: "+winning, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "Winning Hole:Red  P1:Green  P2:Blue", Toast.LENGTH_SHORT).show();

        //First shot of player2 for determining group
        player2Shot = rand.nextInt(50);

        //Determining the group for player2
        player2Group = group(player2Shot);
        player2_all_groups.add(player2Group);
        System.out.println("player 2 group "+ player2Group);

        startButton = (Button) findViewById(R.id.start_button);

        startButton.setOnClickListener(startButtonListener);

        player1 = new HandlerThread("player1");
        player2 = new HandlerThread("player2");

        player1.start();
        looper1 = player1.getLooper();
        handler1 = new Handler(looper1) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                ready3 = false;
                ready4 = false;

                synchronized (lock2){
                    while(!ready2){
                        try{
                            Log.i("handler1","handler1 waiting for lock2 to be released by Strategy2 runnable");
                            lock2.wait();
                        } catch (InterruptedException e){
                            Log.i("handler1","InterruptedException in handler1");
                            return;}
                    }
                }

                //System.out.println("Lock2 released continuing handler1");

                if(!player2.isAlive()){
                    ready4=true;
                }
                if(!player2_win){
                    Log.i("handler1","Handling player1 message");
                    int outcome = msg.arg1;
                    //System.out.println("Player 1 outcome informed by UI thread is "+outcome);

                    if(outcome == CATASTROPHE) {
                        Log.i("handler1","Player2 won");
                        player2.quit();
                        player1.quit();
                        return;
                    }

                    if (outcome == JACKPOT){
                        player1_win = true;
                        player2.interrupt();
                        player1.quit();
                        player2.quit();
                    } else {
                        handler1.post(new Strategy1());
                        synchronized (lock3){
                            ready3 = true;
                            Log.i("handler1","lock3 released in handler1");
                            lock3.notify();
                        }
                    }
                }
            }
        };

        player2.start();
        looper2 = player2.getLooper();
        handler2 = new Handler(looper2){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                synchronized (lock3){
                    while(!ready3){
                        try{
                            Log.i("handler2","handler2 waiting for lock3 to be released by handler1");
                            lock3.wait();
                        } catch (InterruptedException e){
                            Log.i("handler2","Caught interrupted handler2");
                            return;
                        }
                    }
                }

                if(!player1_win){
                    Log.i("handler2","Handling player2 message");
                    int outcome = msg.arg1;
                    Log.i("handler2","Player 2 outcome informed by UI thread is "+outcome);
                    if(outcome == CATASTROPHE) {
                        System.out.println("Player1 won");
                        player1.quit();
                        player2.quit();
                        return;
                    }
                    if(outcome == JACKPOT){
                        player2_win = true;
                        Log.i("handler2","Player2 won cuz Jackpot, interrupting player1 and quitting threads");
                        player1.interrupt();
                        player2.quit();
                        player1.quit();
                        //System.out.println("Player2 won cuz Jackpot, QUITTED threads");
                    }else {
                        handler2.post(new Strategy2());
                        synchronized (lock4){
                            ready4 = true;
                            Log.i("handler2","lock4 released in handler2");
                            lock4.notify();
                        }
                    }
                }
            }
        };
    }

    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            //Changing color of winning hole to red
            mainListView.getChildAt(winning).setBackgroundColor(Color.parseColor("#FF0000"));

            try{
                Thread.sleep(500);
            } catch (InterruptedException e){
                System.out.println("Interrupted UI at beginning");
            }

            // modifyView(winning, "#FF0000");

            handler1.post(new Strategy1());

            handler2.post(new Strategy2());
        }
    };

    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            Message message;
            int what = msg.what;
            switch (what){
                case PLAYER1_ID:
                    //If player1 wins in the first shot
                    if(msg.arg1 == winning) {
                        player2.interrupt();
                    }
                    System.out.println("Inside UI handler for player1");
                    if(player1_lastshot != -1000){
                        modifyView(player1_lastshot, "#EEEEEE");
                    }

                    modifyView(msg.arg1, "#00FF00");
                    if(msg.arg1 == player2_lastshot){
                        Toast.makeText(getApplicationContext(), "CATASTROPHE", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Player 2 won", Toast.LENGTH_SHORT).show();
                        Log.i("UI Handler","Player2 won by catastrophe so interrupting player2");
                        player2.interrupt();
                        player1.interrupt();
                        player1.quit();
                        player2.quit();
                        break;
                    }

                    Toast.makeText(getApplicationContext(), "Player1 shot "+msg.arg1, Toast.LENGTH_SHORT).show();
                    player1_outcome = outcome(msg.arg1, "Player1");

                    message = handler1.obtainMessage(PLAYER1_ID);
                    message.arg1 = player1_outcome;

                    if(!player2_win){
                        handler1.sendMessage(message);
                    }

                    player1_lastshot = msg.arg1;
                    break;
                case PLAYER2_ID:
                    Log.i("UI Handler","inside ui handler player2");
                    Log.i("UI Handler","Player2 last shot "+player2_lastshot);

                    //System.out.println("next statement next statement");
                    if(player2_lastshot != -1000){
                        modifyView(player2_lastshot, "#EEEEEE");
                    }

                    modifyView(msg.arg1, "#0000FF");
                    if(msg.arg1 == player1_lastshot){
                        Toast.makeText(getApplicationContext(), "CATASTROPHE", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Player 1 won", Toast.LENGTH_SHORT).show();
                        Log.i("UI Handler","Player1 won by catastrophe so interrupting player1");
                        player1.interrupt();
                        player2.interrupt();
                        player1.quit();
                        player2.quit();
                        break;
                    }

                    Toast.makeText(getApplicationContext(), "Player2 shot "+msg.arg1, Toast.LENGTH_SHORT).show();
                    player2_outcome = outcome(msg.arg1, "Player2");
                    message = handler2.obtainMessage(PLAYER2_ID);
                    message.arg1 = player2_outcome;
                    Log.i("UI Handler","posting message to handler2");

                    if(!player2.isAlive()){
                        break;
                    }

                    if(!player1_win){
                        handler2.sendMessage(message);
                    }

                    player2_lastshot = msg.arg1;
                    break;
            }
        }
    };



    final Object lock = new Object();
    final Object lock2 = new Object();
    final Object lock3 = new Object();
    final Object lock4 = new Object();
    boolean ready;
    boolean ready2;
    boolean ready3;
    boolean ready4= true;

    private int temp;

    class Strategy1 implements Runnable{

        @Override
        public void run() {

            synchronized (lock4){
                while(!ready4){
                    try{
                        Log.i("Strategy1","Waiting for lock4 release in strategy1");
                        lock4.wait();
                    } catch (InterruptedException e){
                        Log.i("Strategy1","Caught interrupted strategy1");
                        return;}
                }
            }

            ready = false;

            if(!player1_previous_shots.isEmpty()){
                try{
                    player1.sleep(2000);
                }catch (InterruptedException e){
                    Log.i("Strategy1","Player1 sleep interrupted");
                }
            }

            //Determining player1 shot
            player1Shot = rand.nextInt(50);

            while(player1_previous_shots.contains(player1Shot)){
                player1Shot = rand.nextInt(50);
            }

            player1_previous_shots.add(player1Shot);

            //Passing the shot info to UI thread
            Message msg = uiHandler.obtainMessage(PLAYER1_ID);
            msg.arg1 = player1Shot;

            Log.i("Strategy1","player1Shot "+ player1Shot);

            uiHandler.sendMessage(msg);

            try{
                player1.sleep(2000);
            } catch (InterruptedException e){
                return;
            }

            synchronized (lock){
                ready = true;
                Log.i("Strategy1","lock released in strat1");
                lock.notify();
            }
        }
    }


    //Strategy to shoot in same group till all 10 holes exhaust them change the group randomly and continue the strategy
    class Strategy2 implements Runnable{

        @Override
        public void run() {

            ready2 = false;

            synchronized (lock){
                while(!ready){
                    try{
                        Log.i("Strategy2","Waiting for lock release in strat2");
                        lock.wait();
                    } catch (InterruptedException e){
                        Log.i("Strategy2","Caught interrupted strategy2");
                        synchronized (lock2){
                            ready2 = true;
                            lock2.notify();
                        }
                        return;
                    }
                }
            }

            ready = false;

            Log.i("Strategy2","lock released continuing strat2");

            try{
                //System.out.println("Sleeping player 2");
                player2.sleep(2000);
            } catch (InterruptedException e){
                Log.i("Strategy2","Player2 sleep interrupted");
                return;
            }

            Message msg = uiHandler.obtainMessage(PLAYER2_ID);


            //To change the group of player2 after all 10 possibilities have been exhausted
            if(player2_previous_shots.size() ==10){

                temp = 1+rand.nextInt(5);
                while(player2_all_groups.contains(temp)){
                    temp = 1+rand.nextInt(5);
                }
                player2Group = temp;
                player2_all_groups.add(player2Group);
                player2Shot = (player2Group-1)*10 + rand.nextInt(10);
                player2_previous_shots.clear();
                Log.i("Strategy2","new player2 group= "+player2Group);

            }





            if(player2_previous_shots.size() < 10){
                while(player2_previous_shots.contains(player2Shot)){

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
            }

            //****

            /*else{

                player2.quit();
                System.out.println("player 2 thread quit inside runnable");
                synchronized (lock2){
                    ready2 = true;
                    System.out.println("lock2 released after player2 quit in strat2");
                    lock2.notify();
                }
                return;
            }*/

            //****


            player2_previous_shots.add(player2Shot);

            Log.i("Strategy2", "player2Shot "+ player2Shot);
            //System.out.println("player2Shot "+ player2Shot);

            msg.arg1 = player2Shot;
            uiHandler.sendMessage(msg);

            try{
                Log.i("Strategy2", "Sleeping player2 thread");
                //System.out.println("Sleeping player2");
                player2.sleep(2000);
            } catch (InterruptedException e){
                return;
            }

            synchronized (lock2){
                ready2 = true;
                Log.i("Strategy2", "lock2 released in strat2");
               // System.out.println("lock2 released in strat2");
                lock2.notify();
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
            Toast.makeText(getApplicationContext(), player+" Won", Toast.LENGTH_SHORT).show();
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
    }

    //To determine if the shot was a near miss
    private boolean nearMiss(int group){
        return (group == winningGroup);
    }

    //To determine if the shot was a near group
    private boolean nearGroup(int group){
        return(Math.abs(group- winningGroup) == 1);
    }

    //To check if shot was a big miss
    private boolean bigMiss(int group) {
        return (Math.abs(group- winningGroup) != 1) ;
    }

    //To check if it is a catastrophe
    private boolean catastrophe(){
        return(player1Shot == player2_lastshot || player2Shot == player1_lastshot);
    }

    //Method to modify view
    private void modifyView(int position, String color){
        View view = mainListView.getChildAt(position);
        view.setBackgroundColor(Color.parseColor(color));
    }

}



