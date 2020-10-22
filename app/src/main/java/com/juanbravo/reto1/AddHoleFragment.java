package com.juanbravo.reto1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddHoleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddHoleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "latitude";
    private static final String ARG_PARAM2 = "longitude";
    private static final String ARG_PARAM3 = "address";
    private static final String ARG_PARAM4 = "id";

    // TODO: Rename and change types of parameters
    private double latitude;
    private double longitude;
    private String address;
    private String userId;
    private Button confirmBtn;
    private TextView latitudeText;
    private TextView longitudeText;
    private TextView addressText;

    public AddHoleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddHoleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddHoleFragment newInstance(double latitude, double longitude, String address, String userId) {
        AddHoleFragment fragment = new AddHoleFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_PARAM1, latitude);
        args.putDouble(ARG_PARAM2, longitude);
        args.putString(ARG_PARAM3, address);
        args.putString(ARG_PARAM4, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_PARAM1);
            longitude = getArguments().getDouble(ARG_PARAM2);
            address = getArguments().getString(ARG_PARAM3);
            userId = getArguments().getString(ARG_PARAM4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_hole, container, false);

        latitudeText = view.findViewById(R.id.add_latitude_tv);
        longitudeText = view.findViewById(R.id.add_longitude_tv);
        addressText = view.findViewById(R.id.add_address_tv);
        confirmBtn = view.findViewById(R.id.add_confirm_btn);

        latitudeText.setText(String.valueOf(latitude));
        longitudeText.setText(String.valueOf(longitude));
        addressText.setText(address);

        confirmBtn.setOnClickListener(
                (view1) -> {
                    FragmentManager manager = requireActivity().getSupportFragmentManager();
                    Pothole pothole = new Pothole(UUID.randomUUID().toString(), latitude, longitude, userId);

                    Gson gson = new Gson();
                    String json = gson.toJson(pothole);
                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

                    new Thread(
                            () -> {
                                https.PUTrequest(Constants.BASEURL+"potholes/"+pothole.getId()+".json", json);
                            }
                    ).start();

                    manager.beginTransaction().remove(AddHoleFragment.this).commit();
            });


        return view;
    }
}