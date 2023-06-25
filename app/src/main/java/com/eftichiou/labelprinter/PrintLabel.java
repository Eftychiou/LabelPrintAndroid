package com.eftichiou.labelprinter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.bixolon.labelprinter.BixolonLabelPrinter;
import com.bixolon.labelprinter.R;
import com.eftichiou.labelprinter.models.Data;
import com.eftichiou.labelprinter.models.Item;
import com.eftichiou.labelprinter.shared.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PrintLabel extends Activity {
    private TextView description;
    private TextView price;
    private TextView stock;
    private TextView measure;
    private Data data;
    private Item item;
    private String measureMsg;
    private RadioGroup radioGroup;
    private EditText size;
    private EditText quantity;
    private Button searchBtn;
    private EditText barcodeText;
    private Tools tools;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_label);
        Button cleanBtn = findViewById(R.id.clean);
        Button printBtn = findViewById(R.id.print);
        searchBtn = findViewById(R.id.search);
        barcodeText = findViewById(R.id.barcode);
        radioGroup = findViewById(R.id.units);
        description = findViewById(R.id.description);
        price = findViewById(R.id.price);
        stock = findViewById(R.id.stock);
        measure = findViewById(R.id.measure);
        quantity = findViewById(R.id.quantity);
        size = findViewById(R.id.size);
        tools = new Tools(this);
        MainActivity.mBixolonLabelPrinter.setCharacterSet(BixolonLabelPrinter.INTERNATIONAL_CHARACTER_SET_USA,
                BixolonLabelPrinter.CODE_PAGE_WCP1253_GREEK);
        MainActivity.mBixolonLabelPrinter.setCutterPosition(0);
        quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && item != null && item.getPrice() != null) {
                    generateMeasureMessage();
                    measure.setText(measureMsg);
                }
            }
        });
        size.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && item != null && item.getPrice() != null) {
                    generateMeasureMessage();
                    measure.setText(measureMsg);
                }
            }
        });
        cleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearViews();
            }
        });
        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.mBixolonLabelPrinter.isConnected()) {
                    showMessage("Bluetooth not connected");
                    tools.playFailSound();
                    return;
                }
                if (item != null) {
                    tools.playSound();
                    MainActivity.mBixolonLabelPrinter.beginTransactionPrint();
                    MainActivity.mBixolonLabelPrinter.drawText(item.getDescription(), 320, 15,
                            BixolonLabelPrinter.FONT_SIZE_18, 1, 1, 0, BixolonLabelPrinter.ROTATION_90_DEGREES, false
                            , false, BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
                    MainActivity.mBixolonLabelPrinter.draw1dBarcode(item.getBarcode(), 180, 15,
                            BixolonLabelPrinter.BARCODE_EAN13, 3, 5, 90, BixolonLabelPrinter.ROTATION_90_DEGREES,
                            BixolonLabelPrinter.HRI_BELOW_FONT_SIZE_3, 5);
                    MainActivity.mBixolonLabelPrinter.drawText("€" + String.format(java.util.Locale.US, "%.2f",
                                    item.getPrice()), 180, 335, BixolonLabelPrinter.FONT_SIZE_14, 1, 1, 2,
                            BixolonLabelPrinter.ROTATION_90_DEGREES, false, false,
                            BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
                    MainActivity.mBixolonLabelPrinter.drawText(measureMsg, 110, 335, BixolonLabelPrinter.FONT_SIZE_10
                            , 1, 1, 0, BixolonLabelPrinter.ROTATION_90_DEGREES, false, false,
                            BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
                    MainActivity.mBixolonLabelPrinter.print(1, 1);
                    MainActivity.mBixolonLabelPrinter.endTransactionPrint();
                    clearViews();
                } else {
                    showMessage("Scan an item first");
                    tools.playFailSound();
                }
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                try {
                    if (barcodeText.getText().toString().trim().equals("")) {
                        tools.playFailSound();
                        showMessage("Scan barcode");
                        return;
                    }
                    item = data.findItem(barcodeText.getText().toString().trim());
                    if (item != null) {
                        generateMeasureMessage();
                        String thePrice = "€" + String.format(java.util.Locale.US, "%.2f", item.getPrice());
                        description.setText(item.getDescription());
                        price.setText(thePrice);
                        stock.setText(item.getStock() + " stock");
                        measure.setText(measureMsg);
                        tools.playSound();
                    } else {
                        tools.playFailSound();
                        showMessage("Item not found");//
                    }
                } catch (Exception e) {
                    tools.playFailSound();
                    showMessage("Item not found");
                    e.printStackTrace();
                }
            }
        });
        barcodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //do what you want on the press of 'done'
                    searchBtn.performClick();
                }
                return false;
            }
        });
        //------------------------Event Listeners Finished
        File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filename = "data.json";
        FileReader fr;
        File myExternalFile = new File(filepath, filename);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fr = new FileReader(myExternalFile);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = br.readLine();
            }
            String json = stringBuilder.toString();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Data loadData = objectMapper.readValue(json, Data.class);
                System.out.println(loadData.toString());
                data = loadData;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void generateMeasureMessage() {
        int radioId = radioGroup.getCheckedRadioButtonId();
        Integer theQuantity;
        double theSize;
        if (!quantity.getText().toString().trim().equals("")) {
            theQuantity = Integer.valueOf(quantity.getText().toString().trim());
        } else {
            theQuantity = 1;
        }
        if (!size.getText().toString().trim().equals("")) {
            theSize = Double.parseDouble(size.getText().toString().trim());
        } else {
            theSize = 1.0;
        }
        double calculation = (item.getPrice() * theQuantity) / theSize;
        String calculationStringed = "€" + String.format(java.util.Locale.US, "%.2f", calculation) + "/" + theQuantity;
        switch (radioId) {
            case R.id.pcs:
                measureMsg = calculationStringed + " pcs";
                break;
            case R.id.ml:
                measureMsg = calculationStringed + " ml";
                break;
            case R.id.g:
                measureMsg = calculationStringed + " g";
                break;
            case R.id.mz:
                measureMsg = calculationStringed + " m²";
                break;
        }
    }

    public void clearViews() {
        tools.playSound();
        description.setText("");
        barcodeText.setText("");
        price.setText("");
        stock.setText("");
        quantity.setText("");
        size.setText("");
        measure.setText("");
    }

    public void showMessage(String msg) {
        Toast.makeText(PrintLabel.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void checkButton(View v) {
        tools.playSound();
        if (item != null) {
            generateMeasureMessage();
            measure.setText(measureMsg);
        }
    }
}
