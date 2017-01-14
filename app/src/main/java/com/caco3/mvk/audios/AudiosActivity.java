package com.caco3.mvk.audios;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.caco3.mvk.R;
import com.caco3.mvk.ui.BaseActivity;


public class AudiosActivity extends BaseActivity {
  private static final String AUDIOS_FRAGMENT_TAG = "audios_frag";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);

    Fragment toHost = findFragment();
    if (toHost == null) {
      toHost = createFragment();
    }
    hostFragment(toHost);
  }

  @Nullable
  private Fragment findFragment() {
    return getSupportFragmentManager().findFragmentByTag(AUDIOS_FRAGMENT_TAG);
  }

  private Fragment createFragment() {
    Fragment fragment = new AudiosFragment();
    fragment.setRetainInstance(true);
    return fragment;
  }

  private void hostFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment, AUDIOS_FRAGMENT_TAG)
            .commitNow();
  }
}
