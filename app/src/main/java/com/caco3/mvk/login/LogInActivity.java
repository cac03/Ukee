package com.caco3.mvk.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.caco3.mvk.R;
import com.caco3.mvk.ui.BaseActivity;

public class LogInActivity extends BaseActivity {
  private static final String LOG_IN_FRAGMENT_TAG = "log_in_frag";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.log_in_activity);

    Fragment fragment = findFragment();
    if (fragment == null) {
      fragment = createFragment();
    }
    hostFragment(fragment);
  }

  private Fragment findFragment() {
    return getSupportFragmentManager().findFragmentByTag(LOG_IN_FRAGMENT_TAG);
  }

  private void hostFragment(Fragment fragment) {
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, LOG_IN_FRAGMENT_TAG)
            .commitNow();
  }

  private Fragment createFragment() {
    Fragment fragment = new LogInFragment();
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Override
  protected boolean hasParentActivity() {
    return false;
  }
}
