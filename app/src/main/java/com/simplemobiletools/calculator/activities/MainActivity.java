package com.simplemobiletools.calculator.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.simplemobiletools.calculator.Calculator;
import com.simplemobiletools.calculator.CalculatorImpl;
import com.simplemobiletools.calculator.Config;
import com.simplemobiletools.calculator.Constants;
import com.simplemobiletools.calculator.Formatter;
import com.simplemobiletools.calculator.R;
import com.simplemobiletools.calculator.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import me.grantland.widget.AutofitHelper;

public class MainActivity extends SimpleActivity implements Calculator {
    @BindView(R.id.result) TextView mResult;
    @BindView(R.id.formula) TextView mFormula;

    private static CalculatorImpl mCalc;
    private InterstitialAd mInterstitialAd;
    private long AdInterTs;
    private long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCalc = new CalculatorImpl(this);
        AutofitHelper.create(mResult);
        AutofitHelper.create(mFormula);

        this.AdInterTs = System.currentTimeMillis();
        this.count = 0;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7366328858638561/4952581935");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Log.d("hahaha", "ad closed");
            }
        });
        requestNewInterstitial();

        showInterAd();
    }

    private void requestNewInterstitial() {
        //Log.d("hahaha", AdRequest.DEVICE_ID_EMULATOR);
        AdRequest adRequest = new AdRequest.Builder().build();

        /*
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        */
        mInterstitialAd.loadAd(adRequest);
    }


    public void showInterAd() {
        long ts = System.currentTimeMillis();
        long elapsed = ts - this.AdInterTs;
        this.count++;
        if (elapsed > 3 * 60 * 1000 || this.count % 20 == 0){
            this.AdInterTs = ts;
        }
        else{
            Log.d("hahaha", "time too short " + elapsed/1000 + " count:" + this.count);
            return;
        }
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            Log.d("hahaha", "ad 111 ready");
        }
        else{
            Log.d("hahaha", "ad 222 not ready");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.newInstance(getApplicationContext()).setIsFirstRun(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showInterAd();
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.btn_plus)
    public void plusClicked() {
        mCalc.handleOperation(Constants.PLUS);
        showInterAd();
    }

    @OnClick(R.id.btn_minus)
    public void minusClicked() {
        mCalc.handleOperation(Constants.MINUS);
        showInterAd();
    }

    @OnClick(R.id.btn_multiply)
    public void multiplyClicked() {
        mCalc.handleOperation(Constants.MULTIPLY);
        showInterAd();
    }

    @OnClick(R.id.btn_divide)
    public void divideClicked() {
        mCalc.handleOperation(Constants.DIVIDE);
        showInterAd();
    }

    @OnClick(R.id.btn_modulo)
    public void moduloClicked() {
        mCalc.handleOperation(Constants.MODULO);
        showInterAd();
    }

    @OnClick(R.id.btn_power)
    public void powerClicked() {
        mCalc.handleOperation(Constants.POWER);
        showInterAd();
    }

    @OnClick(R.id.btn_root)
    public void rootClicked() {
        mCalc.handleOperation(Constants.ROOT);
        showInterAd();
    }

    @OnClick(R.id.btn_clear)
    public void clearClicked() {
        mCalc.handleClear();
        showInterAd();
    }

    @OnLongClick(R.id.btn_clear)
    public boolean clearLongClicked() {
        mCalc.handleReset();
        showInterAd();
        return true;
    }

    @OnClick(R.id.btn_equals)
    public void equalsClicked() {
        mCalc.handleEquals();
        showInterAd();
    }

    @OnClick({R.id.btn_decimal, R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8,
            R.id.btn_9})
    public void numpadClick(View view) {
        numpadClicked(view.getId());
        showInterAd();
    }

    public void numpadClicked(int id) {
        mCalc.numpadClicked(id);
    }

    @OnLongClick(R.id.formula)
    public boolean formulaLongPressed() {
        return copyToClipboard(false);
    }

    @OnLongClick(R.id.result)
    public boolean resultLongPressed() {
        return copyToClipboard(true);
    }

    private boolean copyToClipboard(boolean copyResult) {
        String value = mFormula.getText().toString().trim();
        if (copyResult) {
            value = mResult.getText().toString().trim();
        }

        if (value.isEmpty())
            return false;

        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(getResources().getString(R.string.app_name), value);
        clipboard.setPrimaryClip(clip);
        Utils.showToast(getApplicationContext(), R.string.copied_to_clipboard);
        return true;
    }

    @Override
    public void setValue(String value) {
        mResult.setText(value);
    }

    // used only by Robolectric
    @Override
    public void setValueDouble(double d) {
        mCalc.setValue(Formatter.doubleToString(d));
        mCalc.setLastKey(Constants.DIGIT);
    }

    public void setFormula(String value) {
        mFormula.setText(value);
    }

    public CalculatorImpl getCalc() {
        return mCalc;
    }
}
