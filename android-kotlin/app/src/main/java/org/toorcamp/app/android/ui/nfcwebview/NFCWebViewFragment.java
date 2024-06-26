package org.toorcamp.app.android.ui.nfcwebview;

import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toorcamp.app.android.R;

public class NFCWebViewFragment extends Fragment implements NfcAdapter.ReaderCallback {

    private WebMessagePort[] webMessagePorts;
    private WebView webView;
    private NfcAdapter nfcAdapter;
    private NfcA lastTag;

    private static final String TAG = "NFCWebView";
    private static final String url = "https://bucks.shady.tel/";

    public NFCWebViewFragment() {
        super(R.layout.fragment_nfcwebview);
    }

    private static char toHexChar(int b) {
        if (b < 0) {
            throw new IllegalArgumentException();
        } else if (b < 10) {
            return (char) ('0' + b);
        } else if (b <= 'f') {
            return (char) ('a' + b - 10);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static byte fromHexChar(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (c - 'a' + 10);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String formatHex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(toHexChar((b & 0xf0) >> 4));
            sb.append(toHexChar(b & 0x0f));
        }
        return sb.toString();
    }

    private static byte[] parseHex(String hexStr) {
        byte[] arr = new byte[hexStr.length() / 2];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) (fromHexChar(hexStr.charAt(i * 2)) << 4);
            arr[i] |= fromHexChar(hexStr.charAt(i * 2 + 1));
        }
        return arr;
    }

    @Override
    @MainThread
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        webView = (WebView)view.findViewById(R.id.webView);
        WebView.setWebContentsDebuggingEnabled(true);
        // FIXME: WHY DO WE NEED THIS?!
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                initWebMessages();
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView = null;
        webMessagePorts = null;
        disableNFC();
    }

    private void initWebMessages() {
        webMessagePorts = webView.createWebMessageChannel();
        webMessagePorts[0].setWebMessageCallback(new WebMessagePort.WebMessageCallback() {
            @Override
            public void onMessage(WebMessagePort port, WebMessage message) {
                try {
                    JSONObject msg = new JSONObject(message.getData());
                    switch (msg.getString("msg")) {
                        case "enableNFC":
                            enableNFC();
                            break;
                        case "tagTransceive":
                            tagTransceive(msg);
                            break;
                        case "tagBulkSend":
                            tagBulkSend(msg);
                            break;
                    }
                } catch (JSONException ex) {
                    Log.e("NFCWebView", ex.toString());
                }
            }
        });
        webView.postWebMessage(new WebMessage("init:android",
                        new WebMessagePort[]{webMessagePorts[1]}),
                Uri.EMPTY);
    }

    private void enableNFC() {
        if (nfcAdapter != null) {
            Bundle extra = new Bundle();
            extra.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);
            nfcAdapter.enableReaderMode(getActivity(), this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    extra);
        } else {
            Log.w(TAG, "enableNFC called when there's no NFC adapter");
        }
    }

    private void disableNFC() {
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(getActivity());
        } else {
            Log.w(TAG, "disableNFC called when there's no NFC adapter");
        }
    }

    private void sendExceptionToWebApp(Exception ex) {
        try {
            JSONObject errMsg = new JSONObject();
            errMsg.put("msg", "exception");
            errMsg.put("str", ex.toString());
            webMessagePorts[0].postMessage(new WebMessage(errMsg.toString()));
        } catch (Exception ex2) {
            Log.e(TAG, "Cannot send exception to web app!");
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        String tagId = formatHex(tag.getId());
        Log.i(TAG, "Found tag " + tagId);
        try {
            lastTag = NfcA.get(tag);
            lastTag.connect();
            lastTag.setTimeout(1000);
            JSONObject msg = new JSONObject();
            msg.put("msg", "newTag");
            msg.put("uid", tagId);
            webMessagePorts[0].postMessage(new WebMessage(msg.toString()));
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            sendExceptionToWebApp(ex);
        }
    }

    private void tagTransceive(JSONObject msg) {
        try {
            byte[] txData = parseHex(msg.getString("txData"));
            // This is very naughty. We aren't supposed to call transceive() on the main thread.
            byte[] rxData = lastTag.transceive(txData);
            JSONObject resp = new JSONObject();
            resp.put("msg", "tagTransceiveResp");
            resp.put("rxData", formatHex(rxData));
            webMessagePorts[0].postMessage(new WebMessage(resp.toString()));
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            sendExceptionToWebApp(ex);
        }
    }

    private void tagBulkSend(JSONObject msg) {
        try {
            JSONArray cmds = msg.getJSONArray("cmds");
            JSONObject resp = new JSONObject();
            JSONArray cmdResps = new JSONArray();
            resp.put("msg", "tagBulkSendResp");
            resp.put("resps", cmdResps);
            for (int i = 0; i < cmds.length(); i++) {
                byte[] txData = parseHex(cmds.getString(i));
                byte[] rxData = lastTag.transceive(txData);
                cmdResps.put(formatHex(rxData));
            }
            webMessagePorts[0].postMessage(new WebMessage(resp.toString()));
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            sendExceptionToWebApp(ex);
        }
    }
}
