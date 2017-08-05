package school52.scoreboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class    MainActivity extends AppCompatActivity {
    private int hostScore;
    private int guestScore;
    private int remainingSeconds;
    private int defaultRemainingSeconds;
    private TimeManager timeManager;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View v = findViewById(R.id.remaining_seconds);

        timeManager = new TimeManager((TextView) v);
        View.OnLongClickListener numberEditor = new NumberEditor();

        findViewById(R.id.host_score).setOnLongClickListener(numberEditor);
        findViewById(R.id.guest_score).setOnLongClickListener(numberEditor);
        v.setOnLongClickListener(numberEditor);
        v.setOnClickListener(timeManager);

        settings = getSharedPreferences("ScoreBoardPrefs", 0);
        defaultRemainingSeconds = settings.getInt("remainingSeconds", 600);

        initAll();
        updateAll();
    }

    public void updateHostScore() {
        ((TextView) findViewById(R.id.host_score)).setText(String.valueOf(hostScore));
    }

    public void updateGuestScore() {
        ((TextView) findViewById(R.id.guest_score)).setText(String.valueOf(guestScore));
    }

    public void updateClock() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            TextView v = (TextView) findViewById(R.id.remaining_seconds);
            v.setText(String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60));
            }
        });
    }

    public int incLimit(int score, int inc) {
        if (score + inc <= 99&& score + inc >=0) {
            return score + inc;
        }
        return score;
    }

    public void updateAll() {
        updateHostScore();
        updateGuestScore();
        updateClock();
    }

    public void initAll() {
        hostScore = 0;
        guestScore = 0;
        remainingSeconds = defaultRemainingSeconds;
        timeManager.reset();
    }

    public void incrementScore(View v) {
        switch (v.getId()) {
            case R.id.incHost1: hostScore = incLimit(hostScore, 1); break;
            case R.id.incHost2: hostScore = incLimit(hostScore, -1); break;
            case R.id.incGuest1: guestScore = incLimit(guestScore, 1); break;
            case R.id.incGuest2: guestScore = incLimit(guestScore, -1); break;

        }
        updateAll();
    }

    public void reset(View v){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        initAll();
                        updateAll();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE: break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage("Шинээр эхлэх үү?").setPositiveButton("Тийм", dialogClickListener)
                .setNegativeButton("Үгүй", dialogClickListener).show();
    }

    private class TimeManager implements View.OnClickListener {
        Timer timer;
        TextView textView;

        TimeManager(TextView v) {
            this.textView = v;
            timer = null;
        }

        @Override
        public void onClick(View view) {
            toggle();
        }

        void reset() {
            stop();
            textView.setTextColor(Color.BLACK);
        }

        void toggle() {
            if (timer != null) {
                stop();
            } else {
                start();
            }
        }

        void stop() {
            if (timer != null) {
                timer.cancel();
                timer = null;
                textView.setTextColor(Color.RED);
            }
        }

        void start() {
            if (timer != null) {
                stop();
            }
            timer = new Timer();
            textView.setTextColor(Color.rgb(0, 100, 0));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (remainingSeconds <= 0) {
                        stop();
                        return;
                    }
                    remainingSeconds--;
                    updateClock();
                }
            }, 0, 1000);
        }
    }

    private class NumberEditor implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(final View v) {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.prompts, (ViewGroup)null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText userInput = (EditText) promptsView.findViewById(R.id.editText);
            TextView title = (TextView) promptsView.findViewById(R.id.editTextTitle);
            int limit = 3;
            switch (v.getId()) {
                case R.id.guest_score:
                case R.id.host_score:
                    userInput.setText(((TextView) v).getText());
                    title.setText("Шинэ оноо");
                    break;
                case R.id.remaining_seconds:
                    userInput.setText(String.valueOf(remainingSeconds));
                    title.setText("Шинэ хугацаа (сек)");
                    limit = 4;
                    break;
            }

            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
            userInput .setFilters(filters);
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Шинэчлэх", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,  int id) {
                            switch (v.getId()) {
                                case R.id.guest_score:
                                    guestScore = Integer.parseInt(userInput.getText().toString());
                                    updateGuestScore();
                                    break;
                                case R.id.host_score:
                                    hostScore = Integer.parseInt(userInput.getText().toString());
                                    updateHostScore();
                                    break;
                                case R.id.remaining_seconds:
                                    remainingSeconds = defaultRemainingSeconds = Integer.parseInt(userInput.getText().toString());
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putInt("remainingSeconds", defaultRemainingSeconds);
                                    editor.apply();
                                    updateClock();
                                    break;
                            }
                        }
                    })
                    .setNegativeButton("Болих", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            userInput.requestFocus();
            userInput.selectAll();
            return false;
        }
    }
}
