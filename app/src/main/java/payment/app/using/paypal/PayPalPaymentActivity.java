package payment.app.using.paypal;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import java.math.BigDecimal;
import java.util.Objects;

public class PayPalPaymentActivity extends AppCompatActivity {

    private static final String TAG = PayPalPaymentActivity.class.getSimpleName();

    private TextView productNameTextView,productDescriptionTextView,productPriceTextView;
    private ProgressBar loadingProgressBar;
    private LinearLayout paymentUsingPaypalButton;

    private PayPalConfiguration payPalConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initView();
        initObject();
        initEvent();
    }
    private void initView()
    {
        productNameTextView = findViewById(R.id.product_name);
        productPriceTextView = findViewById(R.id.product_price);
        productDescriptionTextView = findViewById(R.id.product_description);
        loadingProgressBar = findViewById(R.id.loading_payment);
        paymentUsingPaypalButton = findViewById(R.id.payment_using_paypal);
    }

    private void initObject()
    {
        initPayPalConfiguration();
        initService();
    }

    private void initEvent()
    {
        paymentUsingPaypalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuyPressed("1.00","USD","Men's Basic Polyester T-shirts");
            }
        });
    }

    private void initPayPalConfiguration()
    {
        payPalConfiguration = new PayPalConfiguration();
        /*
         * Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
         * live (ENVIRONMENT_PRODUCTION)
         */
        payPalConfiguration.environment(PayPalConfiguration.ENVIRONMENT_SANDBOX);
        payPalConfiguration.clientId(PayPalConfig.CLIENT_ID);
    }

    private void initService()
    {
        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         * PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         * PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         * later via calls from your server.
         */
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration);
        startService(intent);
    }

    private void onBuyPressed(String price,String currency, String productName)
    {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(price), currency, productName, PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PayPalConfig.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        /*
         * If the result is from PayPal
         */
        if (requestCode == PayPalConfig.REQUEST_CODE)
        {
            /*
             * If the result is OK i.e. user has not canceled the payment
             */
            if (resultCode == Activity.RESULT_OK)
            {
                /*
                 * Getting the payment confirmation
                 */
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                /*
                 * if confirmation is not null
                 */
                if (confirm != null)
                {
                    try
                    {

                        JSONObject jsonObject = new JSONObject(confirm.getPayment().toJSONObject().toString(4));
                        //JSONObject response = new JSONObject(jsonObject.getString("response"));

                        Log.e(TAG, confirm.toJSONObject().toString(4));
                        Log.e(TAG, confirm.getPayment().toJSONObject().toString(4));

                        /*
                         * Getting the payment details
                         */
                        new AlertDialog.Builder(PayPalPaymentActivity.this)
                                .setTitle("Success")
                                .setMessage(confirm.toJSONObject().toString(4)+"\n"+jsonObject)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        /*
                                         * continue with delete
                                         */
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        /*
                                         * do nothing
                                         */
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();

                        /*
                         * TODO: send 'confirm' to your server for verification.
                         * see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         */

                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                Log.i(TAG, "The user canceled.");
            }
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
            {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart() call");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart() call");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause() call");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume call");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop() call");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy() call");
        stopService(new Intent(this, PayPalService.class));
    }
}
