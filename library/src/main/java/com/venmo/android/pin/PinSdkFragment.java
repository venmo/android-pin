package com.venmo.android.pin;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class PinSdkFragment extends Fragment implements PinFragment {

    private static final String KEY_FRAGMENT_VIEW_TYPE = "com.venmo.input_fragment_view_type";

    private PinListener mListener;
    private PinDisplayType mPinDisplayType;
    private BaseViewController mViewController;
    private PinFragmentConfiguration mConfig;
    private View mRootView;

    public static PinSdkFragment newInstanceForVerification() {
        return newInstanceForVerification(null);
    }

    public static PinSdkFragment newInstanceForVerification(PinFragmentConfiguration config) {
        return newInstance(PinDisplayType.VERIFY, config);
    }

    public static PinSdkFragment newInstanceForCreation() {
        return newInstanceForCreation(null);
    }

    public static PinSdkFragment newInstanceForCreation(PinFragmentConfiguration config) {
        return newInstance(PinDisplayType.CREATE, config);
    }

    private static PinSdkFragment newInstance(PinDisplayType type, PinFragmentConfiguration config) {
        PinSdkFragment instance = new PinSdkFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_FRAGMENT_VIEW_TYPE, type);
        instance.setArguments(bundle);
        instance.setConfig(config);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        mPinDisplayType = (PinDisplayType) args.getSerializable(KEY_FRAGMENT_VIEW_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.layout_pin_view, container, false);
        setDisplayType(mPinDisplayType);
        initViewController();
        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof PinListener)) {
            throw new ClassCastException(
                    "Hosting activity must implement PinFragment.Listener");
        } else {
            mListener = (PinListener) activity;
            if (mConfig == null) setConfig(new PinFragmentConfiguration(getActivity()));
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void setConfig(PinFragmentConfiguration config) {
        mConfig = config;
    }

    @Override
    public PinFragmentConfiguration getConfig() {
        return mConfig;
    }

    public void onPinCreationEntered(String pinEntry) {
        mPinDisplayType = PinDisplayType.CONFIRM;
        mViewController = new ConfirmPinViewController(this, mRootView, pinEntry);
    }

    public void setViewController(BaseViewController controller) {
        mViewController = controller;
    }

    public void notifyValid() {
        mListener.onValidated();
    }

    public void notifyCreated() {
        mListener.onPinCreated();
    }

    public void setDisplayType(PinDisplayType type) {
        mPinDisplayType = type;
    }

    private void initViewController() {
        switch (mPinDisplayType) {
            case VERIFY:
                setViewController(new VerifyPinViewController(this, mRootView));
                break;
            case CREATE:
                setViewController(new CreatePinViewController(this, mRootView));
                break;
            case CONFIRM:
                mViewController.refresh(mRootView);
                break;
            default:
                throw new IllegalStateException(
                        "Invalid DisplayType " + mPinDisplayType.toString());
        }
    }

}
