package org.toorcamp.app.android.ui.wificonfig;

import static android.provider.Settings.ACTION_WIFI_ADD_NETWORKS;
import static android.provider.Settings.EXTRA_WIFI_NETWORK_LIST;

import android.content.Intent;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.toorcamp.app.android.R;
import org.toorcamp.app.android.databinding.FragmentWificonfigBinding;
import org.toorcamp.app.android.ui.home.HomeViewModel;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class WiFiConfigFragment extends Fragment implements View.OnClickListener {
    private FragmentWificonfigBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentWificonfigBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /*
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
         */
        binding.setWifiConfigButton.setOnClickListener(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        try {

            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
            enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);

            if  (binding.wifiConfigPrivate.isChecked()) {
                enterpriseConfig.setIdentity("toor.sh");
                enterpriseConfig.setPassword("ireallywanttoputmycomputerontherawinternet");
            } else {
                enterpriseConfig.setIdentity("toorcamp");
                enterpriseConfig.setPassword("toorcamp");
            }
            enterpriseConfig.setAltSubjectMatch("DNS:conwifi.org");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = getResources().openRawResource(R.raw.cacert);
            X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(in);
            enterpriseConfig.setCaCertificate(caCert);

            WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                    .setSsid("ToorCamp")
                    .setWpa2EnterpriseConfig(enterpriseConfig).build();

            /*
            This UI is buggy on my phone.

            ArrayList<Parcelable> list = new ArrayList<Parcelable>();
            list.add(suggestion);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(EXTRA_WIFI_NETWORK_LIST, list);
            Intent intent = new Intent(ACTION_WIFI_ADD_NETWORKS);
            intent.putExtras(bundle);
            startActivity(intent);
             */

            WifiManager wifiManager = getActivity().getApplicationContext()
                    .getSystemService(WifiManager.class);
            wifiManager.addNetworkSuggestions(Arrays.asList(suggestion));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
