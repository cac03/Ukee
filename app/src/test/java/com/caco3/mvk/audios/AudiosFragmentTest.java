package com.caco3.mvk.audios;

import android.view.View;

import com.caco3.mvk.BuildConfig;
import com.caco3.mvk.R;
import com.caco3.mvk.vk.audio.Audio;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;

@RunWith(RobolectricTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = 22
)
public class AudiosFragmentTest {
  @Mock
  private AudiosPresenter presenter;
  private AudiosFragment fragment;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    fragment = new AudiosFragment();
    fragment.presenter = presenter;
    startFragment(fragment);
  }

  @Test
  public void showGlobalProgressCalled_progressBarShownAudiosContentHidden() {
    fragment.showGlobalProgress();
    assertTrue(fragment.progressBar.getVisibility() == View.VISIBLE);
    assertTrue(fragment.audiosContentView.getVisibility() == View.GONE);
  }

  @Test
  public void hideGlobalProgressCalled_progressBarHiddenAudiosContentShown() {
    fragment.hideGlobalProgress();
    assertTrue(fragment.progressBar.getVisibility() == View.GONE);
    assertTrue(fragment.audiosContentView.getVisibility() == View.VISIBLE);
  }

  @Test
  public void hideGlobalProgressCalledAfterShowGlobalProgress_progressBarHiddenAudiosContentShown() {
    fragment.showGlobalProgress();
    fragment.hideGlobalProgress();
    assertTrue(fragment.progressBar.getVisibility() == View.GONE);
    assertTrue(fragment.audiosContentView.getVisibility() == View.VISIBLE);
  }

  @Test
  public void showRefreshLayoutCalled_refreshLayoutIsRefreshing() {
    fragment.showRefreshLayout();
    assertTrue(fragment.swipeRefreshLayout.isRefreshing());
  }

  @Test
  public void hideRefreshLayoutCalled_refreshLayoutIsNotRefreshing() {
    fragment.hideRefreshLayout();
    assertTrue(!fragment.swipeRefreshLayout.isRefreshing());
  }

  @Test
  public void showAudiosCalled_adapterItemCountSameAsProvidedAudios() {
    List<Audio> audios = new ArrayList<>();
    audios.add(new Audio());
    audios.add(new Audio());
    fragment.showAudios(audios);
    int expected = audios.size();
    int actual = fragment.recyclerView.getAdapter().getItemCount();
    assertEquals(expected, actual);
  }

  @Test
  public void showNetworkIsUnavailableCalled_toastShown() {
    fragment.showNetworkIsUnavailableError();
    String expected = fragment.getString(R.string.network_is_unavailable);
    String actual = ShadowToast.getTextOfLatestToast();
    assertEquals(expected, actual);
  }

  @Test
  public void showNetworkErrorOccurredErrorCalled_toastShown() {
    fragment.showNetworkErrorOccurredError();
    String expected = fragment.getString(R.string.network_error_occurred);
    String actual = ShadowToast.getTextOfLatestToast();
    assertEquals(expected, actual);
  }
}
