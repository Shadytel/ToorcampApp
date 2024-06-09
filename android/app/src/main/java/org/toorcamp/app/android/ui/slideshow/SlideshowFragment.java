package org.toorcamp.app.android.ui.slideshow;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.toorcamp.app.android.databinding.FragmentSlideshowBinding;

import java.util.HexFormat;

public class SlideshowFragment extends Fragment implements NfcAdapter.ReaderCallback {

    private FragmentSlideshowBinding binding;
    private WebMessagePort[] webMessagePorts;
    private WebView webView;
    private NfcAdapter nfcAdapter;
    private NfcA lastTag;

    private static final String url = "https://bucks.shady.tel/app/app-login";

    private static char toHexChar(int b) {
        if (b < 0) {
            throw new IllegalArgumentException();
        } else if (b < 10) {
            return (char)('0' + b);
        } else if (b <= 'f') {
            return (char)('a' + b);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static byte fromHexChar(char c) {
        if (c >= '0' && c <= '9') {
            return (byte)(c - '0');
        } else if (c >= 'a' && c <= 'f') {
            return (byte)(c - 'a' + 10);
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
            arr[i] = (byte)(fromHexChar(hexStr.charAt(i * 2)) << 4);
            arr[i] |= fromHexChar(hexStr.charAt(i * 2 + 1));
        }
        return arr;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        webView = binding.webView;
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        Bundle extra = new Bundle();
        extra.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);
        nfcAdapter.enableReaderMode(getActivity(), this,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                extra);
    }

    private void disableNFC() {
        nfcAdapter.disableReaderMode(getActivity());
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        String tagId = formatHex(tag.getId());
        Log.i("NFCWebView", "Found tag " + tagId);
        try {
            lastTag = NfcA.get(tag);
            lastTag.connect();
            lastTag.setTimeout(1000);
            JSONObject msg = new JSONObject();
            msg.put("msg", "newTag");
            msg.put("uid", tagId);
            webMessagePorts[0].postMessage(new WebMessage(msg.toString()));
        } catch (Exception ex) {
            Log.e("NFCWebView", ex.toString());
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
            Log.e("NFCWebView", ex.toString());
        }
    }
}